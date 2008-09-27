/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.makeproject.packaging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.PackagingConfiguration;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platform;
import org.openide.util.NbBundle;

/**
 *
 * @author thp
 */
        
public class SVR4Packager implements PackagerDescriptor {
    public static String PACKAGER_NAME = "SVR4"; // NOI18N

    public String getName() {
        return PACKAGER_NAME;
    }

    public String getDisplayName() {
        return getString("SCR4Package"); // FIXUP: typo...
    }
    
    public boolean hasInfoList() {
        return true;
    }
    
    public List<PackagerInfoElement> getDefaultInfoList(MakeConfiguration makeConfiguration, PackagingConfiguration packagingConfiguration) {
        String defArch;
        if (makeConfiguration.getPlatform().getValue() == Platform.PLATFORM_SOLARIS_INTEL) {
            defArch = "i386"; // NOI18N
        }
        else if (makeConfiguration.getPlatform().getValue() == Platform.PLATFORM_SOLARIS_SPARC) {
            defArch = "sparc"; // NOI18N
        }
        else {
            // Anything else ?
            defArch = "i386"; // NOI18N
        }
        List<PackagerInfoElement> infoList = new ArrayList<PackagerInfoElement>();
        infoList.add(new PackagerInfoElement(PACKAGER_NAME, "PKG", packagingConfiguration.getOutputName(), true, true)); // NOI18N
        infoList.add(new PackagerInfoElement(PACKAGER_NAME, "NAME", "Package description ...", true, true)); // NOI18N
        infoList.add(new PackagerInfoElement(PACKAGER_NAME, "ARCH", defArch, true, true)); // NOI18N
        infoList.add(new PackagerInfoElement(PACKAGER_NAME, "CATEGORY", "application", true, true)); // NOI18N
        infoList.add(new PackagerInfoElement(PACKAGER_NAME, "VERSION", "1.0", true, true)); // NOI18N
        infoList.add(new PackagerInfoElement(PACKAGER_NAME, "BASEDIR", "/opt", false, true)); // NOI18N
        infoList.add(new PackagerInfoElement(PACKAGER_NAME, "PSTAMP", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()), false, true)); // NOI18N
        infoList.add(new PackagerInfoElement(PACKAGER_NAME, "CLASSES", "none", false, true)); // NOI18N
    
        return infoList;
    }

    public String getDefaultOptions() {
        return ""; // NOI18N
    }

    public String getDefaultTool() {
        return "pkgmk"; // NOI18N
    }

    public boolean isOutputAFolder() {
        return true;
    }
    
    public String getOutputFileName(MakeConfiguration makeConfiguration, PackagingConfiguration packagingConfiguration) {
        return null;
    }
    
    public String getOutputFileSuffix() {
        return null;
    }

    public String getTopDir(MakeConfiguration makeConfiguration, PackagingConfiguration packagingConfiguration) {
        return packagingConfiguration.findInfoValueName("PKG"); // NOI18N
    }
   
    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(PackagingConfiguration.class, s); // FIXUP: Using Bundl in .../api.configurations. Too latet to move bundles around
    }
}
