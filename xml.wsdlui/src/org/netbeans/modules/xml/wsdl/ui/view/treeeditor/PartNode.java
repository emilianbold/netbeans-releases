/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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


package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.swing.Action;
import javax.xml.namespace.QName;

import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.actions.CommonAddExtensibilityAttributeAction;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.netbeans.modules.xml.wsdl.ui.actions.RemoveAttributesAction;
import org.netbeans.modules.xml.wsdl.ui.api.property.ElementOrType;
import org.netbeans.modules.xml.wsdl.ui.api.property.ElementOrTypeAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.api.property.ElementOrTypeProvider;
import org.netbeans.modules.xml.wsdl.ui.commands.ConstraintNamedPropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.property.BaseAttributeProperty;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.ui.actions.GoToAction;
import org.openide.ErrorManager;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.NewAction;
import org.openide.actions.PasteAction;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;




/**
 *
 * @author Ritesh Adval
 *
 *
 */
public class PartNode extends WSDLNamedElementNode<Part> {
    
    private Image ICON  = ImageUtilities.loadImage
            ("org/netbeans/modules/xml/wsdl/ui/view/resources/part.png");
    
    private Part mWSDLConstruct;
    
    private PartPropertyAdapter mPropertyAdapter;
    
    private ElementOrTypeAttributeProperty mElementOrTypeProperty;

    private static final SystemAction[] ACTIONS = new SystemAction[]{
        SystemAction.get(CutAction.class),
        SystemAction.get(CopyAction.class),
        SystemAction.get(PasteAction.class),
        null,
        SystemAction.get(NewAction.class),
        SystemAction.get(DeleteAction.class),
        null,
        SystemAction.get(CommonAddExtensibilityAttributeAction.class),
        SystemAction.get(RemoveAttributesAction.class),
        null,
        SystemAction.get(GoToAction.class),
        //SystemAction.get(FindUsagesAction.class),
        (SystemAction)RefactoringActionsFactory.whereUsedAction(),
        null,
        (SystemAction)RefactoringActionsFactory.editorSubmenuAction(),
        null,
        SystemAction.get(PropertiesAction.class)
    };

    public PartNode(Part wsdlConstruct) {
        super(wsdlConstruct);
        mWSDLConstruct = wsdlConstruct;
        
        this.mPropertyAdapter = new PartPropertyAdapter();
        super.setNamedPropertyAdapter(this.mPropertyAdapter);
        ElementOrTypeProvider provider = new PartElementOrTypeProvider(wsdlConstruct, mPropertyAdapter);
        try {
            mElementOrTypeProperty = new ElementOrTypeAttributeProperty(provider);
            mElementOrTypeProperty.setName(Part.ELEMENT_PROPERTY + Part.TYPE_PROPERTY);
            mElementOrTypeProperty.setDisplayName(NbBundle.getMessage(PartNode.class, "PART_ELEMENT_OR_TYPE"));
            mElementOrTypeProperty.setShortDescription(NbBundle.getMessage(PartNode.class, "PART_ELEMENT_OR_TYPE_SD"));
        } catch(Exception ex) {
            mLogger.log(Level.SEVERE, "failed to create property sheet for "+ getWSDLComponent(), ex);
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    
    
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getSource() == getWSDLComponent() && isValid()) {
            String propertyName = event.getPropertyName();
            if (propertyName.equals(Part.ELEMENT_PROPERTY) || propertyName.equals(Part.TYPE_PROPERTY)) {
                propertyName = Part.ELEMENT_PROPERTY + Part.TYPE_PROPERTY;
                updateDisplayName();
                firePropertyChange(propertyName, event.getOldValue(),
                        event.getNewValue());
            } else {
                super.propertyChange(event);
            }
        }
    }
    
    @Override
    public Image getIcon(int type) {
        return ICON;
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return ICON;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return ACTIONS;
    }
    
    @Override
    protected Component getSuperDefinition() {
        NamedComponentReference<GlobalElement> ge = getWSDLComponent().getElement();
        Component component;
        if (ge != null) {
            if ((component = ge.get()) != null) {
                return component;
            }
        } else {
            NamedComponentReference<GlobalType> gt = getWSDLComponent().getType();
            if (gt != null) {
                if ((component = gt.get()) != null) {
                    return component;
                }
            }
        }
        
        return null;
    }
    
    @Override
    protected Node.Property createAttributeProperty(QName attrQName) {
        Node.Property attrValueProperty = null;
        try {
            String attrName = attrQName.getLocalPart();
            //name
            if(attrName.equals(Part.NAME_PROPERTY)) { //NOT I18N
                //name
                attrValueProperty = createNameProperty();
            } else if(attrName.equals(Part.ELEMENT_PROPERTY) || 
                        attrName.equals(Part.TYPE_PROPERTY)) {
                attrValueProperty = createElementOrTypeProperty();
            } else {
                attrValueProperty = super.createAttributeProperty(attrQName);
            }
            
        } catch(Exception ex) {
            mLogger.log(Level.SEVERE, "failed to create property sheet for "+ getWSDLComponent(), ex);
            ErrorManager.getDefault().notify(ex);
        }
        return attrValueProperty;
    }
    
    
    @Override
    protected List<Node.Property> createAlwaysPresentAttributeProperty() throws Exception {
        ArrayList<Node.Property> alwaysPresentAttrProperties = new ArrayList<Node.Property>();
        alwaysPresentAttrProperties.add(createNameProperty());
        alwaysPresentAttrProperties.add(createElementOrTypeProperty());
        return alwaysPresentAttrProperties;
    }
    
    
    private Node.Property createNameProperty() throws NoSuchMethodException {
        Node.Property attrValueProperty;
        attrValueProperty = new BaseAttributeProperty(mPropertyAdapter,
                String.class, Part.NAME_PROPERTY);
        
        
        attrValueProperty.setName(Part.NAME_PROPERTY);
        attrValueProperty.setDisplayName(NbBundle.getMessage(PartNode.class, "PROP_NAME_DISPLAYNAME"));
        attrValueProperty.setShortDescription(NbBundle.getMessage(PartNode.class, "PART_NAME_DESC"));
        
        return attrValueProperty;
    }
    
