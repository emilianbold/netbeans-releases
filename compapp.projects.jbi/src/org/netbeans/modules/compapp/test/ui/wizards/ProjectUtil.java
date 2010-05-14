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
package org.netbeans.modules.compapp.test.ui.wizards;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.ErrorManager;

/**
 * @author radval
 * @author jqian
 *
 * Netbeans Project related utility operations
 */
public class ProjectUtil {

    private static final Logger mLog =
            Logger.getLogger("org.netbeans.modules.compapp.test.ui.wizards.ProjectUtil"); // NOI18N

    public static Set<Project> getClasspathProjects(Project p, boolean recursive) {

        Set<Project> ret = new HashSet<Project>();
        try {
            SubprojectProvider spProvider = p.getLookup().lookup(SubprojectProvider.class);
            if (spProvider != null) {
                for (Project sp : spProvider.getSubprojects()) {
                    ret.add(sp);
                    if (recursive) {
                        ret.addAll(getClasspathProjects(sp, recursive));
                    }
                }
            }
        } catch (Exception exception) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exception);
        }

        return ret;
    }

    /*
    public static Set<Project> getClasspathProjects(Project p) {
        Set<Project> ret = new HashSet<Project>();
        try {
            SubprojectProvider spProvider = p.getLookup().lookup(SubprojectProvider.class);
            Set<? extends Project> sps = spProvider.getSubprojects();

            String pRoot = FileUtil.toFile(p.getProjectDirectory()).getPath();

            AntProjectHelper ah = p.getLookup().lookup(AntProjectHelper.class);
            String jbiContentAdditional = ah.getStandardPropertyEvaluator().getProperty(
                    JbiProjectProperties.JBI_CONTENT_ADDITIONAL);

            // This should be OS-agnostic
            StringTokenizer st = new StringTokenizer(jbiContentAdditional, ";"); //File.pathSeparator); // NOI18N
            while (st.hasMoreTokens()) {
                String spath = st.nextToken();
                // Relative path checking (see BuildServiceAssembly.java)
                if ((spath.indexOf(':') < 0) && (!spath.startsWith("/"))) { // i.e., relative path // NOI18N
                    spath = pRoot + "/" + spath; // NOI18N
                }

                String sfilePath = new File(spath).getCanonicalPath();

                for (Project sp : sps) {
                    File pFile = FileUtil.toFile(sp.getProjectDirectory());
                    if (sfilePath.startsWith(pFile.getCanonicalPath())) {
                        ret.add(sp);
                        break;
                    }
                }
            }
            ret.addAll(sps);
        } catch (Exception exception) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exception);
        }

        return ret;
    }*/
}
