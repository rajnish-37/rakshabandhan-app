import "dotenv/config";

function requiredProductionEnv(name: string): string {
  const value = process.env[name]?.trim();
  if (!value) {
    throw new Error(`${name} is required in production`);
  }
  return value;
}

const nodeEnv = process.env.NODE_ENV?.trim() || "development";

export const config = {
  nodeEnv,
  port: Number(process.env.PORT ?? 8080),
  host: process.env.HOST ?? "0.0.0.0",
  adminApiKey: process.env.ADMIN_API_KEY,
} as const;

export function firebaseConfig() {
  const projectId = requiredProductionEnv("FIREBASE_PROJECT_ID");
  const clientEmail = requiredProductionEnv("FIREBASE_CLIENT_EMAIL");
  const privateKey = requiredProductionEnv("FIREBASE_PRIVATE_KEY");

  return {
    projectId,
    clientEmail,
    privateKey: privateKey.replace(/\\n/g, "\n"),
  } as const;
}

export function validateProductionConfig(): void {
  if (config.nodeEnv !== "production") return;

  requiredProductionEnv("ADMIN_API_KEY");
  requiredProductionEnv("BREVO_API_KEY");
  requiredProductionEnv("BREVO_SENDER_EMAIL");
  requiredProductionEnv("BREVO_SENDER_NAME");
  firebaseConfig();
}
