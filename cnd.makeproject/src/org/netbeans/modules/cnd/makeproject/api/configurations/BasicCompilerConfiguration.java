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

import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.makeproject.configurations.ConfigurationMakefileWriter;
import org.netbeans.modules.cnd.makeproject.configurations.ui.BooleanNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.IntNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.StringNodeProp;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.api.compilers.BasicCompiler;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

public class BasicCompilerConfiguration {
    private String baseDir;
    private BasicCompilerConfiguration master;

    public static int DEVELOPMENT_MODE_FAST = 0;
    public static int DEVELOPMENT_MODE_DEBUG = 1;
    public static int DEVELOPMENT_MODE_DEBUG_PERF = 2;
    public static int DEVELOPMENT_MODE_TEST = 3;
    public static int DEVELOPMENT_MODE_RELEASE_DIAG = 4;
    public static int DEVELOPMENT_MODE_RELEASE = 5;
    public static int DEVELOPMENT_MODE_RELEASE_PERF = 6;
    private static final String[] DEVELOPMENT_MODE_NAMES = {
	getString("FastBuildTxt"),
	getString("DebugTxt"),
	getString("PerformanceDebugTxt"),
	getString("TestCoverageTxt"),
	getString("DiagnosableReleaseTxt"),
	getString("ReleaseTxt"),
	getString("PerformanceReleaseTxt"),
    };
    private IntConfiguration developmentMode;

    public static int WARNING_LEVEL_NO = 0;
    public static int WARNING_LEVEL_DEFAULT = 1;
    public static int WARNING_LEVEL_MORE = 2;
    public static int WARNING_LEVEL_TAGS = 3;
    public static int WARNING_LEVEL_CONVERT = 4;
    public static int WARNING_LEVEL_32_64 = 5;
    private static final String[] WARNING_LEVEL_NAMES = {
	getString("NoWarningsTxt"),
	getString("SomeWarningsTxt"),
	getString("MoreWarningsTxt"),
	getString("ConvertWarningsTxt"),
    }; // FIXUP: from Bundle
    private IntConfiguration warningLevel;

    private BooleanConfiguration sixtyfourBits;
    private BooleanConfiguration strip;
    private StringConfiguration additionalDependencies;
    private StringConfiguration tool;
    private OptionsConfiguration commandLineConfiguration;

    // Constructors
    public BasicCompilerConfiguration(String baseDir, BasicCompilerConfiguration master) {
	this.baseDir = baseDir;
	this.master = master;
	developmentMode = new IntConfiguration(master != null ? master.getDevelopmentMode() : null, DEVELOPMENT_MODE_DEBUG, DEVELOPMENT_MODE_NAMES, null);
	warningLevel = new IntConfiguration(master != null ? master.getWarningLevel() : null, WARNING_LEVEL_DEFAULT, WARNING_LEVEL_NAMES, null);
	sixtyfourBits = new BooleanConfiguration(master != null ? master.getSixtyfourBits() : null, false, "", ""); // NOI18N
	strip = new BooleanConfiguration(master != null ? master.getStrip() : null, false, "", ""); // NOI18N
	additionalDependencies = new StringConfiguration(master != null ? master.getAdditionalDependencies() : null, ""); // NOI18N
	tool = new StringConfiguration(master != null ? master.getTool() : null, ""); // NOI18N
	commandLineConfiguration = new OptionsConfiguration();
    }
    
    public boolean getModified() {
        return developmentMode.getModified() ||
                warningLevel.getModified() ||
                sixtyfourBits.getModified() ||
                strip.getModified() ||
                additionalDependencies.getModified() ||
                tool.getModified() ||
                commandLineConfiguration.getModified();
    }

    // baseDir
    public void setBaseDir(String baseDir) {
	this.baseDir = baseDir;
    }
    public String getBaseDir() {
	return baseDir;
    }

    // To be overridden
    public String getOptions(BasicCompiler compiler) {
	return "OVERRIDE"; // NOI18N
    }

    // Master
    public void setMaster(BasicCompilerConfiguration master) {
	this.master = master;
    }
    public BasicCompilerConfiguration getMaster() {
	return master;
    }

    // Development Mode
    public void setDevelopmentMode(IntConfiguration developmentMode) {
	this.developmentMode = developmentMode;
    }

    public IntConfiguration getDevelopmentMode() {
	return developmentMode;
    }

    // Warning Level
    public void setWarningLevel(IntConfiguration warningLevel) {
	this.warningLevel = warningLevel;
    }

    public IntConfiguration getWarningLevel() {
	return warningLevel;
    }


    // SixtyfourBits
    public void setSixtyfourBits(BooleanConfiguration sixtyfourBits) {
	this.sixtyfourBits = sixtyfourBits;
    }
    public BooleanConfiguration getSixtyfourBits() {
	return sixtyfourBits;
    }

    // Strip
    public void setStrip(BooleanConfiguration strip) {
	this.strip = strip;
    }

    public BooleanConfiguration getStrip() {
	return strip;
    }

