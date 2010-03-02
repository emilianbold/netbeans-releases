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

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import org.netbeans.modules.kenai.FeatureData;
import org.netbeans.modules.kenai.LicenceData;
import org.netbeans.modules.kenai.ProjectData;
import org.netbeans.modules.kenai.api.KenaiService.Type;

/**
 * IDE-side representation of a Kenai project.
 *
 * @author Maros Sandor
 * @author Jan Becicka
 */
public final class KenaiProject {
    /**
     * getSource() returns project being refreshed
     * values are undefined
     */
    public static final String PROP_PROJECT_CHANGED = "project_change";

    /**
     * getNewValue returns instance of KenaiNotification
     */
    public static final String PROP_PROJECT_NOTIFICATION = "project_notification";

    /**
     * getSource() returns project being deleted
     * values are undefined
     */
    public static final String PROP_PROJECT_REMOVED = "project_removed";

    private java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);

    private ProjectData     data;
    private Icon projectIcon;
    
    private KenaiFeature[] features;
    private KenaiProjectMember[] members;
    private KenaiLicense[] licenses;
    private Kenai kenai;
    private boolean deleted = false;

    /**
     * I assume that this constructor does NOT provide full project information. If it does then
     * call fillInfo() just after the object is created.
     *
     * @param p
     */
    private KenaiProject(Kenai kenai, ProjectData p) {
        fillInfo(p);
        this.kenai = kenai;
    }

    static KenaiProject get(Kenai kenai, ProjectData p) {
        synchronized (kenai.projectsCache) {
            WeakReference<KenaiProject> wr = kenai.projectsCache.get(p.name);
            KenaiProject result = null;
            if (wr == null || (result = wr.get()) == null) {
                result = new KenaiProject(kenai, p);
                kenai.projectsCache.put(p.name, new WeakReference<KenaiProject>(result));
            } else {
                result = wr.get();
                result.fillInfo(p);
            }
            return result;
        }
    }

    /**
     * getProject from cache
     * @param name
     * @return returns null if project does not exist in cachce
     */
    static KenaiProject get(Kenai kenai, String name) {
        synchronized (kenai.projectsCache) {
            WeakReference<KenaiProject> wr = kenai.projectsCache.get(name);
            if (wr == null) {
                return null;
            }
            return wr.get();
        }
    }


    /**
     * Unique name of project
     * @return project name
     */
    public synchronized String getName() {
        return data.name;
    }

    /**
     * web location of this project
     * @return web location of this project
     */
    public synchronized URL getWebLocation() {
        try {
            return new URL(data.web_url);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Display name of this project
     * @return display name
     */
    public synchronized String getDisplayName() {
        return data.display_name;
    }

    /**
     * Description of this project
     * @return project description
     * @throws KenaiException 
     */
    public synchronized String getDescription() throws KenaiException {
        checkDeleted();
        fetchDetailsIfNotAvailable();
        return data.description;
    }

    /**
     * Url of the image of this project
     * @return project picture
     * @throws KenaiException
     */
    public synchronized String getImageUrl() throws KenaiException {
        checkDeleted();
        fetchDetailsIfNotAvailable();
        return data.image;
    }

    /**
     * is this project bookmarked?
     * @return true if bookmarked and logged in<br>
     *         false otherwise
     */
    public boolean isMyProject() {
        Collection<KenaiProject> my = kenai.myProjects;
        if (my==null)
            return false;
        return my.contains(this);
    }

    /**
     * get my role. User must be logged in.
     * @return Role or null if logged user does not have any role in this projects
     * @throws KenaiException
     */
    public KenaiProjectMember.Role getMyRole() throws KenaiException {
        checkDeleted();
        PasswordAuthentication passwordAuthentication = kenai.getPasswordAuthentication();
        if (passwordAuthentication==null) {
            return null;
        }
        String myName = passwordAuthentication.getUserName();
        for (KenaiProjectMember user:getMembers()) {
            if (myName.equals(user.getUserName())) {
                return user.getRole();
            }
        }
        return null;
    }

    /**
     * Returns the image of the project, method can load it if needed
     * (synchronous load). If the method is called from AWT thread and icon
     * is not cached outside it before, this method throws an exception - it
     * is to prevent blocking AWT thread due to loading an image.
     * @return the image of the project or null, if loading image fails
     * @throws KenaiException
     */
    public Icon getProjectIcon() throws KenaiException {
        checkDeleted();
        if (projectIcon==null) {
            if (SwingUtilities.isEventDispatchThread()) {
                throw new KenaiException("KenaiProject.getProjectIcon() must not be called from AWT thread unless an icon is cached!");
            }
            try {
                URL url = new URL(getImageUrl());
                BufferedImage img = ImageIO.read(url);
                projectIcon = new ImageIcon(img);
            } catch (IOException ex) {
                throw new KenaiException(ex.getMessage());
            }
        }
        return projectIcon;
    }

    /**
     * @return tags separated by space
     *
     */
    public synchronized String getTags() throws KenaiException {
        checkDeleted();
        fetchDetailsIfNotAvailable();
        return data.tags;
    }

    /**
     * true if this project is private
     * @return
     * @throws KenaiException
     */
    public synchronized boolean isPrivate() throws KenaiException {
        checkDeleted();
        fetchDetailsIfNotAvailable();
        return data.private_hidden;
    }
    
    private static Pattern getRepositoryPattern(Kenai kenai) {
        return Pattern.compile("(https|http)://([a-z]+\\.)?" + kenai.getUrl().getHost().replace(".", "\\.") + "/(svn|hg)/(\\S*)~(.*)");
    }
    private static final int repositoryPatternProjectGroup = 4;

//  XXX not used for now
//    /**
//     * Looks up project by File
//     * @param file file to look for
//     * @return instance of KenaiProject which belongs to file
//     * @throws KenaiException
//     */
//    public static KenaiProject forFile(File file) throws KenaiException {
//        String projectName = NbModuleOwnerSupport.getInstance().getOwner(".kenai", file);//NOI18N
//        if (projectName!=null) {
//            return KenaiProject.get(projectName);
//        } else {
//            FileObject f = FileUtil.toFileObject(file);
//            if (f!=null) {
//                String remoteLocation = (String) f.getAttribute("ProvidedExtensions.RemoteLocation");//NOI18N
//                if (remoteLocation!=null) {
//                    return forRepository(remoteLocation);
//                }
//            }
//        }
//        return null;
//    }

    /**
     * Looks up a project by repository location.
     * The current implementation does not work for external repositories.
     * @param uri location of repository; for example SVN HTTP URL;
     *            typically gotten from {@code ProvidedExtensions.RemoteLocation} file attribute of project directory
     * @return Kenai project associated with that repository, or null
     * @throws KenaiException if the project cannot be loaded
     */
    public static KenaiProject forRepository(String uri) throws KenaiException {
        if (uri == null) {
            return null;
        }
        for (Kenai k : KenaiManager.getDefault().getKenais()) {
            Matcher m = getRepositoryPattern(k).matcher(uri);
            if (m.matches()) {
                return k.getProject(m.group(repositoryPatternProjectGroup));
            }
            //hard coded support for external netbeans repositories
            if (k.getUrl().getHost().equals("netbeans.org") && (uri.startsWith("https://hg.netbeans.org") || uri.startsWith("http://hg.netbeans.org"))) {//NOI18N
                return k.getProject("ide");//NOI18N
            }
        }
        return null;
    }

    /**
     * get project uniqe name for repository
     * The current implementation does not work for external repositories.
     * @param uri location of repository; for example SVN HTTP URL;
     *            typically gotten from {@code ProvidedExtensions.RemoteLocation} file attribute of project directory
     * @return name of kenai project or null, if given uri is not from kenai
     */
    public static String getNameForRepository(String uri) {
        if (uri==null)
            return null;
        for (Kenai k : KenaiManager.getDefault().getKenais()) {
            Matcher m = getRepositoryPattern(k).matcher(uri);
            if (m.matches()) {
                return m.group(repositoryPatternProjectGroup);
            }
            //hard coded support for external netbeans repositories
            if (k.getUrl().getHost().equals("netbeans.org") && (uri.startsWith("https://hg.netbeans.org") || uri.startsWith("http://hg.netbeans.org"))) {//NOI18N
                return "ide";//NOI18N
            }
        }
        return null;
    }

    /**
     * @return features of given project
     * @throws KenaiException
     * @see KenaiFeature
     */
    public synchronized KenaiFeature[] getFeatures() throws KenaiException {
        checkDeleted();
        fetchDetailsIfNotAvailable();
        if (features==null) {
            features=new KenaiFeature[data.features.length];
            int i=0;
            for (FeatureData feature:data.features) {
                features[i++] = new KenaiFeature(feature);
            }
        }
        return features;
    }

    /**
     * returns members of this project
     * @see KenaiUser
     * @return
     * @throws KenaiException
     */
    public synchronized KenaiProjectMember[] getMembers() throws KenaiException {
        checkDeleted();
        if (members==null) {
            Collection<KenaiProjectMember> projectMembers = kenai.getProjectMembers(getName());
            members = projectMembers.toArray(new KenaiProjectMember[projectMembers.size()]);
        }
        return members;
    }

    /**
     * Add member to this project. User must be logged in.
     * @param user
     * @param role
     * @throws KenaiException
     */
    public synchronized void addMember(KenaiUser user, KenaiProjectMember.Role role) throws KenaiException {
        checkDeleted();
        members = null;
        kenai.addMember(this, user, role);
        firePropertyChange(PROP_PROJECT_CHANGED, null, user);
    }

    /**
     * Removes member from project.
     * @param user
     * @throws KenaiException
     */
    public synchronized void deleteMember(KenaiUser user) throws KenaiException {
        checkDeleted();
        members = null;
        //if (user.data.href==null) {
            getMembers();
        //}
        kenai.deleteMember(this, user);
        firePropertyChange(PROP_PROJECT_CHANGED, user, null);
    }

    /**
     * Delete project on server.
     * All data will be lost.
     * @throws KenaiException
     */
    public synchronized void delete() throws KenaiException {
        checkDeleted();
        kenai.delete(this);
        synchronized (kenai.projectsCache) {
            kenai.projectsCache.remove(data.name);
        }
        deleted=true;
        firePropertyChange(PROP_PROJECT_REMOVED, null, null);
    }

    private void checkDeleted() throws KenaiException {
        if (deleted) {
            throw new KenaiException("project " + data.name + " was deleted");
        }
    }

    public synchronized KenaiUser getOwner() throws KenaiException {
        checkDeleted();
        fetchDetailsIfNotAvailable();
        if (data.owner==null) {
            return null;
        }
        return KenaiUser.forName(data.owner + "@" + kenai.getUrl().getHost());
    }

    /**
     * get features of given type
     * @param type
     * @return array of KenaiFetaures of given type
     * @throws KenaiException 
     */
    public synchronized KenaiFeature[] getFeatures(Type type) throws KenaiException {
        checkDeleted();
        ArrayList<KenaiFeature> fs= new ArrayList();
        for (KenaiFeature f:getFeatures()) {
            if (f.getType().equals(type)) {
                fs.add(f);
            }
        }
        return fs.toArray(new KenaiFeature[fs.size()]);
    }

    /**
     * @return licenses of given project
     * @throws KenaiException 
     * @see KenaiLicense
     */
    public synchronized KenaiLicense[] getLicenses() throws KenaiException {
        checkDeleted();
        fetchDetailsIfNotAvailable();
        if (licenses==null) {
            licenses=new KenaiLicense[data.licenses.length];
            int i=0;
            for (LicenceData licence : data.licenses) {
                licenses[i++] = new KenaiLicense(licence);
            }
        }
        return licenses;
    }

    /**
     * Creates new feature for this project
     * @param name
     * @param display_name
     * @param description
     * @param service
     * @param url
     * @param repository_url
     * @param browse_url
     * @return project feature
     * @throws org.netbeans.modules.kenai.api.KenaiException
     * @see KenaiFeature
     */
    public KenaiFeature createProjectFeature(
            String name,
            String display_name,
            String description,
            String service,
            String url,
            String repository_url,
            String browse_url
            ) throws KenaiException {
        checkDeleted();
        KenaiFeature feature = kenai.createProjectFeature(getName(), name, display_name, description, service, url, repository_url, browse_url);
        refresh();
        return feature;
    }

    void fillInfo(ProjectData prj) {
        synchronized (this) {
            if (prj.updated_at==null && this.data!=null && this.data.updated_at!=null) {
                return;
            }
            this.data = prj;
            features = null;
            members = null;
            licenses = null;
            projectIcon = null;
        }
        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, PROP_PROJECT_CHANGED, null, null));
    }

    synchronized ProjectData getData() {
        return data;
    }

    private void fetchDetailsIfNotAvailable() throws KenaiException {
        if (this.data.updated_at != null) {
            return;
        }
        refresh();
    }

    /**
     * Reloads project from kenai.com server
     * @throws org.netbeans.modules.kenai.api.KenaiException
     */
    private void refresh() throws KenaiException {
        fillInfo(kenai.getDetails(getName()));
    }

    @Override
    public synchronized boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final KenaiProject other = (KenaiProject) obj;
        if ((this.data.name == null) ? (other.data.name != null) : !this.data.name.equals(other.data.name)) {
            return false;
        }
        return true;
    }

    @Override
    public synchronized int hashCode() {
        int hash = 5;
        hash = 13 * hash + (this.data.name != null ? this.data.name.hashCode() : 0);
        return hash;
    }

    @Override
    public synchronized String toString() {
        return "KenaiProject " + getName();
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
     * do not use this method. just implementation detail
     * @param name
     * @param oldValue
     * @param newValue
     */
    public synchronized void firePropertyChange(String name, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(name, oldValue, newValue);
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
     * Kenai instance of this project
     * @return kenai instance
     */
    public Kenai getKenai() {
        return kenai;
    }
}
