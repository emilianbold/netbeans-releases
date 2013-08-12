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
package org.netbeans.test.permanentUI;

import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.junit.Manager;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.permanentUI.utils.NbMenuItem;
import org.netbeans.test.permanentUI.utils.Utilities;

/**
 *
 * @author Lukas Hasik
 */
public class OptionsTest extends JellyTestCase {

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(
                OptionsTest.class).clusters(".*").enableModules(".*");

        conf.addTest("testOptionsCategories");
        return conf.suite();
    }

    @Override
    protected void setUp() throws Exception {
        System.out.println("########  " + getName() + "  #######");

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public OptionsTest(String testName) {
        super(testName);
    }

    public void testOptionsCategories() {
        OptionsOperator oo = OptionsOperator.invoke();//open options

        Component optionsPanel = oo.findSubComponent(getCompChooser("org.netbeans.modules.options.OptionsPanel"));
        //we need container to be able to traverse inside   
        Container optionsContainer = ContainerOperator.findContainerUnder(optionsPanel);
        ArrayList<Component> optionsCategories = Utilities.findComponentsInContainer(
                optionsContainer,
                getCompChooser("org.netbeans.modules.options.OptionsPanel$CategoryButton"),
                true);


        NbMenuItem ideOptions = new NbMenuItem(getName());//let store it in NbMenuItem TODO: refactor to make it simplier
        ArrayList<NbMenuItem> categories = new ArrayList<NbMenuItem>();
        NbMenuItem miscCategory = null;//remember the miscellanous because you will add the subcategories to it
        for (Component component : optionsCategories) {
            NbMenuItem optionsCategory = new NbMenuItem(((JLabel) component).getText());
            categories.add(optionsCategory);
            if (optionsCategory.getName().equals("Miscellaneous")) {//NOI18N
                miscCategory = optionsCategory;
            }
        }
        ideOptions.setSubmenu(categories);

        oo.selectMiscellaneous();//switch to Miscelenous

        JTabbedPane miscellaneousPanel = (JTabbedPane) oo.findSubComponent(getCompChooser("javax.swing.JTabbedPane"));

        ArrayList<NbMenuItem> miscCategories = new ArrayList<NbMenuItem>();
        for (int i = 0; i < miscellaneousPanel.getTabCount(); i++) {
            NbMenuItem miscCategoryItem = new NbMenuItem();
            miscCategoryItem.setName(miscellaneousPanel.getTitleAt(i));//
            miscCategories.add(miscCategoryItem);
        }
        miscCategory.setSubmenu(miscCategories);

        //load categories order from golden file
        //ArrayList = categoriesOrderGolden
        final String ideOptionsLogFile = getWorkDirPath() + File.separator + getName() + "_ide.txt";
        PrintStream ideFile = null;
        final String permuiOptionsLogsFile = getWorkDirPath() + File.separator + getName() + "_golden.txt";
        PrintStream goldenFile = null;
        final String diffFile = getWorkDirPath() + File.separator + getName() + ".diff";


        try {
            ideFile = new PrintStream(ideOptionsLogFile);
            goldenFile = new PrintStream(permuiOptionsLogsFile);
            
            //read the golden file
            NbMenuItem goldenOptions = Utilities.parseSubTreeByLines(getOptionsGoldenFile());
            goldenOptions.setName(getName());
            //make a diff
            Utilities.printMenuStructure(ideFile, ideOptions, "  ", 100);
            Utilities.printMenuStructure(goldenFile, goldenOptions, "  ", 100);
            Manager.getSystemDiff().diff(ideOptionsLogFile, permuiOptionsLogsFile, diffFile);
            //assert
            String message = Utilities.readFileToString(diffFile);

            assertFile(message, permuiOptionsLogsFile, ideOptionsLogFile, diffFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private ComponentChooser getCompChooser(final String className) {
        return new ComponentChooser() {

            public boolean checkComponent(Component comp) {
                return comp.getClass().getName().startsWith(className);
            }

            public String getDescription() {
                return className;
            }
        };
    }

    /**
     * constructs the relative path to the golden file to Options permanent UI spec
     * @return
     */
    private String getOptionsGoldenFile() {
        String dataDir = "";
        try {
            dataDir = getDataDir().getCanonicalPath();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return dataDir + File.separator + "permanentUI" + File.separator + "options" + File.separator + "options-categories.txt";
    }
}
