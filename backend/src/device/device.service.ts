import { auth } from "../firebase/admin.js";
import { DeviceRepository } from "./device.repository.js";

export interface RegisterDeviceKeyInput {
  authUid: string;
  sisterId: string;
  keyId: string;
  publicKey: string;
}

export class DeviceService {
  constructor(private readonly repository = new DeviceRepository()) {}

  async register(input: RegisterDeviceKeyInput): Promise<void> {
    if (!input.keyId.trim()) throw new Error("Key ID is required");
    if (!input.publicKey.trim()) throw new Error("Public key is required");

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
      keyId: input.keyId,
      sisterId: input.sisterId,
      authUid: input.authUid,
      publicKey: input.publicKey,
      createdAt: existing?.createdAt ?? new Date(),
      updatedAt: new Date(),
    });
  }
}
