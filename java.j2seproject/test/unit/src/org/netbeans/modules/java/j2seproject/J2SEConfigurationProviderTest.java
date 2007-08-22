/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;

/**
 * @author Jesse Glick
 */
public class J2SEConfigurationProviderTest extends NbTestCase {

    public J2SEConfigurationProviderTest(String name) {
        super(name);
    }

    private FileObject d;
    private J2SEProject p;
    private ProjectConfigurationProvider pcp;

    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        d = J2SEProjectGenerator.createProject(getWorkDir(), "test", null, null).getProjectDirectory();
        p = (J2SEProject) ProjectManager.getDefault().findProject(d);
        pcp = p.getLookup().lookup(ProjectConfigurationProvider.class);
        assertNotNull(pcp);
        Locale.setDefault(Locale.US);
    }

    public void testInitialState() throws Exception {
        assertEquals(1, pcp.getConfigurations().size());
        assertNotNull(pcp.getActiveConfiguration());
        assertEquals(pcp.getActiveConfiguration(), pcp.getConfigurations().iterator().next());
        assertEquals("<default config>", pcp.getActiveConfiguration().getDisplayName());
        assertTrue(pcp.hasCustomizer());
    }

    public void testConfigurations() throws Exception {
        TestListener l = new TestListener();
        pcp.addPropertyChangeListener(l);
        Properties p = new Properties();
        p.setProperty("$label", "Debug");
        write(p, d, "nbproject/configs/debug.properties");
        p = new Properties();
        p.setProperty("$label", "Release");
        write(p, d, "nbproject/configs/release.properties");
        write(new Properties(), d, "nbproject/configs/misc.properties");
        assertEquals(Collections.singleton(ProjectConfigurationProvider.PROP_CONFIGURATIONS), l.events());
        List<ProjectConfiguration> configs = new ArrayList<ProjectConfiguration>(getConfigurations(pcp));
        assertEquals(4, configs.size());
        assertEquals("<default config>", configs.get(0).getDisplayName());
        assertEquals("Debug", configs.get(1).getDisplayName());
        assertEquals("misc", configs.get(2).getDisplayName());
        assertEquals("Release", configs.get(3).getDisplayName());
        assertEquals(Collections.emptySet(), l.events());
        d.getFileObject("nbproject/configs/debug.properties").delete();
        assertEquals(Collections.singleton(ProjectConfigurationProvider.PROP_CONFIGURATIONS), l.events());
        configs = new ArrayList<ProjectConfiguration>(getConfigurations(pcp));
        assertEquals(3, configs.size());
        d.getFileObject("nbproject/configs").delete();
        assertEquals(Collections.singleton(ProjectConfigurationProvider.PROP_CONFIGURATIONS), l.events());
        configs = new ArrayList<ProjectConfiguration>(getConfigurations(pcp));
        assertEquals(1, configs.size());
        write(new Properties(), d, "nbproject/configs/misc.properties");
        assertEquals(Collections.singleton(ProjectConfigurationProvider.PROP_CONFIGURATIONS), l.events());
        configs = new ArrayList<ProjectConfiguration>(getConfigurations(pcp));
        assertEquals(2, configs.size());
    }

    public void testActiveConfiguration() throws Exception {
        write(new Properties(), d, "nbproject/configs/debug.properties");
        write(new Properties(), d, "nbproject/configs/release.properties");
        TestListener l = new TestListener();
        pcp.addPropertyChangeListener(l);
        ProjectConfiguration def = pcp.getActiveConfiguration();
        assertEquals("<default config>", def.getDisplayName());
        List<ProjectConfiguration> configs = new ArrayList<ProjectConfiguration>(getConfigurations(pcp));
        assertEquals(3, configs.size());
        ProjectConfiguration c = configs.get(2);
        assertEquals("release", c.getDisplayName());
        setActiveConfiguration(pcp, c);
        assertEquals(c, pcp.getActiveConfiguration());
        assertEquals(Collections.singleton(ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE), l.events());
        setActiveConfiguration(pcp, c);
        assertEquals(c, pcp.getActiveConfiguration());
        assertEquals(Collections.emptySet(), l.events());
        setActiveConfiguration(pcp, def);
        assertEquals(def, pcp.getActiveConfiguration());
        assertEquals(Collections.singleton(ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE), l.events());
        try {
            setActiveConfiguration(pcp, null);
            fail();
        } catch (IllegalArgumentException x) {/*OK*/}
        assertEquals(Collections.emptySet(), l.events());
        try {
            setActiveConfiguration(pcp, new ProjectConfiguration() {
                public String getDisplayName() {
                    return "bogus";
                }
            });
            fail();
        } catch (IllegalArgumentException x) {
            // OK, not in original set
        } catch (ClassCastException x) {
            // also OK, not of correct type
        }
        assertEquals(Collections.emptySet(), l.events());
        EditableProperties ep = new EditableProperties();
        ep.setProperty("config", "debug");
        p.getUpdateHelper().putProperties("nbproject/private/config.properties", ep);
        assertEquals("debug", pcp.getActiveConfiguration().getDisplayName());
        assertEquals(Collections.singleton(ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE), l.events());
    }

    public void testEvaluator() throws Exception {
        PropertyEvaluator eval = p.evaluator();
        TestListener l = new TestListener();
        eval.addPropertyChangeListener(l);
        Properties p = new Properties();
        p.setProperty("debug", "true");
        write(p, d, "nbproject/configs/debug.properties");
        p = new Properties();
        p.setProperty("debug", "false");
        write(p, d, "nbproject/configs/release.properties");
        p = new Properties();
        p.setProperty("more", "stuff");
        write(p, d, "nbproject/private/configs/release.properties");
        List<ProjectConfiguration> configs = new ArrayList<ProjectConfiguration>(getConfigurations(pcp));
        assertEquals(3, configs.size());
        ProjectConfiguration c = configs.get(1);
        assertEquals("debug", c.getDisplayName());
        setActiveConfiguration(pcp, c);
        assertEquals(new HashSet<String>(Arrays.asList("config", "debug")), l.events());
        assertEquals("debug", eval.getProperty("config"));
        assertEquals("true", eval.getProperty("debug"));
        assertEquals(null, eval.getProperty("more"));
        c = configs.get(2);
        assertEquals("release", c.getDisplayName());
        setActiveConfiguration(pcp, c);
        assertEquals(new HashSet<String>(Arrays.asList("config", "debug", "more")), l.events());
        assertEquals("release", eval.getProperty("config"));
        assertEquals("false", eval.getProperty("debug"));
        assertEquals("stuff", eval.getProperty("more"));
        c = configs.get(0);
        assertEquals("<default config>", c.getDisplayName());
        setActiveConfiguration(pcp, c);
        assertEquals(new HashSet<String>(Arrays.asList("config", "debug", "more")), l.events());
        assertEquals(null, eval.getProperty("config"));
        assertEquals(null, eval.getProperty("debug"));
        assertEquals(null, eval.getProperty("more"));
        // XXX test nbproject/private/configs/*.properties
    }

    public void testInitialListening() throws Exception { // #84781
        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
            public Void run() throws Exception {
                TestListener l = new TestListener();
                pcp.addPropertyChangeListener(l);
                Properties props = new Properties();
                props.setProperty("$label", "X");
                write(props, d, "nbproject/configs/x.properties");
                props = new Properties();
                props.setProperty("config", "x");
                write(props, d, "nbproject/private/config.properties");
                assertEquals(new HashSet<String>(Arrays.asList(ProjectConfigurationProvider.PROP_CONFIGURATIONS, ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE)),
                        l.events());
                assertEquals(2, pcp.getConfigurations().size());
                assertEquals("X", pcp.getActiveConfiguration().getDisplayName());
                return null;
            }
        });
    }

    private void write(Properties p, FileObject d, String path) throws IOException {
        FileObject f = FileUtil.createData(d, path);
        OutputStream os = f.getOutputStream();
        p.store(os, null);
        os.close();
    }

    private static final class TestListener implements PropertyChangeListener {
        private Set<String> events = new HashSet<String>();
        public void propertyChange(PropertyChangeEvent evt) {
            events.add(evt.getPropertyName());
        }
        public Set<String> events() {
            Set<String> copy = events;
            events = new HashSet<String>();
            return copy;
        }
    }

    private static Collection<? extends ProjectConfiguration> getConfigurations(ProjectConfigurationProvider<?> pcp) {
        return pcp.getConfigurations();
    }

    @SuppressWarnings("unchecked")
    private static void setActiveConfiguration(ProjectConfigurationProvider<?> pcp, ProjectConfiguration pc) throws IOException {
        ProjectConfigurationProvider _pcp = pcp;
        _pcp.setActiveConfiguration(pc);
    }

}
