/*
 * AnnotationsTest.java
 *
 * Created on June 30, 2006, 2:05 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.mercurial.main.archeology;

import java.io.File;
import java.io.PrintStream;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.Operator;
//import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.mercurial.utils.RepositoryMaintenance;
import org.netbeans.test.mercurial.utils.TestKit;
import org.netbeans.junit.ide.ProjectSupport;

/**
 *
 * @author pvcs
 */
public class AnnotationsTest extends JellyTestCase {
    
    public static final String PROJECT_NAME = "JavaApp";
    public PrintStream stream;
    String os_name;
    
    /** Creates a new instance of AnnotationsTest */
    public AnnotationsTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        os_name = System.getProperty("os.name");
        //System.out.println(os_name);
        System.out.println("### "+getName()+" ###");
        
    }
    
    protected boolean isUnix() {
        boolean unix = false;
        if (os_name.indexOf("Windows") == -1) {
            unix = true;
        }
        return unix;
    }
    
    public static void main(String[] args) {
        // TODO code application logic here
        TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new AnnotationsTest("testShowAnnotations"));
        return suite;
    }
    
    public void testShowAnnotations() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 30000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 30000);
        try {
            TestKit.closeProject(PROJECT_NAME);
            OutputOperator oo = OutputOperator.invoke();

            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"))
;
            TestKit.loadOpenProject(PROJECT_NAME, getDataDir());
            Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");
            node.performPopupAction("Mercurial|Show Annotations");
            OutputTabOperator oto = new OutputTabOperator("Mercurial");
            oto.waitText("INFO: End of Annotate");
            
            stream.flush();
            stream.close();
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
            TestKit.closeProject(PROJECT_NAME);
        }
    }
}
