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
package org.netbeans.modules.php.editor.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.StructureItem;
import org.netbeans.modules.gsf.api.StructureScanner;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.*;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;

/**
 * Just s dummy impl. to enable the html, javascript and css navigator
 * Needs to be implemented properly.
 *
 * @author marek
 */
public class PhpStructureScanner implements StructureScanner {

    private HtmlFormatter formatter;
    private static String FOLD_CODE_BLOCKS = "codeblocks";
    private static String FOLD_PHPDOC = "comments";
    private static String FOLD_COMMENT = "initial-comment";

    public List<? extends StructureItem> scan(final CompilationInfo info, HtmlFormatter formatter) {
        this.formatter = formatter;
        return Collections.emptyList();
    }

    public Map<String, List<OffsetRange>> folds(CompilationInfo info) {
        Program program = Utils.getRoot(info);
        final Map<String, List<OffsetRange>> folds = new HashMap<String, List<OffsetRange>>();
        if (program != null) {
            program.accept(new FoldVisitor(folds));

            List<Comment> comments = program.getComments();
            if (comments != null) {
                for (Comment comment : comments) {
                    if (comment.getCommentType() == Comment.Type.TYPE_PHPDOC) {
                        getRanges(folds, FOLD_PHPDOC).add(createOffsetRange(comment));
                    } else {
                        if (comment.getCommentType() == Comment.Type.TYPE_MULTILINE) {
                            getRanges(folds, FOLD_COMMENT).add(createOffsetRange(comment));
                        }
                    }
                }
            }
            return folds;
        }
        return Collections.emptyMap();
    }

    private OffsetRange createOffsetRange(ASTNode node) {
        return new OffsetRange(node.getStartOffset(), node.getEndOffset());
    }

    private List<OffsetRange> getRanges(Map<String, List<OffsetRange>> folds, String kind) {
        List<OffsetRange> ranges = folds.get(kind);
        if (ranges == null) {
            ranges = new ArrayList<OffsetRange>();
            folds.put(kind, ranges);
        }
        return ranges;
    }

    private class FoldVisitor extends DefaultVisitor {

        Map<String, List<OffsetRange>> folds;
        boolean foldingBlock;

        public FoldVisitor(Map<String, List<OffsetRange>> folds) {
            this.folds = folds;
            foldingBlock = false;
        }

        @Override
        public void visit(ClassDeclaration cldec) {
            foldingBlock = true;
            if (cldec.getBody() != null) {
                cldec.getBody().accept(this);
            }
        }

        @Override
        public void visit(Block block) {
            if (foldingBlock) {
                getRanges(folds, FOLD_CODE_BLOCKS).add(createOffsetRange(block));
                foldingBlock = false;
                if (block.getStatements() != null) {
                    for (Statement statement : block.getStatements()) {
                        statement.accept(this);
                    }
                }
            }
        }

        @Override
        public void visit(FunctionDeclaration function) {
            foldingBlock = true;
            if (function.getBody() != null) {
                function.getBody().accept(this);
            }
        }
    }
}
