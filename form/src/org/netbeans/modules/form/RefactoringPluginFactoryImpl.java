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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.form;

import java.io.IOException;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.api.SingleCopyRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Entry point for refactoring, registered in META-INF/services. Whenever
 * a refactoring is about to start, createInstance is called where we analyze
 * the type of refactoring and attach RefactoringInfo object to the refactoring,
 * which can be later accessed from various places where we prepare or perform
 * the additional changes in forms and resources.
 * 
 *  @author Tomas Pavek
 */
public class RefactoringPluginFactoryImpl implements RefactoringPluginFactory {

    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        RefactoringInfo.ChangeType changeType = null;
        FileObject primaryFile = null;
        String oldName = null;

        // We must do some more analysis here, though it would be better to do
        // it later in the plugin's prepare method, but we can't be sure the
        // guarded handler is not called sooner from java plugin than our plugin.

        if (refactoring instanceof RenameRefactoring) {
            Lookup sourceLookup = refactoring.getRefactoringSource();
            FileObject file = sourceLookup.lookup(FileObject.class);
            NonRecursiveFolder pkgFolder = sourceLookup.lookup(NonRecursiveFolder.class);
            final TreePathHandle tpHandle = sourceLookup.lookup(TreePathHandle.class);
            // assumption: if file is being renamed (even as result of renaming
            // a class) then file != null, and if something inside the class
            // is renamed then file == null

            if (file != null && RefactoringInfo.isJavaFile(file)) {
                // renaming a java file within the same package
                // (can be a form, or a component used in a form, or both)
                 if (isOnSourceClasspath(file)) {
                    changeType = RefactoringInfo.ChangeType.CLASS_RENAME;
                    primaryFile = file;
                    oldName = file.getName();
                 }
            } else if (file == null && tpHandle != null) {
                // renaming an element inside a java file
                primaryFile = tpHandle.getFileObject();
                if (RefactoringInfo.isJavaFileOfForm(primaryFile)) {
                    JavaSource source = JavaSource.forFileObject(tpHandle.getFileObject());
                    final RefactoringInfo.ChangeType[] changeTypes = new RefactoringInfo.ChangeType[1];
                    final String[] oldNames = new String[1];
                    try {
                        source.runUserActionTask(new CancellableTask<CompilationController>() {
                            public void cancel() {
                            }
                            public void run(CompilationController controller) throws Exception {
                                controller.toPhase(JavaSource.Phase.RESOLVED);
                                Element el = tpHandle.resolveElement(controller);
                                if (el != null) {
                                    switch(el.getKind()) {
                                    case FIELD:
                                        changeTypes[0] = RefactoringInfo.ChangeType.VARIABLE_RENAME;
                                        break;
                                    case LOCAL_VARIABLE:
                                        Element parentEl = el.getEnclosingElement();
                                        if (parentEl.getKind() == ElementKind.METHOD
                                                && "initComponents".equals(parentEl.getSimpleName().toString()) // NOI18N
                                                && ((ExecutableElement)parentEl).getParameters().size() == 0) {
                                            changeTypes[0] = RefactoringInfo.ChangeType.VARIABLE_RENAME;
                                        }
                                        break;
                                    }
                                    // [should we also check if it really matches an existing component in the form?]
                                    oldNames[0] = el.getSimpleName().toString();
                                }
                            }
                        }, true);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    changeType = changeTypes[0];
                    oldName = oldNames[0];
                }
                // TBD: changing a property method of a component
            } else if (file != null && file.isFolder()) {
                // renaming a folder (incl. subfolders)
                if (isOnSourceClasspath(file)) {
                    changeType = RefactoringInfo.ChangeType.FOLDER_RENAME;
                    primaryFile = file;
                    oldName = file.getName();
                }
            } else if (pkgFolder != null) {
                // renaming a package (without subfolders)
                if (isOnSourceClasspath(pkgFolder.getFolder())) {
                    changeType = RefactoringInfo.ChangeType.PACKAGE_RENAME;
                    primaryFile = pkgFolder.getFolder();
                    oldName = primaryFile.getName();
                }
            }
        } else if (refactoring instanceof MoveRefactoring) {
            FileObject file = refactoring.getRefactoringSource().lookup(FileObject.class);
            if (file != null && RefactoringInfo.isJavaFile(file) && isOnSourceClasspath(file)) {
                // moving a java file (between packages)
                changeType = RefactoringInfo.ChangeType.CLASS_MOVE;
                primaryFile = file;
                ClassPath cp = ClassPath.getClassPath(file, ClassPath.SOURCE);
                oldName = cp.getResourceName(file, '.', false);
            }
        } else if (refactoring instanceof SingleCopyRefactoring) {
            FileObject file = refactoring.getRefactoringSource().lookup(FileObject.class);
            if (file != null && RefactoringInfo.isJavaFileOfForm(file) && isOnSourceClasspath(file)) {
                // copying a java file
                changeType = RefactoringInfo.ChangeType.CLASS_COPY;
                primaryFile = file;
                ClassPath cp = ClassPath.getClassPath(file, ClassPath.SOURCE);
                oldName = cp.getResourceName(file, '.', false);
            }
        } else if (refactoring instanceof SafeDeleteRefactoring) {
            FileObject file = refactoring.getRefactoringSource().lookup(FileObject.class);
            if (file != null && RefactoringInfo.isJavaFileOfForm(file) && isOnSourceClasspath(file)) {
                // deleting a form
                changeType = RefactoringInfo.ChangeType.CLASS_DELETE;
                primaryFile = file;
            }            
        }

        if (changeType != null) {
            RefactoringInfo refInfo = new RefactoringInfo(refactoring, changeType, primaryFile, oldName);
            refactoring.getContext().add(refInfo); // to be accessible to the GuardedBlockHandlerFactoryImpl
            return new RefactoringPluginImpl(refInfo);
        }
        return null;
    }

