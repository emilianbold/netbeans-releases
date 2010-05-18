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
