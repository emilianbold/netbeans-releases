/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jackpot.spi.support;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.spi.support.FixFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author Jan Lahoda
 */
public class ErrorDescriptionFactory {

    private ErrorDescriptionFactory() {
    }

//    public static ErrorDescription forTree(HintContext context, String text, Fix... fixes) {
//        return forTree(context, context.getContext(), text, fixes);
//    }

    public static ErrorDescription forTree(HintContext context, TreePath tree, String text, Fix... fixes) {
        return forTree(context, tree.getLeaf(), text, fixes);
    }
    
    public static ErrorDescription forTree(HintContext context, Tree tree, String text, Fix... fixes) {
        int start = (int) context.getInfo().getTrees().getSourcePositions().getStartPosition(context.getInfo().getCompilationUnit(), tree);
        int end = (int) context.getInfo().getTrees().getSourcePositions().getEndPosition(context.getInfo().getCompilationUnit(), tree);

        if (start != (-1) && end != (-1)) {
            List<Fix> fixesForED = resolveDefaultFixes(context, fixes);
            return org.netbeans.spi.editor.hints.ErrorDescriptionFactory.createErrorDescription(context.getSeverity().toEditorSeverity(), text, fixesForED, context.getInfo().getFileObject(), start, end);
        }

        return null;
    }
    
    public static ErrorDescription forName(HintContext context, TreePath tree, String text, Fix... fixes) {
        return forName(context, tree.getLeaf(), text, fixes);
    }

    public static ErrorDescription forName(HintContext context, Tree tree, String text, Fix... fixes) {
        int[] span = computeNameSpan(tree, context);
        
        if (span != null && span[0] != (-1) && span[1] != (-1)) {
            List<Fix> fixesForED = resolveDefaultFixes(context, fixes);
            return org.netbeans.spi.editor.hints.ErrorDescriptionFactory.createErrorDescription(context.getSeverity().toEditorSeverity(), text, fixesForED, context.getInfo().getFileObject(), span[0], span[1]);
        }

        return null;
    }

    private static int[] computeNameSpan(Tree tree, HintContext context) {
        switch (tree.getKind()) {
            case METHOD:
                return context.getInfo().getTreeUtilities().findNameSpan((MethodTree) tree);
            case CLASS:
                return context.getInfo().getTreeUtilities().findNameSpan((ClassTree) tree);
            case VARIABLE:
                return context.getInfo().getTreeUtilities().findNameSpan((VariableTree) tree);
            case MEMBER_SELECT:
                //XXX:
                MemberSelectTree mst = (MemberSelectTree) tree;
                int[] span = context.getInfo().getTreeUtilities().findNameSpan(mst);

                if (span == null) {
                    int end = (int) context.getInfo().getTrees().getSourcePositions().getEndPosition(context.getInfo().getCompilationUnit(), tree);
                    span = new int[] {end - mst.getIdentifier().length(), end};
                }
                return span;
            case METHOD_INVOCATION:
                return computeNameSpan(((MethodInvocationTree) tree).getMethodSelect(), context);
            default:
                return new int[] {
                    (int) context.getInfo().getTrees().getSourcePositions().getStartPosition(context.getInfo().getCompilationUnit(), tree),
                    (int) context.getInfo().getTrees().getSourcePositions().getEndPosition(context.getInfo().getCompilationUnit(), tree),
                };
        }
    }

    private static List<Fix> resolveDefaultFixes(HintContext ctx, Fix... provided) {
        List<Fix> result = new LinkedList<Fix>();
        boolean wasSuppressWarnings = false;
        
        for (Fix f : provided) {
            if (f == null) continue;
            if (FixFactory.isSuppressWarningsFix(f)) {
                wasSuppressWarnings = true;
            }
            result.add(f);
        }

        if (wasSuppressWarnings) {
            return result;
        }

        Set<String> suppressWarningsKeys = new LinkedHashSet<String>();

        for (String key : ctx.getSuppressWarningsKeys()) {
            if (key == null || key.length() == 0) {
                break;
            }
            
            suppressWarningsKeys.add(key);
        }

        if (!suppressWarningsKeys.isEmpty()) {
            result.addAll(FixFactory.createSuppressWarnings(ctx.getInfo(), ctx.getPath(), suppressWarningsKeys.toArray(new String[0])));
        }

        return result;
    }

}
