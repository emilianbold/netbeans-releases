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

package org.netbeans.modules.project.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import javax.swing.Action;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.xml.XMLUtil;
import org.openidex.search.FileObjectFilter;
import org.openidex.search.SearchInfo;
import org.openidex.search.SearchInfoFactory;

/** Root node for list of open projects
 * @author Petr Hrebejk
 */
public class ProjectsRootNode extends AbstractNode {

    static final int PHYSICAL_VIEW = 0;
    static final int LOGICAL_VIEW = 1;
        
    private static final String ICON_BASE = "org/netbeans/modules/project/ui/resources/projectsRootNode.gif"; //NOI18N
    private static final String ACTIONS_FOLDER = "ProjectsTabActions"; // NOI18N
    
    private ResourceBundle bundle;
    private final int type;
    
    public ProjectsRootNode( int type ) {
        super( new ProjectChildren( type ) ); 
        setIconBaseWithExtension( ICON_BASE );
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
            List<Action> actions = new ArrayList<Action>();
            for (Object o : Lookups.forPath(ACTIONS_FOLDER).lookupAll(Object.class)) {
                if (o instanceof Action) {
                    actions.add((Action) o);
                } else if (o instanceof JSeparator) {
                    actions.add(null);
                }
            }
            return actions.toArray(new Action[actions.size()]);
        }
    }
    
    /** Finds node for given object in the view
     * @return the node or null if the node was not found
     */
    Node findNode(FileObject target) {        
        
        ProjectChildren ch = (ProjectChildren)getChildren();
        
        if ( ch.type == LOGICAL_VIEW ) {
            // Speed up search in case we have an owner project - look in its node first.
            Project ownerProject = FileOwnerQuery.getOwner(target);
            for (int lookOnlyInOwnerProject = (ownerProject != null) ? 0 : 1; lookOnlyInOwnerProject < 2; lookOnlyInOwnerProject++) {
                for (Node node : ch.getNodes(true)) {
                    Project p = node.getLookup().lookup(Project.class);
                    assert p != null : "Should have had a Project in lookup of " + node;
                    if (lookOnlyInOwnerProject == 0 && p != ownerProject) {
                        continue; // but try again (in next outer loop) as a fallback
                    }
                    LogicalViewProvider lvp = p.getLookup().lookup(LogicalViewProvider.class);
                    if (lvp != null) {
                        // XXX (cf. #63554): really should be calling this on DataObject usually, since
                        // DataNode does *not* currently have a FileObject in its lookup (should it?)
                        // ...but it is not clear who has implemented findPath to assume FileObject!
                        Node selectedNode = lvp.findPath(node, target);
                        if (selectedNode != null) {
                            return selectedNode;
                        }
                    }
                }
            }
            return null;
            
        }
        else if ( ch.type == PHYSICAL_VIEW ) {
            for (Node node : ch.getNodes(true)) {
                // XXX could do similar optimization as for LOGICAL_VIEW; every nodes[i] must have some Project in its lookup
                PhysicalView.PathFinder pf = node.getLookup().lookup(PhysicalView.PathFinder.class);
                if ( pf != null ) {
                    Node n = pf.findPath(node, target);
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
    static class ProjectChildren extends Children.Keys<ProjectChildren.Pair> implements ChangeListener, PropertyChangeListener {
        
        private java.util.Map <Sources,Reference<Project>> sources2projects = new WeakHashMap<Sources,Reference<Project>>();
        
        int type;
        
        public ProjectChildren( int type ) {
            this.type = type;
        }
        
        // Children.Keys impl --------------------------------------------------
        
        @Override
        public void addNotify() {         
            OpenProjectList.getDefault().addPropertyChangeListener(this);
            setKeys( getKeys() );
        }
        
        @Override
        public void removeNotify() {
            OpenProjectList.getDefault().removePropertyChangeListener(this);
            for (Sources sources : sources2projects.keySet()) {
                sources.removeChangeListener( this );                
            }
            sources2projects.clear();
            setKeys(Collections.<Pair>emptySet());
        }
        
        protected Node[] createNodes(Pair p) {
            Project project = p.project;
            
            Node origNodes[] = null;
            boolean[] projectInLookup = new boolean[1];
            projectInLookup[0] = true;
                        
            if ( type == PHYSICAL_VIEW ) {
                Sources sources = ProjectUtils.getSources( project );
                sources.removeChangeListener( this );
                sources.addChangeListener( this );
                sources2projects.put( sources, new WeakReference<Project>( project ) );
                origNodes = PhysicalView.createNodesForProject( project );
            } else {
                origNodes = new Node[] { logicalViewForProject(project, projectInLookup) };
            }

            Node[] badgedNodes = new Node[ origNodes.length ];
            for( int i = 0; i < origNodes.length; i++ ) {
                if ( type == PHYSICAL_VIEW && !PhysicalView.isProjectDirNode( origNodes[i] ) ) {
                    // Don't badge external sources
                    badgedNodes[i] = origNodes[i];
                }
                else {
                    badgedNodes[i] = new BadgingNode(
                        p,
                        origNodes[i],
                        type == LOGICAL_VIEW  && projectInLookup[0],
                        type == LOGICAL_VIEW
                    );
                }
            }
                        
            return badgedNodes;
        }        
        
        final Node logicalViewForProject(Project project, boolean[] projectInLookup) {
            Node node;
            
            LogicalViewProvider lvp = project.getLookup().lookup(LogicalViewProvider.class);
            
            if ( lvp == null ) {
                ErrorManager.getDefault().log(ErrorManager.WARNING, "Warning - project " + ProjectUtils.getInformation(project).getName() + " failed to supply a LogicalViewProvider in its lookup"); // NOI18N
                Sources sources = ProjectUtils.getSources(project);
                sources.removeChangeListener(this);
                sources.addChangeListener(this);
                Node[] physical = PhysicalView.createNodesForProject(project);
                if (physical.length > 0) {
                    node = physical[0];
                } else {
                    node = Node.EMPTY;
                }
            } else {
                node = lvp.createLogicalView();
                if (node.getLookup().lookup(Project.class) != project) {
                    // Various actions, badging, etc. are not going to work.
                    ErrorManager.getDefault().log(ErrorManager.WARNING, "Warning - project " + ProjectUtils.getInformation(project).getName() + " failed to supply itself in the lookup of the root node of its own logical view"); // NOI18N
                    //#114664
                    if (projectInLookup != null) {
                        projectInLookup[0] = false;
                    }
                }
            }
            
            return node;
        }
        
        // PropertyChangeListener impl -----------------------------------------
        
        public void propertyChange( PropertyChangeEvent e ) {
            if ( OpenProjectList.PROPERTY_OPEN_PROJECTS.equals( e.getPropertyName() ) ) {
                setKeys( getKeys() );
            }
        }
        
        // Change listener impl ------------------------------------------------
        
        public void stateChanged( ChangeEvent e ) {
            
            Reference<Project> projectRef = sources2projects.get(e.getSource());
            if ( projectRef == null ) {
                return;
            }
            
            final Project project = projectRef.get();
            
            if ( project == null ) {
                return;
            }
            
            // Fix for 50259, callers sometimes hold locks
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    refresh(project);
                }
            } );
        }
        
        final void refresh(Project p) {
            refreshKey( new Pair(p, p.getProjectDirectory()) );
        }
                                
        // Own methods ---------------------------------------------------------
        
        public Collection<Pair> getKeys() {
            List<Project> projects = Arrays.asList( OpenProjectList.getDefault().getOpenProjects() );
            Collections.sort( projects, OpenProjectList.PROJECT_BY_DISPLAYNAME );
            
            List<Pair> dirs = Arrays.asList( new Pair[projects.size()] );
            
            for (int i = 0; i < projects.size(); i++) {
                Project project = projects.get(i);
                dirs.set(i, new Pair(project, project.getProjectDirectory()));
            }

            
            return dirs;
        }
        
        /** Object that comparers two projects just by their directory.
         * This allows to replace a LazyProject with real one without discarding
         * the nodes.
         */
        private static final class Pair extends Object {
            public Project project;
            public final FileObject fo;

            public Pair(Project project, FileObject fo) {
                this.project = project;
                this.fo = fo;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                final Pair other = (Pair) obj;
                if (this.fo != other.fo && (this.fo == null || !this.fo.equals(other.fo))) {
                    return false;
                }
                return true;
            }

            @Override
            public int hashCode() {
                int hash = 7;
                hash = 53 * hash + (this.fo != null ? this.fo.hashCode() : 0);
                return hash;
            }
        }
                                                
    }
        
    private static final class BadgingNode extends FilterNode implements PropertyChangeListener, Runnable, FileStatusListener {

        private static String badgedNamePattern = NbBundle.getMessage(ProjectsRootNode.class, "LBL_MainProject_BadgedNamePattern");
        private final FileObject file;
        private final Set<FileObject> files;
        private FileStatusListener fileSystemListener;
        private RequestProcessor.Task task;
        private volatile boolean nameChange;
        private final boolean logicalView;
        private final ProjectChildren.Pair pair;

        public BadgingNode(ProjectChildren.Pair p, Node n, boolean addSearchInfo, boolean logicalView) {
            super(n, null, badgingLookup(n, addSearchInfo));
            this.pair = p;
            this.logicalView = logicalView;
            OpenProjectList.getDefault().addPropertyChangeListener(WeakListeners.propertyChange(this, OpenProjectList.getDefault()));
            Project proj = getOriginal().getLookup().lookup(Project.class);
            if (proj != null) {
                file = proj.getProjectDirectory();
                assert file != null : "Project returns null directory: " + proj;
                files = Collections.singleton(file);
                try {
                    FileSystem fs = file.getFileSystem();
                    fileSystemListener = FileUtil.weakFileStatusListener(this, fs);
                    fs.addFileStatusListener(fileSystemListener);
                } catch (FileStateInvalidException e) {
                    ErrorManager err = ErrorManager.getDefault();
                    err.annotate(e, "Can not get " + file + " filesystem, ignoring..."); // NO18N
                    err.notify(ErrorManager.INFORMATIONAL, e);
                }
            } else {
                file = null;
                files = null; 
            }
        }
        
        private static Lookup badgingLookup(Node n, boolean addSearchInfo) {
            if (addSearchInfo) {
                return new BadgingLookup(n.getLookup(), Lookups.singleton(alwaysSearchableSearchInfo(n.getLookup().lookup(Project.class))));
            } else {
                return new BadgingLookup(n.getLookup());
            }
        }
        
        public void run() {
            if (nameChange) {
                fireDisplayNameChange(null, null);
                nameChange = false;
            }
        }

        public void annotationChanged(FileStatusEvent event) {
            if (task == null) {
                task = RequestProcessor.getDefault().create(this);
            }

            if (nameChange == false && event.isNameChange()) {
                if (event.hasChanged(file)) {
                    nameChange |= event.isNameChange();
                }
            }

            task.schedule(50); // batch by 50 ms
        }


        
        public String getDisplayName() {
            String original = super.getDisplayName();
            Project proj = getOriginal().getLookup().lookup(Project.class);
            if (proj != null) {
                try {            
                    original = proj.getProjectDirectory().getFileSystem().getStatus().annotateName(original, Collections.singleton(proj.getProjectDirectory()));
                } catch (FileStateInvalidException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }           
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
            Project proj = getOriginal().getLookup().lookup(Project.class);
            if (proj != null) {
                try {
                    FileSystem.Status stat = proj.getProjectDirectory().getFileSystem().getStatus();
                    if (stat instanceof FileSystem.HtmlStatus) {
                        FileSystem.HtmlStatus hstat = (FileSystem.HtmlStatus) stat;

                        String result = hstat.annotateNameHtml(super.getDisplayName(), Collections.singleton(proj.getProjectDirectory()));
                        //Make sure the super string was really modified
                        if (result != null && !super.getDisplayName().equals(result)) {
                           return isMain() ? "<b>" + (result) + "</b>" : result; //NOI18N
                        }
                    }
                } catch (FileStateInvalidException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }      
            return isMain() ? "<b>" + (htmlName == null ? dispName : htmlName) + "</b>" : htmlName; //NOI18N
        }

        public void propertyChange( PropertyChangeEvent e ) {
            if ( OpenProjectList.PROPERTY_MAIN_PROJECT.equals( e.getPropertyName() ) ) {
                fireDisplayNameChange( null, null );
            }
            if ( OpenProjectList.PROPERTY_REPLACE.equals(e.getPropertyName())) {
                OpenProjectList.LOGGER.log(Level.FINER, "replacing for {0}", this);
                Project p = getLookup().lookup(Project.class);
                if (p == null) {
                    OpenProjectList.LOGGER.log(Level.FINE, "no project in lookup {0}", this);
                    return;
                }
                FileObject fo = p.getProjectDirectory();
                Project newProj = (Project)e.getNewValue();
                assert newProj != null;
                if (newProj.getProjectDirectory().equals(fo)) {
                    ProjectChildren ch = (ProjectChildren)getParentNode().getChildren();
                    Node n = null;
                    if (logicalView) {
                        n = ch.logicalViewForProject(newProj, null);
                        OpenProjectList.LOGGER.log(Level.FINER, "logical view {0}", n);
                    } else {
                        Node[] arr = PhysicalView.createNodesForProject(newProj);
                        OpenProjectList.LOGGER.log(Level.FINER, "physical view {0}", Arrays.asList(arr));
                        if (arr.length > 1) {
                            pair.project = newProj;
                            OpenProjectList.LOGGER.log(Level.FINER, "refreshing for {0}", newProj);
                            ch.refresh(newProj);
                            OpenProjectList.LOGGER.log(Level.FINER, "refreshed for {0}", newProj);
                            return;
                        }
                        for (Node one : arr) {
                            if (PhysicalView.isProjectDirNode(one)) {
                                n = one;
                                break;
                            }
                        }
                        assert n != null;
                    }
                    OpenProjectList.LOGGER.log(Level.FINER, "change original: {0}", n);
                    changeOriginal(n, true);

                    BadgingLookup bl = (BadgingLookup)getLookup();
                    if (bl.isSearchInfo()) {
                        OpenProjectList.LOGGER.log(Level.FINER, "is search info {0}", bl);
                        bl.setMyLookups(n.getLookup(), Lookups.singleton(alwaysSearchableSearchInfo(newProj)));
                    } else {
                        OpenProjectList.LOGGER.log(Level.FINER, "no search info {0}", bl);
                        bl.setMyLookups(n.getLookup());
                    }
                    OpenProjectList.LOGGER.log(Level.FINER, "done {0}", this);
                } else {
                    OpenProjectList.LOGGER.log(Level.FINE, "wrong directories. current: " + fo + " new " + newProj.getProjectDirectory());
                }
            }
        }

        private boolean isMain() {
            Project p = getLookup().lookup(Project.class);
            return p != null && OpenProjectList.getDefault().isMainProject( p );
        }
        
    } // end of BadgingNode
    
    private static final class BadgingLookup extends ProxyLookup {
        public BadgingLookup(Lookup... lkps) {
            super(lkps);
        }
        public void setMyLookups(Lookup... lkps) {
            setLookups(lkps);
        }
        public boolean isSearchInfo() {
            return getLookups().length > 1;
        }
    } // end of BadgingLookup
    
    /**
     * Produce a {@link SearchInfo} variant that is always searchable, for speed.
     * @see "#48685"
     */
    static SearchInfo alwaysSearchableSearchInfo(Project p) {
        return new AlwaysSearchableSearchInfo(p);
    }
    
    private static final class AlwaysSearchableSearchInfo implements SearchInfo {
        
        private final SearchInfo delegate;
        
        public AlwaysSearchableSearchInfo(Project prj) {
            SearchInfo projectSearchInfo = prj.getLookup().lookup(SearchInfo.class);
            if (projectSearchInfo != null) {
                delegate = projectSearchInfo;
            } else {
                SourceGroup groups[] = ProjectUtils.getSources(prj).getSourceGroups(Sources.TYPE_GENERIC);
                FileObject folders[] = new FileObject[groups.length];
                for (int i = 0; i < groups.length; i++) {
                    folders[i] = groups[i].getRootFolder();
                }
                delegate = SearchInfoFactory.createSearchInfo(
                        folders,
                        true,
                        new FileObjectFilter[] {SearchInfoFactory.VISIBILITY_FILTER});
            }
        }

        public boolean canSearch() {
            return true;
        }

        public Iterator<DataObject> objectsToSearch() {
            return delegate.objectsToSearch();
        }
        
    }
    
}
