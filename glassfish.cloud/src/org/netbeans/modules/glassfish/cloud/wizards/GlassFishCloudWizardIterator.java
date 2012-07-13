/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.cloud.wizards;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.glassfish.cloud.data.GlassFishCloudInstance;
import org.netbeans.modules.glassfish.cloud.data.GlassFishCloudInstanceProvider;
import static org.openide.util.NbBundle.getMessage;

/**
 * GlassFish Cloud Wizard.
 * <p>
 * Adds GlassFish Cloud item into Add Cloud wizard.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class GlassFishCloudWizardIterator extends GlassFishWizardIterator {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Total panels count. */
    private static final int PANELS_COUNT = 1;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    public GlassFishCloudWizardIterator() {
        super(PANELS_COUNT);
        panel[0] = new GlassFishCloudWizardCpasPanel();
        for (int i = 0; i < PANELS_COUNT; i++) {
            this.name[i] = getMessage(GlassFishCloudWizardProvider.class,
                Bundle.addCloudWizardName(i), new Object[]{});
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
        String host = (String)wizard.getProperty(
                GlassFishCloudInstance.PROPERTY_HOST);
        String portStr = (String)wizard.getProperty(
                GlassFishCloudInstance.PROPERTY_PORT);
        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException nfe) {
            port = -1;
        }
        
        GlassFishCloudInstance cloudInstance
                = new GlassFishCloudInstance(name, host, port);
        GlassFishCloudInstanceProvider.addCloudInstance(cloudInstance);
        return Collections.singleton(cloudInstance.getServerInstance());
    }


}
