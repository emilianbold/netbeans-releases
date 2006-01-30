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

package org.netbeans.modules.debugger.ui.views;

import java.awt.BorderLayout;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import javax.swing.JComponent;
import org.netbeans.spi.viewmodel.Models;

import org.netbeans.modules.debugger.ui.Utils;

import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;


// <RAVE>
// Implement HelpCtx.Provider interface to provide help ids for help system
// public class CallStackView extends TopComponent {
// ====
public class View extends TopComponent implements org.openide.util.HelpCtx.Provider {
// </RAVE>
    
    public static final String BREAKPOINTS_VIEW_NAME = "BreakpointsView";
    public static final String CALLSTACK_VIEW_NAME = "CallStackView";
    public static final String LOCALS_VIEW_NAME = "LocalsView";
    public static final String SESSIONS_VIEW_NAME = "SessionsView";
    public static final String THREADS_VIEW_NAME = "ThreadsView";
    public static final String WATCHES_VIEW_NAME = "WatchesView";
    
    private transient JComponent tree;
    private transient ViewModelListener viewModelListener;
    private String name; // Store just the name persistently, we'll create the component from that
    private transient String helpID;
    private transient String propertiesHelpID;
    private transient String displayNameResource;
    private transient String toolTipResource;
    
    private View (String icon, String name, String helpID, String propertiesHelpID,
                  String displayNameResource, String toolTipResource) {
        setIcon (Utils.getIcon(icon).getImage());
        this.name = name;
        this.helpID = helpID;
        this.propertiesHelpID = propertiesHelpID;
        this.displayNameResource = displayNameResource;
        this.toolTipResource = toolTipResource;
    }

    protected String preferredID() {
        return this.getClass().getPackage().getName() + "." + name;
    }

    protected void componentShowing () {
        super.componentShowing ();
        if (viewModelListener != null)
            return;
        if (tree == null) {
            setLayout (new BorderLayout ());
            tree = Models.createView (Models.EMPTY_MODEL);
            tree.setName (NbBundle.getMessage (View.class, toolTipResource));
            add (tree, "Center");  //NOI18N
        }
        // <RAVE> CR 6207738 - fix debugger help IDs
        // Use the modified constructor that stores the propertiesHelpID
        // for nodes in this view
        // viewModelListener = new ViewModelListener (
        //     "ThreadsView",
        //     tree
        // );
        // ====
        viewModelListener = new ViewModelListener (
            name,
            tree,
            propertiesHelpID
        );
        // </RAVE>
    }
    
    protected void componentHidden () {
        super.componentHidden ();
        if (viewModelListener != null) {
            viewModelListener.destroy ();
            viewModelListener = null;
        }
    }
    
    // <RAVE>
    // Implement getHelpCtx() with the correct help ID
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx(helpID);
    }
    // </RAVE>
    
    public int getPersistenceType () {
        return PERSISTENCE_ALWAYS;
    }
        
    public boolean requestFocusInWindow () {
        super.requestFocusInWindow ();
        if (tree == null) return false;
        return tree.requestFocusInWindow ();
    }
    
    public String getName () {
        return NbBundle.getMessage (View.class, displayNameResource);
    }
    
    public String getToolTipText () {
        return NbBundle.getMessage (View.class, toolTipResource);// NOI18N
    }
    
    public Object writeReplace() {
        return new ResolvableHelper(name);
    }
     
    
    
    /**
     * The serializing class.
     */
    private static final class ResolvableHelper implements Externalizable {
        
        private String name;
        
        private static final long serialVersionUID = 1L;
        
        public ResolvableHelper(String name) {
            this.name = name;
        }
        
        public ResolvableHelper() {
            // Just for the purpose of deserialization
        }
        
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(name);
        }
        
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            name = (String) in.readObject();
        }
        
        public Object readResolve() {
            return View.getView(name);
        }
    }
    
    
    /** Creates the view. Call from the module layer only!
     * @deprecated Do not call.
     */
    public static synchronized TopComponent getBreakpointsView() {
        return new View(
            "org/netbeans/modules/debugger/resources/breakpointsView/Breakpoint",
            BREAKPOINTS_VIEW_NAME,
            "NetbeansDebuggerBreakpointNode",
            null,
            "CTL_Breakpoints_view",
            "CTL_Breakpoints_view_tooltip"
        );
    }
    
    /** Creates the view. Call from the module layer only!
     * @deprecated Do not call.
     */
    public static synchronized TopComponent getCallStackView() {
        return new View(
            "org/netbeans/modules/debugger/resources/allInOneView/CallStack",
            CALLSTACK_VIEW_NAME,
            "NetbeansDebuggerCallStackNode",
            null,
            "CTL_Call_stack_view",
            "CTL_Call_stack_view_tooltip"
        );
    }
    
    /** Creates the view. Call from the module layer only!
     * @deprecated Do not call.
     */
    public static synchronized TopComponent getLocalsView() {
        return new View(
            "org/netbeans/modules/debugger/resources/localsView/LocalVariable",
            LOCALS_VIEW_NAME,
            "NetbeansDebuggerVariableNode",
            null,
            "CTL_Variables_view",
            "CTL_Locals_view_tooltip"
        );
    }
    
    /** Creates the view. Call from the module layer only!
     * @deprecated Do not call.
     */
    public static synchronized TopComponent getSessionsView() {
        return new View(
            "org/netbeans/modules/debugger/resources/sessionsView/Session",
            SESSIONS_VIEW_NAME,
            "NetbeansDebuggerSessionNode",
            "NetbeansDebuggerSessionsPropertiesSheet",
            "CTL_Sessions_view",
            "CTL_Sessions_view_tooltip"
        );
    }
    
    /** Creates the view. Call from the module layer only!
     * @deprecated Do not call.
     */
    public static synchronized TopComponent getThreadsView() {
        return new View(
            "org/netbeans/modules/debugger/resources/threadsView/RunningThread",
            THREADS_VIEW_NAME,
            "NetbeansDebuggerThreadNode",
            "NetbeansDebuggerThreadsPropertiesSheet",
            "CTL_Threads_view",
            "CTL_Threads_view_tooltip"
        );
    }
    
    /** Creates the view. Call from the module layer only!
     * @deprecated Do not call.
     */
    public static synchronized TopComponent getWatchesView() {
        return new View(
            "org/netbeans/modules/debugger/resources/watchesView/Watch",
            WATCHES_VIEW_NAME,
            "NetbeansDebuggerWatchNode",
            null,
            "CTL_Watches_view",
            "CTL_Watches_view_tooltip"
        );
    }
    
    private static TopComponent getView(String viewName) {
        if (viewName.equals(BREAKPOINTS_VIEW_NAME)) {
            return getBreakpointsView();
        }
        if (viewName.equals(CALLSTACK_VIEW_NAME)) {
            return getCallStackView();
        }
        if (viewName.equals(LOCALS_VIEW_NAME)) {
            return getLocalsView();
        }
        if (viewName.equals(SESSIONS_VIEW_NAME)) {
            return getSessionsView();
        }
        if (viewName.equals(THREADS_VIEW_NAME)) {
            return getThreadsView();
        }
        if (viewName.equals(WATCHES_VIEW_NAME)) {
            return getWatchesView();
        }
        throw new IllegalArgumentException(viewName);
    }
    
}
