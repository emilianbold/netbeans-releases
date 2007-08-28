/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ruby.railsprojects;

import java.io.File;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

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

    private RailsProject constructRailsProject(String... files) throws Exception {
        return createTestProject("RubyProject_" + getName(), files);
    }
    
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
}
