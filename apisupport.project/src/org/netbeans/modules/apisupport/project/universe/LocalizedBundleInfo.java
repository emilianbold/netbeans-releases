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

package org.netbeans.modules.apisupport.project.universe;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.openide.filesystems.FileObject;

final class LocalizedBundleInfo {
    
    static final LocalizedBundleInfo EMPTY = new LocalizedBundleInfo(null, null, null, null);
    
    private final String localizedName;
    private final String category;
    private final String longDescription;
    private final String shortDescription;
    
    /** Simle factory method. */
    static LocalizedBundleInfo load(FileObject bundleFO) throws IOException {
        if (bundleFO == null) {
            return null;
        }
        InputStream bundleIS = bundleFO.getInputStream();
        try {
            Properties p = new Properties();
            p.load(bundleIS);
            LocalizedBundleInfo bundleInfo = new LocalizedBundleInfo(
                    p.getProperty("OpenIDE-Module-Name"), // NOI18N
                    p.getProperty("OpenIDE-Module-Display-Category"), // NOI18N
                    p.getProperty("OpenIDE-Module-Short-Description"), // NOI18N
                    p.getProperty("OpenIDE-Module-Long-Description")); // NOI18N
            return bundleInfo;
        } finally {
            bundleIS.close();
        }
    }
    
    /** Use factory method instead. */
    private LocalizedBundleInfo(String localizedName, String category,
            String shortDescription, String longDescription) {
        this.localizedName = localizedName;
        this.category = category;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
    }
    
    String getLocalizedName() {
        return localizedName;
    }
    
    String getCategory() {
        return category;
    }
    
    String getShortDescription() {
        return shortDescription;
    }
    
    String getLongDescription() {
        return longDescription;
    }
    
}
