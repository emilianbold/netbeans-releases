/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package guitest;

import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.junit.NbTestSuite;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;


public class GuiTest extends JellyTestCase {
    
    
    public GuiTest(String testName) {
        super(testName);
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new NbTestSuite(GuiTest.class));
    }
    
    public void setUp() {
        /*
         * No special setup needed. JellyTestCase do following initialization:
         *  - Jemmy/jelly output is redirected to jemmy.log file in JUnit working directory
         *  - if an exception is thrown during test execution, screen shot is taken
         *  - all modal dialogs are closed
         *  - wait at least 1000 ms between test cases
         *  - dump xml hierarchy of all components (disabled by default)
         */
    }
    
    /** Simple gui test. It checks properties of HTTP Server node in the runtime 
     * window. If something goes wrong, runtime exception is thrown
     * from jemmy or jelly. It is caught in JellyTestCase, screenshot is created
     * and test finishes with status "fail".
     * You can also use assertXXX() and fail() methods to indicate a failure
     * within the test.
     */
    public void testPart1() {
        Node rootNode = RuntimeTabOperator.invoke().getRootNode();
        Node httpNode = new Node(rootNode, "HTTP Server");
        new PropertiesAction().perform(httpNode);
        PropertySheetOperator pso = new PropertySheetOperator("HTTP Server");
        Property p = new Property(pso, "Hosts With Granted Access");
        String value = p.getValue();
        pso.close();
        String expectedValue = "Selected Hosts: ";
        assertEquals("Wrong value", expectedValue, value);
    }
    
    /** Second test only opens About dialog and close it.
     * Throws RuntimeException from jemmy/jelly if something wrong
     * happenes (timeout expired and so on).
     */
    public void testPart2() {
        new ActionNoBlock("Help|About", null).perform();
        new NbDialogOperator("About").close();
    }
    
    /** It should fail. It is only for demo purposes.
     */
    public void testPart3() {
        log("It should fail. It is only for demo purposes.");
        // change timeout not to wait so long
        JemmyProperties.setCurrentTimeout("JMenuOperator.PushMenuTimeout", 5000);
        new Action("Help|Non existing", null).perform();
    }
}
