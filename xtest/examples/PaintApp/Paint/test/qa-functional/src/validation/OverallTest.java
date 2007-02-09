package validation;

import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jemmy.operators.*;

/**
 * A Test based on JellyTestCase. JellyTestCase redirects Jemmy output
 * to a log file provided by NbTestCase. It can be inspected in results.
 * It also sets timeouts necessary for NetBeans GUI testing.
 *
 * Any JemmyException (which is normally thrown as a result of an unsuccessful
 * operation in Jemmy) going from a test is treated by JellyTestCase as a test
 * failure; any other exception - as a test error.
 *
 * Additionally it:
 *    - closes all modal dialogs at the end of the test case (property jemmy.close.modal - default true)
 *    - generates component dump (XML file containing components information) in case of test failure (property jemmy.screen.xmldump - default false)
 *    - captures screen into a PNG file in case of test failure (property jemmy.screen.capture - default true)
 *    - waits at least 1000 ms between test cases (property jelly.wait.no.event - default true)
 *
 */
public class OverallTest extends JellyTestCase {
    
    /** Constructor required by JUnit */
    public OverallTest(String name) {
        super(name);
    }
    
    /** Creates suite from particular test cases. You can define order of testcases here. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new OverallTest("testBrushSize"));
        suite.addTest(new OverallTest("testPainting"));
        suite.addTest(new OverallTest("testClear"));
        suite.addTest(new OverallTest("testColorChooser"));
        return suite;
    }
    
    /* Method allowing test execution directly from the IDE. */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
        // run only selected test case
        //junit.textui.TestRunner.run(new OverallTest("test1"));
    }
    
    /** Called before every test case. */
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
    }
    
    /** Called after every test case. */
    public void tearDown() {
    }
    
    // Add test methods here, they have to start with 'test' name.
    
    /** Test brush size setting. */
    public void testBrushSize() {
        new Action("File|New Canvas", null).perform();
        JSliderOperator slider = new JSliderOperator(MainWindowOperator.getDefault());
        slider.scrollToMaximum();
        slider.scrollToMinimum();
        slider.scrollToMaximum();
    }
    
    /** Test painting. */
    public void testPainting() {
        TopComponentOperator tcOper = new TopComponentOperator("Image");
        tcOper.clickMouse(tcOper.getCenterX(), tcOper.getCenterY(), 1);
        tcOper.dragNDrop(tcOper.getCenterX(), tcOper.getCenterY(), tcOper.getWidth()-1, tcOper.getHeight()-1);
        tcOper.dragNDrop(tcOper.getWidth()-1, tcOper.getHeight()-1, 0, tcOper.getHeight()-1);
        tcOper.dragNDrop(0, tcOper.getHeight()-1, tcOper.getCenterX(), tcOper.getCenterY());
    }
    
    /** Test clear button. */
    public void testClear() {
        new JButtonOperator(new TopComponentOperator("Image"), "Clear").push();
    }
    
    public void testColorChooser() {
        fail("Not yet implemented.");
    }
}
