import java.util.*;

class MemoryBlock {
    int id;
    int size;
    boolean allocated;

    public MemoryBlock(int id, int size) {
        this.id = id;
        this.size = size;
        this.allocated = false;
    }
}

class Process {
    int id;
    int size;
    int blockAllocated = -1;

    public Process(int id, int size) {
        this.id = id;
        this.size = size;
    }
}

abstract class MemoryAllocation {
    protected List<MemoryBlock> blocks;
    protected List<Process> processes;

    public MemoryAllocation(List<MemoryBlock> blocks, List<Process> processes) {
        this.blocks = blocks;
        this.processes = processes;
    }

    abstract void allocate();

    public void printResult() {
        System.out.println("\nProcess\tSize\tBlock Allocated");
        for (Process p : processes) {
            if (p.blockAllocated != -1)
                System.out.println(p.id + "\t" + p.size + "\tBlock " + p.blockAllocated);
            else
                System.out.println(p.id + "\t" + p.size + "\tNot Allocated");
        }
    }
}

// ---------- FIRST FIT ----------
class FirstFit extends MemoryAllocation {
    public FirstFit(List<MemoryBlock> blocks, List<Process> processes) {
        super(blocks, processes);
    }

    @Override
    void allocate() {
        System.out.println("\n--- First Fit Allocation ---");
        for (Process p : processes) {
            for (MemoryBlock b : blocks) {
                if (!b.allocated && b.size >= p.size) {
                    p.blockAllocated = b.id;
                    b.size -= p.size;
                    if (b.size == 0) b.allocated = true;
                    break;
                }
            }
        }
    }
}

// ---------- BEST FIT ----------
class BestFit extends MemoryAllocation {
    public BestFit(List<MemoryBlock> blocks, List<Process> processes) {
        super(blocks, processes);
    }

    @Override
    void allocate() {
        System.out.println("\n--- Best Fit Allocation ---");
        for (Process p : processes) {
            MemoryBlock bestBlock = null;
            for (MemoryBlock b : blocks) {
                if (!b.allocated && b.size >= p.size) {
                    if (bestBlock == null || b.size < bestBlock.size)
                        bestBlock = b;
                }
            }
            if (bestBlock != null) {
                p.blockAllocated = bestBlock.id;
                bestBlock.size -= p.size;
                if (bestBlock.size == 0) bestBlock.allocated = true;
            }
        }
    }
}

// ---------- WORST FIT ----------
class WorstFit extends MemoryAllocation {
    public WorstFit(List<MemoryBlock> blocks, List<Process> processes) {
        super(blocks, processes);
    }

    @Override
    void allocate() {
        System.out.println("\n--- Worst Fit Allocation ---");
        for (Process p : processes) {
            MemoryBlock worstBlock = null;
            for (MemoryBlock b : blocks) {
                if (!b.allocated && b.size >= p.size) {
                    if (worstBlock == null || b.size > worstBlock.size)
                        worstBlock = b;
                }
            }
            if (worstBlock != null) {
                p.blockAllocated = worstBlock.id;
                worstBlock.size -= p.size;
                if (worstBlock.size == 0) worstBlock.allocated = true;
            }
        }
    }
}

// ---------- NEXT FIT ----------
class NextFit extends MemoryAllocation {
    private int lastIndex = 0;

    public NextFit(List<MemoryBlock> blocks, List<Process> processes) {
        super(blocks, processes);
    }

    @Override
    void allocate() {
        System.out.println("\n--- Next Fit Allocation ---");
        int n = blocks.size();
        for (Process p : processes) {
            int count = 0;
            boolean allocated = false;
            while (count < n) {
                MemoryBlock b = blocks.get(lastIndex);
                if (!b.allocated && b.size >= p.size) {
                    p.blockAllocated = b.id;
                    b.size -= p.size;
                    if (b.size == 0) b.allocated = true;
                    allocated = true;
                    break;
                }
                lastIndex = (lastIndex + 1) % n;
                count++;
            }
        }
    }
}

// ---------- MAIN ----------
public class MemoryAllocationSimulation {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of memory blocks: ");
        int m = sc.nextInt();
        List<MemoryBlock> blocks = new ArrayList<>();
        for (int i = 0; i < m; i++) {
            System.out.print("Enter size of block " + (i + 1) + ": ");
            blocks.add(new MemoryBlock(i + 1, sc.nextInt()));
        }

        System.out.print("Enter number of processes: ");
        int n = sc.nextInt();
        List<Process> processes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            System.out.print("Enter size of process " + (i + 1) + ": ");
            processes.add(new Process(i + 1, sc.nextInt()));
        }

        System.out.println("\nChoose Allocation Strategy:");
        System.out.println("1. First Fit");
        System.out.println("2. Best Fit");
        System.out.println("3. Worst Fit");
        System.out.println("4. Next Fit");
        int choice = sc.nextInt();

        MemoryAllocation strategy = null;
        switch (choice) {
            case 1:
                strategy = new FirstFit(cloneBlocks(blocks), processes);
                break;
            case 2:
                strategy = new BestFit(cloneBlocks(blocks), processes);
                break;
            case 3:
                strategy = new WorstFit(cloneBlocks(blocks), processes);
                break;
            case 4:
                strategy = new NextFit(cloneBlocks(blocks), processes);
                break;
            default:
                System.out.println("Invalid choice!");
                System.exit(0);
        }

        strategy.allocate();
        strategy.printResult();
    }

    // clone blocks to avoid modifying the original list
    private static List<MemoryBlock> cloneBlocks(List<MemoryBlock> original) {
        List<MemoryBlock> copy = new ArrayList<>();
        for (MemoryBlock b : original)
            copy.add(new MemoryBlock(b.id, b.size));
        return copy;
    }
}

