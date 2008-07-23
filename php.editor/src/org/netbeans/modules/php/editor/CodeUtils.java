/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor;

import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.php.editor.index.IndexedConstant;
import org.netbeans.modules.php.editor.index.IndexedFunction;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionName;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.Reference;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;

/**
 *
 * @author tomslot
 */
public class CodeUtils {
    public static final String FUNCTION_TYPE_PREFIX = "func:";

    private CodeUtils() {
    }

    public static String extractVariableName(Variable var) {
        if (var.getName() instanceof Identifier) {
            Identifier id = (Identifier) var.getName();
            StringBuilder varName = new StringBuilder();
            
            if (var.isDollared()){
                varName.append("$");
            }
            
            varName.append(id.getName());
            return varName.toString();
        } else {
            if (var.getName() instanceof Variable) {
                Variable name = (Variable) var.getName();
                return extractVariableName(name);
            }
        }

        return null;
    }
    
    public static void resolveFunctionType(PHPParseResult context, PHPIndex index,
            IndexedConstant variable){
        if (variable.getTypeName() != null 
                && variable.getTypeName().startsWith(FUNCTION_TYPE_PREFIX)){
            
            String fname = variable.getTypeName().substring(FUNCTION_TYPE_PREFIX.length());
            String varType = null;
            
            for (IndexedFunction func : index.getFunctions(context, fname, NameKind.EXACT_NAME)){
                varType = func.getReturnType();
            }
            
            variable.setTypeName(varType);
        }
    }

    public static String extractVariableTypeFromAssignment(Assignment assignment) {
        Expression rightSideExpression = assignment.getRightHandSide();

        if (rightSideExpression instanceof Assignment) {
            // handle nested assignments, e.g. $l = $m = new ObjectName;
            return extractVariableTypeFromAssignment((Assignment) assignment.getRightHandSide());
        }

        if (rightSideExpression instanceof ClassInstanceCreation) {
            ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) rightSideExpression;
            Expression className = classInstanceCreation.getClassName().getName();

            if (className instanceof Identifier) {
                Identifier identifier = (Identifier) className;
                return identifier.getName();
            }
        } else if (rightSideExpression instanceof ArrayCreation) {
            return "array"; //NOI18N
        } else if (rightSideExpression instanceof FunctionInvocation) {
            FunctionInvocation functionInvocation = (FunctionInvocation) rightSideExpression;
            String fname = extractFunctionName(functionInvocation);
            return FUNCTION_TYPE_PREFIX + fname;
        }

        return null;
    }
    
    public static String extractFunctionName(FunctionInvocation functionInvocation){
        return extractFunctionName(functionInvocation.getFunctionName());
    }
    
    public static String extractFunctionName(FunctionDeclaration functionDeclaration){
        return functionDeclaration.getFunctionName().getName();
    }
    
    public static String extractFunctionName(FunctionName functionName){
        if (functionName.getName() instanceof Identifier) {
            Identifier id = (Identifier) functionName.getName();
            return id.getName();
        }
        
        if (functionName.getName() instanceof Variable) {
            Variable var = (Variable) functionName.getName();
            return extractVariableName(var);
        }
        
        return null;
    }
    
    public static String getParamDisplayName(FormalParameter param) {
        Expression paramNameExpr = param.getParameterName();
        StringBuilder paramName = new StringBuilder();

        if (paramNameExpr instanceof Variable) {
            Variable var = (Variable) paramNameExpr;
            Identifier id = (Identifier) var.getName();

            if (var.isDollared()) {
                paramName.append("$"); //NOI18N
            }
            
            paramName.append(id.getName());
        } else if (paramNameExpr instanceof Reference) {
            paramName.append("&");
            Reference reference = (Reference) paramNameExpr;

            if (reference.getExpression() instanceof Variable) {
                Variable var = (Variable) reference.getExpression();
                
                if (var.isDollared()) {
                    paramName.append("$"); //NOI18N
                }
                
                Identifier id = (Identifier) var.getName();
                paramName.append(id.getName());
            }
        }
        
        return paramName.length() == 0 ? null : paramName.toString();
    }
}
