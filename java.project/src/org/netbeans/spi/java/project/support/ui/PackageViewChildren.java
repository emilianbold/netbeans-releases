/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.java.project.support.ui;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.queries.AccessibilityQuery;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.java.project.PackageDisplayUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.FileSensitiveActions;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.*;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.datatransfer.MultiTransferObject;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openidex.search.FileObjectFilter;
import org.openidex.search.SearchInfoFactory;

/**
 * Display of Java sources in a package structure rather than folder structure.
 * @author Adam Sotona, Jesse Glick, Petr Hrebejk, Tomas Zezula
 */
final class PackageViewChildren extends Children.Keys/*<String>*/ implements FileChangeListener, ChangeListener, Runnable {
    
    private static final String NODE_NOT_CREATED = "NNC"; // NOI18N
    private static final String NODE_NOT_CREATED_EMPTY = "NNC_E"; //NOI18N
    
    private static final MessageFormat PACKAGE_FLAVOR = new MessageFormat("application/x-java-org-netbeans-modules-java-project-packagenodednd; class=org.netbeans.spi.java.project.support.ui.PackageViewChildren$PackageNode; mask={0}"); //NOI18N
        
    static final String PRIMARY_TYPE = "application";   //NOI18N
    static final String SUBTYPE = "x-java-org-netbeans-modules-java-project-packagenodednd";    //NOI18N
    static final String MASK = "mask";  //NOI18N

    private java.util.Map/*<String,NODE_NOT_CREATED|NODE_NOT_CREATED_EMPTY|PackageNode>*/ names2nodes;
    private final FileObject root;
    private FileChangeListener wfcl;    // Weak listener on the system filesystem
    private ChangeListener wvqcl;       // Weak listener on the VisibilityQuery

    /**
     * Creates children based on a single source root.
     * @param root the folder where sources start (must be a package root)
     */    
    public PackageViewChildren(FileObject root) {
        
        // Sem mas dat cache a bude to uplne nejrychlejsi na svete
        
        if (root == null) {
            throw new NullPointerException();
        }
        this.root = root;
    }

    FileObject getRoot() {
        return root; // Used from PackageRootNode
    }
    
    protected Node[] createNodes( Object obj ) {
        FileObject fo = root.getFileObject( (String)obj );
        if ( fo != null ) {
            Object o = names2nodes.get( obj );
            PackageNode n;
            if ( o == NODE_NOT_CREATED ) {
                n = new PackageNode( root, DataFolder.findFolder( fo ), false );
            }
            else if ( o ==  NODE_NOT_CREATED_EMPTY ) {
                n = new PackageNode( root, DataFolder.findFolder( fo ), true );
            }
            else {
                n = new PackageNode( root, DataFolder.findFolder( fo ) );
            }            
            names2nodes.put( obj, n );
            return new Node[] {n};
        }
        else {
            return new Node[0];
        }
        
    }
    
    RequestProcessor.Task task = RequestProcessor.getDefault().create( this );
        
    protected void addNotify() {
        // System.out.println("ADD NOTIFY" + root + " : " + this );
        super.addNotify();
        task.schedule( 0 );
    }
    
    public Node[] getNodes( boolean optimal ) {
        if ( optimal ) {
            Node[] garbage = super.getNodes( false );        
            task.waitFinished();
        }
        return super.getNodes( false );
    }
    
    public Node findChild (String name) {
        getNodes (true);
        return super.findChild (name);
    }
    
    public void run() {
        computeKeys();
        refreshKeys();
        try { 
            FileSystem fs = root.getFileSystem();
            wfcl = (FileChangeListener)WeakListeners.create( FileChangeListener.class, this, fs );
            fs.addFileChangeListener( wfcl );
        }
        catch ( FileStateInvalidException e ) {
            ErrorManager.getDefault().notify( ErrorManager.INFORMATIONAL, e );
        }
        wvqcl = WeakListeners.change( this, VisibilityQuery.getDefault() );
        VisibilityQuery.getDefault().addChangeListener( wvqcl );
    }

    protected void removeNotify() {
        // System.out.println("REMOVE NOTIFY" + root + " : " + this );        
        VisibilityQuery.getDefault().removeChangeListener( wvqcl );
        try {
            root.getFileSystem().removeFileChangeListener( wfcl );
        }
        catch ( FileStateInvalidException e ) {
            ErrorManager.getDefault().notify( ErrorManager.INFORMATIONAL, e );
        }
        setKeys(Collections.EMPTY_SET);
        names2nodes.clear();
        super.removeNotify();
    }
    
