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
package org.netbeans.modules.bpel.project.ui;

import java.awt.Component;
import java.awt.Image;
import java.awt.Panel;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.ImageUtilities;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.MultiTransferObject;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openidex.search.SearchInfo;
import org.openidex.search.SearchInfoFactory;


/** Node displaying a packages in given SourceGroup
 * @author Petr Hrebejk
 */
final class PackageRootNode extends AbstractNode implements Runnable, FileStatusListener {

    static Image PACKAGE_BADGE = ImageUtilities.loadImage( "org/netbeans/spi/java/project/support/ui/packageBadge.gif" ); // NOI18N
        
    private static Action actions[]; 

    private SourceGroup group;

    private final FileObject file;
    private final Set files;
    private FileStatusListener fileSystemListener;
    private RequestProcessor.Task task;
    private volatile boolean iconChange;
    private volatile boolean nameChange;
    
    PackageRootNode( SourceGroup group ) {
        this( group, new InstanceContent() );
    }
    
    private PackageRootNode( SourceGroup group, InstanceContent ic ) {
        super( new PackageViewChildren( group.getRootFolder() ),
               new ProxyLookup( new Lookup[] { createLookup ( group ),
                                               new AbstractLookup( ic )} ) );

        ic.add(alwaysSearchableSearchInfo(SearchInfoFactory.createSearchInfoBySubnodes(this)));
        this.group = group;
        file = group.getRootFolder();
        files = Collections.singleton(file);
        try {
            FileSystem fs = file.getFileSystem();
            fileSystemListener = FileUtil.weakFileStatusListener(this, fs);
            fs.addFileStatusListener(fileSystemListener);
        } catch (FileStateInvalidException e) {
            ErrorManager err = ErrorManager.getDefault();
            err.annotate(e, "Cannot get " + file + " filesystem, ignoring...");  // NO18N
            err.notify(ErrorManager.INFORMATIONAL, e);
        }
        setName( group.getName() );
        setDisplayName( group.getDisplayName() );        
    }

    @Override
    public Image getIcon( int type ) {        
        return computeIcon( false, type );
    }

    @Override
    public Image getOpenedIcon( int type ) {
        return computeIcon( true, type );
    }

    @Override
    public String getDisplayName () {
        String s = super.getDisplayName ();

        try {            
            s = file.getFileSystem ().getStatus ().annotateName (s, files);
        } catch (FileStateInvalidException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }

        return s;
    }

    @Override
    public String getHtmlDisplayName() {
         try {
             FileSystem.Status stat = file.getFileSystem().getStatus();
             if (stat instanceof FileSystem.HtmlStatus) {
                 FileSystem.HtmlStatus hstat = (FileSystem.HtmlStatus) stat;

                 String result = hstat.annotateNameHtml (
                     super.getDisplayName(), files);

                 //Make sure the super string was really modified
                 if (!super.getDisplayName().equals(result)) {
                     return result;
                 }
             }
         } catch (FileStateInvalidException e) {
             ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
         }
         return super.getHtmlDisplayName();
    }

    public void run() {
        if (iconChange) {
            fireIconChange();
            fireOpenedIconChange();
            iconChange = false;
        }
        if (nameChange) {
            fireDisplayNameChange(null, null);
            nameChange = false;
        }
    }

    public void annotationChanged(FileStatusEvent event) {
        if (task == null) {
            task = RequestProcessor.getDefault().create(this);
        }

        if ((iconChange == false && event.isIconChange())  || (nameChange == false && event.isNameChange())) {
            if (event.hasChanged(file)) {
                iconChange |= event.isIconChange();
                nameChange |= event.isNameChange();
            }
        }

        task.schedule(50);  // batch by 50 ms
    }    

    @Override
    public Action[] getActions( boolean context ) {

        if ( actions == null ) {
            actions = new Action[] {
                CommonProjectActions.newFileAction(),
                null,
                org.openide.util.actions.SystemAction.get( org.openide.actions.FileSystemAction.class ),
                null,
                org.openide.util.actions.SystemAction.get( org.openide.actions.FindAction.class ),
                null,
                org.openide.util.actions.SystemAction.get( org.openide.actions.PasteAction.class ),
                null,
                org.openide.util.actions.SystemAction.get( org.openide.actions.ToolsAction.class ),
            };
        }
        return actions;            
    }

