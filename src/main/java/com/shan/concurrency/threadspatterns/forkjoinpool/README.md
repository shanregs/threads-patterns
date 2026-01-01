# ForkJoinPool Pattern - Divide-and-Conquer with Work Stealing

This package demonstrates ForkJoinPool for parallel divide-and-conquer algorithms with automatic load balancing.

## 🎯 What Is ForkJoinPool?

**ForkJoinPool** is a specialized thread pool for divide-and-conquer algorithms. It splits large tasks into smaller subtasks (fork), processes them in parallel, and combines the results (join). It uses a work-stealing algorithm where idle threads steal tasks from busy threads.

**Key Characteristics**:
- ✅ **Divide-and-conquer**: Recursively split tasks
- ✅ **Work stealing**: Idle threads steal tasks from busy threads
- ✅ **Fork/Join**: Async fork, blocking join
- ✅ **RecursiveTask/Action**: Extends for result (Task) or void (Action)
- ✅ **Efficient**: Minimizes thread context switching

**Use Cases**:
- **Image processing**: Apply filters to pixels in parallel
- **Array operations**: Parallel sort, map, reduce
- **Tree traversal**: Process tree nodes in parallel
- **Matrix operations**: Parallel matrix multiplication
- **Merge sort**: Parallel sorting algorithm

---

## 📊 Thread Flow Pattern: Recursive Task Splitting with Work Stealing

### Pattern: Divide → Fork → Compute → Join → Merge

```
Image Processing: 5000 pixels, Threshold: 1000 pixels

                    [0-5000] (5000 pixels)
                         │
                         ├─ FORK (split if > threshold)
                         │
            ┌────────────┴────────────┐
            │                         │
         [0-2500]                 [2500-5000]
        (2500 px)                 (2500 px)
            │                         │
            ├─ FORK                   ├─ FORK
            │                         │
      ┌─────┴─────┐           ┌──────┴──────┐
      │           │           │             │
   [0-1250]  [1250-2500]  [2500-3750]  [3750-5000]
   (1250 px) (1250 px)   (1250 px)    (1250 px)
      │           │           │             │
      ├─ FORK     ├─ FORK     ├─ FORK       ├─ FORK
      │           │           │             │
  ┌───┴───┐   ┌───┴───┐   ┌───┴───┐     ┌───┴───┐
  │       │   │       │   │       │     │       │
[0-625] [625-  [1250-  [1875-  [2500-  [3125-  [3750-  [4375-
       1250]  1875]  2500]  3125]  3750]  4375]  5000]

Each leaf task (≤ 1000 px): Process directly
      ↓           ↓       ↓       ↓
   Process    Process  Process  Process
      ↓           ↓       ↓       ↓
      └───────────┴───────┴───────┴── JOIN (merge results)
                    ↓
            [0-5000] Processed!

Work Stealing:
  Thread-1: Working on [0-625]
  Thread-2: Working on [625-1250]
  Thread-3: IDLE → Steals task [1250-1875] from Thread-1's queue
  Thread-4: Working on [1875-2500]

Efficient load balancing without manual coordination!
```

---

## 🔍 Demo: Parallel Image Processing

**Scenario**: Process 5000-pixel image with brightness filter

### Thread Flow

```
ForkJoinPool.commonPool() (Parallelism: number of processors)

Main Task: ImageProcessor [0-5000]
     │
     ├─ Split: length (5000) > threshold (1000)
     │
     ├─ Fork Left:  ImageProcessor [0-2500]    ── Async execution
     └─ Compute Right: ImageProcessor [2500-5000] ── Current thread

Fork Left [0-2500]:
     ├─ Split: length (2500) > threshold (1000)
     ├─ Fork Left:  [0-1250]
     └─ Compute Right: [1250-2500]

Continue splitting until length ≤ 1000:

Base Case [0-625]:
  ✅ Direct processing (625 ≤ 1000)
  Apply brightness filter to each pixel
  Return processed pixels

Join Phase:
  [0-625] JOIN [625-1250] → [0-1250]
  [1250-1875] JOIN [1875-2500] → [1250-2500]
  [0-1250] JOIN [1250-2500] → [0-2500]
  ...
  Final: [0-5000] fully processed!
```

