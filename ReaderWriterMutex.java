import java.util.concurrent.locks.ReentrantLock;

class ReaderWriterMutex {
    static ReentrantLock readLock = new ReentrantLock();
    static ReentrantLock writeLock = new ReentrantLock();
    static int readCount = 0;

    static class Reader extends Thread {
        public void run() {
            try {
                readLock.lock();
                readCount++;
                if (readCount == 1)
                    writeLock.lock(); // first reader locks writers
                readLock.unlock();

                System.out.println(Thread.currentThread().getName() + " is reading...");
                Thread.sleep(500);

                readLock.lock();
                readCount--;
                if (readCount == 0)
                    writeLock.unlock(); // last reader unlocks writers
                readLock.unlock();

                System.out.println(Thread.currentThread().getName() + " finished reading.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class Writer extends Thread {
        public void run() {
            try {
                writeLock.lock();
                System.out.println(Thread.currentThread().getName() + " is writing...");
                Thread.sleep(1000);
                System.out.println(Thread.currentThread().getName() + " finished writing.");
                writeLock.unlock();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Reader r1 = new Reader();
        Reader r2 = new Reader();
        Writer w1 = new Writer();
        Reader r3 = new Reader();
        Writer w2 = new Writer();

        r1.setName("Reader-1");
        r2.setName("Reader-2");
        r3.setName("Reader-3");
        w1.setName("Writer-1");
        w2.setName("Writer-2");

        r1.start();
        w1.start();
        r2.start();
        r3.start();
        w2.start();
    }
}
