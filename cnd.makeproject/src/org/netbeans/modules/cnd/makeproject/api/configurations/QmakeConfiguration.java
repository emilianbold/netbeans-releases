/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.util.StringTokenizer;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.makeproject.platform.Platforms;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.BooleanNodeProp;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.IntNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.StringListNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.StringNodeProp;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author Alexey Vladykin
 */
public class QmakeConfiguration implements Cloneable {

    public static final int DEBUG_MODE = 0;
    public static final int RELEASE_MODE = 1;
    private final String[] BUILD_MODE_NAMES = {"Debug", "Release"}; // NOI18N
    private final String[] BUILD_MODE_OPTIONS = {"debug", "release"}; // NOI18N
    private static final String CORE = "core"; // NOI18N
    private static final String GUI = "gui"; // NOI18N
    private static final String NETWORK = "network"; // NOI18N
    private static final String OPENGL = "opengl"; // NOI18N
    private static final String PHONON = "phonon"; // NOI18N
    private static final String QT3SUPPORT = "qt3support"; // NOI18N
    private static final String SQL = "sql"; // NOI18N
    private static final String SVG = "svg"; // NOI18N
    private static final String WEBKIT = "webkit"; // NOI18N
    private static final String XML = "xml"; // NOI18N
    private final MakeConfiguration makeConfiguration;

    // general
    private StringConfiguration destdir;
    private StringConfiguration target;
    private StringConfiguration version;
    private IntConfiguration buildMode;

    // modules
    private BooleanConfiguration coreEnabled;
    private BooleanConfiguration guiEnabled;
    private BooleanConfiguration networkEnabled;
    private BooleanConfiguration openglEnabled;
    private BooleanConfiguration phononEnabled;
    private BooleanConfiguration qt3SupportEnabled;
    private BooleanConfiguration sqlEnabled;
    private BooleanConfiguration svgEnabled;
    private BooleanConfiguration webkitEnabled;
    private BooleanConfiguration xmlEnabled;

    // intermediate files
    private StringConfiguration mocDir;
    private StringConfiguration rccDir;
    private StringConfiguration uiDir;

    // expert
    private StringConfiguration qmakespec;
    private VectorConfiguration<String> customDefs;

