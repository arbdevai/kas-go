# Firebase Kas Go

The native Android app uses Firebase project `kas-go-sahal-20260927`.

- Google Sign-In is enabled through Firebase Authentication.
- The Android app is registered as `id.or.karangtaruna.kasgo`; release signing fingerprints from the existing APK are registered in Firebase.
- Cloud Firestore Standard is in `asia-southeast2` (Jakarta), with deletion protection enabled.
- Firestore rules and the member-scoped query indexes are deployed from `firebase/firestore.rules` and `firebase/firestore.indexes.json`.
- The first verified Google account `sahal.mahfudh.id@gmail.com` is provisioned as the initial app administrator (Bendahara). Additional administrators are limited to three total and are assigned through the member-role screen.
- New residents create a pending profile. An administrator must approve the profile before the resident can see balances, bills, or payment destinations.

The Android Firebase client configuration in `app/google-services.json` contains public app identifiers and an API key; it is not a server credential. Never put a service-account key, OAuth client secret, or private signing key in the app or repository. Release APK signing is handled by GitHub Actions secrets.

On the first administrator sign-in, the app imports local ledger entries and bills that are not already in the new cloud project, then uses Firestore as the shared source for transactions, bills, payment verification, pickup requests, member roles, and payment settings. Local member identities from older device-only installs have device-specific IDs and are retained in a local backup; residents sign in and submit their profile again so the new project can assign a stable Firebase UID. Previous bill entries remain visible to administrators but do not automatically attach to newly registered Firebase accounts.

The deployed rules deny unauthenticated access, restrict finance reads to active members and administrators, limit resident writes to their own profile/payment/pickup flows, and keep the ledger append-only. Treat these as a security prototype and review them before broad public distribution.

## Push notifications

Android notification permission is requested on Android 13 and newer. Notifications use the signed-in device's Firebase session and Android WorkManager, so this works on Firebase Spark without a server key or Cloud Functions. The app checks for payment verification, bill status, new pickup requests, and pending registrations on a periodic schedule. Android requires periodic work intervals of at least 15 minutes and may defer work under battery restrictions; notifications are therefore best-effort rather than instant push. The app also receives Firestore live updates while open.
