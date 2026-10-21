import java.util.NoSuchElementException;
import java.util.Random;

/**
 * Bank Queue Simulation
 * Customers arrive randomly, wait in ONE shared FIFO queue,
 * and are served by the next free teller.
 * The queue is implemented from scratch (linked list) to show how it works.
 */
public class BankQueueSystem {

    // ---------- Generic FIFO queue (linked list) ----------
    static class LinkedQueue<T> {
        private class Node {
            T data;
            Node next;
            Node(T data) { this.data = data; }
        }

        private Node head, tail;
        private int size;

        void enqueue(T item) {              // O(1) - add at the back
            Node node = new Node(item);
            if (tail == null) head = tail = node;
            else { tail.next = node; tail = node; }
            size++;
        }

        T dequeue() {                       // O(1) - remove from the front
            if (head == null) throw new NoSuchElementException("Queue is empty");
            T data = head.data;
            head = head.next;
            if (head == null) tail = null;
            size--;
            return data;
        }

        T peek() {
            if (head == null) throw new NoSuchElementException("Queue is empty");
            return head.data;
        }

        boolean isEmpty() { return size == 0; }
        int size() { return size; }
    }

    // ---------- Domain classes ----------
    static class Customer {
        final int id;
        final String service;
        final int arrivalTime;
        final int serviceTime;

        Customer(int id, String service, int arrivalTime, int serviceTime) {
            this.id = id;
            this.service = service;
            this.arrivalTime = arrivalTime;
            this.serviceTime = serviceTime;
        }
    }

    static class Teller {
        final int number;
        Customer current;
        int busyUntil;

        Teller(int number) { this.number = number; }
        boolean isFree() { return current == null; }
    }

    // ---------- Simulation ----------
    static final String[] SERVICES = {"Deposit", "Withdrawal", "Account Opening", "Enquiry"};
    static final int[] DURATIONS   = {3, 2, 6, 1};   // minutes

    static String clock(int minute) {
        int total = 8 * 60 + minute;              // bank opens 08:00
        return String.format("%02d:%02d", total / 60, total % 60);
    }

    public static void main(String[] args) {
        final int CLOSING = 60;                   // arrivals stop after 60 minutes
        final int TELLERS = 2;
        final int ARRIVAL_CHANCE = 45;            // % chance a customer arrives each minute

        Random random = new Random(42);           // fixed seed = repeatable output
        LinkedQueue<Customer> queue = new LinkedQueue<>();
        Teller[] tellers = new Teller[TELLERS];
        for (int i = 0; i < TELLERS; i++) tellers[i] = new Teller(i + 1);

        int nextId = 1, served = 0, totalWait = 0, maxWait = 0, maxQueue = 0;

        System.out.println("=== Bank Queue Simulation (" + TELLERS + " tellers) ===");

        int t = 0;
        while (t < CLOSING || !queue.isEmpty() || anyBusy(tellers)) {

            // 1. Arrival: join the BACK of the queue
            if (t < CLOSING && random.nextInt(100) < ARRIVAL_CHANCE) {
                int type = random.nextInt(SERVICES.length);
                Customer c = new Customer(nextId++, SERVICES[type], t, DURATIONS[type]);
                queue.enqueue(c);
                System.out.printf("%s  Customer #%d arrives (%s). Queue length: %d%n",
                        clock(t), c.id, c.service, queue.size());
            }
            maxQueue = Math.max(maxQueue, queue.size());

            // 2. Tellers finish, then take the FRONT of the queue
            for (Teller teller : tellers) {
                if (!teller.isFree() && t >= teller.busyUntil) {
                    System.out.printf("%s  Teller %d finishes Customer #%d%n",
                            clock(t), teller.number, teller.current.id);
                    teller.current = null;
                }
                if (teller.isFree() && !queue.isEmpty()) {
                    Customer c = queue.dequeue();
                    int wait = t - c.arrivalTime;
                    totalWait += wait;
                    maxWait = Math.max(maxWait, wait);
                    served++;
                    teller.current = c;
                    teller.busyUntil = t + c.serviceTime;
                    System.out.printf("%s  Teller %d serves Customer #%d (%s) after waiting %d min%n",
                            clock(t), teller.number, c.id, c.service, wait);
                }
            }
            t++;
        }

        System.out.println("\n=== Summary ===");
        System.out.println("Customers served : " + served);
        System.out.printf("Average wait     : %.1f minutes%n", served == 0 ? 0.0 : (double) totalWait / served);
        System.out.println("Longest wait     : " + maxWait + " minutes");
        System.out.println("Longest queue    : " + maxQueue + " customers");
        System.out.println("Closed at        : " + clock(t));
    }

    static boolean anyBusy(Teller[] tellers) {
        for (Teller teller : tellers) if (!teller.isFree()) return true;
        return false;
    }
}
