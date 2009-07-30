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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.index.IndexedClass;
import org.netbeans.modules.php.editor.index.IndexedClassMember;
import org.netbeans.modules.php.editor.index.IndexedConstant;
import org.netbeans.modules.php.editor.index.IndexedElement;
import org.netbeans.modules.php.editor.index.IndexedFunction;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.nav.SemiAttribute.AttributedElement.Kind;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.CatchClause;
import org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ClassName;
import org.netbeans.modules.php.editor.parser.astnodes.Dispatch;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.GlobalStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.InstanceOfExpression;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Reference;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar.Type;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.StaticConstantAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.VariableBase;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.Union2;

/**
 *
 * @author Jan Lahoda, Radek Matous
 */
public class SemiAttribute extends DefaultVisitor {
    private static final List<String> SUPERGLOBALS = Arrays.asList(
            "GLOBALS", "_SERVER", "_GET", "_POST", "_FILES", //NOI18N
            "_COOKIE", "_SESSION", "_REQUEST", "_ENV"); //NOI18N

    public DefinitionScope global;
    private Stack<DefinitionScope> scopes = new Stack<DefinitionScope>();
    private Map<ASTNode, AttributedElement> node2Element = new HashMap<ASTNode, AttributedElement>();
    private int offset;
    private ParserResult info;
    private Stack<ASTNode> nodes = new Stack<ASTNode>();

    public SemiAttribute(ParserResult info) {
        this(info, -1);
    }

    public SemiAttribute(ParserResult info, int o) {
        this.offset = o;
        this.info = info;
        scopes.push(global = new DefinitionScope());
    }

    @Override
    public void scan(ASTNode node) {
        if (node == null) {
            return;
        }

        if ((offset != (-1) && offset <= node.getStartOffset())) {
            throw new Stop();
        }

        nodes.push(node);

        super.scan(node);

        nodes.pop();

        if ((offset != (-1) && offset <= node.getEndOffset())) {
            throw new Stop();
        }
    }

    @Override
    public void visit(Program program) {
        //functions defined on top-level of the current file are visible before declared:
        performEnterPass(global, program.getStatements());
        //enterAllIndexedClasses();
        super.visit(program);
    }

    @Override
    public void visit(Assignment node) {
        final VariableBase vb = node.getLeftHandSide();

        if (vb instanceof Variable) {
            AttributedType at = null;
            Expression rightSideExpression = node.getRightHandSide();
            if (rightSideExpression instanceof Reference) {
                rightSideExpression = ((Reference)rightSideExpression).getExpression();
            }

            if (rightSideExpression instanceof ClassInstanceCreation) {
                ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) rightSideExpression;
                Expression className = classInstanceCreation.getClassName().getName();

                if (className instanceof Identifier) {
                    Identifier identifier = (Identifier) className;
                    ClassElement ce = (ClassElement) lookup(identifier.getName(), Kind.CLASS);

                    if (ce != null) {
                        at = new ClassType(ce);
                    }
                }
            } else if (rightSideExpression instanceof FieldAccess) {
                FieldAccess access = (FieldAccess) rightSideExpression;
                Variable field = access.getField();
                String name =extractVariableName(field);

                if (name != null) {
                    node2Element.put(vb, scopes.peek().enterWrite(name, Kind.VARIABLE, access, at));
                }
            }

            String name = extractVariableName((Variable) vb);

            if (name != null) {
                node2Element.put(vb, scopes.peek().enterWrite(name, Kind.VARIABLE, vb, at));
            }
        }

