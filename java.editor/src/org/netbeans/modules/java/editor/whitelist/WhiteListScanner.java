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
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.whitelist.WhiteListQuery;
import org.netbeans.api.whitelist.WhiteListQuery.WhiteList;

/**
 *
 * @author Tomas Zezula
 */
class WhiteListScanner extends TreePathScanner<Void, List<? super WhiteListScanner.Problem>> {

    private final Trees trees;
    private final AtomicBoolean cancel;
    private final WhiteList whiteList;
    private final ArrayDeque<MethodInvocationTree> methodInvocation;

    WhiteListScanner(
        final Trees trees,
        final WhiteList whiteList,
        final AtomicBoolean cancel) {
        this.trees = trees;
        this.whiteList = whiteList;
        this.cancel = cancel;
        methodInvocation = new ArrayDeque<MethodInvocationTree>();
    }

    @Override
    public Void visitMethod(MethodTree node, List<? super Problem> p) {
        checkCancel();
        return super.visitMethod(node, p);
    }

    @Override
    public Void visitClass(ClassTree node, List<? super Problem> p) {
        checkCancel();
        return super.visitClass(node, p);
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
        final WhiteListQuery.Result res;
        if (e != null && !(res=whiteList.check(ElementHandle.create(e),WhiteListQuery.Operation.USAGE)).isAllowed()) {
                p.add(new Problem(node,res.getViolatedRuleDescription()));
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
        Tree toReport =  null;
        if (k.isClass() || k.isInterface()) {
            toReport=node;
        } else if ((k == ElementKind.METHOD || k == ElementKind.CONSTRUCTOR) &&
                !methodInvocation.isEmpty()) {
            toReport=methodInvocation.peekFirst();
        }
        final WhiteListQuery.Result res;
        if (toReport != null &&
            !(res=whiteList.check(ElementHandle.create(e),WhiteListQuery.Operation.USAGE)).isAllowed()) {
                p.add(new Problem(toReport,res.getViolatedRuleDescription()));
        }
    }

    private void checkCancel() {
        if (cancel.get()) {
            throw new Cancel();
        }
    }

    static final class Problem {
        final Tree tree;
        final String description;

        private Problem (
                final Tree tree,
                final String description) {
            this.tree = tree;
            this.description = description;
        }
    }

    static final class Cancel extends RuntimeException {
    }

}
