import Fastify from "fastify";
import { config } from "./config.js";

const app = Fastify({
  logger: true,
});

app.get("/health", async () => ({
  status: "ok",
  service: "rakshabandhan-backend",
}));

const start = async (): Promise<void> => {
  try {
    await app.listen({ port: config.port, host: config.host });
  } catch (error) {
    app.log.error(error);
    process.exit(1);
  }
};

await start();
