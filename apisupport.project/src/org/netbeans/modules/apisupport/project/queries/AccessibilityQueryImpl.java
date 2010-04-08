/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.apisupport.project.queries;

import java.util.Iterator;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.spi.java.queries.AccessibilityQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.xml.XMLUtil;
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
                Element config = project.getPrimaryConfigurationData();
                Element pubPkgs = XMLUtil.findElement(config, "public-packages", NbModuleProject.NAMESPACE_SHARED); // NOI18N
                if (pubPkgs == null) {
                    // Try <friend-packages> too.
                    pubPkgs = XMLUtil.findElement(config, "friend-packages", NbModuleProject.NAMESPACE_SHARED); // NOI18N
                }
                if (pubPkgs != null) {
                    Iterator it = XMLUtil.findSubElements(pubPkgs).iterator();
                    while (it.hasNext()) {
                        Element pubPkg = (Element) it.next();
                        boolean sub = "subpackages".equals(pubPkg.getLocalName()); // NOI18N
                        String pubPkgS = XMLUtil.findText(pubPkg);
                        if (name.equals(pubPkgS) || (sub && name.startsWith(pubPkgS + '.'))) {
                            return true;
                        }
                    }
                } else {
                    Util.err.log(ErrorManager.WARNING, "Invalid project.xml for " + project);
                    return null;
                }
            }
        }
        // Everything else assumed to *not* be public.
        return false;
    }

}
