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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author Sergey Grinev
 */
public class RemoteCopySupport extends RemoteConnectionSupport {

    private static final Logger LOG = Logger.getLogger("cnd.remote.logger"); // NOI18N
    
    public RemoteCopySupport(ExecutionEnvironment execEnv) {
        super(execEnv);
    }
    
    public static boolean copyFrom(ExecutionEnvironment execEnv, String remoteName, String localName) {
        RemoteCopySupport support = new RemoteCopySupport(execEnv);
        return support.copyFrom(remoteName, localName);
    }

    public boolean copyFrom(String remoteName, String localName) {
        FileOutputStream fos = null;
        try {
            String prefix = null;
            if (new File(localName).isDirectory()) {
                prefix = localName + File.separator;
            }

            // exec 'scp -f rfile' remotely
            NativeProcessBuilder pb = NativeProcessBuilder.newProcessBuilder(executionEnvironment);
            pb.setExecutable("scp"); // NOI18N
            pb.setArguments("-f", remoteName); //NOI18N
            
            Process process = pb.call();

//            Channel channel = session.openChannel("exec");
//            ((ChannelExec) channel).setCommand(command);

            // get I/O streams for remote scp
            OutputStream out = process.getOutputStream();
            InputStream in = process.getInputStream();

            byte[] buf = new byte[1024];

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();

            while (true) {
                long start = System.currentTimeMillis();

                int c = checkAck(in);
                if (c != 'C') {
                    break;
                }

                // read '0644 '
                in.read(buf, 0, 5);

                long filesize = 0L;
                while (true) {
                    if (in.read(buf, 0, 1) < 0) {
                        // error
                        break;
                    }
                    if (buf[0] == ' ') {
                        break;
                    }
                    filesize = filesize * 10L + (long) (buf[0] - '0');
                }

                String file = null;
                for (int i = 0;; i++) {
                    in.read(buf, i, 1);
                    if (buf[i] == (byte) 0x0a) {
                        file = new String(buf, 0, i);
                        break;
                    }
                }

                // send '\0'
                buf[0] = 0;
                out.write(buf, 0, 1);
                out.flush();

                // read a content of lfile
                fos = new FileOutputStream(prefix == null ? localName : prefix + file);
                int foo;
                while (true) {
                    if (buf.length < filesize) {
                        foo = buf.length;
                    } else {
                        foo = (int) filesize;
                    }
                    foo = in.read(buf, 0, foo);
                    if (foo < 0) {
                        // error 
                        break;
                    }
                    fos.write(buf, 0, foo);
                    filesize -= foo;
                    if (filesize == 0L) {
                        break;
                    }
                }
                fos.close();
                fos = null;

                if (checkAck(in) != 0) {
                    return false;
                }

                // send '\0'
                buf[0] = 0;
                out.write(buf, 0, 1);
                out.flush();

                LOG.finest("Copying: filesize=" + filesize + "b, file=" + file + " took " + (System.currentTimeMillis() - start) + " ms");
            }
            setExitStatus(process.waitFor());

        } catch (InterruptedIOException e) {
            // don't log, this just mean that somebody has interrupted us
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception ee) {
            }
        }
        return getExitStatus() == 0;
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
            Exceptions.printStackTrace(ex);
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
                LOG.warning("Error: Invalid value during reading remote string: " + sb.toString());
            }

            if (b == 2) { // fatal error
                LOG.warning("Fatal error: Invalid value during reading remote string: " + sb.toString());
            }

        }
        return b;
    }
}
