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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.IssueFinder;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.util.BugtrackingOwnerSupport;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.util.IssueFinderUtils;
import org.netbeans.modules.versioning.util.VCSHyperlinkProvider;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Provides hyperlink functionality on issue reference in VCS artefects as e.g. log messages in Serach History
 *
 * @author Tomas Stupka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.versioning.util.VCSHyperlinkProvider.class)
public class VcsHyperlinkProviderImpl extends VCSHyperlinkProvider {

    @Override
    public int[] getSpans(String text) {
        return IssueFinderUtils.getIssueSpans(text);
    }

    @Override
    public String getTooltip(String text, int offsetStart, int offsetEnd) {
        return NbBundle.getMessage(VcsHyperlinkProviderImpl.class, "LBL_OpenIssue", new Object[] { getIssueId(text, offsetStart, offsetEnd) });
    }

    @Override
    public void onClick(final File file, final String text, int offsetStart, int offsetEnd) {
        final String issueId = getIssueId(text, offsetStart, offsetEnd);
        if(issueId == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "No issue found for {0}", text.substring(offsetStart, offsetEnd));
            return;
        }
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                BugtrackingUtil.openIssue(file, issueId);
            }
        });
    }

    private String getIssueId(String text, int offsetStart, int offsetEnd) {        
        IssueFinder issueFinder = IssueFinderUtils.determineIssueFinder(text, offsetStart, offsetEnd);
        if (issueFinder == null) {
            return null;
        }

        return issueFinder.getIssueId(text.substring(offsetStart, offsetEnd));
    }

}
