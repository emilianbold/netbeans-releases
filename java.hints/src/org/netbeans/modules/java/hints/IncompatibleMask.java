/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.text.MessageFormat;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.jackpot.code.spi.Hint;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPattern;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPatterns;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.jackpot.spi.support.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;


/**
 *
 * @author Jan Jancura
 */
@Hint(category="bitwise_operations", suppressWarnings="IncompatibleBitwiseMaskOperation")
public class IncompatibleMask {

    @TriggerPatterns ({
        @TriggerPattern (value="($a & $b) == $c"),
        @TriggerPattern (value="$c == ($a & $b)")
    })
    public static ErrorDescription checkIncompatibleMask1 (HintContext ctx) {
        TreePath treePath = ctx.getPath ();
        Map<String,TreePath> variables = ctx.getVariables ();
        Tree tree = variables.get ("$a").getLeaf ();
        Long v1 = getConstant (tree, ctx);
        if (v1 == null) {
            tree = variables.get ("$b").getLeaf ();
            v1 = getConstant (tree, ctx);
        }
        if (v1 == null)
            return null;
        tree = variables.get ("$c").getLeaf ();
        Long v2 = getConstant (tree, ctx);
        if (v2 == null)
            return null;

        if ((~v1 & v2) > 0)
            return ErrorDescriptionFactory.forName (
                ctx,
                treePath,
                MessageFormat.format (
                    NbBundle.getMessage (IncompatibleMask.class, "MSG_IncompatibleMask"),
                    treePath.getLeaf ().toString ()
                )
            );
        return null;
    }

    @TriggerPatterns ({
        @TriggerPattern (value="($a | $b) == $c"),
        @TriggerPattern (value="$c == ($a | $b)")
    })
    public static ErrorDescription checkIncompatibleMask2 (HintContext ctx) {
        TreePath treePath = ctx.getPath ();
        Map<String,TreePath> variables = ctx.getVariables ();
        Tree tree = variables.get ("$a").getLeaf ();
        Long v1 = getConstant (tree, ctx);
        if (v1 == null) {
            tree = variables.get ("$b").getLeaf ();
            v1 = getConstant (tree, ctx);
        }
        if (v1 == null)
            return null;
        tree = variables.get ("$c").getLeaf ();
        Long v2 = getConstant (tree, ctx);
        if (v2 == null)
            return null;

        if ((v1 & v2) != v1)
            return ErrorDescriptionFactory.forName (
                ctx,
                treePath,
                MessageFormat.format (
                    NbBundle.getMessage (IncompatibleMask.class, "MSG_IncompatibleMask"),
                    treePath.getLeaf ().toString ()
                )
            );
        return null;
    }

    static Long getConstant (
        Tree                    tree,
        HintContext             ctx
    ) {
        Object value = null;
        if (tree instanceof LiteralTree)
            value = ((LiteralTree) tree).getValue ();
        else
        if (tree instanceof ExpressionTree) {
            CompilationInfo compilationInfo = ctx.getInfo ();
            Trees trees = compilationInfo.getTrees ();
            TreePath identifierTreePath = trees.getPath (compilationInfo.getCompilationUnit (), tree);
            Element el = trees.getElement (identifierTreePath);
            if (el == null || el.getKind () != ElementKind.FIELD)
                return null;
            value = ((VariableElement) el).getConstantValue ();
        } else
            return null;

        if (value instanceof Integer)
            return new Long ((Integer) value);
        if (value instanceof Long)
            return (Long) value;
        return null;
    }
}
