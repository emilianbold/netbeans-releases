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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.editor.codegen.ui.ElementNode;
import org.netbeans.modules.java.editor.codegen.ui.ImplementOverridePanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public class ImplementOverrideMethodGenerator implements CodeGenerator {

    public static class Factory implements CodeGenerator.Factory {
        
        Factory() {            
        }
        
        public Iterable<? extends CodeGenerator> create(CompilationController controller, TreePath path) throws IOException {
            List<CodeGenerator> ret = new ArrayList<CodeGenerator>();
            path = Utilities.getPathElementOfKind(Tree.Kind.CLASS, path);
            if (path == null)
                return ret;
            controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            TypeElement typeElement = (TypeElement)controller.getTrees().getElement(path);
            if (!typeElement.getKind().isClass())
                return Collections.emptySet();
            Map<Element, List<ElementNode.Description>> map = new LinkedHashMap<Element, List<ElementNode.Description>>();
            for (ExecutableElement method : GeneratorUtils.findUndefs(controller, typeElement)) {
                List<ElementNode.Description> descriptions = map.get(method.getEnclosingElement());
                if (descriptions == null) {
                    descriptions = new ArrayList<ElementNode.Description>();
                    map.put(method.getEnclosingElement(), descriptions);
                }
                descriptions.add(ElementNode.Description.create(method, null, true, false));
            }
            List<ElementNode.Description> implementDescriptions = new ArrayList<ElementNode.Description>();
            for (Map.Entry<Element, List<ElementNode.Description>> entry : map.entrySet())
                implementDescriptions.add(ElementNode.Description.create(entry.getKey(), entry.getValue(), false, false));
            if (!implementDescriptions.isEmpty())
                ret.add(new ImplementOverrideMethodGenerator(ElementNode.Description.create(implementDescriptions), true));
            map = new LinkedHashMap<Element, List<ElementNode.Description>>();
            ArrayList<Element> orderedElements = new ArrayList<Element>();
            for (ExecutableElement method : GeneratorUtils.findOverridable(controller, typeElement)) {
                List<ElementNode.Description> descriptions = map.get(method.getEnclosingElement());
                if (descriptions == null) {
                    descriptions = new ArrayList<ElementNode.Description>();
                    Element e = method.getEnclosingElement();
                    map.put(e, descriptions);
                    if( !orderedElements.contains( e ) )
                        orderedElements.add( e );
                }
                descriptions.add(ElementNode.Description.create(method, null, true, false));
            }
            List<ElementNode.Description> overrideDescriptions = new ArrayList<ElementNode.Description>();
            for (Element e : orderedElements)
                overrideDescriptions.add(ElementNode.Description.create(e, map.get( e ), false, false));
            if (!overrideDescriptions.isEmpty())
                ret.add(new ImplementOverrideMethodGenerator(ElementNode.Description.create(overrideDescriptions), false));
            return ret;
        }
    }
    
    private ElementNode.Description description;
    private boolean isImplement;
    
    /** Creates a new instance of OverrideMethodGenerator */
    private ImplementOverrideMethodGenerator(ElementNode.Description description, boolean isImplement) {
        this.description = description;
        this.isImplement = isImplement;
    }

    public String getDisplayName() {
        return org.openide.util.NbBundle.getMessage(ImplementOverrideMethodGenerator.class, isImplement ? "LBL_implement_method" : "LBL_override_method"); //NOI18N
    }

    public void invoke(JTextComponent component) {
        final ImplementOverridePanel panel = new ImplementOverridePanel(description, isImplement);
        DialogDescriptor dialogDescriptor = GeneratorUtils.createDialogDescriptor(panel, 
                NbBundle.getMessage(ConstructorGenerator.class, isImplement ?  "LBL_generate_implement" : "LBL_generate_override")); //NOI18N  //NOI18N
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
                            ArrayList<ExecutableElement> methodElements = new ArrayList<ExecutableElement>();
                            for (ElementHandle<? extends Element> elementHandle : panel.getSelectedMethods())
                                methodElements.add((ExecutableElement)elementHandle.resolve(copy));
                            if (isImplement)
                                GeneratorUtils.generateAbstractMethodImplementations(copy, path, methodElements, idx);
                            else
                                GeneratorUtils.generateMethodOverrides(copy, path, methodElements, idx);
                        }
                    }).commit();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
}
