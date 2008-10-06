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
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.swing.Action;
import javax.xml.namespace.QName;

import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.actions.CommonAddExtensibilityAttributeAction;
import org.netbeans.modules.xml.wsdl.ui.actions.RemoveAttributesAction;
import org.netbeans.modules.xml.wsdl.ui.api.property.MessageAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.model.StringAttribute;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.property.BaseAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.view.property.OperationInputOutputFaultPropertyAdapter;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.Named;
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
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;



/**
 *
 *@author skini
 *
 */
public abstract class OperationParameterNode<T extends OperationParameter> extends WSDLNamedElementNode<OperationParameter> implements ComponentListener {
    
    protected Image ICON;
    
    private OperationInputOutputFaultPropertyAdapter mPropertyAdapter;

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

    public OperationParameterNode(OperationParameter wsdlConstruct) {
        super(wsdlConstruct);
        this.mPropertyAdapter = new OperationInputOutputFaultPropertyAdapter(wsdlConstruct);
        super.setNamedPropertyAdapter(this.mPropertyAdapter);
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
        NamedComponentReference<Message> messageRef = getWSDLComponent().getMessage();
        if (messageRef != null) {
            return messageRef.get();
        }
        return null;
    }
    
    
    @Override
    protected Node.Property createAttributeProperty(QName attrQName) {
        Node.Property attrValueProperty = null;
        try {
            String attrName = attrQName.getLocalPart();
            //name
            if(attrName.equals(OperationParameter.NAME_PROPERTY)) { //NOT I18N
                //name
                attrValueProperty = createNameProperty();
                
            } else if(attrName.equals(OperationParameter.MESSAGE_PROPERTY)) {
                attrValueProperty = createMessageProperty();
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
        alwaysPresentAttrProperties.add(createMessageProperty());
        return alwaysPresentAttrProperties;
    }
    
    
    private Node.Property createNameProperty() throws NoSuchMethodException {
        Node.Property attrValueProperty;
        attrValueProperty = new BaseAttributeProperty(mPropertyAdapter, String.class, OperationParameter.NAME_PROPERTY);
        
        
        attrValueProperty.setName(OperationParameter.NAME_PROPERTY);
        attrValueProperty.setDisplayName(NbBundle.getMessage(OperationParameterNode.class, "PROP_NAME_DISPLAYNAME"));
        attrValueProperty.setShortDescription(NbBundle.getMessage(OperationParameterNode.class, "OPERATION_PARAMETER_NAME_DESC"));
        
        return attrValueProperty;
    }
    
    private Node.Property createMessageProperty() throws NoSuchMethodException {
        Node.Property attrValueProperty;
        attrValueProperty = new MessageAttributeProperty(mPropertyAdapter, getWSDLComponent(), String.class, OperationParameter.MESSAGE_PROPERTY); 
        
        attrValueProperty.setName(OperationParameter.MESSAGE_PROPERTY);
        attrValueProperty.setDisplayName(NbBundle.getMessage(OperationParameterNode.class, "PROP_MESSAGE_DISPLAYNAME"));
        attrValueProperty.setShortDescription(NbBundle.getMessage(OperationParameterNode.class, "OPERATIONPARAMETER_MESSAGE_DESC"));
        
        return attrValueProperty;
    }
    
    
    @Override
    public String getHtmlDisplayName() {
        String htmlDisplayName = super.getHtmlDisplayName();
        NamedComponentReference<Message> message = getWSDLComponent().getMessage();
        
        String decoration = null;
        if (message != null && message.get() != null) {
            String tns = message.get().getModel().getDefinitions().getTargetNamespace();
            decoration = NbBundle.getMessage(OperationParameterNode.class, "LBL_Message", 
                    Utility.getNameAndDropPrefixIfInCurrentModel(tns, message.get().getName(), getWSDLComponent().getModel()));
        }
        
        if (decoration == null) {
            //decoration = NbBundle.getMessage(OperationParameterNode.class, "LBL_MessageNotSet");
            return htmlDisplayName;
        }
        return htmlDisplayName + " <font color='#999999'>"+decoration+"</font>";
    }

    @Override
    protected void createPasteTypes(Transferable transferable,
            List<PasteType> list) {
        super.createPasteTypes(transferable, list);
        Node[] nodes = Utility.getNodes(transferable);
        if (nodes.length == 1) {
            final Node node = nodes[0];
            if (node instanceof MessageNode) {
                list.add(new PasteType() {

                    @Override
                    public Transferable paste() throws IOException {
                        Message comp = node.getLookup().lookup(Message.class);
                        if (comp != null) {
                            WSDLModel model = getWSDLComponent().getModel();
                            try {
                                model.startTransaction();
                                getWSDLComponent().setMessage(getWSDLComponent().createReferenceTo(comp, Message.class));
                            } finally {
                                if (model.isIntransaction()) {
                                    model.endTransaction();
                                 }
                            }
                            ActionHelper.selectNode(getWSDLComponent());
                        }
                        return null;
                    }

                });
            }
        }
    }
    
    @Override
    public PasteType getDropType(Transferable transferable, int action,
            int index) {
        Node[] nodes = Utility.getNodes(transferable);
        if (nodes.length == 1) {
            final Node node = nodes[0];
            if (node instanceof MessageNode) {
                return new PasteType() {

                    @Override
                    public Transferable paste() throws IOException {
                        Message comp = node.getLookup().lookup(Message.class);
                        if (comp != null) {
                            WSDLModel model = getWSDLComponent().getModel();
                            try {
                                model.startTransaction();
                                getWSDLComponent().setMessage(getWSDLComponent().createReferenceTo(comp, Message.class));
                            } finally {
                                if (model.isIntransaction()) {
                                    model.endTransaction();
                                 }
                            }
                            ActionHelper.selectNode(getWSDLComponent());
                        }
                        return null;
                    }

                };
            }
        }   
        return super.getDropType(transferable, action, index);
    }
}
