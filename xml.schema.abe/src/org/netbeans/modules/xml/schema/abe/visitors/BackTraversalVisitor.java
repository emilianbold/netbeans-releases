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
