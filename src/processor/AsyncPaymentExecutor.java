package processor;
import enums.PaymentStatus;
import models.Payment;
import repository.Repository;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

public class AsyncPaymentExecutor {
    private final ExecutorService executor = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "payment-worker");
        t.setDaemon(true);
        return t;
    });
    private final Repository repository;
    public AsyncPaymentExecutor(Repository repository) { this.repository = repository; }
    public void execute(Payment payment) {
        payment.updateStatus(PaymentStatus.PROCESSING, "Payment is processing");
        repository.update(payment);
        executor.submit(() -> {
            try {
                Thread.sleep(1600);
                boolean success = ThreadLocalRandom.current().nextInt(100) < 80;
                payment.updateStatus(success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED,
                        success ? "Simulated payment completed successfully" : "Simulated provider declined the payment");
                repository.update(payment);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                payment.updateStatus(PaymentStatus.PENDING, "Processing interrupted; status is pending");
                repository.update(payment);
            } catch (RuntimeException e) {
                payment.updateStatus(PaymentStatus.PENDING, "Processing result is uncertain");
                repository.update(payment);
            }
        });
    }
    public void shutdown() { executor.shutdown(); }
}
