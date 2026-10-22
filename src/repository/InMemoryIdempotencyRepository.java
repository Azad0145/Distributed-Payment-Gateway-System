package repository;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
public class InMemoryIdempotencyRepository implements IdempotencyRepository {
    private final Map<String, String> store = new ConcurrentHashMap<>();
    public boolean exists(String key) { return store.containsKey(key); }
    public String getPaymentId(String key) { return store.get(key); }
    public void save(String key, String paymentId) { store.putIfAbsent(key, paymentId); }
}
