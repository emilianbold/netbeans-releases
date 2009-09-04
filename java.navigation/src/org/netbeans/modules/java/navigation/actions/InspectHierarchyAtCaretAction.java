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

package org.netbeans.modules.java.navigation.actions;

import com.sun.source.util.TreePath;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import org.netbeans.api.editor.EditorRegistry;

import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.editor.BaseAction;

import org.netbeans.editor.ext.ExtKit;
import org.netbeans.modules.editor.NbEditorUtilities;
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

import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.util.RequestProcessor;

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

    private RequestProcessor requestProcessor;

    public void actionPerformed(ActionEvent evt, final JTextComponent target) {
        if (target == null) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        final JavaSource javaSource = JavaSource.forDocument(target.getDocument());

        if (javaSource == null) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        if (requestProcessor == null)
            requestProcessor = new RequestProcessor ("SelectedNodesScheduler");
        requestProcessor.post (new Runnable () {
            public void run () {
                try {
                    javaSource.runUserActionTask(new Task<CompilationController>() {
                        public void run (
                            final CompilationController compilationController
                        ) throws IOException {
                            // Move to resolved phase
                            compilationController.toPhase (Phase.ELEMENTS_RESOLVED);

                            // Get document if open
                            Document document = compilationController.getDocument ();
                            if (document != null) {
                                // Get Caret position
                                int dot = target.getCaret().getDot();

                                // Find the TreePath for the caret position
                                final TreePath tp = compilationController.
                                    getTreeUtilities ().pathFor(dot);

                                // Get Element
                                Element element = compilationController.getTrees().getElement(tp);

                                final FileObject elementFileObject = NbEditorUtilities.
                                    getFileObject(document);
                                if (elementFileObject != null) {
                                    if (element instanceof TypeElement) {
                                        final Element _element = element;
                                        SwingUtilities.invokeLater (new Runnable () {
                                            public void run () {
                                                JavaHierarchy.show (
                                                    elementFileObject,
                                                    new Element[] {_element},
                                                    compilationController
                                                );
                                            }
                                        });
                                    } else if (element instanceof VariableElement) {
                                        TypeMirror typeMirror = ((VariableElement) element).asType();

                                        if (typeMirror.getKind() == TypeKind.DECLARED) {
                                            element = ((DeclaredType) typeMirror).asElement();

                                            if (element != null) {
                                                final Element _element = element;
                                                SwingUtilities.invokeLater (new Runnable () {
                                                    public void run () {
                                                        JavaHierarchy.show (
                                                            elementFileObject,
                                                            new Element[] {_element},
                                                            compilationController
                                                        );
                                                    }
                                                });
                                            }
                                        }
                                    } else if (element instanceof ExecutableElement) {
                                        // Method
                                        if (element.getKind() == ElementKind.METHOD) {
                                            TypeMirror typeMirror = ((ExecutableElement) element).
                                                getReturnType();

                                            if (typeMirror.getKind() == TypeKind.DECLARED) {
                                                element = ((DeclaredType) typeMirror).asElement();

                                                if (element != null) {
                                                    final Element _element = element;
                                                    SwingUtilities.invokeLater (new Runnable () {
                                                        public void run () {
                                                            JavaHierarchy.show (
                                                                elementFileObject,
                                                                new Element[] {_element},
                                                                compilationController
                                                            );
                                                        }
                                                    });
                                                }
                                            }
                                        } else if (element.getKind() == ElementKind.CONSTRUCTOR) {
                                            element = element.getEnclosingElement();

                                            if (element != null) {
                                                final Element _element = element;
                                                SwingUtilities.invokeLater (new Runnable () {
                                                    public void run () {
                                                        JavaHierarchy.show (
                                                            elementFileObject,
                                                            new Element[] {_element},
                                                            compilationController
                                                        );
                                                    }
                                                });
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
        });
    }
}
