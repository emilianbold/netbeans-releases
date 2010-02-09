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

package org.netbeans.modules.cnd.remote.mapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.Exceptions;

/**
 *
 * @author Sergey Grinev
 */
public class HostMappingProviderWindows implements HostMappingProvider {

    public Map<String, String> findMappings(ExecutionEnvironment execEnv, ExecutionEnvironment otherExecEnv) {
        Map<String, String> mappings = null;
        try {
            Process process = Runtime.getRuntime().exec("net use"); //NOI18N
            InputStream output = process.getInputStream();
            mappings = parseNetUseOutput(otherExecEnv.getHost(), new InputStreamReader(output));
            return mappings;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<String, String>emptyMap();
    }

    public boolean isApplicable(PlatformInfo hostPlatform, PlatformInfo otherPlatform) {
        return PlatformTypes.PLATFORM_WINDOWS == hostPlatform.getPlatform()
                && hostPlatform.isLocalhost(); // Windows is only supported as client platform
    }

    /**
     * Parses "net use" Windows command output.
     * Here is an example of the output (note that "\\" means "\")
     *
     * ----- output example start -----
     *      New connections will not be remembered.
     *
     *
     *      Status       Local     Remote                               Network
     *
     *      -------------------------------------------------------------------------------
     *      OK           P:        \\\\serverOne\\pub                     Microsoft Windows Network
     *      Disconnected Y:        \\\\sErvEr_22_\\long name              Microsoft Windows Network
     *      OK           Z:        \\\\name.domen.domen2.zone\\username   Microsoft Windows Network
     *      The command completed successfully.
     *
     * ----- output example end -----
     *
     * @param hostName
     * @param outputReader
     * @return
     * @throws java.io.IOException
     */
    @SuppressWarnings("empty-statement")
    /* package */ static Map<String, String> parseNetUseOutput(String hostName, Reader outputReader) throws IOException {
        Map<String, String> mappings = new HashMap<String, String>();
        BufferedReader reader = new BufferedReader(outputReader);
        String line;
        // firtst, find the "---------" line and remember "Status  Local  Remote Network" one
        String lastNonEmptyLine = null;
        for( line = reader.readLine(); line != null && !line.contains("----------------"); line = reader.readLine()) { //NOI18N
            if (line.length() > 0) {
                lastNonEmptyLine = line;
            }
        }
        // we found "----";
        if (lastNonEmptyLine == null) {
            return Collections.<String, String>emptyMap();
        }

        // lastNonEmptyLine should contain "Status  Local  Remote Network" - probably localized
        String[] words = lastNonEmptyLine.split("[ \t]+"); // NOI18N
        if (words.length < 4) {
            return Collections.<String, String>emptyMap();
        }

        int nLocal = lastNonEmptyLine.indexOf(words[1]); // "Local"
        int nRemote = lastNonEmptyLine.indexOf(words[2]); // "Remote"
        int nNetwork = lastNonEmptyLine.indexOf(words[3]); // "Network"
        // neither of nLocal, nRemote and nNetwork can be negative - no check need
        
        for( line = reader.readLine(); line != null; line = reader.readLine() ) {  //NOI18N
            if (line.indexOf(':') != -1) {
                String local = line.substring(nLocal, nRemote -1).trim(); // something like X:
                String remote = line.substring(nRemote, nNetwork -1).trim(); // something like \\hostname\foldername
                if (remote.length() > 2) {
                    String[] arRemote = remote.substring(2).split("\\\\"); //NOI18N
                    if (arRemote.length >=2) {
                        String host = arRemote[0];
                        String folder = arRemote[1];
                        if (hostName.equals(host)) {
                            mappings.put(folder, local.toLowerCase());
                        }
                    }
                }
            }
        }

        return mappings;
    }
}
