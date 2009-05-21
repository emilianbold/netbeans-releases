/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.*;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Scanner;
import java.util.logging.Logger;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;

/**
 *
 * @author Jan Becicka
 */
public class FindUsagesVisitor extends FindVisitor {

    private boolean findInComments = false;
    private Collection<UsageInComment> usagesInComments = Collections.<UsageInComment>emptyList();
    public FindUsagesVisitor(WorkingCopy workingCopy) {
        super(workingCopy);
    }

    public Collection<UsageInComment> getUsagesInComments() {
        return usagesInComments;
    }

    public FindUsagesVisitor(WorkingCopy workingCopy, boolean findInComments) {
        super(workingCopy);
        this.findInComments = findInComments;
        if (findInComments) {
            usagesInComments = new ArrayList<UsageInComment>();
        }
    }

    @Override
    public Tree visitCompilationUnit(CompilationUnitTree node, Element p) {
        if (findInComments) {
            String originalName = p.getSimpleName().toString();
            TokenSequence<JavaTokenId> ts = workingCopy.getTokenHierarchy().tokenSequence(JavaTokenId.language());
            
            while (ts.moveNext()) {
                Token t = ts.token();
                
                if (t.id() == JavaTokenId.BLOCK_COMMENT || t.id() == JavaTokenId.LINE_COMMENT || t.id() == JavaTokenId.JAVADOC_COMMENT) {
                    Scanner tokenizer = new Scanner(t.text().toString());
                    tokenizer.useDelimiter("[^a-zA-Z0-9_]"); //NOI18N
                    
                    while (tokenizer.hasNext()) {
                        String current = tokenizer.next();
                        if (current.equals(originalName)) {
                            usagesInComments.add(new UsageInComment(ts.offset() + tokenizer.match().start(), ts.offset() + tokenizer.match().end()));
                        }
                    }
                }
            }
        }
        return super.visitCompilationUnit(node, p);
    }

    @Override
    public Tree visitIdentifier(IdentifierTree node, Element p) {
        addIfMatch(getCurrentPath(), node, p);
        return super.visitIdentifier(node, p);
    }

    @Override
    public Tree visitMemberSelect(MemberSelectTree node, Element p) {
        addIfMatch(getCurrentPath(), node,p);
        return super.visitMemberSelect(node, p);
    }
    
    @Override
    public Tree visitNewClass(NewClassTree node, Element p) {
        Trees trees = workingCopy.getTrees();
        ClassTree classTree = ((NewClassTree) node).getClassBody();
        if (classTree != null && p.getKind()==ElementKind.CONSTRUCTOR) {
            Element anonClass = workingCopy.getTrees().getElement(TreePath.getPath(workingCopy.getCompilationUnit(), classTree));
            if (anonClass==null) {
                Logger.getLogger("org.netbeans.modules.refactoring.java").severe("FindUsages cannot resolve " + classTree);
            } else {
                for (ExecutableElement c : ElementFilter.constructorsIn(anonClass.getEnclosedElements())) {
                    MethodTree t = workingCopy.getTrees().getTree(c);
                    TreePath superCall = trees.getPath(workingCopy.getCompilationUnit(), ((ExpressionStatementTree) t.getBody().getStatements().get(0)).getExpression());
                    Element superCallElement = trees.getElement(superCall);
                    if (superCallElement != null && superCallElement.equals(p)) {
                        addUsage(superCall);
                    }
                }
            }
        } else {
            addIfMatch(getCurrentPath(), node, p);
        }
        return super.visitNewClass(node, p);
    }
    
    private void addIfMatch(TreePath path, Tree tree, Element elementToFind) {
        if (workingCopy.getTreeUtilities().isSynthetic(path)) {
            if (ElementKind.CONSTRUCTOR != elementToFind.getKind()
                    || tree.getKind() != Tree.Kind.IDENTIFIER
                    || !"super".contentEquals(((IdentifierTree) tree).getName())) { // NOI18N
                // do not skip synthetic usages of constructor
                return;
            }
        }
        Trees trees = workingCopy.getTrees();
        Element el = trees.getElement(path);
        if (el == null) {
            path = path.getParentPath();
            if (path != null && path.getLeaf().getKind() == Kind.IMPORT) {
                ImportTree impTree = (ImportTree)path.getLeaf();
                if (!impTree.isStatic()) {
                    return;
                }
                Tree idTree = impTree.getQualifiedIdentifier();
                if (idTree.getKind() != Kind.MEMBER_SELECT) {
                    return;
                }
                final Name id = ((MemberSelectTree) idTree).getIdentifier();
                Tree classTree = ((MemberSelectTree) idTree).getExpression();
                path = trees.getPath(workingCopy.getCompilationUnit(), classTree);
                el = trees.getElement(path);
                if (el == null) {
                    return;
                }
                Iterator iter = workingCopy.getElementUtilities().getMembers(el.asType(),new ElementUtilities.ElementAcceptor() {
                    public boolean accept(Element e, TypeMirror type) {
                        return id.equals(e.getSimpleName());
                    }
                }).iterator();
                if (iter.hasNext()) {
                    el = (Element) iter.next();
                }
                if (iter.hasNext()) {
                    return;
                }
            } else {
                return;
            }
        }
        if (elementToFind!=null&& elementToFind.getKind() == ElementKind.METHOD && el.getKind() == ElementKind.METHOD) {
            if (el.equals(elementToFind) || workingCopy.getElements().overrides((ExecutableElement) el, (ExecutableElement) elementToFind, (TypeElement) elementToFind.getEnclosingElement())) {
                addUsage(getCurrentPath());
            }
        } else if (el.equals(elementToFind)) {
            addUsage(getCurrentPath());
        }
    }
    
    public static class UsageInComment {
        int from;
        int to;
        public UsageInComment(int from, int to) {
            this.from = from;
            this.to = to;
        }
    }
}

