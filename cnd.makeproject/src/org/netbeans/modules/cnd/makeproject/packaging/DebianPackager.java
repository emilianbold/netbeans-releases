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

import org.netbeans.modules.cnd.makeproject.api.PackagerFileElement;
import java.io.BufferedWriter;
import java.io.IOException;
import org.netbeans.modules.cnd.makeproject.api.PackagerInfoElement;
import org.netbeans.modules.cnd.makeproject.api.PackagerDescriptor;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.PackagingConfiguration;
import org.openide.util.NbBundle;

/**
 *
 * @author thp
 */
public class DebianPackager implements PackagerDescriptor {

    public static final String PACKAGER_NAME = "Debian"; // NOI18N

    public String getName() {
        return PACKAGER_NAME; // NOI18N
    }

    public String getDisplayName() {
        return getString("Debian");
    }

    public boolean hasInfoList() {
        return true;
    }

    public List<PackagerInfoElement> getDefaultInfoList(MakeConfiguration makeConfiguration, PackagingConfiguration packagingConfiguration) {
        String defArch;
        if (makeConfiguration.getDevelopmentHost().getBuildPlatform() == PlatformTypes.PLATFORM_SOLARIS_INTEL) {
            defArch = "i386"; // NOI18N
        } else if (makeConfiguration.getDevelopmentHost().getBuildPlatform() == PlatformTypes.PLATFORM_SOLARIS_SPARC) {
            defArch = "sparc"; // NOI18N
        } else {
            // Anything else ?
            defArch = "i386"; // NOI18N
        }
        List<PackagerInfoElement> infoList = new ArrayList<PackagerInfoElement>();
        infoList.add(new PackagerInfoElement(PACKAGER_NAME, "Package", packagingConfiguration.getOutputName(), true, true)); // NOI18N
        infoList.add(new PackagerInfoElement(PACKAGER_NAME, "Version", "1.0", true, true)); // NOI18N
        infoList.add(new PackagerInfoElement(PACKAGER_NAME, "Architecture", defArch, false, true)); // NOI18N
        infoList.add(new PackagerInfoElement(PACKAGER_NAME, "Maintainer", System.getProperty("user.name"), false, true)); // NOI18N
        infoList.add(new PackagerInfoElement(PACKAGER_NAME, "Description", "...", false, true)); // NOI18N
        return infoList;
    }

    public List<String> getOptionalInfoList() {
        List<String> entryComboBox = new ArrayList<String>();

        entryComboBox.add("Section"); // NOI18N
        entryComboBox.add("Priority"); // NOI18N
        entryComboBox.add("Architecture"); // NOI18N
        entryComboBox.add("Depends"); // NOI18N
        entryComboBox.add("Maintainer"); // NOI18N
        entryComboBox.add("Description"); // NOI18N

        return entryComboBox;
    }

    public String getDefaultOptions() {
        return ""; // NOI18N
    }

    public String getDefaultTool() {
        return "dpkg-deb"; // NOI18N
    }

    public boolean isOutputAFolder() {
        return false;
    }

    public String getOutputFileName(MakeConfiguration makeConfiguration, PackagingConfiguration packagingConfiguration) {
        return packagingConfiguration.getOutputName();
    }

    public String getOutputFileSuffix() {
        return "deb";  // NOI18N
    }

    public String getTopDir(MakeConfiguration makeConfiguration, PackagingConfiguration packagingConfiguration) {
        return "/usr"; // NOI18N
    }

    public boolean supportsGroupAndOwner() {
        return true;
    }

    public ShellSciptWriter getShellFileWriter() {
        return new ScriptWriter();
    }

    public static class ScriptWriter implements ShellSciptWriter {

        public void writeShellScript(BufferedWriter bw, MakeConfiguration makeConfiguration, PackagingConfiguration packagingConfiguration) throws IOException {
            writePackagingScriptBodyDebian(bw, makeConfiguration);
        }

