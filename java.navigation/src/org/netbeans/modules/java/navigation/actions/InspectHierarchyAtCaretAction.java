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

package org.netbeans.modules.java.navigation.actions;

import com.sun.source.util.TreePath;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import org.netbeans.api.editor.EditorRegistry;

import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.editor.BaseAction;

import org.netbeans.editor.ext.ExtKit;
import org.netbeans.modules.java.navigation.JavaHierarchy;

import org.openide.filesystems.FileObject;

import org.openide.util.NbBundle;

import java.io.IOException;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.ui.OpenProjects;

/**
 * This actions shows the hierarchy of the type of the element under the caret
 * in a popup window.
 *
 * @author Sandip Chitale (Sandip.Chitale@Sun.Com)
 */
public final class InspectHierarchyAtCaretAction extends BaseAction {

    private static final String INSPECT_HIERARCHY_AT_CARET = "inspect-hierarchy-at-caret"; // NOI18N
    private static final String INSPECT_HIERARCHY_AT_CARET_POPUP = 
            INSPECT_HIERARCHY_AT_CARET + "-popup"; // NOI18N

    /**
     *
     */
    public InspectHierarchyAtCaretAction() {
        super(NbBundle.getMessage(InspectMembersAtCaretAction.class, INSPECT_HIERARCHY_AT_CARET), 0);

        putValue(ACTION_COMMAND_KEY, INSPECT_HIERARCHY_AT_CARET);
        putValue(SHORT_DESCRIPTION, getValue(NAME));
        putValue(ExtKit.TRIMMED_TEXT,getValue(NAME));
        putValue(POPUP_MENU_TEXT, NbBundle.getMessage(InspectMembersAtCaretAction.class, 
                                                      INSPECT_HIERARCHY_AT_CARET_POPUP));

        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    @Override
    public boolean isEnabled() {
        if ( EditorRegistry.lastFocusedComponent() == null ||
             !EditorRegistry.lastFocusedComponent().isShowing() ) {
             return false;
        }
        return OpenProjects.getDefault().getOpenProjects().length > 0;        
    }
    
    public void actionPerformed(ActionEvent evt, final JTextComponent target) {
        if (target == null) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        JavaSource javaSource = JavaSource.forDocument(target.getDocument());

        if (javaSource == null) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        try {
            javaSource.runUserActionTask(new Task<CompilationController>() {

                    public void run(
                        CompilationController compilationController)
                        throws IOException {
                        // Move to resolved phase
                        compilationController.toPhase(Phase.ELEMENTS_RESOLVED);

                        // Get document if open
                        Document document = compilationController.getDocument();

                        if (document != null) {
                            // Get Caret position
                            int dot = target.getCaret().getDot();

                            // Find the TreePath for the caret position
                            TreePath tp = compilationController.getTreeUtilities()
                                                               .pathFor(dot);

                            // Get Element
                            Element element = compilationController.getTrees()
                                                                   .getElement(tp);

                            if (element instanceof TypeElement) {
                                FileObject elementFileObject = SourceUtils.getFile(element,
                                        compilationController.getClasspathInfo());

                                if (elementFileObject != null) {
                                    JavaHierarchy.show(elementFileObject, new Element[] {element}, compilationController);
                                }

                            } else if (element instanceof VariableElement) {
                                TypeMirror typeMirror = ((VariableElement) element).asType();

                                if (typeMirror.getKind() == TypeKind.DECLARED) {
                                    element = ((DeclaredType) typeMirror).asElement();

                                    if (element != null) {
                                        FileObject elementFileObject =
                                            SourceUtils.getFile(element,
                                                compilationController.getClasspathInfo());

                                        if (elementFileObject != null) {
                                            JavaHierarchy.show(elementFileObject, new Element[] {element}, compilationController);
                                        }
                                    }
                                }
                            } else if (element instanceof ExecutableElement) {
                                // Method
                                if (element.getKind() == ElementKind.METHOD) {
                                    TypeMirror typeMirror = ((ExecutableElement) element).getReturnType();

                                    if (typeMirror.getKind() == TypeKind.DECLARED) {
                                        element = ((DeclaredType) typeMirror).asElement();

                                        if (element != null) {
                                            FileObject elementFileObject =
                                                SourceUtils.getFile(element,
                                                    compilationController.getClasspathInfo());

                                            if (elementFileObject != null) {
                                                JavaHierarchy.show(elementFileObject, new Element[] {element}, compilationController);
                                            }
                                        }
                                    }
                                } else if (element.getKind() == ElementKind.CONSTRUCTOR) {
                                    element = element.getEnclosingElement();

                                    if (element != null) {
                                        FileObject elementFileObject =
                                            SourceUtils.getFile(element,
                                                compilationController.getClasspathInfo());

                                        if (elementFileObject != null) {
                                            JavaHierarchy.show(elementFileObject, new Element[] {element}, compilationController);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }, true);
        } catch (IOException e) {
            Logger.getLogger(InspectHierarchyAtCaretAction.class.getName()).log(Level.WARNING, e.getMessage(), e);
        }
    }
}
