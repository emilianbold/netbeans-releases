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

package org.netbeans.modules.cnd.makeproject.api;

import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.settings.CppSettings;

public class MakeProjectOptions {

    /**
     * @Deprecated
     */
    public static void setDefaultMakeCommand(String defaultMakeCommand) {
        //CppSettings.getDefault().setMakeName(defaultMakeCommand);
    }

    /**
     * @Deprecated
     */
    public static String getDefaultMakeCommand() {
        return null; //CppSettings.getDefault().getMakeName();
    }
    
    /**
     * @Deprecated
     */
    public static void setDefaultCompilerSet(String name) {
        // Set the default name in global setting
        CppSettings.getDefault().setCompilerSetName(name);
        // Also set the default compiler set in the localhost set. Remote sets will look at the setting in CppSettings.
        CompilerSetManager compilerSetManager = CompilerSetManager.getDefault(ExecutionEnvironmentFactory.getLocal());
        CompilerSet compilerSet = compilerSetManager.getCompilerSet(name);
        if (compilerSet != null) {
            compilerSetManager.setDefault(compilerSet);
        }
//        CompilerSet cs = CompilerSetManager.getDefault(CompilerSetManager.LOCALHOST).getCompilerSet(name);
//        if (cs != null) {
//            CppSettings.getDefault().setCompilerSetName(cs.getName());
////            CppSettings.getDefault().setCompilerSetDirectories(cs.getDirectory());
//        } else {
//            cs = CompilerSetManager.getDefault(CompilerSetManager.LOCALHOST).getCompilerSet(0); // use 0th as default
//        }
    }

    /**
     * @Deprecated
     */
    public static void setDefaultMakeOptions(String defaultMakeOptions) {
        MakeOptions.setDefaultMakeOptions(defaultMakeOptions);
    }

    /**
     * @Deprecated
     */
    public static String getDefaultMakeOptions() {
        return MakeOptions.getDefaultMakeOptions();
    }


    /**
     * @Deprecated
     */
//    public static void setDefaultPlatform(int platform) {
//        MakeOptions.getInstance().setPlatform(platform);
//    }

    /**
     * @Deprecated
     */
//    public static int getDefaultPlatform() {
//        return MakeOptions.getInstance().getPlatform();
//    }

    /**
     * @Deprecated
     */
    public static void setFortranSupport(boolean fortran) {
        //CppSettings.getDefault().setFortranEnabled(fortran);
    }

    /**
     * @Deprecated
     */
    public static boolean getFortranSupport() {
        return true;
        //return CppSettings.getDefault().isFortranEnabled();
    }
    
    public static void setDepencyChecking(boolean val) {
        MakeOptions.getInstance().setDepencyChecking(val);
    }
    
    public static boolean getDepencyChecking() {
        return MakeOptions.getInstance().getDepencyChecking();
    }
}
