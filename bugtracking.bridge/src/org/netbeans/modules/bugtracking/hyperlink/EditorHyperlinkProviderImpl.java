/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.hyperlink;

import java.awt.EventQueue;
import java.io.File;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.util.BugtrackingOwnerSupport;
import org.netbeans.modules.bugtracking.util.IssueFinder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 * 
 * Provides hyperlink functionality on issue reference in code comments
 * 
 * @author Tomas Stupka
 */
public class EditorHyperlinkProviderImpl implements HyperlinkProviderExt {

    private static Logger LOG = Logger.getLogger(EditorHyperlinkProviderImpl.class.getName());

    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return EnumSet.of(HyperlinkType.GO_TO_DECLARATION); 
    }

    public boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType type) {
        return getIssueSpan(doc, offset, type) != null;
    }

    public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type) {
        return getIssueSpan(doc, offset, type);
    }

    public void performClickAction(final Document doc, final int offset, final HyperlinkType type) {
        final String issueId = getIssueId(doc, offset, type);
        if(issueId == null) return;

        final Task[] t = new Task[1];
        Cancellable c = new Cancellable() {
            public boolean cancel() {
                if(t[0] != null) {
                    return t[0].cancel();
                }
                return true;
            }
        };
        DataObject dobj = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
        File file = null;
        if (dobj != null) {
            FileObject fileObject = dobj.getPrimaryFile();
            if(fileObject != null) {
                file = FileUtil.toFile(fileObject);
            }
        }
        if(file == null) return;

        final Repository repo = BugtrackingOwnerSupport.getInstance().getRepository(file, issueId, true);
        if(repo == null) return;

        BugtrackingOwnerSupport.getInstance().setFirmAssociation(file, repo);

        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(EditorHyperlinkProviderImpl.class, "MSG_Opening", new Object[] {issueId}), c); // NOI18N
        class IssueDisplayer implements Runnable {
            private Issue issue = null;
            public void run() {
                if (issue == null) {
                    /* (request processor thread) - find the issue */
                    try {
                        issue = repo.getIssue(issueId);
                    } finally {
                        handle.finish();
                    }
                    if (issue != null) {
                        EventQueue.invokeLater(this);
                    }
                } else {
                    /* (AWT event-dispatching thread) - display the issue */
                    assert EventQueue.isDispatchThread();
                    issue.open();
                }
            }
        }
        handle.start();
        t[0] = RequestProcessor.getDefault().post(new IssueDisplayer());
    }

    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        return NbBundle.getMessage(EditorHyperlinkProviderImpl.class, "LBL_OpenIssue", new Object[] { getIssueId(doc, offset, type) });
    }

    // XXX get/unify from/with hyperlink provider
    private String getIssueId(Document doc, int offset, HyperlinkType type) {
        int[] idx = getIssueSpan(doc, offset, type);
        if (idx == null) {
            return null;
        }
        String issueId = null;
        try {
            for (int i = 1; i < idx.length; i++) {
                if(idx[i-1] <= offset && offset <= idx[i]) {
                    issueId = IssueFinder.getIssueNumber(doc.getText(idx[i-1], idx[i] - idx[i-1]));
                    break;
                }
            }
        } catch (BadLocationException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return issueId;
    }

    private int[] getIssueSpan(Document doc, int offset, HyperlinkType type) {
        TokenHierarchy th = TokenHierarchy.get(doc);
        List<TokenSequence> list = th.embeddedTokenSequences(offset, false);

        for (TokenSequence ts : list) {
            if (ts == null) {
                return null;
            }
            ts.move(offset);
            if (!ts.moveNext()) {
                return null;
            }
            Token t = ts.token();
            if(t.id() == null || t.id().primaryCategory() == null || t.id().name() == null) {
                continue;
            }
            if (t.id().primaryCategory().toUpperCase().indexOf("COMMENT") > -1      ||  // primaryCategory == commment should be more or less a convention // NOI18N
                t.id().name().toUpperCase().indexOf("COMMENT") > -1)                    // consider this as a fallback // NOI18N
            {
                String text = t.text().toString();
                int[] span = IssueFinder.getIssueSpans(text);
                for (int i = 1; i < span.length; i += 2) {
                    if(ts.offset() + span[i-1] <= offset && offset <= ts.offset() + span[i]) {
                        return new int[] {ts.offset() + span[i-1], ts.offset() + span[i]};
                    }
                }
            }
        }
        return null;
    }

}
