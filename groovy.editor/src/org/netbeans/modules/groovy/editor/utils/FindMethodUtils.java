/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.groovy.editor.utils;

import java.util.ArrayList;
import java.util.List;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.api.AstPath;
import org.netbeans.modules.groovy.editor.api.Methods;

/**
 *
 * @author Martin Janicek
 */
public final class FindMethodUtils {

    private FindMethodUtils() {
    }

    public static MethodNode findMethod(AstPath path, MethodCallExpression methodCall) {
        Expression expression = methodCall.getObjectExpression();

        if (expression instanceof VariableExpression) {
            VariableExpression variableExpression = ((VariableExpression) expression);
            Variable variable = variableExpression.getAccessedVariable();

            if (variable != null) {
                if (variable.isDynamicTyped()) {
                    return getDynamicTypeMethodNode();
                }
                return findMethod(variable.getOriginType(), methodCall);

            } else {
                return getThisMethodNode(path, methodCall);
            }
        } else if (expression instanceof ClassExpression) {
            // Situations like: "GroovySupportObject.println()"
            return findMethod(((ClassExpression) expression).getType(), methodCall);
        } else if (expression instanceof ConstructorCallExpression) {
            // Situations like: "new GalacticMaster().destroyWorldMethod()"
            return findMethod(((ConstructorCallExpression) expression).getType(), methodCall);
        }
        return null;
    }

    // Situations like: "this.destroyWorldMethod()" or only "destroyWorldMethod()"
    private static MethodNode getThisMethodNode(AstPath path, MethodCallExpression methodCall) {
        String findingMethod = methodCall.getMethodAsString();
        Expression arguments = methodCall.getArguments();
        ClassNode owningClass = ASTUtils.getOwningClass(path);

        if (owningClass != null) {
            MethodNode findedMethod = owningClass.tryFindPossibleMethod(findingMethod, arguments);
            if (findedMethod != null) {
                return findedMethod;
            }

            List<MethodNode> possibleMethods = new ArrayList<MethodNode>();
            for (MethodNode method : owningClass.getMethods()) {
                if (Methods.isSameMethod(method, methodCall)) {
                    possibleMethods.add(method);
                }
            }
            if (possibleMethods.size() > 1) {
                // In the future we should distinguish between 'size == 1' and 'size > 1'
                // If the size is more than 1, it means we are dealing with more methods
                // with the same name and the same number of parameters. In that case we
                // should either try to interfere parameter types or show some user dialog
                // with selection box and let the user to choose what he want to find
                return possibleMethods.get(0);
            }
        }
        return null;
    }

    // Situations like:
    // 1. def master = new GalacaticMaster()
    // 2. master.destroyWorldMethod()
    private static MethodNode getDynamicTypeMethodNode() {
        // FIXME: we should try to guess real dynamic type even if the declaration
        // is using 'def' and it's not clear what's the exact type (maybe it's possible
        // to find that from exact line declaration)
        return null;
    }

    /**
     * Find and add method usage if the given type contains method corresponding
     * with the given method call. In other words we are looking for the method
     * declaration in the given type and the method we are looking for is based
     * on given method call. Might return null if the type can't be interfered
     * properly and thus we are not able to find correct method - this is typical
     * for dynamic types.
     *
     * @param type where we are looking for the specific method
     * @param methodCall method call for which we want to find method in given
     * type or null if the type is dynamic
     */
    private static MethodNode findMethod(ClassNode type, MethodCallExpression methodCall) {
        String findingMethod = methodCall.getMethodAsString();
        Expression arguments = methodCall.getArguments();
        MethodNode method;
        
        if (type.isResolved()) {
            method = type.tryFindPossibleMethod(findingMethod, arguments);
        } else {
            method = type.redirect().tryFindPossibleMethod(findingMethod, arguments);
        }
        return method;
    }
}
