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
package org.netbeans.modules.bugtracking.api;

import java.io.File;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.spi.IssueFinder;
import org.netbeans.modules.bugtracking.ui.issue.IssueAction;
import org.netbeans.modules.bugtracking.ui.query.QueryAction;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.util.IssueFinderUtils;

/**
 *
 * @author Tomas Stupka
 */
public final class Util {
    
    private Util() { }
    
    /**
     * Opens an issue.
     * 
     * @param repository the repository where where the given issueId originates from
     * @param issueId the issue id
     */
    public static void openIssue(Repository repository, String issueId) {
        IssueAction.openIssue(repository.getImpl(), issueId);
    }    
    
    /**
     * Opens an issue with the given Id. 
     * 
     * @param context
     * @param issueId
     */
    public static void openIssue(File context, String issueId) {
        BugtrackingUtil.openIssue(context, issueId);
    }
    
    /*
     * Creates a new, not yet saved and named query.  
     * 
     * @return 
     */
    public static void createNewQuery(Repository repository) {
        QueryAction.createNewQuery(repository.getImpl());
    }

    /**
     * Creates a new, not yet submitted issue.
     * 
     * @param repository
     */
    public static void createNewIssue(Repository repository) {
        IssueAction.createIssue(repository.getImpl());
    }
    
    /**
     * Creates a new {@link Issue} instance prefilled with 
     * the given summary and description.
     * 
     * @param repository
     * @param summary
     * @param description
     * @return 
     */
    public static Issue createIssue(Repository repository, String summary, String description) {
        IssueImpl issueImpl = repository.getImpl().createNewIssue(summary, description);
        return issueImpl.getIssue();
    }
    
    /**
     * Opens a modal edit repository dialog.<br>
     * Blocks until the dialog isn't closed.
     * 
     * @param repository the repository to be edited
     */
    public static void edit(Repository repository) { 
        BugtrackingUtil.editRepository(repository);
    }
    
    /**
     * Returns the spans from the given text, which represent an potential Issue 
     * reference.
     * - e.g. "Issue #12345", "Bug #1432"
     * 
     * @param text
     * @return 
     */
    public static int[] getIssueSpans(String text) {
        return IssueFinderUtils.getIssueSpans(text);
    }
    
    /**
     * 
     * @param text
     * @return 
     */
    public static String getIssueId(String text) {        
        IssueFinder issueFinder = IssueFinderUtils.determineIssueFinder(text, 0, text.length());
        if (issueFinder == null) {
            return null;
        }
        return issueFinder.getIssueId(text);
    }  
    
}
