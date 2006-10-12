/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.db.explorer;

import javax.swing.SwingUtilities;
import org.netbeans.lib.ddl.DBConnection;
import org.netbeans.modules.db.explorer.ConnectionList;
import org.netbeans.modules.db.explorer.actions.ConnectUsingDriverAction;
import org.netbeans.modules.db.explorer.infos.RootNodeInfo;
import org.netbeans.modules.db.explorer.nodes.RootNode;
import org.openide.ErrorManager;
import org.openide.util.Mutex;

/**
 * Provides access to the list of connections in the Database Explorer.
 *
 * <p>The list of connections can be retrieved using the {@link #getConnections}
 * method. A connection can be also retrieved by name using the 
 * {@link #getConnection} method.</p>
 * 
 * <p>New connections can be added to the Connection Manager using the
 * {@link #addConnection} method (new connections can be created using the
 * {@link DatabaseConnection#create} method. 
 * It is also possible to display the New Database Connection dialog to let the
 * user create a new database connection using the {@link #showAddConnectionDialog}.
 * Connections can be realized using the {@link #showConnectionDialog} method.</p>
 * 
 * <p>Clients can be informed of changes to the ConnectionManager by registering
 * a {@link ConnectionListener} using the {@link #addConnectionListener} method.</p>
 *
 * @see DatabaseConnection
 * 
 * @author Andrei Badea
 */
public final class ConnectionManager {
    
    /**
     * The ConnectionManager singleton instance.
     */
    private static ConnectionManager DEFAULT;
    
