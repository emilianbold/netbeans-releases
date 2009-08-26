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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.xml.wsdl.ui.api.property;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;
import org.netbeans.modules.xml.retriever.catalog.CatalogEntry;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.CategorizedSchemaNodeFactory;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.NodesFactory;
import org.netbeans.modules.xml.wsdl.ui.wsdl.nodes.BuiltInTypeFolderNode;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.ui.customizer.FolderNode;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.FilterNode.Children;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class ElementOrTypeChooserHelper extends ChooserHelper<SchemaComponent> {

    private Node inlineSchemaFolderNode;
    private Node builtinSchemaFolderNode;
    private Node projectsFolderNode;
    private WSDLModel model;
    private List<Class<? extends SchemaComponent>> filters;
    private Project project;

    public ElementOrTypeChooserHelper(WSDLModel model) {
        this.model = model;
    }

    //Called from wizard. the model may not be in the project, if created in temporary location
    public ElementOrTypeChooserHelper(Project project, WSDLModel model) {
        this.model = model;
        this.project = project;
    }

    @Override
    public void populateNodes(Node parentNode) {
        ArrayList<Node> chooserFolders = new ArrayList<Node>();
        filters = new ArrayList<Class<? extends SchemaComponent>>();
        filters.add(GlobalSimpleType.class);
        filters.add(GlobalComplexType.class);
        filters.add(GlobalElement.class);
        Project wsdlProject = null;
        DefaultProjectCatalogSupport catalogSupport = null;
        if (project != null) {
            wsdlProject = project;
            //Fix for IZ 147816
            SubprojectProvider provider = wsdlProject.getLookup().lookup(SubprojectProvider.class);
            if (provider != null) {
                catalogSupport = new DefaultProjectCatalogSupport(wsdlProject);
            }
        } else {
            FileObject wsdlFile = model.getModelSource().getLookup().lookup(FileObject.class);
            if (wsdlFile != null) {
                catalogSupport = DefaultProjectCatalogSupport.getInstance(wsdlFile);
                wsdlProject = FileOwnerQuery.getOwner(wsdlFile);
            }
        }
        if (wsdlProject != null) {
            projectsFolderNode = new FolderNode(new Children.Array());
            projectsFolderNode.setDisplayName(NbBundle.getMessage(ElementOrTypeChooserHelper.class, "LBL_ByFile_DisplayName"));
            LogicalViewProvider viewProvider = wsdlProject.getLookup().lookup(LogicalViewProvider.class);


            ArrayList<Node> nodes = new ArrayList<Node>();
            Node projectNode = new EnabledNode(new SchemaProjectFolderNode(viewProvider.createLogicalView(), wsdlProject, filters));
            nodes.add(projectNode);
            Node catalogNode = getCatalogNode(wsdlProject, filters);
            if (catalogNode != null) {
                projectNode.getChildren().add(new Node[]{catalogNode});
            }


            if (catalogSupport != null) {
                Set refProjects = catalogSupport.getProjectReferences();
                if (refProjects != null && refProjects.size() > 0) {
                    for (Object o : refProjects) {
                        Project refPrj = (Project) o;
                        viewProvider = refPrj.getLookup().lookup(LogicalViewProvider.class);
                        nodes.add(new EnabledNode(new SchemaProjectFolderNode(viewProvider.createLogicalView(), refPrj, filters)));
                    }
                }
            }
            projectsFolderNode.getChildren().add(nodes.toArray(new Node[nodes.size()]));
        }

        if (model != null) {
            Definitions def = model.getDefinitions();
            if (def.getTypes() != null) {
                Collection<Schema> schemas = def.getTypes().getSchemas();
                if (schemas != null && !schemas.isEmpty()) {
                    List<Schema> filteredSchemas = new ArrayList<Schema>();
                    for (Schema schema : schemas) {
                        Collection<SchemaComponent> children = schema.getChildren();
                        for (SchemaComponent comp : children) {
                            boolean isInstance = false;
                            for (Class clazz : filters) {
                                if (clazz.isInstance(comp)) {
                                    isInstance = true;
                                    break;
                                }
                            }
                            if (isInstance) {
                                filteredSchemas.add(schema);
                                break;
                            }
                        }
                    }
                    if (filteredSchemas.size() > 0) {
                        inlineSchemaFolderNode = new InlineTypesFolderNode(NodesFactory.getInstance().create(def.getTypes()), filteredSchemas, filters);
                    }
                }
            }
        }

        builtinSchemaFolderNode = new BuiltInTypeFolderNode();


        if (projectsFolderNode != null) {
            chooserFolders.add(projectsFolderNode);
        }
        if (inlineSchemaFolderNode != null) {
            chooserFolders.add(inlineSchemaFolderNode);
        }

        chooserFolders.add(builtinSchemaFolderNode);

        parentNode.getChildren().add(chooserFolders.toArray(new Node[chooserFolders.size()]));
    }

    @Override
    public Node selectNode(SchemaComponent comp) {
        if (comp == null) {
            return null;
        }
        Node selected = null;
        if (comp != null) {
            String tns = Utility.getTargetNamespace(comp.getModel());
            if (tns != null) {
                if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(tns)) {
                    selected = selectNode(builtinSchemaFolderNode, comp);
                } else {
                    if (inlineSchemaFolderNode == null || (selected = selectNode(inlineSchemaFolderNode, comp)) == null) {
                        selected = projectsFolderNode != null ? selectNode(projectsFolderNode, comp) : null;
                    }
                }
            } else {
                // must be inline.
                if (inlineSchemaFolderNode != null) {
                    selected = selectNode(inlineSchemaFolderNode, comp);
                }
            }
        }
        return selected;
    }

    private Node getCatalogNode(Project wsdlProject, List<Class<? extends SchemaComponent>> filters) {
        CatalogHelper helper = new CatalogHelper(wsdlProject);

        List<CatalogEntry> references = helper.getReferencedResources(SCHEMA_FILE_EXTENSION);
        if (references == null || references.isEmpty()) {
            return null;
        }

        Node node = new CatalogNode(new CatalogChildren(helper, references, filters));
        node.setName(NbBundle.getMessage(ElementOrTypeChooserHelper.class, "LBL_RemoteReferences"));
        return node;
    }

    class CatalogChildren extends Children.Keys<CatalogEntry> {

        List<Class<? extends SchemaComponent>> schemaComponentFilters;
        private List<CatalogEntry> entries;
        private Set<CatalogEntry> emptySet = Collections.emptySet();
        private CatalogHelper helper;

        public CatalogChildren(CatalogHelper helper, List<CatalogEntry> entries, List<Class<? extends SchemaComponent>> filters) {
            this.schemaComponentFilters = filters;
            this.entries = entries;
            this.helper = helper;
        }

        @Override
        public Node[] createNodes(CatalogEntry entry) {
            FileObject fo = helper.getFileObject(entry);
            ModelSource modelSource = Utilities.getModelSource(fo, true);
            if (modelSource != null) {
                SchemaModel sModel =
                        SchemaModelFactory.getDefault().getModel(modelSource);
                if (sModel != null && sModel.getState() == State.VALID) {
                    Schema schema = sModel.getSchema();
                    if (schema != null) {
                        CategorizedSchemaNodeFactory factory =
                                new CategorizedSchemaNodeFactory(
                                sModel, schemaComponentFilters, Lookup.EMPTY);
                        return new Node[]{new FileNode(factory.createNode(schema),
                                    CatalogHelper.getFileName(entry.getSource()))
                                };
                    }
                }
            }
            Node brokenReference = new AbstractNode(LEAF);
            brokenReference.setName(NbBundle.getMessage(
                    ElementOrTypeChooserHelper.class, "LBL_InvalidReference"));
            ;
            return new Node[]{brokenReference};
        }

        @Override
        protected void addNotify() {
            resetKeys();
        }

        @Override
        protected void removeNotify() {
            this.setKeys(emptySet);

        }

        private void resetKeys() {
            ArrayList<CatalogEntry> keys = new ArrayList<CatalogEntry>();
            keys.addAll(entries);
            this.setKeys(keys);
        }
    }

    private Node selectNode(Node parentNode, SchemaComponent element) {
        org.openide.nodes.Children children = parentNode.getChildren();
        for (Node node : children.getNodes()) {
            SchemaComponent sc = null;
            SchemaComponentReference reference = node.getLookup().lookup(SchemaComponentReference.class);
            if (reference != null) {
                sc = reference.get();
            }
            if (sc == null) {
                sc = node.getLookup().lookup(SchemaComponent.class);
            }

            if (sc == element) {
                return node;
            }

            Node node1 = null;
            if ((node1 = selectNode(node, element)) != null) {
                return node1;
            }
        }
        return null;
    }

    class CatalogNode extends FolderNode {

        public CatalogNode(Children.Keys children) {
            super(children);
        }
    }

    class SchemaProjectFolderNode extends FilterNode {

        public SchemaProjectFolderNode(Node original, Project project, List<Class<? extends SchemaComponent>> filters) {
            super(original, new SchemaProjectFolderChildren(original, project, filters));
        }
    }

    class SchemaProjectFolderChildren extends Children.Keys<FileObject> {

        private final FileObject projectDir;
        private final List<Class<? extends SchemaComponent>> schemaComponentFilters;
        private Set<FileObject> emptySet = Collections.emptySet();
        private final Node original;

        public SchemaProjectFolderChildren(Node original, Project project, List<Class<? extends SchemaComponent>> filters) {
            this.schemaComponentFilters = filters;
            this.projectDir = project.getProjectDirectory();
            this.original = original;
        }

        @Override
        public Node[] createNodes(FileObject fo) {
            ModelSource modelSource = org.netbeans.modules.xml.retriever.catalog.Utilities.getModelSource(fo, false);
            SchemaModel sModel = SchemaModelFactory.getDefault().getModel(modelSource);
            if (sModel != null) {
                Schema schema = sModel.getSchema();
                if (schema != null) {
                    CategorizedSchemaNodeFactory factory =
                            new CategorizedSchemaNodeFactory(
                            sModel, schemaComponentFilters, Lookup.EMPTY);
                    return new Node[]{new FileNode(factory.createNode(schema),
                                FileUtil.getRelativePath(projectDir, fo))};
                }
            }
            //
            return null;
        }

        @Override
        protected void addNotify() {
            resetKeys();
        }

        @Override
        protected void removeNotify() {
            this.setKeys(emptySet);

        }

        private void resetKeys() {
            ArrayList<FileObject> keys = new ArrayList<FileObject>();

            Set<FileObject> validFolders = new HashSet<FileObject>();
            populateValidFolders(original, validFolders);
            Set<File> alreadyAddedFiles = new HashSet<File>();
            for (FileObject rootFolder : validFolders) {
                List<File> files = recursiveListFiles(FileUtil.toFile(rootFolder), new SchemaFileFilter());
                for (File file : files) {
                    if (alreadyAddedFiles.contains(file)) continue;
                    FileObject fileobj = FileUtil.toFileObject(file);
                    ModelSource mSource = Utilities.getModelSource(fileobj, false);
                    if (mSource != null) {
                        SchemaModel sModel = SchemaModelFactory.getDefault().getModel(mSource);
                        if (sModel != null && Utility.getTargetNamespace(sModel) != null) {
                            alreadyAddedFiles.add(file);
                            keys.add(fileobj);
                        }
                    }
                }
            }
            this.setKeys(keys);
        }

    }
    public static final String SCHEMA_FILE_EXTENSION = "xsd";

    static class SchemaFileFilter implements FileFilter {

        public boolean accept(File pathname) {
            boolean result = false;
            String fileName = pathname.getName();
            String fileExtension = null;
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex != -1) {
                fileExtension = fileName.substring(dotIndex + 1);
            }

            if (fileExtension != null && (fileExtension.equalsIgnoreCase(SCHEMA_FILE_EXTENSION))) {
                result = true;
            }

            return result;
        }
    }

    static class InlineTypesFolderNode extends FilterNode {

        private Collection<Schema> mSchemas;
        private List<Class<? extends SchemaComponent>> filters;

        public InlineTypesFolderNode(Node node, Collection<Schema> schemas, List<Class<? extends SchemaComponent>> filters) {
            super(node);
            mSchemas = schemas;
            this.filters = filters;
            setDisplayName(NbBundle.getMessage(ElementOrTypeChooserHelper.class, "INLINE_SCHEMATYPE_NAME"));
            setChildren(new TypesChildren());
        }

        class TypesChildren extends Children.Keys<Schema> {

            Set<Schema> set = Collections.emptySet();

            public TypesChildren() {
            }

            @Override
            protected Node[] createNodes(Schema key) {
                CategorizedSchemaNodeFactory factory = new CategorizedSchemaNodeFactory(
                        key.getModel(), filters, Lookup.EMPTY);
                Node node = factory.createNode(key);
                return new Node[]{node};

            }

            @Override
            protected void addNotify() {
                resetKeys();
            }

            @Override
            protected void removeNotify() {
                this.setKeys(set);

            }

            private void resetKeys() {
                this.setKeys(mSchemas);
            }
        }
    }
}
