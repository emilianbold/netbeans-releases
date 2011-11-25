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
package org.netbeans.modules.maven.j2ee.web;

import java.io.IOException;
import java.util.logging.Level;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.j2ee.JavaEEMavenTestSupport;
import org.netbeans.modules.maven.j2ee.PomBuilder;
import org.netbeans.modules.maven.j2ee.PomBuilder.PomPlugin;
import org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Janicek
 */
public class WebModuleImplTest extends NbTestCase {
    
    private PomBuilder builder;
    
    private Project project;
    private WebModuleProviderImpl provider;
    private WebModuleImpl webModule;
    
    
    public WebModuleImplTest(String name) {
        super(name);
        builder = new PomBuilder();
    }
    
    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    @Override
    protected String logRoot() {
        return "org.netbeans.modules.maven.j2ee.web"; //NOI18N
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        builder.clear();
    }
    

    public void testCreateWebInf() throws IOException {
        setUpDefaultPom();
        assertNull(webModule.getWebInf());
        assertNotNull(webModule.createWebInf());
        assertNotNull(webModule.getWebInf());
    }
    
    public void testGetDocumentBase() throws IOException {
        setUpDefaultPom();
        assertEquals(true, webModule.getDocumentBase().getName().endsWith("webapp")); //NOI18N
    }
    
    public void testGetArchive_noExistingArchive() throws IOException {
        builder.appendDefaultTestValues();
        builder.appendPlugin(new PomPlugin("org.apache.maven.plugins", "maven-war-plugin", "2.1.1")); //NOI18N
        createProject(builder);
        
        assertNull(webModule.getArchive());
    }
    
    public void testGetArchive_archiveExists() throws IOException {
        String artifactID = "projectArtifactID"; //NOI18N
        String archiveType = "war"; //NOI18N
        String version = "12345"; //NOI18N
        
        builder.appendPomContent("4.0.0", "group", artifactID, archiveType, version); //NOI18N
        //builder.appendPlugin(new PomPlugin("org.apache.maven.plugins", "maven-war-plugin", "2.1.1")); //NOI18N
        createProject(builder);
        
        FileObject targetDir = project.getProjectDirectory().createFolder("target"); //NOI18N
        FileObject warFile = FileUtil.createData(targetDir, artifactID + "-" + version + "." + archiveType); //NOI18N
        FileObject archiveFile = webModule.getArchive();
        
        assertNotNull(archiveFile);
        assertEquals(warFile, archiveFile);
        assertEquals(archiveType, archiveFile.getExt());
        assertEquals(true, archiveFile.getName().startsWith(artifactID));
        assertEquals(true, archiveFile.getName().contains(version.subSequence(0, version.length())));
    }
    
    /*
    // We need to find a way how to set server properly first
    public void testSetContextPath() throws IOException {
        setUpDefaultPom();
        MavenProjectSupport.setServerID(project, "gfv3ee6");
        FileObject webXml = JavaEEMavenTestSupport.createWebXml(project.getProjectDirectory());
        String contextPath = "whatever";
        
        assertEquals(-1, webXml.asText().indexOf(contextPath));
        webModule.setContextPath(contextPath);
        assertEquals(true, webXml.asText().indexOf(contextPath) > 0);
    }
     */
    
    private void setUpDefaultPom() throws IOException {
        builder.appendDefaultTestValues();
        createProject(builder);
    }
    
    private void createProject(PomBuilder builder) throws IOException {
        project = JavaEEMavenTestSupport.createMavenWebProject(getWorkDir(), builder.buildPom());
        assertNotNull(project);
        provider = project.getLookup().lookup(WebModuleProviderImpl.class);
        assertNotNull(provider);
        webModule = provider.getModuleImpl();
        assertNotNull(webModule);
    }
}
