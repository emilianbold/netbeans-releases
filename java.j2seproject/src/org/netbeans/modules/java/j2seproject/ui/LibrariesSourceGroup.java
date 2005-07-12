/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seproject.ui;

import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import org.netbeans.api.project.SourceGroup;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

/**
 * LibrariesSourceGroup
 * {@link SourceGroup} implementation passed to
 * {@link org.netbeans.spi.java.project.support.ui.PackageView#createPackageView(SourceGroup)}
 * @author Tomas Zezula
 */
final class LibrariesSourceGroup implements SourceGroup {

    private final FileObject root;
    private final String displayName;
    private final Icon icon;
    private final Icon openIcon;

    /**
     * Creates new LibrariesSourceGroup
     * @param root the classpath root
     * @param displayName the display name presented to user
     */              
    LibrariesSourceGroup (FileObject root, String displayName ) {
        this (root, displayName, null, null);
    }

    /**
     * Creates new LibrariesSourceGroup
     * @param root the classpath root
     * @param displayName the display name presented to user
     * @param icon closed icon
     * @param openIcon opened icon
     */          
    LibrariesSourceGroup (FileObject root, String displayName, Icon icon, Icon openIcon) {
        assert root != null;
        this.root = root;
        this.displayName = displayName;
        this.icon = icon;
        this.openIcon = openIcon;
    }


    public FileObject getRootFolder() {
        return this.root;
    }

    public String getName() {
        try {        
            return root.getURL().toExternalForm();
        } catch (FileStateInvalidException fsi) { 
            ErrorManager.getDefault().notify (fsi);
            return root.toString();
        }
    }

    public String getDisplayName() {
        return this.displayName;
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
        return displayName == null ? osg.displayName == null : displayName.equals (osg.displayName) &&
            root == null ? osg.root == null : root.equals (osg.root);  
    }

    public int hashCode () {
        return ((displayName == null ? 0 : displayName.hashCode())<<16) | ((root==null ? 0 : root.hashCode()) & 0xffff);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        //Not needed
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        //Not needed
    }
}
