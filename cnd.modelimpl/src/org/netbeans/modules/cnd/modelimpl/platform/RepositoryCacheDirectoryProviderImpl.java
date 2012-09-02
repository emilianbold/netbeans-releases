/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.platform;

import java.io.File;
import java.util.Collection;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectRegistry;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.repository.spi.RepositoryCacheDirectoryProvider;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author vk155633
 */
@ServiceProvider(service=RepositoryCacheDirectoryProvider.class, position=1000)
public class RepositoryCacheDirectoryProviderImpl implements RepositoryCacheDirectoryProvider  {

    @Override
    public File getCacheBaseDirectory() {
        // That's a temporary solution we need to prove the concept
        // Sure it isn't appropriate to get first NativeProject
        Collection<NativeProject> projects = NativeProjectRegistry.getDefault().getOpenProjects();
        if (projects != null && !projects.isEmpty()) {
            NativeProject np = projects.iterator().next();
            if (CndFileUtils.isLocalFileSystem(np.getFileSystem())) {
                File cache = new File(np.getProjectRoot() + "/nbproject/private/cache/model"); //NOI18N
                if (TraceFlags.CACHE_IN_PROJECT) {
                    cache.mkdirs();
                }
                if (cache.exists()) {
                    return cache;
                }
            }
        }
        return null;
    }

//    @Override
//    public File getUnitCacheBaseDirectory(CharSequence unitName) {
//        CharSequence projectName = ProjectBase.getProjectName(unitName);
//        if (projectName != null) {
//            File projectDir = new File(projectName + "/nbproject"); //NOI18N
//            if (projectDir.exists()) {
//                File cache = new File(projectDir + "/private/cache"); //NOI18N
//                cache.mkdirs();
//                if (cache.exists()) {
//                    return cache;
//                }
//            }
//        }
//        return null;
//    }
}
