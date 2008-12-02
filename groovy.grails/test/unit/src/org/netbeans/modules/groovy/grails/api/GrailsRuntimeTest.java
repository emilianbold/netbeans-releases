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

package org.netbeans.modules.groovy.grails.api;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.groovy.grails.settings.GrailsSettings;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author Petr Hejl
 */
public class GrailsRuntimeTest extends NbTestCase {

    public GrailsRuntimeTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        super.tearDown();
    }

    public void testConfigured() throws IOException {
        final GrailsSettings settings = GrailsSettings.getInstance();
        final GrailsRuntime runtime = GrailsRuntime.getInstance();

        String path = getWorkDirPath();
        FileObject workDir = FileUtil.createFolder(FileUtil.normalizeFile(getWorkDir()));

        settings.setGrailsBase(path);
        assertFalse(runtime.isConfigured());

        FileObject dir = workDir.createFolder("bin");
        assertFalse(runtime.isConfigured());
        dir.createData(Utilities.isWindows() ? "grails.bat" : "grails");
        assertTrue(runtime.isConfigured());
    }

    public void testCommandDescriptor() throws IOException {
        GrailsRuntime.CommandDescriptor desc = new GrailsRuntime.CommandDescriptor(
                "test", getWorkDir(), GrailsEnvironment.DEV);

        assertEquals("test", desc.getName());
        assertEquals(getWorkDir(), desc.getDirectory());
        assertEquals(GrailsEnvironment.DEV, desc.getEnvironment());
        assertEquals(new String[] {}, desc.getArguments());
        assertEquals(new Properties(), desc.getProps());

        String[] args = new String[] {"arg1", "arg2"};
        desc = new GrailsRuntime.CommandDescriptor(
                "test", getWorkDir(), GrailsEnvironment.DEV, args);

        assertEquals("test", desc.getName());
        assertEquals(getWorkDir(), desc.getDirectory());
        assertEquals(GrailsEnvironment.DEV, desc.getEnvironment());
        assertEquals(args, desc.getArguments());
        assertEquals(new Properties(), desc.getProps());

        Properties props = new Properties();
        props.setProperty("prop1", "value1");
        props.setProperty("prop2", "value2");

        desc = new GrailsRuntime.CommandDescriptor(
                "test", getWorkDir(), GrailsEnvironment.DEV, args, props);

        assertEquals("test", desc.getName());
        assertEquals(getWorkDir(), desc.getDirectory());
        assertEquals(GrailsEnvironment.DEV, desc.getEnvironment());
        assertEquals(args, desc.getArguments());
        assertEquals(props, desc.getProps());

        // test immutability
        desc.getArguments()[0] = "wrong";
        assertEquals(args, desc.getArguments());
        desc.getProps().setProperty("wrong", "wrong");
        assertEquals(props, desc.getProps());

        String[] correctArgs = args.clone();
        args[0] = "wrong";
        assertEquals(correctArgs, desc.getArguments());
        Properties correctProps = new Properties(props);
        props.setProperty("wrong", "wrong");
        assertEquals(correctProps, desc.getProps());
    }

    private static void assertEquals(String[] expected, String[] value) {
        assertEquals(expected.length, value.length);

        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], value[i]);
        }
    }

    private static void assertEquals(Properties expected, Properties value) {
        Set<String> valueNames = new HashSet<String>();
        for (Enumeration e = value.propertyNames(); e.hasMoreElements();) {
            valueNames.add(e.nextElement().toString());
        }

        for (Enumeration e = expected.propertyNames(); e.hasMoreElements();) {
            String propName = e.nextElement().toString();
            String propValue = expected.getProperty(propName);
            assertTrue(valueNames.remove(propName));
            assertEquals(expected.getProperty(propName), propValue);
        }

        assertTrue(valueNames.isEmpty());
    }
}
