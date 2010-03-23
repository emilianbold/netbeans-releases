/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.versioning.system.cvss.ui.actions.log;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.ErrorManager;
import org.openide.util.Cancellable;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import org.openide.cookies.EditorCookie;
import org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.annotate.AnnotationBarManager;
import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.ExecutorGroup;
import org.netbeans.lib.cvsclient.command.annotate.AnnotateCommand;
import org.netbeans.lib.cvsclient.command.log.LogCommand;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.netbeans.lib.cvsclient.admin.AdminHandler;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import org.openide.text.NbDocument;

/**
 * Show/Hide Annotations action. It's enabled for single
 * node selections only.
 * 
 * @author Petr Kuzel
 */
public class AnnotationsAction extends AbstractSystemAction {

    protected String getBaseName(Node [] activatedNodes) {
        if (visible(activatedNodes)) {
            return "CTL_MenuItem_HideAnnotations";  // NOI18N
        } else {
            return "CTL_MenuItem_Annotations"; // NOI18N
        }
    }

    public boolean enable(Node[] nodes) {
        return super.enable(nodes) && activatedEditorCookie(nodes) != null;
    }

    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_IN_REPOSITORY;
    }

    protected int getDirectoryEnabledStatus() {
        return 0;
    }

    protected boolean asynchronous() {
        return false;
    }

    public void performCvsAction(Node[] nodes) {
        if (visible(nodes)) {
            JEditorPane pane = activatedEditorPane(nodes);
            AnnotationBarManager.hideAnnotationBar(pane);
        } else {
            EditorCookie ec = activatedEditorCookie(nodes);
            if (ec != null) {
                File file = activatedFile(nodes);
                CvsVersioningSystem cvss = CvsVersioningSystem.getInstance();
                AdminHandler entries = cvss.getAdminHandler();

                JEditorPane[] panes = ec.getOpenedPanes();
                if (panes == null) {
                    ec.open();
                }
                panes = ec.getOpenedPanes();
                if (panes == null) {
                    return;
                }
                final JEditorPane currentPane = panes[0];
                LogOutputListener ab = AnnotationBarManager.showAnnotationBar(currentPane);

                AnnotateCommand annotate = new AnnotateCommand();

                try {
                    Entry entry = entries.getEntry(file);
                    if (entry == null) {
                        return;
                    }
                    String revision = entry.getRevision();
                    annotate.setAnnotateByRevision(revision);
                    File[] cmdFiles = new File[] {file};
                    annotate.setFiles(cmdFiles);

                    ExecutorGroup group = new ExecutorGroup(NbBundle.getMessage(AnnotationsAction.class, "BK0001"));

                    AnnotationsExecutor executor = new AnnotationsExecutor(cvss, annotate);
                    executor.addLogOutputListener(ab);
                    group.addExecutor(executor);

                    // get commit message sfrom log

                    LogCommand log = new LogCommand();
                    log.setFiles(cmdFiles);
                    log.setNoTags(true);

                    LogExecutor lexecutor = new LogExecutor(cvss, log);
                    lexecutor.addLogOutputListener(ab);
                    group.addExecutor(lexecutor);

                    group.addCancellable(new Cancellable(){
                        public boolean cancel() {
                            AnnotationBarManager.hideAnnotationBar(currentPane);
                            return true;
                        }
                    });

                    group.execute();
                } catch (IOException e) {
                    ErrorManager err = ErrorManager.getDefault();
                    err.annotate(e, NbBundle.getMessage(AnnotationsAction.class, "BK0002", file));
                    err.notify(e);
                }
            }
        }
    }

    /**
     * @param nodes or null (then taken from windowsystem, it may be wrong on editor tabs #66700).
     */
    public boolean visible(Node[] nodes) {
        JEditorPane currentPane = activatedEditorPane(nodes);
        return AnnotationBarManager.annotationBarVisible(currentPane);
    }

    /**
     * @return active editor pane or null if selected node
     * does not have any or more nodes selected.
     */
    private JEditorPane activatedEditorPane(Node[] nodes) {
        EditorCookie ec = activatedEditorCookie(nodes);
        if (ec != null && SwingUtilities.isEventDispatchThread()) {
            return NbDocument.findRecentEditorPane(ec);
        }
        return null;
    }

    private EditorCookie activatedEditorCookie(Node[] nodes) {
        if (nodes == null) {
            nodes = WindowManager.getDefault().getRegistry().getActivatedNodes();
        }
        if (nodes.length == 1) {
            Node node = nodes[0];
            return (EditorCookie) node.getCookie(EditorCookie.class);
        }
        return null;
    }

    private File activatedFile(Node[] nodes) {
        if (nodes.length == 1) {
            Node node = nodes[0];
            DataObject dobj = (DataObject) node.getCookie(DataObject.class);
            if (dobj != null) {
                FileObject fo = dobj.getPrimaryFile();
                return FileUtil.toFile(fo);
            }
        }
        return null;

    }
    @Override
    protected String iconResource() {
        return "org/netbeans/modules/versioning/system/cvss/resources/icons/annotate.png"; // NOI18N
    }
}
