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
package org.netbeans.modules.ods.api;

import com.tasktop.c2c.server.cloud.domain.ServiceType;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.domain.project.ProjectService;
import com.tasktop.c2c.server.profile.domain.project.WikiMarkupLanguage;
import com.tasktop.c2c.server.scm.domain.ScmRepository;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.ods.client.api.ODSFactory;
import org.netbeans.modules.ods.client.api.ODSClient;
import org.netbeans.modules.ods.client.api.ODSException;

/**
 * Represents a cloud project
 * 
 * @author Tomas Stupka
 */
public final class ODSProject {
   
    /**
     * getSource() returns project being refreshed
     * values are undefined
     */
    public static final String PROP_PROJECT_CHANGED = "project_change";

    /**
     * getSource() returns project being deleted
     * values are undefined
     */
    public static final String PROP_PROJECT_REMOVED = "project_removed";

    private java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);
    
    private Project project;
    private final CloudServer server;

    private List<ScmRepository> repositories;
    private final Object REPOSITORIES_LOCK = new Object();
    
    ODSProject(Project project, CloudServer server) {
        this.project = project;
        this.server = server;
    }

    public synchronized String getName() {
        return project.getName();
    }

    public String getDescription() {
        String description = project.getDescription();
        return description == null ? "" : description; //NOI18N
    }
    
    public synchronized String getId() {
        return project.getIdentifier();
    }

    public synchronized CloudServer getServer() {
        return server;
    }
    /**
     * Returns all known ScmRepository-s in case the project has a SCM service type. Otherwise null.
     * This method might block awt.
     * 
     * @return
     * @throws CloudException 
     */
    public Collection<ScmRepository> getRepositories() throws ODSException {
        assertLoggedIn();
        if(!hasScm()) {
            return null;
        }
        if(!server.isLoggedIn()) {
            return Collections.emptyList();
        }
        synchronized(REPOSITORIES_LOCK) {
            if(repositories == null) {
                ODSClient client = ODSFactory.getInstance().createClient(server.getUrl().toString(), server.getPasswordAuthentication());
                repositories = client.getScmRepositories(project.getIdentifier());
                if(repositories == null) {
                    repositories = Collections.emptyList();
                } 
            }
            return Collections.unmodifiableCollection(repositories);
        }
    }
    
    void setProject(Project project) {
        synchronized(this) {
            this.project = project;
            synchronized(REPOSITORIES_LOCK) {
                repositories = null;
            }
        }
        propertyChangeSupport.firePropertyChange(PROP_PROJECT_CHANGED, null, null);
    }
   
    @Override
    public synchronized boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ODSProject other = (ODSProject) obj;
        if ((this.project.getIdentifier() == null) ? (other.project.getIdentifier() != null) : !this.project.getIdentifier().equals(other.project.getIdentifier())) {
            return false;
        }
        return true;
    }

    @Override
    public synchronized int hashCode() {
        int hash = 5;
        hash = 13 * hash + (this.project.getIdentifier() != null ? this.project.getIdentifier().hashCode() : 0);
        return hash;
    }

    @Override
    public synchronized String toString() {
        return "ODSProject " + project.getIdentifier(); // NOI18N
    }

    /**
     * @param l
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    /**
     * @param name
     * @param l
     */
    public synchronized void addPropertyChangeListener(String name, PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(name,l);
    }

    /**
     * @param l
     */
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }    

    public boolean hasBuild() {
        return hasService(ServiceType.BUILD); // XXX BUILD_SLAVE
    }
    
    public boolean hasWiki() {
        return hasService(ServiceType.WIKI);
    }
    
    public boolean hasScm() {
        return hasService(ServiceType.SCM);
    }

    public boolean hasTasks () {
        return hasService(ServiceType.TASKS);
    }

    public synchronized String getBuildUrl() {
        List<ProjectService> s = project.getProjectServicesOfType(ServiceType.BUILD);
        if(s != null) {
            for (ProjectService ps : s) {
                if(ps.isAvailable()) {
                    return ps.getUrl();
                }
            }
        } 
        return null;
    }
    
    public synchronized String getScmUrl() {
        List<ProjectService> s = project.getProjectServicesOfType(ServiceType.SCM);
        if (s != null) {
            for (ProjectService ps : s) {
                if (ps.isAvailable()) {
                    return ps.getUrl();
                }
            }
        }
        return null;
    }
    
    public synchronized String getTaskUrl () {
        List<ProjectService> s = project.getProjectServicesOfType(ServiceType.TASKS);
        if (s != null) {
            for (ProjectService ps : s) {
                if (ps.isAvailable()) {
                    return ps.getUrl();
                }
            }
        }
        return null;
    }

    public synchronized String getWikiUrl() {
        List<ProjectService> s = project.getProjectServicesOfType(ServiceType.WIKI);
        if (s != null) {
            for (ProjectService ps : s) {
                if (ps.isAvailable()) {
                    return ps.getUrl();
                }
            }
        }
        return null;
    }

    public synchronized String getMavenUrl() {
        List<ProjectService> s = project.getProjectServicesOfType(ServiceType.MAVEN);
        if (s != null) {
            for (ProjectService ps : s) {
                if (ps.isAvailable()) {
                    return ps.getUrl();
                }
            }
        }
        return null;
    }

    public synchronized String getWebUrl() {
        return server.getUrl().toString() + "/#projects/" +  project.getIdentifier();
    }

    public synchronized String getWikiLanguage () {
        WikiMarkupLanguage wikiLanguage = project.getProjectPreferences().getWikiLanguage();
        return wikiLanguage == null ? null : wikiLanguage.toString();
    }

    public static ODSProject findProjectForRepository (String uri) throws ODSException {
        Map.Entry<CloudServer, String> pair = CloudServer.findServerAndProjectForRepository(uri);
        if (pair == null || pair.getValue() == null) {
            return null;
        } else {
            return pair.getKey().getProject(pair.getValue(), true);
        }
    }

    private synchronized boolean hasService(ServiceType type) {
        List<ProjectService> s = project.getProjectServicesOfType(type);
        if(s != null) {
            for (ProjectService ps : s) {
                if(ps.isAvailable()) {
                    return true;
                }
            }
        } 
        return false;
    }

    private void assertLoggedIn() {
        assert server.isLoggedIn() : "esure login before requesting any service"; // NOI18N
    }

}
