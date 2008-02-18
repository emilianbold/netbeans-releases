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

package org.netbeans.modules.spring.beans.refactoring;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Andrei Badea
 */
public class SpringRefactorings {

    private static final Logger LOGGER = Logger.getLogger(SpringRefactorings.class.getName());

    private static final String JAVA_MIME_TYPE = "text/x-java"; // NOI18N

    public static boolean isJavaFile(FileObject fo) {
        return JAVA_MIME_TYPE.equals(fo.getMIMEType());
    }

    public static RenamedClassName getRenamedClassName(final TreePathHandle oldHandle, final JavaSource javaSource, final String newName) throws IOException {
        final RenamedClassName[] result = { null };
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController cc) throws Exception {
                Element element = oldHandle.resolveElement(cc);
                if (element == null || element.getKind() != ElementKind.CLASS) {
                    return;
                }
                String oldBinaryName = ElementUtilities.getBinaryName((TypeElement)element);
                String oldSimpleName = element.getSimpleName().toString();
                String newBinaryName = null;
                element = element.getEnclosingElement();
                if (element.getKind() == ElementKind.CLASS) {
                    newBinaryName = ElementUtilities.getBinaryName((TypeElement)element) + '$' + newName;
                } else if (element.getKind() == ElementKind.PACKAGE) {
                    String packageName = ((PackageElement)element).getQualifiedName().toString();
                    if (packageName.length() > 0) {
                        newBinaryName = packageName + '.' + newName;
                    } else {
                        newBinaryName = newName;
                    }
                } else {
                    LOGGER.log(Level.WARNING, "Enclosing element of {0} was neither class nor package", oldHandle);
                }
                result[0] = new RenamedClassName(oldSimpleName, oldBinaryName, newBinaryName);
            }
        }, true);
        return result[0];
    }

    public static String getPackageName(FileObject folder) {
        ClassPath cp = ClassPath.getClassPath(folder, ClassPath.SOURCE);
        if (cp != null) {
            return cp.getResourceName(folder, '.', false);
        }
        return null;
    }

    public static String getRenamedPackageName(FileObject folder, String newName) {
        FileObject parent = folder.getParent();
        if (parent == null) {
            return null;
        }
        ClassPath cp = ClassPath.getClassPath(parent, ClassPath.SOURCE);
        if (cp == null) {
            return null;
        }
        String parentName = cp.getResourceName(parent, '.', false);
        if (parentName == null) {
            return null;
        }
        if (parentName.length() > 0) {
            return parentName + '.' + newName;
        } else {
            return newName;
        }
    }

    public static final class RenamedClassName {

        private final String oldSimpleName;
        private final String oldBinaryName;
        private final String newBinaryName;

        public RenamedClassName(String oldSimpleName, String oldBinaryName, String newBinaryName) {
            this.oldSimpleName = oldSimpleName;
            this.oldBinaryName = oldBinaryName;
            this.newBinaryName = newBinaryName;
        }

        public String getOldSimpleName() {
            return oldSimpleName;
        }

        public String getOldBinaryName() {
            return oldBinaryName;
        }

        public String getNewBinaryName() {
            return newBinaryName;
        }
    }
}
