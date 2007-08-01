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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.editor.codegen.ui.ConstructorPanel;
import org.netbeans.modules.java.editor.codegen.ui.ElementNode;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public class ConstructorGenerator implements CodeGenerator {

    public static class Factory implements CodeGenerator.Factory {
        
        Factory() {            
        }
        
        public Iterable<? extends CodeGenerator> create(CompilationController controller, TreePath path) throws IOException {
            path = Utilities.getPathElementOfKind(Tree.Kind.CLASS, path);
            if (path == null)
                return Collections.emptySet();
            controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            TypeElement typeElement = (TypeElement)controller.getTrees().getElement(path);
            if (!typeElement.getKind().isClass())
                return Collections.emptySet();
            final Set<VariableElement> initializedFields = new LinkedHashSet<VariableElement>();
            final Set<VariableElement> uninitializedFields = new LinkedHashSet<VariableElement>();
            final List<ExecutableElement> constructors = new ArrayList<ExecutableElement>();
            final List<ExecutableElement> inheritedConstructors = new ArrayList<ExecutableElement>();
            TypeElement superClass = (TypeElement)((DeclaredType)typeElement.getSuperclass()).asElement();
            for (ExecutableElement executableElement : ElementFilter.constructorsIn(superClass.getEnclosedElements())) {
                inheritedConstructors.add(executableElement);
            }
            GeneratorUtils.scanForFieldsAndConstructors(controller, path, initializedFields, uninitializedFields, constructors);
            ElementHandle<? extends Element> constructorHandle = null;
            ElementNode.Description constructorDescription = null;
            if (typeElement.getKind() != ElementKind.ENUM && inheritedConstructors.size() == 1) {
                constructorHandle = ElementHandle.create(inheritedConstructors.get(0));
            } else if (inheritedConstructors.size() > 1) {
                List<ElementNode.Description> constructorDescriptions = new ArrayList<ElementNode.Description>();
                for (ExecutableElement constructorElement : inheritedConstructors)
                    constructorDescriptions.add(ElementNode.Description.create(constructorElement, null, true, false));
                constructorDescription = ElementNode.Description.create(superClass, constructorDescriptions, false, false);
            }
            ElementNode.Description fieldsDescription = null;
            if (!uninitializedFields.isEmpty()) {
                List<ElementNode.Description> fieldDescriptions = new ArrayList<ElementNode.Description>();
                for (VariableElement variableElement : uninitializedFields)
                    fieldDescriptions.add(ElementNode.Description.create(variableElement, null, true, false));
                fieldsDescription = ElementNode.Description.create(typeElement, fieldDescriptions, false, false);
            }
            if (constructorHandle == null && constructorDescription == null && fieldsDescription == null)
                return Collections.emptySet();
            return Collections.singleton(new ConstructorGenerator(constructorHandle, constructorDescription, fieldsDescription));
        }
    }

    private ElementHandle<? extends Element> constructorHandle;
    private ElementNode.Description constructorDescription;
    private ElementNode.Description fieldsDescription;
    
    /** Creates a new instance of ConstructorGenerator */
    private ConstructorGenerator(ElementHandle<? extends Element> constructorHandle, ElementNode.Description constructorDescription, ElementNode.Description fieldsDescription) {
        this.constructorHandle = constructorHandle;
        this.constructorDescription = constructorDescription;
        this.fieldsDescription = fieldsDescription;
    }

    public String getDisplayName() {
        return org.openide.util.NbBundle.getMessage(ConstructorGenerator.class, "LBL_constructor"); //NOI18N
    }

    public void invoke(JTextComponent component) {
        final List<ElementHandle<? extends Element>> fieldHandles;
        if (constructorDescription != null || fieldsDescription != null) {
            ConstructorPanel panel = new ConstructorPanel(constructorDescription, fieldsDescription);
            DialogDescriptor dialogDescriptor = GeneratorUtils.createDialogDescriptor(panel, NbBundle.getMessage(ConstructorGenerator.class, "LBL_generate_constructor")); //NOI18N
            Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
            dialog.setVisible(true);
            if (dialogDescriptor.getValue() != dialogDescriptor.getDefaultValue())
                return;
            if (constructorHandle == null)
                constructorHandle = panel.getInheritedConstructor();
            fieldHandles = panel.getVariablesToInitialize();
        } else {
            fieldHandles = null;
        }
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
                        if (fieldHandles != null) {
                            for (ElementHandle<? extends Element> elementHandle : fieldHandles)
                                variableElements.add((VariableElement)elementHandle.resolve(copy));
                        }
                        GeneratorUtils.generateConstructor(copy, path, variableElements, constructorHandle != null ? (ExecutableElement)constructorHandle.resolve(copy) : null, idx);
                    }
                }).commit();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
