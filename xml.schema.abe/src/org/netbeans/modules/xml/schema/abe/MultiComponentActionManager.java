/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

/*
 * MultiComponentActionManager.java
 *
 * Created on June 9, 2006, 1:47 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JPopupMenu;
import org.openide.awt.MouseUtils;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOp;

/**
 *
 * @author girix
 */
public class MultiComponentActionManager {
    InstanceUIContext context;
    /** Creates a new instance of MultiComponentActionManager */
    public MultiComponentActionManager(InstanceUIContext context) {
        this.context = context;
    }
    
    public void deleteSelectedComponents(){
        ArrayList<ABEBaseDropPanel> list = context.getComponentSelectionManager().getSelectedComponentList();
        ArrayList<Component> cloneList = (ArrayList<Component>) list.clone();
        context.getComponentSelectionManager().clearPreviousSelectedComponents(true);
        for(Component comp: cloneList){
            //list.remove(comp);
            if(comp instanceof TagPanel){
                ((TagPanel) comp).removeElement();
            }else if(comp instanceof AttributePanel){
                ((AttributePanel)comp).removeAttribute();
            }else if(comp instanceof CompositorPanel){
                ((CompositorPanel)comp).removeCompositor();
            }
        }
    }
    
    public void showPopupMenu(MouseEvent e, ABEBaseDropPanel eventSource) {
        ArrayList<ABEBaseDropPanel> list = context.getComponentSelectionManager().getSelectedComponentList();

        if(!list.contains(eventSource)){
            //right click was performed on some other place than insde already selected items
            //so just remove previous selections and select the current component
            context.getComponentSelectionManager().
                    setSelectedComponent(eventSource);
            //now use this list for right click action
            list = context.getComponentSelectionManager().getSelectedComponentList();
        }
        
        ArrayList<Node> nodeList = new ArrayList<Node>();
        for(Component component: list){
            if(component instanceof ABEBaseDropPanel &&
                    ((ABEBaseDropPanel)component).getNBNode() != null){
                nodeList.add(((ABEBaseDropPanel)component).getNBNode());
            }
        }
        if(nodeList.size() > 0){
            //context.getTopComponent().setActivatedNodes(nodeList.toArray(new Node[0]));
            //get the available action in a popup menu
            //JPopupMenu menu = nodeList.get(0).getContextMenu();
            JPopupMenu menu = NodeOp.findContextMenu(nodeList.toArray(new Node[nodeList.size()]));
            //show the Popup
            Component hostComponent = e.getComponent();
            menu.show(hostComponent, e.getX(), e.getY());
        }
    }

    public void showPopupMenu(KeyEvent e, ABEBaseDropPanel eventSource) {
        ArrayList<ABEBaseDropPanel> list = context.getComponentSelectionManager().getSelectedComponentList();

        if(!list.contains(eventSource)){
            //right click was performed on some other place than insde already selected items
            //so just remove previous selections and select the current component
            context.getComponentSelectionManager().
                    setSelectedComponent(eventSource);
            //now use this list for right click action
            list = context.getComponentSelectionManager().getSelectedComponentList();
        }
        
        ArrayList<Node> nodeList = new ArrayList<Node>();
        for(Component component: list){
            if(component instanceof ABEBaseDropPanel &&
                    ((ABEBaseDropPanel)component).getNBNode() != null){
                nodeList.add(((ABEBaseDropPanel)component).getNBNode());
            }
        }
        if(nodeList.size() > 0){
            //context.getTopComponent().setActivatedNodes(nodeList.toArray(new Node[0]));
            //get the available action in a popup menu
            //JPopupMenu menu = nodeList.get(0).getContextMenu();
            JPopupMenu menu = NodeOp.findContextMenu(nodeList.toArray(new Node[nodeList.size()]));
            //show the Popup
            Component hostComponent = e.getComponent();
            menu.show(hostComponent, hostComponent.getWidth()/3, hostComponent.getHeight()/2);
        }
    }
    
}
