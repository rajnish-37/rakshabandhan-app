export type InvitationStatus = "PENDING" | "REDEEMED" | "EXPIRED" | "REVOKED";

export interface Invitation {
  invitationId: string;
  sisterId: string;
  email: string;
  codeHash: string;
  status: InvitationStatus;
  expiresAt: Date;
  createdAt: Date;
  redeemedAt?: Date;
}
