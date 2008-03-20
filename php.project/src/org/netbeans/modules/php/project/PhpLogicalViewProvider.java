
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
 * Contributor(s): The Original Software is NetBeans. The Initial
 * Developer of the Original Software is Sun Microsystems, Inc. Portions
 * Copyright 1997-2006 Sun Microsystems, Inc. All Rights Reserved.
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
package org.netbeans.modules.php.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.php.rt.spi.providers.Command;
import org.netbeans.modules.php.rt.spi.providers.CommandProvider;
import org.netbeans.modules.php.rt.spi.providers.WebServerProvider;
import org.netbeans.modules.php.rt.utils.PhpCommandUtils;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.Models.ActionPerformer;
import org.openide.ErrorManager;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.FileSystemAction;
import org.openide.actions.FindAction;
import org.openide.actions.OpenAction;
import org.openide.actions.PasteAction;
import org.openide.actions.RenameAction;
import org.openide.actions.SaveAsTemplateAction;
import org.openide.actions.ToolsAction;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 * @author ads
 */
class PhpLogicalViewProvider implements LogicalViewProvider, AntProjectListener {

    static final String SOURCE_ROOT_NODE_NAME = "LBL_PhpFiles";

    PhpLogicalViewProvider(PhpProject project, SubprojectProvider provider) {
        myProject = project;
        mySubProjectProvider = provider;
        myActionsByCommand = new HashMap<Command, Action>();
        getProject().getHelper().addAntProjectListener(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.netbeans.spi.project.ui.LogicalViewProvider#createLogicalView()
     */
    public Node createLogicalView() {
        return new PhpLogicalViewRootNode();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.netbeans.spi.project.ui.LogicalViewProvider#findPath(org.openide.nodes.Node,
     *      java.lang.Object)
     */
    @SuppressWarnings(value = "unchecked")
    public Node findPath(Node root, Object target) {
        // Check each child node in turn.
        Node[] children = root.getChildren().getNodes(true);
        for (Node node : children) {
            if (target instanceof DataObject || target instanceof FileObject) {
                DataObject d = node.getLookup().lookup(DataObject.class);
                if (d == null) {
                    continue;
                }
                // Copied from
                // org.netbeans.spi.java.project.support.ui.TreeRootNode.PathFinder.findPath:
                FileObject kidFO = d.getPrimaryFile();
                FileObject targetFO = target instanceof DataObject ? ((DataObject) target).getPrimaryFile() : (FileObject) target;
                if (kidFO == targetFO) {
                    return node;
                } else if (FileUtil.isParentOf(kidFO, targetFO)) {
                    String relPath = FileUtil.getRelativePath(kidFO, targetFO);
                    List path = Collections.list(new /*<String>*/ StringTokenizer(relPath, "/")); // NOI18N
                    // XXX see original code for justification
                    path.set(path.size() - 1, targetFO.getName());
                    try {
                        Node found = NodeOp.findPath(node, Collections.enumeration(path));

                        if (hasObject(found, target)) {
                            return found;
                        }
                        Node parent = found.getParentNode();
                        Children kids = parent.getChildren();
                        children = kids.getNodes();
                        for (Node child : children) {
                            if (hasObject(child, target)) {
                                return child;
                            }
                        }
                    } catch (NodeNotFoundException e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.project.support.ant.AntProjectListener#configurationXmlChanged(org.netbeans.spi.project.support.ant.AntProjectEvent)
     */
    public void configurationXmlChanged( AntProjectEvent event ) {
        myActionsByCommand.clear();
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.project.support.ant.AntProjectListener#propertiesChanged(org.netbeans.spi.project.support.ant.AntProjectEvent)
     */
    public void propertiesChanged( AntProjectEvent arg0 ) {
        myActionsByCommand.clear();
    }
    
    private boolean hasObject(Node node, Object obj) {
        if (obj == null) {
            return false;
        }
        DataObject dataObject = node.getLookup().lookup(DataObject.class);
        if (dataObject == null) {
            return false;
        }
        if (obj instanceof DataObject) {
            if (dataObject.equals(obj)) {
                return true;
            }
            FileObject fileObject = ((DataObject) obj).getPrimaryFile();
            return hasObject(node, fileObject);
        } else if (obj instanceof FileObject) {
            FileObject fileObject = dataObject.getPrimaryFile();
            return obj.equals(fileObject);
        } else {
            return false;
        }
    }

    private PhpProject getProject() {
        return myProject;
    }

    private SubprojectProvider getSubProjectProvider() {
        return mySubProjectProvider;
    }

    private class PhpLogicalViewRootNode extends AbstractNode {

        private PhpLogicalViewRootNode() {
            super(new LogicalViewChildren(), Lookups.fixed(new Object[]{getProject()}));
            
            setIconBaseWithExtension(
                    ResourceMarker.getLocation() + ResourceMarker.PROJECT_ICON);
            
            setName(ProjectUtils.getInformation(myProject).getDisplayName());
        }

        @Override
        public Action[] getActions(boolean context) {
            if (context) {
                return super.getActions(context);
            } else {
                return getAdditionalActions();
            }
        }

        private Action[] getAdditionalActions() {
            List<Action> list = new LinkedList<Action>();

            list.add(0, CommonProjectActions.newFileAction());

            // actions from provider
            for (Action action : getProviderActions()){
                list.add(action);
            }

            // get our project actions
            for (Action action : getProjectActions()){
                list.add(action);
            }
                
            // get standard project actions
            for (Action action : getStandardProjectActions()){
                list.add(action);
            }
                
            return list.toArray(new Action[list.size()]);
        }

        private Action[] getProjectActions(){
            List<Action> list = new LinkedList<Action>();
            
            PhpActionProvider actionProvider 
                    = getProject().getLookup().lookup(PhpActionProvider.class);
            for (Command command : actionProvider.getProjectCommands()) {
                String id = command.getId();
                String label = command.getLabel();
                list.add(ProjectSensitiveActions.projectCommandAction(id, label, null));
            }
            
            return list.toArray(new Action[]{});
        }
        
        private Action[] getStandardProjectActions(){
            // do not get keysList from actionProvider.getStandardProjectCommands()
            // because it will give only keysList of commands that we support outselves
            // and will not allow desired formatting
            return new Action[]{
                        null, 
                        CommonProjectActions.setAsMainProjectAction(), 
                        CommonProjectActions.openSubprojectsAction(), 
                        CommonProjectActions.closeProjectAction(), 
                        null, 
                        CommonProjectActions.renameProjectAction(), 
                        CommonProjectActions.moveProjectAction(), 
                        CommonProjectActions.copyProjectAction(), 
                        CommonProjectActions.deleteProjectAction(), 
                        null, 
                        CommonProjectActions.customizeProjectAction()
            };
            
        }
        
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(PhpLogicalViewProvider.class);
        }
    }

    private class LogicalViewChildren extends Children.Keys 
            implements FileChangeListener, ChangeListener, PropertyChangeListener
            //,FileStatusListener
    {

        private ChangeListener sourcesListener;
        private java.util.Map groupsListeners;
        //private HashMap<FileSystem, FileStatusListener> fileSystemListeners;

        /* (non-Javadoc)
         * @see org.openide.nodes.Children#addNotify()
         */
        @Override
        protected void addNotify() {
            super.addNotify();
            getProject().getHelper().getProjectDirectory().addFileChangeListener(this);
            createNodes();
        }

        /* (non-Javadoc)
         * @see org.openide.nodes.Children#removeNotify()
         */
        @Override
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
            getProject().getHelper().getProjectDirectory().removeFileChangeListener(this);
            super.removeNotify();
        }

        /* (non-Javadoc)
         * @see org.openide.nodes.Children.Keys#createNodes(java.lang.Object)
         */
        @Override
        protected Node[] createNodes(Object key) {
            Node node = null;
            if (key instanceof SourceGroup) {
                SourceGroup sourceGroup = (SourceGroup) key;
                DataFolder folder = getFolder(sourceGroup.getRootFolder());
                if (folder != null) {
                    /* no need to use sourceGroup.getDisplayName() while we have only one sourceRoot.
                     * Now it contains not good-looking label.
                     * We put label there in PhpSources.configureSources()
                     */
                    //node = new SrcNode(folder, sourceGroup.getDisplayName());
                    node = new SrcNode(folder);
                }
            }
            return node == null ? new Node[]{} : new Node[]{node};
        }

        /* (non-Javadoc)
         * @see org.openide.filesystems.FileChangeListener#fileAttributeChanged(org.openide.filesystems.FileAttributeEvent)
         */
        public void fileAttributeChanged(FileAttributeEvent arg0) {
        }

        /* (non-Javadoc)
         * @see org.openide.filesystems.FileChangeListener#fileChanged(org.openide.filesystems.FileEvent)
         */
        public void fileChanged(FileEvent arg0) {
        }

        /* (non-Javadoc)
         * @see org.openide.filesystems.FileChangeListener#fileDataCreated(org.openide.filesystems.FileEvent)
         */
        public void fileDataCreated(FileEvent arg0) {
        }

        /* (non-Javadoc)
         * @see org.openide.filesystems.FileChangeListener#fileDeleted(org.openide.filesystems.FileEvent)
         */
        public void fileDeleted(FileEvent arg0) {
        }

        /* (non-Javadoc)
         * @see org.openide.filesystems.FileChangeListener#fileFolderCreated(org.openide.filesystems.FileEvent)
         */
        public void fileFolderCreated(FileEvent arg0) {
            // is it useful for us? looks like copied from Enterprise/bpel
            // should invoke createNodes() only if updated file is not a source file
            //createNodes();
        }

        /* (non-Javadoc)
         * @see org.openide.filesystems.FileChangeListener#fileRenamed(org.openide.filesystems.FileRenameEvent)
         */
        public void fileRenamed(FileRenameEvent arg0) {
            // is it useful for us? looks like copied from Enterprise/bpel
            // should invoke createNodes() only if updated file is not a source file
            //createNodes();
        }

        /*
         * @see javax.swing.event.ChangeListener(javax.swing.event.ChangeEvent)
         * sources change
         */
        public void stateChanged(ChangeEvent e) {
            createNodes();
        }

        /*
         * source group change
         */
        public void propertyChange(PropertyChangeEvent evt) {
            String property = evt.getPropertyName();
            if (property.startsWith(PhpProject.SRC_) && property.endsWith(PhpProject._DIR)) {
                createNodes();
            }
        }

        /*
         * @see org.openide.filesystems.FileStatusListener#annotationChanged(org.openide.filesystems.FileStatusEvent)
         * file system change
         */
        /*
        public void annotationChanged(FileStatusEvent ev) {
            createNodes();
        }
         */

        private void createNodes() {
            // update Sources listeners
            Sources sources = ProjectUtils.getSources(myProject);
            updateSourceListeners(sources);

            // parse SG
            // update SG listeners
            // TODO check if this is necessary
            final SourceGroup[] sourceGroups = Utils.getSourceGroups(myProject);
            updateSourceGroupsListeners(sourceGroups);
            final SourceGroup[] groups = new SourceGroup[sourceGroups.length];
            System.arraycopy(sourceGroups, 0, groups, 0, sourceGroups.length);

            List<Object> keysList = new ArrayList<Object>(groups.length);
            //Set<FileObject> roots = new HashSet<FileObject>();
            FileObject fileObject = null;
            for (int i = 0; i < groups.length; i++) {
                fileObject = groups[i].getRootFolder();
                DataFolder srcDir = getFolder(fileObject);

                if (srcDir != null) {
                    keysList.add(groups[i]);
                }
                //roots.add(fileObject);
            }
            if (keysList.size() > 0) {
                setKeys(keysList);
            }
            // Seems that we do not need to implement FileStatusListener
            // to listen to source groups root folders changes.
            // look at RubyLogicalViewRootNode for example.
            //updateSourceRootsListeners(roots);
        }

        private void updateSourceListeners(Sources sources) {
            if (sourcesListener == null) {
                sourcesListener = WeakListeners.change(this, sources);
                sources.addChangeListener(sourcesListener);
            }
        }

        private void updateSourceGroupsListeners(SourceGroup[] sourceGroups) {
            if (groupsListeners != null) {
                Iterator it = groupsListeners.keySet().iterator();
                while (it.hasNext()) {
                    SourceGroup group = (SourceGroup) it.next();
                    PropertyChangeListener pcl = (PropertyChangeListener) groupsListeners.get(group);
                    group.removePropertyChangeListener(pcl);
                }
            }
            groupsListeners = new HashMap();
            for (SourceGroup group : sourceGroups) {
                PropertyChangeListener pcl = WeakListeners.propertyChange(this, group);
                groupsListeners.put(group, pcl);
                group.addPropertyChangeListener(pcl);
            }
        }

        /*
        private void updateSourceRootsListeners(Set<FileObject> files) {
            if (fileSystemListeners != null) {
                Iterator it = fileSystemListeners.keySet().iterator();
                while (it.hasNext()) {
                    FileSystem fs = (FileSystem) it.next();
                    FileStatusListener fsl = fileSystemListeners.get(fs);
                    fs.removeFileStatusListener(fsl);
                }
            }
            
            fileSystemListeners = new HashMap<FileSystem, FileStatusListener>();
            if (files == null) {
                return;
            }
            
            Iterator it = files.iterator();
            Set<FileSystem> hookedFileSystems = new HashSet<FileSystem>();
            while (it.hasNext()) {
                FileObject fo = (FileObject) it.next();
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
                    ErrorManager err = ErrorManager.getDefault();
                    err.annotate(e, ErrorManager.UNKNOWN, "Cannot get " + fo + " filesystem, ignoring...", null, null, null); // NOI18N
                    err.notify(ErrorManager.INFORMATIONAL, e);
                }
            }
        }
         */
        
        private DataFolder getFolder(String propName) {
            String propertyValue = getProject().getEvaluator().getProperty(propName);
            if (propertyValue != null) {
                FileObject fileObject = getProject().getHelper().resolveFileObject(propertyValue);
                return getFolder(fileObject);
            }
            return null;
        }

        private DataFolder getFolder(FileObject fileObject) {
            if (fileObject != null && fileObject.isValid()) {
                try {
                    DataFolder dataFolder = DataFolder.findFolder(fileObject);
                    return dataFolder;
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
            return null;
        }

    }

    private class SrcNode extends FilterNode {

        /**
         * creates source root node based on specified DataFolder.
         * Name is taken from bundle by SOURCE_ROOT_NODE_NAME key.
         * <br/>
         * TODO : if we support several source roots, remove this constructor
         */
        SrcNode(DataFolder folder) {
            this(folder, NbBundle.getMessage(PhpLogicalViewProvider.class, SOURCE_ROOT_NODE_NAME));
        }

        /**
         * creates source root node based on specified DataFolder.
         * Uses specified name.
         */
        SrcNode(DataFolder folder, String name) {
            this(new FilterNode(folder.getNodeDelegate(), 
                                folder.createNodeChildren(new PhpSourcesFilter()))
                 , name);
        }

        private SrcNode(FilterNode node, String name) {
            super(node, new FolderChildren(node));
            disableDelegation(DELEGATE_GET_DISPLAY_NAME 
                    | DELEGATE_SET_DISPLAY_NAME 
                    | DELEGATE_GET_SHORT_DESCRIPTION 
                    | DELEGATE_GET_ACTIONS);
            setDisplayName(name);
        }

        @Override
        public boolean canCopy() {
            return false;
        }

        @Override
        public boolean canCut() {
            return false;
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
        public Action[] getActions(boolean context) {
            if (context) {
                return super.getActions(context);
            } else {
                return getAdditionalActions();
            }
        }

        private Action[] getAdditionalActions() {
            List<Action> list = new LinkedList<Action>();

            list.add(0, CommonProjectActions.newFileAction());

            // the same provider actions as for project
            for (Action action : getProviderActions()){
                list.add(action);
            }

            for (Action action : getSrcActions()){
                list.add(action);
            }

            for (Action action : getStandardSrcActions()){
                list.add(action);
            }

            return list.toArray(new Action[list.size()]);
        }
        
        private Action[] getSrcActions(){
            ImportCommand importComm = new ImportCommand(getProject());
            RunLocalCommand runLocalComm = new RunLocalCommand(getProject());

            Action[] actions = new Action[]{
                ProjectSensitiveActions.projectCommandAction(
                        importComm.getId(), importComm.getLabel(), null),
                ProjectSensitiveActions.projectCommandAction(
                        runLocalComm.getId(), runLocalComm.getLabel(), null)
            };
            return actions;
        }
        
        private Action[] getStandardSrcActions(){
            Action[] actions = new Action[]{
                null,
                SystemAction.get(FindAction.class),
                null,
                SystemAction.get(PasteAction.class),
                null,
                SystemAction.get(FileSystemAction.class),
                SystemAction.get(ToolsAction.class),
            };
            return actions;
        }
    }

/**
     * Children for node that represents folder (SrcNode or PackageNode)
     */
    private class FolderChildren extends FilterNode.Children {

        FolderChildren(final Node originalNode) {
            super(originalNode);
        }

        @Override
        protected Node[] createNodes(Node key) {
            return super.createNodes(key);
        }

        
        @Override
        protected Node copyNode(final Node originalNode) {
            DataObject dobj = originalNode.getLookup().lookup(DataObject.class);
            return (dobj instanceof DataFolder) 
                    ? new PackageNode(originalNode) 
                    : new ObjectNode(originalNode);
        }
    }

    private final class PackageNode extends FilterNode {

        public PackageNode(final Node originalNode) {
            super(originalNode, new FolderChildren(originalNode));
        }

        @Override
        public Action[] getActions(boolean context) {
            if (context) {
                return super.getActions(context);
            } else {
                return getAdditionalActions();
            }

        }
        
        private Action[] getAdditionalActions(){
            List<Action> actions = new LinkedList<Action>();

            for (Action action : super.getActions(false)) {
                actions.add(action);
            }

            // want to add recent after 'NewFile' action.
            // use fixed index not to search for 'NewFile' 
            // in seper actions each time (this wuill need to create 
            // CommonProjectActions.newFileAction() and check equals() )
            int pos = 2;
            for (Action action : getProviderActions()) {
                actions.add(pos++, action);
            }

            for (Action action : getFolderActions()) {
                actions.add(pos++, action);
            }

            return actions.toArray(new Action[]{});
        }

        private Action[] getFolderActions() {
            RunLocalCommand runCommand = new RunLocalCommand(getProject());
            Action runAction = getWrapperAction(runCommand);
            
            Action[] actions = new Action[]{
                runAction,
                null,
                SystemAction.get(FileSystemAction.class)};
            return actions;
        }                
    }

    private boolean isInvokedForProject(){
        return PhpCommandUtils.isInvokedForProject();
    }

    private boolean isInvokedForSrcRoot(){
        return PhpCommandUtils.isInvokedForSrcRoot();
    }
    
    
    /**
     * creates Action object for given Command.
     * Uses ProjectSensitiveActions.projectCommandAction() 
     * if Project is among selected nodes, 
     * Creates AbstractAction otherwise.
     */
    private Action getWrapperAction(Command command) {
        Action action = null;
        if (isInvokedForProject() || isInvokedForSrcRoot()){
            action = ProjectSensitiveActions.projectCommandAction( 
                        command.getId(), command.getLabel(), null);
        } 
        else {
            action = myActionsByCommand.get(command);
            if (action == null){
                action = createWrapperAction(command);
                myActionsByCommand.put(command, action);
            }
        }
        return action;
    }
    
    private Action createWrapperAction(final Command command) {
        Action action = Models.createAction(
                command.getLabel(),
                new ActionPerformer() {

                    public boolean isEnabled(Object node) {
                        return command.isEnabled();
                    }

                    public void perform(Object[] nodes) {
                        PhpActionProvider.PhpCommandRunner
                                .runCommand(command);
                    }
                },
                Models.MULTISELECTION_TYPE_ANY);
        return action;
    }
    
    private final class ObjectNode extends FilterNode {

        public ObjectNode(final Node originalNode) {
            super(originalNode);
        }

        @Override
        public Action[] getActions(boolean context) {
            if (context) {
                return super.getActions(context);
            } else {
                return getAdditionalActions();
            }
        }
        
        private Action[] getAdditionalActions() {
            List<Action> actions = new LinkedList<Action>();

            actions.add(SystemAction.get(OpenAction.class));
            actions.add(null);

            for (Action action : getProviderActions()) {
                actions.add(action);
            }

            for (Action action : getObjectActions()) {
                actions.add(action);
            }

            for (Action action : super.getActions(false)) {
                actions.add(action);
            }
            
            return actions.toArray(new Action[]{});
        }
        
        private Action[] getObjectActions(){
            RunLocalCommand runCommand = new RunLocalCommand(getProject());
            Action runAction = getWrapperAction(runCommand);
            
            Action[] actions = new Action[]{
                runAction,
                null,
                SystemAction.get(CutAction.class), 
                SystemAction.get(CopyAction.class), 
                SystemAction.get(PasteAction.class), 
                null, 
                SystemAction.get(DeleteAction.class), 
                SystemAction.get(RenameAction.class), 
                null, 
                SystemAction.get(SaveAsTemplateAction.class), 
                SystemAction.get(FileSystemAction.class)
            };
            return actions;
        }        

    }

    /** returns provider actions for specified DataObject or for Project if null.
     * 
     * @param dataObject for which actions keysList should be provided.
     * It can be DataObject for file, 
     * DataFolder for forlder
     * or null for Project and SrcRoot Node.
     * 
     * @returns Folder menu items for given DataFolder,
     * File menu items for DataObject,
     * Project menu items for null
     */
    private Action[] getProviderActions() {
        Action[] actions = new Action[]{};
        List<Action> list = new LinkedList<Action>();

        Command[] commands = getProviderCommands();
        if (commands.length > 0 ){
            list.add(null);

            for (Command command : commands) {
                if (command == null) {
                    list.add(null);
                    continue;
                }
                list.add(getWrapperAction(command));
            }

            list.add(null);
            actions = list.toArray(actions);
        }

        return actions;
    }

    private Command[] getProviderCommands() {
        Command[] commands = null;

        WebServerProvider provider = Utils.getProvider(getProject());
        if (provider != null) {
            CommandProvider commandProvider = provider.getCommandProvider();
            
            commands = commandProvider.getCommands(getProject());
        }
        if (commands == null){
            commands = new Command[]{};
        }
        return commands;
    }
    
    private class PhpSourcesFilter implements DataFilter {

        private static final long serialVersionUID = -7439706583318056955L;

        /*
         * (non-Javadoc)
         *
         * @see org.openide.loaders.DataFilter#acceptDataObject(org.openide.loaders.DataObject)
         */
        public boolean acceptDataObject(DataObject object) {
                return     isNotTemporaryFile(object)
                        && isNotProjectFile(object);
        }

        private boolean isNotProjectFile(DataObject object){
            try {
                
                if (PROJECT_XML != null) {
                    File nbProject = PROJECT_XML.getParentFile().getCanonicalFile();
                    File f = FileUtil.toFile(object.getPrimaryFile()).getCanonicalFile();
                    return nbProject != null && !nbProject.equals(f);
                } else {
                    return true;
                }
            } catch (IOException e) {
                return false;
            }
        }
        
        private boolean isNotTemporaryFile(DataObject object){
                String name = object.getPrimaryFile().getNameExt();
                return !name.endsWith(PhpProject.TMP_FILE_POSTFIX);
        }
        
        private final File PROJECT_XML = getProject().getHelper()
                .resolveFile(AntProjectHelper.PROJECT_XML_PATH);
    }
    
    private final Map<Command, Action> myActionsByCommand;

    private final PhpProject myProject;
    private final SubprojectProvider mySubProjectProvider;
}
