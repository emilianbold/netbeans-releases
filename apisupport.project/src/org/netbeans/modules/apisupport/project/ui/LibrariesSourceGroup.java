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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui;

import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import org.netbeans.api.project.SourceGroup;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

// XXX this class is more or less copy-pasted from j2seproject.
// Get rid of it as soon as "some" Libraries Node API is provided.

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
     * @param icon closed icon
     * @param openIcon opened icon
     */
    LibrariesSourceGroup(FileObject root, String displayName, Icon icon, Icon openIcon) {
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
            ErrorManager.getDefault().notify(fsi);
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
    
    public boolean equals(Object other) {
        if (!(other instanceof LibrariesSourceGroup)) {
            return false;
        }
        LibrariesSourceGroup osg = (LibrariesSourceGroup) other;
        return displayName == null ? osg.displayName == null : displayName.equals(osg.displayName) &&
                root == null ? osg.root == null : root.equals(osg.root);
    }
    
    public int hashCode() {
        return ((displayName == null ? 0 : displayName.hashCode())<<16) | ((root==null ? 0 : root.hashCode()) & 0xffff);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        //Not needed
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        //Not needed
    }
    
}
