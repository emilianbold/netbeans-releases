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
import org.netbeans.modules.cnd.makeproject.spi.configurations.AllOptionsProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.BooleanNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.OptionsNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.StringNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.CppUtils;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

public class ArchiverConfiguration implements AllOptionsProvider {
    private MakeConfiguration makeConfiguration;
    
    private StringConfiguration output;
    private NamedBooleanConfiguration runRanlib;
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
        runRanlib = new NamedBooleanConfiguration(true, "", "$(RANLIB)"); // NOI18N
        replaceOption = new BooleanConfiguration(true);
        verboseOption = new BooleanConfiguration(true);
        supressOption = new BooleanConfiguration(false);
        commandLineConfiguration = new OptionsConfiguration();
        additionalDependencies = new OptionsConfiguration();
        tool = new StringConfiguration(null, "ar"); // NOI18N
    }
    
    // MakeConfiguration
    public void setMakeConfiguration(MakeConfiguration makeConfiguration) {
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
    public void setRunRanlib(NamedBooleanConfiguration runRanlib) {
        this.runRanlib = runRanlib;
    }
    public NamedBooleanConfiguration getRunRanlib() {
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
        //setMakeConfiguration(conf.getMakeConfiguration()); // MakeConfiguration should not be assigned
        getOutput().assign(conf.getOutput());
        getRunRanlib().assign(conf.getRunRanlib());
        getReplaceOption().assign(conf.getReplaceOption());
        getVerboseOption().assign(conf.getVerboseOption());
        getSupressOption().assign(conf.getSupressOption());
        getAdditionalDependencies().assign(conf.getAdditionalDependencies());
        getCommandLineConfiguration().assign(conf.getCommandLineConfiguration());
        getTool().assign(conf.getTool());
    }
    
    @Override
    public ArchiverConfiguration clone() {
        ArchiverConfiguration clone = new ArchiverConfiguration(getMakeConfiguration());
        // ArchiverConfiguration
        clone.setOutput(getOutput().clone());
        clone.setRunRanlib(getRunRanlib().clone());
        clone.setReplaceOption(getReplaceOption().clone());
        clone.setVerboseOption(getVerboseOption().clone());
        clone.setSupressOption(getSupressOption().clone());
        clone.setAdditionalDependencies(getAdditionalDependencies().clone());
        clone.setCommandLineConfiguration(getCommandLineConfiguration().clone());
        clone.setTool(getTool().clone());
        return clone;
    }
    
    // Interface OptionsProvider
    public String getOptions() {
        String options = getAllOptions(false) + " "; // NOI18N
        options += getCommandLineConfiguration().getValue() + " "; // NOI18N
        options += getOutputValue() + " "; // NOI18N
        return CppUtils.reformatWhitespaces(options);
    }
    
    @Override
    public String getAllOptions(Tool tool) {
        return getAllOptions(true);
    }
    
    private String getAllOptions(boolean includeOutput) {
        StringBuilder options = new StringBuilder();

        options.append(getReplaceOption().getValue() ? "r" : ""); // NOI18N
        options.append(getVerboseOption().getValue() ? "v" : ""); // NOI18N
        options.append(getSupressOption().getValue() ? "c" : ""); // NOI18N
        if (options.length() > 0) {
            options.insert(0, "-"); // NOI18N
        }
        options.append(" "); // NOI18N
        if (includeOutput) {
            options.append(getOutputValue());  // NOI18N
            options.append(" ");  // NOI18N
        }
        return CppUtils.reformatWhitespaces(options.toString());
    }
    
    // Sheet
    public Sheet getGeneralSheet() {
        Sheet sheet = new Sheet();
        Sheet.Set set1 = new Sheet.Set();
        set1.setName("General"); // NOI18N
        set1.setDisplayName(getString("GeneralTxt"));
        set1.setShortDescription(getString("GeneralHint"));
        set1.put(new OutputNodeProp(getOutput(), getOutputValue(), "Output", getString("OutputTxt"), getString("OutputHint"))); // NOI18N
        set1.put(new BooleanNodeProp(getRunRanlib(), true, "RunRanlib", getString("RunRanlibTxt"), getString("RunRanlibHint"))); // NOI18N
        sheet.put(set1);
        Sheet.Set set2 = new Sheet.Set();
        set2.setName("Options"); // NOI18N
        set2.setDisplayName(getString("OptionsTxt"));
        set2.setShortDescription(getString("OptionsHint"));
        //set2.put(new BooleanNodeProp(getReplaceOption(), "Add", "Add", "Add (-r)"));
        set2.put(new BooleanNodeProp(getVerboseOption(), true, "Verbose", getString("VerboseTxt"), getString("VerboseHint"))); // NOI18N
        set2.put(new BooleanNodeProp(getSupressOption(), true, "SupressDiagnostics", getString("SupressDiagnosticsTxt"), getString("SupressDiagnosticsHint"))); // NOI18N
        sheet.put(set2);
        Sheet.Set set3 = new Sheet.Set();
        String [] texts = new String[] {getString("AdditionalDependenciesTxt1"), getString("AdditionalDependenciesHint"), getString("AdditionalDependenciesTxt2"), getString("InheritedValuesTxt")}; // NOI18N
        set3.setName("Input"); // NOI18N
        set3.setDisplayName(getString("InputTxt"));
        set3.setShortDescription(getString("InputHint"));
        set3.put(new OptionsNodeProp(getAdditionalDependencies(), null, new AdditionalDependenciesOptions(), null, ",", texts)); // NOI18N
        sheet.put(set3);
        Sheet.Set set4 = new Sheet.Set();
        set4.setName("Tool"); // NOI18N
        set4.setDisplayName(getString("ToolTxt1"));
        set4.setShortDescription(getString("ToolHint1"));
        set4.put(new StringNodeProp(getTool(), "Tool", getString("ToolTxt2"), getString("ToolHint2"))); // NOI18N
        sheet.put(set4);
        
        texts = new String[] {getString("AdditionalOptionsTxt1"), getString("AdditionalOptionsHint"), getString("AdditionalOptionsTxt2"), getString("AllOptionsTxt")};
        set2 = new Sheet.Set();
        set2.setName("CommandLine"); // NOI18N
        set2.setDisplayName(getString("CommandLineTxt"));
        set2.setShortDescription(getString("CommandLineHint"));
        set2.put(new OptionsNodeProp(getCommandLineConfiguration(), null, this, null, null, texts));
        sheet.put(set2);
        
        return sheet;
    }
    
    private class AdditionalDependenciesOptions implements AllOptionsProvider {
        public String getOptions() {
            return null; // Not used
        }
        
        @Override
        public String getAllOptions(Tool tool) {
            String options = ""; // NOI18N
            options += additionalDependencies.getPreDefined();
            return CppUtils.reformatWhitespaces(options);
        }
    }
    
    public String getOutputValue() {
        if (getOutput().getModified()) {
            return getOutput().getValue();
        } else {
            return getOutputDefault();
        }
    }
    
    private static class OutputNodeProp extends StringNodeProp {
        public OutputNodeProp(StringConfiguration stringConfiguration, String def, String txt1, String txt2, String txt3) {
            super(stringConfiguration, def, txt1, txt2, txt3);
        }
        
        @Override
        public void setValue(String v) {
            if (CndPathUtilitities.hasMakeSpecialCharacters(v)) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(getString("SPECIAL_CHARATERS_ERROR"), NotifyDescriptor.ERROR_MESSAGE));
                return;
            }
            super.setValue(v);
        }
    }
    
    private String getOutputDefault() {
        String outputName = CndPathUtilitities.getBaseName(getMakeConfiguration().getBaseDir()).toLowerCase();
        switch (getMakeConfiguration().getConfigurationType().getValue()) {
            case MakeConfiguration.TYPE_STATIC_LIB:
                outputName = "lib" + outputName + ".a"; // NOI18N
                break;
        }
        outputName = ConfigurationSupport.makeNameLegal(outputName);
        return "${CND_DISTDIR}" + "/" + "${CND_CONF}" + "/" + "${CND_PLATFORM}" + "/" + outputName; // UNIX path // NOI18N
    }
    
    /*
     * Default output pre version 28
     */
    public String getOutputDefault27() {
        String outputName = CndPathUtilitities.getBaseName(getMakeConfiguration().getBaseDir()).toLowerCase();
        outputName = "lib" + outputName + ".a"; // NOI18N
        return MakeConfiguration.DIST_FOLDER + "/" + getMakeConfiguration().getName() + "/" + outputName; // UNIX path // NOI18N
    }
    
    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(ArchiverConfiguration.class, s);
    }
}
