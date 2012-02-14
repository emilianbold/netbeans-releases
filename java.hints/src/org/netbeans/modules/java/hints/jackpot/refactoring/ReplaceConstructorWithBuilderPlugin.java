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
package org.netbeans.modules.java.hints.jackpot.refactoring;

import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.java.hints.jackpot.impl.tm.Matcher.OccurrenceDescription;
import org.netbeans.modules.java.hints.jackpot.refactoring.JackpotBaseRefactoring2.Transform;
import org.netbeans.modules.java.hints.jackpot.refactoring.ReplaceConstructorWithBuilderRefactoring.Setter;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Becicka
 */
public class ReplaceConstructorWithBuilderPlugin implements RefactoringPlugin {

    private final ReplaceConstructorWithBuilderRefactoring replaceConstructorWithBuilder;

    public ReplaceConstructorWithBuilderPlugin(ReplaceConstructorWithBuilderRefactoring refactoring) {
        this.replaceConstructorWithBuilder = refactoring;
    }

    @Override
    public Problem preCheck() {
        return null;
    }

    @Override
    public Problem checkParameters() {
        return null;
    }

    @Override
    public Problem fastCheckParameters() {
        String builderName = replaceConstructorWithBuilder.getBuilderName();
        if (builderName == null || builderName.length() == 0) {
            return new Problem(true, "No factory method name specified.");
        }
        if (!SourceVersion.isName(builderName)) {
            return new Problem(true, builderName + " is not an identifier.");
        }
        final TreePathHandle constr = replaceConstructorWithBuilder.getRefactoringSource().lookup(TreePathHandle.class);
        ClassPath classPath = ClassPath.getClassPath(constr.getFileObject(), ClassPath.SOURCE);
        FileObject resource = classPath.findResource(replaceConstructorWithBuilder.getBuilderName().replace(".", "/") + ".java");
        if (resource !=null) {
            return new Problem(true, "File " + resource.getName() + " already exists.");
        }
        return null;
    }

    @Override
    public final Problem prepare(RefactoringElementsBag refactoringElements) {
        final TreePathHandle constr = replaceConstructorWithBuilder.getRefactoringSource().lookup(TreePathHandle.class);
        final String[] ruleCode = new String[1];
        final String[] toCode = new String[1];
        final String[] parentSimpleName = new String[1];

        try {
            ModificationResult mod = JavaSource.forFileObject(constr.getFileObject()).runModificationTask(new Task<WorkingCopy>() {

                @Override
                public void run(WorkingCopy workingCopy) throws Exception {
                    workingCopy.toPhase(Phase.RESOLVED);
                    TreePath constrPath = constr.resolve(workingCopy);
                    MethodTree constructor = (MethodTree) constrPath.getLeaf();
                    TypeElement parent = (TypeElement) workingCopy.getTrees().getElement(constrPath.getParentPath());
                    parentSimpleName[0] = parent.getSimpleName().toString();
                    TreeMaker make = workingCopy.getTreeMaker();
                    StringBuilder parameters = new StringBuilder();
                    StringBuilder constraints = new StringBuilder();
                    StringBuilder realParameters = new StringBuilder();
                    int count = 1;
                    for (VariableTree vt : constructor.getParameters()) {
                        if (count > 1) {
                            parameters.append(", "); //NOI18N
                            constraints.append(" && "); //NOI18N
                            realParameters.append(", "); //NOI18N
                        }
                        realParameters.append(vt.getName());
                        parameters.append("$").append(count); //NOI18N
                        constraints.append("$").append(count).append(" instanceof ").append(workingCopy.getTrees().getTypeMirror(new TreePath(new TreePath(constrPath, vt), vt.getType()))); //NOI18N
                        count++;
                    }
                    List members = new ArrayList();
                    final String simpleName = replaceConstructorWithBuilder.getBuilderName().substring(replaceConstructorWithBuilder.getBuilderName().lastIndexOf('.') + 1);

                    StringBuilder args = null;

                    for (Setter set : replaceConstructorWithBuilder.getSetters()) {
                        members.add(make.Variable(
                                make.Modifiers(Collections.singleton(Modifier.PRIVATE)),
                                set.getVarName(),
                                make.QualIdent(set.getType()),
                                set.getDefaultValue() == null ? null : make.Identifier(set.getDefaultValue())));
                        if (args == null) {
                            args = new StringBuilder();
                        } else {
                            args.append(", "); //NOI18N
                        }
                        args.append(set.getVarName());
                    }

                    members.add(make.Constructor(
                            make.Modifiers(EnumSet.of(Modifier.PUBLIC)),
                            Collections.<TypeParameterTree>emptyList(),
                            Collections.<VariableTree>emptyList(),
                            Collections.<ExpressionTree>emptyList(),
                            "{}")); //NOI18N

                    for (Setter set : replaceConstructorWithBuilder.getSetters()) {
                        members.add(make.Method(
                                make.Modifiers(EnumSet.of(Modifier.PUBLIC)),
                                set.getName(),
                                make.Type(simpleName),
                                Collections.<TypeParameterTree>emptyList(),
                                Collections.<VariableTree>singletonList(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), set.getVarName(), make.QualIdent(set.getType()), null)),
                                Collections.<ExpressionTree>emptyList(),
                                "{this." + set.getVarName() + " = " + set.getVarName() + ";\nreturn this;}", //NOI18N
                                null));
                    }


                    for (Setter set : replaceConstructorWithBuilder.getSetters()) {
                    }

                    members.add(make.Method(
                            make.Modifiers(EnumSet.of(Modifier.PUBLIC)),
                            "create" + parent.getSimpleName(), //NOI18N
                            make.Type(parent.asType()),
                            Collections.<TypeParameterTree>emptyList(),
                            Collections.<VariableTree>emptyList(),
                            Collections.<ExpressionTree>emptyList(),
                            "{return new " + parent.getSimpleName() + "(" + args + ");}", //NOI18N
                            null));


                    ClassTree builder = make.Class(make.Modifiers(EnumSet.of(Modifier.PUBLIC)), simpleName,
                            Collections.EMPTY_LIST,
                            null,
                            Collections.EMPTY_LIST,
                            members);
                    FileObject root = ClassPath.getClassPath(constr.getFileObject(), ClassPath.SOURCE).findOwnerRoot(constr.getFileObject());
                    CompilationUnitTree builderUnit = make.CompilationUnit(root, replaceConstructorWithBuilder.getBuilderName().replace('.', '/') + ".java", Collections.EMPTY_LIST, Collections.singletonList(builder));
                    workingCopy.rewrite(null, builderUnit);
                    StringBuilder rule = new StringBuilder();
                    rule.append("new ").append(parent.getQualifiedName()).append("(").append(parameters).append(")"); //NOI18N
                    if (constraints.length() > 0) {
                        rule.append(" :: ").append(constraints); //NOI18N
                    }
                    rule.append(";;"); //NOI18N
                    ruleCode[0] = rule.toString();
                }
            });

