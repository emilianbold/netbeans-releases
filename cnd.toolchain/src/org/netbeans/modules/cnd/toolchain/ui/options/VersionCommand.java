/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.toolchain.ui.options;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.nativeexecution.api.util.LinkSupport;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;

/**
 *
 * @author gordonp
 * @author Alexey Vladykin
 */
/*package-local*/ final class VersionCommand {

    private final Tool tool;
    private String path;
    private boolean alreadyRun;
    private String version;

    /**
     * Creates a new instance of VersionCommand
     */
    public VersionCommand(Tool tool, String path) {
        this.tool = tool;
        this.path = path;
    }

    public String getVersion() {
        if (!alreadyRun) {
            run();
        }
        return version;
    }

    private void run() {
        if (tool.getExecutionEnvironment().isLocal()) {
            // we're dealing with a local toolchain
            path = LinkSupport.resolveWindowsLink(path);
            File file = new File(path);
            if (!file.exists()) {
                alreadyRun = true;
                return;
            }
        }

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(tool.getExecutionEnvironment());
        npb.setExecutable(path);
        npb.setArguments(getVersionFlags());
        npb.redirectError();

        try {
            NativeProcess process = npb.call();
            version = extractVersion(process.getInputStream());

            // version retrieved, now destroy the process
            // (dbx does not exit with -V argument)
            process.destroy();
        } catch (Exception ex) {
            // silently drop
        }
        
        alreadyRun = true;
    }

    private String getVersionFlags() {
        String flags = null;
        if (tool.getDescriptor() != null) {
            flags = tool.getDescriptor().getVersionFlags();
        }
        if (flags == null) {
            return "--version"; // NOI18N
        } else {
            return flags;
        }
    }

    private String extractVersion(InputStream is) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            return br.readLine();
        } catch (IOException ioe) {
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    // silently drop
                }
            }
        }
    }

}
