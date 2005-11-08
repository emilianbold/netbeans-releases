/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.dlg;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.db.explorer.DatabaseConnection;

/**
 * Base class for the connection dialogs.
 *
 * @author Andrei Badea
 */
public abstract class ConnectionDialogMediator {
    
    private List/*<ConnectionProgressListener>*/ listeners = new ArrayList/*<ConnectionProgressListener>*/();
    
    public void addConnectionProgressListener(ConnectionProgressListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    protected abstract boolean retrieveSchemas(SchemaPanel schemaPanel, DatabaseConnection dbcon, String defaultSchema);
    
    protected void fireConnectionStarted() {
        for (Iterator i = listenersCopy(); i.hasNext();) {
            ((ConnectionProgressListener)i.next()).connectionStarted();
        }
    }
    
    protected void fireConnectionStep(String step) {
        for (Iterator i = listenersCopy(); i.hasNext();) {
            ((ConnectionProgressListener)i.next()).connectionStep(step);
        }
    }
    
    protected void fireConnectionFinished() {
        for (Iterator i = listenersCopy(); i.hasNext();) {
            ((ConnectionProgressListener)i.next()).connectionFinished();
        }
    }

    protected void fireConnectionFailed() {
        for (Iterator i = listenersCopy(); i.hasNext();) {
            ((ConnectionProgressListener)i.next()).connectionFailed();
        }
    }
    
    private Iterator/*<ConnectionProgressListener>*/ listenersCopy() {
        List listenersCopy = null;
        synchronized (listeners) {
            listenersCopy = new ArrayList(listeners);
        }
        return listenersCopy.iterator();
    }
}
