/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;

/**
 * Test functionality of BaseDocument.
 *
 * @author Miloslav Metelka
 */
public class BaseDocumentTest extends NbTestCase {

    public BaseDocumentTest(String testName) {
        super(testName);
    }

    public void testGetText() throws Exception {
        BaseDocument doc = new BaseDocument(false, "text/plain");
        CharSequence text = DocumentUtilities.getText(doc);
        assertEquals(1, text.length());
        assertEquals('\n', text.charAt(0));

        text = DocumentUtilities.getText(doc);
        doc.insertString(0, "a\nb", null);
        for (int i = 0; i < doc.getLength() + 1; i++) {
            assertEquals(doc.getText(i, 1).charAt(0), text.charAt(i));
        }
    }

    public void testBreakAtomicLock() throws Exception {
        final BaseDocument doc = new BaseDocument(false, "text/plain");
        doc.runAtomic(new Runnable() {
            public @Override void run() {
                try {
                    doc.insertString(0, "test1", null);
                    doc.breakAtomicLock();
                } catch (BadLocationException e) {
                    // Expected
                }
            }
        });
        boolean failure = false;
        try {
            doc.runAtomic(new Runnable() {
                public @Override void run() {
                    throw new IllegalStateException("test");
                }
            });
            failure = true;
        } catch (Throwable t) {
            // Expected
        }
        if (failure) {
            throw new IllegalStateException("Unexpected");
        }
        doc.runAtomic(new Runnable() {
            public @Override void run() {
                try {
                    doc.insertString(0, "test1", null);
                    doc.insertString(10, "test2", null);
                } catch (BadLocationException e) {
                    // Expected
                }
            }
        });
    }

    public void testPropertyChangeEvents() {
        final List<PropertyChangeEvent> events = new LinkedList<PropertyChangeEvent>();
        final BaseDocument doc = new BaseDocument(false, "text/plain");
        final PropertyChangeListener l = new PropertyChangeListener() {
            public @Override void propertyChange(PropertyChangeEvent evt) {
                events.add(evt);
            }
        };

        DocumentUtilities.addPropertyChangeListener(doc, l);
        assertEquals("No events expected", 0, events.size());

        doc.putProperty("prop-A", "value-A");
        assertEquals("No event fired", 1, events.size());
        assertEquals("Wrong property name", "prop-A", events.get(0).getPropertyName());
        assertNull("Wrong old property value", events.get(0).getOldValue());
        assertEquals("Wrong new property value", "value-A", events.get(0).getNewValue());

        events.clear();
        DocumentUtilities.removePropertyChangeListener(doc, l);
        assertEquals("No events expected", 0, events.size());

        doc.putProperty("prop-B", "value-B");
        assertEquals("Expecting no events on removed listener", 0, events.size());
    }
}
