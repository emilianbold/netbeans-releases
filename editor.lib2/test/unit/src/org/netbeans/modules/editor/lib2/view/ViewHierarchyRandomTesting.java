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
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.undo.UndoManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.editor.util.random.DocumentTesting;
import org.netbeans.lib.editor.util.random.EditorPaneTesting;
import org.netbeans.lib.editor.util.random.RandomTestContainer;
import org.netbeans.lib.editor.util.random.RandomText;

/**
 *
 * @author Miloslav Metelka
 */
public class ViewHierarchyRandomTesting extends NbTestCase {

    public ViewHierarchyRandomTesting(String testName) {
        super(testName);
    }

    public static RandomTestContainer createContainer(EditorKit kit) throws Exception {
        RandomTestContainer container = EditorPaneTesting.initContainer(null, kit);
        DocumentTesting.initContainer(container);
        return container;
    }

    public static void testRandomMods(RandomTestContainer container) throws Exception {
//        container.addOp(new Op());
//        container.addCheck(new Check());
        UndoManager undoManager = new UndoManager();
        Document doc = DocumentTesting.getDocument(container);
        doc.addUndoableEditListener(undoManager);
        doc.putProperty(UndoManager.class, undoManager);

        RandomText randomText = RandomText.join(
                RandomText.lowerCaseAZ(3),
                RandomText.spaceTabNewline(1),
                RandomText.phrase(" \n\n\n", 1),
                RandomText.phrase(" \t\tabcdef\t", 1)
        );
        container.putProperty(RandomText.class, randomText);

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

        // Fixed scenario - last undo throwed exc.
        RandomTestContainer.Context gContext = container.context();
        JEditorPane pane = EditorPaneTesting.getEditorPane(container);
        // Insert initial text into doc
//        DocumentTesting.insert(container.context(), 0, "abc\ndef\n\nghi");
        DocumentTesting.insert(gContext, 0, "a\nb\n\n");
        EditorPaneTesting.setCaretOffset(gContext, 2);
        EditorPaneTesting.performAction(gContext, pane, DefaultEditorKit.deleteNextCharAction);
        EditorPaneTesting.moveOrSelect(gContext, SwingConstants.WEST, false); // Should go to end of first line
        EditorPaneTesting.typeChar(gContext, 'c');
        DocumentTesting.undo(gContext, 1);
        DocumentTesting.undo(gContext, 1); // This throwed ISE for plain text mime type

        container.run(1269936518464L);
//        container.run(1269878830601L);
//        container.run(1269875047713L);
        container.run(0L); // Random operation
    }

}
