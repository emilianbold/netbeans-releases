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
import org.netbeans.modules.cnd.api.utils.CppUtils;
import org.netbeans.modules.cnd.makeproject.api.compilers.BasicCompiler;
import org.netbeans.modules.cnd.makeproject.configurations.*;
import org.netbeans.modules.cnd.makeproject.configurations.ui.OptionsNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.VectorNodeProp;
import org.openide.nodes.Sheet;

public class CCCCompilerConfiguration extends BasicCompilerConfiguration {
    public static int MT_LEVEL_NONE = 0;
    public static int MT_LEVEL_SAFE = 1;
    public static int MT_LEVEL_AUTOMATIC = 2;
    public static int MT_LEVEL_OPENMP = 3;
    private static final String[] MT_LEVEL_NAMES = {
	"None",
	"Safe",
	"Automatic",
	"Open MP",
    };
    private static final String[] MT_LEVEL_OPTIONS = null;
    private IntConfiguration mpLevel;
    
    public static int LIBRARY_LEVEL_NONE = 0;
    public static int LIBRARY_LEVEL_RUNTIME = 1;
    public static int LIBRARY_LEVEL_CLASSIC = 2;
    public static int LIBRARY_LEVEL_BINARY = 3;
    public static int LIBRARY_LEVEL_CONFORMING = 4;
    private static final String[] LIBRARY_LEVEL_NAMES = {
	"None",
	"Runtime Only",
	"Classic Iostreams",
	"Binary Standard",
	"Conforming Standard",
    };
    private static final String[] LIBRARY_LEVEL_OPTIONS = null;
    private IntConfiguration libraryLevel;
    
    public static int STANDARDS_OLD = 0;
    public static int STANDARDS_LEGACY = 1;
    public static int STANDARDS_DEFAULT = 2;
    public static int STANDARDS_MODERN = 3;
    private static final String[] STANDARDS_NAMES = {
	"Old",
	"Legacy",
	"Default",
	"Modern",
    };
    private static final String[] STANDARD_OPTIONS = null;
    private IntConfiguration standardsEvolution;

    public static int LANGUAGE_EXT_NONE = 0;
    public static int LANGUAGE_EXT_DEFAULT = 1;
    public static int LANGUAGE_EXT_ALL = 2;
    private static final String[] LANGUAGE_EXT_NAMES = {
	"None",
	"Default",
	"All",
    };
    private static final String[] LANGUAGE_EXT_OPTIONS = null;
    private IntConfiguration languageExt;

    private VectorConfiguration includeDirectories;
    private BooleanConfiguration inheritIncludes;
    private OptionsConfiguration preprocessorConfiguration;
    private BooleanConfiguration inheritPreprocessor;

    // Constructors
    public CCCCompilerConfiguration(String baseDir, CCCCompilerConfiguration master) {
	super(baseDir, master);
	mpLevel = new IntConfiguration(master != null ? master.getMTLevel() : null, MT_LEVEL_NONE, MT_LEVEL_NAMES, null);
	libraryLevel = new IntConfiguration(master != null ? master.getLibraryLevel() : null, LIBRARY_LEVEL_BINARY, LIBRARY_LEVEL_NAMES, getLibraryLevelOptions());
	standardsEvolution = new IntConfiguration(master != null ? master.getStandardsEvolution() : null, STANDARDS_DEFAULT, STANDARDS_NAMES, getStandardsEvolutionOptions());
	languageExt = new IntConfiguration(master != null ? master.getLanguageExt() : null, LANGUAGE_EXT_DEFAULT, LANGUAGE_EXT_NAMES, getLanguageExtOptions());
	includeDirectories = new VectorConfiguration(master != null ? master.getIncludeDirectories() : null);
	inheritIncludes = new BooleanConfiguration(null, true, null, null);
	preprocessorConfiguration = new OptionsConfiguration();
	inheritPreprocessor = new BooleanConfiguration(null, true, null, null);
    }
    
    // To be overridden
    protected String[] getMTLevelOptions() {
	return MT_LEVEL_OPTIONS;
    }
    
    // To be overridden
    protected String[] getLibraryLevelOptions() {
	return LIBRARY_LEVEL_OPTIONS;
    }

    // To be overridden
    protected String[] getStandardsEvolutionOptions() {
	return STANDARD_OPTIONS;
    }

    // To be overridden
    protected String[] getLanguageExtOptions() {
	return LANGUAGE_EXT_OPTIONS;
    }
    
    // MT Level
    public void setMTLevel(IntConfiguration mpLevel) {
	this.mpLevel = mpLevel;
    }

    public IntConfiguration getMTLevel() {
	return mpLevel;
    }

    // Library Level
    public void setLibraryLevel(IntConfiguration libraryLevel) {
	this.libraryLevel = libraryLevel;
    }
    public IntConfiguration getLibraryLevel() {
	return libraryLevel;
    }

    // Standards Evolution
    public void setStandardsEvolution(IntConfiguration standardsEvolution) {
	this.standardsEvolution = standardsEvolution;
    }

    public IntConfiguration getStandardsEvolution() {
	return standardsEvolution;
    }

    // languageExt
    public void setLanguageExt(IntConfiguration languageExt) {
	this.languageExt = languageExt;
    }

    public IntConfiguration getLanguageExt() {
	return languageExt;
    }

