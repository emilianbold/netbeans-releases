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
package org.netbeans.modules.xslt.tmap.multiview.tree;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.nodes.LogicalTreeHandler;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 * 
 */
public class MultiviewTreeHandler extends LogicalTreeHandler {

    public MultiviewTreeHandler(ExplorerManager explorerManager,
            TMapModel tModel,
            Lookup contextLookup) 
    {
        super(explorerManager, tModel, contextLookup);
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
//        System.out.println("propertyChange: "+evt);
        
        
        String propertyName = evt.getPropertyName();
        TopComponent curTc = getCurrentTopComponent(); 
        if (curTc == null) {
            return;
        }
//        System.out.println("propertyChange name: "+propertyName);
        
        if (propertyName.equals(TopComponent.Registry.PROP_ACTIVATED)) {
            if (TopComponent.getRegistry().getActivated() == curTc) {
//                addUndoManager();
//                triggerValidation();
            }
        }
        else if (propertyName.equals(TopComponent.Registry.PROP_ACTIVATED_NODES)) {
           if (TopComponent.getRegistry().getActivated() != curTc) {
               doTreeNodeSelectionByActiveNode();
           }
           return;
            
        } else if (propertyName.equals(ExplorerManager.PROP_SELECTED_NODES)) {
            // NAVIGATOR SELECTED NODES SETTED AS ACTIVE NODES
            //navigatorTopComponent.setActivatedNodes(new Node[] {});
            curTc.setActivatedNodes((Node[])evt.getNewValue());
        } else if (propertyName.equals(ExplorerManager.PROP_ROOT_CONTEXT)) {
            //EVENT FOR PROPERTY PROP_ROOT_CONTEXT
            doTreeNodeSelectionByActiveNode();
        }
//        } else if (propertyName.equals(TopComponent.Registry.PROP_OPENED)) {
////            System.out.println("the set of the opened topComponent were changed");
//            TMapNavigatorController.activateLogicalPanel();
//        } /***/else if (propertyName.equals(TopComponent.Registry.PROP_CURRENT_NODES)) {
////            System.out.println("the set of currently selected nodes were changed");
//            TMapNavigatorController.activateLogicalPanel();
//        }               
        

    }
    
    private TopComponent getCurrentTopComponent() {
        BeanTreeView beanTreeView = getBeanTreeView();
        if (beanTreeView == null) {
            return null;
        }

        TopComponent tc = null;
        Component parent = beanTreeView;
        while ((parent = parent.getParent()) != null) {
            if (parent instanceof TopComponent) {
                tc = (TopComponent) parent;
                break;
            }
        }
        
        return tc;
    }
    
}
