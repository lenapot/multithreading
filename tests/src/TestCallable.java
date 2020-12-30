import java.util.Random;
import java.util.concurrent.*;

public class TestCallable {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future<Integer> future = executorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                System.out.println("Starting");
                Thread.sleep(500);
                System.out.println("Finished");

                Random random = new Random();
                int randomVal = random.nextInt(10);
                if (randomVal < 5){
                    throw new Exception("Something bad happened");
                }
                return randomVal;
            }
        });

        executorService.shutdown();
        try {
            int result = future.get(); //get дожидается окончания выполнения потока
            System.out.println(result);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            Throwable ex = e.getCause();
            System.out.println(ex.getMessage());
        }
    }
}
