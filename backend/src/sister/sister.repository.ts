import { firestore } from "../firebase/admin.js";
import type { SisterProfile } from "./sister.model.js";

const COLLECTION = "sisters";

export class SisterRepository {
  private readonly collection = firestore.collection(COLLECTION);

  async findById(sisterId: string): Promise<SisterProfile | null> {
    const document = await this.collection.doc(sisterId).get();

    if (!document.exists) return null;

    const data = document.data();
    if (!data) return null;

    return {
      sisterId: data.sisterId,
      name: data.name ?? "Sister",
      email: data.email,
      authUid: data.authUid,
      status: data.status,
      createdAt: data.createdAt.toDate(),
      updatedAt: data.updatedAt.toDate(),
    };
  }

  async upsert(profile: SisterProfile): Promise<void> {
    const reference = this.collection.doc(profile.sisterId);

    await firestore.runTransaction(async (transaction) => {
      const document = await transaction.get(reference);

      if (document.exists) {
        const data = document.data();
        if (!data) throw new Error("Sister profile data is unavailable");

        if (data.email !== profile.email) {
          throw new Error("Sister ID is already linked to a different email");
        }

        if (data.authUid !== profile.authUid) {
          throw new Error("Sister ID is already linked to a different account");
        }

        transaction.update(reference, {
          name: profile.name,
          status: profile.status,
          updatedAt: profile.updatedAt,
        });
        return;
      }

      transaction.create(reference, {
        sisterId: profile.sisterId,
        name: profile.name,
        email: profile.email,
        authUid: profile.authUid,
        status: profile.status,
        createdAt: profile.createdAt,
        updatedAt: profile.updatedAt,
      });
    });
  }
}
