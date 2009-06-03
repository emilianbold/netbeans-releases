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

package org.netbeans.modules.java.navigation;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.swing.JDialog;

import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Sandip Chitale (Sandip.Chitale@Sun.Com)
 */
public final class JavaHierarchy {

    /**
     * Show the hierarchy of the types in the fileObject.
     * 
     * @param fileObject 
     */
    public static void show(final FileObject fileObject) {
        if (fileObject != null) {
            JavaSource javaSource = JavaSource.forFileObject(fileObject);

            if (javaSource != null) {
                try {
                    javaSource.runUserActionTask(new Task<CompilationController>() {
                            public void run(
                                CompilationController compilationController)
                                throws Exception {
                                compilationController.toPhase(Phase.ELEMENTS_RESOLVED);

                                Trees trees = compilationController.getTrees();
                                CompilationUnitTree compilationUnitTree = compilationController.getCompilationUnit();
                                List<?extends Tree> typeDecls = compilationUnitTree.getTypeDecls();

                                Set<Element> elementsSet = new LinkedHashSet<Element>(typeDecls.size() + 1);

                                for (Tree tree : typeDecls) {
                                    Element element = trees.getElement(trees.getPath(compilationUnitTree, tree));

                                    if (element != null) {
                                        elementsSet.add(element);
                                    }
                                }

                                Element[] elements = elementsSet.toArray(JavaMembersModel.EMPTY_ELEMENTS_ARRAY);
                                show(fileObject, elements, compilationController);
                            }
                        }, true);

                    return;
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }
    }

    public static void show(FileObject fileObject, Element[] elements,
        CompilationController compilationController) {
        if (fileObject != null) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(JavaHierarchy.class, "LBL_WaitNode"));
            JDialog dialog = ResizablePopup.getDialog();
            String membersOf = "";
            if (elements != null && elements.length > 0) {
                List<? extends Element> elementsList = Arrays.<Element>asList(elements);
                if (elements[0].getKind() == ElementKind.PACKAGE && elements.length > 1) {
                    membersOf = elementsList.subList(1, elementsList.size()).toString();
                } else {
                    membersOf = elementsList.toString();
                }
            }
            String title = NbBundle.getMessage(JavaHierarchy.class, "TITLE_Hierarchy", membersOf);            
            dialog.setTitle(title); // NOI18N
            dialog.setContentPane(new JavaHierarchyPanel(fileObject, elements, compilationController));
            dialog.setVisible(true);
        }
    }    
}
