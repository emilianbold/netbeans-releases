/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.compapp.projects.common.ui;

import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.compapp.projects.common.CatalogWSDL;
import org.netbeans.modules.xml.retriever.catalog.Utilities.DocumentTypesEnum;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceDecorator;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceNode;
import org.netbeans.modules.xml.xam.ui.customizer.FolderNode;
import org.netbeans.modules.xml.xam.ui.customizer.WaitNode;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.Model;

/**
 * Represents the collection of files belonging to a common namespace.
 * Modified from the original xam ui to allow showing both wsdl and xsd files
 * and namespaces.
 * @author xam ui(orig)
 * @author chikkala
 */
public class ImplicitNamespaceChildren extends Children.Keys {
    /** Map of namespace to a list of files in that namespace. */
    private HashMap<String, List<FileObject>> nsFilesMap;
    /** Set of folders containing referencable files. */
    private FileObject[] rootFolders;
    /** Controls the appearance of child nodes. */
    private ExternalReferenceDecorator decorator;

    /**
     * Creates a new instance of NamespaceChildren.
     *
     * @param  roots      set of root folders.
     * @param  decorator  used to decorate the nodes.
     */
    public ImplicitNamespaceChildren(FileObject[] roots,
            ExternalReferenceDecorator decorator) {
        super();
        rootFolders = roots;
        this.decorator = decorator;
        nsFilesMap = new HashMap<String, List<FileObject>>();
    }

    protected Node[] createNodes(Object key) {
        if (key == WaitNode.WAIT_KEY) {
            return WaitNode.createNode();
        } else if (key instanceof String) {
            List<FileObject> fobjs = nsFilesMap.get(key);
            if (fobjs != null && !fobjs.isEmpty()) {
                Node[] filterNodes = new Node[fobjs.size()];
                int i = 0;
                for (FileObject fobj:fobjs) {
                    try {
                        Node node = DataObject.find(fobj).getNodeDelegate();
                        filterNodes[i++] = decorator.createExternalReferenceNode(node);
                    } catch (DataObjectNotFoundException donfe) {
                    }
                }
                Children.Array children = new Children.Array();
                children.add(filterNodes);
                Node node = new NamespaceNode(children, (String) key, decorator);
                return new Node[] { node };
            }
        }
        return new Node[] { };
    }

    @Override
    protected void addNotify() {
        super.addNotify();
        setKeys(WaitNode.getKeys());
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                for (FileObject root : rootFolders) {
                    //CHIK
                    DocumentTypesEnum docType = decorator.getDocumentType();
                    java.util.Map<FileObject, String> map = new HashMap<FileObject, String>();
                    File rootFile = FileUtil.toFile(root);
                    if ( docType != null ) {
                        map = Utilities.getFiles2NSMappingInProj(rootFile, docType);
                    } else {
                        java.util.Map<FileObject, String> wsdlMap = 
                                Utilities.getFiles2NSMappingInProj(
                                    rootFile, DocumentTypesEnum.wsdl);
                        java.util.Map<FileObject, String> xsdMap = 
                                Utilities.getFiles2NSMappingInProj(
                                    rootFile, DocumentTypesEnum.schema);
                        for (java.util.Map.Entry<FileObject, String> entry : wsdlMap.entrySet()) {
                            String entryNS = entry.getValue();
                            if (CatalogWSDL.TNS.equals(entryNS)) {
                                continue;
                            }
                            map.put(entry.getKey(), entry.getValue());
                            
                        }
                        for (java.util.Map.Entry<FileObject, String> entry : xsdMap.entrySet()) {
                            String entryNS = entry.getValue();
                            if (CatalogWSDL.TNS.equals(entryNS)) {
                                continue;
                            }                            
                            map.put(entry.getKey(), entry.getValue());
                        }                        
                    }
                    
                    for (java.util.Map.Entry<FileObject, String> entry : map.entrySet()) {
                        String ns = entry.getValue();
                        List<FileObject> fobjs = nsFilesMap.get(ns);
                        if (fobjs == null) {
                            fobjs = new ArrayList<FileObject>();
                        }
                        fobjs.add(entry.getKey());
                        nsFilesMap.put(ns, fobjs);
                    }
                }
                // Set the keys on the EDT to avoid clobbering the JTree
                // and causing an AIOOBE (issue 94498).
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        setKeys(nsFilesMap.keySet());
                    }
                });
            }
        });
    }

    @Override
    protected void removeNotify() {
        setKeys(Collections.emptySet());
    }

    private static class NamespaceNode extends FolderNode
            implements ExternalReferenceNode {
        /** Controls the appearance of this node. */
        private ExternalReferenceDecorator decorator;

        NamespaceNode(Children children, String myNamespace,
                ExternalReferenceDecorator decorator) {
            super(children);
            this.decorator = decorator;
            setName(myNamespace);
            if (Utilities.NO_NAME_SPACE.equals(myNamespace)) {
                setDisplayName(NbBundle.getMessage(NamespaceNode.class,
                        "LBL_NoTargetNamespace"));
            }
        }

        @Override
        public String getHtmlDisplayName() {
            String name = getDisplayName();
            if (decorator != null) {
                name = decorator.getHtmlDisplayName(name, this);
            }
            return name;
        }

        public Model getModel() {
            return null;
        }

        public String getNamespace() {
            // Our name is our namespace.
            return getName();
        }
    
        public boolean hasModel() {
            return false;
        }
    }
}
