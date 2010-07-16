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

package org.netbeans.modules.soa.jca.base.inbound;

import org.netbeans.modules.soa.jca.base.GlobalRarRegistry;
import java.awt.Image;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Utilities;

/**
 *
 * @author echou
 */
public class JavaCollabNode extends AbstractNode {

    public JavaCollabNode(Project project) throws Exception {
        super(new JavaCollabNodeChildren(project));
    }

    @Override
    public String getDisplayName() {
        return "Java Collaborations";
    }

    @Override
    public String getName() {
        return "javaCollab";
    }

    @Override
    public Image getIcon(int type) {
        return Utilities.loadImage("org/netbeans/modules/soa/jca/base/inbound/resources/MessageNodeIcon.gif");
    }

    @Override
    public Image getOpenedIcon(int type) {
        return Utilities.loadImage("org/netbeans/modules/soa/jca/base/inbound/resources/MessageNodeIcon.gif");
    }

    @Override
    public Action[] getActions(boolean b) {
        return new Action[] {
        };
    }

    public static class JavaCollabNodeChildren extends Children.Keys<String> {

        private Project project;
        private EjbJarXmlFileChangeListener ejbJarXmlFileChangeListener;
        private FileObject ejbJarXml;

        // mapping of ejb-name to MDB instances
        private java.util.Map<String, JcaMdbModel> mdbMap;

        public JavaCollabNodeChildren(Project project) throws Exception {
            super();
            this.project = project;
            this.mdbMap = new HashMap<String, JcaMdbModel> ();
            this.ejbJarXmlFileChangeListener = new EjbJarXmlFileChangeListener(this);
            refreshMdbMap();


            FileObject confDirectory = EjbProjectUtil.getEjbJarParentDirectoryFileObject(project);
            if (confDirectory != null) {
                confDirectory.addFileChangeListener(new EjbJarParentDirectoryChangeListener(this));
            }
            handleConfDirChanged();
        }

        public void handleConfDirChanged() {
            try {
                if (ejbJarXml == null) {
                    ejbJarXml = EjbProjectUtil.getEjbJarXmlFileObject(project);
                    if (ejbJarXml != null) {
                        ejbJarXml.addFileChangeListener(ejbJarXmlFileChangeListener);
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }

        @Override
        protected Node[] createNodes(String key) {
            return new Node[] { new JcaMdbNode(project, mdbMap.get(key)) };
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            super.setKeys(getKeys());
        }

        @Override
        protected void removeNotify() {
            super.setKeys(Collections.EMPTY_LIST);
            if (ejbJarXml != null) {
                ejbJarXml.removeFileChangeListener(ejbJarXmlFileChangeListener);
            }
            super.removeNotify();
        }

        public void refreshMdbMap() {
            this.mdbMap.clear();
            for (JcaMdbModel model : EjbProjectUtil.getJcaMdbModels(project.getProjectDirectory())) {
                if (model.getJcaModuleName() != null) {
                    if (GlobalRarRegistry.getInstance().getRar(model.getJcaModuleName()) != null) {
                        mdbMap.put(model.getEjbName(), model);
                    }
                }
            }
            super.setKeys(getKeys());
            for (String key : getKeys()) {
                super.refreshKey(key);
            }
        }

        private String[] getKeys() {
            String[] keys = mdbMap.keySet().toArray(new String[0]);
            Arrays.sort(keys, new Comparator<String> () {
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });
            return keys;
        }

    }

    private static class EjbJarXmlFileChangeListener extends FileChangeAdapter {

        private JavaCollabNodeChildren parent;

        public EjbJarXmlFileChangeListener(JavaCollabNodeChildren parent) {
            this.parent = parent;
        }

        @Override
        public void fileChanged(FileEvent evt) {
            handleChangeEvent();
        }

        private void handleChangeEvent() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    parent.refreshMdbMap();
                }
            });
        }
    }

    private static class EjbJarParentDirectoryChangeListener extends FileChangeAdapter {

        private JavaCollabNodeChildren parent;

        public EjbJarParentDirectoryChangeListener(JavaCollabNodeChildren parent) {
            this.parent = parent;
        }

        @Override
        public void fileDataCreated(FileEvent evt) {
            handleChangeEvent();
        }

        private void handleChangeEvent() {
            parent.handleConfDirChanged();
        }
    }
}
