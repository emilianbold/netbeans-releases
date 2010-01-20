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

import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
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
@Hint(category="general")
public class ShiftOutOfRange {

    @TriggerPatterns ({
        @TriggerPattern (value="$v >> $c"),
        @TriggerPattern (value="$v >>> $c"),
        @TriggerPattern (value="$v << $c")
    })
    public static ErrorDescription checkLoggerDeclaration (HintContext ctx) {
        TreePath treePath = ctx.getPath ();
        CompilationInfo compilationInfo = ctx.getInfo ();
        Trees trees = compilationInfo.getTrees ();
        JCBinary binary = (JCBinary) treePath.getLeaf ();
        Element e = trees.getElement (treePath);
        if (binary.getRightOperand () instanceof JCLiteral) {
            JCLiteral literal = (JCLiteral) binary.getRightOperand ();
            Object objectValue = literal.getValue ();
            long value = 0;
            if (objectValue instanceof Integer)
                value = (Integer) objectValue;
            else
            if (objectValue instanceof Long)
                value = (Long) objectValue;
            else
            if (objectValue instanceof Character)
                value = (Character) objectValue;
            else
                return null;
            JCExpression identifier = binary.getLeftOperand ();
            TreePath identifierTreePath = trees.getPath (compilationInfo.getCompilationUnit (), identifier);
            TypeMirror typeMirror = trees.getTypeMirror (identifierTreePath);
            if (typeMirror.toString ().equals ("int")) {
                if (value < 0 || value > 31)
                    return ErrorDescriptionFactory.forName (
                            ctx,
                            treePath,
                            NbBundle.getMessage (ShiftOutOfRange.class, "MSG_ShiftOutOfRange_int", e)
                    );
            } else
            if (typeMirror.toString ().equals ("long")) {
                if (value < 0 || value > 63)
                    return ErrorDescriptionFactory.forName (
                            ctx,
                            treePath,
                            NbBundle.getMessage (ShiftOutOfRange.class, "MSG_ShiftOutOfRange_long", e)
                    );
            }
        }
        return null;
    }
}
