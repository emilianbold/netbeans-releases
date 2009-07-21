/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.util;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import junit.framework.Test;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory.MacroExpander;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;
import org.openide.util.Exceptions;

/**
 *
 * @author ak119685
 */
public class MacroExpanderFactoryTest extends NativeExecutionBaseTestCase {

    public static Test suite() {
        return new NativeExecutionBaseTestSuite(MacroExpanderFactoryTest.class);
    }

    public MacroExpanderFactoryTest(String name) {
        super(name);
    }

    public MacroExpanderFactoryTest(String name, ExecutionEnvironment env) {
        super(name, env);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of getExpander method, of class MacroExpanderFactory.
     */
    public void testGetExpander_ExecutionEnvironment_String() {
        System.out.println("getExpander"); // NOI18N
        ExecutionEnvironment execEnv = ExecutionEnvironmentFactory.getLocal();
        MacroExpander expander = MacroExpanderFactory.getExpander(execEnv);//, "SunStudio"); // NOI18N

        Map<String, String> myenv = new HashMap<String, String>();
        try {
            myenv.put("PATH", expander.expandMacros("/bin:$PATH", myenv)); // NOI18N
            myenv.put("PATH", expander.expandMacros("/usr/bin:$platform:$PATH", myenv)); // NOI18N
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }

        System.out.println(myenv.toString());

        try {
            System.out.println("$osname-${platform}$_isa -> " + expander.expandPredefinedMacros("$osname-$platform$_isa")); // NOI18N
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

//    @Test
    public void testPath() {
        ExecutionEnvironment execEnv = ExecutionEnvironmentFactory.createNew(System.getProperty("user.name"), "localhost"); // NOI18N
        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        npb.setExecutable("/bin/env"); // NOI18N
        npb.addEnvironmentVariable("PATH", "/firstPath:$PATH:${ZZZ}_${platform}"); // NOI18N
        npb.addEnvironmentVariable("PATH", "$PATH:/secondPath"); // NOI18N
        npb.addEnvironmentVariable("XXX", "It WORKS!"); // NOI18N

        try {
            Process p = npb.call();
            String pout = ProcessUtils.readProcessOutputLine(p);
            System.out.println("Output is: " + pout); // NOI18N
            int result = p.waitFor();
            assertEquals(0, result);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}