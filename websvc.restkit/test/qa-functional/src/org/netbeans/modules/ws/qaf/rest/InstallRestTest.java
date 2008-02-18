/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
import java.io.IOException;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.PluginsOperator;
import org.netbeans.jemmy.operators.JButtonOperator;

/**
 * Test (un)installation of the RESTful Web Services plugin from Update Center
 * 
 * @author lukas
 */
public class InstallRestTest extends JellyTestCase {

    static final String FLAG = ".rest.plugin.installed"; //NOI18N
    static final String REST_KIT_LABEL = "RESTful Web Services"; //NOI18N
    private File flagF;

    public InstallRestTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        if (System.getProperty("xtest.tmpdir") != null) { //NOI18N
            //XTest execution
            flagF = new File(System.getProperty("xtest.tmpdir"), FLAG); //NOI18N
        } else {
            //Internal-execution
            flagF = new File(System.getProperty("java.io.tmpdir"), FLAG); //NOI18N
        }
    }
    
    /**
     * Install RESTful plugin iff it is not already installed
     * 
     * @throws java.io.IOException
     */
    public void testInstallRest() throws IOException {
        try {
            Class.forName("org.netbeans.modules.websvc.rest.spi.RestSupport");
        } catch (ClassNotFoundException cnfe) {
            flagF.createNewFile();
            installPlugin();
        }
    }

    /**
     * Install RESTful plugin iff it has been installed by the test.
     * One can bypass this constraint by setting system property:
     * "plugins.rest.forceUninstall=true"
     * 
     * @throws java.io.IOException
     */
    public void testUnInstallRest() {
        if (flagF.exists() && flagF.isFile()) { //NOI18N
            flagF.delete();
            uninstallPlugin();
        } else if (Boolean.getBoolean("plugins.rest.forceUninstall")) { //NOI18N
            uninstallPlugin();
        }
    }

    private void installPlugin() throws IOException {
        PluginsOperator po = PluginsOperator.invoke();
        po.install(REST_KIT_LABEL);
    }

    private void uninstallPlugin() {
        PluginsOperator po = PluginsOperator.invoke();
        po.selectInstalled();
        po.selectPlugins(new String[]{REST_KIT_LABEL});
        po.uninstall();
        // Uninstall
        String uninstallInDialogLabel = Bundle.getStringTrimmed("org.netbeans.modules.autoupdate.ui.wizards.Bundle", "UninstallUnitWizardModel_Buttons_Uninstall");
        new JButtonOperator(po.installer(), uninstallInDialogLabel).push();
        po.installer().finish();
    }

    public static void main(String... args) {
        TestRunner.run(InstallRestTest.class);
    }
}
