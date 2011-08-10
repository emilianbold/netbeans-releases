/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.hints;

import java.util.Collections;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.ext.html.parser.api.AstNode;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.openide.util.NbBundle;

/**
 *
 * @author mfukala@netbeans.org
 */
public class RemoveSurroundingTag extends Hint {

    private static final Rule RULE = new RemoveSurroundingTagRule();
    private static final String DISPLAYNAME = NbBundle.getMessage(RemoveSurroundingTag.class, "MSG_RemoveSurroundingTag");

    public RemoveSurroundingTag(RuleContext context, OffsetRange range) {
        super(RULE,
                DISPLAYNAME,
                context.parserResult.getSnapshot().getSource().getFileObject(),
                range,
                Collections.<HintFix>singletonList(new SurroundWithTagHintFix(context)),
                10);
    }

    private static class SurroundWithTagHintFix implements HintFix {

        RuleContext context;

        public SurroundWithTagHintFix(RuleContext context) {
            this.context = context;
        }

        @Override
        public String getDescription() {
            return DISPLAYNAME;
        }

        @Override
        public void implement() throws Exception {
            
            context.doc.runAtomic(new Runnable() {

                @Override
                public void run() {
                    try {
                        AstNode[] surroundingPair = findPairNodesAtSelection(context) ;
                        if(surroundingPair == null) {
                            return ;
                        }
                        int otfrom = surroundingPair[0].startOffset();
                        int otto = surroundingPair[0].endOffset();
                        int otlen = otto - otfrom;
                        
                        int ctfrom = surroundingPair[1].startOffset();
                        int ctto = surroundingPair[1].endOffset();
                        
                        context.doc.remove(otfrom, otlen);
                        context.doc.remove(ctfrom - otlen, ctto - ctfrom);

                    } catch (BadLocationException ex) {
                        //ignore
                    }

                }
            });
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }
    }

    private static class RemoveSurroundingTagRule implements Rule {

        @Override
        public boolean appliesTo(RuleContext context) {
            return findPairNodesAtSelection(context) != null;
        }

        @Override
        public String getDisplayName() {
            return DISPLAYNAME;
        }

        @Override
        public boolean showInTasklist() {
            return false;
        }

        @Override
        public HintSeverity getDefaultSeverity() {
            return HintSeverity.INFO;
        }
    }

    private static AstNode[] findPairNodesAtSelection(RuleContext context) {
        if (context.selectionStart == -1 || context.selectionEnd == -1) {
            return null;
        }

        HtmlParserResult result = (HtmlParserResult) context.parserResult;

        //check whether the selection starts at a tag and ends at a tag
        //open tag
        AstNode open = result.findLeafTag(context.selectionStart, true, true);

        if (open == null || open.type() != AstNode.NodeType.OPEN_TAG) {
            return null;
        }

        //close tag
        AstNode close = result.findLeafTag(context.selectionEnd, false, true);
        if (close == null || close.type() != AstNode.NodeType.ENDTAG) {
            return null;
        }

        //is the end tag really a pair node of the open tag?
        if (open.getMatchingTag() != close) { //same AST ... reference test is ok
            return null;
        }

        return new AstNode[]{open, close};
    }
    
    
}
