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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.mercurial.ui.annotate;

import org.netbeans.modules.versioning.spi.VCSContext;

import javax.swing.*;
import java.io.File;
import java.util.*;
import java.awt.event.ActionEvent;
import org.openide.nodes.Node;
import org.openide.cookies.EditorCookie;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.openide.windows.TopComponent;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Annotate action for mercurial: 
 * hg annotate - show changeset information per file line 
 * 
 * @author John Rice
 */
public class AnnotateAction extends ContextAction {
    
    private final VCSContext context;
    
    public AnnotateAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }
    
    public boolean isEnabled() {
        File repository  = HgUtils.getRootFile(context);
        if (repository == null) return false;

        Node [] nodes = context.getElements().lookupAll(Node.class).toArray(new Node[0]);
        if (context.getRootFiles().size() > 0 && activatedEditorCookie(nodes) != null) {
            FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
            File file = activatedFile(nodes);
            FileInformation info  = cache.getCachedStatus(file);
            if(info != null) {
                int status = info.getStatus();
                if (status == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY ||
                    status == FileInformation.STATUS_NOTVERSIONED_EXCLUDED) {
                    return false;
                } else {
                    return true;
                }
            } else {
                // XXX won't work properly when staus not chached yet. we should at least force a cahce.refresh
                // at this point
                return true;
            }
        } else {
            return false;
        } 
    } 

    public void performAction(ActionEvent e) {
        Node [] nodes = context.getElements().lookupAll(Node.class).toArray(new Node[0]);
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
            final TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class,  currentPane);
            tc.requestActive();

            final AnnotationBar ab = AnnotationBarManager.showAnnotationBar(currentPane);
            ab.setAnnotationMessage(NbBundle.getMessage(AnnotateAction.class, "CTL_AnnotationSubstitute")); // NOI18N;

            final File repository  = HgUtils.getRootFile(context);
            if (repository == null) return;

            RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
            HgProgressSupport support = new HgProgressSupport() {
                public void perform() {
                    OutputLogger logger = getLogger();
                    logger.outputInRed(
                            NbBundle.getMessage(AnnotateAction.class,
                            "MSG_ANNOTATE_TITLE")); // NOI18N
                    logger.outputInRed(
                            NbBundle.getMessage(AnnotateAction.class,
                            "MSG_ANNOTATE_TITLE_SEP")); // NOI18N
                    computeAnnotations(repository, file, this, ab);
                    logger.output("\t" + file.getAbsolutePath()); // NOI18N
                    logger.outputInRed(
                            NbBundle.getMessage(AnnotateAction.class,
                            "MSG_ANNOTATE_DONE")); // NOI18N
                }
            };
            support.start(rp, repository, NbBundle.getMessage(AnnotateAction.class, "MSG_Annotation_Progress")); // NOI18N
        }
    }

    private void computeAnnotations(File repository, File file, HgProgressSupport progress, AnnotationBar ab) {
        List<String> list = null;
        try {
             list = HgCommand.doAnnotate(repository, file, progress.getLogger());
        } catch (HgException ex) {
            NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
            DialogDisplayer.getDefault().notifyLater(e);
        }
        if (progress.isCanceled()) {
            ab.setAnnotationMessage(NbBundle.getMessage(AnnotateAction.class, "CTL_AnnotationFailed")); // NOI18N;
            return;
        }
        if (list == null) return;
        AnnotateLine [] lines = toAnnotateLines(list);
        HgLogMessage [] logs;
        Set<File> setFile = new HashSet<File>();
        setFile.add(file);
        logs = HgCommand.getLogMessagesNoFileInfo(repository, setFile, -1, progress.getLogger());
        if (progress.isCanceled()) {
            return;
        }
        if (logs == null) return;
        fillCommitMessages(lines, logs);
        ab.setLogs(logs);
        ab.annotationLines(file, Arrays.asList(lines));
    }

    private static void fillCommitMessages(AnnotateLine [] annotations, HgLogMessage [] logs) {
        long lowestRevisionNumber = Long.MAX_VALUE;
        for (int i = 0; i < annotations.length; i++) {
            AnnotateLine annotation = annotations[i];
            if (annotation == null) {
                Mercurial.LOG.log(Level.WARNING, "AnnotateAction: annotation {0} of {1} is null", new Object[]{i, annotations.length}); //NOI18N
                continue;
            }
            for (int j = 0; j < logs.length; j++) {
                HgLogMessage log = logs[j];
                if (log == null) {
                    Mercurial.LOG.log(Level.WARNING, "AnnotateAction: log {0} of {1} is null", new Object[]{j, logs.length}); //NOI18N
                    continue;
                }
                if (log.getRevisionAsLong() < lowestRevisionNumber) {
                    lowestRevisionNumber = log.getRevisionAsLong();
                }
                if (annotation.getRevision().equals(log.getRevision())) {
                    annotation.setDate(log.getDate());
                    annotation.setId(log.getCSetShortID());
                    annotation.setCommitMessage(log.getMessage());
                }
            }
        }
        String lowestRev = Long.toString(lowestRevisionNumber);
        for (int i = 0; i < annotations.length; i++) {
            AnnotateLine annotation = annotations[i];
            if (annotation == null) {
                Mercurial.LOG.log(Level.WARNING, "AnnotateAction: annotation {0} of {1} is null", new Object[]{i, annotations.length}); //NOI18N
            }else{
                annotation.setCanBeRolledBack(!annotation.getRevision().equals(lowestRev));
            }
        }
    }

    private static AnnotateLine [] toAnnotateLines(List<String> annotations)
    {
        final int GROUP_AUTHOR = 1;
        final int GROUP_REVISION = 2;
        final int GROUP_FILENAME = 3;
        final int GROUP_CONTENT = 4;
        
        List<AnnotateLine> lines = new ArrayList<AnnotateLine>();
        int i = 0;
        Pattern p = Pattern.compile("^\\s*(\\w+\\b)\\s+(\\d+)\\s+(\\b\\S*):\\s(.*)$"); //NOI18N
        for (String line : annotations) {
            i++;
            Matcher m = p.matcher(line);
            if (!m.matches()){
                Mercurial.LOG.log(Level.WARNING, "AnnotateAction: toAnnotateLines(): Failed when matching: {0}", new Object[] {line}); //NOI18N
                continue;
            }
            AnnotateLine anLine = new AnnotateLine();
            anLine.setAuthor(m.group(GROUP_AUTHOR));
            anLine.setRevision(m.group(GROUP_REVISION));
            anLine.setFileName(m.group(GROUP_FILENAME));
            anLine.setContent(m.group(GROUP_CONTENT));
            anLine.setLineNum(i);
            
            lines.add(anLine);
        }
        return lines.toArray(new AnnotateLine[lines.size()]);
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
