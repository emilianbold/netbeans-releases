/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
    private HeapFragmentWalkerProvider provider;
    
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
            provider = null;
        } else if (provider != null) {
            setHeapFragmentWalker(provider.getHeapFragmentWalker());
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
    
    public void setHeapFragmentWalkerProvider(HeapFragmentWalkerProvider hfwp) {
        provider = hfwp;
        setHeapFragmentWalker(hfwp.getHeapFragmentWalker());
    }
    
    private void setHeapFragmentWalker(HeapFragmentWalker hfw) {
        if (hfwPanel != null) {
            remove(hfwPanel);
            hfwPanel = null;
        }
        this.hfw = hfw;
        if (hfw == null) return ;
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
    
    public static interface HeapFragmentWalkerProvider {
        
        HeapFragmentWalker getHeapFragmentWalker();
        
    }
    
}
