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
package org.netbeans.modules.xml.schema.abe;

import java.awt.Component;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIContainer;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.schema.abe.nodes.ABEAbstractNode;
import org.netbeans.modules.xml.schema.abe.palette.DnDHelper;
import org.openide.util.NbBundle;

/**
 *
 * @author girix
 */
public class GlobalComplextypeContainerPanel extends ElementsContainerPanel{
    
    private static final long serialVersionUID = 7526472295622776147L;
    /** Creates a new instance of GlobalElementsContainerPanel */
    public GlobalComplextypeContainerPanel(InstanceUIContext context,
            AXIComponent axiComponent, boolean openByDefault) {
        super(context, axiComponent, (Component) null, openByDefault);
        //dont draw annotation
        setDrawAnnotation(false);
        initMouseListener();
    }
    
    
    protected void setupAXIComponentListener() {
        getAXIParent().addPropertyChangeListener(new ModelEventMediator(this, getAXIParent()) {
            public void _propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(ContentModel.PROP_CONTENT_MODEL)){
                    if(context.isUserInducedEventMode()){
                        addAllChildren();
                        setVisible(true);
                    }
                    //event is for child element
                    if((evt.getOldValue() == null) && (evt.getNewValue() != null)){
                        //new element added
                        addElement((AXIContainer)evt.getNewValue());
                    }else if((evt.getNewValue() == null) && (evt.getOldValue() != null)){
                        //old element removed
                        removeElement((AXIContainer)evt.getOldValue());
                    }

                }
            }
        });
    }
    
    public int getChildrenIndent(){
        return InstanceDesignConstants.GLOBAL_ELEMENT_PANEL_INDENT;
    }
    
    public List<? extends AXIComponent> getAXIChildren() {
        ArrayList<ContentModel> list = new ArrayList<ContentModel> (((AXIDocument)
        getAXIParent()).getContentModels());
        ArrayList<ContentModel> cloneList = new ArrayList<ContentModel>(list);
        for(ContentModel child: list){
            if(child.getType() != ContentModel.ContentModelType.COMPLEX_TYPE)
                cloneList.remove(child);
        }
        return getAXIChildrenSorted(cloneList);
    }
    
    
    protected List<? extends AXIComponent> getAXIChildrenSorted(List<ContentModel> elementList) {
        /*Collections.sort(elementList,
                new Comparator<ContentModel>() {
            public int compare(ContentModel e1, ContentModel e2) {
                return e1.getName().compareTo(e2.getName());
            }
         
        });*/
        return elementList;
    }
    
    public void visit(Element element) {
        //ignore
        //super.visit(element);
    }
    
    public void visit(ContentModel contentModel) {
        super.visit(contentModel);
        //care only abt ContentModel
        if(contentModel.getType() == contentModel.getType().COMPLEX_TYPE)
            visitorResult = new ContentModelPanel(context, contentModel, this);
    }
    
    
    public void tweenerDrop(TweenerPanel tweener, DnDHelper.PaletteItem paletteItem) {
        super.tweenerDrop(tweener, paletteItem);
        if(paletteItem == paletteItem.COMPLEXTYPE){
            ABEBaseDropPanel comp = null;
            comp = context.getNamespacePanel();
            context.setUserInducedEventMode(true, comp);
            addNewComplextypeAt(tweener);
        }
    }
    
    public void tweenerDragEntered(TweenerPanel tweener, DnDHelper.PaletteItem paletteItem) {
        super.tweenerDragEntered(tweener, paletteItem);
        if(tweenerDragAccept(tweener, paletteItem)){
            String locDropMsgAccept = NbBundle.getMessage(GlobalComplextypeContainerPanel.class,
                    "MSG_GCTP_DROP_ACCEPT");
            tweener.setDropInfoText(locDropMsgAccept);
        }
    }
    
    public boolean tweenerDragAccept(TweenerPanel tweener, DnDHelper.PaletteItem paletteItem) {
        if(paletteItem != paletteItem.COMPLEXTYPE){
            String locDropMsgAccept = NbBundle.getMessage(GlobalComplextypeContainerPanel.class,
                    "MSG_GCTP_DROP_REJECT");
            UIUtilities.showErrorMessageFor(locDropMsgAccept, context, tweener);
            return false;
        }
        return true;
    }
    
    public void tweenerDragExited(TweenerPanel tweener) {
        super.tweenerDragExited(tweener);
        UIUtilities.hideGlassMessage();
    }
    
    private void addNewComplextypeAt(TweenerPanel tweener) {
        int index = getChildrenList().indexOf(tweener);
        if(index == -1){
            //must not happen
            return;
        }
        //index = index/2;
        AXIModel model = getAXIParent().getModel();
        model.startTransaction();
        try{
            ContentModel cm = model.getComponentFactory().createComplexType();
            String str = UIUtilities.getUniqueName(
                    InstanceDesignConstants.NEW_COMPLEXTYPE_NAME, getAXIParent());
            cm.setName(str);
            ((AXIDocument)getAXIParent()).addContentModel(cm);
        }finally{
            model.endTransaction();
        }
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
