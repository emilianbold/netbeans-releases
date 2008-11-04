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

package org.netbeans.modules.cnd.dwarfdiscovery;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.discovery.api.Configuration;
import org.netbeans.modules.cnd.discovery.api.FolderProperties;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.dwarfdiscovery.provider.*;

/**
 *
 * @author Alexander Simon
 */
public class DwarfAnalyzer {
    
    public static void analyze(String[] files){
        DwarfProvider provider = new DwarfProvider() {
            public int canAnalyze(ProjectProxy project) {
                return 1;
            }
        };
        provider.getProperty(DwarfProvider.EXECUTABLES_KEY).setValue(files);
        dumpProject(provider, new ProjectProxy() {
            public boolean createSubProjects() {
                return false;
            }

            public Project getProject() {
                return null;
            }

            public String getMakefile() {
                return null;
            }

            public String getSourceRoot() {
                return null;
            }

            public String getExecutable() {
                return null;
            }

            public String getWorkingFolder() {
                return null;
            }
        });
    }
    
    private static void dumpProject(DwarfProvider provider, ProjectProxy project){
        List<Configuration> confs = provider.analyze(project,null);
        for (Iterator<Configuration> it = confs.iterator(); it.hasNext();) {
            Configuration conf = it.next();
            List<ProjectProperties> langList = conf.getProjectConfiguration();
            for (Iterator<ProjectProperties> it2 = langList.iterator(); it2.hasNext();) {
                ProjectProperties proj = it2.next();
                String lang = ItemProperties.LanguageKind.CPP == proj.getLanguageKind() ? "c++":"c"; // NOI18N
                System.out.println("Project "+lang); // NOI18N
                System.out.println("User include paths:"); // NOI18N
                StringBuilder buf = new StringBuilder();
                for (String elem : proj.getUserInludePaths()) {
                    System.out.println(elem);
                    if (elem.startsWith(File.separator)){
                        if (buf.length()>0) {
                            buf.append(":"); // NOI18N
                        }
                        buf.append(elem);
                    }
                }
                System.out.println("Configuration string:"); // NOI18N
                System.out.println("    "+buf.toString()); // NOI18N
                System.out.println("User macros:"); // NOI18N
                for (Map.Entry<String,String> elem : proj.getUserMacros().entrySet()) {
                    System.out.println(elem.getKey()+"="+elem.getValue()); // NOI18N
                }
                List<FolderProperties> folders = proj.getConfiguredFolders();
                for (FolderProperties folder : folders) {
                    dumpFolder(folder);
                }
            }
        }
    }
    
    private static void dumpFolder(FolderProperties folder){
        System.out.println("Folder "+folder.getItemPath()); // NOI18N
        System.out.println("User include paths:"); // NOI18N
        StringBuilder buf = new StringBuilder();
        for (String elem : folder.getUserInludePaths()) {
            System.out.println(elem);
            if (elem.startsWith(File.separator)){
                if (buf.length()>0) {
                    buf.append(":"); // NOI18N
                }
                buf.append(elem);
            }
        }
        System.out.println("Configuration string:"); // NOI18N
        System.out.println("    "+buf.toString()); // NOI18N
        System.out.println("User macros:"); // NOI18N
        for (Map.Entry<String,String> elem : folder.getUserMacros().entrySet()) {
            System.out.println(elem.getKey()+"="+elem.getValue()); // NOI18N
        }
    }
}
