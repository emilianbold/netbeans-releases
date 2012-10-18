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

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.QueryImpl;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.spi.RepositoryProvider;

/**
 *
 * @author Tomas Stupka
 */
public final class Repository {
    /**
     * A query from this repository was saved or removed
     */
    public final static String EVENT_QUERY_LIST_CHANGED = RepositoryProvider.EVENT_QUERY_LIST_CHANGED;

    /**
     * RepositoryProvider's attributes have changed, e.g. name, url, etc.
     * Old and new value are maps of changed doubles: attribute-name / attribute-value.
     * Old value can be null in case the repository is created.
     */
    public final static String EVENT_ATTRIBUTES_CHANGED = "bugtracking.repository.attributes.changed"; //NOI18N

    public static final String ATTRIBUTE_URL = "repository.attribute.url"; //NOI18N
    public static final String ATTRIBUTE_DISPLAY_NAME = "repository.attribute.displayName"; //NOI18N

    
    static {
        APIAccessorImpl.createAccesor();
    }
    
    private final RepositoryImpl<?, ?, ?> impl;

    <R, Q, I> Repository(RepositoryImpl<R, Q, I> impl) {
        this.impl = impl;
    }

    /**
     * Returns the icon for this repository
     * 
     * @return 
     */
    public Image getIcon() {
        return impl.getIcon();
    }

    /**
     * Returns the display name for this repository
     * @return 
     */
    public String getDisplayName() {
        return impl.getDisplayName();
    }

    /**
     * Returns the tooltip describing this repository
     * 
     * @return 
     */
    public String getTooltip() {
        return impl.getTooltip();
    }

    /**
     * Returns a unique id assotiated with this repository
     * 
     * @return 
     */
    public String getId() {
        return impl.getId();
    }

    /**
     * Returns this repository url
     * 
     * @return 
     */
    public String getUrl() {
        return impl.getUrl();
    }

    /**
     * Returns a list of all saved queries for this repository
     * 
     * @return 
     */
    public Collection<Query> getQueries() {
        Collection<QueryImpl> c = impl.getQueries();
        Collection<Query> ret = new ArrayList<Query>();
        for (QueryImpl q : c) {
            ret.add(q.getQuery());
        }
        return ret;
    }

    /**
     * Removes this repository
     */
    public void remove() {
        impl.remove();
    }

    /**
     * Registers a PropertyChangeListener 
     * 
     * @param listener 
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        impl.addPropertyChangeListener(listener);
    }
    
    /**
     * Unregisters a PropertyChangeListener 
     * 
     * @param listener 
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        impl.removePropertyChangeListener(listener);
    }

    /**
     * Returns the issue with the given id or null in case such doens't exist.
     * 
     * @param id
     * @return 
     */
    public Issue[] getIssues(String... ids) {
        Collection<IssueImpl> impls = impl.getIssueImpls(ids);
        List<Issue> ret = new ArrayList<Issue>(impls.size());
        for (IssueImpl issueImpl : impls) {
            ret.add(issueImpl.getIssue());
        }
        return ret.toArray(new Issue[ret.size()]);
    }
    
    <R, Q, I> RepositoryImpl<R, Q, I> getImpl() {
        return (RepositoryImpl<R, Q, I>) impl;
    }
}
