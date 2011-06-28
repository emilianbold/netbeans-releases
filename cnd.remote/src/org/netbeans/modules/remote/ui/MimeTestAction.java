/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.remote.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.utils.NamedRunnable;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.openide.windows.WindowManager;

/**
 *
 * @author Vladimir Kvashin
 */
@ActionID(id = "org.netbeans.modules.remote.ui.MimeTestAction", category = "NativeRemote")
@ActionRegistration(displayName = "MimeTestMenuItem")
@ActionReference(path = "Remote/Host/Actions", name = "MimeTestAction", position = 400)
public class MimeTestAction extends SingleHostAction {

    private final RequestProcessor RP = new RequestProcessor("MimeTestAction", 8); // NOI18N
    private static final boolean TRACE = false;

    @Override
    public String getName() {
        return NbBundle.getMessage(HostListRootNode.class, "MimeTestMenuItem");
    }

    @Override
    public boolean isVisible(Node node) {
        return Boolean.getBoolean("remote.mime.test");
    }

    @Override
    protected void performAction(ExecutionEnvironment selectedHost, Node node) {        
        testMimeResolvers(selectedHost);
    }
    
    private void testMimeResolvers(ExecutionEnvironment selectedHost) {
        ExecutionEnvironment env;
        String localPrefix;
        if(selectedHost.isLocal()) {
            env = createLocalEnvAsRemote();
            localPrefix = "";
        } else {
            env = selectedHost;
            localPrefix = "/net/" + selectedHost + '/'; //NOI18N
            
        }
        RP.post(new Worker1(env, localPrefix));
    }

    private static ExecutionEnvironment createLocalEnvAsRemote() {
        ExecutionEnvironment local = ExecutionEnvironmentFactory.getLocal();        
        ExecutionEnvironment env = ExecutionEnvironmentFactory.createNew(local.getUser(), "localhost", 22); // NOI18N
        return env;        
    }

    private void testMimeResolvers(ExecutionEnvironment env, String localPrefix, String[] paths) {
        InputOutput io = IOProvider.getDefault().getIO("Test MIME " + env.getDisplayName(), true); // NOI18N
        io.select();
        OutputWriter out = io.getOut();
        OutputWriter err = io.getErr();
        out.printf("Testing MIME resolvers at %s", env); // NOI18N
        FileObject[] localFileObjects = new FileObject[paths.length];
        FileObject[] remoteFileObjects = new FileObject[paths.length];
        for (int i = 0; i < paths.length; i++) {
            localFileObjects[i] = FileUtil.toFileObject(FileUtil.normalizeFile(new File(paths[i])));
            if (localFileObjects[i] == null) {
                err.printf("Null local file object for " + paths[i]); // NOI18N
            }
            remoteFileObjects[i] = FileSystemProvider.getFileObject(env, paths[i]);
            if (remoteFileObjects[i] == null) {
                err.printf("Null remote file object for " + paths[i]); // NOI18N
            }
        }
        for (int i = 0; i < paths.length; i++) {
            Stat stat = new Stat();
            out.printf("Testing directory %s\n", paths[i]); // NOI18N
            try {
                List<Diff> diffs = new ArrayList<Diff>();
                testMimeResolvers(localFileObjects[i], remoteFileObjects[i], out, err, stat, true, new HashSet<String>(), diffs);
                OutputWriter w = diffs.isEmpty() ? out : err;
                w.printf("Testing directory %s done. %d files checked. %d differences found. Time: %d ms Max. file %s %d ms\n", // NOI18N
                        paths[i], stat.fileCount, diffs.size(), stat.dirTime, stat.maxFileName, stat.maxFileTime);
            } catch (IOException ex) {
                err.printf("Testing directory %s failed\n", paths[i]); // NOI18N
                ex.printStackTrace(System.err);
            }
        }
    }
    
