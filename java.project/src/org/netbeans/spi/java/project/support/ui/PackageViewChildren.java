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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.netbeans.api.java.queries.AccessibilityQuery;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * Display of Java sources in a package structure rather than folder structure.
 * @author Adam Sotona, Jesse Glick, Petr Hrebejk
 */
final class PackageViewChildren extends Children.Keys implements FileChangeListener {
    
    private static final String NODE_NOT_CREATED = "NNC"; // NOI18N
    
    private TreeMap names2nodes;
    private final FileObject root;

    /**
     * Creates children based on a single source root.
     * @param root the folder where sources start (must be a package root)
     */    
    public PackageViewChildren(FileObject root) {
        if (root == null) {
            throw new NullPointerException();
        }
        this.root = root;
    }

    protected Node[] createNodes( Object obj ) {
        FileObject fo = root.getFileObject( (String)obj );
        if ( fo != null ) {
            PackageNode n = new PackageNode( root, DataFolder.findFolder( fo ) );
            names2nodes.put( obj, n );
            return new Node[] {n};
        }
        else {
            return new Node[0];
        }
        
    }
        
    protected void addNotify() {
        // System.out.println("ADD NOTIFY" + root + " : " + this );
        super.addNotify();
        computeKeys();
        refreshKeys();
        try { 
            root.getFileSystem().addFileChangeListener( this );
        }
        catch ( FileStateInvalidException e ) {
            ErrorManager.getDefault().notify( ErrorManager.INFORMATIONAL, e );
        }
    }

