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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.Icon;
import org.codeviation.commons.patterns.Factory;
import org.codeviation.commons.utils.Iterators;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Presence;
import org.netbeans.modules.kenai.FeatureData;
import org.netbeans.modules.kenai.KenaiREST;
import org.netbeans.modules.kenai.KenaiImpl;
import org.netbeans.modules.kenai.LicensesListData;
import org.netbeans.modules.kenai.ProjectData;
import org.netbeans.modules.kenai.ServicesListData.ServicesListItem;
import org.netbeans.modules.kenai.UserData;
import org.netbeans.modules.kenai.api.KenaiProjectMember.Role;
import org.openide.util.ImageUtilities;

/**
 * Main entry point to Kenai integration.
 *
 * @author Maros Sandor
 * @author Jan Becicka
 */
public final class Kenai implements Comparable<Kenai> {

    /**
     * fired when user logs in/out
     * getOldValue() returns old PasswordAuthentication or null
     * getNewValue() returns new PasswordAuthentication or null
     */
    public static final String PROP_LOGIN = "login"; // NOI18N

    /**
     * fired when user logs int xmpp server
     */
    public static final String PROP_XMPP_LOGIN = "xmpp_login"; // NOI18N

    /**
     * fired when user login started
     */
    public static final String PROP_LOGIN_STARTED = "login_started"; // NOI18N

    /**
     * fired when user login started
     */
    public static final String PROP_XMPP_LOGIN_STARTED = "xmpp_login_started"; // NOI18N


    /**
     * fired when user login failed
     */
    public static final String PROP_LOGIN_FAILED = "login_failed"; // NOI18N

    /**
     * fired when log into xmpp failed
     */
    public static final String PROP_XMPP_LOGIN_FAILED = "xmpp_login_failed"; // NOI18N

    /**
     * never fired
     */
    @Deprecated
    public static final String PROP_URL_CHANGED = "url"; // NOI18N

    private static KenaiImpl createImpl(String urlString) throws MalformedURLException {
        if (!urlString.startsWith("https://")) { // NOI18N
            throw new MalformedURLException("the only supported protocol is https: " + urlString); // NOI18N
        }
        if (urlString.endsWith("/")) { // NOI18N
            urlString = urlString.substring(0, urlString.length() - 1);
        }
        URL url = new URL(urlString);
        KenaiImpl impl = new KenaiREST(url);
        return impl;
    }

    private PasswordAuthentication auth = null;
    private KenaiImpl impl;
    private XMPPConnection xmppConnection;
    private PacketListener packetListener;

    /**
     * users cache <name, instance>
     */
    final HashMap<String, KenaiUser> users = new HashMap();

    /**
     * online users
     */
    final HashSet<String> onlineUsers = new HashSet<String>();

    final HashMap<String, WeakReference<KenaiProject>> projectsCache = new HashMap<String, WeakReference<KenaiProject>>();

    private final java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);

     static synchronized Kenai createInstance(String name, String urlString) throws MalformedURLException {
         Kenai k = new Kenai(createImpl(urlString));
         k.name = name;
         return k;
     }

    /**
     * url of the kenai instance
     * @return
     */
    public URL getUrl() {
        return impl.getUrl();
    }

    private Icon icon;
    public Icon getIcon() {
        //hardcoded icons
        //temporary solution for
        //http://kenai.com/jira/browse/KENAI-1761
        if (icon == null) {
            if (getUrl().getHost().contains("netbeans.org")) { //NOI18N
                icon = ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/resources/netbeans-small.png", false); // NOI18N
            } else if (getUrl().getHost().contains("testkenai.com")) { //NOI18N
                icon = ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/resources/testkenai-small.png", false); // NOI18N
            } else if (getUrl().getHost().contains("odftoolkit.org")) { //NOI18N
                icon = ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/resources/odftoolkit-small.png", false); // NOI18N
            } else if (getUrl().getHost().contains("java.net")) { //NOI18N
                icon = ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/resources/javanet.png", false); // NOI18N
            } else {
                icon = ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/resources/kenai-small.png", false); // NOI18N
            }
        }
        return icon;
//        if (icon==null) {
//            //assert !SwingUtilities.isEventDispatchThread();
//            try {
//                URL url = new URL("http://" + getUrl().getHost() + "/favicon.ico");
//                icon = new ImageIcon(url);
//            } catch (IOException ex) {
//                icon = new ImageIcon();
//            }
//        }
//        return icon;

    }

