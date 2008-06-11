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
 *
 * @author gordonp
 */
public class RemoteNativeExecutionSupport extends RemoteConnectionSupport {
        
    private BufferedReader in;
    private ChannelExec echannel;

    public RemoteNativeExecutionSupport(String host, String user, File dirf, String exe, String args, String[] envp, PrintWriter out) {
        super(host, user);
                
        try {
            setChannelCommand(dirf, exe, args, envp);
            InputStream is = channel.getInputStream();
            in = new BufferedReader(new InputStreamReader(is));
            
            String line;
            while ((line = in.readLine()) != null) {
                out.println(line);
                out.flush();
            }
            in.close();
            is.close();
        } catch (JSchException jse) {
        } catch (IOException ex) {
        }
    }

    @Override
    protected Channel createChannel() throws JSchException {
        echannel = (ChannelExec) session.openChannel("exec");
        return echannel;
    }
    
    private void setChannelCommand(File dirf, String exe, String args, String[] envp) throws JSchException {
        String dircmd;
        String path = RemotePathMap.getMapper(host, user).getPath(dirf.getAbsolutePath());
        
        if (path != null) {
            dircmd = "cd " + path + "; "; // NOI18N
        } else {
            dircmd = "";
        }
        
        // The following code is important! But ChannelExec.setEnv(...) was added after JSch 0.1.24,
        // so it can't be used until we get an updated version of JSch.
//        for (String ev : envp) {
//            int pos = ev.indexOf('=');
//            String var = ev.substring(0, pos);
//            String val = ev.substring(pos + 1);
//            echannel.setEnv(var, val); // not in 0.1.24
//        }
        
        echannel.setCommand(dircmd + exe + " " + args); //NOI18N
        echannel.setInputStream(System.in);
        echannel.setErrStream(System.err);
        echannel.connect();
    }
}
