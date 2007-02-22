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
 * Created on May 26, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.xml.namespace.QName;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;

//import org.netbeans.modules.xml.refactoring.actions.FindUsagesAction;
//import org.netbeans.modules.xml.refactoring.actions.RefactorAction;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.ui.actions.RemoveAttributesAction;
import org.netbeans.modules.xml.wsdl.ui.actions.extensibility.AddAttributeAction;
import org.netbeans.modules.xml.wsdl.ui.commands.ConstraintNamedPropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.commands.NamedPropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElement;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElementInfo;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElements;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElementsFactory;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.impl.ExtensibilityUtils;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.property.view.PropertyViewFactory;
import org.netbeans.modules.xml.wsdl.ui.schema.visitor.SchemaDocumentationFinderVisitor;
import org.netbeans.modules.xml.wsdl.ui.spi.ExtensibilityElementConfigurator;
import org.netbeans.modules.xml.wsdl.ui.spi.ExtensibilityElementConfiguratorFactory;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.ExtensibilityElementChildNewTypesFactory;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.NewTypesFactory;
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
import org.openide.nodes.Sheet;
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
               // actions.add(SystemAction.get(FindUsagesAction.class));
                actions.add(RefactoringActionsFactory.whereUsedAction());
                actions.add(null);
                actions.add(RefactoringActionsFactory.editorSubmenuAction());
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
        Sheet.Set defaultSet = null;
        if (mSchemaElement != null) {
            try {
                Sheet.Set[] sets = PropertyViewFactory.getInstance().getPropertySets(mWSDLConstruct, mQName, mSchemaElement);
                if (sets != null) {
                    for (Sheet.Set set : sets) {
                        mSheet.put(set);
                    }
                    if (sets.length > 0)
                        defaultSet = sets[sets.length - 1];
                }
                if (defaultSet != null) {
                    List<Node.Property> properties = createAlwaysPresentAttributeProperty();
                    if(properties != null) {
                        Iterator<Node.Property> itP = properties.iterator();
                        while(itP.hasNext()) {
                            Node.Property property = itP.next();
                            //if property is not present then add it
                            if(defaultSet.get(property.getName()) == null) {
                                defaultSet.put(property);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                ErrorManager.getDefault().notify(e);
            }
        } else {
            super.refreshAttributesSheetSet();
        }
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



