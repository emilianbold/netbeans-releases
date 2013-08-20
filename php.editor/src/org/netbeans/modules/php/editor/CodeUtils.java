/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.model.nodes.NamespaceDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess;
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
import org.netbeans.modules.php.editor.parser.astnodes.Reference;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.StaticConstantAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticDispatch;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.TypeDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.UnaryOperation;
import org.netbeans.modules.php.editor.parser.astnodes.UnaryOperation.Operator;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.netbeans.modules.php.project.api.PhpLanguageProperties;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Parameters;

/**
 *
 * @author tomslot
 */
public final class CodeUtils {
    public static final String FUNCTION_TYPE_PREFIX = "@fn:";
    public static final String METHOD_TYPE_PREFIX = "@mtd:";
    public static final String STATIC_METHOD_TYPE_PREFIX = "@static.mtd:";
    private static final Logger LOGGER = Logger.getLogger(CodeUtils.class.getName());

    private CodeUtils() {
    }

    @CheckForNull
    public static FileObject getFileObject(Document doc) {
        Object sdp = doc.getProperty(Document.StreamDescriptionProperty);
        if (sdp instanceof FileObject) {
            return (FileObject) sdp;
        }
        if (sdp instanceof DataObject) {
            return ((DataObject) sdp).getPrimaryFile();
        }
        return null;
    }

    public static boolean isPhp52(FileObject file) {
        Parameters.notNull("file", file);
        boolean result = false;
        PhpLanguageProperties forFileObject = PhpLanguageProperties.forFileObject(file);
        if (forFileObject.getPhpVersion() == PhpLanguageProperties.PhpVersion.PHP_5) {
            result = true;
        }
        return result;
    }

    public static boolean isPhp53(FileObject file) {
        Parameters.notNull("file", file);
        boolean result = false;
        PhpLanguageProperties forFileObject = PhpLanguageProperties.forFileObject(file);
        if (forFileObject.getPhpVersion() == PhpLanguageProperties.PhpVersion.PHP_53) {
            result = true;
        }
        return result;
    }

    public static boolean isPhp54(FileObject file) {
        Parameters.notNull("file", file);
        boolean result = false;
        PhpLanguageProperties forFileObject = PhpLanguageProperties.forFileObject(file);
        if (forFileObject.getPhpVersion() == PhpLanguageProperties.PhpVersion.PHP_54) {
            result = true;
        }
        return result;
    }

    public static boolean isPhp55(FileObject file) {
        Parameters.notNull("file", file);
        boolean result = false;
        return result;
    }

