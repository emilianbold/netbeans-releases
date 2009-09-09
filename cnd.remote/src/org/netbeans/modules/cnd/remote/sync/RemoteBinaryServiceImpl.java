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

package org.netbeans.modules.cnd.remote.sync;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.cnd.api.remote.RemoteBinaryService;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.cnd.remote.support.RemoteCommandSupport;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.openide.util.lookup.ServiceProvider;

/**
 * RemoteBinaryService implementation
 * @author Vladimir Kvashin
 */
@ServiceProvider(service=RemoteBinaryService.class)
public class RemoteBinaryServiceImpl extends RemoteBinaryService {

    private final Map<ExecutionEnvironment, Delegate> impls = new HashMap<ExecutionEnvironment, Delegate>();
    private static int downloadCount = 0;

    @Override
    protected String getRemoteBinaryImpl(ExecutionEnvironment execEnv, String remotePath) {
        CndUtils.assertNonUiThread();
        Delegate delegate;
        synchronized(this) {
            delegate = impls.get(execEnv);
            if (delegate == null) {
                delegate = new Delegate(execEnv);
                impls.put(execEnv, delegate);
            }
        }
        try {
            return delegate.getRemoteBinaryImpl(remotePath);
        } catch (InterruptedException ex) {
            // don't log InterruptedException
            return null;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } catch (ExecutionException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /*package-local for test purposes*/ static int getDownloadCount() {
        return downloadCount;
    }

    /*package-local for test purposes*/ static void resetDownloadCount() {
        downloadCount = 0;
    }


    /**
     * Corresoinds to the particular execution environment.
     * An instance of impl is created for each execution environment
     */
    private static class Delegate {

        private final ExecutionEnvironment execEnv;
        private final Map<String, Entry> cache = new HashMap<String, Entry>();

        public Delegate(ExecutionEnvironment execEnv) {
            this.execEnv = execEnv;
        }

        public String getRemoteBinaryImpl(String remotePath) throws InterruptedException, IOException, ExecutionException {
            Entry entry;
            synchronized(this) {
                entry = cache.get(remotePath);
                if (entry == null) {
                    entry = new Entry(execEnv, remotePath);
                    cache.put(remotePath, entry);
                }                
            }
            return entry.ensureSync();
        }
    }

    /**
     * Corresponds to a particular file
     */
    private static class Entry {
        
        private final String remotePath;
        private final ExecutionEnvironment execEnv;
        private File localFile;
        private String timeStamp;

        public Entry(ExecutionEnvironment execEnv, String remotePath) {
            this.remotePath = remotePath;
            this.execEnv = execEnv;
        }

        public String ensureSync() throws InterruptedException, IOException, ExecutionException {
            String localPath = RemotePathMap.getPathMap(execEnv).getLocalPath(remotePath, false);
            if (localPath == null) {
                return syncImpl();
            } else {
                if (!RemotePathMap.isTheSame(execEnv,
                        new File(remotePath).getParentFile().getAbsolutePath(), 
                        new File(localPath).getParentFile())) {
                    return syncImpl();
                }
            }
            return localPath;
        }

        private synchronized String syncImpl() throws IOException, InterruptedException, ExecutionException {
            if (localFile == null) {
                localFile = File.createTempFile("cnd-remote-binary-", ".bin"); // NOI18N
                localFile.deleteOnExit();
            }
            boolean copy = true;
            String newTimeStamp;
            RemoteCommandSupport rcs = new RemoteCommandSupport(execEnv, "ls -l " + remotePath); // NOI18N
            if (rcs.run() != 0) {
                // TODO: is there a better solution in the case ls finished with an error?
                return null;
            }
            newTimeStamp = rcs.getOutput();
            if (!newTimeStamp.equals(timeStamp) || !localFile.exists()) {
                Future<Integer> task = CommonTasksSupport.downloadFile(remotePath, execEnv, localFile.getAbsolutePath(), null);
                if (task.get() != 0) {
                    return null;
                }
                timeStamp = newTimeStamp;
                downloadCount++;
            }
            return localFile.getAbsolutePath();
        }
    }
}
