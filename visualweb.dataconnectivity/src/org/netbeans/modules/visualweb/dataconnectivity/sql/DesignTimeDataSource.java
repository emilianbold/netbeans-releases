/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.visualweb.dataconnectivity.sql;

import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import java.util.Properties;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.netbeans.modules.visualweb.dataconnectivity.naming.ContextPersistance;
import org.netbeans.modules.visualweb.dataconnectivity.naming.ObjectChangeListener;
import org.netbeans.modules.visualweb.dataconnectivity.naming.ObjectChangeEvent;

import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;

import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.sql.support.SQLIdentifiers;
import org.netbeans.modules.visualweb.dataconnectivity.datasource.DataSourceResolver;
import org.openide.util.RequestProcessor;

/**
 * DataSource adapter for java.sql.Driver classes.  Used at designtime for all datasources.
 * Implements necesary interfaces to persist in Creator's naming context.
 *
 * @author John Kline
 */
public class DesignTimeDataSource implements DataSource, ContextPersistance, Runnable {

    protected static ResourceBundle rb = ResourceBundle.getBundle("org.netbeans.modules.visualweb.dataconnectivity.sql.Bundle", //NOI18N
        Locale.getDefault());

    private boolean      schemasInitialized;
    private SortedSet    schemas;  // empty set == display all schemas
    private String       driverClassName;
    private String       url;
    private String       username;
    private String       password;
    private String       validationQuery;
    private PrintWriter  logWriter;
    private int          loginTimeout;
    private SQLException testSQLException;
    private boolean      testConnectionSucceeded ;
    private int          testSQLRowsReturned ;
    private ArrayList    objectChangeListeners;
    private Driver       driver;
    private Connection   designtimeConnection; 
    private static boolean isConnectionAttempted = false;
    private DatabaseConnection dbConn;    
    private static URL[]  urls;
    private RequestProcessor.Task task = null;
    private final RequestProcessor CONNECT_RP = new RequestProcessor("DataSourceResolver.WAIT_FOR_MODELING_RP"); //NOI18N    
    
    private static final String alphabet = "abcdefghijklmnopqrstuvwxyz"; // NOI18N
    private static final String DRIVER_CLASS_NET = "org.apache.derby.jdbc.ClientDriver"; // NOI18N 
    private static final String SELECT_PHRASE = "select * from " ;
    private static final String SELECT_PHRASE_APPSERVER8 = "select count(*) from " ;
    public static final int SQL_NOT_RUN = -1 ;

    static private SecretKey secretKey = null;

    static final private char[] secretKeyHex =
    {'D','6','0','7','5','E','2','9','8','A','4','9','6','2','5','1'};

    private static SecretKey getSecretKey() {
        if (secretKey == null) {
            byte[] encodedKey = new byte[secretKeyHex.length/2];
            for (int i = 0; i < encodedKey.length; i++) {
                encodedKey[i] = hexToByte(secretKeyHex[i*2], secretKeyHex[i*2+1]);
            }
            secretKey = new SecretKeySpec(encodedKey, "DES"); // NOI18N
        }

        return secretKey;
    }

    public DesignTimeDataSource() {
        this(null, false, null, null, null, null, null);
    }

    /*
     * The first argument signals whether or not the password is encrypted and should be
     * decrypted.
     */
    public DesignTimeDataSource(Boolean isEncrypted, String driverClassName, String url,
        String validationQuery, String username, String password) {

        this(null, isEncrypted, driverClassName, url, validationQuery, username, password);
    }

    public DesignTimeDataSource(String schemas, Boolean isEncrypted, String driverClassName,
        String url, String validationQuery, String username, String password) {

        this(false, schemas, (isEncrypted == null)? false: isEncrypted.booleanValue(),
            driverClassName, url, validationQuery, username, password);
    }

    public DesignTimeDataSource(Boolean schemasInitialized, String schemas, Boolean isEncrypted,
        String driverClassName, String url, String validationQuery, String username,
        String password) {

        this((schemasInitialized == null)? false: schemasInitialized.booleanValue(), schemas,
            (isEncrypted == null)? false: isEncrypted.booleanValue(),
            driverClassName, url, validationQuery, username, password);
    }

