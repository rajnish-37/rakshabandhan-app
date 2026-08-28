import { randomUUID } from "node:crypto";
import { firestore } from "../firebase/admin.js";
import type { Gift } from "./gift.model.js";

const COLLECTION = "gifts";

export class GiftRepository {
  private readonly collection = firestore.collection(COLLECTION);

  async findBySisterId(sisterId: string): Promise<Gift | null> {
    const snapshot = await this.collection.where("sisterId", "==", sisterId).limit(1).get();
    if (snapshot.empty) return null;

    const document = snapshot.docs[0];
    if (!document) return null;

    const data = document.data();
    return {
      giftId: data.giftId,
      sisterId: data.sisterId,
      amount: Number(data.amount),
      currency: data.currency,
      status: data.status,
      claimEligible: Boolean(data.claimEligible),
      claimDeadline: data.claimDeadline ? data.claimDeadline.toDate() : null,
      createdAt: data.createdAt.toDate(),
      updatedAt: data.updatedAt.toDate(),
    };
  }

  async upsert(gift: Gift): Promise<Gift> {
    const existing = await this.findBySisterId(gift.sisterId);
    const reference = existing ? this.collection.doc(existing.giftId) : this.collection.doc(gift.giftId || randomUUID());
    const stored = { ...gift, giftId: reference.id };

    await reference.set({
      giftId: stored.giftId,
      sisterId: stored.sisterId,
      amount: stored.amount,
      currency: stored.currency,
      status: stored.status,
      claimEligible: stored.claimEligible,
      claimDeadline: stored.claimDeadline,
      createdAt: existing?.createdAt ?? stored.createdAt,
      updatedAt: stored.updatedAt,
    });

    return { ...stored, createdAt: existing?.createdAt ?? stored.createdAt };
  }
}
