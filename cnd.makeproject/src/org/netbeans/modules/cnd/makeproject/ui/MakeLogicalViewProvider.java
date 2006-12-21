/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.ui;

import java.awt.Toolkit;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JSeparator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.MakeActionProvider;
import org.netbeans.modules.cnd.makeproject.api.actions.AddExistingFolderItemsAction;
import org.netbeans.modules.cnd.makeproject.api.actions.AddExistingItemAction;
import org.netbeans.modules.cnd.makeproject.api.actions.NewFolderAction;
import org.netbeans.modules.cnd.makeproject.api.compilers.Tool;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.PasteAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.RenameAction;
import org.openide.actions.ToolsAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.FolderLookup;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Template;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.ExTransferable;
import org.openidex.search.SearchInfo;

/**
 * Support for creating logical views.
 */
public class MakeLogicalViewProvider implements LogicalViewProvider {
    
    private final Project project;
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final SubprojectProvider spp;
    private final ReferenceHelper resolver;
    
    private static Project currentProject = null;
    private static Folder currentFolder = null;
    
    private static final MessageFormat ITEM_VIEW_FLAVOR = new MessageFormat("application/x-org-netbeans-modules-cnd-makeproject-uidnd; class=org.netbeans.modules.cnd.makeproject.ui.MakeLogicalViewProvider$ViewItemNode; mask={0}"); //NOI18N
    static final String PRIMARY_TYPE = "application";   //NOI18N
    static final String SUBTYPE = "x-org-netbeans-modules-cnd-makeproject-uidnd";    //NOI18N
    static final String MASK = "mask";  //NOI18N
    
    public MakeLogicalViewProvider(Project project, AntProjectHelper helper, PropertyEvaluator evaluator, SubprojectProvider spp, ReferenceHelper resolver) {
        this.project = project;
        assert project != null;
        this.helper = helper;
        assert helper != null;
        this.evaluator = evaluator;
        assert evaluator != null;
        this.spp = spp;
        assert spp != null;
        this.resolver = resolver;
        
    }
    
    public Node createLogicalView() {
        if (getMakeConfigurationDescriptor() == null)
            return new MakeLogicalViewRootNodeBroken();
        else
            return new MakeLogicalViewRootNode(getMakeConfigurationDescriptor().getLogicalFolders());
    }
    
    public org.openide.nodes.Node findPath( Node root, Object target ) {
        Node returnNode = null;
        Project rootProject = (Project)root.getLookup().lookup( Project.class );
        if (rootProject == null ) {
            return null;
        }
        
        if (target instanceof DataObject) {
            target = ((DataObject) target).getPrimaryFile();
        }
        
        if (!(target instanceof FileObject))
            return null;
        
        // FIXUP: this doesn't work with file groups (jl: is this still true?)
        File file = FileUtil.toFile((FileObject)target);
        MakeConfigurationDescriptor makeConfigurationDescriptor = getMakeConfigurationDescriptor();
        Item item = makeConfigurationDescriptor.findProjectItemByPath(file.getAbsolutePath());
        
        if (item == null) {
            item = makeConfigurationDescriptor.findExternalItemByPath(file.getAbsolutePath());
            if (item == null) {
                //not found:
                return null;
            }
        }
        
        // FIXUP: assume nde node is last node in current folder. Is this always true?
        // Find the node and return it
        Node folderNode = findFolderNode(root, item.getFolder());
        if (folderNode != null) {
            Node[] nodes = folderNode.getChildren().getNodes( true );
            int index = 0;
            for (index = 0; index < nodes.length; index++) {
                Item nodeItem = (Item)nodes[index].getValue("Item"); // NOI18N
                if (nodeItem == item)
                    break;
            }
            if (nodes.length > 0 && index < nodes.length)
                returnNode = nodes[index];
            /*
            if (nodes.length > 0)
                returnNode = nodes[nodes.length -1];
             */
        }
        
        return returnNode;
    }
    
    /*
     * Recursive method to find the node in the tree with root 'root'
     * that is representing 'folder'
     */
    private static Node findFolderNode(Node root, Folder folder) {
        if (root.getValue("Folder") == folder) // NOI18N
            return root;
        Folder parent = folder.getParent();
        
        if (parent == null)
            return root;
        
        Node parentNode = findFolderNode(root, parent);
        
        if (parentNode == null)
            return null;
        
        Node[] nodes = parentNode.getChildren().getNodes( true );
        for ( int i = 0; i < nodes.length; i++)  {
            if (nodes[i].getValue("Folder") == folder) // NOI18N
                return nodes[i];
        }
        return null;
    }
    
    /*
     * Recursive method to find the node in the tree with root 'root'
     * that is representing 'item'
     */
    private static Node findItemNode(Node root, Item item) {
        Node parentNode = findFolderNode(root, item.getFolder());
        if (parentNode != null) {
            Node[] nodes = parentNode.getChildren().getNodes(true);
            for ( int i = 0; i < nodes.length; i++)  {
                if (nodes[i].getValue("Item") == item) // NOI18N
                    return nodes[i];
            }
        }
        return null;
    }
    