    // Private methods ---------------------------------------------------------
        
    private void refreshKeys() {
        setKeys( names2nodes.keySet() );
    }
    
    private void computeKeys() {
        // XXX this is not going to perform too well for a huge source root...
        // However we have to go through the whole hierarchy in order to find
        // all packages (Hrebejk)
        names2nodes = new TreeMap();
        findNonExcludedPackages( root );
    }
    
    /**
     * Collect all recursive subfolders, except those which have subfolders
     * but no files.
     */    
    private void findNonExcludedPackages( FileObject fo ) {
        PackageView.findNonExcludedPackages( this, fo );
    }
    
    
    /** Finds all empty parents of given package and deletes them
     */
    private void cleanEmptyKeys( FileObject fo ) {
        FileObject parent = fo.getParent(); 
        
        // Special case for default package
        if ( root.equals( parent ) ) {
            PackageNode n = get( parent );
            // the default package is considered empty if it only contains folders,
            // regardless of the contents of these folders (empty or not)
            if ( n != null && PackageDisplayUtils.isEmpty( root, false ) ) {
                remove( root );
            }
            return;
        }
        
        while ( FileUtil.isParentOf( root, parent ) ) {
            PackageNode n = get( parent );
            if ( n != null && n.isLeaf() ) {
                // System.out.println("Cleaning " + parent);
                remove( parent );
            }
            parent = parent.getParent();
        }
    }
    
    // Non private only to be able to have the findNonExcludedPackages impl
    // in on place (PackageView) 
    void add( FileObject fo, boolean empty ) {
        String path = FileUtil.getRelativePath( root, fo );
        assert path != null : "Adding wrong folder " + fo;
        if ( get( fo ) == null ) { 
            names2nodes.put( path, empty ? NODE_NOT_CREATED_EMPTY : NODE_NOT_CREATED );
        }
    }

    private void remove( FileObject fo ) {
        String path = FileUtil.getRelativePath( root, fo );        
        assert path != null : "Removing wrong folder" + fo;
        names2nodes.remove( path );
    }

    private void removeSubTree (FileObject fo) {
        String path = FileUtil.getRelativePath( root, fo );
        assert path != null : "Removing wrong folder" + fo;
        Collection keys = new HashSet (names2nodes.keySet());
        names2nodes.remove(path);
        path = path + '/';  //NOI18N
        for (Iterator it = keys.iterator(); it.hasNext();) {
            String key = (String) it.next();
            if (key.startsWith(path)) {
                names2nodes.remove(key);
            }
        }
    }

    private PackageNode get( FileObject fo ) {
        String path = FileUtil.getRelativePath( root, fo );        
        assert path != null : "Asking for wrong folder" + fo;
        Object o = names2nodes.get( path );
        return !isNodeCreated( o ) ? null : (PackageNode)o;
    }
    
    private boolean contains( FileObject fo ) {
        String path = FileUtil.getRelativePath( root, fo );        
        assert path != null : "Asking for wrong folder" + fo;
        Object o = names2nodes.get( path );
        return o != null;
    }
    
    private boolean exists( FileObject fo ) {
        String path = FileUtil.getRelativePath( root, fo );
        return names2nodes.get( path ) != null;
    }
    
    private boolean isNodeCreated( Object o ) {
        return o instanceof Node;
    }
    
    private PackageNode updatePath( String oldPath, String newPath ) {
        Object o = names2nodes.get( oldPath );
        if ( o == null ) {
            return null;
        }        
        names2nodes.remove( oldPath );
        names2nodes.put( newPath, o );
        return !isNodeCreated( o ) ? null : (PackageNode)o;
    }
    
    // Implementation of FileChangeListener ------------------------------------
    
    public void fileAttributeChanged( FileAttributeEvent fe ) {}

    public void fileChanged( FileEvent fe ) {} 

    public void fileFolderCreated( FileEvent fe ) {
        FileObject fo = fe.getFile();        
        if ( FileUtil.isParentOf( root, fo ) && isVisible( root, fo ) ) {
            cleanEmptyKeys( fo );                
            add( fo, false );
            findNonExcludedPackages( fo );
            refreshKeys();
        }
    }
    
