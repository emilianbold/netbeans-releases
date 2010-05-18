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
 * GlobalElementsContainerPanel.java
 *
 * Created on June 6, 2006, 4:12 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe;

import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.schema.abe.nodes.ABEAbstractNode;

/**
 *
 * @author girix
 */
public class GlobalElementsContainerPanel extends ElementsContainerPanel{
    private static final long serialVersionUID = 7526472295622776147L;
    /** Creates a new instance of GlobalElementsContainerPanel */
    public GlobalElementsContainerPanel(InstanceUIContext context,
            AXIComponent axiComponent, boolean openByDefault) {
        super(context, axiComponent, null, openByDefault);
        //dont draw annotation
        setDrawAnnotation(false);
        initMouseListener();
    }
    
    
    public int getChildrenIndent(){
        return InstanceDesignConstants.GLOBAL_ELEMENT_PANEL_INDENT;
    }
    
    public List<? extends AXIComponent> getAXIChildren() {
        ArrayList<AbstractElement> list = new ArrayList<AbstractElement>
                (getAXIParent().getChildElements());
        
        return getAXIChildrenSorted(list);
    }
    
    protected List<? extends AXIComponent> getAXIChildrenSorted(List<AbstractElement> elementList) {
        /*Collections.sort(elementList,
                new Comparator<AbstractElement>() {
            public int compare(AbstractElement e1, AbstractElement e2) {
                return e1.getName().compareTo(e2.getName());
            }
         
        });*/
        return elementList;
    }
    
    public ABEBaseDropPanel getUIComponentFor(AXIComponent axiComponent) {
        ABEBaseDropPanel retValue;
        retValue = super.getUIComponentFor(axiComponent);
        return retValue;
    }
    
    public void accept(UIVisitor visitor) {
        visitor.visit(this);
    }
    
    public ABEAbstractNode getNBNode() {
        //just return the namespace panel node
        return context.getNamespacePanel().getNBNode();
    }
    
    
    
    protected void initMouseListener(){
        addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                mouseClickedActionHandler(e);
            }
            public void mouseClicked(MouseEvent e){
                mouseClickedActionHandler(e);
            }
            
            public void mousePressed(MouseEvent e) {
                mouseClickedActionHandler(e);
            }
        });
    }
    
    
    protected void mouseClickedActionHandler(MouseEvent e){
        if(e.getClickCount() == 1){
            if(e.isPopupTrigger()){
                context.getMultiComponentActionManager().showPopupMenu(e, this);
                return;
            }
        }
        //the tag is selected
        context.getComponentSelectionManager().setSelectedComponent(this);
    }
    
    public void drop(DropTargetDropEvent event) {
        context.getNamespacePanel().drop(event);
    }
    
    public void dragExit(DropTargetEvent event) {
        context.getNamespacePanel().dragExit(event);
    }
    
    public void dragOver(DropTargetDragEvent event) {
        context.getNamespacePanel().dragOver(event);
    }
    
    public void dragEnter(DropTargetDragEvent event) {
        context.getNamespacePanel().dragEnter(event);
    }
    
    
}
