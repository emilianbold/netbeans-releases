/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * Created on May 26, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.xml.namespace.QName;

import org.netbeans.modules.xml.refactoring.actions.FindUsagesAction;
import org.netbeans.modules.xml.refactoring.actions.RefactorAction;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.SimpleType;
import org.netbeans.modules.xml.schema.model.SimpleTypeRestriction;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.ui.actions.RemoveAttributesAction;
import org.netbeans.modules.xml.wsdl.ui.actions.extensibility.AddAttributeAction;
import org.netbeans.modules.xml.wsdl.ui.commands.CommonAttributePropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.commands.ConstraintNamedPropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.commands.NamedPropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.commands.OtherAttributePropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.commands.PropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.commands.XMLAttributePropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElement;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElementInfo;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElements;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElementsFactory;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.impl.ExtensibilityUtils;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.schema.visitor.SchemaAttributeTypeFinderVisitor;
import org.netbeans.modules.xml.wsdl.ui.schema.visitor.SchemaDocumentationFinderVisitor;
import org.netbeans.modules.xml.wsdl.ui.schema.visitor.SchemaElementAttributeFinderVisitor;
import org.netbeans.modules.xml.wsdl.ui.schema.visitor.SchemaUtility;
import org.netbeans.modules.xml.wsdl.ui.spi.ExtensibilityElementConfigurator;
import org.netbeans.modules.xml.wsdl.ui.spi.ExtensibilityElementConfiguratorFactory;
import org.netbeans.modules.xml.wsdl.ui.view.property.BaseAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.view.property.XSDBooleanAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.view.property.XSDEnumeratedAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.ExtensibilityElementChildNewTypesFactory;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.NewTypesFactory;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.netbeans.modules.xml.xam.ui.actions.GoToAction;
import org.openide.ErrorManager;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.NewAction;
import org.openide.actions.PasteAction;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;



