/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
package org.netbeans.modules.xml.reference;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.Action;

import org.openide.actions.FindAction;
import org.openide.actions.SaveAllAction;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.netbeans.api.project.Project;

import org.netbeans.modules.xml.retriever.catalog.CatalogElement;
import org.netbeans.modules.xml.retriever.catalog.CatalogEntry;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel;
import static org.netbeans.modules.xml.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2008.06.09
 */
public final class ReferenceNode extends AbstractNode {

    public ReferenceNode(Project project) {
        super(Children.LEAF);
        myProject = project;
        myReferenceHelper = new ReferenceHelper(project);

        if (myReferenceHelper.getCatalog() != null) {
            setChildren(new ReferenceChildren(myReferenceHelper.getCatalog()));
        }
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] { new AddLocalAction(), new AddRemoteAction(), null, SystemAction.get(FindAction.class) };
    }

    @Override
    public Image getIcon(int type) {
        return icon(ReferenceNode.class, "node").getImage(); // NOI18N
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public boolean canRename() {
        return false;
    }

    @Override
    public String getDisplayName() {
        return i18n(ReferenceNode.class, "LBL_Referenced_Resources"); // NOI18N
    }

    // ------------------------------------------------------------------
    private class ReferenceChildren extends Children.Keys<CatalogEntry> {

        protected ReferenceChildren(CatalogWriteModel catalog) {
            myCatalog = catalog;
            myCatalogListener = new CatalogListener();
        }

        @Override
        protected Node[] createNodes(CatalogEntry entry) {
            return new Node[]{new ReferenceChild(entry, myCatalogs.get(entry), myProject)};
        }

        @Override
        protected void addNotify() {
//out();
//out("!!! add notify");
            myReferenceHelper.getNextCatalog(myCatalog);
            setKeys(createKeys());

            for (FileObject catalog : myCatalogFiles) {
//out("add listener: " + catalog.getPath());
                catalog.addFileChangeListener(myCatalogListener);
            }
            ((SaveAllAction) SaveAllAction.findObject(SaveAllAction.class, true)).performAction();
        }

        @Override
        protected void removeNotify() {
//out("!!! remove notify");
            setKeys(new HashSet<CatalogEntry>());

            for (FileObject catalog : myCatalogFiles) {
                catalog.removeFileChangeListener(myCatalogListener);
            }
        }

        private List<CatalogEntry> createKeys() {
//out("get keys");
            List<CatalogEntry> systems = new ArrayList<CatalogEntry>();
            myCatalogs = new WeakHashMap<CatalogEntry, CatalogWriteModel>();
            myCatalogFiles = new HashSet<FileObject>();
            findSystems(myCatalog, systems);

            Collections.sort(systems, new Comparator<CatalogEntry>() {
                public int compare(CatalogEntry entry1, CatalogEntry entry2) {
                    if (entry1 == null || entry1.getSource() == null) {
                        return -1;
                    }
                    if (entry2 == null || entry2.getSource() == null) {
                        return 1;
                    }
                    return entry1.getSource().compareToIgnoreCase(entry2.getSource());
                }
            });
//out("systems: " + systems);
            return systems;
        }

        private void findSystems(CatalogWriteModel catalog, List<CatalogEntry> systems) {
            if (catalog == null) {
                return;
            }
            myCatalogFiles.add(catalog.getCatalogFileObject());
            Collection<CatalogEntry> entries = catalog.getCatalogEntries();
//out();
//out("CATALOG: " + catalog.getClass());

            for (CatalogEntry entry : entries) {
//out();
//out("        type: " + entry.getEntryType());
//out("      source: " + entry.getSource());
//out("      target: " + entry.getTarget());
//out("       valid: " + entry.isValid());
                if (entry.getEntryType() == CatalogElement.nextCatalog) {
                    findSystems(myReferenceHelper.getCatalog(entry.getSource()), systems);
                } else if (entry.getEntryType() == CatalogElement.system) {
                    systems.add(entry);
                    myCatalogs.put(entry, catalog);
                }
            }
        }

        // ----------------------------------------------------------
        private class CatalogListener implements FileChangeListener {

            public void fileChanged(FileEvent event) {
//out();
//out("FILE CHANGED: " + event.getFile());
//out();
                new RequestProcessor().post(new Runnable() {

                    public void run() {
                        setKeys(createKeys());
                    }
                });
            }

            public void fileAttributeChanged(FileAttributeEvent event) {}

            public void fileDataCreated(FileEvent event) {}

            public void fileDeleted(FileEvent event) {}

            public void fileFolderCreated(FileEvent event) {}

            public void fileRenamed(FileRenameEvent event) {}
        }

        private CatalogWriteModel myCatalog;
        private Set<FileObject> myCatalogFiles;
        private CatalogListener myCatalogListener;
        private java.util.Map<CatalogEntry, CatalogWriteModel> myCatalogs;
    }

    // ----------------------------------------------------
    private class AddLocalAction extends ReferenceAction {

        @Override
        protected String getKeyName() {
            return "LBL_Add_Local"; // NOI18N
        }

        public void actionPerformed(ActionEvent event) {
            myReferenceHelper.addFileAction();
        }
    }

    // ---------------------------------------------------
    private class AddRemoteAction extends ReferenceAction {

        @Override
        protected String getKeyName() {
            return "LBL_Add_Remote"; // NOI18N
        }

        public void actionPerformed(ActionEvent event) {
            myReferenceHelper.addURLAction(null);
        }
    }

    private Project myProject;
    private ReferenceHelper myReferenceHelper;
}                                                         
