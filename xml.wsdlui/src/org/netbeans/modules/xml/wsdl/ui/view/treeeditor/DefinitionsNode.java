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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.netbeans.modules.xml.wsdl.ui.common.Constants;
import org.netbeans.modules.xml.wsdl.ui.cookies.WSDLDefinitionNodeCookie;
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
import org.openide.ErrorManager;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.NewAction;
import org.openide.actions.PasteAction;
import org.openide.actions.PropertiesAction;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;
import org.openide.windows.TopComponent;

import com.sun.org.apache.xml.internal.utils.XMLChar;

/**
 *
 * @author Ritesh Adval
 *
 * 
 */
public class DefinitionsNode extends WSDLExtensibilityElementNode {
    
    
    private Image ICON = Utilities.loadImage
    ("org/netbeans/modules/xml/wsdl/ui/netbeans/module/resources/wsdl16.png");
    
    private static final String TARGETNAMESPACE_PROP = "targetNamespace";//NOI18N 
    
    private DefinitionsPropertyAdapter mPropertyAdapter;
    
    private ExplorerManager mManager;
    
    private TopComponent mTopComponent;

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

    public DefinitionsNode(Definitions mWSDLDef, 
            ExplorerManager manager,
            TopComponent topComponent) {
        this(mWSDLDef);
        this.mManager = manager;
        this.mTopComponent = topComponent;
        
    }
    
    //Do not allow to create a definition node, with any children other than DefinitionChildren.
    private DefinitionsNode(Definitions wsdlDef, Children children) {
        super(children, wsdlDef, new DefinitionsNewTypesFactory());
        
        getLookupContents().add( new WSDLDefinitionNodeCookie(this));
        
        this.mPropertyAdapter = new DefinitionsPropertyAdapter();
        super.setNamedPropertyAdapter(this.mPropertyAdapter);
    }
    
    public DefinitionsNode(Definitions mWSDLDef) {
        this(mWSDLDef, new DefinitionsChildren(mWSDLDef));
    }

    public DefinitionsNode(Definitions mWSDLDef, List<Class<? extends WSDLComponent>> filters) {
        this(mWSDLDef, new DefinitionsChildren(mWSDLDef, filters));
    }
    
    public ExplorerManager getExplorerManager() {
        return mManager;
    }
    
