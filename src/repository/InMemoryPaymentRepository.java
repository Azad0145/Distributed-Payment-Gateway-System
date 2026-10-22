package repository;
import models.Payment;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
public class InMemoryPaymentRepository implements Repository {
    private final Map<String, Payment> store = new ConcurrentHashMap<>();
    public void save(Payment p) { store.put(p.getPaymentId(), p); }
    public Payment getById(String id) {
        Payment p = store.get(id);
        if (p == null) throw new IllegalArgumentException("Payment not found: " + id);
        return p;
    }
    public void update(Payment p) { store.put(p.getPaymentId(), p); }
    public List<Payment> findAll() {
        List<Payment> list = new ArrayList<>(store.values());
        list.sort(Comparator.comparing(Payment::getCreatedAt).reversed());
        return list;
    }
}
