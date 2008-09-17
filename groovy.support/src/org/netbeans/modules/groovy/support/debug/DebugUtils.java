/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.support.debug;

import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Martin Adamek
 */
public class DebugUtils {

    public static FileObject getFileObjectFromUrl(String url) {
        
        FileObject fo = null;
        
        try {
            fo = URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException e) {
            //noop
        }

        return fo;
    }

    public static String getClassFilter(String url) {
        FileObject fo = getFileObjectFromUrl(url);
        String relativePath = url;
        if (fo != null) {
            FileObject root = ClassPath.getClassPath(fo, ClassPath.SOURCE).findOwnerRoot(fo);
            if (root == null) {
                return null;
            }
            relativePath = FileUtil.getRelativePath(root, fo);
        }
        if (relativePath.endsWith(".groovy")) { // NOI18N
            relativePath = relativePath.substring(0, relativePath.length() - 7);
        }
        return relativePath.replace('/', '.');
    }

    public static String getJspName(String url) {

        FileObject fo = getFileObjectFromUrl(url);
        if (fo != null) {
            return fo.getNameExt();
        }
        return (url == null) ? null : url.toString();
    }
    
    public static String getJspPath(String url) {
       
        FileObject fo = getFileObjectFromUrl(url);
        String relativePath = url;
        if (fo != null) {
            FileObject root = ClassPath.getClassPath(fo, ClassPath.SOURCE).findOwnerRoot(fo);
            relativePath = FileUtil.getRelativePath(root, fo);
        }
        
        return relativePath;

    }
    
}
