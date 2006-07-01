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

package org.openide.text;

import javax.swing.text.*;

import junit.framework.*;

import org.netbeans.junit.*;


/** Testing LineSet impl for CloneableEditorSupport.
 *
 * @author Jaroslav Tulach
 */
public class NbDocumentTest extends NbTestCase {

    private StyledDocument doc = new DefaultStyledDocument();

    public NbDocumentTest(String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        if (args.length == 1) {
            junit.textui.TestRunner.run (new NbDocumentTest (args[0]));
        }
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(NbDocumentTest.class);
        return suite;
    }
    

    protected void setUp () {
	doc = createStyledDocument();
    }
    
    protected StyledDocument createStyledDocument() {
        return new DefaultStyledDocument();
    }


    public void testMarkGuardedAndBack() throws Exception {
        doc.insertString (0, "Line1\nLine2\n", null);
    
        assertEquals ("Document has correct number of lines ",
	        3, doc.getDefaultRootElement().getElementCount());
        
        NbDocument.markGuarded(doc, 0, doc.getLength());

        assertEquals ("Document has correct number of lines ",
	        3, doc.getDefaultRootElement().getElementCount());

        NbDocument.unmarkGuarded(doc, 0, doc.getLength());

        assertEquals ("Document has correct number of lines ",
	        3, doc.getDefaultRootElement().getElementCount());

    }
}
