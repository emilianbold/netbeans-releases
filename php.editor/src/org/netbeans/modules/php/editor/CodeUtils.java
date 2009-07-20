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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.editor.index.IndexedClassMember;
import org.netbeans.modules.php.editor.index.IndexedConstant;
import org.netbeans.modules.php.editor.index.IndexedFunction;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.model.nodes.NamespaceDeclarationInfo;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.CatchClause;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ClassName;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionName;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.InfixExpression;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Reference;
import org.netbeans.modules.php.editor.parser.astnodes.ReflectionVariable;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.StaticDispatch;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 *
 * @author tomslot
 */
public class CodeUtils {
    public static final String FUNCTION_TYPE_PREFIX = "@fn:";
    public static final String METHOD_TYPE_PREFIX = "@mtd:";
    public static final String STATIC_METHOD_TYPE_PREFIX = "@static.mtd:";
    private static final Logger LOGGER = Logger.getLogger(CodeUtils.class.getName());

    private CodeUtils() {
    }

    //TODO: extracting name needs to be take into account namespaces
    @CheckForNull
    public static Identifier extractUnqualifiedIdentifier(Expression typeName) {
        Parameters.notNull("typeName", typeName);
        if (typeName instanceof Identifier) {
            return (Identifier) typeName;
        } else if (typeName instanceof NamespaceName) {
            return extractUnqualifiedIdentifier((NamespaceName)typeName);
        } else if (typeName instanceof Variable){
            Variable v = (Variable)typeName;
            return extractUnqualifiedIdentifier(v.getName()); // #167863
        } else if (typeName instanceof FieldAccess) {
            return extractUnqualifiedIdentifier(((FieldAccess)typeName).getField()); // #167863
        }
        //TODO: php5.3 !!!
        //assert false : typeName.getClass(); //NOI18N
        return null;
    }

    public static String extractUnqualifiedName(Expression typeName) {
        Parameters.notNull("typeName", typeName);
        if (typeName instanceof Identifier) {
            return ((Identifier) typeName).getName();
        } else if (typeName instanceof NamespaceName) {
            return extractUnqualifiedName((NamespaceName)typeName);
        }

        //TODO: php5.3 !!!
        //assert false : "[php5.3] className Expression instead of Identifier"; //NOI18N
        return null;
    }

    public static String extractQualifiedName(Expression typeName) {
        Parameters.notNull("clsName", typeName);
        if (typeName instanceof Identifier) {
            return ((Identifier) typeName).getName();
        } else if (typeName instanceof NamespaceName) {
            return extractQualifiedName((NamespaceName)typeName);
        }
        assert false : typeName.getClass(); //NOI18N
        return null;
    }

    public static String extractUnqualifiedClassName(StaticDispatch dispatch) {
        Parameters.notNull("dispatch", dispatch);
        Expression clsName = dispatch.getClassName();
        return extractUnqualifiedName(clsName);
    }

    public static String extractUnqualifiedTypeName(FormalParameter param) {
        Parameters.notNull("param", param);
        Expression typeName = param.getParameterType();
        return typeName != null ? extractUnqualifiedName(typeName) : null;
    }

    public static String extractUnqualifiedTypeName(CatchClause catchClause) {
        Parameters.notNull("catchClause", catchClause);
        Expression typeName = catchClause.getClassName();
        return typeName != null ? extractUnqualifiedName(typeName) : null;
    }

    public static String extractUnqualifiedSuperClassName(ClassDeclaration clsDeclaration) {
        Parameters.notNull("clsDeclaration", clsDeclaration);
        Expression clsName = clsDeclaration.getSuperClass();
        return clsName != null ? extractUnqualifiedName(clsName) : null;
    }

    public static String extractUnqualifiedName(NamespaceName namespaceName) {
        final List<Identifier> segments = namespaceName.getSegments();
        return segments.get(segments.size() - 1).getName();
    }

    public static String extractQualifiedName(NamespaceName namespaceName) {
        Parameters.notNull("namespaceName", namespaceName);
        StringBuilder sb = new StringBuilder();
        final List<Identifier> segments = namespaceName.getSegments();
        for (Iterator<Identifier> it = segments.iterator(); it.hasNext();) {
            Identifier identifier = it.next();
            if (sb.length() > 0) {
                sb.append(NamespaceDeclarationInfo.NAMESPACE_SEPARATOR);
            }
            sb.append(identifier.getName());
        }
        return sb.toString();
    }
    
    public static Identifier extractUnqualifiedIdentifier(NamespaceName name) {
        if (name instanceof NamespaceName) {
            NamespaceName namespaceName = (NamespaceName) name;
            final List<Identifier> segments = namespaceName.getSegments();
            if (segments.size() >= 1) {
                return segments.get(segments.size()-1);
            }
        }
        //TODO: php5.3 !!!
        //assert false : "[php5.3] className Expression instead of Identifier"; //NOI18N
        return null;
    }
    //TODO: rewrite for php53
    public static String extractClassName(ClassName clsName) {
        Expression name = clsName.getName();
        while (name instanceof Variable || name instanceof FieldAccess) {
            if (name instanceof Variable) {
                Variable var = (Variable) name;
                name = var.getName();
            } else if (name instanceof FieldAccess) {
                FieldAccess fld = (FieldAccess) name;
                name = fld.getField().getName();
            }
        }
        if (name instanceof NamespaceName) {
            return extractUnqualifiedName((NamespaceName)name);
        }
        return (name instanceof Identifier) ? ((Identifier) name).getName() : "";//NOI18N
    }

