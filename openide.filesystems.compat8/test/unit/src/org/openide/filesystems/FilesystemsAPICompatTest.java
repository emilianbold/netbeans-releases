/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.openide.filesystems;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Jaroslav Tulach &lt;jtulach@netbeans.org&gt;
 */
public class FilesystemsAPICompatTest extends NbTestCase {
    
    public FilesystemsAPICompatTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        return NbModuleSuite.createConfiguration(FilesystemsAPICompatTest.class).
            gui(false).
            suite();
    }
    
    @SuppressWarnings("deprecation")
    public void testFSCapability() throws Exception {
        Method m = FileSystem.class.getMethod("getCapability");
        assertEquals("Returns capability", FileSystemCapability.class, m.getReturnType());
    }

    @SuppressWarnings("deprecation")
    public void testFSSetCapability() throws Exception {
        Method m = findProtectedMethod(FileSystem.class, "setCapability", FileSystemCapability.class);
        assertEquals("Returns nothing", void.class, m.getReturnType());
    }

    @SuppressWarnings("deprecation")
    public void testFSGetActions() throws Exception {
        Method m = FileSystem.class.getMethod("getActions");
        assertEquals("Returns arr of actions", SystemAction[].class, m.getReturnType());
    }

    @SuppressWarnings("deprecation")
    public void testFSGetActionsOnASet() throws Exception {
        Method m = FileSystem.class.getMethod("getActions", Set.class);
        assertEquals("Returns arr of actions", SystemAction[].class, m.getReturnType());
    }

    @SuppressWarnings("deprecation")
    public void testFSIsHidden() throws Exception {
        Method m = FileSystem.class.getMethod("isHidden");
        assertEquals("Returns boolean", boolean.class, m.getReturnType());
    }

    @SuppressWarnings("deprecation")
    public void testFSSetHidden() throws Exception {
        Method m = FileSystem.class.getMethod("setHidden", boolean.class);
        assertEquals("Returns void", void.class, m.getReturnType());
    }

    @SuppressWarnings("deprecation")
    public void testFSIsPersistent() throws Exception {
        Method m = findProtectedMethod(FileSystem.class, "isPersistent");
        assertEquals("Returns boolean", boolean.class, m.getReturnType());
    }

    @SuppressWarnings("deprecation")
    public void testPrepereEnvironment() throws Exception {
        Method m = FileSystem.class.getMethod("prepareEnvironment", FileSystem$Environment.class);
        assertEquals("No return type", void.class, m.getReturnType());
        assertEquals("One declared exception", 1, m.getExceptionTypes().length);
        assertEquals(org.openide.filesystems.EnvironmentNotSupportedException.class, m.getExceptionTypes()[0]);
    }
    
    public void testJarFileSystemCapaConstructor() throws Exception {
        assertCapaConstructor(JarFileSystem.class);
    }

    public void testLocalFileSystemCapaConstructor() throws Exception {
        assertCapaConstructor(LocalFileSystem.class);
    }

    public void testXMLFileSystemCapaConstructor() throws Exception {
        assertCapaConstructor(XMLFileSystem.class);
    }
    
    @SuppressWarnings("deprecation")
    private static void assertCapaConstructor(Class<?> clazz) throws NoSuchMethodException {
        Constructor<?> c = clazz.getConstructor(FileSystemCapability.class);
        assertTrue("Is public", (c.getModifiers() | Modifier.PUBLIC) != 0);
    }

    private static Method findProtectedMethod(
        Class<?> clazz, String name, Class<?>... params
    ) throws NoSuchMethodException {
        while (clazz != null) {
            try {
                Method m = clazz.getDeclaredMethod(name, params);
                assertTrue("Is protected: " + m, (m.getModifiers() & Modifier.PROTECTED) != 0);
                return m;
            } catch (NoSuchMethodException ex) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchMethodException(name);
    }
}
