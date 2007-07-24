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
