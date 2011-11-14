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
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.maven.j2ee.model.NbConfiguration;
import org.netbeans.modules.maven.j2ee.model.PomContent;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;

/**
 *
 * @author Martin Janicek
 */
public class MavenTestSupport {
 
    public static final String WEB_INF = "WEB-INF"; //NOI18N
    public static final String WEB_XML = "web.xml"; //NOI18N
    
    private static final StringBuilder sb = new StringBuilder();
    
    
    /**
     * <p>Creates default Maven project structure which could be used for tests
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
     * For creation of additional files like pom.xml see MavenTestSupport methods
     * 
     * @param projectDir root directory of the project
     * @return created project with default structure
     */
    public static Project createMavenProject(File projectDir) {
        return createMavenProject(FileUtil.toFileObject(projectDir));
    }
    
    public static Project createMavenProject(FileObject projectDir) {
        try {
            FileObject src = FileUtil.createFolder(projectDir, "src"); //NOI18N
            FileObject main = FileUtil.createFolder(src, "main"); //NOI18N
            FileObject java = FileUtil.createFolder(main, "java"); //NOI18N
            FileObject resources = FileUtil.createFolder(main, "java"); //NOI18N
            FileObject webapp = FileUtil.createFolder(main, "webapp"); //NOI18N

            MavenTestSupport.createPom(projectDir);
            
            return ProjectManager.getDefault().findProject(projectDir);
        } catch (IOException ex) {
            return null;
        }
    }
    
    public static FileObject createPom(Project project) throws IOException {
        return TestFileUtils.writeFile(project.getProjectDirectory(), "pom.xml", createDefaultPomContent()); //NOI18N
    }
 
    public static FileObject createPom(Project project, PomContent pomContent) throws IOException {
        return TestFileUtils.writeFile(project.getProjectDirectory(), "pom.xml", createPomContent(pomContent)); //NOI18N
    }
    
    public static FileObject createPom(FileObject workDir) throws IOException {
        return TestFileUtils.writeFile(workDir, "pom.xml", createDefaultPomContent()); //NOI18N
    }
    
    public static FileObject createPom(FileObject workDir, PomContent pomContent) throws IOException {
        return TestFileUtils.writeFile(workDir, "pom.xml", createPomContent(pomContent)); //NOI18N
    }
    
    private static String createPomContent(PomContent pomContent) {
        return "<project>" +
                   "<modelVersion>" + pomContent.modelVersion + "</modelVersion>" +
                   "<groupId>" + pomContent.groupId + "</groupId>" +
                   "<artifactId>" + pomContent.artifactId + "</artifactId>" +
                   "<packaging>" + pomContent.packaging + "</packaging>" +
                   "<version>" + pomContent.version + "</version>" +
               "</project>"; //NOI18N
    }
    
    private static String createDefaultPomContent() {
        return "<project>" +
                   "<modelVersion>4.0.0</modelVersion>" +
                   "<groupId>testGroupId</groupId>" +
                   "<artifactId>testArtifactId</artifactId>" +
                   "<packaging>war</packaging>" +
                   "<version>1.0</version>" +
               "</project>"; //NOI18N
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
    
    public static void setJ2eeVersion(Project project, String value) {
        setSettings(project, MavenJavaEEConstants.HINT_J2EE_VERSION, value, false);
    }
    
    private static void setSettings(Project project, String key, String value, boolean shared) {
        AuxiliaryProperties props = project.getLookup().lookup(AuxiliaryProperties.class);
        props.put(key, value, shared);
    }
    
    public static boolean isDDpresent(FileObject projectDir) {
        FileObject src = projectDir.getFileObject("src");
        FileObject main = src.getFileObject("main");
        FileObject webapp = main.getFileObject("webapp");
        FileObject webInf = webapp.getFileObject(WEB_INF);
        
        if (webInf == null) {
            return false;
        }
        
        return webInf.getFileObject(WEB_XML) != null ? true : false;
    }
    
    public static boolean isDDpresent(Project project) {
        return isDDpresent(project.getProjectDirectory());
    }
}
