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
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.Action;
import javax.xml.XMLConstants;

import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Documentation;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.api.property.PropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.commands.NamedPropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElements;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.property.BaseAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.BindingNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.DocumentationNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.ExtensibilityElementNewTypesFactory;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.ImportSchemaNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.ImportWSDLNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.MessageNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.NewTypesFactory;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.PortTypeNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.ServiceNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.TypesNewType;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.ui.actions.GoToAction;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.NewAction;
import org.openide.actions.PasteAction;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;

/**
 *
 * @author Ritesh Adval
 *
 * 
 */
public class DefinitionsNode extends WSDLExtensibilityElementNode<Definitions> {
    
    
    private Image ICON = Utilities.loadImage
    ("org/netbeans/modules/xml/wsdl/ui/netbeans/module/resources/wsdl_file.png");
    
    private DefinitionsPropertyAdapter mPropertyAdapter;
    
    private static final SystemAction[] ACTIONS = new SystemAction[]{
        SystemAction.get(CutAction.class),
        SystemAction.get(CopyAction.class),
        SystemAction.get(PasteAction.class),
        null,
        SystemAction.get(NewAction.class),
        null,
// Currently there are only, partnerlink type and property and property
// alias which is going to be put in main add.
//        SystemAction.get(CommonAddExtensibiltyElementAction.class), 
//        null,
        SystemAction.get(GoToAction.class),
        null,
        SystemAction.get(PropertiesAction.class)
    };

    //Do not allow to create a definition node, with any children other than DefinitionChildren.
    private DefinitionsNode(Definitions wsdlDef, Children children) {
        super(children, wsdlDef, new DefinitionsNewTypesFactory());
        
        this.mPropertyAdapter = new DefinitionsPropertyAdapter();
        super.setNamedPropertyAdapter(this.mPropertyAdapter);
    }
    
    public DefinitionsNode(Definitions mWSDLDef) {
        this(mWSDLDef, new DefinitionsChildren(mWSDLDef));
    }

    public DefinitionsNode(Definitions mWSDLDef, List<Class<? extends WSDLComponent>> filters) {
        this(mWSDLDef, new DefinitionsChildren(mWSDLDef, filters));
    }
    
