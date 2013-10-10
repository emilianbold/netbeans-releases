/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.spi;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;

/**
 * 
 * Represents a bugtracking repository (server).
 * 
 * @author Tomas Stupka, Jan Stola
 * 
 * @param <R> the implementation specific repository type
 * @param <Q> the implementation specific query type
 * @param <I> the implementation specific issue type
 */
public interface RepositoryProvider<R, Q, I> {

    /**
     * A query from this repository was saved or removed
     */
    public final static String EVENT_QUERY_LIST_CHANGED = "bugtracking.repository.queries.changed"; // NOI18N
    
    /**
     * The content of unsubmitted issues for the repository changes.
     */
    public static final String EVENT_UNSUBMITTED_ISSUES_CHANGED = "bugtracking.repository.unsubmittedIssues.changed"; //NOI18N
    
    /**
     * Returns the repository info or null in case the repository is new
     * 
     * @param r
     * @return 
     */
    public RepositoryInfo getInfo(R r);
    
    /**
     * Returns the icon for this repository
     * @return
     */
    public Image getIcon(R r);

    /**
     * Returns an issue with the given ID
     *
     * XXX add flag refresh
     *
     * @param id
     * @return
     */
    public I[] getIssues(R r, String... ids);

    /**
     * Removes this repository from its connector
     *
     */
    public void remove(R r);

    /**
     * Returns the {@link BugtrackignController} for this repository
     * @return
     */
    public RepositoryController getController(R r);

    /**
     * Creates a new query instance. Might block for a longer time.
     *
     * @return a new QueryProvider instance or null if it's not possible
     * to access the repository.
     */
    public Q createQuery(R r); 

    /**
     * Creates a new issue instance. Might block for a longer time.
     *
     * @return return a new issue instance or null if it's not possible
     * to access the repository.
     */
    public I createIssue(R r);

    /**
     * Creates a new issue instance. Might block for a longer time.
     *
     * @param r
     * @param summary
     * @param description
     * @return return a new issue instance or null if it's not possible
     * to access the repository.
     * XXX do we need a canCreate(...) if this is provided?
     */
    public I createIssue(R r, String summary, String description);
    
    /**
     * Returns all saved queries
     * @return
     */
    public Collection<Q> getQueries(R r);

    /**
     * Runs a query against the bugtracking repository to get all issues
     * which applies that their ID or summary contains the given criteria string
     *
     * XXX move to siple search
     *
     * @param criteria
     */
    public Collection<I> simpleSearch(R r, String criteria);
    
    /**
     * Returns unsubmitted issues for the given repository.
     * @param r repository
     * @return collection of unsubmitted issues
     */
    public Collection<I> getUnsubmittedIssues (R r);
    
    /*********
     * EVENTS
     *********/

    public void removePropertyChangeListener(R r, PropertyChangeListener listener);

    public void addPropertyChangeListener(R r, PropertyChangeListener listener);    
}
