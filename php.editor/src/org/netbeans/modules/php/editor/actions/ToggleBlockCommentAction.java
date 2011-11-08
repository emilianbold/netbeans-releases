/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.actions;

import java.awt.event.ActionEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.modules.php.editor.PHPLanguage;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Pisl
 */

@EditorActionRegistration(name = ExtKit.toggleCommentAction, mimeType="text/x-php5")
public class ToggleBlockCommentAction extends BaseAction{

    static final long serialVersionUID = -1L;
    static final private String FORCE_COMMENT = "force-comment";    //NOI18N
    static final private String FORCE_UNCOMMENT = "force-uncomment";    //NOI18N
    
    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        boolean processedHere = false;
        if (target != null) {
            if (!target.isEditable() || !target.isEnabled() || !(target.getDocument() instanceof BaseDocument)) {
                target.getToolkit().beep();
                return;
            }
            int offset = Utilities.isSelectionShowing(target) ? target.getSelectionStart() : target.getCaretPosition();
            final BaseDocument doc = (BaseDocument) target.getDocument();
            TokenSequence<PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, offset);
            if (ts != null) {
                ts.move(offset);
                ts.moveNext();
                if (ts.token().id() != PHPTokenId.T_INLINE_HTML) {
                    boolean newLine = false;
                    while (ts.movePrevious() && ts.token().id() != PHPTokenId.PHP_OPENTAG && !newLine) {
                        if(ts.token().id() == PHPTokenId.WHITESPACE 
                                && ts.token().text().toString().contains("\n")) {
                            newLine = true;
                        }
                    }
                    if (!newLine && ts.token().id() == PHPTokenId.PHP_OPENTAG) {
                        processedHere = true;
                        final int changeOffset = ts.offset() + ts.token().length();
                        final boolean lineComment = (ts.moveNext() && ts.token().id() == PHPTokenId.PHP_LINE_COMMENT)
                                || (ts.token().id() == PHPTokenId.WHITESPACE && ts.moveNext() && ts.token().id() == PHPTokenId.PHP_LINE_COMMENT);
                       
                        final int length = lineComment ? ts.offset() + ts.token().length() - changeOffset : 0;
                        doc.runAtomic(new Runnable() {

                            public @Override
                            void run() {
                                try {
                                    if (!lineComment) {
                                        doc.insertString(changeOffset, " " + PHPLanguage.LINE_COMMENT_PREFIX, null);
                                    } else {
                                        doc.remove(changeOffset, length);
                                    }
                                    
                                } catch (BadLocationException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        });
                    }
                }
            }
        }
        if(!processedHere) {
            BaseAction action = new org.netbeans.modules.csl.api.ToggleBlockCommentAction();
            if (getValue(FORCE_COMMENT) != null) {   
                action.putValue(FORCE_COMMENT, getValue(FORCE_COMMENT)); 
            }
            if (getValue(FORCE_UNCOMMENT) != null) {
                action.putValue(FORCE_UNCOMMENT, getValue(FORCE_UNCOMMENT));
            }
            action.actionPerformed(evt, target);
        }
    }
    
}

