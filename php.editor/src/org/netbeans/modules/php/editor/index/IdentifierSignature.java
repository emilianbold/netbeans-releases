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
package org.netbeans.modules.php.editor.index;

import java.util.List;
import java.util.Map;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocVarTypeTag;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Statement;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;

/**
 *
 * @author Radek Matous
 */
public class IdentifierSignature {

    private static final int DECLARATION = 0x1;//else invocation or use
    private static final int IFACE_MEMBER = 0x2;
    private static final int CLS_MEMBER = 0x4;
    private static final int MODIFIER_STATIC = 0x8;
    private static final int MODIFIER_ABSTRACT = 0x10;
    private static final int MODIFIER_PROTECTED = 0x20;
    private static final int MODIFIER_PUBLIC = 0x40;
    private static final int KIND_FNC = 0x80;
    private static final int KIND_VAR = 0x100;
    private static final int KIND_CONST = 0x200;
    private static final int KIND_CLASS = 0x400;
    private String name;
    private int mask;
    private String typeName;

    //for invocations
    private IdentifierSignature(String name) {
        this(name, null, 0);
    }

    private IdentifierSignature(String name, String typeName, int mask) {
        this.name = name.toLowerCase();
        while (name.startsWith("$")) {//NOI18N
            name = name.substring(1);
        }
        this.mask = mask;
        if (isDeclaration()) {
            this.typeName = typeName;
        }
    }

    private IdentifierSignature(Identifier identifier, int modifier,
            ElementKind kind, String typeName, boolean declaration,
            Boolean clsmember) {
        this.name = identifier.getName().toLowerCase();
        while (name.startsWith("$")) {//NOI18N
            name = name.substring(1);
        }
        if (declaration) {
            mask |= DECLARATION;
        }

        if (clsmember != null && clsmember) {
            mask |= CLS_MEMBER;
        }
        if (clsmember != null && !clsmember) {
            mask |= IFACE_MEMBER;
        }
        switch (kind) {
            case METHOD:
                mask |= KIND_FNC;
                break;
            case FIELD:
                mask |= KIND_VAR;
                break;
            case CONSTANT:
                mask |= KIND_CONST;
                break;
            case CLASS:
                mask |= KIND_CLASS;
                break;
            default:
                throw new IllegalStateException(kind.toString());
        }

        if (BodyDeclaration.Modifier.isAbstract(modifier)) {
            mask |= MODIFIER_ABSTRACT;
        } else if (BodyDeclaration.Modifier.isStatic(modifier)) {
            mask |= MODIFIER_STATIC;
        }

        if (BodyDeclaration.Modifier.isPublic(modifier)) {
            mask |= MODIFIER_PUBLIC;
        } else if (BodyDeclaration.Modifier.isProtected(modifier)) {
            mask |= MODIFIER_PROTECTED;
        }
        if (isDeclaration()) {
            this.typeName = typeName;
        }
    }

    public String getName() {
        return name;
    }

    public String getTypeName() {
        return typeName;
    }

    public boolean isDeclaration() {
        return (mask & DECLARATION) != 0;
    }

    public boolean isClassMember() {
        return (mask & CLS_MEMBER) != 0;
    }

    public boolean isIfaceMember() {
        return (mask & IFACE_MEMBER) != 0;
    }

    public boolean isStatic() {
        return (mask & MODIFIER_STATIC) != 0;
    }

    public boolean isAbstract() {
        return (mask & MODIFIER_ABSTRACT) != 0;
    }

    public boolean isPublic() {
        return (mask & MODIFIER_PUBLIC) != 0;
    }

    public boolean isProtected() {
        return (mask & MODIFIER_PROTECTED) != 0;
    }

    public boolean isPrivate() {
        return !isPublic() && !isProtected();
    }

    public boolean isFunction() {
        return (mask & KIND_FNC) != 0 && (!isClassMember() && !isIfaceMember());
    }

    public boolean isMethod() {
        return (mask & KIND_FNC) != 0 && (isClassMember() || isIfaceMember());
    }

    public boolean isVariable() {
        return (mask & KIND_VAR) != 0 && (!isClassMember() && !isIfaceMember());
    }

    public boolean isField() {
        return (mask & KIND_VAR) != 0 && (isClassMember() || isIfaceMember());
    }

    public boolean isClass() {
        return (mask & KIND_CLASS) != 0;
    }

    public boolean isConstant() {
        return (mask & KIND_CONST) != 0 && (!isClassMember() && !isIfaceMember());
    }

