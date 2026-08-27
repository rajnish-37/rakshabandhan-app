import { FieldValue } from "firebase-admin/firestore";
import { firestore } from "../firebase/admin.js";
import type { Invitation } from "./invitation.model.js";

const COLLECTION = "invitations";

function toFirestore(invitation: Invitation) {
  return {
    invitationId: invitation.invitationId,
    sisterId: invitation.sisterId,
    email: invitation.email,
    codeHash: invitation.codeHash,
    status: invitation.status,
    expiresAt: invitation.expiresAt,
    createdAt: invitation.createdAt,
    ...(invitation.redeemedAt ? { redeemedAt: invitation.redeemedAt } : {}),
  };
}

export class InvitationRepository {
  private readonly collection = firestore.collection(COLLECTION);

  async create(invitation: Invitation): Promise<void> {
    await this.collection.doc(invitation.invitationId).create(toFirestore(invitation));
  }

  async findPendingByEmail(email: string): Promise<Invitation | null> {
    const snapshot = await this.collection
      .where("email", "==", email)
      .where("status", "==", "PENDING")
      .limit(1)
      .get();

    const document = snapshot.docs.at(0);
    if (!document) return null;

    const data = document.data();
    return {
      invitationId: data.invitationId,
      sisterId: data.sisterId,
      email: data.email,
      codeHash: data.codeHash,
      status: data.status,
      expiresAt: data.expiresAt.toDate(),
      createdAt: data.createdAt.toDate(),
      redeemedAt: data.redeemedAt?.toDate(),
    };
  }

  async markRedeemed(invitationId: string): Promise<void> {
    await this.collection.doc(invitationId).update({
      status: "REDEEMED",
      redeemedAt: FieldValue.serverTimestamp(),
    });
  }
}
