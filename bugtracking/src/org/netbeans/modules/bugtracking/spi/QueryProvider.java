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
import java.util.Collection;
import org.openide.nodes.Node;

/**
 * Represents an query on a bugtracing repository.
 *
 *
 * @author Tomas Stupka
 */
public abstract class QueryProvider<Q, I> {

    /**
     * queries issue list was changed
     */
    public final static String EVENT_QUERY_ISSUES_CHANGED = "bugtracking.query.issues_changed";   // NOI18N

    /**
     * query was saved
     */
    public final static String EVENT_QUERY_SAVED   = "bugtracking.query.saved";       // NOI18N

    /**
     * query was removed
     */
    public final static String EVENT_QUERY_REMOVED = "bugtracking.query.removed";     // NOI18N

    static {
        SPIAccessorImpl.createAccesor();
    }
    
    /**
     * Creates a query
     */
    public QueryProvider() {
    }

    /**
     * Returns the queries display name
     * @return
     */
    public abstract String getDisplayName(Q q);

    /**
     * Returns the queries toltip
     * @return
     */
    public abstract String getTooltip(Q q);

    /**
     * Returns the {@link BugtrackignController} for this query
     * XXX we don't need this. use get component instead and get rid of the BugtrackingController
     * @return
     */
    public abstract BugtrackingController getController(Q q);

    /**
     * Returns true if query is saved
     * @return
     */
    public abstract boolean isSaved(Q q);

    public abstract Collection<I> getIssues(Q q);

    /**
     * Returns true if the issue does belong to the query
     * @param issue
     * @return
     */
    public abstract boolean contains(Q q, String id);

//    /**
//     * 
//     * @param issue
//     * @return 
//     * @deprecated
//     */
//    // XXX used only by issue table - move out from spi    
//    public abstract int getIssueStatus(Q q, I i);

    /*********
     * EVENTS
     *********/

    public abstract void removePropertyChangeListener(Q q, PropertyChangeListener listener);

    public abstract void addPropertyChangeListener(Q q, PropertyChangeListener listener);

    public abstract void setContext(Q q, Node[] nodes);

}
