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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.makeproject.platform.Platform;
import org.netbeans.modules.cnd.makeproject.platform.Platforms;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.BooleanNodeProp;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.IntNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.PackagingNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.StringNodeProp;
import org.netbeans.modules.cnd.makeproject.api.PackagerFileElement;
import org.netbeans.modules.cnd.makeproject.api.PackagerFileElement.FileType;
import org.netbeans.modules.cnd.makeproject.api.PackagerDescriptor;
import org.netbeans.modules.cnd.makeproject.api.PackagerInfoElement;
import org.netbeans.modules.cnd.makeproject.api.PackagerManager;
import org.netbeans.modules.cnd.makeproject.packaging.DummyPackager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

public class PackagingConfiguration {

    private MakeConfiguration makeConfiguration;    // Types
    private StringConfiguration type;
    private BooleanConfiguration verbose;
    private VectorConfiguration<PackagerInfoElement> info;
    private VectorConfiguration<String> additionalInfo;
    private VectorConfiguration<PackagerFileElement> files;
    private StringConfiguration output;
    private StringConfiguration tool;
    private StringConfiguration options;
    private StringConfiguration topDir;
    // Constructors
    public PackagingConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
        type = new StringConfiguration(null, "Tar"); // NOI18N // Fixup: better default...
        verbose = new BooleanConfiguration(true);
        info = new VectorConfiguration<PackagerInfoElement>(null); // NOI18N
        additionalInfo = new VectorConfiguration<String>(null); // NOI18N
        files = new VectorConfiguration<PackagerFileElement>(null); // NOI18N
        output = new StringConfiguration(null, ""); // NOI18N
        tool = new StringConfiguration(null, ""); // NOI18N
        options = new StringConfiguration(null, ""); // NOI18N
        topDir = new StringConfiguration(null, null); // NOI18N
        
