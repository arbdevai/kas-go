const { applicationDefault, initializeApp } = require("firebase-admin/app");
const { getFirestore, FieldValue } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp({ credential: applicationDefault() });
const db = getFirestore();
const orgId = "kt-pemuda";
const org = db.collection("organizations").doc(orgId);
const state = db.collection("notification_poller_state");
const now = Date.now();

async function targetsFor(role) {
  const snapshot = await org.collection("members").get();
  return snapshot.docs
    .filter((doc) => doc.get("status") === "active" && role(doc.get("role")))
    .map((doc) => doc.id);
}

async function sendToMembers(memberUids, title, body, data) {
  const uids = [...new Set(memberUids.filter(Boolean))];
  if (uids.length === 0) return;
  const tokens = await org.collection("notification_tokens")
    .where("member_uid", "in", uids.slice(0, 30)).get();
  const docs = tokens.docs.filter((doc) => typeof doc.get("fcm_token") === "string");
  for (let offset = 0; offset < docs.length; offset += 500) {
    const group = docs.slice(offset, offset + 500);
    const result = await getMessaging().sendEachForMulticast({
      tokens: group.map((doc) => doc.get("fcm_token")),
      notification: { title, body },
      data: Object.fromEntries(Object.entries(data).map(([key, value]) => [key, String(value)])),
      android: { priority: "high", notification: { channelId: "kas_go_updates" } },
    });
    const stale = [];
    result.responses.forEach((response, index) => {
      const code = response.error?.code;
      if (code === "messaging/registration-token-not-registered" ||
          code === "messaging/invalid-registration-token") stale.push(group[index].ref.delete());
      if (!response.success) console.warn("FCM delivery error:", code);
    });
    await Promise.all(stale);
  }
}

async function sendToAdmins(title, body, data) {
  const ids = await targetsFor((role) => /^admin[1-3]$/.test(role || ""));
  await sendToMembers(ids, title, body, data);
}

async function sendMemberStatusChange(doc, previousStatus) {
  const entryId = doc.id;
  const entry = doc.data();
  if (entry.status === previousStatus) return;
  if (entry.status === "menunggu_verifikasi" && previousStatus === "belum_bayar") {
    await sendToAdmins("Pembayaran perlu diverifikasi",
      `${entry.member_name || "Warga"} mengirim konfirmasi pembayaran ${entry.period || "iuran"}.`,
      { type: "payment_verification", entry_id: entryId });
  } else if (entry.status === "lunas" && previousStatus === "menunggu_verifikasi") {
    await sendToMembers([entry.member_uid], "Pembayaran sudah diverifikasi",
      `Pembayaran ${entry.period || "iuran"} kamu telah dinyatakan lunas.`,
      { type: "payment_verified", entry_id: entryId });
  } else if (entry.status === "belum_bayar" && previousStatus === "menunggu_verifikasi") {
    await sendToMembers([entry.member_uid], "Konfirmasi pembayaran perlu diperiksa",
      `Konfirmasi ${entry.period || "iuran"} belum disetujui. Silakan hubungi bendahara.`,
      { type: "payment_not_verified", entry_id: entryId });
  } else if (entry.status === "menunggu_verifikasi" && !previousStatus) {
    await sendToAdmins("Pembayaran perlu diverifikasi",
      `${entry.member_name || "Warga"} mengirim konfirmasi pembayaran ${entry.period || "iuran"}.`,
      { type: "payment_verification", entry_id: entryId });
  } else if (entry.status === "lunas" && !previousStatus) {
    await sendToMembers([entry.member_uid], "Pembayaran sudah diverifikasi",
      `Pembayaran ${entry.period || "iuran"} kamu telah dinyatakan lunas.`,
      { type: "payment_verified", entry_id: entryId });
  }
}

async function handleBillEntry(doc) {
  const stateRef = state.doc(`bill_${doc.id}`);
  const previous = await stateRef.get();
  await sendMemberStatusChange(doc, previous.get("status") || null);
  await stateRef.set({ status: doc.get("status"), updated_at_millis: now });
}

async function handlePickup(doc, isNew) {
  const request = doc.data();
  const stateRef = state.doc(`pickup_${doc.id}`);
  const previous = await stateRef.get();
  const oldStatus = previous.get("status");
  if (isNew) {
    await sendToAdmins("Permintaan jemput iuran",
      `${request.name || "Warga"} meminta penjemputan pembayaran.`,
      { type: "pickup_request", request_id: doc.id });
  } else if (oldStatus && oldStatus !== request.status) {
    await sendToMembers([request.member_uid], "Status penjemputan diperbarui",
      `Permintaan jemput pembayaran kamu: ${request.status}.`,
      { type: "pickup_status", request_id: doc.id });
  }
  await stateRef.set({ status: request.status, updated_at_millis: now });
}

async function run() {
  const cursorDoc = await state.doc("cursor").get();
  const cursor = cursorDoc.exists ? cursorDoc.get("last_polled_at_millis") : now - 10 * 60 * 1000;
  const [newMembers, changedEntries, newPickups, changedPickups] = await Promise.all([
    org.collection("members").where("created_at_millis", ">", cursor).get(),
    org.collection("bill_entries").where("updated_at_millis", ">", cursor).get(),
    org.collection("pickup_requests").where("created_at_millis", ">", cursor).get(),
    org.collection("pickup_requests").where("updated_at_millis", ">", cursor).get(),
  ]);

  const adminIds = await targetsFor((role) => /^admin[1-3]$/.test(role || ""));
  for (const member of newMembers.docs) {
    if (member.get("status") === "pending") {
      await sendToMembers(adminIds, "Pendaftaran warga baru",
        `${member.get("name") || "Warga"} menunggu persetujuan pengurus.`,
        { type: "pending_member", uid: member.id });
    }
  }

  for (const entry of changedEntries.docs) await handleBillEntry(entry);
  const newPickupIds = new Set(newPickups.docs.map((doc) => doc.id));
  const handledPickups = new Set();
  for (const pickup of newPickups.docs) {
    await handlePickup(pickup, true);
    handledPickups.add(pickup.id);
  }
  for (const pickup of changedPickups.docs) {
    if (!handledPickups.has(pickup.id)) await handlePickup(pickup, newPickupIds.has(pickup.id));
  }

  await state.doc("cursor").set({ last_polled_at_millis: now, updated_at: FieldValue.serverTimestamp() });
}

run().catch((error) => {
  console.error("Kas Go notification polling failed:", error.message);
  process.exitCode = 1;
});
