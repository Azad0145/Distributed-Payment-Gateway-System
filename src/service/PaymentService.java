package service;
import enums.PaymentMethod;
import models.Payment;
import processor.PaymentProcessor;
import repository.IdempotencyRepository;
import repository.Repository;
import java.util.Map;
import java.util.UUID;

public class PaymentService {
    private final Map<PaymentMethod, PaymentProcessor> processors;
    private final Repository paymentRepository;
    private final IdempotencyRepository idempotencyRepository;
    public PaymentService(Map<PaymentMethod, PaymentProcessor> processors, Repository paymentRepository,
                          IdempotencyRepository idempotencyRepository) {
        this.processors = processors;
        this.paymentRepository = paymentRepository;
        this.idempotencyRepository = idempotencyRepository;
    }
    public synchronized Payment createPayment(double amount, PaymentMethod method, String key) {
        if (!Double.isFinite(amount) || amount <= 0) throw new IllegalArgumentException("Amount must be greater than zero.");
        if (method == null || !processors.containsKey(method)) throw new IllegalArgumentException("Unsupported payment method.");
        if (key == null || key.trim().isEmpty()) throw new IllegalArgumentException("Idempotency key is required.");
        key = key.trim();
        if (idempotencyRepository.exists(key)) return paymentRepository.getById(idempotencyRepository.getPaymentId(key));
        Payment payment = new Payment(UUID.randomUUID().toString(), amount, method, key);
        paymentRepository.save(payment);
        idempotencyRepository.save(key, payment.getPaymentId());
        return payment;
    }
    public void processPayment(String paymentId) {
        Payment payment = paymentRepository.getById(paymentId);
        if (payment.getStatus() != enums.PaymentStatus.CREATED) return;
        PaymentProcessor processor = processors.get(payment.getMethod());
        if (processor == null) throw new IllegalStateException("No processor configured for " + payment.getMethod());
        processor.process(payment);
    }
    public Payment getPaymentStatus(String paymentId) { return paymentRepository.getById(paymentId); }
    public java.util.List<Payment> getAllPayments() { return paymentRepository.findAll(); }
    public boolean idempotencyKeyExists(String key) { return idempotencyRepository.exists(key); }
}
