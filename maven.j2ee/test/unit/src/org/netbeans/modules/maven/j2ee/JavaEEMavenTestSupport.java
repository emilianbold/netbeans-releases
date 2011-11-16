/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.j2ee;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.common.dd.DDHelper;
import org.netbeans.modules.maven.j2ee.model.NbConfiguration;
import org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;

/**
 * Utility class for Java EE Maven tests. Allows to easily create projects of different types (Ejb, War, EA, Package)
 * and offers various methods for creating/updating pom.xml, nb-configuration.xml etc.
 * 
 * @author Martin Janicek
 */
public class JavaEEMavenTestSupport {
 
    public static final String WEB_INF = "WEB-INF"; //NOI18N
    public static final String WEB_XML = "web.xml"; //NOI18N
    
    private static final StringBuilder sb = new StringBuilder();
    
    
    /**
     * <p>Creates default Maven Web project structure which could be used for tests
     * In file system it seems like this:</p>
     * 
     * <pre>
     * |-- pom.xml
     * |
     * `-- src
     *     `-- main
     *         |-- java
     *         |-- resources
     *         `-- webapp
     * </pre>
     * 
     * For creation of additional files like nb-configuration.xml see MavenTestSupport methods.
     * Project has set Java EE 6 as a default platform version. For setting up different version use 
     * {@link MavenProjectSupport#setJ2eeVersion(org.netbeans.api.project.Project, java.lang.String)}
     * 
     * @param projectDir root directory of the project
     * @return created project with structure described above
     */
    public static Project createMavenWebProject(File projectDir) {
        return createMavenWebProject(FileUtil.toFileObject(projectDir));
    }
    
    public static Project createMavenWebProject(File projectDir, String pom) {
        return createMavenWebProject(FileUtil.toFileObject(projectDir), pom);
    }
    
    public static Project createMavenWebProject(FileObject projectDir) {
        return createMavenWebProject(projectDir, null);
    }
    
    public static Project createMavenWebProject(FileObject projectDir, String pom) {
        try {
            FileObject src = FileUtil.createFolder(projectDir, "src"); //NOI18N
            FileObject main = FileUtil.createFolder(src, "main"); //NOI18N
            FileObject java = FileUtil.createFolder(main, "java"); //NOI18N
            FileObject resources = FileUtil.createFolder(main, "resources"); //NOI18N
            FileObject webapp = FileUtil.createFolder(main, "webapp"); //NOI18N

            return createProject(projectDir, pom);
        } catch (IOException ex) {
            return null;
        }
    }
    
    /**
     * <p>Creates default Maven EJB project structure which could be used for tests
     * In file system it seems like this:</p>
     * 
     * <pre>
     * |-- pom.xml
     * |
     * `-- src
     *     `-- main
     *         |-- java
     *         `-- resources
     * </pre>
     * 
     * For creation of additional files like nb-configuration.xml see MavenTestSupport methods.
     * Project has set Java EE 6 as a default platform version. For setting up different version use 
     * {@link MavenProjectSupport#setJ2eeVersion(org.netbeans.api.project.Project, java.lang.String)}
     * 
     * @param projectDir root directory of the project
     * @return created project with structure described above
     */
    public static Project createMavenEjbProject(File projectDir) {
        return createMavenEjbProject(FileUtil.toFileObject(projectDir));
    }
    
    public static Project createMavenEjbProject(FileObject projectDir) {
        try {
            FileObject src = FileUtil.createFolder(projectDir, "src"); //NOI18N
            FileObject main = FileUtil.createFolder(src, "main"); //NOI18N
            FileObject java = FileUtil.createFolder(main, "java"); //NOI18N
            FileObject resources = FileUtil.createFolder(main, "resources"); //NOI18N

            return createProject(projectDir);
        } catch (IOException ex) {
            return null;
        }
    }
    
    /**
     * <p>Creates default Maven Ear project structure which could be used for tests
     * In file system it seems like this:</p>
     * 
     * <pre>
     * |-- pom.xml
     * |
     * `-- src
     *     `-- main
     *         `-- application
     * </pre>
     * 
     * For creation of additional files like nb-configuration.xml see MavenTestSupport methods.
     * Project has set Java EE 6 as a default platform version. For setting up different version use 
     * {@link MavenProjectSupport#setJ2eeVersion(org.netbeans.api.project.Project, java.lang.String)}
     * 
     * @param projectDir root directory of the project
     * @return created project with structure described above
     */
    public static Project createMavenEarProject(File projectDir) {
        return createMavenEarProject(FileUtil.toFileObject(projectDir));
    }
    
    public static Project createMavenEarProject(FileObject projectDir) {
        try {
            FileObject src = FileUtil.createFolder(projectDir, "src"); //NOI18N
            FileObject main = FileUtil.createFolder(src, "main"); //NOI18N
            FileObject application = FileUtil.createFolder(main, "application"); //NOI18N

            return createProject(projectDir);
        } catch (IOException ex) {
            return null;
        }
    }
    
