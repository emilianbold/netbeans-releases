/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.makeproject.api.configurations;
import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.api.project.NativeFileItem.LanguageFlavor;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.IntNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.CppUtils;
import org.netbeans.modules.cnd.makeproject.configurations.ui.OptionsNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.StringNodeProp;
import org.netbeans.modules.cnd.makeproject.spi.configurations.AllOptionsProvider;
import org.netbeans.modules.cnd.makeproject.spi.configurations.CompileOptionsProvider;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

public class CCCompilerConfiguration extends CCCCompilerConfiguration implements Cloneable {
    
    public static final int STANDARD_DEFAULT = 0;
    public static final int STANDARD_CPP98 = 1;
    public static final int STANDARD_CPP11 = 2;
    public static final int STANDARD_INHERITED = 3;
    private static final String[] STANDARD_NAMES = {
        getString("STANDARD_DEFAULT"),
        getString("STANDARD_CPP98"),
        getString("STANDARD_CPP11"),
        getString("STANDARD_INHERITED"),};
    private static final String[] STANDARD_NAMES_ROOT = {
        getString("STANDARD_DEFAULT"),
        getString("STANDARD_CPP98"),
        getString("STANDARD_CPP11"),};    
    private IntConfiguration cppStandard;    
    
    // Constructors
    public CCCompilerConfiguration(String baseDir, CCCompilerConfiguration master, MakeConfiguration owner) {
        super(baseDir, master, owner);
        if (master != null) {
            cppStandard = new IntConfiguration(null, STANDARD_INHERITED, STANDARD_NAMES, null);
        } else {
            cppStandard = new IntConfiguration(null, STANDARD_DEFAULT, STANDARD_NAMES_ROOT, null);
        }
    }
    
    public void fixupMasterLinks(CCCompilerConfiguration compilerConfiguration) {
        super.fixupMasterLinks(compilerConfiguration);
        getCppStandard().setMaster(compilerConfiguration.getCppStandard());
    }    
    
    public IntConfiguration getCppStandard() {
        return cppStandard;
    }    

    public int getCppStandardExternal() {
        switch(getCppStandard().getValue()) {
            case STANDARD_DEFAULT: return LanguageFlavor.DEFAULT.toExternal();
            case STANDARD_CPP98: return LanguageFlavor.CPP.toExternal();
            case STANDARD_CPP11: return LanguageFlavor.CPP11.toExternal();
            case STANDARD_INHERITED:  return LanguageFlavor.UNKNOWN.toExternal();
            default: return LanguageFlavor.UNKNOWN.toExternal();
        }
    }    
    
    public void setCppStandard(IntConfiguration cppStandard) {
        this.cppStandard = cppStandard;
    }

    public void setCppStandardExternal(int cppStandard) {
        if (cppStandard == LanguageFlavor.DEFAULT.toExternal()) {
            this.cppStandard.setValue(STANDARD_DEFAULT);
        } else if (cppStandard == LanguageFlavor.CPP.toExternal()) {
            this.cppStandard.setValue(STANDARD_CPP98);
        } else if (cppStandard == LanguageFlavor.CPP11.toExternal()) {
            this.cppStandard.setValue(STANDARD_CPP11);
        } else if (cppStandard == LanguageFlavor.UNKNOWN.toExternal()) {
            this.cppStandard.setValue(STANDARD_INHERITED);
        }
    }
    
    // Clone and assign
    public void assign(CCCompilerConfiguration conf) {
        // From XCompiler
        super.assign(conf);
        getCppStandard().assign(conf.getCppStandard());
    }
    
    @Override
    public boolean getModified() {
        return super.getModified() || getCppStandard().getModified();
    }    
    
    public boolean isCppStandardChanged() {
        return getCppStandard().getDirty() && getCppStandard().getPreviousValue() != getInheritedCppStandard();
    }
    
