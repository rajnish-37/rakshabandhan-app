import { firestore } from "./admin.js";

export async function checkFirestoreConnection(): Promise<void> {
  await firestore.collection("_system").doc("backend-health").get();
}
