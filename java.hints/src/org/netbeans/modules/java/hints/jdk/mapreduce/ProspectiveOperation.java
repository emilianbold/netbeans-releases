/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s): Alexandru Gyori <Alexandru.Gyori at gmail.com>
 * 
 * Portions Copyrighted 2012-2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.jdk.mapreduce;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author alexandrugyori
 */
class ProspectiveOperation {

    private Tree blockify(StatementTree correspondingTree) {
        return treeMaker.Block(Arrays.asList(correspondingTree), false);
    }

    public Boolean isLazy() {
        return this.opType == OperationType.MAP || this.opType == OperationType.FILTER;
    }

    private Boolean isMergeable() {
        return this.opType == OperationType.FOREACH
                || this.opType == OperationType.MAP
                || this.opType == OperationType.FILTER;
    }

    boolean shouldReturn() {
        return this.opType == OperationType.ANYMATCH || this.opType == OperationType.NONEMATCH;
    }

    boolean shouldAssign() {
        return this.opType == OperationType.REDUCE;
    }

    private Set<Name> buildAvailables(PreconditionsChecker.VariablesVisitor treeVariableVisitor) {
        Set<Name> allVariablesUsedInCurrentOp = treeVariableVisitor.getAllLocalVariablesUsed();
        Set<Name> allVariablesDeclaredInCurrentOp = treeVariableVisitor.getInnervariables();
        allVariablesUsedInCurrentOp.addAll(allVariablesDeclaredInCurrentOp);
        return allVariablesUsedInCurrentOp;
    }

    private Set<Name> buildNeeded(PreconditionsChecker.VariablesVisitor treeVariableVisitor) {
        Set<Name> allVariablesUsedInCurrentOp = treeVariableVisitor.getAllLocalVariablesUsed();
        //Remove the ones also declared in the current block.
        allVariablesUsedInCurrentOp.removeAll(treeVariableVisitor.getInnervariables());
        //Keeps the ones that are local to the loop. These are the ones that need to be passed around
        //in a pipe-like fashion.
        allVariablesUsedInCurrentOp.retainAll(this.innerLoopVariables);
        return allVariablesUsedInCurrentOp;
    }

    public static enum OperationType {

        MAP, FOREACH, FILTER, REDUCE, ANYMATCH, NONEMATCH
    }
    private OperationType opType;
    private StatementTree correspondingTree;
    private final Set<Name> innerLoopVariables;
    private final TreeMaker treeMaker;
    private final CompilationInfo workingCopy;
    private final Map<Name, String> varToType;

    private ProspectiveOperation(StatementTree tree, OperationType operationType, Set<Name> innerLoopVariables, WorkingCopy workingCopy, Map<Name, String> varToType) {
        this.opType = operationType;
        this.correspondingTree = tree;
        this.innerLoopVariables = innerLoopVariables;
        this.treeMaker = workingCopy.getTreeMaker();
        this.workingCopy = workingCopy;
        this.varToType = varToType;
    }

    //Creates a non-eager operation according to the tree type
    public static ProspectiveOperation createOperator(StatementTree tree,
            OperationType operationType, PreconditionsChecker precond, WorkingCopy workingCopy) {
        ProspectiveOperation operation = new ProspectiveOperation(tree, operationType, precond.getInnerVariables(), workingCopy, precond.getVarToName());
        operation.getNeededVariables();
        return operation;
    }

    public static List<ProspectiveOperation> mergeIntoComposableOperations(List<ProspectiveOperation> ls) {
        List<ProspectiveOperation> result = mergeRecursivellyIntoComposableOperations(ls);
        if (result == null || result.contains(null)) {
            return null;
        } else {
            return result;
        }
    }

