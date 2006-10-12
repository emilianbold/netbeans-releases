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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
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
    
    public static final String PROP_VALID = "valid"; // NOI18N
    
    private final List/*<ConnectionProgressListener>*/ connProgressListeners = new ArrayList/*<ConnectionProgressListener>*/();
    private final PropertyChangeSupport propChangeSupport = new PropertyChangeSupport(this);
    
    private boolean valid = true;
    
    public void addConnectionProgressListener(ConnectionProgressListener listener) {
        synchronized (connProgressListeners) {
            connProgressListeners.add(listener);
        }
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propChangeSupport.addPropertyChangeListener(listener);
    }
    
    protected abstract boolean retrieveSchemas(SchemaPanel schemaPanel, DatabaseConnection dbcon, String defaultSchema);
    
    protected void fireConnectionStarted() {
        for (Iterator i = connProgressListenersCopy(); i.hasNext();) {
            ((ConnectionProgressListener)i.next()).connectionStarted();
        }
    }
    
    protected void fireConnectionStep(String step) {
        for (Iterator i = connProgressListenersCopy(); i.hasNext();) {
            ((ConnectionProgressListener)i.next()).connectionStep(step);
        }
    }
    
    protected void fireConnectionFinished() {
        for (Iterator i = connProgressListenersCopy(); i.hasNext();) {
            ((ConnectionProgressListener)i.next()).connectionFinished();
        }
    }

    protected void fireConnectionFailed() {
        for (Iterator i = connProgressListenersCopy(); i.hasNext();) {
            ((ConnectionProgressListener)i.next()).connectionFailed();
        }
    }
    
    private Iterator/*<ConnectionProgressListener>*/ connProgressListenersCopy() {
        List listenersCopy = null;
        synchronized (connProgressListeners) {
            listenersCopy = new ArrayList(connProgressListeners);
        }
        return listenersCopy.iterator();
    }
    
    public void setValid(boolean valid) {
        this.valid = valid;
        propChangeSupport.firePropertyChange(PROP_VALID, null, null);
    }
    
    public boolean getValid() {
        return valid;
    }
}
