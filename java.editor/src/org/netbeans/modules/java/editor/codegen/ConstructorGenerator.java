/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.java.editor.codegen;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.awt.Dialog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.editor.codegen.ui.ConstructorPanel;
import org.netbeans.modules.java.editor.codegen.ui.ElementNode;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public class ConstructorGenerator implements CodeGenerator {

    public static class Factory implements CodeGenerator.Factory {
        
        public List<? extends CodeGenerator> create(Lookup context) {
            ArrayList<CodeGenerator> ret = new ArrayList<CodeGenerator>();
            JTextComponent component = context.lookup(JTextComponent.class);
            CompilationController controller = context.lookup(CompilationController.class);
            TreePath path = context.lookup(TreePath.class);
            path = path != null ? Utilities.getPathElementOfKind(Tree.Kind.CLASS, path) : null;
            if (component == null || controller == null || path == null)
                return ret;
            try {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            } catch (IOException ioe) {
                return ret;
            }
            TypeElement typeElement = (TypeElement)controller.getTrees().getElement(path);
            if (typeElement == null || !typeElement.getKind().isClass() || NestingKind.ANONYMOUS.equals(typeElement.getNestingKind()))
                return ret;
            final Set<VariableElement> initializedFields = new LinkedHashSet<VariableElement>();
            final Set<VariableElement> uninitializedFields = new LinkedHashSet<VariableElement>();
            final List<ExecutableElement> constructors = new ArrayList<ExecutableElement>();
            final List<ExecutableElement> inheritedConstructors = new ArrayList<ExecutableElement>();
            TypeMirror superClassType = typeElement.getSuperclass();
            TypeElement superClass = null;
            if (superClassType.getKind() == TypeKind.DECLARED) {
                superClass = (TypeElement) ((DeclaredType) superClassType).asElement();
                for (ExecutableElement executableElement : ElementFilter.constructorsIn(superClass.getEnclosedElements())) {
                    inheritedConstructors.add(executableElement);
                }
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
            if (constructorHandle != null || constructorDescription != null || fieldsDescription != null)
                ret.add(new ConstructorGenerator(component, constructorHandle, constructorDescription, fieldsDescription));
            return ret;
        }
    }

    private JTextComponent component;
    private ElementHandle<? extends Element> constructorHandle;
    private ElementNode.Description constructorDescription;
    private ElementNode.Description fieldsDescription;
    
    /** Creates a new instance of ConstructorGenerator */
    private ConstructorGenerator(JTextComponent component, ElementHandle<? extends Element> constructorHandle, ElementNode.Description constructorDescription, ElementNode.Description fieldsDescription) {
        this.component = component;
        this.constructorHandle = constructorHandle;
        this.constructorDescription = constructorDescription;
        this.fieldsDescription = fieldsDescription;
    }

    public String getDisplayName() {
        return org.openide.util.NbBundle.getMessage(ConstructorGenerator.class, "LBL_constructor"); //NOI18N
    }

    public void invoke() {
        final List<ElementHandle<? extends Element>> fieldHandles;
        final List<ElementHandle<? extends Element>> constrHandles;
        final int caretOffset = component.getCaretPosition();

        if (constructorDescription != null || fieldsDescription != null) {
            ConstructorPanel panel = new ConstructorPanel(constructorDescription, fieldsDescription);
            DialogDescriptor dialogDescriptor = GeneratorUtils.createDialogDescriptor(panel, NbBundle.getMessage(ConstructorGenerator.class, "LBL_generate_constructor")); //NOI18N
            Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
            dialog.setVisible(true);
            if (dialogDescriptor.getValue() != dialogDescriptor.getDefaultValue())
                return;
            if (constructorHandle == null) {
                constrHandles = panel.getInheritedConstructors();
            } else {
                constrHandles = null;
            }
            fieldHandles = panel.getVariablesToInitialize();
        } else {
            fieldHandles = null;
            constrHandles = null;
        }
        JavaSource js = JavaSource.forDocument(component.getDocument());
        if (js != null) {
            try {
                ModificationResult mr = js.runModificationTask(new Task<WorkingCopy>() {
                    public void run(WorkingCopy copy) throws IOException {
                        copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        TreePath path = copy.getTreeUtilities().pathFor(caretOffset);
                        path = Utilities.getPathElementOfKind(Tree.Kind.CLASS, path);
                        int idx = GeneratorUtils.findClassMemberIndex(copy, (ClassTree)path.getLeaf(), caretOffset);
                        ArrayList<VariableElement> variableElements = new ArrayList<VariableElement>();
                        if (fieldHandles != null) {
                            for (ElementHandle<? extends Element> elementHandle : fieldHandles) {
                                VariableElement field = (VariableElement) elementHandle.resolve(copy);
                                if (field == null)
                                    return;
                                variableElements.add(field);
                            }
                        }
                        if (constrHandles != null && !constrHandles.isEmpty()) {
                            ArrayList<ExecutableElement> constrElements = new ArrayList<ExecutableElement>();
                            for (ElementHandle<? extends Element> elementHandle : constrHandles) {
                                ExecutableElement constr = (ExecutableElement)elementHandle.resolve(copy);
                                if (constr == null)
                                    return;
                                constrElements.add(constr);
                            }
                            GeneratorUtils.generateConstructors(copy, path, variableElements, constrElements, idx);
                        } else {
                            GeneratorUtils.generateConstructor(copy, path, variableElements, constructorHandle != null ? (ExecutableElement)constructorHandle.resolve(copy) : null, idx);
                        }    
                    }
                });
                GeneratorUtils.guardedCommit(component, mr);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
