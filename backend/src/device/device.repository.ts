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
    const snapshot = await this.collection
      .where("sisterId", "==", record.sisterId)
      .limit(1)
      .get();

    const existingDocument = snapshot.docs.at(0);
    const existing = existingDocument ? fromDocument(existingDocument.data()) : null;

    if (existing && existing.authUid !== record.authUid) {
      throw new Error("Sister already has a device registered to a different identity");
    }

    const stableDocument = this.collection.doc(record.sisterId);

    await firestore.runTransaction(async (transaction) => {
      const stableCurrent = await transaction.get(stableDocument);

      if (existingDocument && existingDocument.ref.path !== stableDocument.path) {
        transaction.delete(existingDocument.ref);
      }

      if (stableCurrent.exists) {
        const stableData = stableCurrent.data();
        if (stableData?.authUid !== record.authUid || stableData?.sisterId !== record.sisterId) {
          throw new Error("Sister already has a device registered to a different identity");
        }

        transaction.update(stableDocument, {
          keyId: record.keyId,
          publicKey: record.publicKey,
          updatedAt: record.updatedAt,
        });
        return;
      }

      transaction.create(stableDocument, {
        ...record,
        createdAt: existing?.createdAt ?? record.createdAt,
      });
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
    const stableDocument = await this.collection.doc(sisterId).get();
    if (stableDocument.exists) {
      const data = stableDocument.data();
      if (data) return fromDocument(data);
    }

    const snapshot = await this.collection
      .where("sisterId", "==", sisterId)
      .limit(1)
      .get();

    const document = snapshot.docs.at(0);
    if (!document) return null;

    return fromDocument(document.data());
  }
}
