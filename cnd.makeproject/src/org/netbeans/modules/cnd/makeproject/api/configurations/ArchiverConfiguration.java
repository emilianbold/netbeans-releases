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
import org.netbeans.modules.cnd.makeproject.api.configurations.AllOptionsProvider;
import org.netbeans.modules.cnd.makeproject.configurations.ui.BooleanNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.OptionsNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.StringNodeProp;
import org.netbeans.modules.cnd.api.utils.CppUtils;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.api.compilers.BasicCompiler;
import org.openide.nodes.Sheet;

public class ArchiverConfiguration implements AllOptionsProvider {
    private MakeConfiguration makeConfiguration;

    private StringConfiguration output;
    private BooleanConfiguration runRanlib;
    private BooleanConfiguration replaceOption;
    private BooleanConfiguration verboseOption;
    private BooleanConfiguration supressOption;
    private OptionsConfiguration commandLineConfiguration;
    private OptionsConfiguration additionalDependencies;
    private StringConfiguration tool;

    // Constructors
    public ArchiverConfiguration(MakeConfiguration makeConfiguration) {
	this.makeConfiguration = makeConfiguration;
	output = new StringConfiguration(null, ""); // NOI18N
	runRanlib = new BooleanConfiguration(null, true, "", "$(RANLIB)"); // NOI18N
	replaceOption = new BooleanConfiguration(null, true, "", "r"); // NOI18N
	verboseOption = new BooleanConfiguration(null, true, "", "v"); // NOI18N
	supressOption = new BooleanConfiguration(null, false, "", "c"); // NOI18N
	commandLineConfiguration = new OptionsConfiguration();
	additionalDependencies = new OptionsConfiguration();
	additionalDependencies.setPreDefined(getAdditionalDependenciesPredefined());
	tool = new StringConfiguration(null, "ar"); // NOI18N
    }

