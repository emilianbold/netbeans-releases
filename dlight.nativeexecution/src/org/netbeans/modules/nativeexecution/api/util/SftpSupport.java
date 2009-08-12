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
package org.netbeans.modules.nativeexecution.api.util;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import org.netbeans.modules.nativeexecution.ConnectionManagerAccessor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.openide.util.RequestProcessor;

/**
 * @author Vladimir Kvashin
 */
class SftpSupport {

    //
    // Static stuff
    //

    private static final  java.util.logging.Logger LOG = Logger.getInstance();
    private static final Object instancesLock = new Object();
    private static Map<ExecutionEnvironment, SftpSupport> instances = new HashMap<ExecutionEnvironment, SftpSupport>();

    private static SftpSupport getInstance(ExecutionEnvironment execEnv) {
        SftpSupport instance = null;
        synchronized (instancesLock) {
            instance = instances.get(execEnv);
            if (instance == null) {
                instance = new SftpSupport(execEnv);
                instances.put(execEnv, instance);
            }
        }
        return instance;
    }

    static Future<Integer> uploadFile(
            final String srcFileName,
            final ExecutionEnvironment execEnv,
            final String dstFileName,
            final int mask, final Writer error) {
            return getInstance(execEnv).uploadFile(srcFileName, dstFileName, mask, error);
    }

    //
    // Instance stuff
    //

    private final ExecutionEnvironment execEnv;    
    private final RequestProcessor requestProcessor;

    // its's ok to hav a single one since we have only single-threaded request processor
    private ChannelSftp channel;
    private final Object channelLock = new Object();

    private SftpSupport(ExecutionEnvironment execEnv) {
        this.execEnv = execEnv;
        // we've got some sftp issues => only 1 task at a moment
        requestProcessor = new RequestProcessor("SFTP request processor for " + execEnv, 1);
    }


    private ChannelSftp getChannel() throws IOException, CancellationException, JSchException {
        synchronized (channelLock) {
            if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
                channel = null;
                ConnectionManager.getInstance().connectTo(execEnv);
            }
            if (channel != null && !channel.isConnected()) {
                channel = null;
            }
            if (channel == null) {
                Session session =
                    ConnectionManagerAccessor.getDefault().getConnectionSession(
                    ConnectionManager.getInstance(), execEnv, true);
                channel = (ChannelSftp) session.openChannel("sftp");
                channel.connect();
            }
        }
        return channel;
    }

    private class Uploader implements Callable<Integer> {

        private final String srcFileName;
        private final ExecutionEnvironment execEnv;
        private final String dstFileName;
        private final int mask;
        private final Writer error;

        public Uploader(String srcFileName, ExecutionEnvironment execEnv, String dstFileName, int mask, Writer error) {
            this.srcFileName = srcFileName;
            this.execEnv = execEnv;
            this.dstFileName = dstFileName;
            this.mask = mask;
            this.error = error;
        }

        public Integer call() throws Exception {
            int rc = -1;
            try {
                LOG.fine("Uploading " + srcFileName + " to " + execEnv + ":" + dstFileName + " started");
                ChannelSftp cftp = getChannel();
                cftp.put(srcFileName, dstFileName);
                cftp.chmod(mask, dstFileName);
                rc = 0;
            } catch (JSchException ex) {
                ex.printStackTrace();
                rc = 1;
            } catch (SftpException ex) {
                ex.printStackTrace();
                rc = 2;
            } catch(IOException ex) {
                ex.printStackTrace();
                rc = 3;
            } catch (CancellationException ex) {
                ex.printStackTrace();
                rc = 4;
            }
            LOG.fine("Uploading " + srcFileName + " to " + execEnv + ":" + dstFileName + (rc == 0 ? " OK" : " FAILED"));
            return rc;
        }

    }

    private Future<Integer> uploadFile(
            final String srcFileName,
            final String dstFileName,
            final int mask, final Writer error) {

            Uploader uploader = new Uploader(srcFileName, execEnv, dstFileName, mask, error);
            FutureTask<Integer> ftask = new FutureTask(uploader);
            requestProcessor.post(ftask);
            LOG.fine("Uploading " + srcFileName + " to " + execEnv + ":" + dstFileName + " schedulled");
            return ftask;
    }

}
