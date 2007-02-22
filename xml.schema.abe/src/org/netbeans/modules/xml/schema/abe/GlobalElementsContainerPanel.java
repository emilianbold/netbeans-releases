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
