import java.util.LinkedList;

class ProducerConsumerMutex {
    LinkedList<Integer> buffer = new LinkedList<>();
    int capacity = 5; // buffer size
    final Object mutex = new Object(); // mutex lock

    class Producer extends Thread {
        public void run() {
            int item = 0;
            while (true) {
                synchronized (mutex) {
                    while (buffer.size() == capacity) {
                        try {
                            mutex.wait(); // wait if buffer full
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    item++;
                    buffer.add(item);
                    System.out.println("Producer produced item: " + item);
                    mutex.notify(); // signal consumer
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class Consumer extends Thread {
        public void run() {
            while (true) {
                synchronized (mutex) {
                    while (buffer.isEmpty()) {
                        try {
                            mutex.wait(); // wait if buffer empty
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    int item = buffer.removeFirst();
                    System.out.println("Consumer consumed item: " + item);
                    mutex.notify(); // signal producer
                }
                try {
                    Thread.sleep(800);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        ProducerConsumerMutex pc = new ProducerConsumerMutex();
        Producer p = pc.new Producer();
        Consumer c = pc.new Consumer();
        p.start();
        c.start();
    }
}

