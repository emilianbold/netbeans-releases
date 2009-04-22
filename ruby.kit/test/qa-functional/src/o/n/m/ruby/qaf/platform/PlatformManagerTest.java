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

package o.n.m.ruby.qaf.platform;

import java.io.File;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JProgressBarOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbModuleSuite;
import org.openide.util.Exceptions;

/**
 *
 * @author lukas
 */
public class PlatformManagerTest extends JellyTestCase {

    private NbDialogOperator rpm;
    private JListOperator platforms;

    public PlatformManagerTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        if (isMacOsX()) {
            System.err.println(">>> set dispatch model to ROBOT_MODEL_MASK for Mac OS X");
            JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
        }
        System.out.println("##### "+getName()+" #####");
        rpm = getPlatformManager();
        platforms = new JListOperator(rpm);
    }

    @Override
    public void tearDown() throws Exception {
        rpm.closeByButton();
        super.tearDown();
    }

    public void testRemove() {
        sleep(2000);
        int listSize = platforms.getModel().getSize();
        assertTrue("at least one platform",  listSize > 0);
        //Built-in JRuby 1.1.4
        String bundledJRuby = Bundle.getStringTrimmed("org.netbeans.api.ruby.platform.Bundle", "RubyPlatformManager.CTL_BundledJRubyLabel");
        // XXX this call close the dialog on Mac OS X with default dispatchig model
        // to reproduce just comment out the testInit()
        platforms.selectItem(bundledJRuby);
        //Remove
        String remove = Bundle.getStringTrimmed("org.netbeans.modules.ruby.platform.Bundle", "RubyPlatformCustomizer.removeButton.text");
        JButtonOperator jbo = new JButtonOperator(rpm, remove, 1);
        jbo.push();
        assertEquals("not removed", listSize - 1, platforms.getModel().getSize());
    }

    public void testAutodetect() {
        int listSize = platforms.getModel().getSize();
        //Autodetect Platforms
        String autodetect = Bundle.getStringTrimmed("org.netbeans.modules.ruby.platform.Bundle", "RubyPlatformCustomizer.autoDetectButton.text");
        JButtonOperator jbo = new JButtonOperator(rpm, autodetect);
        jbo.push();
        assertEquals("not detected", listSize + 1, platforms.getModel().getSize());
        //Built-in JRuby 1.1.4
        String bundledJRuby = Bundle.getStringTrimmed("org.netbeans.api.ruby.platform.Bundle", "RubyPlatformManager.CTL_BundledJRubyLabel");
        platforms.selectItem(bundledJRuby);
    }

    public void testAddPlatform() {
        //Built-in JRuby 1.1.4
        String bundledJRuby = Bundle.getStringTrimmed("org.netbeans.api.ruby.platform.Bundle", "RubyPlatformManager.CTL_BundledJRubyLabel");
        platforms.selectItem(bundledJRuby);
        String binPath = new JTextFieldOperator(rpm, "bin" + File.separator + "jruby").getDisplayedText(); //NOI18N
        testRemove();
        int listSize = platforms.getModel().getSize();
        //Add Platform
        String addPlf = Bundle.getStringTrimmed("org.netbeans.modules.ruby.platform.Bundle", "RubyPlatformCustomizer.addButton.text");
        JButtonOperator jbo = new JButtonOperator(rpm, addPlf);
        jbo.push();
        new JFileChooserOperator().chooseFile(binPath);
        new JProgressBarOperator(rpm).waitComponentShowing(false);
        assertEquals("not added", listSize + 1, platforms.getModel().getSize());
    }

    public void testInvokeGemManager() {
        //Gem Manager
        String gemM = Bundle.getStringTrimmed("org.netbeans.modules.ruby.platform.Bundle", "RubyPlatformCustomizer.gemManagerButton.text");
        JButtonOperator jbo = new JButtonOperator(rpm, gemM);
        jbo.push();
        //Ruby Gems
        String gemsTitle = Bundle.getStringTrimmed("org.netbeans.modules.ruby.platform.gems.Bundle", "CTL_RubyGems");
        NbDialogOperator gemsDlg = new NbDialogOperator(gemsTitle);
        gemsDlg.closeByButton();
    }

    private NbDialogOperator getPlatformManager() {
        //Tools
        String toolsMenu = Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle", "Actions/Tools");
        //Ruby Platforms
        String rp = Bundle.getStringTrimmed("org.netbeans.modules.ruby.platform.Bundle", "CTL_RubyPlatformAction");
        String menuPath = toolsMenu + "|" + rp;
        System.err.println(">>> menuPath='" + menuPath + "'");
        new Action(menuPath, null).perform();
        //Ruby Platform Manager
        String mgrTitle = Bundle.getStringTrimmed("org.netbeans.modules.ruby.platform.Bundle", "CTL_RubyPlatformManager_Title");
        return new NbDialogOperator(mgrTitle);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(PlatformManagerTest.class).enableModules(".*").clusters(".*")); //NOI18N
    }

    private void sleep(long milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private boolean isMacOsX() {
        return System.getProperty("os.name").toLowerCase().startsWith("mac os x");
    }
}
