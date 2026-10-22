package processor;
import models.Payment;
public class UPIPaymentProcessor implements PaymentProcessor {
    private final AsyncPaymentExecutor executor;
    public UPIPaymentProcessor(AsyncPaymentExecutor executor) { this.executor = executor; }
    public void process(Payment payment) { executor.execute(payment); }
}
