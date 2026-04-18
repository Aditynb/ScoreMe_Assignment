/**
 * Task2Analysis.java
 * Written analysis of ConcurrentModificationException
 */
//public class Task2Analysis {
    /*
     * 
─────────────────────────────────────────────────────────────────────────
     * QUESTION 1: What is the exact cause of ConcurrentModificationException?
     * 
─────────────────────────────────────────────────────────────────────────
     *
     * Java's ArrayList (and most non-concurrent collections) maintains an
     * internal counter called `modCount`. Every time the list is structurally
     * modified (add / remove / clear etc.), modCount is incremented.
     *
     * When you get an Iterator (which a for-each loop uses internally), it
     * saves the current modCount as `expectedModCount`.
     *
     * On every call to iterator.next(), it calls checkForComodification():
     *      if (modCount != expectedModCount) throw new ConcurrentModificationException
     *
     * So the exception is thrown when:
     *   - The list is modified WHILE it is being iterated
     *   - Either in the same thread (e.g., calling list.remove() inside for-each)
     *   - Or from another thread (no synchronization), which matches the
     *     "intermittent at peak load" symptom in this case.
     *
     * 
─────────────────────────────────────────────────────────────────────────
     * QUESTION 2: Code pattern at line 142 that triggered this error?
     * 
─────────────────────────────────────────────────────────────────────────
     *
     * The most likely pattern at StatementProcessorService.java:142 is:
     *
     *      for (Transaction tx : transactionList) {        // iterator started
     *          if (someCondition(tx)) {
     *              transactionList.remove(tx);             // LINE 142 — modifies list mid-iteration!
     *          }
     *      }
     *
     * OR in a multi-threaded scenario:
     *
     *      // Thread A iterates the shared list
     *      for (Transaction tx : sharedList) { ... }
     *
     *      // Thread B simultaneously adds/removes from the same list
     *      sharedList.remove(tx);    // LINE 142 — triggers CME in Thread A
     *
     * Given the error says "intermittent at peak load", the multi-threaded
     * scenario is more likely — multiple HTTP threads sharing one ArrayList.
     *
     * 
─────────────────────────────────────────────────────────────────────────
     * QUESTION 3: Minimal fix (one or two lines)
     * 
─────────────────────────────────────────────────────────────────────────
     *
     * FIX OPTION A — If removing inside single-threaded iteration:
     * Use Iterator.remove() instead of list.remove():
     *
     *      Iterator<Transaction> it = transactionList.iterator();
     *      while (it.hasNext()) {
     *          Transaction tx = it.next();
     *          if (someCondition(tx)) {
     *              it.remove();   // FIX: safe removal during iteration
     *          }
     *      }
     *
     * FIX OPTION B — If multi-threaded shared list (matches this scenario):
     * Replace ArrayList with CopyOnWriteArrayList (one-line change):
     *
     *      // BEFORE:
     *      List<Transaction> transactionList = new ArrayList<>();
     *
     *      // AFTER (FIX): CopyOnWriteArrayList is thread-safe for iteration
     *      List<Transaction> transactionList = new CopyOnWriteArrayList<>();
     *
     * CopyOnWriteArrayList creates a fresh copy of the array on every write,
     * so iterators always see a consistent snapshot — no CME possible.
     *
     * FIX OPTION C — Collect items to remove, then remove after loop:
     *
     *      List<Transaction> toRemove = new ArrayList<>();
     *      for (Transaction tx : transactionList) {
     *          if (someCondition(tx)) toRemove.add(tx);   // FIX: collect first
     *      }
     *      transactionList.removeAll(toRemove);           // FIX: remove after loop
     *
     * 
─────────────────────────────────────────────────────────────────────────
     * RECOMMENDED FIX for this production scenario:
     * Use CopyOnWriteArrayList (Option B) — single line change, handles both
     * same-thread and cross-thread modification safely.
     * 
─────────────────────────────────────────────────────────────────────────
     */
    //}
