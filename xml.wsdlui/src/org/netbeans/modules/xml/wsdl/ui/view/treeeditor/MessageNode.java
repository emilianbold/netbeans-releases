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


package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.netbeans.modules.xml.wsdl.ui.commands.ConstraintNamedPropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElements;
import org.netbeans.modules.xml.wsdl.ui.view.property.BaseAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.DocumentationNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.ExtensibilityElementNewTypesFactory;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.MessagePartNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.NewTypesFactory;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.NewType;



/**
 *
 * @author Ritesh Adval
 *
 */
public class MessageNode extends WSDLExtensibilityElementNode<Message> {
    
    Image ICON  = Utilities.loadImage
    ("org/netbeans/modules/xml/wsdl/ui/view/resources/message.png");
    
    
    private MessagePropertyAdapter mPropertyAdapter;
    
    public MessageNode(Message wsdlConstruct) {
        super(new GenericWSDLComponentChildren<Message>(wsdlConstruct), wsdlConstruct, new MessageNewTypesFactory());
        
        this.mPropertyAdapter = new MessagePropertyAdapter();
        super.setNamedPropertyAdapter(this.mPropertyAdapter);
    }
    
    @Override
    public String getNameInLayer() {
        return WSDLExtensibilityElements.ELEMENT_MESSAGE;
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
    protected Node.Property createAttributeProperty(QName attrQName) {
        Node.Property attrValueProperty = null;
        try {
            String attrName = attrQName.getLocalPart();
            //name
            if(attrName.equals(Message.NAME_PROPERTY)) { //NOT I18N
                //name
                attrValueProperty = createNameProperty();
                
            }  else {
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
        
        return alwaysPresentAttrProperties;
    }
    
    
    private Node.Property createNameProperty() throws NoSuchMethodException {
        Node.Property attrValueProperty;
        attrValueProperty = new BaseAttributeProperty(mPropertyAdapter, 
                String.class, Message.NAME_PROPERTY);
        attrValueProperty.setName(Message.NAME_PROPERTY);
        attrValueProperty.setDisplayName(NbBundle.getMessage(MessageNode.class, "PROP_NAME_DISPLAYNAME"));
        attrValueProperty.setShortDescription(NbBundle.getMessage(MessageNode.class, "MESSAGE_NAME_DESC"));
        
        return attrValueProperty;
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(MessageNode.class);
    }
    
    public class MessagePropertyAdapter extends ConstraintNamedPropertyAdapter {
        
        public MessagePropertyAdapter() {
            super(getWSDLComponent());
        }
        
        @Override
        public boolean isNameExists(String name) {
            WSDLModel document = getWSDLComponent().getModel();
            return NameGenerator.getInstance().isMessageExists(name, document);
        }
        
    }
    
    public static final class MessageNewTypesFactory implements NewTypesFactory{
        
        public NewType[] getNewTypes(WSDLComponent def) {
            ArrayList<NewType> list = new ArrayList<NewType>();
            list.add(new MessagePartNewType(def));
            if (def.getDocumentation() == null) {
                list.add(new DocumentationNewType(def));
            }
            list.addAll(Arrays.asList(new ExtensibilityElementNewTypesFactory(WSDLExtensibilityElements.ELEMENT_MESSAGE).getNewTypes(def)));
            return list.toArray(new NewType[]{});
        }        
        
    }

    @Override
    public String getTypeDisplayName() {
        return NbBundle.getMessage(MessageNode.class, "LBL_MessageNode_TypeDisplayName");
    }
}
