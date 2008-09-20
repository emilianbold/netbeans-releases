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

import java.io.IOException;
import java.util.Map;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.gsf.api.SourceModel;
import org.netbeans.modules.gsf.api.SourceModelFactory;
import org.netbeans.modules.gsf.api.annotations.CheckForNull;
import org.netbeans.modules.php.editor.index.IndexedConstant;
import org.netbeans.modules.php.editor.index.IndexedFunction;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ClassName;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionName;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Reference;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.openide.util.Exceptions;

/**
 *
 * @author tomslot
 */
public class CodeUtils {
    public static final String FUNCTION_TYPE_PREFIX = "@fn:";
    public static final String METHOD_TYPE_PREFIX = "@mtd:";
    public static final String STATIC_METHOD_TYPE_PREFIX = "@static.mtd:";

    private CodeUtils() {
    }

    public static String extractClassName(ClassName clsName) {
        Expression name = clsName.getName();

        assert name instanceof Identifier :
            "unsupported type of ClassName.getClassName().getName(): "
            + name.getClass().getName();

        return (name instanceof Identifier) ? ((Identifier) name).getName() : "";//NOI18N
    }

    public static String extractClassName(ClassInstanceCreation instanceCreation) {
        Expression name = instanceCreation.getClassName().getName();
        
        assert name instanceof Identifier : 
            "unsupported type of InstanceCreation.getClassName().getName(): "
            + name.getClass().getName();

        return (name instanceof Identifier) ? ((Identifier) name).getName() : "";//NOI18N
    }
    public static String extractClassName(ClassDeclaration clsDeclaration) {
        return clsDeclaration.getName().getName();
    }
    public static String extractSuperClassName(ClassDeclaration clsDeclaration) {
        Identifier superClass = clsDeclaration.getSuperClass();
        return (superClass != null) ? superClass.getName():null;
    }

    @CheckForNull // null for RelectionVariable
    public static String extractVariableName(Variable var) {
        if (var.getName() instanceof Identifier) {
            Identifier id = (Identifier) var.getName();
            StringBuilder varName = new StringBuilder();

            if (var.isDollared()) {
                varName.append("$");
            }

            varName.append(id.getName());
            return varName.toString();
        } else if (var.getName() instanceof Variable) {
            Variable name = (Variable) var.getName();
            return extractVariableName(name);
        }
        
        return null;
    }
    
    public static boolean isVariableTypeResolved(IndexedConstant var){
        
        if (var.getTypeName() != null && var.getTypeName().startsWith("@")){
            return false;
        }
        
        return true;
    }
    
