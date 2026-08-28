import { applicationDefault, cert, getApps, initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { getFirestore } from "firebase-admin/firestore";
import { firebaseConfig } from "../config.js";

function firebaseCredential() {
  const hasProjectId = Boolean(process.env.FIREBASE_PROJECT_ID);
  const hasClientEmail = Boolean(process.env.FIREBASE_CLIENT_EMAIL);
  const hasPrivateKey = Boolean(process.env.FIREBASE_PRIVATE_KEY);

  if (hasProjectId || hasClientEmail || hasPrivateKey) {
    if (!hasProjectId || !hasClientEmail || !hasPrivateKey) {
      throw new Error(
        "Firebase Admin credentials are incomplete. Set FIREBASE_PROJECT_ID, FIREBASE_CLIENT_EMAIL and FIREBASE_PRIVATE_KEY."
      );
    }

    const { projectId, clientEmail, privateKey } = firebaseConfig();
    return cert({ projectId, clientEmail, privateKey });
  }

  return applicationDefault();
}

const firebaseApp =
  getApps()[0] ??
  initializeApp({
    credential: firebaseCredential(),
  });

export const auth = getAuth(firebaseApp);
export const firestore = getFirestore(firebaseApp);