### Code Pattern

**1. Extend RecursiveTask (with result) or RecursiveAction (void)**:
```java
public class ImageProcessor extends RecursiveTask<int[]> {
    private final int[] pixels;
    private final int start, end;
    private static final int THRESHOLD = 1000;

    @Override
    protected int[] compute() {
        int length = end - start;

        // Base case: Process directly
        if (length <= THRESHOLD) {
            return processDirectly();
        }

        // Recursive case: Split and fork
        int mid = start + length / 2;

        ImageProcessor leftTask = new ImageProcessor(pixels, start, mid);
        leftTask.fork(); // Async execution

        ImageProcessor rightTask = new ImageProcessor(pixels, mid, end);
        int[] rightResult = rightTask.compute(); // Current thread

        int[] leftResult = leftTask.join(); // Wait for fork

        return mergeResults(leftResult, rightResult);
    }
}
```

**2. Execute with ForkJoinPool**:
```java
ForkJoinPool pool = ForkJoinPool.commonPool();
ImageProcessor task = new ImageProcessor(pixels, 0, pixels.length);
int[] result = pool.invoke(task); // Execute and wait for result
```

---

## 🚀 Running the Demo

```bash
mvn exec:java -Dexec.mainClass="com.shan.concurrency.threadspatterns.forkjoinpool.Main"
```

### Expected Output
```
=== ForkJoinPool Demo: Parallel Image Processing ===
Scenario: Processing 5000-pixel image with brightness filter

Generated image with 5000 pixels
Using ForkJoinPool with parallelism level: 15

[main] Starting image processing...

[ForkJoinPool.commonPool-worker-1] Processing pixels [0 to 4999] (length: 5000)
[ForkJoinPool.commonPool-worker-1] Forking task: splitting [0 to 4999] into [0 to 2499] and [2500 to 4999]
[ForkJoinPool.commonPool-worker-2] Processing pixels [0 to 2499] (length: 2500)
[ForkJoinPool.commonPool-worker-2] Forking task: splitting [0 to 2499] into [0 to 1249] and [1250 to 2499]
[ForkJoinPool.commonPool-worker-1] Processing pixels [2500 to 4999] (length: 2500)
...
[ForkJoinPool.commonPool-worker-3] Processing directly: 625 pixels
[ForkJoinPool.commonPool-worker-4] Processing directly: 625 pixels
...
[ForkJoinPool.commonPool-worker-3] Completed direct processing of 625 pixels
[ForkJoinPool.commonPool-worker-2] Joining results from [0 to 1249] and [1250 to 2499]
...
[main] Image processing completed!
Processed 5000 pixels in 87 ms
Pool stats - Active threads: 0, Steal count: 127, Queued tasks: 0

=== ForkJoinPool Demo Completed ===
```

---

## 🔑 Key Classes and Methods

### RecursiveTask (with result)
```java
public class MyTask extends RecursiveTask<ResultType> {
    protected ResultType compute() {
        if (small enough) {
            return computeDirectly();
        }
        MyTask subtask1 = new MyTask(...);
        subtask1.fork(); // Async
        MyTask subtask2 = new MyTask(...);
        ResultType result2 = subtask2.compute();
        ResultType result1 = subtask1.join(); // Wait
        return combine(result1, result2);
    }
}
```

### RecursiveAction (void)
```java
public class MyAction extends RecursiveAction {
    protected void compute() {
        if (small enough) {
            processDirectly();
        } else {
            MyAction subtask1 = new MyAction(...);
            MyAction subtask2 = new MyAction(...);
            invokeAll(subtask1, subtask2); // Fork both and join
        }
    }
}
```

