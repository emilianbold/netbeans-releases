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

package org.netbeans.modules.project.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import javax.swing.Action;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.FolderLookup;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.xml.XMLUtil;
import org.openidex.search.SearchInfo;
import org.openidex.search.SearchInfoFactory;

/** Root node for list of open projects
 * @author Petr Hrebejk
 */
public class ProjectsRootNode extends AbstractNode {
    
    static final int PHYSICAL_VIEW = 0;
    static final int LOGICAL_VIEW = 1;
        
    private static final String ICON_BASE = "org/netbeans/modules/project/ui/resources/projectsRootNode"; //NOI18N
    private static final String ACTIONS_FOLDER = "ProjectsTabActions"; // NOI18N
    
    private ResourceBundle bundle;
    private final int type;
    
    public ProjectsRootNode( int type ) {
        super( new ProjectChildren( type ) ); 
        setIconBase( ICON_BASE );
        this.type = type;
    }
        
    public String getName() {
        return ( "OpenProjects" ); // NOI18N
    }
    
    public String getDisplayName() {
        if ( this.bundle == null ) {
            this.bundle = NbBundle.getBundle( ProjectsRootNode.class );
        }
        return bundle.getString( "LBL_OpenProjectsNode_Name" ); // NOI18N
    }
    
    public boolean canRename() {
        return false;
    }
        
    public Node.Handle getHandle() {        
        return new Handle(type);
    }
    
    public Action[] getActions( boolean context ) {
        if (context || type == PHYSICAL_VIEW) {
            return new Action[0];
        } else {
            List/*<Action>*/ actions = new ArrayList();
            DataFolder actionsFolder = DataFolder.findFolder(Repository.getDefault().getDefaultFileSystem().findResource(ACTIONS_FOLDER));
            Iterator/*<Object>*/ instances = new FolderLookup(actionsFolder).getLookup().lookup(new Lookup.Template(Object.class)).allInstances().iterator();
            while (instances.hasNext()) {
                Object o = instances.next();
                if (o instanceof Action) {
                    actions.add((Action) o);
                } else if (o instanceof JSeparator) {
                    actions.add(null);
                }
            }
            return (Action[]) actions.toArray(new Action[actions.size()]);
        }
    }
    
    /** Finds node for given object in the view
     * @return the node or null if the node was not found
     */
    Node findNode( Object target ) {        
        
        ProjectChildren ch = (ProjectChildren)getChildren();
        
        if ( ch.type == LOGICAL_VIEW ) {
            Node[] nodes = ch.getNodes( true );
            // Speed up search in case we have an owner project - look in its node first.
            Project ownerProject;
            if (target instanceof FileObject) {
                ownerProject = FileOwnerQuery.getOwner((FileObject) target);
            } else if (target instanceof DataObject) {
                ownerProject = FileOwnerQuery.getOwner(((DataObject) target).getPrimaryFile());
            } else {
                ownerProject = null;
            }
            for (int lookOnlyInOwnerProject = (ownerProject != null) ? 0 : 1; lookOnlyInOwnerProject < 2; lookOnlyInOwnerProject++) {
                for (int i = 0; i < nodes.length; i++) {
                    Project p = (Project) nodes[i].getLookup().lookup(Project.class);
                    assert p != null : "Should have had a Project in lookup of " + nodes[i];
                    if (lookOnlyInOwnerProject == 0 && p != ownerProject) {
                        continue; // but try again (in next outer loop) as a fallback
                    }
                    LogicalViewProvider lvp = (LogicalViewProvider) p.getLookup().lookup(LogicalViewProvider.class);
                    if (lvp != null) {
                        Node selectedNode = lvp.findPath(nodes[i], target);
                        if (selectedNode != null) {
                            return selectedNode;
                        }
                    }
                }
            }
            return null;
            
        }
        else if ( ch.type == PHYSICAL_VIEW ) {
            Node[] nodes = ch.getNodes( true );
            for( int i = 0; i < nodes.length; i++  ) {
                // XXX could do similar optimization as for LOGICAL_VIEW; every nodes[i] must have some Project in its lookup
                PhysicalView.PathFinder pf = (PhysicalView.PathFinder)nodes[i].getLookup().lookup( PhysicalView.PathFinder.class );
                if ( pf != null ) {
                    Node n = pf.findPath( nodes[i], target );
                    if ( n != null ) {
                        return n;
                    }
                }
            }
            return null;
        }       
        else {
            return null;
        }
    }
    
    private static class Handle implements Node.Handle {

        private static final long serialVersionUID = 78374332058L;
        
        private int viewType;
        
        public Handle( int viewType ) {
            this.viewType = viewType;
        }
        
        public Node getNode() {
            return new ProjectsRootNode( viewType );
        }
        
    }
       
    
    // XXX Needs to listen to project rename
    // However project rename is currently disabled so it is not a big deal
    static class ProjectChildren extends Children.Keys implements ChangeListener, PropertyChangeListener {
        
        private java.util.Map /*<Sources,Reference<Project>>*/ sources2projects = new WeakHashMap();
        
        int type;
        
        public ProjectChildren( int type ) {
            this.type = type;
            OpenProjectList.getDefault().addPropertyChangeListener( this );
        }
        
        // Children.Keys impl --------------------------------------------------
        
        public void addNotify() {            
            setKeys( getKeys() );
        }
        
        public void removeNotify() {
            for( Iterator it = sources2projects.keySet().iterator(); it.hasNext(); ) {
                Sources sources = (Sources)it.next();
                sources.removeChangeListener( this );                
            }
            sources2projects.clear();
            setKeys( Collections.EMPTY_LIST );
        }
        
