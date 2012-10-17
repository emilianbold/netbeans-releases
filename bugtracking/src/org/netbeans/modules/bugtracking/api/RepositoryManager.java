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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.RepositoryRegistry;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;

/**
 * Providing access to registered {@link Repository}-s and related functionality.
 * 
 * @author Tomas Stupka
 */
public final class RepositoryManager {

    /**
     * Name of the <code>PropertyChangeEvent</code> notifying that a repository was created or removed, where:<br>
     * <ul>
     *  <li><code>old value</code> - a Collection of all repositories before the change</li> 
     *  <li><code>new value</code> - a Collection of all repositories after the change</li> 
     * </ul>
     */
    public static final String EVENT_REPOSITORIES_CHANGED = "bugtracking.repositories.changed"; // NOI18N
    
    private static RepositoryManager instance;
    private static RepositoryRegistry impl;
    
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    
    private RepositoryManager () {
        impl = RepositoryRegistry.getInstance();
        impl.addPropertyChangeListener(new RepositoryListener());
    }
    
    /**
     * Returns the only existing <code>RepositoryManager</code> instance.
     * @return a RepositoryManager
     */
    public static synchronized RepositoryManager getInstance() {
        if(instance == null) {
            instance = new RepositoryManager();
        }
        return instance;
    }
    
    /**
     * Add a listener for repository related changes.
     * 
     * @param l the new listener
     */
    public void addPropertChangeListener(PropertyChangeListener l) {
        changeSupport.addPropertyChangeListener(l);
    }
    
    /**
     * Remove a listener for repository related changes.
     * 
     * @param l the new listener
     */
    public void removePropertChangeListener(PropertyChangeListener l) {
        changeSupport.removePropertyChangeListener(l);
    }
    
    /**
     * Returns all registered repositories
     * 
     * @return 
     */
    public Collection<Repository> getRepositories() {
        Collection<Repository> ret = toRepositories(impl.getRepositories());
        return ret;
    }
    
    /**
     * Returns all registered repositories for a connector with the given id
     * @param connectorId
     * @return 
     */
    public Collection<Repository> getRepositories(String connectorId) {
        Collection<Repository> ret = toRepositories(RepositoryRegistry.getInstance().getRepositories(connectorId));
        return ret;
    }
    
    /**
     * Opens the modal create repository dialog and eventually returns a repository.<br>
     * Blocks until the dialog isn't closed. 
     * 
     * @return a repository in case it was properly specified n the ui, otherwise null
     */
    public Repository createRepository() {
        RepositoryImpl impl = BugtrackingUtil.createRepository(false);
        return impl != null ? impl.getRepository() : null;
    }
    
    private Collection<Repository> toRepositories(Collection<RepositoryImpl> impls) {
        Collection<Repository> ret = new ArrayList<Repository>(impls.size());
        for (RepositoryImpl impl : impls) {
            ret.add(impl.getRepository());
        }
        return ret;
    }
    
    private class RepositoryListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if(EVENT_REPOSITORIES_CHANGED.equals(evt.getPropertyName())) {
                Collection<RepositoryImpl> newImpls = (Collection<RepositoryImpl>) evt.getNewValue();
                Collection<RepositoryImpl> oldImpls = (Collection<RepositoryImpl>) evt.getOldValue();
                changeSupport.firePropertyChange(EVENT_REPOSITORIES_CHANGED, toRepositories(oldImpls), toRepositories(newImpls));
            }
        }
    }
}
