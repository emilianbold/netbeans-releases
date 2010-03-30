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
package org.netbeans.modules.cnd.makeproject.configurations;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.configurations.ArchiverConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.BasicCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CustomToolConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibrariesConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem;
import org.netbeans.modules.cnd.makeproject.api.configurations.LinkerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakefileConfiguration;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.makeproject.api.configurations.DefaultMakefileWriter;
import org.netbeans.modules.cnd.makeproject.spi.configurations.MakefileWriter;
import org.netbeans.modules.cnd.makeproject.api.PackagerDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.PackagingConfiguration;
import org.netbeans.modules.cnd.makeproject.api.PackagerManager;
import org.netbeans.modules.cnd.makeproject.platform.Platform;
import org.netbeans.modules.cnd.makeproject.platform.Platforms;
import org.netbeans.modules.cnd.makeproject.packaging.DummyPackager;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class ConfigurationMakefileWriter {

    private MakeConfigurationDescriptor projectDescriptor;

    public ConfigurationMakefileWriter(MakeConfigurationDescriptor projectDescriptor) {
        this.projectDescriptor = projectDescriptor;
    }

    public void write() {
        Collection<MakeConfiguration> okConfs = getOKConfigurations(true);
        cleanup(okConfs);
        writeMakefileImpl();
        for (MakeConfiguration conf : okConfs) {
            writeMakefileConf(conf);
            writePackagingScript(conf);
        }
        writeMakefileVariables(projectDescriptor);
    }

    public void writeMissingMakefiles() {
        Collection<MakeConfiguration> okConfs = getOKConfigurations(false);
        long xmlFileTimeStamp = new File(new File(projectDescriptor.getBaseDir(), MakeConfiguration.NBPROJECT_FOLDER), MakeConfiguration.CONFIGURATIONS_XML).lastModified();
        for (MakeConfiguration conf : okConfs) {
            File file = new File(getMakefilePath(conf));
            if (!file.exists() || file.lastModified() < xmlFileTimeStamp) {
                writeMakefileConf(conf);
            }
            file = new File(getPackageScriptPath(conf));
            if (!file.exists() || file.lastModified() < xmlFileTimeStamp) {
                writePackagingScript(conf);
            }
        }
    }

    private String getMakefilePath(MakeConfiguration conf) {
        return projectDescriptor.getBaseDir() + '/' + MakeConfiguration.NBPROJECT_FOLDER + '/' + "Makefile-" + conf.getName() + ".mk"; // NOI18N
    }

    private String getPackageScriptPath(MakeConfiguration conf) {
        return projectDescriptor.getBaseDir() + '/' + MakeConfiguration.NBPROJECT_FOLDER + '/' + "Package-" + conf.getName() + ".bash"; // NOI18N
    }

    /**
     * @return configurations that should be kept intact, i.e. Makefile
     * and Package script should not be changed. Reason for protecting
     * a configuration is missing tool collection. See IZ #168540.
     */
    private Collection<MakeConfiguration> getOKConfigurations(boolean showWarning) {
        List<MakeConfiguration> ok = new ArrayList<MakeConfiguration>();
        List<MakeConfiguration> noCompilerSet = new ArrayList<MakeConfiguration>();
        List<MakeConfiguration> wrongPlatform = new ArrayList<MakeConfiguration>();
        Configuration[] confs = projectDescriptor.getConfs().toArray();
        for (int i = 0; i < confs.length; i++) {
            MakeConfiguration conf = (MakeConfiguration) confs[i];
            if (conf.getDevelopmentHost().isLocalhost() &&
                    CompilerSetManager.get(conf.getDevelopmentHost().getExecutionEnvironment()).getPlatform() != conf.getDevelopmentHost().getBuildPlatformConfiguration().getValue()) {
                // add configurations if local host and target platform are different (don't have the right compiler set on this platform)
                wrongPlatform.add(conf);
            }
            else if (conf.getCompilerSet().getCompilerSet() == null) {
                // add configurations with unknown compiler sets
                noCompilerSet.add(conf);
            }
            else {
                ok.add(conf);
            }
        }
        if (!wrongPlatform.isEmpty() && showWarning && MakeOptions.getInstance().getShowConfigurationWarning()) {
            ExecutionEnvironment execEnv = ExecutionEnvironmentFactory.fromUniqueID(HostInfoUtils.LOCALHOST);
            int platformID = CompilerSetManager.get(execEnv).getPlatform();
            Platform platform = Platforms.getPlatform(platformID);
            StringBuilder list = new StringBuilder();
            for (MakeConfiguration c : wrongPlatform) {
                list.append(getString("CONF", c.getName(), c.getDevelopmentHost().getBuildPlatformConfiguration().getName())).append("\n"); // NOI18N
            }
            final String msg = getString("TARGET_MISMATCH_TXT", platform.getDisplayName(), list.toString());
            final String title = getString("TARGET_MISMATCH_DIALOG_TITLE.TXT");
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    Object[] options = new Object[]{NotifyDescriptor.OK_OPTION};
                    DialogDescriptor nd = new DialogDescriptor(new ConfigurationWarningPanel(msg), title, true, options, NotifyDescriptor.OK_OPTION, 0, null, null);
                    DialogDisplayer.getDefault().notify(nd);
                }
            });
        }
        return ok;
    }

    /**
     * Remove all Makefile-* and Package-* files
     * except for those belonging to protected configurations.
     *
     * @param protectedConfs
     */
    private void cleanup(Collection<MakeConfiguration> okConfs) {
        List<MakeConfiguration> protectedConfs = new ArrayList<MakeConfiguration>();
        Configuration[] confs = projectDescriptor.getConfs().toArray();
        for (Configuration c : projectDescriptor.getConfs().toArray()) {
            MakeConfiguration conf = (MakeConfiguration)c;
            if (!okConfs.contains(conf)) {
               protectedConfs.add(conf);
            }
        }

        File folder = new File(projectDescriptor.getBaseDir(), MakeConfiguration.NBPROJECT_FOLDER);
        File[] children = folder.listFiles();
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                String filename = children[i].getName();
                if (filename.startsWith("Makefile-") || filename.startsWith("Package-")) { // NOI18N
                    boolean protect = false;
                    for (MakeConfiguration conf : protectedConfs) {
                            if (filename.equals("Makefile-" + conf.getName() + ".mk") // NOI18N
                                    || filename.equals("Package-" + conf.getName() + ".bash")) { // NOI18N
                                protect = true;
                                break;
                            }
                        }
                    if (!protect) {
                        children[i].delete();
                    }
                }
            }
        }
    }

    private void writeMakefileImpl() {
        String resource = "/org/netbeans/modules/cnd/makeproject/resources/MasterMakefile-impl.mk"; // NOI18N
        InputStream is = null;
        FileOutputStream os = null;
        try {
            URL url = new URL("nbresloc:" + resource); // NOI18N
            is = url.openStream();
        } catch (Exception e) {
            is = MakeConfigurationDescriptor.class.getResourceAsStream(resource);
        }

        String outputFileName = projectDescriptor.getBaseDir() + '/' + MakeConfiguration.NBPROJECT_FOLDER + '/' + MakeConfiguration.MAKEFILE_IMPL; // UNIX path // NOI18N
        try {
            os = new FileOutputStream(outputFileName);
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }

        if (is == null || os == null) {
            // FIXUP: ERROR
            return;
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));

        // Project name
        String projectName = CndPathUtilitities.getBaseName(projectDescriptor.getBaseDir());

        // Configurations
        StringBuilder configurations = new StringBuilder();
        for (int i = 0; i < projectDescriptor.getConfs().toArray().length; i++) {
            configurations.append(projectDescriptor.getConfs().toArray()[i].getName());
            configurations.append(" "); // NOI18N
        }

        try {
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                if (line.indexOf("<PN>") >= 0) { // NOI18N
                    line = line.replaceFirst("<PN>", projectName); // NOI18N
                } else if (line.indexOf("<CNS>") >= 0) { // NOI18N
                    line = line.replaceFirst("<CNS>", configurations.toString()); // NOI18N
                } else if (line.indexOf("<CN>") >= 0) { // NOI18N
                    line = line.replaceFirst("<CN>", projectDescriptor.getConfs().getConf(0).getName()); // NOI18N
                }
                bw.write(line + "\n"); // NOI18N
            }
            br.close();
            bw.flush();
            bw.close();
        } catch (Exception e) {
        }

    }

    private void writeMakefileConf(MakeConfiguration conf) {
        String outputFileName = projectDescriptor.getBaseDir() + '/' + MakeConfiguration.NBPROJECT_FOLDER + '/' + "Makefile-" + conf.getName() + ".mk"; // UNIX path // NOI18N

        FileOutputStream os = null;
        try {
            os = new FileOutputStream(outputFileName);
        } catch (Exception e) {
            // FIXUP
        }

        // Find MakefileWriter in toolchain.
        MakefileWriter makefileWriter = null;
        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        if (compilerSet != null) {
            String makefileWriterClassName = compilerSet.getCompilerFlavor().getToolchainDescriptor().getMakefileWriter();
            if (makefileWriterClassName != null) {
                Collection<? extends MakefileWriter> mwc = Lookup.getDefault().lookupAll(MakefileWriter.class);
                for (MakefileWriter instance : mwc) {
                    if (makefileWriterClassName.equals(instance.getClass().getName())) {
                        makefileWriter = instance;
                        break;
                    }
                }
                if (makefileWriter == null) {
                    System.err.println("ERROR: class" + makefileWriterClassName + " is not found or is not instance of MakefileWriter"); // NOI18N
                }
            }
        }
        // Use default MakefileWriter if none is found.
        if (makefileWriter == null) {
            makefileWriter = new DefaultMakefileWriter();
        }

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
        try {
            makefileWriter.writePrelude(projectDescriptor, conf, bw);
            writeBuildTargets(makefileWriter, projectDescriptor, conf, bw);
            writeBuildTestTargets(makefileWriter, projectDescriptor, conf, bw);
            makefileWriter.writeRunTestTarget(projectDescriptor, conf, bw);
            makefileWriter.writeCleanTarget(projectDescriptor, conf, bw);
            makefileWriter.writeDependencyChecking(projectDescriptor, conf, bw);
            bw.flush();
            bw.close();
            if (conf.isQmakeConfiguration()) {
                new QmakeProjectWriter(projectDescriptor, conf).write();
            }
        } catch (IOException e) {
            // FIXUP
        }
    }

    private static boolean isDMake(CompilerSet compilerSet) {
        boolean dMakeSyntax = false;
        Tool makeTool = compilerSet.getTool(PredefinedToolKind.MakeTool);
        if (makeTool != null && makeTool.getFlavor().isSunStudioCompiler()) {
            dMakeSyntax = true;
        }
        return dMakeSyntax;
    }

    public static String getCompilerName(MakeConfiguration conf, PredefinedToolKind tool) {
        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        if (compilerSet != null) {
            Tool compiler = compilerSet.getTool(tool);
            if (compiler != null) {
                BasicCompilerConfiguration compilerConf = null;
                switch (tool) {
                    case CCompiler:
                        compilerConf = conf.getCCompilerConfiguration();
                        break;
                    case CCCompiler:
                        compilerConf = conf.getCCCompilerConfiguration();
                        break;
                    case FortranCompiler:
                        compilerConf = conf.getFortranCompilerConfiguration();
                        break;
                    case Assembler:
                        compilerConf = conf.getAssemblerConfiguration();
                        break;
                }
                if (compilerConf != null && compilerConf.getTool().getModified()) {
                    return compilerConf.getTool().getValue();
                } else if (0 < compiler.getName().length()) {
                    return compiler.getName();
                } else if (compilerSet.isUrlPointer()) {
                    // Fake tool, get name from the descriptor (see IZ#174566).
                    return compiler.getDescriptor().getNames()[0];
                }
            }
        }
        return ""; // NOI18N
    }

    public static void writePrelude(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        if (compilerSet == null) {
            return;
        }
        AbstractCompiler cCompiler = (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCompiler);
        AbstractCompiler ccCompiler = (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCCompiler);
        AbstractCompiler fortranCompiler = (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.FortranCompiler);
        AbstractCompiler assemblerCompiler = (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.Assembler);

        bw.write("#\n"); // NOI18N
        bw.write("# Generated Makefile - do not edit!\n"); // NOI18N
        bw.write("#\n"); // NOI18N
        bw.write("# Edit the Makefile in the project folder instead (../Makefile). Each target\n"); // NOI18N
        bw.write("# has a -pre and a -post target defined where you can add customized code.\n"); // NOI18N
        bw.write("#\n"); // NOI18N
        bw.write("# This makefile implements configuration specific macros and targets.\n"); // NOI18N
        bw.write("\n"); // NOI18N
        bw.write("\n"); // NOI18N
        bw.write("# Environment\n"); // NOI18N
        bw.write("MKDIR=mkdir\n"); // NOI18N
        bw.write("CP=cp\n"); // NOI18N
        bw.write("GREP=grep\n"); // NOI18N
        bw.write("NM=nm\n"); // NOI18N
        bw.write("CCADMIN=CCadmin\n"); // NOI18N
        bw.write("RANLIB=ranlib\n"); // NOI18N
        bw.write("CC=" + getCompilerName(conf, PredefinedToolKind.CCompiler) + "\n"); // NOI18N
        bw.write("CCC=" + getCompilerName(conf, PredefinedToolKind.CCCompiler) + "\n"); // NOI18N
        bw.write("CXX=" + getCompilerName(conf, PredefinedToolKind.CCCompiler) + "\n"); // NOI18N
        bw.write("FC=" + getCompilerName(conf, PredefinedToolKind.FortranCompiler) + "\n"); // NOI18N
        bw.write("AS=" + getCompilerName(conf, PredefinedToolKind.Assembler) + "\n"); // NOI18N
        if (conf.getArchiverConfiguration().getTool().getModified()) {
            bw.write("AR=" + conf.getArchiverConfiguration().getTool().getValue() + "\n"); // NOI18N
        }
        if (conf.isQmakeConfiguration()) {
            bw.write("QMAKE=" + getCompilerName(conf, PredefinedToolKind.QMakeTool) + "\n"); // NOI18N
        }
        bw.write("\n"); // NOI18N

        bw.write("# Macros\n"); // NOI18N
        bw.write("CND_PLATFORM=" + conf.getVariant() + "\n"); // NOI18N
        bw.write("CND_CONF=" + conf.getName() + "\n"); // NOI18N
        bw.write("CND_DISTDIR=" + MakeConfiguration.DIST_FOLDER + "\n"); // NOI18N
        bw.write("\n"); // NOI18N

        bw.write("# Include project Makefile\n"); // NOI18N
        bw.write("include " + projectDescriptor.getProjectMakefileName() + "\n"); // NOI18N
        bw.write("\n"); // NOI18N
        bw.write("# Object Directory\n"); // NOI18N
        bw.write(MakeConfiguration.OBJECTDIR_MACRO_NAME + "=" + getObjectDir(conf) + "\n"); // NOI18N
        bw.write("\n"); // NOI18N
        bw.write("# Object Files\n"); // NOI18N
        bw.write("OBJECTFILES=" + getObjectFiles(projectDescriptor, conf) + "\n"); // NOI18N
        bw.write("\n"); // NOI18N

        if (hasTests(projectDescriptor)) {
            bw.write("# Test Directory\n"); // NOI18N
            bw.write("TESTDIR=" + getObjectDir(conf) + "/tests\n"); // NOI18N
            bw.write("\n"); // NOI18N
            bw.write("# Test Files\n"); // NOI18N
            bw.write("TESTFILES=" + getTestTargetFiles(projectDescriptor, conf) + "\n"); // NOI18N
        }

        bw.write("\n"); // NOI18N
        if (cCompiler != null) {
            bw.write("# C Compiler Flags\n"); // NOI18N
            bw.write("CFLAGS=" + conf.getCCompilerConfiguration().getCFlags(cCompiler) + "\n"); // NOI18N
            bw.write("\n"); // NOI18N
        }
        if (ccCompiler != null) {
            bw.write("# CC Compiler Flags\n"); // NOI18N
            bw.write("CCFLAGS=" + conf.getCCCompilerConfiguration().getCCFlags(ccCompiler) + "\n"); // NOI18N
            bw.write("CXXFLAGS=" + conf.getCCCompilerConfiguration().getCCFlags(ccCompiler) + "\n"); // NOI18N
            bw.write("\n"); // NOI18N
        }
        if (fortranCompiler != null) {
            bw.write("# Fortran Compiler Flags\n"); // NOI18N
            bw.write("FFLAGS=" + conf.getFortranCompilerConfiguration().getFFlags(fortranCompiler) + "\n"); // NOI18N
            bw.write("\n"); // NOI18N
        }
        if (assemblerCompiler != null) {
            bw.write("# Assembler Flags\n"); // NOI18N
            bw.write("ASFLAGS=" + conf.getAssemblerConfiguration().getAsFlags(assemblerCompiler) + "\n"); // NOI18N
            bw.write("\n"); // NOI18N
        }
        bw.write("# Link Libraries and Options\n"); // NOI18N
        bw.write("LDLIBSOPTIONS=" + conf.getLinkerConfiguration().getLibraryItems() + "\n"); // NOI18N
        bw.write("\n"); // NOI18N

        if (conf.isQmakeConfiguration()) {
            String qmakeSpec = conf.getQmakeConfiguration().getQmakeSpec().getValue();
            if (qmakeSpec.length() == 0 && conf.getDevelopmentHost().getBuildPlatform() == PlatformTypes.PLATFORM_MACOSX) {
                // on Mac we force spec to macx-g++, otherwise qmake generates xcode project
                qmakeSpec = CppUtils.getQmakeSpec(compilerSet, conf.getDevelopmentHost().getBuildPlatform());
            }
            if (!qmakeSpec.isEmpty()) {
                qmakeSpec = "-spec " + qmakeSpec + " "; // NOI18N
            }
            bw.write("nbproject/qt-${CND_CONF}.mk: nbproject/qt-${CND_CONF}.pro FORCE\n"); // NOI18N
            // It is important to generate makefile in current directory, and then move it to nbproject/.
            // Otherwise qmake will complain that sources are not found.
            bw.write("\t${QMAKE} VPATH=. " + qmakeSpec + "-o qttmp-${CND_CONF}.mk nbproject/qt-${CND_CONF}.pro\n"); // NOI18N
            bw.write("\tmv -f qttmp-${CND_CONF}.mk nbproject/qt-${CND_CONF}.mk\n"); // NOI18N
            if (conf.getDevelopmentHost().getBuildPlatform() == PlatformTypes.PLATFORM_WINDOWS) {
                // qmake uses backslashes on Windows, this code corrects them to forward slashes
                bw.write("\t@sed -e 's:\\\\\\(.\\):/\\1:g' nbproject/qt-${CND_CONF}.mk >nbproject/qt-${CND_CONF}.tmp\n"); // NOI18N
                bw.write("\t@mv -f nbproject/qt-${CND_CONF}.tmp nbproject/qt-${CND_CONF}.mk\n"); // NOI18N
            }
            bw.write('\n'); // NOI18N
            bw.write("FORCE:\n\n"); // NOI18N
        }
    }

    protected void writeBuildTargets(MakefileWriter makefileWriter, MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, BufferedWriter bw) throws IOException {
        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        if (compilerSet == null) {
            bw.write(".build-conf:\n"); // NOI18N
            bw.write("\t@echo 'Tool collection " + conf.getCompilerSet().getCompilerSetName().getValue() // NOI18N
                    + " was missing when this makefile was generated'\n"); // NOI18N
            bw.write("\t@echo 'Please specify existing tool collection in project properties'\n"); // NOI18N
            bw.write("\t@exit 1\n\n"); // NOI18N
            return;
        }
        if (conf.isCompileConfiguration()) {
            makefileWriter.writeBuildTarget(projectDescriptor, conf, bw);
            if (conf.isLinkerConfiguration()) {
                makefileWriter.writeLinkTarget(projectDescriptor, conf, bw);
            }
            if (conf.isArchiverConfiguration()) {
                makefileWriter.writeArchiveTarget(projectDescriptor, conf, bw);
            }
            if (conf.isCompileConfiguration()) {
                makefileWriter.writeCompileTargets(projectDescriptor, conf, bw);
            }
        } else if (conf.isMakefileConfiguration()) {
            makefileWriter.writeMakefileTarget(projectDescriptor, conf, bw);
        } else if (conf.isQmakeConfiguration()) {
            makefileWriter.writeQTTarget(projectDescriptor, conf, bw);
        }
        makefileWriter.writeSubProjectBuildTargets(projectDescriptor, conf, bw);
    }

    protected void writeBuildTestTargets(MakefileWriter makefileWriter, MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, BufferedWriter bw) throws IOException {
        if (hasTests(projectDescriptor)) {
            CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
            if (compilerSet == null) {
                bw.write(".build-test-conf:\n"); // NOI18N
                bw.write("\t@echo 'Tool collection " + conf.getCompilerSet().getCompilerSetName().getValue() // NOI18N
                        + " was missing when this makefile was generated'\n"); // NOI18N
                bw.write("\t@echo 'Please specify existing tool collection in project properties'\n"); // NOI18N
                bw.write("\t@exit 1\n\n"); // NOI18N
                return;
            }
            if (conf.isCompileConfiguration()) {
                makefileWriter.writeBuildTestTarget(projectDescriptor, conf, bw);
                if (conf.isLinkerConfiguration()) {
                    makefileWriter.writeLinkTestTarget(projectDescriptor, conf, bw);
                }
                if (conf.isCompileConfiguration()) {
                    makefileWriter.writeCompileTestTargets(projectDescriptor, conf, bw);
                }
            }
//        else if (conf.isMakefileConfiguration()) {
//            makefileWriter.writeMakefileTarget(projectDescriptor, conf, bw);
//        } else if (conf.isQmakeConfiguration()) {
//            makefileWriter.writeQTTarget(projectDescriptor, conf, bw);
//        }
        }
    }

    public static void writeQTTarget(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        String output = CppUtils.normalizeDriveLetter(compilerSet, getOutput(conf));
        bw.write("# Build Targets\n"); // NOI18N
        bw.write(".build-conf: ${BUILD_SUBPROJECTS} nbproject/qt-${CND_CONF}.mk\n"); // NOI18N
        bw.write("\t${MAKE} -f nbproject/qt-${CND_CONF}.mk " + output + "\n\n"); // NOI18N

        // #179140 compile single file (qt project)
        // redirect any request for building an object file to the qmake-generated makefile
        bw.write("build/" + conf.getName() + "/%.o: nbproject/qt-" + conf.getName() + ".mk\n"); // NOI18N
        bw.write("\t$(MAKE) -f nbproject/qt-" + conf.getName() + ".mk \"$@\"\n"); // NOI18N
    }

    public static void writeBuildTarget(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        String output = CppUtils.normalizeDriveLetter(compilerSet, getOutput(conf));
        bw.write("# Build Targets\n"); // NOI18N
        bw.write(".build-conf: ${BUILD_SUBPROJECTS}\n"); // NOI18N
        bw.write("\t${MAKE} " // NOI18N
                + " -f nbproject/Makefile-" + conf.getName() + ".mk " // NOI18N
                + output + "\n\n"); // NOI18N
    }

    public static void writeBuildTestTarget(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        if (hasTests(projectDescriptor)) {
            bw.write("# Build Test Targets\n"); // NOI18N
            bw.write(".build-tests-conf: .build-conf ${TESTFILES}\n"); // NOI18N
        }
    }

    public static void writeLinkTarget(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        String output = CppUtils.normalizeDriveLetter(compilerSet, getOutput(conf));
        LinkerConfiguration linkerConfiguration = conf.getLinkerConfiguration();
        CompilerSet cs = conf.getCompilerSet().getCompilerSet();
        output = CppUtils.normalizeDriveLetter(cs,output);
        String command = ""; // NOI18N
        if (linkerConfiguration.getTool().getModified()) {
            command += linkerConfiguration.getTool().getValue() + " "; // NOI18N
        }//	else if (conf.isDynamicLibraryConfiguration())
        //	    command += "${CCC}" + " "; // NOI18N
        else if (conf.hasCPPFiles(projectDescriptor)) {
            command += "${LINK.cc}" + " "; // NOI18N
        } else if (conf.hasFortranFiles(projectDescriptor)) {
            command += "${LINK.f}" + " "; // NOI18N
        } else {
            command += "${LINK.c}" + " "; // NOI18N
        }
        command += linkerConfiguration.getOptions() + " "; // NOI18N
        command += "${OBJECTFILES}" + " "; // NOI18N
        command += "${LDLIBSOPTIONS}" + " "; // NOI18N
        String[] additionalDependencies = linkerConfiguration.getAdditionalDependencies().getValues();
        for (int i = 0; i < additionalDependencies.length; i++) {
            bw.write(output + ": " + additionalDependencies[i] + "\n\n"); // NOI18N
        }
        for (LibraryItem lib : linkerConfiguration.getLibrariesConfiguration().getValue()) {
            String libPath = lib.getPath();
            if (libPath != null && libPath.length() > 0) {
                bw.write(output + ": " + CndPathUtilitities.escapeOddCharacters(CppUtils.normalizeDriveLetter(cs,libPath)) + "\n\n"); // NOI18N
            }
        }
        bw.write(output + ": ${OBJECTFILES}\n"); // NOI18N
        String folders = CndPathUtilitities.getDirName(output);
        if (folders != null) {
            bw.write("\t${MKDIR} -p " + folders + "\n"); // NOI18N
        }
        bw.write("\t" + command + "\n"); // NOI18N
    }

    public static void writeLinkTestTarget(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        if (conf.isCompileConfiguration()) {
            List<Folder> tests = getTests(projectDescriptor);
            CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
            if (compilerSet == null) {
                return;
            }
            if (!tests.isEmpty()) {
                for (Folder folder : tests) {
                    String target = "${TESTDIR}/" + CndPathUtilitities.escapeOddCharacters(CppUtils.normalizeDriveLetter(compilerSet, folder.getPath())); // NOI18N

                    String output = CppUtils.normalizeDriveLetter(compilerSet, getOutput(conf));
                    LinkerConfiguration linkerConfiguration = conf.getLinkerConfiguration();
                    CompilerSet cs = conf.getCompilerSet().getCompilerSet();
                    output = CppUtils.normalizeDriveLetter(cs, output);
                    String command = ""; // NOI18N
                    if (linkerConfiguration.getTool().getModified()) {
                        command += linkerConfiguration.getTool().getValue() + " "; // NOI18N
                    } else if (conf.hasCPPFiles(projectDescriptor)) {
                        command += "${LINK.cc}" + " "; // NOI18N
                    } else if (conf.hasFortranFiles(projectDescriptor)) {
                        command += "${LINK.f}" + " "; // NOI18N
                    } else {
                        command += "${LINK.c}" + " "; // NOI18N
                    }
                    command += "-o " + target + " "; // NOI18N
                    command += "$^" + " "; // NOI18N
                    command += "${LDLIBSOPTIONS}" + " "; // NOI18N
                    String[] additionalDependencies = linkerConfiguration.getAdditionalDependencies().getValues();
                    for (int i = 0; i < additionalDependencies.length; i++) {
                        bw.write(output + ": " + additionalDependencies[i] + "\n\n"); // NOI18N
                    }
                    for (LibraryItem lib : linkerConfiguration.getLibrariesConfiguration().getValue()) {
                        String libPath = lib.getPath();
                        if (libPath != null && libPath.length() > 0) {
                            bw.write(output + ": " + CndPathUtilitities.escapeOddCharacters(CppUtils.normalizeDriveLetter(cs, libPath)) + "\n\n"); // NOI18N
                        }
                    }

                    String objectFiles = ""; // NOI18N
                    Item[] items = folder.getAllItemsAsArray();
                    for (int k = 0; k < items.length; k++) {
                        if (items[k].hasHeaderOrSourceExtension(false, false)) {
                            continue;
                        }
                        String file = CndPathUtilitities.escapeOddCharacters(CppUtils.normalizeDriveLetter(compilerSet, items[k].getPath()));
                        String objectFile = file.replaceFirst("\\..*", ".o"); // NOI18N
                        objectFiles += "${TESTDIR}/" + objectFile + " "; // NOI18N
                    }
                    objectFiles += "${OBJECTFILES:%.o=%_nomain.o}"; // NOI18N

                    bw.write(target + ": " + objectFiles + "\n"); // NOI18N
                    bw.write("\t${MKDIR} -p ${TESTDIR}/" + folder.getParent().getPath() + "\n"); // NOI18N
                    bw.write("\t" + command + "\n\n"); // NOI18N
                }
            }
        }
    }

    public static void writeArchiveTarget(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        String output = CppUtils.normalizeDriveLetter(compilerSet, getOutput(conf));
        ArchiverConfiguration archiverConfiguration = conf.getArchiverConfiguration();
        String command = "${AR}" + " "; // NOI18N
        command += archiverConfiguration.getOptions() + " "; // NOI18N
        command += "${OBJECTFILES}" + " "; // NOI18N
        bw.write(output + ": " + "${OBJECTFILES}" + "\n"); // NOI18N
        String folders = CndPathUtilitities.getDirName(output);
        if (folders != null) {
            bw.write("\t${MKDIR} -p " + folders + "\n"); // NOI18N
        }
        bw.write("\t" + "${RM}" + " " + output + "\n"); // NOI18N
        bw.write("\t" + command + "\n"); // NOI18N
        if (archiverConfiguration.getRunRanlib().getValue()) {
            bw.write("\t" + archiverConfiguration.getRunRanlib().getOption() + " " + output + "\n"); // NOI18N
        }
    }

    public static void writeCompileTargets(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        Item[] items = projectDescriptor.getProjectItems();
        if (conf.isCompileConfiguration()) {
            String target = null;
            String folders = null;
            String file = null;
            String command = null;
            String comment = null;
            String additionalDep = null;
            for (int i = 0; i < items.length; i++) {
                final Folder folder = items[i].getFolder();
                if (folder.isTest() || folder.isTestLogicalFolder() || folder.isTestRootFolder()) {
                    continue;
                }
                ItemConfiguration itemConfiguration = items[i].getItemConfiguration(conf); //ItemConfiguration)conf.getAuxObject(ItemConfiguration.getId(items[i].getPath()));
                if (itemConfiguration.getExcluded().getValue()) {
                    continue;
                }
                CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
                if (compilerSet == null) {
                    continue;
                }
                boolean dMake = isDMake(compilerSet);
                file = CndPathUtilitities.escapeOddCharacters(CppUtils.normalizeDriveLetter(compilerSet,items[i].getPath()));
                command = ""; // NOI18N
                comment = null;
                additionalDep = null;
                if (itemConfiguration.isCompilerToolConfiguration()) {
                    AbstractCompiler compiler = (AbstractCompiler) compilerSet.getTool(itemConfiguration.getTool());
                    BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
                    target = compilerConfiguration.getOutputFile(items[i], conf, false);
                    if (compiler != null && compiler.getDescriptor() != null) {
                        String fromLinker = ""; // NOI18N
                        if (conf.getConfigurationType().getValue() == MakeConfiguration.TYPE_DYNAMIC_LIB) {
                            if (conf.getLinkerConfiguration().getPICOption().getValue()) {
                                fromLinker = " " + conf.getLinkerConfiguration().getPICOption(compilerSet); // NOI18N
                            }
                        }
                        command += compilerConfiguration.getOptions(compiler) + fromLinker + " "; // NOI18N
                        if (conf.getDependencyChecking().getValue() && compiler.getDependencyGenerationOption().length() > 0) {
                            command = "${RM} $@.d\n\t" + command + compiler.getDependencyGenerationOption() + " "; // NOI18N
                        }
                        if (items[i].hasHeaderOrSourceExtension(false, false)) {
                            String flags = compiler.getDescriptor().getPrecompiledHeaderFlags();
                            if (flags == null) {
                                command = "# command to precompile header "; // NOI18N
                                comment = "Current compiler does not support header precompilation"; // NOI18N
                            } else {
                                command += compiler.getDescriptor().getPrecompiledHeaderFlags() + " "; // NOI18N
                            }
                        } else {
                            command += compiler.getDescriptor().getOutputObjectFileFlags() + target + " "; // NOI18N
                        }
                        command += CndPathUtilitities.escapeOddCharacters(CppUtils.normalizeDriveLetter(compilerSet,items[i].getPath(true)));
                    }
                    additionalDep = compilerConfiguration.getAdditionalDependencies().getValue();
                } else if (itemConfiguration.getTool() == PredefinedToolKind.CustomTool) {
                    CustomToolConfiguration customToolConfiguration = itemConfiguration.getCustomToolConfiguration();
                    if (customToolConfiguration.getModified()) {
                        //Fix for #180918: Multiple make targets incompatible with GNU make
                        if (dMake) {
                            target = customToolConfiguration.getOutputs().getValue(" + "); // NOI18N
                        } else {
                            target = customToolConfiguration.getOutputs().getValue();
                        }
                        command = customToolConfiguration.getCommandLine().getValue();
                        comment = customToolConfiguration.getDescription().getValue();
                        additionalDep = customToolConfiguration.getAdditionalDependencies().getValue();
                    } else {
                        continue;
                    }
                } else {
                    assert false;
                }
                folders = CndPathUtilitities.getDirName(target);
                bw.write("\n"); // NOI18N
                // See IZ #151465 for explanation why Makefile is listed as dependency.
                if (additionalDep != null) {
                    bw.write(target + ": nbproject/Makefile-${CND_CONF}.mk " + file + " " + additionalDep + "\n"); // NOI18N
                } else {
                    bw.write(target + ": nbproject/Makefile-${CND_CONF}.mk " + file + "\n"); // NOI18N
                }
                if (folders != null) {
                    bw.write("\t${MKDIR} -p " + folders + "\n"); // NOI18N
                }
                if (comment != null) {
                    bw.write("\t@echo " + comment + "\n"); // NOI18N
                }
                bw.write("\t" + command + "\n"); // NOI18N
            }
        }
    }

    public static void writeCompileTestTargets(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        if (conf.isCompileConfiguration()) {
            List<Folder> tests = getTests(projectDescriptor);
            if (!tests.isEmpty()) {
                for (Folder folder : tests) {
                    Item[] items = folder.getAllItemsAsArray();

                    String target = null;
                    String folders = null;
                    String file = null;
                    String command = null;
                    String comment = null;
                    String additionalDep = null;
                    for (int i = 0; i < items.length; i++) {
                        ItemConfiguration itemConfiguration = items[i].getItemConfiguration(conf);
                        if (itemConfiguration == null) {
                            continue;
                        }
                        if (itemConfiguration.getExcluded().getValue()) {
                            continue;
                        }
                        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
                        if (compilerSet == null) {
                            continue;
                        }
                        boolean dMake = isDMake(compilerSet);
                        file = CndPathUtilitities.escapeOddCharacters(CppUtils.normalizeDriveLetter(compilerSet, items[i].getPath()));
                        command = ""; // NOI18N
                        comment = null;
                        additionalDep = null;
                        if (itemConfiguration.isCompilerToolConfiguration()) {
                            AbstractCompiler compiler = (AbstractCompiler) compilerSet.getTool(itemConfiguration.getTool());
                            BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
                            target = compilerConfiguration.getOutputFile(items[i], conf, false);
                            if (compiler != null && compiler.getDescriptor() != null) {
                                String fromLinker = ""; // NOI18N
                                if (conf.getConfigurationType().getValue() == MakeConfiguration.TYPE_DYNAMIC_LIB) {
                                    if (conf.getLinkerConfiguration().getPICOption().getValue()) {
                                        fromLinker = " " + conf.getLinkerConfiguration().getPICOption(compilerSet); // NOI18N
                                    }
                                }
                                command += compilerConfiguration.getOptions(compiler) + fromLinker + " "; // NOI18N
                                if (conf.getDependencyChecking().getValue() && compiler.getDependencyGenerationOption().length() > 0) {
                                    command = "${RM} $@.d\n\t" + command + compiler.getDependencyGenerationOption() + " "; // NOI18N
                                }
                                if (items[i].hasHeaderOrSourceExtension(false, false)) {
                                    String flags = compiler.getDescriptor().getPrecompiledHeaderFlags();
                                    if (flags == null) {
                                        command = "# command to precompile header "; // NOI18N
                                        comment = "Current compiler does not support header precompilation"; // NOI18N
                                    } else {
                                        command += compiler.getDescriptor().getPrecompiledHeaderFlags() + " "; // NOI18N
                                    }
                                } else {
                                    command += compiler.getDescriptor().getOutputObjectFileFlags() + target + " "; // NOI18N
                                }
                                command += CndPathUtilitities.escapeOddCharacters(CppUtils.normalizeDriveLetter(compilerSet, items[i].getPath(true)));
                            }
                            additionalDep = compilerConfiguration.getAdditionalDependencies().getValue();
                        } else if (itemConfiguration.getTool() == PredefinedToolKind.CustomTool) {
                            CustomToolConfiguration customToolConfiguration = itemConfiguration.getCustomToolConfiguration();
                            if (customToolConfiguration.getModified()) {
                                if (dMake) {
                                    target = customToolConfiguration.getOutputs().getValue(" + "); // NOI18N
                                } else {
                                    target = customToolConfiguration.getOutputs().getValue();
                                }
                                command = customToolConfiguration.getCommandLine().getValue();
                                comment = customToolConfiguration.getDescription().getValue();
                                additionalDep = customToolConfiguration.getAdditionalDependencies().getValue();
                            } else {
                                continue;
                            }
                        } else {
                            assert false;
                        }
                        target = target.replace("${OBJECTDIR}", "${TESTDIR}"); // NOI18N
                        command = command.replace("${OBJECTDIR}", "${TESTDIR}"); // NOI18N
                        folders = CndPathUtilitities.getDirName(target);
                        bw.write("\n"); // NOI18N
                        // See IZ #151465 for explanation why Makefile is listed as dependency.
                        if (additionalDep != null) {
                            bw.write(target + ": " + file + " " + additionalDep + "\n"); // NOI18N
                        } else {
                            bw.write(target + ": " + file + "\n"); // NOI18N
                        }
                        if (folders != null) {
                            bw.write("\t${MKDIR} -p " + folders + "\n"); // NOI18N
                        }
                        if (comment != null) {
                            bw.write("\t@echo " + comment + "\n"); // NOI18N
                        }
                        bw.write("\t" + command + "\n\n"); // NOI18N
                    }
                }
            }

            writeCompileTargetsWithoutMain(projectDescriptor, conf, bw);
        }
    }

    public static void writeCompileTargetsWithoutMain(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        Item[] items = projectDescriptor.getProjectItems();
        if (conf.isCompileConfiguration()) {
            String target = null;
            String folders = null;
            String file = null;
            String command = null;
            String comment = null;
            String additionalDep = null;
            for (int i = 0; i < items.length; i++) {
                final Folder folder = items[i].getFolder();
                if (folder.isTest() || folder.isTestLogicalFolder() || folder.isTestRootFolder()) {
                    continue;
                }
                ItemConfiguration itemConfiguration = items[i].getItemConfiguration(conf); //ItemConfiguration)conf.getAuxObject(ItemConfiguration.getId(items[i].getPath()));
                if (itemConfiguration.getExcluded().getValue()) {
                    continue;
                }
                CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
                if (compilerSet == null) {
                    continue;
                }
                boolean dMake = isDMake(compilerSet);
                file = CndPathUtilitities.escapeOddCharacters(CppUtils.normalizeDriveLetter(compilerSet, items[i].getPath()));
                command = ""; // NOI18N
                comment = null;
                additionalDep = null;
                String name = items[i].getName().replaceAll("\\..*", ""); // NOI18N
                String nomainTarget;
                if (itemConfiguration.isCompilerToolConfiguration()) {
                    AbstractCompiler compiler = (AbstractCompiler) compilerSet.getTool(itemConfiguration.getTool());
                    BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
                    target = compilerConfiguration.getOutputFile(items[i], conf, false);

                    nomainTarget = target.replace(name, name + "_nomain"); // NOI18N

                    if (compiler != null && compiler.getDescriptor() != null) {
                        String fromLinker = ""; // NOI18N
                        if (conf.getConfigurationType().getValue() == MakeConfiguration.TYPE_DYNAMIC_LIB) {
                            if (conf.getLinkerConfiguration().getPICOption().getValue()) {
                                fromLinker = " " + conf.getLinkerConfiguration().getPICOption(compilerSet); // NOI18N
                            }
                        }
                        command += compilerConfiguration.getOptions(compiler) + fromLinker + " -Dmain=__nomain "; // NOI18N
                        if (conf.getDependencyChecking().getValue() && compiler.getDependencyGenerationOption().length() > 0) {
                            command = "${RM} $@.d;\\\n\t    " + command + compiler.getDependencyGenerationOption() + " "; // NOI18N
                        }
                        if (items[i].hasHeaderOrSourceExtension(false, false)) {
                            String flags = compiler.getDescriptor().getPrecompiledHeaderFlags();
                            if (flags == null) {
                                command = "# command to precompile header "; // NOI18N
                                comment = "Current compiler does not support header precompilation"; // NOI18N
                            } else {
                                command += compiler.getDescriptor().getPrecompiledHeaderFlags() + " "; // NOI18N
                            }
                        } else {
                            command += compiler.getDescriptor().getOutputObjectFileFlags() + nomainTarget + " "; // NOI18N
                        }
                        command += CndPathUtilitities.escapeOddCharacters(CppUtils.normalizeDriveLetter(compilerSet, items[i].getPath(true)));
                    }
                    additionalDep = compilerConfiguration.getAdditionalDependencies().getValue();
                } else if (itemConfiguration.getTool() == PredefinedToolKind.CustomTool) {
                    CustomToolConfiguration customToolConfiguration = itemConfiguration.getCustomToolConfiguration();
                    if (customToolConfiguration.getModified()) {
                        if (dMake) {
                            target = customToolConfiguration.getOutputs().getValue(" + "); // NOI18N
                        } else {
                            target = customToolConfiguration.getOutputs().getValue();
                        }
                        command = customToolConfiguration.getCommandLine().getValue();
                        comment = customToolConfiguration.getDescription().getValue();
                        additionalDep = customToolConfiguration.getAdditionalDependencies().getValue();
                    } else {
                        continue;
                    }
                } else {
                    assert false;
                }
                nomainTarget = target.replace(name, name + "_nomain"); // NOI18N
                folders = CndPathUtilitities.getDirName(target);
                bw.write("\n"); // NOI18N
                // See IZ #151465 for explanation why Makefile is listed as dependency.
                if (additionalDep != null) {
                    bw.write(nomainTarget + ": " + target + " " + file + " " + additionalDep + "\n"); // NOI18N
                } else {
                    bw.write(nomainTarget + ": " + target + " " + file + "\n"); // NOI18N
                }
                if (folders != null) {
                    bw.write("\t${MKDIR} -p " + folders + "\n"); // NOI18N
                }
                if (comment != null) {
                    bw.write("\t@echo " + comment + "\n"); // NOI18N
                }

                bw.write("\t@if [ \"`${NM} " + target + " | ${GREP} '|main$$'`\" = \"\" ]; \\\n"); // NOI18N
                bw.write("\tthen  \\\n"); // NOI18N
                bw.write("\t    ${CP} " + target + " " + nomainTarget + ";\\\n"); // NOI18N
                bw.write("\telse  \\\n"); // NOI18N
                bw.write("\t    " + command + ";\\\n"); // NOI18N
                bw.write("\tfi\n"); // NOI18N
            }
            bw.write("\n"); // NOI18N
        }
    }

    public static void writeRunTestTarget(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        if (hasTests(projectDescriptor)) {
            CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
            bw.write("# Build Test Targets\n"); // NOI18N
            bw.write(".test-conf:\n"); // NOI18N

            bw.write("\t@if [ \"${TEST}\" = \"\" ]; \\\n"); // NOI18N
            bw.write("\tthen  \\\n"); // NOI18N
            for (Folder folder : getTests(projectDescriptor)) {
                String target = "${TESTDIR}/" + CndPathUtilitities.escapeOddCharacters(CppUtils.normalizeDriveLetter(compilerSet, folder.getPath())); // NOI18N
                bw.write("\t    " + target + " || true; \\\n"); // NOI18N
            }
            bw.write("\telse  \\\n"); // NOI18N
            bw.write("\t    ${TESTDIR}/${TEST} || true; \\\n"); // NOI18N
            bw.write("\tfi\n\n"); // NOI18N
        }
    }

    public static void writeMakefileTarget(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        MakefileConfiguration makefileConfiguration = conf.getMakefileConfiguration();
        String target = makefileConfiguration.getOutput().getValue();
        String cwd = makefileConfiguration.getBuildCommandWorkingDirValue();
        String command = makefileConfiguration.getBuildCommand().getValue();
        bw.write("# Build Targets\n"); // NOI18N
        bw.write(".build-conf: ${BUILD_SUBPROJECTS}\n"); // NOI18N
        //bw.write(target + ":" + "\n"); // NOI18N
        bw.write("\tcd " + CndPathUtilitities.escapeOddCharacters(CndPathUtilitities.normalize(cwd)) + " && " + command + "\n"); // NOI18N
    }

    public static void writeSubProjectBuildTargets(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        bw.write("\n"); // NOI18N
        bw.write("# Subprojects\n"); // NOI18N
        bw.write(".build-subprojects:" + "\n"); // NOI18N
        LibrariesConfiguration librariesConfiguration = null;
        if (conf.isLinkerConfiguration()) {
            librariesConfiguration = conf.getLinkerConfiguration().getLibrariesConfiguration();

            for (LibraryItem item : librariesConfiguration.getValue()) {
                if (item instanceof LibraryItem.ProjectItem) {
                    LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem) item;
                    MakeArtifact makeArtifact = projectItem.getMakeArtifact();
                    String location = makeArtifact.getWorkingDirectory();
                    if (!makeArtifact.getBuild()) {
                        continue;
                    }
                    bw.write("\tcd " + CndPathUtilitities.escapeOddCharacters(CndPathUtilitities.normalize(location)) + " && " + makeArtifact.getBuildCommand() + "\n"); // NOI18N
                }
            }
        }

        for (LibraryItem.ProjectItem item : conf.getRequiredProjectsConfiguration().getValue()) {
            MakeArtifact makeArtifact = item.getMakeArtifact();
            String location = makeArtifact.getWorkingDirectory();
            if (!makeArtifact.getBuild()) {
                continue;
            }
            bw.write("\tcd " + CndPathUtilitities.escapeOddCharacters(CndPathUtilitities.normalize(location)) + " && " + makeArtifact.getBuildCommand() + "\n"); // NOI18N
        }
        bw.write("\n"); // NOI18N
    }

    private static void writeSubProjectCleanTargets(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        bw.write("\n"); // NOI18N
        bw.write("# Subprojects\n"); // NOI18N
        bw.write(".clean-subprojects:" + "\n"); // NOI18N
        LibrariesConfiguration librariesConfiguration = null;
        if (conf.isLinkerConfiguration()) {
            librariesConfiguration = conf.getLinkerConfiguration().getLibrariesConfiguration();

            for (LibraryItem item : librariesConfiguration.getValue()) {
                if (item instanceof LibraryItem.ProjectItem) {
                    LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem) item;
                    MakeArtifact makeArtifact = projectItem.getMakeArtifact();
                    String location = makeArtifact.getWorkingDirectory();
                    if (!makeArtifact.getBuild()) {
                        continue;
                    }
                    bw.write("\tcd " + CndPathUtilitities.escapeOddCharacters(CndPathUtilitities.normalize(location)) + " && " + makeArtifact.getCleanCommand() + "\n"); // NOI18N
                }
            }
        }

        for (LibraryItem.ProjectItem item : conf.getRequiredProjectsConfiguration().getValue()) {
            MakeArtifact makeArtifact = item.getMakeArtifact();
            String location = makeArtifact.getWorkingDirectory();
            if (!makeArtifact.getBuild()) {
                continue;
            }
            bw.write("\tcd " + CndPathUtilitities.escapeOddCharacters(CndPathUtilitities.normalize(location)) + " && " + makeArtifact.getCleanCommand() + "\n"); // NOI18N
        }
    }

    public static void writeCleanTarget(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        bw.write("# Clean Targets\n"); // NOI18N
        bw.write(".clean-conf: ${CLEAN_SUBPROJECTS}"); // NOI18N
        if (conf.isQmakeConfiguration()) {
            bw.write(" nbproject/qt-" + conf.getName() + ".mk"); // NOI18N
        }
        bw.write('\n'); // NOI18N
        if (conf.isCompileConfiguration()) {
            bw.write("\t${RM} -r " + MakeConfiguration.BUILD_FOLDER + '/' + conf.getName() + "\n"); // UNIX path // NOI18N
            bw.write("\t${RM} " + getOutput(conf) + "\n"); // NOI18N
            if (conf.getCompilerSet().getCompilerSet() != null &&
                    conf.getCompilerSet().getCompilerSet().getCompilerFlavor().isSunStudioCompiler() &&
                    conf.hasCPPFiles(projectDescriptor)) {
                bw.write("\t${CCADMIN} -clean" + "\n"); // NOI18N
            }
            if (conf.hasFortranFiles(projectDescriptor)) {
                bw.write("\t${RM} *.mod" + "\n"); // NOI18N
            }

            // Also clean output from custom tool
            Item[] items = projectDescriptor.getProjectItems();
            for (int i = 0; i < items.length; i++) {
                ItemConfiguration itemConfiguration = items[i].getItemConfiguration(conf); //ItemConfiguration)conf.getAuxObject(ItemConfiguration.getId(items[i].getPath()));
                if (itemConfiguration.getExcluded().getValue()) {
                    continue;
                }
                if (itemConfiguration.getTool() == PredefinedToolKind.CustomTool && itemConfiguration.getCustomToolConfiguration().getModified()) {
                    bw.write("\t${RM} " + itemConfiguration.getCustomToolConfiguration().getOutputs().getValue() + "\n"); // NOI18N
                }
            }
        } else if (conf.isMakefileConfiguration()) {
            MakefileConfiguration makefileConfiguration = conf.getMakefileConfiguration();
            String target = makefileConfiguration.getOutput().getValue();
            String cwd = makefileConfiguration.getBuildCommandWorkingDirValue();
            String command = makefileConfiguration.getCleanCommand().getValue();

            bw.write("\tcd " + CndPathUtilitities.escapeOddCharacters(CndPathUtilitities.normalize(cwd)) + " && " + command + "\n"); // NOI18N
        } else if (conf.isQmakeConfiguration()) {
            bw.write("\t$(MAKE) -f nbproject/qt-" + conf.getName() + ".mk distclean\n"); // NOI18N
        }

        writeSubProjectCleanTargets(projectDescriptor, conf, bw);
    }

    public static void writeDependencyChecking(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        if (conf.getDependencyChecking().getValue() && !conf.isMakefileConfiguration() && !conf.isQmakeConfiguration() && conf.getCompilerSet().getCompilerSet() != null) {
            // if conf.getCompilerSet().getCompilerSet() == null and we write this to makefile,
            // make would give confusing error message (see IZ#168540)
            bw.write("\n"); // NOI18N
            bw.write("# Enable dependency checking\n"); // NOI18N
            bw.write(".dep.inc: .depcheck-impl\n"); // NOI18N
            bw.write("\n"); // NOI18N
            bw.write("include .dep.inc\n"); // NOI18N
        }
    }

    private static String getOutput(MakeConfiguration conf) {
        String output = conf.getOutputValue();
        switch (conf.getDevelopmentHost().getBuildPlatform()) {
            case PlatformTypes.PLATFORM_WINDOWS:
                switch (conf.getConfigurationType().getValue()) {
                    case MakeConfiguration.TYPE_APPLICATION:
                    case MakeConfiguration.TYPE_QT_APPLICATION:
                        output = mangleAppnameWin(output);
                        break;
                }
                break;
        }
        return conf.expandMacros(output);
    }

    private static String mangleAppnameWin(String original) {
        if (original.endsWith(".exe")) { // NOI18N
            return original;
        } else {
            return original + ".exe"; // NOI18N
        }
    }

    public static String getObjectDir(MakeConfiguration conf) {
        return MakeConfiguration.BUILD_FOLDER + '/' + "${CND_CONF}" + '/' + "${CND_PLATFORM}"; // UNIX path // NOI18N
    }

    private static String getObjectFiles(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf) {
        Item[] items = projectDescriptor.getProjectItems();
        StringBuilder linkObjects = new StringBuilder();
        if (conf.isCompileConfiguration()) {
            for (int x = 0; x < items.length; x++) {
                final Folder folder = items[x].getFolder();
                if (folder.isTest() || folder.isTestLogicalFolder() || folder.isTestRootFolder()) {
                    continue;
                }
                ItemConfiguration itemConfiguration = items[x].getItemConfiguration(conf); //ItemConfiguration)conf.getAuxObject(ItemConfiguration.getId(items[x].getPath()));
                //String commandLine = ""; // NOI18N
                if (itemConfiguration.getExcluded().getValue()) {
                    continue;
                }
                if (!itemConfiguration.isCompilerToolConfiguration()) {
                    continue;
                }
                if (items[x].hasHeaderOrSourceExtension(false, false)) {
                    continue;
                }
                BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
                linkObjects.append(" \\\n\t"); // NOI18N
                linkObjects.append(compilerConfiguration.getOutputFile(items[x], conf, false));
            }
        }
        return linkObjects.toString();
    }

    private static String getTestTargetFiles(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf) {
        StringBuilder testTargets = new StringBuilder();
        if (conf.isCompileConfiguration()) {
            CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
            for (Folder folder : getTests(projectDescriptor)) {
                testTargets.append(" \\\n\t"); // NOI18N
                String target = "${TESTDIR}/" + CndPathUtilitities.escapeOddCharacters(CppUtils.normalizeDriveLetter(compilerSet, folder.getPath())); // NOI18N
                testTargets.append(target);
            }
        }
        return testTargets.toString();
    }

    private void writeMakefileVariables(MakeConfigurationDescriptor conf) {
        String outputFileName = projectDescriptor.getBaseDir() + '/' + MakeConfiguration.NBPROJECT_FOLDER + '/' + "Makefile-variables.mk"; // UNIX path // NOI18N


        FileOutputStream os = null;
        try {
            os = new FileOutputStream(outputFileName);
        } catch (Exception e) {
            // FIXUP
        }
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
        try {
            writeMakefileVariablesBody(bw, conf);
            bw.flush();
            bw.close();
        } catch (IOException e) {
            // FIXUP
        }
    }

    private void writeMakefileVariablesBody(BufferedWriter bw, MakeConfigurationDescriptor conf) throws IOException {
        bw.write("#\n"); // NOI18N
        bw.write("# Generated - do not edit!\n"); // NOI18N
        bw.write("#\n"); // NOI18N
        bw.write("# NOCDDL\n"); // NOI18N
        bw.write("#\n"); // NOI18N
        bw.write("CND_BASEDIR=`pwd`\n"); // NOI18N
        bw.write("CND_BUILDDIR=" + MakeConfiguration.BUILD_FOLDER + "\n"); // NOI18N
        bw.write("CND_DISTDIR=" + MakeConfiguration.DIST_FOLDER + "\n"); // NOI18N

        Configuration[] confs = projectDescriptor.getConfs().toArray();
        for (int i = 0; i < confs.length; i++) {
            MakeConfiguration makeConf = (MakeConfiguration) confs[i];
            bw.write("# " + makeConf.getName() + " configuration"); // NOI18N
            bw.write("\n"); // NOI18N // NOI18N
            bw.write("CND_PLATFORM_" + makeConf.getName() + "=" + makeConf.getVariant()); // NOI18N
            bw.write("\n"); // NOI18N // NOI18N
            // output artifact
            String outputPath = makeConf.expandMacros(makeConf.getOutputValue());
            String outputDir = CndPathUtilitities.getDirName(outputPath);
            if (outputDir == null) {
                outputDir = ""; // NOI18N
            }
            String outputName = CndPathUtilitities.getBaseName(outputPath);
            bw.write("CND_ARTIFACT_DIR_" + makeConf.getName() + "=" + outputDir); // NOI18N
            bw.write("\n"); // NOI18N
            bw.write("CND_ARTIFACT_NAME_" + makeConf.getName() + "=" + outputName); // NOI18N
            bw.write("\n"); // NOI18N
            bw.write("CND_ARTIFACT_PATH_" + makeConf.getName() + "=" + outputPath); // NOI18N
            bw.write("\n"); // NOI18N
            // packaging artifact
            PackagerDescriptor packager = PackagerManager.getDefault().getPackager(makeConf.getPackagingConfiguration().getType().getValue());
            outputPath = makeConf.expandMacros(makeConf.getPackagingConfiguration().getOutputValue());
            if (!packager.isOutputAFolder()) {
                outputDir = CndPathUtilitities.getDirName(outputPath);
                if (outputDir == null) {
                    outputDir = ""; // NOI18N
                }
                outputName = CndPathUtilitities.getBaseName(outputPath);
            } else {
                outputDir = outputPath;
                outputPath = ""; // NOI18N
                outputName = ""; // NOI18N
            }
            bw.write("CND_PACKAGE_DIR_" + makeConf.getName() + "=" + outputDir); // NOI18N
            bw.write("\n"); // NOI18N
            bw.write("CND_PACKAGE_NAME_" + makeConf.getName() + "=" + outputName); // NOI18N
            bw.write("\n"); // NOI18N
            bw.write("CND_PACKAGE_PATH_" + makeConf.getName() + "=" + outputPath); // NOI18N
            bw.write("\n"); // NOI18N
        }
    }

    private void writePackagingScript(MakeConfiguration conf) {
        String outputFileName = projectDescriptor.getBaseDir() + '/' + MakeConfiguration.NBPROJECT_FOLDER + '/' + "Package-" + conf.getName() + ".bash"; // UNIX path // NOI18N

        if (conf.getPackagingConfiguration().getFiles().getValue().isEmpty()) {
            // Nothing to do
            return;
        }

        PackagerDescriptor packager = PackagerManager.getDefault().getPackager(conf.getPackagingConfiguration().getType().getValue());
        if (packager == null || packager instanceof DummyPackager) {
            return;
        }

        FileOutputStream os = null;
        try {
            os = new FileOutputStream(outputFileName);
        } catch (Exception e) {
            // FIXUP
        }
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
        try {
            writePackagingScriptBody(bw, conf);
            bw.flush();
            bw.close();
        } catch (IOException e) {
            // FIXUP
        }
    }

    private void writePackagingScriptBody(BufferedWriter bw, MakeConfiguration conf) throws IOException {
        String tmpDirName = "tmp-packaging"; // NOI18N
        String tmpdir = getObjectDir(conf) + "/" + tmpDirName; // NOI18N
        PackagingConfiguration packagingConfiguration = conf.getPackagingConfiguration();
        String output = packagingConfiguration.getOutputValue();
        PackagerDescriptor packager = PackagerManager.getDefault().getPackager(packagingConfiguration.getType().getValue());

        bw.write("#!/bin/bash"); // NOI18N
        if (conf.getPackagingConfiguration().getVerbose().getValue()) {
            bw.write(" -x"); // NOI18N
        }
        bw.write("\n"); // NOI18N
        bw.write("\n"); // NOI18N
        bw.write("#\n"); // NOI18N
        bw.write("# Generated - do not edit!\n"); // NOI18N
        bw.write("#\n"); // NOI18N
        bw.write("\n"); // NOI18N

        bw.write("# Macros\n"); // NOI18N
        bw.write("TOP=" + "`pwd`" + "\n"); // NOI18N
        bw.write("CND_PLATFORM=" + conf.getVariant() + "\n"); // NOI18N
        bw.write("CND_CONF=" + conf.getName() + "\n"); // NOI18N
        bw.write("CND_DISTDIR=" + MakeConfiguration.DIST_FOLDER + "\n"); // NOI18N
        bw.write("NBTMPDIR=" + tmpdir + "\n"); // NOI18N
        bw.write("TMPDIRNAME=" + tmpDirName + "\n"); // NOI18N
        String projectOutput = conf.getOutputValue();
        if (projectOutput == null || projectOutput.length() == 0) {
            projectOutput = "MissingOutputInProject"; // NOI18N
        }
        bw.write("OUTPUT_PATH=" + projectOutput + "\n"); // NOI18N
        bw.write("OUTPUT_BASENAME=" + CndPathUtilitities.getBaseName(projectOutput) + "\n"); // NOI18N
        bw.write("PACKAGE_TOP_DIR=" + (packagingConfiguration.getTopDirValue().length() > 0 ? packagingConfiguration.getTopDirValue() + "/" : "") + "\n"); // NOI18N
        bw.write("\n"); // NOI18N

        bw.write("# Functions\n"); // NOI18N
        bw.write("function checkReturnCode\n"); // NOI18N
        bw.write("{\n"); // NOI18N
        bw.write("    rc=$?\n"); // NOI18N
        bw.write("    if [ $rc != 0 ]\n"); // NOI18N
        bw.write("    then\n"); // NOI18N
        bw.write("        exit $rc\n"); // NOI18N
        bw.write("    fi\n"); // NOI18N
        bw.write("}\n"); // NOI18N
        bw.write("function makeDirectory\n"); // NOI18N
        bw.write("# $1 directory path\n"); // NOI18N
        bw.write("# $2 permission (optional)\n"); // NOI18N
        bw.write("{\n"); // NOI18N
        bw.write("    mkdir -p \"$1\"\n"); // NOI18N
        bw.write("    checkReturnCode\n"); // NOI18N
        bw.write("    if [ \"$2\" != \"\" ]\n"); // NOI18N
        bw.write("    then\n"); // NOI18N
        bw.write("      chmod $2 \"$1\"\n"); // NOI18N
        bw.write("      checkReturnCode\n"); // NOI18N
        bw.write("    fi\n"); // NOI18N
        bw.write("}\n"); // NOI18N
        bw.write("function copyFileToTmpDir\n"); // NOI18N
        bw.write("# $1 from-file path\n"); // NOI18N
        bw.write("# $2 to-file path\n"); // NOI18N
        bw.write("# $3 permission\n"); // NOI18N
        bw.write("{\n"); // NOI18N
        bw.write("    cp \"$1\" \"$2\"\n"); // NOI18N
        bw.write("    checkReturnCode\n"); // NOI18N
        bw.write("    if [ \"$3\" != \"\" ]\n"); // NOI18N
        bw.write("    then\n"); // NOI18N
        bw.write("        chmod $3 \"$2\"\n"); // NOI18N
        bw.write("        checkReturnCode\n"); // NOI18N
        bw.write("    fi\n"); // NOI18N
        bw.write("}\n"); // NOI18N

        bw.write("\n"); // NOI18N
        bw.write("# Setup\n"); // NOI18N
        bw.write("cd \"${TOP}\"\n"); // NOI18N

        if (packager.isOutputAFolder()) {
            bw.write("mkdir -p " + output + "\n"); // NOI18N
        } else {
            bw.write("mkdir -p " + CndPathUtilitities.getDirName(output) + "\n"); // NOI18N
        }
        bw.write("rm -rf ${NBTMPDIR}\n"); // NOI18N
        bw.write("mkdir -p ${NBTMPDIR}\n"); // NOI18N
        bw.write("\n"); // NOI18N

        packager.getShellFileWriter().writeShellScript(bw, conf, packagingConfiguration);

        bw.write("# Cleanup\n"); // NOI18N
        bw.write("cd \"${TOP}\"\n"); // NOI18N
        bw.write("rm -rf ${NBTMPDIR}\n"); // NOI18N
    }

    private static boolean hasTests(MakeConfigurationDescriptor projectDescriptor) {
        return !getTests(projectDescriptor).isEmpty();
    }

    private static List<Folder> getTests(MakeConfigurationDescriptor projectDescriptor) {
        Folder root = projectDescriptor.getLogicalFolders();
        Folder testRootFolder = null;
        for (Folder folder : root.getFolders()) {
            if (folder.isTestRootFolder()) {
                testRootFolder = folder;
                break;
            }
        }
        if (testRootFolder != null) {
            return testRootFolder.getAllTests();
        }
        return Collections.<Folder>emptyList();
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(ConfigurationMakefileWriter.class, s);
    }
    private static String getString(String s, String arg1) {
        return NbBundle.getMessage(ConfigurationMakefileWriter.class, s, arg1);
    }
    private static String getString(String s, String arg1, String arg2) {
        return NbBundle.getMessage(ConfigurationMakefileWriter.class, s, arg1, arg2);
    }
}
