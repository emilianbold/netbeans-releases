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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.javadoc.hints;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Pokorsky
 */
final class GenerateJavadocFix extends JavaFix {

    private static final int NOPOS = -2; // XXX copied from jackpot; should be in api
    private String name;
    private final SourceVersion spec;

    public GenerateJavadocFix(String name, TreePathHandle handle, SourceVersion spec) {
        super(handle);
        this.name = name;
        this.spec = spec;
    }

    @Override
    public String getText() {
        return NbBundle.getMessage(GenerateJavadocFix.class, "MISSING_JAVADOC_HINT", name); // NOI18N
    }

    @Override
    protected void performRewrite(TransformationContext ctx) throws Exception {
        WorkingCopy wc = ctx.getWorkingCopy();
        TreePath path = ctx.getPath();
        Element elm = wc.getTrees().getElement(path);
        
        Tree t = null;
        if (elm != null) {
            t = wc.getTrees().getTree(elm);
        }
        if (t != null) {
            final JavadocGenerator gen = new JavadocGenerator(spec);
            
            String javadocTxt = gen.generateComment(elm, wc);
            Comment javadoc = Comment.create(Comment.Style.JAVADOC, NOPOS, NOPOS, NOPOS, javadocTxt);
            final TreeMaker make = wc.getTreeMaker();
            
            Tree newTree;
            switch(t.getKind()) {
                case ANNOTATION_TYPE: {
                    ClassTree old = (ClassTree) t;
                    newTree = make.AnnotationType(old.getModifiers(), old.getSimpleName(), old.getMembers());
                    break;
                }
                case CLASS: {
                    ClassTree old = (ClassTree) t;
                    newTree = make.Class(old.getModifiers(), old.getSimpleName(), old.getTypeParameters(), old.getExtendsClause(), old.getImplementsClause(), old.getMembers());
                    break;
                }
                case ENUM: {
                    ClassTree old = (ClassTree) t;
                    newTree = make.Enum(old.getModifiers(), old.getSimpleName(), old.getImplementsClause(), old.getMembers());
                    break;
                }
                case INTERFACE: {
                    ClassTree old = (ClassTree) t;
                    newTree = make.Interface(old.getModifiers(), old.getSimpleName(), old.getTypeParameters(), old.getImplementsClause(), old.getMembers());
                    break;
                }
                case METHOD: {
                    MethodTree old = (MethodTree) t;
                    newTree = make.Method(old.getModifiers(), old.getName(), old.getReturnType(), old.getTypeParameters(), old.getParameters(), old.getThrows(), old.getBody(), (ExpressionTree) old.getDefaultValue());
                    break;
                }
                case VARIABLE: {
                    VariableTree old = (VariableTree) t;
                    newTree = make.Variable(old.getModifiers(), old.getName(), old.getType(), old.getInitializer());
                    break;
                }
                default:
                    newTree = null;
            }

            if(newTree != null) {
                make.addComment(newTree, javadoc, true);
                wc.rewrite(t, newTree);
            }
        }
    }
}
