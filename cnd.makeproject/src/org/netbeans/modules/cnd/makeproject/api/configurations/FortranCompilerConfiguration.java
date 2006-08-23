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
import org.netbeans.modules.cnd.makeproject.configurations.ui.OptionsNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.StringNodeProp;
import org.netbeans.modules.cnd.api.utils.CppUtils;
import org.netbeans.modules.cnd.makeproject.api.compilers.BasicCompiler;
import org.netbeans.modules.cnd.makeproject.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.compilers.CompilerSets;
import org.netbeans.modules.cnd.makeproject.api.compilers.Tool;
import org.netbeans.modules.cnd.makeproject.api.configurations.BasicCompilerConfiguration;
import org.openide.nodes.Sheet;

public class FortranCompilerConfiguration extends BasicCompilerConfiguration implements AllOptionsProvider {
    // Constructors
    public FortranCompilerConfiguration(String baseDir, FortranCompilerConfiguration master) {
	super(baseDir, master);
    }
    
    // Clone and assign
    public void assign(FortranCompilerConfiguration conf) {
	// From XCompiler
	super.assign(conf);
    }

    public Object clone() {
	FortranCompilerConfiguration clone = new FortranCompilerConfiguration(getBaseDir(), (FortranCompilerConfiguration)getMaster());
	// BasicCompilerConfiguration
	clone.setDevelopmentMode((IntConfiguration)getDevelopmentMode().clone());
	clone.setWarningLevel((IntConfiguration)getWarningLevel().clone());
	clone.setSixtyfourBits((BooleanConfiguration)getSixtyfourBits().clone());
	clone.setStrip((BooleanConfiguration)getStrip().clone());
	clone.setAdditionalDependencies((StringConfiguration)getAdditionalDependencies().clone());
	clone.setTool((StringConfiguration)getTool().clone());
	clone.setCommandLineConfiguration((OptionsConfiguration)getCommandLineConfiguration().clone());
	return clone;
    }

    // Interface OptionsProvider
    public String getOptions(BasicCompiler compiler) {
	String options = "$(COMPILE.f) ";
	options += getAllOptions2(compiler) + " ";
	options += getCommandLineConfiguration().getValue() + " ";
	return CppUtils.reformatWhitespaces(options);
    }

    public String getFFlagsBasic(BasicCompiler compiler) {
	String options = "";
	options += compiler.getStripOption(getStrip().getValue()) + " ";
	options += compiler.getSixtyfourBitsOption(getSixtyfourBits().getValue()) + " ";
	if (getDevelopmentMode().getValue() == DEVELOPMENT_MODE_TEST)
	    options += compiler.getDevelopmentModeOptions(DEVELOPMENT_MODE_TEST);
	return CppUtils.reformatWhitespaces(options);
    }

    public String getFFlags(BasicCompiler compiler) {
	String options = getFFlagsBasic(compiler) + " ";
	options += getCommandLineConfiguration().getValue() + " ";
	return CppUtils.reformatWhitespaces(options);
    }

    public String getAllOptions(BasicCompiler compiler) {
	CCompilerConfiguration master = (CCompilerConfiguration)getMaster();

	String options = "";
	options += getFFlagsBasic(compiler) + " ";
	if (master != null)
	    options += master.getCommandLineConfiguration().getValue() + " ";
	options += getAllOptions2(compiler) + " ";
	return CppUtils.reformatWhitespaces(options);
    }

    public String getAllOptions2(BasicCompiler compiler) {
	FortranCompilerConfiguration master = (FortranCompilerConfiguration)getMaster();

	String options = "";
        options += compiler.getDevelopmentModeOptions(getDevelopmentMode().getValue()) + " ";
	options += compiler.getWarningLevelOptions(getWarningLevel().getValue()) + " ";
	return CppUtils.reformatWhitespaces(options);
    }

    // Sheet
    public Sheet getGeneralSheet(MakeConfiguration conf) {
	Sheet sheet = new Sheet();
        CompilerSet compilerSet = CompilerSets.getCompilerSet(conf.getCompilerSet().getValue());
        BasicCompiler fortranCompiler = (BasicCompiler)compilerSet.getTool(Tool.FortranCompiler);
        
	sheet.put(getBasicSet());
	if (getMaster() != null)
	    sheet.put(getInputSet());
	Sheet.Set set4 = new Sheet.Set();
	set4.setName("Tool");
	set4.setDisplayName("Tool");
	set4.setShortDescription("Tool...");
	set4.put(new StringNodeProp(getTool(), fortranCompiler.getName(), "Tool", "Tool", "Tool..."));
	sheet.put(set4);

	return sheet;
    }

    public Sheet getCommandLineSheet(Configuration conf) {
	Sheet sheet = new Sheet();
	String[] texts = new String[] {"Additional Options", "Additional Options", "Additional Options:", "All Options:"};
        CompilerSet compilerSet = CompilerSets.getCompilerSet(((MakeConfiguration)conf).getCompilerSet().getValue());
        BasicCompiler cCompiler = (BasicCompiler)compilerSet.getTool(Tool.CCompiler);

	Sheet.Set set2 = new Sheet.Set();
	set2.setName("Command Line");
	set2.setDisplayName("Command Line");
	set2.setShortDescription("Command Line");
	set2.put(new OptionsNodeProp(getCommandLineConfiguration(), null, this, cCompiler, null, texts));
	sheet.put(set2);

	return sheet;
    }
}
