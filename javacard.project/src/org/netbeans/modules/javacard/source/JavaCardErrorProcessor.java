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
package org.netbeans.modules.javacard.source;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic.Kind;
import org.openide.util.NbBundle;
/**
 * Provides error markings for uses of floating point and other unsupported
 * java language features in Java Card.
 */
@SupportedAnnotationTypes("*") //NOI18N
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedOptions(JavaCardErrorProcessor.JAVACARD_OPTION)
public final class JavaCardErrorProcessor extends AbstractProcessor {
    static final String JAVACARD_OPTION = "netbeans_private_enable_javacard_processor"; //NOI18N
    //PENDING (post 6.9):  Allow an SDK to encode this list in platform.properties
    private static final Set<TypeKind> UNSUPPORTED_PRIMITIVES_CLASSIC_PROJECTS =
            EnumSet.of(TypeKind.DOUBLE, TypeKind.FLOAT, TypeKind.LONG, TypeKind.CHAR);
    private static final Set<TypeKind> UNSUPPORTED_PRIMITIVES_EXTENDED_PROJECTS =
            EnumSet.of(TypeKind.DOUBLE, TypeKind.FLOAT);
    private static final Set<Tree.Kind> UNSUPPORTED_LITERALS_CLASSIC_PROJECTS =
            EnumSet.of(Tree.Kind.STRING_LITERAL, Tree.Kind.DOUBLE_LITERAL,
            Tree.Kind.FLOAT_LITERAL, Tree.Kind.LONG_LITERAL);
    private static final Set<Tree.Kind> UNSUPPORTED_LITERALS_EXTENDED_PROJECTS =
            EnumSet.of(Tree.Kind.DOUBLE_LITERAL, Tree.Kind.FLOAT_LITERAL);
    private ProcessingEnvironment env;

    @Override
    public void init(ProcessingEnvironment env) {
        this.env = env;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<String, String> m = env.getOptions();
        boolean containsKey = m.containsKey(JAVACARD_OPTION);

        if (!containsKey) return false;

        boolean classic = Boolean.valueOf(m.get(JAVACARD_OPTION));
        Set<TypeElement> topLevel = new HashSet<TypeElement>();

        for (Element el : roundEnv.getRootElements()) {
            if (el.getEnclosingElement().getKind() == ElementKind.PACKAGE) {
                topLevel.add((TypeElement) el);
                break;
            }
            el = el.getEnclosingElement();
        }

        Trees t = Trees.instance(env);

        for (TypeElement clazz : topLevel) {
            TreePath clazzTP = t.getPath(clazz);

            if (clazzTP == null) {
                //XXX: can happen?
                continue;
            }

            new ScannerImpl(env).scan(clazzTP, classic);
        }

        return false;
    }

    private static final class ScannerImpl extends TreePathScanner<Void, Boolean> {
        private final ProcessingEnvironment env;
        
        private boolean insideAnnotation;
        
        ScannerImpl (ProcessingEnvironment env) {
            this.env = env;
        }

        @Override
        public Void visitAnnotation(AnnotationTree node, Boolean p) {
            boolean oldInsideAnnotation = insideAnnotation;
            insideAnnotation = true;
            try {
                return super.visitAnnotation(node, p);
            } finally {
                insideAnnotation = oldInsideAnnotation;
            }
        }

        @Override
        public Void visitPrimitiveType(PrimitiveTypeTree node, Boolean classic) {
            Set<TypeKind> unsupported = classic ? UNSUPPORTED_PRIMITIVES_CLASSIC_PROJECTS : UNSUPPORTED_PRIMITIVES_EXTENDED_PROJECTS;
            TypeKind kind = node.getPrimitiveTypeKind();
            if (unsupported.contains(kind)) {
                String kindName = NbBundle.getMessage(JavaCardErrorProcessor.class, kind.name());
                Trees.instance(env).printMessage(Kind.ERROR, NbBundle.getMessage(
                        JavaCardErrorProcessor.class, classic ? "ERR_CLASSIC" : "ERR_EXT", //NOI18N
                        kindName), node, getCurrentPath().getCompilationUnit());
            }
            return super.visitPrimitiveType(node, classic);
        }

        @Override
        public Void visitLiteral(LiteralTree node, Boolean classic) {
            Tree.Kind kind = node.getKind();
            Set<Tree.Kind> unsupported = classic ? UNSUPPORTED_LITERALS_CLASSIC_PROJECTS : UNSUPPORTED_LITERALS_EXTENDED_PROJECTS;
            if (unsupported.contains(node.getKind()) && !insideAnnotation) {
                String kindName = NbBundle.getMessage(JavaCardErrorProcessor.class, kind.name());
                Trees.instance(env).printMessage(Kind.ERROR, NbBundle.getMessage(
                        JavaCardErrorProcessor.class, classic ? "LIT_ERR_CLASSIC" : "LIT_ERR_EXT", //NOI18N
                        kindName), node, getCurrentPath().getCompilationUnit());
            }
            return super.visitLiteral(node, classic);
        }

        @Override
        public Void visitMethod(MethodTree node, Boolean p) {
            if (node.getModifiers().getFlags().contains(Modifier.STRICTFP)) {
                        Trees.instance(env).printMessage(Kind.ERROR,
                        NbBundle.getMessage(JavaCardErrorProcessor.class, "ERR_STRICTFP"), //NOI18N
                        node, getCurrentPath().getCompilationUnit());
            }
            return super.visitMethod(node, p);
        }

        @Override
        public Void visitClass(ClassTree node, Boolean p) {
           if (node.getModifiers().getFlags().contains(Modifier.STRICTFP)) {
                        Trees.instance(env).printMessage(Kind.ERROR, NbBundle.getMessage(
                        JavaCardErrorProcessor.class, "ERR_STRICTFP"), //NOI18N
                        node, getCurrentPath().getCompilationUnit());
           }
           return super.visitClass(node, p);
        }
    }
}
