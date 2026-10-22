package models;

import enums.PaymentMethod;
import enums.PaymentStatus;
import java.time.Instant;

public final class Payment {
    private final String paymentId;
    private final double amount;
    private final PaymentMethod method;
    private final String idempotencyKey;
    private final Instant createdAt;
    private volatile PaymentStatus status;
    private volatile String message;

    public Payment(String paymentId, double amount, PaymentMethod method, String idempotencyKey) {
        this.paymentId = paymentId;
        this.amount = amount;
        this.method = method;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = Instant.now();
        this.status = PaymentStatus.CREATED;
        this.message = "Payment created";
    }
    public String getPaymentId() { return paymentId; }
    public double getAmount() { return amount; }
    public PaymentMethod getMethod() { return method; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public Instant getCreatedAt() { return createdAt; }
    public PaymentStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public void updateStatus(PaymentStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
