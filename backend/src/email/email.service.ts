import { BrevoClient } from "@getbrevo/brevo";

export interface InvitationEmailInput {
  recipientEmail: string;
  invitationCode: string;
  expiresAt: Date;
}

export class EmailService {
  private readonly client: BrevoClient;
  private readonly senderEmail: string;
  private readonly senderName: string;

  constructor() {
    const apiKey = process.env.BREVO_API_KEY;
    const senderEmail = process.env.BREVO_SENDER_EMAIL;
    const senderName = process.env.BREVO_SENDER_NAME;

    if (!apiKey) throw new Error("BREVO_API_KEY is not configured");
    if (!senderEmail) throw new Error("BREVO_SENDER_EMAIL is not configured");
    if (!senderName) throw new Error("BREVO_SENDER_NAME is not configured");

    this.client = new BrevoClient({
      apiKey,
      timeoutInSeconds: 15,
      maxRetries: 2,
    });
    this.senderEmail = senderEmail;
    this.senderName = senderName;
  }

  async sendInvitation(input: InvitationEmailInput): Promise<void> {
    const expiresAt = input.expiresAt.toLocaleString("en-IN", {
      dateStyle: "medium",
      timeStyle: "short",
      timeZone: "Asia/Kolkata",
    });

    await this.client.transactionalEmails.sendTransacEmail({
      sender: {
        email: this.senderEmail,
        name: this.senderName,
      },
      to: [{ email: input.recipientEmail }],
      subject: "Your Rakhi Bandhan Invitation",
      textContent: [
        "You have received an invitation to join Rakhi Bandhan.",
        "",
        `Your invitation code: ${input.invitationCode}`,
        "",
        `This code expires on ${expiresAt} (IST).`,
        "",
        "Please enter this code in the Rakhi Bandhan app.",
        "",
        "Regards,",
        this.senderName,
      ].join("\n"),
    });
  }
}
