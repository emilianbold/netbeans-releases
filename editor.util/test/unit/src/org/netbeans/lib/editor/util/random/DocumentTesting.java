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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.lib.editor.util.random;

import java.util.Random;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.random.RandomTestContainer.Context;

public class DocumentTesting {

    /** Name of op that inserts a random single char into document. */
    public static final String INSERT_CHAR = "doc-insert-char";

    /**
     * Name of op that inserts multiple random chars at once into document.
     * INSERT_TEXT_MAX_LENGTH gives how much chars at once at maximum.
     */
    public static final String INSERT_TEXT = "doc-insert-text";

    /** Name of op that inserts a random phrase into document. */
    public static final String INSERT_PHRASE = "doc-insert-phrase";

    /** Name of op that removes a single char from document. */
    public static final String REMOVE_CHAR = "doc-remove-char";

    /**
     * Name of op that removes multiple chars at once from document.
     * REMOVE_TEXT_MAX_LENGTH gives how much chars at once at maximum.
     */
    public static final String REMOVE_TEXT = "doc-remove-text";

    /** Undo by doc.getProperty(UndoManager.class). */
    public static final String UNDO = "doc-undo";

    /** Redo by doc.getProperty(UndoManager.class). */
    public static final String REDO = "doc-redo";

    /** Maxium number of chars inserted by INSERT_TEXT op. */
    public static final String INSERT_TEXT_MAX_LENGTH = "doc-insert-text-max-length";

    /** Maxium number of chars removed by REMOVE_TEXT op. */
    public static final String REMOVE_TEXT_MAX_LENGTH = "doc-remove-text-max-length";

    /** Maximum number of undo/redo (4 by default) to be performed at once. */
    public static final String UNDO_REDO_COUNT = "doc-undo-redo-count";

    /** Ratio (0.3 by default) with which there will be inverse operation
     * to just performed undo/redo.
     */
    public static final String UNDO_REDO_INVERSE_RATIO = "doc-undo-redo-inverse-ratio";

    /** java.lang.Boolean whether whole document text should be logged. */
    public static final String LOG_DOC = "doc-log-doc";

    public static RandomTestContainer initContainer(RandomTestContainer container) {
        if (container == null)
            container = new RandomTestContainer();
        Document doc = getDocument(container);
        if (doc == null) {
            doc = new TestDocument();
            container.putProperty(Document.class, doc);
        }
        
        container.addOp(new InsertOp(INSERT_CHAR));
        container.addOp(new InsertOp(INSERT_TEXT));
        container.addOp(new InsertOp(INSERT_PHRASE));
        container.addOp(new RemoveOp(REMOVE_CHAR));
        container.addOp(new RemoveOp(REMOVE_TEXT));
        container.addOp(new UndoRedo(UNDO));
        container.addOp(new UndoRedo(REDO));
        return container;
    }

    /**
     * Get document from test container by consulting either an editor pane's document
     * or a document property.
     *
     * @param provider non-null property provider
     * @return document instance or null.
     */
    public static Document getDocument(PropertyProvider provider) {
        JEditorPane pane = provider.getInstanceOrNull(JEditorPane.class);
        if (pane != null) {
            return pane.getDocument();
        } else {
            return provider.getInstanceOrNull(Document.class);
        }
    }

    public static boolean isLogDoc(PropertyProvider provider) {
        return Boolean.TRUE.equals(provider.getPropertyOrNull(LOG_DOC));
    }

    public static void setLogDoc(PropertyProvider provider, boolean logDoc) {
        provider.putProperty(LOG_DOC, logDoc);
    }

    public static void insert(Context context, int offset, String text) throws Exception {
        Document doc = getDocument(context);
        // Possibly do logging
        if (context.isLogOp()) {
            int beforeTextStartOffset = Math.max(offset - 5, 0);
            String beforeText = doc.getText(beforeTextStartOffset, offset - beforeTextStartOffset);
            int afterTextEndOffset = Math.min(offset + 5, doc.getLength());
            String afterText = doc.getText(offset, afterTextEndOffset - offset);
            StringBuilder sb = context.logOpBuilder();
            sb.append(" INSERT(").append(offset);
            sb.append(", ").append(text.length()).append("): \"");
            CharSequenceUtilities.debugText(sb, text);
            sb.append("\" text-around: \"");
            CharSequenceUtilities.debugText(sb, beforeText);
            sb.append('|');
            CharSequenceUtilities.debugText(sb, afterText);
            sb.append("\"\n");
            context.logOp(sb);
        }
        if (isLogDoc(context)) {
            StringBuilder sb = new StringBuilder(doc.getLength() + offset + 100);
            String beforeOffsetText = CharSequenceUtilities.debugText(doc.getText(0, offset));
            for (int i = 0; i < beforeOffsetText.length(); i++) {
                sb.append('-');
            }
            sb.append("\\ \"");
            CharSequenceUtilities.debugText(sb, text);
            sb.append("\"\n\"");
            sb.append(beforeOffsetText);
            CharSequenceUtilities.debugText(sb,
                    doc.getText(offset, doc.getLength() - offset));
            sb.append("\"\n");
            context.logOp(sb);
        }

        doc.insertString(offset, text, null);
    }

