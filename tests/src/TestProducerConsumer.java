import java.util.LinkedList;
import java.util.Queue;

public class TestProducerConsumer {
    public static void main(String[] args) throws InterruptedException {
        ProducerConsumer pc = new ProducerConsumer();
        Thread thread1 = new Thread(() -> {
            try {
                pc.produce();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Thread thread2 = new Thread(() -> {
            try {
                pc.consume();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread1.start(); thread2.start();
        thread1.join();  thread2.join();
    }
}

class ProducerConsumer {
    Queue<Integer> queue = new LinkedList();
    Object lock = new Object();
    private static int LIMIT = 10;
    public void produce() throws InterruptedException {
        int val = 0;
        while(true) {
            synchronized (lock) {
                while (queue.size() == LIMIT){
                    lock.wait();
                }
                queue.offer(val++);
                System.out.println("Producer produced " + val);

                lock.notify();
            }
        }
    }
    public void consume() throws InterruptedException{
        while(true) {
            synchronized (lock) {
                while (queue.size() == 0){
                    lock.wait();
                }
                int val = queue.poll();
                System.out.println(val);
                System.out.println("Queue size is " + queue.size());
                lock.notify();
            }
            Thread.sleep(1000);
        }
    }
}
