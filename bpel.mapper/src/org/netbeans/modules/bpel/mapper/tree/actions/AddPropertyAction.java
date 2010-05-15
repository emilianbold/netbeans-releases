/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bpel.mapper.tree.actions;

import org.netbeans.modules.soa.xpath.mapper.tree.actions.MapperAction;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import javax.swing.tree.TreePath;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.mapper.model.MapperTcContext;
import org.netbeans.modules.bpel.mapper.properties.AddEditPropertyPanel;
import org.netbeans.modules.bpel.mapper.properties.ImportRegistrationHelper;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.ext.Extensions;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperTreeNode;
import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELQName;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Query;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.wsdl.util.RelativePath;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class AddPropertyAction extends MapperAction<TreeItem> {
    
    private static final long serialVersionUID = 1L;
    private TreePath treePath;
    
    public AddPropertyAction(MapperTcContext mapperTcContext,
            boolean inLeftTree, TreePath treePath,
            TreeItem treeItem) 
    {
        super(mapperTcContext, treeItem, inLeftTree);
        this.treePath = treePath;
        postInit();
        // putValue(DeleteAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(AddPropertyAction.class,
                "ADD_NM_PROPERTY"); // NOI18N
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        TreeItem treeItem = getActionSubject();

        MapperTcContext tcContext = MapperTcContext.class.cast(getSContext());
        Lookup lookup = tcContext.getTopComponent().getLookup();

        ExtensibleElements variable = findVariableDeclaration();
        
        if (variable == null) {
            return;
        }
        
        Message message = null;
        
        if (variable instanceof VariableDeclaration) {
            WSDLReference<Message> ref = ((VariableDeclaration) variable)
                    .getMessageType();
            message = (ref != null) ? ref.get() : null;
        }
        
        if (message == null) {
            return;
        }
        
        BpelModel bpelModel = variable.getBpelModel();
        bpelModel.getProcess().getNamespaceContext();
        
        TreeItem item = treeItem.getParent();
        while (item != null) {
            if (item.getDataObject() instanceof VariableDeclaration) {
                VariableDeclaration variableDeclaration 
                        = (VariableDeclaration) item.getDataObject();
                WSDLReference<Message> messageRef = variableDeclaration
                        .getMessageType();
                if (messageRef != null) {
                    message = messageRef.get();
                    break;
                }
            }
            item = item.getParent();
        }
        
        AddEditPropertyPanel addEditPanel = new AddEditPropertyPanel(bpelModel, 
                message, lookup, getPrefixesMap(bpelModel));
        
        DialogDescriptor dialogDescriptor = new DialogDescriptor(addEditPanel, 
                NbBundle.getMessage(AddPropertyAction.class,
                "ADD_NM_PROPERTY_DIALOG_TITLE")); // NOI18N
        
        addEditPanel.setDialogDescriptor(dialogDescriptor);
        
        DialogDisplayer.getDefault().createDialog(dialogDescriptor)
                .setVisible(true);

        addEditPanel.setDialogDescriptor(null);
        
        if (dialogDescriptor.getValue() != DialogDescriptor.OK_OPTION) {
            return;
        }
        
        WSDLModel wsdlModel = getWSDLModel(bpelModel, true);
        if (wsdlModel.startTransaction()) {
            try {
                WSDLComponentFactory wsdlFactory = wsdlModel.getFactory();
                Definitions definitions = wsdlModel.getDefinitions();

                Map<String, String> prefixes = addEditPanel
                        .getCreatedPrefixes();
                if (prefixes != null && !prefixes.isEmpty()) {
                    for (Map.Entry<String, String> entry : prefixes.entrySet()) 
                    {
                        ((AbstractDocumentComponent) definitions).addPrefix(
                                entry.getKey(), entry.getValue());
                    }
                }
                
                importWsdlIntoWsdl(wsdlModel, message.getModel());
                importRequiredSchemas(wsdlModel, addEditPanel
                        .getSchemaComponentsToImport());

                CorrelationProperty correlationProperty = (CorrelationProperty) 
                        wsdlFactory.create(definitions, BPELQName
                        .PROPERTY.getQName());
                correlationProperty.setName(addEditPanel.getPropertyName());

                GlobalElement globalElement = addEditPanel.getPropertyElemenet();
                GlobalType globalType = addEditPanel.getPropertyType();

                if (globalType != null) {
                    NamedComponentReference<GlobalType> ref = globalType
                            .createReferenceTo(globalType, GlobalType.class);
                    correlationProperty.setType(ref);
                } else if (globalElement != null) {
                    NamedComponentReference<GlobalElement> ref = globalElement
                            .createReferenceTo(globalElement, GlobalElement
                            .class);
                    correlationProperty.setElement(ref);
                }

                definitions.addExtensibilityElement(correlationProperty);
                
                NamedComponentReference<CorrelationProperty> propertyRef 
                        = correlationProperty.createReferenceTo(
                                correlationProperty, 
                                CorrelationProperty.class);
                
                PropertyAlias propertyAlias = (PropertyAlias) wsdlFactory
                        .create(definitions, BPELQName.PROPERTY_ALIAS
                        .getQName());
                if (addEditPanel.isAssociatePropertyWithMessage()) {
                    NamedComponentReference<Message> messageRef = message
                            .createReferenceTo(message, Message.class);
                    propertyAlias.setMessageType(messageRef);
                    
                    // Ugly hack. Add fake part
                    Collection<Part> partCollection = message.getParts();
                    if (partCollection != null) {
                        for (Part part : partCollection) {
                            String partName = part.getName();
                            if (partName != null && partName.length() > 0) {
                                propertyAlias.setPart(partName);
                                break;
                            }
                        }
                    }
                } else {
                    // Ugly hack. Add fake type
                    SchemaModel primitivesModel = SchemaModelFactory
                            .getDefault().getPrimitiveTypesModel();
                    Collection<GlobalSimpleType> simpleTypes = primitivesModel
                            .getSchema().getSimpleTypes();
                    if (simpleTypes != null) {
                        for (GlobalSimpleType type : simpleTypes) {
                            NamedComponentReference<GlobalType> typeRef = type
                                    .createReferenceTo(type, GlobalType.class);
                            propertyAlias.setType(typeRef);
                            break;
                        }
                    }
                }
                
                propertyAlias.setPropertyName(propertyRef);
                propertyAlias.setAnyAttribute(new QName(Extensions
                        .NM_PROPERTY_EXT_URI, "nmProperty", "nmp"), addEditPanel
                        .getNMProperty());

                String queryString = addEditPanel.getQuery();
                if (queryString != null) {
                    Query query = (Query) wsdlFactory.create(definitions, 
                            BPELQName.QUERY.getQName());
                    query.setContent(queryString);
                    propertyAlias.setQuery(query);
                }
                
                definitions.addExtensibilityElement(propertyAlias);
            } finally {
                wsdlModel.endTransaction();
            }
            
            ImportRegistrationHelper helper 
                    = new ImportRegistrationHelper(bpelModel);
            helper.addImport(wsdlModel);
        }
        
        getSContext().getDesignContextController().reloadMapper();
    }
    
    private Map<String, String> getPrefixesMap(BpelModel bpelModel) {
        WSDLModel wsdlModel = getWSDLModel(bpelModel, true);
        Definitions definitions = wsdlModel.getDefinitions();
        return ((AbstractDocumentComponent) definitions).getPrefixes();
    }
    
    private WSDLModel getWSDLModel(BpelModel bpelModel, boolean create) {
        ModelSource bpelModelSource = bpelModel.getModelSource();
        Lookup lookup = bpelModelSource.getLookup();
        
        FileObject bpelFile = lookup.lookup(FileObject.class);
        FileObject bpelFolder = bpelFile.getParent();

        FileObject wsdlFile = bpelFolder.getFileObject(NM_PROPERTIES_FILE_NAME, 
                "wsdl"); // NOI18N
        
        boolean init = false;
        
        if ((wsdlFile == null) && create) {
            try {
                wsdlFile = FileUtil.copyFile(Repository.getDefault()
                        .getDefaultFileSystem()
                        .findResource("wsdl/wsdl.wsdl"), bpelFolder, 
                                NM_PROPERTIES_FILE_NAME);
                init = true;
            } catch (IOException ex) {
            
            }
        }
        
        if (wsdlFile == null) {
            return null;
        }
        
        ModelSource wsdlModelSource = Utilities.getModelSource(wsdlFile, 
                wsdlFile.canWrite());
        WSDLModel wsdlModel = WSDLModelFactory.getDefault()
                .getModel(wsdlModelSource);
        
        if (init) {
            Process process = bpelModel.getProcess();
            String ns = null;
            if (process != null) {
                ns = process.getTargetNamespace();
                if (ns != null && ns.length() > 0) {
                    if (!ns.endsWith("/")) { // NOI18N
                        ns = ns + "/"; // NOI18N
                    }
                    ns = ns + "nmPropertiesDefinitions"; // NOI18N
                } else {
                    ns = null;
                }
            }
            
            if (ns == null) {
                ns = DEF_WSDL_TARGET_NS;
            }
            
            if (wsdlModel.startTransaction()) {
                try {
                    Definitions definitions = wsdlModel.getDefinitions();
                    definitions.setTargetNamespace(ns);
                    ((AbstractDocumentComponent) definitions)
                            .addPrefix("sxnmp", Extensions.NM_PROPERTY_EXT_URI);
                } finally {
                    wsdlModel.endTransaction();
                }
            } 
        }
        
        return wsdlModel;
    }
    
    private ExtensibleElements findVariableDeclaration() {
        TreePath path = treePath;
        while (path != null) {
            Object node = path.getLastPathComponent();
            if (node instanceof MapperTreeNode) {
                Object dataObject = ((MapperTreeNode) node).getDataObject();
                if (dataObject instanceof ExtensibleElements) {
                    return (ExtensibleElements) dataObject;
                }
            }
            path = path.getParentPath();
        }
        return null;
    }
    
    public static final String NM_PROPERTIES_FILE_NAME 
            = "nmPropertiesDefinitions"; // NOI18N
    
    public static void importWsdlIntoWsdl(final WSDLModel baseWsdlModel, 
            final WSDLModel importedWsdlModel) 
    {
        Import objImport = baseWsdlModel.getFactory().createImport();

        FileObject 
            baseFileObj = baseWsdlModel.getModelSource().getLookup().lookup(FileObject.class),
            importedFileObj = importedWsdlModel.getModelSource().getLookup().lookup(FileObject.class);
        String importRelativePath = getRelativePath(baseFileObj, importedFileObj);

        objImport.setNamespace(importedWsdlModel.getDefinitions().getTargetNamespace());
        objImport.setLocation(importRelativePath);

        if (!wsdlContainsImport(baseWsdlModel, objImport)) {
            baseWsdlModel.getDefinitions().addImport(objImport);
        }
    }
    
    public static void importRequiredSchemas(WSDLModel wsdlModel, 
        final List<SchemaComponent> schemaComponents) 
    {
        for (SchemaComponent schemaComponent : schemaComponents) {
            Utility.addSchemaImport(schemaComponent.getModel(), wsdlModel);
        }
    }    
    
    public static boolean wsdlContainsImport(WSDLModel baseWsdlModel, 
        Import checkedImport) 
    {
        Collection<Import> imports = 
            baseWsdlModel.getDefinitions().getImports();
        for (Import existingImport : imports) {
            if ((existingImport.getNamespace().equals(checkedImport.getNamespace())) &&
                (existingImport.getLocation().equals(checkedImport.getLocation()))) {
                return true;
            }
        }
        return false;
    }
    
    public static String getRelativePath(FileObject baseFileObj, 
        FileObject relatedFileObj) {
        if ((baseFileObj == null) || (relatedFileObj == null)) {
            throw new NullPointerException(baseFileObj == null ? 
                "Base file object is null" : "Related file object is null");
        }
        // both files are located in the same folder
        String relativePath = relatedFileObj.getNameExt();
        
        URI baseFileURI = FileUtil.toFile(baseFileObj).toURI(),
            relatedFileURI = FileUtil.toFile(relatedFileObj).toURI();
        if (! (relatedFileURI.equals(baseFileURI))) {
            DefaultProjectCatalogSupport catalogSupport 
                    = DefaultProjectCatalogSupport.getInstance(baseFileObj);
            if (catalogSupport.needsCatalogEntry(baseFileObj, relatedFileObj)) {
                try { // remove a previous catalog entry, then create a new one
                    URI uri = catalogSupport.getReferenceURI(baseFileObj, relatedFileObj);
                    catalogSupport.removeCatalogEntry(uri);
                    catalogSupport.createCatalogEntry(baseFileObj, relatedFileObj);
                    relativePath = catalogSupport.getReferenceURI(baseFileObj, 
                        relatedFileObj).toString();
                } catch (Exception e) {
                    ErrorManager.getDefault().notify(e);
                }
            } else {
                relativePath = RelativePath.getRelativePath(FileUtil.toFile(
                    baseFileObj).getParentFile(), FileUtil.toFile(relatedFileObj));
            }
        }
        return relativePath;
    }
    
    public static final String DEF_WSDL_TARGET_NS = "http://www.sun.com/wsbpel/" +
            "2.0/process/executable/SUNExtension/NMPDefinitions"; // NOI18N
}
