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

package org.netbeans.modules.java.hints.processor;

import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementScanner6;
import javax.tools.Diagnostic;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
@SupportedAnnotationTypes("*")
@ServiceProvider(service=Processor.class)
public class CleaningAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            if (!shouldWorkaroundBug()) {
                return false;
            }

            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Attempting to workaround 6512707");

            final Trees t = Trees.instance(processingEnv);
            final Collection<JCTree> toClean = new LinkedList<JCTree>();

            new ElementScanner6<Void, Void>() {
                @Override
                public Void visitExecutable(ExecutableElement e, Void p) {
                    Tree tree = t.getTree(e);

                    if (tree != null && tree.getKind() == Kind.METHOD) {
                        JCMethodDecl jcTree = (JCMethodDecl) tree;

                        toClean.add(jcTree.defaultValue);
                    }
                    return super.visitExecutable(e, p);
                }
            }.scan(roundEnv.getRootElements(), null);

            Method cleanTrees = JavacProcessingEnvironment.class.getDeclaredMethod("cleanTrees", List.class);

            cleanTrees.setAccessible(true);
            cleanTrees.invoke(null, List.from(toClean.toArray(new JCTree[0])));
        } catch (Throwable t) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Failed with a Throwable: " + t.getMessage());
            //presumably OK
        }

        return false;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    private static final AtomicReference<Boolean> HAS_BUG = new AtomicReference<Boolean>();

    private boolean shouldWorkaroundBug() {
        Boolean result = HAS_BUG.get();

        if (result != null) {
            return result;
        }
        
        Context ctx = ((JavacProcessingEnvironment) processingEnv).getContext();
        TreeMaker make = TreeMaker.instance(ctx);

        final JCLiteral val = make.Literal("");
        JCMethodDecl method = make.MethodDef(null, null, null, List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), null, val);
        final boolean[] noBug = new boolean[] {false};

        new TreeScanner() {
            @Override
            public void scan(JCTree tree) {
                noBug[0] |= (tree == val);
                super.scan(tree);
            }
        }.scan(method);

        HAS_BUG.compareAndSet(null, Boolean.valueOf(!noBug[0]));

        return HAS_BUG.get();
    }

}
