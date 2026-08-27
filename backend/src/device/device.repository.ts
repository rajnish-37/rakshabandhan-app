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

function fromDocument(data: FirebaseFirestore.DocumentData): DeviceKeyRecord {
  return {
    keyId: data.keyId,
    sisterId: data.sisterId,
    authUid: data.authUid,
    publicKey: data.publicKey,
    createdAt: data.createdAt.toDate(),
    updatedAt: data.updatedAt.toDate(),
  };
}

export class DeviceRepository {
  private readonly collection = firestore.collection(COLLECTION);

  async register(record: DeviceKeyRecord): Promise<void> {
    const existing = await this.findBySisterId(record.sisterId);

    if (existing && existing.authUid !== record.authUid) {
      throw new Error("Sister already has a device registered to a different identity");
    }

    const target = existing
      ? this.collection.doc(existing.keyId)
      : this.collection.doc(record.keyId);

    await firestore.runTransaction(async (transaction) => {
      const current = await transaction.get(target);

      if (existing) {
        if (!current.exists) {
          throw new Error("Existing device registration could not be found");
        }

        transaction.update(target, {
          keyId: record.keyId,
          sisterId: record.sisterId,
          authUid: record.authUid,
          publicKey: record.publicKey,
          updatedAt: record.updatedAt,
        });
        return;
      }

      if (current.exists) {
        const data = current.data();
        if (
          data?.authUid !== record.authUid ||
          data?.sisterId !== record.sisterId ||
          data?.publicKey !== record.publicKey
        ) {
          throw new Error("Device key ID is already registered to a different identity");
        }

        transaction.update(target, { updatedAt: record.updatedAt });
        return;
      }

      transaction.create(target, record);
    });
  }

  async findByKeyId(keyId: string): Promise<DeviceKeyRecord | null> {
    const snapshot = await this.collection
      .where("keyId", "==", keyId)
      .limit(1)
      .get();

    const document = snapshot.docs.at(0);
    if (!document) return null;

    return fromDocument(document.data());
  }

  async findBySisterId(sisterId: string): Promise<DeviceKeyRecord | null> {
    const snapshot = await this.collection
      .where("sisterId", "==", sisterId)
      .limit(1)
      .get();

    const document = snapshot.docs.at(0);
    if (!document) return null;

    return fromDocument(document.data());
  }
}
