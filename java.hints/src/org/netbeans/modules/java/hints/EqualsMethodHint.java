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
 * Portions Copyrighted 2007-2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints;

import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.modules.java.hints.spi.support.FixFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class EqualsMethodHint extends AbstractHint {

    private static final String SUPPRESS_KEY = "EqualsWhichDoesntCheckParameterClass";

    public EqualsMethodHint() {
        super(true, false, HintSeverity.WARNING, SUPPRESS_KEY);
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(EqualsMethodHint.class, "DSC_EqualsMethod");
    }

    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.METHOD);
    }

    public List<ErrorDescription> run(CompilationInfo info, TreePath treePath) {
        if (!getTreeKinds().contains(treePath.getLeaf().getKind())) {
            return null;
        }
        
        if (info.getTreeUtilities().isSynthetic(treePath)) {
            return null;
        }
        
        MethodTree mt = (MethodTree) treePath.getLeaf();
        
        if (!mt.getName().contentEquals("equals")) { // NOI18N
            return null;
        }
        
        Element current = info.getTrees().getElement(treePath);
        
        if (current == null || current.getKind() != ElementKind.METHOD) {
            return null;
        }
        
        ExecutableElement currentMethod = (ExecutableElement) current;
        
        ExecutableElement equals = null;
        TypeElement jlObject = info.getElements().getTypeElement("java.lang.Object"); // NOI18N
        
        if (jlObject != null) {
            for (ExecutableElement ee : ElementFilter.methodsIn(jlObject.getEnclosedElements())) {
                if (ee.getSimpleName().contentEquals("equals")) { // NOI18N
                    equals = ee;
                    break;
                }
            }
        }
        
        if (equals == null || !info.getElements().overrides(currentMethod, equals, (TypeElement) currentMethod.getEnclosingElement())) {
            return null;
        }
        
        Element param = info.getTrees().getElement(new TreePath(treePath, mt.getParameters().get(0)));
        
        if (param == null || param.getKind() != ElementKind.PARAMETER) {
            return null;
        }
        
        if (mt.getBody() == null) {
            //#134255: body may be null
            return null;
        }
        
        try {
            new VisitorImpl(info, param).scan(new TreePath(treePath, mt.getBody()), null);
        } catch (Found f) {
            return null;
        }
        
        int[] span = info.getTreeUtilities().findNameSpan(mt);
        
        if (span == null) {
            return null;
        }

        ErrorDescription ed = ErrorDescriptionFactory.createErrorDescription(getSeverity().toEditorSeverity(),
                NbBundle.getMessage(EqualsMethodHint.class, "ERR_EQUALS_NOT_CHECKING_TYPE"),
                FixFactory.createSuppressWarnings(info, treePath, SUPPRESS_KEY),
                info.getFileObject(),
                span[0],
                span[1]);

        return Collections.singletonList(ed);
    }

    public String getId() {
        return EqualsMethodHint.class.getName();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(EqualsMethodHint.class, "DN_EqualsMethod");
    }

    public void cancel() {
    }
    
    private static final class VisitorImpl extends TreePathScanner<Void, Void> {
        
        private CompilationInfo info;
        private Element parameter;

        public VisitorImpl(CompilationInfo info, Element parameter) {
            this.info = info;
            this.parameter = parameter;
        }

        @Override
        public Void visitInstanceOf(InstanceOfTree node, Void p) {
            Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getExpression()));
            
            if (parameter.equals(e)) {
                throw new Found();
            }
            
            return super.visitInstanceOf(node, p);
        }

        @Override
        public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
            if (node.getArguments().isEmpty() && node.getMethodSelect().getKind() == Kind.MEMBER_SELECT) {
                MemberSelectTree mst = (MemberSelectTree) node.getMethodSelect();
                Element e = info.getTrees().getElement(new TreePath(new TreePath(getCurrentPath(), mst), mst.getExpression()));

                if (parameter.equals(e) && mst.getIdentifier().contentEquals("getClass")) { // NOI18N
                    throw new Found();
                }
            }
            
            return super.visitMethodInvocation(node, p);
        }
        
    }

    private static final class Found extends RuntimeException {}
}
