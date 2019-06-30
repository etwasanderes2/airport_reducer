package ads.set4.airports;

/*

README:

[TLDR: I messed around, don't put any effort into correcting]

So I was tasked with implementing a minimal spanning tree algorthm.
I decided on Boruvka & Sollin because it gave more points.
Then I noticed that paralellization was a possibility and started writing multiple Threads.
[SPOILER ALERT: I didn't help with performance at all]
Then I noticed that even more paralellization was possible and wrote even more Threads.
Then I also made some Datastructures for doing things that seemed useful.
All in all I was just messing around a lot, therefore I don't expect you to put any effort into correcting this ugly mess of code.
I'm planning on writing Dijkstra, so getting 0 Points won't matter anyway.

With that being said, this is the project structure:

Datastructures:

    TreePartitioning:
        Fully featured Union-Find

    SpecialArray:
        Used to keep track of roots in TreePartitioning, delte Elements in O(1), quick to iterate over.
        Could be replaced with a Set, but ...

    SetGroup:
        makes a "Union" of sets without actually union-fying them.
        CAN CONTAIN DUPLICATE ELEMENTS.

Threads:

    States:
        PAUSE: wait for notify()
        CALCULATE: calculate whatever you need to calculate
        TERMINATE: terminate after next notify

    //all threads are started at the beginning of the algorithm and simply idle when not used to reduce overhead.

    Shortest Connection Finder [SCF]:
        Manager divides work.
        Threads find shortest Connection leaving a component in their subset of connections

    Summary:
        Manager provides work to Thread asking for it (This probably bottlenecks this to the point of uselessness, not that it would be anywhere else without)
        Threads compare all the results (generated by SCF) for a component assigned by manager, build their own set of shortest.


Results [SPOILER ALERT]:
    No matter what I did, the speed barely changed.
    Except when I replaced all the Sets/Maps with Arrays it doubled. And ate a lot of memory. But who cares about memory?

Sidenote:
    The plural of sudoku isn't sudokii; I made that up because I needed something that sounded like the plural of sudoku.
    I googled it and it's actually "sudoku" or "sudokus" in some cases, for example when referring to multiple types of sudoku.

I document my journey though the world of paralellization in my following Masterplan (Because everything I did, I planned from the start, like a responsible programmer would)
Lessons learned at end of document.
 */

