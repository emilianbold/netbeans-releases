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
package org.netbeans.modules.ws.qaf.rest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.PluginsOperator;
import org.netbeans.jemmy.operators.JButtonOperator;

/**
 * Test uninstallation of plugins
 * 
 * @author lukas
 */
public class UnInstallPluginsTest extends JellyTestCase {

    private File flagF;
    private File flagF2;

    public UnInstallPluginsTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        if (System.getProperty("xtest.tmpdir") != null) { //NOI18N
            //XTest execution
            flagF = new File(System.getProperty("xtest.tmpdir"), InstallPluginsTest.REST_FLAG); //NOI18N
            flagF2 = new File(System.getProperty("xtest.tmpdir"), InstallPluginsTest.JMAKI_FLAG); //NOI18N
        } else {
            //Internal-execution
            flagF = new File(System.getProperty("java.io.tmpdir"), InstallPluginsTest.REST_FLAG); //NOI18N
            flagF2 = new File(System.getProperty("java.io.tmpdir"), InstallPluginsTest.JMAKI_FLAG); //NOI18N
        }
    }

    /**
     * UnInstall plugins iff they were installed by tests.
     * One can bypass this constraint by using following system properties:
     * "plugins.rest.forceUninstall=true"
     * "plugins.jmaki.forceUninstall=true"
     * 
     * @throws java.io.IOException
     */
    public void testUnInstallPlugins() {
        List<String> toUninstall = new ArrayList<String>();
        boolean hadFlag = false;
        if (flagF.exists() && flagF.isFile()) {
            flagF.delete();
            hadFlag = true;
            toUninstall.add(InstallPluginsTest.REST_KIT_LABEL);
        } else if (Boolean.getBoolean("plugins.rest.forceUninstall")) { //NOI18N
            hadFlag = true;
            toUninstall.add(InstallPluginsTest.REST_KIT_LABEL);
        }
        if (!Boolean.getBoolean("plugins.jmaki.skip")) { //NOI18N
            if (flagF2.exists() && flagF2.isFile()) {
                flagF2.delete();
                hadFlag = true;
                toUninstall.add(InstallPluginsTest.JMAKI_KIT_LABEL);
            } else if (Boolean.getBoolean("plugins.jmaki.forceUninstall")) { //NOI18N
            hadFlag = true;
                toUninstall.add(InstallPluginsTest.JMAKI_KIT_LABEL);
            }
        }
        if (hadFlag) {
            fail(toUninstall.toString() + " is/are already uninstalled"); //NOI18N
        }
        if (!toUninstall.isEmpty()) {
            uninstallPlugins(toUninstall.toArray(new String[toUninstall.size()]));
        }
    }

    private void uninstallPlugins(String[] kits) {
        PluginsOperator po = PluginsOperator.invoke();
        po.selectInstalled();
        po.selectPlugins(kits);
        po.uninstall();
        // Uninstall
        String uninstallInDialogLabel = Bundle.getStringTrimmed("org.netbeans.modules.autoupdate.ui.wizards.Bundle", "UninstallUnitWizardModel_Buttons_Uninstall");
        new JButtonOperator(po.installer(), uninstallInDialogLabel).push();
        po.installer().finish();
    }

    public static void main(String... args) {
        TestRunner.run(UnInstallPluginsTest.class);
    }
}
