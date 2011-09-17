/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import org.netbeans.spi.editor.codegen.CodeGenerator;
import com.sun.source.tree.ClassTree;
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
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.editor.codegen.ui.ElementNode;
import org.netbeans.modules.java.editor.codegen.ui.GetterSetterPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public class GetterSetterGenerator implements CodeGenerator {
    
    public static class Factory implements CodeGenerator.Factory {
        
        private static final String ERROR = "<error>"; //NOI18N

        public List<? extends CodeGenerator> create(Lookup context) {
            ArrayList<CodeGenerator> ret = new ArrayList<CodeGenerator>();
            JTextComponent component = context.lookup(JTextComponent.class);
            CompilationController controller = context.lookup(CompilationController.class);
            TreePath path = context.lookup(TreePath.class);
            path = path != null ? Utilities.getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, path) : null;
            if (component == null || controller == null || path == null)
                return ret;
            try {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            } catch (IOException ioe) {
                return ret;
            }
            Elements elements = controller.getElements();
            TypeElement typeElement = (TypeElement)controller.getTrees().getElement(path);
            if (typeElement == null || !typeElement.getKind().isClass())
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
                ElementNode.Description description = ElementNode.Description.create(controller, variableElement, null, true, false);
                boolean hasGetter = GeneratorUtils.hasGetter(controller, typeElement, variableElement, methods);
                boolean hasSetter = variableElement.getModifiers().contains(Modifier.FINAL) || GeneratorUtils.hasSetter(controller, typeElement, variableElement, methods);
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
                    descriptions.add(ElementNode.Description.create(controller, entry.getKey(), entry.getValue(), false, false));
                Collections.reverse(descriptions);
                ret.add(new GetterSetterGenerator(component, ElementNode.Description.create(controller, typeElement, descriptions, false, false), GeneratorUtils.GETTERS_ONLY));
            }
            if (!sDescriptions.isEmpty()) {
                List<ElementNode.Description> descriptions = new ArrayList<ElementNode.Description>();
                for (Map.Entry<Element, List<ElementNode.Description>> entry : sDescriptions.entrySet())
                    descriptions.add(ElementNode.Description.create(controller, entry.getKey(), entry.getValue(), false, false));
                Collections.reverse(descriptions);
                ret.add(new GetterSetterGenerator(component, ElementNode.Description.create(controller, typeElement, descriptions, false, false), GeneratorUtils.SETTERS_ONLY));
            }
            if (!gsDescriptions.isEmpty()) {
                List<ElementNode.Description> descriptions = new ArrayList<ElementNode.Description>();
                for (Map.Entry<Element, List<ElementNode.Description>> entry : gsDescriptions.entrySet())
                    descriptions.add(ElementNode.Description.create(controller, entry.getKey(), entry.getValue(), false, false));
                Collections.reverse(descriptions);
                ret.add(new GetterSetterGenerator(component, ElementNode.Description.create(controller, typeElement, descriptions, false, false), 0));
            }
            return ret;
        }
    }

    private JTextComponent component;
    private ElementNode.Description description;
    private int type;

    /** Creates a new instance of GetterSetterGenerator */
    private GetterSetterGenerator(JTextComponent component, ElementNode.Description description, int type) {
        this.component = component;
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

    public void invoke() {
        final int caretOffset = component.getCaretPosition();
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
                    ModificationResult mr = js.runModificationTask(new Task<WorkingCopy>() {
                        public void run(WorkingCopy copy) throws IOException {
                            copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                            Element e = description.getElementHandle().resolve(copy);
                            TreePath path = e != null ? copy.getTrees().getPath(e) : copy.getTreeUtilities().pathFor(caretOffset);
                            path = Utilities.getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, path);
                            if (path == null) {
                                String message = NbBundle.getMessage(GetterSetterGenerator.class, "ERR_CannotFindOriginalClass"); //NOI18N
                                org.netbeans.editor.Utilities.setStatusBoldText(component, message);
                            } else {
                                int idx = GeneratorUtils.findClassMemberIndex(copy, (ClassTree)path.getLeaf(), caretOffset);
                                ArrayList<VariableElement> variableElements = new ArrayList<VariableElement>();
                                for (ElementHandle<? extends Element> elementHandle : panel.getVariables()) {
                                    VariableElement elem = (VariableElement)elementHandle.resolve(copy);
                                    if (elem == null) {
                                        String message = NbBundle.getMessage(GetterSetterGenerator.class, "ERR_CannotFindOriginalMember"); //NOI18N
                                        org.netbeans.editor.Utilities.setStatusBoldText(component, message);
                                        return;
                                    }
                                    variableElements.add(elem);
                                }
                                GeneratorUtils.generateGettersAndSetters(copy, path, variableElements, type, idx);
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
}
