package repository;
import models.Payment;
import java.util.List;
public interface Repository {
    void save(Payment payment);
    Payment getById(String paymentId);
    void update(Payment payment);
    List<Payment> findAll();
}
