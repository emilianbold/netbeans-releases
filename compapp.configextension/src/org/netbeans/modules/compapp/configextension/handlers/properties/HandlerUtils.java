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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.compapp.configextension.handlers.properties;

import java.io.File;
import java.util.List;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 *
 * @author jqian
 */
public class HandlerUtils {

    private static final String PROJECT_NS = "http://www.netbeans.org/ns/project/1"; // NOI18N
    private static final String PROJECT_TYPE = "type"; // NOI18N
    private static final String J2SE_PROJECT_TYPE = "org.netbeans.modules.java.j2seproject"; // NOI18N

    /**
     * Gets all the Jar files under the given directory.
     *
     * @param dir       a root directory 
     * @param jarList   the list of collected Jar files (in/out)
     * @param recursive whether search recursively
     */
    public static void getJars(File dir, List<File> jarList, boolean recursive) {

        if (!dir.exists()) {
            return;
        }

        for (File child : dir.listFiles()) {
            if (child.isFile()) {
                if (child.getName().endsWith(".jar")) { // NOI18N
                    jarList.add(child);
                }
            } else {
                if (recursive) {
                    getJars(child, jarList, recursive);
                }
            }
        }
    }

    /**
     * Checks whether the given file is the root directory of a
     * NetBeans J2SE project.
     *
     * @param file
     * @return
     */
    public static boolean isJ2SEProjectDir(File file) {
        FileObject fo = FileUtil.toFileObject(file);

        try {
            Project project = ProjectManager.getDefault().findProject(fo);
            if (project != null) {
                File projectXmlFile = new File(file, "/nbproject/project.xml"); // NOI18N
                if (projectXmlFile.exists()) {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.setNamespaceAware(true);
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document document = builder.parse(projectXmlFile);
                    NodeList typeNodes = document.getElementsByTagNameNS(
                            PROJECT_NS, PROJECT_TYPE);
                    if (typeNodes.getLength() == 1) {
                        String type = typeNodes.item(0).getTextContent();
                        // TODO: add other java project types
                        return J2SE_PROJECT_TYPE.equals(type);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

//    /**
//     * Gets a non-null list of Java libraries defined in the given Java project.
//     *
//     * @param projectDir    (Java) project directory
//     * @return  a list of Java libraries defined in the given project
//     */
//    public static List<String> getJavaProjectLibraries(File projectDir)
//            throws IOException {
//        List<String> ret = new ArrayList<String>();
//        File propertyFile = new File(projectDir, "nbproject/project.properties"); // NOI18N
//        Properties p = new Properties();
//        p.load(new FileInputStream(propertyFile));
//        String classpath = (String) p.get("javac.classpath"); // NOI18N
//        if (classpath != null && classpath.trim().length() > 0) {
//            for (String classpathItem : classpath.split(":")) { // NOI18N
//                classpathItem = classpathItem.trim();
//                if (classpathItem.startsWith("${") && classpathItem.endsWith("}")) { // NOI18N
//                    classpathItem = classpathItem.substring(2, classpathItem.length() - 1);
//                    classpathItem = (String) p.get(classpathItem);
//                }
//                if (classpathItem != null) {
//                    ret.add(classpathItem);
//                }
//            }
//        }
//        return ret;
//    }

    /**
     * Builds the given Java project.
     *
     * @param projectPath   path of the NetBeans J2SE project's root directory
     * @return <code>true</code> if the given project has been built successfully;
     *         <code>false</code> otherwise.
     */
    public static boolean buildJ2SEProject(String projectPath) {
        File buildXmlFile = new File(projectPath + "/build.xml"); // NOI18N
        FileObject buildXmlFO = FileUtil.toFileObject(buildXmlFile);
        String[] targets = new String[] { "jar" }; // NOI18N
        Properties p = null;
        try {
            ExecutorTask task = ActionUtils.runTarget(buildXmlFO, targets, p);
            task.waitFinished();
            return task.result() == 0;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
