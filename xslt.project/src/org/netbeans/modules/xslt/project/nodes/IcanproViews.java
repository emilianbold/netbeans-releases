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
package org.netbeans.modules.xslt.project.nodes;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.xslt.project.XsltproConstants;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileEvent;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;

import org.netbeans.modules.compapp.projects.base.ui.customizer.IcanproProjectProperties;
import static org.netbeans.modules.xslt.project.XsltproConstants.*;
import org.openide.filesystems.FileChangeListener;
import org.openide.loaders.DataObject;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.VisibilityQuery;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataFilter;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class IcanproViews {
    private static Logger logger = Logger.getLogger(IcanproViews.class.getName());
    
    private static final DataFilter NO_FOLDERS_FILTER = new NoFoldersDataFilter();
    
    private IcanproViews() {
    }
    
    public static final class LogicalViewChildren extends Children.Keys implements FileChangeListener {
        
        private static final String KEY_SOURCE_DIR = "srcDir"; // NOI18N
        private static final String KEY_DOC_BASE = "docBase"; //NOI18N
        private static final String KEY_EJBS = "ejbKey"; //NOI18N
        private static final String WEBSERVICES_DIR = "webservicesDir"; // NOI18N
        private static final String XSLT_TRANSFORM_NODE_KEY  = "xsltTransformNodeKey"; // NOI18N
        private static final String KEY_SETUP_DIR = "setupDir"; //NOI18N
        
        private AntProjectHelper helper;
        private final PropertyEvaluator evaluator;
        private FileObject projectDir;
        private Project project;
        
        public LogicalViewChildren(AntProjectHelper helper, PropertyEvaluator evaluator, Project project) {
            assert helper != null;
            this.helper = helper;
            projectDir = helper.getProjectDirectory();
            this.evaluator = evaluator;
            this.project = project;
        }
        
        @Override
        protected void addNotify() {
            super.addNotify();
            projectDir.addFileChangeListener(this);
// TODO r
//            projectDir.getFileObject("src").addFileChangeListener(this);
            createNodes();
        }
        
        public void reload() {
            createNodes();
        }
        
        private void createNodes() {
            List<Object> l = new ArrayList<Object>();
            /*
            l.add(KEY_EJBS);
             */
            
            DataFolder docBaseDir = getFolder(IcanproProjectProperties.META_INF);
            if (docBaseDir != null) {
                /*
                l.add(KEY_DOC_BASE);
                 */
            }
            
            DataFolder srcDir = getFolder(IcanproProjectProperties.SRC_DIR);
            if (srcDir != null) {
                l.add(KEY_SOURCE_DIR);
            }
            
            FileObject setupFolder = getSetupFolder();
            if (setupFolder != null && setupFolder.isFolder()) {
                l.add(KEY_SETUP_DIR);
            }
/*
            l.add(WEBSERVICES_DIR);
 */
// TODO r
//            l.add(XSLT_TRANSFORM_NODE_KEY);

// TODO r            
//////            FileObject xsltMapFo = getXsltmapFO();
////////            Lookup projectLookup = project.getLookup();
////////            System.out.println("projectLookup: "+projectLookup);
//////            if (xsltMapFo != null) {
//////                l.add(XSLT_TRANSFORM_NODE_KEY);
//////            }
            
            if (l.size() > 0) {
                setKeys(l);
            }
            
        }
        
        private FileObject getSetupFolder() {
            return projectDir.getFileObject("setup"); //NOI18N
        }
        
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
            projectDir.removeFileChangeListener(this);
            // todo r
//            projectDir.getFileObject("src").removeFileChangeListener(this);
            super.removeNotify();
        }
        
        protected Node[] createNodes(Object key) {
            List<Node> nodes = new ArrayList<Node>();
            Node n = null;

            if (key == KEY_SOURCE_DIR) {
                FileObject srcRoot = helper.resolveFileObject(
                        evaluator.getProperty(IcanproProjectProperties.SRC_DIR));
                Project p = FileOwnerQuery.getOwner(srcRoot);
                Sources s = ProjectUtils.getSources(p);
                // TODO r | m
                SourceGroup sgs [] = ProjectUtils.getSources(p).getSourceGroups(XsltproConstants.SOURCES_TYPE_ICANPRO);
                for (int i = 0; i < sgs.length; i++) {
                    if (sgs [i].contains(srcRoot)) {
                        try {
                            FileObject folder = sgs[i].getRootFolder();
                            DataObject dobj = DataObject.find(folder);
                            Children children = ((DataFolder) dobj)
                                        .createNodeChildren(NO_FOLDERS_FILTER);
// todo m
//////                            FileObject xsltmapFile = srcRoot.getFileObject(XsltproConstants.XSLTMAP_XML);
////////                            n = new RootNode(dobj.getNodeDelegate(), (DataFolder) dobj);
//////                            Node xsltTransformationsNode = NodeFactory.createXsltTransformationsNode(xsltmapFile);
//////                            if (xsltTransformationsNode != null) {
//////                                children.add(new Node[] {xsltTransformationsNode});
//////                            }
//////                            
                            n = new RootNode(srcRoot, dobj.getNodeDelegate(), children);
                        } catch (DataObjectNotFoundException ex) {
                        }
                        break;
                    }
                }
            } 
////            else if (key == XSLT_TRANSFORM_NODE_KEY) {
////                FileObject xsltmapFile = getXsltmapFO();
////                if (xsltmapFile != null) {
////                    Project project = FileOwnerQuery.getOwner(xsltmapFile);
////                    if (project != null ) {
////                        Children children = new TransformationsChildren(project);
////                        
////                        DataObject dObj;
////                        try {
////                            dObj = DataObject.find(xsltmapFile);
////                            if (dObj != null) {
////                                nodes.add(new XsltTransformationsNode(dObj, children));
////                            }
////                        } catch (DataObjectNotFoundException ex) {
//////                            System.out.println("can't find xsltmap assoc dataobject ");
////                        }
////                    }
////                }
////            }
            
            if (n != null) {
                nodes.add(n);
            }

            return nodes.toArray(new Node[nodes.size()]);
        }
        
        private FileObject getTransformmapFO() {
            DataFolder srcDir = getFolder(IcanproProjectProperties.SRC_DIR);
            if (srcDir != null) {
                FileObject srcFO = srcDir.getPrimaryFile();
                if (srcFO == null) {
                    return null;
                }
                
                // TODO m
                return srcFO.getFileObject(XsltproConstants.TRANSFORMMAP_XML);
            }
            return null;
        }
        
        private DataFolder getFolder(String propName) {
            String propertyValue = evaluator.getProperty(propName);
            if (propertyValue != null ) {
                FileObject fo = helper.resolveFileObject(evaluator.getProperty(propName));
                if ( fo != null && fo.isValid()) {
                    try {
                        DataFolder df = DataFolder.findFolder(fo);
                        return df;
                    }catch (Exception ex) {
                        logger.fine(ex.getMessage());
                    }
                }
            }
            return null;
        }
        
        // file change events in the project directory
        public void fileAttributeChanged(org.openide.filesystems.FileAttributeEvent fe) {
        }
        
        public void fileChanged(org.openide.filesystems.FileEvent fe) {
//            System.out.println("fileChanged: fe: "+fe+"; file: "+fe.getFile());
//            new Exception("FILE CHANGED").printStackTrace();
//            createNodes();
        }
        
        public void fileDataCreated(org.openide.filesystems.FileEvent fe) {
        }
        
        public void fileDeleted(org.openide.filesystems.FileEvent fe) {
//            if (!projectDir.isValid()){
//                    org.netbeans.api.project.ui.OpenProjects.getDefault().close(new Project[] { project });
//            }
//            createNodes();
            // TODO m
//            refreshKey(KEY_SOURCE_DIR);
        }
        
        public void fileFolderCreated(org.openide.filesystems.FileEvent fe) {
            // setup folder could be created
            createNodes();
            // TODO m
//            refreshKey(KEY_SOURCE_DIR);
        }
        
        public void fileRenamed(org.openide.filesystems.FileRenameEvent fe) {
            // setup folder could be renamed
            createNodes();
            // TODO m
//            refreshKey(KEY_SOURCE_DIR);
        }
    }
    
    private static final class RootNode extends FilterNode {
        public RootNode(FileObject sourceFolder, Node n, org.openide.nodes.Children children) {
            super(n,  children);
            assert sourceFolder != null;
            disableDelegation(DELEGATE_GET_DISPLAY_NAME|
                    DELEGATE_SET_DISPLAY_NAME|DELEGATE_GET_SHORT_DESCRIPTION|
                    DELEGATE_GET_ACTIONS);
            setDisplayName(
                    NbBundle.getMessage(IcanproViews.class, "LBL_ProcessFiles"));
        }
// TODO r        
//        public RootNode(Node n, DataFolder dataFolder) {
//            super(n,  dataFolder.createNodeChildren( NO_FOLDERS_FILTER));
//            disableDelegation(DELEGATE_GET_DISPLAY_NAME|
//                    DELEGATE_SET_DISPLAY_NAME|DELEGATE_GET_SHORT_DESCRIPTION|
//                    DELEGATE_GET_ACTIONS);
//            setDisplayName(
//                    NbBundle.getMessage(IcanproViews.class, "LBL_ProcessFiles"));
//            
//        }
        
        @Override
        public Action[] getActions(boolean context) {
            return new Action[] {
                CommonProjectActions.newFileAction(),
                null,
                org.openide.util.actions.SystemAction.get( org.openide.actions.FileSystemAction.class ),
                null,
                org.openide.util.actions.SystemAction.get( org.openide.actions.FindAction.class ),
                null,
                org.openide.util.actions.SystemAction.get( org.openide.actions.PasteAction.class ),
                null,
                org.openide.util.actions.SystemAction.get( org.openide.actions.ToolsAction.class ),
            };
        }
        
        @Override
        public boolean canDestroy() {
            return false;
        }
        
        @Override
        public boolean canRename() {
            return false;
        }
        
        @Override
        public boolean canCopy() {
            return false;
        }
        
        @Override
        public boolean canCut() {
            return false;
        }
    }
    
    static final class NoFoldersDataFilter implements ChangeListener, ChangeableDataFilter {
        
        EventListenerList ell = new EventListenerList();
        
        public NoFoldersDataFilter() {
            VisibilityQuery.getDefault().addChangeListener( this );
        }
        
        public boolean acceptDataObject(DataObject obj) {
            FileObject fo = obj.getPrimaryFile();
            
            // TODO r | m
//////            if (isTransformmapFile(fo)) {
//////                return false;
//////            }
            
            return  VisibilityQuery.getDefault().isVisible( fo );
        }
        
        public void stateChanged( ChangeEvent e) {
            Object[] listeners = ell.getListenerList();
            ChangeEvent event = null;
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i] == ChangeListener.class) {
                    if ( event == null) {
                        event = new ChangeEvent( this );
                    }
                    ((ChangeListener)listeners[i+1]).stateChanged( event );
                }
            }
        }
        
        public void addChangeListener( ChangeListener listener ) {
            ell.add( ChangeListener.class, listener );
        }
        
        public void removeChangeListener( ChangeListener listener ) {
            ell.remove( ChangeListener.class, listener );
        }
        
    }
    
    // TODO m
    // doesn't show xsltmap file
    public static boolean isTransformmapFile(FileObject fo) {
        if (fo == null) {
            return false;
        }
        Project project = FileOwnerQuery.getOwner(fo);
        FileObject srcFolder =null;
        if (project != null) {
            srcFolder = Util.getSrcFolder(project);
        }
        
        // TODO m
        return XsltproConstants.TRANSFORMMAP_XML.equals(fo.getNameExt())
        && srcFolder != null
                && srcFolder.equals(fo.getParent());
    }
    
}
