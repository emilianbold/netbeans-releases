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

import java.io.File;
import java.util.Collection;
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
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.IssueFinder;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.util.BugtrackingOwnerSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import static org.netbeans.modules.bugtracking.util.IssueFinderUtils.HyperlinkSpanInfo;

/**
 * 
 * Provides hyperlink functionality on issue reference in code comments
 * 
 * @author Tomas Stupka
 */
public class EditorHyperlinkProviderImpl implements HyperlinkProviderExt, LookupListener {

    private static Logger LOG = Logger.getLogger(EditorHyperlinkProviderImpl.class.getName());

    //--------------------------------------------------------------------------

    private final Result<IssueFinder> lookupResult = Lookup.getDefault().lookupResult(IssueFinder.class);
    private IssueFinder[] issueFinders;

    {
        lookupResult.addLookupListener(this);
        refreshIssueFinders();
    }

    public void resultChanged(LookupEvent ev) {
        refreshIssueFinders();
    }

    private void refreshIssueFinders() {
        Collection<? extends IssueFinder> allInstances = lookupResult.allInstances();
        IssueFinder[] newResult = new IssueFinder[allInstances.size()];
        allInstances.toArray(newResult);
        issueFinders = newResult;
    }

    //--------------------------------------------------------------------------

    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return EnumSet.of(HyperlinkType.GO_TO_DECLARATION); 
    }

    public boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType type) {
        return getIssueSpan(doc, offset, type) != null;
    }

    public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type) {
        HyperlinkSpanInfo spanInfo = getIssueSpan(doc, offset, type);
        return (spanInfo != null) ? new int[] {spanInfo.startOffset,
                                               spanInfo.endOffset}
                                  : null;
    }

    public void performClickAction(final Document doc, final int offset, final HyperlinkType type) {
        final String issueId = getIssueId(doc, offset, type);
        if(issueId == null) return;

        class IssueDisplayer implements Runnable {
            public void run() {
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
                Issue.open(repo, issueId);
            }
        }
        RequestProcessor.getDefault().post(new IssueDisplayer());
    }

    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        return NbBundle.getMessage(EditorHyperlinkProviderImpl.class, "LBL_OpenIssue", new Object[] { getIssueId(doc, offset, type) });
    }

    // XXX get/unify from/with hyperlink provider
    private String getIssueId(Document doc, int offset, HyperlinkType type) {
        HyperlinkSpanInfo spanInfo = getIssueSpan(doc, offset, type);

        if (spanInfo == null) {
            return null;
        }

        String issueId = null;
        try {
            if ((spanInfo.startOffset <= offset) && (offset <= spanInfo.endOffset)) {
                /* at first, check that it is a valid reference text: */
                int length = spanInfo.endOffset - spanInfo.startOffset;
                String text = doc.getText(spanInfo.startOffset, length);
                int[] spans = spanInfo.issueFinder.getIssueSpans(text);
                if (spans.length == 2) {
                    /* valid - now just retreive the issue id: */
                    issueId = spanInfo.issueFinder.getIssueId(text);
                }
            }
        } catch (BadLocationException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return issueId;
    }

    private HyperlinkSpanInfo getIssueSpan(Document doc, int offset, HyperlinkType type) {
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
                for (IssueFinder issueFinder : issueFinders) {
                    int[] span = issueFinder.getIssueSpans(text);
                    for (int i = 1; i < span.length; i += 2) {
                        if(ts.offset() + span[i-1] <= offset && offset <= ts.offset() + span[i]) {
                            return new HyperlinkSpanInfo(issueFinder,
                                                         ts.offset() + span[i-1],
                                                         ts.offset() + span[i]);
                        }
                    }
                }
            }
        }
        return null;
    }

}