    // Cloning
    @Override
    public CCCompilerConfiguration clone() {
        CCCompilerConfiguration clone = new CCCompilerConfiguration(getBaseDir(), (CCCompilerConfiguration)getMaster(),  getOwner());
        // BasicCompilerConfiguration
        clone.setDevelopmentMode(getDevelopmentMode().clone());
        clone.setWarningLevel(getWarningLevel().clone());
        clone.setMTLevel(getMTLevel().clone());
        clone.setSixtyfourBits(getSixtyfourBits().clone());
        clone.setStrip(getStrip().clone());
        clone.setAdditionalDependencies(getAdditionalDependencies().clone());
        clone.setTool(getTool().clone());
        clone.setCommandLineConfiguration(getCommandLineConfiguration().clone());
        // From CCCCompiler
        clone.setMTLevel(getMTLevel().clone());
        clone.setLibraryLevel(getLibraryLevel().clone());
        clone.setStandardsEvolution(getStandardsEvolution().clone());
        clone.setLanguageExt(getLanguageExt().clone());
        clone.setIncludeDirectories(getIncludeDirectories().clone());
        clone.setInheritIncludes(getInheritIncludes().clone());
        clone.setPreprocessorConfiguration(getPreprocessorConfiguration().clone());
        clone.setInheritPreprocessor(getInheritPreprocessor().clone());
        clone.setUndefinedPreprocessorConfiguration(getUndefinedPreprocessorConfiguration().clone());
        clone.setInheritUndefinedPreprocessor(getInheritUndefinedPreprocessor().clone());
        clone.setUseLinkerLibraries(getUseLinkerLibraries().clone());
        // From CCCompiler
        clone.setCppStandard(getCppStandard().clone());
        return clone;
    }
    
    // Interface OptionsProvider
    @Override
    public String getOptions(AbstractCompiler compiler) {
        StringBuilder options = new StringBuilder("$(COMPILE.cc) "); // NOI18N
        options.append(getAllOptions2(compiler)).append(' '); // NOI18N
        options.append(getCommandLineOptions(true));
        return CppUtils.reformatWhitespaces(options.toString());
    }

    public String getCCFlagsBasic(AbstractCompiler compiler) {
        String options = ""; // NOI18N
        options += compiler.getMTLevelOptions(getMTLevel().getValue()) + " "; // NOI18N
        options += compiler.getLibraryLevelOptions(getLibraryLevel().getValue()) + " "; // NOI18N
        options += compiler.getStandardEvaluationOptions(getStandardsEvolution().getValue()) + " "; // NOI18N
        options += compiler.getLanguageExtOptions(getLanguageExt().getValue()) + " "; // NOI18N
        //options += compiler.getStripOption(getStrip().getValue()) + " "; // NOI18N
        options += compiler.getSixtyfourBitsOption(getSixtyfourBits().getValue()) + " "; // NOI18N
        if (getDevelopmentMode().getValue() == DEVELOPMENT_MODE_TEST) {
            options += compiler.getDevelopmentModeOptions(DEVELOPMENT_MODE_TEST);
        }
        return CppUtils.reformatWhitespaces(options);
    }
    
    public String getCCFlags(AbstractCompiler compiler) {
        String options = getCCFlagsBasic(compiler) + " "; // NOI18N
        options += getCommandLineConfiguration().getValue() + " "; // NOI18N
        return CppUtils.reformatWhitespaces(options);
    }
    
    @Override
    public String getAllOptions(Tool tool) {
        if (!(tool instanceof AbstractCompiler)) {
            return "";
        }
        AbstractCompiler compiler = (AbstractCompiler) tool;
        
        StringBuilder options = new StringBuilder();
        options.append(getCCFlagsBasic(compiler));
        options.append(" "); // NOI18N
        CCCompilerConfiguration master = this;
        while (master != null) {
            options.append(master.getCommandLineConfiguration().getValue());
            options.append(" "); // NOI18N
            master = (CCCompilerConfiguration)master.getMaster();
        }
        options.append(getAllOptions2(compiler));
        options.append(" "); // NOI18N
        return CppUtils.reformatWhitespaces(options.toString());
    }
    
    public String getAllOptions2(AbstractCompiler compiler) {
        String options = ""; // NOI18N
        if (getDevelopmentMode().getValue() != DEVELOPMENT_MODE_TEST) {
            options += compiler.getDevelopmentModeOptions(getDevelopmentMode().getValue()) + " "; // NOI18N
        }
        options += compiler.getWarningLevelOptions(getWarningLevel().getValue()) + " "; // NOI18N
        options += compiler.getStripOption(getStrip().getValue()) + " "; // NOI18N
        options += getPreprocessorOptions(compiler.getCompilerSet());
        options += getIncludeDirectoriesOptions(compiler.getCompilerSet());
        options += getLibrariesFlags();
        options += compiler.getCppStandardOptions(getInheritedCppStandard());
        return CppUtils.reformatWhitespaces(options);
    }

