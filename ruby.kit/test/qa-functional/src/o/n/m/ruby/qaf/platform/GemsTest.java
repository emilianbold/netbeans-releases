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

import javax.swing.JLabel;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JProgressBarOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author lukas
 */
public class GemsTest extends JellyTestCase {

    private NbDialogOperator rgm;

    public GemsTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        rgm = getPlatformManager();
        if (JProgressBarOperator.findJProgressBar(rgm.getContentPane()) != null) {
            new JProgressBarOperator(rgm).waitComponentShowing(false);
        }
        JComboBoxOperator jcbo = new JComboBoxOperator(rgm);
        assertTrue(jcbo.getSelectedItem().toString().contains("JRuby")); //NOI18N
    }

    @Override
    public void tearDown() throws Exception {
        rgm.closeByButton();
        super.tearDown();
    }

    public void testTabs() {
        JTabbedPaneOperator jtpo = new JTabbedPaneOperator(rgm);
        //Installed
        String installedTitle = Bundle.getStringTrimmed("org.netbeans.modules.ruby.platform.gems.Bundle", "GemPanel.installedPanel.TabConstraints.tabTitle");
        installedTitle += "(17)"; //NOI18N
        int idx = jtpo.getSelectedIndex();
        assertEquals(installedTitle, jtpo.getTitleAt(idx));
        JListOperator gemsList = new JListOperator(jtpo);
        gemsList.selectItem(0);
        JLabelOperator jlo = new JLabelOperator((JLabel) gemsList.getRenderedComponent(0));
        String gem = jlo.getText();
        //Settings
        String settingsTitle = Bundle.getStringTrimmed("org.netbeans.modules.ruby.platform.gems.Bundle", "GemPanel.settingsPanel.TabConstraints.tabTitle");
        jtpo.selectPage(settingsTitle);
        //Configure Proxies...
        String proxy = Bundle.getStringTrimmed("org.netbeans.modules.ruby.platform.gems.Bundle", "GemPanel.proxyButton.text");
        new JButtonOperator(jtpo, proxy).push();
        new OptionsOperator().cancel();
        //Fetch All Gem Versions
        String all = Bundle.getStringTrimmed("org.netbeans.modules.ruby.platform.gems.Bundle", "GemPanel.allVersionsCheckbox.text");
        JCheckBoxOperator jcbo = new JCheckBoxOperator(jtpo, all);
        assertFalse(jcbo.isSelected());
        //Fetch Detailed Gem Description
        String details = Bundle.getStringTrimmed("org.netbeans.modules.ruby.platform.gems.Bundle", "GemPanel.descriptionCheckbox.text");
        jcbo = new JCheckBoxOperator(jtpo, details);
        assertTrue(jcbo.isSelected());
        jcbo.clickMouse(1);
        assertFalse(jcbo.isSelected());
        jtpo.selectPage(idx);
        //Reload Gems
        String reload = Bundle.getStringTrimmed("org.netbeans.modules.ruby.platform.gems.Bundle", "GemPanel.reloadInstalledButton.text");
        new JButtonOperator(jtpo, reload).push();
        JProgressBarOperator jpbo = new JProgressBarOperator(rgm);
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitStateTimeout", 60000);
        jpbo.waitComponentShowing(false);
        gemsList = new JListOperator(jtpo);
        gemsList.selectItem(0);
        jlo = new JLabelOperator((JLabel) gemsList.getRenderedComponent(0));
        String gem2 = jlo.getText();
        assertFalse(gem.equals(gem2));
        assertTrue(gem.length() != gem2.length());
        gem2 = gem2.substring(0, 33);
        assertTrue(gem.indexOf(gem2) >= 0);
    }

    public void testManage() {
        //Manage...
        String manageLabel = Bundle.getStringTrimmed("org.netbeans.modules.ruby.platform.gems.Bundle", "GemPanel.manageButton.text");
        JButtonOperator jbo = new JButtonOperator(rgm, manageLabel);
        jbo.push();
        //Ruby Platform Manager
        String mgrTitle = Bundle.getStringTrimmed("org.netbeans.modules.ruby.platform.Bundle", "CTL_RubyPlatformManager_Title");
        new NbDialogOperator(mgrTitle).closeByButton();
    }

    public void testInstall() {
        JTabbedPaneOperator jtpo = new JTabbedPaneOperator(rgm);
        assertTrue(jtpo.getTitleAt(1).contains("17"));
        jtpo.selectPage(2);
        if (JProgressBarOperator.findJProgressBar(rgm.getContentPane()) != null) {
            new JProgressBarOperator(rgm).waitComponentShowing(false);
        }
        JListOperator gemsList = new JListOperator(jtpo);
        //select Abundance Gem
        gemsList.selectItem(1);
        //Install
        String inst = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.ruby.platform.gems.Bundle", "GemPanel.installButton.text");
        new JButtonOperator(jtpo, inst).pushNoBlock();
        //Gem Installation Settings
        String instTitle = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.ruby.platform.gems.Bundle", "ChooseGemSettings");
        new NbDialogOperator(instTitle).ok();
        //Gem Installation
        instTitle = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.ruby.platform.gems.Bundle", "Installation");
        NbDialogOperator gemInst = new NbDialogOperator(inst);
        //Close
        String closeLabel = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.ruby.platform.gems.Bundle", "CTL_Close");
        JButtonOperator close = new JButtonOperator(gemInst, closeLabel);
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitStateTimeout", 60000);
        close.push();
        if (JProgressBarOperator.findJProgressBar(rgm.getContentPane()) != null) {
            new JProgressBarOperator(rgm).waitComponentShowing(false);
        }
        jtpo.selectPage(1);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ex) {
        }
        assertTrue(jtpo.getTitleAt(1).contains("18"));
    }

    public void testUninstall() {
        JTabbedPaneOperator jtpo = new JTabbedPaneOperator(rgm);
        assertTrue(jtpo.getTitleAt(1).contains("18"));
        JListOperator gemsList = new JListOperator(jtpo);
        //select Abundance Gem
        gemsList.selectItem(0);
        //Uninstall
        String inst = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.ruby.platform.gems.Bundle", "GemPanel.uninstallButton.text");
        new JButtonOperator(jtpo, inst).pushNoBlock();
        //Gem Uninstallation
        String instTitle = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.ruby.platform.gems.Bundle", "Uninstallation");
        NbDialogOperator gemInst = new NbDialogOperator(instTitle);
        //Close
        String closeLabel = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.ruby.platform.gems.Bundle", "CTL_Close");
        JButtonOperator close = new JButtonOperator(gemInst, closeLabel);
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitStateTimeout", 60000);
        close.push();
        if (JProgressBarOperator.findJProgressBar(rgm.getContentPane()) != null) {
            new JProgressBarOperator(rgm).waitComponentShowing(false);
        }
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ex) {
        }
        jtpo.selectPage(1);
        assertTrue(jtpo.getTitleAt(1).contains("17"));
    }

    private NbDialogOperator getPlatformManager() {
        //Tools
        String toolsMenu = Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle", "Actions/Tools");
        //Ruby Gems
        String rg = Bundle.getStringTrimmed("org.netbeans.modules.ruby.platform.gems.Bundle", "CTL_GemAction");
        new ActionNoBlock(toolsMenu + "|" + rg, null).perform();
        //Ruby Gems
        String gemsTitle = Bundle.getStringTrimmed("org.netbeans.modules.ruby.platform.gems.Bundle", "CTL_RubyGems");
        return new NbDialogOperator(gemsTitle);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(GemsTest.class).enableModules(".*").clusters("ruby")); //NOI18N
    }
}
