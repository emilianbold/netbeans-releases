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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsProvider;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.modules.java.hints.spi.ErrorRule.Data;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring.ParameterInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import sun.security.krb5.internal.KDCOptions;


public class ChangeMethodParameters implements ErrorRule<Void> {
    private String DEFAULT_NAME = "par";
    private boolean cancel = false;

    @Override
    public Set<String> getCodes() {
        return new HashSet<String>(Arrays.asList("compiler.err.cant.apply.symbol.1",
                                                 "compiler.err.cant.apply.symbols")); // NOI18N
    }

    @Override
    public List<Fix> run(CompilationInfo info, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        try {
            cancel = false;
            return analyze(info, offset);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
            return null;
        }
    }

    @Override
    public String getId() {
        return ChangeMethodParameters.class.getName();
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(CreateElement.class, "LBL_ChangeMethodParameters"); // NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(CreateElement.class, "DSC_ChangeMethodParameters"); // NOI18N
    }

    @Override
    public void cancel() {
        cancel = true;
    }

    private List<Fix> analyze(CompilationInfo info, int offset) throws IOException {
        TreePath errorPath = ErrorHintsProvider.findUnresolvedElement(info, offset);
        if (errorPath == null) return Collections.<Fix>emptyList();
        
        Tree error = errorPath.getParentPath().getLeaf();
        if (error == null) return Collections.<Fix>emptyList();
        
        if (info.getElements().getTypeElement("java.lang.Object") == null) { // NOI18N
            // broken java platform
            return Collections.<Fix>emptyList();
        }
        
        if(error.getKind() == Tree.Kind.METHOD_INVOCATION) {
            MethodInvocationTree invocation = (MethodInvocationTree) error;
            if (invocation.getMethodSelect().getKind().equals(Tree.Kind.IDENTIFIER))  {
                IdentifierTree methodSelect = (IdentifierTree) invocation.getMethodSelect();
                TreePath typePath = findEnclosingType(errorPath.getParentPath());
                List<TreePath> methods = new LinkedList<TreePath>();
                for (Tree tree : ((ClassTree) typePath.getLeaf()).getMembers()) {
                    if(cancel) return Collections.<Fix>emptyList();
                    if (tree.getKind().equals(Tree.Kind.METHOD)) {
                        MethodTree method = (MethodTree) tree;
                        if(method.getName().contentEquals(methodSelect.getName())) {
                            methods.add(new TreePath(typePath, method));
                        }
                    }
                }

                LinkedList<Fix> fixes = new LinkedList<Fix>();
                for (TreePath path : methods) {
                    if(cancel) return Collections.<Fix>emptyList();
                    ExecutableElement method = (ExecutableElement) info.getTrees().getElement(path);
                    List<? extends VariableElement> parameters = method.getParameters();
                    ChangeParametersRefactoring.ParameterInfo[] parameterInfo = new ChangeParametersRefactoring.ParameterInfo[parameters.size()];
                    for (int i = 0; i < parameters.size(); i++) {
                        VariableElement param = parameters.get(i);
                        parameterInfo[i] = new ChangeParametersRefactoring.ParameterInfo(i, param.toString(), param.asType().toString(), null);
                    }
                    ChangeParametersRefactoring.ParameterInfo[] newParameterInfo = new ChangeParametersRefactoring.ParameterInfo[invocation.getArguments().size()];

                    MethodTree methodTree = (MethodTree) path.getLeaf();
                    BlockTree methodBody = methodTree.getBody();
                    Scope scope =  null;
                    if(methodBody != null) {
                        TreePath bodyPath = new TreePath(path, methodBody);
                        scope = info.getTrees().getScope(bodyPath);
                    }
                    int i = 0;
                    for (ExpressionTree argument : invocation.getArguments()) {
                        if(cancel) return Collections.<Fix>emptyList();
                        TreePath argumentPath = new TreePath(path, argument);
                        TypeMirror argumentType = info.getTrees().getTypeMirror(argumentPath);
                        String type = argumentType.toString();
                        String name = Utilities.getName(argumentPath.getLeaf());
                        if (name == null) {
                            name = DEFAULT_NAME;
                        }
                        name = makeNameUnique(info, scope, name, newParameterInfo, i);
                        newParameterInfo[i] = new ChangeParametersRefactoring.ParameterInfo(-1, name, type, argument.toString());
                        i++;
                    }
                    
                    // Find old parameters with the same type and copy the information
                    for (i = 0; i < newParameterInfo.length; i++) {
                        if(cancel) return Collections.<Fix>emptyList();
                        ParameterInfo param = newParameterInfo[i];
                        ParameterInfo next = findNextByType(info, parameterInfo, param.getType());
                        if (next != null) {
                            newParameterInfo[i] = new ParameterInfo(next.getOriginalIndex(), next.getName(), next.getType(), param.getDefaultValue());
                        }
                    }
                    
                    TreePathHandle tph = TreePathHandle.create(path, info);
                    boolean doFullRefactoring = true;
                    if(methodTree.getModifiers().getFlags().contains(Modifier.PRIVATE)) {
                        doFullRefactoring = false;
                    }
		    Set<Modifier> modifiers = method.getModifiers();
                    fixes.add(new ChangeParametersFix(doFullRefactoring, tph, modifiers, genDeclarationString(methodTree, parameterInfo), genDeclarationString(methodTree, newParameterInfo), newParameterInfo));
                }
                return fixes;
            }
        }
        
        return Collections.<Fix>emptyList();
    }
    
