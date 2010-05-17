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
 * ComponentSelectionManager.java
 *
 * Created on June 2, 2006, 11:27 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;

/**
 *
 * @author girix
 */
public class ComponentSelectionManager {
    InstanceUIContext context;
    private ArrayList<ABEBaseDropPanel> selectedComponentList = new ArrayList<ABEBaseDropPanel>();
    private Node fauxRoot;
    /** Creates a new instance of ComponentSelectionManager */
    public ComponentSelectionManager(InstanceUIContext context) {
        this.context = context;
        fauxRoot = new AbstractNode(new Children.Array());
    }
    
    //will be called for single selection
    /**
     * Call to set a Single selection (Sinlgle click).
     * @param component
     */
    public void setSelectedComponent(ABEBaseDropPanel component) {
        clearPreviousSelectedComponents(false);
        addToSelectedComponents(component);
    }
    
    /**
     * Call to add to the multiple selection list(ctrl+click).
     * @param component
     */
    public void addToSelectedComponents(ABEBaseDropPanel component) {
        if(getSelectedComponentList().contains(component)){
            //if its already selected, just unselect it.
            clearPreviousSelectedSingleComponent(component, true);
        }else{
            getSelectedComponentList().add(component);
            selectComponent(component);
        }
    }
    
    /**
     * See if the component is selected.
     * @param component
     * @return
     */
    public boolean isSelected(Component component){
        int index = getSelectedComponentList().indexOf(component);
        return index == -1 ? false : true;
    }
    
    private void selectComponent(final Component component){
        ArrayList<Node> nodes = new ArrayList<Node>();
        for(ABEBaseDropPanel comp: getSelectedComponentList()){
            comp.setSelected(true);
            if(comp.getNBNode() != null){
                nodes.add(comp.getNBNode());
            }
        }
        //set selected nodes
        if(nodes.size() > 0) {
            fauxRoot.getChildren().remove(fauxRoot.getChildren().getNodes(true));
            Node[] selectedNodes = nodes.toArray(new Node[nodes.size()]);
            fauxRoot.getChildren().add(selectedNodes);
            context.getTopComponent().setActivatedNodes(selectedNodes);
        }
        
        Component parent = null;
        if( (parent = context.getTopComponent().getParent()) != null )
            findParentTopComponentSpecial(parent).requestActive();
        component.requestFocusInWindow();
        component.repaint();
    }
    
    public void clearPreviousSelectedComponents(boolean clearNode){
        NBGlassPaneAccessSupport.forceDisposeNBGlassPane();
        if(clearNode) {
            context.getTopComponent().setActivatedNodes(new Node[0]);
            fauxRoot.getChildren().remove(fauxRoot.getChildren().getNodes(true));
        }
        List<ABEBaseDropPanel> clearedList = getSelectedComponentList();
        selectedComponentList = new ArrayList<ABEBaseDropPanel>();
        for(ABEBaseDropPanel comp: clearedList){
            comp.setSelected(false);
            comp.repaint();
        }
    }
    
    public void clearPreviousSelectedSingleComponent(ABEBaseDropPanel comp, boolean clearNode){
        if(clearNode) {
            if(comp.getNBNode() != null){
                Node node = comp.getNBNode();
                Node nodes[] = context.getTopComponent().getActivatedNodes();
                ArrayList<Node> nodeList = new ArrayList<Node>(Arrays.asList(nodes));
                nodeList.remove(node);
                nodes = nodeList.toArray(new Node[nodeList.size()]);
                context.getTopComponent().setActivatedNodes(nodes);
                fauxRoot.getChildren().remove(fauxRoot.getChildren().getNodes(true));
                fauxRoot.getChildren().add(nodes);
            }
        }
        getSelectedComponentList().remove(comp);
        comp.setSelected(false);
        comp.repaint();
    }
    
    public ArrayList<ABEBaseDropPanel> getSelectedComponentList() {
        return selectedComponentList;
    }
    
    public void refreshFocus() {
        for(ABEBaseDropPanel component: getSelectedComponentList()){
            component.requestFocus();
            component.requestFocusInWindow();
        }
    }
    
    private TopComponent findParentTopComponentSpecial(Component parent) {
        while (parent!=null) {
            if (parent instanceof TopComponent)
                return (TopComponent)parent;
            else
                parent=parent.getParent();
        }
        
        return null;
    }
}
