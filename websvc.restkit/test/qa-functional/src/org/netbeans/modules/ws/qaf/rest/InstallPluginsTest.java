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
import javax.swing.JDialog;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.PluginsOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.junit.NbTestSuite;

/**
 * Test installation of plugins
 * 
 * @author lukas
 */
public class InstallPluginsTest extends JellyTestCase {

    static final String REST_FLAG = ".rest.plugin.installed"; //NOI18N
    static final String REST_KIT_LABEL = "RESTful Web Services"; //NOI18N
    static final String JMAKI_FLAG = ".jmaki.plugin.installed"; //NOI18N
    static final String JMAKI_KIT_LABEL = "jMaki Ajax support"; //NOI18N
    private File flagF;
    private File flagF2;
    
    public InstallPluginsTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        if (System.getProperty("xtest.tmpdir") != null) { //NOI18N
            //XTest execution
            flagF = new File(System.getProperty("xtest.tmpdir"), REST_FLAG); //NOI18N
            flagF2 = new File(System.getProperty("xtest.tmpdir"), JMAKI_FLAG); //NOI18N
        } else {
            //Internal-execution
            flagF = new File(System.getProperty("java.io.tmpdir"), REST_FLAG); //NOI18N
            flagF2 = new File(System.getProperty("java.io.tmpdir"), JMAKI_FLAG); //NOI18N
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
            fail(REST_KIT_LABEL + " is already installed.");
        } catch (ClassNotFoundException cnfe) {
            flagF.createNewFile();
            PluginsOperator po = PluginsOperator.invoke();
            po.install(REST_KIT_LABEL);
        }
    }

    /**
     * Install jMaki plugin iff it is not already installed
     * 
     * <b>Important:</b> Runs only if plugins.jmaki.skip=false or is not set at all
     * 
     * @throws java.io.IOException
     */
    public void testInstallJMaki() throws IOException {
        if (Boolean.getBoolean("plugins.jmaki.skip")) { //NOI18N
            fail("plugins.jmaki.skip was set true, skipping the test..."); //NOI18N
        }
        try {
            Class.forName("org.netbeans.modules.sun.jmaki.Installer"); //NOI18N
            fail(JMAKI_KIT_LABEL + " is already installed.");
        } catch (ClassNotFoundException cnfe) {
            flagF2.createNewFile();
            installPlugin();
        }
    }

    private void installPlugin() throws IOException {
        assertNotNull("plugins.jmaki.nbm not set", System.getProperty("plugins.jmaki.nbm")); //NOI18N
        File jmakiNbm = new File(System.getProperty("plugins.jmaki.nbm")); //NOI18N
        assertTrue("jmaki nbm does not exist", jmakiNbm.exists()); //NOI18N
        Thread t = new Thread(new Runnable() {

            public void run() {
                while (true) {
                    String dialogLabel = "Warning";
                    JDialog dialog = JDialogOperator.findJDialog(dialogLabel, false, false);
                    if (dialog != null) {
                        new JButtonOperator(new NbDialogOperator(dialog), "Continue").push();
                        break;
                    }
                }
            }
        });
        t.start();
        PluginsOperator po = PluginsOperator.invoke();
        //"Add Plugins..."
        String addPluginsBtn = Bundle.getStringTrimmed("org.netbeans.modules.autoupdate.ui.Bundle", "UnitTab_bAddLocallyDownloads_Name");
        new JButtonOperator(po.selectDownloaded(), addPluginsBtn).push();
        JFileChooserOperator jfco = new JFileChooserOperator(po);
        jfco.setCurrentDirectory(jmakiNbm.getParentFile());
        jfco.selectFile(jmakiNbm.getName());
        jfco.approve();
        po.install();
        WizardOperator installerOper = po.installer();
        installerOper.next();
        // I accept the terms...
        String acceptLabel = Bundle.getStringTrimmed(
                "org.netbeans.modules.autoupdate.ui.wizards.Bundle",
                "LicenseApprovalPanel.cbAccept.text");
        JCheckBoxOperator acceptCheckboxOper = new JCheckBoxOperator(installerOper, acceptLabel);
        if (!acceptCheckboxOper.isEnabled()) {
            // wait until licence is shown and dialog is re-created
            acceptCheckboxOper.waitComponentShowing(false);
            // find check box again
            acceptCheckboxOper = new JCheckBoxOperator(installerOper, acceptLabel);
        }
        acceptCheckboxOper.push();
        // Install
        String installInDialogLabel = Bundle.getStringTrimmed("org.netbeans.modules.autoupdate.ui.wizards.Bundle", "InstallUnitWizardModel_Buttons_Install");
        new JButtonOperator(installerOper, installInDialogLabel).pushNoBlock();
        installerOper.finish();
    }

    public TestSuite suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new InstallPluginsTest("testInstallRest")); //NOI18N
        if (!Boolean.getBoolean("plugins.jmaki.skip")) { //NOI18N
            suite.addTest(new InstallPluginsTest("testInstallJMaki")); //NOI18N
        }
        return suite;
    }
    
    public static void main(String... args) {
        TestRunner.run(InstallPluginsTest.class);
    }
}
