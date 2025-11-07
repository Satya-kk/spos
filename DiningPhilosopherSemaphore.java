import java.util.concurrent.Semaphore;

class DiningPhilosopherSemaphore {
    static Semaphore[] forks = new Semaphore[5];
    static Semaphore mutex = new Semaphore(1);

    static class Philosopher extends Thread {
        int id;

        Philosopher(int id) {
            this.id = id;
        }

        public void run() {
            try {
                while (true) {
                    System.out.println("Philosopher " + id + " is thinking.");
                    Thread.sleep((int) (Math.random() * 1000));

                    mutex.acquire(); // prevent circular wait
                    forks[id].acquire();
                    forks[(id + 1) % 5].acquire();
                    mutex.release();

                    System.out.println("Philosopher " + id + " is eating.");
                    Thread.sleep((int) (Math.random() * 1000));

                    forks[id].release();
                    forks[(id + 1) % 5].release();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < 5; i++)
            forks[i] = new Semaphore(1);

        for (int i = 0; i < 5; i++)
            new Philosopher(i).start();
    }
}