    public DesignTimeDataSource(String schemas, boolean isEncrypted, String driverClassName,
        String url, String validationQuery, String username, String password) {
        this(false, schemas, isEncrypted, driverClassName, url, validationQuery, username,
            password);
    }

    /*
     * The first argument is used to tell if we have initalized the schemas by calling
     * initSchemas.
     * The second argument is used to limit the schemas to be used in the datasource.
     * schemas == null signals all schemas
     * The third argument signals whether or not the password is encrypted and should therefore
     * be decrypted.
     */
    public DesignTimeDataSource(boolean schemasInitialized, String schemas, boolean isEncrypted,
        String driverClassName, String url, String validationQuery, String username,
        String password) {

        this.schemasInitialized = schemasInitialized;
        this.schemas            = parseSchemas(schemas);
        this.driverClassName    = driverClassName;
        this.url                = url;
        this.username           = username;
        this.password           = null;
        if (password != null) {
            this.password       = isEncrypted? decryptPassword(password): password;
        }
        logWriter               = null;
        loginTimeout            = 0;
        this.validationQuery    = validationQuery;
        testSQLException        = null;
        testConnectionSucceeded = false ;
        testSQLRowsReturned     = SQL_NOT_RUN ;
        objectChangeListeners   = new ArrayList();
        driver                  = null;

    }

    /* props for handling last connection failures.  If a connect fails,
     * save the time and the failure SQLException.
     * When asked for another connection, don't even try if the last try failed
     * _and_ the last connection try was less than XXXX milliseconds ago.
     */
    
    private String enableFastConnectFail = System.getProperty("rave.fastConnectFail", "true") ;
    private boolean lastConnectFail = false ;
    private long lastConnectFailTime = 0 ;
    private static long CONNECT_RETRY_MS = 2690 ; // milliseconds before a retry.
    private SQLException failException = null ;
    
    public void clearConnectFailFlag() {
        lastConnectFail = false ;
    }
    private void setLastConnectFail( SQLException newException ) {
        lastConnectFail = true ;
        lastConnectFailTime = System.currentTimeMillis() ;
        if ( newException != null ) failException = newException ;
    }

    private void checkLastConnectFail() throws SQLException {
        if ( lastConnectFail ) {
            if ( (System.currentTimeMillis() - lastConnectFailTime ) < CONNECT_RETRY_MS ) {
                setLastConnectFail( null ) ;
                SQLException ee = new SQLException("Connect Retry recent failure: " + failException.getLocalizedMessage() ) ;
                ee.setNextException(  failException ) ;
                if ( enableFastConnectFail.equalsIgnoreCase("console")) {
                    System.out.println("DesignTimeDataSource connect retry auto-failure for " + getUrl() );
                }
                throw ee ;
            }
            lastConnectFail = false ; // retry the next time too.
        }
    }
    
    public synchronized Connection getConnection() throws SQLException {
        // cache the connection so that it can be reused
        if (designtimeConnection == null) {
            designtimeConnection = getConnection(username, password);
        }
        return designtimeConnection;
    }

