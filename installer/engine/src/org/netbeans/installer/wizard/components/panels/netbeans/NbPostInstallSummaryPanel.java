/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.wizard.components.panels.netbeans;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.helper.swing.NbiCheckBox;
import org.netbeans.installer.utils.helper.swing.NbiPanel;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.wizard.components.WizardPanel;
import org.netbeans.installer.wizard.components.actions.netbeans.NbRegistrationAction;
import org.netbeans.installer.wizard.containers.SwingContainer;
import org.netbeans.installer.wizard.containers.SwingFrameContainer;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;
import org.netbeans.modules.reglib.BrowserSupport;
import static org.netbeans.installer.utils.helper.DetailedStatus.INSTALLED_SUCCESSFULLY;
import static org.netbeans.installer.utils.helper.DetailedStatus.INSTALLED_WITH_WARNINGS;
import static org.netbeans.installer.utils.helper.DetailedStatus.FAILED_TO_INSTALL;
import static org.netbeans.installer.utils.helper.DetailedStatus.UNINSTALLED_SUCCESSFULLY;
import static org.netbeans.installer.utils.helper.DetailedStatus.UNINSTALLED_WITH_WARNINGS;
import static org.netbeans.installer.utils.helper.DetailedStatus.FAILED_TO_UNINSTALL;

/**
 *
 * @author Kirill Sorokin
 */
public class NbPostInstallSummaryPanel extends WizardPanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public NbPostInstallSummaryPanel() {
        setProperty(TITLE_PROPERTY, 
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY, 
                DEFAULT_DESCRIPTION);
        
        setProperty(MESSAGE_TEXT_SUCCESS_PROPERTY, 
                DEFAULT_MESSAGE_TEXT_SUCCESS);
        setProperty(MESSAGE_CONTENT_TYPE_SUCCESS_PROPERTY, 
                DEFAULT_MESSAGE_CONTENT_TYPE_SUCCESS);
        setProperty(MESSAGE_TEXT_WARNINGS_PROPERTY, 
                DEFAULT_MESSAGE_TEXT_WARNINGS);
        setProperty(MESSAGE_CONTENT_TYPE_WARNINGS_PROPERTY, 
                DEFAULT_MESSAGE_CONTENT_TYPE_WARNINGS);
        setProperty(MESSAGE_TEXT_ERRORS_PROPERTY, 
                DEFAULT_MESSAGE_TEXT_ERRORS);
        setProperty(MESSAGE_CONTENT_TYPE_ERRORS_PROPERTY, 
                DEFAULT_MESSAGE_CONTENT_TYPE_ERRORS);
        
        setProperty(MESSAGE_TEXT_SUCCESS_UNINSTALL_PROPERTY, 
                DEFAULT_MESSAGE_TEXT_SUCCESS_UNINSTALL);
        setProperty(MESSAGE_CONTENT_TYPE_SUCCESS_UNINSTALL_PROPERTY, 
                DEFAULT_MESSAGE_CONTENT_TYPE_SUCCESS_UNINSTALL);
        setProperty(MESSAGE_TEXT_WARNINGS_UNINSTALL_PROPERTY, 
                DEFAULT_MESSAGE_TEXT_WARNINGS_UNINSTALL);
        setProperty(MESSAGE_CONTENT_TYPE_WARNINGS_UNINSTALL_PROPERTY, 
                DEFAULT_MESSAGE_CONTENT_TYPE_WARNINGS_UNINSTALL);
        setProperty(MESSAGE_TEXT_ERRORS_UNINSTALL_PROPERTY, 
                DEFAULT_MESSAGE_TEXT_ERRORS_UNINSTALL);
        setProperty(MESSAGE_CONTENT_TYPE_ERRORS_UNINSTALL_PROPERTY, 
                DEFAULT_MESSAGE_CONTENT_TYPE_ERRORS_UNINSTALL);
        setProperty(MESSAGE_FILES_REMAINING_PROPERTY,
                DEFAULT_MESSAGE_FILES_REMAINING);
        
