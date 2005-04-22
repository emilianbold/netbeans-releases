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