/*

The Masterplan (because any other kind of plan is just not worth it):
Implement Boruvka & Sollin

Day 1 (Let's write everything down day):

Step 01: Make TreePartitioning Class that manages an Partition-Trees Datastructure.
Step 02: "This Algorithm is parallelizeable!!!"
Step 03: Make FSCThread Class for finding the shortest Connection in a given Subset of Connections
Step 04: Layer loop 4 layers deep to find the shortest Connections in the entire Set.
Step 05: Get annoyed an make lt Method for comparing Connections that values null as infinite.
Step 06: Benchmark everything.
Step 07: Notice that 1 Thread would have been faster.
Step 08: "But I tried, dammit".

Day 2 (Optimization day):

Step 01: Try profiling
Step 02: get annoyed at visualvm for only having rotten links on any online resources
Step 03: try JProfiler
Step 04: Can't get JProfiler to connect
Step 05: get JProfiler to connect
Step 06: segfault
Step 07: get annoyed
Step 08: Manage to get it working, but it's really annoying
Step 09: Try to record CPU Information
Step 10: Fail

Day 3 (Getting stuff done day [hopefully]):

Step 01: "Make Threads is probably slow. Let's reuse the old Threads instead"
Step 02: Make enum States for Thread states.
Step 03: Notice this won't work.
Step 04: Write ThreadManager class to manage various things (threads, code-cleanness, my sanity,...)
Step 05: "I need to make this prettier"
Step 06: Refactor basically everything.
Step 07: Concurrency problems.
Step 08: Fix concurrency problems.
Step 09: "IT WORKS!!!"
Step 10: Benchmark everything.
Step 11: Slightly faster sometimes.
Step 12: Make custom tests for bigger Benchmarks
Step 13: 10000 Airports with 9499500 Connections took 9356ms with 1 Threads
Step 14: 10000 Airports with 9499500 Connections took 7971ms with 4 Threads
Step 15: 10000 Airports with 9499500 Connections took 7505ms with 2 Threads
Step 16: Be confused.
Step 17: "Maybe I need a better way to save intermediate results. Maybe I need different Datastructures."
Step 18: 10000 Airports with 9499500 Connections took 7311ms with 3 Threads
Step 19: "???"
Step 20: "How can I save these things well?"
Step 21: Try with lots of threads.
Step 22: Accidentally use 32bit jre.
Step 23: Really slow.
Step 24: Try with 64bit.
Step 25: About the same speed as the other tests.

Day 4 (This is probably a bad idea day):

Step 01: "Oh shit the Algorithm can break"
Step 02: Fix lt method to avoid breaking.
Step 03: "Wait there are other things I can parallelize"
Step 04: "more threads = better"
Step 05: "Who needs memory efficiency?"
Step 06: "Actually, I have a different idea..."
Step 07: "I can do both!"
Step 08: "Wait, no, the second idea was bad"
Step 09: Modify TreePartitioning class to keep track of roots.
Step 10: Notice that would be really slow
Step 11: "How do ArrayLists work?"
Step 12: Come up with new Datastructure that is a linked list slapped onto an array.
Step 13: Write SpecialList class
Step 14: "Wait a HashSet would probably be just a little bit slower"
Step 15: "But half as fun"
Step 16: Write Unit test.
Step 17: Get distracted.
Step 18: Come up with a way more memory-efficient way to implement my Datastructure. Also rename it SpecialArray.
Step 19: Check how long the other stuff I wanted to parallelize takes.
Step 20: Notice it's really quick.
Step 21: Become sad, because my optimizations won't help.
Step 22: "Do it anyway?"

Day 5 (Executing my bad ideas day [if I get the time]):

Step 01: Procrastinate.
Step 02: Rewrite SCFThread to use Arrays for results "yay memory".
Step 03: Rewrite ThreadManager to return raw results.
Step 04: Write SummaryManager & SummaryThread Class for summarizing results.
Step 05: Write class SetGroup & Iterator for easier Itereratability.
Step 06: Mysterious Error.
Step 07: Write Unit tests.
Step 08: Don'T find it.
Step 09: Find Bug.
Step 10: New Bug.
Step 11: Get frustrated.
Step 12: Find incredibly stupid bug.
Step 13: "IT WORKS!!!"
Step 14: Remove println-debug-statements.
Step 15: Benchmark.
Step 16: Significantly faster.
Step 17: "???" (-> D-4 S-19)
Step 18: Benchmark with really big Datasets.
Step 17: Close to no speed difference.
Step 18: "Hey, they pushed back the deadline"
Step 19: Add Javadocs to everything & make intro.
Step 20: Get timeouts with time significantly longer than anything I've seen on my PC.
Step 21: See Interrupted Exceptions.
Step 22: I never interrupt my threads, so it must be caused by the timeout.
Step 23: But none of my things need 200ms.
Step 24: Probably high server load.
Step 25: Try with 1 Thread.

 */

import java.util.*;

import static ads.set4.airports.AirportReducer.lt;
import static java.lang.Integer.min;

/**
 * Iterator for the {@link SetGroup} Class.
 * @param <T> the type of the elements.
 */
class SetGroupIterator<T> implements Iterator<T>{

    /** Iterates over the sets. */
    Iterator<Set<T>> setIterator;
    /** Iterates over the elements in the set {@code setIterator} is currently at. */
    Iterator<T> elementIterator;

    /**
     * Constructs an Iterator.
     * @param group the {@link SetGroup} object to iterate over.
     */
    SetGroupIterator(SetGroup<T> group) {
        setIterator = group.collection.iterator();
        if (setIterator.hasNext()) {
            elementIterator = setIterator.next().iterator();
        } else {
            elementIterator = null;
        }
    }