    public static String extractClassName(ClassDeclaration clsDeclaration) {
        return clsDeclaration.getName().getName();
    }
    @CheckForNull // null for RelectionVariable
    public static String extractVariableName(Variable var) {
        if (var instanceof ReflectionVariable) {
            Expression name = ((ReflectionVariable) var).getName();
            if (name instanceof Scalar) {
                Scalar scalar = (Scalar) name;
                return scalar.getStringValue();
            } else if (name instanceof Variable) {
                var = (Variable)name;
                return extractVariableName(var);
            } else if (name instanceof FieldAccess) {
                var = ((FieldAccess)name).getField();
                return extractVariableName(var);
            } else if (name instanceof InfixExpression) {
                //#157750
                return null;
            }
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Cannot extract variable name of ReflectionVariable: " + name.getClass().toString());
            }
        }
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
        } else if (var.getName() instanceof MethodInvocation){
            // no variable name
            return null; 
        }
        
        LOGGER.fine("Cannot extract variable name of type: " + var.getName().getClass().toString());
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
        if (context.getSnapshot().getSource().getFileObject().equals(variable.getFileObject())){
            return findClassNameEnclosingDeclaration(context.getProgram(), variable);
        }

        
        ClassNameExtractor task = new ClassNameExtractor(variable);
        try {
            ParserManager.parse(Collections.singleton(Source.create(variable.getFileObject())), task);
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return task.className;
    }

    private static class ClassNameExtractor extends UserTask {
        IndexedConstant variable;
        String className;

        public ClassNameExtractor(IndexedConstant variable) {
            this.variable = variable;
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            ParserResult parameter = (ParserResult)resultIterator.getParserResult();
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

                for (IndexedFunction func : index.getFunctions(context, fname, QuerySupport.Kind.EXACT)) {
                    varType = func.getReturnType();
                }
            } else if (rawType.startsWith(STATIC_METHOD_TYPE_PREFIX)){
                String parts[] = rawType.substring(STATIC_METHOD_TYPE_PREFIX.length()).split("\\.");
                String className = parts[0];

                if ("self".equals(className) || "parent".equals(className)){ //NOI18N
                    className = findClassNameEnclosingDeclaration(context, variable);
                }

                String methodName = parts[1];

                for (IndexedClassMember<IndexedFunction> classMember : index.getAllMethods(context, className,
                        methodName, QuerySupport.Kind.EXACT, Integer.MAX_VALUE)) {
                    IndexedFunction func = classMember.getMember();

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
                    for (IndexedClassMember<IndexedFunction> classMember : index.getAllMethods(context, className,
                            methodName, QuerySupport.Kind.EXACT, Integer.MAX_VALUE)) {
                        IndexedFunction func = classMember.getMember();
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

    public static String extractVariableType(Assignment assignment) {
        Expression rightSideExpression = assignment.getRightHandSide();

        if (rightSideExpression instanceof Assignment) {
            // handle nested assignments, e.g. $l = $m = new ObjectName;
            return extractVariableType((Assignment) assignment.getRightHandSide());
        } else if (rightSideExpression instanceof Reference) {
            Reference ref = (Reference) rightSideExpression;
            rightSideExpression = ref.getExpression();
        }

        if (rightSideExpression instanceof ClassInstanceCreation) {
            ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) rightSideExpression;
            Expression className = classInstanceCreation.getClassName().getName();
            return CodeUtils.extractUnqualifiedName(className);
        } else if (rightSideExpression instanceof ArrayCreation) {
            return "array"; //NOI18N
        } else if (rightSideExpression instanceof FunctionInvocation) {
            FunctionInvocation functionInvocation = (FunctionInvocation) rightSideExpression;
            String fname = extractFunctionName(functionInvocation);
            return FUNCTION_TYPE_PREFIX + fname;
        } else if (rightSideExpression instanceof StaticMethodInvocation) {
            StaticMethodInvocation staticMethodInvocation = (StaticMethodInvocation) rightSideExpression;
            String className = CodeUtils.extractUnqualifiedClassName(staticMethodInvocation);
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
        } else if (functionName.getName() instanceof NamespaceName) {
            return extractUnqualifiedName((NamespaceName)functionName.getName());
        }
        if (functionName.getName() instanceof Variable) {
            Variable var = (Variable) functionName.getName();
            return extractVariableName(var);
        }

        return null;
    }

    @CheckForNull
    public static String getParamDefaultValue(FormalParameter param) {
        Expression expr = param.getDefaultValue();
        //TODO: can be improved
        if (expr instanceof Scalar) {
            Scalar scalar = (Scalar) expr;
                return scalar.getStringValue();
        }
        return expr == null ? null : "";//NOI18N
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