    // Show reasonable properties of the DataFolder,
    //it shows the sorting names as rw property, the name as ro property and the path to root as ro property
    @Override
    public PropertySet[] getPropertySets() {            
        PropertySet[] properties =  getDataFolderNodeDelegate().getPropertySets();
        for (int i=0; i< properties.length; i++) {
            if (Sheet.PROPERTIES.equals(properties[i].getName())) {
                //Replace the Sheet.PROPERTIES by the new one
                //having the ro name property and ro path property
                properties[i] = Sheet.createPropertiesSet();
                ((Sheet.Set)properties[i]).put( new PropertySupport.ReadOnly (DataObject.PROP_NAME, String.class,
                    NbBundle.getMessage (PackageRootNode.class,"PROP_name"), NbBundle.getMessage (PackageRootNode.class,"HINT_name")) {

                          public /*@Override*/ Object getValue () {
                              return PackageRootNode.this.getDisplayName();
                          }
                });
                ((Sheet.Set)properties[i]).put( new PropertySupport.ReadOnly ("ROOT_PATH", String.class,    //NOI18N
                    NbBundle.getMessage (PackageRootNode.class,"PROP_rootpath"), NbBundle.getMessage (PackageRootNode.class,"HINT_rootpath")) {

                          public /*@Override*/ Object getValue () {                              
                              return FileUtil.getFileDisplayName(PackageRootNode.this.file);
                          }
                });
            }
        }
        return properties;
    }

