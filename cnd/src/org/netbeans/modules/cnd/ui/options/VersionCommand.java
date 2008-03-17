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
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author gordonp
 */
public class VersionCommand implements Runnable {
    
    private ProcessBuilder pb;
    private String name;
    private String path;
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
    public VersionCommand(CompilerFlavor flavor, String name, String path) {
        String option = null;
        
        this.name = name;
        this.path = path;
        try {
            path = new File(path).getCanonicalPath();
        } catch (IOException ex) {
        }
        
        if (flavor.isGnuCompiler()) { 
            option = "--version"; // NOI18N
            path = cygwinPath(path);
        } else if (flavor.isSunCompiler()) {
            option = "-V"; // NOI18N
        } else if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
            if (path.endsWith("/sfw/bin/gmake")) { // NOI18N
                option = "--version"; // NOI18N
            } else if (path.equals("/usr/ccs/bin/make") || path.equals("/usr/xpg4/bin/make")) { // NOI18N
                path = "/sbin/uname"; // NOI18N
                option = "-sr"; // NOI18N
            } else if (name.equals("dmake")) { // NOI18N
                File inv;
                String base = path.substring(0, path.length() - 10);
                
                inv = new File(base + "/inventory"); // NOI18N
                if (inv.exists() && inv.isDirectory()) {
                    String[] files = inv.list();
                    for (int i = 0; i < files.length; i++) {
                        if (files[i].equals("v14n1")) { // NOI18N
                            version = NbBundle.getMessage(VersionCommand.class, "DMAKE10"); // NOI18N
                        } else if (files[i].equals("v15n1")) { // NOI18N
                            version = NbBundle.getMessage(VersionCommand.class, "DMAKE11"); // NOI18N
                        } else if (files[i].equals("v16n1")) { // NOI18N
                            version = NbBundle.getMessage(VersionCommand.class, "DMAKE12"); // NOI18N
                        }
                    }
                }
            }
        }
        if (option == null) {
            option = "--version"; // NOI18N - Guessing its GNU ...
        }
        
        if (version == null) {
            pb = new ProcessBuilder(path, option);
            pb.redirectErrorStream(true);
        }
    }
    
    public void run() {
        
        if (pb != null) {
            try {
                Process process = pb.start();
                BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                version = br.readLine(); // just read 1st line...
                br.close();
            } catch (IOException ioe) {
            }
        }
        if (version != null) {
            String message = NbBundle.getMessage(VersionCommand.class, "LBL_VersionInfo", name, path, version);
            NotifyDescriptor nd = new NotifyDescriptor.Message(message);
            
            nd.setTitle(NbBundle.getMessage(VersionCommand.class, "LBL_VersionInfo_Title"));
            DialogDisplayer.getDefault().notify(nd);
        }
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
