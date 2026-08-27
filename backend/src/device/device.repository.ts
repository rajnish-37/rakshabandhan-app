import { firestore } from "../firebase/admin.js";

export interface DeviceKeyRecord {
  keyId: string;
  sisterId: string;
  authUid: string;
  publicKey: string;
  createdAt: Date;
  updatedAt: Date;
}

const COLLECTION = "deviceKeys";

export class DeviceRepository {
  private readonly collection = firestore.collection(COLLECTION);

  async register(record: DeviceKeyRecord): Promise<void> {
    const ref = this.collection.doc(record.keyId);

    await firestore.runTransaction(async (transaction) => {
      const existing = await transaction.get(ref);

      if (existing.exists) {
        const data = existing.data();
        if (
          data?.authUid !== record.authUid ||
          data?.sisterId !== record.sisterId ||
          data?.publicKey !== record.publicKey
        ) {
          throw new Error("Device key ID is already registered to a different identity");
        }

        transaction.update(ref, { updatedAt: record.updatedAt });
        return;
      }

      transaction.create(ref, record);
    });
  }

  async findByKeyId(keyId: string): Promise<DeviceKeyRecord | null> {
    const document = await this.collection.doc(keyId).get();
    if (!document.exists) return null;

    const data = document.data();
    if (!data) return null;

    return {
      keyId: data.keyId,
      sisterId: data.sisterId,
      authUid: data.authUid,
      publicKey: data.publicKey,
      createdAt: data.createdAt.toDate(),
      updatedAt: data.updatedAt.toDate(),
    };
  }

  async findBySisterId(sisterId: string): Promise<DeviceKeyRecord | null> {
    const snapshot = await this.collection
      .where("sisterId", "==", sisterId)
      .limit(1)
      .get();

    const document = snapshot.docs.at(0);
    if (!document) return null;

    const data = document.data();
    return {
      keyId: data.keyId,
      sisterId: data.sisterId,
      authUid: data.authUid,
      publicKey: data.publicKey,
      createdAt: data.createdAt.toDate(),
      updatedAt: data.updatedAt.toDate(),
    };
  }
}
