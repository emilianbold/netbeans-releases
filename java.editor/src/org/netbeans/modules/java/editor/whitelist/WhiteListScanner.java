/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.editor.whitelist;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.whitelist.WhiteListQuery.WhiteList;

/**
 *
 * @author Tomas Zezula
 */
class WhiteListScanner extends TreePathScanner<Void, List<? super WhiteListScanner.Problem>> {

    private final Trees trees;
    private final ElementUtilities elementUtil;
    private final AtomicBoolean cancel;
    private final WhiteList whiteList;
    private final ArrayDeque<MethodInvocationTree> methodInvocation;
    private boolean inheritance = false;

    WhiteListScanner(
        final Trees trees,
        final ElementUtilities elementUtil,
        final WhiteList whiteList,
        final AtomicBoolean cancel) {
        this.trees = trees;
        this.elementUtil = elementUtil;
        this.whiteList = whiteList;
        this.cancel = cancel;
        methodInvocation = new ArrayDeque<MethodInvocationTree>();
    }

    @Override
    public Void visitMethod(MethodTree node, List<? super Problem> p) {
        checkCancel();
        final ExecutableElement ee = (ExecutableElement) trees.getElement(getCurrentPath());
        if (ee != null) {
            for (ExecutableElement om = elementUtil.getOverriddenMethod(ee);
                 om!=null;
                 om = elementUtil.getOverriddenMethod(om)) {
                 if (!whiteList.canOverride(ElementHandle.create(om))){
                     p.add(new Problem(Kind.OVERRIDE, om, node));
                 }
            }
        }
        return super.visitMethod(node, p);
    }

    @Override
    public Void visitClass(ClassTree node, List<? super Problem> p) {
        checkCancel();
        scan(node.getModifiers(), p);
	scan(node.getTypeParameters(), p);
        inheritance = true;
        scan(node.getExtendsClause(), p);
        scan(node.getImplementsClause(), p);
        inheritance = false;
	scan(node.getMembers(), p);
        return null;
    }

    @Override
    public Void visitParameterizedType(ParameterizedTypeTree node, List<? super Problem> p) {
        scan(node.getType(), p);
        final boolean ci = inheritance;
        inheritance = false;
        scan(node.getTypeArguments(), p);
        inheritance = ci;
        return null;
    }


    @Override
    public Void visitIdentifier(IdentifierTree node, List<? super Problem> p) {
        handleNode(node,p);
        return super.visitIdentifier(node, p);
    }

    @Override
    public Void visitMemberSelect(MemberSelectTree node, List<? super Problem> p) {
        handleNode(node,p);
        return super.visitMemberSelect(node, p);
    }

    @Override
    public Void visitNewClass(NewClassTree node, List<? super Problem> p) {
        final Element e = trees.getElement(getCurrentPath());
        if (e != null && !whiteList.canInvoke(ElementHandle.create(e))) {
                p.add(new Problem(Kind.INVOKE, e, node));
        }
        scan(node.getTypeArguments(), p);
        scan(node.getArguments(), p);
	scan(node.getClassBody(), p);
        return null;
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, List<? super Problem> p) {
        methodInvocation.offerFirst(node);
        super.visitMethodInvocation(node, p);
        methodInvocation.removeFirst();
        return null;
    }

    private void handleNode(
            final Tree node,
            final List<? super Problem> p) {
        final Element e = trees.getElement(getCurrentPath());
        if (e == null) {
            return;
        }
        final ElementKind k = e.getKind();
        if (k.isClass() || k.isInterface()) {
            if (inheritance) {
                if (!whiteList.canOverride(ElementHandle.create(e))) {
                    p.add(new Problem(Kind.SUBCLASS, e, node));
                }
            } else {
                if (!whiteList.canInvoke(ElementHandle.create(e))) {
                    p.add(new Problem(Kind.REFERENCE, e, node));
                }
            }
        } else if (k == ElementKind.METHOD || k == ElementKind.CONSTRUCTOR) {
            if (!(methodInvocation.isEmpty() || whiteList.canInvoke(ElementHandle.create(e)))) {
                p.add(new Problem(Kind.INVOKE, e, methodInvocation.peekFirst()));
            }
        }
    }

    private void checkCancel() {
        if (cancel.get()) {
            throw new Cancel();
        }
    }


    static enum Kind {
        INVOKE,
        REFERENCE,
        SUBCLASS,
        OVERRIDE
    }

    static final class Problem {
        final Kind kind;
        final Element element;
        final Tree tree;

        private Problem (
                final Kind kind,
                final Element element,
                final Tree tree) {
            this.kind = kind;
            this.element = element;
            this.tree = tree;
        }
    }

    static final class Cancel extends RuntimeException {
    }

}
