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

import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
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

    public WSTransformer(Context context) {
        this.context = context;
        String openingBraceStyle = CodeStyle.get(context.document()).getOpeningBraceStyle();
        newLineReplacement = FmtOptions.OBRACE_NEWLINE.equals(openingBraceStyle)? "\n" : " "; //NOI18N
    }

    @Override
    public void visit(Block node) {
        // TODO: check formatting boundaries here
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

                Replacement replacement = new Replacement(start, length, newLineReplacement);
                replacements.add(0, replacement);
            }
        }
        super.visit(node);
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
}
