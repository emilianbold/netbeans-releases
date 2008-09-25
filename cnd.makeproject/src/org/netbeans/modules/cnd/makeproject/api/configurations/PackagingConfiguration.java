/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platform;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platforms;
import org.netbeans.modules.cnd.makeproject.configurations.ui.BooleanNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.IntNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.PackagingNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.StringNodeProp;
import org.netbeans.modules.cnd.makeproject.packaging.FileElement;
import org.netbeans.modules.cnd.makeproject.packaging.FileElement.FileType;
import org.netbeans.modules.cnd.makeproject.packaging.InfoElement;
import org.netbeans.modules.cnd.makeproject.packaging.PackageDescriptor;
import org.netbeans.modules.cnd.makeproject.ui.customizer.MakeCustomizer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

public class PackagingConfiguration {

    private MakeConfiguration makeConfiguration;    // Types
    private static String[] TYPE_DISPLAY_NAMES = {
        getString("Tar"),
        getString("Zip"),
        getString("SCR4Package"),
        getString("RPM"),
        getString("Debian")
    };
    public static final int TYPE_TAR = 0;
    public static final int TYPE_ZIP = 1;
    public static final int TYPE_SVR4_PACKAGE = 2;
    public static final int TYPE_RPM_PACKAGE = 3;
    public static final int TYPE_DEBIAN_PACKAGE = 4;
    private IntConfiguration type;
    private BooleanConfiguration verbose;
    private VectorConfiguration svr4Header;
    private VectorConfiguration rpmHeader;
    private VectorConfiguration debianHeader;
    private VectorConfiguration files;
    private StringConfiguration output;
    private StringConfiguration tool;
    private StringConfiguration options;
    private StringConfiguration topDir;
    // Constructors
    public PackagingConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
        type = new IntConfiguration(null, TYPE_TAR, TYPE_DISPLAY_NAMES, null);
        verbose = new BooleanConfiguration(null, true);
        svr4Header = new VectorConfiguration(null); // NOI18N
        rpmHeader = new VectorConfiguration(null); // NOI18N
        debianHeader = new VectorConfiguration(null); // NOI18N
        files = new VectorConfiguration(null); // NOI18N
        output = new StringConfiguration(null, ""); // NOI18N
        tool = new StringConfiguration(null, ""); // NOI18N
        options = new StringConfiguration(null, ""); // NOI18N
        topDir = new StringConfiguration(null, null); // NOI18N
        
