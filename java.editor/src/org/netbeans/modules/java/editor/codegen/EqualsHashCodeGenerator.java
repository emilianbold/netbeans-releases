/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.editor.codegen;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.awt.Dialog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.editor.codegen.ui.ElementNode;
import org.netbeans.modules.java.editor.codegen.ui.EqualsHashCodePanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public class EqualsHashCodeGenerator implements CodeGenerator {

    public static class Factory implements CodeGenerator.Factory {
        
        Factory() {            
        }
        
        public Iterable<? extends CodeGenerator> create(CompilationController cc, TreePath path) throws IOException {
            path = Utilities.getPathElementOfKind(Tree.Kind.CLASS, path);
            if (path == null)
                return Collections.emptySet();
            cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            
            EqualsHashCodeGenerator gen = createEqualsHashCodeGenerator(cc, cc.getTrees().getElement(path));
            
            if (gen == null) {
                return Collections.emptySet();
            } else {
                return Collections.singleton(gen);
            }
        }
    }

    final ElementNode.Description description;
    final boolean generateEquals;
    final boolean generateHashCode;
    
    /** Creates a new instance of EqualsHashCodeGenerator */
    private EqualsHashCodeGenerator(ElementNode.Description description, boolean generateEquals, boolean generateHashCode) {
        this.description = description;        
        this.generateEquals = generateEquals;
        this.generateHashCode = generateHashCode;
        
    }

    public String getDisplayName() {
        if( generateEquals && generateHashCode )
            return org.openide.util.NbBundle.getMessage(EqualsHashCodeGenerator.class, "LBL_equals_and_hashcode"); //NOI18N
        if( !generateEquals )
            return org.openide.util.NbBundle.getMessage(EqualsHashCodeGenerator.class, "LBL_hashcode"); //NOI18N
        return org.openide.util.NbBundle.getMessage(EqualsHashCodeGenerator.class, "LBL_equals"); //NOI18N
    }
    
    static EqualsHashCodeGenerator createEqualsHashCodeGenerator(CompilationController cc, Element el) throws IOException {
        if (el.getKind() != ElementKind.CLASS)
            return null;
        TypeElement typeElement = (TypeElement)el;
        
        ExecutableElement[] equalsHashCode = overridesHashCodeAndEquals(cc, typeElement, null);
        
        List<ElementNode.Description> descriptions = new ArrayList<ElementNode.Description>();
        for (VariableElement variableElement : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
            cc.toPhase(JavaSource.Phase.RESOLVED);
            descriptions.add(ElementNode.Description.create(variableElement, null, true, isUsed(cc, variableElement, equalsHashCode)));
        }
        if (descriptions.isEmpty() || (equalsHashCode[0] != null && equalsHashCode[1] != null))
            return null;
        return new EqualsHashCodeGenerator(
            ElementNode.Description.create(typeElement, descriptions, false, false),
            equalsHashCode[0] == null,
            equalsHashCode[1] == null
        );
    }
    
    /** Checks whether a field is used inside given methods.
     */
    private static boolean isUsed(CompilationInfo cc, VariableElement field, ExecutableElement... methods) {
        class Used extends TreePathScanner<Void, VariableElement> {
            boolean found;
            
            @Override
            public Void visitIdentifier(IdentifierTree id, VariableElement what) {
                if (id.getName().equals(what.getSimpleName())) {
                    found = true;
                }
                
                return super.visitIdentifier(id, what);
            }

            @Override
            public Void visitMemberSelect(MemberSelectTree sel, VariableElement what) {
                if (sel.getIdentifier().equals(what.getSimpleName())) {
                    found = true;
                }
                return super.visitMemberSelect(sel, what);
            }
        }
        for (ExecutableElement e : methods) {
            if (e == null) {
                continue;
            }
            Trees tree = cc.getTrees();
            TreePath path = tree.getPath(e);
            Used used = new Used();
            used.scan(path, field);
            if (used.found) {
                return true;
            }
        }
        return false;
    }
    
    /** Computes whether a class defines equals and hashcode or not.
     * @param compilationInfo context 
     * @param type the class element to check
     * @param stop array of booleans that is checked for [0], if true the method imediatelly returns
     * @return array of two elements [0] is equals, if it exists, [1] is hashCode, if it exists, otherwise the indexes are null
     */
    public static ExecutableElement[] overridesHashCodeAndEquals(CompilationInfo compilationInfo, Element type, boolean[] stop) {
        ExecutableElement[] ret = new ExecutableElement[2];

        TypeElement el = compilationInfo.getElements().getTypeElement("java.lang.Object"); // NOI18N
        
        if (el == null) {
            return ret;
        }
        if (type == null || type.getKind() != ElementKind.CLASS) {
            return ret;
        }
        
        ExecutableElement hashCode = null;
        ExecutableElement equals = null;
        
        for (Element method : el.getEnclosedElements()) {
            if (stop != null && stop[0]) {
                return ret;
            }
            if (method.getKind() == ElementKind.METHOD) {
                if (method.getSimpleName().contentEquals("equals")) { // NOI18N
                    assert equals == null;
                    equals = (ExecutableElement)method;
                }
                if (method.getSimpleName().contentEquals("hashCode")) { // NOI18N
                    assert hashCode == null;
                    hashCode = (ExecutableElement)method;
                }
            }
        }
        assert hashCode != null;
        assert equals != null;
        
        TypeElement clazz = (TypeElement)type;
        for (Element ee : type.getEnclosedElements()) {
            if (stop != null && stop[0]) {
                return ret;
            }
            if (ee.getKind() != ElementKind.METHOD) {
                continue;
            }
            ExecutableElement method = (ExecutableElement)ee;
            
            if (compilationInfo.getElements().overrides(method, hashCode, clazz)) {
                ret[1] = method;
            }
            
            if (compilationInfo.getElements().overrides(method, equals, clazz)) {
                ret[0] = method;
            }
        }
        
        return ret;
    }
    
    public static void invokeEqualsHashCode(final TreePathHandle handle, final JTextComponent component) {
        JavaSource js = JavaSource.forDocument(component.getDocument());
        if (js != null) {
            class FillIn implements Task<CompilationController> {
                EqualsHashCodeGenerator gen;
                
                public void run(CompilationController cc) throws Exception {
                    cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    Element e = handle.resolveElement(cc);
                    
                    gen = createEqualsHashCodeGenerator(cc, e);
                }
                
                public void invoke() {
                    if (gen != null) {
                        gen.invoke(component);
                    }
                }

            }
            FillIn fillIn = new FillIn();
            try {
                js.runUserActionTask(fillIn, true);
                fillIn.invoke();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    public void invoke(JTextComponent component) {
        final EqualsHashCodePanel panel = new EqualsHashCodePanel(description, generateEquals, generateHashCode);
        String title = NbBundle.getMessage(ConstructorGenerator.class, "LBL_generate_equals_and_hashcode"); //NOI18N
        if( !generateEquals )
            title = NbBundle.getMessage(ConstructorGenerator.class, "LBL_generate_hashcode"); //NOI18N
        else if( !generateHashCode )
            title = NbBundle.getMessage(ConstructorGenerator.class, "LBL_generate_equals"); //NOI18N
        DialogDescriptor dialogDescriptor = GeneratorUtils.createDialogDescriptor(panel, title);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.setVisible(true);
        if (dialogDescriptor.getValue() == dialogDescriptor.getDefaultValue()) {
            JavaSource js = JavaSource.forDocument(component.getDocument());
            if (js != null) {
                try {
                    final int caretOffset = component.getCaretPosition();
                    js.runModificationTask(new Task<WorkingCopy>() {

                        public void run(WorkingCopy copy) throws IOException {
                            copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                            TreePath path = copy.getTreeUtilities().pathFor(caretOffset);
                            path = Utilities.getPathElementOfKind(Tree.Kind.CLASS, path);
                            int idx = GeneratorUtils.findClassMemberIndex(copy, (ClassTree)path.getLeaf(), caretOffset);
                            ArrayList<VariableElement> equalsElements = new ArrayList<VariableElement>();
                            if( generateEquals ) {
                                for (ElementHandle<? extends Element> elementHandle : panel.getEqualsVariables())
                                    equalsElements.add((VariableElement)elementHandle.resolve(copy));
                                }
                            ArrayList<VariableElement> hashCodeElements = new ArrayList<VariableElement>();
                            if( generateHashCode ) {
                                for (ElementHandle<? extends Element> elementHandle : panel.getHashCodeVariables())
                                    hashCodeElements.add((VariableElement)elementHandle.resolve(copy));
                            }
                            generateEqualsAndHashCode(
                                copy, path, 
                                generateEquals ? equalsElements : null, 
                                generateHashCode ? hashCodeElements : null, 
                                idx
                            );
                        }
                    }).commit();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    
    private static void generateEqualsAndHashCode(WorkingCopy wc, TreePath path, Iterable<? extends VariableElement> equalsFields, Iterable<? extends VariableElement> hashCodeFields, int index) {
        assert path.getLeaf().getKind() == Tree.Kind.CLASS;
        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
        if (te != null) {
            TreeMaker make = wc.getTreeMaker();
            ClassTree nue = (ClassTree)path.getLeaf();
            if (hashCodeFields != null) {
                nue = make.insertClassMember(nue, index, createHashCodeMethod(wc, hashCodeFields, (DeclaredType)te.asType()));
            }
            if (equalsFields != null) {
                nue = make.insertClassMember(nue, index, createEqualsMethod(wc, equalsFields, (DeclaredType)te.asType()));
            }
            wc.rewrite(path.getLeaf(), nue);
        }        
    }
    
    private static MethodTree createEqualsMethod(WorkingCopy wc, Iterable<? extends VariableElement> equalsFields, DeclaredType type) {
        TreeMaker make = wc.getTreeMaker();
        Set<Modifier> mods = EnumSet.of(Modifier.PUBLIC);
        List<VariableTree> params = Collections.singletonList(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "obj", make.Type(wc.getElements().getTypeElement("java.lang.Object").asType()), null)); //NOI18N
        
        List<StatementTree> statements = new ArrayList<StatementTree>();
        //if (obj == null) return false;
        statements.add(make.If(make.Binary(Tree.Kind.EQUAL_TO, make.Identifier("obj"), make.Identifier("null")), make.Return(make.Identifier("false")), null)); //NOI18N
        //if (getClass() != obj.getClass()) return false;
        statements.add(make.If(make.Binary(Tree.Kind.NOT_EQUAL_TO, make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.Identifier("getClass"), Collections.<ExpressionTree>emptyList()), //NOI18N
                make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(make.Identifier("obj"), "getClass"), Collections.<ExpressionTree>emptyList())), make.Return(make.Identifier("false")), null)); //NOI18N
        //<this type> other = (<this type>) o;
        statements.add(make.Variable(make.Modifiers(EnumSet.of(Modifier.FINAL)), "other", make.Type(type), make.TypeCast(make.Type(type), make.Identifier("obj")))); //NOI18N
        for (VariableElement ve : equalsFields) {
            if (ve.asType().getKind().isPrimitive()) {
                //if (this.<var> != other.<var>) return false;                
                statements.add(make.If(make.Binary(Tree.Kind.NOT_EQUAL_TO, make.MemberSelect(make.Identifier("this"), ve.getSimpleName()), make.MemberSelect(make.Identifier("other"), ve.getSimpleName())), make.Return(make.Identifier("false")), null)); //NOI18N
            } else {
                //if (this.<var> != other.<var> && (this.<var> == null || !this.<var>.equals(other.<var>))) return false;                
                ExpressionTree exp1 = make.Binary(Tree.Kind.NOT_EQUAL_TO, make.MemberSelect(make.Identifier("this"), ve.getSimpleName()), make.MemberSelect(make.Identifier("other"), ve.getSimpleName())); //NOI18N
                ExpressionTree exp2 = make.Binary(Tree.Kind.EQUAL_TO, make.MemberSelect(make.Identifier("this"), ve.getSimpleName()), make.Identifier("null")); //NOI18N
                ExpressionTree exp3 = make.Unary(Tree.Kind.LOGICAL_COMPLEMENT, make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(make.MemberSelect(make.Identifier("this"), ve.getSimpleName()), "equals"), Collections.singletonList(make.MemberSelect(make.Identifier("other"), ve.getSimpleName())))); //NOI18N
                statements.add(make.If(make.Binary(Tree.Kind.CONDITIONAL_AND, exp1, make.Parenthesized(make.Binary(Tree.Kind.CONDITIONAL_OR, exp2, exp3))), make.Return(make.Identifier("false")), null)); //NOI18N
            }
        }
        statements.add(make.Return(make.Identifier("true")));
        BlockTree body = make.Block(statements, false);
        
        return make.Method(make.Modifiers(mods), "equals", make.PrimitiveType(TypeKind.BOOLEAN), Collections.<TypeParameterTree> emptyList(), params, Collections.<ExpressionTree>emptyList(), body, null); //NOI18N
    }    
    
    private static MethodTree createHashCodeMethod(WorkingCopy wc, Iterable<? extends VariableElement> hashCodeFields, DeclaredType type) {
        TreeMaker make = wc.getTreeMaker();
        Set<Modifier> mods = EnumSet.of(Modifier.PUBLIC);        

        int startNumber = generatePrimeNumber(2, 10);
        int multiplyNumber = generatePrimeNumber(10, 100);
        List<StatementTree> statements = new ArrayList<StatementTree>();
        //int hash = <startNumber>;
        statements.add(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "hash", make.PrimitiveType(TypeKind.INT), make.Literal(startNumber))); //NOI18N        
        for (VariableElement ve : hashCodeFields) {
            ExpressionTree variableRead;
            switch (ve.asType().getKind()) {
                case BYTE:
                case CHAR:
                case SHORT:
                case INT:
                    variableRead = make.MemberSelect(make.Identifier("this"), ve.getSimpleName()); //NOI18N
                    break;
                case LONG:
                    variableRead = make.TypeCast(make.PrimitiveType(TypeKind.INT), make.Parenthesized(make.Binary(Tree.Kind.XOR,
                            make.MemberSelect(make.Identifier("this"), ve.getSimpleName()), make.Parenthesized(make.Binary(Tree.Kind.UNSIGNED_RIGHT_SHIFT, //NOI18N
                            make.MemberSelect(make.Identifier("this"), ve.getSimpleName()), make.Literal(32)))))); //NOI18N
                    break;
                case FLOAT:
                    variableRead = make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(make.Identifier("Float"), "floatToIntBits"), //NOI18N
                            Collections.singletonList(make.MemberSelect(make.Identifier("this"), ve.getSimpleName()))); //NOI18N
                    break;
                case DOUBLE:
                    ExpressionTree et = make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(make.Identifier("Double"), "doubleToLongBits"), //NOI18N
                            Collections.singletonList(make.MemberSelect(make.Identifier("this"), ve.getSimpleName()))); //NOI18N
                    variableRead = make.TypeCast(make.PrimitiveType(TypeKind.INT), make.Parenthesized(make.Binary(Tree.Kind.XOR,
                            et, make.Parenthesized(make.Binary(Tree.Kind.UNSIGNED_RIGHT_SHIFT, et, make.Literal(32)))))); //NOI18N
                    break;
                case BOOLEAN:
                    variableRead = make.Parenthesized(make.ConditionalExpression(make.MemberSelect(make.Identifier("this"), ve.getSimpleName()), make.Literal(1), make.Literal(0))); //NOI18N
                    break;
                default:
                    variableRead = make.Parenthesized(
                            make.ConditionalExpression(make.Binary(Tree.Kind.NOT_EQUAL_TO, make.MemberSelect(make.Identifier("this"), ve.getSimpleName()), make.Identifier("null")), //NOI18N
                            make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(make.MemberSelect(make.Identifier("this"), ve.getSimpleName()), "hashCode"), Collections.<ExpressionTree>emptyList()), //NOI18N
                            make.Literal(0)));
            }
            statements.add(make.ExpressionStatement(make.Assignment(make.Identifier("hash"), make.Binary(Tree.Kind.PLUS, make.Binary(Tree.Kind.MULTIPLY, make.Literal(multiplyNumber), make.Identifier("hash")), variableRead)))); //NOI18N
        }
        statements.add(make.Return(make.Identifier("hash"))); //NOI18N        
        BlockTree body = make.Block(statements, false);
        
        return make.Method(make.Modifiers(mods), "hashCode", make.PrimitiveType(TypeKind.INT), Collections.<TypeParameterTree> emptyList(), Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), body, null); //NOI18N
    }

    private static boolean isPrimeNumber(int n) {
        int squareRoot = (int) Math.sqrt(n) + 1;
        if (n % 2 == 0)
            return false;
        for (int cntr = 3; cntr < squareRoot; cntr++) {
            if (n % cntr == 0)
                return false;
        }
        return true;
    }
    
    private static int generatePrimeNumber(int lowerLimit, int higherLimit) {
        Random r = new Random(System.currentTimeMillis());
        int proposed = r.nextInt(higherLimit - lowerLimit) + lowerLimit;        
        while (!isPrimeNumber(proposed))
            proposed++;
        if (proposed > higherLimit) {
            proposed--;
            while (!isPrimeNumber(proposed))
                proposed--;
        }
        return proposed;
    }
}
