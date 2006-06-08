/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion.ui.blame;

import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.openide.nodes.Node;
import org.openide.cookies.EditorCookie;
import org.openide.util.RequestProcessor;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.windows.WindowManager;
import org.tigris.subversion.svnclientadapter.*;

import javax.swing.*;
import java.io.File;
import java.util.*;

/**
 *
 * @author Maros Sandor
 */
public class BlameAction extends ContextAction {
    
    protected String getBaseName(Node [] activatedNodes) {
        if (visible(activatedNodes)) {
            return "CTL_MenuItem_HideAnnotations";  // NOI18N
        } else {
            return "CTL_MenuItem_ShowAnnotations"; // NOI18N
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

    protected void performContextAction(Node[] nodes) {
        if (visible(nodes)) {
            JEditorPane pane = activatedEditorPane(nodes);
            AnnotationBarManager.hideAnnotationBar(pane);
        } else {
            EditorCookie ec = activatedEditorCookie(nodes);
            if (ec == null) return;
            
            final File file = activatedFile(nodes);

            JEditorPane[] panes = ec.getOpenedPanes();
            if (panes == null) {
                ec.open();
            }
            panes = ec.getOpenedPanes();
            if (panes == null) {
                return;
            }
            final JEditorPane currentPane = panes[0];
            
            final AnnotationBar ab = AnnotationBarManager.showAnnotationBar(currentPane);

            SVNUrl repository = SvnUtils.getRepositoryRootUrl(file);
            RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repository);
            SvnProgressSupport support = new SvnProgressSupport() {
                public void perform() {                    
                    computeAnnotations(file, this, ab);
                }
            };
            support.start(rp, "Annotating...");
        }
    }

    private void computeAnnotations(File file, SvnProgressSupport progress, AnnotationBar ab) {
        SvnClient client;
        try {
            client = Subversion.getInstance().getClient(file, progress);
        } catch (SVNClientException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return;
        }
        SVNRevision currentRevision = null;
        try {
            currentRevision = Subversion.getInstance().getStatusCache().getStatus(file).getEntry(file).getRevision();
        } catch (Exception e) {
            // ignore, use default (null) revision
        }

        ISVNAnnotations annotations;
        try {
            annotations = client.annotate(file, new SVNRevision.Number(1), currentRevision);
        } catch (SVNClientException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return;
        }
        if (progress.isCanceled()) {
            return;
        }
        AnnotateLine [] lines = toAnnotateLines(annotations);
        ab.annotationLines(file, Arrays.asList(lines));
        
        // fetch log messages
        ISVNLogMessage [] logs;
        try {
            logs = client.getLogMessages(file, new SVNRevision.Number(1), currentRevision, false, true);
        } catch (SVNClientException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return;
        }
        if (progress.isCanceled()) {
            return;
        }
        fillCommitMessages(lines, logs);
    }

    private static void fillCommitMessages(AnnotateLine [] annotations, ISVNLogMessage[] logs) {
        for (int i = 0; i < annotations.length; i++) {
            AnnotateLine annotation = annotations[i];
            for (int j = 0; j < logs.length; j++) {
                ISVNLogMessage log = logs[j];
                if (annotation.getRevision().equals(log.getRevision().toString())) {
                    annotation.setDate(log.getDate());
                    annotation.setCommitMessage(log.getMessage());
                }
            }
        }
    }

    private static AnnotateLine [] toAnnotateLines(ISVNAnnotations annotations) {
        AnnotateLine [] lines = new AnnotateLine[annotations.size()];
        int n = annotations.size();
        for (int i = 0; i < n; i++) {
            lines[i] = new AnnotateLine();
            lines[i].setAuthor(annotations.getAuthor(i));
            lines[i].setContent(annotations.getLine(i));
            lines[i].setLineNum(i + 1);
            lines[i].setRevision(Long.toString(annotations.getRevision(i)));
            lines[i].setDate(annotations.getChanged(i));
        }
        return lines;
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
        if (ec != null) {
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
    
}
