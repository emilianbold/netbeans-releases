/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.ruby.railsprojects;

import java.io.File;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.junit.NbTestCase;

/**
 * @author Tor Norbye
 */
public class MigrateActionTest extends RailsProjectTestBase {
    
    private final String TAB = "        ";
    
    public MigrateActionTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private String annotate(JMenu menu) {
        StringBuilder sb = new StringBuilder();
        
        writeMenu(menu, 0, sb);
        
        return sb.toString();
    }
    
    private void writeMenu(JMenu menu, int indent, StringBuilder sb) {
        for (int i = 0; i < menu.getItemCount(); i++) {
            JMenuItem item = menu.getItem(i);
            if (item == null) { // Separator
                for (int j = 0; j < indent; j++) {
                    sb.append(TAB);
                }
                for (int j = 0; j < 20; j++) {
                    sb.append("-");
                }
                sb.append("\n");
                continue;
            }
            for (int j = 0; j < indent; j++) {
                sb.append(TAB);
            }
            sb.append(item.getText());
            sb.append("\n");
            
            if (item instanceof JMenu) {
                writeMenu((JMenu)item, indent+1, sb);
            }
        }
    }

    private RailsProject constructRailsProject(String dataFile) throws Exception {
        RailsProject project = createTestProject();
        // Create file from data folder
        createFilesFromDesc(project.getProjectDirectory(), dataFile);
        return project;
    }
    
    private void checkMenu(NbTestCase test, String relFilePath, RailsProject project) throws Exception {
        File rubyFile = new File(test.getDataDir(), relFilePath);
        if (!rubyFile.exists()) {
            NbTestCase.fail("File " + rubyFile + " not found.");
        }

        JMenu menu = new JMenu();
        MigrateAction.buildMenu(menu, project);

        String annotatedSource = annotate(menu);

        assertDescriptionMatches(relFilePath, annotatedSource, true, ".menu");
    }

//    private RailsProject constructRailsProject(String... files) throws Exception {
//        return createTestProject("RubyProject_" + getName(), files);
//    }
//    
    public void testMigrations109892() throws Exception {
        RailsProject p = constructRailsProject("testfiles/migrations1.txt");
        assertNotNull(p);

        checkMenu(this, "testfiles/migrations1.txt", p);
    }

    public void testMephisto() throws Exception {
        String name = "testfiles/mephisto.txt";
        RailsProject p = constructRailsProject(name);
        assertNotNull(p);

        checkMenu(this, name, p);
    }

    public void testEmpty1() throws Exception {
        String name = "testfiles/empty.txt";
        RailsProject p = constructRailsProject(name);
        assertNotNull(p);

        checkMenu(this, name, p);
    }

    public void testEmpty2() throws Exception {
        String name = "testfiles/empty2.txt";
        RailsProject p = constructRailsProject(name);
        assertNotNull(p);

        checkMenu(this, name, p);
    }

    public void testManyItems() throws Exception {
        String name = "testfiles/lotsofitems.txt";
        RailsProject p = constructRailsProject(name);
        assertNotNull(p);

        checkMenu(this, name, p);
    }

    public void testFewItems() throws Exception {
        String name = "testfiles/short.txt";
        RailsProject p = constructRailsProject(name);
        assertNotNull(p);

        checkMenu(this, name, p);
    }

    public void testMigrateUTC() throws Exception {
        String name = "testfiles/migrate-utc.txt";
        RailsProject p = constructRailsProject(name);
        assertNotNull(p);

        checkMenu(this, name, p);
    }

    public void testMigrateUTCMixed() throws Exception {
        String name = "testfiles/migrate-utc-mixed.txt";
        RailsProject p = constructRailsProject(name);
        assertNotNull(p);

        checkMenu(this, name, p);
    }

    public void testMigrateUTCMany() throws Exception {
        String name = "testfiles/migrate-utc-many.txt";
        RailsProject p = constructRailsProject(name);
        assertNotNull(p);
        
        checkMenu(this, name, p);
    }
}
