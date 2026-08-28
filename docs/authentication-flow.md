# Authentication Flow — Phase 1C

## Enrollment

1. Admin creates a one-time invitation for a sister identity and email.
2. The sister enters the email and invitation code in the Android app.
3. The app generates an Android Keystore ECDSA signing key if no usable key exists.
4. The backend validates the invitation server-side, provisions the Firebase Auth identity, registers the device public key against the stable sister ID, redeems the invitation, and returns a Firebase custom token.
5. The app signs in with the custom token and enters the authenticated app shell.

## Returning authentication

1. On app start/resume, the app checks both Firebase session state and trusted-device-key state.
2. A Firebase session plus a usable device key is treated as authenticated; the Firebase ID token is refreshed opportunistically.
3. A device key without a Firebase session enters the biometric authentication state.
4. The app requests a short-lived server challenge for the device key ID.
5. The private key signs the challenge only after strong biometric authentication.
6. The backend verifies the signature against the registered public key, atomically consumes the one-time challenge, and issues a Firebase custom token.
7. The app signs in with that token and enters the authenticated app shell.

## Recovery / re-enrollment

- A Firebase session without a usable device key is never accepted as an authenticated state.
- The stale Firebase session is cleared and the app returns to enrollment-required state.
- If Android invalidates the signing key because the biometric enrollment changed, the user must complete a new invitation-based enrollment.
- Re-enrollment replaces the active public key for the stable sister ID; old device keys are not retained as trusted keys.
- Invitation codes and device challenges are one-time credentials and expire server-side.

## Session lifecycle

- Firebase Authentication remains the application session authority after custom-token sign-in.
- Android Keystore remains the trusted-device possession factor.
- The app does not expose a production sign-out control in the UI.
- Firebase ID-token refresh is delegated to the Firebase SDK and explicitly checked when the app returns to the foreground.

## Security boundaries

- Invitation verification, code hashing, redemption, device registration, challenge generation, signature verification, and custom-token issuance remain backend operations.
- Firestore is server-only in the current architecture; client reads/writes are denied by `firestore.rules`.
- Authentication endpoints enforce bounded request sizes, input validation, security headers, and per-IP in-memory rate limits.