    @Override
    public boolean hasNext() {
        if (elementIterator == null) return false;
        if (elementIterator.hasNext()) return true;
        while (! elementIterator.hasNext()) {
            if (setIterator.hasNext()) {
                elementIterator = setIterator.next().iterator();
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public T next() {
        return elementIterator.next();
    }
}

/**
 * Manages a "union" of sets without actually "unionffying" them.
 * MAY CONTAIN DUPLICATE ELEMENTS! [this caused a major headache]
 * Only used methods are implemented.
 * @param <T> the type of the  elements.
 */
class SetGroup<T> implements Iterable<T>{

    /** stores all the sets */
    LinkedList<Set<T>> collection;

    /**
     * Basic constructor.
     */
    SetGroup() {
        collection = new LinkedList<Set<T>>();
    }

    /**
     * Adds a new set to the group.
     * @param s
     */
    void add(Set<T> s) {
        collection.add(s);
    }

    @Override
    public Iterator<T> iterator() {
        return new SetGroupIterator<T>(this);
    }
}

/**
 * Iterator for {@link SpecialArray}
 */
class SpecialArrayIterator implements Iterator<Integer> {

    /** Holds a refernce to the {@link SpecialArray} object used by this Iterator */
    SpecialArray list;
    /** The current element */
    int current;

    /**
     * Constructs Iterator for {@link SpecialArray}
     * @param list the {@link SpecialArray}
     */
    SpecialArrayIterator(SpecialArray list) {
        this.list = list;
        current = list.getFirst();
    }

    @Override
    public boolean hasNext() {
        return current != list.next.length;
    }

    @Override
    public Integer next() {
        int c = current;
        current = list.next[current];
        return c;
    }
}

/**
 * Manages a special array. This only uses ints.
 * Think of a linked list in which every entry is also in an Array.
 * Actually use two arrays for saving previous and next and a magic number for empty.
 * This means you can delete item in constant time and iterate over the elements in linear time based on the amount of items left in the Array.
 */
class SpecialArray implements Iterable<Integer> {

    //-1    start
    //len   end
    //-2    null

    /** Holds the next value for each value. {@code next.length} for last element. */
    int[] next;
    /** Holds the previous value for each value. {@code -1} for first element */
    int[] prev;
    /** Holds the amount of items currently in the datastructure */
    private int size;
    /** The value of the first Element (like first element pointer for regular linked list) */
    private int first;

    /**
     * Constructor  initializes with values from {@code 0} to (excl.) {@code size}
     * @param size the amount of items
     */
    public SpecialArray(int size) {
        next = new int[size];
        prev = new int[size];
        this.size = size;
        first = 0;

        if (size == 0) {
            return;
        }
        if (size == 1) {
            next[0] = next.length;
            prev[0] = -1;
            return;
        }
        {
            assert size >= 2 : "wrong size";

            for (int i = 0; i < size; i++) {
                next[i] = i+1;
                prev[i] = i-1;
            }
        }

    }

    //typical collection functions

    /**
     * Deletes a value from the array.
     * Runs in O(1).
     * @param value the value
     */
    void delete(int value) {
        //input is probably fine
        assert value >= 0 : "too small";
        assert value < next.length : "too big";

        if (prev[value] != -1) {
            next[prev[value]] = next[value];
        } else {
            assert  value == first: "desync";
            first = next[value];
        }

        if (next[value] != next.length) {
            prev[next[value]] = prev[value];
        }

        next[value] = -2;

        size--;
    }

    @Override
    public Iterator iterator() {
        return new SpecialArrayIterator(this);
    }

    /**
     * Checks whether an element exists in the array.
     * Runs in O(1).
     * @param pos the element
     * @return {@code true} if in array
     */
    boolean exists(int pos) {
        return next[pos] != -2;
    }

    /**
     * Getter for the first value.
     * Runs in O(1).
     * @return the smalles value in the array.
     */
    public int getFirst() {
        return first;
    }

    /**
     * Typical size function.
     * @return the amount of elements left in the array.
     */
    public int size() {
        return size;
    }
}


/**
 * Manages a Partitioning. Should be sufficiently thread-save for my purposes.
 */
class TreePartitioning {

    /** negative numbers indicate # of nodes */
    private int[] partitionTable;
    /** Holds all the roots */
    private SpecialArray roots;

    /**
     * Constructor.
     * @param size the amount of elements in the structure.
     */
    TreePartitioning(int size) {
        partitionTable = new int[size];
        roots = new SpecialArray(size);
        Arrays.fill(partitionTable, -1);
    }

    /**
     * Compresses a path.
     * @param from the node from witch to compress the path.
     * @param root the root of the assiciated tree.
     */
    private void compressPath(final int from, final int root) {
        int current = from;
        while (current != root) {
            int next = partitionTable[current];
            partitionTable[current] = root;
            current = next;
        }
    }

    /**
     * Calculates the root of any element in the structure.
     * Compresses path if possible.
     * @param a element to calculate the root for
     * @return the root of the tree of {@code a}. {@code a} if {@code a} is a root.
     */
    int rootOf(final int a) {
        int parent = partitionTable[a];
        // a is root
        if (parent < 0) {
            return a;
        }

        //a is connected to root
        if (partitionTable[parent] < 0) {
            return parent;
        }

        // a is somewhere else
        while (partitionTable[parent] >= 0) {
            parent = partitionTable[parent];
        }

        compressPath(a, parent);
        return parent;
    }

    /**
     * Unionify two partitions.
     * @param a Arbitrary element of partition a
     * @param b Arbitrary element of partition b
     * @return The new root
     */
    synchronized int union(int a, int b) {
        int rootA = rootOf(a);
        int rootB = rootOf(b);

        if (rootA == rootB) return rootA;

        if (partitionTable[rootA] < partitionTable[rootB]) {
            int bSize = partitionTable[rootB];  //store size of b tree
            partitionTable[rootB] = rootA;      //update root of b
            partitionTable[rootA] += bSize;     //update size
            roots.delete(rootB);
            assert partitionTable[rootA] < 0 : "Failure updating sizes";
            return rootA;
        } else {
            assert partitionTable[rootB] < 0 : "not a root: b";
            assert partitionTable[rootA] < 0 : "not a root: a";
            int aSize = partitionTable[rootA];  //store size of a tree
            partitionTable[rootA] = rootB;      //update root of a
            partitionTable[rootB] += aSize;     //update size
            roots.delete(rootA);
            assert partitionTable[rootB] < 0 : "Failure updating sizes: " + partitionTable[rootB] + ":" + partitionTable[rootA] + ", joining: " + a + " " + b + " with roots: " + rootA + " " + rootB + " newly calculated roots: " + rootOf(a)+ " " +rootOf(b);
            return rootB;
        }
    }

    /**
     * Getter.
     * @return amount of partitions
     */
    int getPartitionCount() {
        return roots.size();
    }

    /**
     * Getter.
     * @return amount of elements
     */
    public int size() {
        return partitionTable.length;
    }

    /**
     * Getter.
     * @return the roots
     */
    public SpecialArray roots() {
        return roots;
    }
}

/**
 * Manages SCF Threads.
 */
class SCFThreadManger {
    private Thread[] threads;
    private SCFThread[] threadObjects;
    private int runningThreads;

    /**
     * Construcor.
     * @param totalWork all the Connections that need to be analyzed.
     * @param chunks into how man threads to split
     * @param components pointer to the partitioning table
     */
    SCFThreadManger(Set<Connection> totalWork, int chunks, TreePartitioning components) {
        //Setting up Threads
        int conSize = totalWork.size();
        int chunksize = conSize / chunks;
        int leftovers = conSize % chunks;

        //all the connections arranged to threads
        Connection[][] tasks = new Connection[chunks][];

        //init fist Dimension of Array & Threads
        threadObjects = new SCFThread[chunks];
        threads = new Thread[chunks];

        for (int chunkId = 0; chunkId < chunks; chunkId++) {

            //fast (hopefully)
            if (chunkId < leftovers) {
                tasks[chunkId] = new Connection[chunksize+1];
            } else {
                tasks[chunkId] = new Connection[chunksize];
            }

            threads[chunkId] = new Thread( threadObjects[chunkId] = new SCFThread(tasks[chunkId], components, this));    //empty now, filled later
        }

        //init second Dimension
        int chunkId = 0;
        int taskId = 0;
        for (Connection each: totalWork) {
            //managing ids (imagine this as an if, then change to while for really small graphs)
            while (taskId >= tasks[chunkId].length) {
                taskId = 0;
                chunkId++;
            }
            tasks[chunkId][taskId] = each;  //the bounds should always be ok, bc of the way the Array was set up.
            taskId++;
        }

        //starting threads
        for (Thread each : threads) {
            each.start();
        }
    }

    /**
     * Just a more verbose version of {@code threads.length}
     * @return {@code threads.length}
     */
    private int threatCount() {return threads.length;}  //inline pls

    /**
     * Call when a thread is done.
     */
    synchronized void threadDone() {
        runningThreads--;
        if (runningThreads <= 0) notify();
    }

    /**
     * Does the actual work.
     * Blocking.
     */
    void calculate() {


        for (SCFThread each : threadObjects) {
            assert each.getTask() == States.PAUSE : "Thread in wrong State";
        }

        runningThreads = threatCount();
        for (SCFThread each : threadObjects) {
            each.setTask(States.CALCULATE);
            each.doSomething();
        }

        try {
            synchronized (this) {
                if (runningThreads > 0) {
                    this.wait();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Getter.
     * @return the raw results {@code [thread] [root of partition] = shortest connection}
     */
    Connection[][] raw_results() {

        for (SCFThread each : threadObjects) {
            assert each.getTask() == States.PAUSE : "Thread still running";
        }

        Connection[][] all = new Connection[threatCount()][];
        for (int i = 0; i < threatCount(); i++) {
            all[i] = threadObjects[i].getShortest();
        }
        return all;
        //could be optimized by putting the values directly in the right place, but that is just not the java way.
    }

    /**
     * Ends all the threads.
     */
    public void terminate() {
        for (SCFThread each : threadObjects) {
            each.setTask(States.TERMINATE);
            each.doSomething();
        }
    }
}

/**
 * Possible thread states
 */
enum States {
    PAUSE,
    CALCULATE,
    TERMINATE
}

/**
 * Finds the shortest leaving Connections for a given array of connections and partitioning.
 * S.C.F. : Shortest Connection Finding Thread
 */
class SCFThread implements Runnable {

    /** saves the set of connections to check */
    private Connection[] connections;
    /** pointer to manager for counting */
    private SCFThreadManger manager;
    /** just a pointer to the partition table */
    private TreePartitioning part;
    /** maps roots of partition to shortest outgoing connection (the thing we want to calculate) */
    Connection[] shortest;
    /** saves state */
    private States task = States.PAUSE;

    /**
     * Constructor.
     * @param connections Array of {@link Connection}s to analyze
     * @param part  Pointer to the partitioning table
     * @param threadManger Pointer to the manager
     */
    public SCFThread(Connection[] connections, TreePartitioning part, SCFThreadManger threadManger) {
        this.connections = connections;
        manager = threadManger;
        shortest = new Connection[part.size()];
        this.part = part;
    }

    /**
     * Analyzes all {@link Connection}s in the set specified by the constructor.
     */
    private void calculate() {
        shortest = new Connection[part.size()];
        for (Connection each : connections) {
            int a = each.getAirport1();
            int b = each.getAirport2();

            int rootA = part.rootOf(a);
            int rootB = part.rootOf(b);

            if (rootA != rootB) { //into different partition
                int cost = each.getCost();

                //I love Java expressions: "shortest.putIfAbsent(rootA, each) != null && cost < shortest.get(rootA).getCost()" [didn't use]

                // A is shorter
                {
                    Connection curS = shortest[rootA];
                    if (curS == null || curS.getCost() > cost) {    //yay for short-circuit
                        shortest[rootA] = each;
                    }
                }
                // B is shorter
                {
                    Connection curS = shortest[rootB];
                    if (curS == null || curS.getCost() > cost) {    //yay for short-circuit
                        shortest[rootB] = each;
                    }
                }
            }
        }
    }

    /**
     * Main function of the thread.
     * Checks what to do on state.
     * {@code States.PAUSE}: wait for {@code notify()}
     * {@code States.CALCULATE}: call {@code calculate()}
     * {@code States.TERMINATE}: terminate
     */
    public void run() {
        mainLoop:
        while (true) {
            switch (task) {
                case PAUSE:
                    try {
                        synchronized (this) {
                            this.wait();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case CALCULATE:
                    calculate();
                    setTask(States.PAUSE);
                    manager.threadDone();
                    break;
                case TERMINATE:
                    break mainLoop;
            }
        }
    }

    /**
     * Getter.
     * @return array of the shortest {@link Connection}s
     */
    Connection[] getShortest() {
        return shortest;
    }

    /**
     * Getter.
     * @return current state
     */
    States getTask() {
        return task;
    }

    /**
     * Setter.
     * @param task current state
     */
    synchronized void setTask(States task) {
        this.task = task;
    }

    /**
     * notify() wrapper.
     */
    synchronized void doSomething() {
        notify();
    }
}

/**
 * Manages {@link SummaryThread}s
 */
class SummaryManager {

    /** Raw results as produced by {@link SCFThreadManger} */
    Connection[][] raw_results;
    /** Array of {@link Thread}s */
    private Thread[] threads;
    /** Array of {@link SummaryThread}s associated with {code threads} */
    private SummaryThread[] threadObjects;
    /** Pointer to the partitioning table */
    private TreePartitioning components;
    /** All the roots */
    private Iterator<Integer> roots;
    /** Keeps track of running thread count */
    private int runningThreads;

    /**
     * Verbose wrapper for {@code threads.length}.
     * @return {@code threads.length}
     */
    private int threatCount() {
        return threads.length;
    }

    /**
     *
     * results
     *
     * Calculates and gets results. call {@code calculate()} first
     * @return set of shortest connections
     */
    SetGroup<Connection> getResults() {
        SetGroup<Connection> results = new SetGroup<Connection>();
        for (SummaryThread each : threadObjects) {
            results.add(each.getResults());
        }
        return results;
    }

    /**
     * Constructor.
     * @param threadCount Amount of threads to use
     * @param components Pointer to partitioning-table
     */
    public SummaryManager(int threadCount, TreePartitioning components) {
        threads = new Thread[threadCount];
        threadObjects = new SummaryThread[threadCount];
        this.components = components;
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread( threadObjects[i] = new SummaryThread(this) );
            threadObjects[i].setTask(States.PAUSE);
            threads[i].start();
        }
    }

    /**
     * Call when thread done.
     */
    synchronized void threadDone() {
        runningThreads--;
        if (runningThreads <= 0) notify();
    }

    /**
     * gives tasks to threads
     * @return task no. or -1 if end
     */
    synchronized int getTask() {
        if (roots.hasNext()) {
            return roots.next();
        } else {
            return -1;
        }
    }

    /**
     * Ends all the threads.
     */
    public void terminate() {
        for (SummaryThread each : threadObjects) {
            each.setTask(States.TERMINATE);
            each.doSomething();
        }
    }

    /**
     * Does the actual calculations.
     * @param raw_results raw result as outputted by {@link SCFThreadManger}
     */
    void calculate(Connection[][] raw_results) {

        roots = components.roots().iterator();
        this.raw_results = raw_results;

        for (SummaryThread each : threadObjects) {
            assert each.getTask() == States.PAUSE : "Thread in wrong State";
        }

        runningThreads = threatCount();
        for (SummaryThread each : threadObjects) {
            each.setTask(States.CALCULATE);
            each.doSomething();
        }

        try {
            synchronized (this) {
                if (runningThreads > 0) {
                    this.wait();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

/**
 * Thread for summarizing the results of {@link SCFThreadManger}
 */
class SummaryThread implements Runnable {

    /** Pointer to manager */
    private SummaryManager manager;
    /** Current state */
    private States task;
    /** Set to accumulate results */
    private HashSet<Connection> results;

    /**
     * Constructor.
     * @param manager {@link SummaryManager}
     */
    SummaryThread(SummaryManager manager) {
        this.manager = manager;
    }

    /**
     * Does the actual calculations
     */
    private void calculate() {
        results = new HashSet<>();
        int task;
        while ((task = manager.getTask()) != -1) {

            //find minimal Connection
            Connection shortest = manager.raw_results[0][task];
            for (int i = 1; i < manager.raw_results.length; i++) {
                if (lt(manager.raw_results[i][task], shortest)) {
                    shortest = manager.raw_results[i][task];
                }
            }
            if (shortest != null){
                results.add(shortest);
            }
        }
    }

    @Override
    public void run() {
        mainLoop:
        while (true) {
            switch (task) {
                case PAUSE:
                    try {
                        synchronized (this) {
                            this.wait();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case CALCULATE:
                    calculate();
                    setTask(States.PAUSE);
                    manager.threadDone();
                    break;
                case TERMINATE:
                    break mainLoop;
            }
        }
    }

    /**
     * Getter.
     * @return the current state of the thread.
     */
    States getTask() {
        return task;
    }

    /**
     * Setter.
     * @param task new State
     */
    synchronized void setTask(States task) {
        this.task = task;
    }

    /**
     * Wrapper for {@code notify()}.
     */
    synchronized void doSomething() {
        notify();
    }

    /**
     * Getter.
     * @return the shortest Connections
     */
    HashSet<Connection> getResults() {
        return results;
    }
}

@SuppressWarnings("WeakerAccess")
public class AirportReducer {

    /**
     * Does <, takes care of null the way I want it.
     * @param a
     * @param b
     * @return a < b, whereby {@code null} is considered infinite.
     */
    static boolean lt(Connection a, Connection b) {
        if (a == null) return false;
        if (b == null) return true;
        if (a.getCost() == b.getCost()) return min(a.getAirport1(), a.getAirport2()) < min(b.getAirport1(), b.getAirport2());   //edgecase
        return a.getCost() < b.getCost();
    }

    /** number of threads */
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    //private  static  final int THREAD_COUNT = 1;

    /**
     * Finds a minimal spanning tree in the given graph.
     *
     * @param airportCount
     *            the number of airports we're considering.
     * @param connections
     *            a set of connections between airports. Airports are numbered
     *            from {@code 0} to {@code airportCount - 1} in the connections
     *            and you can assume that there is a (direct or indirect) path
     *            between all airports in the input.
     * @return the connections of a minimal spanning tree.
     */
    public static Set<Connection> minimalSpanningTree(final int airportCount, final Set<Connection> connections) {

        //Benchmarking
        final long startTime = System.currentTimeMillis();

        //Setting up variables

        //holds the partitions
        TreePartitioning components = new TreePartitioning(airportCount);
        // accumulate minimal spanning tree connections
        HashSet<Connection> minimalConnections = new HashSet<>();

        SCFThreadManger threadManger = new SCFThreadManger(connections, THREAD_COUNT, components);
        SummaryManager sum = new SummaryManager(THREAD_COUNT, components);

        //init done.

        //main lööp
        while(components.getPartitionCount() > 1) {

            //System.out.println("Remaining Partitions: " + components.getPartitionCount());

            threadManger.calculate();

            //System.out.println("Calculated!");

            /*
            System.out.println("roots: ");
            for (int each : components.roots()) System.out.print(each + ", ");
            System.out.println();
            */

            Connection[][] rr = threadManger.raw_results();

            /*
            for (Connection[] tr : rr) {
                for (Connection c : tr) {
                    System.out.print(c + " : ");
                }
                System.out.println();
            }
            */

            sum.calculate(rr);

            SetGroup<Connection> shortest = sum.getResults();

            //System.out.println("results compressed");

            //unionfiy all the things
            for (Connection c : shortest) {
                //System.out.println(c);
                components.union(c.getAirport1(), c.getAirport2());
                minimalConnections.add(c);
            }

            //System.out.println("unified");

        }

        final long endTime = System.currentTimeMillis();
        final long duration = endTime - startTime;

        System.out.println(airportCount + " Airports with " + connections.size() + " Connections took " + duration + "ms with " + THREAD_COUNT + " Threads");

        threadManger.terminate();
        sum.terminate();

        return minimalConnections;

    }


}

/*

Lessons learned:

My favorite Paradigm "More Threads = better" is sadly not true.
"java.util.concurrent" exists. Might come in useful sometime.
32-bit JVMs suck.
HashSets seem to be slower that I assumed (further testing required).
Synchronization is:
    not trivial
    slow
Maybe learning about something before attempting it is a good idea.
I am easily distracted.
Putting 10 classes in a single file in mostly random order makes the code ugly.
 */