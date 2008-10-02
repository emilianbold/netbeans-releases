/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.web.client.tools.api;

import java.util.Collection;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/**
 *
 * Utility class that allows access to a specific browser, regardless of the
 * default browser settings in NetBeans.  This is intended to be used for invoking
 * the JavaScript debugger.
 * 
 * @author Quy Nguyen <quynguyen@netbeans.org>
 */
public final class WebClientToolsProjectUtils {
    // Do not show again state
    static final String DIALOG_DISPLAY_CONFIG = "dialogShowDebugPanel"; // NOI18N
    // server/client selection state
    static final String DIALOG_CLIENT_DEBUG = "dialogClientDebug"; // NOI18N
    // browser selection state
    static final String DIALOG_BROWSER = "dialogDebugConfigBrowser"; // NOI18N

    static final String CLIENT_DEBUG_PROP = "clientdebug"; // NOI18N
    static final String SERVER_DEBUG_PROP = "serverdebug"; // NOI18N
    
    private static final boolean CLIENT_DEBUG_DEFAULT = false;
    private static final boolean SERVER_DEBUG_DEFAULT = true;
    
    public static enum Browser {
        FIREFOX,
        INTERNET_EXPLORER;
    }

    static final Browser BROWSER_DEFAULT = Browser.FIREFOX;

    /**
     *
     * Displays the Debug Project dialog that allows the user to select server/client
     * debugging.  User selection in the dialog saves the project's preferences as well
     * as the default value for subsequent projects.
     *
     * @param project the project to debug
     * @return true if the user pressed the Debug button, false if the user cancels
     */
    public static boolean showDebugDialog(Project project) {
        Preferences globalPrefs = NbPreferences.forModule(WebClientToolsProjectUtils.class);
        boolean showDialog = globalPrefs.getBoolean(DIALOG_DISPLAY_CONFIG, true);
        boolean serverDebugEnabled = WebClientToolsProjectUtils.getServerDebugProperty(project);

        if (!showDialog || !serverDebugEnabled) {
            return true;
        } else {
            String projectName = ProjectUtils.getInformation(project).getDisplayName();
            String dialogTitle = NbBundle.getMessage(WebClientToolsProjectUtils.class, "DebugProjectPanel_DialogTitle", projectName);

            JButton debugButton = new JButton();
            Mnemonics.setLocalizedText(debugButton, NbBundle.getMessage(WebClientToolsProjectUtils.class, "DebugProjectPanel_OkButton"));
            debugButton.getAccessibleContext().setAccessibleDescription(
                    NbBundle.getMessage(WebClientToolsProjectUtils.class, "DebugProjectPanel_OkButton.accessibleDescription"));

            Object[] dialogButtons = new Object[] { debugButton, DialogDescriptor.CANCEL_OPTION };
            DebugProjectPanel panel = new DebugProjectPanel(project);

            DialogDescriptor dd = new DialogDescriptor(
                    panel,
                    dialogTitle,
                    true,
                    dialogButtons,
                    debugButton,
                    DialogDescriptor.BOTTOM_ALIGN,
                    null,
                    panel.getPanelCloseHandler(debugButton));

            dd.setClosingOptions(null);
            return DialogDisplayer.getDefault().notify(dd) == debugButton;
        }
    }

    public static Browser getDefaultBrowser() {
        Preferences globalPrefs = NbPreferences.forModule(WebClientToolsProjectUtils.class);
        String browserName = globalPrefs.get(DIALOG_BROWSER, null);

        if (browserName != null) {
            try {
                return Browser.valueOf(browserName);
            } catch (IllegalArgumentException ex) {
                // value is somehow invalid
                }
        }

        if (isFirefoxSupported()) {
            return Browser.FIREFOX;
        } else if (isInternetExplorerSupported()) {
            return Browser.INTERNET_EXPLORER;
        } else {
            return BROWSER_DEFAULT;
        }
    }

    public static boolean getClientDebugDefault() {
        Preferences globalPrefs = NbPreferences.forModule(WebClientToolsProjectUtils.class);
        return globalPrefs.getBoolean(DIALOG_CLIENT_DEBUG, CLIENT_DEBUG_DEFAULT);
    }

    public static boolean getServerDebugDefault() {
        return SERVER_DEBUG_DEFAULT;
    }

