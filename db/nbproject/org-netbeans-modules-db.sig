#Signature file v4.0
#Version 1.30.1

CLSS public abstract interface java.io.Serializable

CLSS public java.lang.Exception
cons public Exception()
cons public Exception(java.lang.String)
cons public Exception(java.lang.String,java.lang.Throwable)
cons public Exception(java.lang.Throwable)
supr java.lang.Throwable
hfds serialVersionUID

CLSS public java.lang.Object
cons public Object()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void finalize() throws java.lang.Throwable
meth public boolean equals(java.lang.Object)
meth public final java.lang.Class<?> getClass()
meth public final void notify()
meth public final void notifyAll()
meth public final void wait() throws java.lang.InterruptedException
meth public final void wait(long) throws java.lang.InterruptedException
meth public final void wait(long,int) throws java.lang.InterruptedException
meth public int hashCode()
meth public java.lang.String toString()

CLSS public java.lang.Throwable
cons public Throwable()
cons public Throwable(java.lang.String)
cons public Throwable(java.lang.String,java.lang.Throwable)
cons public Throwable(java.lang.Throwable)
intf java.io.Serializable
meth public java.lang.StackTraceElement[] getStackTrace()
meth public java.lang.String getLocalizedMessage()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public java.lang.Throwable fillInStackTrace()
meth public java.lang.Throwable getCause()
meth public java.lang.Throwable initCause(java.lang.Throwable)
meth public void printStackTrace()
meth public void printStackTrace(java.io.PrintStream)
meth public void printStackTrace(java.io.PrintWriter)
meth public void setStackTrace(java.lang.StackTraceElement[])
supr java.lang.Object
hfds backtrace,cause,detailMessage,serialVersionUID,stackTrace

CLSS public abstract interface java.util.EventListener

CLSS public abstract interface org.netbeans.api.db.explorer.ConnectionListener
intf java.util.EventListener
meth public abstract void connectionsChanged()

CLSS public final org.netbeans.api.db.explorer.ConnectionManager
cons public ConnectionManager()
meth public boolean connect(org.netbeans.api.db.explorer.DatabaseConnection) throws org.netbeans.api.db.explorer.DatabaseException
meth public org.netbeans.api.db.explorer.DatabaseConnection getConnection(java.lang.String)
meth public org.netbeans.api.db.explorer.DatabaseConnection showAddConnectionDialogFromEventThread(org.netbeans.api.db.explorer.JDBCDriver)
meth public org.netbeans.api.db.explorer.DatabaseConnection showAddConnectionDialogFromEventThread(org.netbeans.api.db.explorer.JDBCDriver,java.lang.String)
meth public org.netbeans.api.db.explorer.DatabaseConnection showAddConnectionDialogFromEventThread(org.netbeans.api.db.explorer.JDBCDriver,java.lang.String,java.lang.String,java.lang.String)
meth public org.netbeans.api.db.explorer.DatabaseConnection[] getConnections()
meth public static org.netbeans.api.db.explorer.ConnectionManager getDefault()
meth public void addConnection(org.netbeans.api.db.explorer.DatabaseConnection) throws org.netbeans.api.db.explorer.DatabaseException
meth public void addConnectionListener(org.netbeans.api.db.explorer.ConnectionListener)
meth public void disconnect(org.netbeans.api.db.explorer.DatabaseConnection)
meth public void removeConnection(org.netbeans.api.db.explorer.DatabaseConnection) throws org.netbeans.api.db.explorer.DatabaseException
meth public void removeConnectionListener(org.netbeans.api.db.explorer.ConnectionListener)
meth public void selectConnectionInExplorer(org.netbeans.api.db.explorer.DatabaseConnection)
meth public void showAddConnectionDialog(org.netbeans.api.db.explorer.JDBCDriver)
meth public void showAddConnectionDialog(org.netbeans.api.db.explorer.JDBCDriver,java.lang.String)
meth public void showAddConnectionDialog(org.netbeans.api.db.explorer.JDBCDriver,java.lang.String,java.lang.String,java.lang.String)
meth public void showConnectionDialog(org.netbeans.api.db.explorer.DatabaseConnection)
supr java.lang.Object
hfds DEFAULT,LOGGER

CLSS public final org.netbeans.api.db.explorer.DatabaseConnection
meth public java.lang.String getDatabaseURL()
meth public java.lang.String getDisplayName()
meth public java.lang.String getDriverClass()
meth public java.lang.String getName()
meth public java.lang.String getPassword()
meth public java.lang.String getSchema()
meth public java.lang.String getUser()
meth public java.lang.String toString()
meth public java.sql.Connection getJDBCConnection()
meth public java.sql.Connection getJDBCConnection(boolean)
meth public static org.netbeans.api.db.explorer.DatabaseConnection create(org.netbeans.api.db.explorer.JDBCDriver,java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean)
supr java.lang.Object
hfds delegate

CLSS public final org.netbeans.api.db.explorer.DatabaseException
cons public DatabaseException(java.lang.String)
cons public DatabaseException(java.lang.String,java.lang.Throwable)
cons public DatabaseException(java.lang.Throwable)
supr java.lang.Exception
hfds serialVersionUID

