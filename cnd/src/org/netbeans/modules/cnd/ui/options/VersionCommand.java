/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.ui.options;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author gordonp
 */
public class VersionCommand {
    private ProcessBuilder pb;
    private String version = null;
    private static HashMap<String, String> cygmap;
    
    static {
        cygmap = new HashMap();
        cygmap.put("cc.exe", "gcc.exe"); // NOI18N
        cygmap.put("i686-pc-cygwin-gcc.exe", "gcc.exe"); // NOI18N
        cygmap.put("c++.exe", "g++.exe"); // NOI18N
        cygmap.put("i686-pc-cygwin-g++.exe", "g++.exe"); // NOI18N
        cygmap.put("i686-pc-cygwin-c++.exe", "g++.exe"); // NOI18N
    }
    
    /**
     * Creates a new instance of VersionCommand
     */
    public VersionCommand(CompilerFlavor flavor, String path) {
        String option = null;
        String name = IpeUtils.getBaseName(path);
        
        try {
            path = new File(path).getCanonicalPath();
        } catch (IOException ex) {
        }
        
        if (flavor.isGnuCompiler()) { 
            option = "--version"; // NOI18N
            path = cygwinPath(path);
        } else if (flavor.isSunCompiler()) {
            option = "-V"; // NOI18N
        }
        if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
            if (name.equals("gmake")) { // NOI18N
                option = "--version"; // NOI18N
            } else if (name.equals("gdb")) { // NOI18N
                option = "--version"; // NOI18N
            } else if (name.equals("make")) { // NOI18N
                path = "/sbin/uname"; // NOI18N
                option = "-sr"; // NOI18N
            } else if (name.equals("dmake")) { // NOI18N
                option = "-v"; // NOI18N
            }
        }
        if (option == null) {
            option = "--version"; // NOI18N - Guessing its GNU ...
        }
        
        if (version == null) {
            pb = new ProcessBuilder(path, option);
            pb.redirectErrorStream(true);
            version = run();
        }
    }
    
    public String run() {
        String v = null;
        if (pb != null) {
            try {
                Process process = pb.start();
                BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                v = br.readLine(); // just read 1st line...
                br.close();
            } catch (IOException ioe) {
            }
        }
        return v;
    }
    
    public String getVersion() {
        return version;
    }
    
    /**
     * Replace Cygwin symlinks with what they point to.
     *
     * @param orig The orignal path of a compiler/tool
     * @returns The possibly modifued path of a real file
     */
    private String cygwinPath(String orig) {
        int pos = orig.lastIndexOf(File.separatorChar);
        String dir = orig.substring(0, pos);
        String name = orig.substring(pos + 1);
        String nuename = cygmap.get(name);
        if (nuename != null) {
            return dir + File.separator + nuename;
        } else {
            return orig;
        }
    }
}
