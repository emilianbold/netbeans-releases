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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.javawebstart;

import java.util.Map;

import javax.swing.JComponent;

import org.netbeans.api.project.Project;

import org.netbeans.modules.java.j2seproject.api.J2SERunConfigProvider;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;

import org.netbeans.modules.javawebstart.ui.customizer.JWSCustomizerPanel;

import org.netbeans.spi.project.support.ant.PropertyEvaluator;

/**
 *
 * @author Milan Kubec
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.java.j2seproject.api.J2SERunConfigProvider.class)
public class J2SERunConfigProviderImpl implements J2SERunConfigProvider {
    
    public J2SERunConfigProviderImpl() {}
    
    public JComponent createComponent(Project p, J2SERunConfigProvider.ConfigChangeListener listener) {
        J2SEPropertyEvaluator j2sePropEval = p.getLookup().lookup(J2SEPropertyEvaluator.class);
        PropertyEvaluator evaluator = j2sePropEval.evaluator();
        String enabled = evaluator.getProperty("jnlp.enabled"); // NOI18N
        JWSCustomizerPanel.runComponent.addListener(listener);
        if ("true".equals(enabled)) {
            JWSCustomizerPanel.runComponent.setCheckboxEnabled(true);
            JWSCustomizerPanel.runComponent.setHintVisible(false);
        } else {
            JWSCustomizerPanel.runComponent.setCheckboxEnabled(false);
            JWSCustomizerPanel.runComponent.setHintVisible(true);
        }
        return JWSCustomizerPanel.runComponent;
    }
    
    public void configUpdated(Map<String,String> m) {
        if ((m.get("$target.run") != null) && (m.get("$target.debug") != null)) { // NOI18N
            JWSCustomizerPanel.runComponent.setCheckboxSelected(true);
        } else {
            JWSCustomizerPanel.runComponent.setCheckboxSelected(false);
        }
    }
    
}
