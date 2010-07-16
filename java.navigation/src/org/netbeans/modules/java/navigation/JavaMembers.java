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

package org.netbeans.modules.java.navigation;

import java.util.ArrayList;
import org.netbeans.api.java.source.JavaSource;
import java.util.List;
import javax.lang.model.element.ElementKind;
import javax.swing.JDialog;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ElementHandle;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Sandip Chitale (Sandip.Chitale@Sun.Com)
 */
public final class JavaMembers {

    /**
     * Show the members of the types in the fileObject.
     * 
     * @param fileObject 
     */
    public static void show(final FileObject fileObject) {
        if (fileObject != null) {
            JavaSource javaSource = JavaSource.forFileObject(fileObject);
            if (javaSource != null) {
                String name = null;
                final ClassPath srcPath = ClassPath.getClassPath(fileObject, ClassPath.SOURCE);
                if (srcPath != null) {
                    name = srcPath.getResourceName(fileObject, '.', false); //NOI18N
                }
                if (name == null) {
                    name = "";  //NOI18N
                }
                showDialog(name, new JavaMembersPanel(fileObject), fileObject);
            }
        }
    }

    public static void show(FileObject fileObject, ElementHandle<?>[] elements) {
        if (fileObject != null) {
            String membersOf = "";
            if (elements != null && elements.length > 0) {
                List<String> namesList = new ArrayList<String>(elements.length);
                for (ElementHandle<?> handle : elements) {
                    namesList.add(handle.getQualifiedName());
                }
                if (elements[0].getKind() == ElementKind.PACKAGE && elements.length > 1) {
                    membersOf = namesList.subList(1, namesList.size()).toString();
                } else {
                    membersOf = namesList.toString();
                }
            }
            showDialog(membersOf, new JavaMembersPanel(fileObject, elements), fileObject);
        }
    }

    //<editor-fold desc="Private methods">
    private static void showDialog (final String membersOf, final JavaMembersPanel panel, FileObject file) {
        assert membersOf != null;
        assert panel != null;
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(JavaMembers.class, "LBL_WaitNode"));
            JDialog dialog = ResizablePopup.getDialog(file);

            String title = NbBundle.getMessage(JavaMembers.class, "TITLE_Members", membersOf);
            dialog.setTitle(title); // NOI18N
            dialog.setContentPane(panel);
            dialog.setVisible(true);
    }


    //</editor-fold>

}
