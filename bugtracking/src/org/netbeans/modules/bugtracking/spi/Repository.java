/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.spi;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import org.netbeans.modules.bugtracking.ui.nodes.RepositoryNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * 
 * Represents a bug tracking repository (server)
 * 
 * @author Tomas Stupka, Jan Stola
 */
public abstract class Repository implements Lookup.Provider {

    private RepositoryNode node;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    /**
     * a query from this repository was saved or removed
     */
    public final static String EVENT_QUERY_LIST_CHANGED = "bugtracking.repository.queries.changed"; // NOI18N

    /**
     * Repository's attributes have changed, e.g. name, url, etc.
     * Old and new value are maps of changed doubles: attribute-name / attribute-value.
     * Old value can be null in case the repository is created.
     */
    public final static String EVENT_ATTRIBUTES_CHANGED = "bugtracking.repository.attributes.changed"; //NOI18N

    /**
     * Returns the icon for this repository
     * @return
     */
    public abstract Image getIcon();

    /**
     * Returns the display name for this repository
     * @return
     */
    public abstract String getDisplayName();

    /**
     * Returs the tooltip for this repository
     * @return
     */
    public abstract String getTooltip();

    /**
     * Returns a unique ID for this repository
     * 
     * @return
     */
    public abstract String getID();

    /**
     * Returns a Node representing this repository
     * @return
     */
    public final Node getNode() {
        if(node == null) {
            node = new RepositoryNode(this);
        }
        return node;
    }

    /**
     * Returns the repositories url
     * @return
     */
    public abstract String getUrl();

    /**
     * Returns an issue with the given ID
     *
     * XXX add flag refresh
     *
     * @param id
     * @return
     */
    public abstract Issue getIssue(String id);

    /**
     * Removes this repository from its connector
     *
     */
    public abstract void remove();

    /**
     * Returns the {@link BugtrackignController} for this repository
     * @return
     */
    public abstract BugtrackingController getController();

    /**
     * Creates a new query instance. Might block for a longer time.
     *
     * @return a new Query instance or null if it's not possible
     * to access the repository.
     */
    public abstract Query createQuery(); 

    /**
     * Creates a new Issue instance. Might block for a longer time.
     *
     * @return return a new Issue instance or null if it's not possible
     * to access the repository.
     */
    public abstract Issue createIssue();

    /**
     * Returns all saved queries
     * @return
     */
    public abstract Query[] getQueries();

    /**
     * Returns all known repository users.
     *
     * @return all known repository users.
     */
    public abstract Collection<RepositoryUser> getUsers();

    /**
     * Runs a query against the bugtracking repository to get all issues
     * which applies that their ID or summary contains the given criteria string
     *
     * @param criteria
     */
    public abstract Issue[] simpleSearch(String criteria);

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /**
     * Notify listeners on this repository that a query was either removed or saved
     * XXX make use of new/old value
     */
    protected void fireQueryListChanged() {
        support.firePropertyChange(EVENT_QUERY_LIST_CHANGED, null, null);
    }

    /**
     * Notify listeners on this repository that some of repository's attributes have changed.
     * @param oldValue map of old attributes
     * @param newValue map of new attributes
     */
    protected void fireAttributesChanged (java.util.Map<String, Object> oldAttributes, java.util.Map<String, Object> newAttributes) {
        support.firePropertyChange(new java.beans.PropertyChangeEvent(this, EVENT_ATTRIBUTES_CHANGED, oldAttributes, newAttributes));
    }

}
