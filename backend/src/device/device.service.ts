import { createHash, createPublicKey, timingSafeEqual } from "node:crypto";
import { auth } from "../firebase/admin.js";
import { DeviceRepository } from "./device.repository.js";

export interface RegisterDeviceKeyInput {
  authUid: string;
  sisterId: string;
  keyId: string;
  publicKey: string;
}

function validateRegistrationKey(keyId: string, publicKey: string): void {
  const normalizedKeyId = keyId.trim();
  const normalizedPublicKey = publicKey.trim();
  if (!normalizedKeyId) throw new Error("Key ID is required");
  if (!normalizedPublicKey) throw new Error("Public key is required");

  let encodedKey: Buffer;
  try {
    encodedKey = Buffer.from(normalizedPublicKey, "base64");
    const key = createPublicKey({ key: encodedKey, format: "der", type: "spki" });
    if (key.asymmetricKeyType !== "ec") throw new Error("Device public key must be an EC key");
  } catch {
    throw new Error("Invalid device public key");
  }

  const expectedKeyId = createHash("sha256").update(encodedKey).digest("hex");
  const expected = Buffer.from(expectedKeyId, "utf8");
  const supplied = Buffer.from(normalizedKeyId, "utf8");
  if (expected.length !== supplied.length || !timingSafeEqual(expected, supplied)) {
    throw new Error("Device key ID does not match the public key");
  }
}

export class DeviceService {
  constructor(private readonly repository = new DeviceRepository()) {}

  async register(input: RegisterDeviceKeyInput): Promise<void> {
    validateRegistrationKey(input.keyId, input.publicKey);

    const user = await auth.getUser(input.authUid);
    const claimSisterId = user.customClaims?.sisterId;
    const claimRole = user.customClaims?.role;

    if (claimRole !== "SISTER" || claimSisterId !== input.sisterId) {
      throw new Error("Firebase identity is not authorized for this sister");
    }

    const existing = await this.repository.findBySisterId(input.sisterId);
    if (existing && existing.authUid !== input.authUid) {
      throw new Error("Sister already has a device registered to a different identity");
    }

    await this.repository.register({
      keyId: input.keyId.trim(),
      sisterId: input.sisterId,
      authUid: input.authUid,
      publicKey: input.publicKey.trim(),
      createdAt: existing?.createdAt ?? new Date(),
      updatedAt: new Date(),
    });
  }
}