    public static void remove(Context context, int offset, int length) throws Exception {
        Document doc = getDocument(context);
        // Possibly do logging
        if (context.isLogOp()) {
            StringBuilder sb = context.logOpBuilder();
            sb.append(" REMOVE(").append(offset).append(", ").append(length).append("): \"");
            CharSequenceUtilities.debugText(sb, doc.getText(offset, length));
            sb.append("\"\n");
            context.logOp(sb);
        }
        if (isLogDoc(context)) {
            StringBuilder sb = new StringBuilder(doc.getLength() + offset + 100);
            String beforeOffsetText = CharSequenceUtilities.debugText(doc.getText(0, offset));
            for (int i = 0; i <= beforeOffsetText.length(); i++) {
                sb.append('-');
            }
            for (int i = 0; i < length; i++) {
                sb.append('x');
            }
            sb.append("\n\"");
            sb.append(beforeOffsetText);
            CharSequenceUtilities.debugText(sb,
                    doc.getText(offset, doc.getLength() - offset));
            sb.append("\"\n");
            context.logOp(sb);
        }

        doc.remove(offset, length);
    }

    public static void undo(Context context, int count) {
        Document doc = getDocument(context);
        UndoManager undoManager = (UndoManager) doc.getProperty(UndoManager.class);
        logUndoRedoOp(context, "UNDO", count);
        while (undoManager.canUndo() && --count >= 0) {
            undoManager.undo();
        }
        logPostUndoRedoOp(context, count);
    }

    public static void redo(Context context, int count) {
        Document doc = getDocument(context);
        UndoManager undoManager = (UndoManager) doc.getProperty(UndoManager.class);
        logUndoRedoOp(context, "REDO", count);
        if (undoManager.canRedo() && --count >= 0) {
            undoManager.redo();
        }
        logPostUndoRedoOp(context, count);
    }

    private static void logUndoRedoOp(Context context, String opType, int count) {
        if (context.isLogOp()) {
            StringBuilder sb = context.logOpBuilder();
            sb.append(opType);
            sb.append(' ').append(count).append(" times");
            sb.append('\n');
            context.logOp(sb);
        }
    }

    private static void logPostUndoRedoOp(Context context, int remainingCount) {
        if (remainingCount > 0 && context.isLogOp()) {
            StringBuilder sb = new StringBuilder(100);
            sb.append(remainingCount).append(" unfinished");
            sb.append('\n');
            context.logOp(sb);
        }
    }

    final static class InsertOp extends RandomTestContainer.Op {

        public InsertOp(String name) {
            super(name);
        }

        @Override
        protected void run(Context context) throws Exception {
            Document doc = getDocument(context);
            Random random = context.container().random();
            int offset = random.nextInt(doc.getLength() + 1);
            RandomText randomText = context.getInstance(RandomText.class);
            String text;
            if (INSERT_CHAR == name()) { // Just use ==
                text = randomText.randomText(random, 1);
            } else if (INSERT_TEXT == name()) { // Just use ==
                Integer maxLength = (Integer) context.getPropertyOrNull(INSERT_TEXT_MAX_LENGTH);
                if (maxLength == null)
                    maxLength = Integer.valueOf(10);
                int textLength = random.nextInt(maxLength) + 1;
                text = randomText.randomText(random, textLength);
            } else if (INSERT_PHRASE == name()) { // Just use ==
                text = randomText.randomPhrase(random);
            } else {
                throw new IllegalStateException("Unexpected op name=" + name());
            }
            insert(context, offset, text);
        }

    }

    final static class RemoveOp extends RandomTestContainer.Op {

        public RemoveOp(String name) {
            super(name);
        }

        @Override
        protected void run(Context context) throws Exception {
            Document doc = getDocument(context);
            int docLen = doc.getLength();
            if (docLen == 0)
                return; // Nothing to possibly remove
            Random random = context.container().random();
            int length;
            if (REMOVE_CHAR == name()) { // Just use ==
                length = 1;
            } else if (REMOVE_TEXT == name()) { // Just use ==
                Integer maxLength = (Integer) context.getPropertyOrNull(REMOVE_TEXT_MAX_LENGTH);
                if (maxLength == null)
                    maxLength = Integer.valueOf(10);
                if (maxLength > docLen)
                    maxLength = Integer.valueOf(docLen);
                length = random.nextInt(maxLength) + 1;
            } else {
                throw new IllegalStateException("Unexpected op name=" + name());
            }
            int offset = random.nextInt(docLen - length + 1);
            remove(context, offset, length);
        }

    }

    final static class UndoRedo extends RandomTestContainer.Op {

        UndoRedo(String name) {
            super(name);
        }

        @Override
        protected void run(Context context) throws Exception {
            Integer maxCount = context.getProperty(UNDO_REDO_COUNT, Integer.valueOf(4));
            Random random = context.container().random();
            int count = random.nextInt(maxCount + 1);
            int inverseCount = (count > 0) ? random.nextInt(count + 1) : 0;
            if (UNDO == name()) { // Just use ==
                undo(context, count);
                redo(context, inverseCount);
            } else if (REDO == name()) { // Just use ==
                redo(context, count);
                undo(context, inverseCount);
            } else {
                throw new IllegalStateException("Unexpected op name=" + name());
            }
        }

    }

}
