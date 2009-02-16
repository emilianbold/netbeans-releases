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

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.codeviation.commons.utils.CollectionsUtil;
import org.netbeans.modules.kenai.FeatureData;
import org.netbeans.modules.kenai.ProjectData;

/**
 * IDE-side representation of a Kenai project.
 *
 * @author Maros Sandor
 * @author Jan Becicka
 */
public final class KenaiProject {

    private String    name;

    private URL       href;

    private ProjectData     data;
    
    private KenaiProjectFeature[] features;

    /**
     * When detailed properties of this project has been fetched.
     */
    private long        detailsTimestamp;

    /**
     * I assume that this constructor does NOT provide full project information. If it does then
     * call fillInfo() just after the object is created.
     *
     * @param p
     */
    private KenaiProject(ProjectData p) {
        this.name = p.name;
        try {
            this.href = new URL(p.href);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
        this.data = p;
    }

    static synchronized KenaiProject get(ProjectData p) {
        final HashMap<String, WeakReference<KenaiProject>> projectsCache = Kenai.getDefault().projectsCache;
        WeakReference<KenaiProject> wr = projectsCache.get(p.name);
        KenaiProject result = null;
        if (wr==null || (result = wr.get()) == null) {
            result = new KenaiProject(p);
            projectsCache.put(p.name, new WeakReference<KenaiProject>(result));
        }
        return result;
    }

    /**
     * Unique name of project
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * web location of this project
     * @return
     */
    public URL getWebLocation() {
        return href;
    }

    /**
     * Display name of this project
     * @return
     */
    public String getDisplayName() {
        return data.display_name;
    }

    /**
     * Display name of this project
     * @return
     */
    public String getDescription() {
        fetchDetailsIfNotAvailable();
        return data.description;
    }

    /**
     * @return comma separated tags
     */
    public String getTags() {
        return data.tags;
    }

    /**
     * Opens project
     * @see Kenai#getOpenProjects()
     */
    public synchronized void open() {
        final Kenai kenai = Kenai.getDefault();
        Kenai.getDefault().getOpenProjects().add(this);
        kenai.storeProjects();
        kenai.fireKenaiEvent(new KenaiEvent(this, KenaiEvent.PROJECT_OPEN));
    }

    /**
     * Closes project
     * @see Kenai#getOpenProjects()
     */
    public synchronized void close() {
        final Kenai kenai = Kenai.getDefault();
        Kenai.getDefault().getOpenProjects().remove(this);
        kenai.storeProjects();
        kenai.fireKenaiEvent(new KenaiEvent(this, KenaiEvent.PROJECT_CLOSE));
    }

    private static Pattern repositoryPattern = Pattern.compile("(https|http)://(testkenai|kenai)\\.com/(svn|hg)/(\\S*)~(.*)");

    /**
     * Returns KenaiProject for given repository uri. Current implementation does not work for external repositories
     * @param uri
     * @return instance of KenaiProject or null
     * @throws org.netbeans.modules.kenai.api.KenaiException
     */
    public static KenaiProject forRepository(String uri) throws KenaiException {
        Matcher m = repositoryPattern.matcher(uri);
        if (m.matches()) {
            return Kenai.getDefault().getProject(m.group(4));
        }

        return null;
    }

    /**
     * @return features of given project
     * @see KenaiProjectFeature
     */
    public synchronized KenaiProjectFeature[] getFeatures() {
        if (features==null) {
            features=new KenaiProjectFeature[data.features.length];
            int i=0;
            for (FeatureData feature:data.features) {
                features[i++] = new KenaiProjectFeature(feature);
            }
        }
        return features;
    }

    /**
     * get features of given type
     * @param type
     * @return
     */
    public synchronized KenaiProjectFeature[] getFeatures(KenaiFeature type) {
        ArrayList<KenaiProjectFeature> fs= new ArrayList();
        for (KenaiProjectFeature f:getFeatures()) {
            if (f.getType().equals(type)) {
                fs.add(f);
            }
        }
        return fs.toArray(new KenaiProjectFeature[fs.size()]);
    }

    /**
     * Creates new feateru for this project
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
            String name,
            String display_name,
            String description,
            String service,
            String url,
            String repository_url,
            String browse_url
            ) throws KenaiException {
        KenaiProjectFeature feature = Kenai.getDefault().createProjectFeature(getName(), name, display_name, description, service, url, repository_url, browse_url);
        refresh();
        return feature;
    }

    void fillInfo(ProjectData prj) {
        detailsTimestamp = System.currentTimeMillis();
    }

    ProjectData getData() {
        return data;
    }

    private void fetchDetailsIfNotAvailable() {
        if (detailsTimestamp > 0) return;

//        try {
//            ProjectData prj = kenai.getDetails(name);
//            fillInfo(prj);
//        } catch (KenaiException kenaiException) {
//            Utils.logError(this, kenaiException);
//        }
    }

    /**
     * Reloads project from kenai.com server
     * @throws org.netbeans.modules.kenai.api.KenaiException
     */
    public synchronized  void refresh() throws KenaiException {
        this.data = Kenai.getDefault().getDetails(getName());

        this.name = data.name;
        try {
            this.href = new URL(data.href);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
        features=null;
        Kenai.getDefault().fireKenaiEvent(new KenaiEvent(this, KenaiEvent.PROJECT_CHANGED));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final KenaiProject other = (KenaiProject) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 13 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "KenaiProject " + getName();
    }


}
