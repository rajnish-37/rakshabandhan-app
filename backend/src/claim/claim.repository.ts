import { firestore } from "../firebase/admin.js";
import type { GiftClaim } from "./claim.model.js";

const COLLECTION = "claims";

type TimestampLike = { toDate?: () => Date };

function toDate(value: unknown, field: string): Date {
  if (value instanceof Date) return value;
  if (value && typeof value === "object" && typeof (value as TimestampLike).toDate === "function") {
    return (value as TimestampLike).toDate!();
  }
  if (typeof value === "string" || typeof value === "number") {
    const parsed = new Date(value);
    if (!Number.isNaN(parsed.getTime())) return parsed;
  }
  throw new Error(`Invalid ${field} timestamp`);
}

function nullableDate(value: unknown, field: string): Date | null {
  if (value == null || value === "") return null;
  return toDate(value, field);
}

function toClaim(data: FirebaseFirestore.DocumentData): GiftClaim {
  return {
    claimId: data.claimId,
    sisterId: data.sisterId,
    sisterName: data.sisterName,
    sisterEmail: data.sisterEmail,
    giftId: data.giftId,
    amount: Number(data.amount),
    currency: data.currency,
    upiId: data.upiId,
    status: data.status,
    createdAt: toDate(data.createdAt ?? data.created_at, "createdAt"),
    updatedAt: toDate(data.updatedAt ?? data.updated_at, "updatedAt"),
    paidAt: nullableDate(data.paidAt ?? data.paid_at, "paidAt"),
    paidBy: data.paidBy ?? data.paid_by ?? null,
  };
}

export type ClaimCreateResult =
  | { kind: "created"; claim: GiftClaim }
  | { kind: "duplicate"; claim: GiftClaim };

export class ClaimRepository {
  private readonly collection = firestore.collection(COLLECTION);

  async createClaim(claim: GiftClaim): Promise<ClaimCreateResult> {
    const reference = this.collection.doc(`${claim.sisterId}_${claim.giftId}`);

    return firestore.runTransaction(async (transaction) => {
      const document = await transaction.get(reference);
      if (document.exists) {
        const data = document.data();
        if (!data) throw new Error("Existing claim data is unavailable");
        return { kind: "duplicate", claim: toClaim(data) };
      }

      transaction.create(reference, {
        claimId: claim.claimId,
        sisterId: claim.sisterId,
        sisterName: claim.sisterName,
        sisterEmail: claim.sisterEmail,
        giftId: claim.giftId,
        amount: claim.amount,
        currency: claim.currency,
        upiId: claim.upiId,
        status: claim.status,
        createdAt: claim.createdAt,
        updatedAt: claim.updatedAt,
        paidAt: null,
        paidBy: null,
      });

      return { kind: "created", claim };
    });
  }

  async findBySisterAndGift(sisterId: string, giftId: string): Promise<GiftClaim | null> {
    const document = await this.collection.doc(`${sisterId}_${giftId}`).get();
    if (!document.exists) return null;
    const data = document.data();
    return data ? toClaim(data) : null;
  }

  async findPending(): Promise<GiftClaim[]> {
    const snapshot = await this.collection.where("status", "==", "PENDING").limit(100).get();
    return snapshot.docs.map((document) => toClaim(document.data()));
  }

  async markPaid(claimId: string, paidBy: string, paidAt: Date): Promise<GiftClaim | null> {
    const query = await this.collection.where("claimId", "==", claimId).limit(1).get();
    const document = query.docs.at(0);
    if (!document) return null;

    return firestore.runTransaction(async (transaction) => {
      const current = await transaction.get(document.ref);
      if (!current.exists) return null;
      const data = current.data();
      if (!data) throw new Error("Claim data is unavailable");
      if (data.status !== "PENDING") return null;

      transaction.update(document.ref, {
        status: "PAID",
        updatedAt: paidAt,
        paidAt,
        paidBy,
      });

      return toClaim({ ...data, status: "PAID", updatedAt: paidAt, paidAt, paidBy });
    });
  }
}
