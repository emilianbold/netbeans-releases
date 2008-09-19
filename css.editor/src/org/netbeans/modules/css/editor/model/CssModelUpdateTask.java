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
package org.netbeans.modules.css.editor.model;

import javax.swing.text.Document;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.napi.gsfret.source.Phase;
import org.netbeans.napi.gsfret.source.Source.Priority;
import org.netbeans.napi.gsfret.source.support.EditorAwareSourceTaskFactory;
import org.openide.filesystems.FileObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.gsf.api.DataLoadersBridge;
import org.netbeans.napi.gsfret.source.CompilationInfo;

/**
 * 
 * @author Marek Fukala
 */
public final class CssModelUpdateTask implements CancellableTask<CompilationInfo> {

    private FileObject file;

    CssModelUpdateTask(FileObject file) {
        this.file = file;
    }

    public Document getDocument() {
        return DataLoadersBridge.getDefault().getDocument(file);
    }
    private boolean cancel;

    synchronized boolean isCanceled() {
        return cancel;
    }

    public synchronized void cancel() {
        cancel = true;
    }

    synchronized void resume() {
        cancel = false;
    }

    public void run(CompilationInfo info) {
        resume();

        Document doc = getDocument();

        if (doc == null) {
            Logger.getLogger(CssModelUpdateTask.class.getName()).log(Level.INFO, "Cannot get document!");
            return;
        }

        CssModel.get(getDocument()).parsed(info);
    }

    public static class CssModelUpdateTaskFactory extends EditorAwareSourceTaskFactory {

        public CssModelUpdateTaskFactory() {
            super(Phase.PARSED, Priority.BELOW_NORMAL);
        }

        public CancellableTask<CompilationInfo> createTask(FileObject file) {
            if ("text/x-css".equals(file.getMIMEType())) { //NOI18N
                return new CssModelUpdateTask(file);
            } else {
                //return empty task for non css files
                return new CancellableTask<CompilationInfo>() {
                    public void cancel() {
                    }
                    public void run(CompilationInfo parameter) throws Exception {
                    }
                };
            }
        }
    }
    
    
}

