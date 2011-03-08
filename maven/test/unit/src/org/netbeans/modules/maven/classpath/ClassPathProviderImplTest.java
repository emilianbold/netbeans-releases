/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.classpath;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.spi.RepositoryIndexerImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;
import org.openide.util.test.MockPropertyChangeListener;

public class ClassPathProviderImplTest extends NbTestCase {

    public ClassPathProviderImplTest(String n) {
        super(n);
    }

    private FileObject d;
    protected @Override void setUp() throws Exception {
        clearWorkDir();
        d = FileUtil.toFileObject(getWorkDir());
    }

    public void testClassPath() throws Exception {
        TestFileUtils.writeFile(d,
                "pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>" +
                "<modelVersion>4.0.0</modelVersion>" +
                "<groupId>grp</groupId>" +
                "<artifactId>art</artifactId>" +
                "<packaging>jar</packaging>" +
                "<version>1.0-SNAPSHOT</version>" +
                "<name>Test</name>" +
                "</project>");
        FileObject src = FileUtil.createFolder(d, "src/main/java");
        assertRoots(ClassPath.getClassPath(src, ClassPath.COMPILE));
    }

    public void testSourcePathWithResources() throws Exception {
        TestFileUtils.writeFile(d,
                "pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>" +
                "<modelVersion>4.0.0</modelVersion>" +
                "<groupId>grp</groupId>" +
                "<artifactId>art</artifactId>" +
                "<packaging>jar</packaging>" +
                "<version>1.0-SNAPSHOT</version>" +
                "<name>Test</name>" +
                "</project>");
        FileObject src = FileUtil.createFolder(d, "src/main/java");
        FileObject rsrc = FileUtil.createFolder(d, "src/main/resources");
        FileObject tsrc = FileUtil.createFolder(d, "src/test/java");
        FileObject trsrc = FileUtil.createFolder(d, "src/test/resources");
        assertRoots(ClassPath.getClassPath(src, ClassPath.SOURCE), src, rsrc);
        assertRoots(ClassPath.getClassPath(rsrc, ClassPath.SOURCE), src, rsrc);
        assertRoots(ClassPath.getClassPath(tsrc, ClassPath.SOURCE), tsrc, trsrc);
        assertRoots(ClassPath.getClassPath(trsrc, ClassPath.SOURCE), tsrc, trsrc);
    }

    public void testITSourcePath() throws Exception {
        TestFileUtils.writeFile(d,
                "pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>" +
                "<modelVersion>4.0.0</modelVersion>" +
                "<groupId>grp</groupId>" +
                "<artifactId>art</artifactId>" +
                "<packaging>jar</packaging>" +
                "<version>1.0-SNAPSHOT</version>" +
                "<name>Test</name>" +
                "<build>" +
                "<testSourceDirectory>src/it/java</testSourceDirectory>" +
                "</build>" +
                "</project>");
        FileObject itsrc = FileUtil.createFolder(d, "src/it/java");
        assertRoots(ClassPath.getClassPath(itsrc, ClassPath.SOURCE), itsrc);
    }

    public void testEndorsedClassPath() throws Exception {
        MockLookup.setInstances(new RepositoryIndexerImplementation() { // need to suppress RepositoryQueries.findBySHA1 for test
            public @Override String getType() {
                return RepositoryPreferences.TYPE_NEXUS;
            }
            public @Override Lookup getCapabilityLookup() {
                return Lookup.EMPTY;
            }
            public @Override void indexRepo(RepositoryInfo repo) {}
            public @Override void updateIndexWithArtifacts(RepositoryInfo repo, Collection<Artifact> artifacts) {}
            public @Override void deleteArtifactFromIndex(RepositoryInfo repo, Artifact artifact) {}
        });
        TestFileUtils.writeFile(d,
                "pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>" +
                "<modelVersion>4.0.0</modelVersion>" +
                "<groupId>grp</groupId>" +
                "<artifactId>art</artifactId>" +
                "<packaging>jar</packaging>" +
                "<version>1.0-SNAPSHOT</version>" +
                "<name>Test</name>" +
                "</project>");
        FileObject src = FileUtil.createFolder(d, "src/main/java");
        ClassPath cp = ClassPath.getClassPath(src, ClassPathSupport.ENDORSED);
        assertNotNull(cp);
        MockPropertyChangeListener pcl = new MockPropertyChangeListener();
        cp.addPropertyChangeListener(pcl);
        assertRoots(cp);
        FileObject jar = TestFileUtils.writeZipFile(d, "target/endorsed/override.jar", "javax/Whatever.class:whatever");
        pcl.assertEvents(ClassPath.PROP_ENTRIES, ClassPath.PROP_ROOTS);
        assertRoots(cp, jar);
        d.getFileObject("target").delete();
        pcl.assertEvents(ClassPath.PROP_ENTRIES, ClassPath.PROP_ROOTS);
        assertRoots(cp);
    }

    private static void assertRoots(ClassPath cp, FileObject... files) {
        assertNotNull(cp);
        Set<FileObject> roots = new LinkedHashSet<FileObject>();
        for (FileObject file : files) {
            roots.add(FileUtil.isArchiveFile(file) ? FileUtil.getArchiveRoot(file) : file);
        }
        assertEquals(roots, new LinkedHashSet<FileObject>(Arrays.asList(cp.getRoots())));
    }

}
