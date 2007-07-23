/*
 * MigrateActionTest.java
 * JUnit based test
 *
 * Created on July 16, 2007, 2:18 PM
 */

package org.netbeans.modules.ruby.railsprojects;

import java.io.File;
import org.netbeans.api.project.FileOwnerQuery;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ruby.rubyproject.RubyProjectTestBase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tor Norbye
 */
public class MigrateActionTest extends RubyProjectTestBase {
    
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

    protected RailsProject getRailsProject(String path) {
        Project p = getTestProject(path);
        assertNotNull(p);
        assertTrue(p instanceof RailsProject);
        
        return (RailsProject)p;
    }
    
    private String annotate(JMenu menu) {
        StringBuilder sb = new StringBuilder();
        
        writeMenu(menu, 0, sb);
        
        return sb.toString();
    }
    
    
    private final String TAB = "        ";
    
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
    
    private RailsProject constructRailsProject(String dbtaskFile) throws Exception {
        String projectName = "RailsProject_" + getName();
                
        File projectFile = new File(getDataDir(), projectName);
        if (projectFile.exists()) {
            FileObject fo = FileUtil.toFileObject(projectFile);
            
            Project p = FileOwnerQuery.getOwner(fo);
            assertNotNull(p);
            assertTrue(p instanceof RailsProject);
            
            return (RailsProject)p;
        }
        
        // Build the Rails project
        FileObject parentDir = FileUtil.toFileObject(getDataDir());
        assertNotNull(parentDir);
        FileObject dir = parentDir.createFolder(projectName);
        assertNotNull(dir);
        FileObject nbproject = dir.createFolder("nbproject");
        FileObject projectXml = nbproject.createData("project", "xml");
        String xml =
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<project xmlns=\"http://www.netbeans.org/ns/project/1\">\n" +
"    <type>org.netbeans.modules.ruby.railsprojects</type>\n" +
"    <configuration>\n" +
"        <data xmlns=\"http://www.netbeans.org/ns/rails-project/1\">\n" +
"            <name>" + projectName + "</name>\n" +
"        </data>\n" +
"    </configuration>\n" +
"</project>\n";
        OutputStream os = projectXml.getOutputStream();
        Writer writer = new BufferedWriter(new OutputStreamWriter(os));
        writer.write(xml);
        writer.close();
        
        // Create the db folders!
        createFilesFromDesc(dir, dbtaskFile);
        
        Project p = FileOwnerQuery.getOwner(dir);
        assertNotNull(p);
        assertTrue(p instanceof RailsProject);
        
        return (RailsProject)p;
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
