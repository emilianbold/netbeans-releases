/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.gizmo.addr2line;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cnd.dwarfdump.CompileLineService;
import org.netbeans.modules.cnd.dwarfdump.CompileLineService.SourceFile;
import org.netbeans.modules.cnd.dwarfdump.Offset2LineService;
import org.netbeans.modules.cnd.dwarfdump.Offset2LineService.AbstractFunctionToLine;
import org.netbeans.modules.cnd.gizmo.RemoteJarServiceProvider;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;

/**
 *
 * @author Alexander Simon
 */
public class Offset2LineServiceTest extends NbTestCase {

    public Offset2LineServiceTest() {
        super("Offset2LineServiceTest");
    }

    public void testProfilingdemo() throws IOException {
        // java -cp /export/home/as204739/link_to_nbbuild/netbeans/cnd/modules/org-netbeans-modules-cnd-dwarfdump.jar org.netbeans.modules.cnd.dwarfdump.Offset2LineService profilingdemo
        //Offset2LineService.main(new String[]{executable});
        String executable = getResource("/org/netbeans/modules/cnd/gizmo/addr2line/profilingdemo");
        Map<String, AbstractFunctionToLine> res1 = Offset2LineService.getOffset2Line(executable);
        res1 = new TreeMap<String, AbstractFunctionToLine>(res1);
        Map<String, AbstractFunctionToLine> res2;
        
        //ByteArrayOutputStream wr = new ByteArrayOutputStream();
        //PrintStream ps = new PrintStream(wr);
        //Offset2LineService.dump(executable, ps);
        //BufferedReader br = new  BufferedReader(new StringReader(wr.toString()));
        //res2 = Offset2LineService.getOffset2Line(br);
        //assertEquals(res1.size(), res2.size());
        //res2 = new TreeMap<String, AbstractFunctionToLine>(res2);
        //for(String function : res1.keySet()) {
        //    AbstractFunctionToLine line1 = res1.get(function);
        //    AbstractFunctionToLine line2 = res2.get(function);
        //    assertNotNull(line1);
        //    assertNotNull(line2);
        //    assertEquals(res1, res2);
        //}

        NativeProcess process = getJavaProcess(Offset2LineService.class, ExecutionEnvironmentFactory.getLocal(), new String[]{executable});
        assertNotNull(process);
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        res2 = Offset2LineService.getOffset2Line(br);
        process.destroy();
        assertEquals(res1.size(), res2.size());
        res2 = new TreeMap<String, AbstractFunctionToLine>(res2);
        for(String function : res1.keySet()) {
            AbstractFunctionToLine line1 = res1.get(function);
            AbstractFunctionToLine line2 = res2.get(function);
            assertNotNull(line1);
            assertNotNull(line2);
            assertEquals(line1, line2);
        }
    }

    public void testFftimagetransformer() throws IOException {
        // java -cp /export/home/as204739/link_to_nbbuild/netbeans/cnd/modules/org-netbeans-modules-cnd-dwarfdump.jar org.netbeans.modules.cnd.dwarfdump.Offset2LineService profilingdemo
        //Offset2LineService.main(new String[]{executable});
        String executable = getResource("/org/netbeans/modules/cnd/gizmo/addr2line/fftimagetransformer");
        List<SourceFile> res1 = CompileLineService.getSourceFileProperties(executable);

        NativeProcess process = getJavaProcess(CompileLineService.class, ExecutionEnvironmentFactory.getLocal(), new String[]{"-file",  executable});
        assertNotNull(process);
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        List<SourceFile> res2 = CompileLineService.getSourceProperties(br);
        process.destroy();
        assertEquals(res1.size(), res2.size());
        for(int i = 0; i < res1.size(); i++){
            SourceFile f1 = res1.get(i);
            SourceFile f2 = res2.get(i);
            assertEquals(f1.getCompileDir(), f2.getCompileDir());
            assertEquals(f1.getSource(), f2.getSource());
            assertEquals(f1.getCompileLine(), f2.getCompileLine());
        }
    }

    public void testFolder() throws IOException {
        // java -cp /export/home/as204739/link_to_nbbuild/netbeans/cnd/modules/org-netbeans-modules-cnd-dwarfdump.jar org.netbeans.modules.cnd.dwarfdump.Offset2LineService profilingdemo
        //Offset2LineService.main(new String[]{executable});
        String executable = getResource("/org/netbeans/modules/cnd/gizmo/addr2line");
        List<SourceFile> res1 = CompileLineService.getSourceFolderProperties(executable);

        NativeProcess process = getJavaProcess(CompileLineService.class, ExecutionEnvironmentFactory.getLocal(), new String[]{"-folder",  executable});
        assertNotNull(process);
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        List<SourceFile> res2 = CompileLineService.getSourceProperties(br);
        process.destroy();
        assertEquals(res1.size(), res2.size());
        for(int i = 0; i < res1.size(); i++){
            SourceFile f1 = res1.get(i);
            SourceFile f2 = res2.get(i);
            assertEquals(f1.getCompileDir(), f2.getCompileDir());
            assertEquals(f1.getSource(), f2.getSource());
            assertEquals(f1.getCompileLine(), f2.getCompileLine());
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
