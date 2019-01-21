/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.cloud.wizards;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.glassfish.tooling.data.cloud.GlassFishCloud;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.glassfish.cloud.data.GlassFishAccountInstance;
import org.netbeans.modules.glassfish.cloud.data.GlassFishAccountInstanceProvider;
import org.netbeans.modules.glassfish.cloud.data.GlassFishCloudInstanceProvider;
import static org.openide.util.NbBundle.getMessage;

/**
 * GlassFish User Account Wizard.
 * <p>
 * Adds GlassFish User Account item into Add Server wizard.
 * <p/>
 */
public class GlassFishAccountWizardIterator extends GlassFishWizardIterator {

    
    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(
            GlassFishAccountWizardIterator.class.getSimpleName());

    /** Total panels count. */
    private static final int PANELS_COUNT = 1;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    public GlassFishAccountWizardIterator() {
        super(PANELS_COUNT);
        panel[0] = new GlassFishAcocuntWizardUserPanel(name, 1);
        for (int i = 0; i < PANELS_COUNT + 1; i++) {
            this.name[i] = getMessage(GlassFishCloudWizardProvider.class,
                Bundle.addAccountWizardName(i), new Object[]{});
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented Interface Methods                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns set of instantiated objects.
     * <p/>
     * If instantiation fails then wizard remains open to enable correct values.
     * <p/>
     * @throws IOException
     * @return A set of objects created (the exact type is at the discretion
     *         of the caller).
     */
    @SuppressWarnings("LocalVariableHidesMemberVariable") // String name
    @Override
    public Set<ServerInstance> instantiate() throws IOException {
        String name = (String)wizard.getProperty(
                GlassFishWizardIterator.PROPERTY_WIZARD_DISPLAY_NAME);
        String account = (String)wizard.getProperty(
                GlassFishAccountInstance.PROPERTY_ACCOUNT);
        String userName = (String)wizard.getProperty(
                GlassFishAccountInstance.PROPERTY_USER_NAME);
        String userPassword = (String)wizard.getProperty(
                GlassFishAccountInstance.PROPERTY_USER_PASSWORD);
        String cloudName = (String)wizard.getProperty(
                GlassFishAccountInstance.PROPERTY_CLOUD_NAME);
        GlassFishCloud cloudEntity = cloudName != null
                    ? GlassFishCloudInstanceProvider.getCloudInstance(cloudName)
                    : null;
        GlassFishAccountInstance accountInstance
                = new GlassFishAccountInstance(
                name, account, userName, userPassword, cloudEntity);
        GlassFishAccountInstanceProvider.addAccountInstance(accountInstance);
        LOG.log(Level.FINER,
                "Added GlassFishCloudInstance({0}, {1}, {2}, <password>, {4})",
                new Object[]{name, account, userName, cloudEntity != null
                ? cloudEntity.getName() : "null"});
        return Collections.singleton(accountInstance.getServerInstance());
    }

}
