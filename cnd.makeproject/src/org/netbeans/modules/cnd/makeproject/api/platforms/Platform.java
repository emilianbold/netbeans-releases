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

package org.netbeans.modules.cnd.makeproject.api.platforms;

import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.PlatformTypes;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem;
import org.openide.util.Utilities;

public abstract class Platform implements PlatformTypes {
    
    private static int defaultPlatform = -1;
    
    private String name;
    private String displayName;
    private int id;
    
    public Platform(String name, String displayName, int id) {
        this.name = name;
        this.displayName = displayName;
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getId() {
        return id;
    }
    
    public abstract LibraryItem.StdLibItem[] getStandardLibraries();
    
    public abstract String getLibraryName(String baseName);
    
    public abstract String getLibraryLinkOption(String libName, String libDir, String libPath, CompilerSet compilerSet);
    
    public LibraryItem.StdLibItem getStandardLibrarie(String name) {
	for (int i = 0; i < getStandardLibraries().length; i++) {
	    if (getStandardLibraries()[i].getName().equals(name))
		return getStandardLibraries()[i];
	}
	return null;
    }
    
    public static int getDefaultPlatform() {
        if (defaultPlatform <= 0) {
            String arch = System.getProperty("os.arch"); // NOI18N
            if (Utilities.isWindows())
                defaultPlatform = Platform.PLATFORM_WINDOWS;
            else if (Utilities.getOperatingSystem() == Utilities.OS_LINUX)
                defaultPlatform = Platform.PLATFORM_LINUX;
            else if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS && arch.indexOf("86") >= 0) // NOI18N
                defaultPlatform = Platform.PLATFORM_SOLARIS_INTEL;
            else if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS)
                defaultPlatform = Platform.PLATFORM_SOLARIS_SPARC;
            else if (Utilities.getOperatingSystem() == Utilities.OS_MAC)
                defaultPlatform = Platform.PLATFORM_MACOSX;
            else 
                defaultPlatform = Platform.PLATFORM_GENERIC;
        }
        return defaultPlatform;
    }
}