    private static List<ProspectiveOperation> mergeRecursivellyIntoComposableOperations(List<ProspectiveOperation> ls) {
        for (int i = ls.size() - 1; i > 0; i--) {
            ProspectiveOperation current = ls.get(i);
            ProspectiveOperation prev = ls.get(i - 1);
            if (!(areComposable(current, prev))) {
                if (!current.isMergeable() || !prev.isMergeable()) {
                    return null;
                }
                if (current.opType == OperationType.FILTER || prev.opType == OperationType.FILTER) {
                    int lengthOfLs;
                    ProspectiveOperation last;
                    ProspectiveOperation nlast;
                    while ((lengthOfLs = ls.size()) > i) {
                        last = ls.get(lengthOfLs - 1);
                        nlast = ls.get(lengthOfLs - 2);
                        ls.remove(lengthOfLs - 1);
                        //method mutates in place, no need to remove and add again.
                        nlast.merge(last);
                    }
                } else {
                    prev.merge(current);
                    ls.remove(i);
                }
            }
        }
        beautify(ls);
        return ls;
    }

    private static void beautify(List<ProspectiveOperation> ls) {
        for (int i = ls.size() - 1; i > 0; i--) {
            ProspectiveOperation current = ls.get(i - 1);
            ProspectiveOperation next = ls.get(i);
            Set<Name> needed = next.getNeededVariables();
            current.beautify(needed);
        }
    }

    public StatementTree getCorrespondingTree() {
        return this.correspondingTree;
    }

    public void eagerize() {
        if (this.opType == OperationType.MAP) {
            this.opType = OperationType.FOREACH;
        }
    }

    private void beautify(Set<Name> needed) {
        if (this.opType == OperationType.MAP) {
            beautifyLazy(needed);
        }
    }

    private void beautifyLazy(Set<Name> needed) {
        if (needed.isEmpty()) {
            {
                if (!this.neededVariables.isEmpty()) {
                    this.beautify(this.neededVariables);
                } else {
                    Set<Name> newSet = new HashSet<Name>();
                    newSet.add(null);
                    beautifyLazy(newSet);
                };
            }
        } else {
            StatementTree currentTree = this.correspondingTree;
            if (currentTree.getKind() == Tree.Kind.BLOCK) {
                BlockTree currentBlock = (BlockTree) currentTree;
                if (currentBlock.getStatements().size() == 1) {
                    this.correspondingTree = currentBlock.getStatements().get(0);
                    this.beautify(needed);
                } else {
                    this.correspondingTree = this.addReturn(currentBlock, getOneFromSet(needed));
                }
            } else if (currentTree.getKind() == Tree.Kind.VARIABLE) {
                VariableTree varTree = (VariableTree) currentTree;
                if (needed.contains(varTree.getName())) {
                    this.correspondingTree = treeMaker.ExpressionStatement(varTree.getInitializer());
                } else {
                    this.correspondingTree = this.addReturn(currentTree, getOneFromSet(needed));
                }
            } else if (currentTree.getKind() == Tree.Kind.ASSIGNMENT) {
                AssignmentTree assigned = (AssignmentTree) currentTree;
                ExpressionTree variable = assigned.getVariable();
                if (variable.getKind() == Tree.Kind.IDENTIFIER) {
                    IdentifierTree id = (IdentifierTree) variable;

                    if (needed.contains(id.getName())) {
                        this.correspondingTree = treeMaker.ExpressionStatement(assigned.getExpression());
                    } else {
                        this.correspondingTree = this.addReturn(currentTree, getOneFromSet(needed));
                    }
                } else {
                    this.correspondingTree = this.addReturn(currentTree, getOneFromSet(needed));
                }
            } else {
                this.correspondingTree = this.addReturn(currentTree, getOneFromSet(needed));
            }
        }
    }

    private BlockTree addReturn(StatementTree statement, Name varName) {
        List<StatementTree> ls = new ArrayList<StatementTree>();
        if (statement.getKind() == Tree.Kind.BLOCK) {
            ls.addAll(((BlockTree) statement).getStatements());
        } else {
            ls.add(statement);
        }
        if (varName != null) {
            ls.add(this.treeMaker.Return(treeMaker.Identifier(varName.toString())));
        } else {
            ls.add(this.treeMaker.Return(treeMaker.Identifier("_")));
        }
        return treeMaker.Block(ls, false);
    }

