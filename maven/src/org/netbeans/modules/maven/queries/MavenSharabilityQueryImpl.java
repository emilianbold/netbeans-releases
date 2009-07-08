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
package org.netbeans.modules.maven.queries;

import java.io.File;
import java.util.Collection;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.spi.queries.SharabilityQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author  Milos Kleint
 */
public class MavenSharabilityQueryImpl implements SharabilityQueryImplementation {
    
    private final NbMavenProjectImpl project;
    /** Creates a new instance of MavenSharabilityQueryImpl */
    public MavenSharabilityQueryImpl(NbMavenProjectImpl proj) {
        project = proj;
    }
    
    private Boolean checkShare(File file) {
        File basedir = FileUtil.toFile(project.getProjectDirectory());
        // is this condition necessary?
        if (!file.getAbsolutePath().startsWith(basedir.getAbsolutePath())) {
            return null;
        }
        if (basedir.equals(file.getParentFile()) && "nbproject".equals(file.getName())) { //NOI18N
            // screw the netbeans profiler directory creation.
            // #98662
            return false;
        }
        if (file.equals(new File(basedir, "profiles.xml"))) { //NOI18N
            //profiles.xml are not meant to be put in version control.
            return false;
        }
        if (file.getName().startsWith("nbactions") && file.getParentFile().equals(basedir)) { //NOI18N
            //non shared custom configurations shall not be added to version control.
            M2ConfigProvider configs = project.getLookup().lookup(M2ConfigProvider.class);
            if (configs != null) {
                Collection<M2Configuration> col = configs.getNonSharedConfigurations();
                for (M2Configuration conf : col) {
                    if (file.getName().equals(M2Configuration.getFileNameExt(conf.getId()))) {
                        return false;
                    }
                }
            }
        }

        //this part is slow if invoked on built project that is not opened (needs to load the embedder)
        //can it be replaced with code not touching the embedder?
        MavenProject proj = project.getOriginalMavenProject();
        Build build = proj.getBuild();
        if (build != null && build.getDirectory() != null) {
            File target = new File(build.getDirectory());
            if (target.equals(file) || file.getAbsolutePath().startsWith(target.getAbsolutePath())) {
                return false;
            }
        }
        return true;
    }
    
    public int getSharability(File file) {
        //#119541 for the project's root, return MIXED right away.
        FileObject fo = FileUtil.toFileObject(file);
        if (fo != null && fo.equals(project.getProjectDirectory())) {
            return SharabilityQuery.MIXED;
        }
        Boolean check = checkShare(file);
        if (check == null) {
            return SharabilityQuery.UNKNOWN;
        }
        if (Boolean.TRUE.equals(check)) {
            if (file.isDirectory()) {
                //#119541 let's play safe  here and always return MIXED for directories.
                //consider this setup:
                // project root
                //     -- modules
                //            -- subproject1
                //            -- subproject2
                // The "modules" folder itself doesn't contain a project, therefore belongs to root project
                // however it cannot be marked as SHARABLE because the subproject1+2 folder would be added automatically then.
                
                return SharabilityQuery.MIXED;
            }
            return SharabilityQuery.SHARABLE;
        }
        return SharabilityQuery.NOT_SHARABLE;
    }
    
}
