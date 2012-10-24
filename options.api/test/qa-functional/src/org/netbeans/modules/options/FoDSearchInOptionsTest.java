/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.options;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JLabel;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.StringComparator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author theofanis
 */
public class FoDSearchInOptionsTest extends JellyTestCase {

    private StringComparator stringComparator;
    private OptionsOperator optionsOperator;
    private JTextFieldOperator jTextFieldOperator;
    private JTabbedPaneOperator jTabbedPaneOperator;

    /**
     * Constructor required by JUnit
     */
    public FoDSearchInOptionsTest(String testName) {
        super(testName);
    }

    /**
     * Creates suite from particular test cases.
     */
    public static Test suite() {
        return NbModuleSuite.createConfiguration(FoDSearchInOptionsTest.class).addTest(
                "testSearchInOptionsWindow").clusters(".*").gui(true).suite();
    }

    public void testSearchInOptionsWindow() {
        OptionsOperator.invoke();
        new EventTool().waitNoEvent(1000);
        log("Option dialog was opened");

        optionsOperator = new OptionsOperator();
        new EventTool().waitNoEvent(1000);

        jTextFieldOperator = new JTextFieldOperator(optionsOperator);
        stringComparator = Operator.getDefaultStringComparator();

        int[] tabIndexes = {0};
        String[] selectedCategories = {"Editor"};
        ArrayList<String> enabledCategories = new ArrayList<String>();
        enabledCategories.add("Editor");
        searchFor("general editor", tabIndexes, selectedCategories, enabledCategories, null);

        tabIndexes[0] = 6;
        searchFor("macros", tabIndexes, selectedCategories, enabledCategories, null);

        tabIndexes[0] = 2;
        searchFor("completion", tabIndexes, selectedCategories, enabledCategories, null);

        enabledCategories.clear();
        enabledCategories.add("General");
        tabIndexes[0] = -1;
        selectedCategories[0] = "General";
        searchFor("proxy", tabIndexes, selectedCategories, enabledCategories, null);

        enabledCategories.clear();
        enabledCategories.add("Fonts & Colors");
        tabIndexes[0] = 0;
        selectedCategories[0] = "FontsAndColors";
        searchFor("syntax", tabIndexes, selectedCategories, enabledCategories, null);

        tabIndexes[0] = 4;
        searchFor("versioning", tabIndexes, selectedCategories, enabledCategories, null);

        enabledCategories.clear();
        enabledCategories.add("Java");
        tabIndexes[0] = 4;
        selectedCategories[0] = "Java";
        searchFor("maven", tabIndexes, selectedCategories, enabledCategories, "java");

        tabIndexes[0] = 0;
        selectedCategories[0] = "Java";
        searchFor("ant", tabIndexes, selectedCategories, enabledCategories, null);

        tabIndexes[0] = 6;
        selectedCategories[0] = "Java";
        searchFor("fx", tabIndexes, selectedCategories, enabledCategories, "javafx");

        tabIndexes[0] = 2;
        selectedCategories[0] = "Java";
        searchFor("java card", tabIndexes, selectedCategories, enabledCategories, "javacard");

        enabledCategories.clear();
        enabledCategories.add("Miscellaneous");
        tabIndexes[0] = 3;
        selectedCategories[0] = "Miscellaneous";
        searchFor("groovy", tabIndexes, selectedCategories, enabledCategories, "groovy");

        tabIndexes[0] = 6;
        selectedCategories[0] = "Miscellaneous";
        searchFor("terminal", tabIndexes, selectedCategories, enabledCategories, null);

        enabledCategories.add("Fonts & Colors");
        tabIndexes = new int[2];
        selectedCategories = new String[2];
        tabIndexes[0] = 3;
        tabIndexes[1] = 1;
        selectedCategories[0] = "FontsAndColors";
        selectedCategories[1] = "Miscellaneous";
        searchFor("diff", tabIndexes, selectedCategories, enabledCategories, null);

        enabledCategories.clear();
        enabledCategories.add("PHP");
        tabIndexes = new int[1];
        selectedCategories = new String[1];
        tabIndexes[0] = 5;
        selectedCategories[0] = "PHP";
        searchFor("apigen", tabIndexes, selectedCategories, enabledCategories, null);

        tabIndexes[0] = 8;
        searchFor("symfony2", tabIndexes, selectedCategories, enabledCategories, "php");

        tabIndexes[0] = 11;
        searchFor("smarty", tabIndexes, selectedCategories, enabledCategories, "php");

        enabledCategories.clear();
        enabledCategories.add("C/C++");
        tabIndexes[0] = 0;
        selectedCategories[0] = "C/C++";
        searchFor("build tools", tabIndexes, selectedCategories, enabledCategories, null);

        tabIndexes[0] = 2;
        searchFor("code assistance", tabIndexes, selectedCategories, enabledCategories, null);

        enabledCategories.clear();
        enabledCategories.addAll(Arrays.asList("General", "Editor", "Fonts & Colors", "Keymap", "Java", "PHP", "C/C++", "Miscellaneous"));
        searchFor("", tabIndexes, selectedCategories, enabledCategories, null);
    }

