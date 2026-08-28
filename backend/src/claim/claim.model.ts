export type ClaimStatus = "PENDING" | "PAID";

export interface GiftClaim {
  claimId: string;
  sisterId: string;
  sisterName: string;
  sisterEmail: string;
  giftId: string;
  amount: number;
  currency: string;
  upiId: string;
  status: ClaimStatus;
  createdAt: Date;
  updatedAt: Date;
  paidAt: Date | null;
  paidBy: string | null;
}