    private Node.Property createElementOrTypeProperty() {
        return mElementOrTypeProperty;
    }
    
    
    
    public class PartPropertyAdapter extends ConstraintNamedPropertyAdapter {
        
        PartPropertyAdapter() {
            super(mWSDLConstruct);
        }
        
        @Override
        public boolean isNameExists(String name) {
            return NameGenerator.getInstance().isMessagePartExists(name,
                    (Message) getWSDLComponent().getParent());
        }
        
        public ElementOrType getElementOrType() {
            //since we always create ElementOrType
            //so ElementOrType overrides equal and hashcode
            //so that two ElementOrType can be compared
            ElementOrType elementOrType = null;
            if (getWSDLComponent().isInDocumentModel()) {
                NamedComponentReference element = getWSDLComponent().getElement();
                if(element == null) {
                    NamedComponentReference partType = getWSDLComponent().getType();
                    if(partType != null) {
                        if (partType.get() != null) {
                            elementOrType = new ElementOrType((GlobalType) partType.get(), getWSDLComponent().getModel());
                        } else {
                            elementOrType = new ElementOrType(partType.getQName(), mWSDLConstruct.getModel(), false);
                        }
                    } else {
                        elementOrType = new ElementOrType(new QName(""), mWSDLConstruct.getModel(), false);
                    }
                } else {
                    if (element.get() != null)
                        elementOrType = new ElementOrType((GlobalElement) element.get(), getWSDLComponent().getModel());
                    else
                        elementOrType = new ElementOrType(element.getQName(), mWSDLConstruct.getModel(), true);
                }
                
            }
            return elementOrType;
        }
        
        public void setElementOrType(ElementOrType newValue) {
            if (newValue == null) {
                return;
            }
            
            ElementOrType oldValue = getElementOrType();
            
            if (oldValue != null && oldValue.toString().equals(newValue.toString())) return;
            
            if (newValue.getType() != null || newValue.getElement() != null) { 
                WSDLModel model = getWSDLComponent().getModel();
                model.startTransaction();
                if (newValue.isElement()) {
                    GlobalElement element = newValue.getElement();
                    Utility.addSchemaImport(element, model);
                    Utility.addNamespacePrefix(element, getWSDLComponent().getModel().getDefinitions(), null);
                    getWSDLComponent().setElement(getWSDLComponent().createSchemaReference(newValue.getElement(), GlobalElement.class));
                    getWSDLComponent().setType(null);
                } else {
                    getWSDLComponent().setElement(null);
                    Utility.addSchemaImport(newValue.getType(), model);
                    Utility.addNamespacePrefix(newValue.getType().getModel().getSchema(), getWSDLComponent().getModel(), null);
                    getWSDLComponent().setType(getWSDLComponent().createSchemaReference(newValue.getType(), GlobalType.class));
                }
                model.endTransaction();
                ActionHelper.selectNode(getWSDLComponent());
            }
        }
        
        
    }
    
    public class PartElementOrTypeProvider implements ElementOrTypeProvider {
        private Part mPart;
        private PartPropertyAdapter mPartPropertyAdapter;
        
        PartElementOrTypeProvider(Part part, PartPropertyAdapter adapter) {
            mPart = part;
            mPartPropertyAdapter = adapter;
        }
        
        public WSDLModel getModel() {
            return mPart.getModel();
        }
    
        public ElementOrType getElementOrType() {
            return mPartPropertyAdapter.getElementOrType();
        }
    
        public void setElementOrType(ElementOrType o) {
            mPartPropertyAdapter.setElementOrType(o);
        }
    
    }

    @Override
    public String getTypeDisplayName() {
        return NbBundle.getMessage(PartNode.class, "LBL_PartNode_TypeDisplayName");
    }


    @Override
    public String getHtmlDisplayName() {
        String htmlDisplayName = super.getHtmlDisplayName();
        NamedComponentReference<GlobalType> type = mWSDLConstruct.getType();
        NamedComponentReference<GlobalElement> element = mWSDLConstruct.getElement();
        String decoration = null;
        if (type != null && type.get() != null) {
            String tns = Utility.getTargetNamespace(type.get().getModel());
/*            decoration = NbBundle.getMessage(PartNode.class, "LBL_Typeof", 
                    Utility.getNameAndDropPrefixIfInCurrentModel(tns, type.get().getName(), mWSDLConstruct.getModel()));*/
            decoration = Utility.getNameAndDropPrefixIfInCurrentModel(tns, type.get().getName(), mWSDLConstruct.getModel());
        } else if (element != null && element.get() != null) {
            String tns = Utility.getTargetNamespace(element.get().getModel());
            /*decoration = NbBundle.getMessage(PartNode.class, "LBL_Typeof", 
                    Utility.getNameAndDropPrefixIfInCurrentModel(tns, element.get().getName(), mWSDLConstruct.getModel()));*/
            decoration = Utility.getNameAndDropPrefixIfInCurrentModel(tns, element.get().getName(), mWSDLConstruct.getModel());
        }
        if (decoration == null) {
            //decoration = NbBundle.getMessage(PartNode.class, "LBL_TypeOrElementNotSet");
            return htmlDisplayName;
        }
        return htmlDisplayName + " <font color='#999999'>"+decoration+"</font>";
    }
    
    
    

    
    
}
