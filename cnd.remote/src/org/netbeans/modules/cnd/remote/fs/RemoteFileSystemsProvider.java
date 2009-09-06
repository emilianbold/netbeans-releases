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

package org.netbeans.modules.cnd.remote.fs;

import java.io.File;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.makeproject.api.compilers.BasicCompiler;
import org.netbeans.modules.cnd.spi.utils.FileSystemsProvider;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Vladimir Kvashin
 */
@ServiceProvider(service=FileSystemsProvider.class)
public class RemoteFileSystemsProvider extends FileSystemsProvider {

    public static final boolean USE_REMOTE_FS = CndUtils.getBoolean("cnd.remote.fs", true);

    @Override
    protected Data getImpl(File file) {
        if (USE_REMOTE_FS) {
            return getImpl(file.getAbsolutePath());
        }
        return null;
    }

    @Override
    protected Data getImpl(CharSequence path) {
        if (USE_REMOTE_FS) {
            String prefix = BasicCompiler.getIncludeFileBase();
            if (CharSequenceUtils.startsWith(path, prefix)) {
                CharSequence rest = path.subSequence(prefix.length(), path.length());
                int slashPos = CharSequenceUtils.indexOf(rest, "/"); // NOI18N
                if (slashPos >= 0) {
                    String hostName = rest.subSequence(0, slashPos).toString();
                    CharSequence remotePath = rest.subSequence(slashPos + 1, rest.length());
                    ExecutionEnvironment env = getExecutionEnvironment(hostName);
                    if (env != null) {
                        final RemoteFileSystem fs = RemoteFileSystemManager.getInstance().get(env);
                        Data data = new Data(fs, remotePath.toString());
                        return data;
                    }
                }
            }
        }
        return null;
    }

    private ExecutionEnvironment getExecutionEnvironment(String hostName) {
        ExecutionEnvironment result = null;
        for(ExecutionEnvironment env : ServerList.getEnvironments()) {
            if (env.getHost().equals(hostName)) {
                result = env;
                if (ConnectionManager.getInstance().isConnectedTo(env)) {
                    break;
                }
            }
        }
        return result;
    }

}
