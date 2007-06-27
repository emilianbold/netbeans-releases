/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd;

import java.io.File;
import java.io.IOException;
import org.openide.util.Utilities;
import org.netbeans.editor.Settings;
import org.netbeans.modules.cnd.builds.OutputWindowOutputStream;
import org.netbeans.modules.cnd.editor.cplusplus.CCKit;
import org.netbeans.modules.cnd.editor.cplusplus.CCPrintOptions;
import org.netbeans.modules.cnd.editor.cplusplus.CCSettingsInitializer;
import org.netbeans.modules.cnd.editor.cplusplus.CKit;
import org.netbeans.modules.cnd.editor.fortran.FKit;
import org.netbeans.modules.cnd.editor.fortran.FPrintOptions;
import org.netbeans.modules.cnd.editor.fortran.FSettingsInitializer;
import org.netbeans.modules.cnd.editor.makefile.MakefileKit;
import org.netbeans.modules.cnd.editor.makefile.MakefilePrintOptions;
import org.netbeans.modules.cnd.editor.makefile.MakefileSettingsInitializer;
import org.netbeans.modules.cnd.editor.shell.ShellKit;
import org.netbeans.modules.cnd.editor.shell.ShellPrintOptions;
import org.netbeans.modules.cnd.editor.shell.ShellSettingsInitializer;
import org.openide.ErrorManager;
import org.openide.modules.ModuleInstall;
import org.openide.modules.InstalledFileLocator;
import org.openide.options.SystemOption;
import org.openide.text.PrintSettings;

public class CndModule extends ModuleInstall {

    // Used in other CND sources...
    public static final ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.cnd"); // NOI18N

    @Override public void uninstalled() {
        OutputWindowOutputStream.detachAllAnnotations();

        // Print Options
        PrintSettings ps = (PrintSettings) PrintSettings.findObject(PrintSettings.class, true);
	ps.removeOption((SystemOption)SystemOption.findObject(FPrintOptions.class, true));
       	ps.removeOption((SystemOption)SystemOption.findObject(CCPrintOptions.class, true));
	ps.removeOption((SystemOption)SystemOption.findObject(MakefilePrintOptions.class, true));
	ps.removeOption((SystemOption)SystemOption.findObject(ShellPrintOptions.class, true));
    }

    /** Module is being opened (NetBeans startup, or enable-toggled) */
    @Override public void restored() {

	// Settings for editor kits
        Settings.addInitializer(new CCSettingsInitializer(CCKit.class));
	Settings.addInitializer(new CCSettingsInitializer(CKit.class));
	Settings.addInitializer(new FSettingsInitializer(FKit.class));
	Settings.addInitializer(new MakefileSettingsInitializer(MakefileKit.class));
	Settings.addInitializer(new ShellSettingsInitializer(ShellKit.class));
	
	PrintSettings ps = (PrintSettings) PrintSettings.findObject(PrintSettings.class, true);
	ps.addOption ((SystemOption) SystemOption.findObject(FPrintOptions.class, true));
	ps.addOption ((SystemOption) SystemOption.findObject(CCPrintOptions.class, true));
	ps.addOption ((SystemOption) SystemOption.findObject(MakefilePrintOptions.class, true));
	ps.addOption ((SystemOption) SystemOption.findObject(ShellPrintOptions.class, true));
        
        if (Utilities.isUnix()) {
            setExecutionPermission("bin/dorun.sh");
            setExecutionPermission("bin/stdouterr.sh");
        }
    }
    
    private void setExecutionPermission(String relpath) {
        File file = InstalledFileLocator.getDefault().locate(relpath, null, false);
        if (file.exists()) {
            ProcessBuilder pb = new ProcessBuilder("/usr/bin/chmod", "755", file.getAbsolutePath()); // NOI18N
            try {
                pb.start();
            } catch (IOException ex) {
            }
        }
    }
}