    private void testMimeResolvers(FileObject localFO, FileObject remoteFO, OutputWriter out, OutputWriter err, 
            Stat stat, boolean recursive, Set<String> antiLoop, List<Diff> diffs) throws IOException {
        
        String canonicalPath = FileSystemProvider.getCanonicalPath(localFO);
        if (antiLoop.contains(canonicalPath)) {
            return;
        } else {
            antiLoop.add(canonicalPath);
        }        
        
        System.err.printf("Testing MIME resolvers for %s\n", localFO.getPath());
        
        FileObject[] localChildren = localFO.getChildren();
        // sorting is just for better user experience
        Arrays.sort(localChildren, new Comparator<FileObject>() {
            @Override
            public int compare(FileObject o1, FileObject o2) {
                return o1.getNameExt().compareTo(o2.getNameExt());
            }
            
        });
        
        long dirTime = 0;
        long maxFileTime = 0;
        long plainChilrenCount = 0;
        String maxFileName = "";
                
        for (FileObject localChild : localChildren) {
            FileObject remoteChild = remoteFO.getFileObject(localChild.getNameExt());
            if (remoteChild == null) {
                err.printf("Can not get remote file object for %s\n", localChild.getPath()); // NOI18N
                continue;
            }
            if (localChild.isData()) {
                plainChilrenCount++;
                String localMimeType = localChild.getMIMEType();
                long fileTime = System.currentTimeMillis();
                String remoteMimeType = remoteChild.getMIMEType();
                fileTime = System.currentTimeMillis() - fileTime;
                dirTime += fileTime;
                if (fileTime > maxFileTime) {
                    maxFileTime = fileTime;
                    maxFileName = localChild.getNameExt();
                }
                if (TRACE) {
                    out.printf("%s\t\t\t LOCAL: %s REMOTE: %s\n", localChild.getPath(), localMimeType, remoteMimeType); // NOI18N
                }
                if (!localMimeType.equals(remoteMimeType)) {
                    Diff diff = new Diff(localChild.getPath(), localMimeType, remoteMimeType);
                    diffs.add(diff);
                    reportDiff(diff, err);
                }
            }
        }

        if (recursive) {
            for (FileObject localChild : localChildren) {
                FileObject remoteChild = remoteFO.getFileObject(localChild.getNameExt());
                if (remoteChild != null && localChild.isFolder()) {
                    testMimeResolvers(localChild, remoteChild, out, err, stat, true, antiLoop, diffs);
                }
            }
        }        
        
        if (stat != null) {
            stat.dirTime += dirTime; 
            stat.fileCount += plainChilrenCount;
            if (maxFileTime > stat.maxFileTime) {
                stat.maxFileName = maxFileName;
                stat.maxFileTime = maxFileTime;
            }
        }
    }        

    private void reportDiff(Diff diff, OutputWriter err) {
        err.printf("MIME times differ for %s:\t\t\t LOCAL: %s REMOTE: %s\n", diff.path, diff.localMIME, diff.remoteMIME); // NOI18N
    }
    
    
    private static class Diff {
        public Diff(String path, String localMIME, String remoteMIME) {
            this.path = path;
            this.localMIME = localMIME;
            this.remoteMIME = remoteMIME;
        }        
        public final String path;
        public final String localMIME;
        public final String remoteMIME;
    }
    
    private static class Stat {
        public long dirTime;
        public long maxFileTime;
        public String maxFileName;
        public int fileCount;
    }
    
    private class Worker1 implements Runnable {
        private final ExecutionEnvironment env;
        private final String localPrefix;

        public Worker1(ExecutionEnvironment env, String localPrefix) {
            this.env = env;
            this.localPrefix = localPrefix;
        }
        
        @Override
        public void run() {
            try {
                ConnectionManager.getInstance().connectTo(env);
                SwingUtilities.invokeLater(new Worker2(env, localPrefix));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (CancellationException ex) {
            }
        }        
    };
    
    private static final String DEF_PATH_KEY = "mime.test.default.path"; //NOI18N
    private static String initialPath = NbPreferences.forModule(MimeTestAction.class).get(DEF_PATH_KEY, "/"); // NOI18N
    
    private class Worker2 implements Runnable {
        
        private final ExecutionEnvironment env;
        private final String localPrefix;

        public Worker2(ExecutionEnvironment env, String localPrefix) {
            this.env = env;
            this.localPrefix = localPrefix;
        }
        
        @Override
        public void run() {
            JFileChooser fc = RemoteFileUtil.createFileChooser(
                    env, "Choose directory", "Test", JFileChooser.DIRECTORIES_ONLY, null, initialPath, true); // NOI18N
            fc.setMultiSelectionEnabled(true);
            int ret = fc.showOpenDialog(WindowManager.getDefault().getMainWindow());
            if (ret == JFileChooser.APPROVE_OPTION) {
                File[] files = fc.getSelectedFiles();
                final String[] paths = new String[files.length];
                for (int i = 0; i < paths.length; i++) {
                    paths[i] = files[i].getAbsolutePath();                                        
                    if (i == 0) {
                        initialPath = paths[i];
                        NbPreferences.forModule(MimeTestAction.class).put(DEF_PATH_KEY, initialPath);
                    }                    
                }
                RP.post(new Worker3(env, localPrefix, paths));
            }
        }        
    };
    
    private class Worker3 extends NamedRunnable {

        private final ExecutionEnvironment env;
        private final String localPrefix;
        private final String[] paths;

        public Worker3(ExecutionEnvironment env, String localPrefix, String[] paths) {
            super("Testing MIME resolvers at " + env + ' ' + paths[0] + (paths.length > 1 ? "..." : "")); // NOI18N
            this.env = env;
            this.localPrefix = localPrefix;
            this.paths = paths;
        }
        
        
        @Override
        protected void runImpl() {
            testMimeResolvers(env, localPrefix, paths);
        }        
    };    
}
