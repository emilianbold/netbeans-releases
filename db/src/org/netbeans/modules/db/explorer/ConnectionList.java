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

package org.netbeans.modules.db.explorer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.db.explorer.ConnectionListener;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.explorer.nodes.RootNode;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;


/**
 * Class which encapsulates the Vector used to keep a list of connections
 * in the Database Explorer. It also can fire events when connections are added
 * and removed through ConnectionListener.
 *
 * This class only maintains a list of DBConnection objects. It has no links
 * to the UI (nodes representing these objects), therefore adding a DBConnection 
 * doesn't create a node for it.
 * 
 * @author Andrei Badea
 */
public class ConnectionList {
    
    private static ConnectionList DEFAULT;
    
    private Lookup.Result result = getLookupResult();
    
    private List/*<ConnectionListener>*/ listeners = new ArrayList(1);    
    
    public static synchronized ConnectionList getDefault() {
        if (DEFAULT == null) {
            DatabaseConnectionConvertor.importOldConnections();
            RootNode.getOption().save();
            DEFAULT = new ConnectionList();
        }
        return DEFAULT;
    }
    
    private ConnectionList() {
        result.addLookupListener(new LookupListener() {
            public void resultChanged(LookupEvent e) {
                fireListeners();
            }
        });
    }
    
    public DatabaseConnection[] getConnections() {
        Collection dbconns = result.allInstances();
        return (DatabaseConnection[])dbconns.toArray(new DatabaseConnection[dbconns.size()]);
    }
    
    public DatabaseConnection getConnection(DatabaseConnection impl) {
        if (impl == null) {
            throw new NullPointerException();
        }
        DatabaseConnection[] dbconns = getConnections();
        for (int i = 0; i < dbconns.length; i++) {
            if (impl.equals(dbconns[i])) {
                return dbconns[i];
            }
        }
        return null;
    }
    
    public void add(DatabaseConnection dbconn) throws DatabaseException {
        if (dbconn == null) {
            throw new NullPointerException();
        }
        try {
            DatabaseConnectionConvertor.create(dbconn);
        } catch (IOException e) {
            throw new DatabaseException(e);
        }
    }
    
    public boolean contains(DatabaseConnection dbconn) {
        return getConnection(dbconn) != null;
    }
    
    public void remove(DatabaseConnection dbconn) throws DatabaseException {
        if (dbconn == null) {
            throw new NullPointerException();
        }
        try {
            DatabaseConnectionConvertor.remove(dbconn);
        } catch (IOException e) {
            throw new DatabaseException(e);
        }
    }
    
    public void addConnectionListener(ConnectionListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    public void removeConnectionListener(ConnectionListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    private void fireListeners() {
        List listenersCopy;
        
        synchronized (listeners) {
            listenersCopy = new ArrayList(listeners);
        }
        
        for (Iterator i = listenersCopy.iterator(); i.hasNext();) {
            ConnectionListener l = (ConnectionListener)i.next();
            l.connectionsChanged();
        }
    }
    
    private synchronized Lookup.Result getLookupResult() {
        return Lookups.forPath(DatabaseConnectionConvertor.CONNECTIONS_PATH).lookupResult(DatabaseConnection.class);
    }
}
