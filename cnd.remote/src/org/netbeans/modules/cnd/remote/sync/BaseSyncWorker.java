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
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import org.netbeans.modules.cnd.api.remote.RemoteSyncWorker;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.NbBundle;

/**
 * A common base class for RemoteSyncWorker implementations
 * @author Vladimir Kvashin
 */
/*package-local*/ abstract class BaseSyncWorker implements RemoteSyncWorker {

    protected final File topLocalDir;
    protected final File topDir;
    protected final File[] localDirs;
    protected final File privProjectStorageDir;
    protected final ExecutionEnvironment executionEnvironment;
    protected final PrintWriter out;
    protected final PrintWriter err;

    public BaseSyncWorker(ExecutionEnvironment executionEnvironment, PrintWriter out, PrintWriter err, File privProjectStorageDir, File... localDirs) {
        this.localDirs = new File[localDirs.length];
        System.arraycopy(localDirs, 0, this.localDirs, 0, localDirs.length);
        this.topLocalDir = getTopDir(localDirs);
        this.privProjectStorageDir = privProjectStorageDir;
        this.executionEnvironment = executionEnvironment;
        this.out = out;
        this.err = err;
        topDir = findCommonTop(localDirs);
    }

    private static File getTopDir(File[] dirs) {
        if (dirs == null || dirs.length == 0) {
            return null;
        }
        if (dirs.length == 1) {
            return dirs[0];
        }
        File top = dirs[0];
        while (top != null) {
            boolean isParent = true;
            for (int i = 1; i < dirs.length; i++) {
                String currPath = dirs[i].getAbsolutePath();
                if (!currPath.startsWith(top.getAbsolutePath())) {
                    isParent = false;
                    break;
                }
            }
            if (isParent) {
                return top;
            } else {
                top = top.getParentFile();
            }
        }
        return null;
    }

    private File findCommonTop(File[] dirs) {
        // TODO: implement
        return dirs[0];
    }

}
