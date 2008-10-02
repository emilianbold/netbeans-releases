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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.db.mysql.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import org.netbeans.modules.db.mysql.util.DatabaseUtils;
import org.netbeans.modules.db.mysql.util.Utils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.db.mysql.Database;
import org.netbeans.modules.db.mysql.DatabaseServer;
import org.netbeans.modules.db.mysql.DatabaseUser;
import org.netbeans.modules.db.mysql.util.ExecSupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.Utilities;

/**
 * Model for a server.  Currently just uses MySQLOptions since we only
 * support one server, but this can be migrated to use an approach that
 * supports more than one server
 *
 * @author David Van Couvering
 */
public class MySQLDatabaseServer implements DatabaseServer, PropertyChangeListener {
    // Synchronized on this
    private String displayName;
    // Synchronized on this
    private String shortDescription;

    private static final Logger LOGGER = Logger.getLogger(DatabaseServer.class.getName());

    private static DatabaseServer DEFAULT;;

    private static final MySQLOptions OPTIONS = MySQLOptions.getDefault();

    // SQL commands
    private static final String GET_DATABASES_SQL = "SHOW DATABASES"; // NOI18N
    private static final String GET_USERS_SQL =
            "SELECT DISTINCT user, host FROM mysql.user"; // NOI18N
    private static final String CREATE_DATABASE_SQL = "CREATE DATABASE "; // NOI18N
    private static final String DROP_DATABASE_SQL = "DROP DATABASE "; // NOI18N

    // This is in two parts because the database name is an identifier and can't
    // be parameterized (it gets quoted and it is a syntax error to quote it).
    private static final String GRANT_ALL_SQL_1 = "GRANT ALL ON "; // NOI18N
    private static final String GRANT_ALL_SQL_2 = ".* TO ?@?"; // NOI18N

    final LinkedBlockingQueue<Runnable> commandQueue = new LinkedBlockingQueue<Runnable>();
    final ConnectionProcessor connProcessor = new ConnectionProcessor(commandQueue);
    final CopyOnWriteArrayList<ChangeListener> changeListeners = new CopyOnWriteArrayList<ChangeListener>();

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    // Cache this in cases where it is not being saved to disk
    // Synchronized on the instance (this)
    private String adminPassword;

    private final RequestProcessor checkRunningRequestProcessor = new RequestProcessor();

    // True if running, false otherwise.  Not synchronized.
    private volatile boolean running = false;


    // Cache list of databases, refresh only if connection is changed
    // or an explicit refresh is requested
    // Synchronized on the instance (this)
    private volatile HashMap<String, Database> databases = new HashMap<String, Database>();

    public static synchronized DatabaseServer getDefault() {
        if ( DEFAULT == null ) {
            DEFAULT = new MySQLDatabaseServer();
        }

        return DEFAULT;
    }

    private MySQLDatabaseServer() {
        RequestProcessor.getDefault().post(connProcessor);

        MySQLOptions.getDefault().addPropertyChangeListener(this);

        // Setup property change listeners
        addPropertyChangeListener(ConnectManager.getDefault().getReconnectListener());
        addPropertyChangeListener(StartManager.getDefault().getStartListener());
        addPropertyChangeListener(StopManager.getDefault().getStopListener());

        checkRunning(1000);
        updateDisplayName();
    }

    public String getHost() {
        return Utils.isEmpty(OPTIONS.getHost()) ?
            MySQLOptions.getDefaultHost() : OPTIONS.getHost();
    }

    public void setHost(String host) {
        OPTIONS.setHost(host);
        updateDisplayName();
        notifyChange();
    }

    public String getPort() {
        String port = OPTIONS.getPort();
        if (Utils.isEmpty(port)) {
            return MySQLOptions.getDefaultPort();
        } else {
            return port;
        }
    }

    public void setPort(String port) {
        OPTIONS.setPort(port);
        updateDisplayName();
        notifyChange();
    }

    public String getUser() {
        String user = OPTIONS.getAdminUser();
        if (Utils.isEmpty(user)) {
            return MySQLOptions.getDefaultAdminUser();
        } else {
            return user;
        }
    }

