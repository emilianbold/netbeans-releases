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
package org.netbeans.modules.cnd.makeproject.api.configurations;

import org.netbeans.modules.cnd.makeproject.spi.configurations.AllOptionsProvider;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.LinkerDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.BooleanNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.LibrariesNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.OptionsNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.StringNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.VectorNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.CppUtils;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCCompilerConfiguration.OptionToString;
import org.netbeans.modules.cnd.makeproject.platform.Platforms;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class LinkerConfiguration implements AllOptionsProvider {

    private MakeConfiguration makeConfiguration;
    private StringConfiguration output;
    private VectorConfiguration<String> additionalLibs;
    private VectorConfiguration<String> dynamicSearch;
    private BooleanConfiguration stripOption;
    private BooleanConfiguration picOption;
    private BooleanConfiguration norunpathOption;
    private BooleanConfiguration nameassignOption;
    private OptionsConfiguration commandLineConfiguration;
    private OptionsConfiguration additionalDependencies;
    private LibrariesConfiguration librariesConfiguration;
    private StringConfiguration tool;

    // Constructors
    public LinkerConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
        output = new StringConfiguration(null, ""); // NOI18N
        additionalLibs = new VectorConfiguration<String>(null);
        dynamicSearch = new VectorConfiguration<String>(null);
        stripOption = new BooleanConfiguration(false); // NOI18N
        picOption = new BooleanConfiguration(true); // NOI18N
        norunpathOption = new BooleanConfiguration(true); // NOI18N
        nameassignOption = new BooleanConfiguration(true);
        commandLineConfiguration = new OptionsConfiguration();
        additionalDependencies = new OptionsConfiguration();
        additionalDependencies.setPreDefined(getAdditionalDependenciesPredefined());
        librariesConfiguration = new LibrariesConfiguration();
        tool = new StringConfiguration(null, ""); // NOI18N
    }

    private String getAdditionalDependenciesPredefined() {
        String pd = "${BUILD_SUBPROJECTS} ${OBJECTFILES}"; // NOI18N
        return pd;
    }

    // MakeConfiguration
    public void setMakeConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
    }

    public MakeConfiguration getMakeConfiguration() {
        return makeConfiguration;
    }

    // Output
    public void setOutput(StringConfiguration output) {
        this.output = output;
    }

    public StringConfiguration getOutput() {
        return output;
    }

    // Additional Libraries
    public VectorConfiguration<String> getAdditionalLibs() {
        return additionalLibs;
    }

    public void setAdditionalLibs(VectorConfiguration<String> additionalLibs) {
        this.additionalLibs = additionalLibs;
    }

    // Dynamic Search
    public VectorConfiguration<String> getDynamicSearch() {
        return dynamicSearch;
    }

    public void setDynamicSearch(VectorConfiguration<String> dynamicSearch) {
        this.dynamicSearch = dynamicSearch;
    }

    // Strip
    public void setStripOption(BooleanConfiguration stripOption) {
        this.stripOption = stripOption;
    }

    public BooleanConfiguration getStripOption() {
        return stripOption;
    }

    // Kpic
    public void setPICOption(BooleanConfiguration picOption) {
        this.picOption = picOption;
    }

    public BooleanConfiguration getPICOption() {
        return picOption;
    }

    // Norunpath
    public void setNorunpathOption(BooleanConfiguration norunpathOption) {
        this.norunpathOption = norunpathOption;
    }

    public BooleanConfiguration getNorunpathOption() {
        return norunpathOption;
    }

    // Name Assign
    public void setNameassignOption(BooleanConfiguration nameassignOption) {
        this.nameassignOption = nameassignOption;
    }

    public BooleanConfiguration getNameassignOption() {
        return nameassignOption;
    }

    // CommandLine
    public OptionsConfiguration getCommandLineConfiguration() {
        return commandLineConfiguration;
    }

    public void setCommandLineConfiguration(OptionsConfiguration commandLineConfiguration) {
        this.commandLineConfiguration = commandLineConfiguration;
    }

    // Additional Dependencies
    public OptionsConfiguration getAdditionalDependencies() {
        return additionalDependencies;
    }

    public void setAdditionalDependencies(OptionsConfiguration additionalDependencies) {
        this.additionalDependencies = additionalDependencies;
    }

    // LibrariesConfiguration
    public LibrariesConfiguration getLibrariesConfiguration() {
        return librariesConfiguration;
    }

    public void setLibrariesConfiguration(LibrariesConfiguration librariesConfiguration) {
        this.librariesConfiguration = librariesConfiguration;
    }

    // Tool
    public void setTool(StringConfiguration tool) {
        this.tool = tool;
    }

    public StringConfiguration getTool() {
        return tool;
    }


    // Clone and assign
    public void assign(LinkerConfiguration conf) {
        // LinkerConfiguration
        //setMakeConfiguration(conf.getMakeConfiguration()); // MakeConfiguration should not be assigned
        getOutput().assign(conf.getOutput());
        getAdditionalLibs().assign(conf.getAdditionalLibs());
        getDynamicSearch().assign(conf.getDynamicSearch());
        getCommandLineConfiguration().assign(conf.getCommandLineConfiguration());
        getAdditionalDependencies().assign(conf.getAdditionalDependencies());
        getStripOption().assign(conf.getStripOption());
        getPICOption().assign(conf.getPICOption());
        getNorunpathOption().assign(conf.getNorunpathOption());
        getNameassignOption().assign(conf.getNameassignOption());
        getLibrariesConfiguration().assign(conf.getLibrariesConfiguration());
        getTool().assign(conf.getTool());
    }

    @Override
    public LinkerConfiguration clone() {
        LinkerConfiguration clone = new LinkerConfiguration(getMakeConfiguration());
        // LinkerConfiguration
        clone.setOutput(getOutput().clone());
        clone.setAdditionalLibs(getAdditionalLibs().clone());
        clone.setDynamicSearch(getDynamicSearch().clone());
        clone.setCommandLineConfiguration(getCommandLineConfiguration().clone());
        clone.setAdditionalDependencies(getAdditionalDependencies().clone());
        clone.setStripOption(getStripOption().clone());
        clone.setPICOption(getPICOption().clone());
        clone.setNorunpathOption(getNorunpathOption().clone());
        clone.setNameassignOption(getNameassignOption().clone());
        clone.setLibrariesConfiguration(getLibrariesConfiguration().clone());
        clone.setTool(getTool().clone());
        return clone;
    }

    public String getOptions() {
        String options = getCommandLineConfiguration().getValue() + " "; // NOI18N
        options += getBasicOptions() + " "; // NOI18N
        return CppUtils.reformatWhitespaces(options);
    }

    public String getBasicOptions() {
        String options = ""; // NOI18N
        CompilerSet cs = getMakeConfiguration().getCompilerSet().getCompilerSet();
        if (getMakeConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_DYNAMIC_LIB) {
            String libName = getOutputValue();
            int sep = libName.lastIndexOf('/');
            if (sep >= 0 && libName.length() > 1) {
                libName = libName.substring(sep + 1);
            }
            // FIXUP: should be move to Platform...
            if (cs != null) {
                options += cs.getCompilerFlavor().getToolchainDescriptor().getLinker().getDynamicLibraryBasicFlag();
                if (cs.getCompilerFlavor().isGnuCompiler() && getMakeConfiguration().getDevelopmentHost().getBuildPlatform() == PlatformTypes.PLATFORM_MACOSX) {
                    options += libName + " "; // NOI18N
                }
            }
        }
        if (cs != null) {
            options += cs.getCompilerFlavor().getToolchainDescriptor().getLinker().getOutputFileFlag() + getOutputValue() + " "; // NOI18N
        }
        if (cs != null && getStripOption().getValue()) {
            options += cs.getCompilerFlavor().getToolchainDescriptor().getLinker().getStripFlag() + " "; // NOI18N
        }
        if (getMakeConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_DYNAMIC_LIB) {
            // FIXUP: should move to Platform
            if (cs != null) {
                if (getPICOption().getValue()) {
                    options += getPICOption(cs);
                }
                if (cs.getCompilerFlavor().isSunStudioCompiler()) {
                    if (getNorunpathOption().getValue()) {
                        options += "-norunpath "; // NOI18N
                    }
                    options += getNameassignOption(getNameassignOption().getValue()) + " "; // NOI18N
                }
            }
        }
        return CppUtils.reformatWhitespaces(options);
    }

    public String getPICOption(CompilerSet cs) {
        LinkerDescriptor linker = cs.getCompilerFlavor().getToolchainDescriptor().getLinker();
        if (linker != null) {
            return linker.getPICFlag();
        }
        return null;
    }

    public String getLibraryItems() {
        CompilerSet cs = getMakeConfiguration().getCompilerSet().getCompilerSet();
        LinkerDescriptor linker = cs == null ? null : cs.getCompilerFlavor().getToolchainDescriptor().getLinker();
        if (linker == null) {
            return ""; // NOI18N
        }
        String options = ""; // NOI18N
        OptionToString staticSearchVisitor = new OptionToString(cs, linker.getLibrarySearchFlag());
        options += getAdditionalLibs().toString(staticSearchVisitor) + " "; // NOI18N
        if (linker.getDynamicLibrarySearchFlag().length() > 0) {
            OptionToString dynamicSearchVisitor = new OptionToString(cs, linker.getDynamicLibrarySearchFlag());
            options += getDynamicSearch().toString(dynamicSearchVisitor) + " "; // NOI18N
        }
        LibraryToString libVisitor = new LibraryToString(getMakeConfiguration());
        options += getLibrariesConfiguration().toString(libVisitor) + " "; // NOI18N
        return CppUtils.reformatWhitespaces(options);
    }

    // Interface OptionsProvider
    @Override
    public String getAllOptions(Tool tool) {
        String options = getBasicOptions() + " "; // NOI18N
        options += getLibraryItems() + " "; // NOI18N
        return CppUtils.reformatWhitespaces(options);
    }

    // Sheet
    public Sheet getGeneralSheet(Project project, MakeConfigurationDescriptor configurationDescriptor, MakeConfiguration conf, boolean isQtMode) {
        Sheet sheet = new Sheet();
        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        LinkerDescriptor linker = null;
        String linkDriver = null;
        String[] texts = null;
        if (compilerSet != null) {
            linker = compilerSet == null ? null : compilerSet.getCompilerFlavor().getToolchainDescriptor().getLinker();
            if (conf.hasCPPFiles(configurationDescriptor)) {
                AbstractCompiler ccCompiler = (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCCompiler);
                linkDriver = ccCompiler.getName();
            } else {
                AbstractCompiler cCompiler = (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCompiler);
                linkDriver = cCompiler.getName();
            }
        }

        Sheet.Set set1 = new Sheet.Set();
        set1.setName("General"); // NOI18N
        set1.setDisplayName(getString("GeneralTxt"));
        set1.setShortDescription(getString("GeneralHint"));
        if (!isQtMode) {
            set1.put(new OutputNodeProp(getOutput(), getOutputDefault(), "Output", getString("OutputTxt"), getString("OutputHint"))); // NOI18N
            set1.put(new VectorNodeProp(getAdditionalLibs(), null, getMakeConfiguration().getBaseDir(), new String[]{"AdditionalLibraryDirectories", getString("AdditionalLibraryDirectoriesTxt"), getString("AdditionalLibraryDirectoriesHint")}, true, new HelpCtx("AddtlLibraryDirectories"))); // NOI18N
        }
        if (linker != null && linker.getDynamicLibrarySearchFlag().length() > 0) {
            set1.put(new VectorNodeProp(getDynamicSearch(), null, getMakeConfiguration().getBaseDir(), new String[]{"RuntimeSearchDirectories", getString("RuntimeSearchDirectoriesTxt"), getString("RuntimeSearchDirectoriesHint")}, false, new HelpCtx("RuntimeSearchDirectories"))); // NOI18N
        }
        sheet.put(set1);
        if (!isQtMode) {
            Sheet.Set set2 = new Sheet.Set();
            set2.setName("Options"); // NOI18N
            set2.setDisplayName(getString("OptionsTxt"));
            set2.setShortDescription(getString("OptionsHint"));
            set2.put(new BooleanNodeProp(getStripOption(), true, "StripSymbols", getString("StripSymbolsTxt"), getString("StripSymbolsHint"))); // NOI18N
            if (conf.getConfigurationType().getValue() == MakeConfiguration.TYPE_DYNAMIC_LIB) {
                set2.put(new BooleanNodeProp(getPICOption(), true, "PositionIndependantCode", getString("PositionIndependantCodeTxt"), getString("PositionIndependantCodeHint"))); // NOI18N
                if (compilerSet != null && compilerSet.getCompilerFlavor().isSunStudioCompiler()) {
                    set2.put(new BooleanNodeProp(getNorunpathOption(), true, "NoRunPath", getString("NoRunPathTxt"), getString("NoRunPathHint"))); // NOI18N
                    set2.put(new BooleanNodeProp(getNameassignOption(), true, "AssignName", getString("AssignNameTxt"), getString("AssignNameHint"))); // NOI18N
                }
            }
            sheet.put(set2);
            Sheet.Set set3 = new Sheet.Set();
            texts = new String[]{getString("AdditionalDependenciesTxt1"), getString("AdditionalDependenciesHint"), getString("AdditionalDependenciesTxt2"), getString("InheritedValuesTxt")};
            set3.setName("Input"); // NOI18N
            set3.setDisplayName(getString("InputTxt"));
            set3.setShortDescription(getString("InputHint"));
            set3.put(new OptionsNodeProp(getAdditionalDependencies(), null, new AdditionalDependenciesOptions(), null, ",", texts)); // NOI18N
            sheet.put(set3);
            Sheet.Set set4 = new Sheet.Set();
            set4.setName("Tool"); // NOI18N
            set4.setDisplayName(getString("ToolTxt1"));
            set4.setShortDescription(getString("ToolHint1"));
            if (linkDriver != null) {
                set4.put(new StringNodeProp(getTool(), linkDriver, "Tool", getString("ToolTxt1"), getString("ToolHint1"))); // NOI18N
            }
            sheet.put(set4);
        }

        texts = new String[]{getString("LibrariesTxt1"), getString("LibrariesHint"), getString("LibrariesTxt2"), getString("AllOptionsTxt2")};
        Sheet.Set set5 = new Sheet.Set();
        set5.setName("Libraries"); // NOI18N
        set5.setDisplayName(getString("LibrariesTxt1"));
        set5.setShortDescription(getString("LibrariesHint"));
        set5.put(new LibrariesNodeProp(getLibrariesConfiguration(), project, conf, getMakeConfiguration().getBaseDir(), texts));
        sheet.put(set5);

        if (!isQtMode) {
            texts = new String[]{getString("AdditionalOptionsTxt1"), getString("AdditionalOptionsHint"), getString("AdditionalOptionsTxt2"), getString("AllOptionsTxt")}; // NOI18N
            Sheet.Set set6 = new Sheet.Set();
            set6.setName("CommandLine"); // NOI18N
            set6.setDisplayName(getString("CommandLineTxt"));
            set6.setShortDescription(getString("CommandLineHint"));
            set6.put(new OptionsNodeProp(getCommandLineConfiguration(), null, this, null, null, texts));
            sheet.put(set6);
        }

        return sheet;
    }

    private final class AdditionalDependenciesOptions implements AllOptionsProvider {

        @Override
        public String getAllOptions(Tool tool) {
            String options = ""; // NOI18N
            options += additionalDependencies.getPreDefined();
            return CppUtils.reformatWhitespaces(options);
        }
    }

    private String getNameassignOption(boolean val) {
        if (val) {
            return "-h " + CndPathUtilitities.getBaseName(getOutputValue()); // NOI18N
        } else {
            return ""; // NOI18N
        }
    }

    public String getOutputValue() {
        if (getOutput().getModified()) {
            return getOutput().getValue();
        } else {
            return getOutputDefault();
        }
    }

    private String getOutputDefault() {
        String outputName = CndPathUtilitities.getBaseName(getMakeConfiguration().getBaseDir());
        switch (getMakeConfiguration().getConfigurationType().getValue()) {
            case MakeConfiguration.TYPE_APPLICATION:
                outputName = outputName.toLowerCase();
                break;
            case MakeConfiguration.TYPE_DYNAMIC_LIB:
                outputName = Platforms.getPlatform(getMakeConfiguration().getDevelopmentHost().getBuildPlatform()).getLibraryName(outputName);
                break;
        }
        outputName = ConfigurationSupport.makeNameLegal(outputName);
        return "${CND_DISTDIR}" + "/" + "${CND_CONF}" + "/" + "${CND_PLATFORM}" + "/" + outputName; // NOI18N
    }

    public String getOutputDefault27() {
        String outputName = CndPathUtilitities.getBaseName(getMakeConfiguration().getBaseDir());
        if (getMakeConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_APPLICATION) {
            outputName = outputName.toLowerCase();
        } else if (getMakeConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_DYNAMIC_LIB) {
            outputName = "lib" + outputName + ".so"; // NOI18N
        }
        return MakeConfiguration.DIST_FOLDER + "/" + getMakeConfiguration().getName() + "/" + outputName; // NOI18N
    }

    private static class OutputNodeProp extends StringNodeProp {

        public OutputNodeProp(StringConfiguration stringConfiguration, String def, String txt1, String txt2, String txt3) {
            super(stringConfiguration, def, txt1, txt2, txt3);
        }

        @Override
        public void setValue(String v) {
            if (CndPathUtilitities.hasMakeSpecialCharacters(v)) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(getString("SPECIAL_CHARATERS_ERROR"), NotifyDescriptor.ERROR_MESSAGE));
                return;
            }
            super.setValue(v);
        }
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(LinkerConfiguration.class, s);
    }

    public static class LibraryToString implements VectorConfiguration.ToString<LibraryItem> {

        private final MakeConfiguration conf;

        public LibraryToString(MakeConfiguration conf) {
            this.conf = conf;
        }

        @Override
        public String toString(LibraryItem item) {
            return item.getOption(conf);
        }
    }
}
