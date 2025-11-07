import java.util.concurrent.locks.ReentrantLock;

class DiningPhilosopherMutex {
    static ReentrantLock[] forks = new ReentrantLock[5];

    static class Philosopher extends Thread {
        int id;

        Philosopher(int id) {
            this.id = id;
        }

        public void run() {
            while (true) {
                try {
                    System.out.println("Philosopher " + id + " is thinking.");
                    Thread.sleep((int) (Math.random() * 1000));

                    forks[id].lock();
                    forks[(id + 1) % 5].lock();

                    System.out.println("Philosopher " + id + " is eating.");
                    Thread.sleep((int) (Math.random() * 1000));

                    forks[id].unlock();
                    forks[(id + 1) % 5].unlock();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < 5; i++)
            forks[i] = new ReentrantLock();

        for (int i = 0; i < 5; i++)
            new Philosopher(i).start();
    }
}
