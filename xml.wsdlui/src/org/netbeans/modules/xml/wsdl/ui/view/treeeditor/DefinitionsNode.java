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
import java.util.Map;
import java.util.logging.Level;
import javax.swing.Action;
import javax.xml.XMLConstants;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Documentation;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.api.property.PropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.commands.NamedPropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.common.Constants;
import org.netbeans.modules.xml.wsdl.ui.cookies.WSDLDefinitionNodeCookie;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElements;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.BindingNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.DocumentationNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.ExtensibilityElementNewTypesFactory;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.MessageNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.NewTypesFactory;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.PortTypeNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.ServiceNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.TypesNewType;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.openide.ErrorManager;
import org.openide.actions.NewAction;
import org.openide.actions.PropertiesAction;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;
import org.openide.windows.TopComponent;
import com.sun.org.apache.xml.internal.utils.XMLChar;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.xml.wsdl.ui.view.property.BaseAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.ImportSchemaNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.ImportWSDLNewType;
import org.netbeans.modules.xml.xam.ui.actions.GoToAction;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.PasteAction;

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
    
    public DefinitionsNode(Definitions mWSDLDef) {
        super(new DefinitionsChildren(mWSDLDef), mWSDLDef, new DefinitionsNewTypesFactory());
       
        getLookupContents().add( new WSDLDefinitionNodeCookie(this));
        
        this.mPropertyAdapter = new DefinitionsPropertyAdapter();
        super.setNamedPropertyAdapter(this.mPropertyAdapter);
        
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
            if(defaultNamespaceProperty.getValue() == null) {
                defaultNamespaceProperty.setValue(Constants.WSDL_DEFAUL_NAMESPACE);
            }
            
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
        
        public DefinitionsChildren(Definitions definitions) {
            super(definitions);
            //specialTargetNamespacesSet.addAll(BPELExtensibilityElementsFolderNode.getSupportedExtensionNamespaceSet());
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
            Documentation doc = def.getDocumentation();
            if (doc != null) {
                keys.add(doc);
            }
            Types types = def.getTypes();
            if (types != null) {
                keys.add(types);
            }
/*            Collection imports = def.getImports();
            if (imports != null && imports.size() > 0) {*/
                keys.add(IMPORTS_FOLDER);
//            }
            keys.add(MESSAGES_FOLDER);
            keys.add(PORTTYPES_FOLDER);
            keys.add(BINDING_FOLDER);
            keys.add(SERVICES_FOLDER);
            
            
            keys.add(EXTENSIBILITY_ELEMENTS_FOLDER);

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
            return (String) ((AbstractDocumentComponent) getWSDLComponent()).getPrefixes().get(XMLConstants.DEFAULT_NS_PREFIX);
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
