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

package org.netbeans.modules.web.project.ui;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.web.project.UpdateHelper;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.ui.SourceNodeFactory.PreselectPropertiesAction;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author mkleint
 */
public final class DocBaseNodeFactory implements NodeFactory {
    
    /** Creates a new instance of LibrariesNodeFactory */
    public DocBaseNodeFactory() {
    }

    public NodeList createNodes(Project p) {
        WebProject project = (WebProject)p.getLookup().lookup(WebProject.class);
        assert project != null;
        return new DocBaseNodeList(project);
    }

    private static class DocBaseNodeList implements NodeList<String>, PropertyChangeListener {
        private static final String DOC_BASE = "docBase"; //NOI18N

        private final WebProject project;
        private final ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();

        private final PropertyEvaluator evaluator;
        private final UpdateHelper helper;
        
        DocBaseNodeList(WebProject proj) {
            project = proj;
            WebLogicalViewProvider logView = (WebLogicalViewProvider) project.getLookup().lookup(WebLogicalViewProvider.class);
            assert logView != null;
            evaluator = logView.getEvaluator();
            helper = logView.getUpdateHelper();
        }
        
        public List<String> keys() {
            List<String> result = new ArrayList<String>();
            result.add(DOC_BASE);
            return result;
        }

        public synchronized void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }

        public synchronized void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
        }
        
        private void fireChange() {
            ArrayList<ChangeListener> list = new ArrayList<ChangeListener>();
            synchronized (this) {
                list.addAll(listeners);
            }
            Iterator<ChangeListener> it = list.iterator();
            while (it.hasNext()) {
                ChangeListener elem = it.next();
                elem.stateChanged(new ChangeEvent( this ));
            }
        }

        public Node node(String key) {
            if (key == DOC_BASE) {
                return new DocBaseNode(getFolder(WebProjectProperties.WEB_DOCBASE_DIR), project);
            }
            assert false: "No node for key: " + key;
            return null;
        }

        public void addNotify() {
        }

        public void removeNotify() {
        }

        public void propertyChange(PropertyChangeEvent evt) {
            // The caller holds ProjectManager.mutex() read lock
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    fireChange();
                }
            });
        }
        
        private DataFolder getFolder(String propName) {
            FileObject fo = getFileObject(propName);
            if (fo != null) {
                DataFolder df = DataFolder.findFolder(fo);
                return df;
            }
            return null;
        }
        
        private FileObject getFileObject(String propName) {
            String foName = evaluator.getProperty (propName);
            if (foName == null) {
                return null;
            }
            FileObject fo = helper.getAntProjectHelper().resolveFileObject(foName);
            // when the project is deleted externally, the sources change could
            // trigger a call to thid method before the project directory is 
            // notified about the deletion - invalid FileObject-s could be returned
            return fo != null && fo.isValid() ? fo : null;
        }
        
    }
    
    private static final class DocBaseNode extends FilterNode {
        private javax.swing.Action actions[]; 
        private static final DataFilter VISIBILITY_QUERY_FILTER = new VisibilityQueryDataFilter();
        
        private static Image WEB_PAGES_BADGE = Utilities.loadImage( "org/netbeans/modules/web/project/ui/resources/webPagesBadge.gif" ); //NOI18N
        
        private final Project project;
        
        DocBaseNode (DataFolder folder, Project project) {
            super (folder.getNodeDelegate(), folder.createNodeChildren(VISIBILITY_QUERY_FILTER));
            this.project = project;
        }
        
        public Image getIcon(int type) {        
            return computeIcon(false, type);
        }

        public Image getOpenedIcon(int type) {
            return computeIcon(true, type);
        }

        private Node getDataFolderNodeDelegate() {
            return ((DataFolder) getLookup().lookup(DataFolder.class)).getNodeDelegate();
        }

        private Image computeIcon(boolean opened, int type) {
            Image image;

            image = opened ? getDataFolderNodeDelegate().getOpenedIcon(type) : getDataFolderNodeDelegate().getIcon(type);
            image = Utilities.mergeImages(image, WEB_PAGES_BADGE, 7, 7);

            return image;        
        }
        
        public String getDisplayName () {
            return NbBundle.getMessage(DocBaseNodeFactory.class, "LBL_Node_DocBase"); //NOI18N
        }

        public javax.swing.Action[] getActions( boolean context ) {
            if ( actions == null ) {
                actions = new javax.swing.Action[9];
                actions[0] = org.netbeans.spi.project.ui.support.CommonProjectActions.newFileAction();
                actions[1] = null;
                actions[2] = org.openide.util.actions.SystemAction.get( org.openide.actions.FileSystemAction.class );
                actions[3] = null;
                actions[4] = org.openide.util.actions.SystemAction.get( org.openide.actions.FindAction.class );
                actions[5] = null;
                actions[6] = org.openide.util.actions.SystemAction.get( org.openide.actions.PasteAction.class );
                actions[7] = null;
                actions[8] = new PreselectPropertiesAction(project, "Sources"); //NOI18N
            }
            return actions;            
        }
        
        /* Can the original node be renamed?
        *
        * @return true if the node can be renamed
        */
        public boolean canRename() {
            return false;
        }

    }

    static final class VisibilityQueryDataFilter implements ChangeListener, ChangeableDataFilter {
        
        EventListenerList ell = new EventListenerList();        
        
        public VisibilityQueryDataFilter() {
            VisibilityQuery.getDefault().addChangeListener( this );
        }
                
        public boolean acceptDataObject(DataObject obj) {                
            FileObject fo = obj.getPrimaryFile();                
            return VisibilityQuery.getDefault().isVisible( fo );
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

}
