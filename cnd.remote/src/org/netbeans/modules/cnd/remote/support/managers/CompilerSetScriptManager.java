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

package org.netbeans.modules.cnd.remote.support.managers;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.remote.support.RemoteScriptSupport;

/**
 * Manage the getCompilerSets script.
 * 
 * @author gordonp
 */
public class CompilerSetScriptManager implements ScriptManager {
        
    private RemoteScriptSupport support;
    private BufferedReader in;
    private StringWriter out;
    private StringTokenizer st;
    private String platform;
    private static Logger log = Logger.getLogger("cnd.remote.logger"); // NOI18N
    
    public void setSupport(RemoteScriptSupport support) {
        this.support = support;
    }

    private static int emulateFailure = Integer.getInteger("cnd.remote.failure", 0); // NOI18N

    public void runScript() {
        if (!support.isFailedOrCancelled()) {
            ChannelExec channel = (ChannelExec) support.getChannel();
            channel.setInputStream(null);
            channel.setErrStream(System.err);
            
            try {
                channel.connect();
                InputStream is = channel.getInputStream();
                in = new BufferedReader(new InputStreamReader(is));
                out = new StringWriter();

                if (emulateFailure>0) {
                    log.warning("CSSM.runScript: failure emulation [" + emulateFailure + "]"); // NOI18N
                    support.setFailed("failure emulation in CompilerSetScriptManager"); // NOI18N
                    emulateFailure--;
                    return;
                }


                String line;
                platform = in.readLine();
                log.fine("CSSM.runScript: Reading input from getCompilerSets.bash");
                log.fine("    platform [" + platform + "]");
                while ((line = in.readLine()) != null) {
                    log.fine("    line [" + line + "]");
                    out.write(line + '\n');
                    out.flush();
                }
                in.close();
                is.close();
                st = new StringTokenizer(out.toString());
            } catch (IOException ex) {
                log.warning("CSSM.runScript: IOException [" + ex.getMessage() + "]") ; // NOI18N
                support.setFailed(ex.getMessage());
            } catch (JSchException ex) {
                log.warning("CSSM.runScript: JSchException"); // NOI18N
                support.setFailed(ex.getMessage());
            } finally {
                support.disconnect();
            }
        }
    }

    public static final String SCRIPT = ".netbeans/6.5/cnd2/scripts/getCompilerSets.bash"; // NOI18N

    public String getScript() {
        return SCRIPT;
    }
    
    public String getPlatform() {
        return platform;
    }

    public boolean hasMoreCompilerSets() {
        return st != null && st.hasMoreTokens();
    }

    public String getNextCompilerSetData() {
        String compilerSetInfo = st.nextToken();
        return compilerSetInfo;
    }
    
    @Override
    public String toString() {
        if (out != null) {
            return out.toString();
        } else {
            return "";
        }
    }
}
