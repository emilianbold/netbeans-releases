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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.api;

import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;

public class MakeArtifact {
    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_APPLICATION = 1;
    public static final int TYPE_DYNAMIC_LIB = 2;
    public static final int TYPE_STATIC_LIB = 3;

    // Project
    private String projectLocation;
    // Configuration
    private int configurationType;
    private String configurationName;
    private boolean active;
    private boolean build;
    // Artifact
    private String workingDirectory;
    private String buildCommand;
    private String cleanCommand;
    private String output;

    public MakeArtifact(
	    String projectLocation,
	    int configurationType, 
	    String configurationName, 
	    boolean active, 
	    boolean build, 
	    String workingDirectory, 
	    String buildCommand, 
	    String cleanCommand, 
	    String output) {
	this.projectLocation = projectLocation;
	this.configurationType = configurationType;
	this.configurationName = configurationName;
	this.active = active;
	this.build = build;
	this.workingDirectory = workingDirectory;
	this.buildCommand = buildCommand;
	this.cleanCommand = cleanCommand;
	this.output = output;
    }

    public MakeArtifact(MakeConfigurationDescriptor pd, MakeConfiguration makeConfiguration) {
		projectLocation = makeConfiguration.getBaseDir();
		configurationName = makeConfiguration.getName();
		active = makeConfiguration.isDefault();
		build = true;
		workingDirectory = projectLocation;
		buildCommand = "${MAKE} " + MakeOptions.getInstance().getMakeOptions() + " -f " + pd.getProjectMakefileName() + " CONF=" + configurationName; // NOI18N
		cleanCommand = "${MAKE} " + MakeOptions.getInstance().getMakeOptions() + " -f " + pd.getProjectMakefileName() + " CONF=" + configurationName + " clean"; // NOI18N
		if (makeConfiguration.getConfigurationType().getValue() == MakeConfiguration.TYPE_MAKEFILE) {
		    configurationType = MakeArtifact.TYPE_UNKNOWN;
		    output = makeConfiguration.getMakefileConfiguration().getOutput().getValue();
		}
		else if (makeConfiguration.getConfigurationType().getValue() == MakeConfiguration.TYPE_APPLICATION) {
		    configurationType = MakeArtifact.TYPE_APPLICATION;
		    output = makeConfiguration.getLinkerConfiguration().getOutputValue();
		}
		else if (makeConfiguration.getConfigurationType().getValue() == MakeConfiguration.TYPE_DYNAMIC_LIB) {
		    configurationType = MakeArtifact.TYPE_DYNAMIC_LIB;
		    output = makeConfiguration.getLinkerConfiguration().getOutputValue();
		}
		else if (makeConfiguration.getConfigurationType().getValue() == MakeConfiguration.TYPE_STATIC_LIB) {
		    configurationType = MakeArtifact.TYPE_STATIC_LIB;
		    output = makeConfiguration.getArchiverConfiguration().getOutputValue();
		}
		else {
		    assert false;// FIXUP: error
		}
    }
    
    public String getProjectLocation() {
	return projectLocation;
    }

    public void setProjectLocation(String projectLocation) {
	this.projectLocation = projectLocation;
    }


    public int getConfigurationType() {
	return configurationType;
    }

    public String getConfigurationName() {
	return configurationName;
    }

    public boolean getActive() {
	return active;
    }

    public boolean getBuild() {
	return build;
    }

    public void setBuild(boolean build) {
	this.build = build;
    }

    public String getWorkingDirectory() {
	return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
	this.workingDirectory = workingDirectory;
    }

    public String getBuildCommand() {
	return buildCommand;
    }

    public String getBuildCommand(String makeCommand, String makeFlags) {
	String bc = getBuildCommand();
	int i = bc.indexOf("${MAKE}"); // NOI18N
	if (i == 0)
	    bc = makeCommand + bc.substring(i + 7);
	else if (i > 0)
	    bc = bc.substring(0, i) + makeCommand + bc.substring(i + 7);

	i = bc.indexOf("${MAKEFLAGS}"); // NOI18N
	if (i == 0)
	    bc = makeFlags + bc.substring(i + 12);
	else if (i > 0)
	    bc = bc.substring(0, i) + makeFlags + bc.substring(i + 12);
        else
            bc = makeFlags + bc;

	return bc;
    }

    public String getCleanCommand() {
	return cleanCommand;
    }

    public String getCleanCommand(String makeCommand, String makeFlags) {
	String cc = getCleanCommand();
	int i = cc.indexOf("${MAKE}"); // NOI18N
	if (i == 0)
	    cc = makeCommand + cc.substring(i + 7);
	else if (i > 0)
	    cc = cc.substring(0, i) + makeCommand + cc.substring(i + 7);

	i = cc.indexOf("${MAKEFLAGS}"); // NOI18N
	if (i == 0)
	    cc = makeFlags + cc.substring(i + 12);
	else if (i > 0)
	    cc = cc.substring(0, i) + makeFlags + cc.substring(i + 12);
        else
            cc = makeFlags + cc;

	return cc;
    }

    public String getOutput() {
	return output;
    }

    @Override
    public String toString() {
        String ret = getConfigurationName();
        if (getOutput() != null && getOutput().length() > 0)
	    ret = ret + " (" + getOutput() + ")"; // NOI18N
        return ret;
    }

    public static MakeArtifact[] getMakeArtifacts(Project project) {
	MakeArtifactProvider map = project.getLookup().lookup(MakeArtifactProvider.class);
	if (map != null)
	    return map.getBuildArtifacts();
	else
	    return null;
    }

    @Override
    public Object clone() {
	return new MakeArtifact(
	    projectLocation,
	    configurationType,
	    configurationName,
	    active,
	    build,
	    workingDirectory,
	    buildCommand,
	    cleanCommand,
	    output
	);
    }
}
