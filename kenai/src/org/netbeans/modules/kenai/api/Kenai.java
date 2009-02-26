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

package org.netbeans.modules.kenai.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.codeviation.commons.patterns.Factory;
import org.codeviation.commons.utils.Iterators;
import org.netbeans.modules.kenai.FeatureData;
import org.netbeans.modules.kenai.KenaiREST;
import org.netbeans.modules.kenai.KenaiImpl;
import org.netbeans.modules.kenai.LicensesListData;
import org.netbeans.modules.kenai.ProjectData;
import org.netbeans.modules.kenai.ServicesListData.ServicesListItem;

/**
 * Main entry point to Kenai integration.
 *
 * @author Maros Sandor
 * @author Jan Becicka
 */
public final class Kenai {

    /**
     * fired when user logs in/out
     * getOldValue() returns old PasswordAuthentication or null
     * getNewValue() returns new PasswordAuthentication or null
     */
    public static final String PROP_LOGIN = "login";
    
    private static Kenai instance;
    private PasswordAuthentication auth = null;
    private static URL url;

    HashMap<String, WeakReference<KenaiProject>> projectsCache = new HashMap<String, WeakReference<KenaiProject>>();

    private java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);

    public static synchronized Kenai getDefault() {
        if (instance == null) {
            try {
                Kenai.url = new URL(System.getProperty("kenai.com.url", "https://kenai.com"));
                KenaiImpl impl = new KenaiREST(Kenai.url);
                instance = new Kenai(impl);
            } catch (MalformedURLException ex) {
                throw new RuntimeException(ex);
            }
        }
        return instance;
    }

    private final KenaiImpl     impl;

    Kenai(KenaiImpl impl) {
        this.impl = impl;
    }

    /**
     * Logs an existing user into Kenai. Login session persists until the login method
     * is called again or logout is called. If the login fails then the current session
     * resumes (if any).
     *
     * @param username
     * @param password
     * @throws KenaiException
     */
    public void login(final String username, final char [] password) throws KenaiException {
        PasswordAuthentication old = auth;
        impl.verify(username, password);
        auth = new PasswordAuthentication(username, password);
//        Authenticator.setDefault(new Authenticator() {
//            @Override
//            protected PasswordAuthentication getPasswordAuthentication() {
//                return auth;
//            }
//        });
        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, PROP_LOGIN, old, auth));
    }



    /**
     * Logs out current session
     */
    public void logout() {
        PasswordAuthentication old=auth;
        auth = null;
        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, PROP_LOGIN, old, auth));
    }

    /**
     * Adds listener to Kenai instance
     * @param l
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    /**
     * Adds listener to Kenai instance
     * @param name 
     * @param l
     */
    public synchronized void addPropertyChangeListener(String name, PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(name,l);
    }

    /**
     * Removes listener from Kenai instance
     * @param l
     */
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    /**
     * Removes listener from Kenai instance
     * @param name
     * @param l
     */
    public synchronized void removePropertyChangeListener(String name, PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(name, l);
    }

    /**
     * Creates a new account in the Kenai system. Note that you must call login() to start
     * using these new credentials.
     *
     * @param username username to use
     * @param password password to use
     * @throws org.netbeans.modules.kenai.api.KenaiException
     */
    public void register(String username, char [] password) throws KenaiException {
        impl.register(username, password);
    }

    /**
     * Search for Kenai projects on the Kenai server. The format of the search pattern is as follows:
     *
     * @param pattern search pattern. Only one method is recognized now: substring match
     * @return an interator over kenai domains that match given search pattern
     * @throws KenaiException
     */
    public Collection<KenaiProject> searchProjects(String pattern) throws KenaiException {
        Collection<ProjectData> prjs = impl.searchProjects(pattern);
        return new LazyCollection(prjs);
    }

    /**
     *
     * @return
     * @throws org.netbeans.modules.kenai.api.KenaiException
     */
    public Collection<KenaiLicense> getLicenses() throws KenaiException {
        Collection<LicensesListData.LicensesListItem> licenses = impl.getLicenses();
        return new LazyCollection(licenses);
    }

    /**
     * 
     * @return
     * @throws org.netbeans.modules.kenai.api.KenaiException
     */
    public Collection<KenaiService> getServices() throws KenaiException {
        Collection<ServicesListItem> services = impl.getServices();
        return new LazyCollection(services);
    }


    /**
     * Get information about a specific project.
     *
     * @param name name of the project
     * @return KenaiProject
     * @throws KenaiException
     */
    public KenaiProject getProject(String name) throws KenaiException {
        ProjectData prj = impl.getProject(name);
        return KenaiProject.get(prj);
    }

    ProjectData getDetails(String name) throws KenaiException {
        return impl.getProject(name);
    }

    /**
     * Creates a new Kenai domain on the Kenai server
     *
     * @param name name of the project
     * @param displayName display name of the project
     * @param description project description
     * @param licenses array of licenses hashes
     * @param tags comma separated tags
     * @return instance of KenaiProject
     * @throws org.netbeans.modules.kenai.api.KenaiException
     */
    public KenaiProject createProject(
            String name,
            String displayName,
            String description,
            String[] licenses,
            String tags
            ) throws KenaiException {
        if (auth.getUserName()== null) {
            throw new KenaiException("Guest user is not allowed to create new domains");
        }
        ProjectData prj = impl.createProject(name, displayName, description, licenses, tags);
        return KenaiProject.get(prj);
    }

    /**
     * 
     * @param projectName
     * @param name
     * @param display_name
     * @param description
     * @param service
     * @param url
     * @param repository_url
     * @param browse_url
     * @return
     * @throws org.netbeans.modules.kenai.api.KenaiException
     */
    KenaiProjectFeature createProjectFeature(
            String projectName,
            String name,
            String display_name,
            String description,
            String service,
            String url,
            String repository_url,
            String browse_url
            ) throws KenaiException {
        if (getPasswordAuthentication() == null) {
            throw new KenaiException("Guest user is not allowed to create new domains");
        }
        FeatureData prj = impl.createProjectFeature(
                projectName,
                name,
                display_name,
                description,
                url,
                repository_url,
                browse_url,
                service);
        return new KenaiProjectFeature(prj);
    }

    String checkName(String name) throws KenaiException {
        return impl.checkName(name);
    }

    /**
     * is currently logged user authorized for given activity on given project?
     * @param project
     * @param activity
     * @return
     * @throws org.netbeans.modules.kenai.api.KenaiException
     */
    public boolean isAuthorized(KenaiProject project, KenaiActivity activity) throws KenaiException {
        return impl.isAuthorized(project.getName(), activity.getFeature().getId(), activity.getName());
    }

    /**
     * @return instance of PasswordAuthentication class holding current name 
     * and passord. If user is not logged in, method returns null;
     */
    public PasswordAuthentication getPasswordAuthentication() {
        return auth;
    }

    /**
     * get my projects of logged user
     * @return collection of projects
     * @throws org.netbeans.modules.kenai.api.KenaiException
     */
    public Collection<KenaiProject> getMyProjects() throws KenaiException {
        Collection<ProjectData> prjs = impl.getMyProjects();
        return new LazyCollection(prjs);
    }

    Collection<KenaiProject> loadProjects() {
        return Persistence.getInstance().loadProjects();
    }

    void storeProjects(Collection<KenaiProject> projects) {
        Persistence.getInstance().storeProjects(projects);
    }

    private class LazyCollection<I,O> extends AbstractCollection<O> {

        private Collection<I> delegate;

        private LazyCollection(Collection<I> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Iterator<O> iterator() {
            return Iterators.translating(delegate.iterator(), new Factory<O,I>() {
                public O create(I param) {
                    if (param instanceof ProjectData) {
                        return (O) KenaiProject.get((ProjectData) param);
                    } else if (param instanceof LicensesListData.LicensesListItem) {
                        return (O) new KenaiLicense((LicensesListData.LicensesListItem) param);
                    } else if (param instanceof ServicesListItem) {
                        return (O) new KenaiService((ServicesListItem) param);
                    }
                    throw new IllegalStateException();
                }
            });
        }

        @Override
        public int size() {
            return delegate.size();
        }
        
    }
}
