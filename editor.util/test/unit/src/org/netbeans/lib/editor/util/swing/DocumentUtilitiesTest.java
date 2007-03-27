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

package org.netbeans.lib.editor.util.swing;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.PlainDocument;
import org.netbeans.junit.NbTestCase;

public class DocumentUtilitiesTest extends NbTestCase {

    public DocumentUtilitiesTest(String testName) {
        super(testName);
    }

    public void testIsReadLocked() throws Exception {
        PlainDocument doc = new PlainDocument();
        assertFalse(DocumentUtilities.isReadLocked(doc));
        doc.readLock();
        try {
            assertTrue(DocumentUtilities.isReadLocked(doc));
        } finally {
            doc.readUnlock();
        }
    }
    
    public void testIsWriteLocked() throws Exception {
        PlainDocument doc = new PlainDocument();
        assertFalse(DocumentUtilities.isWriteLocked(doc));
        doc.addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent evt) {
                assertTrue(DocumentUtilities.isWriteLocked(evt.getDocument()));
            }
            public void removeUpdate(DocumentEvent evt) {
            }
            public void changedUpdate(DocumentEvent evt) {
            }
        });
        doc.insertString(0, "test", null);
    }

}
