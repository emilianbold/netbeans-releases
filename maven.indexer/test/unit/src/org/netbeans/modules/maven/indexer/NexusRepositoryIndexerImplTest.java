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

package org.netbeans.modules.maven.indexer;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.apache.maven.artifact.installer.ArtifactInstaller;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.index.ArtifactInfo;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.QueryField;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.spi.ClassUsageQuery.ClassUsageResult;
import org.openide.modules.api.PlacesTestUtils;
import org.openide.util.test.JarBuilder;
import org.openide.util.test.TestFileUtils;

public class NexusRepositoryIndexerImplTest extends NbTestCase {

    public NexusRepositoryIndexerImplTest(String n) {
        super(n);
    }

    private ArtifactRepository defaultArtifactRepository;
    private MavenEmbedder embedder;
    private ArtifactInstaller artifactInstaller;
    private RepositoryInfo info;
    private NexusRepositoryIndexerImpl nrii;
    
    @Override protected void setUp() throws Exception {
        clearWorkDir();
        PlacesTestUtils.setUserDirectory(getWorkDir());
        File repo = new File(getWorkDir(), "repo");
        embedder = EmbedderFactory.getProjectEmbedder();
        defaultArtifactRepository = embedder.lookupComponent(ArtifactRepositoryFactory.class).createArtifactRepository("test", repo.toURI().toString(), "default", null, null);
        embedder.setUpLegacySupport(); // XXX could use org.sonatype.aether.RepositorySystem to avoid maven-compat
        artifactInstaller = embedder.lookupComponent(ArtifactInstaller.class);
        info = new RepositoryInfo("test", RepositoryPreferences.TYPE_NEXUS, "Test", repo.getAbsolutePath(), null);
        RepositoryPreferences.getInstance().addOrModifyRepositoryInfo(info);
        nrii = new NexusRepositoryIndexerImpl();
    }

    @Override protected Level logLevel() {
        return Level.FINER;
    }

    @Override protected String logRoot() {
        return "org.netbeans.modules.maven.indexer";
    }

    private void install(File f, String groupId, String artifactId, String version, String packaging) throws Exception {
        artifactInstaller.install(f, embedder.createArtifact(groupId, artifactId, version, packaging), defaultArtifactRepository);
    }

    private void installPOM(String groupId, String artifactId, String version, String packaging) throws Exception {
        install(TestFileUtils.writeFile(new File(getWorkDir(), artifactId + ".pom"),
                "<project><modelVersion>4.0.0</modelVersion>" +
                "<groupId>" + groupId + "</groupId><artifactId>" + artifactId + "</artifactId>" +
                "<version>" + version + "</version><packaging>" + packaging + "</packaging></project>"), groupId, artifactId, version, "pom");
    }

    public void testFilterGroupIds() throws Exception {
        install(File.createTempFile("whatever", ".txt", getWorkDir()), "test", "spin", "1.1", "txt");
        assertEquals(Collections.singleton("test"), nrii.filterGroupIds("", Collections.singletonList(info)));
    }

    public void testFind() throws Exception {
        installPOM("test", "plugin", "0", "maven-plugin");
        install(TestFileUtils.writeZipFile(new File(getWorkDir(), "plugin.jar"), "META-INF/maven/plugin.xml:<plugin><goalPrefix>stuff</goalPrefix></plugin>"), "test", "plugin", "0", "maven-plugin");
        QueryField qf = new QueryField();
        qf.setField(ArtifactInfo.PLUGIN_PREFIX);
        qf.setValue("stuff");
        qf.setOccur(QueryField.OCCUR_MUST);
        qf.setMatch(QueryField.MATCH_EXACT);
        assertEquals("[test:plugin:0:test]", nrii.find(Collections.singletonList(qf), Collections.singletonList(info)).toString());
    }

    public void testLastUpdated() throws Exception { // #197670
        installPOM("test", "art", "0", "jar");
        install(TestFileUtils.writeZipFile(new File(getWorkDir(), "art.jar"), "stuff:whatever"), "test", "art", "0", "jar");
        File empty = TestFileUtils.writeFile(new File(getWorkDir(), "empty"), "# placeholder\n");
        install(empty, "test", "art", "0", "pom.lastUpdated");
        install(empty, "test", "art", "0", "jar.lastUpdated");
        List<NBVersionInfo> versions = nrii.getVersions("test", "art", Collections.singletonList(info));
        assertEquals(1, versions.size());
        NBVersionInfo v = versions.get(0);
        assertEquals("test:art:0:test", v.toString());
        assertEquals("jar", v.getPackaging());
        assertEquals("jar", v.getType());
    }