    private static String findClassNameEnclosingDeclaration(PHPParseResult context,
            IndexedConstant variable) {
        if (context.getFile().getFileObject().equals(variable.getFileObject())){
            return findClassNameEnclosingDeclaration(context.getProgram(), variable);
        }

        SourceModel model = SourceModelFactory.getInstance().getModel(variable.getFileObject());

        ClassNameExtractor task = new ClassNameExtractor(variable);

        try {
            model.runUserActionTask(task, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return task.className;
    }
    
    private static class ClassNameExtractor implements CancellableTask<CompilationInfo> {
        IndexedConstant variable;
        String className;

        public ClassNameExtractor(IndexedConstant variable) {
            this.variable = variable;
        }

        public void cancel() {}

        public void run(CompilationInfo parameter) throws Exception {
            Program program = Utils.getRoot(parameter);
            className = findClassNameEnclosingDeclaration(program, variable);
        }
    }
    
    private static String findClassNameEnclosingDeclaration(Program program,
            IndexedConstant variable) {    
        ASTNode node = Utils.getNodeAtOffset(program,
                variable.getOffset(), ClassDeclaration.class);

        if (node instanceof ClassDeclaration) {
            ClassDeclaration classDeclaration = (ClassDeclaration) node;
            return classDeclaration.getName().getName();
        }

        return null;
    }
    
    public static void resolveFunctionType(PHPParseResult context,
            PHPIndex index,
            Map<String, IndexedConstant> varStack,
            IndexedConstant variable){
        
        String rawType = variable.getTypeName();
        
        if (!isVariableTypeResolved(variable)){ 
            String varType = null;
            boolean unresolvedType = true;
            
            if (rawType.startsWith(FUNCTION_TYPE_PREFIX)) {

                String fname = rawType.substring(FUNCTION_TYPE_PREFIX.length());
                
                for (IndexedFunction func : index.getFunctions(context, fname, NameKind.EXACT_NAME)) {
                    varType = func.getReturnType();
                }
            } else if (rawType.startsWith(STATIC_METHOD_TYPE_PREFIX)){
                String parts[] = rawType.substring(STATIC_METHOD_TYPE_PREFIX.length()).split("\\.");
                String className = parts[0];
                
                if ("self".equals(className) || "parent".equals(className)){ //NOI18N
                    className = findClassNameEnclosingDeclaration(context, variable);
                }
                
                String methodName = parts[1];
                
                for (IndexedFunction func : index.getAllMethods(context, className,
                        methodName, NameKind.EXACT_NAME, Integer.MAX_VALUE)) {
                    
                    varType = func.getReturnType();
                }
            } else if (rawType.startsWith(METHOD_TYPE_PREFIX)) {
                String parts[] = rawType.substring(METHOD_TYPE_PREFIX.length()).split("\\.");
                String varName = parts[0];
                String methodName = parts[1];
                String className = null;

                if ("$this".equals(varName)) { //NOI18N
                    className = findClassNameEnclosingDeclaration(context, variable);
                } else {
                    IndexedConstant dispatcher = varStack.get(varName);

                    if (dispatcher != null
                            // preventing infinite loop
                            && dispatcher != variable) {
                        resolveFunctionType(context, index, varStack, dispatcher);
                        className = dispatcher.getTypeName();
                    }
                }

                if (className != null) {
                    for (IndexedFunction func : index.getAllMethods(context, className,
                            methodName, NameKind.EXACT_NAME, Integer.MAX_VALUE)) {

                        varType = func.getReturnType();
                    }
                }
            } else {
                unresolvedType = false;
            }
            
            if (unresolvedType){
                variable.setTypeName(varType);
            }
        }
    }

    public static String extractVariableTypeFromAssignment(Assignment assignment) {
        Expression rightSideExpression = assignment.getRightHandSide();

        if (rightSideExpression instanceof Assignment) {
            // handle nested assignments, e.g. $l = $m = new ObjectName;
            return extractVariableTypeFromAssignment((Assignment) assignment.getRightHandSide());
        } else if (rightSideExpression instanceof Reference) {
            Reference ref = (Reference) rightSideExpression;
            rightSideExpression = ref.getExpression();
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
        } else if (rightSideExpression instanceof StaticMethodInvocation) {
            StaticMethodInvocation staticMethodInvocation = (StaticMethodInvocation) rightSideExpression;
            String className = staticMethodInvocation.getClassName().getName();
            String methodName = extractFunctionName(staticMethodInvocation.getMethod());
            
            if (className != null && methodName != null){
                return STATIC_METHOD_TYPE_PREFIX + className + '.' + methodName;
            }
        } else if (rightSideExpression instanceof MethodInvocation) {
            MethodInvocation methodInvocation = (MethodInvocation) rightSideExpression;
            String varName = null;
            
            if (methodInvocation.getDispatcher() instanceof Variable) {
                Variable var = (Variable) methodInvocation.getDispatcher();
                varName = extractVariableName(var);
            }
            
            String methodName = extractFunctionName(methodInvocation.getMethod());
            
            if (varName != null && methodName != null){
                return METHOD_TYPE_PREFIX + varName + '.' + methodName;
            }
        }

        return null;
    }
    
    public static String extractFunctionName(FunctionInvocation functionInvocation){
        return extractFunctionName(functionInvocation.getFunctionName());
    }
    
    public static String extractFunctionName(FunctionDeclaration functionDeclaration){
        return functionDeclaration.getFunctionName().getName();
    }

    public static String extractMethodName(MethodDeclaration methodDeclaration){
        return methodDeclaration.getFunction().getFunctionName().getName();
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
