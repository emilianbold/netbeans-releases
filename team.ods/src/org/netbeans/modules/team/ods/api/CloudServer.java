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
package org.netbeans.modules.team.ods.api;

import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.project.Project;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.swing.Icon;
import org.netbeans.modules.team.ods.client.api.ODSFactory;
import org.netbeans.modules.team.ods.client.api.ODSClient;
import org.netbeans.modules.team.ods.client.api.ODSException;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Ondrej Vrabec
 */
public final class CloudServer {
    
    /**
     * fired when user logs in/out
     * getOldValue() returns old PasswordAuthentication or null
     * getNewValue() returns new PasswordAuthentication or null
     */
    public static final String PROP_LOGIN = "login";
    
    /**
     * fired when user login started
     */
    public static final String PROP_LOGIN_STARTED = "login_started";

    /**
     * fired when user login failed
     */
    public static final String PROP_LOGIN_FAILED = "login_failed";
    
    
    private java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);
    private final URL url;
    private final String displayName;
    private Icon icon;
    private PasswordAuthentication auth;
    private Profile currentProfile;
    final HashMap<String, ODSProject> projectsCache = new HashMap<String, ODSProject>();

    private CloudServer (String displayName, String url) throws MalformedURLException {
        while (url.endsWith("/")) { //NOI18N
            url = url.substring(0, url.length() - 1);
        }
        this.displayName = displayName;
        this.url = new URL(url);
    }

    static CloudServer createInstance (String displayName, String url) throws MalformedURLException {
        return new CloudServer(displayName, url);
    }
    
    /**
     * Adds listener to the server instance
     * @param l
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    /**
     * Adds listener to the server instance
     * @param name 
     * @param l
     */
    public void addPropertyChangeListener(String name, PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(name,l);
    }

    /**
     * Removes listener from the server instance
     * @param l
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    /**
     * Removes listener from the server instance
     * @param name
     * @param l
     */
    public void removePropertyChangeListener(String name, PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(name, l);
    }

    public URL getUrl () {
        return url;
    }

    public String getDisplayName () {
        return displayName;
    }

    public Icon getIcon () {
        if (icon == null) {
            icon = ImageUtilities.loadImageIcon("org/netbeans/modules/team/ods/resources/server.png", false); //NOI18N
        }
        return icon;
    }

    public void logout () {
        PasswordAuthentication old = auth;
        synchronized(this) {
            auth = null;
            currentProfile = null;
            projectsCache.clear();
        }
        PropertyChangeEvent propertyChangeEvent = new PropertyChangeEvent(this, PROP_LOGIN, old, auth);
        firePropertyChange(propertyChangeEvent);
    }

    public boolean isLoggedIn () {
        return auth != null;
    }

    public PasswordAuthentication getPasswordAuthentication () {
        return auth;
    }

    public void login (String username, char[] password) throws ODSException {
        PasswordAuthentication old = auth;
        firePropertyChange(new PropertyChangeEvent(this, PROP_LOGIN_STARTED, null, username));
        ODSClient createClient = null;
        synchronized (this) {
            createClient = ODSFactory.getInstance().createClient(getUrl().toString(), new PasswordAuthentication(username, password.clone()));

            currentProfile = createClient.getCurrentProfile();

            auth = new PasswordAuthentication(username, password.clone());
            Arrays.fill(password, '\0');
        }
        // XXX ts - need perhaps a different way how to determine if failed or not
        if(currentProfile == null) {
            firePropertyChange(new PropertyChangeEvent(this, PROP_LOGIN_FAILED, null, null));
        } else {
            firePropertyChange(new PropertyChangeEvent(this, PROP_LOGIN, old, auth));
        }    
    }

    private void firePropertyChange (PropertyChangeEvent event) {
        propertyChangeSupport.firePropertyChange(event);
        CloudServerManager.getDefault().propertyChangeSupport.firePropertyChange(event);
    }

    public void refresh(ODSProject odsProject) throws ODSException {
        if(!isLoggedIn()) {
            return;
        }
        ODSClient client = ODSFactory.getInstance().createClient(getUrl().toString(), getPasswordAuthentication());
        Project p = client.getProjectById(odsProject.getId());
        odsProject.setProject(p);
    }
    
    public Collection<ODSProject> getMyProjects(boolean force) throws ODSException {
        if(!isLoggedIn()) {
            return Collections.EMPTY_LIST;
        }
        ODSClient client = ODSFactory.getInstance().createClient(getUrl().toString(), getPasswordAuthentication());
        synchronized(projectsCache) {
            if(force || projectsCache.isEmpty()) { 
                List<Project> ps = client.getMyProjects();
                if(ps == null) {
                    return Collections.EMPTY_LIST;
                }
                Collection<ODSProject> ret = new ArrayList<ODSProject>(ps.size());
                for (Project project : ps) {
                    ODSProject p = new ODSProject(project, this);
                    ret.add(p);
                    projectsCache.put(project.getIdentifier(), p);
                }
                return ret;
            } else {
                return Collections.unmodifiableCollection(projectsCache.values());
            }
        }   
    }

    public Collection<ODSProject> getMyProjects() throws ODSException {
        if(!isLoggedIn()) {
            return Collections.EMPTY_LIST;
        }
        synchronized(projectsCache) {
            if(projectsCache.isEmpty()) {
                return getMyProjects(true);
            } else {
                return Collections.unmodifiableCollection(projectsCache.values());
            }
        }
    }
    
}
