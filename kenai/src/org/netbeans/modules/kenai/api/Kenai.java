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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import org.codeviation.commons.patterns.Factory;
import org.codeviation.commons.utils.Iterators;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.netbeans.modules.kenai.FeatureData;
import org.netbeans.modules.kenai.KenaiREST;
import org.netbeans.modules.kenai.KenaiImpl;
import org.netbeans.modules.kenai.LicensesListData;
import org.netbeans.modules.kenai.ProjectData;
import org.netbeans.modules.kenai.ServicesListData.ServicesListItem;
import org.netbeans.modules.kenai.UserData;

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

    public static final String PROP_XMPP_LOGIN = "xmpp_login";

    /**
     * fired when user login started
     */
    public static final String PROP_LOGIN_STARTED = "login_started";

    /**
     * fired when user login started
     */
    public static final String PROP_XMPP_LOGIN_STARTED = "xmpp_login_started";


    /**
     * fired when user login failed
     */
    public static final String PROP_LOGIN_FAILED = "login_failed";

    public static final String PROP_XMPP_LOGIN_FAILED = "xmpp_login_failed";

    private static Kenai instance;
    private PasswordAuthentication auth = null;
    private static URL url;

    final HashMap<String, WeakReference<KenaiProject>> projectsCache = new HashMap<String, WeakReference<KenaiProject>>();

    private java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);

    /**
     * Singleton instance of Kenai
     * @return singleton instance
     */
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

    /**
    * Helper method that fixed slash on the Kenai.com url
    * @param urlStr String with URL as it was passed
    * @return Fixed URL string
    */
    public static String normalizeUrl(String urlStr) {
        if (urlStr.endsWith("/")) {
            return urlStr.substring(0, urlStr.length() - 1);
        } else {
            return urlStr;
        }
    }

    private final KenaiImpl     impl;
    private XMPPConnection xmppConnection;

    Kenai(KenaiImpl impl) {
        this.impl = impl;
    }

    public synchronized XMPPConnection getXMPPConnection() {
        return xmppConnection;
    }

    private static final String XMPP_SERVER = System.getProperty("kenai.com.url","https://kenai.com").substring(System.getProperty("kenai.com.url","https://kenai.com").lastIndexOf("/")+1);

    /**
     * Logs an existing user into Kenai. Login session persists until the login method
     * is called again or logout is called. If the login fails then the current session
     * resumes (if any).
     *
     * @param username
     * @param password
     * @throws KenaiException
     */
    public void login(final String username, final char [] password, boolean xmppLogin) throws KenaiException {
        if (this.auth!=null) {
            if (Arrays.equals(password, auth.getPassword()) && username.equals(auth.getUserName())) {
                if (xmppLogin && xmppConnection!=null && xmppConnection.isConnected()) {
                    //already connected;
                    return;
                } else if (xmppLogin) {
                    xmppConnect();
                    return;
                }
                if (!xmppLogin && xmppConnection==null) {
                    //already connected without xmpp
                    return;
                } else {
                    xmppDisconnect();
                    return;
                }
            }
        }
        PasswordAuthentication old = auth;
        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, PROP_LOGIN_STARTED, null, null));
        try {
            synchronized (this) {
                String shortName = impl.verify(username, password);
                auth = new PasswordAuthentication(shortName, password);
                myProjects=null;
                if (xmppLogin)
                    xmppConnect();
            }
        } catch (KenaiException ke) {
            propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, PROP_LOGIN_FAILED, null, null));
            throw ke;
        }
        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, PROP_LOGIN, old, auth));
    }

    public void login(final String username, final char [] password) throws KenaiException {
        login(username, password, true);
    }


    /**
     * Logs out current session
     */
    public void logout() {
        PasswordAuthentication old=auth;
        auth = null;
        synchronized(this) {
            myProjects=null;
            xmppDisconnect();
        }
        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, PROP_LOGIN, old, auth));
    }

    /**
     * Adds listener to Kenai instance
     * @param l
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    /**
     * Adds listener to Kenai instance
     * @param name 
     * @param l
     */
    public void addPropertyChangeListener(String name, PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(name,l);
    }

    /**
     * Removes listener from Kenai instance
     * @param l
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    /**
     * Removes listener from Kenai instance
     * @param name
     * @param l
     */
    public void removePropertyChangeListener(String name, PropertyChangeListener l) {
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

    Collection<KenaiUser> getProjectMembers(String name) throws KenaiException {
        Collection<UserData> usrs = impl.getProjectMembers(name);
        return new LazyCollection(usrs);
    }


    /**
     * Getter for collection of available licences
     * @return lazy collection of available licences
     * @throws org.netbeans.modules.kenai.api.KenaiException
     */
    public Collection<KenaiLicense> getLicenses() throws KenaiException {
        Collection<LicensesListData.LicensesListItem> licenses = impl.getLicenses();
        return new LazyCollection(licenses);
    }

    /**
     * Getter for collection of available services
     * @return lazy collection of available services
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
     * @param forceServerReload if true -> data will be downloaded from server.
     * Otherwise data are returned from cache.
     * @return KenaiProject
     * @throws KenaiException
     */
    public KenaiProject getProject(String name, boolean forceServerReload) throws KenaiException {
        if (forceServerReload) {
            return _getProject(name);
        } else {
            KenaiProject result = KenaiProject.get(name);
            if (result!=null) {
                return result;
            }
            return _getProject(name);
        }
    }

    /**
     * Get information about a specific project.
     *
     * @param name name of the project
     * @return instance of KenaiProject from cache or downloads KenaiProject
     * from server if requested project is not available in cache
     * @throws KenaiException
     */
    public KenaiProject getProject(String name) throws KenaiException {
        return getProject(name, false);
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
        final KenaiProject result = KenaiProject.get(prj);
        synchronized(this) {
            if (myProjects!=null)
                myProjects.add(result);
        }
        return result;
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
    KenaiFeature createProjectFeature(
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
        return new KenaiFeature(prj);
    }

    String checkName(String name) throws KenaiException {
        return impl.checkName(name);
    }

    /**
     * is currently logged user authorized for given activity on given project?
     * @param project
     * @param activity
     * @return true if author is authorized to perform given activity,
     * false otherwise
     * @throws org.netbeans.modules.kenai.api.KenaiException
     */
    public boolean isAuthorized(KenaiProject project, KenaiActivity activity) throws KenaiException {
        return impl.isAuthorized(project.getName(), activity.getFeature().getId(), activity.getName());
    }

    /**
     * Getter for PasswordAuthentication of logged in user. Returns null of user
     * is not logged in. 
     * @return instance of PasswordAuthentication class holding current name
     * and passord. If user is not logged in, method returns null;
     */
    public PasswordAuthentication getPasswordAuthentication() {
        return auth;
    }

    private Collection<KenaiProject> myProjects = null;
    /**
     * get my projects of logged user
     * @return collection of projects
     * @throws org.netbeans.modules.kenai.api.KenaiException
     */
    public synchronized Collection<KenaiProject> getMyProjects() throws KenaiException {
        if (auth==null)
            return Collections.emptyList();
        if (myProjects!=null)
            return myProjects;
        return getMyProjects(true);
    }

    public Status getStatus() {
        if (auth==null) {
            return Status.OFFLINE;
        }
        if (xmppConnection==null) {
            return Status.LOGGED_IN;
        }
        return Status.ONLINE;
    }

    /**
     * get my projects of logged user
     * @param forceServerReload
     * @return collection of projects
     * @throws org.netbeans.modules.kenai.api.KenaiException
     */
    public synchronized Collection<KenaiProject> getMyProjects(boolean forceServerReload) throws KenaiException {
        if (auth==null)
            return Collections.emptyList();
        if (forceServerReload==false) {
                return getMyProjects();
            }
            Collection<ProjectData> prjs = impl.getMyProjects();
            myProjects = new LinkedList<KenaiProject>(new LazyCollection(prjs));
            return myProjects;
        }


    Collection<KenaiProject> loadProjects() {
        return Persistence.getInstance().loadProjects();
    }

    void storeProjects(Collection<KenaiProject> projects) {
        Persistence.getInstance().storeProjects(projects);
    }

    private KenaiProject _getProject(String name) throws KenaiException {
        ProjectData prj = impl.getProject(name);
        return KenaiProject.get(prj);
    }

    private static class LazyCollection<I,O> extends AbstractCollection<O> {

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
                    } else if (param instanceof UserData) {
                        return (O) new KenaiUser((UserData) param);
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

    private void xmppConnect() throws KenaiException {
        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, PROP_XMPP_LOGIN_STARTED, null, null));
        synchronized (this) {
            xmppConnection = new XMPPConnection(XMPP_SERVER);
            try {
                xmppConnection.connect();
                xmppConnection.login(auth.getUserName(), new String(auth.getPassword()), "NetBeans"); //NOI18N
            } catch (XMPPException xMPPException) {
                propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, PROP_XMPP_LOGIN_FAILED, null, null));
                throw new KenaiException(xMPPException);
            }
        }
        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, PROP_XMPP_LOGIN, null, xmppConnection));
    }

    private void xmppDisconnect() {
        if (xmppConnection != null) {
            xmppConnection.disconnect();
        }
        XMPPConnection temp = xmppConnection;
        xmppConnection = null;
        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, PROP_XMPP_LOGIN, temp, null));
    }

    public static enum Status {
        ONLINE,
        LOGGED_IN,
        OFFLINE
    }
}