CLSS public final org.netbeans.api.db.explorer.DatabaseMetaDataTransfer
fld public static java.awt.datatransfer.DataFlavor COLUMN_FLAVOR
fld public static java.awt.datatransfer.DataFlavor CONNECTION_FLAVOR
fld public static java.awt.datatransfer.DataFlavor TABLE_FLAVOR
fld public static java.awt.datatransfer.DataFlavor VIEW_FLAVOR
innr public final static Column
innr public final static Connection
innr public final static Table
innr public final static View
supr java.lang.Object

CLSS public final static org.netbeans.api.db.explorer.DatabaseMetaDataTransfer$Column
meth public java.lang.String getColumnName()
meth public java.lang.String getTableName()
meth public java.lang.String toString()
meth public org.netbeans.api.db.explorer.DatabaseConnection getDatabaseConnection()
meth public org.netbeans.api.db.explorer.JDBCDriver getJDBCDriver()
supr java.lang.Object
hfds columnName,dbconn,jdbcDriver,tableName

CLSS public final static org.netbeans.api.db.explorer.DatabaseMetaDataTransfer$Connection
meth public java.lang.String toString()
meth public org.netbeans.api.db.explorer.DatabaseConnection getDatabaseConnection()
meth public org.netbeans.api.db.explorer.JDBCDriver getJDBCDriver()
supr java.lang.Object
hfds dbconn,jdbcDriver

CLSS public final static org.netbeans.api.db.explorer.DatabaseMetaDataTransfer$Table
meth public java.lang.String getTableName()
meth public java.lang.String toString()
meth public org.netbeans.api.db.explorer.DatabaseConnection getDatabaseConnection()
meth public org.netbeans.api.db.explorer.JDBCDriver getJDBCDriver()
supr java.lang.Object
hfds dbconn,jdbcDriver,tableName

CLSS public final static org.netbeans.api.db.explorer.DatabaseMetaDataTransfer$View
meth public java.lang.String getViewName()
meth public java.lang.String toString()
meth public org.netbeans.api.db.explorer.DatabaseConnection getDatabaseConnection()
meth public org.netbeans.api.db.explorer.JDBCDriver getJDBCDriver()
supr java.lang.Object
hfds dbconn,jdbcDriver,viewName

CLSS public final org.netbeans.api.db.explorer.JDBCDriver
meth public java.lang.String getClassName()
meth public java.lang.String getDisplayName()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.net.URL[] getURLs()
meth public java.sql.Driver getDriver() throws org.netbeans.api.db.explorer.DatabaseException
meth public static org.netbeans.api.db.explorer.JDBCDriver create(java.lang.String,java.lang.String,java.lang.String,java.net.URL[])
supr java.lang.Object
hfds clazz,displayName,name,urls

CLSS public abstract interface org.netbeans.api.db.explorer.JDBCDriverListener
meth public abstract void driversChanged()

CLSS public final org.netbeans.api.db.explorer.JDBCDriverManager
meth public org.netbeans.api.db.explorer.JDBCDriver showAddDriverDialogFromEventThread()
meth public org.netbeans.api.db.explorer.JDBCDriver[] getDrivers()
meth public org.netbeans.api.db.explorer.JDBCDriver[] getDrivers(java.lang.String)
meth public static org.netbeans.api.db.explorer.JDBCDriverManager getDefault()
meth public void addDriver(org.netbeans.api.db.explorer.JDBCDriver) throws org.netbeans.api.db.explorer.DatabaseException
meth public void addDriverListener(org.netbeans.api.db.explorer.JDBCDriverListener)
meth public void removeDriver(org.netbeans.api.db.explorer.JDBCDriver) throws org.netbeans.api.db.explorer.DatabaseException
meth public void removeDriverListener(org.netbeans.api.db.explorer.JDBCDriverListener)
meth public void showAddDriverDialog()
supr java.lang.Object
hfds DEFAULT,listeners,result

CLSS public final org.netbeans.api.db.explorer.support.DatabaseExplorerUIs
meth public static void connect(javax.swing.JComboBox,org.netbeans.api.db.explorer.ConnectionManager)
supr java.lang.Object
hcls ConnectionComboBoxModel,ConnectionComparator,ConnectionDataComboBoxModel

CLSS public final org.netbeans.api.db.sql.support.SQLIdentifiers
innr public abstract static Quoter
meth public static org.netbeans.api.db.sql.support.SQLIdentifiers$Quoter createQuoter(java.sql.DatabaseMetaData)
supr java.lang.Object
hcls DatabaseMetaDataQuoter

CLSS public abstract static org.netbeans.api.db.sql.support.SQLIdentifiers$Quoter
meth public abstract java.lang.String quoteAlways(java.lang.String)
meth public abstract java.lang.String quoteIfNeeded(java.lang.String)
meth public java.lang.String getQuoteString()
meth public java.lang.String unquote(java.lang.String)
supr java.lang.Object
hfds quoteString

CLSS public abstract interface org.netbeans.spi.db.explorer.DatabaseRuntime
meth public abstract boolean acceptsDatabaseURL(java.lang.String)
meth public abstract boolean canStart()
meth public abstract boolean isRunning()
meth public abstract java.lang.String getJDBCDriverClass()
meth public abstract void start()
meth public abstract void stop()

