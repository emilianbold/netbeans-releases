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

package org.netbeans.modules.java.j2seproject.ui;

import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.project.SourceGroup;


/**
 * LibrariesSourceGroup
 * {@link SourceGroup} implementation passed to
 * {@link org.netbeans.spi.java.project.support.ui.PackageView#createPackageView(SourceGroup)}
 * @author Tomas Zezula
 */
final class LibrariesSourceGroup implements SourceGroup {

    private final FileObject root;
    private final String name;
    private final Icon icon;
    private final Icon openIcon;

    LibrariesSourceGroup (FileObject root, String name ) {
        this (root, name, null, null);
    }

    LibrariesSourceGroup (FileObject root, String name, Icon icon, Icon openIcon) {
        this.root = root;
        this.name = name;
        this.icon = icon;
        this.openIcon = openIcon;
    }


    public FileObject getRootFolder() {
        return this.root;
    }

    public String getName() {
        return this.name;
    }

    public String getDisplayName() {
        return this.getName();
    }

    public Icon getIcon(boolean opened) {
        return opened ? openIcon : icon;
    }

    public boolean contains(FileObject file) throws IllegalArgumentException {
        return root.equals(file) || FileUtil.isParentOf(root,file);
    }

    public boolean equals (Object other) {
        if (!(other instanceof LibrariesSourceGroup)) {
            return false;
        }
        LibrariesSourceGroup osg = (LibrariesSourceGroup) other;
        return name == null ? osg.name == null : name.equals (osg.name) &&
            root == null ? osg.root == null : root.equals (osg.root);  
    }

    public int hashCode () {
        return ((name == null ? 0 : name.hashCode())<<16) | ((root==null ? 0 : root.hashCode()) & 0xffff);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        //Not needed
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        //Not needed
    }
}
