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
import org.netbeans.modules.cnd.makeproject.api.compilers.Tool;
import org.netbeans.modules.cnd.makeproject.api.configurations.AllOptionsProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.configurations.ui.IntNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.OptionsNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.StringNodeProp;
import org.netbeans.modules.cnd.api.utils.CppUtils;
import org.netbeans.modules.cnd.makeproject.api.compilers.BasicCompiler;
import org.netbeans.modules.cnd.makeproject.api.compilers.CCCCompiler;
import org.netbeans.modules.cnd.makeproject.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.compilers.CompilerSets;
import org.openide.nodes.Sheet;

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
    public Object clone() {
        CCCompilerConfiguration clone = new CCCompilerConfiguration(getBaseDir(), (CCCompilerConfiguration)getMaster());
        // BasicCompilerConfiguration
        clone.setDevelopmentMode((IntConfiguration)getDevelopmentMode().clone());
        clone.setWarningLevel((IntConfiguration)getWarningLevel().clone());
        clone.setMTLevel((IntConfiguration)getMTLevel().clone());
        clone.setSixtyfourBits((BooleanConfiguration)getSixtyfourBits().clone());
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
    public String getOptions(BasicCompiler compiler) {
        String options = "$(COMPILE.cc) ";
        options += getAllOptions2(compiler) + " ";
        options += getCommandLineConfiguration().getValue() + " ";
        return CppUtils.reformatWhitespaces(options);
    }
    
    public String getCCFlagsBasic(BasicCompiler compiler) {
        CCCCompiler cccCompiler = (CCCCompiler)compiler;
        String options = "";
        options += cccCompiler.getMTLevelOptions(getMTLevel().getValue()) + " ";
        options += cccCompiler.getLibraryLevelOptions(getLibraryLevel().getValue()) + " ";
        options += cccCompiler.getStandardsEvolutionOptions(getStandardsEvolution().getValue()) + " ";
        options += cccCompiler.getLanguageExtOptions(getLanguageExt().getValue()) + " ";
        //options += compiler.getStripOption(getStrip().getValue()) + " ";
        options += compiler.getSixtyfourBitsOption(getSixtyfourBits().getValue()) + " ";
        if (getDevelopmentMode().getValue() == DEVELOPMENT_MODE_TEST)
            options += compiler.getDevelopmentModeOptions(DEVELOPMENT_MODE_TEST);
        return CppUtils.reformatWhitespaces(options);
    }
    
    public String getCCFlags(BasicCompiler compiler) {
        String options = getCCFlagsBasic(compiler) + " ";
        options += getCommandLineConfiguration().getValue() + " ";
        return CppUtils.reformatWhitespaces(options);
    }
    
    public String getAllOptions(BasicCompiler compiler) {
        CCCompilerConfiguration master = (CCCompilerConfiguration)getMaster();
        
        String options = "";
        options += getCCFlagsBasic(compiler) + " ";
        if (master != null)
            options += master.getCommandLineConfiguration().getValue() + " ";
        options += getAllOptions2(compiler) + " ";
        return CppUtils.reformatWhitespaces(options);
    }
    
    public String getAllOptions2(BasicCompiler compiler) {
        CCCompilerConfiguration master = (CCCompilerConfiguration)getMaster();
        
        String options = "";
        options += compiler.getDevelopmentModeOptions(getDevelopmentMode().getValue()) + " ";
        options += compiler.getWarningLevelOptions(getWarningLevel().getValue()) + " ";
        options += compiler.getStripOption(getStrip().getValue()) + " ";
        if (master != null && getInheritPreprocessor().getValue())
            options += master.getPreprocessorConfiguration().getOptions("-D") + " ";
        options += getPreprocessorConfiguration().getOptions("-D") + " " ;
        if (master != null && getInheritIncludes().getValue())
            options += master.getIncludeDirectories().getOption("-I") + " ";
        options += getIncludeDirectories().getOption("-I") + " ";
        return CppUtils.reformatWhitespaces(options);
    }
    
    // Sheet
    public Sheet getSheet(MakeConfiguration conf) {
        Sheet sheet = new Sheet();
        CompilerSet compilerSet = CompilerSets.getCompilerSet(conf.getCompilerSet().getValue());
        BasicCompiler ccCompiler = (BasicCompiler)compilerSet.getTool(Tool.CCCompiler);
        
        sheet.put(getSet());
        if (conf.isCompileConfiguration()) {
            sheet.put(getBasicSet());
            if (conf.getCompilerSet().getValue() == CompilerSets.SUN_COMPILER_SET) { // FIXUP: should be moved to SunCCompiler
                Sheet.Set set2 = new Sheet.Set();
                set2.setName("OtherOptions");
                set2.setDisplayName("Other Options");
                set2.setShortDescription("Other Options...");
                set2.put(new IntNodeProp(getMTLevel(), getMaster() != null ? false : true, "MultithreadingLevel", "Multithreading Level", "Multithreading Level ..."));
                set2.put(new IntNodeProp(getLibraryLevel(), getMaster() != null ? false : true, "LibraryLevel", "Library Level", "Library Level ..."));
                set2.put(new IntNodeProp(getStandardsEvolution(), getMaster() != null ? false : true, "StandardsEvolution", "Standards Evolution", "Standards Evolution ..."));
                set2.put(new IntNodeProp(getLanguageExt(), getMaster() != null ? false : true, "LanguageExtensions", "Language Extensions", "Language Extensions ..."));
                sheet.put(set2);
            }
            if (getMaster() != null)
                sheet.put(getInputSet());
            Sheet.Set set4 = new Sheet.Set();
            set4.setName("Tool");
            set4.setDisplayName("Tool");
            set4.setShortDescription("Tool");
            set4.put(new StringNodeProp(getTool(), ccCompiler.getName(), "Tool", "Tool", "Tool"));
            sheet.put(set4);
        }
        
        return sheet;
    }
    
    public Sheet getCommandLineSheet(Configuration conf) {
        Sheet sheet = new Sheet();
        String[] texts = new String[] {"Additional Options", "Additional Options", "Additional Options:", "All Options:"};
        CompilerSet compilerSet = CompilerSets.getCompilerSet(((MakeConfiguration)conf).getCompilerSet().getValue());
        BasicCompiler ccCompiler = (BasicCompiler)compilerSet.getTool(Tool.CCCompiler);
        
        Sheet.Set set2 = new Sheet.Set();
        set2.setName("Command Line");
        set2.setDisplayName("Command Line");
        set2.setShortDescription("Command Line");
        set2.put(new OptionsNodeProp(getCommandLineConfiguration(), null, this, ccCompiler, null, texts));
        sheet.put(set2);
        
        return sheet;
    }
}
