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

package org.netbeans.modules.cnd.makeproject.platform;

import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem;

public class PlatformLinux extends Platform {
    public static final String NAME = "Linux-x86"; // NOI18N

    private static final LibraryItem.StdLibItem[] standardLibrariesLinux = {
        StdLibraries.getStandardLibary("Motif"), // NOI18N
        StdLibraries.getStandardLibary("Mathematics"), // NOI18N
        StdLibraries.getStandardLibary("DataCompression"), // NOI18N
        StdLibraries.getStandardLibary("PosixThreads"), // NOI18N
        StdLibraries.getStandardLibary("Curses"), // NOI18N
        StdLibraries.getStandardLibary("DynamicLinking"), // NOI18N
    };
    
    public PlatformLinux() {
        super(NAME, "Linux x86", PlatformTypes.PLATFORM_LINUX); // NOI18N
    }
    
    @Override
    public LibraryItem.StdLibItem[] getStandardLibraries() {
        return standardLibrariesLinux;
    }
    
    @Override
    public String getLibraryName(String baseName) {
        return "lib" + baseName + ".so"; // NOI18N // NOI18N
    }
    
    @Override
    public String getLibraryLinkOption(String libName, String libDir, String libPath, CompilerSet compilerSet) {
        if (libName.endsWith(".so")) { // NOI18N
            int i = libName.indexOf(".so"); // NOI18N
            if (i > 0) {
                libName = libName.substring(0, i);
            }
            if (libName.startsWith("lib")) { // NOI18N
                libName = libName.substring(3);
            }

            return compilerSet.getCompilerFlavor().getToolchainDescriptor().getLinker().getDynamicLibrarySearchFlag()
                    + CndPathUtilitities.escapeOddCharacters(libDir)
                    + " " + compilerSet.getCompilerFlavor().getToolchainDescriptor().getLinker().getLibrarySearchFlag() // NOI18N
                    + CndPathUtilitities.escapeOddCharacters(libDir)
                    + " " + compilerSet.getCompilerFlavor().getToolchainDescriptor().getLinker().getLibraryFlag() // NOI18N
                    + CndPathUtilitities.escapeOddCharacters(libName);
        } else {
            return CndPathUtilitities.escapeOddCharacters(libPath);
        }
    }
}
