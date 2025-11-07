import java.util.*;

class Process {
    int pid;        // Process ID
    int arrival;    // Arrival Time
    int burst;      // Burst Time
    int priority;   // Priority
    int remaining;  // Remaining Time
    int completion; // Completion Time
    int waiting;    // Waiting Time
    int turnaround; // Turnaround Time

    Process(int pid, int arrival, int burst, int priority) {
        this.pid = pid;
        this.arrival = arrival;
        this.burst = burst;
        this.priority = priority;
        this.remaining = burst;
    }
}

public class SchedulingAlgorithms {

    // First Come First Serve
    static void fcfs(List<Process> processes) {
        System.out.println("\n--- First Come First Serve (FCFS) ---");
        processes.sort(Comparator.comparingInt(p -> p.arrival));

        int time = 0;
        for (Process p : processes) {
            if (time < p.arrival)
                time = p.arrival;
            time += p.burst;
            p.completion = time;
            p.turnaround = p.completion - p.arrival;
            p.waiting = p.turnaround - p.burst;
        }
        printTable(processes);
    }

    // Shortest Job First (Preemptive - SRTF)
    static void sjfPreemptive(List<Process> processes) {
        System.out.println("\n--- Shortest Job First (Preemptive – SRTF) ---");
        int n = processes.size(), time = 0, completed = 0;

        while (completed < n) {
            Process shortest = null;
            for (Process p : processes) {
                if (p.arrival <= time && p.remaining > 0) {
                    if (shortest == null || p.remaining < shortest.remaining)
                        shortest = p;
                }
            }

            if (shortest == null) {
                time++;
                continue;
            }

            shortest.remaining--;
            time++;

            if (shortest.remaining == 0) {
                shortest.completion = time;
                shortest.turnaround = shortest.completion - shortest.arrival;
                shortest.waiting = shortest.turnaround - shortest.burst;
                completed++;
            }
        }

        printTable(processes);
    }

    // Priority Scheduling (Non-Preemptive)
    static void priorityNonPreemptive(List<Process> processes) {
        System.out.println("\n--- Priority Scheduling (Non-Preemptive) ---");
        int time = 0, completed = 0, n = processes.size();

        while (completed < n) {
            Process highest = null;
            for (Process p : processes) {
                if (p.arrival <= time && p.remaining > 0) {
                    if (highest == null || p.priority < highest.priority)
                        highest = p;
                }
            }

            if (highest == null) {
                time++;
                continue;
            }

            time += highest.burst;
            highest.remaining = 0;
            highest.completion = time;
            highest.turnaround = highest.completion - highest.arrival;
            highest.waiting = highest.turnaround - highest.burst;
            completed++;
        }
        printTable(processes);
    }

    // Round Robin (Preemptive)
    static void roundRobin(List<Process> processes, int quantum) {
        System.out.println("\n--- Round Robin (Preemptive) ---");
        Queue<Process> queue = new LinkedList<>();
        int time = 0, completed = 0, n = processes.size();
        int i = 0;

        processes.sort(Comparator.comparingInt(p -> p.arrival));

        while (completed < n) {
            while (i < n && processes.get(i).arrival <= time)
                queue.add(processes.get(i++));

            if (queue.isEmpty()) {
                time++;
                continue;
            }

            Process p = queue.poll();
            int exec = Math.min(quantum, p.remaining);
            p.remaining -= exec;
            time += exec;

            while (i < n && processes.get(i).arrival <= time)
                queue.add(processes.get(i++));

            if (p.remaining > 0)
                queue.add(p);
            else {
                p.completion = time;
                p.turnaround = p.completion - p.arrival;
                p.waiting = p.turnaround - p.burst;
                completed++;
            }
        }
        printTable(processes);
    }

    // Print Table
    static void printTable(List<Process> processes) {
        System.out.println("PID\tAT\tBT\tPR\tCT\tTAT\tWT");
        for (Process p : processes) {
            System.out.println(p.pid + "\t" + p.arrival + "\t" + p.burst + "\t" + p.priority + "\t" +
                               p.completion + "\t" + p.turnaround + "\t" + p.waiting);
        }
    }

    public static void main(String[] args) {
        List<Process> processes = new ArrayList<>();
        processes.add(new Process(1, 0, 5, 2));
        processes.add(new Process(2, 1, 3, 1));
        processes.add(new Process(3, 2, 8, 4));
        processes.add(new Process(4, 3, 6, 3));

        // Run each algorithm with fresh data
        fcfs(cloneProcesses(processes));
        sjfPreemptive(cloneProcesses(processes));
        priorityNonPreemptive(cloneProcesses(processes));
        roundRobin(cloneProcesses(processes), 2);
    }

    static List<Process> cloneProcesses(List<Process> processes) {
        List<Process> newList = new ArrayList<>();
        for (Process p : processes) {
            newList.add(new Process(p.pid, p.arrival, p.burst, p.priority));
        }
        return newList;
    }
}