    public void setUser(String adminUser) {
        OPTIONS.setAdminUser(adminUser);
        updateDisplayName();
        notifyChange();
    }

    public synchronized String getPassword() {
        if ( adminPassword != null ) {
            return adminPassword;
        } else{
            return OPTIONS.getAdminPassword();
        }
    }

    public synchronized void setPassword(String adminPassword) {
        this.adminPassword = adminPassword == null ? "" : adminPassword;

        if ( isSavePassword() ) {
            OPTIONS.setAdminPassword(adminPassword);
        }
    }

    public boolean isSavePassword() {
        return OPTIONS.isSavePassword();
    }

    public void setSavePassword(boolean savePassword) {
        OPTIONS.setSavePassword(savePassword);

        // Save the password in case it was already set...
        OPTIONS.setAdminPassword(getPassword());
    }

    public String getAdminPath() {
        return OPTIONS.getAdminPath();
    }

    public void setAdminPath(String path) {
        OPTIONS.setAdminPath(path);
    }

    public String getStartPath() {
        return OPTIONS.getStartPath();
    }

    public void setStartPath(String path) {
        OPTIONS.setStartPath(path);
    }

    public String getStopPath() {
        return OPTIONS.getStopPath();
    }

    public void setStopPath(String path) {
        OPTIONS.setStopPath(path);
    }

    public String getStopArgs() {
        return OPTIONS.getStopArgs();
    }

    public void setStopArgs(String args) {
        OPTIONS.setStopArgs(args);
    }
    public String getStartArgs() {
        return OPTIONS.getStartArgs();
    }

    public void setStartArgs(String args) {
        OPTIONS.setStartArgs(args);
    }
    public String getAdminArgs() {
        return OPTIONS.getAdminArgs();
    }

    public void setAdminArgs(String args) {
        OPTIONS.setAdminArgs(args);
    }

    public boolean isConnected() {
        return connProcessor.isConnected();
    }

    public synchronized String getDisplayName() {
        return displayName;
    }

    private synchronized void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    private synchronized void updateDisplayName() {
        String stateLabel;
        String displayNameLabel = "LBL_ServerDisplayName";
        String shortDescriptionLabel = "LBL_ServerShortDescription";
        if (!isRunning()) {
            stateLabel="LBL_NotRunning";
        } else if (!isConnected()) {
            stateLabel="LBL_Disconnected";
        } else {
            stateLabel = "LBL_Connected";
        }
        
        String hostPort = getHostPort();
        String user = getUser();
        setDisplayName(Utils.getMessage(displayNameLabel, hostPort, user, Utils.getMessage(stateLabel)));
        setShortDescription(Utils.getMessage(shortDescriptionLabel, hostPort, user));
    }

    public String getShortDescription() {
        return shortDescription;
    }

    private synchronized void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    private String getHostPort() {
        String port = getPort();
        if ( Utils.isEmpty(port)) {
            port = "";
        } else {
            port = ":" + port;
        }
        return getHost() + port;
   }

    public String getURL() {
        return DatabaseUtils.getURL(getHost(), getPort());
    }

    public String getURL(String databaseName) {
        return DatabaseUtils.getURL(getHost(), getPort(), databaseName);
    }

    private void notifyChange() {
        postCheckRunning();
        
        ChangeEvent evt = new ChangeEvent(this);

        for ( ChangeListener listener : changeListeners ) {
            listener.stateChanged(evt);
        }
    }

    private void reportConnectionInvalid(DatabaseException dbe) {
        disconnect();
        LOGGER.log(Level.INFO, null, dbe);
        Utils.displayErrorMessage(dbe.getMessage());
    }

