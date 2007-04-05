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

package org.netbeans.modules.debugger.jpda.heapwalk.views;

import com.sun.tools.profiler.heap.Heap;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import javax.swing.JPanel;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.profiler.heapwalk.ClassesController;
import org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker;
import org.netbeans.modules.profiler.heapwalk.InstancesController;

import org.netbeans.modules.debugger.jpda.heapwalk.HeapImpl;

import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * The Instances View that refers to Profiler Heap Walking UI
 * 
 * @author Martin Entlicher
 */
public class ClassesCountsView extends TopComponent implements org.openide.util.HelpCtx.Provider {
    
    private transient EngineListener listener;
    private transient JPanel content;
    private transient HeapFragmentWalker hfw;
    
    /**
     * Creates a new instance of ClassesCountsView
     */
    public ClassesCountsView () {
        setIcon (Utilities.loadImage ("org/netbeans/modules/debugger/jpda/resources/root.gif")); // NOI18N
    }
    
    private void setUp() {
        DebuggerManager.getDebuggerManager ().addDebuggerListener (
            DebuggerManager.PROP_CURRENT_ENGINE,
            listener
        );
        setContent();
    }
    
    private void setContent() {
        assert javax.swing.SwingUtilities.isEventDispatchThread();
        JPDADebugger debugger = null;
        DebuggerEngine engine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (engine != null) {
            debugger = (JPDADebugger) engine.lookupFirst(null, JPDADebugger.class);
        }
        if (content != null) {
            remove(content);
            content = null;
        }
        if (debugger != null && debugger.canGetInstanceInfo()) {
            Heap heap = new HeapImpl(debugger);
            setLayout (new BorderLayout ());
            hfw = new DebuggerHeapFragmentWalker(heap);
            //InstancesController ic = new InstancesController(hfw);
            //ic.getPanel();
            ClassesController cc = hfw.getClassesController();//new ClassesController(hfw);
            content = cc.getPanel();
            add(content, "Center");
        }
    }
    
    HeapFragmentWalker getCurrentFragmentWalker() {
        return hfw;
    }
    
    protected void componentShowing () {
        super.componentShowing ();
        if (listener == null) {
            listener = new EngineListener();
        }
        setUp();
    }
    
    protected void componentHidden () {
        super.componentHidden ();
        if (content != null) {
            remove(content);
            content = null;
            hfw = null;
        }
        if (listener != null) {
            DebuggerManager.getDebuggerManager ().removeDebuggerListener (
                DebuggerManager.PROP_CURRENT_ENGINE,
                listener
            );
            listener = null;
        }
    }
    
    // <RAVE>
    // Implement getHelpCtx() with the correct helpID
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx("NetbeansDebuggerInstancesNode"); // NOI18N
    }
    // </RAVE>
    
    public int getPersistenceType () {
        return PERSISTENCE_ALWAYS;
    }
        
    public boolean requestFocusInWindow () {
        super.requestFocusInWindow ();
        if (content == null) return false;
        return content.requestFocusInWindow ();
    }
    
    public String getName () {
        //return "Class Counts";
        return NbBundle.getMessage (ClassesCountsView.class, "CTL_Classes_view");
    }
    
    public String getToolTipText () {
        //return "Class Counts";
        return NbBundle.getMessage (ClassesCountsView.class, "CTL_Classes_tooltip");// NOI18N
    }
    
    private final class EngineListener extends DebuggerManagerAdapter {
        
        public void propertyChange (PropertyChangeEvent e) {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setContent();
                }
            });
        }
    
    }
}
