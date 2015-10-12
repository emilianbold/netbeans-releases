/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.source.parsing;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.tools.JavaFileManager.Location;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.classfile.ClassFile;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomas Zezula
 */
final class UserModuleProvider implements ModuleFileManager.ModuleProvider {

    private static final String MODULE_INFO = "module-info";    //NOI18N

    private final ClassPath cp;

    UserModuleProvider(@NonNull final ClassPath cp) {
        assert cp != null;
        this.cp = cp;
    }

    @Override
    public Map<URL,Set<Location>> getModulePath(@NonNull final Location location) {
        final Map<URL,Set<Location>> roots = new HashMap<>();
        cp.entries().stream()
                .map((e) -> e.getRoot())
                .map((fo) -> {
                    if (fo == null) {
                        return null;
                    }
                    FileObject moduleInfo = fo.getFileObject(MODULE_INFO,FileObjects.CLASS);
                    if (moduleInfo != null) {
                        return moduleInfo;
                    };
                    return fo.getFileObject(MODULE_INFO,FileObjects.SIG);
                })
                .filter((fo) -> fo != null)
                .forEach((moduleInfo) -> {
                    final URL modulePathRoot = moduleInfo.getParent().toURL();
                    Set<Location> modules = roots.get(modulePathRoot);
                    if (modules == null) {
                        modules = new HashSet<>();
                        roots.put(modulePathRoot, modules);
                    }
                    final String moduleName = getModuleName(moduleInfo);
                    if (moduleName != null) {
                        modules.add(ModuleLocation.create(
                                location,
                                moduleInfo.getParent().toURL(),
                                moduleName));
                    }
                });
        return roots;
    }

    @CheckForNull
    private static String getModuleName(@NonNull final FileObject fo) {
        try {
            try (InputStream in = fo.getInputStream()) {
                ClassFile cf = new ClassFile(in, false);
                return FileObjects.getPackageAndName(cf.getName().getExternalName())[0];
            }
        } catch (IOException ioe) {
            return null;
        }
    }
}
