import java.util.*;

abstract class PageReplacement {
    protected int frames;
    protected int[] pages;
    protected List<Integer> memory;
    protected int pageFaults;

    public PageReplacement(int frames, int[] pages) {
        this.frames = frames;
        this.pages = pages;
        this.memory = new ArrayList<>();
        this.pageFaults = 0;
    }

    abstract void execute();

    public void printResult() {
        System.out.println("\nTotal Page Faults: " + pageFaults);
        System.out.println("Page Fault Rate: " + (double) pageFaults / pages.length);
    }
}

// ---------- FIFO Page Replacement ----------
class FIFO extends PageReplacement {
    public FIFO(int frames, int[] pages) {
        super(frames, pages);
    }

    @Override
    void execute() {
        Queue<Integer> queue = new LinkedList<>();

        System.out.println("\n--- FIFO Page Replacement ---");
        for (int page : pages) {
            if (!memory.contains(page)) {
                if (memory.size() == frames) {
                    int removed = queue.poll();
                    memory.remove(Integer.valueOf(removed));
                }
                memory.add(page);
                queue.add(page);
                pageFaults++;
                System.out.println("Page " + page + " -> Page Fault (Memory: " + memory + ")");
            } else {
                System.out.println("Page " + page + " -> No Fault (Memory: " + memory + ")");
            }
        }
    }
}

// ---------- LRU Page Replacement ----------
class LRU extends PageReplacement {
    public LRU(int frames, int[] pages) {
        super(frames, pages);
    }

    @Override
    void execute() {
        System.out.println("\n--- LRU Page Replacement ---");
        LinkedHashMap<Integer, Integer> recent = new LinkedHashMap<>();

        for (int page : pages) {
            if (!memory.contains(page)) {
                if (memory.size() == frames) {
                    int lruPage = recent.entrySet().iterator().next().getKey();
                    memory.remove(Integer.valueOf(lruPage));
                    recent.remove(lruPage);
                }
                memory.add(page);
                pageFaults++;
                System.out.println("Page " + page + " -> Page Fault (Memory: " + memory + ")");
            } else {
                System.out.println("Page " + page + " -> No Fault (Memory: " + memory + ")");
            }
            // Update recent usage
            recent.remove(page);
            recent.put(page, 1);
        }
    }
}

// ---------- Optimal Page Replacement ----------
class Optimal extends PageReplacement {
    public Optimal(int frames, int[] pages) {
        super(frames, pages);
    }

    @Override
    void execute() {
        System.out.println("\n--- Optimal Page Replacement ---");

        for (int i = 0; i < pages.length; i++) {
            int page = pages[i];
            if (!memory.contains(page)) {
                if (memory.size() == frames) {
                    int farthestIndex = -1, pageToReplace = -1;
                    for (int memPage : memory) {
                        int nextUse = Integer.MAX_VALUE;
                        for (int j = i + 1; j < pages.length; j++) {
                            if (pages[j] == memPage) {
                                nextUse = j;
                                break;
                            }
                        }
                        if (nextUse > farthestIndex) {
                            farthestIndex = nextUse;
                            pageToReplace = memPage;
                        }
                    }
                    memory.remove(Integer.valueOf(pageToReplace));
                }
                memory.add(page);
                pageFaults++;
                System.out.println("Page " + page + " -> Page Fault (Memory: " + memory + ")");
            } else {
                System.out.println("Page " + page + " -> No Fault (Memory: " + memory + ")");
            }
        }
    }
}

// ---------- Main Program ----------
public class PageReplacementSimulation {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of frames: ");
        int frames = sc.nextInt();

        System.out.print("Enter number of pages: ");
        int n = sc.nextInt();

        int[] pages = new int[n];
        System.out.println("Enter page reference string:");
        for (int i = 0; i < n; i++) {
            pages[i] = sc.nextInt();
        }

        System.out.println("\nChoose Page Replacement Algorithm:");
        System.out.println("1. FIFO");
        System.out.println("2. LRU");
        System.out.println("3. Optimal");
        int choice = sc.nextInt();

        PageReplacement algo = null;
        switch (choice) {
            case 1:
                algo = new FIFO(frames, pages);
                break;
            case 2:
                algo = new LRU(frames, pages);
                break;
            case 3:
                algo = new Optimal(frames, pages);
                break;
            default:
                System.out.println("Invalid choice!");
                System.exit(0);
        }

        algo.execute();
        algo.printResult();
    }
}