/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensibilityElementNode extends WSDLNamedElementNode{

    
    private static final Image ICON  = Utilities.loadImage
        ("org/netbeans/modules/xml/wsdl/ui/view/resources/generic.png");

    
    private ExtensibilityElement mWSDLConstruct;
   
    private Node mLayerDelegateNode;
    
    private WSDLExtensibilityElement mExtensibilityElement;
    
    private Element mSchemaElement;
    
    private boolean canRename = false;
    
    private QName mQName = null;
    
    private ExtensibilityElementConfigurator mConfigurator;
    
    public ExtensibilityElementNode(ExtensibilityElement wsdlConstruct) {
        super(new GenericWSDLComponentChildren(wsdlConstruct), wsdlConstruct);
        mWSDLConstruct = wsdlConstruct;
        QName qName = mWSDLConstruct.getQName();
        //Fix qname, sometimes there is no namespace associated with it.
        String namespace = null;
        if (qName.getPrefix() != null) {
            namespace = Utility.getNamespaceURI(qName.getPrefix(), mWSDLConstruct);
            mQName = new QName(namespace, qName.getLocalPart(), qName.getPrefix());
        } else {
            mQName = qName;
        }
        
        String displayName = mQName != null ?  Utility.fromQNameToString(mQName) : "Missing Name"; 
        mConfigurator = new ExtensibilityElementConfiguratorFactory().getExtensibilityElementConfigurator(mQName);
        boolean isNameSet = false;
        if (isNamedReferenceable()) {
            setNamedPropertyAdapter(new ExtensibilityElementConstrainedNamedPropertyAdapter());
            canRename = true;
        } else {
            if (mConfigurator != null) {
                String attributeName = mConfigurator.getDisplayAttributeName(wsdlConstruct, mQName);
                
                if (attributeName != null) {
                    setNamedPropertyAdapter(attributeName, new ExtensibilityElementNamedPropertyAdapter(attributeName));
                    String value = wsdlConstruct.getAttribute(attributeName);
                    if (value != null && value.trim().length() > 0) {
                        isNameSet = true;
                        setDisplayName(value);
                    }
                    canRename = true;
                }
            }
            
            if (!isNameSet) {
                this.setDisplayName(displayName);
                setShortDescription(mQName != null ?  mQName.toString() : "Missing Name");
            }
        }
        try {
            WSDLComponent parentComponent = mWSDLConstruct.getParent();
            String extensibilityElementType = ExtensibilityUtils.getExtensibilityElementType(parentComponent);
            if(extensibilityElementType != null) {
                WSDLExtensibilityElements elements = WSDLExtensibilityElementsFactory.getInstance().getWSDLExtensibilityElements();
                mExtensibilityElement = elements.getWSDLExtensibilityElement(extensibilityElementType);
                if(mExtensibilityElement != null) {
                    WSDLExtensibilityElementInfo info = mExtensibilityElement.getWSDLExtensibilityElementInfos(mQName);
                    if(info != null && info.getElement() != null) {
                        this.mLayerDelegateNode = getLayerDelegateNode(info);
                        
                        this.mSchemaElement = info.getElement();
                        /*SchemaElementCookie sCookie = new SchemaElementCookie(mElement);
                        getLookupContents().add(sCookie);*/
                    }
                }
            } else {
                mSchemaElement = ExtensibilityUtils.getElement(mWSDLConstruct);
                
/*                SchemaElementCookie sCookie = (SchemaElementCookie) parent.getCookie(SchemaElementCookie.class);
                if(sCookie != null && sCookie.getElement() != null) {
                    Element parentElement = sCookie.getElement();
                    if(parentElement != null) {
                        SchemaElementFinderVisitor seFinder = new SchemaElementFinderVisitor(qName.getLocalPart());
                        parentElement.accept(seFinder);
                        this.mElement = seFinder.getSuccessorElement();
                        if(this.mElement != null) {
                            sCookie = new SchemaElementCookie(mElement);
                            getLookupContents().add(sCookie);
                        }
                    }
                    XMLType type = parentElement.getType();
                     if(type instanceof ComplexType) {
                     ComplexType cType = (ComplexType) type;
                     this.mElement = cType.getElementDecl(this.mWSDLConstruct.getLocalName());
                     if(this.mElement != null) {
                     sCookie = new SchemaElementCookie(mElement);
                     getLookupContents().add(sCookie);
                     }
                     }
                } else {
                    mLogger.warning("Failed to find SchemaElementCookie in parent " + wsdlConstruct.toString() + " or SchemaElementCookie has null schema element");
                }*/
            }
        } catch(Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        
        if(this.mSchemaElement != null) {
            SchemaDocumentationFinderVisitor sdFinder = new SchemaDocumentationFinderVisitor();
            this.mSchemaElement.accept(sdFinder);
            String docStr = sdFinder.getDocumentation();
            if(docStr != null && !docStr.trim().equals("")) {
                this.setShortDescription(docStr);
                
            }
        }
        
        
        this.mWSDLConstruct.getModel().addComponentListener(this);
        
    }
    
    private Node getLayerDelegateNode(WSDLExtensibilityElementInfo elementInfo) {
        Node delegateNode = elementInfo.getDataObject().getNodeDelegate();
        return delegateNode;
    }
    
    private boolean isNamedReferenceable() {
        if (mWSDLConstruct instanceof NamedReferenceable) {
            return true;
        }
        return false;
    }
    
    @Override
    public Image getIcon(int type) {
        if(mLayerDelegateNode != null) {
            return mLayerDelegateNode.getIcon(type);
        }
        return ICON;
    }

    @Override
    public Image getOpenedIcon(int type) {
        if(mLayerDelegateNode != null) {
            return mLayerDelegateNode.getOpenedIcon(type);
        }
        return ICON;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return createDynamicActions();
    }
    
    
    
    @Override
    public NewTypesFactory getNewTypesFactory() {
        return new ExtensibilityElementChildNewTypesFactory(mSchemaElement);
    }

    @Override
    public boolean canRename() {
        return canRename;
    }

    private Action[] createDynamicActions() {
        List<Action> actions = new ArrayList<Action>();
        
        //add these always
        actions.add(SystemAction.get(CutAction.class));
        actions.add(SystemAction.get(CopyAction.class));
        actions.add(SystemAction.get(PasteAction.class));
        actions.add(null);
        actions.add(SystemAction.get(NewAction.class));
        actions.add(SystemAction.get(DeleteAction.class));
        actions.add(null);
        //if optional attributes missing then add this action
        if (mSchemaElement != null) {
            if (Utils.isExtensionAttributesAllowed(mSchemaElement)) {
                actions.add(SystemAction.get(AddAttributeAction.class));
                actions.add(SystemAction.get(RemoveAttributesAction.class));
                actions.add(null);
            }
            actions.add(SystemAction.get(GoToAction.class));
            if (isNamedReferenceable()) {
                actions.add(SystemAction.get(FindUsagesAction.class));
                actions.add(null);
                actions.add(SystemAction.get(RefactorAction.class));
            }
            actions.add(null);
            actions.add(SystemAction.get(PropertiesAction.class));
        } else {
            actions.add(SystemAction.get(GoToAction.class));
            actions.add(null);
            actions.add(SystemAction.get(PropertiesAction.class));
        }
        
        return actions.toArray(new Action[actions.size()]);
    }
    
    @Override
    protected void refreshAttributesSheetSet()  {
        Sheet.Set ss = createPropertiesSheetSet();
        if (mSchemaElement != null) {
            SchemaElementAttributeFinderVisitor seaFinder = new SchemaElementAttributeFinderVisitor(mSchemaElement, true);
            this.mSchemaElement.accept(seaFinder);
            List<Attribute> attributes = seaFinder.getAttributes();
            for (Attribute attr : attributes) {
                if(attr instanceof Nameable) {
                    Nameable namedAttr = (Nameable) attr;
                    QName attrQName = new QName(mSchemaElement.getModel().getSchema().getTargetNamespace(), namedAttr.getName());
                    Node.Property attrValueProperty = createAttributeProperty(attrQName);
                    if (attrValueProperty != null) {//if null, property is hidden
                        ss.put(attrValueProperty);
                    } 
                }
            }
            
            //find if there needs to be a any element tag, display a simple string property editor.
//          TODO:SKINI enable any.  
            MixedContentFinderVisitor aeFinder = new MixedContentFinderVisitor(mSchemaElement);
            mSchemaElement.accept(aeFinder);
            if (aeFinder.hasMixedContent()) {
                Node.Property attrValueProperty;
                try {
                    attrValueProperty = new BaseAttributeProperty(new AnyElementPropertyAdapter(mWSDLConstruct, mQName), String.class, "value");
                    attrValueProperty.setName(NbBundle.getMessage(ExtensibilityElementNode.class, "PROP_NAME_ANY_CONTENT"));
                    ss.put(attrValueProperty);
                } catch (NoSuchMethodException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
            
            List<Node.Property> properties = null;
            try {
                properties = createAlwaysPresentAttributeProperty();
            } catch (Exception e) {
                ErrorManager.getDefault().notify(e);
            }
            if(properties != null) {
                Iterator<Node.Property> itP = properties.iterator();
                while(itP.hasNext()) {
                    Node.Property property = itP.next();
                    //if property is not present then add it
                    if(ss.get(property.getName()) == null) {
                        ss.put(property);
                    }
                }
            }
            
        } else {
            super.refreshAttributesSheetSet();
        }
    }
    
    
    private Node.Property getProperty(QName attrQName, 
            CommonAttributePropertyAdapter propertyAdapter) throws NoSuchMethodException {
        Node.Property attrValueProperty = null;
        SimpleType gst = findAttributeType(attrQName.getLocalPart());
        
        if(gst != null) {
            attrValueProperty = processSimpleType(gst, propertyAdapter);
            
/*            //If not boolean or enumeration, check if spi has been implemented for this element.
            if(attrValueProperty == null) {
                QName qname = mWSDLConstruct.getQName();
                attrValueProperty = new ExtensibilityElementConfiguratorFactory().getNodeProperty(mWSDLConstruct, 
                        qname , attrQName.getLocalPart());  
            }*/
            
            
            //Set the description of the attribute with documentation provided from the schema for the element.
            if (attrValueProperty != null) {
                if (attrValueProperty.getName() == null || attrValueProperty.getName().trim().length() ==0) {
                    attrValueProperty.setName(attrQName.getLocalPart());
                }
                String description = attrValueProperty.getShortDescription();
                
                if (description == null || description.equals(attrValueProperty.getDisplayName()) || description.trim().length() == 0) {
                    String desc = getAttributeShortDescription(attrQName);
                    if(desc != null && !desc.trim().equals("")) {
                        attrValueProperty.setShortDescription(desc);
                    } else {
                        attrValueProperty.setShortDescription(attrQName.toString());
                    }
                }
            }
        }
        
        return attrValueProperty;
    }

    
   @Override
   protected Node.Property getOtherAttributeNodeProperty(QName attrQName, 
           OtherAttributePropertyAdapter propertyAdapter) throws NoSuchMethodException {
       
       Node.Property attrValueProperty =  getProperty(attrQName, propertyAdapter);
       if(attrValueProperty != null) {
           return attrValueProperty;
       }
       
       return super.getOtherAttributeNodeProperty(attrQName, propertyAdapter);
   }
    
    @Override
    protected Node.Property getAttributeNodeProperty(String attrName, 
            XMLAttributePropertyAdapter propertyAdapter) throws NoSuchMethodException {
        Node.Property attrValueProperty =  getProperty(QName.valueOf(attrName), propertyAdapter);
        if(attrValueProperty != null) {
            return attrValueProperty;
        }
        
        return super.getAttributeNodeProperty(attrName, propertyAdapter);
    }
    
    @Override
    protected String getAttributeShortDescription(String attrName) {
        if(this.mSchemaElement != null) {
            Attribute attribute = SchemaUtility.findAttribute(QName.valueOf(attrName), this.mSchemaElement);
            if(attribute != null) {
                SchemaDocumentationFinderVisitor sdfFinder = new SchemaDocumentationFinderVisitor();
                attribute.accept(sdfFinder);
                return sdfFinder.getDocumentation();
            }
        }
        return null;
    }
    
    @Override
    protected String getAttributeShortDescription(QName attrQName) {
        if (mSchemaElement != null) {
            Attribute attribute = SchemaUtility.findAttribute(attrQName, this.mSchemaElement);
            if(attribute != null) {
                SchemaDocumentationFinderVisitor sdfFinder = new SchemaDocumentationFinderVisitor();
                attribute.accept(sdfFinder);
                return sdfFinder.getDocumentation();
            }
        }
        return null;
    }
    
    private SimpleType findAttributeType(String attrName) {
        SimpleType gst = null;
        if(this.mSchemaElement != null) {
            SchemaElementAttributeFinderVisitor seaFinder = new SchemaElementAttributeFinderVisitor(mSchemaElement);
            this.mSchemaElement.accept(seaFinder);

            List<Attribute> attributes = seaFinder.getAttributes();
            Iterator<Attribute> it = attributes.iterator();

            while(it.hasNext()) {
                Attribute attr = it.next();
                if(attr instanceof Nameable) {
                    Nameable namedAttr = (Nameable) attr;
                    if(attrName.equals(namedAttr.getName()))  {
                        SchemaAttributeTypeFinderVisitor typeFinder = new SchemaAttributeTypeFinderVisitor();
                        attr.accept(typeFinder);
                        gst = typeFinder.getSimpleType();
                        break;
                    }
                }
            }
        }
        return gst;
    }
    
    
    private Node.Property processSimpleType(SimpleType gst, CommonAttributePropertyAdapter propertyAdapter) throws NoSuchMethodException {
        Node.Property attrValueProperty = null;  
        //for boolean type we show true/false drop down
        if(gst != null) {
            String simpleTypeName = null;
            if (gst instanceof GlobalSimpleType) {
                simpleTypeName = ((GlobalSimpleType) gst).getName();
            }
            String namesapce = gst.getModel().getSchema().getTargetNamespace();
            SchemaModel primitiveTypesModel = SchemaModelFactory.getDefault().getPrimitiveTypesModel();
            String primitiveTypeNamesapce = primitiveTypesModel.getSchema().getTargetNamespace();
            if(namesapce != null 
                    && namesapce.equals(primitiveTypeNamesapce)
                    && simpleTypeName != null && simpleTypeName.equals("boolean")) {//NOI18N
                attrValueProperty = 
                    new XSDBooleanAttributeProperty((GlobalSimpleType) gst, propertyAdapter, String.class, "getValue", "setValue");//NOI18N
                
            } else if(gst.getDefinition() instanceof  SimpleTypeRestriction) {
                //if attribute has enumeration facet
                //then use the first enumeration value
                SimpleTypeRestriction sr = (SimpleTypeRestriction) gst.getDefinition();
                Collection enumerations = sr.getEnumerations();
                if(enumerations != null && enumerations.size() > 0) {
                    attrValueProperty = 
                        new XSDEnumeratedAttributeProperty(gst, propertyAdapter, String.class, "getValue", "setValue");//NOI18N
                    
                }
                
            }
        }
        
        
        return attrValueProperty;
    }
    
    
    public class ExtensibilityElementNamedPropertyAdapter implements NamedPropertyAdapter {
        String attributeName;
        
        
        public ExtensibilityElementNamedPropertyAdapter(String attributeName) {
            this.attributeName = attributeName;
        }

        public void setName(String name) {
            mWSDLConstruct.getModel().startTransaction();
            mWSDLConstruct.setAttribute(attributeName, name);
                mWSDLConstruct.getModel().endTransaction();
        }

        public String getName() {
            return mWSDLConstruct.getAttribute(attributeName);
        }
        
        public boolean isWritable() {
            return XAMUtils.isWritable(mWSDLConstruct.getModel());
        }
        
    }
    public class ExtensibilityElementConstrainedNamedPropertyAdapter extends ConstraintNamedPropertyAdapter {
        public ExtensibilityElementConstrainedNamedPropertyAdapter() {
            super(getWSDLComponent());
        }


        @Override
        public boolean isNameExists(String name) {
            return false;
        }
        
    }


    @Override
    protected Property createAttributeProperty(QName attrQName) {
        QName elementQName = new QName(attrQName.getNamespaceURI(), mWSDLConstruct.getQName().getLocalPart());
        ExtensibilityElementConfigurator configurator = new ExtensibilityElementConfiguratorFactory().getExtensibilityElementConfigurator(
                elementQName);
        if (configurator != null) {
            if (configurator.isHidden(mWSDLConstruct, elementQName, attrQName.getLocalPart())) {
                return null;
            }
            
            Property property = configurator.getProperty(mWSDLConstruct, elementQName, attrQName.getLocalPart());
            if (property != null){ 
                if(property.getName() == null) {
                    property.setName(attrQName.getLocalPart());
                }
                if (property.getShortDescription() == null) {
                    property.setShortDescription(this.getAttributeShortDescription(attrQName));
                }
                return property;
            }
        }
        return super.createAttributeProperty(attrQName);
    }
    
    public class AnyElementPropertyAdapter extends PropertyAdapter {
        QName qname;
        
        public AnyElementPropertyAdapter(ExtensibilityElement delegate, QName qname) {
            super(delegate);
            this.qname = qname;
        }
        
        public String getValue() {
            String content = ((ExtensibilityElement)getDelegate()).getContentFragment();
            if (content != null) {
                return content;
            }
            return "";
        }
        
        public void setValue(String string) {
            
            boolean isInTransaction = Utility.startTransaction(getDelegate().getModel());
            try {
                ((ExtensibilityElement)getDelegate()).setContentFragment(string);
                if (!isInTransaction) {
                    Utility.endTransaction(getDelegate().getModel(), isInTransaction);
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        
        
    }


    @Override
    public String getTypeDisplayName() {
        if (mConfigurator == null) return null;
        
        return mConfigurator.getTypeDisplayName(mWSDLConstruct, mQName);
    }
    
    @Override
    public String getHtmlDisplayName() {
        String htmlDisplayName = super.getHtmlDisplayName();
        
        if (mConfigurator == null) 
            return htmlDisplayName;
        
        String decoration = mConfigurator.getHtmlDisplayNameDecoration(mWSDLConstruct, mQName);
        
        if (decoration == null)
            return htmlDisplayName;
        
        return htmlDisplayName + " <font color='#999999'>"+decoration+"</font>";
        
    }
}