    public void refreshDatabaseList() {
        if ( isConnected() ) {
            final  DatabaseServer server = this;

            new DatabaseCommand() {
                public void execute() throws Exception {
                    try {
                        HashMap<String,Database> dblist = new HashMap<String,Database>();

                        if (! checkRunning()) {
                            setDatabases(dblist);
                            return;
                        }

                        try { 
                            connProcessor.validateConnection();
                        } catch (DatabaseException dbe) {
                            reportConnectionInvalid(dbe);
                            setDatabases(dblist);
                            return;
                        }

                        Connection conn = connProcessor.getConnection();
                        PreparedStatement ps = conn.prepareStatement(GET_DATABASES_SQL);
                        ResultSet rs = ps.executeQuery();

                        while (rs.next()) {
                            String dbname = rs.getString(1);
                            dblist.put(dbname, new Database(server, dbname));
                        }

                        setDatabases(dblist);
                    } finally {
                        notifyChange();
                    }
                }
            }.postCommand("refreshDatabaseList"); // NOI18N
        } else {
            setDatabases(new HashMap<String,Database>());
            notifyChange();
        }
    }

    private synchronized void setDatabases(HashMap<String,Database> list) {
        databases = list;
    }

    public synchronized Collection<Database> getDatabases()
            throws DatabaseException {
        return databases.values();
    }

    private void checkNotOnDispatchThread() {
        if (SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Can not call this method on the event dispatch thread");
        }
    }

    public void disconnectSync() {
        disconnect(false);
    }

    public void disconnect() {
        disconnect(true);
    }

