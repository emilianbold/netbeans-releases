/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.support.actions;

import java.io.IOException;
import java.lang.ref.Reference;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.spi.project.SingleMethod;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;

/**
 *
 * @author Martin Janicek
 */
public final class TestMethodUtil {

    private static final Logger LOGGER = Logger.getLogger(TestMethodUtil.class.getName());
    private Reference<JavaSource> resolver;

    static boolean isTestClass(Node activatedNode) {
        FileObject fo = getFileObjectFromNode(activatedNode);
        if (fo != null) {
            if (!isGroovyFile(fo)) {
                return false;
            }
            //TODO add more checks here when action gets enabled?
        }
        return false;
    }

    static SingleMethod getTestMethod(Document doc, int cursor){
        SingleMethod sm = null;
        if (doc != null){
            JavaSource js = JavaSource.forDocument(doc);
            GroovyTestClassInfoTask task = new GroovyTestClassInfoTask(cursor);
            try {
                if (js != null) {
                    Future<Void> f = js.runWhenScanFinished(task, true);
                    if (f.isDone() && task.getFileObject() != null && task.getMethodName() != null){
                        sm = new SingleMethod(task.getFileObject(), task.getMethodName());
                    }
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }
        return sm;
    }

    static boolean canHandle(Node activatedNode) {
        FileObject fo = getFileObjectFromNode(activatedNode);
        if (fo != null) {
            if (!isGroovyFile(fo)) {
                return false;
            }

            EditorCookie ec = activatedNode.getLookup().lookup(EditorCookie.class);
            if (ec != null) {
                JEditorPane pane = NbDocument.findRecentEditorPane(ec);
                if (pane != null) {
                    SingleMethod sm = getTestMethod(pane.getDocument(), pane.getCaret().getDot());
                    if(sm != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static FileObject getFileObjectFromNode(Node node) {
        DataObject dO;
        DataFolder df;

        dO = node.getLookup().lookup(DataObject.class);
        if (null != dO) {
            return dO.getPrimaryFile();
        }

        df = node.getLookup().lookup(DataFolder.class);
        if (null != df) {
            return df.getPrimaryFile();
        }
        return null;
    }

    private static boolean isGroovyFile(FileObject fileObj) {
        return "groovy".equals(fileObj.getExt()) || "text/x-groovy".equals(FileUtil.getMIMEType(fileObj)); //NOI18N
    }
}