    public void fileDataCreated( FileEvent fe ) {
        FileObject fo = fe.getFile();
        if ( FileUtil.isParentOf( root, fo ) && isVisible( root, fo ) ) {
            FileObject parent = fo.getParent();
            if ( !VisibilityQuery.getDefault().isVisible( parent ) ) {
                return; // Adding file into ignored directory
            }
            PackageNode n = get( parent );
            if ( n == null && !contains( parent ) ) {                
                add( parent, false );
                refreshKeys();
            }
            else if ( n != null ) {
                n.updateChildren();
            }
        }
    }

    public void fileDeleted( FileEvent fe ) {
        FileObject fo = fe.getFile();       
        
        // System.out.println("FILE DELETED " + FileUtil.getRelativePath( root, fo ) );
        
        if ( FileUtil.isParentOf( root, fo ) && isVisible( root, fo ) ) {
            
            // System.out.println("IS FOLDER? " + fo + " : " + fo.isFolder() );
                                  /* Hack for MasterFS see #42464 */
            if ( fo.isFolder() || get( fo ) != null ) {
                // System.out.println("REMOVING FODER " + fo );                
                removeSubTree( fo );
                // Now add the parent if necessary 
                FileObject parent = fo.getParent();
                if ( ( FileUtil.isParentOf( root, parent ) || root.equals( parent ) ) && get( parent ) == null && parent.isValid() ) {
                    // Candidate for adding
                    if ( !toBeRemoved( parent ) ) {
                        // System.out.println("ADDING PARENT " + parent );
                        add( parent, true );
                    }
                }
                refreshKeys();
            }
            else {
                FileObject parent = fo.getParent();
                PackageNode n = get( parent );
                if ( n != null ) {
                    n.updateChildren();
                }
                // If the parent folder only contains folders remove it
                if ( toBeRemoved( parent ) ) {
                    remove( parent );
                    refreshKeys();
                }
                 
            }
        }
        // else {
        //    System.out.println("NOT A PARENT " + fo );
        // }
    }
    
    /** Returns true if the folder should be removed from the view
     * i.e. it has some unignored children and the children are folders only
     */
    private boolean toBeRemoved( FileObject folder ) {
        boolean ignoredOnly = true;
        boolean foldersOnly = true;
        FileObject kids[] = folder.getChildren();
        for ( int i = 0; i < kids.length; i++ ) {
            if ( VisibilityQuery.getDefault().isVisible( kids[i] ) ) {
                ignoredOnly = false;
                if ( !kids[i].isFolder() ) {
                    foldersOnly = false;
                    break;
                }
            }                                  
        }
        if ( ignoredOnly ) {
            return false; // It is either empty or it only contains ignored files
                          // thus is leaf and it means package
        }
        else {
            return foldersOnly;
        }
    }
    
    
    public void fileRenamed( FileRenameEvent fe ) {
        FileObject fo = fe.getFile();        
        if ( FileUtil.isParentOf( root, fo ) && fo.isFolder() ) {
            String rp = FileUtil.getRelativePath( root, fo.getParent() );
            String oldPath = rp + ( rp.length() == 0 ? "" : "/" ) + fe.getName() + fe.getExt(); // NOI18N

            boolean visible = VisibilityQuery.getDefault().isVisible( fo );
            boolean doUpdate = false;
            
            // Find all entries which have to be updated
            ArrayList needsUpdate = new ArrayList();            
            for( Iterator it = names2nodes.keySet().iterator(); it.hasNext(); ) {
                String p = (String)it.next();
                if ( p.startsWith( oldPath ) ) { 
                    if ( visible ) {
                        needsUpdate.add( p );
                    }
                    else {
                        names2nodes.remove( p );
                        doUpdate = true;
                    }
                }    
            }   
                        
            // If the node does not exists then there might have been update
            // from ignored to non ignored
            if ( get( fo ) == null && visible ) {
                cleanEmptyKeys( fo );                
                findNonExcludedPackages( fo );
                doUpdate = true;  // force refresh
            }
            
            int oldPathLen = oldPath.length();
            String newPath = FileUtil.getRelativePath( root, fo );
            for( Iterator it = needsUpdate.iterator(); it.hasNext(); ) {
                String p = (String)it.next();
                StringBuffer np = new StringBuffer( p );
                np.replace( 0, oldPathLen, newPath );                    
                PackageNode n = updatePath( p, np.toString() ); // Replace entries in cache
                if ( n != null ) {
                    n.updateDisplayName(); // Update nodes
                }
            }
            
            if ( needsUpdate.size() > 1 || doUpdate ) {
                // Sorting might change
                refreshKeys();
            }
        }
        /*
        else if ( FileUtil.isParentOf( root, fo ) && fo.isFolder() ) {
            FileObject parent = fo.getParent();
            PackageNode n = get( parent );
            if ( n != null && VisibilityQuery.getDefault().isVisible( parent ) ) {
                n.updateChildren();
            }
            
        }
        */
        
    }
    
