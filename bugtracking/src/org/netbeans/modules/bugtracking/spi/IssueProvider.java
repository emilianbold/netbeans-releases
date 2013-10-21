/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking.spi;

import java.beans.PropertyChangeListener;
import java.io.File;

/**
 * Provides access to a bugtracking Issue.
 *
 * @author Tomas Stupka
 * @param <I> the implementation specific issue type
 */
public interface IssueProvider<I> {

    /**
     * issue data were refreshed
     */
    public static final String EVENT_ISSUE_REFRESHED = "issue.data_changed"; // NOI18N

    /**
     * Returns this issues display name. 
     * 
     * @param i
     * @return
     */
    public String getDisplayName(I i);

    /**
     * Returns this issues tooltip.
     * 
     * @param i
     * @return
     */
    public String getTooltip(I i);

    /**
     * Returns this issues unique ID
     * @param i
     * @return
     */
    public String getID(I i);
    
    /**
     * Returns the ID-s of all issues where this one could be considered
     * being superordinate to them. 
     * e.g. the blocks/depends relationship in Bugzilla, or sub-/parent-task in JIRA
     * 
     * 
     * @param i
     * @return 
     */
    public String[] getSubtasks(I i);

    /**
     * Returns this issues summary
     * @param i
     * @return
     */
    public String getSummary(I i);

    /**
     * Returns true if the issue isn't stored in a repository yet. Otherwise false.
     * 
     * @param i
     * @return
     */
    public boolean isNew(I i);
    
    /**
     * Determines if the issue is considered finished 
     * in the means of the particular bugtracking.
     * 
     * @param i
     * @return true if finished, otherwise false
     */
    public boolean isFinished(I i);

    /**
     * Refreshes this Issues data from its bugtracking repository
     *
     * @param i
     * @return true if the issue was refreshed, otherwise false
     */
    public boolean refresh(I i);

    /**
     * Add a comment to this issue and close it as fixed eventually.
     * 
     * @param i
     * @param comment
     * @param closeAsFixed 
     */
    // XXX throw exception
    // XXX provide way so that we know commit hooks are supported
    public void addComment(I i, String comment, boolean closeAsFixed);

    /**
     * Attach a file to this issue.
     * <br/>
     * Note that in case this functionality isn't available then
     * {@link RepositoryProvider#canAttachFile(java.lang.Object)} is expected to return <code>false</code>
     * 
     * @param i an implementation specific issue instance
     * @param file the to be attached file
     * @param description description to be associated with the file 
     * @param isPatch <code>true</code> in case the given file is a patch, otherwise <code>false</code>
     * 
     * @see RepositoryProvider#canAttachFile(java.lang.Object) 
     */
    // XXX throw exception
    public void attachFile(I i, File file, String description, boolean isPatch);

    /**
     * Returns this issues controller
     * @param i
     * @return
     */
    public IssueController getController(I i);

    /**
     * Remove a PropertyChangeListener from the given issue.
     * @param i
     * @param listener 
     */
    public void removePropertyChangeListener(I i, PropertyChangeListener listener);

    /**
     * Add a PropertyChangeListener to the given issue.
     * 
     * @param i
     * @param listener 
     */
    public void addPropertyChangeListener(I i, PropertyChangeListener listener);
    
    /**
     * Submits the issue. Override and implement if you support issue
     * submitting.
     *
     * @param i issue data
     * @return <code>true</code> if the task was successfully
     * submitted,<code>false</code> if the task was not submitted for any
     * reason.
     */
    public boolean submit (I i);
    
}
