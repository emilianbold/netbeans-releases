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
package org.netbeans.modules.etl.project.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.Action;
import org.netbeans.api.project.Project;

import org.netbeans.api.queries.VisibilityQuery;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import net.java.hulp.i18n.Logger;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.support.CommonProjectActions;

import org.netbeans.modules.compapp.projects.base.ui.customizer.IcanproProjectProperties;
import org.netbeans.modules.etl.project.Localizer;
import org.netbeans.modules.etl.project.EtlproProjectGenerator;
import org.openide.filesystems.FileChangeListener;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

class EtlproViews {

    private EtlproViews() {
    }

    static final class LogicalViewChildren extends Children.Keys implements FileChangeListener {

        private static final String KEY_SOURCE_DIR = "srcDir"; // NOI18N        

        private static final String KEY_DATA_DIR = "data"; //NOI18N

        private static final String KEY_DB_DIR = "databases"; //NOI18N

        private AntProjectHelper helper;
        private final PropertyEvaluator evaluator;
        private FileObject projectDir;
        private Project project;
        private static transient final Logger mLogger = Logger.getLogger(EtlproViews.class.getName());
        private static transient final Localizer mLoc = Localizer.get();

        public LogicalViewChildren(AntProjectHelper helper, PropertyEvaluator evaluator, Project project) {
            assert helper != null;
            this.helper = helper;
            projectDir = helper.getProjectDirectory();
            this.evaluator = evaluator;
            this.project = project;
        }

        protected void addNotify() {
            super.addNotify();
            projectDir.addFileChangeListener(this);
            createNodes();
        }

        private void createNodes() {
            List l = new ArrayList();

            DataFolder srcDir = getFolder(IcanproProjectProperties.SRC_DIR);//EtlproProjectGenerator.DEFAULT_SRC_FOLDER);

            if (srcDir != null) {
                l.add(KEY_SOURCE_DIR);
            }

            FileObject dataFolder = getDataFolder(EtlproProjectGenerator.DEFAULT_DATA_DIR);
            if (dataFolder != null && dataFolder.isFolder()) {
                l.add(KEY_DATA_DIR);
            }

            dataFolder = getDataFolder(EtlproProjectGenerator.DEFAULT_DATABASES_DIR);
            if (dataFolder != null && dataFolder.isFolder()) {
                l.add(KEY_DB_DIR);
            }

            setKeys(l);
        }

        private FileObject getDataFolder(String propName) {
            return projectDir.getFileObject(propName); //NOI18N

        }

        @Override
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
            projectDir.removeFileChangeListener(this);
            super.removeNotify();
        }

        protected Node[] createNodes(Object key) {
            Node n = null;
            if (key == KEY_SOURCE_DIR) {
                FileObject srcRoot = helper.resolveFileObject(evaluator.getProperty(IcanproProjectProperties.SRC_DIR));
                DataObject fileDO;
                try {
                    fileDO = DataObject.find(srcRoot);
                    n = new ViewItemNode((DataFolder) fileDO, "Collaborations", "Collaborations");
                } catch (DataObjectNotFoundException e) {
                    throw new AssertionError(e);
                }
            }
            return n == null ? new Node[0] : new Node[]{n};
        }

        private DataFolder getFolder(String propName) {
            String propertyValue = evaluator.getProperty(propName);
            if (propertyValue != null) {
                FileObject fo = helper.resolveFileObject(evaluator.getProperty(propName));
                if (fo != null && fo.isValid()) {
                    try {
                        DataFolder df = DataFolder.findFolder(fo);
                        return df;
                    } catch (Exception ex) {
                        mLogger.errorNoloc(mLoc.t("PRJS021: Exception :{0}", ex.getMessage()), ex);
                    }
                }
            }
            return null;
        }

        // file change events in the project directory
        public void fileAttributeChanged(org.openide.filesystems.FileAttributeEvent fe) {
        }

        public void fileChanged(org.openide.filesystems.FileEvent fe) {
        }

        public void fileDataCreated(org.openide.filesystems.FileEvent fe) {
        }

        public void fileDeleted(org.openide.filesystems.FileEvent fe) {
            // createNodes();
        }

        public void fileFolderCreated(org.openide.filesystems.FileEvent fe) {
            createNodes();
        }

        public void fileRenamed(org.openide.filesystems.FileRenameEvent fe) {
            createNodes();
        }
    }
    private static final DataFilter FILTER = new CollabDataFilter();

    private static final class ViewItemNode extends FilterNode implements PropertyChangeListener {

        private final String name;
        private final String displayName;

        public ViewItemNode(DataFolder folder, String name, String displayName) {
            super(folder.getNodeDelegate(), folder.createNodeChildren(FILTER));
            this.name = name;
            this.displayName = displayName;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public boolean canRename() {
            return false;
        }

        @Override
        public boolean canDestroy() {
            return false;
        }

        @Override
        public boolean canCut() {
            return false;
        }

        @Override
        public boolean canCopy() {
            return false;
        }

        @Override
        public void setName(String arg0) {
            super.setName(arg0);
        }

        @Override
        public Action[] getActions(boolean context) {
            //return super.getActions(context);
            return new Action[]{
                        CommonProjectActions.newFileAction(),
                        null,
                        org.openide.util.actions.SystemAction.get(org.openide.actions.FileSystemAction.class),
                        null,
                        org.openide.util.actions.SystemAction.get(org.openide.actions.FindAction.class),
                        null,
                        org.openide.util.actions.SystemAction.get(org.openide.actions.PasteAction.class),
                        null,
                        org.openide.util.actions.SystemAction.get(org.openide.actions.ToolsAction.class),
                    };
        }

        public void propertyChange(PropertyChangeEvent evt) {
            fireNameChange(null, null);
            fireDisplayNameChange(null, null);
            fireIconChange();
            fireOpenedIconChange();
        }
    }

    static final class CollabDataFilter implements ChangeListener, ChangeableDataFilter {

        EventListenerList ell = new EventListenerList();

        public CollabDataFilter() {
            VisibilityQuery.getDefault().addChangeListener(this);
        }

        public boolean acceptDataObject(DataObject obj) {
            FileObject fo = obj.getPrimaryFile();
            return VisibilityQuery.getDefault().isVisible(fo);
        }

        public void stateChanged(ChangeEvent e) {
            Object[] listeners = ell.getListenerList();
            ChangeEvent event = null;
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == ChangeListener.class) {
                    if (event == null) {
                        event = new ChangeEvent(this);
                    }
                    ((ChangeListener) listeners[i + 1]).stateChanged(event);
                }
            }
        }

        public void addChangeListener(ChangeListener listener) {
            ell.add(ChangeListener.class, listener);
        }

        public void removeChangeListener(ChangeListener listener) {
            ell.remove(ChangeListener.class, listener);
        }
    }
}
