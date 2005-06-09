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

package org.netbeans.modules.apisupport.project.queries;

import java.util.Iterator;
import org.netbeans.spi.java.queries.AccessibilityQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Element;
import org.netbeans.modules.apisupport.project.*;

// XXX need unit test

/**
 * Says which module packages are accessible.
 * @author Jesse Glick
 */
public final class AccessibilityQueryImpl implements AccessibilityQueryImplementation {
    
    private final NbModuleProject project;
    
    public AccessibilityQueryImpl(NbModuleProject project) {
        this.project = project;
    }
    
    public Boolean isPubliclyAccessible(FileObject pkg) {
        FileObject srcdir = project.getSourceDirectory();
        if (srcdir != null) {
            String path = FileUtil.getRelativePath(srcdir, pkg);
            if (path != null) {
                String name = path.replace('/', '.');
                Element config = project.getHelper().getPrimaryConfigurationData(true);
                Element pubPkgs = Util.findElement(config, "public-packages", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
                if (pubPkgs == null) {
                    // Try <friend-packages> too.
                    pubPkgs = Util.findElement(config, "friend-packages", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
                }
                if (pubPkgs != null) {
                    Iterator it = Util.findSubElements(pubPkgs).iterator();
                    while (it.hasNext()) {
                        Element pubPkg = (Element) it.next();
                        boolean sub = "subpackages".equals(pubPkg.getLocalName()); // NOI18N
                        String pubPkgS = Util.findText(pubPkg);
                        if (name.equals(pubPkgS) || (sub && name.startsWith(pubPkgS + '.'))) {
                            return Boolean.TRUE;
                        }
                    }
                    return Boolean.FALSE;
                } else {
                    assert false : "Invalid project.xml for " + project;
                    return null;
                }
            }
        }
        return null;
    }
    
}
