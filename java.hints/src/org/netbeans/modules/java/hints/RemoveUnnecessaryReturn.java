/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.List;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.modules.java.hints.jackpot.code.spi.Hint;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPattern;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.jackpot.spi.JavaFix;
import org.netbeans.modules.java.hints.jackpot.spi.support.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
@Hint(category="general")
public class RemoveUnnecessaryReturn {

    @TriggerPattern("return $val$;")
    public static ErrorDescription hint(HintContext ctx) {
        TreePath tp = ctx.getPath();

        OUTER: while (tp != null && !TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
            Tree current = tp.getLeaf();
            List<? extends StatementTree> statements;

            tp = tp.getParentPath();

            switch (tp.getLeaf().getKind()) {
                case METHOD:
                    MethodTree mt = (MethodTree) tp.getLeaf();

                    if (mt.getReturnType() == null) {
                        if (mt.getName().contentEquals("<init>"))
                            break OUTER;//constructor
                        else
                            return null; //a method without a return type - better ignore
                    }
                    
                    TypeMirror tm = ctx.getInfo().getTrees().getTypeMirror(new TreePath(tp, mt.getReturnType()));

                    if (tm == null || tm.getKind() != TypeKind.VOID) return null;
                    break OUTER;
                case BLOCK: statements = ((BlockTree) tp.getLeaf()).getStatements(); break;
                case CASE: {
                    boolean exits = Utilities.exitsFromAllBranchers(ctx.getInfo(), tp);
                    
                    if (tp.getParentPath().getLeaf().getKind() == Kind.SWITCH) {
                        List<? extends CaseTree> cases = ((SwitchTree) tp.getParentPath().getLeaf()).getCases();
                        exits |= cases.get(cases.size() - 1) == tp.getLeaf();
                    }

                    if (!exits) return null;
                    
                    statements = ((CaseTree) tp.getLeaf()).getStatements();
                    break;
                }
                default: continue OUTER;
            }

            assert !statements.isEmpty();

            if (statements.get(statements.size() - 1) != current) return null;
        }

        String displayName = NbBundle.getMessage(RemoveUnnecessaryReturn.class, "ERR_UnnecessaryReturnStatement");
        String fixDisplayName = NbBundle.getMessage(RemoveUnnecessaryReturn.class, "FIX_UnnecessaryReturnStatement");
        
        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), displayName, JavaFix.removeFromParent(ctx, fixDisplayName, ctx.getPath()));
    }
}
