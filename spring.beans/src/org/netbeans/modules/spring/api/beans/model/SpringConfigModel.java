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

package org.netbeans.modules.spring.api.beans.model;

import java.io.File;
import java.io.IOException;
import javax.swing.text.Document;
import javax.swing.text.Position.Bias;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.ConfigFileGroup;
import org.netbeans.modules.spring.api.beans.SpringScope;
import org.netbeans.modules.spring.beans.SpringScopeAccessor;
import org.netbeans.modules.spring.beans.model.SpringConfigFileModelController.DocumentWrite;
import org.netbeans.modules.spring.beans.model.SpringConfigModelController;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionRef;

/**
 * Encapsulates a model of Spring configuration files.
 *
 * @author Andrei Badea
 */
public final class SpringConfigModel {

    private final SpringConfigModelController controller;

    /**
     * Returns a Spring configuration model for the given file.
     *
     * @param  file a file; never null.
     * @return a Spring configuration model or null
     */
    public static SpringConfigModel forFileObject(FileObject file) {
        SpringScope scope = SpringScope.getSpringScope(file);
        if (scope != null) {
            return SpringScopeAccessor.DEFAULT.getConfigModel(scope, file);
        }
        return null;
    }

    // XXX should not be public.
    public SpringConfigModel(ConfigFileGroup configFileGroup) {
        controller = SpringConfigModelController.create(configFileGroup);
    }

    /**
     * Provides access to the model. This method expects an {@link Action}
     * whose run method will be passed an instance of {@link SpringBeans}.
     *
     * <p><strong>All clients must make sure that no objects obtained from
     * the {@code SpringBeans} instance "escape" the {@code run()} method, in the
     * sense that they are reachable when the {@code run()} method has
     * finished running.</strong></p>
     *
     * @param action
     */
    public void runReadAction(final Action<SpringBeans> action) throws IOException {
        controller.runReadAction(action);
    }

    public void runWriteAction(Action<WriteContext> action) throws IOException {
        controller.runWriteAction(action);
    }

    // XXX rename to DocumentAccess.
    // XXX remove public constructor.
    public static final class WriteContext {

        private final SpringBeans springBeans;
        private final DocumentWrite docWrite;
        private final File file;

        public WriteContext(SpringBeans springBeans, File file, DocumentWrite docWrite) {
            this.springBeans = springBeans;
            this.docWrite = docWrite;
            this.file = file;
        }

        public SpringBeans getSpringBeans() {
            return springBeans;
        }

        public Document getDocument() {
            return docWrite.getDocument();
        }

        public File getFile() {
            return file;
        }

        public FileObject getFileObject() {
            return NbEditorUtilities.getFileObject(docWrite.getDocument());
        }

        public PositionRef createPositionRef(int offset, Bias bias) {
            return docWrite.createPositionRef(offset, bias);
        }

        public void commit() throws IOException {
            docWrite.commit();
        }
    }
}
