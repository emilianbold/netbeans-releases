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

package org.netbeans.modules.cnd.makeproject.api.configurations;

import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.utils.CppUtils;
import org.netbeans.modules.cnd.makeproject.api.compilers.BasicCompiler;
import org.netbeans.modules.cnd.makeproject.configurations.ui.OptionsNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.VectorNodeProp;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class CCCCompilerConfiguration extends BasicCompilerConfiguration {
    public static int MT_LEVEL_NONE = 0;
    public static int MT_LEVEL_SAFE = 1;
    public static int MT_LEVEL_AUTOMATIC = 2;
    public static int MT_LEVEL_OPENMP = 3;
    private static final String[] MT_LEVEL_NAMES = {
	getString("NoneTxt"),
	getString("SafeTxt"),
	getString("AutomaticTxt"),
	getString("OpenMPTxt"),
    };
    private static final String[] MT_LEVEL_OPTIONS = null;
    private IntConfiguration mpLevel;
    
    public static int LIBRARY_LEVEL_NONE = 0;
    public static int LIBRARY_LEVEL_RUNTIME = 1;
    public static int LIBRARY_LEVEL_CLASSIC = 2;
    public static int LIBRARY_LEVEL_BINARY = 3;
    public static int LIBRARY_LEVEL_CONFORMING = 4;
    private static final String[] LIBRARY_LEVEL_NAMES = {
	getString("NoneTxt"),
	getString("RuntimeOnlyTxt"),
	getString("ClassicIostreamsTxt"),
	getString("BinaryStandardTxt"),
	getString("ConformingStandardTxt"),
    };
    private static final String[] LIBRARY_LEVEL_OPTIONS = null;
    private IntConfiguration libraryLevel;
    
    public static int STANDARDS_OLD = 0;
    public static int STANDARDS_LEGACY = 1;
    public static int STANDARDS_DEFAULT = 2;
    public static int STANDARDS_MODERN = 3;
    private static final String[] STANDARDS_NAMES = {
	getString("OldTxt"),
	getString("LegacyTxt"),
	getString("DefaultTxt"),
	getString("ModernTxt"),
    };
    private static final String[] STANDARD_OPTIONS = null;
    private IntConfiguration standardsEvolution;

    public static int LANGUAGE_EXT_NONE = 0;
    public static int LANGUAGE_EXT_DEFAULT = 1;
    public static int LANGUAGE_EXT_ALL = 2;
    private static final String[] LANGUAGE_EXT_NAMES = {
	getString("NoneTxt"),
	getString("DefaultTxt"),
	getString("AllTxt"),
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
    
    @Override
    public boolean getModified() {
        return super.getModified() ||
                mpLevel.getModified() ||
                libraryLevel.getModified() ||
                standardsEvolution.getModified() ||
                languageExt.getModified() ||
                includeDirectories.getModified() ||
                inheritIncludes.getModified() ||
                preprocessorConfiguration.getModified() ||
                inheritPreprocessor.getModified();
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

    @Override
    public Object clone() {
	CCCCompilerConfiguration clone = new CCCCompilerConfiguration(getBaseDir(), (CCCCompilerConfiguration)getMaster());
	// BasicCompilerConfiguration
	clone.setDevelopmentMode((IntConfiguration)getDevelopmentMode().clone());
	clone.setWarningLevel((IntConfiguration)getWarningLevel().clone());
	clone.setSixtyfourBits((IntConfiguration)getSixtyfourBits().clone());
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
        CCCCompilerConfiguration master;
        
	Sheet.Set set1 = new Sheet.Set();
	set1.setName("General"); // NOI18N
	set1.setDisplayName(getString("GeneralTxt"));
	set1.setShortDescription(getString("GeneralHint"));
        // Include Dirctories
	String inheritedValues = ""; // NOI18N
        master = (CCCCompilerConfiguration)getMaster();
	while (master != null) {
	    inheritedValues += master.getIncludeDirectories().getOption(""); // NOI18N
	    if (master.getInheritIncludes().getValue())
                master = (CCCCompilerConfiguration)master.getMaster();
            else
                master = null;
	}
	set1.put(new VectorNodeProp(getIncludeDirectories(), getInheritIncludes(), getBaseDir(), new String[] {"IncludeDirectories", getString("IncludeDirectoriesTxt"), getString("IncludeDirectoriesHint"), inheritedValues}, true, new HelpCtx("AddtlIncludeDirectories"))); // NOI18N
	// Preprocessor Macros
	inheritedValues = ""; // NOI18N
        master = (CCCCompilerConfiguration)getMaster();
	while (master != null) {
	    inheritedValues += master.getPreprocessorConfiguration().getValue();
	    if (master.getInheritPreprocessor().getValue())
                master = (CCCCompilerConfiguration)master.getMaster();
            else
                master = null;
	}
	String[] texts = new String[] {getString("PreprocessorDefinitionsTxt1"), getString("PreprocessorDefinitionsHint"), getString("PreprocessorDefinitionsTxt2"), getString("InheritedValuesTxt"), inheritedValues};
	set1.put(new OptionsNodeProp(getPreprocessorConfiguration(), getInheritPreprocessor(), new PreprocessorOptions(), null, null, texts)); // NOI18N
        
        return set1;
    }
    
    private class PreprocessorOptions implements AllOptionsProvider {
	public String getAllOptions(BasicCompiler compiler) {
	    CCCCompilerConfiguration master = (CCCCompilerConfiguration)getMaster();

	    String options = ""; // NOI18N
	    while (master != null) {
		options += master.getPreprocessorConfiguration().getValue() + " "; // NOI18N
                if (master.getInheritPreprocessor().getValue())
                    master = (CCCCompilerConfiguration)master.getMaster();
                else
                    master = null;
            }
	    return CppUtils.reformatWhitespaces(options);
	}
    }

    // Sheet
    public Sheet getSheet(Project project) {
	Sheet sheet = new Sheet();
	sheet.put(getSet());
	return sheet;
    }
    
    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(CCCCompilerConfiguration.class, s);
    }
}
