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
import org.netbeans.modules.cnd.makeproject.configurations.ui.BooleanNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.IntNodeProp;
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
    private final String[] BUILD_MODE_NAMES = { "Debug", "Release" }; // NOI18N
    private final String[] BUILD_NODE_OPTIONS = { "debug", "release" }; // NOI18N

    private IntConfiguration buildMode;
    private BooleanConfiguration coreEnabled;
    private BooleanConfiguration guiEnabled;
    private BooleanConfiguration networkEnabled;
    private BooleanConfiguration openglEnabled;
    private BooleanConfiguration sqlEnabled;
    private BooleanConfiguration svgEnabled;
    private BooleanConfiguration xmlEnabled;
    private StringConfiguration mocDir;
    private StringConfiguration uiDir;
    private VectorConfiguration<String> customDefs;

    public QmakeConfiguration() {
        buildMode = new IntConfiguration(null, 0, BUILD_MODE_NAMES, BUILD_NODE_OPTIONS);
        coreEnabled = new BooleanConfiguration(null, true);
        guiEnabled = new BooleanConfiguration(null, true);
        networkEnabled = new BooleanConfiguration(null, false);
        openglEnabled = new BooleanConfiguration(null, false);
        sqlEnabled = new BooleanConfiguration(null, false);
        svgEnabled = new BooleanConfiguration(null, false);
        xmlEnabled = new BooleanConfiguration(null, false);
        mocDir = new StringConfiguration(null, ""); // NOI18N
        uiDir = new StringConfiguration(null, ""); // NOI18N
        customDefs = new VectorConfiguration<String>(null);
    }

    public Sheet getGeneralSheet() {
        Sheet sheet = new Sheet();

        Sheet.Set basic = new Sheet.Set();
        basic.setName("QtGeneral"); // NOI18N
        basic.setDisplayName(getString("QtGeneralTxt")); // NOI18N
        basic.setShortDescription(getString("QtGeneralHint")); // NOI18N
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
        modules.put(new BooleanNodeProp(sqlEnabled, true, "QtSql", getString("QtSqlTxt"), getString("QtSqlHint"))); // NOI18N
        modules.put(new BooleanNodeProp(svgEnabled, true, "QtSvg", getString("QtSvgTxt"), getString("QtSvgHint"))); // NOI18N
        modules.put(new BooleanNodeProp(xmlEnabled, true, "QtXml", getString("QtXmlTxt"), getString("QtXmlHint"))); // NOI18N
        sheet.put(modules);

        Sheet.Set generatedFiles = new Sheet.Set();
        generatedFiles.setName("QtGeneratedFiles"); // NOI18N
        generatedFiles.setDisplayName(getString("QtGeneratedFilesTxt")); // NOI18N
        generatedFiles.setShortDescription(getString("QtGeneratedFilesHint")); // NOI18N
        generatedFiles.put(new StringNodeProp(mocDir, "QtMocDir", getString("QtMocDirTxt"), getString("QtMocDirHint"))); // NOI18N
        generatedFiles.put(new StringNodeProp(uiDir, "QtUicDir", getString("QtUiDirTxt"), getString("QtUiDirHint"))); // NOI18N
        sheet.put(generatedFiles);

        Sheet.Set expert = new Sheet.Set();
        expert.setName("QtExpert"); // NOI18N
        expert.setDisplayName(getString("QtExpertTxt")); // NOI18N
        expert.setShortDescription(getString("QtExpertHint")); // NOI18N
        expert.put(new StringListNodeProp(customDefs, null, new String[] {"QtCustomDefs", getString("QtCustomDefsTxt"), getString("QtCustomDefsHint")}, false, HelpCtx.DEFAULT_HELP)); // NOI18N
        sheet.put(expert);

        return sheet;
    }

    public IntConfiguration getBuildMode() {
        return buildMode;
    }

    public void setBuildMode(IntConfiguration buildMode) {
        this.buildMode = buildMode;
    }

    public String getEnabledModules() {
        StringBuilder buf = new StringBuilder();
        if (isCoreEnabled().getValue()) {
            append(buf, "core"); // NOI18N
        }
        if (isGuiEnabled().getValue()) {
            append(buf, "gui"); // NOI18N
        }
        if (isNetworkEnabled().getValue()) {
            append(buf, "network"); // NOI18N
        }
        if (isOpenglEnabled().getValue()) {
            append(buf, "opengl"); // NOI18N
        }
        if (isSqlEnabled().getValue()) {
            append(buf, "sql"); // NOI18N
        }
        if (isSvgEnabled().getValue()) {
            append(buf, "svg"); // NOI18N
        }
        if (isXmlEnabled().getValue()) {
            append(buf, "xml"); // NOI18N
        }
        return buf.toString();
    }

    public void setEnabledModules(String modules) {
        isCoreEnabled().setValue(false);
        isGuiEnabled().setValue(false);
        isNetworkEnabled().setValue(false);
        isOpenglEnabled().setValue(false);
        isSqlEnabled().setValue(false);
        isSvgEnabled().setValue(false);
        isXmlEnabled().setValue(false);
        StringTokenizer st = new StringTokenizer(modules);
        while (st.hasMoreTokens()) {
            String t = st.nextToken();
            if (t.equals("core")) { // NOI18N
                isCoreEnabled().setValue(true);
            } else if (t.equals("gui")) { // NOI18N
                isGuiEnabled().setValue(true);
            } else if (t.equals("network")) { // NOI18N
                isNetworkEnabled().setValue(true);
            } else if (t.equals("opengl")) { // NOI18N
                isOpenglEnabled().setValue(true);
            } else if (t.equals("sql")) { // NOI18N
                isSqlEnabled().setValue(true);
            } else if (t.equals("svg")) { // NOI18N
                isSvgEnabled().setValue(true);
            } else if (t.equals("xml")) { // NOI18N
                isXmlEnabled().setValue(true);
            } else {
                // unknown module
            }
        }
    }

    public BooleanConfiguration isCoreEnabled() {
        return coreEnabled;
    }

    public void setCoreEnabled(BooleanConfiguration val) {
        coreEnabled = val;
    }

    public BooleanConfiguration isGuiEnabled() {
        return guiEnabled;
    }

    public void setGuiEnabled(BooleanConfiguration val) {
        guiEnabled = val;
    }

    public BooleanConfiguration isNetworkEnabled() {
        return networkEnabled;
    }

    public void setNetworkEnabled(BooleanConfiguration val) {
        networkEnabled = val;
    }

    public BooleanConfiguration isOpenglEnabled() {
        return openglEnabled;
    }

    public void setOpenglEnabled(BooleanConfiguration val) {
        openglEnabled = val;
    }

    public BooleanConfiguration isSqlEnabled() {
        return sqlEnabled;
    }

    public void setSqlEnabled(BooleanConfiguration val) {
        sqlEnabled = val;
    }

    public BooleanConfiguration isSvgEnabled() {
        return svgEnabled;
    }

    public void setSvgEnabled(BooleanConfiguration val) {
        svgEnabled = val;
    }

    public BooleanConfiguration isXmlEnabled() {
        return xmlEnabled;
    }

    public void setXmlEnabled(BooleanConfiguration val) {
        xmlEnabled = val;
    }

    public StringConfiguration getMocDir() {
        return mocDir;
    }

    public void setMocDir(StringConfiguration mocDir) {
        this.mocDir = mocDir;
    }

    public StringConfiguration getUiDir() {
        return uiDir;
    }

    public void setUiDir(StringConfiguration uicDir) {
        this.uiDir = uicDir;
    }

    public VectorConfiguration<String> getCustomDefs() {
        return customDefs;
    }

    public void setCustomDefs(VectorConfiguration<String> customDefs) {
        this.customDefs = customDefs;
    }

    public void assign(QmakeConfiguration other) {
        getBuildMode().assign(other.getBuildMode());
        isCoreEnabled().assign(other.isCoreEnabled());
        isGuiEnabled().assign(other.isGuiEnabled());
        isNetworkEnabled().assign(other.isNetworkEnabled());
        isOpenglEnabled().assign(other.isOpenglEnabled());
        isSqlEnabled().assign(other.isSqlEnabled());
        isSvgEnabled().assign(other.isSvgEnabled());
        isXmlEnabled().assign(other.isXmlEnabled());
        getMocDir().assign(other.getMocDir());
        getUiDir().assign(other.getUiDir());
        getCustomDefs().assign(other.getCustomDefs());
    }

    @Override
    public QmakeConfiguration clone() {
        try {
            QmakeConfiguration clone = (QmakeConfiguration) super.clone();
            clone.setBuildMode(getBuildMode().clone());
            clone.setCoreEnabled(isCoreEnabled().clone());
            clone.setGuiEnabled(isGuiEnabled().clone());
            clone.setNetworkEnabled(isNetworkEnabled().clone());
            clone.setOpenglEnabled(isOpenglEnabled().clone());
            clone.setSqlEnabled(isSqlEnabled().clone());
            clone.setSvgEnabled(isSvgEnabled().clone());
            clone.setXmlEnabled(isXmlEnabled().clone());
            clone.setMocDir(getMocDir().clone());
            clone.setUiDir(getUiDir().clone());
            clone.setCustomDefs(getCustomDefs().clone());
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