    String getSuitableMethod() {
        if (this.opType == OperationType.FOREACH) {
            return "forEach";
        } else if (this.opType == OperationType.MAP) {
            return "map";
        } else if (this.opType == OperationType.FILTER) {
            return "filter";
        } else if (this.opType == OperationType.ANYMATCH) {
            return "anyMatch";
        } else if (this.opType == OperationType.NONEMATCH) {
            return "noneMatch";
        } else //if (this.opType == OperationType.REDUCE) 
        {
            return "reduce";
        }
    }

    List<ExpressionTree> getArguments() {
        VariableTree var;
        LambdaExpressionTree lambda;
        Tree lambdaBody;
        if (this.correspondingTree.getKind() == Tree.Kind.BLOCK) {
            lambdaBody = this.correspondingTree;
        } else {
            if (this.opType == OperationType.FILTER || this.opType == OperationType.ANYMATCH || this.opType == OperationType.NONEMATCH) {
                lambdaBody = ((IfTree) this.correspondingTree).getCondition();
            } else if (this.opType == OperationType.MAP) {
                lambdaBody = ((ExpressionStatementTree) this.correspondingTree).getExpression();
            } else if (this.opType == OperationType.FOREACH) {
                lambdaBody = blockify(this.correspondingTree);
            } else //if(this.opType== OperationType.REDUCE)
            {
                Tree.Kind opKind = ((ExpressionStatementTree) this.correspondingTree).getExpression().getKind();
                if (opKind == Tree.Kind.POSTFIX_INCREMENT || opKind == Tree.Kind.PREFIX_INCREMENT || opKind == Tree.Kind.POSTFIX_DECREMENT || opKind == Tree.Kind.PREFIX_DECREMENT) {
                    UnaryTree statement = (UnaryTree) ((ExpressionStatementTree) this.correspondingTree).getExpression();
                    //first arg of reduce
                    List<ExpressionTree> args = new ArrayList<ExpressionTree>();
                    args.add(statement.getExpression());
                    //second arg of reduce, i.e., lambda expression
                    //If types should be put in replace null with expression
                    Tree type = null;//treeMaker.Type("Integer");
                    var = this.treeMaker.Variable(treeMaker.Modifiers(new HashSet<Modifier>()), "accumulator", null, null);
                    VariableTree var1 = this.treeMaker.Variable(treeMaker.Modifiers(new HashSet<Modifier>()), "_", null, null);
                    lambdaBody = null;
                    if (opKind == Tree.Kind.POSTFIX_INCREMENT || opKind == Tree.Kind.PREFIX_INCREMENT) {
                        lambdaBody = treeMaker.Binary(Tree.Kind.PLUS, treeMaker.Identifier("accumulator"), treeMaker.Literal(1));
                    } else if (opKind == Tree.Kind.POSTFIX_DECREMENT || opKind == Tree.Kind.PREFIX_DECREMENT) {
                        lambdaBody = treeMaker.Binary(Tree.Kind.MINUS, treeMaker.Identifier("accumulator"), treeMaker.Literal(1));
                    }
                    lambda = treeMaker.LambdaExpression(Arrays.asList(var, var1), lambdaBody);
                    args.add(lambda);
                    args.add(treeMaker.Literal(null));
                    return args;

                } else {
                    //this shouldn't happen, but in the future, we will also reduce +=, -= and so on
                    return null;
                }
            }

        }
        if (this.neededVariables.isEmpty()) {
            var = this.treeMaker.Variable(treeMaker.Modifiers(new HashSet<Modifier>()), "_", null, null);
        } else {
            Name varName = getOneFromSet(this.neededVariables);
            //If types need to be made explicit the null should be replaced with the commented expression
            Tree type = null;// treeMaker.Type(this.varToType.get(varName).toString());
            var = this.treeMaker.Variable(treeMaker.Modifiers(new HashSet<Modifier>()), varName.toString(), type, null);
        }
        lambda = treeMaker.LambdaExpression(Arrays.asList(var), lambdaBody);
        List<ExpressionTree> args = new ArrayList<ExpressionTree>();
        args.add(lambda);
        return args;
    }

