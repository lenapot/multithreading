import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
* Пусть нашему приложению нужно получить изделие по его идентификатору, причем процесс получения может быть дорогостоящим,
* т.к. включает удаленный доступ: вызов REST-совместимой веб-службы по сети, обращение к БД или еще что-то в этом роде.
* Поэтому мы решили создать локальный кэш изделий в виде Map. Тогда при запросе изделия система сначала проверит,
* есть ли оно уже в кэше, и, только если нет, начнет более дорогую операцию.
* */
public class TestCompletableFuture {
    private Logger logger = Logger.getLogger(this.getClass().getName());

    private Map<Integer, Product> cache = new HashMap<>();

    private Product getLocal(int id) {
        return cache.get(id);
    }

    private Product getRemote(int id) {
        try {
            Thread.sleep(100); //Имитировать задержку получения
            if (id == 666) {
                throw new RuntimeException("Evil request"); //Имитировать ошибку сети, базы данных или еще чего-то
            }
        } catch (InterruptedException ignored) {
        }
        return new Product(id, "name");
    }

    public CompletableFuture<Product> getProduct(int id) {
        try {
            Product product = getLocal(id);
            if (product != null) {
                logger.info("getLocal with id=" + id);
                return CompletableFuture.completedFuture(product); //Завершить, записав изделие из кэша, если оно там есть
            } else {
                // Synchronous (simulating legacy system)
                logger.info("getRemote with id=" + id);
                CompletableFuture<Product> future = new CompletableFuture<>();
                Product p = getRemote(id);
                cache.put(id, p);
                future.complete(p); //Завершить после получения
                return future;
            }
        } catch (Exception e) {
            logger.info("exception thrown");
            CompletableFuture<Product> future = new CompletableFuture<>();
            future.completeExceptionally(e); //Завершить с исключением, если что-то пошло не так
            return future;
        }
    }

    public CompletableFuture<Product> getProductAsync(int id) {
        try {
            Product product = getLocal(id);
            if (product != null) {
                logger.info("getLocal with id=" + id);
                return CompletableFuture.completedFuture(product);
            } else {
                logger.info("getRemote with id=" + id);
                // Asynchronous
                return CompletableFuture.supplyAsync(() -> {
                    Product p = getRemote(id);
                    cache.put(id, p);
                    return p;
                });
            }
        } catch (Exception e) {
            logger.info("exception thrown");
            CompletableFuture<Product> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        TestCompletableFuture demo = new TestCompletableFuture();

        //remote
        Product product = demo.getProduct(1).get();
        assertEquals(1, product.getId());

        //local
        Product productLocal = demo.getProduct(1).get();
        assertEquals(1, productLocal.getId());

        //exception
        try {

            demo.getProduct(666).get();
        } catch (ExecutionException e){
            System.out.println("ExecutionException catched");
            assertEquals(ExecutionException.class, e.getClass());
            assertEquals(RuntimeException.class, e.getCause().getClass());
        }

        //async
        Product productAsync = demo.getProductAsync(1).get();
        assertEquals(1, productAsync.getId());

    }
}
class Product {
    private int id;
    private String name;

    public Product(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}