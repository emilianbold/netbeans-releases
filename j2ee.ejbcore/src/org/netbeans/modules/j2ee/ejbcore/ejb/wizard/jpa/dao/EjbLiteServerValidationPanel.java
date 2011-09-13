/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.jpa.dao;

import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.modules.j2ee.core.api.support.wizard.DelegatingWizardDescriptorPanel;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 * A validation panel which checks that the target project has a valid server set
 * otherwise it delegates to the real panel.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public final class EjbLiteServerValidationPanel extends DelegatingWizardDescriptorPanel {

    public EjbLiteServerValidationPanel(WizardDescriptor.Panel delegate) {
        super(delegate);
    }

    @Override
    public boolean isValid() {
        Project project = getProject();
        WizardDescriptor wizardDescriptor = getWizardDescriptor();

        // check that this project has a valid target server
        if (!org.netbeans.modules.j2ee.common.Util.isValidServerInstance(project)) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(EjbLiteServerValidationPanel.class, "ERR_MissingServer")); // NOI18N
            return false;
        }

        // check that target server supports full JEE6 platform if Java EE 6 sources
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());

        // webModule is null in cases of EJB application (Session beans from Entity classes)
        // where should be proper server set (issue #202050)
        if (wm != null
                && (wm.getJ2eeProfile() == Profile.JAVA_EE_6_FULL || wm.getJ2eeProfile() == Profile.JAVA_EE_6_WEB)) {
            J2eeProjectCapabilities cap = J2eeProjectCapabilities.forProject(project);
            if (cap == null || !cap.isEjbLiteIncluded()) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(EjbLiteServerValidationPanel.class,
                        "ERR_J2ee6AndNotSufficientJ2eeServer")); // NOI18N
                return false;
            }
        }

        return super.isValid();
    }
}
