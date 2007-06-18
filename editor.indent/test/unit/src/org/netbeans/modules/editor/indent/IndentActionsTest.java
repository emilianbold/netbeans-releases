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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.indent;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import org.netbeans.api.editor.indent.*;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseKit;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.editor.indent.IndentTestMimeDataProvider;
import org.netbeans.spi.editor.indent.Context;
import org.netbeans.spi.editor.indent.ExtraLock;
import org.netbeans.spi.editor.indent.IndentTask;

/**
 *
 * @author Miloslav Metelka
 */
public class IndentActionsTest extends NbTestCase {
    
    private static final String MIME_TYPE = "text/x-test-actions";

    public IndentActionsTest(String name) {
        super(name);
    }

    public void testIndentActions() {
        // Must run in AWT thread (BaseKit.install() checks for that)
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        testIndentActions();
                    }
                }
            );
            return;
        }

        assertTrue(SwingUtilities.isEventDispatchThread());
        TestIndentTask.TestFactory factory = new TestIndentTask.TestFactory();
        IndentTestMimeDataProvider.addInstances(MIME_TYPE, factory);
        TestKit kit = new TestKit();
        JEditorPane pane = new JEditorPane();
        pane.setEditorKit(kit);
        assertEquals(MIME_TYPE, pane.getDocument().getProperty("mimeType"));
        //doc.putProperty("mimeType", MIME_TYPE);
        
        // Test insert new line action
        Action a = kit.getActionByName(BaseKit.insertBreakAction);
        assertNotNull(a);
        a.actionPerformed(new ActionEvent(pane, 0, ""));
        // Check that the factory was used
        assertTrue(factory.lastCreatedTask.indentPerformed);

        // Test reformat action
         a = kit.getActionByName(BaseKit.formatAction);
        assertNotNull(a);
        a.actionPerformed(new ActionEvent(pane, 0, ""));

    }

    private static final class TestIndentTask implements IndentTask {
        
        private Context context;
        
        TestExtraLocking lastCreatedLocking;
        
        boolean indentPerformed;

        TestIndentTask(Context context) {
            this.context = context;
        }

        public void reindent() throws BadLocationException {
            assertTrue(lastCreatedLocking.locked);
            context.document().insertString(0, " ", null);
            indentPerformed = true;
        }
        
        public ExtraLock indentLock() {
            return (lastCreatedLocking = new TestExtraLocking());
        }
        
        static final class TestFactory implements IndentTask.Factory {
            
            static TestIndentTask lastCreatedTask;

            public IndentTask createTask(Context context) {
                return (lastCreatedTask = new TestIndentTask(context));
            }
            
        }

    }
    
    private static final class TestExtraLocking implements ExtraLock {
        
        Boolean locked;
        
        public Boolean locked() {
            return locked;
        }

        public void lock() {
            if (locked != null)
                assertFalse(locked);
            locked = true;
        }

        public void unlock() {
            assertTrue(locked);
            locked = false;
        }
        
    }
    
    private static final class TestKit extends NbEditorKit {
        
        public String getContentType() {
            return MIME_TYPE;
        }
        
    }
}
