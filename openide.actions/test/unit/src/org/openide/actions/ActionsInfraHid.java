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

package org.openide.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Set;
import org.netbeans.junit.MockServices;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ContextGlobalProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;

/** Utilities for actions tests.
 * @author Jesse Glick
 */
public abstract class ActionsInfraHid {
    
    private ActionsInfraHid() {}
    
    public static final UsefulThings UT;
    static {
        MockServices.setServices(new Class[] {UsefulThings.class});
        UT = (UsefulThings) Lookup.getDefault().lookup(UsefulThings.class);
    }
    
    /** An action manager and top component registry.
     */
    public static final class UsefulThings implements TopComponent.Registry, ContextGlobalProvider {
        // Registry:
        private TopComponent activated;
        /** instances to keep */
        private InstanceContent ic = new InstanceContent ();
        /** lookup */
        private Lookup lookup = new AbstractLookup (ic);
        /** changes */
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        
        public void addPropertyChangeListener(PropertyChangeListener l) {
            pcs.addPropertyChangeListener(l);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener l) {
            pcs.removePropertyChangeListener(l);
        }
        
        private void firePropertyChange(String p, Object o, Object n) {
            pcs.firePropertyChange(p, o, n);
        }
        
        
        public TopComponent getActivated() {
            return activated;
        }
        
        public void setActivated(TopComponent nue) {
            TopComponent old = activated;
            activated = nue;
            firePropertyChange(PROP_ACTIVATED, old, nue);
            updateLookup ();
        }
        
        private Node[] activatedNodes = new Node[0];
        private Node[] currentNodes = null;
        
        public Node[] getActivatedNodes() {
            return activatedNodes;
        }
        
        public Node[] getCurrentNodes() {
            return currentNodes;
        }
        
        public void setCurrentNodes(Node[] nue) {
            if (nue != null) {
                Node[] old = activatedNodes;
                activatedNodes = nue;
                firePropertyChange(PROP_ACTIVATED_NODES, old, nue);
            }
            Node[] old = currentNodes;
            currentNodes = nue;
            firePropertyChange(PROP_CURRENT_NODES, old, nue);
            updateLookup ();
        }
        
        private Set opened = null;
        
        public Set getOpened() {
            return opened;
        }
        
        public void setOpened(Set nue) {
            Set old = opened;
            opened = nue;
            firePropertyChange(PROP_OPENED, old, nue);
        }
        
        private void updateLookup () {
            ArrayList items = new ArrayList ();
            if (currentNodes != null) {
                for (int i = 0; i < currentNodes.length; i++) {
                    items.add (new IPair (currentNodes[i]));
                }
            } else {
                items.add (IPair.NULL_NODES);
            }
            if (activated != null) {
                items.add (new IPair (activated.getActionMap ()));
            }
            ic.setPairs (items);
        }
                
        //
        // ContextGlobalProvider
        //
        public Lookup createGlobalContext() {
            return lookup;
        }
    }
    
    /** Prop listener that will tell you if it gets a change.
     */
    public static final class WaitPCL implements PropertyChangeListener {
        /** whether a change has been received, and if so count */
        public int gotit = 0;
        /** optional property name to filter by (if null, accept any) */
        private final String prop;
        public WaitPCL(String p) {
            prop = p;
        }
        public synchronized void propertyChange(PropertyChangeEvent evt) {
            if (prop == null || prop.equals(evt.getPropertyName())) {
                gotit++;
                notifyAll();
            }
        }
        public boolean changed() {
            return changed(1500);
        }
        public synchronized boolean changed(int timeout) {
            if (gotit > 0) {
                return true;
            }
            try {
                wait(timeout);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            return gotit > 0;
        }
    }

    // Stolen from RequestProcessorTest.
    public static void doGC() {
        doGC(10);
    }
    public static void doGC(int count) {
        ArrayList l = new ArrayList(count);
        while (count-- > 0) {
            System.gc();
            System.runFinalization();
            l.add(new byte[1000]);
        }
    }

    private static final class IPair extends AbstractLookup.Pair {
        private Object obj;
        
        public static final IPair NULL_NODES = new IPair(new AbstractNode(Children.LEAF));
        
        public IPair (Object obj) {
            this.obj = obj;
        }
        
        protected boolean creatorOf(Object obj) {
            return this.obj == obj;
        }
        
        public String getDisplayName() {
            return obj.toString ();
        }
        
        public String getId() {
            if (this == NULL_NODES) {
                return "none"; // NOI18N
            }
            return obj.toString ();
        }
        
        public Object getInstance() {
            if (this == NULL_NODES) {
                return null;
            }
            return obj;
        }
        
        public Class getType() {
            return obj.getClass();
        }
        
        protected boolean instanceOf(Class c) {
            return c.isInstance(obj);
        }
        
    } // end of IPair
}
