/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */

package org.netbeans.modules.team.server.ui.spi;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Abstraction of a single Team project.
 *
 * 
 */
public abstract class ProjectHandle<P> implements Comparable<ProjectHandle> {

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    /**
     * The name of property which when fired will force a complete refresh of
     * all project related info.
     * The property value is undefined.
     */
    public static final String PROP_CONTENT = "content"; // NOI18N
    /**
     * The name of property which is fired when the list of builds for this project
     * has changed (builds added/removed/renamed).
     * The property value should ideally be the new list of BuildHandles.
     */
    public static final String PROP_BUILD_LIST = "buildList"; // NOI18N
    /**
     * The name of property which is fired when the list of source repositories
     * for this project has changed (repos added/removed/renamed).
     * The property value should ideally be the new list of SourceHandles.
     */
    public static final String PROP_SOURCE_LIST = "sourceList"; // NOI18N
    /**
     * The name of property which is fired when the list of queries
     * for this project has changed (queries added/removed/renamed).
     * The property value should ideally be the new list of QueryHandles.
     */
    public static final String PROP_QUERY_LIST = "queryList"; // NOI18N

    /**
     * The name of property which is fired when the nonmember project is removed
     * from dashboard.
     * This event is not fired, when user logs out.
     * Value is undefined (null)
     * @see TeamServer#PROP_LOGIN
     */
    public static final String PROP_CLOSE = "close"; // NOI18N



    private final String id;

    protected ProjectHandle( String id ) {
        this.id = id;
    }

    /**
     *
     * @return Project's unique identification
     */
    public final String getId() {
        return id;
    }

    /**
     *
     * @return Display name
     */
    public abstract String getDisplayName();

    public abstract P getTeamProject();
    
    /**
     * Is this project private?
     * @return 
     */
    public abstract boolean isPrivate();

    public final void addPropertyChangeListener( PropertyChangeListener l ) {
        changeSupport.addPropertyChangeListener(l);
    }

    public final void removePropertyChangeListener( PropertyChangeListener l ) {
        changeSupport.removePropertyChangeListener(l);
    }

    public final void firePropertyChange( String propName, Object oldValue, Object newValue ) {
        changeSupport.firePropertyChange(propName, oldValue, newValue);
    }

    @Override
    public int compareTo( ProjectHandle other ) {
        return getDisplayName().compareToIgnoreCase(other.getDisplayName());
    }

    @Override
    public boolean equals(Object obj) {
        if( obj == null ) {
            return false;
        }
        if( getClass() != obj.getClass() ) {
            return false;
        }
        final ProjectHandle other = (ProjectHandle) obj;
        if( (this.id == null) ? (other.id != null) : !this.id.equals(other.id) ) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return getId();
    }
}

