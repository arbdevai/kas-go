const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");
const { onDocumentCreated, onDocumentUpdated } = require("firebase-functions/v2/firestore");
const logger = require("firebase-functions/logger");

initializeApp();
const db = getFirestore();

async function sendToMembers(orgId, memberUids, title, body, data) {
  const uids = [...new Set(memberUids.filter(Boolean))];
  if (uids.length === 0) return;
  const deviceDocs = await db.collection(`organizations/${orgId}/notification_tokens`)
    .where("member_uid", "in", uids.slice(0, 30))
    .get();
  const docs = deviceDocs.docs.filter((doc) => typeof doc.get("fcm_token") === "string");
  if (docs.length === 0) return;

  const response = await getMessaging().sendEachForMulticast({
    tokens: docs.map((doc) => doc.get("fcm_token")),
    notification: { title, body },
    data: Object.fromEntries(Object.entries(data).map(([key, value]) => [key, String(value)])),
    android: { priority: "high", notification: { channelId: "kas_go_updates" } },
  });
  const stale = [];
  response.responses.forEach((result, index) => {
    const code = result.error?.code;
    if (code === "messaging/registration-token-not-registered" ||
        code === "messaging/invalid-registration-token") stale.push(docs[index].ref.delete());
    if (!result.success) logger.warn("FCM delivery failed", { code, orgId });
  });
  await Promise.all(stale);
}

async function admins(orgId) {
  const members = await db.collection(`organizations/${orgId}/members`).get();
  return members.docs
    .filter((doc) => doc.get("status") === "active" && /^admin[1-3]$/.test(doc.get("role") || ""))
    .map((doc) => doc.id);
}

exports.notifyPendingMember = onDocumentCreated(
  { document: "organizations/{orgId}/members/{uid}", region: "asia-southeast2" },
  async (event) => {
    const member = event.data?.data();
    if (member?.status !== "pending") return;
    await sendToMembers(event.params.orgId, await admins(event.params.orgId),
      "Pendaftaran warga baru", `${member.name || "Warga"} menunggu persetujuan pengurus.`,
      { type: "pending_member", uid: event.params.uid });
  }
);

exports.notifyPaymentStatus = onDocumentUpdated(
  { document: "organizations/{orgId}/bill_entries/{entryId}", region: "asia-southeast2" },
  async (event) => {
    const before = event.data?.before.data();
    const after = event.data?.after.data();
    if (!before || !after || before.status === after.status) return;
    const orgId = event.params.orgId;
    const entryId = event.params.entryId;
    if (after.status === "menunggu_verifikasi" && before.status === "belum_bayar") {
      await sendToMembers(orgId, await admins(orgId), "Pembayaran perlu diverifikasi",
        `${after.member_name || "Warga"} mengirim konfirmasi pembayaran ${after.period || "iuran"}.`,
        { type: "payment_verification", entry_id: entryId });
    } else if (after.status === "lunas" && before.status === "menunggu_verifikasi") {
      await sendToMembers(orgId, [after.member_uid], "Pembayaran sudah diverifikasi",
        `Pembayaran ${after.period || "iuran"} kamu telah dinyatakan lunas.`,
        { type: "payment_verified", entry_id: entryId });
    } else if (after.status === "belum_bayar" && before.status === "menunggu_verifikasi") {
      await sendToMembers(orgId, [after.member_uid], "Konfirmasi pembayaran perlu diperiksa",
        `Konfirmasi ${after.period || "iuran"} belum disetujui. Silakan hubungi bendahara.`,
        { type: "payment_not_verified", entry_id: entryId });
    }
  }
);

exports.notifyPickupCreated = onDocumentCreated(
  { document: "organizations/{orgId}/pickup_requests/{requestId}", region: "asia-southeast2" },
  async (event) => {
    const request = event.data?.data();
    if (!request) return;
    await sendToMembers(event.params.orgId, await admins(event.params.orgId), "Permintaan jemput iuran",
      `${request.name || "Warga"} meminta penjemputan pembayaran.`,
      { type: "pickup_request", request_id: event.params.requestId });
  }
);

exports.notifyPickupStatus = onDocumentUpdated(
  { document: "organizations/{orgId}/pickup_requests/{requestId}", region: "asia-southeast2" },
  async (event) => {
    const before = event.data?.before.data();
    const after = event.data?.after.data();
    if (!before || !after || before.status === after.status) return;
    await sendToMembers(event.params.orgId, [after.member_uid], "Status penjemputan diperbarui",
      `Permintaan jemput pembayaran kamu: ${after.status}.`,
      { type: "pickup_status", request_id: event.params.requestId });
  }
);
