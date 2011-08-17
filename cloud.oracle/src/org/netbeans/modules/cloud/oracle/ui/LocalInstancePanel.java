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
import java.io.File;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.ui.wizard.ServerLocationVisual;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public class LocalInstancePanel implements WizardDescriptor.Panel<WizardDescriptor>, ChangeListener {

    public static final String LOCAL_SERVER = "localServer";

    private static final Version MINIMAL_WL_VERSION =
            Version.fromJsr277OrDottedNotationWithFallback("10.3.6"); // NOI18N

    private final ChangeSupport support = new ChangeSupport(this);

    private LocalInstanceComponent component;
    
    private WizardDescriptor wd;

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new LocalInstanceComponent();
            component.addChangeListener(this);
            component.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, OracleWizardIterator.getPanelContentData());
            component.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(2));
        }
        return component;
    }

    @Override
    public boolean isValid() {
        if (component != null && wd != null) {
            if (!checkServerRoot()) {
                return false;
            }
        }
        setErrorMessage("");
        return true;
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        wd = settings;
    }
    
    @Override
    public void storeSettings(WizardDescriptor settings) {
        if (component != null) {
            settings.putProperty(LOCAL_SERVER, component.getLocalServerDirectory());
        }
    }

    @Override
    public HelpCtx getHelp() {
        return null;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        support.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        support.removeChangeListener(l);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        support.fireChange();
    }
    
    // TODO bit duplicated from ServerLocationVisual of j2ee.weblogic9
    private boolean checkServerRoot() {
        // check for the validity of the entered installation directory
        // if it's invalid, return false
        String location = component.getLocalServerDirectory();

        if (location == null || location.trim().length() < 1) {
            return true;
        }

        File serverRoot = FileUtil.normalizeFile(new File(location.trim()));

        serverRoot = ServerLocationVisual.findServerLocation(serverRoot , wd);
        if (serverRoot == null) {
            return false;
        }
        location = serverRoot.getPath();

        Version version = WLPluginProperties.getServerVersion(serverRoot);

        if (!isSupportedVersion(version)) {
            String msg = NbBundle.getMessage(OracleWizardPanel.class, "ERR_INVALID_SERVER_VERSION");
            wd.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, decorateMessage(msg));
            return false;
        }

        if (!WLPluginProperties.isGoodServerLocation(serverRoot)) {
            String msg = NbBundle.getMessage(OracleWizardPanel.class, "ERR_INVALID_SERVER_ROOT");
            wd.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, decorateMessage(msg));
            return false;
        }

        WLPluginProperties.setLastServerRoot(location);
        return true;
    }
    
    public static boolean isSupportedVersion(Version version) {
        return version != null && version.isAboveOrEqual(MINIMAL_WL_VERSION);
    }

    private void setErrorMessage(String message) {
        wd.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
    }

    private static String decorateMessage(String message) {
        return message == null
            ? null
            : "<html>" + message.replaceAll("<",  "&lt;").replaceAll(">",  "&gt;") + "</html>"; // NIO18N
    }
}
