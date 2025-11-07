import java.util.concurrent.Semaphore;

class ReaderWriterSemaphore {
    static Semaphore wrt = new Semaphore(1);
    static Semaphore mutex = new Semaphore(1);
    static int readCount = 0;

    static class Reader extends Thread {
        public void run() {
            try {
                mutex.acquire();
                readCount++;
                if (readCount == 1)
                    wrt.acquire(); // First reader locks writing
                mutex.release();

                System.out.println(Thread.currentThread().getName() + " is reading");
                Thread.sleep(500);

                mutex.acquire();
                readCount--;
                if (readCount == 0)
                    wrt.release(); // Last reader releases writing
                mutex.release();

                System.out.println(Thread.currentThread().getName() + " finished reading");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class Writer extends Thread {
        public void run() {
            try {
                wrt.acquire();
                System.out.println(Thread.currentThread().getName() + " is writing");
                Thread.sleep(1000);
                System.out.println(Thread.currentThread().getName() + " finished writing");
                wrt.release();
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

