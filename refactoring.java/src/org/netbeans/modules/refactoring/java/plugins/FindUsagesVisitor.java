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
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.WhereUsedElement;
import org.netbeans.modules.refactoring.java.spi.JavaWhereUsedFilters;
import org.netbeans.modules.refactoring.java.spi.ToPhaseException;
import org.openide.ErrorManager;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Becicka
 */
public class FindUsagesVisitor extends TreePathScanner<Tree, Element> {

    private Collection<TreePath> usages = new ArrayList<TreePath>();
    private List<WhereUsedElement> elements = new ArrayList<WhereUsedElement>();
    protected CompilationController workingCopy;
    private boolean findInComments = false;
    private final boolean fromTestRoot;
    private final AtomicBoolean inImport;
    private Boolean usagesInComments;

    public FindUsagesVisitor(CompilationController workingCopy) {
        this(workingCopy, false);
    }
    
    public FindUsagesVisitor(CompilationController workingCopy, boolean findInComments) {
        this(workingCopy, findInComments, RefactoringUtils.isFromTestRoot(workingCopy.getFileObject(), workingCopy.getClasspathInfo().getClassPath(PathKind.SOURCE)), new AtomicBoolean());
    }

    public FindUsagesVisitor(CompilationController workingCopy, boolean findInComments, boolean fromTestRoot, AtomicBoolean inImport) {
        try {
            setWorkingCopy(workingCopy);
        } catch (ToPhaseException ex) {
            Exceptions.printStackTrace(ex);
        }
        this.findInComments = findInComments;
        this.fromTestRoot = fromTestRoot;
        this.inImport = inImport;
    }

