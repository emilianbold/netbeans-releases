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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.suggestions;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.SourceVersion;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.modules.java.hints.jackpot.code.spi.Constraint;
import org.netbeans.modules.java.hints.jackpot.code.spi.Hint;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPattern;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerTreeKind;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.jackpot.spi.HintMetadata.Kind;
import org.netbeans.modules.java.hints.jackpot.spi.JavaFix;
import org.netbeans.modules.java.hints.jackpot.spi.support.ErrorDescriptionFactory;
import org.netbeans.modules.java.hints.spi.AbstractHint.HintSeverity;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class Tiny {

    @Hint(category="suggestions", hintKind=Kind.SUGGESTION, severity=HintSeverity.CURRENT_LINE_WARNING)
    @TriggerPattern(value="$this.equals($other)",
                    constraints={
                        @Constraint(variable="$this", type="java.lang.Object"),
                        @Constraint(variable="$other", type="java.lang.Object")
                    })
    public static ErrorDescription flipEquals(HintContext ctx) {
        int caret = CaretAwareJavaSourceTaskFactory.getLastPosition(ctx.getInfo().getFileObject());
        MethodInvocationTree mit = (MethodInvocationTree) ctx.getPath().getLeaf();
        ExpressionTree select = mit.getMethodSelect();
        int selectStart;
        int selectEnd;

        switch (select.getKind()) {
            case MEMBER_SELECT:
                int[] span = ctx.getInfo().getTreeUtilities().findNameSpan((MemberSelectTree) select);

                if (span == null) {
                    return null;
                }

                selectStart = span[0];
                selectEnd = span[1];
                break;
            case IDENTIFIER:
                selectStart = (int) ctx.getInfo().getTrees().getSourcePositions().getStartPosition(ctx.getInfo().getCompilationUnit(), select);
                selectEnd   = (int) ctx.getInfo().getTrees().getSourcePositions().getEndPosition(ctx.getInfo().getCompilationUnit(), select);
                break;
            default:
                Logger.getLogger(Tiny.class.getName()).log(Level.FINE, "flipEquals: unexpected method select kind: {0}", select.getKind());
                return null;
        }

        if (selectStart > caret || selectEnd < caret) {
            return null;
        }

        String fixDisplayName = NbBundle.getMessage(Tiny.class, "FIX_flipEquals");
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_flipEquals");
        String fixPattern;

        if (ctx.getVariables().containsKey("$this")) {
            fixPattern = "$other.equals($this)";
        } else {
            fixPattern = "$other.equals(this)";
        }

        Fix fix = JavaFix.rewriteFix(ctx, fixDisplayName, ctx.getPath(), fixPattern);
        
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName, fix);
    }
    
    @Hint(category="suggestions", hintKind=Kind.SUGGESTION, severity=HintSeverity.CURRENT_LINE_WARNING)
    @TriggerTreeKind({Tree.Kind.INT_LITERAL, Tree.Kind.LONG_LITERAL})
    public static ErrorDescription convertToDifferentBase(HintContext ctx) {
        int start = (int) ctx.getInfo().getTrees().getSourcePositions().getStartPosition(ctx.getInfo().getCompilationUnit(), ctx.getPath().getLeaf());
        int end   = (int) ctx.getInfo().getTrees().getSourcePositions().getEndPosition(ctx.getInfo().getCompilationUnit(), ctx.getPath().getLeaf());
        String code = ctx.getInfo().getText().substring(start, end);
        int currentRadix = 10;
        
        if (code.startsWith("0x") || code.startsWith("0X")) currentRadix = 16;
        else if (code.startsWith("0b") || code.startsWith("0B")) currentRadix = 2;
        else if (code.startsWith("0") || code.startsWith("0")) currentRadix = 8;
        
        List<Fix> fixes = new LinkedList<Fix>();
        
        if (currentRadix != 16) {
            fixes.add(JavaFix.toEditorFix(new ToDifferentRadixFixImpl(ctx.getInfo(), ctx.getPath(), "0x", 16)));
        }
        if (currentRadix != 10) {
            fixes.add(JavaFix.toEditorFix(new ToDifferentRadixFixImpl(ctx.getInfo(), ctx.getPath(), "", 10)));
        }
        if (currentRadix != 8) {
            fixes.add(JavaFix.toEditorFix(new ToDifferentRadixFixImpl(ctx.getInfo(), ctx.getPath(), "0", 8)));
        }
        if (currentRadix != 2 && ctx.getInfo().getSourceVersion().compareTo(SourceVersion.RELEASE_7) >= 0) {
            fixes.add(JavaFix.toEditorFix(new ToDifferentRadixFixImpl(ctx.getInfo(), ctx.getPath(), "0b", 2)));
        }
        
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), NbBundle.getMessage(Tiny.class, "ERR_convertToDifferentBase"), fixes.toArray(new Fix[0]));
    }
    
    private static final class ToDifferentRadixFixImpl extends JavaFix {
        private final String prefix;

        private final int radix;

        public ToDifferentRadixFixImpl(CompilationInfo info, TreePath tp, String prefix, int radix) {
            super(info, tp);
            this.prefix = prefix;
            this.radix = radix;
        }
        
        @Override
        protected String getText() {
            return NbBundle.getMessage(Tiny.class, "FIX_convertToDifferentBase_" + radix);
        }

        @Override
        protected void performRewrite(WorkingCopy wc, TreePath tp, UpgradeUICallback callback) {
            LiteralTree leaf = (LiteralTree) tp.getLeaf();
            String suffix;
            
            if (leaf.getKind() == Tree.Kind.INT_LITERAL) {
                suffix = "";
            } else if (leaf.getKind() == Tree.Kind.LONG_LITERAL) {
                int  end = (int) wc.getTrees().getSourcePositions().getEndPosition(wc.getCompilationUnit(), leaf);
                
                suffix = wc.getText().substring(end - 1, end);
            } else {
                throw new IllegalStateException();
            }
            
            long value = ((Number) leaf.getValue()).longValue();

            String target;

            switch (radix) {
                case  2: target = Long.toBinaryString(value); break;
                case  8: target = Long.toOctalString(value); break;
                case 10: target = Long.toString(value); break;
                case 16: target = Long.toHexString(value); break;
                default:
                    throw new IllegalStateException();
            }

            target = prefix + target + suffix;

            wc.rewrite(leaf, wc.getTreeMaker().Identifier(target));
        }
        
    }
}
