import { applicationDefault, getApps, initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { getFirestore } from "firebase-admin/firestore";

const firebaseApp =
  getApps()[0] ??
  initializeApp({
    credential: applicationDefault(),
  });

export const auth = getAuth(firebaseApp);
export const firestore = getFirestore(firebaseApp);