    protected void removeNotify() {
        // System.out.println("REMOVE NOTIFY" + root + " : " + this );        
        try {
            root.getFileSystem().removeFileChangeListener( this );
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
        // How ever we have to go through the whole hierarchy in order to find
        // all packages (Hrebejk)
        names2nodes = new TreeMap();
        findNonExcludedPackages( root );
    }
    
    /**
     * Collect all recursive subfolders, except those which have subfolders
     * but no files.
     */
    private void findNonExcludedPackages( FileObject fo ) {
        assert fo.isFolder() : "Package view only accepts folders"; // NOI18N
        FileObject[] kids = fo.getChildren();
        boolean hasSubfolders = false;
        boolean hasFiles = false;
        for (int i = 0; i < kids.length; i++) {
            if (kids[i].isFolder() ) {
                findNonExcludedPackages( kids[i] );
                hasSubfolders = true;
            } else {
                hasFiles = true;
            }
        }
        if (hasFiles || !hasSubfolders) {
            add( fo );
        }
    }
    
    /** Finds all empty parents of given package and deletes them
     */
    private void cleanEmptyKeys( FileObject fo ) {
        FileObject parent = fo.getParent(); 
        while ( FileUtil.isParentOf( root, parent ) ) {
            PackageNode n = get( parent );
            if ( n != null && n.isLeaf() ) {
                // System.out.println("Cleaning " + parent);
                remove( parent );
            }
            parent = parent.getParent();
        }
    }
    
    
    private void add( FileObject fo ) {
        String path = FileUtil.getRelativePath( root, fo );
        assert path != null : "Adding wrong folder " + fo;
        names2nodes.put( path, NODE_NOT_CREATED );
    }

    private void remove( FileObject fo ) {
        String path = FileUtil.getRelativePath( root, fo );        
        assert path != null : "Removing wrong folder" + fo;
        names2nodes.remove( path );
    }
    
    private PackageNode get( FileObject fo ) {
        String path = FileUtil.getRelativePath( root, fo );        
        assert path != null : "Asking for wrong folder" + fo;
        Object o = names2nodes.get( path );
        return o == NODE_NOT_CREATED ? null : (PackageNode)o;
    }
    
    private boolean exists( FileObject fo ) {
        String path = FileUtil.getRelativePath( root, fo );
        return names2nodes.get( path ) != null;
    }
    
    private PackageNode updatePath( String oldPath, String newPath ) {
        Object o = names2nodes.get( oldPath );
        if ( o == null ) {
            return null;
        }        
        names2nodes.remove( oldPath );
        names2nodes.put( newPath, o );
        return o == NODE_NOT_CREATED ? null : (PackageNode)o;
    }
    
    // Implementation of FileChangeListener ------------------------------------
    
    public void fileAttributeChanged( FileAttributeEvent fe ) {}

    public void fileChanged( FileEvent fe ) {} 

    public void fileFolderCreated( FileEvent fe ) {
        FileObject fo = fe.getFile();        
        if ( FileUtil.isParentOf( root, fo ) ) {
            cleanEmptyKeys( fo );                
            add( fo );
            refreshKeys();
        }
    }
    
    public void fileDataCreated( FileEvent fe ) {
        FileObject fo = fe.getFile();
        // System.out.println("Created " + fe);
        if ( FileUtil.isParentOf( root, fo ) ) {
            FileObject parent = fo.getParent();
            PackageNode n = get( parent );
            if ( n == null ) {
                // System.out.println("created adding parent " + parent );
                add( parent );
                refreshKeys();
            }
            else {
                // System.out.println("updateing " + n );                
                n.updateChildren();
            }
        }
    }

    public void fileDeleted( FileEvent fe ) {
        FileObject fo = fe.getFile();       
        
        // System.out.println("FILE DELETED " + FileUtil.getRelativePath( root, fo ) );
        
        if ( FileUtil.isParentOf( root, fo ) ) {
            
            if ( fo.isFolder() ) {
                // System.out.println("REMOVING FODER " + fo );                
                remove( fo );
                // Now add the parent if necessary 
                FileObject parent = fo.getParent();
                if ( FileUtil.isParentOf( root, parent ) && get( parent ) == null && parent.isValid() ) {
                    // System.out.println("ADDING PARENT " + parent );
                    add( parent );
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
                FileObject kids[] = parent.getChildren();
                boolean onlyFolders = true;
                for ( int i = 0; i < kids.length; i++ ) {
                    if ( !kids[i].isFolder() ) {
                        onlyFolders = false;
                        break;
                    }
                }
                if ( kids.length != 0 && onlyFolders ) {
                    remove( parent );
                    refreshKeys();
                }
                 
            }
        } 
    }
    
    
    public void fileRenamed( FileRenameEvent fe ) {
        FileObject fo = fe.getFile();        
        if ( FileUtil.isParentOf( root, fo ) && fo.isFolder() ) {
            String rp = FileUtil.getRelativePath( root, fo.getParent() );
            String oldPath = rp + ( rp.length() == 0 ? "" : "/" ) + fe.getName() + fe.getExt(); // NOI18N

            // Find all entries which have to be updated
            ArrayList needsUpdate = new ArrayList();
            for( Iterator it = names2nodes.keySet().iterator(); it.hasNext(); ) {
                String p = (String)it.next();
                if ( p.startsWith( oldPath ) ) { 
                    needsUpdate.add( p );
                }    
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
            
            if ( needsUpdate.size() > 1 ) {
                // Sorting might change
                refreshKeys();
            }
        }
    }
    
    

    /*
    private void debugKeySet() {
        for( Iterator it = names2nodes.keySet().iterator(); it.hasNext(); ) {
            String k = (String)it.next();
            System.out.println( "    " + k + " -> " +  names2nodes.get( k ) );
        }
    }
     */
     
    

    private static final class PackageNode extends FilterNode {

        private static final DataFilter NO_FOLDERS_FILTER = new DataFilter() {
            public boolean acceptDataObject(DataObject obj) {
                return !(obj instanceof DataFolder);
            }
        };
        
        private static final Image PACKAGE = Utilities.loadImage("org/netbeans/spi/java/project/support/ui/package.gif"); // NOI18N
        private static final Image PACKAGE_EMPTY = Utilities.loadImage("org/netbeans/spi/java/project/support/ui/packageEmpty.gif"); // NOI18N
        private static final Image PACKAGE_PRIVATE = Utilities.loadImage("org/netbeans/spi/java/project/support/ui/packagePrivate.gif"); // NOI18N
        private static final Image PACKAGE_PUBLIC = Utilities.loadImage("org/netbeans/spi/java/project/support/ui/packagePublic.gif"); // NOI18N
        
        private final FileObject root;
        private DataFolder dataFolder;

        public PackageNode( FileObject root, DataFolder dataFolder ) {
            super( dataFolder.getNodeDelegate(), 
                   isEmpty( dataFolder ) ? Children.LEAF : dataFolder.createNodeChildren( NO_FOLDERS_FILTER ) );
            this.root = root;
            this.dataFolder = dataFolder;
            disableDelegation(DELEGATE_GET_DISPLAY_NAME | DELEGATE_SET_DISPLAY_NAME);
            setDisplayName( computeDisplayName() );
        }
                       
        public String getShortDescription() {
            DataFolder df = getDataFolder();
            Boolean b = AccessibilityQuery.isPubliclyAccessible(df.getPrimaryFile());
            if (b != null) {
                if (b.booleanValue()) {
                    return NbBundle.getMessage(PackageViewChildren.class, "LBL_public_package");
                } else {
                    return NbBundle.getMessage(PackageViewChildren.class, "LBL_private_package");
                }
            } else {
                return NbBundle.getMessage(PackageViewChildren.class, "LBL_package");
            }
        }

        
        public Image getIcon(int type) {
            if (isLeaf()) {
                return PACKAGE_EMPTY;
            } else {
                DataFolder df = getDataFolder();
                Boolean b = df.isValid() ? AccessibilityQuery.isPubliclyAccessible(df.getPrimaryFile()) : null;
                if (b != null) {
                    if (b.booleanValue()) {
                        return PACKAGE_PUBLIC;
                    } else {
                        return PACKAGE_PRIVATE;
                    }
                } else {
                    return PACKAGE;
                } 
            }
        }
        
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
        
        public void update() {
            fireIconChange();
            fireOpenedIconChange();
            //fileRenamed();
        }
        
        public void updateDisplayName() {
            setDisplayName( computeDisplayName() );
        }
        
        public void updateChildren() {
            boolean leaf = isLeaf();
            DataFolder df = getDataFolder();
            if ( leaf != isEmpty( df ) ) {
                setChildren( leaf ? df.createNodeChildren( NO_FOLDERS_FILTER ) : Children.LEAF);
                update();
            }
        }
                
        private String computeDisplayName() {
            String path = FileUtil.getRelativePath( root, dataFolder.getPrimaryFile());
            if (path.length() == 0) {
                return NbBundle.getMessage(PackageViewChildren.class, "LBL_DefaultPackage"); // NOI18N
            } else {
                return path.replace('/', '.'); // NOI18N
            }
        }
        
        private DataFolder getDataFolder() {
            return (DataFolder)getCookie(DataFolder.class);
        }
        
        private static boolean isEmpty( DataFolder dataFolder ) {
            if ( dataFolder == null ) {
                return true;
            }
            FileObject fo = dataFolder.getPrimaryFile();
            FileObject[] kids = fo.getChildren();
            for( int i = 0; i < kids.length; i++ ) {
                if ( !kids[i].isFolder() ) {
                    return false;
                }  
            }
            return true;
        }
        
        
    }
    
    
}