    /**
     * Gets the ConnectionManager singleton instance.
     */
    public static synchronized ConnectionManager getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new ConnectionManager();
        }
        return DEFAULT;
    }
    
    /**
     * Returns the list of connections in the Database Explorer.
     *
     * @return a non-null array of connections.
     */
    public DatabaseConnection[] getConnections() {
        DBConnection[] conns = ConnectionList.getDefault().getConnections();
        DatabaseConnection[] dbconns = new DatabaseConnection[conns.length];
        for (int i = 0; i < conns.length; i++) {
            dbconns[i] = ((org.netbeans.modules.db.explorer.DatabaseConnection)conns[i]).getDatabaseConnection();
        }
        return dbconns;
    }
    
    /**
     * Returns the connection with the specified name.
     *
     * @param name the connection name 
     *
     * @throws NullPointerException if the specified database name is null.
     */
    public DatabaseConnection getConnection(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        DBConnection[] conns = ConnectionList.getDefault().getConnections();
        for (int i = 0; i < conns.length; i++) {
            DatabaseConnection dbconn = ((org.netbeans.modules.db.explorer.DatabaseConnection)conns[i]).getDatabaseConnection();
            if (name.equals(dbconn.getName())) {
                return dbconn;
            }
        }
        return null;
    }
    
    /**
     * Adds a new connection to Database Explorer. This method does not display any UI and
     * does not try to connect to the respective database.
     *
     * @param dbconn the connection to be added; must not be null.
     *
     * @throws NullPointerException if dbconn is null.
     * @throws DatabaseException if an error occurs while adding the connection.
     */
    public void addConnection(DatabaseConnection dbconn) throws DatabaseException {
        if (dbconn == null) {
            throw new NullPointerException();
        }
        ((RootNodeInfo)RootNode.getInstance().getInfo()).addConnectionNoConnect(dbconn.getDelegate());
    }
    
    /**
     * Shows the dialog for adding a new connection. The specified driver will be
     * selected by default in the New Database Connection dialog.
     *
     * @param driver the JDBC driver; can be null.
     */
    public void showAddConnectionDialog(JDBCDriver driver) {
        showAddConnectionDialog(driver, null, null, null);
    }
    
    /**
     * Shows the dialog for adding a new connection with the specified database URL. 
     * The specified driver be filled as the single element of the 
     * Driver combo box of the New Database Connection dialog box.
     * The database URL will be filled in the Database URL field in the 
     * New Database Connection dialog box.
     *
     * @param driver the JDBC driver; can be null.
     * @param databaseUrl the database URL; can be null.
     */
    public void showAddConnectionDialog(JDBCDriver driver, final String databaseUrl) {
        showAddConnectionDialog(driver, databaseUrl, null, null);
    }
    
    /**
     * Shows the dialog for adding a new connection with the specified database URL, user and password
     * The specified driver be filled as the single element of the 
     * Driver combo box of the New Database Connection dialog box.
     * The database URL will be filled in the Database URL field in the 
     * New Database Connection dialog box.
     * The user and password will be filled in the User Name and Password
     * fields in the New Database Connection dialog box.
     *
     * @param driver the JDBC driver; can be null.
     * @param databaseUrl the database URL; can be null.
     * @param user the database user; can be null.
     * @param password user's password; can be null.
     *
     * @since 1.19
     */
    public void showAddConnectionDialog(final JDBCDriver driver, final String databaseUrl, final String user, final String password) {
        Mutex.EVENT.readAccess(new Runnable() {
            public void run() {
                new ConnectUsingDriverAction.NewConnectionDialogDisplayer().showDialog(driver, databaseUrl, user, password);
            }
        });
    }
    
    /**
     * The counterpart of {@link #showAddConnectionDialog(JDBCDriver) } which returns
     * the newly created database connection, but must be called from the event dispatching
     * thread.
     *
     * @param driver the JDBC driver; can be null.
     *
     * @return the new database connection or null if no database connection
     *         was created (e.g. the user pressed Cancel).
     *
     * @throws IllegalStateException if the calling thread is not the event
     *         dispatching thread.
     *
     * @since 1.19
     */
    public DatabaseConnection showAddConnectionDialogFromEventThread(JDBCDriver driver) {
        return showAddConnectionDialogFromEventThread(driver, null, null, null);
    }
    
    /**
     * The counterpart of {@link #showAddConnectionDialog(JDBCDriver, String) } which returns
     * the newly created database connection, but must be called from the event dispatching
     * thread.
     *
     * @param driver the JDBC driver; can be null.
     * @param databaseUrl the database URL; can be null.
     *
     * @return the new database connection or null if no database connection
     *         was created (e.g. the user pressed Cancel).
     *
     * @throws IllegalStateException if the calling thread is not the event
     *         dispatching thread.
     *
     * @since 1.19
     */
    public DatabaseConnection showAddConnectionDialogFromEventThread(JDBCDriver driver, String databaseUrl) {
        return showAddConnectionDialogFromEventThread(driver, databaseUrl, null, null);
    }
    
    /**
     * The counterpart of {@link #showAddConnectionDialog(JDBCDriver, String, String, String) } 
     * which returns the newly created database connection, but must be called 
     * from the event dispatching thread.
     *
     * @param driver the JDBC driver; can be null.
     * @param databaseUrl the database URL; can be null.
     * @param user the database user; can be null.
     * @param password user's password; can be null.
     *
     * @return the new database connection or null if no database connection
     *         was created (e.g. the user pressed Cancel).
     *
     * @throws IllegalStateException if the calling thread is not the event
     *         dispatching thread.
     *
     * @since 1.19
     */
    public DatabaseConnection showAddConnectionDialogFromEventThread(JDBCDriver driver, String databaseUrl, String user, String password) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("The current thread is not the event dispatching thread."); // NOI18N
        }
        org.netbeans.modules.db.explorer.DatabaseConnection internalDBConn = new ConnectUsingDriverAction.NewConnectionDialogDisplayer().showDialog(driver, databaseUrl, user, password);
        if (internalDBConn != null) {
            return internalDBConn.getDatabaseConnection();
        }
        return null;
    }
    
    /**
     * Shows the Connect dialog for the specified connection if not all data 
     * needed to connect, such as the user name or password, 
     * are known), or displays a modal progress dialog and attempts
     * to connect to the database immediately.
     *
     * @param dbconn the database connection to be connected
     *
     * @throws NullPointerException if the dbconn parameter is null
     * @throws IllegalStateException if this connection is not added to the
     *         ConnectionManager.
     */
    public void showConnectionDialog(DatabaseConnection dbconn) {
        if (dbconn == null) {
            throw new NullPointerException();
        }
        if (!ConnectionList.getDefault().contains(dbconn.getDelegate())) {
            throw new IllegalStateException("This connection is not added to the ConnectionManager."); // NOI18N
        }
        dbconn.getDelegate().showConnectionDialog();
    }

    /**
     * Disconnects this connection from the database. Does not do anything
     * if not connected.
     *
     * @param dbconn the database connection to be connected
     *
     * @throws NullPointerException if the dbconn parameter is null
     * @throws IllegalStateException if this connection is not added to the
     *         ConnectionManager.
     */
    public void disconnect(DatabaseConnection dbconn) {
        if (dbconn == null) {
            throw new NullPointerException();
        }
        if (!ConnectionList.getDefault().contains(dbconn.getDelegate())) {
            throw new IllegalStateException("This connection is not added to the ConnectionManager."); // NOI18N
        }
        try {
            dbconn.getDelegate().disconnect();
        } catch (DatabaseException e) {
            // XXX maybe shouldn't catch the exception
            ErrorManager.getDefault().notify(e);
        }
    }
    
    /**
     * Selects the node corresponding to the specified connection in the
     * Runtime tab.
     *
     * @param dbconn the connection to select
     *
     * @throws NullPointerException if the dbconn parameter is null
     * @throws IllegalStateException if this connection is not added to the
     *         ConnectionManager.
     */
    public void selectConnectionInExplorer(DatabaseConnection dbconn) {
        if (dbconn == null) {
            throw new NullPointerException();
        }
        if (!ConnectionList.getDefault().contains(dbconn.getDelegate())) {
            throw new IllegalStateException("This connection is not added to the ConnectionManager."); // NOI18N
        }
        dbconn.getDelegate().selectInExplorer();
    }
    
    /**
     * Registers a ConnectionListener.
     */
    public void addConnectionListener(ConnectionListener listener) {
        ConnectionList.getDefault().addConnectionListener(listener);
    }
    
    /**
     * Unregisters the specified connection listener.
     */
    public void removeConnectionListener(ConnectionListener listener) {
        ConnectionList.getDefault().removeConnectionListener(listener);
    }
}
