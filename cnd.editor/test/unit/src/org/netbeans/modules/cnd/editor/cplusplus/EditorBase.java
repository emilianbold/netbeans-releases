/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.editor.cplusplus;

import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.editor.indent.CppIndentTask;
import org.netbeans.modules.cnd.editor.indent.HotCharIndent;
import org.netbeans.modules.cnd.editor.options.EditorOptions;
import org.netbeans.modules.cnd.editor.reformat.Reformatter;
import org.netbeans.modules.cnd.test.base.BaseDocumentUnitTestCase;
import org.netbeans.modules.editor.indent.api.Indent;
import org.openide.util.Exceptions;

/**
 *
 * @author Alexander Simon
 */
public class EditorBase extends BaseDocumentUnitTestCase {
    private boolean isCPP = true;
    
    public EditorBase(String testMethodName) {
        super(testMethodName);
    }
    
    @Override
    protected final EditorKit createEditorKit() {
        if (isCPP) {
            return new CCKit();
        } else {
            return new CKit();
        }
    }

    protected final void setCppEditorKit(boolean isCPP){
        this.isCPP = isCPP;
    }

    protected final void setDefaultsOptions(){
        // Note due to IZ#130533 the default style is changed. Hence we reset some properties.
        if (isCPP) {
            EditorOptions.resetToDefault(CodeStyle.getDefault(CodeStyle.Language.CPP));
            EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceNamespace, 
                CodeStyle.BracePlacement.NEW_LINE.name());
            EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceClass, 
                CodeStyle.BracePlacement.NEW_LINE.name());
            EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE.name());
        } else {
            EditorOptions.resetToDefault(CodeStyle.getDefault(CodeStyle.Language.C));
            EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.C)).
                put(EditorOptions.newLineBeforeBraceNamespace, 
                CodeStyle.BracePlacement.NEW_LINE.name());
            EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.C)).
                put(EditorOptions.newLineBeforeBraceClass, 
                CodeStyle.BracePlacement.NEW_LINE.name());
            EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.C)).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE.name());
        }
    }
    protected final void setDefaultsOptions(String style){
        if (isCPP) {
            EditorOptions.resetToDefault(CodeStyle.getDefault(CodeStyle.Language.CPP));
            EditorOptions.resetToDefault(CodeStyle.getDefault(CodeStyle.Language.CPP), style);
        } else {
            EditorOptions.resetToDefault(CodeStyle.getDefault(CodeStyle.Language.C));
            EditorOptions.resetToDefault(CodeStyle.getDefault(CodeStyle.Language.C), style);
        }
    }

    /**
     * Perform new-line insertion followed by indenting of the new line
     * by the formatter.
     * The caret position should be marked in the document text by '|'.
     */
    protected final void indentNewLine() {
        try {
            int offset = getCaretOffset();
            getDocument().insertString(offset, "\n", null); // NOI18N
//            Indent indent = Indent.get(getDocument());
//            indent.lock();
//            try {
//                indent.reindent(offset-1);
//            } finally {
//                indent.unlock();
//            }
            CppIndentTask task = new CppIndentTask(getDocument());
            task.reindent(offset+1);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Perform new-line insertion followed by indenting of the new line
     * by the formatter.
     * The caret position should be marked in the document text by '|'.
     */
    protected final void indentLine() {
        try {
            int offset = getCaretOffset();
            CppIndentTask task = new CppIndentTask(getDocument());
            task.reindent(offset);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Perform reformatting of the whole document's text.
     */
    protected final void reformat() {
        Reformatter f = new Reformatter(getDocument(), CodeStyle.getDefault(getDocument()));
        try {
            f.reformat();
        } catch (BadLocationException e) {
            e.printStackTrace(getLog());
            fail(e.getMessage());
    	}
    }

    // ------- help methods -------------

    protected final void typeChar(char ch, boolean indentNewLine) throws Exception {
        int pos = getCaretOffset();
        getDocument ().insertString(pos, String.valueOf(ch), null);
        if (HotCharIndent.INSTANCE.getKeywordBasedReformatBlock(getDocument(), pos, ""+ch)) {
            indentLine();
            indentNewLine = false;
        }
        BracketCompletion.charInserted(getDocument(), pos, getCaret(), ch, false);
        if (indentNewLine) {
            indentNewLine();
        }
    }

    protected final void typeQuoteChar(char ch) throws Exception {
        typeChar(ch, false);
    }

    protected final boolean isSkipRightParen() {
        return isSkipRightBracketOrParen(true);
    }

    protected final boolean isSkipRightBracketOrParen(boolean parenthesis) {
        CppTokenId bracketTokenId = parenthesis
        ? CppTokenId.RPAREN
        : CppTokenId.RBRACKET;

        try {
            return BracketCompletion.isSkipClosingBracket(getDocument(),
            getCaretOffset(), bracketTokenId);
        } catch (BadLocationException e) {
            e.printStackTrace(getLog());
            fail();
            return false; // should never be reached
        }
    }

    protected final boolean isAddRightBrace() {
        try {
            return BracketCompletion.isAddRightBrace(getDocument(), getCaretOffset());
        } catch (BadLocationException e) {
            e.printStackTrace(getLog());
            fail();
            return false; // should never be reached
        }
    }

    protected final void breakLine() {
        DocumentUtilities.setTypingModification(getDocument(), true);
        Indent indent = Indent.get(getDocument());
        indent.lock();
        try {
            CCKit.CCInsertBreakAction action = new CCKit.CCInsertBreakAction();
            Object out = action.beforeBreak(null, getDocument(), getCaret());
            indentNewLine();
            action.afterBreak(null, getDocument(), getCaret(), out);
        } finally {
            indent.unlock();
            DocumentUtilities.setTypingModification(getDocument(), false);
        }
    }
}
