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
