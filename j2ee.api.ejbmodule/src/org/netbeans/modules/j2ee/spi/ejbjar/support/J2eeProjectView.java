/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.spi.ejbjar.support;

import java.awt.Image;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbNodesFactory;
import org.netbeans.modules.j2ee.ejbjar.project.ui.EjbContainerNode;
import org.netbeans.modules.j2ee.ejbjar.project.ui.ServerResourceNode;
import org.openide.ErrorManager;
import org.openide.actions.FindAction;
import org.openide.filesystems.FileObject;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/** Utility class for creating J2EE project nodes.
 *
 * @author Pavel Buzek
 */
public final class J2eeProjectView {
    private static EjbNodesFactory factoryInstance = null;
    
    private J2eeProjectView() {
    }

    /** Returns an instance of EjbNodesFactory if there is any
     * available in default lookup. Otherwise returns null.
     */
    public static EjbNodesFactory getEjbNodesFactory () {
        if (factoryInstance == null) {
            factoryInstance = (EjbNodesFactory) Lookup.getDefault().lookup(EjbNodesFactory.class);
        }
        if (factoryInstance == null) {
            ErrorManager.getDefault().log("No EjbNodesFactory instance available: Enterprise Beans nodes cannot be creater");
        }
        return factoryInstance;
    }
    
    public static Node createServerResourcesNode (Project p) {
        return new ServerResourceNode (p);
    }
    
    public static Node createEjbsView (EjbJar model, ClassPath srcPath, FileObject ddFile, Project p) {
        return new EjbContainerNode(model, srcPath, ddFile, p, getEjbNodesFactory());
    }
    
    public static Node createConfigFilesView (FileObject folder) {
        return new DocBaseNode(DataFolder.findFolder(folder));
    }
    
    private static final class DocBaseNode extends FilterNode {
        private static final DataFilter VISIBILITY_QUERY_FILTER = new VisibilityQueryDataFilter();

        private static Image CONFIGURATION_FILES_BADGE =
                Utilities.loadImage( "org/netbeans/modules/j2ee/ejbjar/project/ui/ejbjar.gif", true ); // NOI18N

        public DocBaseNode(DataFolder folder) {
            super(folder.getNodeDelegate(), folder.createNodeChildren(VISIBILITY_QUERY_FILTER));
        }

        public Image getIcon( int type ) {
            return computeIcon( false, type );
        }

        public Image getOpenedIcon( int type ) {
            return computeIcon( true, type );
        }

        private Image computeIcon( boolean opened, int type ) {
            Node folderNode = getOriginal();
            Image image = opened ? folderNode.getOpenedIcon( type ) : folderNode.getIcon( type );
            return Utilities.mergeImages( image, CONFIGURATION_FILES_BADGE, 7, 7 );
        }

        public String getDisplayName() {
            return NbBundle.getMessage(J2eeProjectView.class, "LBL_Node_ConfigFiles"); //NOI18N
        }

        public boolean canCopy() {
            return false;
        }

        public boolean canCut() {
            return false;
        }

        public boolean canRename() {
            return false;
        }

        public boolean canDestroy() {
            return false;
        }

        public Action[] getActions( boolean context ) {
            return new Action[] {
                SystemAction.get(FindAction.class),
            };
        }
    }
    
    private static final class VisibilityQueryDataFilter implements ChangeListener, ChangeableDataFilter {
        
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
