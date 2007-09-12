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
package org.netbeans.modules.bpel.nodes.children;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.bpel.core.helper.api.BusinessProcessHelper;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.properties.editors.controls.filter.ChildTypeFilter;
import org.netbeans.modules.bpel.nodes.SchemaFileNode;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.nodes.ImportSchemaNode;
import org.netbeans.modules.bpel.nodes.ReloadableChildren;
import org.netbeans.modules.bpel.properties.Util;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Supports the list of Schema files and Schema Import nodes of the Process
 *
 * @author nk160297
 */
public class SchemaImportsChildren extends Children.Keys
        implements ReloadableChildren {

    private Lookup myLookup;
    private Process myKey;
    
    public SchemaImportsChildren(Process process, Lookup lookup) {
        myLookup = lookup;
        myKey = process;
        setKeys(new Object[] {process});
    }
    
    protected Node[] createNodes(Object key) {
        if (!(key instanceof Process)) {
            return null;
        }
        Process process = (Process)key;
        //
        NodeFactory nodeFactory =
                (NodeFactory)myLookup.lookup(NodeFactory.class);
        BpelModel bpelModel =
                (BpelModel)myLookup.lookup(BpelModel.class);
        FileObject bpelFo = (FileObject)bpelModel.getModelSource().
                getLookup().lookup(FileObject.class);
        FileObject bpelFolderFo = bpelFo.getParent();
        //
        BpelNode.NodeTypeComparator comparator =
                new BpelNode.NodeTypeComparator(
                ImportSchemaNode.class, SchemaFileNode.class);
        //
        ArrayList<Node> nodesList = new ArrayList<Node>();
        ArrayList<FileObject> importedFiles = new ArrayList<FileObject>();
        //
        // Create Schema imports nodes
        Import[] importsArr = process.getImports();
        for (Import importObj : importsArr) {
            String importType = importObj.getImportType();
            if (Import.SCHEMA_IMPORT_TYPE.equals(importType)) {
                String location = importObj.getLocation();
                if (location != null) {
                    FileObject importFo = 
                            Util.getRelativeFO(bpelFolderFo, location);
                    if (importFo != null) {
                        // Collect imported Schema files to exclude them later
                        importedFiles.add(importFo);
                    }
                }
                //
                Node newSchemaImportNode = nodeFactory.createNode(
                        NodeType.IMPORT_SCHEMA, importObj, myLookup);
                nodesList.add(newSchemaImportNode);
            }
        }
        //
        // Check if it is necessary to show not imported Schema files
        boolean showSchemaFiles = true;
        ChildTypeFilter filter =
                (ChildTypeFilter)myLookup.lookup(ChildTypeFilter.class);
        Node parentNode = getNode();
        if (filter != null && parentNode != null && parentNode instanceof BpelNode) {
            NodeType parentNodeType = ((BpelNode)parentNode).getNodeType();
            showSchemaFiles = filter.isPairAllowed(
                    parentNodeType, NodeType.SCHEMA_FILE);
        }
        //
        // Create Schema file nodes
        if (showSchemaFiles) {
            BusinessProcessHelper bpHelper = (BusinessProcessHelper)myLookup.
                    lookup(BusinessProcessHelper.class);
            Collection<FileObject> schemaFiles = bpHelper.getSchemaFilesInProject();
            for (FileObject schemaFile : schemaFiles) {
                if (importedFiles.contains(schemaFile)) {
                    // Skip imported Schema files
                    continue;
                }
                //
                String extension = schemaFile.getExt();
                {
                    ModelSource modelSource = Utilities.getModelSource(
                            schemaFile, true);
                    if (modelSource != null) {
                        SchemaModel schemaModel = SchemaModelFactory.getDefault().
                                getModel(modelSource);
                        if (schemaModel != null) {
                            Node newNode = nodeFactory.createNode(
                                    NodeType.SCHEMA_FILE, schemaModel, myLookup);
                            if (newNode != null) {
                                nodesList.add(newNode);
                            }
                        }
                    }
                }
            }
        }
        //
        Collections.sort(nodesList, comparator);
        Node[] nodes = nodesList.toArray(new Node[nodesList.size()]);
        return nodes;
    }
    
    public void reload() {
        setKeys(new Object[] {new Object()});
        setKeys(new Object[] {myKey});
        // refreshKey(myKey); // this method invoke exception :-( 
    }
}