    @Override
    public String getNameInLayer() {
        return WSDLExtensibilityElements.ELEMENT_DEFINITIONS;
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
    public boolean canCopy() {
        return false;
    }

    @Override
    public boolean canCut() {
        return false;
    }

    
    /*
     * For purpose of column view, definition node doesnt have any child nodes other 
     * than folder nodes and type node (which is considered as folder node).
     * (non-Javadoc)
     * @see org.netbeans.modules.xml.wsdl.ui.view.treeeditor.WSDLElementNode#getChildCount()
     */
    @Override
    public int getChildCount() {
        return 0;
    }
    
    @Override
    protected void updateDisplayName() {
        Definitions defs = getWSDLComponent();
        String name = defs.getTargetNamespace();
        if (name == null) {
            name = NbBundle.getMessage(DefinitionsNode.class,
                    "LBL_DefinitionsNode_NoTargetNamespace");
        }
        setDisplayName(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void refreshAttributesSheetSet(Sheet sheet) {
        Sheet.Set ss = sheet.get(Sheet.PROPERTIES);
        
        try {
            //name
            Node.Property nameProperty = new BaseAttributeProperty(mPropertyAdapter, 
                    String.class, Definitions.NAME_PROPERTY);
            nameProperty.setName(Definitions.NAME_PROPERTY);
            nameProperty.setDisplayName(NbBundle.getMessage(DefinitionsNode.class, "PROP_NAME_DISPLAYNAME"));
            nameProperty.setShortDescription(NbBundle.getMessage(DefinitionsNode.class, "DEFINITIONSNODE_NAME_DESCRIPTION"));
            ss.put(nameProperty);
            
            //targetNamespace
            Node.Property targetNamespaceProperty = new BaseAttributeProperty(mPropertyAdapter, 
                    String.class, Definitions.TARGET_NAMESPACE_PROPERTY);
            targetNamespaceProperty.setName(Definitions.TARGET_NAMESPACE_PROPERTY);
            targetNamespaceProperty.setDisplayName(NbBundle.getMessage(DefinitionsNode.class, "PROP_TARGET_NAMESPACE_DISPLAYNAME"));
            targetNamespaceProperty.setShortDescription(NbBundle.getMessage(DefinitionsNode.class, "DEFINITIONSNODE_TARGETNAMESPACE_DESCRIPTION"));
            ss.put(targetNamespaceProperty);
            
            
            //default namespace
            Node.Property defaultNamespaceProperty = new BaseAttributeProperty(mPropertyAdapter, 
                    String.class, "defaultNamespace"); //NOI18N
            
            defaultNamespaceProperty.setName("defaultNamespace");//NOI18N
            defaultNamespaceProperty.setDisplayName(NbBundle.getMessage(DefinitionsNode.class, "PROP_DEFAULT_NAMESPACE"));//NOI18N
            defaultNamespaceProperty.setShortDescription(NbBundle.getMessage(DefinitionsNode.class, "DEFINITIONS_NODE_DEFAULTNAMESPACE_DESC"));
            ss.put(defaultNamespaceProperty);
            
            //add prefixes sheet
            refreshPrefixesSheetSet(sheet);
            
        } catch(Exception ex) {
            mLogger.log(Level.SEVERE, "failed to create property sheet for "+ getWSDLComponent(), ex);
        }
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (isValid() && event.getSource() == getWSDLComponent()) {
            String propName = event.getPropertyName();
            if (propName.equals("xmlns")) {
                firePropertyChange("defaultNamespace", event.getOldValue(), event.getNewValue());
            } else {
                refreshPrefixesSheetSet(getSheet());
                super.propertyChange(event);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void refreshPrefixesSheetSet(Sheet sheet) {
        
        Sheet.Set prefixesSheetSet = sheet.get(NbBundle.getMessage(DefinitionsNode.class, "PROP_SHEET_CATEGORY_PREFIXES"));
        if (prefixesSheetSet == null) {
            prefixesSheetSet = new Sheet.Set();
            String prefixesSheetName = NbBundle.getMessage(DefinitionsNode.class, "PROP_SHEET_CATEGORY_PREFIXES");
            prefixesSheetSet.setName(prefixesSheetName);
            prefixesSheetSet.setDisplayName(prefixesSheetName);
            sheet.put(prefixesSheetSet);
        }
        
        Map<String, String> prefixesToNamespaceMap = Utility.getPrefixes(getWSDLComponent());
        prefixesToNamespaceMap.remove(XMLConstants.XMLNS_ATTRIBUTE);
        prefixesToNamespaceMap.remove(XMLConstants.DEFAULT_NS_PREFIX);
        
        
        for (String prefix : prefixesToNamespaceMap.keySet()) {
            String prefixAttributeName = XMLConstants.XMLNS_ATTRIBUTE + ":" + prefix;
            if (prefixesSheetSet.get(prefixAttributeName) == null) {
                PropertyAdapter pn = new PrefixToNamespace(prefix, getWSDLComponent());
                
                Node.Property prefixToNamespaceProperty = null;
                
                try {
                    prefixToNamespaceProperty = new BaseAttributeProperty(pn, String.class, "namespace"); //NOI18N
                } catch (NoSuchMethodException e) {
                }
                
                if (prefixToNamespaceProperty == null) continue;

                prefixToNamespaceProperty.setName(prefixAttributeName);
                prefixToNamespaceProperty.setDisplayName(prefix);
                prefixToNamespaceProperty.setShortDescription(NbBundle.getMessage(DefinitionsNode.class, "DEFINITIONSNODE_USER_DEF_PREFIX_DESC"));
                prefixesSheetSet.put(prefixToNamespaceProperty);
            }
            
            
            
        }
//      remove unused properties
        Set<String> set = prefixesToNamespaceMap.keySet();
        for (Property prop : prefixesSheetSet.getProperties()) {
            String[] splits = prop.getName().split(":");
            if (splits.length == 2) {
                if (!set.contains(splits[1])) {
                    prefixesSheetSet.remove(prop.getName());
                }
            }
        }
        
    }
    
    public static class PrefixToNamespace extends PropertyAdapter {
        
        private Definitions mWSDLConstruct;
        
        private String mPrefix;
        
        public PrefixToNamespace(String prefix, Definitions definitions) {
            super(definitions);
            this.mPrefix = prefix;
            this.mWSDLConstruct = definitions;
        }
        
        public String getPrefix() {
            return this.mPrefix;
        }
        
        @SuppressWarnings("unchecked")
        public void setPrefix(String prefix) {
            
            String namespace = getNamespace();
            mWSDLConstruct.getModel().startTransaction();
            ((AbstractDocumentComponent) mWSDLConstruct).removePrefix(mPrefix);
            ((AbstractDocumentComponent) mWSDLConstruct).addPrefix(prefix, namespace);
                mWSDLConstruct.getModel().endTransaction();
            mPrefix = prefix;
        }
        
        @SuppressWarnings("unchecked")
        public void setNamespace(String namespace) {
            mWSDLConstruct.getModel().startTransaction();
            ((AbstractDocumentComponent) mWSDLConstruct).addPrefix(mPrefix, namespace);
                mWSDLConstruct.getModel().endTransaction();
        }
        
        public String getNamespace() {
            return Utility.getNamespaceURI(this.mPrefix, mWSDLConstruct);
        }    
    }
    public static final class DefinitionsChildren extends RefreshableChildren {
        
        List<Class<? extends WSDLComponent>> filters;
        Definitions def;
        public DefinitionsChildren(Definitions definitions) {
            super();
            def = definitions;
        }
        
        @Override
        public void refreshChildren() {
            super.refreshChildren();
            Node[] nds = getNodes();
            for (Node node : nds) {
                Children children = node.getChildren();
                if (children instanceof RefreshableChildren) {
                    RefreshableChildren.class.cast(children).refreshChildren();
                }
            }
        }
        
        //Hack for creating children with only specific categories
        /**
         * Only top level filters are supported.
         * Message, Import, Types, Documentation, PortType, Binding , Service and ExtensibilityElement are supported.
         * If filters are specified, then only those folders which support that top level component are created.
         * 
         */
        public DefinitionsChildren(Definitions definitions, List<Class<? extends WSDLComponent>> filters) {
            this(definitions);
            this.filters = filters;
        }
        
        @Override
        protected Node[] createNodes(Object key) {
            Node node = null;
            
            if (key instanceof WSDLComponent) {
               node = NodesFactory.getInstance().create(WSDLComponent.class.cast(key));
            } else if (IMPORTS_FOLDER.equals(key)) {
                node = new ImportFolderNode(def);
            } else if (MESSAGES_FOLDER.equals(key)) {
                node = new MessageFolderNode(def);
            } else if (PORTTYPES_FOLDER.equals(key)) {
                node = new PortTypeFolderNode(def);
            } else if (BINDING_FOLDER.equals(key)) {
                node = new BindingFolderNode(def);
            } else if (SERVICES_FOLDER.equals(key)) {
                node = new ServiceFolderNode(def);
            } else if (EXTENSIBILITY_ELEMENTS_FOLDER.equals(key)) {
                node = new ExtensibilityElementsFolderNode(def);
            }
            
            
            if(node != null) {
                return new Node[] {node};
            }
            
            return null;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public Collection getKeys() {
            Collection<Object> keys = new ArrayList<Object>();
            
            
            if (filters == null || filters.contains(Documentation.class)) {
                Documentation doc = def.getDocumentation();
                if (doc != null) {
                    keys.add(doc);
                }
            }
            
            if (filters == null || filters.contains(Types.class)) {
                Types types = def.getTypes();
                if (types != null) {
                    keys.add(types);
                }
            }
            
            if (filters == null || filters.contains(Import.class)) {
                keys.add(IMPORTS_FOLDER);
            }
            
            if (filters == null || filters.contains(Message.class)) {
                keys.add(MESSAGES_FOLDER);
            }
            
            if (filters == null || filters.contains(PortType.class)) {
                keys.add(PORTTYPES_FOLDER);
            }
            if (filters == null || filters.contains(Binding.class)) {
                
                keys.add(BINDING_FOLDER);
            }
            if (filters == null || filters.contains(Service.class)) {
                keys.add(SERVICES_FOLDER);
            }
            
            if (filters == null || filters.contains(ExtensibilityElement.class)) {
                keys.add(EXTENSIBILITY_ELEMENTS_FOLDER);
            }

            return keys;
        }
        
        
        
    }
    
    public static final String IMPORTS_FOLDER = "IMPORTS_FOLDER"; //NOI18N
    public static final String MESSAGES_FOLDER = "MESSAGES_FOLDER";//NOI18N
    public static final String PORTTYPES_FOLDER = "PORTTYPES_FOLDER";//NOI18N
    public static final String BINDING_FOLDER  = "BINDING_FOLDER";//NOI18N
    public static final String SERVICES_FOLDER = "SERVICES_FOLDER";//NOI18N
    public static final String EXTENSIBILITY_ELEMENTS_FOLDER = "EXTENSIBILITY_ELEMENTS_FOLDER";//NOI18N
    //public static final String PREFIXES_FOLDER = "PREFIXES_FOLDER";//NOI18N
    
    
    public class DefinitionsPropertyAdapter extends PropertyAdapter implements NamedPropertyAdapter {
        
        public DefinitionsPropertyAdapter() {
            super(getWSDLComponent());
        }
        
        public void setName(String name) {
            if (name != null && name.trim().length() == 0) {
                name = null;
            }
            
            if (name == null || !org.netbeans.modules.xml.xam.dom.Utils.isValidNCName(name)) {
            	DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(DefinitionsNode.class, "ERR_MSG_INVALID_NCNAME", name == null ? "" : name)));
                return;
            }
            getWSDLComponent().getModel().startTransaction();
            getWSDLComponent().setName(name);
            getWSDLComponent().getModel().endTransaction(); 
        }
        
        public String getName() {
            String name = getWSDLComponent().getName();
            if(name == null) {
                return "";
            }
            
            return name;
        }
        
        public void setTargetNamespace(String targetNamespace) {
            getWSDLComponent().getModel().startTransaction();
            getWSDLComponent().setTargetNamespace(targetNamespace);
            getWSDLComponent().getModel().endTransaction();
        }
        
        public String getTargetNamespace() {
            String tns = getWSDLComponent().getTargetNamespace();
            if(tns == null) {
                return "";
            }
            
            return tns;
        }
        
        @SuppressWarnings("unchecked")
        public void setDefaultNamespace(String defaultNamespace) {
            WSDLModel model = getWSDLComponent().getModel();
            model.startTransaction();
            ((AbstractDocumentComponent)getWSDLComponent()).addPrefix(XMLConstants.DEFAULT_NS_PREFIX, defaultNamespace);//getPrefixes().put("", defaultNamespace);
                model.endTransaction();
        }
        
        @SuppressWarnings("unchecked")
        public String getDefaultNamespace() {
            String dns = (String) ((AbstractDocumentComponent) getWSDLComponent()).getPrefixes().get(XMLConstants.DEFAULT_NS_PREFIX);
            if(dns == null) {
                return "";
            }
            return dns;
        }
    }
    
    public static final class DefinitionsNewTypesFactory implements NewTypesFactory{
        
        public NewType[] getNewTypes(WSDLComponent def) {
            
            ArrayList<NewType> list = new ArrayList<NewType>();
            if (def.getDocumentation() == null) {
                list.add(new DocumentationNewType(def));
            }
            if (((Definitions) def).getTypes() == null) {
                list.add(new TypesNewType(def));
            }
            list.add(new ImportSchemaNewType(def));
            list.add(new ImportWSDLNewType(def));
            list.add(new MessageNewType(def));
            list.add(new PortTypeNewType(def)); 
            list.add(new BindingNewType(def));
            list.add(new ServiceNewType(def));
            list.addAll(Arrays.asList(new ExtensibilityElementNewTypesFactory(WSDLExtensibilityElements.ELEMENT_DEFINITIONS).getNewTypes(def)));

            return list.toArray(new NewType[list.size()]);
        }        
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(DefinitionsNode.class);
    }

    @Override
    public String getTypeDisplayName() {
        return NbBundle.getMessage(DefinitionsNode.class, "LBL_DefinitionsNode_TypeDisplayName");
    }
    
    
}
