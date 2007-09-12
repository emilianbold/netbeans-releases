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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.bpel.debugger;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.api.project.ProjectInformation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public final class ProjectUtil {
    public static Project getProject(String baseDir) throws IOException {
        Project proj = null;
        
        File projFolder = new File(baseDir);
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(projFolder));
        try {
            proj = ProjectManager.getDefault().findProject(fo);
        } catch (IllegalArgumentException ex){
            //log.warning("Unable to get Netbeans Project object for: " + baseDir);
        }
        return proj;
    }
    
    public static Set<String> getSubprojectsBaseDirs(String projBaseDir) throws IOException {
        Project proj = getProject(projBaseDir);
        if (proj != null) {
            return getSubprojectsBaseDirs(proj);
        } else {
            return new HashSet<String>();
        }
    }
    
    public static Set<String> getSubprojectsBaseDirs(Project proj) throws IOException{
        Set<String> ret = new HashSet<String>();
        
        if (proj != null){
            SubprojectProvider sp = (SubprojectProvider) proj.getLookup().lookup(SubprojectProvider.class);
            if (sp != null){
                Set sprjs = sp.getSubprojects();
                Iterator itr = sprjs.iterator();
                while (itr.hasNext()){
                    Project sprj = (Project) itr.next();
                    String baseDir = sprj.getProjectDirectory().getPath();
                    ret.add(baseDir);
                    ret.addAll(getSubprojectsBaseDirs(sprj));
                }
            }
        }
        return ret;
    }
    
    public static String getProjectDisplayName(String baseDir) throws IOException {
        Project proj = getProject(baseDir);
        if (proj != null) {
            ProjectInformation info = (ProjectInformation)proj.getLookup().lookup(ProjectInformation.class);
            return info.getDisplayName();
        } else {
            return null;
        }
    }
}
