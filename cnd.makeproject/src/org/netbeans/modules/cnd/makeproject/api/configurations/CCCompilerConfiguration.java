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
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.makeproject.configurations.ui.IntNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.OptionsNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.StringNodeProp;
import org.netbeans.modules.cnd.api.utils.CppUtils;
import org.netbeans.modules.cnd.makeproject.api.compilers.BasicCompiler;
import org.netbeans.modules.cnd.makeproject.api.compilers.CCCCompiler;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

public class CCCompilerConfiguration extends CCCCompilerConfiguration implements AllOptionsProvider {
    // Constructors
    public CCCompilerConfiguration(String baseDir, CCCompilerConfiguration master) {
        super(baseDir, master);
    }
    
    // Clone and assign
    public void assign(CCCompilerConfiguration conf) {
        // From XCompiler
        super.assign(conf);
    }
    
    // Cloning
    @Override
    public Object clone() {
        CCCompilerConfiguration clone = new CCCompilerConfiguration(getBaseDir(), (CCCompilerConfiguration)getMaster());
        // BasicCompilerConfiguration
        clone.setDevelopmentMode((IntConfiguration)getDevelopmentMode().clone());
        clone.setWarningLevel((IntConfiguration)getWarningLevel().clone());
        clone.setMTLevel((IntConfiguration)getMTLevel().clone());
        clone.setSixtyfourBits((IntConfiguration)getSixtyfourBits().clone());
        clone.setStrip((BooleanConfiguration)getStrip().clone());
        clone.setAdditionalDependencies((StringConfiguration)getAdditionalDependencies().clone());
        clone.setTool((StringConfiguration)getTool().clone());
        clone.setCommandLineConfiguration((OptionsConfiguration)getCommandLineConfiguration().clone());
        // From CCCCompiler
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
    
    // Interface OptionsProvider
    @Override
    public String getOptions(BasicCompiler compiler) {
        String options = "$(COMPILE.cc) "; // NOI18N
        options += getAllOptions2(compiler) + " "; // NOI18N
        options += getCommandLineConfiguration().getValue() + " "; // NOI18N
        return CppUtils.reformatWhitespaces(options);
    }
    
    public String getCCFlagsBasic(BasicCompiler compiler) {
        CCCCompiler cccCompiler = (CCCCompiler)compiler;
        String options = ""; // NOI18N
        options += cccCompiler.getMTLevelOptions(getMTLevel().getValue()) + " "; // NOI18N
        options += cccCompiler.getLibraryLevelOptions(getLibraryLevel().getValue()) + " "; // NOI18N
        options += cccCompiler.getStandardsEvolutionOptions(getStandardsEvolution().getValue()) + " "; // NOI18N
        options += cccCompiler.getLanguageExtOptions(getLanguageExt().getValue()) + " "; // NOI18N
        //options += compiler.getStripOption(getStrip().getValue()) + " "; // NOI18N
        options += compiler.getSixtyfourBitsOption(getSixtyfourBits().getValue()) + " "; // NOI18N
        if (getDevelopmentMode().getValue() == DEVELOPMENT_MODE_TEST)
            options += compiler.getDevelopmentModeOptions(DEVELOPMENT_MODE_TEST);
        return CppUtils.reformatWhitespaces(options);
    }
    
    public String getCCFlags(BasicCompiler compiler) {
        String options = getCCFlagsBasic(compiler) + " "; // NOI18N
        options += getCommandLineConfiguration().getValue() + " "; // NOI18N
        return CppUtils.reformatWhitespaces(options);
    }
    
    public String getAllOptions(BasicCompiler compiler) {
        CCCompilerConfiguration master;
        
        String options = ""; // NOI18N
        options += getCCFlagsBasic(compiler) + " "; // NOI18N
        master = (CCCompilerConfiguration)getMaster();
        while (master != null) {
            options += master.getCommandLineConfiguration().getValue() + " "; // NOI18N
            master = (CCCompilerConfiguration)master.getMaster();
        }
        options += getAllOptions2(compiler) + " "; // NOI18N
        return CppUtils.reformatWhitespaces(options);
    }
    
    public String getAllOptions2(BasicCompiler compiler) {
        CCCompilerConfiguration master;
        
        String options = ""; // NOI18N
        if (getDevelopmentMode().getValue() != DEVELOPMENT_MODE_TEST)
            options += compiler.getDevelopmentModeOptions(getDevelopmentMode().getValue()) + " "; // NOI18N
        options += compiler.getWarningLevelOptions(getWarningLevel().getValue()) + " "; // NOI18N
        options += compiler.getStripOption(getStrip().getValue()) + " "; // NOI18N
        options += getPreprocessorOptions();
        options += getIncludeDirectoriesOptions();
        return CppUtils.reformatWhitespaces(options);
    }
    public String getPreprocessorOptions() {
        CCCompilerConfiguration master = (CCCompilerConfiguration)getMaster();
        String options = getPreprocessorConfiguration().getOptions("-D") + " " ; // NOI18N
        while (master != null && getInheritPreprocessor().getValue()) {
            options += master.getPreprocessorConfiguration().getOptions("-D") + " "; // NOI18N
            if (master.getInheritPreprocessor().getValue())
                master = (CCCompilerConfiguration)master.getMaster();
            else
                master = null;
        }
        return options;
    }
    
    public String getIncludeDirectoriesOptions() {
        CCCompilerConfiguration master = (CCCompilerConfiguration)getMaster();
        String options = getIncludeDirectories().getOption("-I") + " "; // NOI18N
        while (master != null && getInheritIncludes().getValue()) {
            options += master.getIncludeDirectories().getOption("-I") + " "; // NOI18N
            if (master.getInheritIncludes().getValue())
                master = (CCCompilerConfiguration)master.getMaster();
            else
                master = null;
        }
        return options;
    } 
    
    // Sheet
    public Sheet getSheet(MakeConfiguration conf, Folder folder) {
        Sheet sheet = new Sheet();
        CompilerSet compilerSet = CompilerSetManager.getDefault().getCompilerSet(conf.getCompilerSet().getValue());
        BasicCompiler ccCompiler = (BasicCompiler)compilerSet.getTool(Tool.CCCompiler);
        
        sheet.put(getSet());
        if (conf.isCompileConfiguration() && folder == null) {
            sheet.put(getBasicSet());
            if (conf.getCompilerSet().getValue() == CompilerFlavor.Sun.ordinal()) { // FIXUP: should be moved to SunCCompiler
                Sheet.Set set2 = new Sheet.Set();
                set2.setName("OtherOptions"); // NOI18N
                set2.setDisplayName(getString("OtherOptionsTxt"));
                set2.setShortDescription(getString("OtherOptionsHint"));
                set2.put(new IntNodeProp(getMTLevel(), getMaster() != null ? false : true, "MultithreadingLevel", getString("MultithreadingLevelTxt"), getString("MultithreadingLevelHint"))); // NOI18N
                set2.put(new IntNodeProp(getLibraryLevel(), getMaster() != null ? false : true, "LibraryLevel", getString("LibraryLevelTxt"), getString("LibraryLevelHint"))); // NOI18N
                set2.put(new IntNodeProp(getStandardsEvolution(), getMaster() != null ? false : true, "StandardsEvolution", getString("StandardsEvolutionTxt"), getString("StandardsEvolutionHint"))); // NOI18N
                set2.put(new IntNodeProp(getLanguageExt(), getMaster() != null ? false : true, "LanguageExtensions", getString("LanguageExtensionsTxt"), getString("LanguageExtensionsHint"))); // NOI18N
                sheet.put(set2);
            }
            if (getMaster() != null)
                sheet.put(getInputSet());
            Sheet.Set set4 = new Sheet.Set();
            set4.setName("Tool"); // NOI18N
            set4.setDisplayName(getString("ToolTxt1"));
            set4.setShortDescription(getString("ToolHint1"));
            set4.put(new StringNodeProp(getTool(), ccCompiler.getName(), "Tool", getString("ToolTxt2"), getString("ToolHint2"))); // NOI18N
            sheet.put(set4);
        }
        
        return sheet;
    }
    
    public Sheet getCommandLineSheet(Configuration conf) {
        Sheet sheet = new Sheet();
        String[] texts = new String[] {getString("AdditionalOptionsTxt1"), getString("AdditionalOptionsHint"), getString("AdditionalOptionsTxt2"), getString("AllOptionsTxt")};
        CompilerSet compilerSet = CompilerSetManager.getDefault().getCompilerSet(((MakeConfiguration)conf).getCompilerSet().getValue());
        BasicCompiler ccCompiler = (BasicCompiler)compilerSet.getTool(Tool.CCCompiler);
        
        Sheet.Set set2 = new Sheet.Set();
        set2.setName("CommandLine"); // NOI18N
        set2.setDisplayName(getString("CommandLineTxt"));
        set2.setShortDescription(getString("CommandLineHint"));
        set2.put(new OptionsNodeProp(getCommandLineConfiguration(), null, this, ccCompiler, null, texts));
        sheet.put(set2);
        
        return sheet;
    }
    
    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(CCCompilerConfiguration.class, s);
    }
}
