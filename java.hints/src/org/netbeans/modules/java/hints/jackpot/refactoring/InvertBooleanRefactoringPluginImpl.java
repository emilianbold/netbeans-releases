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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.jackpot.spi.PatternConvertor;
import org.netbeans.modules.refactoring.api.Problem;
import org.openide.util.Exceptions;
import org.openide.util.MapFormat;

/**
 *
 * @author lahvac
 */
public class InvertBooleanRefactoringPluginImpl extends JackpotBasedRefactoring {

    private final InvertBooleanRefactoring invertBooleanRefactoring;

    public InvertBooleanRefactoringPluginImpl(InvertBooleanRefactoring replaceConstructorRefactoring) {
        super(replaceConstructorRefactoring);
        this.invertBooleanRefactoring = replaceConstructorRefactoring;
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
        String name = invertBooleanRefactoring.getNewName();
        
        if (name == null || name.length() == 0) {
            return new Problem(true, "No factory method name specified.");
        }
        if (!SourceVersion.isIdentifier(name)) {
            return new Problem(true, name + " is not an identifier.");
        }
        return null;
    }

    private static final String INVERT_FIXES =
            "=> ${newName-with-enclosing}$ $left != $right; :: matchesWithBind($val, \"$left == $right\")\n" +
            "=> ${newName-with-enclosing}$ $left == $right; :: matchesWithBind($val, \"$left != $right\")\n" +
            "=> ${newName-with-enclosing}$ $op; :: matchesWithBind($val, \"!($op)\")" +
            "=> ${newName-with-enclosing}$ $op; :: matchesWithBind($val, \"(!$op)\")" +
            "=> ${newName-with-enclosing}$ $op; :: !matchesAny($val, \"!($op)\") && matchesWithBind($val, \"!$op\")\n" +
            "=> ${newName-with-enclosing}$ false; :: matchesAny($val, \"true\")\n" +
            "=> ${newName-with-enclosing}$ true; :: matchesAny($val, \"false\")\n" +
            "=> ${newName-with-enclosing}$ !$val; :: otherwise\n";

    private static final String VAR_SCRIPT_TEMPLATE =
            "   $enclosing.${originalName}$ :: $enclosing instanceof ${enclosing}$ && !parentMatches(\"$enclosing.${originalName}$ = $newVal\") && !parentMatches(\"!$enclosing.${originalName}$\")\n" +
            "=> !$enclosing.${newName}$\n" +
            ";;\n" +
            "   !$enclosing.${originalName}$ :: $enclosing instanceof ${enclosing}$\n" +
            "=> $enclosing.${newName}$\n" +
            ";;\n" +
            "   $enclosing.${originalName}$ = $val :: $enclosing instanceof ${enclosing}$ && !matchesAny($val, \"!$enclosing.${originalName}$\")\n" +
            INVERT_FIXES.replace(";", "").replace("${newName-with-enclosing}$", "$enclosing.${newName}$ =") +
            ";;\n";

    private static final String VAR_SCRIPT_TEMPLATE_STATIC =
            "   ${enclosing}$.${originalName}$ :: !parentMatches(\"$enclosing.${originalName}$ = $newVal\") && !parentMatches(\"!$enclosing.${originalName}$\")\n" +
            "=> !${enclosing}$.${newName}$\n" +
            ";;\n" +
            "   !${enclosing}$.${originalName}$\n" +
            "=> ${enclosing}$.${newName}$\n" +
            ";;\n" +
            "   ${enclosing}$.${originalName}$ = $val :: !matchesAny($val, \"!$enclosing.${originalName}$\")\n" +
            INVERT_FIXES.replace(";", "").replace("${newName-with-enclosing}$", "${enclosing}$.${newName}$ =") +
            ";;\n";

    private static final String VAR_INIT =
            "   $mods$ $type ${originalName}$ = $val;" +
            INVERT_FIXES.replace("${newName-with-enclosing}$", "$mods$ $type ${newName}$  =") +
            ";;";

    private static final String MTH_INIT =
            "   return $val;" +
            INVERT_FIXES.replace("${newName-with-enclosing}$", "return ") +
            ";;";