    /** Test whether file and all it's parent up to parent paremeter
     * are visible
     */    
    private boolean isVisible( FileObject parent, FileObject file ) {
        
        do {    
            if ( !VisibilityQuery.getDefault().isVisible( file ) )  {
                return false;
            }
            file = file.getParent();
        }
        while ( file != null && file != parent );    
                
        return true;        
    }
    

    // Implementation of ChangeListener ------------------------------------
        
    public void stateChanged( ChangeEvent e ) {
        computeKeys();
        refreshKeys();
    }
    

    /*
    private void debugKeySet() {
        for( Iterator it = names2nodes.keySet().iterator(); it.hasNext(); ) {
            String k = (String)it.next();
            System.out.println( "    " + k + " -> " +  names2nodes.get( k ) );
        }
    }
     */
     
    

    static final class PackageNode extends FilterNode {
        
        private static final DataFilter NO_FOLDERS_FILTER = new NoFoldersDataFilter();
        
        private final FileObject root;
        private DataFolder dataFolder;
        private boolean isDefaultPackage;
        
        private static Action actions[];
        
        public PackageNode( FileObject root, DataFolder dataFolder ) {
            this( root, dataFolder, isEmpty( dataFolder ) );
        }
        
        public PackageNode( FileObject root, DataFolder dataFolder, boolean empty ) {    
            super( dataFolder.getNodeDelegate(), 
                   empty ? Children.LEAF : dataFolder.createNodeChildren( NO_FOLDERS_FILTER ),
                   new ProxyLookup(new Lookup[] {
                        Lookups.singleton(new NoFoldersContainer (dataFolder)),
                        dataFolder.getNodeDelegate().getLookup(),
                        Lookups.singleton(PackageRootNode.alwaysSearchableSearchInfo(SearchInfoFactory.createSearchInfo(
                                                  dataFolder.getPrimaryFile(),
                                                  false,      //not recursive
                                                  new FileObjectFilter[] {
                                                          SearchInfoFactory.VISIBILITY_FILTER}))),
                   })
            );
            this.root = root;
            this.dataFolder = dataFolder;
            this.isDefaultPackage = root.equals( dataFolder.getPrimaryFile() );
        }
    
        FileObject getRoot() {
            return root; // Used from PackageRootNode
        }
    
        
        public String getName() {
            String relativePath = FileUtil.getRelativePath(root, dataFolder.getPrimaryFile());
            return relativePath == null ?  null : relativePath.replace('/', '.'); // NOI18N
        }
        
        public Action[] getActions( boolean context ) {
            
            if ( !context ) {
                if ( actions == null ) {                
                    // Copy actions and leave out the PropertiesAction and FileSystemAction.                
                    Action superActions[] = super.getActions( context );            
                    ArrayList actionList = new ArrayList( superActions.length );
                    
                    for( int i = 0; i < superActions.length; i++ ) {

                        if ( superActions[ i ] == null && superActions[i + 1] instanceof org.openide.actions.PropertiesAction ) {
                            i ++;
                            continue;
                        }
                        else if ( superActions[i] instanceof org.openide.actions.PropertiesAction ) {
                            continue;
                        }
                        else if ( superActions[i] instanceof org.openide.actions.FileSystemAction ) {
                            actionList.add (null); // insert separator and new action
                            actionList.add (FileSensitiveActions.fileCommandAction(ActionProvider.COMMAND_COMPILE_SINGLE, 
                                NbBundle.getMessage( PackageViewChildren.class, "LBL_CompilePackage_Action" ), // NOI18N
                                null ));                            
                        }
                        
                        actionList.add( superActions[i] );                                                  
                    }

                    actions = new Action[ actionList.size() ];
                    actionList.toArray( actions );
                }
                return actions;
            }
            else {
                return super.getActions( context );
            }
        }
        