    public static HtmlBrowser.Factory getFirefoxBrowser() {
        return findBrowser("org.netbeans.modules.extbrowser.FirefoxBrowser"); // NOI18N
    }
    
    public static HtmlBrowser.Factory getInternetExplorerBrowser() {
        return findBrowser("org.netbeans.modules.extbrowser.IExplorerBrowser"); // NOI18N
    }
    
    public static boolean getClientDebugProperty(Project project) {
        return getProjectProperty(project, CLIENT_DEBUG_PROP, getClientDebugDefault());
    }

    public static boolean getServerDebugProperty(Project project) {
        // If no browser is available, always select server-side debugging;
        // client-side debugging is not automatically de-selected to allow it
        // to display a relevant error message
        if (supportedBrowsersAvailable()) {
            return getProjectProperty(project, SERVER_DEBUG_PROP, SERVER_DEBUG_DEFAULT);
        } else {
            return true;
        }
    }
    
    public static boolean isDebugPropertySet(Project project) {
        return isPropertySet(project, CLIENT_DEBUG_PROP);
    }

    static boolean isPropertySet(Project project, String propKey) {
        Preferences prefs = ProjectUtils.getPreferences(project, WebClientToolsProjectUtils.class, false);
        assert prefs != null;

        return prefs.get(propKey, null) != null;
    }

    static Preferences getPreferencesForProject(Project project) {
        Preferences prefs = ProjectUtils.getPreferences(project, WebClientToolsProjectUtils.class, false);
        assert prefs != null;
        
        return prefs;
    }

    public static boolean isFirefox(Project project) {
        return getProjectProperty(project, Browser.FIREFOX.name(), (getDefaultBrowser() == Browser.FIREFOX));
    }

    public static boolean isInternetExplorer(Project project) {
        return getProjectProperty(project, Browser.INTERNET_EXPLORER.name(), (getDefaultBrowser() == Browser.INTERNET_EXPLORER));
    }
    
    private static boolean getProjectProperty(Project project, String propKey, boolean def) {
        Preferences prefs = ProjectUtils.getPreferences(project, WebClientToolsProjectUtils.class, false);
        assert prefs != null;
        
        return prefs.getBoolean(propKey, def);
    }

    public static void setProjectProperties(final Project project, final boolean serverDebug, final boolean clientDebug, final Browser browser) {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {

                public Boolean run() throws BackingStoreException {
                    Preferences prefs = ProjectUtils.getPreferences(project, WebClientToolsProjectUtils.class, false);
                    assert prefs != null;

                    boolean isFirefox = (browser == Browser.FIREFOX);
                    boolean isInternetExplorer = (browser == Browser.INTERNET_EXPLORER);
                    
                    // always write properties to allow isDebugPropertySet() method
                    prefs.putBoolean(SERVER_DEBUG_PROP, serverDebug);
                    prefs.putBoolean(CLIENT_DEBUG_PROP, clientDebug);
                    prefs.putBoolean(Browser.FIREFOX.name(), isFirefox);
                    prefs.putBoolean(Browser.INTERNET_EXPLORER.name(), isInternetExplorer);
                    
                    prefs.sync();

                    return Boolean.TRUE;
                }
            });
        } catch (MutexException ex) {
            Log.getLogger().log(Level.SEVERE, "Unable to set javascript debugger project properties", ex);
        }
    }
    
    private static HtmlBrowser.Factory findBrowser(String browserClass) {
        Collection<? extends HtmlBrowser.Factory> htmlBrowserFactories = Lookup.getDefault().lookupAll(HtmlBrowser.Factory.class);
        for (HtmlBrowser.Factory factory : htmlBrowserFactories) {
            if (factory.getClass().getName().equals(browserClass)) {
                return factory;
            }
        }
        return null;
    }
    
    public static boolean isFirefoxSupported() {
        return getFirefoxBrowser() != null;
    }
    
    public static boolean isInternetExplorerSupported() {
        return Utilities.isWindows() && getInternetExplorerBrowser() != null;
    }
    
    /**
     *  Checks if any supported browsers (Firefox, Internet Explorer on Windows) are
     *  configured in the IDE
     * 
     * @return true if some supported browser is configured
     */
    public static boolean supportedBrowsersAvailable() {
        return isInternetExplorerSupported() || isFirefoxSupported();
    }
}
