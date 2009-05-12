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
import java.io.PrintWriter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.RemoteSyncWorker;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;

/**
 *
 * @author Vladimir Kvashin
 */
/*package-local*/ class ScpSyncWorker extends BaseSyncWorker implements RemoteSyncWorker {

    private Logger logger = Logger.getLogger("cnd.remote.logger"); // NOI18N

    public ScpSyncWorker(File localDir, ExecutionEnvironment executionEnvironment, PrintWriter out, PrintWriter err) {
        super(localDir, executionEnvironment, out, err);
    }

    public boolean synchronize() {

        // determine the remote directory
        PathMap mapper = HostInfoProvider.getMapper(executionEnvironment);
        String localParent = this.localDir.getParentFile().getAbsolutePath();
        String remoteParent = mapper.getRemotePath(localParent);
        if (!HostInfoProvider.fileExists(executionEnvironment, remoteParent)) {
            if (mapper.checkRemotePath(localParent, true)) {
                remoteParent = mapper.getRemotePath(localParent);
            } else {
                return false;
            }
        }
        String remoteDir = remoteParent + '/' + localDir.getName(); //NOI18N
        try {
            synchronizeImpl(remoteDir);
            return true;
        } catch (InterruptedException ex) {
            logger.log(Level.FINE, null, ex);
        } catch (ExecutionException ex) {
            logger.log(Level.FINE, null, ex);
        } catch (IOException ex) {
            logger.log(Level.FINE, null, ex);
        }
        return false;
    }

    public void synchronizeImpl(String remoteDir) throws InterruptedException, ExecutionException, IOException {
        CommonTasksSupport.mkDir(executionEnvironment, remoteDir, err);
        for (File file : localDir.listFiles()) {
            synchronizeImpl(file, remoteDir);
        }
    }

    private void synchronizeImpl(File file, String remoteDir) throws InterruptedException, ExecutionException, IOException {
        if (file.isDirectory()) {
            remoteDir += "/"  + file.getName(); // NOI18N
            CommonTasksSupport.mkDir(executionEnvironment, remoteDir, err);
            for (File child : file.listFiles()) {
                synchronizeImpl(child, remoteDir);
            }
        } else {
            String path = file.getAbsolutePath();
            Future<Integer> upload = CommonTasksSupport.uploadFile(path, executionEnvironment, remoteDir, 0777, err);
            int rc = upload.get();
            logger.finest("scp: uploading " + path + " to " + remoteDir + " rc=" + rc); //NOI18N
            if (rc != 0) {
                throw new IOException("uploading " + path + " to " + remoteDir + // NOI18N
                        "finished with error code " + rc); // NOI18N
            }
        }
    }

    @Override
    public boolean cancel() {
        return false;
    }
}
