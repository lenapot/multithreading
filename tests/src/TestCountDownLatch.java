import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestCountDownLatch {
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(3);

        ExecutorService service = Executors.newFixedThreadPool(3);
        for(int i = 0; i<3; i++){
            service.submit(new Processor(countDownLatch));
        }
        countDownLatch.await();
        System.out.println("Lanch has been opened");
    }

}
class Processor implements Runnable{
    CountDownLatch countDownLatch;
    public Processor(CountDownLatch countDownLatch){
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        countDownLatch.countDown();
    }
}