    public int getInheritedCppStandard() {
        CCCompilerConfiguration master = this;
        while (master != null) {
            if (master.getCppStandard().getValue() != STANDARD_INHERITED) {
                return master.getCppStandard().getValue();
            }
            master = (CCCompilerConfiguration) master.getMaster();
        }
        return STANDARDS_DEFAULT;
    }
    
    public String getPreprocessorOptions(CompilerSet cs) {
        CCCompilerConfiguration master = (CCCompilerConfiguration)getMaster();
        OptionToString visitor = new OptionToString(null, getUserMacroFlag(cs));
        List<CCCCompilerConfiguration> list = new ArrayList<CCCCompilerConfiguration>();
        list.add(this);
        while (master != null && getInheritPreprocessor().getValue()) {
            list.add(master);
            if (master.getInheritPreprocessor().getValue()) {
                master = (CCCompilerConfiguration) master.getMaster();
            } else {
                master = null;
            }
        }
        StringBuilder options = new StringBuilder();
        for(int i = list.size() - 1; i >= 0; i--) {
            options.append(list.get(i).getPreprocessorConfiguration().toString(visitor));
            options.append(' '); // NOI18N
        }
        return options.toString();
    }
    
    public String getIncludeDirectoriesOptions(CompilerSet cs) {
        CCCompilerConfiguration master = (CCCompilerConfiguration)getMaster();
        OptionToString visitor = new OptionToString(cs, getUserIncludeFlag(cs));
        StringBuilder options = new StringBuilder(getIncludeDirectories().toString(visitor));
        options.append(' '); // NOI18N
        List<CCCCompilerConfiguration> list = new ArrayList<CCCCompilerConfiguration>();
        while (master != null && getInheritIncludes().getValue()) {
            list.add(master);
            if (master.getInheritIncludes().getValue()) {
                master = (CCCompilerConfiguration) master.getMaster();
            } else {
                master = null;
            }
        }
        for(int i = list.size() - 1; i >= 0; i--) {
            options.append(list.get(i).getIncludeDirectories().toString(visitor));
            options.append(' '); // NOI18N
        }
        return options.toString();
    } 

    @Override
    protected String getUserIncludeFlag(CompilerSet cs){
        return cs.getCompilerFlavor().getToolchainDescriptor().getCpp().getUserIncludeFlag();
    }

    @Override
    protected String getUserMacroFlag(CompilerSet cs){
        return cs.getCompilerFlavor().getToolchainDescriptor().getCpp().getUserMacroFlag();
    }

