/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.installer.products.javafxsdk.wizard.panels;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.File;
import java.util.List;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.BrowserUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.applications.JavaUtils.JavaInfo;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.wizard.components.actions.SearchForJavaAction;
import org.netbeans.installer.wizard.components.panels.DestinationPanel;
import org.netbeans.installer.wizard.containers.SwingContainer;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;


/**
 *
 * @author ynov
 */
public class JavaFXSDKPanel extends DestinationPanel {
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public JavaFXSDKPanel() {        

        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);
        
        setProperty(DESTINATION_LABEL_TEXT_PROPERTY,
                DEFAULT_DESTINATION_LABEL_TEXT);
        setProperty(DESTINATION_BUTTON_TEXT_PROPERTY,
                DEFAULT_DESTINATION_BUTTON_TEXT);
        setProperty(WARNING_SDK_CANNOT_BE_INSTALLED_PROPERTY,
                DEFAULT_WARNING_SDK_CANNOT_BE_INSTALLED);
        setProperty(ERROR_JDK_NOT_FOUND_PROPERTY,
                DEFAULT_ERROR_JDK_NOT_FOUND_PROPERTY);
    }

   @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new JavaFXSDKPanelUi(this);
        }
        
        return wizardUi;
    }      
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class JavaFXSDKPanelUi extends DestinationPanelUi {
       protected JavaFXSDKPanel component;

       public JavaFXSDKPanelUi(JavaFXSDKPanel component) {
            super(component);            
            this.component = component;
        }
       
        @Override
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new JavaFXSDKPanelSwingUi(component, container);
            }            
            return super.getSwingUi(container);
        }
    
        public static class JavaFXSDKPanelSwingUi extends DestinationPanelSwingUi {    
            protected JavaFXSDKPanel panel;
            private NbiTextPane statusLabel;

            public JavaFXSDKPanelSwingUi(JavaFXSDKPanel panel, SwingContainer container) {
                super(panel, container);
                this.panel = panel;

                initComponents();
            }
            
            @Override
            protected void initialize() {
                if (!isSuitableJDKInstalled()) {
                    statusLabel.setContentType("text/html");
                    statusLabel.setText(StringUtils.format(component.getProperty(ERROR_JDK_NOT_FOUND_PROPERTY)));
                    statusLabel.addHyperlinkListener(BrowserUtils.createHyperlinkListener());
                } else {
                    statusLabel.clearText();
                    statusLabel.setVisible(false);
                }
                super.initialize();
            }


            @Override
            protected String getWarningMessage() {
                String msg = super.getWarningMessage();

                if(!isSuitableJDKInstalled()) {
                    if(msg == null) {
                        msg = StringUtils.format(component.getProperty(WARNING_SDK_CANNOT_BE_INSTALLED_PROPERTY));
                    }
                }
                return msg;
            }


           /* @Override
            protected String validateInput() {
                String errorMessage = super.validateInput();

                if (errorMessage == null) {
                    if(!isSuitableJDKInstalled()) {
                           errorMessage = StringUtils.format(component.getProperty(ERROR_SDK_CANNOT_BE_INSTALLED_PROPERTY));
                     }

                }

                if (errorMessage != null) {
                    return errorMessage;
                }

                return null;
            }*/


            // private //////////////////////////////////////////////////////////////////
            private void initComponents() {
                // statusLabel //////////////////////////////////////////////////////////
                 statusLabel = new NbiTextPane();

                 add(statusLabel, new GridBagConstraints(
                        0, 2,                             // x, y
                        2, 1,                             // width, height
                        1.0, 0.0,                         // weight-x, weight-y
                        GridBagConstraints.LINE_START,    // anchor
                        GridBagConstraints.HORIZONTAL,    // fill
                        new Insets(11, 11, 0, 11),         // padding
                        0, 0));
            }



            private boolean isSuitableJDKInstalled() {
               final Product product = (Product) component.
                    getWizard().
                    getContext().
                    get(Product.class);

                LogManager.log("... SDK Panel: product arch = " + product.getPlatforms().get(0).getHardwareArch());
                LogManager.log("... SDK Panel: current platform arch = " + SystemUtils.getCurrentPlatform().getHardwareArch());

                List<File> javaLocations = SearchForJavaAction.getJavaLocations();
                for(File javaLocation : javaLocations) {
                    final JavaInfo javaInfo = JavaUtils.getInfo(javaLocation);
                    final Version javaVersion = javaInfo.getVersion();
                    final String javaArch = javaInfo.getArch();
                    final boolean isJDK = JavaUtils.isJdk(javaLocation);
                    final String platformArch = product.getPlatforms().get(0).getHardwareArch();
                    LogManager.log("... SDK Panel: javaLocation: " + javaLocation.getAbsolutePath());                    
                    LogManager.log("... SDK Panel: javaInfo arch: " + javaInfo.getArch());                    
                    if(isJDK &&
                           //platformArch == null in case if Platform is WINDOWS and not null if x86/x64 is set
                         (platformArch == null || javaArch.matches(platformArch)) &&
                          javaVersion.newerOrEquals(Version.getVersion("1.6.0.24.0")) ) {
                        LogManager.log("... This java is suitable for JavaFX SDK");
                        product.setStatus(Status.TO_BE_INSTALLED);
                        return true;
                    }
                }
                LogManager.log("... No suitable java is installed for SDK");
                product.setStatus(Status.NOT_INSTALLED);
                return false;
            }
        }          
    }


    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants

    public static final String WARNING_SDK_CANNOT_BE_INSTALLED_PROPERTY =
            "warning.sdk.cannot.be.installed"; // NOI18N
    public static final String ERROR_JDK_NOT_FOUND_PROPERTY =
            "error.jdk.not.found"; // NOI18N

    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(JavaFXSDKPanel.class,
            "JFXP.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(JavaFXSDKPanel.class,
            "JFXP.description"); // NOI18N
    public static final String DEFAULT_DESTINATION_LABEL_TEXT =
            ResourceUtils.getString(JavaFXSDKPanel.class,
            "JFXP.destination.label.text"); // NOI18N
    public static final String DEFAULT_DESTINATION_BUTTON_TEXT =
            ResourceUtils.getString(JavaFXSDKPanel.class,
            "JFXP.destination.button.text"); // NOI18N
    public static final String DEFAULT_WARNING_SDK_CANNOT_BE_INSTALLED =
            ResourceUtils.getString(JavaFXSDKPanel.class,
            "JFXP.warning.sdk.cannot.be.installed"); // NOI18N
        public static final String DEFAULT_ERROR_JDK_NOT_FOUND_PROPERTY =
            ResourceUtils.getString(JavaFXSDKPanel.class,
            "JFXP.error.jdk.not.found"); // NOI18N
}
