
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
public class Task3 {
    // FIX 1: Changed `int processedCount` to `AtomicInteger processedCount`
    //
    // ROOT CAUSE (Race Condition):
    // `processedCount++` looks like one operation but it is actually THREE steps:
    //   1. READ  — read current value from memory
    //   2. ADD   — add 1 to the value
    //   3. WRITE — write the new value back to memory
    //
    // With 10 threads running simultaneously, two threads can READ the same value
    // (say 5), both add 1 (get 6), and both WRITE 6 back — so one increment is lost.
    // This is called a "lost update" race condition. Over thousands of records,
    // many increments are lost, causing processedCount to report less than actual.
    //
    // FIX: AtomicInteger.incrementAndGet() is a single atomic CPU instruction
    // (Compare-And-Swap), so no two threads can interfere with each other.
    private AtomicInteger processedCount = new AtomicInteger(0);
    public void process(List<StatementRecord> records) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (StatementRecord record : records) {
            executor.submit(() -> {
                processRecord(record);
                // FIX 2: Changed `processedCount++` to `processedCount.incrementAndGet()`
                //         This is an atomic operation — thread-safe, no lost updates.
                processedCount.incrementAndGet();
            });
        }
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);
    }
    public int getProcessedCount() {
        // FIX 3: Return .get() to read the int value from AtomicInteger
    
        return processedCount.get();
    }
    
    private void processRecord(StatementRecord record) {
        // existing processing logic
    }
}
// ─── Supporting class (for compilation reference) ────────────────────────────
class StatementRecord {
    // fields omitted — not relevant to fix
}