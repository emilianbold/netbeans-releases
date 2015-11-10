/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.ui.wizard;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.docker.DockerInstance;
import org.netbeans.modules.docker.DockerRegistry;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public class DockerConnectionPanel implements WizardDescriptor.Panel<WizardDescriptor>, ChangeListener {

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private DockerConnectionVisual component;

    private WizardDescriptor wizard;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public DockerConnectionVisual getComponent() {
        if (component == null) {
            component = new DockerConnectionVisual();
            component.addChangeListener(this);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx("help.key.here");
    }

    @NbBundle.Messages({
        "MSG_EmptyDisplayName=Display name must not be empty.",
        "MSG_AlreadyUsedDisplayName=Display name is already used by another instance.",
        "MSG_EmptyUrl=URL must not be empty.",
        "MSG_InvalidUrl=URL must be valid http or https URL.",
        "MSG_NonExistingCertificatePath=The certificates path does not exist.",
        "# {0} - missing file",
        "MSG_CertificatePathMissingFile=The certificates path does not contain {0}."
    })
    @Override
    public boolean isValid() {
        String displayName = component.getDisplayName();
        if (displayName == null) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.MSG_EmptyDisplayName());
            return false;
        }
        for (DockerInstance instance : DockerRegistry.getInstance().getInstances()) {
            if (displayName.equals(instance.getDisplayName())) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.MSG_AlreadyUsedDisplayName());
                return false;
            }
        }

        String url = component.getUrl();
        if (url == null) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.MSG_EmptyUrl());
            return false;
        }
        boolean urlWrong = false;
        try {
            URL realUrl = new URL(url);
            if (!"http".equals(realUrl.getProtocol()) && !"https".equals(realUrl.getProtocol())) { // NOI18N
                urlWrong = true;
            }
        } catch (MalformedURLException ex) {
            urlWrong = true;
        }
        if (urlWrong) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.MSG_InvalidUrl());
            return false;
        }

        String certPath = component.getCertPath();
        if (certPath != null) {
            File certPathFile = new File(certPath);
            if (!certPathFile.isDirectory()) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.MSG_NonExistingCertificatePath());
                return false;
            }
            if (!new File(certPathFile, AddDockerInstanceWizard.DEFAULT_CA_FILE).isFile()) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        Bundle.MSG_CertificatePathMissingFile(AddDockerInstanceWizard.DEFAULT_CA_FILE));
                return false;
            }
            if (!new File(certPathFile, AddDockerInstanceWizard.DEFAULT_CERT_FILE).isFile()) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        Bundle.MSG_CertificatePathMissingFile(AddDockerInstanceWizard.DEFAULT_CERT_FILE));
                return false;
            }
            if (!new File(certPathFile, AddDockerInstanceWizard.DEFAULT_KEY_FILE).isFile()) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        Bundle.MSG_CertificatePathMissingFile(AddDockerInstanceWizard.DEFAULT_KEY_FILE));
                return false;
            }
        }

        return true;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    @Override
    public void readSettings(WizardDescriptor wiz) {
        if (wizard == null) {
            wizard = wiz;
        }

        if (Utilities.isMac() || Utilities.isWindows()) {
            component.setUrl("https://192.168.59.103:2376"); // NOI18N
            File docker = new File(System.getProperty("user.home"), ".docker"); // NOI18N
            if (!docker.isDirectory()) {
                docker = new File(System.getProperty("user.home"), ".boot2docker"); // NOI18N
            }
            if (docker.isDirectory()) {
                component.setCertPath(docker.getAbsolutePath());
            }
        }
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        wiz.putProperty(AddDockerInstanceWizard.DISPLAY_NAME_PROPERTY, component.getDisplayName());
        wiz.putProperty(AddDockerInstanceWizard.URL_PROPERTY, component.getUrl());
        wiz.putProperty(AddDockerInstanceWizard.CERTIFICATE_PATH_PROPERTY, component.getCertPath());
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }
}
