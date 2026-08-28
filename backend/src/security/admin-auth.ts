import { timingSafeEqual } from "node:crypto";
import { config } from "../config.js";

type AdminAuthRequest = {
  headers: { [key: string]: string | string[] | undefined };
};

export function isAdminAuthorized(request: AdminAuthRequest): boolean {
  const configuredKey = config.adminApiKey;
  const supplied = request.headers["x-admin-api-key"];

  if (!configuredKey || typeof supplied !== "string") return false;
  if (supplied.length > 4096) return false;

  const expected = Buffer.from(configuredKey, "utf8");
  const actual = Buffer.from(supplied, "utf8");

  return expected.length === actual.length && timingSafeEqual(expected, actual);
}

export function requireAdminAuthorization(
  request: AdminAuthRequest,
): { status: "error"; message: "Admin authorization required" } | null {
  return isAdminAuthorized(request)
    ? null
    : { status: "error", message: "Admin authorization required" };
}