    @Override
    protected void prepareAndConstructRule(final Context result) {
        final TreePathHandle original = invertBooleanRefactoring.getRefactoringSource().lookup(TreePathHandle.class);

        try {
            ModificationResult mod = JavaSource.forFileObject(original.getFileObject()).runModificationTask(new Task<WorkingCopy>() {

                @Override
                public void run(final WorkingCopy parameter) throws Exception {
                    parameter.toPhase(Phase.RESOLVED);

                    final TreePath path = original.resolve(parameter);
                    Map<String, String> arguments = new HashMap<String, String>();
                    String scriptTemplate;
                    Tree leaf = path.getLeaf();
                    TypeElement parent = (TypeElement) parameter.getTrees().getElement(path.getParentPath());

                    arguments.put("newName", invertBooleanRefactoring.getNewName());
                    arguments.put("enclosing", parent.getQualifiedName().toString());

                    if (leaf.getKind() == Kind.VARIABLE) {
                        VariableTree var = (VariableTree) leaf;

                        scriptTemplate = var.getModifiers().getFlags().contains(Modifier.STATIC) ? VAR_SCRIPT_TEMPLATE_STATIC : VAR_SCRIPT_TEMPLATE;
                        arguments.put("originalName", var.getName().toString());

                        if (var.getInitializer() != null) {
                            MapFormat format = new MapFormat(arguments);
                            format.setLeftBrace("${");
                            format.setRightBrace("}$");
                            String initFormat = format.format(VAR_INIT);

                            Hacks.findHintsAndApplyFixes(parameter, PatternConvertor.create(initFormat), path, cancel);
                        }
                    } else if (leaf.getKind() == Kind.METHOD) {
                        MethodTree mt = (MethodTree) leaf;

                        arguments.put("originalName", mt.getName().toString());

                        MapFormat format = new MapFormat(arguments);
                        format.setLeftBrace("${");
                        format.setRightBrace("}$");
                        final String mthFormat = format.format(MTH_INIT);

                        new TreePathScanner<Void, Void>() {
                            @Override public Void visitReturn(ReturnTree node, Void p) {
                                Hacks.findHintsAndApplyFixes(parameter, PatternConvertor.create(mthFormat), getCurrentPath(), cancel);
                                return super.visitReturn(node, p);
                            }
                            @Override public Void visitClass(ClassTree node, Void p) {
                                return null;
                            }
                        }.scan(path, null);

                        parameter.rewrite(leaf, parameter.getTreeMaker().setLabel(leaf, invertBooleanRefactoring.getNewName()));

                        StringBuilder parameters = new StringBuilder();
                        StringBuilder constraints = new StringBuilder();
                        int count = 1;
                        for (VariableTree vt : mt.getParameters()) {
                            if (count > 1) {
                                parameters.append(", ");
                                constraints.append(" && ");
                            }
                            parameters.append("$").append(count);
                            TypeMirror type = parameter.getTrees().getTypeMirror(new TreePath(new TreePath(path, vt), vt.getType()));
                            type = parameter.getTypes().erasure(type);
                            constraints.append("$").append(count).append(" instanceof ").append(type);
                            count++;
                        }

                        String andConstraints = (constraints.length() > 0 ? " && " : "") + constraints;

                        StringBuilder script = new StringBuilder();

                        if (mt.getModifiers().getFlags().contains(Modifier.STATIC)) {
                            script.append("   ${enclosing}$.<$T$>${originalName}$(").append(parameters).append(") :: !parentMatches(\"!$enclosing.${originalName}$($args$)\") ").append(andConstraints);
                            script.append("=> !${enclosing}$.<$T$>${newName}$(").append(parameters).append(")\n");
                            script.append(";;\n");
                            script.append("   !${enclosing}$.<$T$>${originalName}$(").append(parameters).append(") :: ").append(constraints);
                            script.append("=> ${enclosing}$.<$T$>${newName}$(").append(parameters).append(")\n");
                            script.append(";;\n");
                        } else {
                            script.append("   $enclosing.<$T$>${originalName}$(").append(parameters).append(") :: $enclosing instanceof ${enclosing}$ && !parentMatches(\"!$enclosing.${originalName}$($args$)\") ").append(andConstraints);
                            script.append("=> !$enclosing.<$T$>${newName}$(").append(parameters).append(")\n");
                            script.append(";;\n");
                            script.append("   !$enclosing.<$T$>${originalName}$(").append(parameters).append(") :: $enclosing instanceof ${enclosing}$ ").append(andConstraints);
                            script.append("=> $enclosing.<$T$>${newName}$(").append(parameters).append(")\n");
                            script.append(";;\n");
                        }

                        scriptTemplate = script.toString();
                    } else {
                        throw new UnsupportedOperationException();
                    }

                    MapFormat format = new MapFormat(arguments);

                    format.setLeftBrace("${");
                    format.setRightBrace("}$");

                    result.addScript(parent.getQualifiedName().toString(), format.format(scriptTemplate), ScriptOptions.RUN/*, ScriptOptions.STORE*/);
                }
            });

            result.addModificationResult(mod);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
