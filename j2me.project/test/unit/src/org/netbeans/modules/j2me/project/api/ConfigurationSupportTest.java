/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2me.project.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2me.project.J2MEProject;
import org.netbeans.modules.j2me.project.TestUtils;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Tomas Zezula
 */
public class ConfigurationSupportTest extends NbTestCase {

    private File projectFolder;

    public ConfigurationSupportTest(@NonNull final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        final File workDir = getWorkDir();
        MockLookup.setLayersAndInstances(new TestUtils.MockJavaPlatformProvider(
             new File(workDir, "j2me")));   //NOI18N

        projectFolder = new File (workDir, "testProject");  //NOI18N
        projectFolder.mkdirs();
        assertTrue(projectFolder.isDirectory());
    }    


    public void testNoConfigurations() throws IOException {
        final JavaPlatform mePlatform = TestUtils.findMEPlatform();
        assertNotNull("No ME Platform", mePlatform);    //NOI18N
        final JavaPlatform sePlatform = TestUtils.findSEPlatfrom();
        assertNotNull("No SE Platform", sePlatform);    //NOI18N
        final AntProjectHelper aph = J2MEProjectBuilder.forDirectory(
                projectFolder,
                "Test Project",     //NOI18N
                mePlatform).
            addDefaultSourceRoots().
            setSDKPlatform(sePlatform).
            build();
        assertNotNull(aph);
        final Project prj = FileOwnerQuery.getOwner(aph.getProjectDirectory());
        assertNotNull(prj);        
        assertEquals(1, ConfigurationSupport.getConfigurations(prj).size());
        assertNotNull(ConfigurationSupport.getActiveConfiguration(prj));
        //Default cfg
        EditableProperties ep = ConfigurationSupport.getProperties(prj, null, true);
        assertNotNull(ep);
        assertContentEquals(
            prj.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH),
            ep);
        ep = ConfigurationSupport.getProperties(prj, null, false);
        assertNotNull(ep);
        assertContentEquals(
            prj.getProjectDirectory().getFileObject(AntProjectHelper.PRIVATE_PROPERTIES_PATH),
            ep);
        //Default cfg - explicit
        ep = ConfigurationSupport.getProperties(prj, ConfigurationSupport.getActiveConfiguration(prj), true);
        assertNotNull(ep);
        assertContentEquals(
            prj.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH),
            ep);
        ep = ConfigurationSupport.getProperties(prj, ConfigurationSupport.getActiveConfiguration(prj), false);
        assertNotNull(ep);
        assertContentEquals(
            prj.getProjectDirectory().getFileObject(AntProjectHelper.PRIVATE_PROPERTIES_PATH),
            ep);
    }

    public void testNamedConfigurations() throws IOException, MutexException {
        final JavaPlatform mePlatform = TestUtils.findMEPlatform();
        assertNotNull("No ME Platform", mePlatform);    //NOI18N
        final JavaPlatform sePlatform = TestUtils.findSEPlatfrom();
        assertNotNull("No SE Platform", sePlatform);    //NOI18N
        final AntProjectHelper aph = ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<AntProjectHelper>(){
            @Override
            public AntProjectHelper run() throws Exception {
                final AntProjectHelper aph = J2MEProjectBuilder.forDirectory(
                    projectFolder,
                    "Test Project",     //NOI18N
                    mePlatform).
                addDefaultSourceRoots().
                setSDKPlatform(sePlatform).
                build();
                FileUtil.runAtomicAction(new Runnable(){
                    @Override
                    public void run() {
                        try {
                            FileObject publicCfgFile = FileUtil.createData(
                                FileUtil.toFileObject(projectFolder),
                                "nbproject/configs/mycfg.properties");  //NOI18N
                            FileObject privateCfgFile = FileUtil.createData(
                                FileUtil.toFileObject(projectFolder),
                                "nbproject/private/configs/mycfg.properties"); //NOI18N
                            FileLock lock = publicCfgFile.lock();
                            try(final OutputStream out = publicCfgFile.getOutputStream(lock)) {
                                final EditableProperties ep = new EditableProperties(true);
                                ep.setProperty("$label", "My Configuration");   //NOI18N
                                ep.setProperty("cfg_property","public");        //NOI18N
                                ep.store(out);
                            } finally {
                                lock.releaseLock();
                            }
                            lock = privateCfgFile.lock();
                            try (final OutputStream out = privateCfgFile.getOutputStream(lock)) {
                                final EditableProperties ep = new EditableProperties(true);
                                ep.setProperty("cfg_property","private");   //NOI18N
                                ep.store(out);
                            } finally {
                                lock.releaseLock();
                            }
                        } catch (IOException e) {
                            TestUtils.<Void,RuntimeException>sneaky(e);
                        }
                    }
                });
                return aph;
            }
        });
        assertNotNull(aph);
        final Project prj = FileOwnerQuery.getOwner(aph.getProjectDirectory());
        assertNotNull(prj);
        assertEquals(2, ConfigurationSupport.getConfigurations(prj).size());
        assertNotNull(ConfigurationSupport.getActiveConfiguration(prj));

        //Default cfg
        EditableProperties ep = ConfigurationSupport.getProperties(prj, null, true);
        assertNotNull(ep);
        assertContentEquals(
            prj.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH),
            ep);
        ep = ConfigurationSupport.getProperties(prj, null, false);
        assertNotNull(ep);
        assertContentEquals(
            prj.getProjectDirectory().getFileObject(AntProjectHelper.PRIVATE_PROPERTIES_PATH),
            ep);

        //Default cfg - explicit
        ep = ConfigurationSupport.getProperties(prj, ConfigurationSupport.getActiveConfiguration(prj), true);
        assertNotNull(ep);
        assertContentEquals(
            prj.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH),
            ep);
        ep = ConfigurationSupport.getProperties(prj, ConfigurationSupport.getActiveConfiguration(prj), false);
        assertNotNull(ep);
        assertContentEquals(
            prj.getProjectDirectory().getFileObject(AntProjectHelper.PRIVATE_PROPERTIES_PATH),
            ep);

        ProjectConfiguration nonDefault = null;
        for (ProjectConfiguration cfg : ConfigurationSupport.getConfigurations(prj)) {
            if (!cfg.equals(ConfigurationSupport.getActiveConfiguration(prj))) {
                nonDefault = cfg;
                break;
            }
        }
        assertNotNull(nonDefault);
        //Non default
        ep = ConfigurationSupport.getProperties(prj, nonDefault, true);
        assertNotNull(ep);
        assertContentEquals(
            prj.getProjectDirectory().getFileObject("nbproject/configs/mycfg.properties"),  //NOI18N
            ep);
        ep = ConfigurationSupport.getProperties(prj, nonDefault, false);
        assertNotNull(ep);
        assertContentEquals(
            prj.getProjectDirectory().getFileObject("nbproject/private/configs/mycfg.properties"),  //NOI18N
            ep);
    }

    public void testUnNamedConfigurations() throws IOException, MutexException {
        final JavaPlatform mePlatform = TestUtils.findMEPlatform();
        assertNotNull("No ME Platform", mePlatform);    //NOI18N
        final JavaPlatform sePlatform = TestUtils.findSEPlatfrom();
        assertNotNull("No SE Platform", sePlatform);    //NOI18N
        final AntProjectHelper aph = ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<AntProjectHelper>(){
            @Override
            public AntProjectHelper run() throws Exception {
                final AntProjectHelper aph = J2MEProjectBuilder.forDirectory(
                    projectFolder,
                    "Test Project",     //NOI18N
                    mePlatform).
                addDefaultSourceRoots().
                setSDKPlatform(sePlatform).
                build();
                FileUtil.runAtomicAction(new Runnable(){
                    @Override
                    public void run() {
                        try {
                            FileObject publicCfgFile = FileUtil.createData(
                                FileUtil.toFileObject(projectFolder),
                                "nbproject/configs/mycfg.properties");  //NOI18N
                            FileObject privateCfgFile = FileUtil.createData(
                                FileUtil.toFileObject(projectFolder),
                                "nbproject/private/configs/mycfg.properties"); //NOI18N
                            FileLock lock = publicCfgFile.lock();
                            try(final OutputStream out = publicCfgFile.getOutputStream(lock)) {
                                final EditableProperties ep = new EditableProperties(true);
                                ep.setProperty("cfg_property","public");        //NOI18N
                                ep.store(out);
                            } finally {
                                lock.releaseLock();
                            }
                            lock = privateCfgFile.lock();
                            try (final OutputStream out = privateCfgFile.getOutputStream(lock)) {
                                final EditableProperties ep = new EditableProperties(true);
                                ep.setProperty("cfg_property","private");   //NOI18N
                                ep.store(out);
                            } finally {
                                lock.releaseLock();
                            }
                        } catch (IOException e) {
                            TestUtils.<Void,RuntimeException>sneaky(e);
                        }
                    }
                });
                return aph;
            }
        });
        assertNotNull(aph);
        final Project prj = FileOwnerQuery.getOwner(aph.getProjectDirectory());
        assertNotNull(prj);
        assertEquals(2, ConfigurationSupport.getConfigurations(prj).size());
        assertNotNull(ConfigurationSupport.getActiveConfiguration(prj));

        ProjectConfiguration nonDefault = null;
        for (ProjectConfiguration cfg : ConfigurationSupport.getConfigurations(prj)) {
            if (!cfg.equals(ConfigurationSupport.getActiveConfiguration(prj))) {
                nonDefault = cfg;
                break;
            }
        }
        assertNotNull(nonDefault);
        //Non default
        EditableProperties ep = ConfigurationSupport.getProperties(prj, nonDefault, true);
        assertNotNull(ep);
        assertContentEquals(
            prj.getProjectDirectory().getFileObject("nbproject/configs/mycfg.properties"),  //NOI18N
            ep);
        ep = ConfigurationSupport.getProperties(prj, nonDefault, false);
        assertNotNull(ep);
        assertContentEquals(
            prj.getProjectDirectory().getFileObject("nbproject/private/configs/mycfg.properties"),  //NOI18N
            ep);
    }

    public void testWriteConfigurations() throws IOException, MutexException {
        final JavaPlatform mePlatform = TestUtils.findMEPlatform();
        assertNotNull("No ME Platform", mePlatform);    //NOI18N
        final JavaPlatform sePlatform = TestUtils.findSEPlatfrom();
        assertNotNull("No SE Platform", sePlatform);    //NOI18N
        final AntProjectHelper aph = ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<AntProjectHelper>(){
            @Override
            public AntProjectHelper run() throws Exception {
                final AntProjectHelper aph = J2MEProjectBuilder.forDirectory(
                    projectFolder,
                    "Test Project",     //NOI18N
                    mePlatform).
                addDefaultSourceRoots().
                setSDKPlatform(sePlatform).
                build();
                FileUtil.runAtomicAction(new Runnable(){
                    @Override
                    public void run() {
                        try {
                            FileObject publicCfgFile = FileUtil.createData(
                                FileUtil.toFileObject(projectFolder),
                                "nbproject/configs/mycfg.properties");  //NOI18N
                            FileObject privateCfgFile = FileUtil.createData(
                                FileUtil.toFileObject(projectFolder),
                                "nbproject/private/configs/mycfg.properties"); //NOI18N
                            FileLock lock = publicCfgFile.lock();
                            try(final OutputStream out = publicCfgFile.getOutputStream(lock)) {
                                final EditableProperties ep = new EditableProperties(true);
                                ep.setProperty("$label", "My Configuration");   //NOI18N
                                ep.setProperty("cfg_property","public");        //NOI18N
                                ep.store(out);
                            } finally {
                                lock.releaseLock();
                            }
                            lock = privateCfgFile.lock();
                            try (final OutputStream out = privateCfgFile.getOutputStream(lock)) {
                                final EditableProperties ep = new EditableProperties(true);
                                ep.setProperty("cfg_property","private");   //NOI18N
                                ep.store(out);
                            } finally {
                                lock.releaseLock();
                            }
                        } catch (IOException e) {
                            TestUtils.<Void,RuntimeException>sneaky(e);
                        }
                    }
                });
                return aph;
            }
        });
        assertNotNull(aph);
        final Project prj = FileOwnerQuery.getOwner(aph.getProjectDirectory());
        assertNotNull(prj);
        assertEquals(2, ConfigurationSupport.getConfigurations(prj).size());
        assertNotNull(ConfigurationSupport.getActiveConfiguration(prj));

        //Default
        EditableProperties ep = ConfigurationSupport.getProperties(prj, null, true);
        assertNotNull(ep);
        assertContentEquals(
            prj.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH),
            ep);
        ep.setProperty("default_property", "public");  //NOI18N
        ConfigurationSupport.putProperties(prj, null, true, ep);
        ProjectManager.getDefault().saveProject(prj);
        ep = prj.getLookup().lookup(J2MEProject.class).getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals("public", ep.getProperty("default_property"));    //NOI18N

        ep = ConfigurationSupport.getProperties(prj, null, false);
        assertNotNull(ep);
        assertContentEquals(
            prj.getProjectDirectory().getFileObject(AntProjectHelper.PRIVATE_PROPERTIES_PATH),
            ep);
        ep.setProperty("default_property", "private");  //NOI18N
        ConfigurationSupport.putProperties(prj, null, false, ep);
        ProjectManager.getDefault().saveProject(prj);
        ep = prj.getLookup().lookup(J2MEProject.class).getUpdateHelper().getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        assertEquals("private", ep.getProperty("default_property"));    //NOI18N

        //Non default
        ProjectConfiguration nonDefault = null;
        for (ProjectConfiguration cfg : ConfigurationSupport.getConfigurations(prj)) {
            if (!cfg.equals(ConfigurationSupport.getActiveConfiguration(prj))) {
                nonDefault = cfg;
                break;
            }
        }
        assertNotNull(nonDefault);
        ep = ConfigurationSupport.getProperties(prj, nonDefault, true);
        assertNotNull(ep);
        assertContentEquals(
            prj.getProjectDirectory().getFileObject("nbproject/configs/mycfg.properties"),  //NOI18N
            ep);
        ep.setProperty("cfg_property", "public2");  //NOI18N
        ConfigurationSupport.putProperties(prj, nonDefault, true, ep);
        ProjectManager.getDefault().saveProject(prj);
        ep = prj.getLookup().lookup(J2MEProject.class).getUpdateHelper().getProperties("nbproject/configs/mycfg.properties");   //NOI18N
        assertEquals("public2", ep.getProperty("cfg_property"));    //NOI18N

        ep = ConfigurationSupport.getProperties(prj, nonDefault, false);
        assertNotNull(ep);
        assertContentEquals(
            prj.getProjectDirectory().getFileObject("nbproject/private/configs/mycfg.properties"),  //NOI18N
            ep);
        ep.setProperty("cfg_property", "private2");  //NOI18N
        ConfigurationSupport.putProperties(prj, nonDefault, false, ep);
        ProjectManager.getDefault().saveProject(prj);
        ep = prj.getLookup().lookup(J2MEProject.class).getUpdateHelper().getProperties("nbproject/private/configs/mycfg.properties");   //NOI18N
        assertEquals("private2", ep.getProperty("cfg_property"));    //NOI18N
    }

    private static void assertContentEquals(@NullAllowed final FileObject def, @NullAllowed final EditableProperties ep) throws IOException {
        assertNotNull("def", def);  //NOI18N
        assertNotNull("ep", ep);    //NOI18N
        final EditableProperties defEp = new EditableProperties(true);
        try (InputStream in = def.getInputStream()) {
            defEp.load(in);
        }
        assertEquals(defEp.size(), ep.size());
        assertEquals(toString(defEp), toString(ep));
    }

    @NonNull
    private static String toString(@NonNull final EditableProperties ep) {
        final StringBuilder sb = new StringBuilder();
        for (Map.Entry<String,String> e: ep.entrySet()) {
            sb.append(e.getKey());
            sb.append("=");
            sb.append(e.getValue());
            sb.append('\n');
        }
        return sb.toString();
    }

}