    private String getAdditionalDependenciesPredefined() {
	String pd = "<FIXUP: do we have any dependencies?";
	// FIXUP: what goes in here?
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

    // RunRanlib
    public void setRunRanlib(BooleanConfiguration runRanlib) {
	this.runRanlib = runRanlib;
    }
    public BooleanConfiguration getRunRanlib() {
	return runRanlib;
    }

    // Replace
    public void setReplaceOption(BooleanConfiguration replaceOption) {
	this.replaceOption = replaceOption;
    }
    public BooleanConfiguration getReplaceOption() {
	return replaceOption;
    }

    // Verbose
    public void setVerboseOption(BooleanConfiguration verboseOption) {
	this.verboseOption = verboseOption;
    }
    public BooleanConfiguration getVerboseOption() {
	return verboseOption;
    }

    // Supress
    public void setSupressOption(BooleanConfiguration supressOption) {
	this.supressOption = supressOption;
    }
    public BooleanConfiguration getSupressOption() {
	return supressOption;
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

    // Tool
    public void setTool(StringConfiguration tool) {
	this.tool = tool;
    }
    public StringConfiguration getTool() {
	return tool;
    }

    // Clone and assign
    public void assign(ArchiverConfiguration conf) {
	// ArchiverConfiguration
	setMakeConfiguration(conf.getMakeConfiguration());
	getOutput().assign(conf.getOutput());
	getRunRanlib().assign(conf.getRunRanlib());
	getReplaceOption().assign(conf.getReplaceOption());
	getVerboseOption().assign(conf.getVerboseOption());
	getSupressOption().assign(conf.getSupressOption());
	getAdditionalDependencies().assign(conf.getAdditionalDependencies());
	getCommandLineConfiguration().assign(conf.getCommandLineConfiguration());
	getTool().assign(conf.getTool());
    }

    public Object clone() {
	ArchiverConfiguration clone = new ArchiverConfiguration(getMakeConfiguration());
	// ArchiverConfiguration
	clone.setOutput((StringConfiguration)getOutput().clone());
	clone.setRunRanlib((BooleanConfiguration)getRunRanlib().clone());
	clone.setReplaceOption((BooleanConfiguration)getReplaceOption().clone());
	clone.setVerboseOption((BooleanConfiguration)getVerboseOption().clone());
	clone.setSupressOption((BooleanConfiguration)getSupressOption().clone());
	clone.setAdditionalDependencies((OptionsConfiguration)getAdditionalDependencies().clone());
	clone.setCommandLineConfiguration((OptionsConfiguration)getCommandLineConfiguration().clone());
	clone.setTool((StringConfiguration)getTool().clone());
	return clone;
    }

    // Interface OptionsProvider
    public String getOptions() {
	String options = getAllOptions(false) + " "; // NOI18N
	options += getCommandLineConfiguration().getValue() + " "; // NOI18N
	options += getOutputValue() + " "; // NOI18N
	return CppUtils.reformatWhitespaces(options);
    }

    public String getAllOptions(BasicCompiler compiler) {
	return getAllOptions(true);
    }

    private String getAllOptions(boolean includeOutput) {
	String options = "";
	options += getReplaceOption().getOption();
	options += getVerboseOption().getOption();
	options += getSupressOption().getOption() + " "; // NOI18N
	if (includeOutput)
	    options += getOutputValue() + " ";  // NOI18N
	return CppUtils.reformatWhitespaces(options);
    }

    // Sheet
    public Sheet getGeneralSheet() {
	Sheet sheet = new Sheet();
	Sheet.Set set1 = new Sheet.Set();
	set1.setName("General");
	set1.setDisplayName("General");
	set1.setShortDescription("General");
	set1.put(new StringNodeProp(getOutput(), getOutputValue(), "Output", "Output", "Output"));
	set1.put(new BooleanNodeProp(getRunRanlib(), true, "RunRanlib", "Run Ranlib", "Run Ranlib"));
	sheet.put(set1);
	Sheet.Set set2 = new Sheet.Set();
	set2.setName("Options");
	set2.setDisplayName("Options");
	set2.setShortDescription("Options");
	//set2.put(new BooleanNodeProp(getReplaceOption(), "Add", "Add", "Add (-r)"));
	set2.put(new BooleanNodeProp(getVerboseOption(), true, "Verbose", "Verbose", "Verbose (-v)"));
	set2.put(new BooleanNodeProp(getSupressOption(), true, "SupressDiagnostics", "Supress Diagnostics", "Supress Diagnostics (-c)"));
	sheet.put(set2);
	Sheet.Set set3 = new Sheet.Set();
	String [] texts = new String[] {"Additional Dependencies", "Additional Dependencies", "Additional Dependencies:", "Inherited values:"};
	set3.setName("Input");
	set3.setDisplayName("Input");
	set3.setShortDescription("Input");
	set3.put(new OptionsNodeProp(getAdditionalDependencies(), null, new AdditionalDependenciesOptions(), null, ",", texts));
	sheet.put(set3);
	Sheet.Set set4 = new Sheet.Set();
	set4.setName("Tool");
	set4.setDisplayName("Tool");
	set4.setShortDescription("Tool");
	set4.put(new StringNodeProp(getTool(), "Tool", "Tool", "Tool"));
	sheet.put(set4);
	return sheet;
    }

    class AdditionalDependenciesOptions implements AllOptionsProvider {
	public String getOptions() {
	    return null; // Not used
	}

	public String getAllOptions(BasicCompiler compiler) {
	    String options = "";
	    options += additionalDependencies.getPreDefined();
	    return CppUtils.reformatWhitespaces(options);
	}
    }

    public Sheet getCommandLineSheet() {
	Sheet sheet = new Sheet();
	String[] texts = new String[] {"Additional Options", "Additional Options", "Additional Options:", "All Options:"};

	Sheet.Set set2 = new Sheet.Set();
	set2.setName("Command Line");
	set2.setDisplayName("Command Line");
	set2.setShortDescription("Command Line");
	set2.put(new OptionsNodeProp(getCommandLineConfiguration(), null, this, null, null, texts));
	sheet.put(set2);

	return sheet;
    }

    public String getOutputValue() {
        if (getOutput().getModified())
            return getOutput().getValue();
        else
            return getOutputDefault();
    }
    
    private String getOutputDefault() {
	String outputName = IpeUtils.getBaseName(getMakeConfiguration().getBaseDir()).toLowerCase();
	outputName = "lib" + outputName + ".a";
	return MakeConfiguration.DIST_FOLDER + "/" + getMakeConfiguration().getName() + "/" + getMakeConfiguration().getVariant() + "/" + outputName; // UNIX path
    }
    
    /*
     * Default output pre version 28
     */
    public String getOutputDefault27() {
	String outputName = IpeUtils.getBaseName(getMakeConfiguration().getBaseDir()).toLowerCase();
	outputName = "lib" + outputName + ".a";
	return MakeConfiguration.DIST_FOLDER + "/" + getMakeConfiguration().getName() + "/" + outputName; // UNIX path
}
}