    private static boolean isOnSourceClasspath(FileObject fo) {
        // TBD
        return true;
    }

    // -----

    private static class RefactoringPluginImpl implements RefactoringPlugin {

        private RefactoringInfo refInfo;

        RefactoringPluginImpl(RefactoringInfo refInfo) {
            this.refInfo = refInfo;
        }

        public Problem preCheck() {
            return null;
        }

        public Problem checkParameters() {
            return null;
        }

        public Problem fastCheckParameters() {
            return null;
        }

        public void cancelRequest() {
        }

        public Problem prepare(RefactoringElementsBag refactoringElements) {
            // even if guarded blocks are not affected directly we might want some changes
            if (refInfo.isForm()) {
                FormRefactoringUpdate update = refInfo.getUpdateForFile(refInfo.getPrimaryFile());
                switch (refInfo.getChangeType()) {
                case CLASS_DELETE: // in case of delete we only backup the form file
                    refactoringElements.registerTransaction(update);
                    return null;
                case CLASS_RENAME: // renaming form class, always needs to load - auto-i18n
                    if (!update.prepareForm(true)) {
                        return new Problem(true, "Error loading form. Cannot update generated code.");
                    }
                    break;
                // for VARIABLE_RENAME and EVENT_HANDLER_RENAME we don't know yet
                // if they affect the form - guarded block handler will take care
                }
                refactoringElements.add(refInfo.getRefactoring(), update.getPreviewElement());
                refactoringElements.addFileChange(refInfo.getRefactoring(), update);
            } else if (refInfo.getChangeType() == RefactoringInfo.ChangeType.PACKAGE_RENAME
                       || refInfo.getChangeType() == RefactoringInfo.ChangeType.FOLDER_RENAME) {
                boolean anyForm = false;
                for (FileObject fo : refInfo.getPrimaryFile().getChildren()) {
                    if (RefactoringInfo.isJavaFileOfForm(fo)) {
                        anyForm = true;
                        FormRefactoringUpdate update = refInfo.getUpdateForFile(fo);
                        refactoringElements.addFileChange(refInfo.getRefactoring(), update);
                    }
                }
                if (anyForm) {
                    // TODO add refactoring element informing about updating references to resources in GUI forms in this package
                }
            }
            return null;
        }
    }
}
