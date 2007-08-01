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
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.awt.Dialog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.editor.codegen.ui.DelegatePanel;
import org.netbeans.modules.java.editor.codegen.ui.ElementNode;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public class DelegateMethodGenerator implements CodeGenerator {

    public static class Factory implements CodeGenerator.Factory {
        
        Factory() {            
        }
        
        public Iterable<? extends CodeGenerator> create(CompilationController controller, TreePath path) throws IOException {
            path = Utilities.getPathElementOfKind(Tree.Kind.CLASS, path);
            if (path == null)
                return Collections.emptySet();
            controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            Elements elements = controller.getElements();
            TypeElement typeElement = (TypeElement)controller.getTrees().getElement(path);
            if (!typeElement.getKind().isClass())
                return Collections.emptySet();
            Trees trees = controller.getTrees();
            Scope scope = trees.getScope(path);
            Map<Element, List<ElementNode.Description>> map = new LinkedHashMap<Element, List<ElementNode.Description>>();
            TypeElement cls;
            while(scope != null && (cls = scope.getEnclosingClass()) != null) {
                DeclaredType type = (DeclaredType)cls.asType();
                for (VariableElement field : ElementFilter.fieldsIn(elements.getAllMembers(cls))) {
                    if (!field.asType().getKind().isPrimitive() && trees.isAccessible(scope, field, type)) {
                        List<ElementNode.Description> descriptions = map.get(field.getEnclosingElement());
                        if (descriptions == null) {
                            descriptions = new ArrayList<ElementNode.Description>();
                            map.put(field.getEnclosingElement(), descriptions);
                        }
                        descriptions.add(ElementNode.Description.create(field, null, false, false));
                    }
                }
                scope = scope.getEnclosingScope();
            }
            List<ElementNode.Description> descriptions = new ArrayList<ElementNode.Description>();
            for (Map.Entry<Element, List<ElementNode.Description>> entry : map.entrySet())
                descriptions.add(ElementNode.Description.create(entry.getKey(), entry.getValue(), false, false));           
            if (descriptions.isEmpty())
                return Collections.emptySet();
            Collections.reverse(descriptions);
            return Collections.singleton(new DelegateMethodGenerator(ElementNode.Description.create(descriptions)));
        }
    }

    private ElementNode.Description description;
    
    /** Creates a new instance of DelegateMethodGenerator */
    private DelegateMethodGenerator(ElementNode.Description description) {
        this.description = description;
    }

    public String getDisplayName() {
        return org.openide.util.NbBundle.getMessage(DelegateMethodGenerator.class, "LBL_delegate_method"); //NOI18N
    }

    public void invoke(JTextComponent component) {
        final DelegatePanel panel = new DelegatePanel(component, description);
        DialogDescriptor dialogDescriptor = GeneratorUtils.createDialogDescriptor(panel, NbBundle.getMessage(ConstructorGenerator.class, "LBL_generate_delegate")); //NOI18N
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
                            ElementHandle<? extends Element> handle = panel.getDelegateField();
                            VariableElement delegate = handle != null ? (VariableElement)handle.resolve(copy) : null;
                            ArrayList<ExecutableElement> methods = new ArrayList<ExecutableElement>();
                            for (ElementHandle<? extends Element> elementHandle : panel.getDelegateMethods())
                                methods.add((ExecutableElement)elementHandle.resolve(copy));
                            generateDelegatingMethods(copy, path, delegate, methods, idx);
                        }
                    }).commit();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    public static ElementNode.Description getAvailableMethods(final JTextComponent component, final ElementHandle<? extends Element> elementHandle) {
        if (elementHandle.getKind().isField()) {
            JavaSource js = JavaSource.forDocument(component.getDocument());
            if (js != null) {
                try {
                    final int caretOffset = component.getCaretPosition();
                    final ElementNode.Description[] description = new ElementNode.Description[1];
                    js.runUserActionTask(new Task<CompilationController>() {

                        public void run(CompilationController controller) throws IOException {
                            VariableElement field = (VariableElement)elementHandle.resolve(controller);
                            if (field.asType().getKind() == TypeKind.DECLARED) {
                                DeclaredType type = (DeclaredType) field.asType();
                                Trees trees = controller.getTrees();
                                Scope scope = controller.getTreeUtilities().scopeFor(caretOffset);
                                Map<Element, List<ElementNode.Description>> map = new LinkedHashMap<Element, List<ElementNode.Description>>();
                                for (ExecutableElement method : ElementFilter.methodsIn(controller.getElements().getAllMembers((TypeElement)type.asElement()))) {
                                    if (trees.isAccessible(scope, method, type)) {
                                        List<ElementNode.Description> descriptions = map.get(method.getEnclosingElement());
                                        if (descriptions == null) {
                                            descriptions = new ArrayList<ElementNode.Description>();
                                            map.put(method.getEnclosingElement(), descriptions);
                                        }
                                        descriptions.add(ElementNode.Description.create(method, null, true, false));
                                    }
                                }
                                List<ElementNode.Description> descriptions = new ArrayList<ElementNode.Description>();
                                for (Map.Entry<Element, List<ElementNode.Description>> entry : map.entrySet())
                                    descriptions.add(ElementNode.Description.create(entry.getKey(), entry.getValue(), false, false));
                                if (!descriptions.isEmpty())
                                    Collections.reverse(descriptions);
                                    description[0] = ElementNode.Description.create(descriptions);
                            }
                        }
                    }, true);
                    return description[0];
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return null;
    }
    
    private static void generateDelegatingMethods(WorkingCopy wc, TreePath path, VariableElement delegate, Iterable<? extends ExecutableElement> methods, int index) {
        assert path.getLeaf().getKind() == Tree.Kind.CLASS;
        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
        if (te != null) {
            TreeMaker make = wc.getTreeMaker();
            ClassTree nue = (ClassTree)path.getLeaf();
            for (ExecutableElement executableElement : methods)
                nue = make.insertClassMember(nue, index, createDelegatingMethod(wc, delegate, executableElement, (DeclaredType)te.asType()));
            wc.rewrite(path.getLeaf(), nue);
        }        
    }
    
    private static MethodTree createDelegatingMethod(WorkingCopy wc, VariableElement delegate, ExecutableElement method, DeclaredType type) {
        TreeMaker make = wc.getTreeMaker();
        ExecutableType methodType = (ExecutableType)wc.getTypes().asMemberOf((DeclaredType)delegate.asType(), method);
        Set<Modifier> mods = EnumSet.copyOf(method.getModifiers());        
        mods.remove(Modifier.ABSTRACT);
        
        List<VariableTree> params = new ArrayList<VariableTree>();
        List<ExpressionTree> args = new ArrayList<ExpressionTree>();
        
        Iterator<? extends VariableElement> it = method.getParameters().iterator();
        Iterator<? extends TypeMirror> tIt = methodType.getParameterTypes().iterator();
        while(it.hasNext() && tIt.hasNext()) {
            VariableElement ve = it.next();
            params.add(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), ve.getSimpleName(), make.Type(tIt.next()), null));
            args.add(make.Identifier(ve.getSimpleName()));
        }

        ExpressionTree exp = make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(make.Identifier(delegate.getSimpleName()), method.getSimpleName()), args);
        StatementTree stmt = method.getReturnType().getKind() == TypeKind.VOID ? make.ExpressionStatement(exp) : make.Return(exp);
        BlockTree body = make.Block(Collections.singletonList(stmt), false);
        
        List<ExpressionTree> throwsClause = new ArrayList<ExpressionTree>();
        for (TypeMirror tm : methodType.getThrownTypes())
            throwsClause.add((ExpressionTree)wc.getTreeMaker().Type(tm));
        
        return make.Method(make.Modifiers(mods), method.getSimpleName(), make.Type(methodType.getReturnType()), Collections.<TypeParameterTree>emptyList(), params, throwsClause, body, null);
    }
}
