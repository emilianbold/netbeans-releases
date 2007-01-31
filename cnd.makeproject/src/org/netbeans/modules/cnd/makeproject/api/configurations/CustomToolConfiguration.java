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

import org.netbeans.modules.cnd.makeproject.configurations.ui.StringNodeProp;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

public class CustomToolConfiguration {
    // Custom tool
    private StringConfiguration commandLine;
    private StringConfiguration description;
    private StringConfiguration outputs;
    private StringConfiguration additionalDependencies;

    public CustomToolConfiguration() {
	// Custom Tool
	commandLine = new StringConfiguration(null, ""); // NOI18N
	description = new StringConfiguration(null, getString("PerformingStepTxt"));
	outputs = new StringConfiguration(null, ""); // NOI18N
	additionalDependencies = new StringConfiguration(null, ""); // NOI18N
    }

    public boolean getModified() {
	return commandLine.getModified() || description.getModified() || outputs.getModified() || additionalDependencies.getModified();
    }


    public void setCommandLine(StringConfiguration commandLine) {
	this.commandLine = commandLine;
    }

    public StringConfiguration getCommandLine() {
	return commandLine;
    }

    public void setDescription(StringConfiguration description) {
	this.description = description;
    }

    public StringConfiguration getDescription() {
	return description;
    }

    public void setOutputs(StringConfiguration outputs) {
	this.outputs = outputs;
    }

    public StringConfiguration getOutputs() {
	return outputs;
    }

    public void setAdditionalDependencies(StringConfiguration additionalDependencies) {
	this.additionalDependencies = additionalDependencies;
    }

    public StringConfiguration getAdditionalDependencies() {
	return additionalDependencies;
    }

    public void assign(CustomToolConfiguration conf) {
	getCommandLine().assign(conf.getCommandLine());
	getDescription().assign(conf.getDescription());
	getOutputs().assign(conf.getOutputs());
	getAdditionalDependencies().assign(conf.getAdditionalDependencies());
    }

    public Object clone() {
	CustomToolConfiguration i = new CustomToolConfiguration();

	i.setCommandLine((StringConfiguration)getCommandLine().clone());
	i.setDescription((StringConfiguration)getDescription().clone());
	i.setOutputs((StringConfiguration)getOutputs().clone());
	i.setAdditionalDependencies((StringConfiguration)getAdditionalDependencies().clone());

	return i;
    }

    public Sheet getSheet() {
	Sheet sheet = new Sheet();

	Sheet.Set set = new Sheet.Set();
	set.setName("CustomBuild"); // NOI18N
	set.setDisplayName(getString("CustomBuildTxt"));
	set.setShortDescription(getString("CustomBuildHint"));
	set.put(new StringNodeProp(getCommandLine(), "Command Line", getString("CommandLineTxt2"), getString("CommandLineHint2"))); // NOI18N
	set.put(new StringNodeProp(getDescription(), "Description", getString("DescriptionTxt"), getString("DescriptionHint"))); // NOI18N
	set.put(new StringNodeProp(getOutputs(), "Outputs", getString("OutputsTxt"), getString("OutputsNint"))); // NOI18N
	set.put(new StringNodeProp(getAdditionalDependencies(), "AdditionalDependencies", getString("AdditionalDependenciesTxt1"), getString("AdditionalDependenciesHint"))); // NOI18N
	sheet.put(set);

	return sheet;
    }
    
    
    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(CustomToolConfiguration.class, s);
    }
}
