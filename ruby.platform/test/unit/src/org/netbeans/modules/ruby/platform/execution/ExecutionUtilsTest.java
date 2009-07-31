/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.ruby.platform.execution;

import java.util.concurrent.Future;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.ruby.platform.RubyPlatformManager;
import org.netbeans.api.ruby.platform.RubyTestBase;
import org.netbeans.api.ruby.platform.TestUtil;
import org.openide.util.Utilities;

public class ExecutionUtilsTest extends RubyTestBase {

    public ExecutionUtilsTest(String testName) {
        super(testName);
    }

    public void testComputeJRubyClassPath() {
        String[] expectedJars = {
            "jruby.jar",
            "profile.jar",};
        Arrays.sort(expectedJars);
        File jrubyLib = new File(TestUtil.getXTestJRubyHome(), "lib");
        String cp = ExecutionUtils.computeJRubyClassPath(null, jrubyLib);
        String finalCP;
        if (Utilities.isWindows()) {
            assertTrue(cp.startsWith("\""));
            assertTrue(cp.endsWith("\""));
            finalCP = cp.substring(1, cp.length() - 1);
        } else {
            finalCP = cp;
        }
        String[] jars = finalCP.split(File.pathSeparator);
        // assertEquals(Arrays.asList(expectedJars), Arrays.asList(jars));
        assertEquals(expectedJars.length, jars.length);
        Arrays.sort(jars);
        for (int i = 0; i < jars.length; i++) {
            assertTrue(jars[i] + " ends with " + expectedJars[i], jars[i].endsWith(expectedJars[i]));
        }
    }

    public void testAdditionalEnvironment() throws Exception {
        RubyPlatform platform = RubyPlatformManager.getDefaultPlatform();
        RubyExecutionDescriptor descriptor = new RubyExecutionDescriptor(platform);

        descriptor.cmd(platform.getInterpreterFile());
        String gemPath = getWorkDirPath() + File.separator + "fake-repo";
        descriptor.addAdditionalEnv(Collections.<String, String>singletonMap("GEM_PATH", gemPath));

        List<String> argList = new ArrayList<String>();
        argList.add("-e");
        File file = new File(getWorkDir(), "gp.txt");
        argList.add("File.open('" + file.getAbsolutePath() + "', 'w'){|f|f.printf ENV['GEM_PATH']}");
        descriptor.additionalArgs(argList.toArray(new String[argList.size()]));
        RubyProcessCreator rpc = new RubyProcessCreator(descriptor);
        org.netbeans.api.extexecution.ExecutionService service =
                org.netbeans.api.extexecution.ExecutionService.newService(rpc, descriptor.toExecutionDescriptor(), null);
        Future<Integer> execution = service.run();
        execution.get();
        assertTrue(execution.isDone());
        assertEquals("right GEM_PATH", gemPath, slurp(file));
    }
}
