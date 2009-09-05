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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;

/**
 * Reponsible for copying files from remote host
 * @author Vladimir Kvashin
 */
public class RemoteFileSupport {

    private final ExecutionEnvironment execEnv;

    public RemoteFileSupport(ExecutionEnvironment execEnv) {
        this.execEnv = execEnv;
    }

    public void syncDirStruct(File dir, String remotePath) throws IOException {
        if (dir.exists()) {
            assert dir.isDirectory();
        } else {
            if( !dir.mkdirs()) {
                throw new IOException("Can not create directory " + dir.getAbsolutePath()); //NOI18N
            }
        }
        NativeProcessBuilder processBuilder = NativeProcessBuilder.newProcessBuilder(execEnv);
        // TODO: error processing
        processBuilder.setWorkingDirectory(remotePath);
        processBuilder.setCommandLine("ls -1F"); // NOI18N
        processBuilder.redirectError();
        NativeProcess process = processBuilder.call();
        final InputStream is = process.getInputStream();
        final BufferedReader rdr = new BufferedReader(new InputStreamReader(is));
        String fileName;
        while ((fileName = rdr.readLine()) != null) {
            boolean directory = fileName.endsWith("/"); // NOI18N
            File file = new File(dir, fileName);
            boolean result = directory ? file.mkdirs() : file.createNewFile();
            // TODO: error processing
            RemoteUtil.LOGGER.finest("\t" + fileName);
            file.createNewFile(); // TODO: error processing
        }
        rdr.close();
        is.close();
    }
}
