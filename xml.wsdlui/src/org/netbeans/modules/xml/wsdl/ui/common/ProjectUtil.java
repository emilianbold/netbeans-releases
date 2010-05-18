/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.xml.wsdl.ui.common;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.ErrorManager;

/**
 * @author radval
 *
 * Netbeans Project related utility operations
 */
public class ProjectUtil {


    public static  Set getClasspathProjects(Project p) {
        Set classpathProjects = new HashSet();
        try {
            SubprojectProvider sp = (SubprojectProvider) p.getLookup().lookup(SubprojectProvider.class);
            Set ls = sp.getSubprojects();
            if (ls.size() < 1) {
                return classpathProjects;
            }

            Project[] sps = (Project[])ls.toArray(new Project[ls.size()]);
            String[] spn = new String[sps.length];
            for (int i = 0; i < sps.length; i++) {
                spn[i] = sps[i].getProjectDirectory().getPath().toLowerCase();
            }

            String sroot = p.getProjectDirectory().getPath();
            AntProjectHelper ah = (AntProjectHelper) p.getLookup().lookup(AntProjectHelper.class);
            String src = ah.getStandardPropertyEvaluator().getProperty("javac.classpath");//NOI18N
            StringTokenizer st = new StringTokenizer(src, ";");//NOI18N
            Vector v = new Vector();
            while (st.hasMoreTokens()) {
                String spath = st.nextToken();
                File sfile =  new File(sroot+"/"+spath);//NOI18N
                v.add(sfile.getCanonicalPath().replace('\\', '/').toLowerCase());
            }
            String[] vs = (String[]) v.toArray(new String[0]);

            v.removeAllElements();
            for (int i = 0; i < vs.length; i++) {
                for (int j =0; j <sps.length; j++) {
                    if (vs[i].startsWith(spn[j])) {
                        v.add(sps[j]);
                        break;
                    }
                }
            }
            
            classpathProjects.addAll(v);
            return classpathProjects;
        } catch(Exception exception) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exception);
        }

        return classpathProjects;
    }

}
