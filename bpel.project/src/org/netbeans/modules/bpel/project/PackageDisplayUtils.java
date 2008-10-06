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


package org.netbeans.modules.bpel.project;

import java.awt.Image;
import org.netbeans.api.java.queries.AccessibilityQuery;
import org.netbeans.api.queries.VisibilityQuery;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

// XXX needs unit test

/**
 * Provides display name and icon utilities for
 * {@link PackageViewChildren.PackageNode} and {@link PackageListView.PackageItem}.
 * @author Jesse Glick
 */
public final class PackageDisplayUtils {

    private PackageDisplayUtils() {}
    
    /** whether to turn on #42589 */
    private static final boolean TRUNCATE_PACKAGE_NAMES =
        Boolean.getBoolean("org.netbeans.spi.java.project.support.ui.packageView.TRUNCATE_PACKAGE_NAMES"); // NOI18N

    private static final Image PACKAGE = ImageUtilities.loadImage("org/netbeans/spi/java/project/support/ui/package.gif"); // NOI18N
    private static final Image PACKAGE_EMPTY = ImageUtilities.loadImage("org/netbeans/spi/java/project/support/ui/packageEmpty.gif"); // NOI18N
    private static final Image PACKAGE_PRIVATE = ImageUtilities.loadImage("org/netbeans/spi/java/project/support/ui/packagePrivate.gif"); // NOI18N
    private static final Image PACKAGE_PUBLIC = ImageUtilities.loadImage("org/netbeans/spi/java/project/support/ui/packagePublic.gif"); // NOI18N

    /**
     * Find the proper display label for a package.
     * @param pkg the actual folder
     * @param pkgname the dot-separated package name (<code>""</code> for default package)
     * @return an appropriate display label for it
     */
    public static String getDisplayLabel(String pkgname) {
        return computePackageName(pkgname, TRUNCATE_PACKAGE_NAMES);
    }
    
    /**
     * Find the proper tool tip for a package.
     * May have more info than the display label.
     * @param pkg the actual folder
     * @param pkgname the dot-separated package name (<code>""</code> for default package)
     * @return an appropriate display label for it
     */
    public static String getToolTip(FileObject pkg, String pkgname) {
        String pkglabel = computePackageName(pkgname, false);
        Boolean b = AccessibilityQuery.isPubliclyAccessible(pkg);
        if (b != null) {
            if (b.booleanValue()) {
                return NbBundle.getMessage(PackageDisplayUtils.class, "LBL_public_package", pkglabel);
            } else {
                return NbBundle.getMessage(PackageDisplayUtils.class, "LBL_private_package", pkglabel);
            }
        } else {
            return NbBundle.getMessage(PackageDisplayUtils.class, "LBL_package", pkglabel);
        }
    }
    
    /**
     * Get package name.
     * Handles default package specially.
     * @param truncate if true, show a truncated version to save display space
     */
    private static String computePackageName(String pkgname, boolean truncate) {
        if (pkgname.length() == 0) {
            return NbBundle.getMessage(PackageDisplayUtils.class, "LBL_DefaultPackage"); // NOI18N
        } else {
            if (truncate) {
                // #42589: keep only first letter of first package component, up to three of others
                return pkgname.replaceFirst("^([^.])[^.]+\\.", "$1.").replaceAll("([^.]{3})[^.]+\\.", "$1."); // NOI18N
            } else {
                return pkgname;
            }
        }
    }

     
    
    /**
     * Find the proper display icon for a package.
     * @param pkg the actual folder
     * @param pkgname the dot-separated package name (<code>""</code> for default package)
     * @return an appropriate display icon for it
     */
    public static Image getIcon(FileObject pkg, String pkgname) {
        return getIcon( pkg, pkgname, isEmpty(pkg) );
    }
    
    /** Performance optiomization if the the isEmpty status is alredy known.
     * 
     */
    public static Image getIcon(FileObject pkg, String pkgname, boolean empty ) {
        if ( empty ) {
            return PACKAGE_EMPTY;
        } else {
            Boolean b = pkg.isValid() ? AccessibilityQuery.isPubliclyAccessible(pkg) : null;
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
    
    
    /**
     * Check whether a package is empty (devoid of files except for subpackages).
     */
    public static boolean isEmpty( FileObject fo ) {    
        return isEmpty (fo, true );
    }

    /**
     * Check whether a package is empty (devoid of files except for subpackages).
     * @param recurse specifies whether to check if subpackages are empty too.
     */
    public static boolean isEmpty( FileObject fo, boolean recurse ) {            
        FileObject[] kids = fo.getChildren();
        for( int i = 0; i < kids.length; i++ ) {
            if ( !kids[i].isFolder() && VisibilityQuery.getDefault().isVisible( kids[i] ) ) {
                return false;
            }  
            else if (recurse && VisibilityQuery.getDefault().isVisible( kids[i] ) && !isEmpty(kids[i])) {
                    return false;
            }
        }
        return true;
    }
    
    /**
     * Check whether a package should be displayed.
     * It should be displayed if {@link VisibilityQuery} says it should be,
     * and it is either completely empty, or contains files (as opposed to
     * containing some subpackages but no files).
     */
    public static boolean isSignificant(FileObject pkg) throws IllegalArgumentException {
        if (!pkg.isFolder()) {
            throw new IllegalArgumentException("Not a folder"); // NOI18N
        }
        if (!VisibilityQuery.getDefault().isVisible(pkg)) {
            return false;
        }
        FileObject[] kids = pkg.getChildren();
        boolean subpackages = false;
        for (int i = 0; i < kids.length; i++) {
            if (!VisibilityQuery.getDefault().isVisible(kids[i])) {
                continue;
            }
            if (kids[i].isData()) {
                return true;
            } else {
                subpackages = true;
            }
        }
        return !subpackages;
    }
    
}
