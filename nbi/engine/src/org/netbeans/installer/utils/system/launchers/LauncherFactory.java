/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */


package org.netbeans.installer.utils.system.launchers;

import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.system.launchers.impl.CommandLauncher;
import org.netbeans.installer.utils.system.launchers.impl.ExeLauncher;
import org.netbeans.installer.utils.system.launchers.impl.JarLauncher;
import org.netbeans.installer.utils.system.launchers.impl.ShLauncher;

/**
 *
 * @author Dmitry Lipin 
 */
public class LauncherFactory {
    
    /** Creates a new instance of LauncherFactory */
    private LauncherFactory() {
    }
    public static Launcher newLauncher (LauncherProperties props, Platform platform) {
        Launcher launcher;
        switch (platform) {
            case WINDOWS :
                launcher = new ExeLauncher(props);
                break;
                
            case LINUX :
            case SOLARIS_SPARC :
            case SOLARIS_X86 :
                launcher = new ShLauncher(props);
                break;
            case MACOS_X_PPC:
            case MACOS_X_X86:
                launcher = new CommandLauncher(props);
                break;
                
            default:
                launcher = new JarLauncher(props);
                break;
        }
        return launcher;
    }
}
