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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.api;

import org.netbeans.modules.cnd.makeproject.MakeOptions;

public class MakeProjectOptions {

    public static void setDefaultMakeCommand(String defaultMakeCommand) {
        MakeOptions.setDefaultMakeCommand(defaultMakeCommand);
    }

    public static String getDefaultMakeCommand() {
        return MakeOptions.getDefaultMakeCommand();
    }

    public static void setDefaultCompilerSet(int compilerSet) {
        MakeOptions.getInstance().setCompilerSet(compilerSet);
    }

    public static int getDefaultCompilerSet() {
        return MakeOptions.getInstance().getCompilerSet();
    }
    
    public static void setDefaultPlatform(int platform) {
        MakeOptions.getInstance().setPlatform(platform);
    }
    
    public static int getDefaultPlatform() {
        return MakeOptions.getInstance().getPlatform();
    }
    
    public static void setFortranSupport(boolean fortran) {
        MakeOptions.getInstance().setFortran(fortran);
    }
    
    public static boolean getFortranSupport() {
        return MakeOptions.getInstance().getFortran();
    }
    
    public static void setDepencyChecking(boolean val) {
        MakeOptions.getInstance().setDepencyChecking(val);
    }
    
    public static boolean getDepencyChecking() {
        return MakeOptions.getInstance().getDepencyChecking();
    }
}