    // XXX Paste types - probably not very nice 
    @Override
    public void createPasteTypes( Transferable t, List list ) {
        if (t.isDataFlavorSupported(ExTransferable.multiFlavor)) {
            try {
                MultiTransferObject mto = (MultiTransferObject) t.getTransferData (ExTransferable.multiFlavor);
                List l = new ArrayList ();
                boolean isPackageFlavor = false;
                boolean hasTheSameRoot = false;
                int op = -1;
                for (int i=0; i < mto.getCount(); i++) {
                    Transferable pt = mto.getTransferableAt(i);
                    DataFlavor[] flavors = mto.getTransferDataFlavors(i);
                    for (int j=0; j< flavors.length; j++) {
                        if (PackageViewChildren.SUBTYPE.equals(flavors[j].getSubType ()) &&
                                PackageViewChildren.PRIMARY_TYPE.equals(flavors[j].getPrimaryType ())) {
                            if (op == -1) {
                                op = Integer.valueOf (flavors[j].getParameter (PackageViewChildren.MASK)).intValue ();
                            }
                            PackageViewChildren.PackageNode pkgNode = (PackageViewChildren.PackageNode) pt.getTransferData(flavors[j]);
                            if ( !((PackageViewChildren)getChildren()).getRoot().equals( pkgNode.getRoot() ) ) {
                                l.add(pkgNode);
                            }
                            else {
                                hasTheSameRoot = true;
                            }
                            isPackageFlavor = true;
                        }
                    }
                }
                if (isPackageFlavor && !hasTheSameRoot) {
                    list.add (new PackageViewChildren.PackagePasteType (this.group.getRootFolder(),
                                    (PackageViewChildren.PackageNode[]) l.toArray(new PackageViewChildren.PackageNode[l.size()]),
                                    op));
                }
                else if (!isPackageFlavor) {
                    list.addAll( Arrays.asList( getDataFolderNodeDelegate().getPasteTypes( t ) ) );
                }
            } catch (UnsupportedFlavorException e) {
                ErrorManager.getDefault().notify(e);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        else {
            DataFlavor[] flavors = t.getTransferDataFlavors();
            FileObject root = this.group.getRootFolder();
            boolean isPackageFlavor = false;
            if (root!= null  && root.canWrite()) {
                for (int i=0; i<flavors.length; i++) {
                    if (PackageViewChildren.SUBTYPE.equals(flavors[i].getSubType ()) &&
                            PackageViewChildren.PRIMARY_TYPE.equals(flavors[i].getPrimaryType ())) {
                        isPackageFlavor = true;
                        try {
                            int op = Integer.valueOf (flavors[i].getParameter (PackageViewChildren.MASK)).intValue ();
                            PackageViewChildren.PackageNode pkgNode = (PackageViewChildren.PackageNode) t.getTransferData(flavors[i]);
                            if ( !((PackageViewChildren)getChildren()).getRoot().equals( pkgNode.getRoot() ) ) {
                                list.add(new PackageViewChildren.PackagePasteType (root, new PackageViewChildren.PackageNode[] {pkgNode}, op));
                            }
                        } catch (IOException ioe) {
                            ErrorManager.getDefault().notify(ioe);
                        }
                        catch (UnsupportedFlavorException ufe) {
                            ErrorManager.getDefault().notify(ufe);
                        }
                    }
                }
            }
            if (!isPackageFlavor) {
                list.addAll( Arrays.asList( getDataFolderNodeDelegate().getPasteTypes( t ) ) );
            }
        }
    }
    
    @Override
    public /*@Override*/ PasteType getDropType(Transferable t, int action, int index) {        
        PasteType pasteType = super.getDropType(t, action, index);
        //The pasteType can be:
        // 1) PackagePasteType - the t.flavor is package flavor
        // 2) null or DataPasteType - the t.flavor in not package flavor
        if (pasteType instanceof PackageViewChildren.PackagePasteType) {
            ((PackageViewChildren.PackagePasteType)pasteType).setOperation (action);
        }
        return pasteType;
    }

    // Private methods ---------------------------------------------------------
    
    private Node getDataFolderNodeDelegate() {
        return ((DataFolder)getLookup().lookup( DataFolder.class )).getNodeDelegate();
    }
    
    private Image computeIcon( boolean opened, int type ) {
        Image image;
        Icon icon = group.getIcon( opened );
        
        if ( icon == null ) {
            image = opened ? getDataFolderNodeDelegate().getOpenedIcon( type ) : 
                             getDataFolderNodeDelegate().getIcon( type );
            image = ImageUtilities.mergeImages( image, PACKAGE_BADGE, 7, 7 );
        }
        else {
            if ( icon instanceof ImageIcon ) {
                image = ((ImageIcon)icon).getImage();
            }
            else {
                image = icon2image( icon );
            }
        }
        
        return image;        
    }
    
    private static Component CONVERTOR_COMPONENT = new Panel();
    
    static Image icon2image(Icon icon) {
        int height = icon.getIconHeight();
        int width = icon.getIconWidth();
        
        BufferedImage bImage = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
        icon.paintIcon( CONVERTOR_COMPONENT, bImage.getGraphics(), 0, 0 );
        
        return bImage;
    }
            
    private static Lookup createLookup( SourceGroup group ) {
        // XXX Remove DataFolder when paste, find and refresh are reimplemented
        FileObject rootFolder = group.getRootFolder();
        DataFolder dataFolder = DataFolder.findFolder( rootFolder );        
        return Lookups.fixed( new Object[]{ dataFolder, new PathFinder( group ) } );
    }
    
    /** If contained in the lookup can perform the search for a node
     */    
    public static class PathFinder {
        
        private SourceGroup group;
        
        public PathFinder( SourceGroup group ) {
            this.group = group;
        }
        
        public Node findPath( Node root, Object object ) {
            FileObject fo;
            if (object instanceof FileObject) {
                fo = (FileObject) object;
            } else if (object instanceof DataObject) {
                fo = ((DataObject) object).getPrimaryFile();
            } else {
                return null;
            }
            
            FileObject groupRoot = group.getRootFolder();
            if ( FileUtil.isParentOf( groupRoot, fo ) /* && group.contains( fo ) */ ) {
                // The group contains the object

                String relPath = FileUtil.getRelativePath( groupRoot, fo );
                int lastSlashIndex = relPath.lastIndexOf( '/' ); // NOI18N

                String[] path = null;
                if ( fo.isFolder() ) {
                    String packageName = relPath.replace( '/', '.' ); // NOI18N
                    path = new String[] { packageName };
                }
                else if ( lastSlashIndex == -1 ) {
                    path = new String[] { "", fo.getName() };
                }
                else {
                    String packageName = relPath.substring( 0, lastSlashIndex ).replace( '/', '.' ); // NOI18N
                    path = new String[] { packageName, fo.getName() };                    
                } 
                try {
                    // XXX if there are two files differing only by extension in the package,
                    // this will be wrong...
                    return NodeOp.findPath( root, path );
                }
                catch ( NodeNotFoundException e ) {
                    if (!fo.isFolder()) {
                        // If it is a DefaultDataObject, the node name contains the extension.
                        if (lastSlashIndex == -1) {
                            path = new String[] {"", fo.getNameExt()};
                        } else {
                            String packageName = relPath.substring(0, lastSlashIndex).replace('/', '.'); // NOI18N
                            path = new String[] {packageName, fo.getNameExt()};
                        }
                        try {
                            return NodeOp.findPath(root, path);
                        } catch (NodeNotFoundException e2) {
                            // already handled
                        }
                    }
                    // did not manage to find it after all... why?
                    return null;
                }
            }   
            else if ( groupRoot.equals( fo ) ) {
                // First try to find default package
                try {
                    return NodeOp.findPath( root, new String[] { "" } ); // NOI18N
                }
                catch ( NodeNotFoundException e ) {
                    // If it does not exists return this node
                }                        
                return root;
            }

            return null;
        }
        
        @Override
        public String toString() {
            return "PathFinder[" + group + "]"; // NOI18N
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
