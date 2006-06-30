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

package org.netbeans.modules.xml.tools.actions;

import java.util.Arrays;
import org.netbeans.tests.xml.XTest;
import org.openide.nodes.Node;

public abstract class AbstractCheckTest extends XTest {

    /** Creates new AbstractCheckTest */
    public AbstractCheckTest(String testName) {
        super(testName);
    }

    /** Check all selected nodes. */
    abstract protected QaIOReporter performAction(Node[] nodes);

    // LIBS ////////////////////////////////////////////////////////////////////

    /** Checks document located in 'data' folder. */
    protected void performAction(String name, int bugCount) {
        QaIOReporter reporter = performAction(name);
        String message = "\nUnexpected bug count, expected: " + bugCount + " reported: "+ reporter.getBugCount();
        assertEquals(message, bugCount, reporter.getBugCount());
    }
    
    /** Checks document located in 'data' folder. */
    protected void performAction(String name, int[] errLines) {
        QaIOReporter reporter = performAction(name);
        int[] report = reporter.getErrLines();
        Arrays.sort(errLines);
        Arrays.sort(report);
        
        if (!!! Arrays.equals(errLines, report)) {
            String pattern = arrayToString(errLines);
            String result = arrayToString(report);
            fail("\nUnexpected Validation result.\nPattern: " + pattern + "\nResult:  " + result);
        }
    }
    
    /** Checks document located in 'data' folder. */
    protected QaIOReporter performAction(String name) {
        Node node = null;
        try {
            node = TestUtil.THIS.findData(name).getNodeDelegate();
        } catch (Exception ex) {
            ex.printStackTrace(dbg);
            fail("Cannot get Node Delegate for 'data/" + name +"' due:\n" + ex);
        }
        QaIOReporter reporter = performAction(new Node[] {node});
        return reporter;
    }
    
    private static String arrayToString(int[] array) {
        StringBuffer buf = new StringBuffer("[");
        for (int i = 0;  i < array.length; i++) {
            buf.append(array[i]);
            buf.append(", ");
        }
        buf.replace(buf.length() - 2, buf.length(), "]");
        return buf.toString();
    }
}
