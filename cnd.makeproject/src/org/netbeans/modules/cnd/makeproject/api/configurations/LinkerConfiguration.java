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

package org.netbeans.modules.cnd.makeproject.api.configurations;

import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.compilers.CompilerSets;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platform;
import org.netbeans.modules.cnd.makeproject.configurations.ui.BooleanNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.LibrariesNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.OptionsNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.StringNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.VectorNodeProp;
import org.netbeans.modules.cnd.api.utils.CppUtils;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.api.compilers.BasicCompiler;
import org.netbeans.modules.cnd.makeproject.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.compilers.Tool;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platforms;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

public class LinkerConfiguration implements AllOptionsProvider {
    private MakeConfiguration makeConfiguration;

    private StringConfiguration output;
    private VectorConfiguration additionalLibs;
    private VectorConfiguration dynamicSearch;
    private BooleanConfiguration stripOption;
    private BooleanConfiguration kpicOption;
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
	additionalLibs = new VectorConfiguration(null);
	dynamicSearch = new VectorConfiguration(null);
	stripOption = new BooleanConfiguration(null, false, "", "-s"); // NOI18N
	kpicOption = new BooleanConfiguration(null, true, "", "-Kpic"); // NOI18N
	norunpathOption = new BooleanConfiguration(null, true, "", "-norunpath"); // NOI18N
	nameassignOption = new BooleanConfiguration(null, true);
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
    public void setMakeConfiguration(MakeConfiguration MakeConfiguration) {
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
    public VectorConfiguration getAdditionalLibs() {
	return additionalLibs;
    }

    public void setAdditionalLibs(VectorConfiguration additionalLibs) {
	this.additionalLibs = additionalLibs;
    }

    // Dynamic Search
    public VectorConfiguration getDynamicSearch() {
	return dynamicSearch;
    }

    public void setDynamicSearch(VectorConfiguration dynamicSearch) {
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
    public void setKpicOption(BooleanConfiguration kpicOption) {
	this.kpicOption = kpicOption;
    }
    public BooleanConfiguration getKpicOption() {
	return kpicOption;
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
	setMakeConfiguration(conf.getMakeConfiguration());
	getOutput().assign(conf.getOutput());
	getAdditionalLibs().assign(conf.getAdditionalLibs());
	getDynamicSearch().assign(conf.getDynamicSearch());
	getCommandLineConfiguration().assign(conf.getCommandLineConfiguration());
	getAdditionalDependencies().assign(conf.getAdditionalDependencies());
	getStripOption().assign(conf.getStripOption());
	getKpicOption().assign(conf.getKpicOption());
	getNorunpathOption().assign(conf.getNorunpathOption());
	getNameassignOption().assign(conf.getNameassignOption());
	getLibrariesConfiguration().assign(conf.getLibrariesConfiguration());
	getTool().assign(conf.getTool());
    }

    public Object clone() {
	LinkerConfiguration clone = new LinkerConfiguration(getMakeConfiguration());
	// LinkerConfiguration
	clone.setOutput((StringConfiguration)getOutput().clone());
	clone.setAdditionalLibs((VectorConfiguration)getAdditionalLibs().clone());
	clone.setDynamicSearch((VectorConfiguration)getDynamicSearch().clone());
	clone.setCommandLineConfiguration((OptionsConfiguration)getCommandLineConfiguration().clone());
	clone.setAdditionalDependencies((OptionsConfiguration)getAdditionalDependencies().clone());
	clone.setStripOption((BooleanConfiguration)getStripOption().clone());
	clone.setKpicOption((BooleanConfiguration)getKpicOption().clone());
	clone.setNorunpathOption((BooleanConfiguration)getNorunpathOption().clone());
	clone.setNameassignOption((BooleanConfiguration)getNameassignOption().clone());
	clone.setLibrariesConfiguration((LibrariesConfiguration)getLibrariesConfiguration().clone());
	clone.setTool((StringConfiguration)getTool().clone());
	return clone;
    }

    public String getOptions() {
	String options = getCommandLineConfiguration().getValue() + " "; // NOI18N
	options += getBasicOptions() + " "; // NOI18N
	return CppUtils.reformatWhitespaces(options);
    }

    public String getBasicOptions() {
	String options = ""; // NOI18N
	if (getMakeConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_DYNAMIC_LIB ) {
            if (getMakeConfiguration().getCompilerSet().getValue() == CompilerSets.SUN_COMPILER_SET)
                options += "-G "; // NOI18N
            else if (getMakeConfiguration().getCompilerSet().getValue() == CompilerSets.GNU_COMPILER_SET)
                options += "-shared "; // NOI18N
            else
                assert false;
        }
	options += getOutputOptions() + " "; // NOI18N
	options += getStripOption().getOption() + " "; // NOI18N
	if (getMakeConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_DYNAMIC_LIB && // FIXUP: should move to Platform
                getMakeConfiguration().getCompilerSet().getValue() == CompilerSets.SUN_COMPILER_SET) {
	    options += getKpicOption().getOption() + " "; // NOI18N
	    options += getNorunpathOption().getOption() + " "; // NOI18N
	    options += getNameassignOption(getNameassignOption().getValue()) + " "; // NOI18N
	}
	return CppUtils.reformatWhitespaces(options);
    }

    public String getLibraryItems() {
        String libPrefix = "-L"; // NOI18N
        String dynSearchPrefix = ""; // NOI18N
        if (getMakeConfiguration().getCompilerSet().getValue() == CompilerSets.SUN_COMPILER_SET)
            dynSearchPrefix = "-R"; // NOI18N
        else if (getMakeConfiguration().getCompilerSet().getValue() == CompilerSets.GNU_COMPILER_SET)
            dynSearchPrefix = "-Wl,-rpath "; // NOI18N
        else
            assert false;
	String options = ""; // NOI18N
	options += getAdditionalLibs().getOption(libPrefix) + " "; // NOI18N
	options += getDynamicSearch().getOption(dynSearchPrefix) + " "; // NOI18N
	options += getLibrariesConfiguration().getOptions(getMakeConfiguration()) + " "; // NOI18N
	return CppUtils.reformatWhitespaces(options);
    }

    // Interface OptionsProvider
    public String getAllOptions(BasicCompiler compiler) {
	String options = getBasicOptions() + " "; // NOI18N
	options += getLibraryItems() + " "; // NOI18N
	return CppUtils.reformatWhitespaces(options);
    }

    // Sheet
    public Sheet getGeneralSheet(MakeConfigurationDescriptor configurationDescriptor, MakeConfiguration conf) {
	Sheet sheet = new Sheet();
        CompilerSet compilerSet = CompilerSets.getCompilerSet(conf.getCompilerSet().getValue());
        String linkDriver;
        if (conf.hasCPPFiles(configurationDescriptor)) {
            BasicCompiler ccCompiler = (BasicCompiler)compilerSet.getTool(Tool.CCCompiler);
            linkDriver = ccCompiler.getName();
        }
        else {
            BasicCompiler cCompiler = (BasicCompiler)compilerSet.getTool(Tool.CCompiler);
            linkDriver = cCompiler.getName();
        }
        
	Sheet.Set set1 = new Sheet.Set();
	set1.setName("General"); // NOI18N
	set1.setDisplayName(getString("GeneralTxt"));
	set1.setShortDescription(getString("GeneralHint"));
	set1.put(new StringNodeProp(getOutput(), getOutputDefault(), "Output", getString("OutputTxt"), getString("OutputHint"))); // NOI18N
	set1.put(new VectorNodeProp(getAdditionalLibs(), null, getMakeConfiguration().getBaseDir(), new String[] {"AdditionalLibraryDirectories", getString("AdditionalLibraryDirectoriesTxt"), getString("AdditionalLibraryDirectoriesHint")}, true)); // NOI18N
	set1.put(new VectorNodeProp(getDynamicSearch(), null, getMakeConfiguration().getBaseDir(), new String[] {"RuntimeSearchDirectories", getString("RuntimeSearchDirectoriesTxt"), getString("RuntimeSearchDirectoriesHint")}, false)); // NOI18N
	sheet.put(set1);
	Sheet.Set set2 = new Sheet.Set();
	set2.setName("Options"); // NOI18N
	set2.setDisplayName(getString("OptionsTxt"));
	set2.setShortDescription(getString("OptionsHint"));
	set2.put(new BooleanNodeProp(getStripOption(), true, "StripSymbols", getString("StripSymbolsTxt"), getString("StripSymbolsHint"))); // NOI18N
	if (conf.getConfigurationType().getValue() == MakeConfiguration.TYPE_DYNAMIC_LIB && conf.getCompilerSet().getValue() == CompilerSets.SUN_COMPILER_SET) {
	    set2.put(new BooleanNodeProp(getKpicOption(), true, "PositionIndependantCode", getString("PositionIndependantCodeTxt"), getString("PositionIndependantCodeHint"))); // NOI18N
	    set2.put(new BooleanNodeProp(getNorunpathOption(), true, "NoRunPath", getString("NoRunPathTxt"), getString("NoRunPathHint"))); // NOI18N
	    set2.put(new BooleanNodeProp(getNameassignOption(), true, "AssignName", getString("AssignNameTxt"), getString("AssignNameHint"))); // NOI18N
	}
	sheet.put(set2);
	Sheet.Set set3 = new Sheet.Set();
	String [] texts = new String[] {getString("AdditionalDependenciesTxt1"), getString("AdditionalDependenciesHint"), getString("AdditionalDependenciesTxt2"), getString("InheritedValuesTxt")};
	set3.setName("Input"); // NOI18N
	set3.setDisplayName(getString("InputTxt"));
	set3.setShortDescription(getString("InputHint"));
	set3.put(new OptionsNodeProp(getAdditionalDependencies(), null, new AdditionalDependenciesOptions(), null, ",", texts)); // NOI18N
	sheet.put(set3);
	Sheet.Set set4 = new Sheet.Set();
	set4.setName("Tool"); // NOI18N
	set4.setDisplayName(getString("ToolTxt1"));
	set4.setShortDescription(getString("ToolHint1"));
	set4.put(new StringNodeProp(getTool(), linkDriver, "Tool", getString("ToolTxt1"), getString("ToolHint1"))); // NOI18N
	sheet.put(set4);
	return sheet;
    }

    class AdditionalDependenciesOptions implements AllOptionsProvider {
	public String getAllOptions(BasicCompiler compiler) {
	    String options = ""; // NOI18N
	    options += additionalDependencies.getPreDefined();
	    return CppUtils.reformatWhitespaces(options);
	}
    }

    public Sheet getLibrariesSheet(Project project, MakeConfiguration conf) {
	Sheet sheet = new Sheet();
	String[] texts = new String[] {getString("LibrariesTxt1"), getString("LibrariesHint"), getString("LibrariesTxt2"), getString("AllOptionsTxt2")};

	Sheet.Set set2 = new Sheet.Set();
	set2.setName("Libraries"); // NOI18N
	set2.setDisplayName(getString("LibrariesTxt1"));
	set2.setShortDescription(getString("LibrariesHint"));
	set2.put(new LibrariesNodeProp(getLibrariesConfiguration(), project, conf, getMakeConfiguration().getBaseDir(), texts));
	sheet.put(set2);

	return sheet;
    }

    public Sheet getCommandLineSheet() {
	Sheet sheet = new Sheet();
	String[] texts = new String[] {getString("AdditionalOptionsTxt1"), getString("AdditionalOptionsHint"), getString("AdditionalOptionsTxt2"), getString("AllOptionsTxt")}; // NOI18N

	Sheet.Set set2 = new Sheet.Set();
	set2.setName("CommandLine"); // NOI18N
	set2.setDisplayName(getString("CommandLineTxt"));
	set2.setShortDescription(getString("CommandLineHint"));
	set2.put(new OptionsNodeProp(getCommandLineConfiguration(), null, this, null, null, texts));
	sheet.put(set2);

	return sheet;
    }

    private String getNameassignOption(boolean val) {
	if (val)
	    return "-h " + IpeUtils.getBaseName(getOutputValue()); // NOI18N
	else
	    return ""; // NOI18N
    }

    private String getOutputOptions() {
	return "-o " + getOutputValue() + " "; // NOI18N
    }

    public String getOutputValue() {
        if (getOutput().getModified())
            return getOutput().getValue();
        else
            return getOutputDefault();
    }

    private String getOutputDefault() {
	String outputName = IpeUtils.getBaseName(getMakeConfiguration().getBaseDir());
	if (getMakeConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_APPLICATION)
	    outputName = outputName.toLowerCase();
	else if (getMakeConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_DYNAMIC_LIB) {
            Platform platform = Platforms.getPlatform(getMakeConfiguration().getPlatform().getValue());
            outputName = platform.getLibraryName(outputName);
        }
        
	return MakeConfiguration.DIST_FOLDER + "/" + getMakeConfiguration().getName() + "/" + getMakeConfiguration().getVariant() + "/" + outputName; // NOI18N 
    }
    
    /*
    private String getOutputDefault30() {
	String outputName = IpeUtils.getBaseName(getMakeConfiguration().getBaseDir());
	if (getMakeConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_APPLICATION)
	    outputName = outputName.toLowerCase();
	else if (getMakeConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_DYNAMIC_LIB)
	    outputName = "lib" + outputName + ".so"; // NOI18N
        
	return MakeConfiguration.DIST_FOLDER + "/" + getMakeConfiguration().getName() + "/" + getMakeConfiguration().getVariant() + "/" + outputName; // NOI18N 
    }
     **/
    
    public String getOutputDefault27() {
	String outputName = IpeUtils.getBaseName(getMakeConfiguration().getBaseDir());
	if (getMakeConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_APPLICATION)
	    outputName = outputName.toLowerCase();
	else if (getMakeConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_DYNAMIC_LIB)
	    outputName = "lib" + outputName + ".so"; // NOI18N
        
	return MakeConfiguration.DIST_FOLDER + "/" + getMakeConfiguration().getName() + "/" + outputName; // NOI18N 
    }
    
    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(LinkerConfiguration.class, s);
    }
}
