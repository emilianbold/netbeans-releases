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

import java.awt.BorderLayout;
import org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Martin Entlicher
 */
public class InstancesView extends TopComponent {
    
    private javax.swing.JPanel hfwPanel;
    private HeapFragmentWalker hfw;
    
    /** Creates a new instance of InstancesView */
    public InstancesView() {
        setIcon (Utilities.loadImage ("org/netbeans/modules/debugger/jpda/resources/root.gif")); // NOI18N
    }

    protected void componentShowing() {
        super.componentShowing ();
        ClassesCountsView cc = (ClassesCountsView) WindowManager.getDefault().findTopComponent("classes");
        HeapFragmentWalker hfw = cc.getCurrentFragmentWalker();
        if (hfw != null) {
            setHeapFragmentWalker(hfw);
        }
    }
    
    protected void componentHidden () {
        super.componentHidden ();
        if (hfwPanel != null) {
            remove(hfwPanel);
            hfwPanel = null;
        }
        hfw = null;
    }
    
    public void setHeapFragmentWalker(HeapFragmentWalker hfw) {
        if (hfwPanel != null) {
            remove(hfwPanel);
            hfwPanel = null;
        }
        this.hfw = hfw;
        setLayout (new BorderLayout ());
        java.awt.Container header;
        header = (java.awt.Container) hfw.getInstancesController().getFieldsBrowserController().getPanel().getComponent(0);
        header.getComponent(1).setVisible(false);
        header = (java.awt.Container) hfw.getInstancesController().getInstancesListController().getPanel().getComponent(0);
        header.getComponent(1).setVisible(false);
        header = (java.awt.Container) hfw.getInstancesController().getReferencesBrowserController().getPanel().getComponent(0);
        header.getComponent(1).setVisible(false);
        hfwPanel = hfw.getInstancesController().getPanel();
        add(hfwPanel, "Center");
    }
    
    public HeapFragmentWalker getCurrentFragmentWalker() {
        return hfw;
    }
    
    public String getName () {
        return NbBundle.getMessage (InstancesView.class, "CTL_Instances_view");
    }
    
    public String getToolTipText () {
        return NbBundle.getMessage (InstancesView.class, "CTL_Instances_tooltip");// NOI18N
    }

    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }
    
}
