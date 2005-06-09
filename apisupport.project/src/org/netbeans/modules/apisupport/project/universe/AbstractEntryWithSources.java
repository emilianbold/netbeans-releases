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

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.apisupport.project.ManifestManager;

abstract class AbstractEntryWithSources extends AbstractEntry {
    
    private LocalizedBundleInfo bundleInfo;
    
    protected LocalizedBundleInfo getBundleInfo() {
        if (bundleInfo == null) {
            bundleInfo = ModuleList.loadBundleInfo(getSourceLocation());
        }
        return bundleInfo;
    }
    
    protected Set/*<String>*/ computePublicClassNamesInMainModule() throws IOException {
        Set/*<String>*/ result = new HashSet();
        File src = new File(getSourceLocation(), "src"); // XXX hardcoding src.dir
        ManifestManager.PackageExport[] pkgs = getPublicPackages();
        for (int i = 0; i < pkgs.length; i++) {
            String pkg = pkgs[i].getPackage();
            scanForClasses(result, pkg, new File(src, pkg.replace('.', File.separatorChar)), pkgs[i].isRecursive());
        }
        return result;
    }
    
    private void scanForClasses(Set/*<String>*/ result, String pkg, File dir, boolean recurse) throws IOException {
        if (!dir.isDirectory()) {
            return;
        }
        File[] kids = dir.listFiles();
        if (kids == null) {
            throw new IOException(dir.getAbsolutePath());
        }
        for (int i = 0; i < kids.length; i++) {
            String name = kids[i].getName();
            if (name.endsWith(".java")) { // NOI18N
                String basename = name.substring(0, name.length() - 5);
                result.add(pkg + '.' + basename);
                // no inner classes scanned, too slow
            }
            if (recurse && kids[i].isDirectory()) {
                scanForClasses(result, pkg + '.' + name, kids[i], true);
            }
        }
    }
    
}
