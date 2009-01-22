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

import org.netbeans.modules.cnd.makeproject.configurations.ui.BooleanNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.StringNodeProp;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 * @author Alexey Vladykin
 */
public class QmakeConfiguration implements Cloneable {

    public static final String DEBUG_FLAG = "debug"; // NOI18N
    public static final String RELEASE_FLAG = "release"; // NOI18N

    private StringConfiguration config;
    private BooleanConfiguration coreEnabled;
    private BooleanConfiguration guiEnabled;
    private BooleanConfiguration networkEnabled;
    private BooleanConfiguration openglEnabled;
    private BooleanConfiguration sqlEnabled;
    private BooleanConfiguration svgEnabled;
    private BooleanConfiguration xmlEnabled;

    public QmakeConfiguration() {
        config = new StringConfiguration(null, ""); // NOI18N
        coreEnabled = new BooleanConfiguration(null, true);
        guiEnabled = new BooleanConfiguration(null, true);
        networkEnabled = new BooleanConfiguration(null, false);
        openglEnabled = new BooleanConfiguration(null, false);
        sqlEnabled = new BooleanConfiguration(null, false);
        svgEnabled = new BooleanConfiguration(null, false);
        xmlEnabled = new BooleanConfiguration(null, false);
    }

    public Sheet getGeneralSheet() {
        Sheet sheet = new Sheet();

        Sheet.Set basic = new Sheet.Set();
        basic.setName("QtGeneral"); // NOI18N
        basic.setDisplayName(getString("QtGeneralTxt")); // NOI18N
        basic.setShortDescription(getString("QtGeneralHint")); // NOI18N
        basic.put(new StringNodeProp(config, "QtConfig", getString("QtConfigTxt"), getString("QtConfigHint"))); // NOI18N
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

        return sheet;
    }

    public StringConfiguration getConfig() {
        return config;
    }

    public void setConfig(StringConfiguration config) {
        this.config = config;
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

    public void assign(QmakeConfiguration other) {
        getConfig().assign(other.getConfig());
        isCoreEnabled().assign(other.isCoreEnabled());
        isGuiEnabled().assign(other.isGuiEnabled());
        isNetworkEnabled().assign(other.isNetworkEnabled());
        isOpenglEnabled().assign(other.isOpenglEnabled());
        isSqlEnabled().assign(other.isSqlEnabled());
        isSvgEnabled().assign(other.isSvgEnabled());
        isXmlEnabled().assign(other.isXmlEnabled());
    }

    @Override
    public QmakeConfiguration clone() {
        try {
            QmakeConfiguration clone = (QmakeConfiguration) super.clone();
            clone.setConfig(getConfig().clone());
            clone.setCoreEnabled(isCoreEnabled().clone());
            clone.setGuiEnabled(isGuiEnabled().clone());
            clone.setNetworkEnabled(isNetworkEnabled().clone());
            clone.setOpenglEnabled(isOpenglEnabled().clone());
            clone.setSqlEnabled(isSqlEnabled().clone());
            clone.setSvgEnabled(isSvgEnabled().clone());
            clone.setXmlEnabled(isXmlEnabled().clone());
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

}
