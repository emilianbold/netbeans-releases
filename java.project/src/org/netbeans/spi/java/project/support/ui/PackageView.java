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

import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;

/**
 * Factory for package views.
 * @see org.netbeans.spi.project.ui.LogicalViewProvider
 * @author Jesse Glick
 */
public class PackageView {
    
    private PackageView() {}
    
    /**
     * Create a package-oriented view of a Java source root.
     * @param root the root folder of a Java source tree (corresponds to default package)
     * @return children to display packages and files in those packages
     * @throws IllegalArgumentException if the supplied file object is not a folder
     */
    public static Children createPackageView(FileObject root) throws IllegalArgumentException {
        return new PackageViewChildren( root );
    }
    
}
