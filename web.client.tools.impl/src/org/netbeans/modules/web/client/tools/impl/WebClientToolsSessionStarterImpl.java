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
package org.netbeans.modules.web.client.tools.impl;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.client.javascript.debugger.api.NbJSDebugger;
import org.netbeans.modules.web.client.tools.api.WebClientToolsProjectUtils;
import org.netbeans.modules.web.client.tools.api.WebClientToolsSessionException;
import org.netbeans.modules.web.client.tools.spi.WebClientToolsSessionStarter;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser.Factory;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 * This is the implementation of web client tools service.
 * 
 * @author Sandip V. Chitale <sandipchitale@netbeans.org>
 */
public class WebClientToolsSessionStarterImpl implements WebClientToolsSessionStarter {

    public void startSession(final URI uri, final Factory browser, final Lookup lookup) throws WebClientToolsSessionException {
        Runnable startSession = new Runnable() {

            public void run() {
                try {
                    startSessionImpl(uri, browser, lookup);
                } catch (WebClientToolsSessionException ex) {
                    Log.getLogger().log(Level.INFO, "Unexpected exception while starting debugger", ex);
                }
            }
        };

        RequestProcessor.getDefault().post(startSession);
    }

    private void startSessionImpl(URI uri, Factory browser, Lookup lookup) throws WebClientToolsSessionException {
        boolean ieSupported = WebClientToolsProjectUtils.isInternetExplorerSupported();
        boolean ffSupported = WebClientToolsProjectUtils.isFirefoxSupported();
        Preferences globalPrefs = NbPreferences.forModule(DebugConstants.class);

        if (!ieSupported && !ffSupported) {
            displayNoBrowserDialog(globalPrefs);
            displayInBrowser(uri);
            return;
        }

        browser = getSupportedBrowser(browser, ffSupported, ieSupported);
        NbJSDebugger.startDebugging(uri, browser, lookup);
    }

    private void displayNoBrowserDialog(final Preferences globalPrefs) {
        if (globalPrefs.getBoolean(DebugConstants.DISPLAY_NOBROWSER, true)) {
            JCheckBox doNotShowAgain = new JCheckBox();
            Mnemonics.setLocalizedText(doNotShowAgain, NbBundle.getMessage(WebClientToolsSessionStarterImpl.class, "DO_NOT_SHOW_AGAIN"));
            doNotShowAgain.setSelected(false);
            String dialogText = NbBundle.getMessage(WebClientToolsSessionStarterImpl.class, "NO_BROWSER_TEXT");
            JLabel text = new JLabel(dialogText);
            text.setOpaque(false);

            NotifyDescriptor nd = new NotifyDescriptor.Message(
                    new Object[]{text, doNotShowAgain});
            nd.setMessageType(NotifyDescriptor.ERROR_MESSAGE);

            DialogDisplayer.getDefault().notify(nd);
            if (doNotShowAgain.isSelected()) {
                globalPrefs.putBoolean(DebugConstants.DISPLAY_NOBROWSER, false);
                try {
                    globalPrefs.sync();
                } catch (BackingStoreException ex) {
                    Log.getLogger().log(Level.WARNING, "Could not save preference", ex);
                }
            }
        }
    }

    private void displayInBrowser(URI uri) {
        try {
            URLDisplayer.getDefault().showURL(uri.toURL());
        } catch (MalformedURLException ex) {
            Log.getLogger().log(Level.SEVERE, "Could not launch browser", ex);
        }
    }

    /**
     * Ensures that the selected browser type is valid.  Since values are only validated
     * when the user edits project Properties, this is required before debugging starts.
     * 
     * @param baseBrowser
     * @param ffSupported
     * @param ieSupported
     * @return
     */
    private Factory getSupportedBrowser(Factory baseBrowser, boolean ffSupported, boolean ieSupported) {
        if (ffSupported && ieSupported) {
            return baseBrowser;
        }

        if (ieSupported) {
            return WebClientToolsProjectUtils.getInternetExplorerBrowser();
        } else {
            return WebClientToolsProjectUtils.getFirefoxBrowser();
        }
    }
}