    // Include Directories
    public VectorConfiguration getIncludeDirectories() {
	return includeDirectories;
    }

    public void setIncludeDirectories(VectorConfiguration includeDirectories) {
	this.includeDirectories = includeDirectories;
    }

    // Inherit Include Directories
    public BooleanConfiguration getInheritIncludes() {
	return inheritIncludes;
    }

    public void setInheritIncludes(BooleanConfiguration inheritIncludes) {
	this.inheritIncludes = inheritIncludes;
    }

    // Preprocessor
    public OptionsConfiguration getPreprocessorConfiguration() {
	return preprocessorConfiguration;
    }
    public void setPreprocessorConfiguration(OptionsConfiguration preprocessorConfiguration) {
	this.preprocessorConfiguration = preprocessorConfiguration;
    }

    // Inherit Include Directories
    public BooleanConfiguration getInheritPreprocessor() {
	return inheritPreprocessor;
    }

    public void setInheritPreprocessor(BooleanConfiguration inheritPreprocessor) {
	this.inheritPreprocessor = inheritPreprocessor;
    }

    // Clone and assign
    public void assign(CCCCompilerConfiguration conf) {
	// BasicCompilerConfiguration
	super.assign(conf);
	// XCompilerConfiguration
	getMTLevel().assign(conf.getMTLevel());
	getLibraryLevel().assign(conf.getLibraryLevel());
	getStandardsEvolution().assign(conf.getStandardsEvolution());
	getLanguageExt().assign(conf.getLanguageExt());
	getIncludeDirectories().assign(conf.getIncludeDirectories());
	getInheritIncludes().assign(conf.getInheritIncludes());
	getPreprocessorConfiguration().assign(conf.getPreprocessorConfiguration());
	getInheritPreprocessor().assign(conf.getInheritPreprocessor());
    }

    public Object clone() {
	CCCCompilerConfiguration clone = new CCCCompilerConfiguration(getBaseDir(), (CCCCompilerConfiguration)getMaster());
	// BasicCompilerConfiguration
	clone.setDevelopmentMode((IntConfiguration)getDevelopmentMode().clone());
	clone.setWarningLevel((IntConfiguration)getWarningLevel().clone());
	clone.setSixtyfourBits((BooleanConfiguration)getSixtyfourBits().clone());
	clone.setStrip((BooleanConfiguration)getStrip().clone());
	clone.setAdditionalDependencies((StringConfiguration)getAdditionalDependencies().clone());
	clone.setTool((StringConfiguration)getTool().clone());
	clone.setCommandLineConfiguration((OptionsConfiguration)getCommandLineConfiguration().clone());
	// XCompilerConfiguration
	clone.setMTLevel((IntConfiguration)getMTLevel().clone());
	clone.setLibraryLevel((IntConfiguration)getLibraryLevel().clone());
	clone.setStandardsEvolution((IntConfiguration)getStandardsEvolution().clone());
	clone.setLanguageExt((IntConfiguration)getLanguageExt().clone());
	clone.setIncludeDirectories((VectorConfiguration)getIncludeDirectories().clone());
	clone.setInheritIncludes((BooleanConfiguration)getInheritIncludes().clone());
	clone.setPreprocessorConfiguration((OptionsConfiguration)getPreprocessorConfiguration().clone());
	clone.setInheritPreprocessor((BooleanConfiguration)getInheritPreprocessor().clone());
	return clone;
    }

    // Sheet
    public Sheet.Set getSet() {
	Sheet.Set set1 = new Sheet.Set();
	set1.setName("General");
	set1.setDisplayName("General");
	set1.setShortDescription("General");
        // Include Dirctories
	String inheritedValues = null;
	BooleanConfiguration inheritIncludes = null;
	if (getMaster() != null) {
	    inheritedValues = ((CCCCompilerConfiguration)getMaster()).getIncludeDirectories().getOption("");
	    inheritIncludes = getInheritIncludes();
	}
	set1.put(new VectorNodeProp(getIncludeDirectories(), inheritIncludes, getBaseDir(), new String[] {"IncludeDirectories", "Include Directories", "Include Directories (-I)", inheritedValues}, true));
	// Preprocessor Macros
	inheritedValues = null;
	inheritIncludes = null;
	if (getMaster() != null) {
	    inheritedValues = ((CCCCompilerConfiguration)getMaster()).getPreprocessorConfiguration().getValue();
	    inheritIncludes = getInheritPreprocessor();
	}
	String[] texts = new String[] {"Preprocessor Definitions", "Preprocessor Definitions (-D)", "Preprocessor Definitions:", "Inherited Values:", inheritedValues};
	set1.put(new OptionsNodeProp(getPreprocessorConfiguration(), inheritIncludes, new PreprocessorOptions(), null, ";", texts));
        
        return set1;
    }
    
    private class PreprocessorOptions implements AllOptionsProvider {
	public String getAllOptions(BasicCompiler compiler) {
	    CCCCompilerConfiguration master = (CCCCompilerConfiguration)getMaster();

	    String options = ""; // NOI18N
	    if (master != null)
		options += master.getPreprocessorConfiguration().getValue() + " "; // NOI18N
	    return CppUtils.reformatWhitespaces(options);
	}
    }

    // Sheet
    public Sheet getSheet(Project project) {
	Sheet sheet = new Sheet();
	sheet.put(getSet());
	return sheet;
    }
}
