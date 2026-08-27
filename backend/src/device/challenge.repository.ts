import { FieldValue } from "firebase-admin/firestore";
import { firestore } from "../firebase/admin.js";
import type { DeviceChallenge } from "./challenge.model.js";

const COLLECTION = "deviceChallenges";

export class DeviceChallengeRepository {
  private readonly collection = firestore.collection(COLLECTION);

  async create(challenge: DeviceChallenge): Promise<void> {
    await this.collection.doc(challenge.challengeId).create({
      challengeId: challenge.challengeId,
      keyId: challenge.keyId,
      challenge: challenge.challenge,
      expiresAt: challenge.expiresAt,
      createdAt: challenge.createdAt,
    });
  }

  async find(challengeId: string): Promise<DeviceChallenge | null> {
    const document = await this.collection.doc(challengeId).get();
    if (!document.exists) return null;

    const data = document.data();
    if (!data) return null;

    return {
      challengeId: data.challengeId,
      keyId: data.keyId,
      challenge: data.challenge,
      expiresAt: data.expiresAt.toDate(),
      createdAt: data.createdAt.toDate(),
      usedAt: data.usedAt?.toDate(),
    };
  }

  async consume(challengeId: string): Promise<boolean> {
    const ref = this.collection.doc(challengeId);

    return firestore.runTransaction(async (transaction) => {
      const snapshot = await transaction.get(ref);
      if (!snapshot.exists) return false;

      const data = snapshot.data();
      if (!data || data.usedAt) return false;

      transaction.update(ref, { usedAt: FieldValue.serverTimestamp() });
      return true;
    });
  }
}