    /**
     * HACK: set the folder node visible in the project explorer
     * See IZ7551
     */
    public static void setVisible(Project project, Folder folder) {
        Node rootNode = ProjectTabBridge.getInstance().getExplorerManager().getRootContext();
        Node projectRoot = findProjectNode(rootNode, project);
        
        if (projectRoot == null)
            return ;
        
        Node folderNode = findFolderNode(projectRoot, folder);
        try {
            ProjectTabBridge.getInstance().getExplorerManager().setSelectedNodes(new Node[] {folderNode});
        } catch (Exception e) {
            ; // FIXUP
        }
    }
    
    /**
     * HACK: set the folder node visible in the project explorer
     * See IZ7551
     */
    public static void setVisible(Project project, Item item) {
        setVisible(project, new Item[] {item});
    }
    
    public static void setVisible(Project project, Item[] items) {
        Node rootNode = ProjectTabBridge.getInstance().getExplorerManager().getRootContext();
        List nodes = new ArrayList();
        for (int i = 0; i < items.length; i++) {
            Node root = findProjectNode(rootNode, project);
            
            if (root != null)
                nodes.add(findItemNode(root, items[i]));
        }
        try {
            ProjectTabBridge.getInstance().getExplorerManager().setSelectedNodes((Node[]) nodes.toArray(new Node[0]));
        } catch (Exception e) {
            ; // FIXUP
        }
    }
    
    private static Node findProjectNode(Node root, Project p) {
        Node[] n = root.getChildren().getNodes(true);
        Template t = new Template(null, null, p);
        
        for (int cntr = 0; cntr < n.length; cntr++) {
            if (n[cntr].getLookup().lookupItem(t) != null) {
                return n[cntr];
            }
        }
        
        return null;
    }
    
    private static Lookup createLookup( Project project ) {
        DataFolder rootFolder = DataFolder.findFolder( project.getProjectDirectory() );
        // XXX Remove root folder after FindAction rewrite
        return Lookups.fixed( new Object[] { project, rootFolder } );
    }
    
    
    // Private innerclasses ----------------------------------------------------
    
    public static boolean hasBrokenLinks(AntProjectHelper helper, ReferenceHelper resolver) {
        return false;
        /*
        return BrokenReferencesSupport.isBroken(helper, resolver, BREAKABLE_PROPERTIES,
            new String[] {MakeProjectProperties.JAVA_PLATFORM});
         */
    }
    
    private static Image brokenProjectBadge = Utilities.loadImage( "org/netbeans/modules/cnd/makeproject/ui/resources/brokenProjectBadge.gif" ); // NOI18N
    
    /** Filter node containin additional features for the Make physical
     */
    private final class MakeLogicalViewRootNode extends AnnotatedNode implements ChangeListener {
        
        private Image icon;
        private Lookup lookup;
        private Action brokenLinksAction;
        private boolean broken;
        private Folder folder;
        
        public MakeLogicalViewRootNode(Folder folder) {
            super(new LogicalViewChildren(folder), Lookups.fixed(new Object[] {
                folder,
                project,
                new FolderSearchInfo(folder),
            }));
            this.folder = folder;
            setIconBaseWithExtension(MakeConfigurationDescriptor.ICON);
            setName( ProjectUtils.getInformation( project ).getDisplayName() );
            if (hasBrokenLinks(helper, resolver)) {
                broken = true;
            }
            brokenLinksAction = new BrokenLinksAction();
            // Handle annotations
            setForceAnnotation(true);
            updateAnnotationFiles();
        }
        
        public Folder getFolder() {
            return folder;
        }
        
        private void updateAnnotationFiles() {
            Vector vec = new Vector();
            FileObject[]  fos = project.getProjectDirectory().getChildren();
            for (int i = 0; i < fos.length; i++)
                vec.add(fos[i]);
            setFiles(new LinkedHashSet(vec));
            Vector allFolders = new Vector();
            allFolders.add(folder);
            allFolders.addAll(folder.getAllFolders(true));
            Iterator iter = allFolders.iterator();
            while (iter.hasNext()) {
                ((Folder)iter.next()).addChangeListener(this);
            }
        }
        
        /*
         * Something in the folder has changed
         **/
        public void stateChanged(ChangeEvent e) {
            updateAnnotationFiles();
            fireIconChange();
            fireOpenedIconChange();
        }
        
        public Object getValue(String valstring) {
            if (valstring == null)
                return super.getValue(valstring);
            if (valstring.equals("Folder")) // NOI18N
                return folder;
            else if (valstring.equals("Project")) // NOI18N
                return project;
            else if (valstring.equals("This")) // NOI18N
                return this;
            return super.getValue(valstring);
        }
        
        public Image getIcon( int type ) {
            Image original = annotateIcon(super.getIcon(type), type);
            return broken ? Utilities.mergeImages(original, brokenProjectBadge, 8, 0) : original;
        }
        
        public Image getOpenedIcon( int type ) {
            Image original = annotateIcon(super.getOpenedIcon(type), type);
            return broken ? Utilities.mergeImages(original, brokenProjectBadge, 8, 0) : original;
        }
        
        public Action[] getActions( boolean context ) {
            currentProject = project;
            currentFolder = getMakeConfigurationDescriptor().getLogicalFolders();
            Vector actions = new Vector();
            // Add standard actions
            Action[] standardActions = getAdditionalActions();
            for (int i = 0; i < standardActions.length; i++)
                actions.add(standardActions[i]);
            addActionsFromLayers(actions, "NativeProjects/Actions");
            addActionsFromLayers(actions, "Projects/Actions");
            // Add remaining actions
            actions.add(null);
            actions.add(SystemAction.get(ToolsAction.class));
            actions.add(null);
            actions.add(CommonProjectActions.customizeProjectAction());
            return (Action[])actions.toArray(new Action[actions.size()]);
        }
        
