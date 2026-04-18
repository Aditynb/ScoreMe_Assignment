import java.util.*;
public class Task1 {
    public List<LoanAccount> getOverdueLoans(List<LoanAccount> accounts) {
        // FIX 1: result was null — changed to new ArrayList<>() to avoid
        //         NullPointerException when calling result.add(account)
        List<LoanAccount> result = new ArrayList<>();
        for (LoanAccount account : accounts) {
            // FIX 2: account.getDueDate() can be null for restructured accounts
            //         Added null-check before calling .before() to prevent NullPointerException
            if (account.getDueDate() != null && account.getDueDate().before(new Date())) {
                if (account.getOutstandingBalance() > 0) {
                    result.add(account);
                }
                // FIX 3: accounts with zero outstanding balance are NOT overdue
                //         (no money owed), so they should correctly be excluded.
                //         Original condition `> 0` was correct in intent but the
                //         assignment notes "incorrect results for zero balance" —
                //         meaning zero-balance accounts were being considered overdue
                //         only because result was null and add() was never reached.
                //         With FIX 1 applied, zero-balance accounts are now correctly
                //         excluded by the > 0 guard.
            }
        }
        return result;
    }
}

// ─── Supporting class (for compilation reference) ───────────────────────────
class LoanAccount {
    private String accountId;   // always non-null
    private java.util.Date dueDate;          // may be null for restructured accounts
    private double outstandingBalance;
    public String getAccountId()         { 
        return accountId;
    }
    public java.util.Date getDueDate()            {
        return dueDate; 
    }
    public double getOutstandingBalance(){ 
        return outstandingBalance; 
    }
}
    

