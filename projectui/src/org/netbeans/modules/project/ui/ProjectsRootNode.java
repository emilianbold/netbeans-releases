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

import java.awt.EventQueue;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
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
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.xml.XMLUtil;
import org.openidex.search.FileObjectFilter;
import org.openidex.search.SearchInfo;
import org.openidex.search.SearchInfoFactory;
import org.openidex.search.Utils;

/** Root node for list of open projects
 */
public class ProjectsRootNode extends AbstractNode {

    private static final Logger LOG = Logger.getLogger(ProjectsRootNode.class.getName());
    private static final Set<ProjectsRootNode> all = new WeakSet<ProjectsRootNode>();

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
        synchronized(all){
            all.add(this);
        }
    }
        
    @Override
    public String getName() {
        return ( "OpenProjects" ); // NOI18N
    }
    
    @Override
    public String getDisplayName() {
        if ( this.bundle == null ) {
            this.bundle = NbBundle.getBundle( ProjectsRootNode.class );
        }
        return bundle.getString( "LBL_OpenProjectsNode_Name" ); // NOI18N
    }
    
    @Override
    public boolean canRename() {
        return false;
    }
        
    @Override
    public Node.Handle getHandle() {        
        return new Handle(type);
    }
    
    @Override
    public Action[] getActions( boolean context ) {
        if (context || type == PHYSICAL_VIEW) {
            return new Action[0];
        } else {
            List<? extends Action> actions = Utilities.actionsForPath(ACTIONS_FOLDER);
            return actions.toArray(new Action[actions.size()]);
        }
    }
    
    /** Finds node for given object in the view
     * @return the node or null if the node was not found
     */
    Node findNode(FileObject target) {        

        ProjectChildren ch = (ProjectChildren)getChildren();

        assert ((ch.type == LOGICAL_VIEW) || (ch.type == PHYSICAL_VIEW));
        // Speed up search in case we have an owner project - look in its node first.
        Project ownerProject = FileOwnerQuery.getOwner(target);
        for (int lookOnlyInOwnerProject = (ownerProject != null) ? 0 : 1; lookOnlyInOwnerProject < 2; lookOnlyInOwnerProject++) {
            for (Node node : ch.getNodes(true)) {
                Project p = node.getLookup().lookup(Project.class);
                assert p != null : "Should have had a Project in lookup of " + node;
                if (lookOnlyInOwnerProject == 0 && p != ownerProject) {
                    continue; // but try again (in next outer loop) as a fallback
                }
                Node n = null;
                if (ch.type == LOGICAL_VIEW) {
                    LogicalViewProvider lvp = p.getLookup().lookup(LogicalViewProvider.class);
                    if (lvp != null) {
                        // XXX (cf. #63554): really should be calling this on DataObject usually, since
                        // DataNode does *not* currently have a FileObject in its lookup (should it?)
                        // ...but it is not clear who has implemented findPath to assume FileObject!
                        n = lvp.findPath(node, target);
                    }
                } else if (ch.type == PHYSICAL_VIEW){
                    PhysicalView.PathFinder pf = node.getLookup().lookup(PhysicalView.PathFinder.class);
                    if ( pf != null ) {
                        n = pf.findPath(node, target);
                    }
                }
                if ( n != null ) {
                    return n;
                }
            }
        }
        return null;
    }

    static void checkNoLazyNode(Object msg) {
        synchronized(all){
            for (ProjectsRootNode root : all) {
                for (Node n : root.getChildren().getNodes()) {
                    if (n instanceof BadgingNode) {
                        ((BadgingNode)n).replaceProject(null);
                    }

                    if (n.getLookup().lookup(LazyProject.class) != null) {
                        LogRecord REC = new LogRecord(Level.WARNING, "LazyProjects remain visible:\n {0}"); // NOI18N
                        REC.setLoggerName(OpenProjectList.LOGGER.getName());
                        REC.setParameters(new Object[] { msg });
                        OpenProjectList.log(REC);
                    }
                }
            }
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

        @Override
        public int getNodesCount(boolean optimalResult) {
            if (optimalResult) {
                setKeys(getKeys());
            }
            return super.getNodesCount(optimalResult);
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
                        this,
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
                LOG.warning("Warning - project of " + project.getClass() + " in " + FileUtil.getFileDisplayName(project.getProjectDirectory()) + " failed to supply a LogicalViewProvider in its lookup"); // NOI18N
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
                if (!project.equals(node.getLookup().lookup(Project.class))) {
                    // Various actions, badging, etc. are not going to work.
                    LOG.warning("Warning - project " + ProjectUtils.getInformation(project).getName() + " failed to supply itself in the lookup of the root node of its own logical view"); // NOI18N
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
            refreshKey(new Pair(p));
        }
                                
        // Own methods ---------------------------------------------------------
        
        public Collection<Pair> getKeys() {
            List<Project> projects = Arrays.asList( OpenProjectList.getDefault().getOpenProjects() );
            Collections.sort( projects, OpenProjectList.PROJECT_BY_DISPLAYNAME );
            
            List<Pair> dirs = Arrays.asList( new Pair[projects.size()] );
            
            for (int i = 0; i < projects.size(); i++) {
                Project project = projects.get(i);
                dirs.set(i, new Pair(project));
            }

            
            return dirs;
        }
        
        /** Object that comparers two projects just by their directory.
         * This allows to replace a LazyProject with real one without discarding
         * the nodes.
         */
        static final class Pair extends Object {
            public Project project;
            public final FileObject fo;

            public Pair(Project project) {
                this.project = project;
                this.fo = project.getProjectDirectory();
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

    static final class BadgingNode extends FilterNode implements ChangeListener, PropertyChangeListener, Runnable, FileStatusListener {

        private static String badgedNamePattern = NbBundle.getMessage(ProjectsRootNode.class, "LBL_MainProject_BadgedNamePattern");
        private final Object privateLock = new Object();
        private Set<FileObject> files;
        private Map<FileSystem,FileStatusListener> fileSystemListeners;
        private ChangeListener sourcesListener;
        private Map<SourceGroup,PropertyChangeListener> groupsListeners;
        RequestProcessor.Task task;
        private boolean nameChange;
        private boolean iconChange;
        private final ProjectChildren ch;
        private final boolean logicalView;
        private final ProjectChildren.Pair pair;
        private final Set<FileObject> projectDirsListenedTo = new WeakSet<FileObject>();
        private static final int DELAY = 50;
        private final FileChangeListener newSubDirListener = new FileChangeAdapter() {
            public @Override void fileDataCreated(FileEvent fe) {
                if (Boolean.getBoolean("test.nodelay")) { //for tests only
                    setProjectFiles();
                    return ;
                }
                fsRefreshTask.schedule(DELAY);
            }
            public @Override void fileFolderCreated(FileEvent fe) {
                if (Boolean.getBoolean("test.nodelay")) { //for tests only
                    setProjectFiles();
                    return ;
                }
                fsRefreshTask.schedule(DELAY);
            }
        };
        private final RequestProcessor.Task fsRefreshTask = Hacks.RP.create(new Runnable() {
            public void run() {
                setProjectFiles();
            }
        });

        public BadgingNode(ProjectChildren ch, ProjectChildren.Pair p, Node n, boolean addSearchInfo, boolean logicalView) {
            super(n, null, badgingLookup(n, addSearchInfo));
            this.ch = ch;
            this.pair = p;
            this.logicalView = logicalView;
            OpenProjectList.log(Level.FINER, "BadgingNode init {0}", toStringForLog()); // NOI18N
            OpenProjectList.getDefault().addPropertyChangeListener(WeakListeners.propertyChange(this, OpenProjectList.getDefault()));
            setProjectFiles();
            OpenProjectList.log(Level.FINER, "BadgingNode finished {0}", toStringForLog()); // NOI18N
        }
        
        private static Lookup badgingLookup(Node n, boolean addSearchInfo) {
            if (addSearchInfo) {
                return new BadgingLookup(n.getLookup(), Lookups.singleton(alwaysSearchableSearchInfo(n.getLookup().lookup(Project.class))));
            } else {
                return new BadgingLookup(n.getLookup());
            }
        }
        
        protected final void setProjectFiles() {
            Project prj = getLookup().lookup(Project.class);

            if (prj != null && /* #145682 */ !(prj instanceof LazyProject)) {
                setProjectFiles(prj);
            }
        }

        private void replaceProject(Project newProj) {
            if (newProj == null) {
                try {
                    newProj = ProjectManager.getDefault().findProject(pair.fo);
                    if (newProj == pair.project) {
                        return;
                    }
                } catch (IOException ex) {
                    OpenProjectList.log(Level.INFO, "No project for " + pair.fo, ex); // NOI18N
                } catch (IllegalArgumentException ex) {
                    OpenProjectList.log(Level.INFO, "No project for " + pair.fo, ex); // NOI18N
                }

            }
            
            OpenProjectList.log(Level.FINER, "replacing for {0}", toStringForLog());
            Project p = getLookup().lookup(Project.class);
            if (p == null) {
                OpenProjectList.log(Level.FINE, "no project in lookup {0}", toStringForLog());
                return;
            }
            FileObject fo = p.getProjectDirectory();
            if (newProj != null && newProj.getProjectDirectory().equals(fo)) {
                Node n = null;
                if (logicalView) {
                    n = ch.logicalViewForProject(newProj, null);
                    OpenProjectList.log(Level.FINER, "logical view {0}", n);
                } else {
                    Node[] arr = PhysicalView.createNodesForProject(newProj);
                    OpenProjectList.log(Level.FINER, "physical view {0}", Arrays.asList(arr));
                    if (arr.length > 1) {
                        pair.project = newProj;
                        OpenProjectList.log(Level.FINER, "refreshing for {0}", newProj);
                        ch.refresh(newProj);
                        OpenProjectList.log(Level.FINER, "refreshed for {0}", newProj);
                        return;
                    }
                    for (Node one : arr) {
                        if (PhysicalView.isProjectDirNode(one)) {
                            n = one;
                            break;
                        }
                    }
                    if (n == null) {
                        OpenProjectList.log(Level.WARNING, "newProject yields null node: " + newProj);
                        n = Node.EMPTY;
                    }
                }
                OpenProjectList.log(Level.FINER, "change original: {0}", n);
                OpenProjectList.log(Level.FINER, "children before change original: {0}", getChildren());
                OpenProjectList.log(Level.FINER, "delegate children before change original: {0}", getOriginal().getChildren());
                changeOriginal(n, true);
                OpenProjectList.log(Level.FINER, "delegate after change original: {0}", getOriginal());
                OpenProjectList.log(Level.FINER, "name after change original: {0}", getName());
                OpenProjectList.log(Level.FINER, "children after change original: {0}", getChildren());
                OpenProjectList.log(Level.FINER, "delegate children after change original: {0}", getOriginal().getChildren());
                BadgingLookup bl = (BadgingLookup) getLookup();
                if (bl.isSearchInfo()) {
                    OpenProjectList.log(Level.FINER, "is search info {0}", newProj);
                    bl.setMyLookups(n.getLookup(), Lookups.singleton(alwaysSearchableSearchInfo(newProj)));
                } else {
                    OpenProjectList.log(Level.FINER, "no search info {0}", newProj);
                    bl.setMyLookups(n.getLookup());
                }
                OpenProjectList.log(Level.FINER, "done {0}", toStringForLog());
                setProjectFiles();
            } else {
                FileObject newDir;
                if (newProj != null) {
                    newDir = newProj.getProjectDirectory();
                } else {
                    newDir = null;
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            OpenProjectList.getDefault().close(new Project[] { pair.project }, false);
                        }
                    });
                }
                OpenProjectList.log(Level.FINE, "wrong directories. current: " + fo + " new " + newDir);
            }
        }

        private final void setProjectFiles(Project project) {
            Sources sources = ProjectUtils.getSources(project);  // returns singleton
            if (sourcesListener == null) {
                sourcesListener = WeakListeners.change(this, sources);
                sources.addChangeListener(sourcesListener);
            }
            setGroups(Arrays.asList(sources.getSourceGroups(Sources.TYPE_GENERIC)), project.getProjectDirectory());
        }

        private final void setGroups(Collection<SourceGroup> groups, FileObject projectDirectory) {
            if (groupsListeners != null) {
                for (Map.Entry<SourceGroup, PropertyChangeListener> entry : groupsListeners.entrySet()) {
                    entry.getKey().removePropertyChangeListener(entry.getValue());
                }
            }
            Map<SourceGroup,PropertyChangeListener> _groupsListeners = new HashMap<SourceGroup, PropertyChangeListener>();
            Set<FileObject> roots = new HashSet<FileObject>();
            for (SourceGroup group : groups) {
                PropertyChangeListener pcl = WeakListeners.propertyChange(this, group);
                _groupsListeners.put(group, pcl);
                group.addPropertyChangeListener(pcl);
                FileObject fo = group.getRootFolder();
                if (fo.equals(projectDirectory)) {
                    // #78994: do not listen to project root folder since changes in a nested project will mark it as modified.
                    // Instead, listen to direct subdirs which are owned by this project. Not very precise but the best we can do.
                    // (Would ideally obtain a complete but minimal list of dirs which cover this project but no subprojects.
                    // Unfortunately the current APIs provide no efficient way of doing this in general.)
                    for (FileObject kid : fo.getChildren()) {
                        Project owner = FileOwnerQuery.getOwner(kid);
                        // Not sufficient to check owner == project, because at startup owner will be a LazyProject.
                        if (owner != null && owner.getProjectDirectory() == projectDirectory) {
                            roots.add(kid);
                        }
                    }
                    if (projectDirsListenedTo.add(fo)) {
                        fo.addFileChangeListener(FileUtil.weakFileChangeListener(newSubDirListener, fo));
                    }
                } else {
                    roots.add(fo);
                }
            }
            groupsListeners = _groupsListeners;
            setFiles(roots);
        }

        protected final void setFiles(Set<FileObject> files) {
            if (fileSystemListeners != null) {
                for (Map.Entry<FileSystem, FileStatusListener> entry : fileSystemListeners.entrySet()) {
                    entry.getKey().removeFileStatusListener(entry.getValue());
                }
            }

            fileSystemListeners = new HashMap<FileSystem, FileStatusListener>();
            this.files = files;

            Set<FileSystem> hookedFileSystems = new HashSet<FileSystem>();
            for (FileObject fo : files) {
                try {
                    FileSystem fs = fo.getFileSystem();
                    if (hookedFileSystems.contains(fs)) {
                        continue;
                    }
                    hookedFileSystems.add(fs);
                    FileStatusListener fsl = FileUtil.weakFileStatusListener(this, fs);
                    fs.addFileStatusListener(fsl);
                    fileSystemListeners.put(fs, fsl);
                } catch (FileStateInvalidException e) {
                    LOG.log(Level.INFO, "Cannot get " + fo + " filesystem, ignoring...", e); // NOI18N
                }
            }
        }

        public void run() {
            boolean fireIcon;
            boolean fireName;
            synchronized (privateLock) {
                fireIcon = iconChange;
                fireName = nameChange;
                iconChange = false;
                nameChange = false;
            }
            if (fireIcon) {
                fireIconChange();
                fireOpenedIconChange();
            }
            if (fireName) {
                fireDisplayNameChange(null, null);
            }
        }

        public void annotationChanged(FileStatusEvent event) {
            if (task == null) {
                task = Hacks.RP.create(this);
            }

            synchronized (privateLock) {
                if ((iconChange == false && event.isIconChange())  || (nameChange == false && event.isNameChange())) {
                    for (FileObject fo : files) {
                        if (event.hasChanged(fo)) {
                            iconChange |= event.isIconChange();
                            nameChange |= event.isNameChange();
                        }
                    }
                }
            }

            task.schedule(50);  // batch by 50 ms
        }
    
        public @Override String getDisplayName() {
            String original = super.getDisplayName();
            if (files != null && files.iterator().hasNext()) {
                try {
                    original = files.iterator().next().getFileSystem().getStatus().annotateName(original, files);
                } catch (FileStateInvalidException e) {
                    LOG.log(Level.INFO, null, e);
                }
            }
            return isMain() ? MessageFormat.format( badgedNamePattern, new Object[] { original } ) : original;
        }

        /** Get display name used for logging as original display name can cause deadlock issue #160512 */
        private String getLogName() {
            String original = super.getDisplayName();
            if (files != null && files.iterator().hasNext()) {
                try {
                    original = files.iterator().next().getFileSystem().getStatus().annotateName(original, files);
                } catch (FileStateInvalidException e) {
                    LOG.log(Level.INFO, null, e);
                }
            }
            return original;
        }
        
        /** Special version of to Strign used for logging as original toString uses display name
         * => can cause deadlock issue #160512 */
        private String toStringForLog() {
            return getClass().getName() + "@" + Integer.toHexString(hashCode()) //NOI18N
                   + "[Name=" + getName() + ", displayName=" + getLogName() + "]"; //NOI18N
        }

        public @Override String getHtmlDisplayName() {
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
            if (files != null && files.iterator().hasNext()) {
                try {
                    FileSystem.Status stat = files.iterator().next().getFileSystem().getStatus();
                    if (stat instanceof FileSystem.HtmlStatus) {
                        FileSystem.HtmlStatus hstat = (FileSystem.HtmlStatus) stat;

                        String result = hstat.annotateNameHtml(super.getDisplayName(), files);
                        //Make sure the super string was really modified
                        if (result != null && !super.getDisplayName().equals(result)) {
                           return isMain() ? "<b>" + (result) + "</b>" : result; //NOI18N
                        }
                    }
                } catch (FileStateInvalidException e) {
                    LOG.log(Level.INFO, null, e);
                }
            }      
            return isMain() ? "<b>" + (htmlName == null ? dispName : htmlName) + "</b>" : htmlName; //NOI18N
        }

        public @Override Image getIcon(int type) {
            Image img = super.getIcon(type);

            if (logicalView && files != null && files.iterator().hasNext()) {
                try {
                    FileObject fo = files.iterator().next();
                    img = fo.getFileSystem().getStatus().annotateIcon(img, type, files);
                } catch (FileStateInvalidException e) {
                    LOG.log(Level.INFO, null, e);
                }
            }

            return img;
        }

        public @Override Image getOpenedIcon(int type) {
            Image img = super.getOpenedIcon(type);

            if (logicalView && files != null && files.iterator().hasNext()) {
                try {
                    FileObject fo = files.iterator().next();
                    img = fo.getFileSystem().getStatus().annotateIcon(img, type, files);
                } catch (FileStateInvalidException e) {
                    LOG.log(Level.INFO, null, e);
                }
            }

            return img;
        }

        public void propertyChange( PropertyChangeEvent e ) {
            if ( OpenProjectList.PROPERTY_MAIN_PROJECT.equals( e.getPropertyName() ) ) {
                fireDisplayNameChange( null, null );
            }
            if ( OpenProjectList.PROPERTY_REPLACE.equals(e.getPropertyName())) {
                replaceProject((Project)e.getNewValue());
            }
            if (SourceGroup.PROP_CONTAINERSHIP.equals(e.getPropertyName())) {
                setProjectFiles();
            }
        }

        private boolean isMain() {
            Project p = getLookup().lookup(Project.class);
            return p != null && OpenProjectList.getDefault().isMainProject( p );
        }
        
        // sources change
        public void stateChanged(ChangeEvent e) {
            fsRefreshTask.schedule(DELAY);
        }

        @Override
        public Object getValue(String attributeName) {
            if ("customDelete".equals(attributeName)) {
                return true;
            }
            return super.getValue(attributeName);
        }

        @Override
        public boolean canDestroy() {
            Project p = getLookup().lookup(Project.class);
            if (p == null) {
                return false;
            }
            ActionProvider ap = p.getLookup().lookup(ActionProvider.class);

            String[] sa = ap != null ? ap.getSupportedActions() : new String[0];
            int k = sa.length;

            for (int i = 0; i < k; i++) {
                if (ActionProvider.COMMAND_DELETE.equals(sa[i])) {
                    return ap.isActionEnabled(ActionProvider.COMMAND_DELETE, getLookup());
                }
            }
            return false;
        }

        @Override
        public void destroy() throws IOException {
            Project p = getLookup().lookup(Project.class);
            if (p == null) {
                return;
            }
            ActionProvider ap = p.getLookup().lookup(ActionProvider.class);
            ap.invokeAction(ActionProvider.COMMAND_DELETE, getLookup());
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
    
    private static final class AlwaysSearchableSearchInfo implements SearchInfo.Files {
        
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
                        new FileObjectFilter[] { SearchInfoFactory.VISIBILITY_FILTER,
                                                 SearchInfoFactory.SHARABILITY_FILTER });
            }
        }

        public boolean canSearch() {
            return true;
        }

        public Iterator<DataObject> objectsToSearch() {
            return delegate.objectsToSearch();
        }
        
        public Iterator<FileObject> filesToSearch() {
            return Utils.getFileObjectsIterator(delegate);
        }
        
    }
}
