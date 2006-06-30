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

package org.netbeans.modules.apisupport.project.queries;

import java.util.Iterator;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleProjectType;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.spi.java.queries.AccessibilityQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Element;

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
                    Util.err.log(ErrorManager.WARNING, "Invalid project.xml for " + project);
                    return null;
                }
            }
        }
        return null;
    }
    
}
