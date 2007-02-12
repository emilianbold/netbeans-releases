package validation;

import com.toy.anagrams.ui.Anagrams;
import javax.swing.JTextField;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/* Overall functional test of Anagram game */
public class OverallTest extends TestCase {
    
    public OverallTest(String testName) {
        super(testName);
    }

    /** Method called before each test case method. */
    protected void setUp() throws Exception {
        System.out.println("### "+getName()+" ###");
    }

    /** Method called after each test case method. */
    protected void tearDown() throws Exception {
    }
    
    /** Define test cases to be included and optionally do some initialization
     * for whole suite.
     */
    public static Test suite() {
        new Anagrams().setVisible(true);
        TestSuite suite = new TestSuite(OverallTest.class);
        return suite;
    }
    
    /** Simple overall test case:
     * - wait for frame with "Anagrams" title
     * - type "abstraction" in "Your Guess:" text field
     * - press Guess button
     * - compare feedback messasge with expected one
     */
    public void testOverall() {
        JFrameOperator frmAnagrams = new JFrameOperator("Anagrams");
        JLabelOperator lblWord = new JLabelOperator(frmAnagrams, "Scrambled Word:");
        JTextFieldOperator txtWord = new JTextFieldOperator(frmAnagrams, 0);
        JLabelOperator lblGuess = new JLabelOperator(frmAnagrams, "Your Guess:");
        JTextFieldOperator txtGuess = new JTextFieldOperator((JTextField)lblGuess.getLabelFor());
        txtGuess.typeText("abstraction");
        new JButtonOperator(frmAnagrams, "Guess").push();
        JLabelOperator lblFeedback = new JLabelOperator(frmAnagrams, 2);
        String expectedMessage = "Correct! Try a new word!";
        assertEquals("Evaluation was wrong:", expectedMessage, lblFeedback.getText());
    }
    
    /** Test of About dialog
     * - wait for frame with "Anagrams" title
     * - push "File|About" main menu item
     * - wait for About dialog and close it
     */
    public void testAbout() {
        JFrameOperator frmAnagrams = new JFrameOperator("Anagrams");
        new JMenuBarOperator(frmAnagrams).pushMenuNoBlock("File|About");
        new JDialogOperator("About Anagrams").close();
    }

}
