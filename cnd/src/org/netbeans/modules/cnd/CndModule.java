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

package org.netbeans.modules.cnd;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.editor.Settings;
import org.netbeans.modules.cnd.editor.shell.ShellKit;
import org.netbeans.modules.cnd.editor.shell.ShellSettingsInitializer;
import org.openide.ErrorManager;
import org.openide.modules.ModuleInstall;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Utilities;

public class CndModule extends ModuleInstall {

    // Used in other CND sources...
    public static final ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.cnd"); // NOI18N

    /** Module is being opened (NetBeans startup, or enable-toggled) */
    @Override public void restored() {

	// Settings for editor kits
	Settings.addInitializer(new ShellSettingsInitializer(ShellKit.class));
	
//	PrintSettings ps = (PrintSettings) PrintSettings.findObject(PrintSettings.class, true);
//	ps.addOption ((SystemOption) SystemOption.findObject(FPrintOptions.class, true));
//	ps.addOption ((SystemOption) SystemOption.findObject(CCPrintOptions.class, true));
//	ps.addOption ((SystemOption) SystemOption.findObject(MakefilePrintOptions.class, true));
//	ps.addOption ((SystemOption) SystemOption.findObject(ShellPrintOptions.class, true));
        
        if (Utilities.isUnix()) {
            // TODO: why not set permissions for bin/* ?
            List<String> files = new ArrayList<String>();
            addFile(files, "bin/dorun.sh"); // NOI18N
            //addFile(files, "bin/stdouterr.sh"); // NOI18N
            if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
                if (System.getProperty("os.arch").equals("sparc")) { // NOI18N
                    //addFile(files, "bin/GdbHelper-SunOS-sparc.so"); // NOI18N
                    addFile(files, "bin/unbuffer-SunOS-sparc.so"); // NOI18N
                    addFile(files, "bin/unbuffer-SunOS-sparc_64.so"); // NOI18N
                } else {
                    //addFile(files, "bin/GdbHelper-SunOS-x86.so"); // NOI18N
                    addFile(files, "bin/unbuffer-SunOS-x86.so"); // NOI18N
                    addFile(files, "bin/unbuffer-SunOS-x86_64.so"); // NOI18N
                }
            } else if (Utilities.getOperatingSystem() == Utilities.OS_LINUX) {
                //addFile(files, "bin/GdbHelper-Linux-x86.so"); // NOI18N
                addFile(files, "bin/unbuffer-Linux-x86.so"); // NOI18N
                addFile(files, "bin/unbuffer-Linux-x86_64.so"); // NOI18N
            } else if (Utilities.isMac()) {
                //addFile(files, "bin/GdbHelper-Mac_OS_X-x86.dylib"); // NOI18N
                addFile(files, "bin/unbuffer-Mac_OS_X-x86.dylib"); // NOI18N
                addFile(files, "bin/unbuffer-Mac_OS_X-x86_64.dylib"); // NOI18N
            }
            setPermissions(files);
        }
    }

    private static void addFile(List<String> files, String relpath) {
        File file = InstalledFileLocator.getDefault().locate(relpath, null, false);
        if (file != null && file.exists()) {
            files.add(file.getAbsolutePath());
        }
    }

    private static void setPermissions(List<String> files) {
        if (files.isEmpty()) {
            return;
        }
        List<String> commands = new ArrayList<String>();
        commands.add("/bin/chmod"); // NOI18N
        commands.add("755"); // NOI18N
        commands.addAll(files);
        ProcessBuilder pb = new ProcessBuilder(commands);
        try {
            pb.start();
        } catch (IOException ex) {
        }
    }
}
