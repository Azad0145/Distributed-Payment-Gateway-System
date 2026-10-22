package processor;
import models.Payment;
public class CardPaymentProcessor implements PaymentProcessor {
    private final AsyncPaymentExecutor executor;
    public CardPaymentProcessor(AsyncPaymentExecutor executor) { this.executor = executor; }
    public void process(Payment payment) { executor.execute(payment); }
}
