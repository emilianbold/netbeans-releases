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

package org.netbeans.modules.php.editor.nav;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.gsf.api.ColoringAttributes;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OccurrencesFinder;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.nav.SemiAttribute.AttributedElement;
import org.netbeans.modules.php.editor.nav.SemiAttribute.AttributedElement.Kind;
import org.netbeans.modules.php.editor.nav.SemiAttribute.ClassElement;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.CatchClause;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ClassName;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.InstanceOfExpression;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.StaticConstantAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.VariableBase;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;

/**
 *
 * @author Jan Lahoda
 */
public class OccurrencesFinderImpl implements OccurrencesFinder {

    private int offset;
    private Map<OffsetRange, ColoringAttributes> range2Attribs;
    
    public void setCaretPosition(int position) {
        this.offset = position;
        this.range2Attribs = new HashMap<OffsetRange, ColoringAttributes>();
    }

    public Map<OffsetRange, ColoringAttributes> getOccurrences() {
        return range2Attribs;
    }

    public void cancel() {
    }

    public void run(CompilationInfo parameter) throws Exception {
        for (OffsetRange r : compute(parameter, offset)) {
            range2Attribs.put(r, ColoringAttributes.MARK_OCCURRENCES);
        }
    }
    
    static Collection<OffsetRange> compute(final CompilationInfo parameter, final int offset) {
        final List<OffsetRange> result = new LinkedList<OffsetRange>();
        final List<ASTNode> path = NavUtils.underCaret(parameter, offset);
        final SemiAttribute a = SemiAttribute.semiAttribute(parameter);
        final AttributedElement el = NavUtils.findElement(parameter, path, offset, a);
        
        if (el == null) {
            return result;
        }
        Identifier id = null;
        Collections.reverse(path);
        for (ASTNode aSTNode : path) {
            if (aSTNode instanceof Identifier) {
                Identifier identifier = (Identifier) aSTNode;
                if (identifier != null) {
                    String name = identifier.getName();
                    if (name.equals("self")) {//NOI18N
                        return result;
                    } else if (name.equals("parent")) {//NOI18N
                        return result;
                    } else if (name.equals("this")) {//NOI18N
                        return result;
                    } else {
                        id = identifier;
                        break;
                    }
                }
            }
        }

        final Identifier identifier = id;
        final List<ASTNode> usages = new LinkedList<ASTNode>();
        final List<ASTNode> memberDeclaration = new LinkedList<ASTNode>();
        
        new DefaultVisitor() {
            private String clsName = null;
            private String superClsName = null;
            @Override
            public void visit(StaticMethodInvocation node) {
                StaticMethodInvocation methAccess = (StaticMethodInvocation) node;
                Identifier className = methAccess.getClassName();
                if (!className.getName().equals("self") && !className.getName().equals("parent")) {//NOI18N
                    if (el == a.getElement(className)) {
                        usages.add(className);
                    }
                }
                super.visit(node);
            }

            
            @Override
            public void visit(StaticFieldAccess node) {
                StaticFieldAccess fldAccess = (StaticFieldAccess) node;
                Identifier className = fldAccess.getClassName();
                if (!className.getName().equals("self") && !className.getName().equals("parent")) {//NOI18N
                    if (el == a.getElement(className)) {
                        usages.add(className);
                    }
                }
                super.visit(node);
            }

            @Override
            public void visit(StaticConstantAccess node) {
                boolean found = false;
                Identifier classNameId = ((StaticConstantAccess) node).getClassName();
                String className;
                if (classNameId.getName().equals("self")) {//NOI18N
                    className = clsName;
                } else if (classNameId.getName().equals("parent")) {//NOI18N
                    className = superClsName;
                } else {
                    className = node.getClassName().getName();
                }
                if (el instanceof SemiAttribute.ClassMemberElement) {
                    SemiAttribute.ClassMemberElement clsEl = (SemiAttribute.ClassMemberElement) el;
                    String constantName = node.getConstant().getName();
                    Identifier constantNode = node.getConstant();
                    if (className != null && clsEl.getClassName().equals(className) && clsEl.getName().equals(constantName)) {
                        usages.add(constantNode);
                        found = true;
                    }
                }
                if (!found) {
                    super.visit(node);
                }
            }

            @Override
            public void visit(MethodDeclaration node) {
                boolean found = false;
                if (el instanceof SemiAttribute.ClassMemberElement) {
                    SemiAttribute.ClassMemberElement clsEl = (SemiAttribute.ClassMemberElement) el;
                    String methName = CodeUtils.extractMethodName(node);
                    Identifier methNode = node.getFunction().getFunctionName();
                    if (clsName != null && clsEl.getClassName().equals(clsName) && clsEl.getName().equals(methName)) {
                        memberDeclaration.add(methNode);
                        usages.add(methNode);
                        found = true;
                    }
                    ClassElement superClass = clsEl.getClassElement().getSuperClass();
                    while(!found && superClass != null) {
                        if (superClass != null && clsName != null && superClass.getName().equals(clsName) && clsEl.getName().equals(methName)) {
                            memberDeclaration.add(0, methNode);
                            usages.add(methNode);
                            found = true;
                        }
                        superClass = superClass.getSuperClass();
                    }
                }
                if (!found) {
                    super.visit(node);
                }
            }


            @Override
            public void visit(SingleFieldDeclaration node) {
                boolean found = false;
                if (el instanceof SemiAttribute.ClassMemberElement) {
                    SemiAttribute.ClassMemberElement clsEl = (SemiAttribute.ClassMemberElement) el;
                    Variable variable = node.getName();
                    String varName = CodeUtils.extractVariableName(variable);
                    if (varName.startsWith("$")) {//NOI18N
                        varName = varName.substring(1);
                    }
                    if (clsName != null && clsEl.getClassName().equals(clsName) && clsEl.getName().equals(varName)) {
                        memberDeclaration.add(variable);
                        usages.add(variable);
                        found = true;
                    }
                    ClassElement superClass = clsEl.getClassElement().getSuperClass();
                    while(!found && superClass != null) {
                        if (superClass != null && clsName != null && superClass.getName().equals(clsName) && clsEl.getName().equals(varName)) {
                            memberDeclaration.add(0, variable);
                            usages.add(variable);
                            found = true;
                        }
                        superClass = superClass.getSuperClass();
                    }
                }
                if (!found) {
                    super.visit(node);
                }
            }


            @Override
            public void visit(FunctionDeclaration node) {
                if (!(el instanceof SemiAttribute.ClassMemberElement)) {
                    if (el == a.getElement(node)) {
                        usages.add(node.getFunctionName());
                    }
                }
                super.visit(node);
            }

            @Override
            public void visit(InterfaceDeclaration node) {
                if (el == a.getElement(node)) {
                    usages.add(node.getName());
                } else {
                    List<Identifier> interfaes = node.getInterfaes();
                    for (Identifier identifier : interfaes) {
                        if (el == a.getElement(identifier)) {
                            usages.add(identifier);
                            break;
                        }
                    }
                }
                super.visit(node);
            }



            @Override
            public void visit(ClassDeclaration node) {
                Identifier superClass = node.getSuperClass();
                if (el == a.getElement(node)) {
                    usages.add(node.getName());
                } else if (el == a.getElement(superClass)) {
                    usages.add(superClass);
                } else {
                    List<Identifier> interfaes = node.getInterfaes();
                    for (Identifier identifier : interfaes) {
                        if (el == a.getElement(identifier)) {
                            usages.add(identifier);
                            break;
                        }
                    }
                }
                clsName = CodeUtils.extractClassName(node);
                superClsName = CodeUtils.extractSuperClassName(node);
                super.visit(node);
                /*
                 * do not mark two method decl., if happens then remove
                 * the superclass method.
                 */
                while (memberDeclaration.size() > 1) {
                    usages.remove(memberDeclaration.remove(0));
                }
            }

            @Override
            public void visit(FormalParameter node) {
                Identifier parameterType = node.getParameterType();
                if (parameterType != null) {
                    String name = parameterType.getName();
                    if (name != null && el == a.getElement(parameterType)) {
                        usages.add(parameterType);
                    }
                }
                super.visit(node);
            }

            @Override
            public void visit(InstanceOfExpression node) {
                ClassName className = node.getClassName();
                Expression expr = className != null ? className.getName() : null;
                String name = (expr instanceof Identifier) ? ((Identifier) expr).getName() : null;
                if (name != null && el == a.getElement(expr)) {
                    usages.add(expr);
                }
                super.visit(node);
            }


            @Override
            public void visit(CatchClause node) {
                Identifier className = node.getClassName();
                if (className != null) {
                    String name = className.getName();
                    if (name != null && el == a.getElement(className)) {
                        usages.add(className);
                    }
                }
                super.visit(node);
            }

            @Override
            public void visit(FunctionInvocation node) {
                if (el == a.getElement(node)) {
                    usages.add(node.getFunctionName());
                }
                super.visit(node);
            }

            @Override
            public void visit(Variable node) {
                if (!(node instanceof ArrayAccess)) {
                    if (el == a.getElement(node)) {
                        usages.add(node);
                    }
                    super.visit(node);
                }
            }
            @Override
            public void visit(ArrayAccess node) {                
                if (el == a.getElement(node)) {
                    VariableBase varName = node.getName();
                    String name = (varName instanceof Variable) ? 
                        CodeUtils.extractVariableName((Variable)varName): null;
                    if (el.getName().equals(name)) {
                        usages.add(varName);
                        return;
                    } 
                } else {
                    Expression index = node.getIndex();
                    if (index != null && !(index instanceof Scalar) && el == a.getElement(index)) {
                        usages.add(index);
                        return;
                    }
                }
                super.visit(node);
            }

            @Override
            public void visit(Scalar scalar) {
                if (el == a.getElement(scalar)) {
                    usages.add(scalar);
                }
                super.visit(scalar);
            }

            @Override
            public void visit(ClassInstanceCreation node) {
                if (el == a.getElement(node)) {
                    usages.add(node.getClassName());
                }
                super.visit(node);
            }
        }.scan(Utils.getRoot(parameter));

        for (ASTNode n : usages) {
            OffsetRange forNode = forNode(n, el.getKind());
            if (forNode != null) {
                result.add(forNode);
            }
        }
        
        return result;
    }

    private static OffsetRange forNode(ASTNode n, Kind kind) {
        OffsetRange retval = null;
        if (n instanceof Scalar && ((Scalar) n).getScalarType() == Scalar.Type.STRING && NavUtils.isQuoted(((Scalar) n).getStringValue())) {
            retval = new OffsetRange(n.getStartOffset() + 1, n.getEndOffset() - 1);
        } else if (n instanceof Variable && ((Variable) n).isDollared()) {
            retval = new OffsetRange(n.getStartOffset() + 1, n.getEndOffset());
        } else if (n instanceof ArrayAccess && kind == Kind.VARIABLE) {
            ArrayAccess arrayAccess = (ArrayAccess) n;
            Expression index = arrayAccess.getIndex();
            retval = forNode(index, kind);
        } else if (n != null) {
            retval = new OffsetRange(n.getStartOffset(), n.getEndOffset());
        }
        return retval;
    }

}