        setDefaultValues();
    }
    
    public void setDefaultValues() {
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
            if (makeConfiguration.getPlatform().getValue() == Platform.PLATFORM_WINDOWS) {
                suffix = ".exe"; // NOI18N
            }
        } else if (makeConfiguration.isLibraryConfiguration()) {
            perm = MakeOptions.getInstance().getDefFilePerm();
            packageDir = "${PACKAGE_TOP_DIR}lib"; // NOI18N
        }
        else {
            assert false;
        }
        FileElement elem = new FileElement(
                FileType.FILE,
                "${OUTPUT_PATH}" + suffix, // NOI18N
                packageDir + "/${OUTPUT_BASENAME}" + suffix, // NOI18N
                perm,
                MakeOptions.getInstance().getDefOwner(),
                MakeOptions.getInstance().getDefGroup());
        elem.setDefaultValue(true);
        files.add(elem);
        
        String defArch;
        if (makeConfiguration.getPlatform().getValue() == Platform.PLATFORM_SOLARIS_INTEL) {
            defArch = "i386"; // NOI18N
        }
        else if (makeConfiguration.getPlatform().getValue() == Platform.PLATFORM_SOLARIS_SPARC) {
            defArch = "sparc"; // NOI18N
        }
        else {
            // Anything else ?
            defArch = "i386"; // NOI18N
        }
        
        // Solaris SVR4
        List<InfoElement> headerList = getSvr4Header().getValue();
        headerList.add(new InfoElement("PKG", getOutputName(), true, true)); // NOI18N
        headerList.add(new InfoElement("NAME", "Package description ...", true, true)); // NOI18N
        headerList.add(new InfoElement("ARCH", defArch, true, true)); // NOI18N
        headerList.add(new InfoElement("CATEGORY", "application", true, true)); // NOI18N
        headerList.add(new InfoElement("VERSION", "1.0", true, true)); // NOI18N
        headerList.add(new InfoElement("BASEDIR", "/opt", false, true)); // NOI18N
        headerList.add(new InfoElement("PSTAMP", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()), false, true)); // NOI18N
        headerList.add(new InfoElement("CLASSES", "none", false, true)); // NOI18N
        
        // RPM
        List<InfoElement> rpmHeaderList = getRpmHeader().getValue();
        rpmHeaderList.add(new InfoElement("Summary", "Sumary...", true, true)); // NOI18N
        rpmHeaderList.add(new InfoElement("Name", getOutputName(), true, true)); // NOI18N
        rpmHeaderList.add(new InfoElement("Version", "1.0", true, true)); // NOI18N
        rpmHeaderList.add(new InfoElement("Release", "1", true, true)); // NOI18N
        rpmHeaderList.add(new InfoElement("Group", "Applications/System", true, true)); // NOI18N
        rpmHeaderList.add(new InfoElement("License", "BSD-type", true, true)); // NOI18N
        rpmHeaderList.add(new InfoElement("%description", "Description...", true, true)); // NOI18N
        
        // Debian
        List<InfoElement> debianHeaderList = getDebianHeader().getValue();
        debianHeaderList.add(new InfoElement("Package", getOutputName(), true, true)); // NOI18N
        debianHeaderList.add(new InfoElement("Version", "1.0", true, true)); // NOI18N
        debianHeaderList.add(new InfoElement("Architecture", defArch, false, true)); // NOI18N
        debianHeaderList.add(new InfoElement("Maintainer", System.getProperty("user.name"), false, true)); // NOI18N
        debianHeaderList.add(new InfoElement("Description", "...", false, true)); // NOI18N
    }
    
    public boolean isModified() {
        if (getType().getModified()) {
            return true;
        }
        if (files.getValue().size() != 1) {
            return true;
        }
        for (FileElement elem : (List<FileElement>)files.getValue()) {
            if (!elem.isDefaultValue()) {
                return true;
            }
        }
        if (getType().getValue() == TYPE_SVR4_PACKAGE) {
            if (svr4Header.getValue().size() != 8) {
                return true;
            }
            for (InfoElement elem : (List<InfoElement>)svr4Header.getValue()) {
                if (!elem.isDefaultValue()) {
                    return true;
                }
            }
        }
        if (getType().getValue() == TYPE_RPM_PACKAGE) {
            if (rpmHeader.getValue().size() != 6) {
                return true;
            }
            for (InfoElement elem : (List<InfoElement>)rpmHeader.getValue()) {
                if (!elem.isDefaultValue()) {
                    return true;
                }
            }
        }
        if (getType().getValue() == TYPE_DEBIAN_PACKAGE) {
            if (debianHeader.getValue().size() != 5) {
                return true;
            }
            for (InfoElement elem : (List<InfoElement>)debianHeader.getValue()) {
                if (!elem.isDefaultValue()) {
                    return true;
                }
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

    public IntConfiguration getType() {
        return type;
    }

    public void setType(IntConfiguration type) {
        this.type = type;
    }

    public BooleanConfiguration getVerbose() {
        return verbose;
    }

    public void setVerbose(BooleanConfiguration verbose) {
        this.verbose = verbose;
    }

    public VectorConfiguration getSvr4Header() {
        return svr4Header;
    }

    public void setSvr4Header(VectorConfiguration svr4Header) {
        this.svr4Header = svr4Header;
    }
    
    public VectorConfiguration getRpmHeader() {
        return rpmHeader;
    }

    public void setRpmHeader(VectorConfiguration rpmHeader) {
        this.rpmHeader = rpmHeader;
    }
    
    public VectorConfiguration getDebianHeader() {
        return debianHeader;
    }

    public void setDebianHeader(VectorConfiguration debianHeader) {
        this.debianHeader = debianHeader;
    }

    public VectorConfiguration getFiles() {
        return files;
    }

    public void setFiles(VectorConfiguration files) {
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
        setMakeConfiguration(conf.getMakeConfiguration());
        getType().assign(conf.getType());
        getVerbose().assign(conf.getVerbose());
        getSvr4Header().assign(conf.getSvr4Header());
        getRpmHeader().assign(conf.getRpmHeader());
        getDebianHeader().assign(conf.getDebianHeader());
        getFiles().assign(conf.getFiles());
        getOutput().assign(conf.getOutput());
        getTool().assign(conf.getTool());
        getOptions().assign(conf.getOptions());
        getTopDir().assign(conf.getTopDir());
    }

    @Override
    public Object clone() {
        PackagingConfiguration clone = new PackagingConfiguration(getMakeConfiguration());
        clone.setType((IntConfiguration) getType().clone());
        clone.setVerbose((BooleanConfiguration) getVerbose().clone());
        clone.setSvr4Header((VectorConfiguration) getSvr4Header().clone());
        clone.setRpmHeader((VectorConfiguration) getRpmHeader().clone());
        clone.setDebianHeader((VectorConfiguration) getDebianHeader().clone());
        clone.setFiles((VectorConfiguration) getFiles().clone());
        clone.setOutput((StringConfiguration) getOutput().clone());
        clone.setTool((StringConfiguration) getTool().clone());
        clone.setOptions((StringConfiguration) getOptions().clone());
        clone.setTopDir((StringConfiguration) getTopDir().clone());
        return clone;
    }
    TypePropertyChangeListener typePropertyChangeListener;
    // Sheet
    public Sheet getGeneralSheet(MakeCustomizer makeCustomizer) {
        IntNodeProp intNodeprop;
        OutputNodeProp outputNodeProp;
        StringNodeProp toolNodeProp;
        StringNodeProp optionsNodeProp;

        Sheet sheet = new Sheet();
        Sheet.Set set = new Sheet.Set();
        set.setName("General"); // NOI18N
        set.setDisplayName(getString("GeneralTxt"));
        set.setShortDescription(getString("GeneralHint"));

        set.put(intNodeprop = new IntNodeProp(getType(), true, "PackageType", getString("PackageTypeName"), getString("PackageTypeHint"))); // NOI18N
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

    class TypePropertyChangeListener implements PropertyChangeListener {

        private MakeCustomizer makeCustomizer;
        private OutputNodeProp outputNodeProp;
        private StringNodeProp toolNodeProp;
        private StringNodeProp optionsNodeProp;

        TypePropertyChangeListener(MakeCustomizer makeCustomizer, OutputNodeProp outputNodeProp, StringNodeProp toolNodeProp, StringNodeProp optionsNodeProp) {
            this.makeCustomizer = makeCustomizer;
            this.outputNodeProp = outputNodeProp;
            this.toolNodeProp = toolNodeProp;
            this.optionsNodeProp = optionsNodeProp;
        }

        public void propertyChange(PropertyChangeEvent arg0) {
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
            String val = null;
            
            if (getType().getValue() == TYPE_RPM_PACKAGE) {
                val = "/usr"; // NOI18N
            }
            else if (getType().getValue() == TYPE_DEBIAN_PACKAGE) {
                val = "/usr"; // NOI18N
            }
            else if (getType().getValue() == TYPE_SVR4_PACKAGE) {
                val = findInfoValueName("PKG"); // NOI18N
            }
            else if (getType().getValue() == TYPE_TAR) {
                val = IpeUtils.getBaseName(getOutputValue());
            }
            else if (getType().getValue() == TYPE_ZIP) {
                val = IpeUtils.getBaseName(getOutputValue());
            }
            else {
                assert false;
            }
            
            int i = val.lastIndexOf("."); // NOI18N
            if (i > 0) {
                val = val.substring(0, i);
            }
            return val;
        }
    }
    
    private String getOutputName() {
        String outputName = IpeUtils.getBaseName(getMakeConfiguration().getBaseDir());
        if (getMakeConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_APPLICATION) {
            outputName = outputName.toLowerCase();
        } else if (getMakeConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_DYNAMIC_LIB) {
            Platform platform = Platforms.getPlatform(getMakeConfiguration().getPlatform().getValue());
            outputName = platform.getLibraryName(outputName);
        }
        outputName = createValidPackageName(outputName);
        return outputName;
    }
    
    private String getOutputDefault() {
        String outputPath = MakeConfiguration.DIST_FOLDER + "/" + getMakeConfiguration().getName() + "/" + "${PLATFORM}" + "/package"; // NOI18N 
        String outputName = getOutputName();
        
        if (getType().getValue() == PackagingConfiguration.TYPE_SVR4_PACKAGE) {
            // nothing
        } else if (getType().getValue() == PackagingConfiguration.TYPE_RPM_PACKAGE) {
            // nothing
        } else if (getType().getValue() == PackagingConfiguration.TYPE_DEBIAN_PACKAGE) {
            outputPath += "/" + outputName + ".deb"; // NOI18N
        } else if (getType().getValue() == PackagingConfiguration.TYPE_TAR) {
            outputPath += "/" + outputName + ".tar"; // NOI18N
        } else if (getType().getValue() == PackagingConfiguration.TYPE_ZIP) {
            outputPath += "/" + outputName + ".zip"; // NOI18N
        } else {
            assert false;
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
        String tool = null;
        if (getType().getValue() == PackagingConfiguration.TYPE_SVR4_PACKAGE) {
            tool = "pkgmk"; // NOI18N // FIXUP 
        } else if (getType().getValue() == PackagingConfiguration.TYPE_RPM_PACKAGE) {
            tool = "rpmbuild"; // NOI18N
        } else if (getType().getValue() == PackagingConfiguration.TYPE_DEBIAN_PACKAGE) {
            tool = "dpkg-deb"; // NOI18N
        } else if (getType().getValue() == PackagingConfiguration.TYPE_TAR) {
            tool = "tar"; // NOI18N
        } else if (getType().getValue() == PackagingConfiguration.TYPE_ZIP) {
            tool = "zip"; // NOI18N
        } else {
            assert false;
        }

        return tool;
    }

    public String getOptionsValue() {
        if (getOptions().getModified()) {
            return getOptions().getValue();
        } else {
            return getOptionsDefault();
        }
    }

    private String getOptionsDefault() {
        String option = null;
        if (getType().getValue() == PackagingConfiguration.TYPE_SVR4_PACKAGE) {
            option = ""; // NOI18N // FIXUP 
        } else if (getType().getValue() == PackagingConfiguration.TYPE_RPM_PACKAGE) {
            option = ""; // NOI18N
        } else if (getType().getValue() == PackagingConfiguration.TYPE_DEBIAN_PACKAGE) {
            option = ""; // NOI18N
        } else if (getType().getValue() == PackagingConfiguration.TYPE_TAR) {
            option = "-v"; // NOI18N
        } else if (getType().getValue() == PackagingConfiguration.TYPE_ZIP) {
            option = ""; // NOI18N
        } else {
            assert false;
        }

        return option;
    }

    private class OutputNodeProp extends StringNodeProp {

        public OutputNodeProp(StringConfiguration stringConfiguration, String def, String txt1, String txt2, String txt3) {
            super(stringConfiguration, def, txt1, txt2, txt3);
        }

        @Override
        public void setValue(Object v) {
            if (IpeUtils.hasMakeSpecialCharacters((String) v)) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(getString("SPECIAL_CHARATERS_ERROR"), NotifyDescriptor.ERROR_MESSAGE));
                return;
            }
            super.setValue(v);
        }
    }

    public String[] getDisplayNames() {
        return TYPE_DISPLAY_NAMES;
    }

    public String getDisplayName() {
        return TYPE_DISPLAY_NAMES[getType().getValue()];
    }
    
    public String getName() {
        return PackageDescriptor.NAMES[getType().getValue()];
    }
    
    public String expandMacros(String s) {
        s = makeConfiguration.expandMacros(s);
        s = IpeUtils.expandMacro(s, "${PACKAGE_TOP_DIR}", getTopDirValue().length() > 0 ? getTopDirValue() + "/" : ""); // NOI18N
        return s;
    }
    
    private String createValidPackageName(String name) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '_') 
                continue;
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }
    
    
    public InfoElement findInfoElement(String name) {
        List<InfoElement> infoList = getSvr4Header().getValue();
        for (InfoElement elem : infoList) {
            if (elem.getName().equals(name)) {
                return elem;
            }
        }
        return null;
    }
    
    public String findInfoValueName(String name) {
        List<InfoElement> infoList = getSvr4Header().getValue();
        for (InfoElement elem : infoList) {
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
