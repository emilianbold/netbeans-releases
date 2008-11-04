/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.java.j2seplatform.libraries;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.List;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.classpath.ClassPathProvider.class, position=150)
public class J2SELibraryClassPathProvider implements ClassPathProvider {

    public ClassPath findClassPath(FileObject file, String type) {
        assert file != null;
        Library ll = this.getLastUsedLibrary(file);
        if (ll != null) {
            ClassPath[] cp = findClassPathOrNull(file, type, ll);
            return cp != null ? cp[0] : null;
        }
        else {
            for (LibraryManager mgr : LibraryManager.getOpenManagers()) {
                for (Library lib : mgr.getLibraries()) {
                    ClassPath[] cp = findClassPathOrNull(file, type, lib);
                    if (cp != null) {
                        return cp[0];
                    }
                }
            }
            return null;
        }
    }
    private ClassPath[] findClassPathOrNull(FileObject file, String type, Library lib) {
        if (lib.getType().equals(J2SELibraryTypeProvider.LIBRARY_TYPE)) {
            List<URL> resources = lib.getContent(J2SELibraryTypeProvider.VOLUME_TYPE_SRC);
            try {
                ClassPath sourcePath = ClassPathSupport.createClassPath(resources.toArray(new URL[resources.size()]));
                FileObject root = sourcePath.findOwnerRoot(file);
                if (root != null) {
                    setLastUsedLibrary(root, lib);
                    if (ClassPath.SOURCE.equals(type)) {
                        return new ClassPath[] {sourcePath};
                    } else if (ClassPath.COMPILE.equals(type)) {
                        resources = lib.getContent(J2SELibraryTypeProvider.VOLUME_TYPE_CLASSPATH);
                        return new ClassPath[] {ClassPathSupport.createClassPath(resources.toArray(new URL[resources.size()]))};
                    } else if (ClassPath.BOOT.equals(type)) {
                        return new ClassPath[] {JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries()};
                    } else {
                        return new ClassPath[] {null};
                    }
                }
            } catch (final IllegalArgumentException e) {
                final IllegalArgumentException ne = new IllegalArgumentException("LibraryImplementation:["+getImplClassName(lib)+"] returned wrong root:" + e.getMessage());
                Exceptions.printStackTrace(ne.initCause(e));
            }
        }
        return null;
    }
    
    private static String getImplClassName (final Library lib) {
        String result = ""; //NOI18N
        try {
            final Class cls = lib.getClass();
            final Field fld = cls.getDeclaredField("impl"); //NOI18N
            if (fld != null) {                            
                fld.setAccessible(true);
                Object res = fld.get(lib);                            
                if (res != null) {
                    result = res.getClass().getName();
                }
            }
        } catch (NoSuchFieldException noSuchFieldException) {
            //Not needed
        } catch (SecurityException securityException) {
            //Not needed
        } catch (IllegalArgumentException illegalArgumentException) {
            //Not needed
        } catch (IllegalAccessException illegalAccessException) {
            //Not needed
        }
        return result;
    }
    
            
    private synchronized Library getLastUsedLibrary (FileObject fo) {
        if (this.lastUsedRoot != null && FileUtil.isParentOf(this.lastUsedRoot,fo)) {
            return this.lastUsedLibrary;
        }
        else {
            return null;
        }
    }

    private synchronized void setLastUsedLibrary (FileObject root, Library lib) {
        this.lastUsedRoot = root;
        this.lastUsedLibrary = lib;
    }

    private FileObject lastUsedRoot;
    private Library lastUsedLibrary;
}
