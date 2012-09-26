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
package org.netbeans.modules.cloud.oracle.ui;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cloud.common.spi.support.ui.CloudResourcesWizardPanel;
import org.netbeans.modules.cloud.oracle.OracleInstance;
import org.netbeans.modules.cloud.oracle.OracleInstanceManager;
import org.netbeans.modules.j2ee.weblogic9.DomainSupport;
import org.netbeans.modules.j2ee.weblogic9.DomainSupport.WLDomain;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 *
 */
public class OracleWizardIterator implements WizardDescriptor.AsynchronousInstantiatingIterator<WizardDescriptor>{

    private static final String LOCAL_DOMAIN_DIR = "JavaEE/Cloud9"; // NOI18N
    
    private static final String LOCAL_DOMAIN_USERNAME = "weblogic"; // NOI18N
    
    private static final String LOCAL_DOMAIN_PASSWORD = "welcome1"; // NOI18N

    private final ChangeSupport listeners;
    private WizardDescriptor wizard;
    private OracleWizardPanel panel;
    private CloudResourcesWizardPanel panel2;
    private int count = 0;

    public OracleWizardIterator() {
        listeners = new ChangeSupport(this);
    }
    
    public static final String PROP_DISPLAY_NAME = "ServInstWizard_displayName"; // NOI18N

    @Override
    public Set instantiate() throws IOException {
        String username = (String)wizard.getProperty(OracleWizardPanel.USERNAME);
        assert username != null;
        String pwd = (String)wizard.getProperty(OracleWizardPanel.PASSWORD);
        assert pwd != null;
        String adminURL = (String)wizard.getProperty(OracleWizardPanel.ADMIN_URL);
        assert adminURL != null;
        String dataCenter = (String)wizard.getProperty(OracleWizardPanel.DATA_CENTER);
        assert dataCenter != null;
        String name = (String)wizard.getProperty(PROP_DISPLAY_NAME);
        assert name != null;
        String identityDomain = (String)wizard.getProperty(OracleWizardPanel.IDENTITY_DOMAIN);
        assert identityDomain != null;
        String javaServiceName = (String)wizard.getProperty(OracleWizardPanel.JAVA_SERVICE_NAME);
        assert javaServiceName != null;
        String dbServiceName = (String)wizard.getProperty(OracleWizardPanel.DB_SERVICE_NAME);
        if (dbServiceName == null) {
            dbServiceName = "";
        }
        String sdk = (String)wizard.getProperty(OracleWizardPanel.SDK);
        assert sdk != null;
        Collection<WLDomain> localInstances = DomainSupport.getUsableDomainInstances(null);
        OracleInstance instance = new OracleInstance(name, OracleWizardComponent.getPrefixedUserName(identityDomain, username), pwd, adminURL, dataCenter,
                identityDomain, javaServiceName, dbServiceName,
                localInstances.isEmpty() ? null : localInstances.iterator().next().getUrl(), sdk);
        OracleInstanceManager.getDefault().add(instance);
        
        
        return Collections.singleton(instance.getServerInstance());
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        panel = null;
        panel2 = null;
    }

    @Override
    public Panel<WizardDescriptor> current() {
        switch (count) {
            case 0:
                if (panel == null) {
                    panel = new OracleWizardPanel();
                }
                return panel;
            default:
                if (panel2 == null) {
                    panel2 = new CloudResourcesWizardPanel(getPanelContentData(), 1);
                }
                return panel2;
        }
    }

    static String[] getPanelContentData() {
        return new String[] {
                NbBundle.getMessage(OracleWizardPanel.class, "LBL_ACIW_Oracle"),
                NbBundle.getMessage(OracleWizardPanel.class, "LBL_ACIW_Resources")
            };
    }
    
    @Override
    public String name() {
        return "Oracle Cloud";
    }

    @Override
    public boolean hasNext() {
        return count < 1;
    }

    @Override
    public boolean hasPrevious() {
        return count > 0;
    }

    @Override
    public void nextPanel() {
        count++;
    }

    @Override
    public void previousPanel() {
        count--;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        listeners.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        listeners.removeChangeListener(l);
    }
    
}
