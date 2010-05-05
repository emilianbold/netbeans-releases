/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.refactoring.codegen;

import org.netbeans.modules.cnd.refactoring.support.CsmContext;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.refactoring.support.GeneratorUtils;
import java.awt.Dialog;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmConstructor;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.services.CsmInheritanceUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.refactoring.codegen.ui.ConstructorPanel;
import org.netbeans.modules.cnd.modelutil.ui.ElementNode;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Vladimir Voskresensky
 */
public class ConstructorGenerator implements CodeGenerator {

    public static class Factory implements CodeGenerator.Factory {

        public List<? extends CodeGenerator> create(Lookup context) {
            ArrayList<CodeGenerator> ret = new ArrayList<CodeGenerator>();
            if (!CsmRefactoringUtils.REFACTORING_EXTRA) {
                return ret;
            }
            JTextComponent component = context.lookup(JTextComponent.class);
            CsmContext path = context.lookup(CsmContext.class);
            if (component == null || path == null) {
                return ret;
            }
            CsmClass typeElement = path.getEnclosingClass();
            if (typeElement == null) {
                return ret;
            }
            final Set<CsmField> initializedFields = new LinkedHashSet<CsmField>();
            final Set<CsmField> uninitializedFields = new LinkedHashSet<CsmField>();
            final List<CsmConstructor> constructors = new ArrayList<CsmConstructor>();
            final List<CsmConstructor> inheritedConstructors = new ArrayList<CsmConstructor>();
            CsmClass superClass = null;
            // check base class
            for (CsmInheritance csmInheritance : typeElement.getBaseClasses()) {
                CsmClass baseClass = CsmInheritanceUtilities.getCsmClass(csmInheritance);
                if (baseClass != null) {
                    superClass = baseClass;
                    for (CsmMember member : baseClass.getMembers()) {
                        if (CsmKindUtilities.isConstructor(member) && CsmInheritanceUtilities.matchVisibility(member, csmInheritance.getVisibility())) {
                            inheritedConstructors.add((CsmConstructor)member);
                        }
                    }
                    // TODO: for now we stop on the first base class
                    break;
                }
            }
            GeneratorUtils.scanForFieldsAndConstructors(typeElement, initializedFields, uninitializedFields, constructors);
            CsmConstructor constructorHandle = null;
            ElementNode.Description constructorDescription = null;
            if (inheritedConstructors.size() == 1) {
                constructorHandle = inheritedConstructors.get(0);
            } else if (inheritedConstructors.size() > 1) {
                List<ElementNode.Description> constructorDescriptions = new ArrayList<ElementNode.Description>();
                for (CsmConstructor constructorElement : inheritedConstructors) {
                    constructorDescriptions.add(ElementNode.Description.create(constructorElement, null, true, false));
                }
                constructorDescription = ElementNode.Description.create(superClass, constructorDescriptions, false, false);
            }
            ElementNode.Description fieldsDescription = null;
            if (!uninitializedFields.isEmpty()) {
                List<ElementNode.Description> fieldDescriptions = new ArrayList<ElementNode.Description>();
                for (CsmField variableElement : uninitializedFields) {
                    fieldDescriptions.add(ElementNode.Description.create(variableElement, null, true, false));
                }
                fieldsDescription = ElementNode.Description.create(typeElement, fieldDescriptions, false, false);
            }
            if (constructorHandle != null || constructorDescription != null || fieldsDescription != null) {
                ret.add(new ConstructorGenerator(component, constructorHandle, constructorDescription, fieldsDescription));
            }
            return ret;
        }
    }
    private JTextComponent component;
    private CsmConstructor constructorHandle;
    private ElementNode.Description constructorDescription;
    private ElementNode.Description fieldsDescription;

    /** Creates a new instance of ConstructorGenerator */
    private ConstructorGenerator(JTextComponent component, CsmConstructor constructorHandle, ElementNode.Description constructorDescription, ElementNode.Description fieldsDescription) {
        this.component = component;
        this.constructorHandle = constructorHandle;
        this.constructorDescription = constructorDescription;
        this.fieldsDescription = fieldsDescription;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(ConstructorGenerator.class, "LBL_constructor"); //NOI18N
    }

    public void invoke() {
        final List<CsmField> fieldHandles;
        final List<CsmConstructor> constrHandles;
        if (constructorDescription != null || fieldsDescription != null) {
            ConstructorPanel panel = new ConstructorPanel(constructorDescription, fieldsDescription);
            DialogDescriptor dialogDescriptor = GeneratorUtils.createDialogDescriptor(panel, NbBundle.getMessage(ConstructorGenerator.class, "LBL_generate_constructor")); //NOI18N
            Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
            dialog.setVisible(true);
            if (dialogDescriptor.getValue() != dialogDescriptor.getDefaultValue()) {
                return;
            }
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
//        JavaSource js = JavaSource.forDocument(component.getDocument());
//        if (js != null) {
//            try {
//                final int caretOffset = component.getCaretPosition();
//                ModificationResult mr = js.runModificationTask(new Task<WorkingCopy>() {
//
//                    public void run(WorkingCopy copy) throws IOException {
//                        copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
//                        TreePath path = copy.getTreeUtilities().pathFor(caretOffset);
//                        path = Utilities.getPathElementOfKind(Tree.Kind.CLASS, path);
//                        int idx = GeneratorUtils.findClassMemberIndex(copy, (ClassTree) path.getLeaf(), caretOffset);
//                        ArrayList<VariableElement> variableElements = new ArrayList<VariableElement>();
//                        if (fieldHandles != null) {
//                            for (ElementHandle<? extends Element> elementHandle : fieldHandles) {
//                                variableElements.add((VariableElement) elementHandle.resolve(copy));
//                            }
//                        }
//                        if (constrHandles != null && !constrHandles.isEmpty()) {
//                            ArrayList<ExecutableElement> constrElements = new ArrayList<ExecutableElement>();
//                            for (ElementHandle<? extends Element> elementHandle : constrHandles) {
//                                constrElements.add((ExecutableElement) elementHandle.resolve(copy));
//                            }
//                            GeneratorUtils.generateConstructors(copy, path, variableElements, constrElements, idx);
//                        } else {
//                            GeneratorUtils.generateConstructor(copy, path, variableElements, constructorHandle != null ? (ExecutableElement) constructorHandle.resolve(copy) : null, idx);
//                        }
//                    }
//                });
//                GeneratorUtils.guardedCommit(component, mr);
//            } catch (IOException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        }
    }
}