    public void testFindClassUsages() throws Exception {
        installPOM("test", "mod1", "0", "jar");
        File mod1 = new JarBuilder(getWorkDir()).
                source("mod1.API", "public class API {}").
                source("mod1.Util", "public class Util {}").
                source("mod1.Stuff", "public class Stuff implements Outer {}").
                source("mod1.Outer", "public interface Outer {interface Inner {} interface Unused {}}").
                build();
        install(mod1, "test", "mod1", "0", "jar");
        installPOM("test", "mod2", "0", "jar");
        install(new JarBuilder(getWorkDir()).
                source("mod2.Client", "class Client extends mod1.API {}").
                source("mod2.OtherClient", "class OtherClient extends mod1.API {}").
                source("mod2.Outer", "class Outer implements mod1.Outer, mod1.Outer.Inner {static class Inner implements mod1.Outer.Inner {}}").
                classpath(mod1).build(), "test", "mod2", "0", "jar");
        installPOM("test", "mod3", "0", "jar");
        install(new JarBuilder(getWorkDir()).
                source("mod3.Client", "class Client extends mod1.API {}").
                classpath(mod1).build(), "test", "mod3", "0", "jar");
        // This is what nbm:populate-repository currently produces:
        install(TestFileUtils.writeFile(new File(getWorkDir(), "mod4.pom"),
                "<project><modelVersion>4.0.0</modelVersion>" +
                "<groupId>test</groupId><artifactId>mod4</artifactId>" +
                "<version>0</version></project>"), "test", "mod4", "0", "pom");
        install(new JarBuilder(getWorkDir()).
                source("mod4.Install", "class Install extends mod1.Util {}").
                classpath(mod1).build(), "test", "mod4", "0", "jar");
        install(TestFileUtils.writeZipFile(new File(getWorkDir(), "mod4.nbm"), "Info/info.xml:<whatever/>"), "test", "mod4", "0", "nbm");
        // And as produced by a Maven source build of a module:
        installPOM("test", "mod5", "0", "nbm");
        install(new JarBuilder(getWorkDir()).
                source("mod5.Install", "class Install extends mod1.Stuff {}").
                classpath(mod1).build(), "test", "mod5", "0", "jar");
        install(TestFileUtils.writeZipFile(new File(getWorkDir(), "mod5.nbm"), "Info/info.xml:<whatever/>"), "test", "mod5", "0", "nbm");
        // repo set up, now index and query:
        assertEquals("[test:mod2:0:test[mod2.Client, mod2.OtherClient], test:mod3:0:test[mod3.Client]]", nrii.findClassUsages("mod1.API", Collections.singletonList(info)).toString());
        List<ClassUsageResult> r = nrii.findClassUsages("mod1.Util", Collections.singletonList(info));
        assertEquals("[test:mod4:0:test[mod4.Install]]", r.toString());
        assertEquals("jar", r.get(0).getArtifact().getType());
        r = nrii.findClassUsages("mod1.Stuff", Collections.singletonList(info));
        assertEquals("[test:mod5:0:test[mod5.Install]]", r.toString());
        assertEquals("jar", r.get(0).getArtifact().getType());
        assertEquals("[]", nrii.findClassUsages("java.lang.Object", Collections.singletonList(info)).toString());
        assertEquals("[test:mod2:0:test[mod2.Outer]]", nrii.findClassUsages("mod1.Outer", Collections.singletonList(info)).toString());
        assertEquals("[test:mod2:0:test[mod2.Outer]]", nrii.findClassUsages("mod1.Outer$Inner", Collections.singletonList(info)).toString());
        assertEquals("[]", nrii.findClassUsages("mod1.Outer$Unused", Collections.singletonList(info)).toString());
        // XXX InnerClass attribute will produce spurious references to outer classes even when just an inner is used
    }

    public void testCrc32base64() throws Exception {
        assertEquals("ThFDsw", NexusRepositoryIndexerImpl.crc32base64("whatever"));
        assertEquals("tqQ_oA", NexusRepositoryIndexerImpl.crc32base64("mod1/Stuff"));
    }

}
