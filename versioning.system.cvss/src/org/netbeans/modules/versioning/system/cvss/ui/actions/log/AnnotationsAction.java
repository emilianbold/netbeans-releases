/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
            JEditorPane[] panes = ec.getOpenedPanes();
            if (panes != null && panes.length > 0) {
                return panes[0];
            }
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
