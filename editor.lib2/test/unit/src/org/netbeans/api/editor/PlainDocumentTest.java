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

package org.netbeans.api.editor;

import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;
import org.netbeans.junit.NbTestCase;

/**
 * Tests of curiosities of Swing's PlainDocument implementation.
 *
 * @author Miloslav Metelka
 */
public class PlainDocumentTest extends NbTestCase {
    
    public PlainDocumentTest(String name) {
        super(name);
    }
    
    public void testCuriosities() throws Exception {
        // Test position at offset 0 does not move after insert
        Document doc = new PlainDocument();
        doc.insertString(0, "test", null);
        Position pos = doc.createPosition(0);
        assertEquals(0, pos.getOffset());
        doc.insertString(0, "a", null);
        assertEquals(0, pos.getOffset());
        
        // Test there is an extra newline above doc.getLength()
        assertEquals("\n", doc.getText(doc.getLength(), 1));
        assertEquals("atest\n", doc.getText(0, doc.getLength() + 1));
        
        // Test the last line element contains the extra newline
        Element lineElem = doc.getDefaultRootElement().getElement(0);
        assertEquals(0, lineElem.getStartOffset());
        assertEquals(doc.getLength() + 1, lineElem.getEndOffset());
    }
    
}
