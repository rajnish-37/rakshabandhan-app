import { createHash, randomBytes } from "node:crypto";

const CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
const CODE_LENGTH = 8;

export interface GeneratedInvitationCode {
  rawCode: string;
  codeHash: string;
}

function randomCode(): string {
  const bytes = randomBytes(CODE_LENGTH);
  return Array.from(bytes, (byte) => CODE_ALPHABET[byte % CODE_ALPHABET.length]).join("");
}

export function hashInvitationCode(code: string): string {
  return createHash("sha256").update(code, "utf8").digest("hex");
}

export function generateInvitationCode(): GeneratedInvitationCode {
  const rawCode = randomCode();

  return {
    rawCode,
    codeHash: hashInvitationCode(rawCode),
  };
}
