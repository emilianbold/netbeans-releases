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
 * ElementsContainerPanel.java
 *
 * Created on June 26, 2006, 3:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIContainer;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.axi.AnyElement;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.schema.abe.palette.DnDHelper;
import org.openide.util.NbBundle;

/**
 *
 * @author girix
 */
public abstract class ElementsContainerPanel extends ContainerPanel{
    private static final long serialVersionUID = 7526472295622776147L;
    /** Creates a new instance of ElementsContainerPanel */
    public ElementsContainerPanel(InstanceUIContext context, AXIComponent axiComponent,
            Component parentPanel, boolean openByDefault) {
        super(context, axiComponent, parentPanel, openByDefault);
    }
    
    
    protected void setupAXIComponentListener() {
        getAXIParent().addPropertyChangeListener(new ModelEventMediator(this, getAXIParent()) {
            public void _propertyChange(PropertyChangeEvent evt) {
                String property = evt.getPropertyName();
                if(property.equals(Element.PROP_ELEMENT) || property.equals(Element.PROP_ELEMENT_REF)) {
                    if(context.isUserInducedEventMode())
                        setVisible(true);
                    //event is for child element
                    if((evt.getOldValue() == null) && (evt.getNewValue() != null)){
                        //new element added
                        addElement((AbstractElement)evt.getNewValue());
                    }else if((evt.getNewValue() == null) && (evt.getOldValue() != null)){
                        //old element removed
                        removeElement((AbstractElement)evt.getOldValue());
                    }
                }
            }
        });
    }
    
    public void visit(Element element) {
        super.visit(element);
        visitorResult = new ElementPanel(context, element,
                ElementsContainerPanel.this);
    }
    
    public void visit(AnyElement element){
        super.visit(element);
        visitorResult = new ElementPanel(context, element,
                ElementsContainerPanel.this);
    }
    
    public List<? extends AXIComponent> getAXIChildren() {
        return getAXIParent().getChildElements();
    }
    
    
    
    public void addElement(AXIContainer element){
        //look in to the children list find out where the child was added
        //create a new ElementPanel and add @ that index. Adjust the layout accordingly
        int index = getAXIChildren().indexOf(element);
        ABEBaseDropPanel temp = null;
        if( (temp = isAlreadyAdded(element)) != null){
            //if already added the just ignore
            showNameEditorIfNeeded(temp);
            return;
        }
        
        visitorResult = null;
        element.accept(this);
        final ElementPanel ep = (ElementPanel) visitorResult;
        if(ep == null)
            return;
        
        if(!addChildAt(ep, index)){
            //then some problem happened which caused a redraw. So, ignore rest of the steps.
            return;
        }
        showNameEditorIfNeeded(ep);
    }
    
    
    public void showNameEditorIfNeeded(ABEBaseDropPanel comp){
        final ElementPanel ep;
        if(comp instanceof ElementPanel)
            ep = (ElementPanel) comp;
        else
            return;
        if(context.isUserInducedEventMode() &&
                ( ((getParentContainerPanel() instanceof ElementPanel) &&
                (((ElementPanel)getParentContainerPanel()).getStartTagPanel()
                == context.getUserActedComponent()) )
                || (  (getParentContainerPanel() == null) && (context.getUserActedComponent() == context.getNamespacePanel() )
                ) ) ){
            
            //show the tag name editor after
            SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                    //skipping this threadtime is needed to let the UI refresh before the
                    //tag editor is shown. If I do it in the same threadtime the editor
                    //appears in a wrong position.
                    SwingUtilities.invokeLater(new Runnable(){
                        public void run() {
                            UIUtilities.scrollViewTo(ep.getStartTagPanel(), context);
                            context.getComponentSelectionManager().setSelectedComponent(ep.getStartTagPanel());
                            ep.showNameEditor(true);
                            context.setUserInducedEventMode(false);
                            context.resetUserActedComponent();
                        }
                    });
                }
            });
        }
    }
    
    public void removeElement(AXIContainer element){
        //look in to the children list find out where the child was present
        //remove the ElementPanel @ that index. Adjust the layout accordingly
        Component rmComp = null;
        for(Component component: getChildrenList()){
            if(component instanceof ElementPanel){
                if( ((ElementPanel)component).getAXIContainer() == element){
                    //dont call removeComponent() from here as it affects getChildrenList()
                    rmComp = component;
                    break;
                }
            }
        }
        if(rmComp != null){
            removeComponent(rmComp);
        }
        
    }
    
    private void addNewElementAt(TweenerPanel tweener){
        int index = getChildrenList().indexOf(tweener);
        if(index == -1){
            //must not happen
            return;
        }
        index = index/2;
        AXIModel model = getAXIParent().getModel();
        model.startTransaction();
        try{
            Element elm = model.getComponentFactory().createElement();
            String str = UIUtilities.getUniqueName(
                    InstanceDesignConstants.NEW_ELEMENT_NAME, getAXIParent());
            elm.setName(str);
            getAXIParent().addChildAtIndex(elm, index);
        }finally{
            model.endTransaction();
        }
    }
    
    public void tweenerDrop(TweenerPanel tweener, DnDHelper.PaletteItem paletteItem) {
        super.tweenerDrop(tweener, paletteItem);
        if(paletteItem == paletteItem.ELEMENT){
            ABEBaseDropPanel comp = null;
            if(getParentContainerPanel() instanceof ElementPanel)
                comp = ((ElementPanel)getParentContainerPanel()).getStartTagPanel();
            else if(getParentContainerPanel() == null)
                comp = context.getNamespacePanel();
            context.setUserInducedEventMode(true, comp);
            addNewElementAt(tweener);
        }
    }
    
    public void tweenerDragEntered(TweenerPanel tweener, DnDHelper.PaletteItem paletteItem) {
        super.tweenerDragEntered(tweener, paletteItem);
        if(tweenerDragAccept(tweener, paletteItem)){
            String locDropMsgAccept = NbBundle.getMessage(GlobalComplextypeContainerPanel.class,
                    "MSG_GEP_DROP_ACCEPT");
            tweener.setDropInfoText(locDropMsgAccept);
        }
    }
    
    public boolean tweenerDragAccept(TweenerPanel tweener, DnDHelper.PaletteItem paletteItem) {
        if(paletteItem != paletteItem.ELEMENT){
            String locDropMsgAccept = NbBundle.getMessage(GlobalComplextypeContainerPanel.class,
                    "MSG_GEP_DROP_REJECT");
            UIUtilities.showErrorMessageFor(locDropMsgAccept, context, tweener);
            return false;
        }
        return true;
    }
    
    public void tweenerDragExited(TweenerPanel tweener) {
        super.tweenerDragExited(tweener);
        UIUtilities.hideGlassMessage();
    }
    
}
