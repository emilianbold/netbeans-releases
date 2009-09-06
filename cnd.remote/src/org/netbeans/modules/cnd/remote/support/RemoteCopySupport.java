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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author Sergey Grinev
 */
public class RemoteCopySupport extends RemoteConnectionSupport {

    public RemoteCopySupport(ExecutionEnvironment execEnv) {
        super(execEnv);
    }
    
    public static boolean copyFrom(ExecutionEnvironment execEnv, String remoteName, String localName) {
        RemoteCopySupport support = new RemoteCopySupport(execEnv);
        return support.copyFrom(remoteName, localName);
    }

    public boolean copyFrom(String remoteName, String localName) {
        boolean result;
        long time = System.currentTimeMillis();
        Future<Integer> task = CommonTasksSupport.downloadFile(remoteName, executionEnvironment, localName, null);
        try {
            int rc = task.get().intValue();
            result = (rc == 0);
        } catch (InterruptedException ex) {
            result = false;
        } catch (ExecutionException ex) {
            ex.printStackTrace();
            result = false;
        }
        RemoteUtil.LOGGER.finest("Copying: size=" + (new File(localName).length()) + ", file=" + localName + " took " + (System.currentTimeMillis() - time) + " ms");
        return result;
    }

    public static boolean copyTo(ExecutionEnvironment execEnv, String localFile, String remoteFile) {
        RemoteCopySupport support = new RemoteCopySupport(execEnv);
        return support.copyTo(localFile, remoteFile);
    }

    public boolean copyTo(String localFile, String remoteFile) {
        Future<Integer> result = CommonTasksSupport.uploadFile(localFile, executionEnvironment, remoteFile, 0775, null);
        try {
            Integer i = result.get();
            if (i != null) {
                return i.intValue() == 0;
            }
        } catch (InterruptedException ex) {
            // don't report InterruptedException
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    private static int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //          -1
        if (b == 0) {
            return b;
        }

        if (b == -1) {
            return b;
        }

        if (b == 1 || b == 2) {
            StringBuilder sb = new StringBuilder();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            } while (c != '\n');
            if (b == 1) { // error
                RemoteUtil.LOGGER.warning("Error: Invalid value during reading remote string: " + sb.toString());
            }

            if (b == 2) { // fatal error
                RemoteUtil.LOGGER.warning("Fatal error: Invalid value during reading remote string: " + sb.toString());
            }

        }
        return b;
    }
}
