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
package org.netbeans.modules.php.editor.model.nodes;

import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.model.PhpKind;
import org.netbeans.modules.php.editor.model.QualifiedName;
import org.netbeans.modules.php.editor.nav.NavUtils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ClassName;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.Reference;
import org.netbeans.modules.php.editor.parser.astnodes.ReturnStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.StaticConstantAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticDispatch;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.UseStatementPart;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;

/**
 *
 * @author Radek Matous
 */
public class ASTNodeInfo<T extends ASTNode> {

    private T node;
    private Kind kind;

    public enum Kind {

        NAMESPACE_DECLARATION, USE_STATEMENT, IFACE, CLASS, CLASS_INSTANCE_CREATION,
        METHOD, STATIC_METHOD,
        FIELD, STATIC_FIELD,
        CLASS_CONSTANT, STATIC_CLASS_CONSTANT,
        VARIABLE, CONSTANT, FUNCTION,PARAMETER,
        INCLUDE, RETURN_MARKER}

    ASTNodeInfo(T node) {
        this.node = node;
    }

    ASTNodeInfo(Kind kind, T node) {
        this.kind = kind;
        this.node = node;
    }

    public String getName() {
        return toName(getOriginalNode());
    }

    public QualifiedName getQualifiedName() {
        return toQualifiedName(node);
    }

    private static QualifiedName toQualifiedName(ASTNode node) {
        if (node instanceof FunctionInvocation) {
            FunctionInvocation fi = (FunctionInvocation) node;
            return QualifiedName.create(fi.getFunctionName().getName());
        } else if (node instanceof ClassName) {
            ClassName cname = (ClassName) node;
            return QualifiedName.create(cname.getName());
        } else if (node instanceof Identifier) {
            Identifier cname = (Identifier) node;
            return QualifiedName.createUnqualifiedName(cname);
        } else if (node instanceof NamespaceName) {
            return QualifiedName.create((NamespaceName)node);
        } else if (node instanceof ClassInstanceCreation) {
            ClassInstanceCreation instanceCreation = (ClassInstanceCreation) node;
            return QualifiedName.create(instanceCreation.getClassName().getName());
        } else if (node instanceof UseStatementPart) {
            UseStatementPart statementPart = (UseStatementPart) node;
            return QualifiedName.create(statementPart.getName());
        }
        String toName = toName(node);
        return QualifiedName.createUnqualifiedName(toName);
    }

    public Kind getKind() {
        return (kind == null) ? toKind(getOriginalNode()) : kind;
    }

    public PhpKind getPhpKind() {
        Kind k = getKind();
        switch (k) {
            case INCLUDE:
                return PhpKind.INCLUDE;
            case IFACE:
                return PhpKind.IFACE;
            case CLASS:
                return PhpKind.CLASS;
            case CLASS_INSTANCE_CREATION:
                return PhpKind.CLASS;
            case METHOD:
                return PhpKind.METHOD;
            case STATIC_METHOD:
                return PhpKind.METHOD;
            case FIELD:
                return PhpKind.FIELD;
            case STATIC_FIELD:
                return PhpKind.FIELD;
            case CLASS_CONSTANT:
                return PhpKind.CLASS_CONSTANT;
            case STATIC_CLASS_CONSTANT:
                return PhpKind.CLASS_CONSTANT;
            case VARIABLE:
                return PhpKind.VARIABLE;
            case CONSTANT:
                return PhpKind.CONSTANT;
            case FUNCTION:
                return PhpKind.FUNCTION;
            case USE_STATEMENT:
                return PhpKind.USE_STATEMENT;
        }
        throw new IllegalStateException();
    }
    
    public OffsetRange getRange() {
        return toOffsetRange(getOriginalNode());
    }

    public final T getOriginalNode() {
        return node;
    }
    public static ASTNodeInfo<FieldAccess> create(FieldAccess fieldAccess) {
        return new ASTNodeInfo<FieldAccess>(fieldAccess);
    }

    public static ASTNodeInfo<UseStatementPart> create(UseStatementPart statementPart) {
        return new ASTNodeInfo<UseStatementPart>(statementPart);
    }

    public static ASTNodeInfo<FunctionInvocation> create(FunctionInvocation functionInvocation) {
        return new ASTNodeInfo<FunctionInvocation>(functionInvocation);
    }

    public static ASTNodeInfo<Variable> create(Variable variable) {
        return new ASTNodeInfo<Variable>(variable);
    }

