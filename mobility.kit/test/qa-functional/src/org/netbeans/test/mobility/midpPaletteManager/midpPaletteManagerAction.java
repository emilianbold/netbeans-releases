/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.test.mobility.midpPaletteManager;

//<editor-fold desc="imports">
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.modules.form.ComponentPaletteOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
//</editor-fold>

/**
 *
 * @author tester
 */
public class midpPaletteManagerAction extends JellyTestCase {
    public static final String BUTTON_NEW_CATEGORY = "New Category";
    public static final String DIALOG_NEW_CATEGORY = "New Palette Category";
    public static final String DIALOG_PALETTE_MANAGER = "Palette Manager";
    public static final String MENU_MIDP_PALETTE = "Tools|Palette|MIDP Visual Mobile Designer";
    public static final String MENU_SWITCH2FLOW = "View|Editors|Flow";
    public static final String NEW_CATEGORY = "MyNewCategory";
    public static final String PROJECT_NAME_MIDP = "MobileApplication";
    
    //<editor-fold desc="Test Suite - base">
    /** Constructor required by JUnit */
    public midpPaletteManagerAction(String tname, boolean init) {
        super(tname);
        if (init) init();
    }
    
    /** Creates suite from particular test cases. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        // Prepare some projects
        suite.addTest(new midpPaletteManagerAction("testCreateNewCategory", true));
        // Create MIDP Files
        return suite;
    }
    
    public void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    //</editor-fold>
    
    public void init() {
        new Action(null, "Open").perform(new Node(ProjectsTabOperator.invoke().tree(), PROJECT_NAME_MIDP + "|Source Packages|hello|HelloMIDlet.java"));
    }
    
    public void testCreateNewCategory() {
          Action invokeCreateNewCategoryDialog = new Action(MENU_MIDP_PALETTE, null);
          invokeCreateNewCategoryDialog.setComparator(new DefaultStringComparator(true, true));
          invokeCreateNewCategoryDialog.perform();
          NbDialogOperator paletteManager = new NbDialogOperator(DIALOG_PALETTE_MANAGER);
          new JButtonOperator(paletteManager,BUTTON_NEW_CATEGORY).push();
          NbDialogOperator newCategory = new NbDialogOperator(DIALOG_NEW_CATEGORY);
          new JTextFieldOperator(newCategory, 0).setText(NEW_CATEGORY);
          newCategory.btOK().push();
          paletteManager.btClose().push();
          sleep(1000);
          new Action(MENU_SWITCH2FLOW, null).perform();
          sleep(2000);
          ComponentPaletteOperator cpo = new ComponentPaletteOperator();
          cpo.expand(new JCheckBoxOperator(cpo,NEW_CATEGORY), true);
          
    }
}