    //<editor-fold defaultstate="collapsed" desc="Find in Comments">
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
                            WhereUsedElement comment = WhereUsedElement.create(ts.offset() + tokenizer.match().start(),
                                                                               ts.offset() + tokenizer.match().end(), workingCopy, fromTestRoot);
                            elements.add(comment);
                            usagesInComments = true;
                        }
                    }
                }
            }
        }
        return super.visitCompilationUnit(node, p);
    }
    //</editor-fold>

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
                ImportTree impTree = (ImportTree) path.getLeaf();
                if (!impTree.isStatic()) {
                    return;
                }
                Tree idTree = impTree.getQualifiedIdentifier();
                if (idTree.getKind() != Kind.MEMBER_SELECT) {
                    return;
                }
                final Name id = ((MemberSelectTree) idTree).getIdentifier();
                if (id.contentEquals("*")) {
                    return;
                }
                Tree classTree = ((MemberSelectTree) idTree).getExpression();
                path = trees.getPath(workingCopy.getCompilationUnit(), classTree);
                el = trees.getElement(path);
                if (el == null) {
                    return;
                }
                Iterator iter = workingCopy.getElementUtilities().getMembers(el.asType(), new ElementUtilities.ElementAcceptor() {
                    @Override
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
        if (elementToFind != null && elementToFind.getKind() == ElementKind.METHOD && el.getKind() == ElementKind.METHOD) {
            if (el.equals(elementToFind) || workingCopy.getElements().overrides((ExecutableElement) el, (ExecutableElement) elementToFind, (TypeElement) elementToFind.getEnclosingElement())) {
                addUsage(path);
            }
        } else if (el.equals(elementToFind)) {
            final ElementKind kind = elementToFind.getKind();
            if(kind.isField() || kind == ElementKind.LOCAL_VARIABLE || kind == ElementKind.RESOURCE_VARIABLE) {
                JavaWhereUsedFilters.ReadWrite access = JavaWhereUsedFilters.ReadWrite.READ;
                TreePath parentPath = path.getParentPath();
                Tree parentTree = parentPath.getLeaf();
                Kind parentKind = parentTree.getKind();
                
                switch(parentKind) {
                    case ARRAY_ACCESS:
                    case MEMBER_SELECT:
                        // TODO: Check usages of arrays for writing
                        break;

                    case POSTFIX_INCREMENT:
                    case POSTFIX_DECREMENT:
                    case PREFIX_INCREMENT:
                    case PREFIX_DECREMENT:
                        access = JavaWhereUsedFilters.ReadWrite.READ_WRITE;
                        break;

                    case ASSIGNMENT: {
                        AssignmentTree assignmentTree = (AssignmentTree) parentTree;
                        ExpressionTree left = assignmentTree.getVariable();
                        if (left.equals(tree)) {
                            access = JavaWhereUsedFilters.ReadWrite.WRITE;
                        }
                        break;
                    }
                    case MULTIPLY_ASSIGNMENT:
                    case DIVIDE_ASSIGNMENT:
                    case REMAINDER_ASSIGNMENT:
                    case PLUS_ASSIGNMENT:
                    case MINUS_ASSIGNMENT:
                    case LEFT_SHIFT_ASSIGNMENT:
                    case RIGHT_SHIFT_ASSIGNMENT:
                    case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
                    case AND_ASSIGNMENT:
                    case XOR_ASSIGNMENT:
                    case OR_ASSIGNMENT: {
                        CompoundAssignmentTree compoundAssignmentTree = (CompoundAssignmentTree) parentTree;
                        ExpressionTree left = compoundAssignmentTree.getVariable();
                        if (left.equals(tree)) {
                            access = JavaWhereUsedFilters.ReadWrite.READ_WRITE;
                        }
                        break;
                    }
                }
                addUsage(path, access);
            } else {
                addUsage(path);
            }
        }
    }

    /**
     *
     * @param workingCopy
     * @throws org.netbeans.modules.refactoring.java.spi.ToPhaseException
     */
    public final void setWorkingCopy(CompilationController workingCopy) throws ToPhaseException {
        this.workingCopy = workingCopy;
        try {
            if (this.workingCopy.toPhase(JavaSource.Phase.RESOLVED) != JavaSource.Phase.RESOLVED) {
                throw new ToPhaseException();
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
    }
    
    protected void addUsage(TreePath tp, JavaWhereUsedFilters.ReadWrite access) {
        assert tp != null;
        elements.add(WhereUsedElement.create(workingCopy, tp, access, fromTestRoot, inImport));
        usages.add(tp);
    }

    public boolean isInImport() {
        return inImport.get();
    }

    protected void addUsage(TreePath tp) {
        assert tp != null;
        elements.add(WhereUsedElement.create(workingCopy, tp, fromTestRoot, inImport));
        usages.add(tp);
    }
    
    public Collection<WhereUsedElement> getElements() {
        if(findInComments) { // the elements need to be sorted. Comments are searched for the whole file at once.
            Collections.sort(elements, new Comparator<WhereUsedElement>() {

                @Override
                public int compare(WhereUsedElement o1, WhereUsedElement o2) {
                    return o1.getPosition().getBegin().getOffset() - o2.getPosition().getBegin().getOffset();
                }
            });
        }
        return elements;
    }
    
    public Collection<TreePath> getUsages() {
        return usages;
    }

    @Override
    public Tree visitIdentifier(IdentifierTree node, Element p) {
        addIfMatch(getCurrentPath(), node, p);
        return super.visitIdentifier(node, p);
    }

    @Override
    public Tree visitMemberSelect(MemberSelectTree node, Element p) {
        addIfMatch(getCurrentPath(), node, p);
        return super.visitMemberSelect(node, p);
    }

    @Override
    public Tree visitNewClass(NewClassTree node, Element p) {
        Trees trees = workingCopy.getTrees();
        ClassTree classTree = ((NewClassTree) node).getClassBody();
        if (classTree != null && p.getKind() == ElementKind.CONSTRUCTOR) {
            Element anonClass = workingCopy.getTrees().getElement(TreePath.getPath(workingCopy.getCompilationUnit(), classTree));
            if (anonClass == null) {
                Logger.getLogger("org.netbeans.modules.refactoring.java").log(Level.SEVERE, "FindUsages cannot resolve {0}", classTree); // NOI18N
            } else {
                for (ExecutableElement c : ElementFilter.constructorsIn(anonClass.getEnclosedElements())) {
                    MethodTree t = workingCopy.getTrees().getTree(c);
                    TreePath superCall = trees.getPath(workingCopy.getCompilationUnit(), ((ExpressionStatementTree) t.getBody().getStatements().get(0)).getExpression());
                    Element superCallElement = trees.getElement(superCall);
                    if (superCallElement != null && superCallElement.equals(p) && !workingCopy.getTreeUtilities().isSynthetic(superCall)) {
                        addUsage(superCall);
                    }
                }
            }
        } else {
            addIfMatch(getCurrentPath(), node, p);
        }
        return super.visitNewClass(node, p);
    }

    public boolean usagesInComments() {
        return Boolean.TRUE == usagesInComments;
    }
}