        private void addActionsFromLayers(final Vector actions, String folderName) {
            // Add other project actions
            try {
                FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource(folderName); // NOI18N
                if (fo != null) {
                    DataObject dobj = DataObject.find(fo);
                    FolderLookup actionRegistry = new FolderLookup((DataFolder)dobj);
                    Lookup.Template query = new Lookup.Template(Object.class);
                    Lookup lookup = actionRegistry.getLookup();
                    Iterator it = lookup.lookup(query).allInstances().iterator();
                    if (it.hasNext()) {
                        actions.add(null);
                    }
                    while (it.hasNext()) {
                        Object next = it.next();
                        if (next instanceof Action) {
                            actions.add(next);
                        } else if (next instanceof JSeparator) {
                            actions.add(null);
                        }
                    }
                }
            } catch (DataObjectNotFoundException ex) {
                // data folder for existing fileobject expected
                ErrorManager.getDefault().notify(ex);
            }
        }
        
        public boolean canRename() {
            return false;
        }
        
        public PasteType getDropType(Transferable transferable, int action, int index) {
            DataFlavor[] flavors = transferable.getTransferDataFlavors();
            for (int i = 0; i < flavors.length; i++) {
                if (flavors[i].getSubType().equals(SUBTYPE))
                    return super.getDropType(transferable, action, index);
            }
            return null;
        }
        
        protected void createPasteTypes(Transferable transferable, List list) {
            DataFlavor[] flavors = transferable.getTransferDataFlavors();
            for (int i = 0; i < flavors.length; i++) {
                if (flavors[i].getSubType().equals(SUBTYPE)) {
                    try {
                        ViewItemNode viewItemNode = (ViewItemNode)transferable.getTransferData(flavors[i]);
                        int type = new Integer(flavors[i].getParameter(MASK)).intValue();
                        list.add(new ViewItemPasteType(this.getFolder(), viewItemNode, type));
                    } catch (Exception e) {
                    }
                }
            }
            super.createPasteTypes(transferable, list);
        }
        
        // Private methods -------------------------------------------------
        
