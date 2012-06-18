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
package org.netbeans.modules.swingapp;

import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.support.EditorAwareJavaSourceTaskFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.form.FormDataObject;
import org.netbeans.modules.nbform.FormEditorSupport;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author joshy
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.api.java.source.JavaSourceTaskFactory.class)
public class ActionRescanJavaSourceTaskFactoryImpl extends EditorAwareJavaSourceTaskFactory {

    public ActionRescanJavaSourceTaskFactoryImpl() {
        super(Phase.RESOLVED, Priority.LOW); //getPhase(),getPriority());
    }

    @Override
    public CancellableTask<CompilationInfo> createTask(FileObject file) {
        return new RescanTask(file);
    }

    public Priority getPriority() {
        return Priority.LOW;
    }

    public Phase getPhase() {
        return Phase.RESOLVED;
    }

    private static class RescanTask implements CancellableTask<CompilationInfo> {
        FileObject file;

        public RescanTask(FileObject file) {
            this.file = file;
        }

        @Override
        public void cancel() {
        }

        @Override
        public void run(CompilationInfo info) throws Exception {
            // Rescan the file only if it is a java file from which some opened
            // form might read action methods - technically this can be the form
            // itself or a class representing the application class.
            if (ActionManager.anyFormOpened()
                    && AppFrameworkSupport.isFrameworkLibAvailable(file)
                    && (isApplicationSourceFile(file)
                        || isOpenedForm(file))) {
                ActionManager.lazyRescan(file);
            }
        }

        private static boolean isApplicationSourceFile(FileObject file) {
            String appClsName = AppFrameworkSupport.getAppClassNameFromProjectConfig(FileOwnerQuery.getOwner(file));
            if (appClsName != null) {
                return appClsName.equals(AppFrameworkSupport.getClassNameForFile(file));
            }
            return false;
        }

        private static boolean isOpenedForm(FileObject fo) {
            if (fo.existsExt("form") && fo.hasExt("java")) { // NOI18N
                try {
                    DataObject dobj = DataObject.find(fo);
                    if (dobj instanceof FormDataObject) {
                        FormDataObject formDO = (FormDataObject) dobj;
                        return ((FormEditorSupport)formDO.getFormEditorSupport()).isOpened();
                    }
                } catch(DataObjectNotFoundException ex) {
                    assert false;
                }

            }
            return false;
        }
    }

}