    private void searchFor(String searchTxt, int[] selectedTabIndexes, String[] selectedCategories, ArrayList<String> enabledCategories, String clusterName) {
        jTextFieldOperator.setText(searchTxt);
        new EventTool().waitNoEvent(500);
        jTextFieldOperator.pushKey(KeyEvent.VK_ENTER);
        new EventTool().waitNoEvent(1000);
        for (int i = 0; i < selectedCategories.length; i++) {
            String selectedCategory = selectedCategories[i];
            if (selectedCategory.equals("General")) { // NOI18N
                optionsOperator.selectGeneral();
            } else if (selectedCategory.equals("Editor")) { // NOI18N
                optionsOperator.selectEditor();
            } else if (selectedCategory.equals("FontsAndColors")) { // NOI18N
                optionsOperator.selectFontAndColors();
            } else if (selectedCategory.equals("Keymaps")) { // NOI18N
                optionsOperator.selectKeymap();
            } else if (selectedCategory.equals("Java")) { // NOI18N
                optionsOperator.selectJava();
            } else if (selectedCategory.equals("Miscellaneous")) { // NOI18N
                optionsOperator.selectMiscellaneous();
            } else if (selectedCategory.equals("PHP") || selectedCategory.equals("C/C++")) { // NOI18N
                optionsOperator.selectCategory(selectedCategory);
            }
            new EventTool().waitNoEvent(1000);
            int selectedTabIndex = selectedTabIndexes[i];
            if (selectedTabIndex != -1) {
                jTabbedPaneOperator = new JTabbedPaneOperator(optionsOperator);
                assertEquals(selectedTabIndex, jTabbedPaneOperator.getSelectedIndex());
		if(clusterName != null) {
		    JLabelOperator jLabelOperator = new JLabelOperator(jTabbedPaneOperator);
		    assertEquals("In order to use this functionality, support for "+clusterName+" must be activated.", jLabelOperator.getText()); // NOI18N
		}
            }
        }
        for (String category : enabledCategories) {
            assertTrue(getJLabelOperator(category).isEnabled());
        }
    }

    private JLabelOperator getJLabelOperator(final String category) {
        return new JLabelOperator(optionsOperator, new ComponentChooser() {
            @Override
            public boolean checkComponent(Component comp) {
                if (comp.getClass().getName().equals("org.netbeans.modules.options.OptionsPanel$CategoryButton") ||// NOI18N
                        comp.getClass().getName().equals("org.netbeans.modules.options.OptionsPanel$NimbusCategoryButton")) { // NOI18N
                    if (((JLabel) comp).getText() != null) {
                        return stringComparator.equals(((JLabel) comp).getText(), category);
                    }
                }
                return false;
            }

            @Override
            public String getDescription() {
                return "OptionsPanel$CategoryButton with text " + category; // NOI18N
            }
        });
    }
}