    public QmakeConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
        destdir = new StringConfiguration(null, ""); // NOI18N
        target = new StringConfiguration(null, ""); // NOI18N
        version = new StringConfiguration(null, "1.0.0"); // NOI18N
        buildMode = new IntConfiguration(null, 0, BUILD_MODE_NAMES, BUILD_MODE_OPTIONS);
        coreEnabled = new BooleanConfiguration(null, true);
        guiEnabled = new BooleanConfiguration(null, true);
        networkEnabled = new BooleanConfiguration(null, false);
        openglEnabled = new BooleanConfiguration(null, false);
        phononEnabled = new BooleanConfiguration(null, false);
        qt3SupportEnabled = new BooleanConfiguration(null, false);
        sqlEnabled = new BooleanConfiguration(null, false);
        svgEnabled = new BooleanConfiguration(null, false);
        xmlEnabled = new BooleanConfiguration(null, false);
        webkitEnabled = new BooleanConfiguration(null, false);
        mocDir = new StringConfiguration(null, ""); // NOI18N
        rccDir = new StringConfiguration(null, ""); // NOI18N
        uiDir = new StringConfiguration(null, ""); // NOI18N
        customDefs = new VectorConfiguration<String>(null);
        qmakespec = new StringConfiguration(null, ""); // NOI18N
    }

    public Sheet getGeneralSheet() {
        Sheet sheet = new Sheet();

        Sheet.Set basic = new Sheet.Set();
        basic.setName("QtGeneral"); // NOI18N
        basic.setDisplayName(getString("QtGeneralTxt")); // NOI18N
        basic.setShortDescription(getString("QtGeneralHint")); // NOI18N
        basic.put(new StringNodeProp(destdir, getDestdirDefault(), "QtDestdir", getString("QtDestdirTxt"), getString("QtDestdirHint"))); // NOI18N
        basic.put(new StringNodeProp(target, getTargetDefault(), "QtTarget", getString("QtTargetTxt"), getString("QtTargetHint"))); // NOI18N
        basic.put(new StringNodeProp(version, "QtVersion", getString("QtVersionTxt"), getString("QtVersionHint"))); // NOI18N
        basic.put(new IntNodeProp(buildMode, true, "QtBuildMode", getString("QtBuildModeTxt"), getString("QtBuildModeHint"))); // NOI18N
        sheet.put(basic);

        Sheet.Set modules = new Sheet.Set();
        modules.setName("QtModules"); // NOI18N
        modules.setDisplayName(getString("QtModulesTxt")); // NOI18N
        modules.setShortDescription(getString("QtModulesHint")); // NOI18N
        modules.put(new BooleanNodeProp(coreEnabled, true, "QtCore", getString("QtCoreTxt"), getString("QtCoreHint"))); // NOI18N
        modules.put(new BooleanNodeProp(guiEnabled, true, "QtGui", getString("QtGuiTxt"), getString("QtGuiHint"))); // NOI18N
        modules.put(new BooleanNodeProp(networkEnabled, true, "QtNetwork", getString("QtNetworkTxt"), getString("QtNetworkHint"))); // NOI18N
        modules.put(new BooleanNodeProp(openglEnabled, true, "QtOpengl", getString("QtOpenglTxt"), getString("QtOpenglHint"))); // NOI18N
        modules.put(new BooleanNodeProp(phononEnabled, true, "QtPhonon", getString("QtPhononTxt"), getString("QtPhononHint"))); // NOI18N
        modules.put(new BooleanNodeProp(qt3SupportEnabled, true, "Qt3Support", getString("Qt3SupportTxt"), getString("Qt3SupportHint"))); // NOI18N
        modules.put(new BooleanNodeProp(sqlEnabled, true, "QtSql", getString("QtSqlTxt"), getString("QtSqlHint"))); // NOI18N
        modules.put(new BooleanNodeProp(svgEnabled, true, "QtSvg", getString("QtSvgTxt"), getString("QtSvgHint"))); // NOI18N
        modules.put(new BooleanNodeProp(xmlEnabled, true, "QtXml", getString("QtXmlTxt"), getString("QtXmlHint"))); // NOI18N
        modules.put(new BooleanNodeProp(webkitEnabled, true, "QtWebkit", getString("QtWebkitTxt"), getString("QtWebkitHint"))); // NOI18N
        sheet.put(modules);

        Sheet.Set generatedFiles = new Sheet.Set();
        generatedFiles.setName("QtIntermediateFiles"); // NOI18N
        generatedFiles.setDisplayName(getString("QtIntermediateFilesTxt")); // NOI18N
        generatedFiles.setShortDescription(getString("QtIntermediateFilesHint")); // NOI18N
        generatedFiles.put(new StringNodeProp(mocDir, "QtMocDir", getString("QtMocDirTxt"), getString("QtMocDirHint"))); // NOI18N
        generatedFiles.put(new StringNodeProp(rccDir, "QtRccDir", getString("QtRccDirTxt"), getString("QtRccDirHint"))); // NOI18N
        generatedFiles.put(new StringNodeProp(uiDir, "QtUiDir", getString("QtUiDirTxt"), getString("QtUiDirHint"))); // NOI18N
        sheet.put(generatedFiles);

        Sheet.Set expert = new Sheet.Set();
        expert.setName("QtExpert"); // NOI18N
        expert.setDisplayName(getString("QtExpertTxt")); // NOI18N
        expert.setShortDescription(getString("QtExpertHint")); // NOI18N
        expert.put(new StringNodeProp(qmakespec, "QtQmakeSpec", getString("QtQmakeSpecTxt"), getString("QtQmakeSpecHint"))); // NOI18N
        expert.put(new StringListNodeProp(customDefs, null, new String[]{"QtCustomDefs", getString("QtCustomDefsTxt"), getString("QtCustomDefsHint"), getString("QtCustomDefsLbl")}, false, HelpCtx.DEFAULT_HELP)); // NOI18N
        sheet.put(expert);

        return sheet;
    }

    public String getDestdirValue() {
        if (destdir.getModified()) {
            return destdir.getValue();
        } else {
            return getDestdirDefault();
        }
    }

    private String getDestdirDefault() {
        return "${CND_DISTDIR}" + "/" + "${CND_CONF}" + "/" + "${CND_PLATFORM}"; // NOI18N
    }

    public StringConfiguration getDestdir() {
        return destdir;
    }

    private void setDestdir(StringConfiguration destdir) {
        this.destdir = destdir;
    }

    public String getTargetValue() {
        if (target.getModified()) {
            return target.getValue();
        } else {
            return getTargetDefault();
        }
    }

    private String getTargetDefault() {
        return ConfigurationSupport.makeNameLegal(CndPathUtilitities.getBaseName(makeConfiguration.getBaseDir()));
    }

    public StringConfiguration getTarget() {
        return target;
    }

    private void setTarget(StringConfiguration target) {
        this.target = target;
    }

    public StringConfiguration getVersion() {
        return version;
    }

    private void setVersion(StringConfiguration version) {
        this.version = version;
    }

    public String getOutputValue() {
        String dir = getDestdirValue();
        String file = getTargetValue();
        switch (makeConfiguration.getConfigurationType().getValue()) {
            case MakeConfiguration.TYPE_QT_DYNAMIC_LIB:
                file = Platforms.getPlatform(makeConfiguration.getDevelopmentHost().getBuildPlatform()).getQtLibraryName(file, getVersion().getValue());
                break;
            case MakeConfiguration.TYPE_QT_STATIC_LIB:
                file = "lib" + file + ".a"; // NOI18N
                break;
        }
        return 0 < dir.length() ? dir + "/" + file : file; // NOI18N
    }

    public IntConfiguration getBuildMode() {
        return buildMode;
    }

    private void setBuildMode(IntConfiguration buildMode) {
        this.buildMode = buildMode;
    }

    public String getEnabledModules() {
        StringBuilder buf = new StringBuilder();
        if (isCoreEnabled().getValue()) {
            append(buf, CORE);
        }
        if (isGuiEnabled().getValue()) {
            append(buf, GUI);
        }
        if (isNetworkEnabled().getValue()) {
            append(buf, NETWORK);
        }
        if (isOpenglEnabled().getValue()) {
            append(buf, OPENGL);
        }
        if (isPhononEnabled().getValue()) {
            append(buf, PHONON);
        }
        if (isQt3SupportEnabled().getValue()) {
            append(buf, QT3SUPPORT);
        }
        if (isSqlEnabled().getValue()) {
            append(buf, SQL);
        }
        if (isSvgEnabled().getValue()) {
            append(buf, SVG);
        }
        if (isXmlEnabled().getValue()) {
            append(buf, XML);
        }
        if (isWebkitEnabled().getValue()) {
            append(buf, WEBKIT);
        }
        return buf.toString();
    }

    public void setEnabledModules(String modules) {
        isCoreEnabled().setValue(false);
        isGuiEnabled().setValue(false);
        isNetworkEnabled().setValue(false);
        isOpenglEnabled().setValue(false);
        isPhononEnabled().setValue(false);
        isQt3SupportEnabled().setValue(false);
        isSqlEnabled().setValue(false);
        isSvgEnabled().setValue(false);
        isXmlEnabled().setValue(false);
        isWebkitEnabled().setValue(false);
        StringTokenizer st = new StringTokenizer(modules);
        while (st.hasMoreTokens()) {
            String t = st.nextToken();
            if (t.equals(CORE)) {
                isCoreEnabled().setValue(true);
            } else if (t.equals(GUI)) {
                isGuiEnabled().setValue(true);
            } else if (t.equals(NETWORK)) {
                isNetworkEnabled().setValue(true);
            } else if (t.equals(OPENGL)) {
                isOpenglEnabled().setValue(true);
            } else if (t.equals(PHONON)) {
                isPhononEnabled().setValue(true);
            } else if (t.equals(QT3SUPPORT)) {
                isQt3SupportEnabled().setValue(true);
            } else if (t.equals(SQL)) {
                isSqlEnabled().setValue(true);
            } else if (t.equals(SVG)) {
                isSvgEnabled().setValue(true);
            } else if (t.equals(XML)) {
                isXmlEnabled().setValue(true);
            } else if (t.equals(WEBKIT)) {
                isWebkitEnabled().setValue(true);
            } else {
                // unknown module
            }
        }
    }

    public BooleanConfiguration isCoreEnabled() {
        return coreEnabled;
    }

    private void setCoreEnabled(BooleanConfiguration val) {
        coreEnabled = val;
    }

    public BooleanConfiguration isGuiEnabled() {
        return guiEnabled;
    }

    private void setGuiEnabled(BooleanConfiguration val) {
        guiEnabled = val;
    }

    public BooleanConfiguration isNetworkEnabled() {
        return networkEnabled;
    }

    private void setNetworkEnabled(BooleanConfiguration val) {
        networkEnabled = val;
    }

    public BooleanConfiguration isOpenglEnabled() {
        return openglEnabled;
    }

    private void setOpenglEnabled(BooleanConfiguration val) {
        openglEnabled = val;
    }

    public BooleanConfiguration isPhononEnabled() {
        return phononEnabled;
    }

    private void setPhononEnabled(BooleanConfiguration val) {
        this.phononEnabled = val;
    }

    public BooleanConfiguration isQt3SupportEnabled() {
        return qt3SupportEnabled;
    }

    private void setQt3SupportEnabled(BooleanConfiguration val) {
        this.qt3SupportEnabled = val;
    }

    public BooleanConfiguration isSqlEnabled() {
        return sqlEnabled;
    }

    private void setSqlEnabled(BooleanConfiguration val) {
        sqlEnabled = val;
    }

    public BooleanConfiguration isSvgEnabled() {
        return svgEnabled;
    }

    private void setSvgEnabled(BooleanConfiguration val) {
        svgEnabled = val;
    }

    public BooleanConfiguration isXmlEnabled() {
        return xmlEnabled;
    }

    private void setXmlEnabled(BooleanConfiguration val) {
        xmlEnabled = val;
    }

    public BooleanConfiguration isWebkitEnabled() {
        return webkitEnabled;
    }

    private void setWebkitEnabled(BooleanConfiguration val) {
        this.webkitEnabled = val;
    }

    public StringConfiguration getMocDir() {
        return mocDir;
    }

    private void setMocDir(StringConfiguration mocDir) {
        this.mocDir = mocDir;
    }

    public StringConfiguration getRccDir() {
        return rccDir;
    }

    private void setRccDir(StringConfiguration rccDir) {
        this.rccDir = rccDir;
    }

    public StringConfiguration getUiDir() {
        return uiDir;
    }

    private void setUiDir(StringConfiguration uicDir) {
        this.uiDir = uicDir;
    }

    public VectorConfiguration<String> getCustomDefs() {
        return customDefs;
    }

    private void setCustomDefs(VectorConfiguration<String> customDefs) {
        this.customDefs = customDefs;
    }

    public StringConfiguration getQmakeSpec() {
        return qmakespec;
    }

    private void setQmakespec(StringConfiguration qmakespec) {
        this.qmakespec = qmakespec;
    }

    public void assign(QmakeConfiguration other) {
        getDestdir().assign(other.getDestdir());
        getTarget().assign(other.getTarget());
        getVersion().assign(other.getVersion());
        getBuildMode().assign(other.getBuildMode());
        isCoreEnabled().assign(other.isCoreEnabled());
        isGuiEnabled().assign(other.isGuiEnabled());
        isNetworkEnabled().assign(other.isNetworkEnabled());
        isOpenglEnabled().assign(other.isOpenglEnabled());
        isPhononEnabled().assign(other.isPhononEnabled());
        isQt3SupportEnabled().assign(other.isQt3SupportEnabled());
        isSqlEnabled().assign(other.isSqlEnabled());
        isSvgEnabled().assign(other.isSvgEnabled());
        isXmlEnabled().assign(other.isXmlEnabled());
        isWebkitEnabled().assign(other.isWebkitEnabled());
        getMocDir().assign(other.getMocDir());
        getRccDir().assign(other.getRccDir());
        getUiDir().assign(other.getUiDir());
        getCustomDefs().assign(other.getCustomDefs());
        getQmakeSpec().assign(other.getQmakeSpec());
    }

    @Override
    public QmakeConfiguration clone() {
        try {
            QmakeConfiguration clone = (QmakeConfiguration) super.clone();
            clone.setDestdir(getDestdir().clone());
            clone.setTarget(getTarget().clone());
            clone.setVersion(getVersion().clone());
            clone.setBuildMode(getBuildMode().clone());
            clone.setCoreEnabled(isCoreEnabled().clone());
            clone.setGuiEnabled(isGuiEnabled().clone());
            clone.setNetworkEnabled(isNetworkEnabled().clone());
            clone.setOpenglEnabled(isOpenglEnabled().clone());
            clone.setPhononEnabled(isPhononEnabled().clone());
            clone.setQt3SupportEnabled(isQt3SupportEnabled().clone());
            clone.setSqlEnabled(isSqlEnabled().clone());
            clone.setSvgEnabled(isSvgEnabled().clone());
            clone.setXmlEnabled(isXmlEnabled().clone());
            clone.setWebkitEnabled(isWebkitEnabled().clone());
            clone.setMocDir(getMocDir().clone());
            clone.setRccDir(getRccDir().clone());
            clone.setUiDir(getUiDir().clone());
            clone.setCustomDefs(getCustomDefs().clone());
            clone.setQmakespec(getQmakeSpec().clone());
            return clone;
        } catch (CloneNotSupportedException ex) {
            // should not happen while this class implements Cloneable
            ex.printStackTrace();
            return null;
        }
    }

    private static String getString(String s) {
        return NbBundle.getMessage(QmakeConfiguration.class, s);
    }

    private static void append(StringBuilder buf, String val) {
        if (0 < buf.length() && buf.charAt(buf.length() - 1) != ' ') { // NOI18N
            buf.append(' '); // NOI18N
        }
        buf.append(val);
    }
}
