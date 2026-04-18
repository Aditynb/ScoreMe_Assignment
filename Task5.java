import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class Task5 {
    // FIX (setup): Use SLF4J logger instead of e.printStackTrace()
    //              printStackTrace() floods logs with full stack traces for
    //              EVERY exception including expected ones (null doc, empty content).
    //              SLF4J lets us log at appropriate levels: warn vs error.
    private static final Logger logger = LoggerFactory.getLogger(Task5.class);
    public ValidationResult validate(Document doc) {
        try {
            if (doc == null) {
                // FIX ISSUE 1 (partly): Use a specific custom/checked exception
                //   instead of generic RuntimeException for expected validation failures.
                //   This lets callers and catch blocks distinguish "bad input" from
                //   "unexpected system error". We use IllegalArgumentException here
                //   as a lightweight option without adding new classes.
                throw new IllegalArgumentException("Document is null");
            }
            String content = doc.extractContent();
            if (content.isEmpty()) {
                throw new IllegalArgumentException("Empty content");
            }
            return runValidationRules(content);
        } catch (IllegalArgumentException e) {
            // FIX ISSUE 1: Catch expected validation failures SEPARATELY.
            //              Log at WARN level (not ERROR) — these are known, expected
            //              cases and do not need a full stack trace in logs.
            logger.warn("Validation failed for document: {}", e.getMessage());
            // FIX ISSUE 2: Return a proper "invalid" ValidationResult instead of null.
            //              Returning null causes NullPointerException downstream
            //              (e.g., r.isValid() in validateBatch — Issue 3 below).
            return ValidationResult.invalid(e.getMessage());
        } catch (Exception e) {
            // Unexpected runtime errors (e.g., extractContent() throws) go here.
            // FIX ISSUE 1 (continued): Log at ERROR level WITH stack trace —
            //              but only for truly unexpected exceptions.
            logger.error("Unexpected error during document validation", e);
            return ValidationResult.invalid("Unexpected validation error");
        }
    }
    public void validateBatch(List<Document> docs) {
        for (Document doc : docs) {
            try {
                ValidationResult r = validate(doc);
                // FIX ISSUE 3: validate() was returning null on exception.
                //              Now that validate() always returns a non-null
                //              ValidationResult (fixed in Issue 2 above),
                //              r.isValid() will never throw NullPointerException.
                //              Guard kept for extra safety.
                if (r != null && r.isValid()) {
                    saveResult(r);
                }
            } catch (Exception e) {
                // FIX ISSUE 4: Silent swallow replaced with proper logging.
                //              Exceptions here should at minimum be logged so the
                //              support team can diagnose problems.
                //              We log at ERROR with the doc reference if available.
                logger.error("Failed to process document in batch: {}",
                             doc != null ? doc.toString() : "null", e);
                // Note: We continue the loop (don't rethrow) so one bad document
                //       doesn't abort the entire batch — this is correct behavior
                //       for a batch processor.
            }
        }
    }
    // 
//─── These methods are NOT modified (assignment constraint) ──────────────
    private ValidationResult runValidationRules(String content) {
        // existing logic — not modified
        return null;
    }
    private void saveResult(ValidationResult r) {
        // existing logic — not modified
    }
}
// ─── Supporting classes (for compilation reference) ──────────────────────────
class Document {
    public String extractContent() { return ""; }
}
class ValidationResult {
    private boolean valid;
    private String message;
    private ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }
    public boolean isValid() { return valid; }
    // FIX ISSUE 2 helper: factory method to create a failed result (non-null)
    public static ValidationResult invalid(String reason) {
        return new ValidationResult(false, reason);
    }
}