            List<ModificationResult> results = new ArrayList<ModificationResult>();

            results.add(mod);

            results.addAll(JackpotBaseRefactoring2.performTransformation(ruleCode[0], new Transform() {

                @Override
                public void transform(WorkingCopy copy, OccurrenceDescription occurrence) {
                    final TreeMaker make = copy.getTreeMaker();
                    ExpressionTree expression = make.NewClass(null, Collections.EMPTY_LIST, make.QualIdent(replaceConstructorWithBuilder.getBuilderName()), Collections.EMPTY_LIST, null);

                    int i = 0;
                    for (Setter set : replaceConstructorWithBuilder.getSetters()) {
                        i++;
                        final Tree value = occurrence.getVariables().get("$" + i).getLeaf(); //NOI18N
                        if (treeEquals(copy, value, set.getDefaultValue())) {
                            continue;
                        }
                        expression = make.MethodInvocation(
                                Collections.<ExpressionTree>emptyList(),
                                make.MemberSelect(expression, set.getName()),
                                Collections.singletonList((ExpressionTree) value));
                    }

                    MethodInvocationTree create = make.MethodInvocation(
                            Collections.<ExpressionTree>emptyList(),
                            make.MemberSelect(expression, "create" + parentSimpleName[0]), //NOI18N
                            Collections.<ExpressionTree>emptyList());


                    copy.rewrite(occurrence.getOccurrenceRoot().getLeaf(), create);
                }
            }));

            JackpotBaseRefactoring2.createAndAddElements(replaceConstructorWithBuilder, refactoringElements, results);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    private boolean treeEquals(WorkingCopy copy, Tree value, String defaultValue) {
        if (defaultValue == null) {
            return false;
        }
        defaultValue = defaultValue.trim();
        if (value instanceof LiteralTree) {
            ExpressionTree parseExpression = copy.getTreeUtilities().parseExpression(defaultValue, new SourcePositions[1]);
            if (parseExpression instanceof LiteralTree) {
                return ((LiteralTree) value).getValue().equals(((LiteralTree) parseExpression).getValue());
            }
        }
        return defaultValue.equals(value.toString());
    }

    @Override
    public void cancelRequest() {
    }
}
