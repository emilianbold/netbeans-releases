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

import org.netbeans.modules.cnd.makeproject.configurations.ui.IntNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.StringNodeProp;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 * @author Alexey Vladykin
 */
public class QmakeConfiguration implements Cloneable {

    public static final String DEBUG_FLAG = "debug"; // NOI18N
    public static final String RELEASE_FLAG = "release"; // NOI18N

    private static final String[] TEMPLATE_NAMES = new String[] {
        getString("QmakeTemplateApp"), // NOI18N
        getString("QmakeTemplateLib") // NOI18N
    };

    private static final String[] TEMPLATE_OPTIONS = new String[] {
        "app", // NOI18N
        "lib" // NOI18N
    };

    private IntConfiguration template;
    private StringConfiguration config;

    public QmakeConfiguration() {
        template = new IntConfiguration(null, 0, TEMPLATE_NAMES, TEMPLATE_OPTIONS);
        config = new StringConfiguration(null, ""); // NOI18N
    }

    public Sheet getGeneralSheet() {
        Sheet sheet = new Sheet();

        Sheet.Set basic = new Sheet.Set();
        basic.setName("QmakeGeneral"); // NOI18N
        basic.setDisplayName(getString("QmakeGeneralTxt")); // NOI18N
        basic.setShortDescription(getString("QmakeGeneralHint")); // NOI18N
        basic.put(new IntNodeProp(template, true, null, getString("QmakeTemplateTxt"), getString("QmakeTemplateHint"))); // NOI18N
        basic.put(new StringNodeProp(config, "CONFIG", getString("QmakeConfigTxt"), getString("QmakeConfigHint"))); // NOI18N
        sheet.put(basic);

        return sheet;
    }

    public IntConfiguration getTemplate() {
        return template;
    }

    public void setTemplate(IntConfiguration template) {
        this.template = template;
    }

    public StringConfiguration getConfig() {
        return config;
    }

    public void setConfig(StringConfiguration config) {
        this.config = config;
    }

    public void assign(QmakeConfiguration other) {
        getTemplate().setValue(other.getTemplate().getValue());
        getConfig().setValue(other.getConfig().getValue());
    }

    @Override
    public QmakeConfiguration clone() {
        try {
            QmakeConfiguration clone = (QmakeConfiguration) super.clone();
            clone.setTemplate(getTemplate().clone());
            clone.setConfig(getConfig().clone());
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