    private void disconnect(boolean async) {
        ArrayBlockingQueue<Runnable> queue = null;

        if ( ! async ) {
            checkNotOnDispatchThread();
            queue = new ArrayBlockingQueue<Runnable>(1);
        }

        DatabaseCommand cmd = new DatabaseCommand(queue) {

            @Override
            public void execute() throws Exception {
                Connection conn = connProcessor.getConnection();
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        // Not important, since we want to disconnect anyway.
                        LOGGER.log(Level.FINE, null, e);
                    }
                }

                connProcessor.setConnection(null);

                updateDisplayName();
                refreshDatabaseList();
            }
        };

        cmd.postCommand("disconnect"); // NOI8N

        if (!async) {
            // Sync up
            try {
                cmd.syncUp();

                if (cmd.getException() != null) {
                    throw new RuntimeException(cmd.getException());
                }
            } catch (InterruptedException ie) {
                throw new RuntimeException(ie);
            }
        }

    }

    public void reconnect() throws DatabaseException {
        if (! checkRunning()) {
            throw new DatabaseException(NbBundle.getMessage(MySQLDatabaseServer.class, "MSG_UnableToReconnectServerNotRunning",
                    getHost(), getPort()));
        }
        
        ArrayBlockingQueue<Runnable> queue = null;

        checkNotOnDispatchThread();
        queue = new ArrayBlockingQueue<Runnable>(1);

        DatabaseCommand cmd = new DatabaseCommand(queue) {
            @Override
            public void execute() throws Exception {
                Connection conn = connProcessor.getConnection();

                if (conn != null && (! conn.isClosed())) {
                    conn.close();
                }

                connProcessor.setConnection(null);

                ProgressHandle progress = ProgressHandleFactory.createHandle(
                    Utils.getMessage("MSG_ConnectingToServer"));

                try {
                    progress.start();
                    progress.switchToIndeterminate();

                    for ( ; ; ) {
                         conn = DatabaseUtils.connect(getURL(), getUser(), getPassword());
                         connProcessor.setConnection(conn);
                         break;
                     }
                } finally {
                    updateDisplayName();
                    refreshDatabaseList();
                    progress.finish();
                }
            }
        };

        cmd.postCommand("reconnect"); // NOI18N

        // Sync up
        try {
            cmd.syncUp();

            if (cmd.getException() != null) {
                if (cmd.getException() instanceof DatabaseException) {
                    throw (DatabaseException)cmd.getException();
                } else {
                    throw new DatabaseException(cmd.getException());
                }
            }
        } catch (InterruptedException ie) {
            throw new RuntimeException(ie);
        }
    }

    public void validateConnection() throws DatabaseException {
        ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(1);

        DatabaseCommand cmd = new DatabaseCommand(queue, true) {

            @Override
            public void execute() throws Exception {
                // Just run the preprocessing for execute() which checks the
                // status of the server and the connection.
            }            
        };

        cmd.postCommand("validateConnection"); // NOI18N

        try {
            cmd.syncUp();

            Throwable e = cmd.getException();
            if (e instanceof DatabaseException) {
                throw (DatabaseException)e;
            } else {
                throw new DatabaseException(e);
            }
        } catch (InterruptedException e) {
            disconnect();
            throw new DatabaseException(e);
        }
    }

    public boolean databaseExists(String dbname)  throws DatabaseException {
        return databases.containsKey(dbname);
    }

    public void createDatabase(final String dbname) {
        new DatabaseCommand(true) {
            public void execute() throws Exception {
                try {
                    Connection conn = connProcessor.getConnection();
                    conn.prepareStatement(CREATE_DATABASE_SQL + dbname).executeUpdate();
                } finally {
                    refreshDatabaseList();
                }
            }
        }.postCommand("createDatabase");  // NOI18N
    }


    public void dropDatabase(final String dbname, final boolean deleteConnections) {
        new DatabaseCommand(true) {
            public void execute() throws Exception {
                try {
                    Connection conn = connProcessor.getConnection();
                    conn.prepareStatement(DROP_DATABASE_SQL + dbname).executeUpdate();

                    if (deleteConnections) {
                        DatabaseConnection[] dbconns = ConnectionManager.getDefault().getConnections();
                        for (DatabaseConnection dbconn : dbconns) {
                            if (dbconn.getDriverClass().equals(MySQLOptions.getDriverClass()) &&
                                    dbconn.getDatabaseURL().contains("/" + dbname)) {
                                ConnectionManager.getDefault().removeConnection(dbconn);
                            }
                        }
                    }
                } finally {
                    refreshDatabaseList();
                }
            }
        }.postCommand("dropDatabase"); // NOI18N
    }


    public void dropDatabase(final String dbname) {
        dropDatabase(dbname, true);
    }

    /**
     * Get the list of users defined for this server
     *
     * @return the list of users
     *
     * @throws org.netbeans.api.db.explorer.DatabaseException
     *      if some problem occurred
     */
    public List<DatabaseUser> getUsers() throws DatabaseException {
        final ArrayList<DatabaseUser> users = new ArrayList<DatabaseUser>();
        if ( ! isConnected() ) {
            return users;
        }

        ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(1);

        DatabaseCommand cmd = new DatabaseCommand(queue, true) {
            @Override
            public void execute() throws Exception {
                ResultSet rs = connProcessor.getConnection().
                        prepareStatement(GET_USERS_SQL).executeQuery();

                while ( rs.next() ) {
                    String user = rs.getString(1).trim();
                    String host = rs.getString(2).trim();
                    users.add(new DatabaseUser(user, host));
                }

                rs.close();
            }
        };

        cmd.postCommand("getUsers"); // NOI18N

        // Synch up
        try {
            cmd.syncUp();
            if (cmd.getException() != null) {
                throw new DatabaseException(cmd.getException());
            }
        } catch ( InterruptedException e ) {
            throw new DatabaseException(e);
        }

        return users;
    }

    public void grantFullDatabaseRights(final String dbname, final DatabaseUser grantUser) {
        new DatabaseCommand(true) {
            @Override
            public void execute() throws Exception {                
                PreparedStatement ps = connProcessor.getConnection().
                        prepareStatement(GRANT_ALL_SQL_1 + dbname + GRANT_ALL_SQL_2);
                ps.setString(1, grantUser.getUser());
                ps.setString(2, grantUser.getHost());
                ps.executeUpdate();
            }
        }.postCommand("grantFullDatabaseRights"); // NOI8N
    }

    /**
     * Run the start command.  Display stdout and stderr to an output
     * window.  Wait the configured wait time, attempt to connect, and
     * then return.
     *
     * @return true if the server is definitely started, false otherwise (the server is
     *  not started or the status is unknown).
     *
     * @throws org.netbeans.api.db.explorer.DatabaseException
     *
     * @see #getStartWaitTime()
     */
    public void start() throws DatabaseException {
        if (checkRunning()) {
            throw new DatabaseException(NbBundle.getMessage(MySQLDatabaseServer.class,
                    "MSG_CantStartServerIsAlreadyRunning", getPort()));
        }

        if (!Utils.isValidExecutable(getStartPath(), false)) {
            throw new DatabaseException(Utils.getMessage("MSG_InvalidStartCommand"));
        }
        
        new DatabaseCommand() {
            @Override
            public void execute() throws Exception {
                try {
                    runProcess(getStartPath(), getStartArgs(), true, Utils.getMessage("LBL_MySQLOutputTab"));
                } finally {
                    refreshDatabaseList();
                }
            }
        }.postCommand("start"); // NOI18N
    }

    public void stop() throws DatabaseException {
        if ( !Utils.isValidExecutable(getStopPath(), false)) {
            throw new DatabaseException(Utils.getMessage("MSG_InvalidStopCommand"));
        }

        final DatabaseServer server = this;

        new DatabaseCommand() {
            @Override
            public void execute() throws Exception {
                ProgressHandle handle = ProgressHandleFactory.createHandle(Utils.getMessage("LBL_StoppingMySQLServer"));
                Process proc = null;
                try {
                   handle.start();
                   handle.switchToIndeterminate();
                   proc = runProcess(getStopPath(), getStopArgs(), true, Utils.getMessage("LBL_MySQLOutputTab"));
                   // wait until server is shut down
                   synchronized(proc) {
                       while(checkRunning()) {
                           proc.wait();
                       }
                   }
               } finally {
                   if (proc != null) {
                       proc.destroy();
                   }
                   handle.finish();
               } 
            }
        }.postCommand("stop"); // NOI18N
    }

    /**
     * Launch the admin tool.  If the specified admin path is a URL,
     * a browser is launched with the URL.  If the specified admin path
     * is a file, the file path is executed.
     *
     * @return a process object for the executed command if the admin
     *   path was a file.  Returns null if the browser was launched.
     *
     * @throws org.netbeans.api.db.explorer.DatabaseException
     */
    public void startAdmin() throws DatabaseException {
        String adminCommand = getAdminPath();

        if ( adminCommand == null || adminCommand.length() == 0) {
            throw new DatabaseException(NbBundle.getMessage(
                    DatabaseServer.class,
                    "MSG_AdminCommandNotSet"));
        }

        if ( Utils.isValidURL(adminCommand, false)) {
            launchBrowser(adminCommand);
        } else if ( Utils.isValidExecutable(adminCommand, false)) {
            runProcess(adminCommand, getAdminArgs(),
                    true, Utils.getMessage(
                        "LBL_MySQLOutputTab"));
        } else {
            throw new DatabaseException(NbBundle.getMessage(
                    DatabaseServer.class,
                    "MSG_InvalidAdminCommand", adminCommand));
        }

    }

    private Process runProcess(String command, String args, boolean displayOutput,
            String outputLabel) throws DatabaseException {

        if ( Utilities.isMac() && command.endsWith(".app") ) {  // NOI18N
            // The command is actually the first argument, with /usr/bin/open
            // as the actual command.  Put the .app file path in quotes to
            // deal with spaces in the path.
            args = "\"" + command + "\" " + args; // NOI18N
            command = "/usr/bin/open"; // NOI18N
        }
        try {
            NbProcessDescriptor desc = new NbProcessDescriptor(command, args);
            Process proc = desc.exec();

            if ( displayOutput ) {
                new ExecSupport().displayProcessOutputs(proc, outputLabel);
            }
            return proc;
        } catch ( Exception e ) {
            throw new DatabaseException(e);
        }

    }

    private void launchBrowser(String adminCommand)  throws DatabaseException {
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURL(new URL(adminCommand));
        } catch ( Exception e ) {
            throw new DatabaseException(e);
        }
    }

    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeListeners.remove(listener);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public boolean isRunning() {
        return running;
    }

    public boolean checkRunning() {
        return checkRunning(3000);
    }
    public boolean checkRunning(long timeToWait) {
        Task task = checkRunningRequestProcessor.post(new Runnable() {
            public void run() {
                // Open a socket to the database port and see if it responds.  If it
                // does, let's assume we're running.  There's a possibility that another
                // service is running on that port, but that's an uncommon scenario...
                try {
                    new Socket(getHost(), Integer.parseInt(getPort()));
                    running = true;
                } catch (UnknownHostException ex) {
                    running = false;
                } catch (IOException ex) {
                    running = false;
                } catch (NumberFormatException nfe) {
                    NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(MySQLDatabaseServer.class, "MSG_MySQL_InvalidPortNumber", getPort()), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                    LOGGER.log(Level.WARNING, nfe.getMessage());
                    running = false;
                }

            }
        });

        try {
            // Sometimes the socket request can hang indefinitely.  Wait and
            // if we have no response, give up.  This at least stops the
            // caller from getting blocked.
            task.waitFinished(timeToWait);
            if (! task.isFinished()) {
                LOGGER.log(Level.INFO, "Gave up waiting on task to see if MySQL is running ");
                task.cancel();
                running = false;
            }
        } catch (InterruptedException e) {
            LOGGER.log(Level.INFO, "Task to see if MySQL is running was interrupted");
        }

        return running;
    }

    public void postCheckRunning() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                checkRunning();
            }
        });
    }

    public void propertyChange(PropertyChangeEvent evt) {
        // Use this as an opportunity to see if things have changed with the status of
        // the server
        postCheckRunning();
        
        pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
    }

    private abstract class DatabaseCommand<T> implements Runnable {
        private Throwable throwable;
        private final BlockingQueue<Runnable> outqueue;
        private T result;
        private boolean checkConnection = false;
        private String callingMethod = "<unknown>"; // NOI18N

        public DatabaseCommand(BlockingQueue<Runnable> outqueue) {
            this(outqueue, false);
        }

        public DatabaseCommand(BlockingQueue<Runnable> outqueue, boolean checkConnection) {
            this.outqueue = outqueue;
            this.checkConnection = checkConnection;
        }

        public T getResult() {
            return result;
        }

        public void setResult(T result) {
            this.result = result;
        }

        public DatabaseCommand(boolean checkConnection) {
            this(null, checkConnection);
        }

        public DatabaseCommand() {
            this(null, false);
        }

        public void postCommand(String callingMethod) {
            this.callingMethod = callingMethod;
            if (connProcessor.isConnProcessorThread()) {
                run();
            } else {
                commandQueue.offer(this);
            }
        }

        public void syncUp() throws InterruptedException {
            if (connProcessor.isConnProcessorThread()) {
                return;
            } else {
                assert(outqueue != null);
                outqueue.take();
            }
        }

        public void run() {
            try {
                if(!checkRunning() && checkConnection) {
                    throw new DatabaseException(NbBundle.getMessage(MySQLDatabaseServer.class, "MSG_ServerNoLongerRunning"));
                }
                if (checkConnection) {
                    try {
                        connProcessor.validateConnection();
                    } catch (DatabaseException dbe) {
                        try {
                            // See if we can quickly reconnect...
                            reconnect();
                        } catch (DatabaseException dbe2) {
                            LOGGER.log(Level.INFO, null, dbe2);
                            disconnect();
                            throw dbe;
                        }
                    }
                }
                
                this.execute();

            } catch ( Exception e ) {
                if ( outqueue != null ) {
                    this.throwable = e;
                } else {
                    // Since this is asynchronous, we are responsible for reporting the exception to the user.
                    LOGGER.log(Level.INFO, NbBundle.getMessage(MySQLDatabaseServer.class, "MSG_DatabaseCommandFailed", callingMethod), e);
                    Utils.displayErrorMessage(e.getMessage());
                }
            } finally {
                if (outqueue != null) {
                    outqueue.offer(this);
                }
            }
        }

        public abstract void execute() throws Exception;

        public Throwable getException() {
            return throwable;
        }
    }    
}
