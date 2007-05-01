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

package org.netbeans.modules.cnd.makeproject.api;

import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.settings.CppSettings;

public class MakeProjectOptions {

    public static void setDefaultMakeCommand(String defaultMakeCommand) {
        CppSettings.getDefault().setMakeName(defaultMakeCommand);
    }

    public static String getDefaultMakeCommand() {
        return CppSettings.getDefault().getMakeName();
    }
    
    /**
     * Choose either Sun or GNU compiler sets. Unfortunately, we no longer guarantee either
     * exists. In CND 5.5, you had a Sun and GNU compiler set regardless of whether you had
     * compilers to make either set usable. In CND 5.5.1, a compiler set is defined for every
     * directory which has executables recognized as compilers.
     * 
     * @deprecated
     */
    public static void setDefaultCompilerSet(int compilerSet) {
        CompilerSet cs = null;
        
        if (compilerSet == 0) {
            cs = CompilerSetManager.getDefault().getCompilerSet("Sun"); // NOI18N
        } else if (compilerSet == 1) {
            cs = CompilerSetManager.getDefault().getCompilerSet("GNU"); // NOI18N
        }
        if (cs != null) {
            CppSettings.getDefault().setCompilerSetName(cs.getName());
            CppSettings.getDefault().setCompilerSetDirectories(cs.getDirectory());
        } else {
            cs = CompilerSetManager.getDefault().getCompilerSet(0); // use 0th as default
        }
    }
    
    /**
     * Choose either Sun or GNU compiler sets. Unfortunately, we no longer guarantee either
     * exists. In CND 5.5, you had a Sun and GNU compiler set regardless of whether you had
     * compilers to make either set usable. In CND 5.5.1, a compiler set is defined for every
     * directory which has executables recognized as compilers.
     */
    public static void setDefaultCompilerSet(String name) {
        CompilerSet cs = CompilerSetManager.getDefault().getCompilerSet(name);
        if (cs != null) {
            CppSettings.getDefault().setCompilerSetName(cs.getName());
            CppSettings.getDefault().setCompilerSetDirectories(cs.getDirectory());
        } else {
            cs = CompilerSetManager.getDefault().getCompilerSet(0); // use 0th as default
        }
    }

    /**
     * Return a default compiler set index. Note that this index is only valid if the user
     * doesn't modify their path INSIDE THE IDE. Also, if there are no compiler sets in the
     * user's path, the return is 0 and the results are somewhat undefined.
     *
     * @returns index of the current default compiler set
     *
     * @deprecated
     */
    public static int getDefaultCompilerSet() {
        CompilerSet cs = CompilerSetManager.getDefault().getCompilerSet(CppSettings.getDefault().getCompilerSetName());
        if (cs != null) {
            int i = 0;
            for (CompilerSet cs2 : CompilerSetManager.getDefault().getCompilerSets()) {
                if (cs2 == cs) {
                    return i;
                } else {
                    i++;
                }
            }
        }
        return 0;
    }

    public static void setDefaultMakeOptions(String defaultMakeOptions) {
        MakeOptions.setDefaultMakeOptions(defaultMakeOptions);
    }

    public static String getDefaultMakeOptions() {
        return MakeOptions.getDefaultMakeOptions();
    }

    public static void setDefaultCompilerSetName(String compilerSetName) {
        CppSettings.getDefault().setCompilerSetName(compilerSetName);
    }

    public static String getDefaultCompilerSetName() {
        return CppSettings.getDefault().getCompilerSetName();
    }

    public static void setDefaultCompilerSetDirectories(String compilerSetDirectories) {
        CppSettings.getDefault().setCompilerSetDirectories(compilerSetDirectories);
    }

    public static String getDefaultCompilerSetDirectories() {
        return CppSettings.getDefault().getCompilerSetDirectories();
    }
    
    public static void setDefaultPlatform(int platform) {
        MakeOptions.getInstance().setPlatform(platform);
    }
    
    public static int getDefaultPlatform() {
        return MakeOptions.getInstance().getPlatform();
    }
    
    public static void setFortranSupport(boolean fortran) {
        CppSettings.getDefault().setFortranEnabled(fortran);
    }
    
    public static boolean getFortranSupport() {
        return CppSettings.getDefault().isFortranEnabled();
    }
    
    public static void setDepencyChecking(boolean val) {
        MakeOptions.getInstance().setDepencyChecking(val);
    }
    
    public static boolean getDepencyChecking() {
        return MakeOptions.getInstance().getDepencyChecking();
    }
}
