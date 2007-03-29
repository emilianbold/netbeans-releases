/*
 * SwitchTest.java
 *
 * Created on Mar 28, 2007, 12:11:27 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.netbeans.test.options.general;

import junit.textui.TestRunner;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jemmy.operators.DialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author ms159439
 */
public class SwitchTest extends NbTestCase {
    
    /**
     * Create new instance
     * @param testName
     */
    public SwitchTest(String testName) {
	super(testName);
    }
    
    /**
     * Returns su
     * @return
     */
    public static NbTestSuite suite() {
	NbTestSuite suite = new NbTestSuite(SwitchTest.class);
	return suite;
    }
    
    /**
     * This tests tries to detect unneccessary confirmation dialog
     * "Do you want to apply changes?
     *
     */
    public void testSwitchToAdvancedOptions() {
	OptionsOperator odop = OptionsOperator.invoke();
	odop.selectCategory("Miscellaneous"); //select 'Misc' panel
	JButtonOperator jbo = new JButtonOperator(odop, "Advanced Options");
	jbo.push();
	new org.netbeans.jemmy.EventTool().waitNoEvent(2000);
	assertFalse("Unnecessary question dialog when " +
		"changing ro 'Advanced OD'(IZ 99210)",
		odop.isVisible()); //modern OD should not be visible,
                                   //Advanced shloub be focused by now
    }
    
    /**
     * Main method for testing purposes
     * @param args 
     */
    public static void main(String[] args) {
	TestRunner.run(new NbTestSuite(SwitchTest.class));
    }

    
}
