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

package org.netbeans.modules.debugger.jpda.projects;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.ActionsManager;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.openide.ErrorManager;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
*
* @author   Jan Jancura
*/
public class FixActionProvider extends ActionsProviderSupport {

    private JPDADebugger debugger;
    private Listener listener;
    
    
    public FixActionProvider (ContextProvider lookupProvider) {
        debugger = (JPDADebugger) lookupProvider.lookupFirst 
            (null, JPDADebugger.class);
        
        listener = new Listener ();
        MainProjectManager.getDefault ().addPropertyChangeListener (listener);
        debugger.addPropertyChangeListener (JPDADebugger.PROP_STATE, listener);
        TopComponent.getRegistry ().addPropertyChangeListener (listener);
        
        setEnabled (
            ActionsManager.ACTION_FIX,
            shouldBeEnabled ()
        );
    }
    
    private void destroy () {
        debugger.removePropertyChangeListener (JPDADebugger.PROP_STATE, listener);
        MainProjectManager.getDefault ().removePropertyChangeListener (listener);
        TopComponent.getRegistry ().removePropertyChangeListener (listener);
    }
    
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_FIX);
    }
    
    public void doAction (Object action) {
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        invokeAction();
                    }
                });
            } catch (InterruptedException iex) {
                // Procceed
            } catch (java.lang.reflect.InvocationTargetException itex) {
                ErrorManager.getDefault().notify(itex);
            }
        } else {
            invokeAction();
        }
    }
    
    private void invokeAction() {
        ((ActionProvider) getCurrentProject().getLookup ().lookup (
                ActionProvider.class
            )).invokeAction (
                JavaProjectConstants.COMMAND_DEBUG_FIX, 
                getLookup ()
            );
    }

    /**
     * Returns the project that the active node's fileobject belongs to. 
     * If this cannot be determined for some reason, returns the main project.
     *  
     * @return the project that the active node's fileobject belongs to
     */ 
    private Project getCurrentProject() {
        Node[] nodes = TopComponent.getRegistry ().getActivatedNodes ();
        if (nodes == null || nodes.length == 0) return MainProjectManager.getDefault().getMainProject();
        DataObject dao = (DataObject) nodes[0].getCookie(DataObject.class);
        if (dao == null) return MainProjectManager.getDefault().getMainProject();
        return FileOwnerQuery.getOwner(dao.getPrimaryFile());        
    }
    
    private boolean shouldBeEnabled () {
        // check if current debugger supports this action
        if (!debugger.canFixClasses()) return false;
        // check if current project supports this action
        Project p = getCurrentProject();
        if (p == null) return false;
        ActionProvider actionProvider = (ActionProvider) p.getLookup ().
            lookup (ActionProvider.class);
        if (actionProvider == null) return false;
        String[] sa = actionProvider.getSupportedActions ();
        int i, k = sa.length;
        for (i = 0; i < k; i++)
            if (JavaProjectConstants.COMMAND_DEBUG_FIX.equals (sa [i]))
                break;
        if (i == k) return false;

        // check if this action should be enabled
        return ((ActionProvider) p.getLookup ().lookup (
                ActionProvider.class
            )).isActionEnabled (
                JavaProjectConstants.COMMAND_DEBUG_FIX, 
                getLookup ()
            );
    }
    
    private Lookup getLookup () {
        Node[] nodes = TopComponent.getRegistry ().getActivatedNodes ();
        int i, k = nodes.length;
        ArrayList l = new ArrayList ();
        for (i = 0; i < k; i++) {
            Object o = nodes [i].getCookie (DataObject.class);
            if (o != null)
                l.add (o);
        }
        return Lookups.fixed (l.toArray (new DataObject [l.size ()]));
    }
    
    private class Listener implements PropertyChangeListener, 
    DebuggerManagerListener {
        public Listener () {}
        
        public void propertyChange (PropertyChangeEvent e) {
            boolean en = shouldBeEnabled ();
            setEnabled (
                ActionsManager.ACTION_FIX,
                en
            );
            if (debugger.getState () == JPDADebugger.STATE_DISCONNECTED) 
                destroy ();
        }
        public void sessionRemoved (Session session) {}
        public void breakpointAdded (Breakpoint breakpoint) {}
        public void breakpointRemoved (Breakpoint breakpoint) {}
        public Breakpoint[] initBreakpoints () {return new Breakpoint [0];}
        public void initWatches () {}
        public void sessionAdded (Session session) {}
        public void watchAdded (Watch watch) {}
        public void watchRemoved (Watch watch) {}
        public void engineAdded (DebuggerEngine engine) {}
        public void engineRemoved (DebuggerEngine engine) {}
    }
}
