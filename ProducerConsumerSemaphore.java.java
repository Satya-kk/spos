import java.util.concurrent.Semaphore;

class ProducerConsumerSemaphore {
    static Semaphore mutex = new Semaphore(1);
    static Semaphore empty = new Semaphore(5); // Buffer size = 5
    static Semaphore full = new Semaphore(0);
    static int item = 0;

    static class Producer extends Thread {
        public void run() {
            try {
                while (true) {
                    empty.acquire();      // Wait if buffer full
                    mutex.acquire();      // Enter critical section
                    item++;
                    System.out.println("Producer produced item: " + item);
                    Thread.sleep(500);
                    mutex.release();      // Exit critical section
                    full.release();       // Signal buffer not empty
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class Consumer extends Thread {
        public void run() {
            try {
                while (true) {
                    full.acquire();       // Wait if buffer empty
                    mutex.acquire();      // Enter critical section
                    System.out.println("Consumer consumed item: " + item);
                    item--;
                    Thread.sleep(800);
                    mutex.release();      // Exit critical section
                    empty.release();      // Signal buffer has space
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Producer p = new Producer();
        Consumer c = new Consumer();
        p.start();
        c.start();
    }
}