### ForkJoinPool
```java
ForkJoinPool pool = ForkJoinPool.commonPool();        // Default pool
ForkJoinPool pool = new ForkJoinPool(parallelism);    // Custom parallelism
ResultType result = pool.invoke(task);                 // Execute and wait
pool.submit(task);                                     // Async execution
```

---

## 🎯 Real-World Use Cases

### 1. Parallel Array Sum
```java
public class ArraySum extends RecursiveTask<Long> {
    private final int[] array;
    private final int start, end;
    private static final int THRESHOLD = 10000;

    protected Long compute() {
        if (end - start <= THRESHOLD) {
            long sum = 0;
            for (int i = start; i < end; i++) {
                sum += array[i];
            }
            return sum;
        }
        int mid = start + (end - start) / 2;
        ArraySum left = new ArraySum(array, start, mid);
        ArraySum right = new ArraySum(array, mid, end);
        left.fork();
        return right.compute() + left.join();
    }
}
```

### 2. Parallel Merge Sort
```java
public class ParallelMergeSort extends RecursiveAction {
    private final int[] array;
    private final int start, end;
    private static final int THRESHOLD = 8192;

    protected void compute() {
        if (end - start <= THRESHOLD) {
            Arrays.sort(array, start, end);
        } else {
            int mid = start + (end - start) / 2;
            invokeAll(
                new ParallelMergeSort(array, start, mid),
                new ParallelMergeSort(array, mid, end)
            );
            merge(array, start, mid, end);
        }
    }
}
```

---

## ⚠️ Common Pitfalls

### 1. Threshold Too Small
```java
// ❌ BAD: Threshold too small, excessive overhead
private static final int THRESHOLD = 10;
// Creates millions of tiny tasks, overhead > benefit
```

**Solution**: Use appropriate threshold (1000-10000 elements)
```java
// ✅ GOOD: Balanced threshold
private static final int THRESHOLD = 5000;
```

### 2. Not Using fork() + compute() Pattern
```java
// ❌ BAD: Both tasks use fork() - less efficient
leftTask.fork();
rightTask.fork();
int leftResult = leftTask.join();
int rightResult = rightTask.join();
```

**Solution**: Use fork-compute-join pattern
```java
// ✅ GOOD: Fork one, compute other in current thread
leftTask.fork();
int rightResult = rightTask.compute();
int leftResult = leftTask.join();
```

---

## 🎓 Best Practices

✅ **DO:**
- Use for CPU-intensive divide-and-conquer algorithms
- Set appropriate threshold (1000-10000 items)
- Use fork() for one subtask, compute() for the other
- Use RecursiveTask for result, RecursiveAction for void

❌ **DON'T:**
- Use for I/O-bound tasks (use regular ExecutorService)
- Make threshold too small (overhead) or too large (no parallelism)
- Fork both subtasks (wastes threads)
- Use for non-recursive algorithms

---

## 📊 ForkJoinPool vs ExecutorService vs Parallel Streams

| Feature | ForkJoinPool | ExecutorService | Parallel Streams |
|---------|--------------|-----------------|------------------|
| **Use Case** | Divide-and-conquer | Independent tasks | Data parallelism |
| **Work Stealing** | ✅ Yes | ❌ No | ✅ Yes (uses FJP) |
| **Recursive** | ✅ Yes | ❌ No | 🟡 Internally |
| **Ease of Use** | 🟡 Medium | ✅ Easy | ✅ Very easy |

**When to Choose**:
- **ForkJoinPool**: Recursive algorithms, divide-and-conquer
- **ExecutorService**: Independent tasks, simple parallelism
- **Parallel Streams**: Collection processing with built-in operations

---

**Package**: `com.shan.concurrency.threadspatterns.forkjoinpool`

**Pattern Type**: Parallel Execution - Divide-and-Conquer
**Thread Flow**: Recursive splitting with work stealing
**Best For**: Image processing, array operations, merge sort, parallel algorithms
