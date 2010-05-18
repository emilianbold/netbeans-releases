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
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Enumeration;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.bpel.project.PackageDisplayUtils;
import org.netbeans.modules.bpel.project.PackageViewSettings;
//import org.netbeans.modules.java.project.PackageDisplayUtils;
//import org.netbeans.modules.java.project.PackageViewSettings;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbPreferences;
import org.openide.util.WeakListeners;

/**
 * Factory for package views.
 * @see org.netbeans.spi.project.ui.LogicalViewProvider
 * @author Jesse Glick
 */
public class PackageView {
        
    private PackageView() {}
    
    /**
     * Create a node which will contain package-oriented view of a source group.
     * <p>
     * The precise structure of this node is <em>not</em> specified by the API
     * and is subject to arbitrary change (perhaps at user option).
     * Callers should not make assumptions about the nature of subnodes, the
     * code or display names of certain nodes, and so on. You may use cookies/lookup
     * to find if particular subnodes correspond to folders or files.
     * </p>
     * @param group a source group which should be represented
     * @return node which will display packages in given group
     */
    public static Node createPackageView( SourceGroup group ) {
        return new RootNode (group);                
    }
    
    /**
     * Finds the node representing given object, if any.
     * The current implementation works only for {@link org.openide.filesystems.FileObject}s
     * and {@link org.openide.loaders.DataObject}s.
     * @param rootNode a node some descendant of which should contain the object
     * @param object object to find
     * @return a node representing the given object, or null if no such node was found
     */
    public static Node findPath(Node rootNode, Object object) {
        
        PackageRootNode.PathFinder pf = (PackageRootNode.PathFinder)rootNode.getLookup().lookup( PackageRootNode.PathFinder.class );
        
        if ( pf != null ) {
            return pf.findPath( rootNode, object );
        } else {
            TreeRootNode.PathFinder pf2 = (TreeRootNode.PathFinder) rootNode.getLookup().lookup(TreeRootNode.PathFinder.class);
            if (pf2 != null) {
                return pf2.findPath(rootNode, object);
            } else {
                return null;
            }
        }
    }
    
    /**
     * Create a list or combo box model suitable for {@link javax.swing.JList} from a source group
     * showing all Java packages in the source group.
     * To display it you will also need {@link #listRenderer}.
     * <p>No particular guarantees are made as to the nature of the model objects themselves,
     * except that {@link Object#toString} will give the fully-qualified package name
     * (or <code>""</code> for the default package), regardless of what the renderer
     * actually displays.</p>
     * @param group a Java-like source group
     * @return a model of its packages
     * @since org.netbeans.modules.java.project/1 1.3 
     */
    
    public static ComboBoxModel createListView(SourceGroup group) {        
        TreeSet data = new TreeSet();        
        findNonExcludedPackages( data, group.getRootFolder(), group );
        return new DefaultComboBoxModel( new Vector( data) );        
    }
    
    /** Fills given collection with flatened packages under given folder
     *@param target The collection to be filled
     *@param fo The folder to be scanned
     *@param group if null the collection will be filled with file objects if 
     *       non null PackageItems will be created.
     */    
    static void findNonExcludedPackages( PackageViewChildren children, FileObject fo ) {
        findNonExcludedPackages( children, null, fo, null );
    }
    
    static void findNonExcludedPackages( Collection target, FileObject fo, SourceGroup group ) {
        findNonExcludedPackages( null, target, fo, group );
    }
     
    
    private static void findNonExcludedPackages( PackageViewChildren children, Collection target, FileObject fo, SourceGroup group ) {
        
        assert fo.isFolder() : "Package view only accepts folders"; // NOI18N
               
        if ( !VisibilityQuery.getDefault().isVisible( fo ) ) {
            return; // Don't show hidden packages
        }
        
        FileObject[] kids = fo.getChildren();
        boolean hasSubfolders = false;
        boolean hasFiles = false;
        for (int i = 0; i < kids.length; i++) {            
            // XXX could use PackageDisplayUtils.isSignificant here
            if ( VisibilityQuery.getDefault().isVisible( kids[i] ) ) {
                if (kids[i].isFolder() ) {
                    findNonExcludedPackages( children, target, kids[i], group );
                    hasSubfolders = true;
                } 
                else {
                    hasFiles = true;
                }
            }
        }
        if (hasFiles || !hasSubfolders) {
            if ( group != null ) {
                target.add( new PackageItem(group, fo, !hasFiles ) );
            }
            else {
                children.add( fo, !hasFiles );
            }
        }
    }
         
//    public static ComboBoxModel createListView(SourceGroup group) {
//        DefaultListModel model = new DefaultListModel();
//        SortedSet/*<PackageItem>*/ items = new TreeSet();
//        FileObject root = group.getRootFolder();
//        if (PackageDisplayUtils.isSignificant(root)) {
//            items.add(new PackageItem(group, root));
//        }
//        Enumeration/*<FileObject>*/ files = root.getChildren(true);
//        while (files.hasMoreElements()) {
//            FileObject f = (FileObject) files.nextElement();
//            if (f.isFolder() && PackageDisplayUtils.isSignificant(f)) {
//                items.add(new PackageItem(group, f));
//            }
//        }
//        return new DefaultComboBoxModel(items.toArray(new PackageItem[items.size()]));
//    }
    
    
    /**
     * Create a renderer suited to rendering models created using {@link #createListView}.
     * The exact nature of the display is not specified.
     * Instances of String can also be rendered.
     * @return a suitable package renderer
     * @since org.netbeans.modules.java.project/1 1.3 
     */
    public static ListCellRenderer listRenderer() {
        return new PackageListCellRenderer();
    }
    
