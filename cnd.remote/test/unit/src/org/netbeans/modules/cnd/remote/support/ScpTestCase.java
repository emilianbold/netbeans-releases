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
package org.netbeans.modules.cnd.remote.support;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.remote.mapper.RemoteHostInfoProvider;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 * @author Sergey Grinev
 */
public class ScpTestCase extends RemoteTestBase {

    public ScpTestCase(String testName) {
        super(testName);
    }

//    public void testNewJsch() throws Exception {
//        // this test will fail, setEnv doesn't allow to set any vars
//        System.err.println("test");
//        Map<String, String> env = new HashMap<String, String>();
//        env.put("envTestKey", "envTestValue");
//        String key = getKey();
//        RemoteCommandSupport support = new RemoteCommandSupport(key, "setenv", env, 32);
//        support.run();
//        System.err.println("result = " + support.getExitStatus());
//        System.err.println("output=" + support.toString());
//        assert support.toString().indexOf("envTestKey=envTestValue") > -1;
//    }

    private static final String cshLine = "setenv envTestKey \"envTestValue\";setenv envTestKey2 \"envTestValue2\";";
    private static final String cshLine2 = "setenv envTestKey2 \"envTestValue2\";setenv envTestKey \"envTestValue\";";
    private static final String bashLine = "export envTestKey=\"envTestValue\";export envTestKey2=\"envTestValue2\";";
    private static final String bashLine2 = "export envTestKey2=\"envTestValue2\";export envTestKey=\"envTestValue\";";

    public void testShellUtils() throws Exception {
        Map<String, String> env = new HashMap<String, String>();
        env.put("envTestKey", "envTestValue");
        env.put("envTestKey2", "envTestValue2");
        String line = ShellUtils.prepareExportString(true, env);
        assert cshLine.equals(line) || cshLine2.equals(line);
        String line2 = ShellUtils.prepareExportString(false, env);
        assert bashLine.equals(line2) || bashLine2.equals(line2);
        String[] env2 = {"envTestKey=envTestValue","envTestKey2=envTestValue2"};
        String line3= ShellUtils.prepareExportString(true, env2);
        assert cshLine.equals(line3) || cshLine2.equals(line3);
        String line4 = ShellUtils.prepareExportString(false, env2);
        assert bashLine.equals(line4) || bashLine2.equals(line4);
    }

//    public void testFileExistst() throws Exception {
//        HostInfoProvider hip = HostInfoProvider.getDefault();
//        assert hip.fileExists(getKey(), "/tmp/xxx");
//        assert !hip.fileExists(getKey(), "/tmp/xxx222");
//    }

//    public void testGetEnv() throws Exception {
//        Map<String, String> env = RemoteHostInfoProvider.getDefault().getEnv(getKey());
//        assert env != null && env.size() > 0;
//    }
//
//    public void testCopyTo() throws Exception {
//        File localFile = File.createTempFile("cnd", ".cnd");
//        FileWriter fstream = new FileWriter(localFile);
//        BufferedWriter out = new BufferedWriter(fstream);
//        out.write("File from "); //NOI18N
//        try {
//            InetAddress addr = InetAddress.getLocalHost();
//            out.write( addr.getHostName() );
//        } catch (UnknownHostException e) {
//        }
//        out.write("\n");
//        out.write(System.currentTimeMillis()+ "\n");
//        out.close();
//        RemoteCopySupport rcs = new RemoteCopySupport(getKey());
//        assert rcs.copyTo(localFile.getAbsolutePath(), "/tmp");
//    }
//    
//    public void qtestCopyFile() throws Exception {
//        File localFile = File.createTempFile("cnd", ".cnd");
//        File localFile2 = new File(localFile.getAbsolutePath().replace(".cnd", ".2.cnd"));
//        RemoteCopySupport rcs = new RemoteCopySupport(getKey());
//        assert rcs.copyFrom("/tmp/ReadMe.txt", localFile.getAbsolutePath());
//        System.err.println("testLoadIncludes created " + localFile.getAbsolutePath());
//        assert localFile.exists();
//
//        assert rcs.copyFrom("/tmp/ReadMe.txt", localFile2.getAbsolutePath());
//        System.err.println("testLoadIncludes created " + localFile2.getAbsolutePath());
//        assert localFile2.exists();
//
//        rcs.disconnect();
//    }
//    
//    public void qtestRun() throws Exception {
//        RemoteCopySupport rcs = new RemoteCopySupport(getKey());
//        rcs.run("ls /tmp");
//        rcs.disconnect();
//    }
//
//    public void qtestUnzip() {
//        long start = System.currentTimeMillis();
//        SystemIncludesUtils.unzip("C:\\123","C:\\123.zip");
//        System.err.println("Unzipping took " + (System.currentTimeMillis() - start) + "ms.");
//    }
//    
//    public void testSystemIncludesUtils() throws Exception {
//        SystemIncludesUtils.load(getHostName(), getUserName(), new SystemIncludesUtils.FakeCompilerSet());
//    }
}
