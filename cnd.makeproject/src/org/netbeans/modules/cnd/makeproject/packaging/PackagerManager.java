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
import org.netbeans.modules.cnd.makeproject.api.configurations.PackagingConfiguration;

public class PackagerManager  {
    private List<PackagerDescriptor> list = null;
    
    private static PackagerManager instance;
    
    public static PackagerManager getDefault() {
        if (instance == null) {
            instance = new PackagerManager();
            instance.populate();
        }
        return instance;
    }
    
    private void populate() {
        addPackagingDescriptor(createTarPackager());
        addPackagingDescriptor(createZipPackager());
        addPackagingDescriptor(createSVR4());
        addPackagingDescriptor(createRPMPackager());
        addPackagingDescriptor(createDebianPackager());
    }
    
    public void addPackagingDescriptor(PackagerDescriptor packagingDescriptor) {
        list.add(packagingDescriptor);
    }
    
    private PackagerDescriptor createTarPackager() {
        PackagerDescriptorImpl pd;
        pd = new PackagerDescriptorImpl("Tar", "Tar File"); // NOI18N
        pd.setIsOutputAFolder(false);
        pd.setSuffix(".tar"); // NOI18N
        pd.setDefaultOptions("-v"); // NOI18N
        pd.setDefaultTool("tar"); // NOI18N
        //pd.setTopDir(IpeUtils.getBaseName(getOutputValue()); // FIXUP
        return pd;
    }
    
    private PackagerDescriptor createZipPackager() {
        PackagerDescriptorImpl pd;
        pd = new PackagerDescriptorImpl("Zip", "Zip File"); // NOI18N
        pd.setIsOutputAFolder(false);
        pd.setSuffix(".zip"); // NOI18N
        pd.setDefaultOptions(""); // NOI18N
        pd.setDefaultTool("zip"); // NOI18N
        //pd.setTopDir(IpeUtils.getBaseName(getOutputValue()); // FIXUP
        return pd;
    }
    
    private PackagerDescriptor createSVR4() {
        PackagerDescriptorImpl pd = new PackagerDescriptorImpl("SVR4", "Solaris SVR4 Package"); // NOI18N
        pd.setHasInfoList(true);
        List<InfoElement> infoList = new ArrayList<InfoElement>();
        //infoList.add(new InfoElement(PackagingConfiguration.TYPE_SVR4_PACKAGE, "PKG", getOutputName(), true, true)); // NOI18N
        infoList.add(new InfoElement(PackagingConfiguration.TYPE_SVR4_PACKAGE, "NAME", "Package description ...", true, true)); // NOI18N
        //infoList.add(new InfoElement(PackagingConfiguration.TYPE_SVR4_PACKAGE, "ARCH", defArch, true, true)); // NOI18N
        infoList.add(new InfoElement(PackagingConfiguration.TYPE_SVR4_PACKAGE, "CATEGORY", "application", true, true)); // NOI18N
        infoList.add(new InfoElement(PackagingConfiguration.TYPE_SVR4_PACKAGE, "VERSION", "1.0", true, true)); // NOI18N
        infoList.add(new InfoElement(PackagingConfiguration.TYPE_SVR4_PACKAGE, "BASEDIR", "/opt", false, true)); // NOI18N
        infoList.add(new InfoElement(PackagingConfiguration.TYPE_SVR4_PACKAGE, "PSTAMP", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()), false, true)); // NOI18N
        infoList.add(new InfoElement(PackagingConfiguration.TYPE_SVR4_PACKAGE, "CLASSES", "none", false, true)); // NOI18N
        pd.setDefaultInfoList(infoList);
        addPackagingDescriptor(pd);
        pd.setIsOutputAFolder(true);
        pd.setDefaultOptions(""); // NOI18N
        pd.setDefaultTool("pkgmk"); // NOI18N
        //pd.setTopDir(findInfoValueName("PKG")); // FIXUP
        return pd;
    }
    
    private PackagerDescriptor createRPMPackager() {
        PackagerDescriptorImpl pd;
        pd = new PackagerDescriptorImpl("RPM", "RPM Package"); // NOI18N
        List<InfoElement> infoList = new ArrayList<InfoElement>();
        infoList.add(new InfoElement(PackagingConfiguration.TYPE_RPM_PACKAGE, "Summary", "Sumary...", true, true)); // NOI18N
        //infoList.add(new InfoElement(PackagingConfiguration.TYPE_RPM_PACKAGE, "Name", getOutputName(), true, true)); // NOI18N
        infoList.add(new InfoElement(PackagingConfiguration.TYPE_RPM_PACKAGE, "Version", "1.0", true, true)); // NOI18N
        infoList.add(new InfoElement(PackagingConfiguration.TYPE_RPM_PACKAGE, "Release", "1", true, true)); // NOI18N
        infoList.add(new InfoElement(PackagingConfiguration.TYPE_RPM_PACKAGE, "Group", "Applications/System", true, true)); // NOI18N
        infoList.add(new InfoElement(PackagingConfiguration.TYPE_RPM_PACKAGE, "License", "BSD-type", true, true)); // NOI18N
        infoList.add(new InfoElement(PackagingConfiguration.TYPE_RPM_PACKAGE, "%description", "Description...", true, true)); // NOI18N
        pd.setDefaultInfoList(infoList);
        pd.setIsOutputAFolder(true);
        pd.setDefaultOptions(""); // NOI18N
        pd.setDefaultTool("rpmbuild"); // NOI18N
        pd.setTopDir("/usr"); // NOI18N
        return pd;
    }
    
    private PackagerDescriptor createDebianPackager() {
        PackagerDescriptorImpl pd;
        pd = new PackagerDescriptorImpl("Debian", "Debian Package"); // NOI18N
        List<InfoElement> infoList = new ArrayList<InfoElement>();
        //infoList.add(new InfoElement(PackagingConfiguration.TYPE_DEBIAN_PACKAGE, "Package", getOutputName(), true, true)); // NOI18N
        infoList.add(new InfoElement(PackagingConfiguration.TYPE_DEBIAN_PACKAGE, "Version", "1.0", true, true)); // NOI18N
        //infoList.add(new InfoElement(PackagingConfiguration.TYPE_DEBIAN_PACKAGE, "Architecture", defArch, false, true)); // NOI18N
        infoList.add(new InfoElement(PackagingConfiguration.TYPE_DEBIAN_PACKAGE, "Maintainer", System.getProperty("user.name"), false, true)); // NOI18N
        infoList.add(new InfoElement(PackagingConfiguration.TYPE_DEBIAN_PACKAGE, "Description", "...", false, true)); // NOI18N
        pd.setIsOutputAFolder(false);
        pd.setSuffix(".deb"); // NOI18N
        pd.setDefaultOptions(""); // NOI18N
        pd.setDefaultTool("dpkg-deb"); // NOI18N
        pd.setTopDir("/usr"); // NOI18N
        return pd;
    }
}