    @CheckForNull
    public static Identifier extractUnqualifiedIdentifier(Expression typeName) {
        Parameters.notNull("typeName", typeName);
        if (typeName instanceof Identifier) {
            return (Identifier) typeName;
        } else if (typeName instanceof NamespaceName) {
            return extractUnqualifiedIdentifier((NamespaceName) typeName);
        } else if (typeName instanceof Variable) {
            Variable v = (Variable) typeName;
            return extractUnqualifiedIdentifier(v.getName()); // #167863
        } else if (typeName instanceof FieldAccess) {
            return extractUnqualifiedIdentifier(((FieldAccess) typeName).getField()); // #167863
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
            return extractUnqualifiedName((NamespaceName) typeName);
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
            return extractQualifiedName((NamespaceName) typeName);
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
        String retval;
        StringBuilder sb = new StringBuilder();
        final List<Identifier> segments = namespaceName.getSegments();
        if (namespaceName.isGlobal()) {
            sb.append(NamespaceDeclarationInfo.NAMESPACE_SEPARATOR);
        }
        for (Iterator<Identifier> it = segments.iterator(); it.hasNext();) {
            Identifier identifier = it.next();
            sb.append(identifier.getName());
            sb.append(NamespaceDeclarationInfo.NAMESPACE_SEPARATOR);
        }
        retval = sb.toString();
        return retval.substring(0, retval.length() - NamespaceDeclarationInfo.NAMESPACE_SEPARATOR.length());
    }

    public static Identifier extractUnqualifiedIdentifier(NamespaceName namespaceName) {
        final List<Identifier> segments = namespaceName.getSegments();
        if (segments.size() >= 1) {
            return segments.get(segments.size() - 1);
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
            return extractUnqualifiedName((NamespaceName) name);
        }
        return (name instanceof Identifier) ? ((Identifier) name).getName() : ""; //NOI18N
    }

    public static String extractClassName(ClassDeclaration clsDeclaration) {
        return clsDeclaration.getName().getName();
    }

    public static String extractTypeName(TypeDeclaration typeDeclaration) {
        return typeDeclaration.getName().getName();
    }

    private static final class VariableNameVisitor extends DefaultVisitor {

        private String name = null;
        private boolean isDollared = false;

        private VariableNameVisitor() {
        }

        private static class SingletonHolder {
            public static final VariableNameVisitor INSTANCE = new VariableNameVisitor();
        }

        public static VariableNameVisitor getInstance() {
            return SingletonHolder.INSTANCE;
        }

        public String findName(Variable var) {
            name = null;
            scan(var);
            return name;
        }

        @Override
        public void visit(Scalar node) {
            final String scalarName = node.getStringValue();
            if ((scalarName.startsWith("'") && scalarName.endsWith("'"))
                    || (scalarName.startsWith("\"") && scalarName.endsWith("\""))) { //NOI18N
                name = scalarName.substring(1, scalarName.length() - 1);
            } else {
                name = scalarName;
            }
        }

        @Override
        public void visit(Variable node) {
            isDollared = node.isDollared();
            super.visit(node);
        }

        @Override
        public void visit(InfixExpression node) {
        }

        @Override
        public void visit(Identifier identifier) {
            name = isDollared ? "$" + identifier.getName() : identifier.getName();
        }

        @Override
        public void visit(ArrayAccess node) {
            scan(node.getName());
        }
    }


    @CheckForNull // null for RelectionVariable
    public static String extractVariableName(Variable var) {
        String variableName = VariableNameVisitor.getInstance().findName(var);
        if (variableName == null) {
            LOGGER.log(Level.FINE, "Can not retrieve variable name: {0}", var);
        }
        return variableName;
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
            return Type.ARRAY;
        } else if (rightSideExpression instanceof FunctionInvocation) {
            FunctionInvocation functionInvocation = (FunctionInvocation) rightSideExpression;
            String fname = extractFunctionName(functionInvocation);
            return FUNCTION_TYPE_PREFIX + fname;
        } else if (rightSideExpression instanceof StaticMethodInvocation) {
            StaticMethodInvocation staticMethodInvocation = (StaticMethodInvocation) rightSideExpression;
            String className = CodeUtils.extractUnqualifiedClassName(staticMethodInvocation);
            String methodName = extractFunctionName(staticMethodInvocation.getMethod());

            if (className != null && methodName != null) {
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

            if (varName != null && methodName != null) {
                return METHOD_TYPE_PREFIX + varName + '.' + methodName;
            }
        }

        return null;
    }

    public static String extractFunctionName(FunctionInvocation functionInvocation) {
        return extractFunctionName(functionInvocation.getFunctionName());
    }

    public static String extractFunctionName(FunctionDeclaration functionDeclaration) {
        return functionDeclaration.getFunctionName().getName();
    }

    public static String extractMethodName(MethodDeclaration methodDeclaration) {
        return methodDeclaration.getFunction().getFunctionName().getName();
    }

    public static String extractFunctionName(FunctionName functionName) {
        if (functionName.getName() instanceof Identifier) {
            Identifier id = (Identifier) functionName.getName();
            return id.getName();
        } else if (functionName.getName() instanceof NamespaceName) {
            return extractUnqualifiedName((NamespaceName) functionName.getName());
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
        Operator operator = null;
        if (expr instanceof UnaryOperation) {
            UnaryOperation unaryExpr = (UnaryOperation) expr;
            operator = unaryExpr.getOperator();
            expr = unaryExpr.getExpression();
        }
        if (expr instanceof Scalar) {
            Scalar scalar = (Scalar) expr;
            String returnValue = scalar.getStringValue();
            return Operator.MINUS.equals(operator) ? "-" + returnValue : returnValue; // NOI18N
        } else if (expr instanceof ArrayCreation) {
            return "array()"; //NOI18N
        } else if (expr instanceof StaticConstantAccess) {
            StaticConstantAccess staticConstantAccess = (StaticConstantAccess) expr;
            Expression className = staticConstantAccess.getClassName();

            if (className instanceof Identifier) {
                Identifier i = (Identifier) className;

                return i.getName() + "::" + staticConstantAccess.getConstant().getName(); // NOI18N
            } else if (className instanceof NamespaceName) {
                NamespaceName namespace = (NamespaceName) className;
                List<Identifier> segments = namespace.getSegments();
                StringBuilder sb = new StringBuilder();

                if (namespace.isGlobal()) {
                    sb.append(NamespaceDeclarationInfo.NAMESPACE_SEPARATOR);
                }

                for (Iterator<Identifier> iter = segments.iterator(); iter.hasNext();) {
                    Identifier namespaceSegment = iter.next();
                    sb.append(namespaceSegment.getName());

                    if (iter.hasNext()) {
                        sb.append(NamespaceDeclarationInfo.NAMESPACE_SEPARATOR);
                    }
                }

                return sb.toString() + "::" + staticConstantAccess.getConstant().getName(); // NOI18N
            }
        }
        return expr == null ? null : " "; //NOI18N
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
