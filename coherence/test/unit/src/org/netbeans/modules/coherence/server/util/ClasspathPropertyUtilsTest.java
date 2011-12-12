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
package org.netbeans.modules.coherence.server.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.server.properties.InstanceProperties;
import org.netbeans.api.server.properties.InstancePropertiesManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.coherence.server.CoherenceModuleProperties;
import org.openide.filesystems.FileUtil;

/**
 * Tests for checking ClasspathPropertyUtils helper methods.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ClasspathPropertyUtilsTest extends NbTestCase {

    public ClasspathPropertyUtilsTest(String name) {
        super(name);
    }

    public void testClasspathFromStringToArray() throws Exception {
        String classpath = "/home/marfous/path1" + CoherenceModuleProperties.CLASSPATH_SEPARATOR
                + "/home/marfous/path2";
        String[] array = ClasspathPropertyUtils.classpathFromStringToArray(classpath);
        assertEquals(array.length, 2);

        classpath = "/home/marfous/path1" + CoherenceModuleProperties.CLASSPATH_SEPARATOR
                + "/home/marfous/path2" + CoherenceModuleProperties.CLASSPATH_SEPARATOR;
        array = ClasspathPropertyUtils.classpathFromStringToArray(classpath);
        assertEquals(array.length, 2);

        classpath = "    ";
        array = ClasspathPropertyUtils.classpathFromStringToArray(classpath);
        assertEquals(array.length, 0);

        classpath = CoherenceModuleProperties.CLASSPATH_SEPARATOR;
        array = ClasspathPropertyUtils.classpathFromStringToArray(classpath);
        assertEquals(array.length, 0);
    }

    public void testClasspathFromListToString() throws Exception {
        List<String> cps = new ArrayList<String>(Arrays.asList("/home/marfous/path1", "/home/marfous/path2"));
        String string = ClasspathPropertyUtils.classpathFromListToString(cps);
        assertEquals("/home/marfous/path1" + CoherenceModuleProperties.CLASSPATH_SEPARATOR
                + "/home/marfous/path2", string);

        cps.remove(1);
        string = ClasspathPropertyUtils.classpathFromListToString(cps);
        assertEquals("/home/marfous/path1", string);

        cps.remove(0);
        string = ClasspathPropertyUtils.classpathFromListToString(cps);
        assertEquals("", string);
    }

    public void testGetAbsolutePath() throws Exception {
        File serverLibDir = new File(getWorkDirPath() + "/server/lib");
        serverLibDir.mkdirs();
        FileUtil.createData(new File(serverLibDir, "myJar.jar"));
        String absolutePath = ClasspathPropertyUtils.getAbsolutePath(serverLibDir.getParent(), "myJar.jar");

        assertEquals(getWorkDirPath() + "/server/lib/myJar.jar", absolutePath);
    }

    public void testIsCoherenceServerJar() throws Exception {
        String jarPath = "/home/marfous/lib/coherence-jpa.jar";
        assertTrue(ClasspathPropertyUtils.isCoherenceServerJar(jarPath));

        jarPath = "/home/marfous/lib/coherence.jar";
        assertFalse(ClasspathPropertyUtils.isCoherenceServerJar(jarPath));

        jarPath = "/home/marfous/lib/coherence.jar";
        assertTrue(ClasspathPropertyUtils.isCoherenceServerJar(jarPath, true));

        jarPath = "/home/marfous/lib/jaxb.jar";
        assertFalse(ClasspathPropertyUtils.isCoherenceServerJar(jarPath));
    }

    public void testUpdateClasspathProperty() throws Exception {
        String classpath = "/home/marfous/Coherence/lib/coherence.jar";
        InstanceProperties properties = InstancePropertiesManager.getInstance().createProperties("coherence");
        properties.putString(CoherenceModuleProperties.PROP_CLASSPATH, classpath);

        String addtionalCp = "/home/marfous/jars/jaxb.jar";
        ClasspathPropertyUtils.updateClasspathProperty(properties, new String[] {addtionalCp}, null);
        assertEquals(classpath + CoherenceModuleProperties.CLASSPATH_SEPARATOR + addtionalCp,
                properties.getString(CoherenceModuleProperties.PROP_CLASSPATH, ""));

        String libCp = "/home/marfous/Coherence/lib/coherence-jpa.jar";
        ClasspathPropertyUtils.updateClasspathProperty(properties, null, new String[] {libCp});
        assertEquals(libCp + CoherenceModuleProperties.CLASSPATH_SEPARATOR + classpath + CoherenceModuleProperties.CLASSPATH_SEPARATOR + addtionalCp,
                properties.getString(CoherenceModuleProperties.PROP_CLASSPATH, ""));
    }

}
