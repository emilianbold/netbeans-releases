import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

public class DummyDriver implements Driver {
    
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return new DriverPropertyInfo[0];
    }

    public Connection connect(String url, Properties info) throws SQLException {
        return null;
    }

    public boolean acceptsURL(String url) throws SQLException {
        return false;
    }

    public boolean jdbcCompliant() {
        return false;
    }

    public int getMajorVersion() {
        return 1;
    }
    
    public int getMinorVersion() {
        return 0;
    }
}
