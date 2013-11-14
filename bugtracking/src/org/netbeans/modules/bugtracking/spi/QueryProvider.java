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

import java.beans.PropertyChangeListener;

/**
 * Provides access to a bugtracking query.
 *
 * @author Tomas Stupka
 * 
 * @param <Q> the implementation specific query type
 * @param <I> the implementation specific issue type
 * @since 1.85
 */
public interface QueryProvider<Q, I> {

    /**
     * Returns the queries display name
     * @param q the particular query instance
     * @return the display name
     * @since 1.85
     */
    public String getDisplayName(Q q);

    /**
     * Returns the queries tooltip
     * @param q the particular query instance
     * @return the tooltip
     * @since 1.85
     */
    public String getTooltip(Q q);

    /**
     * Returns the {@link QueryController} for this query
     * @param q the implementation specific query type
     * @return a controller for the given query
     * @since 1.85
     */
    public QueryController getController(Q q);

    /**
     * Determines whether it is possible to remove the given Query.
     * 
     * @param q the particular query instance
     * @return  <code>true</code> in case it is possible to remove the query, otherwise <code>fasle</code>
     * @since 1.85
     */
    public boolean canRemove(Q q);
    
    /** 
     * Removes the given query.
     * 
     * @param q the particular query instance
     * @since 1.85
     */
    public void remove(Q q);
    
    /**
     * Determines whether it is possible to rename the given Query
     * @param q the particular query instance
     * @return <code>true</code> in case it is possible to rename the query, otherwise <code>fasle</code>
     * @since 1.85
     */
    public boolean canRename(Q q);
    
    /**
     * Renames the given query.
     * 
     * @param q the particular query instance
     * @param newName new name
     * @since 1.85
     */
    public void rename(Q q, String newName);
    
    /**
     * Sets a {@link IssueContainer}. 
     * 
     * @param q the particular query instance
     * @param c a IssueContainer
     */
    void setIssueContainer(Q q, IssueContainer<I> c);
    
    /**
     * Refreshes the given query. 
     * 
     * <p>
     * <b>Note</p> that this call is made periodically by the infrastructure. 
     * <p>
     * 
     * @param q the particular query instance
     * @since 1.85
     */
    public void refresh(Q q);
    
    /**
     * Remove a PropertyChangeListener from the given query.
     * 
     * @param q the particular query instance
     * @param listener a PropertyChangeListener
     * @since 1.85
     */
    public void removePropertyChangeListener(Q q, PropertyChangeListener listener);

    /**
     * Add a PropertyChangeListener to the given query.
     * 
     * @param q the particular query instance
     * @param listener a PropertyChangeListener
     * @since 1.85
     */
    public void addPropertyChangeListener(Q q, PropertyChangeListener listener);

    /**
     * Callback for Queries to notify about refreshing progress 
     * and retrieved Issues
     * 
     * @param <I> the implementation specific issue type
     * @since 1.85
     */
    public interface IssueContainer<I> {
        
        /**
         * The Query refreshing started.
         */
        void refreshingStarted();
        
        /**
         * The Query refreshing finished.
         */
        void refreshingFinished();
        
        /**
         * Add an Issue.
         * 
         * @param issue a particular issue instance
         */
        void add(I issue);
        
        /**
         * Add an Issue.
         * 
         * @param issue a particular issue instance
         */
        void remove(I issue);
    }
}