//    public void setUrl(URL url) {
//        try {
//            if (impl.getUrl().toURI().equals(url.toURI())) {
//                return;
//            }
//        } catch (URISyntaxException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//        if (getStatus()!=Status.OFFLINE) {
//            logout(true);
//        }
//        URL old = impl.getUrl();
//        synchronized (Kenai.class) {
//            impl = new KenaiREST(url);
//        }
//        prefs.put(DEFAULT_INSTANCE_PREF, url.toString());
//        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, PROP_URL_CHANGED, null, impl.getUrl()));
//    }

    private String name;
    
    /**
     * name of this kenai instance
     * @return e.g. java.net, odftoolkit.org, netbeans.org
     */
    public String getName() {
        if (name!=null) {
            return name;
        }
        return getUrl().toString().substring("https://".length());// NOI18N
    }

    Kenai(KenaiImpl impl) {
        this.impl = impl;
    }

    public void setUrl(String url) throws MalformedURLException {
        this.impl = createImpl(url);
    }

    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * getter for xmpp connection. returns null, if user is not Status.ONLINE
     * @return instance of XMPP connection
     */
    public synchronized XMPPConnection getXMPPConnection() {
        return xmppConnection;
    }

    /**
     * Logs an existing user into Kenai. Login session persists until the login method
     * is called again or logout is called. If the login fails then the current session
     * resumes (if any).
     *
     * @param username
     * @param password
     * @param xmppLogin should log into xmpp server?
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
                    xmppDisconnect(false);
                    return;
                }
            }
        }
        PasswordAuthentication old = auth;
        firePropertyChange(new PropertyChangeEvent(this, PROP_LOGIN_STARTED, null, username));
        try {
            synchronized (this) {
                String shortName = impl.verify(username, password);
                auth = new PasswordAuthentication(shortName, password);
                myProjects=null;
                if (xmppLogin)
                    xmppConnect();
            }
        } catch (KenaiException ke) {
            firePropertyChange(new PropertyChangeEvent(this, PROP_LOGIN_FAILED, null, null));
            throw ke;
        }
        firePropertyChange(new PropertyChangeEvent(this, PROP_LOGIN, old, auth));
    }

    private void firePropertyChange(PropertyChangeEvent event) {
        propertyChangeSupport.firePropertyChange(event);
        KenaiManager.getDefault().propertyChangeSupport.firePropertyChange(event);
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
        login(username, password, true);
    }

    /**
     * Logs out current session
     */
    public void logout() {
        logout(false);
    }

    private void logout(boolean setPropId) {
        PasswordAuthentication old=auth;
        synchronized(this) {
            auth = null;
            myProjects=null;
            xmppDisconnect(setPropId);
        }
        PropertyChangeEvent propertyChangeEvent = new PropertyChangeEvent(this, PROP_LOGIN, old, auth);
        if (setPropId) {
            propertyChangeEvent.setPropagationId(PROP_URL_CHANGED);
        }
        firePropertyChange(propertyChangeEvent);
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
        Collection<ProjectData> prjs = impl.searchProjects(pattern, auth);
        return new LazyCollection(prjs);
    }

    Collection<KenaiProjectMember> getProjectMembers(String name) throws KenaiException {
        Collection<UserData> usrs = impl.getProjectMembers(name, auth);
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
            KenaiProject result = KenaiProject.get(this, name);
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
        return impl.getProject(name, auth);
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
            throw new KenaiException("Guest user is not allowed to create new domains"); // NOI18N
        }
        ProjectData prj = impl.createProject(name, displayName, description, licenses, tags, auth);
        final KenaiProject result = KenaiProject.get(this, prj);
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
            throw new KenaiException("Guest user is not allowed to create new domains"); // NOI18N
        }
        FeatureData prj = impl.createProjectFeature(
                projectName,
                name,
                display_name,
                description,
                url,
                repository_url,
                browse_url,
                service,
                auth);
        return new KenaiFeature(prj);
    }


    /**
     * Checks weather proposed name is unique and valid
     * @param name proposed name
     * @return Error message or null, if name is valid
     * @throws org.netbeans.modules.kenai.api.KenaiException
     */
    public String checkProjectName(String name) throws KenaiException {
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
        return impl.isAuthorized(project.getName(), activity.getFeature().getId(), activity.getName(), getPasswordAuthentication());
    }

    /**
     * Getter for PasswordAuthentication of logged in user. Returns null of user
     * is not logged in. 
     * @return instance of PasswordAuthentication class holding current name
     * and password. If user is not logged in, method returns null;
     */
    public PasswordAuthentication getPasswordAuthentication() {
        return auth;
    }

    Collection<KenaiProject> myProjects = null;
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

    /**
     * status of user
     * @see Status
     * @return
     */
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
            Collection<ProjectData> prjs = impl.getMyProjects(auth);
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
        ProjectData prj = impl.getProject(name, auth);
        return KenaiProject.get(this, prj);
    }

    void addMember(KenaiProject project, KenaiUser user, Role role) throws KenaiException {
        assert auth!=null;
        impl.addMember(project.getName(), user.getUserName(), role.toString(), auth);
        synchronized (this) {
            if (myProjects != null) {
                myProjects.add(project);
            }
        }

    }

    void deleteMember(KenaiProject project, KenaiUser user) throws KenaiException {
        assert auth!=null;
        impl.deleteMember(project.getName(), user.data.member_id, auth);
        synchronized (this) {
            if (myProjects != null) {
                myProjects.remove(project);
            }
        }
    }

    void delete(KenaiProject project) throws KenaiException {
        assert auth!=null;
        impl.deleteProject(project.getName(), auth);
        synchronized (this) {
            if (myProjects != null) {
                myProjects.remove(project);
            }
        }
    }

    private class LazyCollection<I,O> extends AbstractCollection<O> {

        private final Collection<I> delegate;

        private LazyCollection(Collection<I> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Iterator<O> iterator() {
            return Iterators.translating(delegate.iterator(), new Factory<O,I>() {
                @Override
                public O create(I param) {
                    if (param instanceof ProjectData) {
                        return (O) KenaiProject.get(Kenai.this, (ProjectData) param);
                    } else if (param instanceof LicensesListData.LicensesListItem) {
                        return (O) new KenaiLicense((LicensesListData.LicensesListItem) param);
                    } else if (param instanceof ServicesListItem) {
                        return (O) new KenaiService((ServicesListItem) param);
                    } else if (param instanceof UserData) {
                        return (O) new KenaiProjectMember(Kenai.this, (UserData) param);
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
        firePropertyChange(new PropertyChangeEvent(this, PROP_XMPP_LOGIN_STARTED, null, null));
        synchronized (this) {
            xmppConnection = new XMPPConnection(getUrl().getHost());
            packetListener = new KenaiUser.KenaiPacketListener();
            try {
                xmppConnection.removePacketListener(packetListener);
                xmppConnection.connect();
                xmppConnection.addPacketListener(packetListener, new PacketTypeFilter(Presence.class));
                xmppConnection.login(auth.getUserName(), new String(auth.getPassword()), "NetBeans"); //NOI18N
            } catch (XMPPException xMPPException) {
                xmppConnection = null;
                firePropertyChange(new PropertyChangeEvent(this, PROP_XMPP_LOGIN_FAILED, null, null));
                throw new KenaiException(xMPPException);
            } catch (IllegalStateException ise) {
                xmppConnection = null;
                firePropertyChange(new PropertyChangeEvent(this, PROP_XMPP_LOGIN_FAILED, null, null));
                throw new KenaiException(ise);
            }
        }
        firePropertyChange(new PropertyChangeEvent(this, PROP_XMPP_LOGIN, null, xmppConnection));
    }

    private void xmppDisconnect(boolean setPropId) {
        if (xmppConnection == null) {
            return;
        }
        xmppConnection.disconnect();

        synchronized (users) {
            users.clear();
        }
        synchronized (onlineUsers) {
            onlineUsers.clear();
        }

        XMPPConnection temp = xmppConnection;
        xmppConnection = null;
        temp.removePacketListener(packetListener);
        PropertyChangeEvent propertyChangeEvent = new PropertyChangeEvent(this, PROP_XMPP_LOGIN, temp, null);
        if (setPropId) {
            propertyChangeEvent.setPropagationId(PROP_URL_CHANGED);
        }
        firePropertyChange(propertyChangeEvent);
    }
    /**
     * user status on kenai
     */
    public static enum Status {
        /**
         * user is logged in, online on chat
         */
        ONLINE,
        /**
         * user is logged in, offline on chat
         */
        LOGGED_IN,
        /**
         * user is not logged in
         */
        OFFLINE
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Kenai other = (Kenai) obj;
        if ((this.getUrl() == null) ? (other.getUrl() != null) : !this.getUrl().toString().equals(other.getUrl().toString())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.getUrl() != null ? this.getUrl().toString().hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(Kenai o) {
        return this.getName().compareToIgnoreCase(o.getName());
    }

    @Override
    public String toString() {
        return getName() + " (" + getUrl().toString() + ")"; // NOI18N
    }
}
