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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.actions;

import java.awt.Container;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.junit.NbTestSuite;

/** Test of ShowDescriptionAreaAction class.
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class ShowDescriptionAreaActionTest extends JellyTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public ShowDescriptionAreaActionTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new ShowDescriptionAreaActionTest("testPerformPopup"));
        return suite;
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    /** simple test case
     */
    public void testPerformPopup() {
        Node node = new Node(new SourcePackagesNode("SampleProject"), "sample1|SampleClass1.java"); // NOI18N
        new PropertiesAction().perform(node);
        PropertySheetOperator pso = new PropertySheetOperator("SampleClass1.java"); // NOI18N
        // check whether description area is shown
        pso.lblDescriptionHeader();
        new ShowDescriptionAreaAction().perform(pso);
        // check whether description area is not shown
        Object label = JLabelOperator.findJLabel((Container)pso.getSource(), ComponentSearcher.getTrueChooser("JLabel")); //NOI18N
        new ShowDescriptionAreaAction().perform(pso);
        // check whether description area is shown
        pso.lblDescriptionHeader();
        pso.close();
        assertNull("Description area not dismissed.", label); // NOI18N
    }
}