        public boolean canRename() {
            if ( isDefaultPackage ) {
                return false;
            }
            else {         
                return true;
            }
        }

        public boolean canCut () {
            return !isDefaultPackage;    
        }

        /**
         * Copy handling
         */
        public Transferable clipboardCopy () throws IOException {
            try {
                return new PackageTransferable (this, DnDConstants.ACTION_COPY);
            } catch (ClassNotFoundException e) {
                Exception ioe = new IOException ();
                throw (IOException) ErrorManager.getDefault().annotate(ioe,e);
            }
        }
        
        public Transferable clipboardCut () throws IOException {
            try {
                return new PackageTransferable (this, DnDConstants.ACTION_MOVE);
            } catch (ClassNotFoundException e) {
                Exception ioe = new IOException ();
                throw (IOException) ErrorManager.getDefault().annotate(ioe,e);
            }
        }

        public PasteType[] getPasteTypes(Transferable t) {
            if (t.isDataFlavorSupported(ExTransferable.multiFlavor)) {
                try {
                    MultiTransferObject mto = (MultiTransferObject) t.getTransferData (ExTransferable.multiFlavor);
                    boolean hasPackageFlavor = false;
                    for (int i=0; i < mto.getCount(); i++) {
                        DataFlavor[] flavors = mto.getTransferDataFlavors(i);
                        if (isPackageFlavor(flavors)) {
                            hasPackageFlavor = true;
                        }
                    }
                    return hasPackageFlavor ? new PasteType[0] : super.getPasteTypes (t);
                } catch (UnsupportedFlavorException e) {
                    ErrorManager.getDefault().notify(e);
                    return new PasteType[0];
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(e);
                    return new PasteType[0];
                }
            }
            else {
                DataFlavor[] flavors = t.getTransferDataFlavors();
                if (isPackageFlavor(flavors)) {
                    return new PasteType[0];
                }
                else {
                    return super.getPasteTypes(t);
                }
            }
        }