        private void writePackagingScriptBodyDebian(BufferedWriter bw, MakeConfiguration conf) throws IOException {
            PackagingConfiguration packagingConfiguration = conf.getPackagingConfiguration();
            List<PackagerFileElement> fileList = packagingConfiguration.getFiles().getValue();

            bw.write("# Copy files and create directories and links\n"); // NOI18N
            for (PackagerFileElement elem : fileList) {
                bw.write("cd \"${TOP}\"\n"); // NOI18N
                if (elem.getType() == PackagerFileElement.FileType.FILE) {
                    String toDir = CndPathUtilitities.getDirName(conf.getPackagingConfiguration().expandMacros(elem.getTo()));
                    if (toDir != null && toDir.length() >= 0) {
                        bw.write("makeDirectory " + "${TMPDIR}/" + toDir + "\n"); // NOI18N
                    }
                    bw.write("copyFileToTmpDir \"" + elem.getFrom() + "\" \"${TMPDIR}/" + elem.getTo() + "\" 0" + elem.getPermission() + "\n"); // NOI18N
                } else if (elem.getType() == PackagerFileElement.FileType.DIRECTORY) {
                    bw.write("makeDirectory " + " \"${TMPDIR}/" + elem.getTo() + "\"" + " 0" + elem.getPermission() + "\n"); // NOI18N
                } else if (elem.getType() == PackagerFileElement.FileType.SOFTLINK) {
                    String toDir = CndPathUtilitities.getDirName(elem.getTo());
                    String toName = CndPathUtilitities.getBaseName(elem.getTo());
                    if (toDir != null && toDir.length() >= 0) {
                        bw.write("makeDirectory " + "\"" + "${TMPDIR}/" + toDir + "\"" + "\n"); // NOI18N
                    }
                    bw.write("cd " + "\"" + "${TMPDIR}/" + toDir + "\"" + "\n"); // NOI18N
                    bw.write("ln -s " + "\"" + elem.getFrom() + "\"" + " " + "\"" + toName + "\"" + "\n"); // NOI18N
                } else if (elem.getType() == PackagerFileElement.FileType.UNKNOWN) {
                    // skip ???
                } else {
                    assert false;
                }
                bw.write("\n"); // NOI18N
            }
            bw.write("\n"); // NOI18N

            bw.write("# Create control file\n"); // NOI18N
            bw.write("cd \"${TOP}\"\n"); // NOI18N
            bw.write("CONTROL_FILE=${TMPDIR}/DEBIAN/control\n"); // NOI18N
            bw.write("rm -f ${CONTROL_FILE}\n"); // NOI18N
            bw.write("mkdir -p ${TMPDIR}/DEBIAN\n"); // NOI18N
            bw.write("\n"); // NOI18N        
            bw.write("cd \"${TOP}\"\n"); // NOI18N
            List<PackagerInfoElement> infoList = packagingConfiguration.getHeaderSubList(PACKAGER_NAME);
            for (PackagerInfoElement elem : infoList) {
                String value = elem.getValue();
                int i = 0;
                int j = value.indexOf("\\n"); // NOI18N 
                while (j >= 0) {
                    if (i == 0) {
                        bw.write("echo \'" + elem.getName() + ": " + value.substring(i, j) + "\' >> ${CONTROL_FILE}\n"); // NOI18N 
                    } else {
                        bw.write("echo \'" + value.substring(i, j) + "\' >> ${CONTROL_FILE}\n"); // NOI18N
                    }
                    i = j + 2;
                    j = value.indexOf("\\n", i); // NOI18N 
                }
                if (i < value.length()) {
                    if (i == 0) {
                        bw.write("echo \'" + elem.getName() + ": " + value.substring(i) + "\' >> ${CONTROL_FILE}\n"); // NOI18N 
                    } else {
                        bw.write("echo \'" + value.substring(i) + "\' >> ${CONTROL_FILE}\n"); // NOI18N
                    }
                }
            }

            bw.write("\n"); // NOI18N
            bw.write("# Create Debian Package\n"); // NOI18N
            bw.write("cd \"${TOP}\"\n"); // NOI18N
            bw.write("cd \"${TMPDIR}/..\"\n"); // NOI18N
            bw.write(packagingConfiguration.getToolValue() + " " + packagingConfiguration.getOptionsValue() + " --build ${TMPDIRNAME}\n"); // NOI18N
            bw.write("checkReturnCode\n"); // NOI18N
            bw.write("cd \"${TOP}\"\n"); // NOI18N
            bw.write("mkdir -p  " + CndPathUtilitities.getDirName(packagingConfiguration.getOutputValue()) + "\n"); // NOI18N
            bw.write("mv ${TMPDIR}.deb " + packagingConfiguration.getOutputValue() + "\n"); // NOI18N
            bw.write("checkReturnCode\n"); // NOI18N

            bw.write("echo Debian: " + packagingConfiguration.getOutputValue() + "\n"); // NOI18N
            bw.write("\n"); // NOI18N
        }
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(DebianPackager.class, s); // FIXUP: Using Bundl in .../api.configurations. Too latet to move bundles around
    }
}
