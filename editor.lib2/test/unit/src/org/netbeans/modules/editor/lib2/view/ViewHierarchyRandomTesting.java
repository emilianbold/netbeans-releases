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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.editor.lib2.view;

import javax.swing.JEditorPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.undo.UndoManager;
import org.netbeans.lib.editor.util.random.DocumentTesting;
import org.netbeans.lib.editor.util.random.EditorPaneTesting;
import org.netbeans.lib.editor.util.random.RandomTestContainer;
import org.netbeans.lib.editor.util.random.RandomTestContainer.Context;
import org.netbeans.lib.editor.util.random.RandomText;

/**
 *
 * @author Miloslav Metelka
 */
public class ViewHierarchyRandomTesting {

    public static RandomTestContainer createContainer(EditorKit kit) throws Exception {
        // Set the property for synchronous highlights firing since otherwise
        // the repeatability of problems with view hierarchy is none or limited.
        System.setProperty("org.netbeans.editor.sync.highlights", "true");
        System.setProperty("org.netbeans.editor.linewrap.edt", "true");


        RandomTestContainer container = EditorPaneTesting.initContainer(null, kit);
        DocumentTesting.initContainer(container);
        return container;
    }

    public static void disableHighlighting(RandomTestContainer container) throws Exception {
        final JEditorPane pane = EditorPaneTesting.getEditorPane(container);
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                pane.putClientProperty("HighlightsLayerIncludes", "");
            }
        });
    }

    public static void initUndoManager(RandomTestContainer container) throws Exception {
        UndoManager undoManager = new UndoManager();
        Document doc = DocumentTesting.getDocument(container);
        doc.addUndoableEditListener(undoManager);
        doc.putProperty(UndoManager.class, undoManager);
    }

    public static void initRandomText(RandomTestContainer container) throws Exception {
//        container.addOp(new Op());
        container.addCheck(new ViewHierarchyCheck());
        RandomText randomText = RandomText.join(
                RandomText.lowerCaseAZ(3),
                RandomText.spaceTabNewline(1),
                RandomText.phrase(" \n\n\n", 1),
                RandomText.phrase(" \t\tabcdef\t", 1)
        );
        container.putProperty(RandomText.class, randomText);
    }

    public static RandomTestContainer.Round addRound(RandomTestContainer container) throws Exception {
        RandomTestContainer.Round round = container.addRound();
        round.setOpCount(100);
        round.setRatio(DocumentTesting.INSERT_CHAR, 5);
        round.setRatio(DocumentTesting.INSERT_TEXT, 3);
        round.setRatio(DocumentTesting.INSERT_PHRASE, 3);
        round.setRatio(DocumentTesting.REMOVE_CHAR, 3);
        round.setRatio(DocumentTesting.REMOVE_TEXT, 1);
        round.setRatio(DocumentTesting.UNDO, 1);
        round.setRatio(DocumentTesting.REDO, 1);

        round.setRatio(EditorPaneTesting.TYPE_CHAR, 10);
        EditorPaneTesting.setActionRatio(round, DefaultEditorKit.insertBreakAction, 1);
        EditorPaneTesting.setActionRatio(round, DefaultEditorKit.insertTabAction, 1);
        EditorPaneTesting.setActionRatio(round, DefaultEditorKit.deleteNextCharAction, 1);
        EditorPaneTesting.setActionRatio(round, DefaultEditorKit.deletePrevCharAction, 1);

        round.setRatio(EditorPaneTesting.MOVE, 20);
        round.setRatio(EditorPaneTesting.SELECT, 20);
        round.setRatio(EditorPaneTesting.SET_CARET_OFFSET, 1);
        return round;
    }

    public static void testFixedScenarios(RandomTestContainer container) throws Exception {
        // Fixed scenario - last undo throwed exc.
        RandomTestContainer.Context gContext = container.context();
        JEditorPane pane = EditorPaneTesting.getEditorPane(container);
        // Insert initial text into doc
//        DocumentTesting.insert(container.context(), 0, "abc\ndef\n\nghi");
        DocumentTesting.insert(gContext, 0, "\n\n\n\n\n");
        DocumentTesting.remove(gContext, 0, DocumentTesting.getDocument(gContext).getLength());

        // Check for an error caused by delete a line-2-begining and insert at line-1-end and two undos
        DocumentTesting.insert(gContext, 0, "a\nb\n\n");
        EditorPaneTesting.setCaretOffset(gContext, 2);
        EditorPaneTesting.performAction(gContext, pane, DefaultEditorKit.deleteNextCharAction);
        EditorPaneTesting.moveOrSelect(gContext, SwingConstants.WEST, false); // Should go to end of first line
        EditorPaneTesting.typeChar(gContext, 'c');
        DocumentTesting.undo(gContext, 1);
        DocumentTesting.undo(gContext, 1); // This throwed ISE for plain text mime type
    }

    private static final class ViewHierarchyCheck extends RandomTestContainer.Check {

        @Override
        protected void check(final Context context) throws Exception {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    JEditorPane pane = EditorPaneTesting.getEditorPane(context);
                    DocumentView docView = DocumentView.get(pane);
                    docView.checkIntegrity();
                    // View hierarchy dump
//                    System.err.println("\nView Hierarchy Dump:\n" + docView.toStringDetail() + "\n");
                }
            });

        }


    }

}
