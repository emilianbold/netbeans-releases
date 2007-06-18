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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.ui.models;

import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Vector;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;


/**
 * @author   Jan Jancura
 */
public class SesionsNodeModel implements ExtendedNodeModel {

    public static final String SESSION =
        "org/netbeans/modules/debugger/resources/sessionsView/session_16.png";
    public static final String CURRENT_SESSION =
        "org/netbeans/modules/debugger/resources/sessionsView/CurrentSession.gif";

    private Vector listeners = new Vector ();
    private Listener listener;
    
    
    public String getDisplayName (Object o) throws UnknownTypeException {
        if (listener == null)
            listener = new Listener (this);
        if (o == TreeModel.ROOT) {
            return NbBundle.getBundle(SesionsNodeModel.class).getString("CTL_SessionsModel_Column_Name_Name");
        } else
        if (o instanceof Session) {
            return ((Session) o).getName ();
        } else
        throw new UnknownTypeException (o);
    }
    
    public String getShortDescription (Object o) throws UnknownTypeException {
        if (listener == null)
            listener = new Listener (this);
        if (o == TreeModel.ROOT) {
            return TreeModel.ROOT;
        } else
        if (o instanceof Session) {
            return null;
        } else
        throw new UnknownTypeException (o);
    }
    
    public String getIconBase (Object o) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not supported.");
    }

    public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
        if (listener == null)
            listener = new Listener (this);
        if (node == TreeModel.ROOT) {
            return SESSION;
        } else
        if (node instanceof Session) {
            if (node == DebuggerManager.getDebuggerManager ().getCurrentSession ())
                return CURRENT_SESSION;
            else
                return SESSION;
        } else
        throw new UnknownTypeException (node);
    }

    public boolean canRename(Object node) throws UnknownTypeException {
        return false;
    }

    public boolean canCopy(Object node) throws UnknownTypeException {
        return false;
    }

    public boolean canCut(Object node) throws UnknownTypeException {
        return false;
    }

    public Transferable clipboardCopy(Object node) throws IOException,
                                                          UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Transferable clipboardCut(Object node) throws IOException,
                                                         UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PasteType[] getPasteTypes(Object node, Transferable t) throws UnknownTypeException {
        return null;
    }

    public void setName(Object node, String name) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** 
     *
     * @param l the listener to add
     */
    public void addModelListener (ModelListener l) {
        listeners.add (l);
    }

    /** 
     *
     * @param l the listener to remove
     */
    public void removeModelListener (ModelListener l) {
        listeners.remove (l);
    }
    
    private void fireTreeChanged () {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((ModelListener) v.get (i)).modelChanged (
                new ModelEvent.TreeChanged (this)
            );
    }
    
    
    // innerclasses ............................................................
    
    
    /**
     * Listens on DebuggerManager on PROP_CURRENT_SESSION.
     */
    private static class Listener extends DebuggerManagerAdapter {
        
        private WeakReference ref;
        
        public Listener (
            SesionsNodeModel rm
        ) {
            ref = new WeakReference (rm);
            DebuggerManager.getDebuggerManager ().addDebuggerListener (
                DebuggerManager.PROP_CURRENT_SESSION,
                this
            );
        }
        
        private SesionsNodeModel getModel () {
            SesionsNodeModel rm = (SesionsNodeModel) ref.get ();
            if (rm == null) {
                DebuggerManager.getDebuggerManager ().
                    removeDebuggerListener (
                        DebuggerManager.PROP_CURRENT_SESSION,
                        this
                    );
            }
            return rm;
        }
        
        public void propertyChange (PropertyChangeEvent e) {
            SesionsNodeModel rm = getModel ();
            if (rm == null) return;
            rm.fireTreeChanged ();
        }
    }
}
