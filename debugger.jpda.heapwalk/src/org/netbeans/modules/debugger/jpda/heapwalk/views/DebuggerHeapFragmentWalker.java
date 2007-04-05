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

import javax.swing.JPanel;

import org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker;
import org.netbeans.modules.profiler.heapwalk.HeapWalker;

import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author martin
 */
public class DebuggerHeapFragmentWalker extends HeapFragmentWalker {
    
    /** Creates a new instance of DebuggerHeapFragmentWalker */
    public DebuggerHeapFragmentWalker(Heap heap) {
        super(heap, new HeapWalker(heap));
    }

    public JPanel getPanel() {
        // Not supported
        return null;
    }

    public void switchToClassesView() {
        openComponent("classesCounts", true);
    }

    public void switchToInstancesView() {
        openComponent("dbgInstances", true);
    }
    
    static TopComponent openComponent (String viewName, boolean activate) {
        TopComponent view = WindowManager.getDefault().findTopComponent(viewName);
        if (view == null) {
            throw new IllegalArgumentException(viewName);
        }
        view.open();
        if (activate) {
            view.requestActive();
        }
        return view;
    }
    
}