        setProperty(NEXT_BUTTON_TEXT_PROPERTY, 
                DEFAULT_NEXT_BUTTON_TEXT);
    }
    
    @Override
    public boolean isPointOfNoReturn() {
        return true;
    }
    
    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new NbPostInstallSummaryPanelUi(this);
        }
        
        return wizardUi;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class NbPostInstallSummaryPanelUi extends WizardPanelUi {
        protected NbPostInstallSummaryPanel component;
        
        public NbPostInstallSummaryPanelUi(NbPostInstallSummaryPanel component) {
            super(component);
            
            this.component = component;
        }
        
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new NbPostInstallSummaryPanelSwingUi(component, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class NbPostInstallSummaryPanelSwingUi extends WizardPanelSwingUi {
        protected NbPostInstallSummaryPanel component;
        
        private NbiTextPane messagePaneInstall;
        private NbiTextPane messagePaneUninstall;
        private NbiTextPane messagePaneNetBeans;
        private NbiTextPane messagePaneRegistration;                                 
        private NbiCheckBox checkBoxRegistration;                                    
        private NbiPanel spacer;

        public NbPostInstallSummaryPanelSwingUi(
                final NbPostInstallSummaryPanel component,
                final SwingContainer container) {
            super(component, container);
            
            this.component = component;
            
            initComponents();
        }
        
        protected void initializeContainer() {
            super.initializeContainer();
            
            // set up the back button
            container.getBackButton().setVisible(false);
            container.getBackButton().setEnabled(false);
            
            // set up the next (or finish) button
            container.getNextButton().setVisible(true);
            container.getNextButton().setEnabled(true);
            
            container.getNextButton().setText(
                    component.getProperty(NEXT_BUTTON_TEXT_PROPERTY));
            
            // set up the cancel button
            container.getCancelButton().setVisible(false);
            container.getCancelButton().setEnabled(false);            
        }
        
        @Override
        public void evaluateNextButtonClick() {
            container.getNextButton().setEnabled(false);            
            super.evaluateNextButtonClick();
        }
        
        protected void initialize() {
            final Registry registry = Registry.getInstance();
            
            if (registry.getProducts(INSTALLED_SUCCESSFULLY).size() +
                    registry.getProducts(INSTALLED_WITH_WARNINGS).size() +
                    registry.getProducts(FAILED_TO_INSTALL).size() > 0) {
                boolean warningsEncountered =
                        registry.getProducts(INSTALLED_WITH_WARNINGS).size() > 0;
                
                boolean errorsEncountered =
                        registry.getProducts(FAILED_TO_INSTALL).size() > 0;
                
                if (errorsEncountered) {
                    messagePaneInstall.setContentType(component.getProperty(MESSAGE_CONTENT_TYPE_ERRORS_PROPERTY));
                    messagePaneInstall.setText(StringUtils.format(component.getProperty(MESSAGE_TEXT_ERRORS_PROPERTY), LogManager.getLogFile()));
                } else if (warningsEncountered) {
                    messagePaneInstall.setContentType(component.getProperty(MESSAGE_CONTENT_TYPE_WARNINGS_PROPERTY));
                    messagePaneInstall.setText(StringUtils.format(component.getProperty(MESSAGE_TEXT_WARNINGS_PROPERTY), LogManager.getLogFile()));
                } else {
                    messagePaneInstall.setContentType(component.getProperty(MESSAGE_CONTENT_TYPE_SUCCESS_PROPERTY));
                    messagePaneInstall.setText(StringUtils.format(component.getProperty(MESSAGE_TEXT_SUCCESS_PROPERTY), LogManager.getLogFile()));
                }
            } else {
                messagePaneInstall.setVisible(false);
            }
            
            if (registry.getProducts(UNINSTALLED_SUCCESSFULLY).size() +
                    registry.getProducts(UNINSTALLED_WITH_WARNINGS).size() +
                    registry.getProducts(FAILED_TO_UNINSTALL).size() > 0) {
                boolean warningsEncountered =
                        registry.getProducts(UNINSTALLED_WITH_WARNINGS).size() > 0;
                
                boolean errorsEncountered =
                        registry.getProducts(FAILED_TO_UNINSTALL).size() > 0;
                
                if (errorsEncountered) {
                    messagePaneUninstall.setContentType(component.getProperty(MESSAGE_CONTENT_TYPE_ERRORS_UNINSTALL_PROPERTY));
                    messagePaneUninstall.setText(StringUtils.format(component.getProperty(MESSAGE_TEXT_ERRORS_UNINSTALL_PROPERTY), LogManager.getLogFile()));
                } else if (warningsEncountered) {
                    messagePaneUninstall.setContentType(component.getProperty(MESSAGE_CONTENT_TYPE_WARNINGS_UNINSTALL_PROPERTY));
                    messagePaneUninstall.setText(StringUtils.format(component.getProperty(MESSAGE_TEXT_WARNINGS_UNINSTALL_PROPERTY), LogManager.getLogFile()));
                } else {
                    messagePaneUninstall.setContentType(component.getProperty(MESSAGE_CONTENT_TYPE_SUCCESS_UNINSTALL_PROPERTY));
                    messagePaneUninstall.setText(StringUtils.format(component.getProperty(MESSAGE_TEXT_SUCCESS_UNINSTALL_PROPERTY), LogManager.getLogFile()));
                }
            } else {
                messagePaneUninstall.setVisible(false);
            }
            
            final List<Product> products = new LinkedList<Product>();
            
            products.addAll(registry.getProducts(INSTALLED_SUCCESSFULLY));
            products.addAll(registry.getProducts(INSTALLED_WITH_WARNINGS));
            
            messagePaneNetBeans.setContentType(DEFAULT_MESSAGE_NETBEANS_CONTENT_TYPE);
            messagePaneNetBeans.setText("");
            boolean nbInstalled = false;
            for (Product product: products) {
                if (product.getUid().equals("nb-base")) {
                    if (SystemUtils.isWindows()) {
                        messagePaneNetBeans.setText(DEFAULT_MESSAGE_NETBEANS_TEXT_WINDOWS);
                    } else if (SystemUtils.isMacOS()) {
                        messagePaneNetBeans.setText(DEFAULT_MESSAGE_NETBEANS_TEXT_MACOSX);
                    } else {
                        messagePaneNetBeans.setText(DEFAULT_MESSAGE_NETBEANS_TEXT_UNIX);
                    }
                    nbInstalled = true;
                    break;
                }
            }
            // initialize registration components
            List<Product> toRegister = new LinkedList<Product>();
            for (Product product : products) {
                final String uid = product.getUid();
                if (uid.equals("nb-base") || uid.equals("jdk") || uid.equals("glassfish") || uid.equals("sjsas")) {
                    toRegister.add(product);
                }
            }
            boolean registrationEnabled = 
                    nbInstalled            && // if NetBeans is among installed products
                    !toRegister.isEmpty()  && // if anything to register
                    !SystemUtils.isMacOS() && // no support on mac
                    Boolean.getBoolean(ALLOW_SERVICETAG_REGISTRATION_PROPERTY) && //system property is defined
                    (BrowserSupport.isSupported() ||                   // if JDK6 supports browser or
                    SystemUtils.isWindows() ||                         // on windows we can find browser in registry or
                    NbRegistrationAction.getUnixBrowser() != null);    // on unix we can found it in some predefined locations
            
            if (!registrationEnabled) {
                messagePaneRegistration.setVisible(false);
                checkBoxRegistration.setVisible(false);
                spacer.setVisible(false);
                checkBoxRegistration.setSelected(false);
                System.setProperty(ALLOW_SERVICETAG_REGISTRATION_PROPERTY, "" + false);
            } else {
                String productsString = StringUtils.EMPTY_STRING;
                for (Product product : toRegister) {
                    final String uid = product.getUid();
                    String name = StringUtils.EMPTY_STRING;
                    if (uid.equals("nb-base")) {
                        name = DEFAULT_MESSAGE_REGISTRATION_NETBEANS;
                    } else if (uid.equals("jdk")) {
                        name = DEFAULT_MESSAGE_REGISTRATION_JDK;
                    } else if (uid.equals("glassfish")) {
                        name = DEFAULT_MESSAGE_REGISTRATION_GLASSFISH;
                    } else if (uid.equals("sjsas")) {
                        name = DEFAULT_MESSAGE_REGISTRATION_APPSERVER;
                    }
                    if (productsString.equals(StringUtils.EMPTY_STRING)) {
                        productsString = name;
                    } else {
                        productsString = StringUtils.format(DEFAULT_MESSAGE_REGISTRATION_CONCAT, productsString, name);
                    }
                }
                messagePaneRegistration.setContentType(DEFAULT_MESSAGE_REGISTRATION_CONTENT_TYPE);
                messagePaneRegistration.setText(StringUtils.format(DEFAULT_MESSAGE_REGISTRATION_TEXT, productsString));
                checkBoxRegistration.setText(StringUtils.format(DEFAULT_MESSAGE_REGISTRATION_CHECKBOX, productsString));
                // be default - it is checked
                checkBoxRegistration.doClick();
                // do not show message about starting the IDE and plugin manager in case of registration 
                messagePaneNetBeans.setVisible(false);
            }

            products.clear();
            
            products.addAll(registry.getProducts(UNINSTALLED_SUCCESSFULLY));
            products.addAll(registry.getProducts(UNINSTALLED_WITH_WARNINGS));
            
            final List<Product> notCompletelyRemoved = new LinkedList<Product>();
            for (Product product: products) {
                if (!FileUtils.isEmpty(product.getInstallationLocation())) {
                    notCompletelyRemoved.add(product);
                }
            }
            
            if (notCompletelyRemoved.size() > 0) {
                final String text = messagePaneUninstall.getText();
                messagePaneUninstall.setText(text + StringUtils.format(
                        panel.getProperty(MESSAGE_FILES_REMAINING_PROPERTY),
                        StringUtils.asString(notCompletelyRemoved)));
            }
        }
        
        private void initComponents() {
            // messagePaneInstall ///////////////////////////////////////////////////
            messagePaneInstall = new NbiTextPane();
            
            // messagePaneUninstall /////////////////////////////////////////////////
            messagePaneUninstall = new NbiTextPane();
            
            // messagePaneNetBeans ///////////////////////////////////////////////////
            messagePaneNetBeans = new NbiTextPane();

            // messagePaneRegistration
            messagePaneRegistration = new NbiTextPane();

            //checkBoxRegistration
            checkBoxRegistration = new NbiCheckBox();
            checkBoxRegistration.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    System.setProperty(ALLOW_SERVICETAG_REGISTRATION_PROPERTY,
                            "" + checkBoxRegistration.isSelected());
                }
            });

            // spacer
            spacer = new NbiPanel();
            
            // this /////////////////////////////////////////////////////////////////
            add(messagePaneInstall, new GridBagConstraints(
                    0, 0,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.PAGE_START,    // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(messagePaneUninstall, new GridBagConstraints(
                    0, 1,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.PAGE_START,    // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(messagePaneNetBeans, new GridBagConstraints(
                    0, 2,                             // x, y
                    1, 1,                             // width, height
                    1.0, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.PAGE_START,    // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(11, 11, 11, 11),       // padding
                    0, 0));                           // padx, pady - ???
            add(messagePaneRegistration, new GridBagConstraints(
                    0, 3, // x, y
                    1, 1, // width, height
                    1.0, 1.0, // weight-x, weight-y
                    GridBagConstraints.PAGE_START, // anchor
                    GridBagConstraints.HORIZONTAL, // fill
                    new Insets(11, 11, 11, 11), // padding
                    0, 0));                           // padx, pady - ???
            add(checkBoxRegistration, new GridBagConstraints(
                    0, 4, // x, y
                    1, 1, // width, height
                    1.0, 1.0, // weight-x, weight-y
                    GridBagConstraints.PAGE_START, // anchor
                    GridBagConstraints.HORIZONTAL, // fill
                    new Insets(0, 11, 11, 11), // padding
                    0, 0));                           // padx, pady - ???
            add(spacer, new GridBagConstraints(
                    0, 5, // x, y
                    1, 1, // width, height
                    1.0, 10.0, // weight-x, weight-y
                    GridBagConstraints.CENTER, // anchor
                    GridBagConstraints.BOTH, // fill
                    new Insets(0, 11, 0, 11), // padding
                    0, 0));                           // padx, pady - ???
            if (container instanceof SwingFrameContainer) {
                final SwingFrameContainer sfc = (SwingFrameContainer) container;
                sfc.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent event) {
                        SwingUi currentUi = component.getWizardUi().getSwingUi(container);
                        if (currentUi != null) {
                            if (!container.getCancelButton().isEnabled() && // cancel button is disabled
                                    !container.getCancelButton().isVisible() && // no cancel button at this panel
                                    !container.getBackButton().isVisible() && // no back button at this panel
                                    container.getNextButton().isVisible() && // next button is visible
                                    container.getNextButton().isEnabled()) { // and enabled                                                                
                                currentUi.evaluateNextButtonClick();
                                sfc.removeWindowListener(this);
                            }
                        }
                    }
                });
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String MESSAGE_TEXT_SUCCESS_PROPERTY =
            "message.text.success"; // NOI18N
    public static final String MESSAGE_CONTENT_TYPE_SUCCESS_PROPERTY =
            "message.content.type.success"; // NOI18N
    public static final String MESSAGE_TEXT_WARNINGS_PROPERTY =
            "message.text.warnings"; // NOI18N
    public static final String MESSAGE_CONTENT_TYPE_WARNINGS_PROPERTY =
            "message.content.type.warnings"; // NOI18N
    public static final String MESSAGE_TEXT_ERRORS_PROPERTY =
            "message.text.errors"; // NOI18N
    public static final String MESSAGE_CONTENT_TYPE_ERRORS_PROPERTY =
            "message.content.type.errors"; // NOI18N
    public static final String MESSAGE_TEXT_SUCCESS_UNINSTALL_PROPERTY =
            "message.text.success.uninstall"; // NOI18N
    public static final String MESSAGE_CONTENT_TYPE_SUCCESS_UNINSTALL_PROPERTY =
            "message.content.type.success.uninstall"; // NOI18N
    public static final String MESSAGE_TEXT_WARNINGS_UNINSTALL_PROPERTY =
            "message.text.warnings.uninstall"; // NOI18N
    public static final String MESSAGE_CONTENT_TYPE_WARNINGS_UNINSTALL_PROPERTY =
            "message.content.type.warnings.uninstall"; // NOI18N
    public static final String MESSAGE_TEXT_ERRORS_UNINSTALL_PROPERTY =
            "message.text.errors.uninstall"; // NOI18N
    public static final String MESSAGE_CONTENT_TYPE_ERRORS_UNINSTALL_PROPERTY =
            "message.content.type.errors.uninstall"; // NOI18N
    public static final String MESSAGE_FILES_REMAINING_PROPERTY =
            "message.files.remaining"; // NOI18N
    
    public static final String DEFAULT_MESSAGE_TEXT_SUCCESS =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.text.success"); // NOI18N
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE_SUCCESS =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.content.type.success"); // NOI18N
    public static final String DEFAULT_MESSAGE_TEXT_WARNINGS =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.text.warnings"); // NOI18N
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE_WARNINGS =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.content.type.warnings"); // NOI18N
    public static final String DEFAULT_MESSAGE_TEXT_ERRORS =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.text.errors"); // NOI18N
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE_ERRORS =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.content.type.errors"); // NOI18N
    public static final String DEFAULT_MESSAGE_TEXT_SUCCESS_UNINSTALL =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.text.success.uninstall"); // NOI18N
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE_SUCCESS_UNINSTALL =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.content.type.success.uninstall"); // NOI18N
    public static final String DEFAULT_MESSAGE_TEXT_WARNINGS_UNINSTALL =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.text.warnings.uninstall"); // NOI18N
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE_WARNINGS_UNINSTALL =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.content.type.warnings.uninstall"); // NOI18N
    public static final String DEFAULT_MESSAGE_TEXT_ERRORS_UNINSTALL =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.text.errors.uninstall"); // NOI18N
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE_ERRORS_UNINSTALL =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.content.type.errors.uninstall"); // NOI18N
    public static final String DEFAULT_MESSAGE_FILES_REMAINING =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.files.remaining"); // NOI18N
    
    public static final String DEFAULT_MESSAGE_NETBEANS_TEXT_WINDOWS = 
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.netbeans.text.windows"); // NOI18N
    public static final String DEFAULT_MESSAGE_NETBEANS_TEXT_UNIX = 
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.netbeans.text.unix"); // NOI18N
    public static final String DEFAULT_MESSAGE_NETBEANS_TEXT_MACOSX = 
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.netbeans.text.macosx"); // NOI18N
    public static final String DEFAULT_MESSAGE_NETBEANS_CONTENT_TYPE = 
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.netbeans.content.type"); // NOI18N
    public static final String DEFAULT_MESSAGE_REGISTRATION_TEXT =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.registration.text"); // NOI18N
    public static final String DEFAULT_MESSAGE_REGISTRATION_NETBEANS =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.registration.netbeans"); // NOI18N
    public static final String DEFAULT_MESSAGE_REGISTRATION_GLASSFISH =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.registration.glassfish"); // NOI18N
    public static final String DEFAULT_MESSAGE_REGISTRATION_APPSERVER =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.registration.appserver"); // NOI18N
    public static final String DEFAULT_MESSAGE_REGISTRATION_JDK =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.registration.jdk"); // NOI18N
    public static final String DEFAULT_MESSAGE_REGISTRATION_CONCAT =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.registration.concat");//NOI18N
    public static final String DEFAULT_MESSAGE_REGISTRATION_CHECKBOX =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.registration.checkbox"); // NOI18N
    public static final String DEFAULT_MESSAGE_REGISTRATION_CONTENT_TYPE =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.registration.content.type");//NOI18N    
    public static final String ALLOW_SERVICETAG_REGISTRATION_PROPERTY =
            "servicetag.allow.register";

    public static final String DEFAULT_TITLE = ResourceUtils.getString(
            NbPostInstallSummaryPanel.class,
            "NPoISP.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.description"); // NOI18N
    
    public static final String DEFAULT_NEXT_BUTTON_TEXT =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.next.button.text"); // NOI18N
}