    public static ASTNodeInfo<StaticDispatch> create(StaticDispatch staticDispatch) {
        return new ASTNodeInfo<StaticDispatch>(staticDispatch);
    }

    public static ASTNodeInfo<StaticMethodInvocation> create(StaticMethodInvocation staticMethodInvocation) {
        return new ASTNodeInfo<StaticMethodInvocation>(staticMethodInvocation);
    }

    public static ASTNodeInfo<StaticFieldAccess> create(StaticFieldAccess staticFieldAccess) {
        return new ASTNodeInfo<StaticFieldAccess>(staticFieldAccess);
    }

    public static ASTNodeInfo<StaticConstantAccess> create(StaticConstantAccess staticConstantAccess) {
        return new ASTNodeInfo<StaticConstantAccess>(staticConstantAccess);
    }

    public static ASTNodeInfo<ClassInstanceCreation> create(ClassInstanceCreation instanceCreation) {
        return new ASTNodeInfo<ClassInstanceCreation>(instanceCreation);
    }

    public static ASTNodeInfo<ClassName> create(ClassName className) {
        return new ASTNodeInfo<ClassName>(className);
    }

    public static ASTNodeInfo<Expression> create(Kind kind, NamespaceName namespaceName) {
        return new ASTNodeInfo<Expression>(kind, namespaceName);
    }

    public static ASTNodeInfo<Expression> create(Kind kind, Identifier identifier) {
        return new ASTNodeInfo<Expression>(kind, identifier);
    }

    public static ASTNodeInfo<Scalar> create(Kind kind, Scalar scalar) {
        return new ASTNodeInfo<Scalar>(kind, scalar);
    }

    public static ASTNodeInfo<MethodInvocation> create(MethodInvocation methodInvocation) {
        return new ASTNodeInfo<MethodInvocation>(methodInvocation);
    }

    public static ASTNodeInfo<ReturnStatement> create(ReturnStatement returnStatement) {
        return new ASTNodeInfo<ReturnStatement>(returnStatement);
    }

    private static Kind toKind(ASTNode node) {
        if (node instanceof FunctionInvocation) {
            return Kind.FUNCTION;
        } else if (node instanceof Variable) {
            return Kind.VARIABLE;
        } else if (node instanceof StaticMethodInvocation) {
            return Kind.STATIC_METHOD;
        } else if (node instanceof StaticFieldAccess) {
            return Kind.STATIC_FIELD;
        } else if (node instanceof MethodInvocation) {
            return Kind.METHOD;
        } else if (node instanceof StaticConstantAccess) {
            return Kind.STATIC_CLASS_CONSTANT;
        } else if (node instanceof ClassName) {
            return Kind.CLASS;
        } else if (node instanceof ClassInstanceCreation) {
            return Kind.CLASS_INSTANCE_CREATION;
        } else if (node instanceof FieldAccess) {
            return Kind.FIELD;
        } else if (node instanceof ReturnStatement) {
            return Kind.RETURN_MARKER;
        } else if (node instanceof UseStatementPart) {
            return Kind.USE_STATEMENT;
        }
        throw new IllegalStateException();
    }

    protected static String toName(ASTNode node) {
        if (node instanceof FunctionInvocation) {
            FunctionInvocation fi = (FunctionInvocation) node;
            return CodeUtils.extractFunctionName(fi);
        } else if (node instanceof Variable) {
            Variable var = (Variable) node;
            return toNameVar(var);
        } else if (node instanceof StaticMethodInvocation) {
            StaticMethodInvocation smi = (StaticMethodInvocation) node;
            return toName(smi.getMethod());
        } else if (node instanceof StaticFieldAccess) {
            StaticFieldAccess sfa = (StaticFieldAccess) node;
            return toNameField(sfa.getField());
        } else if (node instanceof MethodInvocation) {
            MethodInvocation mi = (MethodInvocation) node;
            return toName(mi.getMethod());
        } else if (node instanceof StaticConstantAccess) {
            StaticConstantAccess sca = (StaticConstantAccess) node;
            return sca.getConstant().getName();
        } else if (node instanceof ClassName) {
            ClassName cname = (ClassName) node;
            return CodeUtils.extractClassName(cname);
        } else if (node instanceof Identifier) {
            Identifier cname = (Identifier) node;
            return cname.getName();
        } else if (node instanceof NamespaceName) {
            return toName(CodeUtils.extractUnqualifiedIdentifier((NamespaceName)node));
        } else if (node instanceof Scalar) {
            Scalar scalar = (Scalar) node;
            return NavUtils.isQuoted(scalar.getStringValue()) ? NavUtils.dequote(scalar.getStringValue()) : scalar.getStringValue();
        } else if (node instanceof ClassInstanceCreation) {
            ClassInstanceCreation instanceCreation = (ClassInstanceCreation) node;
            return toName(instanceCreation.getClassName());
        } else if (node instanceof FieldAccess) {
            FieldAccess fieldAccess = (FieldAccess) node;
            return toNameField(fieldAccess.getField());
        } else if (node instanceof ReturnStatement) {
            return "return";//NOI18N
        } else if (node instanceof Reference) {
            return toName(((Reference)node).getExpression());
        } else if (node instanceof UseStatementPart) {
            return toQualifiedName(node).toString();
        }
        throw new IllegalStateException(node.getClass().toString());
    }

