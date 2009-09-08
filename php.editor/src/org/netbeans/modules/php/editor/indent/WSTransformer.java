/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.php.editor.indent;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.ForStatement;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultTreePathVisitor;

/**
 * This class calculates all white-space tranformations other than
 * line indentation, e.g. breaking or merging lines,
 * removing reduntant spaces
 *
 * @author Tomasz.Slota@Sun.COM
 */
class WSTransformer extends DefaultTreePathVisitor {
    private String newLineReplacement = "\n"; //NOI18N

    private Context context;
    private List<Replacement> replacements = new LinkedList<WSTransformer.Replacement>();
    private Collection<CodeRange> unbreakableRanges = new TreeSet<CodeRange>();
    private Collection<Integer> breakPins = new LinkedList<Integer>();

    private Collection<PHPTokenId> WS_AND_COMMENT_TOKENS = Arrays.asList(PHPTokenId.PHPDOC_COMMENT_START,
            PHPTokenId.PHPDOC_COMMENT_END, PHPTokenId.PHPDOC_COMMENT, PHPTokenId.WHITESPACE,
            PHPTokenId.PHP_COMMENT_START, PHPTokenId.PHP_COMMENT_END, PHPTokenId.PHP_COMMENT,
            PHPTokenId.PHP_LINE_COMMENT);

    public WSTransformer(Context context) {
        this.context = context;
        String openingBraceStyle = CodeStyle.get(context.document()).getOpeningBraceStyle();
        newLineReplacement = FmtOptions.OBRACE_NEWLINE.equals(openingBraceStyle)? "\n" : " "; //NOI18N
    }

    @Override
    public void visit(Block node) {
        // TODO: check formatting boundaries here

        if (getPath().get(0) instanceof NamespaceDeclaration){
            return;
        }
        

        if (node.isCurly()){
            TokenSequence<PHPTokenId> tokenSequence = tokenSequence(node.getStartOffset());
            tokenSequence.move(node.getStartOffset());

            if (tokenSequence.moveNext()
                    && tokenSequence.token().id() == PHPTokenId.PHP_CURLY_OPEN){
                int start = tokenSequence.offset();
                int length = 0;

                if (tokenSequence.movePrevious()
                        && tokenSequence.token().id() == PHPTokenId.WHITESPACE){
                    length = tokenSequence.token().length();
                }

                Replacement preOpenBracket = new Replacement(start, length, newLineReplacement);
                replacements.add(preOpenBracket);
            }

            tokenSequence.move(node.getStartOffset());

            if (tokenSequence.moveNext() && !wsAndCommentsContainBreak(tokenSequence, true)){
                Replacement postOpen = new Replacement(tokenSequence.offset() +
                        tokenSequence.token().length(), 0, "\n"); //NOI18N
                replacements.add(postOpen);
            }

            tokenSequence.move(node.getEndOffset());

            if (tokenSequence.movePrevious()){
                int closPos = tokenSequence.offset();
                if (!wsAndCommentsContainBreak(tokenSequence, false)){
                    // avoid adding double line break in case that } is preceded with ;
                    tokenSequence.movePrevious();
                    if (tokenSequence.token().id() != PHPTokenId.PHP_SEMICOLON){
                        Replacement preClose = new Replacement(closPos, 0, "\n"); //NOI18N
                        replacements.add(preClose);
                    }
                    tokenSequence.moveNext();
                }

                tokenSequence.move(node.getEndOffset());
                if (tokenSequence.movePrevious() && !wsAndCommentsContainBreak(tokenSequence, true)){
                    Replacement postClose = new Replacement(tokenSequence.offset() +
                            tokenSequence.token().length(), 0, "\n"); //NOI18N
                    replacements.add(postClose);
                }
            }
            
        }
        super.visit(node);
    }

    @Override
    public void visit(ForStatement node) {
        int start = node.getInitializers().get(0).getStartOffset();
        int end = node.getUpdaters().get(0).getStartOffset();

        unbreakableRanges.add(new CodeRange(start, end));

        super.visit(node);
    }

    public void tokenScan(){
        // TODO: check formatting boundaries here
        TokenSequence<PHPTokenId> tokenSequence = tokenSequence(0);
        tokenSequence.moveStart();

        while (tokenSequence.moveNext()){
            if (!isWithinUnbreakableRange(tokenSequence.offset())
                    && splitTrigger(tokenSequence)){
                int splitPos = tokenSequence.offset() + 1;
                if (wsAndCommentsContainBreak(tokenSequence, true)){
                    continue;
                }

                Replacement replacement = new Replacement(splitPos, 0, "\n");
                replacements.add(replacement);
            }
        }
    }

    private boolean splitTrigger(TokenSequence<PHPTokenId> tokenSequence){

        PHPTokenId tokenId = tokenSequence.token().id();

        if (tokenId == PHPTokenId.PHP_SEMICOLON){
            return true;
        }

        //TODO: handle 'case:'

        return false;
    }

    private boolean wsAndCommentsContainBreak(TokenSequence<PHPTokenId> tokenSequence, boolean fwd) {
        //int orgOffset = tokenSequence.offset();
        boolean retVal = false;
        while (fwd && tokenSequence.moveNext() || !fwd && tokenSequence.movePrevious()) {
            if (WS_AND_COMMENT_TOKENS.contains(tokenSequence.token().id())) {
                if (textContainsBreak(tokenSequence.token().text())) {
                    retVal = true;
                    break;
                }
            } else {
                if (fwd){
                    tokenSequence.movePrevious();
                } else {
                    tokenSequence.moveNext();
                }
                break; // return false
            }
        }

//        tokenSequence.move(orgOffset);
//        tokenSequence.moveNext();

        return retVal;
    }

    private static final boolean textContainsBreak(CharSequence charSequence){

        for (int i = 0; i < charSequence.length(); i++) {
            if (charSequence.charAt(i) == '\n'){
                return true;
            }
        }

        return false;
    }

    private boolean isWithinUnbreakableRange(int offset){
        // TODO: optimize for n*log(n) complexity
        for (CodeRange codeRange : unbreakableRanges){
            if (codeRange.contains(offset)){
                return true;
            }
        }

        return false;
    }

    List<Replacement> getReplacements() {
        return replacements;
    }

    private TokenSequence<PHPTokenId> tokenSequence(int offset){
        return LexUtilities.getPHPTokenSequence(context.document(), offset);
    }

    static class Replacement implements Comparable<Replacement>{
        private Integer offset;
        private int length;
        private String newString;

        public Replacement(int offset, int length, String newString) {
            this.offset = offset;
            this.length = length;
            this.newString = newString;
        }

        public int length() {
            return length;
        }

        public String newString() {
            return newString;
        }

        public int offset() {
            return offset;
        }

        public int compareTo(Replacement r) {
            return offset.compareTo(r.offset);
        }
    }

    static class CodeRange implements Comparable<CodeRange>{
        private Integer start;
        private Integer end;

        public CodeRange(int start, int end) {
            this.start = start;
            this.end = end;
        }

        boolean contains(int offset){
            return offset >= start && offset <= end;
        }

        public int compareTo(CodeRange o) {
            int r = start.compareTo(o.start);

            if (r == 0){
                return end.compareTo(o.end);
            }

            return r;
        }

        
    }
}
