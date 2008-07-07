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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.makeproject.api.compilers.BasicCompiler;
import org.netbeans.modules.cnd.remote.server.RemoteServerRecord;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Sergey Grinev
 */
public class SystemIncludesUtils {

    public static RequestProcessor.Task load(final RemoteServerRecord server) {
        final CompilerSet cs = new FakeCompilerSet(); // server.getCompilerSets() ???
        return RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                boolean success = load(server.getServerName(), server.getUserName(), cs);
                System.err.println("Loading done = " + success);
            }
        });

    // TODO: to think about next way:
    // just put links in the path mapped from server and set up
    // toolchain accordingly. Although those files will confuse user... 
    // Hiding links in nbproject can help but would lead for different
    // include set for each project and issues with connecting to new
    // hosts with the same project...
    }

    static boolean load(String serverName, String userName, CompilerSet cs) {
        File rsf = new File(storagePrefix + File.separator + serverName); // TODO: that's not enough
        if (!rsf.exists()) {
            rsf.mkdirs();
        }
        if (!rsf.isDirectory()) {
            //log
            return false;
        }

        RemoteCopySupport rcs = new RemoteCopySupport(userName + '@' + serverName);
        for (Tool tool : cs.getTools()) {
            if (tool instanceof BasicCompiler) {
                if (!load(rsf.getAbsolutePath(), rcs, (List<String>) ((BasicCompiler) tool).getSystemIncludeDirectories())) {
                    return false;
                }
            }
        }
        return true;
    }
    private static final String tempDir = System.getProperty("java.io.tmpdir");
    
    // should be communicated back to toolchain
    private static final String storagePrefix = System.getProperty("user.home") + "\\.netbeans\\remote-inc"; //NOI18N //TODO

    private static boolean load(String rsf, RemoteCopySupport rcs, List<String> paths) {
        //TODO: toolchain most probably will contain local paths.
        //for now let's assume they are remote
        for (String path : paths) {
            //TODO: check file existence (or make shell script to rule them all ?)
            System.err.println("loading " + path);
            String zipRemote = "cnd" + path.replaceAll("(/|\\\\)", "-") + ".zip"; //NOI18N
            String zipRemotePath = "/tmp/" + zipRemote;
            String zipLocalPath = tempDir + File.separator + zipRemote;

            rcs.run("zip -r -q " + zipRemotePath + " " + path); //NOI18N
            rcs.copyFrom(zipRemotePath, zipLocalPath);
            unzip(rsf, zipLocalPath);
        }
        return true;
    }

    static void unzip(String path, String fileName) {
        long start = System.currentTimeMillis();
        Enumeration entries;
        ZipFile zipFile;

        try {
            File parent = new File(path);
            if (!parent.exists()) {
                parent.mkdir();
            }
            assert parent.isDirectory();

            zipFile = new ZipFile(fileName);

            entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();

                File file = new File(parent, entry.getName());
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    //System.err.println("Extracting file: " + entry.getName());
                    copyInputStream(zipFile.getInputStream(entry),
                            new BufferedOutputStream(new FileOutputStream(file.getAbsolutePath())));
                }
            }

            zipFile.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return;
        } finally {
            System.err.println("unzipping " + fileName + " to " + path + " took " + (System.currentTimeMillis() - start) + " ms");
        }
    }

    private static final void copyInputStream(InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[1024];
        int len;

        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }
        in.close();
        out.close();
    }

    static class FakeCompilerSet extends CompilerSet {

        private List<Tool> tools = Collections.<Tool>singletonList(new FakeTool());

        @Override
        public List<Tool> getTools() {
            return tools;
        }

        private static class FakeTool extends BasicCompiler {

            private List<String> fakeIncludes = new ArrayList<String>();

            private FakeTool() {
                super(CompilerFlavor.GNU, 0, "fakeTool", "fakeTool", "/usr/sfw/bin");
                fakeIncludes.add("/usr/include");
                fakeIncludes.add("/usr/local/include");
                fakeIncludes.add("/usr/sfw/include");
            //fakeIncludes.add("/usr/sfw/lib/gcc/i386-pc-solaris2.10/3.4.3/include");
            }

            @Override
            public List getSystemIncludeDirectories() {
                return fakeIncludes;
            }
        }
    }
}
