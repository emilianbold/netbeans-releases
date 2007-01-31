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
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.awt.Dialog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CancellableTask;
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
    
    private int type;

    /** Creates a new instance of GetterSetterGenerator */
    GetterSetterGenerator() {
        this(0);
    }
    
    GetterSetterGenerator(int type) {
        this.type = type;
    }

    public String getDisplayName() {
        if (type == GeneratorUtils.GETTERS_ONLY)
            return org.openide.util.NbBundle.getMessage(GetterSetterGenerator.class, "LBL_getter"); //NOI18N
        if (type == GeneratorUtils.SETTERS_ONLY)
            return org.openide.util.NbBundle.getMessage(GetterSetterGenerator.class, "LBL_setter"); //NOI18N
        return org.openide.util.NbBundle.getMessage(GetterSetterGenerator.class, "LBL_getter_and_setter"); //NOI18N
    }

    public boolean accept(TreePath path) {
        return Utilities.getPathElementOfKind(Tree.Kind.CLASS, path) != null;
    }

    public void invoke(JTextComponent component) {
        JavaSource js = JavaSource.forDocument(component.getDocument());
        if (js != null) {
            try {
                final int caretOffset = component.getCaretPosition();
                final ElementNode.Description[] description = new ElementNode.Description[1];
                js.runUserActionTask(new CancellableTask<CompilationController>() {
                    public void cancel() {
                    }
                    public void run(CompilationController controller) throws IOException {
                        controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        TreePath path = controller.getTreeUtilities().pathFor(caretOffset);
                        path = Utilities.getPathElementOfKind(Tree.Kind.CLASS, path);
                        if (path != null) {
                            TypeElement typeElement = (TypeElement)controller.getTrees().getElement(path);
                            List<ElementNode.Description> descriptions = new ArrayList<ElementNode.Description>();
                            for (VariableElement variableElement : ElementFilter.fieldsIn(typeElement.getEnclosedElements()))
                                descriptions.add(ElementNode.Description.create(variableElement, null));
                            description[0] = ElementNode.Description.create(typeElement, descriptions);
                        }
                    }
                }, true);
                if (description[0] != null) {
                    final GetterSetterPanel panel = new GetterSetterPanel(description[0], type);
                    String title;
                    if (type == GeneratorUtils.GETTERS_ONLY)
                        title = NbBundle.getMessage(ConstructorGenerator.class, "LBL_generate_getter"); //NOI18N
                    else if (type == GeneratorUtils.SETTERS_ONLY)
                        title = NbBundle.getMessage(ConstructorGenerator.class, "LBL_generate_setter"); //NOI18N
                    else
                        title = NbBundle.getMessage(ConstructorGenerator.class, "LBL_generate_getter_and_setter"); //NOI18N
                    DialogDescriptor dialogDescriptor = new DialogDescriptor(panel, title);
                    Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
                    dialog.setVisible(true);
                    if (dialogDescriptor.getValue() == DialogDescriptor.OK_OPTION) {
                        js.runModificationTask(new CancellableTask<WorkingCopy>() {
                            public void cancel() {
                            }
                            public void run(WorkingCopy copy) throws IOException {
                                copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                                TreePath path = copy.getTreeUtilities().pathFor(caretOffset);
                                path = Utilities.getPathElementOfKind(Tree.Kind.CLASS, path);
                                int idx = 0;
                                SourcePositions sourcePositions = copy.getTrees().getSourcePositions();
                                for (Tree tree : ((ClassTree)path.getLeaf()).getMembers()) {
                                    if (sourcePositions.getStartPosition(path.getCompilationUnit(), tree) < caretOffset)
                                        idx++;
                                    else
                                        break;
                                }
                                ArrayList<VariableElement> variableElements = new ArrayList<VariableElement>();
                                for (ElementHandle<? extends Element> elementHandle : panel.getVariables())
                                    variableElements.add((VariableElement)elementHandle.resolve(copy));
                                GeneratorUtils.generateGettersAndSetters(copy, path, variableElements, type, idx);
                            }
                        }).commit();
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

}