        setDefaultValues();
    }
    
    public final void setDefaultValues() {
        // Init default values
        String perm = MakeOptions.getInstance().getDefExePerm();
        String packageDir = "${PACKAGE_TOP_DIR}bin"; // NOI18N
        String suffix = ""; // NOI18N

        if (makeConfiguration.isMakefileConfiguration()) {
            perm = MakeOptions.getInstance().getDefExePerm();
            packageDir = "${PACKAGE_TOP_DIR}bin"; // NOI18N
        } else if (makeConfiguration.isApplicationConfiguration()) {
            perm = MakeOptions.getInstance().getDefExePerm();
            packageDir = "${PACKAGE_TOP_DIR}bin"; // NOI18N
            if (makeConfiguration.getDevelopmentHost().getBuildPlatform() == PlatformTypes.PLATFORM_WINDOWS) {
                suffix = ".exe"; // NOI18N
            }
        } else if (makeConfiguration.isLibraryConfiguration()) {
            perm = MakeOptions.getInstance().getDefFilePerm();
            packageDir = "${PACKAGE_TOP_DIR}lib"; // NOI18N
        }
        else {
            assert false;
        }
        PackagerFileElement elem = new PackagerFileElement(
                FileType.FILE,
                "${OUTPUT_PATH}" + suffix, // NOI18N
                packageDir + "/${OUTPUT_BASENAME}" + suffix, // NOI18N
                perm,
                MakeOptions.getInstance().getDefOwner(),
                MakeOptions.getInstance().getDefGroup());
        elem.setDefaultValue(true);
        files.add(elem);
        
        // Add default info lists
        List<PackagerInfoElement> infoList = getInfo().getValue();
        List<PackagerDescriptor> packagerList = PackagerManager.getDefault().getPackagerList();
        for (PackagerDescriptor packagerDescriptor : packagerList) {
            if (packagerDescriptor.hasInfoList()) {
                infoList.addAll(packagerDescriptor.getDefaultInfoList(makeConfiguration, this));
            }
        }
    }
    
    public List<PackagerInfoElement> getHeaderSubList(String packager) {
        List<PackagerInfoElement> list = new ArrayList<PackagerInfoElement>();
        List<PackagerInfoElement> headerList = getInfo().getValue();
        for (PackagerInfoElement elem : headerList) {
            if (elem.getPackager().equals(packager)) {
                list.add(elem);
            }
        }
        return list;
    }
    
    public boolean isModified() {
        if (getType().getModified() || getOutput().getModified() || getOptions().getModified() || getVerbose().getModified() || getTool().getModified()) {
            return true;
        }
        if (files.getValue().size() != 1) {
            return true;
        }
        for (PackagerFileElement elem : files.getValue()) {
            if (!elem.isDefaultValue()) {
                return true;
            }
        }
        
        List<PackagerInfoElement> headerList = getHeaderSubList(getType().getValue());
        PackagerDescriptor packager = PackagerManager.getDefault().getPackager(getType().getValue());
        if (packager != null && packager.hasInfoList()) {
            if (headerList.size() != packager.getDefaultInfoList(makeConfiguration, this).size()) {
                return true;
            }
        }
        return false;
    }
    
    // MakeConfiguration
    public void setMakeConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
    }

    public MakeConfiguration getMakeConfiguration() {
        return makeConfiguration;
    }

    public StringConfiguration getType() {
        return type;
    }

    public void setType(StringConfiguration type) {
        PackagerDescriptor packager = PackagerManager.getDefault().getPackager(type.getValue());
        if (packager == null) {
            // Doesn't exist. Switch to a dummy packager...
            packager = new DummyPackager(type.getValue());
            PackagerManager.getDefault().addPackagingDescriptor(packager);
        }
        this.type = type;
    }

    public BooleanConfiguration getVerbose() {
        return verbose;
    }

    public void setVerbose(BooleanConfiguration verbose) {
        this.verbose = verbose;
    }

    public VectorConfiguration<PackagerInfoElement> getInfo() {
        return info;
    }

    public void setInfo(VectorConfiguration<PackagerInfoElement> svr4Header) {
        this.info = svr4Header;
    }

    public VectorConfiguration<String> getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(VectorConfiguration<String> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public VectorConfiguration<PackagerFileElement> getFiles() {
        return files;
    }

    public void setFiles(VectorConfiguration<PackagerFileElement> files) {
        this.files = files;
    }

    public void setOutput(StringConfiguration output) {
        this.output = output;
    }

    public StringConfiguration getOutput() {
        return output;
    }

    public void setTool(StringConfiguration output) {
        this.tool = output;
    }

    public StringConfiguration getTool() {
        return tool;
    }

    public void setOptions(StringConfiguration options) {
        this.options = options;
    }

    public StringConfiguration getOptions() {
        return options;
    }
    
    public void setTopDir(StringConfiguration topDir) {
        this.topDir = topDir;
    }

    public StringConfiguration getTopDir() {
        return topDir;
    }
    
    // Clone and assign
    public void assign(PackagingConfiguration conf) {
        //setMakeConfiguration(conf.getMakeConfiguration()); // MakeConfiguration should not be assigned
        getType().assign(conf.getType());
        getVerbose().assign(conf.getVerbose());
        getInfo().assign(conf.getInfo());
        getAdditionalInfo().assign(conf.getAdditionalInfo());
        getFiles().assign(conf.getFiles());
        getOutput().assign(conf.getOutput());
        getTool().assign(conf.getTool());
        getOptions().assign(conf.getOptions());
        getTopDir().assign(conf.getTopDir());
    }

    @Override
    public PackagingConfiguration clone() {
        PackagingConfiguration clone = new PackagingConfiguration(getMakeConfiguration());
        clone.setType(getType().clone());
        clone.setVerbose(getVerbose().clone());
        clone.setInfo(getInfo().clone());
        clone.setAdditionalInfo(getAdditionalInfo().clone());
        clone.setFiles(getFiles().clone());
        clone.setOutput(getOutput().clone());
        clone.setTool(getTool().clone());
        clone.setOptions(getOptions().clone());
        clone.setTopDir(getTopDir().clone());
        return clone;
    }
    
    private TypePropertyChangeListener typePropertyChangeListener;
    // Sheet
    public Sheet getGeneralSheet(JPanel makeCustomizer) {
        IntNodeProp intNodeprop;
        OutputNodeProp outputNodeProp;
        StringNodeProp toolNodeProp;
        StringNodeProp optionsNodeProp;

        Sheet sheet = new Sheet();
        Sheet.Set set = new Sheet.Set();
        set.setName("General"); // NOI18N
        set.setDisplayName(getString("GeneralTxt"));
        set.setShortDescription(getString("GeneralHint"));

        IntConfiguration tmpIntConfiguration = new PackagerIntConfiguration(null, 0, PackagerManager.getDefault().getDisplayNames(), null);
        
        set.put(intNodeprop = new PackagerIntNodeProp(tmpIntConfiguration, true, "PackageType", getString("PackageTypeName"), getString("PackageTypeHint"))); // NOI18N
        set.put(outputNodeProp = new OutputNodeProp(getOutput(), getOutputDefault(), "Output", getString("OutputTxt"), getString("OutputHint"))); // NOI18N
        String[] texts = new String[]{"Files", getString("FilesName"), getString("FilesHint")}; // NOI18N
        set.put(new PackagingNodeProp(this, makeConfiguration, texts)); // NOI18N
        set.put(toolNodeProp = new StringNodeProp(getTool(), getToolDefault(), "Tool", getString("ToolTxt1"), getString("ToolHint1"))); // NOI18N
        set.put(optionsNodeProp = new StringNodeProp(getOptions(), getOptionsDefault(), "AdditionalOptions", getString("AdditionalOptionsTxt1"), getString("AdditionalOptionsHint"))); // NOI18N
        set.put(new BooleanNodeProp(getVerbose(), true, "Verbose", getString("VerboseName"), getString("VerboseHint"))); // NOI18N

        sheet.put(set);

        intNodeprop.getPropertyEditor().addPropertyChangeListener(typePropertyChangeListener = new TypePropertyChangeListener(makeCustomizer, outputNodeProp, toolNodeProp, optionsNodeProp));
        return sheet;
    }
    
    private class PackagerIntConfiguration extends IntConfiguration {
        PackagerIntConfiguration(IntConfiguration master, int def, String[] names, String[] options) {
            super(master, def, names, options);
        }
        
        @Override
        public void setValue(String s) {
            if (s != null) {
                String displayName = s;
                String name = PackagerManager.getDefault().getName(displayName);
                if (name != null) {
                    getType().setValue(name);
                }
                else {
                    assert false;
                }
            }
        }

        @Override
        public int getValue() {
            int i = PackagerManager.getDefault().getNameIndex(getType().getValue());
            return i;
        }
    
    }
    
    private class PackagerIntNodeProp extends IntNodeProp {
        public PackagerIntNodeProp(IntConfiguration intConfiguration, boolean canWrite, String unused, String name, String description) {
            super(intConfiguration, canWrite, unused, name, description);
        }
        
        
        @Override
        public Object getValue() {
            return Integer.valueOf(PackagerManager.getDefault().getNameIndex(getType().getValue()));
        }
    
        @Override
        public void setValue(Object v) {
            String displayName = (String)v;
            String name = PackagerManager.getDefault().getName(displayName);
            if (name != null) {
                getType().setValue(name);
            }
            else {
                assert false;
            }
        }
    }

    private class TypePropertyChangeListener implements PropertyChangeListener {

        private JPanel makeCustomizer;
        private OutputNodeProp outputNodeProp;
        private StringNodeProp toolNodeProp;
        private StringNodeProp optionsNodeProp;

        TypePropertyChangeListener(JPanel makeCustomizer, OutputNodeProp outputNodeProp, StringNodeProp toolNodeProp, StringNodeProp optionsNodeProp) {
            this.makeCustomizer = makeCustomizer;
            this.outputNodeProp = outputNodeProp;
            this.toolNodeProp = toolNodeProp;
            this.optionsNodeProp = optionsNodeProp;
        }

        @Override
        public void propertyChange(PropertyChangeEvent arg0) {
            toolNodeProp.setCanWrite(getToolDefault().length() > 0);
            optionsNodeProp.setCanWrite(getToolDefault().length() > 0);
            if (!output.getModified()) {
                outputNodeProp.setDefaultValue(getOutputDefault());
                output.reset();
            }
            if (!tool.getModified()) {
                toolNodeProp.setDefaultValue(getToolDefault());
                tool.reset();
            }
            if (!options.getModified()) {
                optionsNodeProp.setDefaultValue(getOptionsDefault());
                options.reset();
            }
            makeCustomizer.validate(); // this swill trigger repainting of the property
            makeCustomizer.repaint();
        }
    }

    public String getOutputValue() {
        if (getOutput().getModified()) {
            return getOutput().getValue();
        } else {
            return getOutputDefault();
        }
    }
    
    public String getTopDirValue() {
        if (getTopDir().getModified()) {
            return getTopDir().getValue();
        } else {
            PackagerDescriptor packager = PackagerManager.getDefault().getPackager(getType().getValue());
            if (packager != null) {
                return packager.getTopDir(makeConfiguration, this);
            }
            else {
                return ""; // NOI18N
            }
        }
    }
    
    public String getOutputName() {
        String outputName = CndPathUtilitities.getBaseName(getMakeConfiguration().getBaseDir());
        if (getMakeConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_APPLICATION) {
            outputName = outputName.toLowerCase();
        } else if (getMakeConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_DYNAMIC_LIB) {
            Platform platform = Platforms.getPlatform(getMakeConfiguration().getDevelopmentHost().getBuildPlatform());
            outputName = platform.getLibraryName(outputName);
        }
        outputName = createValidPackageName(outputName);
        return outputName;
    }
    
    private String getOutputDefault() {
        String outputPath = "${CND_DISTDIR}" + "/" + "${CND_CONF}" + "/" + "${CND_PLATFORM}" + "/package"; // NOI18N
//        String outputName = getOutputName();
        PackagerDescriptor packager = PackagerManager.getDefault().getPackager(getType().getValue());

        if (!packager.isOutputAFolder()) {
            outputPath += "/" + packager.getOutputFileName(makeConfiguration, this) + "." + packager.getOutputFileSuffix(); // NOI18N
        }

        return outputPath;
    }

    public String getToolValue() {
        if (getTool().getModified()) {
            return getTool().getValue();
        } else {
            return getToolDefault();
        }
    }

    private String getToolDefault() {
        PackagerDescriptor packager = PackagerManager.getDefault().getPackager(getType().getValue());
        return packager.getDefaultTool();
    }

    public String getOptionsValue() {
        if (getOptions().getModified()) {
            return getOptions().getValue();
        } else {
            return getOptionsDefault();
        }
    }

    private String getOptionsDefault() {
        PackagerDescriptor packager = PackagerManager.getDefault().getPackager(getType().getValue());
        return packager.getDefaultOptions();
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

    public String[] getDisplayNames() {
//        return TYPE_DISPLAY_NAMES;
        return PackagerManager.getDefault().getDisplayNames(); // FIXUP?
    }

    public String getDisplayName() {
//        return TYPE_DISPLAY_NAMES[getType().getValue()];
        return PackagerManager.getDefault().getDisplayName(getType().getValue()); // FIXUP?
    }
    
    public String getName() {
        return getType().getValue();
    }
    
    public String expandMacros(String s) {
        s = makeConfiguration.expandMacros(s);
        s = CndPathUtilitities.expandMacro(s, "${PACKAGE_TOP_DIR}", getTopDirValue().length() > 0 ? getTopDirValue() + "/" : ""); // NOI18N
        return s;
    }
    
    private String createValidPackageName(String name) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '_') {
                continue;
            }
            else if (c == ' ') {
                continue;
            }
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }
    
    
    public PackagerInfoElement findInfoElement(String name) {
        List<PackagerInfoElement> infoList = getInfo().getValue();
        for (PackagerInfoElement elem : infoList) {
            if (elem.getName().equals(name)) {
                return elem;
            }
        }
        return null;
    }
    
    public String findInfoValueName(String name) {
        List<PackagerInfoElement> infoList = getInfo().getValue();
        for (PackagerInfoElement elem : infoList) {
            if (elem.getName().equals(name)) {
                return elem.getValue();
            }
        }
        return null;
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(PackagingConfiguration.class, s);
    }
}
