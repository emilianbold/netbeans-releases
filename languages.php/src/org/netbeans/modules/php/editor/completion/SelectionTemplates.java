/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.php.editor.completion;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.php.editor.TokenUtils;

/**
 * <code>CodeTemplateProvider</code> for selection templates.
 * 
 * @author Victor G. Vasilyev
 */
public class SelectionTemplates implements CodeTemplateProvider {
    
    /**
     * Provider must have only parameterless constructor.
     */
    public SelectionTemplates() {
    }
    
    /**
     * @see org.netbeans.modules.php.editor.completion.CodeTemplateProvider#isApplicable(TemplateContext context)
     */
    public boolean isApplicable(TemplateContext context) {
        OffsetPair selection = context.getSelectionOffsets();
        if (!selection.isValid()) {
            // no selection, i.e. either start or end < 0
            // Note: only end != -1 is checked in the Ruby Module impl.
            return true;
        }
        if (selection.length() <= 0) {
            // selected nothing, i.e. start == end
            return false;
        }
        try {
            CompilationInfo info = context.getCompilationInfo(); 
            BaseDocument doc = (BaseDocument)info.getDocument();
            if (isApplicableSelectionEnvironment(doc, selection)) {
                // There are no text to the left of the beginning or 
                // text to the right of the end
                if (isApplicableSelectionContent(doc, selection)) {
                    return true;
                }
            }
            return false;
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * @see org.netbeans.modules.php.editor.completion.CodeTemplateProvider#getAbbreviationSet(TemplateContext context)
     */
    public Set<String> getAbbreviationSet(TemplateContext context) {
        return selectionTemplates;
    }

    /**
     * Returns <code>true</code> if (a) there are no any non-whitespace symbols 
     * to the left and the right of the selection range or (b) selected range 
     * is started and ended on empty lines.
     * @param doc
     * @param selection
     * @return
     * @todo review conditions.
     */
    private boolean isApplicableSelectionEnvironment(BaseDocument doc, 
            OffsetPair selection) {
        try {
            int start = selection.getStartOffset();
            int end = selection.getEndOffset();

            boolean isStartLineEmpty = Utilities.isRowEmpty(doc, start);
            boolean isEndLineEmpty = Utilities.isRowEmpty(doc, end);
            boolean isSelectionStartBeforeText = 
                    start <= Utilities.getRowFirstNonWhite(doc, start);
            boolean isSelectionEndAfterText = 
                    end > Utilities.getRowLastNonWhite(doc, end);
            return (isStartLineEmpty || isSelectionStartBeforeText) && 
                    (isEndLineEmpty || isSelectionEndAfterText);
        } catch (BadLocationException ex) {
            return false;
        }
    }
    
    /**
     * Returns <code>true</code> if (a) selected range contains only empty 
     * line(s) or applicable tokens.
     * @param doc
     * @param selection
     * @return
     */
    private boolean isApplicableSelectionContent(BaseDocument doc, OffsetPair selection) {
        try {
            int start = selection.getStartOffset();
            if (!TokenUtils.checkPhp(doc, start)) {
                return false;
            }
            int end = selection.getEndOffset();
            if (!TokenUtils.checkPhp(doc, end)) {
                return false;
            }
            String text = getSelectedText(doc, selection);
            for (int i = 0; i < text.length(); i++) {
                // TODO: Use PHP whitespaces instead.
                if (!Character.isWhitespace(text.charAt(i))) {
                    if(isApplicableTokens(doc, start + i, end)) {
                        return true;
                    }
                    break;
                }
            }
            return false;
        } catch (BadLocationException ble) {
            return false;
        }
        
    }
    
    private boolean isApplicableTokens(BaseDocument doc, int start, int end) {
        // Make sure that we're not in a string etc
        if (!TokenUtils.checkPhp(doc, start)) {
            return false;
        }
        TokenSequence ts = TokenUtils.getEmbeddedTokenSequence(doc, start);
        ts.move(start);
        if(!ts.moveNext()) {
            return false;
        }
        while(ts.offset() < end) {
            Token t = ts.token();
            if(!isApplicableToken(t)) {
                return false;
            }
            if(!ts.moveNext()) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isApplicableToken(Token t) {
        if(t == null) {
            return false;
        }
        String name = TokenUtils.getTokenType(t);
        return !nonApplicableTokens.contains(name);
    }
    
    private String getSelectedText(BaseDocument doc, OffsetPair selection) 
            throws BadLocationException {
        return doc.getText(selection.getStartOffset(), selection.length());
    }
    
    private static final Set<String> nonApplicableTokens = new HashSet<String>();
    static {
        // TODO: review this list
        nonApplicableTokens.add(TokenUtils.BLOCK_COMMENT); // NOI18N
        nonApplicableTokens.add(TokenUtils.LINE_COMMENT); // NOI18N
    }
        
    private static final Set<String> selectionTemplates = new HashSet<String>();
    static {
        selectionTemplates.add("if"); // NOI18N
        selectionTemplates.add("ife"); // NOI18N
        selectionTemplates.add("wh"); // NOI18N
        selectionTemplates.add("do"); // NOI18N
        selectionTemplates.add("for"); // NOI18N
        selectionTemplates.add("fore"); // NOI18N
        selectionTemplates.add("fork"); // NOI18N
        selectionTemplates.add("sw"); // NOI18N
        selectionTemplates.add("de"); // NOI18N
        selectionTemplates.add("det"); // NOI18N
        selectionTemplates.add("func"); // NOI18N
        selectionTemplates.add("cl"); // NOI18N
        selectionTemplates.add("con"); // NOI18N
        selectionTemplates.add("des"); // NOI18N
    }

}
