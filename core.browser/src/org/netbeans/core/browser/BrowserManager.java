/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.core.browser;

import java.io.File;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.mozilla.browser.MozillaConfig;
import org.netbeans.core.IDESettings;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.ChangeSupport;

/**
 * Enables/disables embedded browser according to OS, CPU architecture etc.
 * It also attempts to download os-specific binaries at runtime if they're not
 * already available.
 *
 * @author S. Aubrecht
 */
public class BrowserManager {

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final Logger LOG = Logger.getLogger(BrowserManager.class.getName());

    private BrowserManager() {
        initXULRunnerHome();
        //TODO implement and install WindowCreator
        initProfileDir();
    }
    
    public static BrowserManager getDefault() {
        return Holder.theInstance;
    }

    public static final boolean isSupportedPlatform() {
        if( !Platform.platform.is32Bit() ) {
            return false;
        }

        if( Platform.platform == Platform.Unsupported ) {
            return false;
        }

        if( Platform.platform == Platform.Linux ) {
            if( !Platform.checkJavaVersion("1.6.0", null) ) //NOI18N
                return false;
            if( "GTK".equals(UIManager.getLookAndFeel().getID()) && !Platform.checkJavaVersion("1.6.0_14", null) ) //NOI18N
                return false;
        }
        return true;
    }

    public final boolean isEnabled() {
        return IDESettings.getWWWBrowser() instanceof BrowserFactory;
    }

    public void disable() {
        IDESettings.setWWWBrowser( IDESettings.getExternalWWWBrowser() );
        fireChangeEventLater();
    }

    public final boolean isNativeModuleAvailable() {
        File xpcom = InstalledFileLocator.getDefault().locate(Platform.xulRunnerHome+"/javaxpcom.jar", null, false); //NOI18N
        return null != xpcom;
    }

    public void addChangeListener( ChangeListener l ) {
        changeSupport.addChangeListener(l);
        if( !isEnabled() || isNativeModuleAvailable() ) {
            l.stateChanged(new ChangeEvent(this));
        }
    }

    public void removeChangeListener( ChangeListener l ) {
        changeSupport.removeChangeListener(l);
    }

    public JComponent createDownloadNativeModulePanel() {
        return new DownloadPanel(Platform.codeNameBase);
    }

    private void fireChangeEventLater() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                changeSupport.fireChange();
            }
        });
    }
    
    void nativeModuleDownloaded() {
        initXULRunnerHome();
        fireChangeEventLater();
    }

    private void initXULRunnerHome() {
        File xpcom = InstalledFileLocator.getDefault().locate(Platform.xulRunnerHome+"/javaxpcom.jar", null, false); //NOI18N
        if( null != xpcom ) {
            File xulRunnerDir = xpcom.getParentFile();
            LOG.fine("Loooking for XULRunner distribution in: " + xulRunnerDir); //NOI18N
            MozillaConfig.setXULRunnerHome(xulRunnerDir);
        } else {
            LOG.fine("XULRunner distribution not found"); //NOI18N
        }
    }

    private void initProfileDir() {
        File profileDir;
        String userDir = System.getProperty("netbeans.user"); // NOI18N
        if (userDir != null) {
            profileDir = new File(new File(new File (userDir, "var"), "cache"), "mozillaprofilev1"); // NOI18N
        } else {
            profileDir = FileUtil.toFile(FileUtil.getConfigRoot());
            profileDir = new File(profileDir, "mozillaprofilev1"); // NOI18N
        }
        profileDir.mkdirs();
        MozillaConfig.setProfileDir(profileDir);
    }

    private static class Holder {
        private static final BrowserManager theInstance = new BrowserManager();
    }
}