        protected Node[] createNodes( Object key ) {
            
            Project project = (Project)key;
            
            LogicalViewProvider lvp = (LogicalViewProvider)project.getLookup().lookup( LogicalViewProvider.class );
            
            Node nodes[] = null;
                        
            if ( type == PHYSICAL_VIEW ) {
                Sources sources = ProjectUtils.getSources( project );
                sources.removeChangeListener( this );
                sources.addChangeListener( this );
                sources2projects.put( sources, new WeakReference( project ) );
                nodes = PhysicalView.createNodesForProject( project );
            }            
            else if ( lvp == null ) {
                ErrorManager.getDefault().log(ErrorManager.WARNING, "Warning - project " + ProjectUtils.getInformation(project).getName() + " failed to supply LogicalViewProvider in it's lookup"); // NOI18N
                Sources sources = ProjectUtils.getSources( project );
                sources.removeChangeListener( this );
                sources.addChangeListener( this );
                nodes = PhysicalView.createNodesForProject( project );
                if ( nodes.length > 0 ) {
                    nodes = new Node[] { nodes[0] };
                }
                else {
                    nodes = new Node[] { Node.EMPTY };
                }
            }
            else {
                nodes = new Node[] { lvp.createLogicalView() };
                if (nodes[0].getLookup().lookup(Project.class) != project) {
                    // Various actions, badging, etc. are not going to work.
                    ErrorManager.getDefault().log(ErrorManager.WARNING, "Warning - project " + ProjectUtils.getInformation(project).getName() + " failed to supply itself in the lookup of the root node of its own logical view"); // NOI18N
                }
            }

            Node[] badgedNodes = new Node[ nodes.length ];
            for( int i = 0; i < nodes.length; i++ ) {
                if ( type == PHYSICAL_VIEW && !PhysicalView.isProjectDirNode( nodes[i] ) ) {
                    // Don't badge external sources
                    badgedNodes[i] = nodes[i];
                }
                else {
                    badgedNodes[i] = new BadgingNode( nodes[i],
                                                      type == LOGICAL_VIEW );
                }
            }
                        
            return badgedNodes;
        }        
        
        // PropertyChangeListener impl -----------------------------------------
        
        public void propertyChange( PropertyChangeEvent e ) {
            if ( OpenProjectList.PROPERTY_OPEN_PROJECTS.equals( e.getPropertyName() ) ) {
                setKeys( getKeys() );
            }
        }
        
        // Change listener impl ------------------------------------------------
        
        public void stateChanged( ChangeEvent e ) {
            
            WeakReference projectRef = (WeakReference)sources2projects.get( e.getSource() );
            if ( projectRef == null ) {
                return;
            }
            
            final Project project = (Project)projectRef.get();
            
            if ( project == null ) {
                return;
            }
            
            // Fix for 50259, callers sometimes hold locks
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    refreshKey( project );
                }
            } );
        }
                                
        // Own methods ---------------------------------------------------------
        
        public Collection getKeys() {
            List projects = Arrays.asList( OpenProjectList.getDefault().getOpenProjects() );
            Collections.sort( projects, OpenProjectList.PROJECT_BY_DISPLAYNAME );
            
            return projects;
        }
                                                
    }
        
    private static final class BadgingNode extends FilterNode implements PropertyChangeListener {

        private static String badgedNamePattern = NbBundle.getMessage( ProjectsRootNode.class, "LBL_MainProject_BadgedNamePattern" );
        
        public BadgingNode( Node n, boolean addSearchInfo ) {
            super( n,
                   null,                //default children
                   addSearchInfo
                   ? new ProxyLookup( new Lookup[] {
                         n.getLookup(),
                         Lookups.singleton(alwaysSearchableSearchInfo(SearchInfoFactory
                                            .createSearchInfoBySubnodes(n))),
                    })
                   : n.getLookup() );
            OpenProjectList.getDefault().addPropertyChangeListener( WeakListeners.propertyChange( this, OpenProjectList.getDefault() ) );
        }
        
        public String getDisplayName() {
            String original = super.getDisplayName();
            return isMain() ? MessageFormat.format( badgedNamePattern, new Object[] { original } ) : original;
        }

        public String getHtmlDisplayName() {
            String htmlName = getOriginal().getHtmlDisplayName();
            String dispName = null;
            if (isMain() && htmlName == null) {
                dispName = super.getDisplayName();
                try {
                    dispName = XMLUtil.toElementContent(dispName);
                } catch (CharConversionException ex) {
                    // ignore
                }
            }
            return isMain() ? "<b>" + (htmlName == null ? dispName : htmlName) + "</b>" : htmlName; //NOI18N
        }

        public void propertyChange( PropertyChangeEvent e ) {
            if ( OpenProjectList.PROPERTY_MAIN_PROJECT.equals( e.getPropertyName() ) ) {
                fireDisplayNameChange( null, null );
            }
        }

        private boolean isMain() {
            Project p = (Project)getLookup().lookup( Project.class );
            return p != null && OpenProjectList.getDefault().isMainProject( p );
        }
        
    }
    
    /**
     * Produce a {@link SearchInfo} variant that is always searchable, for speed.
     * @see "#48685"
     */
    static SearchInfo alwaysSearchableSearchInfo(SearchInfo i) {
        return new AlwaysSearchableSearchInfo(i);
    }
    
    private static final class AlwaysSearchableSearchInfo implements SearchInfo {
        
        private final SearchInfo delegate;
        
        public AlwaysSearchableSearchInfo(SearchInfo delegate) {
            this.delegate = delegate;
        }

        public boolean canSearch() {
            return true;
        }

        public Iterator/*<DataObject>*/ objectsToSearch() {
            return delegate.objectsToSearch();
        }
        
    }
    
}
