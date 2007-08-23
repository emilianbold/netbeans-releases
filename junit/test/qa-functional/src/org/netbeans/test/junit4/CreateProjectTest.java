package org.netbeans.test.junit4;

import java.util.ArrayList;
import java.util.Hashtable;
import javax.swing.ListModel;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.junit.NbTestSuite;

public class CreateProjectTest extends ExtJellyTestCase {

    public CreateProjectTest(String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new CreateProjectTest("testCreateJUnit4Project")); // NOI18N
        suite.addTest(new CreateProjectTest("testAddLibrary")); // NOI18N
        suite.addTest(new CreateProjectTest("testGeneratedRootSuiteFile")); // NOI18N
        suite.addTest(new CreateProjectTest("testGeneratedProjectSuiteFile")); // NOI18N
        suite.addTest(new CreateProjectTest("testGeneratedMainTestFile")); // NOI18N
        return suite;
    }
        
    public void testCreateJUnit4Project() {
        // create anagram project
        new Action("File|New Project", null).perform();
        NewProjectWizardOperator newOp = new NewProjectWizardOperator();
        newOp.selectCategory("Java");
        newOp.selectProject("Java Application");
        newOp.next();
        new JTextFieldOperator(newOp, 0).typeText(TEST_PROJECT_NAME);
        newOp.finish();

        // select source packages node
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(TEST_PROJECT_NAME);
        prn.select();
        Node node = new Node(prn, "Source Packages"); // NOI18N
        node.setComparator(new Operator.DefaultStringComparator(true, false));
        node.select();

        // create test
        new ActionNoBlock(null,"Tools|Create JUnit Test").perform(node);

        // select junit version
        NbDialogOperator versionOp = new NbDialogOperator("Select jUnit Version");        
        new JRadioButtonOperator(versionOp, 1).setSelected(true);
        new JButtonOperator(versionOp,"Select").clickMouse();

        NbDialogOperator newTestOp = new NbDialogOperator("Create Tests");
        new JButtonOperator(newTestOp, "OK").clickMouse();
        
        new Action("Window|Close All Documents", null).perform();
    }

    public void testAddLibrary() {
        // useless method while executing this test internally
        // but there is a missing libraty while executing this test on external IDE
        ProjectRootNode prn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        Node libNode = new Node(prn, "Test Libraries");
        new ActionNoBlock(null,"Add Library").perform(libNode);
        NbDialogOperator libDialog = new NbDialogOperator("Add Library");
        JListOperator listOp = new JListOperator(libDialog,0);
        listOp.clickOnItem("junit", new Operator.DefaultStringComparator(true, false));
        new JButtonOperator(libDialog, "Add Library").push();
    }
    
    public void testGeneratedRootSuiteFile() {
        ProjectRootNode prn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        OpenAction openAc = new OpenAction();

        // testing RootSuite.java
        openAc.perform(new Node(prn, "Test Packages|<default package>|RootSuite.java"));
        
        ArrayList<String> lines = new ArrayList<String>();
        lines.add("import org.junit.");
        lines.add("@RunWith(Suite.class)");
        lines.add("@Suite.SuiteClasses({Junit4testprojectSuite.class})");
        lines.add("public static void tearDownClass() throws Exception");
        lines.add("@Before");
        
        findInCode(lines,new EditorOperator("RootSuite.java"));
    }

    public void testGeneratedProjectSuiteFile() {
        ProjectRootNode prn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        OpenAction openAc = new OpenAction();

        // testing Junit4testprojectSuite.java
        openAc.perform(new Node(prn, "Test Packages|junit4testproject|Junit4testprojectSuite.java"));
        
        ArrayList<String> lines = new ArrayList<String>();
        lines.add("import org.junit.");
        lines.add("@Suite.SuiteClasses({MainTest.class})");
        lines.add("@RunWith(Suite.class)");
        lines.add("public static void tearDownClass() throws Exception");
        lines.add("@Before");
        
        findInCode(lines,new EditorOperator("Junit4testprojectSuite.java"));
    }

    public void testGeneratedMainTestFile() {
        ProjectRootNode prn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        OpenAction openAc = new OpenAction();

        // testing MainTest.java
        openAc.perform(new Node(prn, "Test Packages|junit4testproject|MainTest.java"));
        
        ArrayList<String> lines = new ArrayList<String>();
        lines.add("import org.junit.");        
        lines.add("import static org.junit.Assert.*;");
        lines.add("@Test");
        lines.add("@BeforeClass");
        lines.add("@AfterClass");
        
        findInCode(lines,new EditorOperator("MainTest.java"));
    }
}