        private Action[] getAdditionalActions() {
            
            ResourceBundle bundle = NbBundle.getBundle( MakeLogicalViewProvider.class );
            
            return new Action[] {
                CommonProjectActions.newFileAction(),
                SystemAction.get(AddExistingItemAction.class),
                SystemAction.get(AddExistingFolderItemsAction.class),
                SystemAction.get(NewFolderAction.class),
                new AddExternalItemAction(project),
                null,
                ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_BUILD, bundle.getString( "LBL_BuildAction_Name" ), null ), // NOI18N
                ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_REBUILD, bundle.getString( "LBL_RebuildAction_Name" ), null ), // NOI18N
                ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_CLEAN, bundle.getString( "LBL_CleanAction_Name" ), null ), // NOI18N
                ProjectSensitiveActions.projectCommandAction(MakeActionProvider.COMMAND_BATCH_BUILD, "Batch Build...", null ), // NOI18N
                new SetConfigurationAction(project, helper),
                null,
                ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_RUN, bundle.getString( "LBL_RunAction_Name" ), null ), // NOI18N
                //new DebugMenuAction(project, helper),
                ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_DEBUG, bundle.getString( "LBL_DebugAction_Name" ), null ),
                ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_DEBUG_STEP_INTO, bundle.getString( "LBL_DebugAction_Step_Name" ), null ),
                null,
                CommonProjectActions.setAsMainProjectAction(),
                CommonProjectActions.openSubprojectsAction(),
                CommonProjectActions.closeProjectAction(),
                null,
                SystemAction.get(org.openide.actions.FindAction.class ),
                null,
                CommonProjectActions.renameProjectAction(),
                CommonProjectActions.moveProjectAction(),
                CommonProjectActions.copyProjectAction(),
                CommonProjectActions.deleteProjectAction(),
                null,
                (broken ? brokenLinksAction : null),
            };
            
        }
        
        /** This action is created only when project has broken references.
         * Once these are resolved the action is disabled.
         */
        private class BrokenLinksAction extends AbstractAction implements PropertyChangeListener, Runnable {
            
            private RequestProcessor.Task task = null;
            
            private PropertyChangeListener weakPCL;
            
            public BrokenLinksAction() {
                /*
                putValue(Action.NAME, NbBundle.getMessage(MakePhysicalViewProvider.class, "LBL_Fix_Broken_Links_Action"));
                setEnabled(broken);
                evaluator.addPropertyChangeListener( this );
                // When evaluator fires changes that platform properties were
                // removed the platform still exists in JavaPlatformManager.
                // That's why I have to listen here also on JPM:
                weakPCL = WeakListeners.propertyChange( this, JavaPlatformManager.getDefault() );
                JavaPlatformManager.getDefault().addPropertyChangeListener( weakPCL );
                 */
            }
            
            public void actionPerformed(ActionEvent e) {
                /* FIXUP
                BrokenReferencesSupport.showCustomizer(helper, resolver, BREAKABLE_PROPERTIES, new String[]{MakeProjectProperties.JAVA_PLATFORM});
                run();
                 */
            }
            
            public void propertyChange(PropertyChangeEvent evt) {
                // check project state whenever there was a property change
                // or change in list of platforms.
                // Coalesce changes since they can come quickly:
                if (task == null) {
                    task = RequestProcessor.getDefault().create(this);
                }
                task.schedule(100);
            }
            
            public synchronized void run() {
                boolean old = broken;
                broken = hasBrokenLinks(helper, resolver);
                if (old != broken) {
                    setEnabled(broken);
                    fireIconChange();
                    fireOpenedIconChange();
                }
            }
            
        }
    }
    
    private final class MakeLogicalViewRootNodeBroken extends AbstractNode {
        public MakeLogicalViewRootNodeBroken() {
            super(Children.LEAF, Lookups.fixed(new Object[] {project}));
            setIconBaseWithExtension(MakeConfigurationDescriptor.ICON);
            setName( ProjectUtils.getInformation( project ).getDisplayName() );
        }
        
        public Image getIcon( int type ) {
            Image original = super.getIcon(type);
            return Utilities.mergeImages(original, brokenProjectBadge, 8, 0);
        }
        
        public Image getOpenedIcon( int type ) {
            Image original = super.getOpenedIcon(type);
            return Utilities.mergeImages(original, brokenProjectBadge, 8, 0);
        }
        
        public Action[] getActions( boolean context ) {
            Vector actions = new Vector();
            actions.add(CommonProjectActions.closeProjectAction());
            return (Action[])actions.toArray(new Action[actions.size()]);
        }
        
        public boolean canRename() {
            return false;
        }
    }
    
    private class LogicalViewChildren extends Children.Keys/*<SourceGroup>*/ implements ChangeListener {
        private Folder folder;
        public LogicalViewChildren(Folder folder) {
            this.folder = folder;
        }
        
        protected void addNotify() {
            super.addNotify();
            setKeys( getKeys() );
            folder.addChangeListener(this);
        }
        
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
            super.removeNotify();
            folder.removeChangeListener(this);
        }
        
        protected Node[] createNodes(Object key) {
            Node node = null;
            if (key instanceof Node)
                node = (Node)key;
            else if (key instanceof Folder) {
                Folder folder = (Folder)key;
                if (folder.isProjectFiles()) {
                    //FileObject srcFileObject = project.getProjectDirectory().getFileObject("src");
                    FileObject srcFileObject = project.getProjectDirectory();
                    DataObject srcDataObject;
                    try {
                        srcDataObject = DataObject.find(srcFileObject);
                    } catch (DataObjectNotFoundException e) {
                        throw new AssertionError(e);
                    }
                    node = new LogicalFolderNode(((DataFolder)srcDataObject).getNodeDelegate(), folder);
                } else {
                    node = new ExternalFilesNode(folder);
                }
            } else if (key instanceof Item) {
                Item item = (Item)key;
                DataObject fileDO = item.getDataObject();
                if (fileDO != null) {
                    node = new ViewItemNode(this, folder, item, fileDO);
                } else {
                    node = new BrokenViewItemNode(this, folder, item);
                }
            }
            return new Node[] {node};
        }
        
        public void stateChanged( ChangeEvent e ) {
            setKeys( getKeys() );
        }
        
        // Private methods -----------------------------------------------------
        
        private Collection getKeys() {
            return folder.getElements();
        }
        
        public void refreshItem(Item item) {
            refreshKey(item);
        }
    }
    
    private MakeConfigurationDescriptor getMakeConfigurationDescriptor() {
        ConfigurationDescriptorProvider pdp = (ConfigurationDescriptorProvider)project.getLookup().lookup(ConfigurationDescriptorProvider.class );
        MakeConfigurationDescriptor makeConfigurationDescriptor = (MakeConfigurationDescriptor)pdp.getConfigurationDescriptor();
        return makeConfigurationDescriptor;
    }
    
    
    /** Yet another cool filter node just to add properties action
     */
    /* FIXUP
    private static class PackageViewFilterNode extends FilterNode {
     
        private String nodeName;
        private Project project;
     
        Action[] actions;
     
        public PackageViewFilterNode( SourceGroup sourceGroup, Project project ) {
            super( PackageView.createPackageView( sourceGroup ) );
            this.project = project;
     
            if ( "${src.dir}".equals( sourceGroup.getName() ) ) {  // NOI18N
                this.nodeName = "BuildCategory/Build"; // NOI18N
            }
            else if ( "${test.src.dir}".equals( sourceGroup.getName() ) ) { // NOI18N
                this.nodeName = "BuildCategory/BuildTests"; // NOI18N
            }
     
        }
     
     
        public Action[] getActions( boolean context ) {
            if ( !context ) {
                if ( actions == null ) {
                    Action superActions[] = super.getActions( context );
                    actions = new Action[ superActions.length + 2 ];
                    System.arraycopy( superActions, 0, actions, 0, superActions.length );
                    actions[superActions.length] = null;
                    actions[superActions.length + 1] = new PreselectPropertiesAction( project, nodeName );
                }
     
                return actions;
     
            }
            else {
                return super.getActions( context );
            }
        }
     
     
    }
     */
    
    
    /** The special properties action
     */
    private static class PreselectPropertiesAction extends AbstractAction {
        
        private Project project;
        private String nodeName;
        
        public PreselectPropertiesAction( Project project, String nodeName ) {
            super( NbBundle.getMessage( MakeLogicalViewProvider.class, "LBL_Properties_Action" ) ); // NOI18N
            this.project = project;
            this.nodeName = nodeName;
        }
        
        public void actionPerformed( ActionEvent e ) {
            MakeCustomizerProvider cp = (MakeCustomizerProvider)project.getLookup().lookup( MakeCustomizerProvider.class );
            if ( cp != null ) {
                cp.showCustomizer( nodeName );
            }
        }
    }
    
    public class LogicalFolderNode extends AnnotatedNode implements ChangeListener {
        private Folder folder;
        
        public LogicalFolderNode(Node folderNode, Folder folder) {
            super(new LogicalViewChildren(folder), Lookups.fixed(new Object[] {
                folder,
                project,
                new FolderSearchInfo(folder),
            }));
            this.folder = folder;
            setForceAnnotation(true);
            updateAnnotationFiles();
        }
        
        private void updateAnnotationFiles() {
            new UpdateAnnotationFilesTHread(this).start();
        }
        
        class UpdateAnnotationFilesTHread extends Thread {
            LogicalFolderNode logicalFolderNode;
            
            UpdateAnnotationFilesTHread(LogicalFolderNode logicalFolderNode) {
                this.logicalFolderNode = logicalFolderNode;
            }
            public void run() {
                setFiles(folder.getAllItemsAsFileObjectSet(true));
                Vector allFolders = new Vector();
                allFolders.add(folder);
                allFolders.addAll(folder.getAllFolders(true));
                Iterator iter = allFolders.iterator();
                while (iter.hasNext()) {
                    ((Folder)iter.next()).addChangeListener(logicalFolderNode);
                }
            }
        }
        
        
        /*
         * Something in the folder has changed
         **/
        public void stateChanged(ChangeEvent e) {
            updateAnnotationFiles();
            fireIconChange();
            fireOpenedIconChange();
        }
        
        public Folder getFolder() {
            return folder;
        }
        public Object getValue(String valstring) {
            if (valstring == null)
                return super.getValue(valstring);
            if (valstring.equals("Folder")) // NOI18N
                return folder;
            else if (valstring.equals("Project")) // NOI18N
                return project;
            else if (valstring.equals("This")) // NOI18N
                return this;
            return super.getValue(valstring);
        }
        
        public Image getIcon( int type ) {
            return annotateIcon(Utilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/logicalFilesFolder.gif"), type); // NOI18N
        }
        
        public Image getOpenedIcon( int type ) {
            return annotateIcon(Utilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/logicalFilesFolderOpened.gif"), type); // NOI18N
        }
        
        public String getName() {
            return folder.getDisplayName();
        }
        
        public String getDisplayName() {
            return annotateName(folder.getDisplayName());
        }
        
        public void setName(String newName) {
            String oldName = folder.getName();
            if (folder.getParent() != null && folder.getParent().findFolderByDisplayName(newName) != null) {
                String msg = NbBundle.getMessage(MakeLogicalViewProvider.class, "CANNOT_RENAME", oldName, newName); // NOI18N
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
                return;
            }
            folder.setDisplayName(newName);
            fireDisplayNameChange(oldName, newName);
        }
        
        public void setDisplayName(String newName) {
            setDisplayName(newName);
        }
        
        public boolean canRename() {
            return true;
        }
        
        public boolean canDestroy() {
            return true;
        }
        
        public boolean canCut() {
            return false; // FIXUP
        }
        
        public boolean canCopy() {
            return false; // FIXUP
        }
        
        public PasteType getDropType(Transferable transferable, int action, int index) {
            DataFlavor[] flavors = transferable.getTransferDataFlavors();
            for (int i = 0; i < flavors.length; i++) {
                if (flavors[i].getSubType().equals(SUBTYPE))
                    return super.getDropType(transferable, action, index);
            }
            return null;
        }
        
        protected void createPasteTypes(Transferable transferable, List list) {
            DataFlavor[] flavors = transferable.getTransferDataFlavors();
            for (int i = 0; i < flavors.length; i++) {
                if (flavors[i].getSubType().equals(SUBTYPE)) {
                    try {
                        ViewItemNode viewItemNode = (ViewItemNode)transferable.getTransferData(flavors[i]);
                        int type = new Integer(flavors[i].getParameter(MASK)).intValue();
                        list.add(new ViewItemPasteType(this.getFolder(), viewItemNode, type));
                    } catch (Exception e) {
                    }
                }
            }
            super.createPasteTypes(transferable, list);
        }
        
        public void newLogicalFolder() {
        }
        
        public Action[] getActions( boolean context ) {
            currentProject = project;
            currentFolder = folder;
            return new Action[] {
                CommonProjectActions.newFileAction(),
                SystemAction.get(AddExistingItemAction.class),
                SystemAction.get(AddExistingFolderItemsAction.class),
                SystemAction.get(NewFolderAction.class),
                null,
                new RefreshItemAction((LogicalViewChildren)getChildren(), folder, null),
                null,
                SystemAction.get(CutAction.class),
                SystemAction.get(CopyAction.class),
                SystemAction.get(PasteAction.class),
                SystemAction.get(RemoveFolderAction.class),
                SystemAction.get(RenameAction.class),
                null,
                SystemAction.get(org.openide.actions.FindAction.class ),
                null,
                SystemAction.get(PropertiesAction.class),
            };
        }
    }
    
    class ViewItemPasteType extends PasteType {
        Folder toFolder;
        ViewItemNode viewItemNode;
        int type;
        
        public ViewItemPasteType(Folder toFolder, ViewItemNode viewItemNode, int type) {
            this.toFolder = toFolder;
            this.viewItemNode = viewItemNode;
            this.type = type;
        }
        
        public Transferable paste() throws IOException {
            if (type == DnDConstants.ACTION_MOVE) {
                // Drag&Drop, Cut&Paste
                if (toFolder.getProject() == viewItemNode.getFolder().getProject()) {
                    // Move within same project
                    Item item = viewItemNode.getItem();
                    viewItemNode.getFolder().removeItem(item);
                    toFolder.addItem(item);
                } else {
                    Item item = viewItemNode.getItem();
                    if (IpeUtils.isPathAbsolute(item.getPath())) {
                        viewItemNode.getFolder().removeItem(item);
                        toFolder.addItem(item);
                    } else if (item.getPath().startsWith("..")) { // NOI18N
                        String originalFilePath = FileUtil.toFile(viewItemNode.getFolder().getProject().getProjectDirectory()).getPath();
                        String newFilePath = FileUtil.toFile(toFolder.getProject().getProjectDirectory()).getPath();
                        String fromNewToOriginal = IpeUtils.getRelativePath(newFilePath, originalFilePath) + "/"; // NOI18N
                        fromNewToOriginal = FilePathAdaptor.normalize(fromNewToOriginal);
                        String newPath = fromNewToOriginal + item.getPath();
                        newPath = IpeUtils.trimDotDot(newPath);
                        viewItemNode.getFolder().removeItem(item);
                        toFolder.addItem(new Item(FilePathAdaptor.normalize(newPath)));
                    } else {
                        Project toProject = toFolder.getProject();
                        FileObject fo = item.getFileObject();
                        FileObject copy = fo.copy(toProject.getProjectDirectory(), fo.getName(), fo.getExt());
                        String newPath = IpeUtils.toRelativePath(FileUtil.toFile(toProject.getProjectDirectory()).getPath(), FileUtil.toFile(copy).getPath());
                        viewItemNode.getFolder().removeItem(item);
                        fo.delete();
                        toFolder.addItem(new Item(FilePathAdaptor.normalize(newPath)));
                    }
                }
            } else if (type == DnDConstants.ACTION_COPY) {
                // Copy&Paste
                if (toFolder.getProject() == viewItemNode.getFolder().getProject()) {
                    Item item = viewItemNode.getItem();
                    if (IpeUtils.isPathAbsolute(item.getPath()) || item.getPath().startsWith("..")) { // NOI18N
                        Toolkit.getDefaultToolkit().beep();
                    } else {
                        FileObject fo = FileUtil.toFileObject(item.getFile());
                        String parent = FileUtil.toFile(fo.getParent()).getPath();
                        String ext = fo.getExt();
                        String newName = IpeUtils.createUniqueFileName(parent, fo.getName(), ext);
                        fo.copy(fo.getParent(), newName, ext);
                        String newPath = parent + "/" + newName; // NOI18N
                        if (ext.length() > 0)
                            newPath = newPath + "." + ext; // NOI18N
                        newPath = IpeUtils.toRelativePath(FileUtil.toFile(viewItemNode.getFolder().getProject().getProjectDirectory()).getPath(), newPath);
                        toFolder.addItem(new Item(FilePathAdaptor.normalize(newPath)));
                    }
                } else {
                    Item item = viewItemNode.getItem();
                    if (IpeUtils.isPathAbsolute(item.getPath())) {
                        toFolder.addItem(new Item(item.getPath()));
                    } else if (item.getPath().startsWith("..")) { // NOI18N
                        String originalFilePath = FileUtil.toFile(viewItemNode.getFolder().getProject().getProjectDirectory()).getPath();
                        String newFilePath = FileUtil.toFile(toFolder.getProject().getProjectDirectory()).getPath();
                        String fromNewToOriginal = IpeUtils.getRelativePath(newFilePath, originalFilePath) + "/"; // NOI18N
                        fromNewToOriginal = FilePathAdaptor.normalize(fromNewToOriginal);
                        String newPath = fromNewToOriginal + item.getPath();
                        newPath = IpeUtils.trimDotDot(newPath);
                        toFolder.addItem(new Item(FilePathAdaptor.normalize(newPath)));
                    } else {
                        Project toProject = toFolder.getProject();
                        String parent = FileUtil.toFile(toProject.getProjectDirectory()).getPath();
                        FileObject fo = item.getFileObject();
                        String ext = fo.getExt();
                        String newName = IpeUtils.createUniqueFileName(parent, fo.getName(), ext);
                        FileObject copy = fo.copy(toProject.getProjectDirectory(), newName, ext);
                        String newPath = newName;
                        if (ext.length() > 0)
                            newPath = newPath + "." + ext; // NOI18N
                        toFolder.addItem(new Item(FilePathAdaptor.normalize(newPath))); // NOI18N
                    }
                }
            }
            return null;
        }
    }
    
    private final class ExternalFilesNode extends AbstractNode {
        private Image icon;
        private Lookup lookup;
        private Action brokenLinksAction;
        private boolean broken;
        private Folder folder;
        
        public ExternalFilesNode(Folder folder) {
            super( new ExternalFilesChildren(project, folder), Lookups.fixed(new Object[] {project, new FolderSearchInfo(folder)}));
            setName(folder.getName());
            setDisplayName(folder.getDisplayName());
            setShortDescription(NbBundle.getBundle(getClass()).getString("ONLY_REFERENCE_TXT"));
            this.folder = folder;
        }
        
        public Object getValue(String valstring) {
            if (valstring == null)
                return super.getValue(valstring);
            if (valstring.equals("Folder")) // NOI18N
                return folder;
            else if (valstring.equals("Project")) // NOI18N
                return project;
            else if (valstring.equals("This")) // NOI18N
                return this;
            return super.getValue(valstring);
        }
        
        public Image getIcon( int type ) {
            //return Utilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/externalFilesFolder.gif"); // NOI18N
            return Utilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/defaultFolder.gif"); // NOI18N
        }
        
        public Image getOpenedIcon( int type ) {
            //return Utilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/externalFilesFolderOpened.gif"); // NOI18N
            return Utilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/defaultFolderOpen.gif"); // NOI18N
        }
        
        public Action[] getActions( boolean context ) {
            currentProject = project;
            currentFolder = null;
            return new Action[] {
                new AddExternalItemAction(project),
                null,
                SystemAction.get(org.openide.actions.FindAction.class ),
            };
        }
        
        public boolean canRename() {
            return false;
        }
    }
    
    private class ExternalFilesChildren extends Children.Keys/*<SourceGroup>*/ implements ChangeListener {
        private Project project;
        private Folder folder;
        
        public ExternalFilesChildren(Project project, Folder folder) {
            this.project = project;
            this.folder = folder;
        }
        
        protected void addNotify() {
            super.addNotify();
            folder.addChangeListener( this );
            setKeys( getKeys() );
        }
        
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
            folder.removeChangeListener( this );
            super.removeNotify();
        }
        
        protected Node[] createNodes( Object key ) {
            if (!(key instanceof Item)) {
                System.err.println("wrong item in external files folder " + key); // NOI18N
                return null;
            }
            Item item = (Item)key;
            DataObject fileDO = item.getDataObject();
            Node node;
            if (fileDO != null) {
                node = new ViewItemNode(this, folder, item, fileDO);
            } else {
                node = new BrokenViewItemNode(this, folder, item);
            }
            return new Node[] {node};
        }
        
        public void stateChanged( ChangeEvent e ) {
            setKeys( getKeys() );
        }
        
        private Collection getKeys() {
            return folder.getElements();
        }
        
        public void refreshItem(Item item) {
            refreshKey(item);
        }
    }
    
    private class ViewItemNode extends FilterNode {
        Children.Keys childrenKeys;
        private Folder folder;
        private Item item;
        
        public ViewItemNode(Children.Keys childrenKeys, Folder folder, Item item, DataObject dataObject) {
            super(dataObject.getNodeDelegate());
            this.childrenKeys = childrenKeys;
            this.folder = folder;
            this.item = item;
            File file = item.getFile();
            setShortDescription(file.getPath());
        }
        
        public Folder getFolder() {
            return folder;
        }
        
        public Item getItem() {
            return item;
        }
        public boolean canRename() {
            return true;
        }
        
        public boolean canDestroy() {
            return true;
        }
        
        public boolean canCut() {
            return true;
        }
        
        public boolean canCopy() {
            return true;
        }
        
        public Transferable clipboardCopy() throws IOException {
            try {
                Transferable t = new ViewItemTransferable(this, DnDConstants.ACTION_COPY);
                return t;
            } catch (ClassNotFoundException e) {
                throw new AssertionError(e);
            }
            
        }
        
        public Transferable clipboardCut() throws IOException {
            try {
                Transferable t = new ViewItemTransferable(this, DnDConstants.ACTION_MOVE);
                return t;
            } catch (ClassNotFoundException e) {
                throw new AssertionError(e);
            }
        }
        
        public Transferable drag() throws IOException {
            try {
                Transferable t = new ViewItemTransferable(this, DnDConstants.ACTION_NONE);
                return t;
            } catch (ClassNotFoundException e) {
                throw new AssertionError(e);
            }
        }
        
        // The node will be removed when the Item gets notification that the file has been destroyed.
        // No need to do it here.
//        public void destroy() {
//            File file = new File(item.getAbsPath());
//            if (file.exists())
//                file.delete();
//            folder.removeItem(item);
//        }
        
        public Object getValue(String valstring) {
            if (valstring == null)
                return super.getValue(valstring);
            if (valstring.equals("Folder")) // NOI18N
                return getFolder();
            else if (valstring.equals("Project")) // NOI18N
                return project;
            else if (valstring.equals("Item")) // NOI18N
                return getItem();
            else if (valstring.equals("This")) // NOI18N
                return this;
            return super.getValue(valstring);
        }
        
        public Action[] getActions( boolean context ) {
            // Replace DeleteAction with Remove Action
            // Replace PropertyAction with customizeProjectAction
            Action[] oldActions = super.getActions();
            Vector newActions = new Vector();
            for (int i = 0; i < oldActions.length; i++) {
                if (oldActions[i] != null && oldActions[i] instanceof org.openide.actions.OpenAction) {
                    newActions.add(oldActions[i]);
                    newActions.add(null);
                    newActions.add(new RefreshItemAction(childrenKeys, null, getItem()));
                    newActions.add(null);
                    newActions.add(SystemAction.get(CompileSingleAction.class));
                    newActions.add(null);
                } else if (oldActions[i] != null && oldActions[i] instanceof DeleteAction) {
                    newActions.add(SystemAction.get(RemoveItemAction.class));
                    newActions.add(SystemAction.get(DeleteAction.class));
                } else if (oldActions[i] != null && oldActions[i] instanceof org.openide.actions.PropertiesAction && getFolder().isProjectFiles()) {
                    newActions.add(SystemAction.get(PropertiesItemAction.class));
                } else {
                    newActions.add(oldActions[i]);
                }
            }
            return (Action[]) newActions.toArray(new Action[newActions.size()]);
        }
    }
    
    static class ViewItemTransferable extends ExTransferable.Single {
        private ViewItemNode node;
        
        public ViewItemTransferable(ViewItemNode node, int operation) throws ClassNotFoundException {
            super(new DataFlavor(ITEM_VIEW_FLAVOR.format(new Object[] {new Integer(operation)}), null, MakeLogicalViewProvider.class.getClassLoader()));
            this.node = node;
        }
        
        protected Object getData() throws IOException, UnsupportedFlavorException {
            return this.node;
        }
    }
    
    private final class BrokenViewItemNode extends AbstractNode {
        private boolean broken;
        private Children.Keys childrenKeys;
        private Folder folder;
        private Item item;
        
        public BrokenViewItemNode(Children.Keys childrenKeys, Folder folder, Item item) {
            super(Children.LEAF);
            this.childrenKeys = childrenKeys;
            this.folder = folder;
            this.item = item;
            File file = item.getFile();
            setName(file.getPath());
            setDisplayName(file.getName());
            setShortDescription(NbBundle.getMessage(getClass(), "BrokenTxt", file.getPath())); // NOI18N
            broken = true;
        }
        
        public Image getIcon( int type ) {
            //Image original = Utilities.loadImage("org/openide/loaders/instanceObject.gif"); // NOI18N
            //Image original = Utilities.loadImage("org/netbeans/modules/cnd/loaders/CCSrcIcon.gif"); // NOI18N
            Image original;
            int tool = item.getDefaultTool();
            if (tool == Tool.CCompiler)
                original = Utilities.loadImage("org/netbeans/modules/cnd/loaders/CSrcIcon.gif"); // NOI18N
            else if (tool == Tool.CCCompiler)
                original = Utilities.loadImage("org/netbeans/modules/cnd/loaders/CCSrcIcon.gif"); // NOI18N
            else if (tool == Tool.FortranCompiler)
                original = Utilities.loadImage("org/netbeans/modules/cnd/loaders/FortranSrcIcon.gif"); // NOI18N
            else
                original = Utilities.loadImage("org/netbeans/modules/cnd/loaders/unknown.gif"); // NOI18N
            return broken ? Utilities.mergeImages(original, brokenProjectBadge, 11, 0) : original;
        }
        
        public Action[] getActions( boolean context ) {
            return new Action[] {
                SystemAction.get(RemoveItemAction.class),
                new RefreshItemAction(childrenKeys, null, item),
                null,
                SystemAction.get(PropertiesItemAction.class),
            };
        }
        
        public boolean canRename() {
            return false;
        }
        
        public Object getValue(String valstring) {
            if (valstring == null)
                return super.getValue(valstring);
            if (valstring.equals("Folder")) // NOI18N
                return folder;
            else if (valstring.equals("Project")) // NOI18N
                return project;
            else if (valstring.equals("Item")) // NOI18N
                return item;
            else if (valstring.equals("This")) // NOI18N
                return this;
            return super.getValue(valstring);
        }
    }
    
    class RefreshItemAction extends AbstractAction {
        private Children.Keys childrenKeys;
        private Folder folder;
        private Item item;
        
        public RefreshItemAction(Children.Keys childrenKeys, Folder folder, Item item) {
            this.childrenKeys = childrenKeys;
            this.folder = folder;
            this.item = item;
            putValue(NAME, NbBundle.getBundle(getClass()).getString("CTL_Refresh")); //NOI18N
        }
        
        public void actionPerformed(ActionEvent e) {
            if (item != null) {
                refresh(item);
            } else {
                Item[] items = folder.getItemsAsArray();
                for (int i = 0; i < items.length; i++)
                    refresh(items[i]);
            }
        }
        
        private void refresh(Item item) {
            if (childrenKeys instanceof ExternalFilesChildren)
                ((ExternalFilesChildren)childrenKeys).refreshItem(item);
            else if (childrenKeys instanceof LogicalViewChildren)
                ((LogicalViewChildren)childrenKeys).refreshItem(item);
        }
    }
    
    class FolderSearchInfo implements SearchInfo {
        Folder folder;
        
        FolderSearchInfo(Folder folder) {
            this.folder = folder;
        }
        
        public boolean canSearch() {
            return true;
        }
        
        public Iterator objectsToSearch() {
            return folder.getAllItemsAsDataObjectSet(false, "text/").iterator(); // NOI18N
        }
    }
}
