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
package org.netbeans.modules.maven.pom;

import hidden.org.codehaus.plexus.util.StringOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.MailingList;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.modules.maven.model.pom.Parent;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.PluginExecution;
import org.netbeans.modules.maven.model.pom.Project;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.modules.maven.model.profile.Profile;
import org.netbeans.modules.maven.model.profile.ProfilesModel;
import org.netbeans.modules.maven.model.profile.ProfilesModelFactory;
import org.netbeans.modules.maven.model.profile.ProfilesRoot;
import org.netbeans.modules.maven.model.settings.Settings;
import org.netbeans.modules.maven.model.settings.SettingsModel;
import org.netbeans.modules.maven.model.settings.SettingsModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mkleint
 */
public class ModelTest extends TestCase {

    public ModelTest(String name) {
        super(name);
    }

    public void testModelWrite() throws Exception {
        ModelSource source = createModelSource("sample.pom");
        try {
            POMModel model = POMModelFactory.getDefault().getModel(source);
            assertNotNull(model.getRootComponent());
            Project prj = model.getProject();

            Parent parent = prj.getPomParent();
            assertNotNull(parent);
            assertNotNull(parent.getGroupId());
            assertEquals("org.codehaus.mojo", parent.getGroupId());

            model.startTransaction();
            parent.setGroupId("foo.bar");
            model.endTransaction();
            assertEquals("foo.bar", parent.getGroupId());

            //this test fails here.. cannot rollback single property changes..
            model.startTransaction();
            parent.setGroupId("bar.foo");
            model.rollbackTransaction();
            
            assertEquals("foo.bar", parent.getGroupId());

        } finally {
            File file = source.getLookup().lookup(File.class);
            file.deleteOnExit();
        }
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    public void testModel() throws Exception {
        ModelSource source = createModelSource("sample.pom");
        try {

            POMModel model = POMModelFactory.getDefault().getModel(source);
            assertNotNull(model.getRootComponent());
            Project prj = model.getProject();
            assertNotNull(prj);
            Parent parent = prj.getPomParent();
            assertNotNull(parent);
            assertNotNull(parent.getGroupId());
            assertEquals("org.codehaus.mojo", parent.getGroupId());


            List<MailingList> lists = prj.getMailingLists();
            assertEquals(3, lists.size());
            for (MailingList lst : lists) {
                assertNotNull(lst);
            }


            Properties props = prj.getProperties();
            assertNotNull(props);
            String val1 = props.getProperty("prop1");
            assertEquals("foo", val1);
            String val2 = props.getProperty("prop2");
            assertEquals("bar", val2);

            Map<String, String> p = props.getProperties();
            assertEquals(2, p.size());
            assertEquals("foo", p.get("prop1"));
            assertEquals("bar", p.get("prop2"));

            List<Plugin> plugins = prj.getBuild().getPlugins();
            assertNotNull(plugins);
            assertEquals(4, plugins.size());

            Plugin plug = plugins.get(1);
            assertEquals("modello-maven-plugin", plug.getArtifactId());
            Configuration config = plug.getConfiguration();
            assertNotNull(config);
            List<POMExtensibilityElement> lst = config.getConfigurationElements();
            assertEquals(2, lst.size());
            POMExtensibilityElement el = lst.get(1);
            assertEquals("version", el.getQName().getLocalPart());
            assertEquals("1.0.0", el.getElementText());
            List<PluginExecution> execs = plug.getExecutions();
            assertNotNull(execs);
            PluginExecution ex = execs.get(0);
            assertEquals("build", ex.getId());
            String[] goals = new String[]{
                "xpp3-reader",
                "java",
                "xdoc",
                "xsd"
            };
            assertEquals(Arrays.asList(goals), ex.getGoals());

//        model.startTransaction();
//        try {
//            parent.setGroupId("XXX");
//            assertEquals("XXX", parent.getGroupId());
//        } finally {
//            model.endTransaction();
//        }

        } finally {
            File file = source.getLookup().lookup(File.class);
            file.deleteOnExit();
        }

    }

    public void testSettings() throws Exception {
        ModelSource source = createModelSource("settings.xml");
        try {
            assertTrue(source.isEditable());
            SettingsModel model = SettingsModelFactory.getDefault().getModel(source);
            assertNotNull(model.getRootComponent());
            Settings prj = model.getSettings();
            assertNotNull(prj);

            List<org.netbeans.modules.maven.model.settings.Profile> profiles = prj.getProfiles();
            assertNotNull(profiles);
            assertNotNull(prj.findProfileById("mkleint"));

            List<String> actives = prj.getActiveProfiles();
            assertNotNull(actives);
            assertEquals("mkleint", actives.get(0));
        } finally {
            File file = source.getLookup().lookup(File.class);
            file.deleteOnExit();
        }

    }

    public void testProfiles() throws Exception {
        ModelSource source = createModelSource("profiles.xml");
        try {
            assertTrue(source.isEditable());
            ProfilesModel model = ProfilesModelFactory.getDefault().getModel(source);
            assertNotNull(model.getRootComponent());
            ProfilesRoot prj = model.getProfilesRoot();
            assertNotNull(prj);

            List<Profile> profiles = prj.getProfiles();
            assertNotNull(profiles);
            assertNotNull(prj.findProfileById("profile1"));

            List<String> actives = prj.getActiveProfiles();
            assertNotNull(actives);
            assertEquals("profile1", actives.get(0));
        } finally {
            File file = source.getLookup().lookup(File.class);
            file.deleteOnExit();
        }
    }

    public void testMissingProfiles() throws Exception {
        String dir = System.getProperty("java.io.tmpdir");
        File sourceFile = new File(dir, "foo.bar");
        try {
            assertFalse(sourceFile.exists());
            String PROFILES_SKELETON = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<profilesXml xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/profiles-1.0.0.xsd\">\n" +
                    "</profilesXml>";

            ModelSource source = Utilities.createModelSourceForMissingFile(sourceFile, true,
                    PROFILES_SKELETON, "text/xml");
            assertTrue(source.isEditable());
            ProfilesModel model = ProfilesModelFactory.getDefault().getModel(source);
            assertNotNull(model.getRootComponent());
            model.startTransaction();
            model.getProfilesRoot().addActiveProfile("active");
            model.endTransaction();
            Utilities.saveChanges(model);
        } finally {
            sourceFile.deleteOnExit();
        }
    }

    private ModelSource createModelSource(String templateName) throws FileNotFoundException, IOException, URISyntaxException {
        URL url = getClass().getClassLoader().getResource(templateName);
        File templateFile = new File(url.toURI());
        assertTrue(templateFile.exists());
        FileObject fo = FileUtil.toFileObject(templateFile);
        FileInputStream str = new FileInputStream(templateFile);
        StringOutputStream out = new StringOutputStream();
        FileUtil.copy(str, out);
        String dir = System.getProperty("java.io.tmpdir");
        File sourceFile = new File(dir, templateName);
        ModelSource source = Utilities.createModelSourceForMissingFile(sourceFile, true, out.toString(), "text/xml");
        assertTrue(source.isEditable());
        return source;
    }
}