        private boolean isPackageFlavor (DataFlavor[] flavors) {
            for (int i=0; i<flavors.length; i++) {
                if (SUBTYPE.equals(flavors[i].getSubType ()) && PRIMARY_TYPE.equals(flavors[i].getPrimaryType ())) {
                    //Disable pasting into package, only paste into root is allowed
                    return true;
                }
            }
            return false;
        }

        
        public void setName(String name) {
            if (isDefaultPackage) {
                return;
            }
            String oldName = getName();
            if (oldName.equals(name)) {
                return;
            }
            if (!isValidPackageName (name)) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message (
                        NbBundle.getMessage(PackageViewChildren.class,"MSG_InvalidPackageName"), NotifyDescriptor.INFORMATION_MESSAGE));
                return;
            }
            name = name.replace('.','/')+'/';           //NOI18N
            oldName = oldName.replace('.','/')+'/';     //NOI18N
            int i;
            for (i=0; i<oldName.length() && i< name.length(); i++) {
                if (oldName.charAt(i) != name.charAt(i)) {
                    break;
                }
            }
            i--;
            int index = oldName.lastIndexOf('/',i);     //NOI18N
            String commonPrefix = index == -1 ? null : oldName.substring(0,index);
            String toCreate = (index+1 == name.length()) ? "" : name.substring(index+1);    //NOI18N
            try {
                FileObject commonFolder = commonPrefix == null ? this.root : this.root.getFileObject(commonPrefix);
                FileObject destination = commonFolder;
                StringTokenizer dtk = new StringTokenizer(toCreate,"/");    //NOI18N
                while (dtk.hasMoreTokens()) {
                    String pathElement = dtk.nextToken();
                    FileObject tmp = destination.getFileObject(pathElement);
                    if (tmp == null) {
                        tmp = destination.createFolder (pathElement);
                    }
                    destination = tmp;
                }
                FileObject source = this.dataFolder.getPrimaryFile();                
                DataFolder sourceFolder = DataFolder.findFolder (source);
                DataFolder destinationFolder = DataFolder.findFolder (destination);
                DataObject[] children = sourceFolder.getChildren();
                for (int j=0; j<children.length; j++) {
                    if (children[j].getPrimaryFile().isData()) {
                        children[j].move(destinationFolder);
                    }
                }
                while (!commonFolder.equals(source)) {
                    if (source.getChildren().length==0) {
                        FileObject tmp = source;
                        source = source.getParent();
                        tmp.delete();
                    }
                    else {
                        break;
                    }
                }
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify (ioe);
            }
        }
        
        
        
        public boolean canDestroy() {
            if ( isDefaultPackage ) {
                return false;
            }
            else {
                return true;
            }
        }
        
        public void destroy() throws IOException {
            FileObject parent = dataFolder.getPrimaryFile().getParent();
            // First; delete all files except packages
            DataObject ch[] = dataFolder.getChildren();
            boolean empty = true;
            for( int i = 0; ch != null && i < ch.length; i++ ) {
                if ( !ch[i].getPrimaryFile().isFolder() ) {
                    ch[i].delete();
                }
                else {
                    empty = false;
                }
            }
            
            // If empty delete itself
            if ( empty ) {
                super.destroy();
            }
            
            
            // Second; delete empty super packages
            while( !parent.equals( root ) && parent.getChildren().length == 0  ) {
                FileObject newParent = parent.getParent();
                parent.delete();
                parent = newParent;
            }
        }
        
        
        public String getDisplayName() {
            FileObject folder = dataFolder.getPrimaryFile();
            String path = FileUtil.getRelativePath(root, folder);
            if (path == null) {
                // ???
                return "";
            }
            return PackageDisplayUtils.getDisplayLabel( path.replace('/', '.'));
        }
        
        public String getShortDescription() {
            FileObject folder = dataFolder.getPrimaryFile();
            String path = FileUtil.getRelativePath(root, folder);
            if (path == null) {
                // ???
                return "";
            }
            return PackageDisplayUtils.getToolTip(folder, path.replace('/', '.'));
        }

        
        public Image getIcon(int type) {
            FileObject folder = dataFolder.getPrimaryFile();
            String path = FileUtil.getRelativePath(root, folder);
            if (path == null) {
                // ???
                return null;
            }
            return PackageDisplayUtils.getIcon(folder, path.replace('/', '.'), isLeaf() );
        }
        
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
        
        public void update() {
            fireIconChange();
            fireOpenedIconChange();            
        }
        
        public void updateDisplayName() {
            fireNameChange(null, null);
            fireDisplayNameChange(null, null);
            fireShortDescriptionChange(null, null);
        }
        
        public void updateChildren() {            
            boolean leaf = isLeaf();
            DataFolder df = getDataFolder();
            boolean empty = isEmpty( df ); 
            if ( leaf != empty ) {
                setChildren( empty ? Children.LEAF: df.createNodeChildren( NO_FOLDERS_FILTER ) );                
                update();
            }
        }
        
        private DataFolder getDataFolder() {
            return (DataFolder)getCookie(DataFolder.class);
        }
        
        private static boolean isEmpty( DataFolder dataFolder ) {
            if ( dataFolder == null ) {
                return true;
            }
            return PackageDisplayUtils.isEmpty( dataFolder.getPrimaryFile() );
        }
        
       
        private static boolean isValidPackageName (String name) {
            if (name.length() == 0) {
                //Fast check of default pkg
                return true;
            }
            StringTokenizer tk = new StringTokenizer(name,".",true); //NOI18N
            boolean delimExpected = false;
            while (tk.hasMoreTokens()) {
                String namePart = tk.nextToken();
                if (!delimExpected) {
                    if (namePart.equals(".")) { //NOI18N
                        return false;
                    }
                    for (int i=0; i< namePart.length(); i++) {
                        char c = namePart.charAt(i);
                        if (i == 0) {
                            if (!Character.isJavaIdentifierStart (c)) {
                                return false;
                            }
                        }
                        else {
                            if (!Character.isJavaIdentifierPart(c)) {
                                return false;
                            }
                        }
                    }
                }
                else {
                    if (!namePart.equals(".")) { //NOI18N
                        return false;
                    }
                }
                delimExpected = !delimExpected;
            }
            return delimExpected;
        }
    }
    
    private static final class NoFoldersContainer 
    implements DataObject.Container, java.beans.PropertyChangeListener,
               NonRecursiveFolder {
        private DataFolder folder;
        private PropertyChangeSupport prop = new PropertyChangeSupport (this);
        
        public NoFoldersContainer (DataFolder folder) {
            this.folder = folder;
        }
        
        public FileObject getFolder() {
            return folder.getPrimaryFile();
        }
        
        public DataObject[] getChildren () {
            DataObject[] arr = folder.getChildren ();
            ArrayList list = new ArrayList (arr.length);
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] instanceof DataFolder) continue;
                
                list.add (arr[i]);
            }
            return list.size () == arr.length ? arr : (DataObject[])list.toArray (new DataObject[0]);
        }

        public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
            prop.addPropertyChangeListener (l);
        }

        public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
            prop.removePropertyChangeListener (l);
        }

        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if (DataObject.Container.PROP_CHILDREN.equals (evt.getPropertyName ())) {
                prop.firePropertyChange (PROP_CHILDREN, null, null);
            }
        }
    }
    
    static final class NoFoldersDataFilter implements ChangeListener, ChangeableDataFilter {
        
        EventListenerList ell = new EventListenerList();        
        
        public NoFoldersDataFilter() {
            VisibilityQuery.getDefault().addChangeListener( this );
        }
                
        public boolean acceptDataObject(DataObject obj) {                
            FileObject fo = obj.getPrimaryFile();                
            return  VisibilityQuery.getDefault().isVisible( fo ) && !(obj instanceof DataFolder);
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

    static class PackageTransferable extends ExTransferable.Single {

        private PackageNode node;

        public PackageTransferable (PackageNode node, int operation) throws ClassNotFoundException {
            super (new DataFlavor (PACKAGE_FLAVOR.format(new Object[] {new Integer(operation)})));
            this.node = node;
        }

        protected Object getData() throws IOException, UnsupportedFlavorException {
            return this.node;
        }
    }


    static class PackagePasteType extends PasteType {
        
        private int op;
        private PackageNode[] nodes;
        private FileObject srcRoot;

        public PackagePasteType (FileObject srcRoot, PackageNode[] node, int op) {
            assert op == DnDConstants.ACTION_COPY || op == DnDConstants.ACTION_MOVE : "Invalid DnD operation";  //NOI18N
            this.nodes = node;
            this.op = op;
            this.srcRoot = srcRoot;
        }

        public Transferable paste() throws IOException {
            for (int ni=0; ni< nodes.length; ni++) {
                FileObject fo = srcRoot;
                if (!nodes[ni].isDefaultPackage) {
                    String pkgName = nodes[ni].getName();
                    StringTokenizer tk = new StringTokenizer(pkgName,".");  //NOI18N
                    while (tk.hasMoreTokens()) {
                        String name = tk.nextToken();
                        FileObject tmp = fo.getFileObject(name,null);
                        if (tmp == null) {
                            tmp = fo.createFolder(name);
                        }
                        fo = tmp;
                    }
                }
                DataFolder dest = DataFolder.findFolder(fo);
                DataObject[] children = nodes[ni].dataFolder.getChildren();
                boolean cantDelete = false;
                for (int i=0; i< children.length; i++) {
                    if (children[i].getPrimaryFile().isData() 
                    && VisibilityQuery.getDefault().isVisible (children[i].getPrimaryFile())) {
                        //Copy only the package level
                        children[i].copy (dest);
                        if (this.op == DnDConstants.ACTION_MOVE) {
                            try {
                                children[i].delete();
                            } catch (IOException ioe) {
                                cantDelete = true;
                            }
                        }
                    }
                    else {
                        cantDelete = true;
                    }
                }
                if (this.op == DnDConstants.ACTION_MOVE && !cantDelete) {
                    try {
                        FileObject tmpFo = nodes[ni].dataFolder.getPrimaryFile();
                        FileObject originalRoot = nodes[ni].root;
                        assert tmpFo != null && originalRoot != null;
                        while (!tmpFo.equals(originalRoot)) {
                            if (tmpFo.getChildren().length == 0) {
                                FileObject tmpFoParent = tmpFo.getParent();
                                tmpFo.delete ();
                                tmpFo = tmpFoParent;
                            }
                            else {
                                break;
                            }
                        }
                    } catch (IOException ioe) {
                        //Not important
                    }
                }
            }
            return ExTransferable.EMPTY;
        }

        public String getName() {
            return NbBundle.getMessage(PackageViewChildren.class,"TXT_PastePackage");
        }
    }

}
