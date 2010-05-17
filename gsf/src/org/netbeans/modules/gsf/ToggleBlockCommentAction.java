/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.gsf;

import java.awt.event.ActionEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.gsf.spi.CommentHandler;
import org.openide.util.Exceptions;

public class ToggleBlockCommentAction extends ExtKit.ToggleCommentAction {

    static final long serialVersionUID = -1L;
    private final CommentHandler commentHandler;
    private final int COMMENT_START_LENGTH;
    private final int COMMENT_END_LENGTH;

    public ToggleBlockCommentAction(CommentHandler commentHandler) {
        super("");
        this.COMMENT_START_LENGTH = commentHandler.getCommentStartDelimiter().length();
        this.COMMENT_END_LENGTH = commentHandler.getCommentEndDelimiter().length();
        this.commentHandler = commentHandler;
    }

    @Override
    public void actionPerformed(ActionEvent evt, final JTextComponent target) {
        if (target != null) {
            if (!target.isEditable() || !target.isEnabled()) {
                target.getToolkit().beep();
                return;
            }

            final Caret caret = target.getCaret();
            final BaseDocument doc = (BaseDocument) target.getDocument();

            int from = Utilities.isSelectionShowing(caret) ? target.getSelectionStart() : caret.getDot();
            int to = Utilities.isSelectionShowing(caret) ? target.getSelectionEnd() : caret.getDot();

            boolean lineSelection = false;
            if (from == to) {
                //no selection
                if (!isInComment(doc, from)) {
                    try {
                        //check for commenting empty line
                        if (Utilities.isRowEmpty(doc, from)) {
                            return;
                        }

                        //extend the range to the whole line
                        from = Utilities.getFirstNonWhiteFwd(doc, Utilities.getRowStart(doc, from));
                        to = Utilities.getFirstNonWhiteBwd(doc, Utilities.getRowEnd(doc, to)) + 1;
                        lineSelection = true;
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }

            final int comments[] = commentHandler.getCommentBlocks(doc, from, to);

            assert comments != null;

//            debug(doc, comments, from, to);

            check(comments, from, to);

            final int _from = from;
            final int _to = to;
            final boolean _lineSelection = lineSelection;

            doc.runAtomic(new Runnable() {

                public void run() {
                    try {
                        int[] commentRange = getCommentRange(comments, _from);
                        if (commentRange == null) {
                            //comment
                            comment(target, doc, comments, _from, _to, _lineSelection);
                        } else if (comments.length > 0) {
                            //uncomment
                            uncomment(target, doc, comments, _from, _to, _lineSelection);
                        }
                    } catch (BadLocationException e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            });


        }

    }

    private void comment(JTextComponent target, BaseDocument doc, int[] comments, int from, int to, boolean lineSelection) throws BadLocationException {
//        System.out.println("comment");

        int diff = 0;

        //put the comment start
        diff += insert(doc, from, commentHandler.getCommentStartDelimiter());

        for (int i = 0; i < comments.length; i += 2) {
            int commentStart = comments[i];
            int commentEnd = comments[i + 1];

            diff += remove(doc, commentStart + diff, COMMENT_START_LENGTH);

            if (commentEnd <= to) {
                diff += remove(doc, commentEnd + diff - COMMENT_END_LENGTH, COMMENT_END_LENGTH);
            }

        }

        //add closing comment if the last comment doesn't contain the 'to' offset
        if (comments.length == 0 || comments[comments.length - 1] <= to) {
            diff += insert(doc, to + diff, commentHandler.getCommentEndDelimiter());
        }

        if (!lineSelection) {
            //update the selection range, we always add the starting delimiter out of the selection
            target.setSelectionStart(from);
            target.setSelectionEnd(to + diff);
        }

    }

    private void uncomment(JTextComponent target, BaseDocument doc, int[] comments, int from, int to, boolean lineSelection) throws BadLocationException {
//        System.out.println("uncomment");

        int diff = 0;

        //no selection handling
        if (from == to) {
            //extend the range to the only possible comment
            assert comments.length == 2;

            from = comments[0];
            to = comments[1];

            lineSelection = true;
        }

        if (comments[0] < from) {
            //we need to end the existing comment
            diff += insert(doc, from, commentHandler.getCommentEndDelimiter());
        }

        int selectionStart = from + diff;

        for (int i = 0; i < comments.length; i += 2) {
            int commentStart = comments[i];
            int commentEnd = comments[i + 1];

            if (commentStart >= from) {
                diff += remove(doc, commentStart + diff, COMMENT_START_LENGTH);
            }

            if (commentEnd <= to) {
                diff += remove(doc, commentEnd + diff - COMMENT_END_LENGTH, COMMENT_END_LENGTH);
            }

        }

        int selectionEnd = to + diff;
        //add opening comment if the last comment doesn't contain the 'to' offset
        if (comments[comments.length - 1] > to) {
            diff += insert(doc, to + diff, commentHandler.getCommentStartDelimiter());
        }

        if (!lineSelection) {
            //update the selection range, we always add the starting delimiter out of the selection
            target.setSelectionStart(selectionStart);
            target.setSelectionEnd(selectionEnd);
        }

    }

    private int insert(Document doc, int offset, String text) throws BadLocationException {
        doc.insertString(offset, text, null);
        return text.length();
    }

    private int remove(Document doc, int offset, int length) throws BadLocationException {
        doc.remove(offset, length);
        return -length;
    }

    private int[] getCommentRange(int[] comments, int offset) {
        //linear search
        for (int i = 0; i < comments.length; i++) {
            int from = comments[i];
            int to = comments[++i];

            if (from <= offset && to > offset) { //end offset exclusive
                return new int[]{from, to};
            }
        }

        return null; //not comment offset

    }

    private boolean isInComment(Document doc, int offset) {
        CharSequence text = DocumentUtilities.getText(doc); //shared instance, low cost
        int lastCommentStartIndex = CharSequenceUtilities.lastIndexOf(text, commentHandler.getCommentStartDelimiter(), offset);
        int lastCommentEndIndex = CharSequenceUtilities.lastIndexOf(text, commentHandler.getCommentEndDelimiter(), offset);

        return lastCommentStartIndex > -1 && (lastCommentStartIndex > lastCommentEndIndex || lastCommentEndIndex == -1);

    }

    private void debug(Document doc, int[] comments, int start, int end) {
        System.out.println("TOGGLE_COMENT [" + start + "-" + end + "]");
        for (int i = 0; i < comments.length; i++) {
            try {
                int from = comments[i];
                int to = comments[++i];
                if (from <= start && to > end) {
                    System.out.print("*");
                }
                System.out.print("[" + from + " - " + to + "]");
                System.out.println(doc.getText(from, to - from));
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        System.out.println("----------------");
    }

    private void check(int[] comments, int from, int to) {
        if (comments.length % 2 != 0) {
            throw new IllegalArgumentException("Comments array size must be even, e.g. contain just pairs.");
        }

        for (int i = 0; i < comments.length; i++) {
            int cfrom = comments[i];
            int cto = comments[++i];
            if (cfrom < from && cto < from || cto > to && cfrom > to) {
                throw new IllegalArgumentException("Comment [" + cfrom + " - " + cto + " is out of the range [" + from + " - " + to + "]!");
            }
        }
    }
}