    private static String makeNameUnique(CompilationInfo info, Scope s, String name, ParameterInfo[] parameters, int current) {
        int counter = 0;
        boolean cont = true;
        String proposedName = name;
        
        while (cont) {
            proposedName = name + (counter != 0 ? String.valueOf(counter) : "");
            
            cont = false;
            
            if (s != null) {
                for (Element e : info.getElementUtilities().getLocalMembersAndVars(s, new Utilities.VariablesFilter())) {
                    if (proposedName.equals(e.getSimpleName().toString())) {
                        counter++;
                        cont = true;
                        break;
                    }
                }
            }
            for (int i = 0; i < parameters.length; i++) {
                ParameterInfo parameterInfo = parameters[i];
                if (current != i && parameterInfo != null && proposedName.equals(parameterInfo.getName())) {
                    counter++;
                    cont = true;
                    break;
                }
            }
        }
        
        return proposedName;
    }
    
    public String genDeclarationString(MethodTree methodTree, ChangeParametersRefactoring.ParameterInfo[] parameters) {
        StringBuilder buf = new StringBuilder();
        buf.append(methodTree.getName());
        buf.append('('); //NOI18N
        if (parameters.length > 0) {
            int i;
            for (i = 0; i < parameters.length - 1; i++) {
                buf.append(parameters[i].getType());
                buf.append(' '); //NOI18N
                buf.append(parameters[i].getName());
                buf.append(',').append(' '); //NOI18N
            }
            buf.append(parameters[i].getType());
            buf.append(' '); //NOI18N
            buf.append(parameters[i].getName());
        }
        buf.append(')'); //NOI18N
        
        return buf.toString();
    }

    private TreePath findEnclosingType(TreePath parentPath) {
        EnumSet<Kind> types = EnumSet.of(Tree.Kind.CLASS, Tree.Kind.INTERFACE, Tree.Kind.ENUM, Tree.Kind.ANNOTATION_TYPE);
        TreePath klazz = parentPath;
        while(klazz != null) {
            if(types.contains(klazz.getLeaf().getKind()))
                return klazz;
            klazz = klazz.getParentPath();
        }
        return null;
    }

    private ParameterInfo findNextByType(CompilationInfo info, ParameterInfo[] parameterInfo, String type) {
        for (int i = 0; i < parameterInfo.length; i++) {
            ParameterInfo param = parameterInfo[i];
            if(param.getOriginalIndex() > -1 && isSameType(info, type, param.getType())) {
                parameterInfo[i] = new ParameterInfo(-1, param.getName(), param.getType(), null); // NOI18N
                return param;
            }
        }
        return null;
    }

    private boolean isSameType(CompilationInfo info, String from, String to) {
        if(from.equals(to)) {
            return true;
        } else {
            TypeElement fromType = info.getElements().getTypeElement(from);
            TypeElement toType = info.getElements().getTypeElement(to);
            if (fromType != null && toType != null) {
                return info.getTypes().isSubtype(fromType.asType(), toType.asType());
            } else {
                return false;
            }
        }
    }
}