    public synchronized Connection getConnection(String username, String password) throws SQLException {
        if (designtimeConnection == null) {
            Log.getLogger().entering(getClass().getName(), toString() + ".getConnection()", new Object[]{username, password});
            checkLastConnectFail();

            DatabaseConnection[] dbConns = ConnectionManager.getDefault().getConnections();

            for (int i = 0; i < dbConns.length; i++) {
                if (url.equalsIgnoreCase(dbConns[i].getDatabaseURL())) {
                    dbConn = dbConns[i];
                    url = dbConns[i].getDatabaseURL();
                    break;
                }
            }

            if (dbConn != null) {
                JDBCDriver jdbcDriver = DataSourceResolver.getInstance().findMatchingDriver(dbConn.getDriverClass());
                urls = jdbcDriver.getURLs();

                driverClassName = dbConn.getDriverClass();
                loadDriver();
            }

            Properties props = new Properties();
            if (username != null) {
                props.put("user", username); // NOI18N
            }
            if (password != null) {
                props.put("password", password); // NOI18N
            }

            /*
             * It turns out we can't rely on drivers to only throw SQLExceptions.
             * e.g., see bug #5046309 - Test Connection throws Exception when URL has #PORTNUMBER
             *       the above throws a NumberFormatException
             * so let's catch everything and "wrap" any non-SQLException exceptions
             */
            try {
                if (driverClassName.equals(DRIVER_CLASS_NET) && !isConnectionAttempted) {
                    isConnectionAttempted = true;
                    ensureConnection();
                }

                Connection conn = driver.connect(url, props);
                /*
                 * See Driver.connect spec.  connect is supposed to return null if it realizes
                 * it is not the right driver for the url.  This is #@!$!$@$#@, but we have to
                 * live with it.
                 */
                if (conn == null) {
                    SQLException se = new SQLException(MessageFormat.format(rb.getString("WRONG_DRIVER_FOR_URL"),
                            new Object[]{driverClassName, url                            })); // NOI18N
                    setLastConnectFail(se);
                    throw se;
                }

                designtimeConnection = new DesignTimeConnection(this, conn);

        } catch (Exception e) {
            if (e instanceof SQLException) {
                setLastConnectFail((SQLException) e);
                throw (SQLException) e;
            }
            SQLException sqlEx = new SQLException(e.getLocalizedMessage());
            sqlEx.initCause(e);
            setLastConnectFail(sqlEx);
            throw sqlEx;
        }
        }
        return designtimeConnection;
    }

    public PrintWriter getLogWriter() throws SQLException {
        return logWriter;
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        this.logWriter = out;
    }