    protected static OffsetRange toOffsetRange(ASTNode node) {
        if (node instanceof FunctionInvocation) {
            return toOffsetRange(((FunctionInvocation) node).getFunctionName().getName());
        } else if (node instanceof Variable) {
            Variable var = (Variable) node;
            return toOffsetRangeVar(var);
        } else if (node instanceof StaticMethodInvocation) {
            StaticMethodInvocation smi = (StaticMethodInvocation) node;
            return toOffsetRange(smi.getMethod());
        } else if (node instanceof StaticFieldAccess) {
            StaticFieldAccess sfa = (StaticFieldAccess) node;
            return toOffsetRange(sfa.getField());
        } else if (node instanceof MethodInvocation) {
            MethodInvocation mi = (MethodInvocation) node;
            return toOffsetRange(mi.getMethod());
        } else if (node instanceof StaticConstantAccess) {
            StaticConstantAccess sca = (StaticConstantAccess) node;
            Identifier constant = sca.getConstant();
            return new OffsetRange(constant.getStartOffset(), constant.getEndOffset());
        } else if (node instanceof ClassName) {
            Identifier id = CodeUtils.extractUnqualifiedIdentifier(((ClassName) node).getName());

            if (id == null){ // #168459
                return new OffsetRange(node.getStartOffset(), node.getEndOffset());
            }

            return new OffsetRange(id.getStartOffset(), id.getEndOffset());
        } else if (node instanceof Identifier) {
            Identifier cname = (Identifier) node;
            return new OffsetRange(cname.getStartOffset(), cname.getEndOffset());
        } else if (node instanceof NamespaceName) {
            return toOffsetRange(CodeUtils.extractUnqualifiedIdentifier((NamespaceName)node));
        } else if (node instanceof Scalar) {
            Scalar scalar = (Scalar) node;
            if (NavUtils.isQuoted(scalar.getStringValue())) {
                return new OffsetRange(node.getStartOffset() + 1, node.getEndOffset() - 1);
            } else {
                return new OffsetRange(node.getStartOffset(), node.getEndOffset());
            }
        } else if (node instanceof ClassInstanceCreation) {
            ClassInstanceCreation instanceCreation = (ClassInstanceCreation) node;
            return toOffsetRange(instanceCreation.getClassName());
        } else if (node instanceof FieldAccess) {
            FieldAccess fieldAccess = (FieldAccess) node;
            return toOffsetRange(fieldAccess.getField());
        } else if (node instanceof ReturnStatement) {
            ReturnStatement returnStatement = (ReturnStatement) node;
            return new OffsetRange(returnStatement.getStartOffset(), returnStatement.getEndOffset());
        } else if (node instanceof Reference) {
            return toOffsetRange(((Reference)node).getExpression());
        } else if (node instanceof UseStatementPart) {
            return new OffsetRange(node.getStartOffset(), node.getEndOffset());
        }
        throw new IllegalStateException();
    }

    static String toNameVar(Variable var) {
        return CodeUtils.extractVariableName(var);
    }
    static String toNameField(Variable var) {
        String retval = CodeUtils.extractVariableName(var);
        if (retval != null && !retval.startsWith("$")) {
            retval = "$"+retval;
        }
        return retval;
    }

    static OffsetRange toOffsetRangeVar(Variable node) {
        Expression name = node.getName();
        //TODO: dangerous never ending loop
        while ((name instanceof Variable)) {
            while (name instanceof ArrayAccess) {
                ArrayAccess access = (ArrayAccess) name;
                name = access.getName();
            }
            if (name instanceof Variable) {
                Variable var = (Variable) name;
                name = var.getName();
            }
        }
        return new OffsetRange(name.getStartOffset(), name.getEndOffset());
    }
}