        super.visit(node);
    }

    @Override
    public void visit(FunctionDeclaration node) {
        String name = node.getFunctionName().getName();
        FunctionElement fc = (FunctionElement) global.enterWrite(name, Kind.FUNC, node);

        DefinitionScope top = scopes.peek();

        if (!node2Element.containsKey(node)) {
            assert !top.classScope;
            node2Element.put(node, fc);
        }

        scopes.push(fc.enclosedElements);

        if (top.classScope) {
            assert top.thisVar != null;
            scopes.peek().enter(top.thisVar.name, top.thisVar.getKind(), top.thisVar);
        }

        super.visit(node);

        scopes.pop();
    }

    @Override
    public void visit(InstanceOfExpression node) {
        ClassName className = node.getClassName();
        if (className != null) {
            Expression expr = className.getName();
            String name = (expr instanceof Identifier) ? ((Identifier)expr).getName() : null;
            if (name != null) {
                Collection<AttributedElement> namedGlobalElements = getNamedGlobalElements(Kind.CLASS, name);
                if (!namedGlobalElements.isEmpty()) {
                    node2Element.put(expr, lookup(name, Kind.CLASS));
                } else {
                    node2Element.put(expr, lookup(name, Kind.IFACE));
                }
            }
        }
        Expression expression = node.getExpression();
        if (expression instanceof Variable) {
            Variable var = (Variable) expression;
            final String name = extractVariableName(var);
            if (var != null && name != null) {
                node2Element.put(var,
                        scopes.peek().enterWrite(name, Kind.VARIABLE, var));
            }            
        }
        super.visit(node);
    }

    @Override
    public void visit(CatchClause node) {
        Identifier className = (node.getClassName() != null) ? CodeUtils.extractUnqualifiedIdentifier(node.getClassName()) : null;
        AttributedElement ae = null;
        if (className != null) {
            String name = className.getName();
            Collection<AttributedElement> namedGlobalElements =
                    getNamedGlobalElements(Kind.CLASS, name);
            if (!namedGlobalElements.isEmpty()) {
                node2Element.put(className, ae = lookup(name, Kind.CLASS));
            } else {
                node2Element.put(className, ae = lookup(name, Kind.IFACE));
            }
        }
        Variable var = node.getVariable();
        final String name = extractVariableName(var);

        if (var != null && name != null) {       
            node2Element.put(var,
                    scopes.peek().enterWrite(name, Kind.VARIABLE, var));
        } 

        super.visit(node);
    }


    @Override
    public void visit(FormalParameter node) {
        Variable var = null;
        if (node.getParameterName() instanceof Reference) {
            Reference ref = (Reference)node.getParameterName();
            Expression parameterName = ref.getExpression();
            if (parameterName instanceof Variable) {
                var = (Variable)parameterName;
            }
        } else if (node.getParameterName() instanceof Variable) {
            var = (Variable) node.getParameterName();
        }
        if (var != null) {
            String name = extractVariableName(var);
            if (name != null) {
                scopes.peek().enterWrite(name, Kind.VARIABLE, var);
            }
        }
        Identifier parameterType = (node.getParameterType() != null) ? 
            CodeUtils.extractUnqualifiedIdentifier(node.getParameterType()) : null;

        if (parameterType != null){
            String name = parameterType.getName();
            if (name != null) {
                Collection<AttributedElement> namedGlobalElements = getNamedGlobalElements(Kind.CLASS, name);
                if (!namedGlobalElements.isEmpty()) {
                    node2Element.put(parameterType, lookup(name, Kind.CLASS));
                } else {
                    node2Element.put(parameterType, lookup(name, Kind.IFACE));
                }
            }
        }
        
        super.visit(node);
    }

    @Override
    public void visit(Variable node) {
        if (!node2Element.containsKey(node)) {
            String name = extractVariableName(node);
            if (name != null) {
               node2Element.put(node, lookup(name, Kind.VARIABLE));
            }
        }

        super.visit(node);
    }

    @Override
    public void visit(FunctionInvocation node) {
        Expression exp = node.getFunctionName().getName();
        String name = null;

        if (exp instanceof Identifier) {
            name = ((Identifier) exp).getName();
        }

        if (exp instanceof Variable) {
            //XXX:
            Expression n = ((Variable) exp).getName();

            if (n instanceof Identifier) {
                name = ((Identifier) n).getName();
            }
        }

        if (name != null) {
            AttributedElement thisEl = null;
            ASTNode n = nodes.pop();
            ASTNode par = nodes.peek();

            nodes.push(n);

            if (par instanceof MethodInvocation) {
                ClassElement ce = resolveTypeSimple((Dispatch) par);

                if (ce != null) {
                    thisEl = ce.lookup(name, Kind.FUNC);
                }
            } else {
                if (par instanceof StaticMethodInvocation) {
                    StaticMethodInvocation smi = (StaticMethodInvocation) par;
                    final String clsName = CodeUtils.extractUnqualifiedClassName(smi);
                    Collection<AttributedElement> nn = getNamedGlobalElements(Kind.CLASS,clsName);
                    if (!nn.isEmpty()) {
                        String contextClassName = clsName;
                        if ("parent".equals(clsName)) {//NOI18N
                            contextClassName = getContextSuperClassName();
                        } else if ("self".equals(clsName)) {//NOI18N
                            contextClassName = getContextClassName();
                        }
                        for (AttributedElement ell : nn) {
                            ClassElement ce = (ClassElement) ell;
                            if (ce != null && (contextClassName == null || contextClassName.equals(ce.getName()))) {
                                thisEl = ce.lookup(name, Kind.FUNC);
                                if (thisEl != null) {
                                    node2Element.put(smi.getClassName(), ce);
                                    node2Element.put(smi, thisEl);
                                    node2Element.put(smi.getMethod(), thisEl);
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    thisEl = lookup(name, Kind.FUNC);
                }
            }

            node2Element.put(node, thisEl);

            if ("define".equals(name) && node.getParameters().size() == 2) {
                Expression d = node.getParameters().get(0);

                if (d instanceof Scalar && ((Scalar) d).getScalarType() == Type.STRING) {
                    String value = ((Scalar) d).getStringValue();

                    if (NavUtils.isQuoted(value)) {
                        node2Element.put(d, global.enterWrite(NavUtils.dequote(value), Kind.CONST, d));
                    }
                }
            }
        }

        if (node2Element.containsKey(node)) {
            //super.visit(node);
            scan(node.getParameters());
        } else {
            super.visit(node);
        }
    }

    @Override
    public void visit(InterfaceDeclaration node) {
        String name = node.getName().getName();
        ClassElement ce = (ClassElement) global.enterWrite(name, Kind.IFACE, node);

        node2Element.put(node, ce);
        List<Expression> interfaes = node.getInterfaes();
        for (Expression identifier : interfaes) {
            ClassElement iface = (ClassElement) lookup(CodeUtils.extractUnqualifiedName(identifier), Kind.IFACE);
            ce.ifaces.add(iface);
            node2Element.put(identifier, iface);
        }


        scopes.push(ce.enclosedElements);

        if (node.getBody() != null) {
            performEnterPass(ce.enclosedElements, node.getBody().getStatements());
        }

        super.visit(node);

        scopes.pop();
    }



    @Override
    public void visit(ClassDeclaration node) {
        String name = node.getName().getName();
        ClassElement ce = (ClassElement) global.enterWrite(name, Kind.CLASS, node);

        node2Element.put(node, ce);
        Identifier superClsName = (node.getSuperClass() != null) ?
            CodeUtils.extractUnqualifiedIdentifier(node.getSuperClass()) : null;
        if (superClsName != null) {
            ce.superClass = (ClassElement) lookup(superClsName.getName(), Kind.CLASS);
        }
        List<Expression> interfaes = node.getInterfaes();
        for (Expression identifier : interfaes) {
            ClassElement iface = (ClassElement) lookup(CodeUtils.extractUnqualifiedName(identifier), Kind.IFACE);
            ce.ifaces.add(iface);
            node2Element.put(identifier, iface);
        }

        scopes.push(ce.enclosedElements);

        if (node.getBody() != null) {
            performEnterPass(ce.enclosedElements, node.getBody().getStatements());
        }

        super.visit(node);

        scopes.pop();
    }

    @Override
    public void visit(ClassInstanceCreation node) {
        Expression name = node.getClassName().getName();

        if (name instanceof Identifier) {
            node2Element.put(node, lookup(((Identifier) name).getName(), Kind.CLASS));
        }

        super.visit(node);
    }

    @Override
    public void visit(GlobalStatement node) {
        for (Variable v : node.getVariables()) {
            String name = extractVariableName(v);

            if (name != null) {
                enterGlobalVariable(name);
            }
        }
        super.visit(node);
    }

    @Override
    public void visit(Scalar scalar) {
        if (scalar.getScalarType() == Type.STRING && !NavUtils.isQuoted(scalar.getStringValue())) {
            AttributedElement def = global.lookup(scalar.getStringValue(), Kind.CONST);

            node2Element.put(scalar, def);
        }

        super.visit(scalar);
    }

    @Override
    public void visit(FieldAccess node) {
        scan(node.getDispatcher());

        ClassElement ce = resolveTypeSimple(node);
        String name = extractVariableName(node.getField());

        if (ce != null && name != null) {
            AttributedElement thisEl = ce.lookup(name, Kind.VARIABLE);
            node2Element.put(node, thisEl);
            Variable field = node.getField();
            node2Element.put(field, thisEl);
            if (field instanceof ArrayAccess) {
                Expression exprName = field.getName();
                if (exprName instanceof Variable) {
                    node2Element.put(exprName, thisEl);
                }
                super.visit(node);
            }
        } else {
            scan(node.getField());
        }
    }

    private ClassElement getCurrentClassElement() {
        ClassElement c = null;
        for (int i = scopes.size()-1; i >= 0; i--) {
            DefinitionScope scope = scopes.get(i);
            if (scope != null && scope.enclosingClass != null) {
                c = scope.enclosingClass;
                break;
            }
        }
        return c;
    }

    @Override
    public void visit(StaticConstantAccess node) {
        String clsName = CodeUtils.extractUnqualifiedClassName(node);
        if (clsName.equals("self")) {//NOI18N
            ClassElement c = getCurrentClassElement();
            if (c != null) {
                clsName = c.getName();
            }
        } else if (clsName.equals("parent")) {//NOI18N
            ClassElement c = getCurrentClassElement();
            if (c != null) {
                c = c.getSuperClass();
                if (c != null) {
                    clsName = c.getName();
                }
            }
        }
        Collection<AttributedElement> nn = getNamedGlobalElements(Kind.CLASS,clsName);//NOI18N

        if (!nn.isEmpty()) {
            for (AttributedElement ell : nn) {
                ClassElement ce = (ClassElement)ell;
                if (ce != null && ce.getName().equals(clsName)) {
                    String name = CodeUtils.extractUnqualifiedClassName(node);
                    AttributedElement thisEl = ce.lookup(name, Kind.CONST);
                    node2Element.put(node.getClassName(), ce);
                    node2Element.put(node, thisEl);
                    node2Element.put(node.getConstant(), thisEl);
                    break;
                }
            }

        }
        super.visit(node);
    }

    @Override
    public void visit(StaticFieldAccess node) {
        Collection<AttributedElement> nn = getNamedGlobalElements(Kind.CLASS,
                CodeUtils.extractUnqualifiedClassName(node));
        if (!nn.isEmpty()) {
            String contextClassName = CodeUtils.extractUnqualifiedClassName(node);
            if ("parent".equals(contextClassName)) {
                contextClassName = getContextSuperClassName();
            } else if ("self".equals(contextClassName)) {
                contextClassName = getContextClassName();
            }
            for (AttributedElement ell : nn) {
                ClassElement ce = (ClassElement) ell;
                if (ce != null && (contextClassName == null || contextClassName.equals(ce.getName()))) {
                    String name = extractVariableName(node.getField());

                    if (name != null) {
                        AttributedElement thisEl = ce.lookup(name, Kind.VARIABLE);
                        if (thisEl != null) {
                            Variable field = node.getField();
                            node2Element.put(node.getClassName(), ce);
                            node2Element.put(node, thisEl);
                            node2Element.put(field, thisEl);
                            if (field instanceof ArrayAccess) {
                                Expression expr = field.getName();
                                if (expr instanceof Variable) {
                                    node2Element.put(expr, thisEl);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
        super.visit(node);
    }

    private AttributedElement enterGlobalVariable(String name) {
        AttributedElement g = global.lookup(name, Kind.VARIABLE);

        if (g == null) {
            //XXX: untested:
            g = global.enterWrite(name, Kind.VARIABLE, (ASTNode) null);
        }

        scopes.peek().enter(name, Kind.VARIABLE, g);

        return g;
    }

    @Override
    public void visit(ArrayAccess node) {
        if (node.getName() instanceof Variable && node.getIndex() instanceof Scalar) {
            String variableName = extractVariableName((Variable) node.getName());

            if (variableName != null && "GLOBALS".equals(variableName)) {
                Scalar v = (Scalar) node.getIndex();

                if (v.getScalarType() == Type.STRING) {
                    String value = v.getStringValue();
                    if (NavUtils.isQuoted(value)) {
                        node2Element.put(v, enterGlobalVariable(NavUtils.dequote(value)));
                    }
                }
            }
        }

        super.visit(node);
    }

    private String getContextClassName() {
        String contextClassName = null;
        Enumeration<DefinitionScope> elements = scopes.elements();
        while (elements.hasMoreElements()) {
            DefinitionScope nextElement = elements.nextElement();
            if (nextElement.enclosingClass != null) {
                contextClassName = nextElement.enclosingClass.getName();
            }
        }
        return contextClassName;
    }

    private String getContextSuperClassName() {
        String contextClassName = null;
        Enumeration<DefinitionScope> elements = scopes.elements();
        while (elements.hasMoreElements()) {
            DefinitionScope nextElement = elements.nextElement();
            if (nextElement.enclosingClass != null && nextElement.enclosingClass.superClass != null) {
                contextClassName = nextElement.enclosingClass.superClass.getName();
            }
        }
        return contextClassName;
    }

    private ParserResult getInfo() {
        return info;
    }

    private AttributedElement lookup(String name, Kind k) {
        DefinitionScope ds = scopes.peek();

        AttributedElement e;

        switch (k) {
            case FUNC:
            case IFACE:    
            case CLASS:
                e = global.lookup(name, k);
                break;
            default:
                e = ds.lookup(name, k);
                break;
        }

        if (e != null) {
            return e;
        }

        switch (k) {
            case FUNC:
            case IFACE:
            case CLASS:
                return global.enterWrite(name, k, (ASTNode) null);
            default:
                return ds.enterWrite(name, k, (ASTNode) null);
        }
    }

    public Collection<AttributedElement> getGlobalElements(Kind k) {
        return global.getElements(k);
    }

    public Collection<AttributedElement> getNamedGlobalElements(Kind k, String... filterNames) {
        Map<String, AttributedElement> name2El = global.name2Writes.get(k);

        List<AttributedElement> retval = new ArrayList<AttributedElement>();
        for (String fName : filterNames) {
            if (fName.equals("self")) {//NOI18N
                String ctxName = getContextClassName();
                if (ctxName != null) {
                    fName = ctxName;
                }
            }
            if (Kind.CLASS.equals(k) && fName.equals("parent")) {//NOI18N
                Collection<AttributedElement> values = name2El.values();
                if (name2El != null) {
                    for (AttributedElement ael : values) {
                        if (ael instanceof ClassElement) {
                            ClassElement ce = (ClassElement) ael;
                            ClassElement superClass = ce.getSuperClass();
                            if (superClass != null) {
                                retval.add(superClass);
                            }
                        }
                    }
                }
            } else {
                AttributedElement el = (name2El != null) ? name2El.get(fName) : null;
                if (el != null) {
                    retval.add(el);
                } else {
                    PHPIndex index = PHPIndex.get(info);
                    for (IndexedClass m : index.getClasses(null, fName, QuerySupport.Kind.PREFIX)) {
                        String idxName = m.getName();
                        el = global.enterWrite(idxName, Kind.CLASS, m);
                        retval.add(el);
                    }
                }
            }
        }
        return retval;
    }

    public AttributedElement getElement(ASTNode n) {
        return node2Element.get(n);
    }
    private Collection<IndexedElement> name2ElementCache;

    public void enterAllIndexedClasses() {
        if (name2ElementCache == null) {
            PHPIndex index = PHPIndex.get(info);
            name2ElementCache = new LinkedList<IndexedElement>();
            name2ElementCache.addAll(index.getClasses(null, "", QuerySupport.Kind.PREFIX));
        }

        for (IndexedElement f : name2ElementCache) {
            if (f instanceof IndexedClass) {
                global.enterWrite(f.getName(), Kind.CLASS, f);
            }
        }
    }

    private void performEnterPass(DefinitionScope scope, Collection<? extends ASTNode> nodes) {
        for (ASTNode n : nodes) {
            if (n instanceof MethodDeclaration) {
                FunctionDeclaration nn = ((MethodDeclaration) n).getFunction();
                String name = nn.getFunctionName().getName();
                node2Element.put(n, scope.enterWrite(name, Kind.FUNC, n));
                node2Element.put(nn, scope.enterWrite(name, Kind.FUNC, n));
                continue;
            }

            if (n instanceof FunctionDeclaration) {
                String name = ((FunctionDeclaration) n).getFunctionName().getName();

                node2Element.put(n, scope.enterWrite(name, Kind.FUNC, n));
            }

            if (n instanceof FieldsDeclaration) {
                for (SingleFieldDeclaration f : ((FieldsDeclaration) n).getFields()) {
                    String name = extractVariableName(f.getName());

                    if (name != null) {
                        node2Element.put(n, scope.enterWrite(name, Kind.VARIABLE, n));
                    }
                }
            }

            if (n instanceof ClassDeclaration) {
                ClassDeclaration node = (ClassDeclaration) n;
                String name = node.getName().getName();
                ClassElement ce = (ClassElement) global.enterWrite(name, Kind.CLASS, node);
                node2Element.put(node, ce);
                Identifier superClsName = (node.getSuperClass() != null) ?
                    CodeUtils.extractUnqualifiedIdentifier(node.getSuperClass()) : null;

                if (superClsName != null) {
                    ce.superClass = (ClassElement) lookup(superClsName.getName(), Kind.CLASS);
                    node2Element.put(node.getSuperClass(), ce.superClass);
                }
                List<Expression> interfaes = node.getInterfaes();
                for (Expression identifier : interfaes) {
                    //TODO: ifaces must be fixed;
                }
                if (node.getBody() != null) {
                    performEnterPass(ce.enclosedElements, node.getBody().getStatements());
                }
            }

            if (n instanceof ConstantDeclaration) {
                List<Identifier> constNames = ((ConstantDeclaration) n).getNames();
                for (Identifier id : constNames) {
                    node2Element.put(n, scope.enterWrite(id.getName(), Kind.CONST, n));
                }
            }

        }
    }
    private static Map<ParserResult, SemiAttribute> info2Attr = new WeakHashMap<ParserResult, SemiAttribute>();

    public static SemiAttribute semiAttribute(ParserResult info) {
        SemiAttribute a = info2Attr.get(info);

        if (a == null) {
            long startTime = System.currentTimeMillis();

            a = new SemiAttribute(info);
            a.scan(Utils.getRoot(info));

            a.info = null;

            info2Attr.put(info, a);

            long endTime = System.currentTimeMillis();

            FileObject fo = info.getSnapshot().getSource().getFileObject();

            Logger.getLogger("TIMER").log(Level.FINE, "SemiAttribute global instance", new Object[]{fo, a});
            Logger.getLogger("TIMER").log(Level.FINE, "SemiAttribute global time", new Object[]{fo, (endTime - startTime)});
        }

        return a;
    }

    public static SemiAttribute semiAttribute(ParserResult info, int stopOffset) {
        SemiAttribute a = new SemiAttribute(info, stopOffset);

        try {
            a.scan(Utils.getRoot(info));
        } catch (Stop s) {
        }

        return a;
    }

    private static String name(ASTNode n) {
        if (n instanceof Identifier) {
            return ((Identifier) n).getName();
        }

        return null;
    }

    @CheckForNull
    //TODO converge this method with CodeUtils.extractVariableName()
    public static String extractVariableName(Variable var) {
        String varName = CodeUtils.extractVariableName(var);

        if (varName != null && varName.startsWith("$")){ //NOI18N
            return varName.substring(1);
        }

        return varName;
    }

    private ClassElement resolveTypeSimple(Dispatch node) {
        ClassElement ce = null;
        AttributedElement el = node2Element.get(node.getDispatcher());

        if (el != null) {
            AttributedType type = el.writesTypes.get(el.getWrites().size() - 1);

            if (type instanceof ClassType) {
                ce = ((ClassType) type).getElement();
            }
        }

        return ce;
    }

    public Collection<AttributedElement> getFunctions() {
        Collection<AttributedElement> retval = null;
        if (global != null) {
            retval = global.getFunctions();
        } else {
            retval = Collections.emptyList();
        }
        return retval;
    }

    public Collection<AttributedElement> getConstants() {
        Collection<AttributedElement> retval = null;
        if (global != null) {
            retval = global.getConstants();
        } else {
            retval = Collections.emptyList();
        }
        return retval;
    }

    public Collection<AttributedElement> getGlobalVariables() {
        Collection<AttributedElement> retval = null;
        if (global != null) {
            retval = global.getVariables();
        } else {
            retval = Collections.emptyList();
        }
        return retval;
    }

    public Collection<ClassElement> getClasses() {
        Collection<ClassElement> retval = null;
        if (global != null) {
            retval = global.getClasses();
        } else {
            retval = Collections.emptyList();
        }
        return retval;
    }

    public boolean hasGlobalVisibility(AttributedElement elem) {
        if (elem.isClassMember()) {
            ClassMemberElement cme = (ClassMemberElement) elem;
            boolean isGlobal = (cme.getModifier() == -1 || !cme.isPrivate()) && hasGlobalVisibility(cme.getClassElement());
            return isGlobal;
        }
        return (global != null) ? global.getElements(elem.getKind()).contains(elem) : false;
    }

    public static class AttributedElement {

        private List<Union2<ASTNode, IndexedElement>> writes; //aka declarations

        private List<AttributedType> writesTypes;
        private String name;
        private Kind k;

        public AttributedElement(Union2<ASTNode, IndexedElement> n, String name, Kind k) {
            this(n, name, k, null);
        }

        public AttributedElement(Union2<ASTNode, IndexedElement> n, String name, Kind k, AttributedType type) {
            this.writes = new LinkedList<Union2<ASTNode, IndexedElement>>();
            this.writesTypes = new LinkedList<AttributedType>();
            this.writes.add(n);

            this.writesTypes.add(type);
            this.name = name;
            this.k = k;
        }

        public boolean isClassMember() {
            return false;
        }

        public List<Union2<ASTNode, IndexedElement>> getWrites() {
            return writes;
        }

        public Kind getKind() {
            return k;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof AttributedElement)) {
                return false;
            }
            AttributedElement element = (AttributedElement) obj;
            return this.name.equals(element.name) && this.k.equals(element.k);
        }

        void addWrite(Union2<ASTNode, IndexedElement> node, AttributedType type) {
            writes.add(node);
            writesTypes.add(type);
        }

        Types getTypes() {
            return new Types(this);
        }

        public String getScopeName() {
            String retval = "";//NOI18N
            Types types = getTypes();
            for (int i = 0; i < types.size(); i++) {
                AttributedType type = types.getType(i);
                if (type != null) {
                    retval = type.getTypeName();
                    break;
                }
            }
            return retval;
        }

        public enum Kind {

            VARIABLE, FUNC, CLASS, CONST, IFACE;
        }
    }

    private static class Types {

        private AttributedElement el;

        Types(AttributedElement el) {
            this.el = el;
        }

        int size() {
            return el.writesTypes.size();
        }

        AttributedType getType(int idx) {
            return el.writesTypes.get(idx);
        }
    }

    public static class ClassMemberElement extends AttributedElement {

        private ClassElement classElement;
        int modifier = -1;

        public ClassMemberElement(Union2<ASTNode, IndexedElement> n, ClassElement classElement, String name, Kind k) {
            super(n, name, k);
            this.classElement = classElement;
            setModifiers(n, name);
            assert classElement != null;
        }

        public String getClassName() {
            return getClassElement().getName();
        }

        @Override
        public String getScopeName() {
            return getClassName();
        }

        public int getModifier() {
            return modifier;
        }

        public boolean isPublic() {
            return BodyDeclaration.Modifier.isPublic(getModifier());
        }

        public boolean isPrivate() {
            return BodyDeclaration.Modifier.isPrivate(getModifier());
        }

        public boolean isProtected() {
            return BodyDeclaration.Modifier.isProtected(getModifier());
        }

        public boolean isStatic() {
            return BodyDeclaration.Modifier.isStatic(getModifier());
        }

        public ClassElement getClassElement() {
            return classElement;
        }

        @Override
        public boolean isClassMember() {
            return true;
        }

        public ClassMemberKind getClassMemberKind() {
            ClassMemberKind retval = null;
            switch (getKind()) {
                case CONST:
                    retval = ClassMemberKind.CONST;
                    break;
                case FUNC:
                    retval = ClassMemberKind.METHOD;
                    break;
                case VARIABLE:
                    retval = ClassMemberKind.FIELD;
                    break;
                default:
                    assert false;

            }
            assert retval != null;
            return retval;
        }

        private void setModifiers(Union2<ASTNode, IndexedElement> n, String name) {
            if (n.hasFirst()) {
                ASTNode node = n.first();
                if (node instanceof BodyDeclaration) {
                    modifier = ((BodyDeclaration) node).getModifier();
                } else if (name.equals("this")) {
                    //NOI18N
                    assert false;
                } else if (node instanceof ConstantDeclaration) {
                    modifier |= BodyDeclaration.Modifier.PUBLIC;
                } else {
                    assert false : name;
                }
            } else if (n.hasSecond()) {
                IndexedElement index = n.second();
                if (index != null) {
                    Set<Modifier> modifiers = index.getModifiers();
                    for (Modifier mod : modifiers) {
                        switch (mod) {
                            case PRIVATE:
                                modifier |= BodyDeclaration.Modifier.PRIVATE;
                                break;
                            case PROTECTED:
                                modifier |= BodyDeclaration.Modifier.PROTECTED;
                                break;
                            case PUBLIC:
                                modifier |= BodyDeclaration.Modifier.PUBLIC;
                                break;
                            case STATIC:
                                modifier |= BodyDeclaration.Modifier.STATIC;
                                break;
                        }
                    }
                }
            }
        }

        public enum ClassMemberKind {

            FIELD, METHOD, CONST;
        }
    }
    public  class ClassElement extends AttributedElement {

        private final DefinitionScope enclosedElements;
        private ClassElement superClass;
        private Set<ClassElement> ifaces = new HashSet<ClassElement>();
        private boolean initialized;

        public ClassElement(Union2<ASTNode, IndexedElement> n, String name, Kind k) {
            super(n, name, k);
            enclosedElements = new DefinitionScope(this);
        }

        public AttributedElement lookup(String name, Kind k) {
            AttributedElement el = enclosedElements.lookup(name, k);
            if (el != null) {
                return el;
            }
            PHPIndex index = PHPIndex.get(info);
            int attrs = PHPIndex.ANY_ATTR;
            switch(k) {
                case CONST:
                    
                for (IndexedClassMember<IndexedConstant> classMember : index.getAllTypeConstants(null, getName(), name, QuerySupport.Kind.PREFIX)) {
                    IndexedConstant m = classMember.getMember();
                    String idxName = m.getName();
                    idxName = (idxName.startsWith("$")) ? idxName.substring(1) : idxName;
                    enclosedElements.enterWrite(idxName, Kind.CONST, m);
                } break;
                case FUNC:
                for (IndexedClassMember<IndexedFunction> classMember : index.getAllMethods(null, getName(), name, QuerySupport.Kind.PREFIX, attrs)) {
                    IndexedFunction m = classMember.getMember();
                    enclosedElements.enterWrite(m.getName(), Kind.FUNC, m);
                } break;
                case VARIABLE:
                for (IndexedClassMember<IndexedConstant> classMember : index.getAllFields(null, getName(), name, QuerySupport.Kind.PREFIX, attrs)) {
                    IndexedConstant m = classMember.getMember();
                    String idxName = m.getName();
                    idxName = (idxName.startsWith("$")) ? idxName.substring(1) : idxName;
                    enclosedElements.enterWrite(idxName, Kind.VARIABLE, m);
                } break;

            }
            return enclosedElements.lookup(name, k);
        }

        public Collection<AttributedElement> getElements(Kind k) {
            List<AttributedElement> elements = new ArrayList<AttributedElement>();

            getElements0(elements, k);

            return Collections.unmodifiableList(elements);
        }

        public Collection<AttributedElement> getNamedElements(Kind k, String... filterNames) {
            Collection<AttributedElement> elements = getElements(k);
            List<AttributedElement> retval = new ArrayList<AttributedElement>();
            for (String fName : filterNames) {
                for (AttributedElement el : elements) {
                    if (el.getName().equals(fName)) {
                        retval.add(el);
                    }
                }
            }
            return retval;
        }

        public Collection<AttributedElement> getMethods() {
            return getElements(Kind.FUNC);
        }

        public Collection<AttributedElement> getFields() {
            Collection<AttributedElement> elems = getElements(Kind.VARIABLE);
            List<AttributedElement> retval = new ArrayList<AttributedElement>();
            for (AttributedElement elm : elems) {
                if (!elm.getName().equals("this")) {
                    retval.add(elm);
                }
            }
            return retval;
        }

        public ClassElement getSuperClass() {
            return superClass;
        }

        private void getElements0(List<AttributedElement> elements, Kind k) {
            elements.addAll(enclosedElements.getElements(k));

            if (superClass != null) {
                superClass.getElements0(elements, k);
            }
        }

        boolean isInitialized() {
            return initialized;
        }

        void initialized() {
            initialized = true;
        }
    }

    public  class FunctionElement extends AttributedElement {

        private final DefinitionScope enclosedElements;
        private boolean initialized;

        public FunctionElement(Union2<ASTNode, IndexedElement> n, String name, Kind k) {
            super(n, name, k);
            enclosedElements = new DefinitionScope(this);
        }

        public AttributedElement lookup(String name, Kind k) {
            return enclosedElements.lookup(name, k);
        }

        public Collection<AttributedElement> getElements(Kind k) {
            List<AttributedElement> elements = new ArrayList<AttributedElement>();

            getElements0(elements, k);

            return Collections.unmodifiableList(elements);
        }

        public Collection<AttributedElement> getNamedElements(Kind k, String... filterNames) {
            Collection<AttributedElement> elements = getElements(k);
            List<AttributedElement> retval = new ArrayList<AttributedElement>();
            for (String fName : filterNames) {
                for (AttributedElement el : elements) {
                    if (el.getName().equals(fName)) {
                        retval.add(el);
                    }
                }
            }
            return retval;
        }

        public Collection<AttributedElement> getVariables() {
            return getElements(Kind.VARIABLE);
        }

        private void getElements0(List<AttributedElement> elements, Kind k) {
            elements.addAll(enclosedElements.getElements(k));
        }

        boolean isInitialized() {
            return initialized;
        }

        void initialized() {
            initialized = true;
        }
    }

    public  class DefinitionScope {

        private final Map<Kind, Map<String, AttributedElement>> name2Writes = new HashMap<Kind, Map<String, AttributedElement>>();
//        private final Map<AttributedElement, ASTNode> reads = new HashMap<AttributedElement, ASTNode>();
        private boolean classScope;
        private boolean functionScope;
        private AttributedElement thisVar;
        private ClassElement enclosingClass;
        private FunctionElement enclosingFunction;

        public DefinitionScope() {
        }

        public DefinitionScope(ClassElement enclosingClass) {
            this.enclosingClass = enclosingClass;
            this.classScope = enclosingClass != null;


            if (classScope) {
                thisVar = enterWrite("this", Kind.VARIABLE, (ASTNode) null, new ClassType(enclosingClass));
            }
        }

        public DefinitionScope(FunctionElement enclosingFunction) {
            this.enclosingFunction = enclosingFunction;
            this.functionScope = enclosingFunction != null;
        }

        public AttributedElement enterWrite(String name, Kind k, ASTNode node) {
            return enterWrite(name, k, node, null);
        }

        public AttributedElement enterWrite(String name, Kind k, ASTNode node, AttributedType type) {
            return enterWrite(name, k, Union2.<ASTNode, IndexedElement>createFirst(node), type);
        }

        public AttributedElement enterWrite(String name, Kind k, IndexedElement el) {
            return enterWrite(name, k, Union2.<ASTNode, IndexedElement>createSecond(el), null);
        }

        private AttributedElement enterWrite(String name, Kind k, Union2<ASTNode, IndexedElement> node, AttributedType type) {
            if (k == Kind.VARIABLE && this != global) {
                //TODO: review
                if (SUPERGLOBALS.contains(name)) {
                    return SemiAttribute.this.enterGlobalVariable(name);
                }
            }

            Map<String, AttributedElement> name2El = name2Writes.get(k);

            if (name2El == null) {
                name2Writes.put(k, name2El = new HashMap<String, AttributedElement>());
            }

            AttributedElement el = name2El.get(name);

            if (el == null) {
                if (k == Kind.CLASS || k == Kind.IFACE) {
                    el = new ClassElement(node, name, k);
                } else {
                    if (classScope && !Arrays.asList(new String[]{"this"}).contains(name)) {
                        switch (k) {
                            case CONST:
                            case FUNC:
                            case VARIABLE:
                                el = new ClassMemberElement(node, enclosingClass, name, k);
                                break;
                            default:
                                assert false;
                        }
                    } else {
                        if (k == Kind.FUNC) {
                            el = new FunctionElement(node, name, k);
                        } else if (k == Kind.VARIABLE) {
                            if (type == null && functionScope && enclosingFunction != null) {
                                type = new FunctionType(enclosingFunction);
                            }
                            el = new AttributedElement(node, name, k, type);
                        } else {
                            el = new AttributedElement(node, name, k, type);
                        }
                    }
                }

                name2El.put(name, el);
            } else {
                el.addWrite(node, type);
            }

            return el;
        }

        public AttributedElement enter(String name, Kind k, AttributedElement el) {
            Map<String, AttributedElement> name2El = name2Writes.get(k);
            if (name2El == null) {
                name2Writes.put(k, name2El = new HashMap<String, AttributedElement>());
            }
            name2El.put(name, el);
            return el;
        }

        public AttributedElement lookup(String name, Kind k) {            
            AttributedElement el = null;
            Map<String, AttributedElement> name2El = name2Writes.get(k);
            if (name2El != null) {
                el = name2El.get(name);
            }
            if (el == null) {
                PHPIndex index = PHPIndex.get(info);
                switch(k) {
                    case CONST:
                    for (IndexedConstant m : index.getConstants(null, name, QuerySupport.Kind.PREFIX)) {
                        String idxName = m.getName();
                        el = enterWrite(idxName, Kind.CONST, m);
                    }
                    break;
                }
            }
            return el;
        }

        public Collection<AttributedElement> getElements(Kind k) {
            Map<String, AttributedElement> name2El = name2Writes.get(k);
            if (name2El != null) {
                return Collections.unmodifiableCollection(name2El.values());
            }
            return Collections.emptyList();
        }

        public Collection<AttributedElement> getFunctions() {
            return getElements(Kind.FUNC);
        }

        public Collection<AttributedElement> getVariables() {
            return getElements(Kind.VARIABLE);
        }

        private Collection<AttributedElement> getConstants() {
            return getElements(Kind.CONST);
        }

        public Collection<ClassElement> getClasses() {
            Collection<ClassElement> retval = new LinkedHashSet<ClassElement>();
            Collection<AttributedElement> elements = getElements(Kind.CLASS);
            for (AttributedElement el : elements) {
                assert el instanceof ClassElement;
                retval.add((ClassElement) el);
            }
            return retval;
        }
    }

    private static final class Stop extends Error {
    }

    public static abstract class AttributedType {

        public abstract String getTypeName();
    }

    public static class ClassType extends AttributedType {

        private ClassElement element;

        public ClassType(ClassElement element) {
            this.element = element;
        }

        public ClassElement getElement() {
            return element;
        }

        @Override
        public String getTypeName() {
            return getElement().getName();
        }
    }

    public static class FunctionType extends AttributedType {

        private FunctionElement element;

        public FunctionType(FunctionElement element) {
            this.element = element;
        }

        public FunctionElement getElement() {
            return element;
        }

        @Override
        public String getTypeName() {
            return getElement().getName();
        }
    }
}
