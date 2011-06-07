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
package org.netbeans.modules.cnd.gizmo.addr2line;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import org.netbeans.modules.cnd.dwarfdump.LddService;
import org.netbeans.modules.cnd.dwarfdump.reader.ElfReader.SharedLibraries;
import org.netbeans.modules.cnd.gizmo.RemoteJarServiceProvider;
import org.netbeans.modules.cnd.test.CndBaseTestCase;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.NativeProcess;

/**
 *
 * @author Alexander Simon
 */
public class LddServiceTest extends CndBaseTestCase {

    public LddServiceTest() {
        super("LddServiceTest");
    }

    public void testProfilingdemo() throws IOException {
        // java -cp /export/home/as204739/link_to_nbbuild/netbeans/cnd/modules/org-netbeans-modules-cnd-dwarfdump.jar org.netbeans.modules.cnd.dwarfdump.Offset2LineService profilingdemo
        //Offset2LineService.main(new String[]{executable});
        String executable = getResource("/org/netbeans/modules/cnd/gizmo/addr2line/profilingdemo");
        SharedLibraries res1 = LddService.getPubNames(executable);
        NativeProcess process = getJavaProcess(LddService.class, ExecutionEnvironmentFactory.getLocal(), new String[]{executable});
        assertNotNull(process);
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(),Charset.forName("UTF-8"))); // NOI18N
        SharedLibraries res2 = LddService.getPubNames(br);
        process.destroy();
        assertEquals(res1.getDlls().size(), res2.getDlls().size());
        assertEquals(res1.getPaths().size(), res2.getPaths().size());
        for(int i = 0; i < res1.getDlls().size(); i++) {
            assert  res1.getDlls().get(i).equals(res2.getDlls().get(i));
        }
        for(int i = 0; i < res1.getPaths().size(); i++) {
            assert  res1.getPaths().get(i).equals(res2.getPaths().get(i));
        }
    }

    private NativeProcess getJavaProcess(Class<?> clazz, ExecutionEnvironment env, String[] arguments) throws IOException{
        return RemoteJarServiceProvider.getJavaProcess(clazz, env, arguments);
    }

    private String getResource(String resource) {
        File dataDir = getDataDir();
        return dataDir.getAbsolutePath() + resource;
    }
}