    public int getLoginTimeout() throws SQLException {
        return loginTimeout;
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        this.loginTimeout = seconds;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
        /*
         * invalidate any driver instance we are holding on to
         */
        driver = null;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    // We only store the ValidationQuery as "select * from <table>"
    // Extract the <table> here.
    public String getValidationTable() {
        // see if it start's with SELECT_PHRASE, otherwise try a hack
        String validationTable = parseForValidationTable( getValidationQuery() );
        
        return ( validationTable == null ? "" : validationTable ) ; // NOI18N
    }
    public void setValidationTable(String table) {
        setValidationQuery( composeValidationQuery(table) ) ;
    }
    
    protected static String composeValidationQuery( String table ) {
        return SELECT_PHRASE + table ;
    }
    protected static String parseForValidationTable( String valQuery ) {
        // see if it start's with SELECT_PHRASE, otherwise try a hack
        String validationTable = null;
        
        if ( valQuery == null ) return null ;
        String vq = valQuery.toLowerCase() ;

        if ( vq != null && vq.startsWith(SELECT_PHRASE.toLowerCase()) ) {
            if ( vq.length() > SELECT_PHRASE.length() ) {
                validationTable = valQuery.substring(SELECT_PHRASE.length()) ;
            }
        }
        else {
            // HACK - look for last "from" and get the next word after that.
            if ((valQuery != null) && !valQuery.trim().equals("")){  // NOI18N
                StringTokenizer st = new  StringTokenizer(valQuery);
                while (st.hasMoreTokens()){
                    if (st.nextToken().toUpperCase().equals("FROM")){ // NOI18N
                        if(st.hasMoreTokens()){
                            validationTable = st.nextToken();
                        }
                        break;
                    }
                }
            }
        }  
        return validationTable ;
    }

    public boolean test() {
        testSQLException = null;
        testConnectionSucceeded = false ;
        testSQLRowsReturned = SQL_NOT_RUN ;
        Connection conn = null;
        try {
            conn = getConnection();
            testConnectionSucceeded = true ;
        }
        catch (SQLException e) {
            testSQLException  = e;
        }
        if ( testConnectionSucceeded
             && validationQuery != null && !validationQuery.equals("") ) {
            try {
                    Statement stmt = conn.createStatement();
                    String valQuery = SELECT_PHRASE_APPSERVER8 + getValidationTable() ;
                    ResultSet rs = stmt.executeQuery(valQuery);
                    // There could be table with out rows. Let us depend
                    // only on the SQL exception.
                    // testSQLRowsReturned = 0 ; // shouldn't need if we select count(*).
                    if (rs.next()) {
                        testSQLRowsReturned = rs.getInt(1) ;
                    }

            } catch (SQLException e) {
                testSQLException  = e;
                return false;
            }
        }
        // lastly, close the connection
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e2) {
            }
        }
        return testConnectionSucceeded ;
    }

    public SQLException getTestException() {
        return testSQLException;
    }
    public int getTestRowsReturned() {
        return testSQLRowsReturned ;
    }
    public boolean getTestConnectionSucceeded() {
        return testConnectionSucceeded ;
    }

    /**
     * gets schemas selected for this datasource, an empty set means all schemas
     */
    public String[] getSchemas() {
        return (String[])schemas.toArray(new String[0]);
    }

    public void setSchemas(Collection schemas) {
        clearSchemas();
        this.schemas.addAll(schemas);
    }

    /**
     * attempt to set the initial setting for schemas
     * do this by attempting to get a schema with the same name as the username
     * if this is found, set this datasource to only show that one schema
     * else don't do anything (all schemas will show or the database doesn't support
     * schemas)
     *
     * throws SQLException if we can't connect or can't get DatabaseMetaData
     *
     * throws NamingException if this DesignTimeDataSource is already bound to
     * a context and something goes wrong in the save
     */
    synchronized public void initSchemas() throws SQLException, NamingException {
        if (getSchemasInitialized()) {
            return;
        }
        clearSchemas();
        DatabaseMetaDataHelper dbmdh = new DatabaseMetaDataHelper(this);
        String[] schemas = dbmdh.getSchemas();
        for (int i = 0; i < schemas.length; i++) {
            if (schemas[i].toLowerCase().equals(username.toLowerCase())) {
                /*
                 * make sure we have at least one table or view in this schema
                 * if we don't, then show all schemas
                 */
                String query;
                if ((query = dbmdh.getValidationQuery(schemas[i])) != null) {
                    if (validationQuery == null) {
                        validationQuery = query;
                    }
                    addSchema(schemas[i]);
                } else {
                    /*
                     * We shouldn't have to do this.  No schemas selected
                     * is supposed to signal all schemas -- but servernav
                     * seems to think differently.
                     */
                    for (int j = 0; j < schemas.length; j++) {
                        addSchema(schemas[j]);
                    }
                }
                break;
            }
        }
        /* Validation query will still be null if this database has no schemas or if
         * we didn't limit this datasource to a single schema above.  If so, let's try
         * to set it now.
         */
        if (validationQuery == null) {
            validationQuery = dbmdh.getValidationQuery(null);
        }
        setSchemasInitialized(true);
        save();
    }

    public boolean getSchemasInitialized() {
        return schemasInitialized;
    }

    public void setSchemasInitialized(boolean schemasInitialized) {
        this.schemasInitialized = schemasInitialized;
    }

    public void clearSchemas() {
        this.schemas.clear();
    }

    public void addSchema(String schema) {
        schemas.add(schema);
    }

    public void removeSchema(String schema) {
        schemas.remove(schema);
    }

    public String getTag(String key, int level, int tabWidth) {

        return getSpaces(level, tabWidth)
            + "<object name=\"" + key + "\" class=\"" + getClass().getName() + "\">\n" //NOI18N

            + getSpaces(level + 1, tabWidth)
            + "<arg class=\"java.lang.Boolean\" value=\""           //NOI18N
            + (getSchemasInitialized()? "true": "false") + "\"/>\n" //NOI18N

            + getSpaces(level + 1, tabWidth)
            + "<arg class=\"java.lang.String\"" + getSchemaValueAttribute() + "/>\n" //NOI18N

            + getSpaces(level + 1, tabWidth)
            + "<arg class=\"java.lang.Boolean\" value=\"true\"/>\n" // NOI18N

            + getSpaces(level + 1, tabWidth)
            + "<arg class=\"java.lang.String\"" // NOI18N
            + ((driverClassName == null)? "": " value=\"" + driverClassName //NOI18N
            + "\"") //NOI18N
            + "/>\n" //NOI18N

            + getSpaces(level + 1, tabWidth)
            + "<arg class=\"java.lang.String\"" // NOI18N
            + ((url == null)? "": " value=\"" + escapeXML(url) + "\"") // NOI18N
            + "/>\n" // NOI18N

            + getSpaces(level + 1, tabWidth)
            + "<arg class=\"java.lang.String\"" // NOI18N
            + ((validationQuery == null)? "": " value=\"" + escapeXML(validationQuery) + "\"") // NOI18N
            + "/>\n" // NOI18N

            + getSpaces(level + 1, tabWidth)
            + "<arg class=\"java.lang.String\"" // NOI18N
            + ((username == null)? "": " value=\"" + username + "\"") // NOI18N
            + "/>\n" // NOI18N

            + getSpaces(level + 1, tabWidth)
            + "<arg class=\"java.lang.String\"" // NOI18N
            + ((password == null)? "": " value=\"" + encryptPassword(password) + "\"") // NOI18N
            + "/>\n" // NOI18N

            + getSpaces(level, tabWidth)
            + "</object>\n"; // NOI18N
    }

    /**
     * escape to XML legal.  The chars -
     *   quote("), apostrophe('), ampersand, less than, greater than
     * need to be escaped.
     */
    protected static String escapeXML(String orig) {
        String retVal = orig.replaceAll("&","&amp;" ) ; // do this first.
        retVal = retVal.replaceAll("<","&lt;" ) ;
        retVal = retVal.replaceAll(">","&gt;" ) ;
        retVal = retVal.replaceAll("\"","&quot;" ) ;
        retVal = retVal.replaceAll("'","&apos;" ) ;
        return retVal ;
    }

    public void addObjectChangeListener(ObjectChangeListener listener) {
        objectChangeListeners.add(listener);
    }

    public void removeObjectChangeListener(ObjectChangeListener listener) {
        objectChangeListeners.remove(listener);
    }

    public void save() throws NamingException {
        if (!objectChangeListeners.isEmpty()) {
            ObjectChangeEvent evt = new ObjectChangeEvent(this);
            for (Iterator i = objectChangeListeners.iterator(); i.hasNext();) {
                ((ObjectChangeListener)i.next()).objectChanged(evt);
            }
        }
    }

    private static SortedSet parseSchemas(String schemas) {

        SortedSet set = new TreeSet();

        if (schemas != null && !schemas.trim().equals("")) {
            String[] schema = schemas.split(","); // NOI18N
            for (int i = 0; i < schema.length; i++) {
                if (!schema[i].trim().equals("")) {
                    set.add(schema[i]);
                }
            }
        }
        return set;
    }

    private String getSchemaValueAttribute() {

        String csv = "";

        for (Iterator i = schemas.iterator(); i.hasNext();) {
            if (!csv.equals("")) {
                csv += ","; // NOI18N
            }
            csv += (String)i.next();
        }

        return csv.equals("")? "": (" value=\"" + csv + "\""); // NOI18N
    }

    public static String encryptPassword(String password) {

        if (password == null) {
            return null;
        }

        try {
            Cipher cipher = Cipher.getInstance("DES"); // NOI18N
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
            byte[] passwordBytes = password.getBytes("UTF8"); // NOI18N
            // encrypt
            byte[] encryptedBytes = cipher.doFinal(passwordBytes);
            // convert to hex
            char[] hexChars = new char[encryptedBytes.length * 2];
            for (int i = 0; i < encryptedBytes.length; i++) {
                hexChars[i*2]     = nibbleToHex((encryptedBytes[i] & 240) >> 4);
                hexChars[i*2 + 1] = nibbleToHex(encryptedBytes[i] & 15);
            }
            return new String(hexChars);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    public static String composeSelect(String tableName, DatabaseMetaData metaData) {
        
        SQLIdentifiers.Quoter quoter;
        quoter = SQLIdentifiers.createQuoter(metaData);
        
        String[] names = tableName.split("\\.");
        String selectName = "";
        for (int i = 0; i < names.length; i++) {
            if (i != 0) {
                selectName += ".";
            }
            // Delimit identifiers if necessary
            selectName += quoter.quoteIfNeeded(names[i]);
        }
        return "SELECT * FROM " + selectName; // NOI18N
    }

    private static final char[] hexCharacters = { '0','1','2','3','4','5','6','7','8','9','A','B',
        'C','D','E','F'};

    private static char nibbleToHex(int val) {
        return hexCharacters[val];
    }

    private static final String hexString = "0123456789ABCDEF"; // NOI18N

    private static byte hexToByte(char char1, char char2) {
        return (byte)((hexString.indexOf(char1) << 4) + hexString.indexOf(char2));
    }

    static String decryptPassword(String password) {

        if (password == null) {
            return null;
        }

        try {
            char[] hexChars = password.toCharArray();
            /*
            char[] hexChars = new char[password.length()];
            for (int i = 0; i < hexChars.length; i++) {
                hexChars[i] = password.charAt(hexChars.length-1-i);
            }
             */
            byte[] encryptedBytes = new byte[hexChars.length/2];
            for (int i = 0; i < encryptedBytes.length; i++) {
                encryptedBytes[i] = hexToByte(hexChars[i*2], hexChars[i*2+1]);
            }
            Cipher cipher = Cipher.getInstance("DES"); // NOI18N
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
            byte[] passwordBytes = cipher.doFinal(encryptedBytes);
            return new String(passwordBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    protected String getSpaces(int level, int tabWidth) {

        String str = "";

        for (int i = 0; i < level; i++) {
            for (int j = 0; j < tabWidth; j++) {
                str += " ";
            }
        }

        return str;
    }

    private void loadDriver() throws SQLException {

        Log.getLogger().entering(getClass().getName(), toString()+".loadDriver"); //NOI18N
        if (driver == null) {
            try {
                Class driverClass =  Class.forName(driverClassName, true,
                    getDriverClassLoader(getClass().getClassLoader()));
                driver = (Driver) driverClass.newInstance();
            } catch (ClassNotFoundException e) {
                throw new SQLException(e.getClass().getName()+": "+e.getLocalizedMessage());//NOI18N
            } catch (InstantiationException e) {
                throw new SQLException(e.getClass().getName()+": "+e.getLocalizedMessage());//NOI18N
            } catch (IllegalAccessException e) {
                throw new SQLException(e.getClass().getName()+": "+e.getLocalizedMessage());//NOI18N
            }
        }
    }

    private static URLClassLoader getDriverClassLoader(ClassLoader parent) {

        Log.getLogger().entering("DesignTimeDataSource", "getDriverClassLoader()"); //NOI18N
        return URLClassLoader.newInstance(urls, parent);
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(getClass().getName());
        s.append('(');
        boolean first = true;
        if (driverClassName != null) {
            s.append(driverClassName);
            first = false;
        }
        if (url != null) {
            if (!first) {
                s.append(',');
            }
            s.append(url);
            first = false;
        }
        if (username != null) {
            if (!first) {
                s.append(',');
            }
            s.append(username);
            first = false;
        }
        if (validationQuery != null) {
            if (!first) {
                s.append(',');
            }
            s.append(validationQuery);
            first = false;
        }
        s.append(')');
        return s.toString();
    }

    // Methods added for compliance with Java 1.6
    
    public boolean isWrapperFor(Class iface) throws SQLException {
        throw new RuntimeException(rb.getString("NOT_IMPLEMENTED"));
    }

    public Object unwrap(Class iface) throws SQLException {
        throw new RuntimeException(rb.getString("NOT_IMPLEMENTED"));
    }
        
    /*
     * Make connection if needed
     */
    public synchronized void run() {        
        JDBCDriver jdbcDriver = DataSourceResolver.getInstance().findMatchingDriver(driverClassName);
        DatabaseConnection conn = DatabaseConnection.create(jdbcDriver, url, username, username.toUpperCase(), password, true);
        ConnectionManager.getDefault().showConnectionDialog(conn);
    }
    
    public void ensureConnection() {        
         if (task == null) {
            task = CONNECT_RP.create(this);
        }
        task.schedule(10);
    }
        
}