    public boolean isClassConstant() {
        return (mask & KIND_CONST) != 0 && (isClassMember() || isIfaceMember());
    }

    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(";");//NOI18N
        if (mask != 0) {
            sb.append(mask).append(";");//NOI18N
        } else {
            assert !isDeclaration();
        }
        if (isDeclaration()) {
            sb.append(typeName).append(";");//NOI18N
        }
        return sb.toString();
    }

    public static IdentifierSignature createDeclaration(Signature sign) {
        String name = sign.string(0);
        int mask = sign.integer(1);
        String typeName = ((mask & DECLARATION) != 0) ? sign.string(2) : null;
        return new IdentifierSignature(name, typeName, mask);
    }
    public static IdentifierSignature createInvocation(Signature sign) {
        String name = sign.string(0);
        return new IdentifierSignature(name);
    }

    public static void add(ClassDeclaration declaration, List<PHPDocVarTypeTag> propertyTags, List<IdentifierSignature> results) {
        String className = CodeUtils.extractClassName(declaration);
        add(declaration.getBody().getStatements(), className, true, results);
        add(propertyTags, className, true, results);
    }

    public static void add(InterfaceDeclaration declaration, List<IdentifierSignature> results) {
        add(declaration.getBody().getStatements(), declaration.getName().getName(), false, results);
    }

    public static void add(List<Statement> statements, String typeName, boolean isClass, List<IdentifierSignature> results) {
        for (Statement statement : statements) {
            if (statement instanceof MethodDeclaration) {
                MethodDeclaration methodDeclaration = (MethodDeclaration) statement;
                add(methodDeclaration, typeName, isClass, results);
            } else if (statement instanceof FieldsDeclaration) {
                FieldsDeclaration fieldsDeclaration = (FieldsDeclaration) statement;
                add(fieldsDeclaration, typeName, isClass, results);
            } else if (statement instanceof ClassConstantDeclaration) {
                ClassConstantDeclaration constDeclaration = (ClassConstantDeclaration) statement;
                add(constDeclaration, typeName, isClass, results);
            }
        }
    }

    private static void add(MethodDeclaration declaration, String typename, Boolean clsMember, List<IdentifierSignature> results) {
        IdentifierSignature is = new IdentifierSignature(declaration.getFunction().getFunctionName(),
                declaration.getModifier(), ElementKind.METHOD, typename, true, clsMember);
        results.add(is);
    }

    private static void add(FieldsDeclaration declaration, String typename, Boolean clsMember, List<IdentifierSignature> results) {
        List<SingleFieldDeclaration> fields = declaration.getFields();
        for (SingleFieldDeclaration fldDeclaration : fields) {
            add(fldDeclaration, declaration.getModifier(), typename, clsMember, results);
        }
    }

    private static void add(SingleFieldDeclaration field,
            int modifier, String typename, Boolean clsMember, List<IdentifierSignature> results) {
        Variable var = field.getName();
        Identifier id = get(var);

        IdentifierSignature is = id != null ? new IdentifierSignature(id, modifier,
                ElementKind.FIELD, typename, true, clsMember) : null;
        if (is != null) {
            results.add(is);
        }
    }

    private static void add(List<PHPDocVarTypeTag> tags, String typename, Boolean clsMember, List<IdentifierSignature> results) {
        int mask = IdentifierSignature.DECLARATION;
        mask |= IdentifierSignature.MODIFIER_PUBLIC;
        if (clsMember) {
            mask |= IdentifierSignature.CLS_MEMBER;
        }
        for (PHPDocVarTypeTag tag : tags) {
            String name = tag.getVariable().getValue();
            if (name != null) {
                if (name.charAt(0) == '$') {
                    name = name.substring(1);
                }
                results.add(new IdentifierSignature(name, typename, mask));
            }
        }
    }

    private static void add(ClassConstantDeclaration declaration, String typename, Boolean clsMember, List<IdentifierSignature> results) {
        List<Identifier> ids = declaration.getNames();
        for (Identifier identifier : ids) {
            results.add(new IdentifierSignature(identifier, 0, ElementKind.CONSTANT, typename, true, clsMember));
        }
    }

    public static void add(Identifier node, Map<String,IdentifierSignature> results) {
        String name = node.getName().toLowerCase();
        results.put(name,new IdentifierSignature(name));
    }

    public static void add(String name, Map<String,IdentifierSignature> results) {
        results.put(name,new IdentifierSignature(name));
    }

    private static Identifier get(Variable var) {
        Expression expr = var.getName();
        if (expr instanceof Identifier) {
            return (Identifier) expr;
        } else if (expr instanceof Variable) {
            return get((Variable) expr);
        }
        return null;
    }
}
