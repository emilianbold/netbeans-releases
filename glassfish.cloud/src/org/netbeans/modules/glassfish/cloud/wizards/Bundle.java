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

/**
 * GlassFish Cloud Bundle Keys.
 * <p>
 * Constants to access bundle values.
 * <p/>
 */
public class Bundle {
    
    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes - cloud wizard panel                                  //
    ////////////////////////////////////////////////////////////////////////////

    /** Add Cloud panel name property prefix
     *  (GlassFish.cloud.wizard.panel.&lt;index&gt;.name
     *  before &lt;index&gt;). */
    private static final String ADD_CLOUD_WIZARD_PANEL_NAME_PREFIX
            = "GlassFish.cloud.wizard.panel.";

    /** Add Cloud panel name property suffix
     *  (GlassFish.cloud.wizard.panel.&lt;index&gt;.name
     *  after &lt;index&gt;). */
    private static final String ADD_CLOUD_WIZARD_PANEL_NAME_SUFFIX = ".name";

    /** Cloud panel validation failed. */
    static final String CLOUD_PANEL_VALIDATION_FAILED
            = "GlassFishCloudWizardCpasPanel.validation.failed";

    /** Cloud panel component was not initialized. */
    static final String CLOUD_PANEL_ERROR_COMPONENT_UNINITIALIZED
            = "GlassFishCloudWizardCpasPanel.error.component.uninitialized";

    /** Cloud panel validation error: Display name name is empty. */
    static final String CLOUD_PANEL_ERROR_DISPLAY_NAME_EMPTY
            = "GlassFishCloudWizardCpasPanel.error.displayName.empty";

    /** Cloud panel validation error: Display name is duplicated. */
    static final String CLOUD_PANEL_ERROR_DISPLAY_NAME_DUPLICATED
            = "GlassFishCloudWizardCpasPanel.error.displayName.duplicated";

    /** Cloud panel validation error: Host name is empty. */
    static final String CLOUD_PANEL_ERROR_HOST_EMPTY
            = "GlassFishCloudWizardCpasPanel.error.host.empty";

    /** Cloud panel validation error: Port value is empty. */
    static final String CLOUD_PANEL_ERROR_PORT_EMPTY
            = "GlassFishCloudWizardCpasPanel.error.port.empty";

    /** Cloud panel validation error: Local server location value is empty. */
    static final String CLOUD_PANEL_ERROR_LOCAL_SERVER_EMPTY
            = "GlassFishCloudWizardCpasPanel.error.localServer.empty";

    /** Cloud panel validation error: Local server location does not exist. */
    static final String CLOUD_PANEL_ERROR_LOCAL_SERVER_NOT_EXISTS
            = "GlassFishCloudWizardCpasPanel.error.localServer.notExists";

    /** Cloud panel validation error: Local server version unknown. */
    static final String CLOUD_PANEL_ERROR_LOCAL_SERVER_UNKNOWN
            = "GlassFishCloudWizardCpasPanel.error.localServer.unknown";

    /** Cloud panel validation error: Local server version too low. */
    static final String CLOUD_PANEL_ERROR_LOCAL_SERVER_LOW
            = "GlassFishCloudWizardCpasPanel.error.localServer.low";

    /** Cloud panel validation error: Port value is not a number. */
    static final String CLOUD_PANEL_ERROR_PORT_FORMAT
            = "GlassFishCloudWizardCpasPanel.error.port.format";

    /** Cloud panel file browser header. */
    static final String CLOUD_PANEL_BROWSER_FILE_HEADER
            = "GlassFishCloudWizardCpasPanel.browser.file.header";

    /** Cloud panel file browser choose button. */
    static final String CLOUD_PANEL_BROWSER_FILE_CHOOSE
            = "GlassFishCloudWizardCpasPanel.browser.file.choose";

    /** Cloud panel file browser directory type. */
    static final String CLOUD_PANEL_BROWSER_FILE_DIR_TYPE
            = "GlassFishCloudWizardCpasPanel.browser.file.dirType";

    /** Cloud panel GlassFish version field prefix. */
    static final String CLOUD_PANEL_LOCAL_SERVER_VERSION_PREFIX
            = "GlassFishCloudWizardCpasPanel.localServer.version.prefix";

    /** Add Cloud wizard name property. */
    static final String ADD_CLOUD_WIZARD_DISPLAY_NAME
            = "GlassFish.cloud.wizard.displayName";

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes - cloud wizard actions                                //
    ////////////////////////////////////////////////////////////////////////////

    /** Cloud instance pop up menu: remove action display name. */
    static final String CLOUD_ACTION_REMOVE_NAME
            = "GlassFishCloudActionRemoveInstance.name";

