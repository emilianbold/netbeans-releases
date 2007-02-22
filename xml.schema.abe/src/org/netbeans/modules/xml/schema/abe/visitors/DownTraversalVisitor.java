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
 * LastComponentVisitor.java
 *
 * Created on August 30, 2006, 8:27 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe.visitors;

import java.util.List;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.schema.abe.ABEBaseDropPanel;
import org.netbeans.modules.xml.schema.abe.AttributePanel;
import org.netbeans.modules.xml.schema.abe.CompositorPanel;
import org.netbeans.modules.xml.schema.abe.ContainerPanel;
import org.netbeans.modules.xml.schema.abe.ElementPanel;
import org.netbeans.modules.xml.schema.abe.GlobalComplextypeContainerPanel;
import org.netbeans.modules.xml.schema.abe.GlobalElementsContainerPanel;
import org.netbeans.modules.xml.schema.abe.NamespacePanel;
import org.netbeans.modules.xml.schema.abe.StartTagPanel;

/**
 *
 * @author girix
 */
public class DownTraversalVisitor extends TraversalVisitor{
    ABEBaseDropPanel currentComponent;
    ABEBaseDropPanel result;
    
    /** Creates a new instance of LastComponentVisitor */
    public DownTraversalVisitor(ABEBaseDropPanel currentComponent) {
        this.currentComponent = currentComponent;
    }
    
    public ABEBaseDropPanel getResult(){
        return result;
    }
    
    public void visit(GlobalComplextypeContainerPanel panel) {
        super.visit(panel);
    }
    
    public void visit(AttributePanel panel) {
        super.visit(panel);
    }
    
    public void visit(GlobalElementsContainerPanel panel) {
        super.visit(panel);
    }
    
    public void visit(StartTagPanel panel) {
        super.visit(panel);
    }
    
    
    public void visit(NamespacePanel panel) {
        super.visit(panel);
    }
    
    public void visit(CompositorPanel panel) {
        ABEBaseDropPanel abdp = (ABEBaseDropPanel)panel.getParent();
        TraversalVisitor tv = new DownTraversalVisitor(panel);
        abdp.accept(tv);
        result = tv.getResult();
    }
    
    
    public void visit(ElementPanel elementPanel) {
        elementPanel.expandChild();
        if(currentComponent instanceof ContainerPanel){
            AXIComponent comp = currentComponent.getAXIComponent();
            List list = elementPanel.getAXIContainer().getChildren(TraversalVisitor.getEnCFilterList());
            int i = list.indexOf(comp);
            if( (i >= 0) && (i+1 < list.size()) ){
                //next comp
                result = elementPanel.getChildUIComponentFor((AXIComponent) list.get(i+1));
                return;
            }
        }else{
            //put focus on first compositor
            AXIComponent comp = currentComponent.getAXIComponent();
            List list = elementPanel.getAXIContainer().getChildren(TraversalVisitor.getEnCFilterList());
            if(list.size() > 0){
                //return first compositor
                result = elementPanel.getChildUIComponentFor((AXIComponent) list.get(0));
                return;
            }
        }
        //has to move to next comp. So, call parents forward
        TraversalVisitor tv = new FrontTraversalVisitor(elementPanel);
        elementPanel.getParentContainerPanel().accept(tv);
        result = tv.getResult();
    }
    
    
}
