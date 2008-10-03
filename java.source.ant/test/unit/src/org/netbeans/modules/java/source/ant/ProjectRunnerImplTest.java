/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.ant;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.swing.Icon;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;
import static org.junit.Assert.*;

/**
 *
 * @author Jan Lahoda
 */
public class ProjectRunnerImplTest {

    public ProjectRunnerImplTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testComputeProperties1() throws MalformedURLException {
        ClassPath cp = ClassPathSupport.createClassPath(new URL("file:///E/"));
        checkProperties(Arrays.asList("classname", "A", "platform.java", "J", "execute.classpath", cp, "work.dir", "W"),
                        Arrays.asList("classname", "A", "platform.java", "J", "classpath", "/E", "work.dir", "W", "application.args", "", "run.jvmargs", ""));
    }

    @Test
    public void testComputeProperties2() throws MalformedURLException, IOException {
        File wd = getWD();
        FileObject fo = FileUtil.toFileObject(wd);

        assertNotNull(fo);

        FileObject java = FileUtil.createData(fo, "prj/A.java");
        FileObject prj = java.getParent();

        String prjPath = FileUtil.toFile(prj).getAbsolutePath();

        MockLookup.setInstances(new ProjectFactoryImpl(prj));
        
        checkProperties(Arrays.asList("execute.file", java, "platform.java", "J"),
                        Arrays.asList("classname", "A", "platform.java", "J", "classpath", prjPath, "work.dir", prjPath, "application.args", "", "run.jvmargs", ""),
                        "prj");
    }
    
    @Test
    public void testComputeProperties3() throws MalformedURLException, IOException {
        File wd = getWD();
        FileObject fo = FileUtil.toFileObject(wd);

        assertNotNull(fo);

        FileObject java = FileUtil.createData(fo, "prj/A.java");
        FileObject prj  = java.getParent();
        FileObject dir  = FileUtil.createFolder(fo, "prj/test");

        String prjPath = FileUtil.toFile(prj).getAbsolutePath();

        MockLookup.setInstances(new ProjectFactoryImpl(prj));

        Project fake = new Project() {
            public FileObject getProjectDirectory() {
                return null;
            }
            public Lookup getLookup() {
                return Lookups.singleton(new ProjectInformation() {
                    public String getName() {
                        return null;
                    }
                    public String getDisplayName() {
                        return "fake";
                    }
                    public Icon getIcon() {
                        return null;
                    }
                    public Project getProject() {
                        return null;
                    }
                    public void addPropertyChangeListener(PropertyChangeListener listener) {}
                    public void removePropertyChangeListener(PropertyChangeListener listener) {}
                });
            }
        };

        Collection<String> args = Arrays.asList("test1", "test2");
        Collection<String> jvmArgs = Arrays.asList("test3", "test4");

        checkProperties(Arrays.asList("execute.file", java, "platform.java", "J", "work.dir", dir, "project", fake, "application.args", args, "run.jvmargs", jvmArgs),
                        Arrays.asList("classname", "A", "platform.java", "J", "classpath", prjPath, "work.dir", FileUtil.toFile(dir).getAbsolutePath(), "application.args", "test1 test2", "run.jvmargs", "test3 test4"),
                        "fake");
    }
    
    private void checkProperties(Collection<?> source, Collection<String> target) {
        checkProperties(source, target, null);
    }
    
    private void checkProperties(Collection<?> source, Collection<String> target, String displayName) {
        Map<String, Object> sourceMap = new HashMap<String, Object>();

        for (Iterator<?> it = source.iterator(); it.hasNext();) {
            String key = (String) it.next();
            Object value = it.next();

            sourceMap.put(key, value);

        }

        Properties golden = new Properties();

        for (Iterator<String> it = target.iterator(); it.hasNext();) {
            String key = it.next();
            String value = it.next();

            golden.setProperty(key, value);
        }

        String[] projectName = new String[1];
        Properties out = ProjectRunnerImpl.computeProperties(sourceMap, projectName);

        assertEquals(golden, out);

        if (displayName != null) {
            assertEquals(displayName, projectName[0]);
        }
    }

    private File getWD() throws IOException {
        String name = "unknown";
        for (StackTraceElement e : new Exception().getStackTrace()) {
            if (e.getMethodName().startsWith("test")) {
                name = e.getMethodName();
                break;
            }
        }
        
        NbTestCase ntc = new NbTestCase(name) {};

        ntc.clearWorkDir();

        return ntc.getWorkDir();
    }

    private static final class ProjectFactoryImpl implements ProjectFactory {

        private final FileObject file;

        public ProjectFactoryImpl(FileObject file) {
            this.file = file;
        }

        public boolean isProject(FileObject projectDirectory) {
            return projectDirectory.equals(file);
        }

        public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
            return new ProjectImpl(file);
        }

        public void saveProject(Project project) throws IOException, ClassCastException {
        }

    }
    private static final class ProjectImpl implements Project {

        private final FileObject dir;

        public ProjectImpl(FileObject dir) {
            this.dir = dir;
        }

        public FileObject getProjectDirectory() {
            return dir;
        }

        public Lookup getLookup() {
            return  Lookups.fixed(new ClassPathProvider() {
                private final ClassPath cp = ClassPathSupport.createClassPath(dir);

                public ClassPath findClassPath(FileObject file, String type) {
                    if (ClassPath.EXECUTE.equals(type) || ClassPath.SOURCE.equals(type)) {
                        return cp;
                    }
                    return null;
                }
            });
        }
        
    }
    
}