    // Sheet
    public Sheet getSheet(MakeConfiguration conf, Folder folder, Item item) {
        Sheet sheet = new Sheet();
        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        AbstractCompiler ccCompiler = compilerSet == null ? null : (AbstractCompiler)compilerSet.getTool(PredefinedToolKind.CCCompiler);

        IntNodeProp standardProp = new IntNodeProp(getCppStandard(), true, "CPPStandard", getString("CPPStandardTxt"), getString("CPPStandardHint")) {  // NOI18N

                @Override
                public PropertyEditor getPropertyEditor() {
                    if (intEditor == null) {
                        intEditor = new NewIntEditor();
                    }
                    return intEditor;
                }

                class NewIntEditor extends IntEditor {

                    @Override
                    public String getAsText() {
                        if (CCCompilerConfiguration.this.getCppStandard().getValue() == STANDARD_INHERITED) {
                             return NbBundle.getMessage(CCCompilerConfiguration.class, "STANDARD_INHERITED_WITH_VALUE", STANDARD_NAMES[CCCompilerConfiguration.this.getInheritedCppStandard()]); //NOI18N
                        }
                        return super.getAsText();
                    }
                                       
                }
         
        };
        Sheet.Set set0 = getSet();
        sheet.put(set0);
        if (conf.isCompileConfiguration()) {
            if (folder == null) {
                Sheet.Set bset = getBasicSet();
                sheet.put(bset);
                if (compilerSet != null && compilerSet.getCompilerFlavor().isSunStudioCompiler()) { // FIXUP: should be moved to SunCCompiler                
                    Sheet.Set set2 = new Sheet.Set();
                    set2.setName("OtherOptions"); // NOI18N
                    set2.setDisplayName(getString("OtherOptionsTxt"));
                    set2.setShortDescription(getString("OtherOptionsHint"));
                    set2.put(new IntNodeProp(getMTLevel(), getMaster() != null ? false : true, "MultithreadingLevel", getString("MultithreadingLevelTxt"), getString("MultithreadingLevelHint"))); // NOI18N
                    set2.put(new IntNodeProp(getLibraryLevel(), getMaster() != null ? false : true, "LibraryLevel", getString("LibraryLevelTxt"), getString("LibraryLevelHint"))); // NOI18N
                    set2.put(new IntNodeProp(getStandardsEvolution(), getMaster() != null ? false : true, "StandardsEvolution", getString("StandardsEvolutionTxt"), getString("StandardsEvolutionHint"))); // NOI18N
                    set2.put(new IntNodeProp(getLanguageExt(), getMaster() != null ? false : true, "LanguageExtensions", getString("LanguageExtensionsTxt"), getString("LanguageExtensionsHint"))); // NOI18N
                    sheet.put(set2);
                } else {
                    bset.put(standardProp);
                }
                if (getMaster() != null) {
                    sheet.put(getInputSet());
                }

                Sheet.Set set4 = new Sheet.Set();
                set4.setName("Tool"); // NOI18N
                set4.setDisplayName(getString("ToolTxt1"));
                set4.setShortDescription(getString("ToolHint1"));
                if (ccCompiler != null) {
                    set4.put(new StringNodeProp(getTool(), ccCompiler.getName(), false, "Tool", getString("ToolTxt2"), getString("ToolHint2"))); // NOI18N
                }
                sheet.put(set4);
            }
            
            String[] texts = new String[]{getString("AdditionalOptionsTxt1"), getString("AdditionalOptionsHint"), getString("AdditionalOptionsTxt2"), getString("AllOptionsTxt")};
            Sheet.Set set2 = new Sheet.Set();
            set2.setName("CommandLine"); // NOI18N
            set2.setDisplayName(getString("CommandLineTxt"));
            set2.setShortDescription(getString("CommandLineHint"));
            if (ccCompiler != null) {
                set2.put(new OptionsNodeProp(getCommandLineConfiguration(), null, this, ccCompiler, null, texts));
            }
            sheet.put(set2);
        } else if (conf.getConfigurationType().getValue() == MakeConfiguration.TYPE_MAKEFILE && item != null && ccCompiler != null) {
            AllOptionsProvider options = CompileOptionsProvider.getDefault().getOptions(item);
            if (options != null) {
                String compileLine = options.getAllOptions(ccCompiler);
                if (compileLine != null) {
                    int hasPath = compileLine.indexOf('#');
                    if (hasPath >= 0) {
                        set0.put(new StringRONodeProp(getString("CommandLineTxt"), getString("CommandLineHint"), compileLine.substring(hasPath+1)));
                        set0.put(new StringRONodeProp(getString("CompileFolderTxt"), getString("CompileFolderHint"), compileLine.substring(0, hasPath)));
                    } else {
                        set0.put(new StringRONodeProp(getString("CommandLineTxt"), getString("CommandLineHint"), compileLine.substring(hasPath)));
                    }
                }
            }
        }  
        if (conf.getConfigurationType().getValue() == MakeConfiguration.TYPE_MAKEFILE) {
            if (compilerSet == null || !compilerSet.getCompilerFlavor().isSunStudioCompiler()) {
                set0.put(standardProp);
            }
        }
        if (conf.getConfigurationType().getValue() == MakeConfiguration.TYPE_QT_APPLICATION || 
            conf.getConfigurationType().getValue() == MakeConfiguration.TYPE_QT_DYNAMIC_LIB || 
           conf.getConfigurationType().getValue() == MakeConfiguration.TYPE_QT_STATIC_LIB) {
            if (compilerSet == null || !compilerSet.getCompilerFlavor().isSunStudioCompiler()) {
                set0.put(standardProp);
            }
        }
        
        return sheet;
    }
    
    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(CCCompilerConfiguration.class, s);
    }
}
