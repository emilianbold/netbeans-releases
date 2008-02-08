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
import org.netbeans.editor.Settings;
import org.netbeans.modules.cnd.builds.OutputWindowOutputStream;
import org.netbeans.modules.cnd.editor.fortran.FKit;
import org.netbeans.modules.cnd.editor.fortran.FSettingsInitializer;
import org.netbeans.modules.cnd.editor.makefile.MakefileKit;
import org.netbeans.modules.cnd.editor.makefile.MakefileSettingsInitializer;
import org.netbeans.modules.cnd.editor.shell.ShellKit;
import org.netbeans.modules.cnd.editor.shell.ShellSettingsInitializer;
import org.openide.ErrorManager;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInstall;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Utilities;

public class CndModule extends ModuleInstall {

    // Used in other CND sources...
    public static final ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.cnd"); // NOI18N

    @Override public void uninstalled() {
        OutputWindowOutputStream.detachAllAnnotations();

        // Print Options
//        PrintSettings ps = (PrintSettings) PrintSettings.findObject(PrintSettings.class, true);
//	ps.removeOption((SystemOption)SystemOption.findObject(FPrintOptions.class, true));
//      ps.removeOption((SystemOption)SystemOption.findObject(CCPrintOptions.class, true));
//	ps.removeOption((SystemOption)SystemOption.findObject(MakefilePrintOptions.class, true));
//	ps.removeOption((SystemOption)SystemOption.findObject(ShellPrintOptions.class, true));
    }

    /** Module is being opened (NetBeans startup, or enable-toggled) */
    @Override public void restored() {

	// Settings for editor kits
	Settings.addInitializer(new FSettingsInitializer(FKit.class));
	Settings.addInitializer(new MakefileSettingsInitializer(MakefileKit.class));
	Settings.addInitializer(new ShellSettingsInitializer(ShellKit.class));
	
//	PrintSettings ps = (PrintSettings) PrintSettings.findObject(PrintSettings.class, true);
//	ps.addOption ((SystemOption) SystemOption.findObject(FPrintOptions.class, true));
//	ps.addOption ((SystemOption) SystemOption.findObject(CCPrintOptions.class, true));
//	ps.addOption ((SystemOption) SystemOption.findObject(MakefilePrintOptions.class, true));
//	ps.addOption ((SystemOption) SystemOption.findObject(ShellPrintOptions.class, true));
        
        if (Utilities.isUnix()) {
            setExecutionPermission("bin/dorun.sh"); // NOI18N
            setExecutionPermission("bin/stdouterr.sh"); // NOI18N
        }
    }
    
    private void setExecutionPermission(String relpath) {
        File file = InstalledFileLocator.getDefault().locate(relpath, null, false);
        if (file.exists()) {
            ProcessBuilder pb = new ProcessBuilder("/bin/chmod", "755", file.getAbsolutePath()); // NOI18N
            try {
                pb.start();
            } catch (IOException ex) {
            }
        }
    }
}
