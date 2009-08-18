/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking.kenai;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.ui.spi.KenaiIssueAccessor;
import org.openide.util.NbBundle;

/**
 *
 * @author joshis
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.kenai.ui.spi.KenaiIssueAccessor.class)
public class IssueAccessorImpl extends KenaiIssueAccessor {

    /**
     * Open a TC with an issue in the IDE
     * @param project Kenai project
     * @param issueID Issue identifier
     */
    @Override
    public void open(final KenaiProject project, final String issueID) {
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(Issue.class, "LBL_GETTING_REPO"));
        handle.start();
        BugtrackingManager.getInstance().getRequestProcessor().post(new Runnable() {

            public void run() {
                final Repository repo = KenaiRepositories.getInstance().getRepository(project);
                handle.finish();
                try {
                    Issue.open(repo, issueID);
                } catch (NullPointerException e) {
                    //
                }
            }
        });
    }
}
