/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.makeproject.configurations.CppUtils;
import org.netbeans.modules.cnd.makeproject.configurations.ui.StringListNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.VectorNodeProp;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public abstract class CCCCompilerConfiguration extends BasicCompilerConfiguration {

    public static final int LIBRARY_LEVEL_NONE = 0;
    public static final int LIBRARY_LEVEL_RUNTIME = 1;
    public static final int LIBRARY_LEVEL_CLASSIC = 2;
    public static final int LIBRARY_LEVEL_BINARY = 3;
    public static final int LIBRARY_LEVEL_CONFORMING = 4;
    private static final String[] LIBRARY_LEVEL_NAMES = {
        getString("NoneTxt"),
        getString("RuntimeOnlyTxt"),
        getString("ClassicIostreamsTxt"),
        getString("BinaryStandardTxt"),
        getString("ConformingStandardTxt"),};
    private static final String[] LIBRARY_LEVEL_OPTIONS = null;
    private IntConfiguration libraryLevel;
    public static final int STANDARDS_OLD = 0;
    public static final int STANDARDS_LEGACY = 1;
    public static final int STANDARDS_DEFAULT = 2;
    public static final int STANDARDS_MODERN = 3;
    private static final String[] STANDARDS_NAMES = {
        getString("OldTxt"),
        getString("LegacyTxt"),
        getString("DefaultTxt"),
        getString("ModernTxt"),};
    private static final String[] STANDARD_OPTIONS = null;
    private IntConfiguration standardsEvolution;
    public static final int LANGUAGE_EXT_NONE = 0;
    public static final int LANGUAGE_EXT_DEFAULT = 1;
    public static final int LANGUAGE_EXT_ALL = 2;
    private static final String[] LANGUAGE_EXT_NAMES = {
        getString("NoneTxt"),
        getString("DefaultTxt"),
        getString("AllTxt"),};
    private static final String[] LANGUAGE_EXT_OPTIONS = null;
    private IntConfiguration languageExt;
    private VectorConfiguration<String> includeDirectories;
    private BooleanConfiguration inheritIncludes;
    private VectorConfiguration<String> preprocessorConfiguration;
    private BooleanConfiguration inheritPreprocessor;

    // Constructors
    protected CCCCompilerConfiguration(String baseDir, CCCCompilerConfiguration master) {
        super(baseDir, master);
        libraryLevel = new IntConfiguration(master != null ? master.getLibraryLevel() : null, LIBRARY_LEVEL_BINARY, LIBRARY_LEVEL_NAMES, getLibraryLevelOptions());
        standardsEvolution = new IntConfiguration(master != null ? master.getStandardsEvolution() : null, STANDARDS_DEFAULT, STANDARDS_NAMES, getStandardsEvolutionOptions());
        languageExt = new IntConfiguration(master != null ? master.getLanguageExt() : null, LANGUAGE_EXT_DEFAULT, LANGUAGE_EXT_NAMES, getLanguageExtOptions());
        includeDirectories = new VectorConfiguration<String>(master != null ? master.getIncludeDirectories() : null);
        inheritIncludes = new BooleanConfiguration(null, true, null, null);
        preprocessorConfiguration = new VectorConfiguration<String>(master != null ? master.getPreprocessorConfiguration() : null);
        inheritPreprocessor = new BooleanConfiguration(null, true, null, null);
    }

    public void fixupMasterLinks(CCCCompilerConfiguration compilerConfiguration) {
        super.fixupMasterLinks(compilerConfiguration);
        getMTLevel().setMaster(compilerConfiguration.getMTLevel());
        getLibraryLevel().setMaster(compilerConfiguration.getLibraryLevel());
        getStandardsEvolution().setMaster(compilerConfiguration.getStandardsEvolution());
        getLanguageExt().setMaster(compilerConfiguration.getLanguageExt());
    }

    @Override
    public boolean getModified() {
        return super.getModified() ||
                libraryLevel.getModified() ||
                standardsEvolution.getModified() ||
                languageExt.getModified() ||
                includeDirectories.getModified() ||
                inheritIncludes.getModified() ||
                preprocessorConfiguration.getModified() ||
                inheritPreprocessor.getModified();
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
    public VectorConfiguration<String> getIncludeDirectories() {
        return includeDirectories;
    }

    public void setIncludeDirectories(VectorConfiguration<String> includeDirectories) {
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
    public VectorConfiguration<String> getPreprocessorConfiguration() {
        return preprocessorConfiguration;
    }

    public void setPreprocessorConfiguration(VectorConfiguration<String> preprocessorConfiguration) {
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
    protected void assign(CCCCompilerConfiguration conf) {
        // BasicCompilerConfiguration
        super.assign(conf);
        // XCompilerConfiguration
        getLibraryLevel().assign(conf.getLibraryLevel());
        getStandardsEvolution().assign(conf.getStandardsEvolution());
        getLanguageExt().assign(conf.getLanguageExt());
        getIncludeDirectories().assign(conf.getIncludeDirectories());
        getInheritIncludes().assign(conf.getInheritIncludes());
        getPreprocessorConfiguration().assign(conf.getPreprocessorConfiguration());
        getInheritPreprocessor().assign(conf.getInheritPreprocessor());
    }

    // Sheet
    protected Sheet.Set getSet() {
        CCCCompilerConfiguration master;
        OptionToString visitor = new OptionToString(null, null);

        Sheet.Set set1 = new Sheet.Set();
        set1.setName("General"); // NOI18N
        set1.setDisplayName(getString("GeneralTxt"));
        set1.setShortDescription(getString("GeneralHint"));
        // Include Dirctories
        StringBuilder inheritedValues = new StringBuilder();
        master = (CCCCompilerConfiguration) getMaster();
        while (master != null) {
            inheritedValues.append(master.getIncludeDirectories().toString(visitor));
            if (master.getInheritIncludes().getValue()) {
                master = (CCCCompilerConfiguration) master.getMaster();
            } else {
                master = null;
            }
        }
        set1.put(new VectorNodeProp(getIncludeDirectories(), getInheritIncludes(), getBaseDir(), new String[]{"IncludeDirectories", getString("IncludeDirectoriesTxt"), getString("IncludeDirectoriesHint"), inheritedValues.toString()}, true, new HelpCtx("AddtlIncludeDirectories"))); // NOI18N
        // Preprocessor Macros
        inheritedValues = new StringBuilder();
        master = (CCCCompilerConfiguration) getMaster();
        while (master != null) {
            inheritedValues.append(master.getPreprocessorConfiguration().toString(visitor));
            if (master.getInheritPreprocessor().getValue()) {
                master = (CCCCompilerConfiguration) master.getMaster();
            } else {
                master = null;
            }
        }
        set1.put(new StringListNodeProp(getPreprocessorConfiguration(), getInheritPreprocessor(), new String[]{"preprocessor-definitions", getString("PreprocessorDefinitionsTxt"), getString("PreprocessorDefinitionsHint"), getString("PreprocessorDefinitionsLbl"), inheritedValues.toString()}, true, new HelpCtx("preprocessor-definitions"))); // NOI18N

        return set1;
    }

    // Sheet
    protected Sheet getSheet(Project project) {
        Sheet sheet = new Sheet();
        sheet.put(getSet());
        return sheet;
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(CCCCompilerConfiguration.class, s);
    }

    protected abstract String getUserIncludeFlag(CompilerSet cs);

    protected abstract String getUserMacroFlag(CompilerSet cs);

    public static class OptionToString implements VectorConfiguration.ToString<String> {

        private final CompilerSet compilerSet;
        private final String prepend;

        public OptionToString(CompilerSet compilerSet, String prepend) {
            this.compilerSet = compilerSet;
            this.prepend = prepend;
        }

        @Override
        public String toString(String item) {
            if (0 < item.length()) {
                if (compilerSet != null) {
                    item = CppUtils.normalizeDriveLetter(compilerSet, item);
                }
                item = CndPathUtilitities.escapeOddCharacters(item);
                return prepend == null ? item : prepend + item;
            } else {
                return ""; // NOI18N
            }
        }
    }
}
