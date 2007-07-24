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
 * ComponentAfterVisitor.java
 *
 * Created on August 30, 2006, 8:27 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe.visitors;

import java.util.List;
import org.netbeans.modules.xml.schema.abe.ABEBaseDropPanel;
import org.netbeans.modules.xml.schema.abe.AttributePanel;
import org.netbeans.modules.xml.schema.abe.CompositorPanel;
import org.netbeans.modules.xml.schema.abe.ElementPanel;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.schema.abe.ContainerPanel;
import org.netbeans.modules.xml.schema.abe.GlobalComplextypeContainerPanel;
import org.netbeans.modules.xml.schema.abe.GlobalElementsContainerPanel;
import org.netbeans.modules.xml.schema.abe.NamespacePanel;
import org.netbeans.modules.xml.schema.abe.StartTagPanel;
import org.netbeans.modules.xml.schema.abe.TagPanel;

/**
 *
 * @author girix
 */
public class UpTraversalVisitor extends TraversalVisitor{
    ABEBaseDropPanel currentComponent;
    ABEBaseDropPanel result;
    
    /** Creates a new instance of ComponentAfterVisitor */
    public UpTraversalVisitor(ABEBaseDropPanel currentComponent) {
        this.currentComponent = currentComponent;
    }
    
    public ABEBaseDropPanel getResult(){
        return result;
    }
    
    public void visit(GlobalComplextypeContainerPanel panel) {
        super.visit(panel);
    }
    
    public void visit(AttributePanel panel) {
        visit((StartTagPanel)panel.getParent());
    }
    
    public void visit(GlobalElementsContainerPanel panel) {
        super.visit(panel);
    }
    
    public void visit(StartTagPanel panel) {
        List<AttributePanel> list = panel.getAttributePanels();
        if(currentComponent instanceof AttributePanel){
            //already an attr selected so select next
            int i = list.indexOf(currentComponent);
            if( (i+1) < list.size()){
                result = list.get(i+1);
                return;
            }
        }else if(currentComponent instanceof StartTagPanel){
            //select the first attr
            if(list.size() > 0){
                result = list.get(0);
                return;
            }
        }
        result = panel;
    }
    
    public void visit(NamespacePanel panel) {
        super.visit(panel);
    }
    
    public void visit(CompositorPanel panel) {
        super.visit(panel);
    }

    public void visit(ElementPanel elementPanel) {
        elementPanel.collapseChild();
        if(currentComponent instanceof ContainerPanel){
            AXIComponent comp = currentComponent.getAXIComponent();
            List list = elementPanel.getAXIContainer().getChildren(TraversalVisitor.getEnCFilterList());
            int i = list.indexOf(comp);
            if( i > 0) {
                //next comp
                result = elementPanel.getChildUIComponentFor((AXIComponent) list.get(i-1));
                return;
            } else {
                StartTagPanel startPanel = (StartTagPanel)elementPanel.getStartTagPanel();
                List<AttributePanel> attrPanels = startPanel.getAttributePanels();
                if(attrPanels.size() > 0) {
                    result = attrPanels.get(attrPanels.size() -1 );
                    return;
                } else {
                    result = elementPanel.getStartTagPanel();
                    return;
                }  
            }
        }
        //has to move  to prev comp. So, call parents forward
        TraversalVisitor tv = new BackTraversalVisitor(elementPanel);
        elementPanel.getParentContainerPanel().accept(tv);
        result = tv.getResult();
    }
    
    
}
