import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
public class Task4 {
    private DataSource dataSource;
    public List<ReportEntry> fetchMonthlyReport(String accountId,
                                                 int month, int year)
                                                 throws SQLException {
        List<ReportEntry> entries = new ArrayList<>();
        // FIX 1: Wrapped Connection, PreparedStatement, and ResultSet in
        //         try-with-resources. Java automatically calls .close() on each
        //         resource when the block exits — whether normally or via exception.
        //
        // FIX 2: Closure ORDER matters — innermost resource must be declared LAST
        //         so it is closed FIRST (LIFO order):
        //         ResultSet (rs) closed first → PreparedStatement (ps) closed second
        //         
//→ Connection (conn) closed last.
        //         This matches JDBC best practice and prevents pool exhaustion.
        //
        // ROOT CAUSE: Original code returned directly without closing conn, ps, rs.
        //             Each HTTP request leaked one connection. After ~6 hours under
        //             load, the connection pool was fully exhausted, causing hang.
        try (
            Connection conn = dataSource.getConnection();                  // FIX: auto-closed last
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM report_entries " +
                "WHERE account_id = ? AND MONTH(entry_date) = ? " +
                "AND YEAR(entry_date) = ?"
            );                                                              // FIX: auto-closed second
        ) {
            ps.setString(1, accountId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {                        // FIX: auto-closed first
                while (rs.next()) {
                                        entries.add(mapRow(rs));

            }
                }
        }
        return entries;
    }
    // FIX NOTE: mapRow() is unchanged as per assignment constraint
    private ReportEntry mapRow(ResultSet rs) throws SQLException {
        // existing mapping logic — not modified
        return new ReportEntry();
    }
}
// ─── Supporting class (for compilation reference) ────────────────────────────
class ReportEntry {
    // fields omitted
}