    private Name getOneFromSet(Set<Name> needed) {
        return needed.iterator().next();
    }

    public void merge(ProspectiveOperation op) {
        //If it's a filter, just change the type, the AST is alredy there(the original is stored).
        if (this.opType == OperationType.FILTER) {
            this.opType = op.opType;
        } else {
            this.opType = op.opType;
            List<StatementTree> statements = new ArrayList<StatementTree>();
            statements.add(this.correspondingTree);
            if (op.correspondingTree.getKind() == Tree.Kind.BLOCK) {
                statements.addAll(((BlockTree) op.correspondingTree).getStatements());
            } else {
                statements.add(op.correspondingTree);
            }
            HashSet<Name> futureAvailable = new HashSet<Name>();
            HashSet<Name> futureNeeded = new HashSet<Name>();

            futureAvailable.addAll(this.getAvailableVariables());
            futureAvailable.addAll(op.getAvailableVariables());


            futureNeeded.addAll(op.getNeededVariables());
            futureNeeded.removeAll(this.getAvailableVariables());
            futureNeeded.addAll(this.getNeededVariables());


            this.neededVariables = futureNeeded;
            this.availableVariables = futureAvailable;
            this.correspondingTree = this.treeMaker.Block(statements, false);
        }
    }

    private static boolean areComposable(ProspectiveOperation current, ProspectiveOperation prev) {
        Set<Name> needed = current.getNeededVariables();
        return needed.size() <= 1 && prev.areAvailableVariables(needed);
    }

    private Set<Name> getAvailableVariables() {
        if (this.availableVariables == null) {
            PreconditionsChecker.VariablesVisitor treeVariableVisitor = new PreconditionsChecker.VariablesVisitor(new TreePath(this.workingCopy.getCompilationUnit()));
            treeVariableVisitor.scan(correspondingTree, this.workingCopy.getTrees());
            this.availableVariables = buildAvailables(treeVariableVisitor);

        }
        //If the operation is a filter, then it only makes available what it gets
        //if needed is empty, it can pull anything needed from upstream.
        if (this.opType == OperationType.FILTER) {
            return this.getNeededVariables();
        }
        return this.availableVariables;
    }
    Set<Name> neededVariables;

    public String getTypeForVar(Name varName) {
        return this.varToType.get(varName);
    }

    public Set<Name> getNeededVariables() {
        if (neededVariables == null) {
            PreconditionsChecker.VariablesVisitor treeVariableVisitor = new PreconditionsChecker.VariablesVisitor(new TreePath(this.workingCopy.getCompilationUnit()));
            treeVariableVisitor.scan(correspondingTree, this.workingCopy.getTrees());
            this.neededVariables = buildNeeded(treeVariableVisitor);
        }
        return this.neededVariables;
    }
    Set<Name> availableVariables;

    public Boolean areAvailableVariables(Set<Name> needed) {
        Set<Name> available = this.getAvailableVariables();
        //If the prospective operations does not need any variables from upstream
        //(available is a superset of needeld so the test is sound - the available set includes all the uses)
        //(because, for example, it uses fields or other variables that remain in scope even after refactoring)        
        if (available.isEmpty()) {
            //then the needed variables propagate from the downstream operation in order to facillitate chaining.
            //(both to the needed and available sets).
            available.addAll(needed);
            this.getNeededVariables().addAll(needed);
            return true;
        }
        return available.containsAll(needed);
    }
}