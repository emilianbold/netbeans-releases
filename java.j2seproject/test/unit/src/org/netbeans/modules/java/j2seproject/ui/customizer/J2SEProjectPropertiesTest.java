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

package org.netbeans.modules.java.j2seproject.ui.customizer;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileUtil;

public class J2SEProjectPropertiesTest extends NbTestCase {

    public J2SEProjectPropertiesTest(String name) {
        super(name);
    }

    private J2SEProject p;
    private J2SEProjectProperties pp;

    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        J2SEProjectGenerator.createProject(getWorkDir(), "test", null, null);
        p = (J2SEProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(getWorkDir()));
        pp = new J2SEProjectProperties(p, p.getUpdateHelper(), p.evaluator(), /* XXX unneeded probably */null, null);
    }

    public void testRunConfigs() throws Exception {
        Map<String,Map<String,String>> m = pp.readRunConfigs();
        assertEquals("{null={run.jvmargs=}}", m.toString());
        // Define a new config and set some arguments.
        Map<String,String> c = new TreeMap<String,String>();
        c.put("application.args", "foo");
        m.put("foo", c);
        storeRunConfigs(m);
        m = pp.readRunConfigs();
        assertEquals("{null={run.jvmargs=}, foo={application.args=foo}}", m.toString());
        // Define args in default config.
        m.get(null).put("application.args", "bland");
        storeRunConfigs(m);
        m = pp.readRunConfigs();
        assertEquals("{null={application.args=bland, run.jvmargs=}, foo={application.args=foo}}", m.toString());
        // Reset to default in foo config.
        m.get("foo").put("application.args", null);
        storeRunConfigs(m);
        m = pp.readRunConfigs();
        assertEquals("{null={application.args=bland, run.jvmargs=}, foo={}}", m.toString());
        // Override as blank in foo config.
        m.get("foo").put("application.args", "");
        storeRunConfigs(m);
        m = pp.readRunConfigs();
        assertEquals("{null={application.args=bland, run.jvmargs=}, foo={application.args=}}", m.toString());
    }

    private void storeRunConfigs(Map<String,Map<String,String>> m) throws IOException {
        EditableProperties prj = p.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        EditableProperties prv = p.getUpdateHelper().getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        pp.storeRunConfigs(m, prj, prv);
        p.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, prj);
        p.getUpdateHelper().putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, prv);
        ProjectManager.getDefault().saveProject(p);
    }

}
