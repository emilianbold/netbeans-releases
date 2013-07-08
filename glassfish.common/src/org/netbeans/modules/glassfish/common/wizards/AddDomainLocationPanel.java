/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.glassfish.common.wizards;

import java.awt.Component;
import java.io.File;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.glassfish.tools.ide.data.GlassFishConfig;
import org.glassfish.tools.ide.data.GlassFishVersion;
import org.glassfish.tools.ide.server.config.ConfigBuilderProvider;
import org.glassfish.tools.ide.server.config.GlassFishConfigManager;
import org.glassfish.tools.ide.server.config.JavaSEPlatform;
import org.glassfish.tools.ide.server.config.JavaSESet;
import org.glassfish.tools.ide.utils.ServerUtils;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.glassfish.common.GlassfishInstance;
import org.netbeans.modules.glassfish.spi.ServerUtilities;
import org.netbeans.modules.glassfish.spi.Utils;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class AddDomainLocationPanel implements WizardDescriptor.Panel, ChangeListener {

    private final String PROP_ERROR_MESSAGE = WizardDescriptor.PROP_ERROR_MESSAGE;
    private final String PROP_WARNING_MESSAGE = WizardDescriptor.PROP_WARNING_MESSAGE;
    private final String PROP_INFO_MESSAGE = WizardDescriptor.PROP_INFO_MESSAGE;

    private ServerWizardIterator wizardIterator;
    private AddDomainLocationVisualPanel component;
    private WizardDescriptor wizard;
    private transient List<ChangeListener> listeners = new CopyOnWriteArrayList<ChangeListener>();
    private String gfRoot;

    /** Default Java SE platform is supported by selected GlassFish server. */
    boolean defaultJavaSESupported;

    /**
     * 
     * @param instantiatingIterator 
     */
    public AddDomainLocationPanel(ServerWizardIterator wizardIterator) {
        this.wizardIterator = wizardIterator;
        wizard = null;
    }

    private AtomicBoolean isValidating = new AtomicBoolean();

    /**
     * 
     * @return 
     */
    @Override
    public boolean isValid() {
        if (isValidating.compareAndSet(false, true)) {
            try {
                AddDomainLocationVisualPanel panel = (AddDomainLocationVisualPanel) getComponent();
                if (panel.registerLocalDomain()) {
                    return validateForLocalDomain(panel);
                } else {
                    return validateForRemoteDomain(panel);
                }
            } finally {
                isValidating.set(false);
            }
        }
        return true;
    }

    /**
     * 
     * @param ev 
     */
    @Override
    public void stateChanged(ChangeEvent ev) {
        fireChangeEvent(ev);
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChangeEvent(ChangeEvent ev) {
        for(ChangeListener listener: listeners) {
            listener.stateChanged(ev);
        }
    }

    /**
     * 
     * @return 
     */
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new AddDomainLocationVisualPanel();
            component.addChangeListener(this);
        }
        return component;
    }

    /**
     * 
     * @return 
     */
    @Override
    public HelpCtx getHelp() {
        FileObject fo = FileUtil.getConfigFile("Services/JavaHelp/org-netbeans-modules-j2ee-helpset.xml");
        if (null != fo)
            return new HelpCtx("registering_app_server_hk2_domain"); //NOI18N
        else
            return null;
    }

    /**
     * 
     * @param settings 
     */
    @Override
    public void readSettings(Object settings) {
        if (wizard == null) {
            wizard = (WizardDescriptor) settings;
        }
        gfRoot = wizardIterator.getGlassfishRoot();
        ((AddDomainLocationVisualPanel) getComponent()).initModels(gfRoot);

        // Check if default Java SE platform is supported\
        // by selected GlassFish server.
        JavaPlatform defaultJava = JavaPlatform.getDefault();
        JavaSEPlatform defaultJavaPlatform = JavaSEPlatform.toValue(
                    defaultJava.getSpecification().getVersion().toString());
        GlassFishVersion glassFishVersion
                = ServerUtils.getServerVersion(gfRoot);
        GlassFishConfig configAdapter = GlassFishConfigManager.getConfig(
                ConfigBuilderProvider.getBuilderConfig(glassFishVersion));
        JavaSESet javaSEConfig = configAdapter != null
                ? configAdapter.getJavaSE() : null;
        Set<JavaSEPlatform> glassFishJavaSEPlatfors = javaSEConfig != null
                ? javaSEConfig.platforms() : null;
        defaultJavaSESupported = glassFishJavaSEPlatfors != null
                ? glassFishJavaSEPlatfors.contains(defaultJavaPlatform) : false;
        wizardIterator.serDefaultJavaSESupported(defaultJavaSESupported);
    }

    /**
     * 
     * @param settings 
     */
    @Override
    public void storeSettings(Object settings) {
    }

    /**
     * Sets GlassFish server target, administrator's user name and password into
     * wizard iterator.
     * <p/>
     * @param wizardIterator Target wizard iterator object.
     * @param panel Source wizard panel component.
     */
    private static void setGlobalValues(ServerWizardIterator wizardIterator, AddDomainLocationVisualPanel panel) {
        wizardIterator.setTargetValue(panel.getTargetValue());
        wizardIterator.setUserName(panel.getUserNameValue());
        wizardIterator.setPassword(panel.getPasswordValue());
    }

    private boolean validateForLocalDomain(AddDomainLocationVisualPanel panel) throws MissingResourceException {
        String domainField = panel.getDomainField().trim();
        File domainDirCandidate = new File(gfRoot, GlassfishInstance.DEFAULT_DOMAINS_FOLDER + File.separator + domainField); // NOI18N
        if (domainField.length() < 1) {
            if (!Utils.canWrite(domainDirCandidate)) {
                // the user needs to enter the name of a directory for
                // a personal domain
                wizard.putProperty(PROP_INFO_MESSAGE, NbBundle.getMessage(this.getClass(), "MSG_EnterDomainDirectory")); // NOI18N
            } else {
                // the user probably deleted a valid name from the field.
                wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(this.getClass(), "MSG_MustHaveName")); // NOI18N
            }
            return false;
        }
        int dex = domainField.indexOf(File.separator);
        if (AddServerLocationPanel.isRegisterableDomain(domainDirCandidate)) {
            org.netbeans.modules.glassfish.common.utils.Util.readServerConfiguration(domainDirCandidate, wizardIterator);
            String uri = wizardIterator.formatUri(GlassfishInstance.DEFAULT_HOST_NAME, wizardIterator.getAdminPort(), panel.getTargetValue(),
                    new File(gfRoot, GlassfishInstance.DEFAULT_DOMAINS_FOLDER).getAbsolutePath(), domainField);
            if (-1 == wizardIterator.getHttpPort()) {
                wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(this.getClass(), "ERR_InvalidDomainData", domainField)); // NOI18N
                return false;
            }
            if (-1 == wizardIterator.getAdminPort()) {
                wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(this.getClass(), "ERR_InvalidDomainData", domainField)); // NOI18N
                return false;
            }
            if (wizardIterator.hasServer(uri)) {
                wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(this.getClass(), "ERR_DomainAlreadyRegistered", domainField)); // NOI18N
                return false;
            }
            // the entry resolves to a domain name that we can register
            wizardIterator.setDomainLocation(domainDirCandidate.getAbsolutePath());
            wizardIterator.setHostName("localhost");
            setGlobalValues(wizardIterator, panel);
            wizard.putProperty(PROP_INFO_MESSAGE, NbBundle.getMessage(this.getClass(), "MSG_RegisterExistingEmbedded", domainField)); // NOI18N
            return true;
        }
        File domainsDir = domainDirCandidate.getParentFile();
        if (Utils.canWrite(domainsDir) && dex < 0 && !ServerUtilities.isTP2(gfRoot) &&
                !domainDirCandidate.exists()) {
            wizardIterator.setDomainLocation(domainDirCandidate.getAbsolutePath());
            wizardIterator.setHostName("localhost");
            wizardIterator.setUseDefaultPorts(panel.getUseDefaultPorts());
            setGlobalValues(wizardIterator, panel);
            if (defaultJavaSESupported) {
                wizard.putProperty(PROP_INFO_MESSAGE,
                        NbBundle.getMessage(this.getClass(),
                        "MSG_CreateEmbedded", domainField)); // NOI18N
            } else {
                wizard.putProperty(PROP_WARNING_MESSAGE,
                        NbBundle.getMessage(this.getClass(),
                        "WRN_CreateEmbedded", domainField)); // NOI18N               
            }
            return defaultJavaSESupported;
        }
        domainDirCandidate = new File(domainField);
        String domainLoc = domainDirCandidate.getAbsolutePath();
        if (AddServerLocationPanel.isRegisterableDomain(domainDirCandidate)) {
            // the entry resolves to a domain name that we can register
            //String domainLoc = domainDirCandidate.getAbsolutePath();
            wizardIterator.setDomainLocation(domainLoc);
            wizardIterator.setHostName("localhost");
            setGlobalValues(wizardIterator, panel);
            wizard.putProperty(PROP_INFO_MESSAGE, NbBundle.getMessage(this.getClass(), "MSG_RegisterExisting", domainField)); // NOI18N
            org.netbeans.modules.glassfish.common.utils.Util.readServerConfiguration(domainDirCandidate, wizardIterator);
            return true;
        }
        if (AddServerLocationPanel.canCreate(domainDirCandidate) && !ServerUtilities.isTP2(gfRoot)) {
            wizardIterator.setDomainLocation(domainLoc);
            wizard.putProperty(PROP_INFO_MESSAGE, NbBundle.getMessage(this.getClass(), "MSG_CreateDomain", domainField)); // NOI18N
            wizardIterator.setUseDefaultPorts(panel.getUseDefaultPorts());
            wizardIterator.setHostName("localhost");
            setGlobalValues(wizardIterator, panel);
            return true;
        }
        if (new File(domainsDir, domainField).exists()) {
            wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(this.getClass(), "ERR_UnusableDomain", domainField)); // NOI18N
        } else if (domainDirCandidate.exists()) {
            wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(this.getClass(), "ERR_UnusableDomain", domainField)); // NOI18N
        } else {
            wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(this.getClass(), "ERR_CannotCreateDomain", domainField)); // NOI18N
        }
        return false;
    }

    private boolean validateForRemoteDomain(AddDomainLocationVisualPanel panel) {
        String hn = panel.getHostName();
        String port = panel.getPortValue();
        try {
            int portval = Integer.parseInt(port);
            wizardIterator.setAdminPort(portval);
            wizardIterator.setHostName(hn);
            wizardIterator.setDomainLocation(null);
            setGlobalValues(wizardIterator, panel);
            wizard.putProperty(PROP_INFO_MESSAGE, NbBundle.getMessage(this.getClass(), "MSG_RegisterRemote", hn,port)); // NOI18N
            return true;
        } catch (NumberFormatException nfe) {
            wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(this.getClass(), "ERR_InvalidAdminPort", port)); // NOI18N
            return false;
        }

    }
    
}
