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

import java.awt.Component;
import java.beans.BeanInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import oracle.cloud.paas.exception.ManagerException;
import org.netbeans.modules.cloud.common.spi.support.ui.CloudResourcesWizardPanel;
import org.netbeans.modules.cloud.common.spi.support.ui.ServerResourceDescriptor;
import org.netbeans.modules.cloud.oracle.OracleInstance;
import org.netbeans.modules.cloud.oracle.serverplugin.OracleJ2EEInstance;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 */
public class OracleWizardPanel implements WizardDescriptor.AsynchronousValidatingPanel<WizardDescriptor>, ChangeListener {

    public static final String USERNAME = "username"; // String
    public static final String PASSWORD = "password"; // String
    public static final String ADMIN_URL = "admin-url"; // List<Node>
    public static final String INSTANCE_URL = "instance-url"; // List<Node>
    public static final String CLOUD_URL = "cloud-url"; // List<Node>
    public static final String SERVICE_GROUP = "service-group"; // List<Node>
    public static final String SERVICE_NAME = "service-name"; // List<Node>
    
    private OracleWizardComponent component;
    private ChangeSupport listeners;
    private WizardDescriptor wd = null;
    private List<ServerResourceDescriptor> servers;
    
    private static final Logger LOG = Logger.getLogger(OracleWizardComponent.class.getName());
    
    public OracleWizardPanel() {
        listeners = new ChangeSupport(this);
    }
    
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new OracleWizardComponent();
            component.attachSingleListener(this);
            component.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, OracleWizardIterator.getPanelContentData());
            component.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(0));
        }
        return component;
    }
    
    @Override
    public HelpCtx getHelp() {
        return null;
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        wd = settings;
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        if (component != null) {
            settings.putProperty(USERNAME, component.getUserName());
            settings.putProperty(PASSWORD, component.getPassword());
            settings.putProperty(ADMIN_URL, component.getAdminUrl());
            settings.putProperty(INSTANCE_URL, component.getInstanceUrl());
            settings.putProperty(CLOUD_URL, component.getCloudUrl());
            settings.putProperty(SERVICE_GROUP, component.getServiceGroup());
            settings.putProperty(SERVICE_NAME, component.getServiceInstance());
            settings.putProperty(CloudResourcesWizardPanel.PROP_SERVER_RESOURCES, servers);
        }
    }

    public void setErrorMessage(String message) {
        wd.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
    }
    
    @Override
    public boolean isValid() {
        if (component == null || wd == null) {
            // ignore this case
        } else if (component.getServiceInstance().trim().length() == 0) {
            setErrorMessage(NbBundle.getMessage(OracleWizardPanel.class, "OracleWizardPanel.missingServiceInstance"));
            return false;
        } else if (component.getServiceGroup().trim().length() == 0) {
            setErrorMessage(NbBundle.getMessage(OracleWizardPanel.class, "OracleWizardPanel.missingServiceGroup"));
            return false;
        } else if (component.getUserName().trim().length() == 0) {
            setErrorMessage(NbBundle.getMessage(OracleWizardPanel.class, "OracleWizardPanel.missingUserName"));
            return false;
        } else if (component.getPassword().trim().length() == 0) {
            setErrorMessage(NbBundle.getMessage(OracleWizardPanel.class, "OracleWizardPanel.missingPassword"));
            return false;
        } else if (component.getAdminUrl().trim().length() == 0) {
            setErrorMessage(NbBundle.getMessage(OracleWizardPanel.class, "OracleWizardPanel.missingAdminUrl"));
            return false;
        } else if (component.getInstanceUrl().trim().length() == 0) {
            setErrorMessage(NbBundle.getMessage(OracleWizardPanel.class, "OracleWizardPanel.missingInstanceUrl"));
            return false;
        } else if (component.getCloudUrl().trim().length() == 0) {
            setErrorMessage(NbBundle.getMessage(OracleWizardPanel.class, "OracleWizardPanel.missingCloudUrl"));
            return false;
        }
        setErrorMessage("");
        return true;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        listeners.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        listeners.removeChangeListener(l);
    }
    
    void fireChange() {
        listeners.fireChange();
    }

    @Override
    public void prepareValidation() {
        getComponent().setCursor(Utilities.createProgressCursor(getComponent()));
    }

    @Override
    public void validate() throws WizardValidationException {
        try {
            servers = new ArrayList<ServerResourceDescriptor>();
            OracleInstance ai = new OracleInstance("Oracle Cloud", component.getUserName(), 
                    component.getPassword(), component.getAdminUrl(), component.getInstanceUrl(),
                    component.getCloudUrl(), component.getServiceGroup(), component.getServiceInstance(), null);
            try {
                ai.testConnection();
            } catch (ManagerException ex) {
                LOG.log(Level.INFO, "cannot connect to oracle cloud", ex);
                throw new WizardValidationException((JComponent)getComponent(), 
                        "connection failed", NbBundle.getMessage(OracleWizardPanel.class, "OracleWizardPanel.wrong.credentials"));
            } catch (Throwable t) {
                LOG.log(Level.INFO, "cannot connect", t);
                throw new WizardValidationException((JComponent)getComponent(), 
                        "connection exception", NbBundle.getMessage(OracleWizardPanel.class, "OracleWizardPanel.something.wrong"));
            }
            OracleJ2EEInstance instance = ai.readJ2EEServerInstance();
            OracleJ2EEInstanceNode n = new OracleJ2EEInstanceNode(instance, true);
            servers.add(new ServerResourceDescriptor("Server", n.getDisplayName(), "", ImageUtilities.image2Icon(n.getIcon(BeanInfo.ICON_COLOR_16x16))));
        } finally {
            getComponent().setCursor(null);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireChange();
    }
    
}
