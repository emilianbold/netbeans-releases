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
 * ComponentBeforeVisitor.java
 *
 * Created on August 30, 2006, 8:27 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe.visitors;

import java.util.List;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIContainer;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.AbstractElement;
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
public class BackTraversalVisitor extends TraversalVisitor{
    ABEBaseDropPanel currentComponent;
    ABEBaseDropPanel result;
    
    /** Creates a new instance of ComponentBeforeVisitor */
    public BackTraversalVisitor(ABEBaseDropPanel currentComponent) {
        this.currentComponent = currentComponent;
    }
    
    public ABEBaseDropPanel getResult(){
        return result;
    }

    public void visit(GlobalComplextypeContainerPanel panel) {
        if(currentComponent instanceof NamespacePanel ){
            //go to the last element in the panel
            result = getPrevComponent(panel, null);
        } else {
           
            result = getPrevComponent(panel, currentComponent);
        }
        
        if(result == null){
            currentComponent = panel;
            visit(panel.getContext().getInstanceDesignerPanel().getGlobalElementsPanel());
        }
    }

    public void visit(AttributePanel panel) {
        StartTagPanel stp = (StartTagPanel)panel.getParent();
        visit(stp);
    }

    public void visit(GlobalElementsContainerPanel panel) {
        if(currentComponent instanceof GlobalComplextypeContainerPanel){
            //point to the last element in the panel
            result = getPrevComponent(panel, null);
        } else {
            result = getPrevComponent(panel, currentComponent);
        }
        if(result == null){
        //means this is the first comp, so backward is actually the NS
        result = panel.getContext().getNamespacePanel();
        }
    }

    public void visit(StartTagPanel panel) {
        if(panel == null)
            return;
        if(currentComponent instanceof AttributePanel){
            //go the the Attr before the current attar
            List<AbstractAttribute> attrList = ((AXIContainer)panel.getAXIComponent()).getAttributes();
            int i = attrList.indexOf((AbstractAttribute)((AttributePanel)currentComponent).getAXIComponent());
            if( i-1 > 0) {
                //not the first attr, so get the prev
                result = panel.getChildUIComponentFor(attrList.get(i-1));
                return;
            } else {
                result = panel;
                if(result instanceof StartTagPanel){
                    ((StartTagPanel)result).hideAttributes();
                }
               return ;
            }
            }
       
        //else go up to the next peer of me
        ElementPanel ep = (ElementPanel)panel.getParent();
        //upward traversal visitors job
        TraversalVisitor tv = new UpTraversalVisitor(panel);
        ep.accept(tv);
        result = tv.getResult();
        
    }

    public void visit(NamespacePanel panel) {
       //back from NSP is actually the GCT
       GlobalComplextypeContainerPanel gecp = panel.getContext().getInstanceDesignerPanel().getGlobalComplextypePanel();
       visit(gecp);
    }

    public void visit(CompositorPanel panel) {
        if(currentComponent instanceof CompositorPanel){
            ABEBaseDropPanel elmp = (ABEBaseDropPanel)panel.getParentContainerPanel();
            TraversalVisitor tv = new UpTraversalVisitor(panel);
            elmp.accept(tv);
            result = tv.getResult();
            return;
        }
        result = getPrevComponent(panel, currentComponent);
        if(result == null){
            result=panel;
        }else {
           //we have a panel but we need to go the last attr in the last tag
           List<AbstractElement> child = result.getAXIComponent().getChildElements();
          if(child.size() > 0){
               ElementPanel ep = (ElementPanel)result.getParent();
               List list = ep.getAXIContainer().getChildren(TraversalVisitor.getEnCFilterList());
               if(list.size() > 0){
                    //return last element
                    result = ep.getChildUIComponentFor((AXIComponent) list.get(list.size() -1 ));
                    return;
              }
          } else
             return;
        }
    }

    private ABEBaseDropPanel getPrevComponent(ContainerPanel panel, ABEBaseDropPanel current) {
        if(current != null){
        //get the component before current
        AXIComponent comp = current.getAXIComponent();
        int i = panel.getAXIChildren().indexOf(comp);
        if( i-1 >= 0){
            //already at the first
            return panel.getChildUIComponentFor(panel.getAXIChildren().get(i-1));
        }else
            //already at the last
            return null;
      } else{
            //just return the last comp
            if(panel.getAXIChildren().size() > 0 ) {
                //return the last one
                return panel.getChildUIComponentFor(panel.getAXIChildren().get(panel.getAXIChildren().size() - 1));
            }
       }
      return null;
    }    
 
    
    
}
