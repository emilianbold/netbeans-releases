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
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.api.java.queries.AccessibilityQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * Display of Java sources in a package structure rather than folder structure.
 * @author Adam Sotona, Jesse Glick
 */
final class PackageViewChildren extends Children.Keys/*<DataFolder>*/ implements PropertyChangeListener {

    private static final DataFilter NO_FOLDERS_FILTER = new DataFilter() {
        public boolean acceptDataObject(DataObject obj) {
            return !(obj instanceof DataFolder);
        }
    };

    // XXX should be rewritten to use FileObject
    private final DataFolder root;

    /**
     * Creates children based on a single source root.
     * @param root the folder where sources start (must be a package root)
     */    
    public PackageViewChildren(DataFolder root) {
        if (root == null) {
            throw new NullPointerException();
        }
        this.root = root;
    }

    protected Node[] createNodes(Object obj) {
        // XXX bad - adding listener too often
        ((DataFolder)obj).addPropertyChangeListener(this);
        PackageNode n = new PackageNode(root, (DataFolder)obj);
        return new Node[] {n};
    }

    protected void destroyNodes(Node[] arr) {
        if (arr != null) {
            for (int i=0; i<arr.length; i++) {
                if (arr[i] != null) {
                    DataFolder df = (DataFolder)arr[i].getCookie(DataFolder.class);
                    if (df != null) {
                        df.removePropertyChangeListener(this);
                    }
                }
            }
        }
        super.destroyNodes(arr);
    }

    private void setKeys() {
        // XXX this is not going to perform too well for a huge source root...
        SortedSet/*<DataFolder>*/ s = new TreeSet(new PackageNameComparator(root));
        findNonExcludedPackages(root, s);
        setKeys(s);
    }
    
    /**
     * Collect all recursive subfolders, except those which have subfolders
     * but no files.
     */
    private static void findNonExcludedPackages(DataFolder f, Set/*<DataFolder>*/ s) {
        DataObject[] kids = f.getChildren();
        boolean hasSubfolders = false;
        boolean hasFiles = false;
        for (int i = 0; i < kids.length; i++) {
            if (kids[i] instanceof DataFolder) {
                findNonExcludedPackages((DataFolder)kids[i], s);
                hasSubfolders = true;
            } else {
                hasFiles = true;
            }
        }
        if (hasFiles || !hasSubfolders) {
            s.add(f);
        }
    }
    
    /**
     * Sorts folders by package name.
     */
    private static final class PackageNameComparator implements Comparator/*<DataFolder>*/ {
        
        private final FileObject root;
        
        PackageNameComparator(DataFolder root) {
            this.root = root.getPrimaryFile();
        }
        
        public int compare(Object o1, Object o2) {
            DataFolder d1 = (DataFolder)o1;
            DataFolder d2 = (DataFolder)o2;
            String p1 = FileUtil.getRelativePath(root, d1.getPrimaryFile());
            String p2 = FileUtil.getRelativePath(root, d2.getPrimaryFile());
            return p1.compareTo(p2);
        }
        
    }
    
    protected void addNotify() {
        super.addNotify();
        setKeys();
        root.addPropertyChangeListener(this);
    }

    protected void removeNotify() {
        root.removePropertyChangeListener(this);
        setKeys(Collections.EMPTY_SET);
        super.removeNotify();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (DataFolder.PROP_CHILDREN.equals(evt.getPropertyName())) {
            setKeys();
            Node n[] = getNodes();
            for (int i = 0; i < n.length; i++) {
                // XXX bad, PackageNode's should do their own listening if they need to
                if (n[i] instanceof PackageNode)
                    ((PackageNode)n[i]).updateIcon();
            }
        } else if (DataFolder.PROP_NAME.equals(evt.getPropertyName())) {
            Node n[] = getNodes();
            for (int i = 0; i < n.length; i++) {
                if (n[i] instanceof PackageNode) {
                    ((PackageNode)n[i]).updateDisplayName();
                }
            }
        }
    }

    private static String computeDisplayName(DataFolder root, DataFolder df) {
        String path = FileUtil.getRelativePath(root.getPrimaryFile(), df.getPrimaryFile());
        if (path.length() == 0) {
            return NbBundle.getMessage(PackageViewChildren.class, "LBL_DefaultPackage");
        } else {
            return path.replace('/', '.');
        }
    }

    private static boolean checkEmpty(DataFolder df) {
        if (df == null) return true;
        Enumeration en = df.children();
        while (en.hasMoreElements()) {
            if (NO_FOLDERS_FILTER.acceptDataObject((DataObject)en.nextElement())) {
                return false;
            }
        }
        return true;
    }


    private static final class PackageNode extends FilterNode {

        private static final Image PACKAGE = Utilities.loadImage("org/netbeans/spi/java/project/support/ui/package.gif"); // NOI18N
        private static final Image PACKAGE_EMPTY = Utilities.loadImage("org/netbeans/spi/java/project/support/ui/packageEmpty.gif"); // NOI18N
        private static final Image PACKAGE_PRIVATE = Utilities.loadImage("org/netbeans/spi/java/project/support/ui/packagePrivate.gif"); // NOI18N
        private static final Image PACKAGE_PUBLIC = Utilities.loadImage("org/netbeans/spi/java/project/support/ui/packagePublic.gif"); // NOI18N
        
        private final DataFolder root;

        public PackageNode(DataFolder root, DataFolder df) {
            super(df.getNodeDelegate(), checkEmpty(df)? Children.LEAF : df.createNodeChildren(NO_FOLDERS_FILTER));
            this.root = root;
            disableDelegation(DELEGATE_GET_DISPLAY_NAME | DELEGATE_SET_DISPLAY_NAME);
            setDisplayName(computeDisplayName(root, df));
        }

        public Image getIcon(int type) {
            if (isLeaf()) {
                return PACKAGE_EMPTY;
            } else {
                DataFolder df = (DataFolder)getCookie(DataFolder.class);
                Boolean b = AccessibilityQuery.isPubliclyAccessible(df.getPrimaryFile());
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
        
        public String getShortDescription() {
            DataFolder df = (DataFolder)getCookie(DataFolder.class);
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

        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        public void updateIcon() {
            final boolean leaf = isLeaf();
            final DataFolder df = (DataFolder)getCookie(DataFolder.class);
            if (leaf != checkEmpty(df)) {
                setChildren(leaf ? df.createNodeChildren(NO_FOLDERS_FILTER) : Children.LEAF);
                fireIconChange();
                fireOpenedIconChange();
            }
        }

        public void updateDisplayName() {
            DataFolder df = (DataFolder)getCookie(DataFolder.class);
            if (df == null) return;
            String n = computeDisplayName(root, df);
            if (!n.equals(getDisplayName())) setDisplayName(n);
        }
    }
}