    /**
     * FilterNode which listens on the PackageViewSettings and changes the view to 
     * the package view or tree view
     *
     */
    private static final class RootNode extends FilterNode implements PropertyChangeListener {
        
        private SourceGroup sourceGroup;
        private PackageViewSettings settings;
        
        private RootNode (SourceGroup group) {
            super (getOriginalNode (group, PackageViewSettings.getDefault()));
            this.sourceGroup = group;
            this.settings = PackageViewSettings.getDefault();
            this.settings.addPropertyChangeListener(WeakListeners.propertyChange(this, this.settings));
        }
        
        public void propertyChange (PropertyChangeEvent event) {
            if (PackageViewSettings.PROP_PACKAGE_VIEW_TYPE.equals(event.getPropertyName())) {
                changeOriginal(getOriginalNode (this.sourceGroup, this.settings), true);
            }
        }
        
        private static Node getOriginalNode (SourceGroup group, PackageViewSettings settings) {            
            assert settings != null : "PackageViewSettings can't be null"; //NOI18N
            FileObject root = group.getRootFolder();
            //Guard condition, if the project is (closed) and deleted but not yet gced
            // and the view is switched, the source group is not valid.
            if ( root == null || !root.isValid()) {
                return new AbstractNode (Children.LEAF);
            }
            switch (settings.getPackageViewType()) {
                case PackageViewSettings.TYPE_PACKAGE_VIEW:
                    return new PackageRootNode(group);
                case PackageViewSettings.TYPE_TREE:
                    return new TreeRootNode(group);
                default:
                    assert false : "Unknown PackageView Type"; //NOI18N
                    return new PackageRootNode(group);
            }
        }        
    }
    
    /**
     * Model item representing one package.
     */
    static final class PackageItem implements Comparable {
        
        private static IdentityHashMap/*<Image,Icon>*/ image2icon = new IdentityHashMap();
        
        private final boolean empty;
        private final FileObject pkg;
        private final String pkgname;
        private Icon icon;
        
        public PackageItem(SourceGroup group, FileObject pkg, boolean empty) {
            this.pkg = pkg;
            this.empty = empty;
            String path = FileUtil.getRelativePath(group.getRootFolder(), pkg);
            assert path != null : "No " + pkg + " in " + group;
            pkgname = path.replace('/', '.');
        }
        
        public String toString() {
            return pkgname;
        }
        
        public String getLabel() {
            return PackageDisplayUtils.getDisplayLabel(pkgname);
        }
        
        public Icon getIcon() {
            if ( icon == null ) {
                Image image = PackageDisplayUtils.getIcon(pkg, pkgname, empty);                
                icon = (Icon)image2icon.get( image );
                if ( icon == null ) {            
                    icon = new ImageIcon( image );
                    image2icon.put( image, icon );
                }
            }
            return icon;
        }

        public int compareTo(Object obj) {
            return pkgname.compareTo(((PackageItem) obj).pkgname);
        }
        
    }
    
    /**
     * The renderer which just displays {@link PackageItem#getLabel} and {@link PackageItem#getIcon}.
     */
    private static final class PackageListCellRenderer extends DefaultListCellRenderer {
        
        public PackageListCellRenderer() {}

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof PackageItem) {
                PackageItem pkgitem = (PackageItem) value;
                super.getListCellRendererComponent(list, pkgitem.getLabel(), index, isSelected, cellHasFocus);
                setIcon(pkgitem.getIcon());
            } else {
                // #49954: render a specially inserted package somehow.
                String pkgitem = (String) value;
                super.getListCellRendererComponent(list, pkgitem, index, isSelected, cellHasFocus);
            }
            return this;
        }
        
    }
    
    
}