    public TopComponent getTopComponent() {
        return this.mTopComponent;
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
    
    protected void updateDisplayName() {
        Definitions defs = (Definitions) getWSDLComponent();
        String name = defs.getTargetNamespace();
        if (name == null) {
            name = NbBundle.getMessage(DefinitionsNode.class,
                    "LBL_DefinitionsNode_NoTargetNamespace");
        }
        setDisplayName(name);
    }

    @Override
    protected void refreshAttributesSheetSet() {
        Sheet.Set ss = createPropertiesSheetSet();
        
        try {
            //name
            Node.Property nameProperty = new BaseAttributeProperty(mPropertyAdapter, 
                    String.class, NAME_PROP);
            nameProperty.setName(NbBundle.getMessage(DefinitionsNode.class, "PROP_NAME_DISPLAYNAME"));
            nameProperty.setShortDescription(NbBundle.getMessage(DefinitionsNode.class, "DEFINITIONSNODE_NAME_DESCRIPTION"));
            ss.put(nameProperty);
            
            //targetNamespace
            Node.Property targetNamespaceProperty = new BaseAttributeProperty(mPropertyAdapter, 
                    String.class, TARGETNAMESPACE_PROP);
            targetNamespaceProperty.setName(NbBundle.getMessage(DefinitionsNode.class, "PROP_TARGET_NAMESPACE_DISPLAYNAME"));
            targetNamespaceProperty.setShortDescription(NbBundle.getMessage(DefinitionsNode.class, "DEFINITIONSNODE_TARGETNAMESPACE_DESCRIPTION"));
            ss.put(targetNamespaceProperty);
            
            
            //default namespace
            Node.Property defaultNamespaceProperty = new BaseAttributeProperty(mPropertyAdapter, 
                    String.class,"defaultNamespace"); //NOI18N
            
            defaultNamespaceProperty.setName(NbBundle.getMessage(DefinitionsNode.class, "PROP_DEFAULT_NAMESPACE"));//NOI18N
            defaultNamespaceProperty.setShortDescription(NbBundle.getMessage(DefinitionsNode.class, "DEFINITIONS_NODE_DEFAULTNAMESPACE_DESC"));
            ss.put(defaultNamespaceProperty);
            
            //add prefixes sheet
            refreshPrefixesSheetSet(Utility.getNamespaces((Definitions) getWSDLComponent()));
            
        } catch(Exception ex) {
            mLogger.log(Level.SEVERE, "failed to create property sheet for "+ getWSDLComponent(), ex);
        }
    }
    
    private void refreshPrefixesSheetSet(Map prefixToNamespaceMap) throws Exception {
        Sheet.Set prefixesSheetSet = createPrefixesSheetSet(prefixToNamespaceMap, (Definitions) getWSDLComponent());
        //remove default namespace prefix since it is also added in properties 
        prefixesSheetSet.remove(NbBundle.getMessage(DefinitionsNode.class, "PROP_DEFAULT_NAMESPACE"));
        
        mSheet.remove(NbBundle.getMessage(DefinitionsNode.class, "PROP_SHEET_CATEGORY_PREFIXES"));
        mSheet.put(prefixesSheetSet);
    }
    public static Sheet.Set createPrefixesSheetSet(final Map prefixToNamespaceMap, Definitions definitions) throws Exception {
        
        Sheet.Set prefixesSheetSet = new Sheet.Set();
        String prefixesSheetName = NbBundle.getMessage(DefinitionsNode.class, "PROP_SHEET_CATEGORY_PREFIXES");
        prefixesSheetSet.setName(prefixesSheetName);
        prefixesSheetSet.setDisplayName(prefixesSheetName);
        
        Iterator it = prefixToNamespaceMap.keySet().iterator();
        while(it.hasNext()) {
            final String prefix = (String) it.next();
            PropertyAdapter pn = null;
            Node.Property prefixToNamespaceProperty = null;
            
            
            pn = new PrefixToNamespace(prefix, definitions);
            
            
            
            prefixToNamespaceProperty = new BaseAttributeProperty(pn, String.class, "namespace"); //NOI18N
            if(prefix.equals(XMLConstants.XMLNS_ATTRIBUTE) || prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                //prefixToNamespaceProperty.setName(XMLConstants.XMLNS_ATTRIBUTE);
            } else {
                prefixToNamespaceProperty.setName(prefix);
                prefixToNamespaceProperty.setShortDescription(NbBundle.getMessage(DefinitionsNode.class, "DEFINITIONSNODE_USER_DEF_PREFIX_DESC"));
                prefixesSheetSet.put(prefixToNamespaceProperty);
            }

        }
        
        return prefixesSheetSet;
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
        
        public void setPrefix(String prefix) {
            
            String namespace = getNamespace();
            mWSDLConstruct.getModel().startTransaction();
            ((AbstractDocumentComponent) mWSDLConstruct).removePrefix(mPrefix);
            ((AbstractDocumentComponent) mWSDLConstruct).addPrefix(prefix, namespace);
                mWSDLConstruct.getModel().endTransaction();
            mPrefix = prefix;
        }
        
        public void setNamespace(String namespace) {
            mWSDLConstruct.getModel().startTransaction();
            ((AbstractDocumentComponent) mWSDLConstruct).addPrefix(mPrefix, namespace);
                mWSDLConstruct.getModel().endTransaction();
        }
        
        public String getNamespace() {
            return Utility.getNamespaceURI(this.mPrefix, mWSDLConstruct);
        }    
    }
    public static final class DefinitionsChildren extends GenericWSDLComponentChildren {
        
        List<Class<? extends WSDLComponent>> filters;
        public DefinitionsChildren(Definitions definitions) {
            super(definitions);
        }
        
        //Hack for creating children with only specific categories
        /**
         * Only top level filters are supported.
         * Message, Import, Types, Documentation, PortType, Binding , Service and ExtensibilityElement are supported.
         * If filters are specified, then only those folders which support that top level component are created.
         * 
         */
        public DefinitionsChildren(Definitions definitions, List<Class<? extends WSDLComponent>> filters) {
            super(definitions);
            this.filters = filters;
        }
        
        @Override
        protected Node[] createNodes(Object key) {
            Node node = null;
           
            if (IMPORTS_FOLDER.equals(key)) {
                node = new ImportFolderNode((Definitions) getWSDLComponent());
            }
            if (MESSAGES_FOLDER.equals(key)) {
                node = new MessageFolderNode((Definitions) getWSDLComponent());
            }
            if (PORTTYPES_FOLDER.equals(key)) {
                node = new PortTypeFolderNode((Definitions) getWSDLComponent());
            }
            if (BINDING_FOLDER.equals(key)) {
                node = new BindingFolderNode((Definitions) getWSDLComponent());
            }
            if (SERVICES_FOLDER.equals(key)) {
                node = new ServiceFolderNode((Definitions) getWSDLComponent());
            }
            
            if (EXTENSIBILITY_ELEMENTS_FOLDER.equals(key)) {
                node = new ExtensibilityElementsFolderNode((Definitions) getWSDLComponent());
            }
            
            
            if(node != null) {
                return new Node[] {node};
            }
            return super.createNodes(key);
        }
        
        @Override
        protected Collection getKeys() {
            Collection<Object> keys = new ArrayList<Object>();
            
            
            Definitions def = ((Definitions) getWSDLComponent());
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
            
            if (name != null && !XMLChar.isValidNCName(name)) {
                ErrorManager.getDefault().notify(new Exception(NbBundle.getMessage(DefinitionsNode.class, "ERR_MSG_INVALID_NMTOKEN")));
                return;
            }
            getWSDLComponent().getModel().startTransaction();
            ((Definitions) getWSDLComponent()).setName(name);
            getWSDLComponent().getModel().endTransaction();
        }
        
        public String getName() {
            String name = ((Definitions)getWSDLComponent()).getName();
            if(name == null) {
                return "";
            }
            
            return name;
        }
        
        public void setTargetNamespace(String targetNamespace) {
            getWSDLComponent().getModel().startTransaction();
            ((Definitions) getWSDLComponent()).setTargetNamespace(targetNamespace);
                getWSDLComponent().getModel().endTransaction();
        }
        
        public String getTargetNamespace() {
            String tns = ((Definitions)getWSDLComponent()).getTargetNamespace();
            if(tns == null) {
                return "";
            }
            
            return tns;
        }
        
        public void setDefaultNamespace(String defaultNamespace) {
            WSDLModel model = getWSDLComponent().getModel();
            model.startTransaction();
            ((AbstractDocumentComponent)getWSDLComponent()).addPrefix(XMLConstants.DEFAULT_NS_PREFIX, defaultNamespace);//getPrefixes().put("", defaultNamespace);
                model.endTransaction();
        }
        
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
