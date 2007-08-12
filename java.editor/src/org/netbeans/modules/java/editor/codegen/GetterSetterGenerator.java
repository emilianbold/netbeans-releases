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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.awt.Dialog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.editor.codegen.ui.ElementNode;
import org.netbeans.modules.java.editor.codegen.ui.GetterSetterPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public class GetterSetterGenerator implements CodeGenerator {
    
    public static class Factory implements CodeGenerator.Factory {
        
        private static final String ERROR = "<error>"; //NOI18N
        
        Factory() {
        }

        public Iterable<? extends CodeGenerator> create(CompilationController controller, TreePath path) throws IOException {
            List<CodeGenerator> ret = new ArrayList<CodeGenerator>();
            path = Utilities.getPathElementOfKind(Tree.Kind.CLASS, path);
            if (path == null)
                return ret;
            controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            Elements elements = controller.getElements();
            TypeElement typeElement = (TypeElement)controller.getTrees().getElement(path);
            if (!typeElement.getKind().isClass())
                return ret;
            Map<String, List<ExecutableElement>> methods = new HashMap<String, List<ExecutableElement>>();
            for (ExecutableElement method : ElementFilter.methodsIn(elements.getAllMembers(typeElement))) {
                List<ExecutableElement> l = methods.get(method.getSimpleName().toString());
                if (l == null) {
                    l = new ArrayList<ExecutableElement>();
                    methods.put(method.getSimpleName().toString(), l);
                }
                l.add(method);
            }
            Map<Element, List<ElementNode.Description>> gDescriptions = new LinkedHashMap<Element, List<ElementNode.Description>>();
            Map<Element, List<ElementNode.Description>> sDescriptions = new LinkedHashMap<Element, List<ElementNode.Description>>();
            Map<Element, List<ElementNode.Description>> gsDescriptions = new LinkedHashMap<Element, List<ElementNode.Description>>();
            for (VariableElement variableElement : ElementFilter.fieldsIn(elements.getAllMembers(typeElement))) {
                if (ERROR.contentEquals(variableElement.getSimpleName()))
                    continue;
                ElementNode.Description description = ElementNode.Description.create(variableElement, null, true, false);
                boolean hasGetter = GeneratorUtils.hasGetter(controller, variableElement, methods);
                boolean hasSetter = variableElement.getModifiers().contains(Modifier.FINAL) || GeneratorUtils.hasSetter(controller, variableElement, methods);
                if (!hasGetter) {
                    List<ElementNode.Description> descriptions = gDescriptions.get(variableElement.getEnclosingElement());
                    if (descriptions == null) {
                        descriptions = new ArrayList<ElementNode.Description>();
                        gDescriptions.put(variableElement.getEnclosingElement(), descriptions);
                    }
                    descriptions.add(description);
                }
                if (!hasSetter) {
                    List<ElementNode.Description> descriptions = sDescriptions.get(variableElement.getEnclosingElement());
                    if (descriptions == null) {
                        descriptions = new ArrayList<ElementNode.Description>();
                        sDescriptions.put(variableElement.getEnclosingElement(), descriptions);
                    }
                    descriptions.add(description);
                }
                if (!hasGetter && !hasSetter) {
                    List<ElementNode.Description> descriptions = gsDescriptions.get(variableElement.getEnclosingElement());
                    if (descriptions == null) {
                        descriptions = new ArrayList<ElementNode.Description>();
                        gsDescriptions.put(variableElement.getEnclosingElement(), descriptions);
                    }
                    descriptions.add(description);
                }
            }
            if (!gDescriptions.isEmpty()) {
                List<ElementNode.Description> descriptions = new ArrayList<ElementNode.Description>();
                for (Map.Entry<Element, List<ElementNode.Description>> entry : gDescriptions.entrySet())
                    descriptions.add(ElementNode.Description.create(entry.getKey(), entry.getValue(), false, false));
                Collections.reverse(descriptions);
                ret.add(new GetterSetterGenerator(ElementNode.Description.create(typeElement, descriptions, false, false), GeneratorUtils.GETTERS_ONLY));
            }
            if (!sDescriptions.isEmpty()) {
                List<ElementNode.Description> descriptions = new ArrayList<ElementNode.Description>();
                for (Map.Entry<Element, List<ElementNode.Description>> entry : sDescriptions.entrySet())
                    descriptions.add(ElementNode.Description.create(entry.getKey(), entry.getValue(), false, false));
                Collections.reverse(descriptions);
                ret.add(new GetterSetterGenerator(ElementNode.Description.create(typeElement, descriptions, false, false), GeneratorUtils.SETTERS_ONLY));
            }
            if (!gsDescriptions.isEmpty()) {
                List<ElementNode.Description> descriptions = new ArrayList<ElementNode.Description>();
                for (Map.Entry<Element, List<ElementNode.Description>> entry : gsDescriptions.entrySet())
                    descriptions.add(ElementNode.Description.create(entry.getKey(), entry.getValue(), false, false));
                Collections.reverse(descriptions);
                ret.add(new GetterSetterGenerator(ElementNode.Description.create(typeElement, descriptions, false, false), 0));
            }
            return ret;
        }
    }

    private ElementNode.Description description;
    private int type;

    /** Creates a new instance of GetterSetterGenerator */
    private GetterSetterGenerator(ElementNode.Description description, int type) {
        this.description = description;
        this.type = type;
    }

    public String getDisplayName() {
        if (type == GeneratorUtils.GETTERS_ONLY)
            return org.openide.util.NbBundle.getMessage(GetterSetterGenerator.class, "LBL_getter"); //NOI18N
        if (type == GeneratorUtils.SETTERS_ONLY)
            return org.openide.util.NbBundle.getMessage(GetterSetterGenerator.class, "LBL_setter"); //NOI18N
        return org.openide.util.NbBundle.getMessage(GetterSetterGenerator.class, "LBL_getter_and_setter"); //NOI18N
    }

    public void invoke(JTextComponent component) {
        final GetterSetterPanel panel = new GetterSetterPanel(description, type);
        String title;
        if (type == GeneratorUtils.GETTERS_ONLY)
            title = NbBundle.getMessage(ConstructorGenerator.class, "LBL_generate_getter"); //NOI18N
        else if (type == GeneratorUtils.SETTERS_ONLY)
            title = NbBundle.getMessage(ConstructorGenerator.class, "LBL_generate_setter"); //NOI18N
        else
            title = NbBundle.getMessage(ConstructorGenerator.class, "LBL_generate_getter_and_setter"); //NOI18N
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
                            ArrayList<VariableElement> variableElements = new ArrayList<VariableElement>();
                            for (ElementHandle<? extends Element> elementHandle : panel.getVariables())
                                variableElements.add((VariableElement)elementHandle.resolve(copy));
                            GeneratorUtils.generateGettersAndSetters(copy, path, variableElements, type, idx);
                        }
                    }).commit();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
}
