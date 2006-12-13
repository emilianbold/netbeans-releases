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
package org.netbeans.modules.editor.hints;

import java.util.Arrays;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.NbEditorDocument;

/**
 *
 * @author Jan Lahoda
 */
public class HintsControllerImplTest extends NbTestCase {
    
    public HintsControllerImplTest(String name) {
        super(name);
    }
    
    public void testComputeLineSpan() throws Exception {
        doTestComputeLineSpan(new DocumentCreator() {
            public Document createDocument() {
                return new NbEditorDocument(BaseKit.class);
            }
        });
        doTestComputeLineSpan(new DocumentCreator() {
            public Document createDocument() {
                return new DefaultStyledDocument();
            }
        });
    }
    
    private void doTestComputeLineSpan(DocumentCreator creator) throws Exception {
        Document bdoc = creator.createDocument();
        
        bdoc.insertString(0, "  1234  \n 567\n567 \n456", null);
        
        assertSpan(bdoc, 1,  2,  6);
        assertSpan(bdoc, 2, 10, 13);
        assertSpan(bdoc, 3, 14, 17);
        assertSpan(bdoc, 4, 19, 22);
        
        bdoc = creator.createDocument();
        
        bdoc.insertString(0, "456", null);
        
        assertSpan(bdoc, 1, 0, 3);
        
        bdoc = creator.createDocument();
        
        bdoc.insertString(0, " ", null);
        
        assertSpan(bdoc, 1, 0, 0);
    }
    
    private static interface DocumentCreator {
        public Document createDocument();
    }
    
    private void assertSpan(Document doc, int lineNumber, int... expectedSpan) throws Exception {
        int[] returnedSpan = HintsControllerImpl.computeLineSpan(doc, lineNumber);
        
        assertTrue(Arrays.toString(returnedSpan), Arrays.equals(expectedSpan, returnedSpan));
    }
}
