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

/*
 * InlineIncludeHint.java
 *
 * Created on August 2, 2005, 1:24 PM
 *
 */
package org.netbeans.modules.mobility.editor.hints;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.mobility.antext.preprocessor.LineParserTokens;
import org.netbeans.mobility.antext.preprocessor.PPBlockInfo;
import org.netbeans.mobility.antext.preprocessor.PPLine;
import org.netbeans.mobility.antext.preprocessor.PPToken;
import org.netbeans.modules.mobility.editor.actions.RecommentAction;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.ErrorManager;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;

/**
 *
 * @author Adam Sotona
 */
public class ReplaceOldSyntaxHint implements Fix {
    
    final protected Document doc;
    final protected List<PPLine> lineList;
    protected PPBlockInfo block;
    
    /** Creates a new instance of InlineIncludeHint */
    public ReplaceOldSyntaxHint(final Document doc, final List<PPLine> lineList, final PPBlockInfo block) {
        this.doc = doc;
        this.lineList = lineList;
        this.block = block;
    }
    
    protected boolean isNegative(final List<PPToken> tokens) {
        return tokens.size() > 1 && tokens.get(1).getType() == LineParserTokens.OP_NOT;
    }
    
    protected List<String> extractAbilities(final List<PPToken> tokens) {
        final ArrayList<String> abilities = new ArrayList<String>();
        for ( final PPToken tok : tokens ) {
            if (tok.getType() == LineParserTokens.ABILITY) abilities.add(tok.getText());
        }
        return abilities;
    }
    
    public synchronized ChangeInfo implement() {
        NbDocument.runAtomic((StyledDocument)doc, new Runnable() {
            public void run() {
                try {
                    final PPLine startLine  = lineList.get(block.getStartLine()-1);
                    final List<PPToken> tokens = startLine.getTokens();
                    boolean negative = isNegative(tokens);
                    final List<String> abilities = extractAbilities(tokens);
                    PPBlockInfo elseBlock = findContraryBlock(negative, abilities);
                    if (elseBlock != null && elseBlock.getStartLine() < block.getStartLine()) {
                        //else is before if -> exchange the blocks and switch the negation
                        final PPBlockInfo x = elseBlock;
                        elseBlock = block;
                        block = x;
                        negative ^= true;
                    }
                    if (elseBlock != null) {
                        doc.insertString(removeLine(doc, elseBlock.getEndLine()), "//#endif", null); //NOI18N
                        doc.insertString(removeLine(doc, elseBlock.getStartLine()), "//#else", null); //NOI18N
                        doc.remove(removeLine(doc, block.getEndLine()), 1);
                    } else {
                        doc.insertString(removeLine(doc, block.getEndLine()), "//#endif", null); //NOI18N
                    }
                    doc.insertString(removeLine(doc, block.getStartLine()), "//#if " + constructCondition(negative, abilities), null); //NOI18N
                } catch (BadLocationException ble) {
                    ErrorManager.getDefault().notify(ble);
                }
                RecommentAction.actionPerformed(doc);
            }
        });
        return null;
    }
    
    protected String constructCondition(final boolean negative, final List<String> abilities) {
        final StringBuffer sb = new StringBuffer();
        for ( String str : abilities ) {
            if (sb.length() > 0) sb.append(" || "); //NOI18N
            sb.append(str);
        }
        return negative ? "!(" + sb.toString() + ")" : sb.toString(); //NOI18N
    }
    
    protected int removeLine(final Document doc, final int line) throws BadLocationException {
        final int i = Utilities.getRowStartFromLineOffset((BaseDocument)doc, line - 1);
        doc.remove(i, Utilities.getRowEnd((BaseDocument)doc, i) - i);
        return i;
    }
    
    private PPBlockInfo checkContraryBlock(PPBlockInfo b, final boolean negative, final List<String> abilities) {
        while (b != null && b.getType() != PPLine.OLDIF) b = b.getParent();
        if (b != null && b.getType() == PPLine.OLDIF) {
            final PPLine startLine  = lineList.get(b.getStartLine()-1);
            final List<PPToken> tokens = startLine.getTokens();
            if (negative != isNegative(tokens)) {
                final List<String> abs = extractAbilities(tokens);
                if (abs.size() == abilities.size() && abs.containsAll(abilities)) {
                    return b;
                }
            }
        }
        return null;
    }
    
    protected PPBlockInfo findContraryBlock(final boolean negative, final List<String> abilities) {
        if (block.getStartLine() > 1) {
            final PPBlockInfo b = checkContraryBlock(lineList.get(block.getStartLine()-2).getBlock(), negative, abilities);
            if (b != null) return b;
        }
        if (block.getEndLine() < lineList.size()) {
            final PPBlockInfo b = checkContraryBlock(lineList.get(block.getEndLine()).getBlock(), negative, abilities);
            if (b != null) return b;
        }
        return null;
    }
    
//    public int getType() {
//        return SUGGESTION;
//    }
    
    public String getText() {
        return NbBundle.getMessage(ReplaceOldSyntaxHint.class, "HintReplaceOldSyntax"); //NOI18N
    }
}
