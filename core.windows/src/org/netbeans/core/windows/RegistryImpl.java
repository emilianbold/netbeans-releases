/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.MenuElement;

import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;
import org.openide.nodes.Node;

import org.netbeans.core.NbTopManager;
import org.openide.windows.WindowManager;

/** Implementstion of registry of top components. This implementation
 * receives information about top component changes from the window
 * manager implementation, to which is listening to.
 *
 * @author Dafe Simonek
 */
public final class RegistryImpl extends Object
implements TopComponent.Registry, TopComponentListener, StateManager.StateListener {
    
    // fields
    /** Activated top component */
    private TopComponent activatedTopComponent;
    /** Set of opened TopComponents */
    private HashSet openSet;
    /** Currently selected nodes. */
    private Node[] current;
    /** Last non-null value of current nodes */
    private Node[] activated;
    /** PropertyChange support */
    private PropertyChangeSupport support;
    /** Flag indicating whether listener was set on StateManager */
    private boolean listenerInited;
    

    /** Creates new RegistryImpl */
    public RegistryImpl() {
        support = new PropertyChangeSupport(this);
        openSet = new HashSet(30);
        activated = new Node[0];
        
        // force registry to listen to top components' events
        WindowManagerImpl.getInstance().addTopComponentListener(this);
    }
    
    /** Get all opened componets in the system.
     *
     * @return immutable set of {@link TopComponent}s
     */
    public synchronized Set getOpened() {
        return java.util.Collections.unmodifiableSet(openSet);
    }
    
    /** Get the currently selected element.
     * @return the selected top component, or <CODE>null</CODE> if there is none
     */
    public TopComponent getActivated() {
        return activatedTopComponent;
    }
    
    /** Getter for the currently selected nodes.
     * @return array of nodes or null if no component activated or it returns
     *   null from getActivatedNodes ().
     */
    public Node[] getCurrentNodes() {
        if (activatedTopComponent == null) {
            return NbTopManager.getUninitialized().getDefaultNodes(false);
        }
        
        return current;
    }
    
    /** Getter for the lastly activated nodes. Comparing
     * to previous method it always remembers the selected nodes
     * of the last component that had ones.
     *
     * @return array of nodes (not null)
     */
    public Node[] getActivatedNodes() {
        if (activatedTopComponent == null) {
            return NbTopManager.getUninitialized().getDefaultNodes(true);
        }
        
        return activated;
    }
    
    /** Add a property change listener.
     * @param l the listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        support.addPropertyChangeListener(l);
    }
    
    /** Remove a property change listener.
     * @param l the listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        support.removePropertyChangeListener(l);
    }
    
    /** Called when a TopComponent is activated.
     *
     * @param ev TopComponentChangedEvent
     */
    public void topComponentActivated(TopComponentChangedEvent ev) {
        TopComponent old = activatedTopComponent;
        activatedTopComponent = ev.topComponent;
        
        java.awt.Window w = ev.topComponent == null ?
        null : SwingUtilities.windowForComponent(ev.topComponent);
        
        cancelMenu(w);
        
        
        if (old == activatedTopComponent) {
            return;
        }
        
        support.firePropertyChange(PROP_ACTIVATED, old, activatedTopComponent);
        
        // XXX #23113. Inform also about activated nodes change, getActivatedNodes
        // method return different values after these cases of change.
        if(old == null && activatedTopComponent != null) {
            support.firePropertyChange(PROP_ACTIVATED_NODES, new Node[0], activated);
        } else if(old != null && activatedTopComponent == null) {
            support.firePropertyChange(PROP_ACTIVATED_NODES, activated, new Node[0]);
        }
    }
    
    
    
    /** Called when a TopComponent is opened.
     *
     * @param ev TopComponentChangedEvent
     */
    public synchronized void topComponentOpened(TopComponentChangedEvent ev) {
        if (openSet.contains(ev.topComponent)) {
            return;
        }
        Set old = (Set) openSet.clone();
        openSet.add(ev.topComponent);
        support.firePropertyChange(PROP_OPENED, old, openSet);
    }
    
    /** Called when a TopComponent is closed.
     *
     * @param ev TopComponentChangedEvent
     */
    public synchronized void topComponentClosed(TopComponentChangedEvent ev) {
        if (! openSet.contains(ev.topComponent)) {
            return;
        }
        // we should remove it from the set only if it is closed
        // on all workspaces
        WindowManagerImpl wm = (WindowManagerImpl)WindowManager.getDefault();
        Workspace[] workspaces = wm.getWorkspaces();
        for (int i = 0; i < workspaces.length; i++) {
            if (wm.findManager(ev.topComponent).isOpened(workspaces[i]))
                return;
        }
        // conditions satisfied, now remove...
        Set old = (Set) openSet.clone();
        openSet.remove(ev.topComponent);
        support.firePropertyChange(PROP_OPENED, old, openSet);
    }
    
    /** Called when selected nodes change..
     *
     * @param ev TopComponentChangedEvent
     */
    public void selectedNodesChanged(SelectedNodesChangedEvent ev) {
        Node[] old = current;
        Node[] c = ev.getSelectedNodes();
        
        //Fixed bug #8933 27 Mar 2001 by Marek Slama
        //Selected nodes event was processed for other than activated component.
        //Check if activatedTopComponent is the same as event source top component
        //If not ignore event
        if (activatedTopComponent != null) {
            if (!activatedTopComponent.equals(ev.topComponent)) {
                return;
            }
        }
        //End of bugfix #8933
        
        if (Arrays.equals(old, c)) {
            return;
        }
        if (!listenerInited) {
            // initialize and start to listen to state changes
            listenerInited = true;
            StateManager.getDefault().addStateListener(this);
        }
        current = c == null ? null : (Node[])c.clone();
        // fire immediatelly only if window manager in proper state
        tryFireChanges(StateManager.getDefault().getState(), old, current);
    }
    
    /** called when state of window manager changes */
    public void stateChanged(int state) {
        tryFireChanges(state, null, current);
    }
    
    /** Cancels the menu if it is not assigned to specified window.
     * @param window window that the menu should be checked against
     *    (if this window contains the menu, then the menu will not be closed)
     */
    /** Closes popup menu.
     */
    static void cancelMenu(java.awt.Window window) {
        MenuSelectionManager msm = MenuSelectionManager.defaultManager();
        MenuElement[] path = msm.getSelectedPath();
        
        for (int i = 0; i < path.length; i++) {
            //      if (newPath[i] != path[i]) return;
            java.awt.Window w = SwingUtilities.windowForComponent(
            path[i].getComponent()
            );
            
            // we must check for null because windowForComponent above can return null
            if ((w != null) && (w == window || w.getOwner() == window)) {
                // ok, this menu can stay
                return;
            }
            
        }
        
        msm.clearSelectedPath();
    }
    
    
    /** If window manager in proper state, fire selected and
     * activated node changes */
    private void tryFireChanges(int state, Node[] oldNodes, Node[] newNodes) {
        if (state == (StateManager.READY | StateManager.VISIBLE)) {
            support.firePropertyChange(PROP_CURRENT_NODES, oldNodes, newNodes);
            if (newNodes != null) {
                oldNodes = activated;
                activated = newNodes;
                support.firePropertyChange(PROP_ACTIVATED_NODES, oldNodes, activated);
            }
        } else {
            // defer firing, do nothing now
        }
    }
    
}