    public void setAdditionalDependencies(StringConfiguration additionalDependencies) {
	this.additionalDependencies = additionalDependencies;
    }

    public StringConfiguration getAdditionalDependencies() {
	return additionalDependencies;
    }

    // Tool
    public void setTool(StringConfiguration tool) {
	this.tool = tool;
    }
    public StringConfiguration getTool() {
	return tool;
    }

    // CommandLine
    public OptionsConfiguration getCommandLineConfiguration() {
	return commandLineConfiguration;
    }
    public void setCommandLineConfiguration(OptionsConfiguration commandLineConfiguration) {
	this.commandLineConfiguration = commandLineConfiguration;
    }

    public String getOutputFile(String filePath, MakeConfiguration conf, boolean expanded) {
	String fileName = filePath;
	int i = fileName.lastIndexOf("."); // NOI18N
	if (i >= 0)
	    fileName = fileName.substring(0, i) + ".o"; // NOI18N
	else
	    fileName = fileName + ".o"; // NOI18N

	String dirName;
        if (expanded)
            dirName = ConfigurationMakefileWriter.getObjectDir(conf);
        else
            dirName = MakeConfiguration.OBJECTDIR_MACRO;
        
	if (IpeUtils.isPathAbsolute(fileName)) {
            String absPath = fileName;
            if (absPath.charAt(0) != '/')
                absPath = '/' + absPath;
            absPath = dirName + '/' + MakeConfiguration.EXT_FOLDER + absPath; // UNIX path
            absPath = absPath.replace(':', '_');
            absPath = absPath.replace(' ', '_');
            return absPath;
        }
	else if (filePath.startsWith("..")) { // NOI18N
            String absPath = IpeUtils.toAbsolutePath(getBaseDir(), fileName);
            absPath = FilePathAdaptor.normalize(absPath);
            absPath = absPath.replace(':', '_');
            absPath = absPath.replace(' ', '_');
            if (absPath.charAt(0) != '/')
                absPath = '/' + absPath;
	    return dirName + '/' + MakeConfiguration.EXT_FOLDER + absPath; // UNIX path
        }
	else
	    return dirName + '/' + fileName; // UNIX path
    }

    // Assigning & Cloning
    public void assign(BasicCompilerConfiguration conf) {
	setBaseDir(conf.getBaseDir());
	getDevelopmentMode().assign(conf.getDevelopmentMode());
	getWarningLevel().assign(conf.getWarningLevel());
	getSixtyfourBits().assign(conf.getSixtyfourBits());
	getStrip().assign(conf.getStrip());
	getAdditionalDependencies().assign(conf.getAdditionalDependencies());
	getTool().assign(conf.getTool());
	getCommandLineConfiguration().assign(conf.getCommandLineConfiguration());
    }

    public Object clone() {
	BasicCompilerConfiguration clone = new BasicCompilerConfiguration(getBaseDir(), getMaster());
	clone.setDevelopmentMode((IntConfiguration)getDevelopmentMode().clone());
	clone.setWarningLevel((IntConfiguration)getWarningLevel().clone());
	clone.setSixtyfourBits((BooleanConfiguration)getSixtyfourBits().clone());
	clone.setStrip((BooleanConfiguration)getStrip().clone());
	clone.setAdditionalDependencies((StringConfiguration)getAdditionalDependencies().clone());
	clone.setTool((StringConfiguration)getTool().clone());
	clone.setCommandLineConfiguration((OptionsConfiguration)getCommandLineConfiguration().clone());
	return clone;
    }


    // Sheets
    public Sheet.Set getBasicSet() {
	Sheet.Set set = new Sheet.Set();
	set.setName("BasicOptions"); // NOI18N
	set.setDisplayName(getString("BasicOptionsTxt"));
	set.setShortDescription(getString("BasicOptionsHint"));
	set.put(new IntNodeProp(getDevelopmentMode(), true, "DevelopmentMode", getString("DevelopmentModeTxt"), getString("DevelopmentModeHint"))); // NOI18N
	set.put(new IntNodeProp(getWarningLevel(), true, "WarningLevel", getString("WarningLevelTxt"), getString("WarningLevelHint"))); // NOI18N
	set.put(new BooleanNodeProp(getSixtyfourBits(), getMaster() != null ? false : true, "64BitArchitecture", getString("64BitArchitectureTxt"), getString("64BitArchitectureHint"))); // NOI18N
	set.put(new BooleanNodeProp(getStrip(), true, "StripSymbols", getString("StripSymbolsTxt"), getString("StripSymbolsHint"))); // NOI18N
	return set;
    }

    public Sheet.Set getInputSet() {
	Sheet.Set set = new Sheet.Set();
	set.setName("Input"); // NOI18N
	set.setDisplayName(getString("InputTxt"));
	set.setShortDescription(getString("InputHint"));
	set.put(new StringNodeProp(getAdditionalDependencies(), "AdditionalDependencies", getString("AdditionalDependenciesTxt1"), getString("AdditionalDependenciesHint")));  // NOI18N
	return set;
    }
    
    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(BasicCompilerConfiguration.class, s);
    }
}
