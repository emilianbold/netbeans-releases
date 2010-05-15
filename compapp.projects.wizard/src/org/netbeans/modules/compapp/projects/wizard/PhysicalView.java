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

package org.netbeans.modules.compapp.projects.wizard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.ErrorManager;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.NbBundle;

/**
 * Support for creating logical views.
 *
 * @author  Tientien Li
 */
public class PhysicalView {

    public static boolean isProjectDirNode( Node n ) {
        return n instanceof GroupNode && ((GroupNode)n).isProjectDir;
    }

    public static Node[] createNodesForProject( Project p ) {
        Sources s = ProjectUtils.getSources(p);
        SourceGroup[] groups = s.getSourceGroups(NewFileIterator.SOURCE_TYPE_BIZPRO /* Sources.TYPE_GENERIC */);
        if ((groups == null) || (groups.length < 1)) {
            groups = s.getSourceGroups(Sources.TYPE_GENERIC);
        }

        FileObject projectDirectory = p.getProjectDirectory();
        SourceGroup projectDirGroup = null;

        // First find the source group which will represent the project
        for( int i = 0; i < groups.length; i++ ) {
            FileObject groupRoot = groups[i].getRootFolder();
            if ( projectDirectory.equals( groupRoot ) ||
                 FileUtil.isParentOf( groupRoot, projectDirectory ) ) {
                if ( projectDirGroup != null ) {
                    // more than once => Illegal
                    projectDirGroup = null;
                    break;
                }
                else {
                    projectDirGroup = groups[i];
                }
            }
        }

        if ( projectDirGroup == null ) {
            // Illegal project
            ErrorManager.getDefault().notify( ErrorManager.INFORMATIONAL,
                new IllegalStateException( "Project " + p +                         // NOI18N
                    "either does not contain it's project directory under the " +   // NOI18N
                    "Generic source groups or the project directory is under" +     // NOI18N
                    "more than one source group" ) );                               // NOI18N
            return new Node[0];
        }


        // Create the nodes
        ArrayList nodesList = new ArrayList( groups.length );
        nodesList.add( new GroupNode( p, projectDirGroup, true, DataFolder.findFolder( projectDirGroup.getRootFolder() ) ) );

        for( int i = 0; i < groups.length; i++ ) {

            if ( groups[i] == projectDirGroup ) {
                continue;
            }

            nodesList.add( new GroupNode( p, groups[i], false, DataFolder.findFolder( groups[i].getRootFolder() ) ) );
        }

        Node nodes[] = new Node[ nodesList.size() ];
        nodesList.toArray( nodes );
        return nodes;
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

    static final class GroupNode extends FilterNode implements PropertyChangeListener {

        private static final DataFilter VISIBILITY_QUERY_FILTER = new VisibilityQueryDataFilter();

        static final String GROUP_NAME_PATTERN = NbBundle.getMessage(
            PhysicalView.class, "FMT_PhysicalView_GroupName" ); // NOI18N

        private Project project;
        private ProjectInformation pi;
        private SourceGroup group;
        private boolean isProjectDir;

        public GroupNode(Project project, SourceGroup group, boolean isProjectDir, DataFolder dataFolder ) {
            super( dataFolder.getNodeDelegate(),
                   dataFolder.createNodeChildren( VISIBILITY_QUERY_FILTER ),
                   createLookup( project, group, dataFolder ) );

            this.project = project;
            this.pi = ProjectUtils.getInformation( project );
            this.group = group;
            this.isProjectDir = isProjectDir;
            pi.addPropertyChangeListener(WeakListeners.propertyChange(this, pi));
            group.addPropertyChangeListener( WeakListeners.propertyChange( this, group ) );
        }

        // XXX May need to change icons as well

        public String getName() {
            if ( isProjectDir ) {
                return pi.getName();
            }
            else {
                return group.getName();
            }
        }

        public String getDisplayName() {
            if ( isProjectDir ) {
                return pi.getDisplayName();
            }
            else {
                return MessageFormat.format( GROUP_NAME_PATTERN,
                    new Object[] { group.getDisplayName(), pi.getDisplayName(), getOriginal().getDisplayName() } );
            }
        }

        public String getShortDescription() {
            FileObject gdir = group.getRootFolder();
            String dir = FileUtil.getFileDisplayName(gdir);
            return NbBundle.getMessage(PhysicalView.class,
                                       isProjectDir ? "HINT_project" : "HINT_group", // NOI18N
                                       dir);
        }

        public boolean canRename() {
            return false;
        }

        public boolean canCut() {
            return false;
        }

        public boolean canCopy() {
            // At least for now.
            return false;
        }

        public boolean canDestroy() {
            return false;
        }

        public Action[] getActions( boolean context ) {

            if ( context ) {
                return super.getActions( true );
            }
            else {
                Action[] folderActions = super.getActions( false );
                Action[] projectActions;

                if ( isProjectDir ) {
                    // If this is project dir then the properties action
                    // has to be replaced to invoke project customizer
                    projectActions = new Action[ folderActions.length ];
                    for ( int i = 0; i < folderActions.length; i++ ) {
                        if ( folderActions[i] instanceof org.openide.actions.PropertiesAction ) {
                            projectActions[i] = CommonProjectActions.customizeProjectAction();
                        }
                        else {
                            projectActions[i] = folderActions[i];
                        }
                    }
                }
                else {
                    projectActions = folderActions;
                }

                return projectActions;
            }
        }

        // Private methods -------------------------------------------------

        public void propertyChange(PropertyChangeEvent evt) {
            String prop = evt.getPropertyName();
            if (ProjectInformation.PROP_DISPLAY_NAME.equals(prop)) {
                fireDisplayNameChange(null, null);
            } else if (ProjectInformation.PROP_NAME.equals(prop)) {
                fireNameChange(null, null);
            } else if (ProjectInformation.PROP_ICON.equals(prop)) {
                // OK, ignore
            } else if ( "name".equals(prop) ) { // NOI18N
                fireNameChange(null, null);
            } else if ( "displayName".equals(prop) ) { // NOI18N
                fireDisplayNameChange(null, null);
            } else if ( "icon".equals(prop) ) { // NOI18N
                // OK, ignore
            } else if ( "rootFolder".equals(prop) ) { // NOI18N
                // XXX Do something to children and lookup
                fireNameChange(null, null);
                fireDisplayNameChange(null, null);
                fireShortDescriptionChange(null, null);
            } else {
                assert false : "Attempt to fire an unsupported property change event from " + pi.getClass().getName() + ": " + prop;
            }
        }

        private static Lookup createLookup( Project p, SourceGroup group, DataFolder dataFolder ) {
            return new ProxyLookup(new Lookup[] {
                dataFolder.getNodeDelegate().getLookup(),
                Lookups.fixed( new Object[] { p, new PathFinder( group ) } ),
                p.getLookup(),
            });
        }

    }

    public static class PathFinder {

        private SourceGroup group;

        public PathFinder( SourceGroup group ) {
            this.group = group;
        }

        public Node findPath( Node root, Object object ) {

            if ( !( object instanceof FileObject ) ) {
                return null;
            }

            FileObject fo = (FileObject)object;
            FileObject groupRoot = group.getRootFolder();
            if ( FileUtil.isParentOf( groupRoot, fo ) /* && group.contains( fo ) */ ) {
                // The group contains the object

                String relPath = FileUtil.getRelativePath( groupRoot, fo );

                ArrayList path = new ArrayList();
                StringTokenizer strtok = new StringTokenizer( relPath, "/" );
                while( strtok.hasMoreTokens() ) {
                   path.add( strtok.nextToken() );
                }

                String name = fo.getName();

                try {
                    DataObject dobj = DataObject.find( fo );
                    name = dobj.getNodeDelegate().getName();
                }
                catch( DataObjectNotFoundException e ) {
                }

                path.set( path.size() - 1, name );

                try {
                    return NodeOp.findPath( root, Collections.enumeration( path ) );
                }
                catch ( NodeNotFoundException e ) {
                    return null;
                }
            }
            else if ( groupRoot.equals( fo ) ) {
                return root;
            }

            return null;
        }

    }

}