    /** Cloud instance pop up menu: properties action display name. */
    static final String CLOUD_ACTION_PROPERTIES_NAME
            = "GlassFishCloudActionProperties.name";

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes - user account wizard panel                           //
    ////////////////////////////////////////////////////////////////////////////

    /** Add Cloud panel name property prefix
     *  (GlassFish.cloud.wizard.panel.&lt;index&gt;.name
     *  before &lt;index&gt;). */
    private static final String ADD_ACCOUNT_WIZARD_PANEL_NAME_PREFIX
            = "GlassFish.account.wizard.panel.";

    /** Add Cloud panel name property suffix
     *  (GlassFish.cloud.wizard.panel.&lt;index&gt;.name
     *  after &lt;index&gt;). */
    private static final String ADD_ACCOUNT_WIZARD_PANEL_NAME_SUFFIX = ".name";

    /** Add User Account wizard name property. */
    static final String ADD_ACCOUNT_WIZARD_DISPLAY_NAME
            = "GlassFish.account.wizard.displayName";

    /** User account panel validation failed. */
    static final String USER_PANEL_VALIDATION_FAILED
            = "GlassFishAcocuntWizardUserPanel.validation.failed";

    /** User account panel component was not initialized. */
    static final String USER_PANEL_ERROR_COMPONENT_UNINITIALIZED
            = "GlassFishAcocuntWizardUserPanel.error.component.uninitialized";
    
    /** Cloud panel validation error: Display name name is empty. */
    static final String USER_PANEL_ERROR_DISPLAY_NAME_EMPTY
            = "GlassFishAcocuntWizardUserPanel.error.displayName.empty";

    /** Cloud panel validation error: Display name is duplicated. */
    static final String USER_PANEL_ERROR_DISPLAY_NAME_DUPLICATED
            = "GlassFishAcocuntWizardUserPanel.error.displayName.duplicated";

    /** User account panel validation error: Account is empty. */
    static final String USER_PANEL_ERROR_ACCOUNT_EMPTY
            = "GlassFishAcocuntWizardUserPanel.error.account.empty";

    /** User account panel validation error: User name is empty. */
    static final String USER_PANEL_ERROR_USER_NAME_EMPTY
            = "GlassFishAcocuntWizardUserPanel.error.user.name.empty";

    /** User account panel validation error: User password is empty. */
    static final String USER_PANEL_ERROR_USER_PASSWORD_EMPTY
            = "GlassFishAcocuntWizardUserPanel.error.user.password.empty";

    /** User account panel validation error: GlassFish cloud not selected. */
    static final String USER_PANEL_ERROR_CLOUD_EMPTY
            = "GlassFishAcocuntWizardUserPanel.error.cloud.empty";

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes - cloud wizard actions                                //
    ////////////////////////////////////////////////////////////////////////////

    /** User account instance pop up menu: remove action display name. */
    static final String USER_ACTION_REMOVE_NAME
            = "GlassFishAccountActionRemoveInstance.name";

    /** User account instance pop up menu: properties action display name. */
    static final String USER_ACTION_PROPERTIES_NAME
            = "GlassFishAccountActionProperties.name";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Build the display name of GlassFish cloud wizard panel with given index.
     * <p/>
     * @param index Index of panel display name.
     */
    public static String addCloudWizardName(int index) {
        StringBuilder sb = new StringBuilder(
                ADD_CLOUD_WIZARD_PANEL_NAME_PREFIX.length()
                + ADD_CLOUD_WIZARD_PANEL_NAME_SUFFIX.length() + 2);
        sb.append(ADD_CLOUD_WIZARD_PANEL_NAME_PREFIX);
        sb.append(index);
        sb.append(ADD_CLOUD_WIZARD_PANEL_NAME_SUFFIX);
        return sb.toString();
    }

    /**
     * Build the display name of GlassFish cloud wizard panel with given index.
     * <p/>
     * @param index Index of panel display name.
     */
    public static String addAccountWizardName(int index) {
        StringBuilder sb = new StringBuilder(
                ADD_ACCOUNT_WIZARD_PANEL_NAME_PREFIX.length()
                + ADD_ACCOUNT_WIZARD_PANEL_NAME_SUFFIX.length() + 2);
        sb.append(ADD_ACCOUNT_WIZARD_PANEL_NAME_PREFIX);
        sb.append(index);
        sb.append(ADD_ACCOUNT_WIZARD_PANEL_NAME_SUFFIX);
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Disable instantiating of this class.
     */
    private Bundle() {
    }

}