    /**
     * <p>Creates default Maven Ear project structure which could be used for tests
     * In file system it seems like this:</p>
     * 
     * <pre>
     * |-- pom.xml
     * |
     * |-- projectName-ear
     * |-- projectName-ejb
     * `-- projectName-web
     * </pre>
     * 
     * Each of these subdirectories contains default files/folders as well - see 
     * {@link #createMavenEarProject(File projectDir)}, {@link #createMavenEjbProject(File projectDir)},
     * {@link #createMavenWebProject(File projectDir)}
     * 
     * @param projectDir root directory of the project
     * @return created project with structure described above
     */
    public static Project createMavenEAProject(FileObject projectDir) {
        try {
            String name = projectDir.getName();
            FileObject ear = FileUtil.createFolder(projectDir, name + "ear"); //NOI18N
            FileObject ejb = FileUtil.createFolder(projectDir, name + "ejb"); //NOI18N
            FileObject web = FileUtil.createFolder(projectDir, name + "web"); //NOI18N

            createMavenEarProject(ear);
            createMavenEjbProject(ejb);
            createMavenWebProject(web);

            return createProject(projectDir);
        } catch (IOException ex) {
            return null;
        }
    }
    
    private static Project createProject(FileObject projectDir) throws IOException {
        return createProject(projectDir, null);
    }
    
    private static Project createProject(FileObject projectDir, String pom) throws IOException {
        if (pom != null) {
            JavaEEMavenTestSupport.createPom(projectDir, pom);
        } else {
            JavaEEMavenTestSupport.createPom(projectDir);
        }
        
        Project project = ProjectManager.getDefault().findProject(projectDir);
        MavenProjectSupport.setJ2eeVersion(project, "1.6"); //NOI18N

        return project;
    }
    
    public static FileObject createPom(Project project) throws IOException {
        return TestFileUtils.writeFile(project.getProjectDirectory(), "pom.xml", createDefaultPom()); //NOI18N
    }
 
    public static FileObject createPom(Project project, String pomContent) throws IOException {
        return TestFileUtils.writeFile(project.getProjectDirectory(), "pom.xml", pomContent); //NOI18N
    }
    
    public static FileObject createPom(FileObject workDir) throws IOException {
        return TestFileUtils.writeFile(workDir, "pom.xml", createDefaultPom()); //NOI18N
    }
    
    public static FileObject createPom(FileObject workDir, String pomContent) throws IOException {
        return TestFileUtils.writeFile(workDir, "pom.xml", pomContent); //NOI18N
    }
    
    public static String createSimplePom(String modelVersion, String groupID, String artifactID, String packaging, String version) {
        PomBuilder builder = new PomBuilder();
        
        builder.appendPomContent(modelVersion, groupID, artifactID, packaging, version);
        return builder.buildPom();
    }
    
    private static String createDefaultPom() {
        PomBuilder builder = new PomBuilder();
        
        builder.appendDefaultTestValues();
        return builder.buildPom();
    }

    public static FileObject createWebXml(FileObject projectDir) throws IOException {
        return DDHelper.createWebXml(Profile.JAVA_EE_6_WEB, getWebInf(projectDir));
    }
    
    private static FileObject getWebInf(FileObject projectDir) throws IOException {
        return FileUtil.createFolder(projectDir, "src/main/webapp/WEB-INF"); //NOI18N
    }
    
    public static FileObject createNbActions(Project project) throws IOException {
        return TestFileUtils.writeFile(project.getProjectDirectory(), "nbactions.xml", createNbActionContent()); //NOI18N
    }
    
    // TODO should be parametrizeable
    private static String createNbActionContent() {
        return "<actions>" +
                    "<action>" + 
                        "<actionName>run</actionName>" +
                        "<goals>" +
                            "<goal>package</goal>" +
                        "</goals>" +
                    "</action>" +
                "</actions>"; //NOI18N
    }
    
    public static FileObject createNbConfiguration(Project project) throws IOException {
        return TestFileUtils.writeFile(project.getProjectDirectory(), "nb-configuration.xml", createNbConfigContent()); //NOI18N
    }
    
    private static String createNbConfigContent() {
        return createNbConfigContent(null);
    }
    
    private static String createNbConfigContent(NbConfiguration nbConfiguration) {
        sb.delete(0, sb.length());
        sb.append("<project-shared-configuration>"); //NOI18N
        sb.append("    <properties xmlns=\"http://www.netbeans.org/ns/maven-properties-data/1\">"); //NOI18N
        
        if (nbConfiguration != null) {
            if (nbConfiguration.compileOnSave != null) {
                sb.append("<netbeans.compile.on.save>"); //NOI18N
                sb.append(nbConfiguration.compileOnSave);
                sb.append("</netbeans.compile.on.save>"); //NOI18N
            }
        }
        
        sb.append("    </properties>"); //NOI18N
        sb.append("</project-shared-configuration>"); //NOI18N
        
        return sb.toString();
    }
    
    public static boolean isWebDDpresent(FileObject projectDir) {
        FileObject src = projectDir.getFileObject("src"); //NOI18N
        FileObject main = src.getFileObject("main"); //NOI18N
        FileObject webapp = main.getFileObject("webapp"); //NOI18N
        FileObject webInf = webapp.getFileObject(WEB_INF);
        
        if (webInf == null) {
            return false;
        }
        
        return webInf.getFileObject(WEB_XML) != null ? true : false;
    }
    
    public static boolean isWebDDpresent(Project project) {
        return isWebDDpresent(project.getProjectDirectory());
    }
}
