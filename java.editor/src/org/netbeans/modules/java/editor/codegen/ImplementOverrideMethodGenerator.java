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

import com.sun.source.util.TreePath;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.swing.text.JTextComponent;

import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.editor.codegen.ui.ElementNode;
import org.netbeans.modules.java.editor.codegen.ui.ImplementOverridePanel;
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
public class ImplementOverrideMethodGenerator implements CodeGenerator {

    public static class Factory implements CodeGenerator.Factory {

        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            ArrayList<CodeGenerator> ret = new ArrayList<>();
            JTextComponent component = context.lookup(JTextComponent.class);
            CompilationController controller = context.lookup(CompilationController.class);
            if (component == null || controller == null) {
                return ret;
            }
            TreePath path = context.lookup(TreePath.class);
            path = controller.getTreeUtilities().getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, path);
            if (path == null) {
                return ret;
            }
            try {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            } catch (IOException ioe) {
                return ret;
            }
            TypeElement typeElement = (TypeElement)controller.getTrees().getElement(path);
            if (typeElement == null || typeElement.getKind() == ElementKind.ANNOTATION_TYPE) {
                return ret;
            }
            ElementUtilities eu = controller.getElementUtilities();
            if (typeElement.getKind().isClass() || typeElement.getKind().isInterface() && SourceVersion.RELEASE_8.compareTo(controller.getSourceVersion()) <= 0) {
                Map<Element, List<ElementNode.Description>> map = new LinkedHashMap<>();
                for (ExecutableElement method : eu.findUnimplementedMethods(typeElement)) {
                    List<ElementNode.Description> descriptions = map.get(method.getEnclosingElement());
                    if (descriptions == null) {
                        descriptions = new ArrayList<>();
                        map.put(method.getEnclosingElement(), descriptions);
                    }
                    descriptions.add(ElementNode.Description.create(controller, method, null, true, false));
                }
                List<ElementNode.Description> implementDescriptions = new ArrayList<>();
                for (Map.Entry<Element, List<ElementNode.Description>> entry : map.entrySet()) {
                    implementDescriptions.add(ElementNode.Description.create(controller, entry.getKey(), entry.getValue(), false, false));
                }
                if (!implementDescriptions.isEmpty()) {
                    ret.add(new ImplementOverrideMethodGenerator(component, ElementHandle.create(typeElement), ElementNode.Description.create(implementDescriptions), true));
                }
            }
            if (typeElement.getKind().isClass() || typeElement.getKind().isInterface()) {
                Map<Element, List<ElementNode.Description>> map = new LinkedHashMap<>();
                ArrayList<Element> orderedElements = new ArrayList<>();
                for (ExecutableElement method : eu.findOverridableMethods(typeElement)) {
                    List<ElementNode.Description> descriptions = map.get(method.getEnclosingElement());
                    if (descriptions == null) {
                        descriptions = new ArrayList<>();
                        Element e = method.getEnclosingElement();
                        map.put(e, descriptions);
                        if( !orderedElements.contains( e ) ) {
                            orderedElements.add( e );
                        }
                    }
                    descriptions.add(ElementNode.Description.create(controller, method, null, true, false));
                }
                List<ElementNode.Description> overrideDescriptions = new ArrayList<>();
                for (Element e : orderedElements) {
                    overrideDescriptions.add(ElementNode.Description.create(controller, e, map.get( e ), false, false));
                }
                if (!overrideDescriptions.isEmpty()) {
                    ret.add(new ImplementOverrideMethodGenerator(component, ElementHandle.create(typeElement), ElementNode.Description.create(overrideDescriptions), false));
                }
            }
            return ret;
        }
    }

    private final JTextComponent component;
    private final ElementHandle<TypeElement> handle;
    private final ElementNode.Description description;
    private final boolean isImplement;
    
    /** Creates a new instance of OverrideMethodGenerator */
    private ImplementOverrideMethodGenerator(JTextComponent component, ElementHandle<TypeElement> handle, ElementNode.Description description, boolean isImplement) {
        this.component = component;
        this.handle = handle;
        this.description = description;
        this.isImplement = isImplement;
    }

    @Override
    public String getDisplayName() {
        return org.openide.util.NbBundle.getMessage(ImplementOverrideMethodGenerator.class, isImplement ? "LBL_implement_method" : "LBL_override_method"); //NOI18N
    }

    @Override
    public void invoke() {
        final ImplementOverridePanel panel = new ImplementOverridePanel(description, isImplement);
        final int caretOffset = component.getCaretPosition();
        final DialogDescriptor dialogDescriptor = GeneratorUtils.createDialogDescriptor(panel, 
                NbBundle.getMessage(ConstructorGenerator.class, isImplement ?  "LBL_generate_implement" : "LBL_generate_override")); //NOI18N
        panel.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                List<ElementHandle<? extends Element>> meths = panel.getSelectedMethods();
                dialogDescriptor.setValid(meths != null && !meths.isEmpty());
            }
        });
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.setVisible(true);
        if (dialogDescriptor.getValue() == dialogDescriptor.getDefaultValue()) {
            JavaSource js = JavaSource.forDocument(component.getDocument());
            if (js != null) {
                try {
                    ModificationResult mr = js.runModificationTask(new Task<WorkingCopy>() {
                        @Override
                        public void run(WorkingCopy copy) throws IOException {
                            copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                            Element e = handle.resolve(copy);
                            TreePath path = e != null ? copy.getTrees().getPath(e) : copy.getTreeUtilities().pathFor(caretOffset);
                            path = copy.getTreeUtilities().getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, path);
                            if (path == null) {
                                String message = NbBundle.getMessage(ImplementOverrideMethodGenerator.class, "ERR_CannotFindOriginalClass"); //NOI18N
                                org.netbeans.editor.Utilities.setStatusBoldText(component, message);
                            } else {
                                ArrayList<ExecutableElement> methodElements = new ArrayList<>();
                                for (ElementHandle<? extends Element> elementHandle : panel.getSelectedMethods()) {
                                    ExecutableElement methodElement = (ExecutableElement)elementHandle.resolve(copy);
                                    if (methodElement != null) {
                                        methodElements.add(methodElement);
                                    }
                                }
                                if (!methodElements.isEmpty()) {
                                    if (isImplement) {
                                        GeneratorUtils.generateAbstractMethodImplementations(copy, path, methodElements, caretOffset);
                                    } else {
                                        GeneratorUtils.generateMethodOverrides(copy, path, methodElements, caretOffset);
                                    }
                                }
                            }
                        }
                    });
                    GeneratorUtils.guardedCommit(component, mr);
                    int span[] = mr.getSpan("methodBodyTag"); // NOI18N
                    if(span != null) {
                        component.setSelectionStart(span[0]);
                        component.setSelectionEnd(span[1]);
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
}
