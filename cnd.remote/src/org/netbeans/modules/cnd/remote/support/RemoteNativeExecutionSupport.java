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

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;

/**
 * This support is intended to work with RemoteNativeExecution and provide input (and eventually
 * output) for project actions.
 * 
 * @author gordonp
 */
public class RemoteNativeExecutionSupport extends RemoteConnectionSupport {
        
    public RemoteNativeExecutionSupport(String key, int port, File dirf, String exe, String args, String[] envp, PrintWriter out) {
        super(key, port);
        
        log.fine("RNES<Init>: Running [" + exe + "] on " + key);   
        try {
            setChannelCommand(dirf, exe, args, envp);
            InputStream is = channel.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(is)); // XXX - Change to non-buffered input
            channel.connect();
            
//            String line;
//            while ((line = in.readLine()) != null) { // XXX - Change to character oriented input
//                out.println(line);
//                out.flush();
//            }
//            in.close();
//            is.close();
    
            String line;
            while ((line = in.readLine()) != null || !channel.isClosed()) {
                if (line!=null) {
                    out.write(line + "\n"); // NOI18N
                    out.flush();
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
            }
            is.close();
            in.close();

        } catch (JSchException jse) {
        } catch (IOException ex) {
        } finally {
            disconnect();
        } 
    }

    public RemoteNativeExecutionSupport(String key, File dirf, String exe, String args, String[] envp, PrintWriter out) {
        this(key, 22, dirf, exe, args, envp, out);
    }

    @Override
    protected Channel createChannel() throws JSchException {
        return session.openChannel("exec"); // NOI18N
    }
    
    private void setChannelCommand(File dirf, String exe, String args, String[] envp) throws JSchException {
        String dircmd;
        String path = RemotePathMap.getMapper(key).getRemotePath(dirf.getAbsolutePath());
        
        if (path != null) {
            dircmd = "cd " + path + "; "; // NOI18N
        } else {
            dircmd = "";
        }
        
        String cmdline = dircmd + exe + " " + args + " 2>&1"; // NOI18N

        for (String ev : envp) {
            int pos = ev.indexOf('=');
            String var = ev.substring(0, pos);
            String val = ev.substring(pos + 1);
            // The following code is important! But ChannelExec.setEnv(...) was added after JSch 0.1.24,
            // so it can't be used until we get an updated version of JSch.
            //echannel.setEnv(var, val); // not in 0.1.24
            
            //as a workaround
            cmdline = "export " + var + "=" + val + ";" + cmdline; // NOI18N
            //cmdline = "export PATH=/usr/bin:/usr/sfw/bin/;" + cmdline;
        }
        
        channel = createChannel();
        ((ChannelExec)channel).setCommand(cmdline.replace('\\', '/'));
        //channel.setInputStream(System.in);
        ((ChannelExec)channel).setErrStream(System.err);
    }
}
