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

package org.netbeans.modules.kenai;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codeviation.pojson.*;
import org.netbeans.modules.kenai.api.KenaiException;

/**
 * Talks to remote Kenai server via Web services API.
 *
 * 
 * 
 */
public class KenaiREST extends KenaiImpl {

    private URL baseURL;

    @Override
    public URL getUrl() {
        return baseURL;
    }

    private static final Logger TIMER = Logger.getLogger("TIMER.kenai");

    public KenaiREST(URL baseURL) {
        this.baseURL = baseURL;
    }

    @Override
    public boolean isAuthorized(String projectName, String feature, String activity, PasswordAuthentication pa) throws KenaiException {
        String [][] params = {
            new String [] { "feature_id", feature },
            new String [] { "activity_id", activity },
            new String [] { "person_id", pa.getUserName()},
            new String [] { "project_id", projectName }
        };
        RestConnection conn = new RestConnection(baseURL.toString() + "/api/login/authorize", params, pa);
        long start = 0;
        if (TIMER.isLoggable(Level.FINE)) {
            start = System.currentTimeMillis();
            System.err.println("Loading page " + baseURL.toString() + "/api/login/authorize");
        }
        RestResponse resp = null;
        try {
            resp = conn.get(null);
        } catch (IOException iOException) {
            throw new KenaiException(iOException);
        }
        if (TIMER.isLoggable(Level.FINE)) {
            System.err.println("Page " + baseURL.toString() + "/api/login/authorize loaded in " + (System.currentTimeMillis()-start) + " ms");
        }

        return resp.getResponseCode() == 200;
    }

    @Override
    public ProjectData getProject(String name, PasswordAuthentication pa) throws KenaiException {
        RestConnection conn = new RestConnection(baseURL.toString() + "/api/projects/" + name + ".json", pa);
        RestResponse resp = null;
        long start = 0;
        if (TIMER.isLoggable(Level.FINE)) {
            start = System.currentTimeMillis();
            System.err.println("Loading project " + name);
        }
        try {
            resp = conn.get(null);
        } catch (IOException iOException) {
            throw new KenaiException(iOException);
        }
        if (TIMER.isLoggable(Level.FINE)) {
            System.err.println("Project " +name +" loaded in " + (System.currentTimeMillis()-start) + " ms");
        }

        if (resp.getResponseCode() != 200)
            throw new KenaiException(name + ": " + resp.getResponseMessage(), resp.getDataAsString()); // NOI18N

        String sss = resp.getDataAsString();

        PojsonLoad pload = PojsonLoad.create();
        return pload.load(sss, ProjectData.class);
    }

    @Override
    public Collection<ProjectData> searchProjects(String pattern, PasswordAuthentication pa) throws KenaiException {
        try {
            String m = "/api/projects.json?full=true";
            if (pattern.length()>0) {
                if (pattern.startsWith("&")) {
                    m+=pattern;
                } else {
                    m+="&q=" + URLEncoder.encode(pattern, "UTF-8");
                }
            }
            ProjectsListData pld = loadPage(baseURL.toString() + m, ProjectsListData.class, null);
            return new LazyList(pld, ProjectsListData.class, pa);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }

    }

    @Override
    public Collection<ProjectData> getMyProjects(PasswordAuthentication pa) throws KenaiException {
        ProjectsListData pld = loadPage(baseURL.toString() + "/api/projects/mine.json?full=true", ProjectsListData.class, pa);
        return new LazyList(pld, ProjectsListData.class, pa);
    }

    private static <T> T loadPage(String url, Class<T> clazz, PasswordAuthentication pa) throws KenaiException {

        long start = 0;
        if (TIMER.isLoggable(Level.FINE)) {
            start = System.currentTimeMillis();
            System.err.println("Loading page " + url);
        }
        RestConnection conn = new RestConnection(url, pa);
        RestResponse resp = null;
        try {
            resp = conn.get(null);
        } catch (IOException iOException) {
            throw new KenaiException(iOException);
        }
        if (TIMER.isLoggable(Level.FINE)) {
            System.err.println("Page " + url + " loaded in " + (System.currentTimeMillis()-start) + " ms");
        }
        if (resp.getResponseCode() != 200)
            throw new KenaiException(resp.getResponseMessage(),resp.getDataAsString());
        String responseString = resp.getDataAsString();

        PojsonLoad pload = PojsonLoad.create();
        T data = pload.load(responseString, clazz);

        return data;
    }

    @Override
    public Collection<LicensesListData.LicensesListItem> getLicenses() throws KenaiException {
        LicensesListData pld = loadPage(baseURL.toString() + "/api/licenses.json", LicensesListData.class, null);
        return new LazyList(pld, LicensesListData.class, null);
    }

    @Override
    public Collection<ServicesListData.ServicesListItem> getServices() throws KenaiException {
        ServicesListData pld = null;
        try {
            pld = loadPage(baseURL.toString() + "/api/services.json", ServicesListData.class, null);
        } catch (IllegalArgumentException iae) {
            if(iae.getMessage().equals("OBJECT_OR_ARRAY_EXP")) {
                Logger.getLogger(KenaiREST.class.getName()).log(Level.WARNING, null, iae);
                return Collections.emptyList();
            }
            throw iae;
        }
        return new LazyList(pld, ServicesListData.class, null);
    }

    @Override
    public String checkName(String name) throws KenaiException {
        CheckNameData cnd = loadPage(baseURL.toString() + "/api/projects/check_unique.json?name="+name, CheckNameData.class, null);
        return cnd.is_unique?null:cnd.message;
    }

    @Override
    public Collection<UserData> getProjectMembers(String name, PasswordAuthentication pa) throws KenaiException {
        MembersListData members = loadPage(baseURL.toString() + "/api/projects/"+name+"/members.json", MembersListData.class, pa);
        return new LazyList(members, MembersListData.class, pa);
    }

    @Override
    public MemberData addMember(String project, String user, String role, PasswordAuthentication pa) throws KenaiException {
        RestConnection conn = new RestConnection(baseURL.toString() + "/api/projects/" + project + "/members.json", pa);
        RestResponse resp = null;
        PojsonSave<MemberData> save = PojsonSave.create(MemberData.class);
        MemberData mdata = new MemberData();
        mdata.member.username = user;
        mdata.member.role = role;

        long start = 0;
        if (TIMER.isLoggable(Level.FINE)) {
            start = System.currentTimeMillis();
            System.err.println("Add member " + user + " to project " + project);
        }
        try {
            resp = conn.post(null, save.asString(mdata));
        } catch (IOException iOException) {
            throw new KenaiException(iOException);
        }
        if (TIMER.isLoggable(Level.FINE)) {
            System.err.println("User " + user + " add in " + (System.currentTimeMillis()-start) + " ms");
        }

        if (resp.getResponseCode() != 200)
            throw new KenaiException(resp.getResponseMessage(),resp.getDataAsString());

        String response = resp.getDataAsString();

        PojsonLoad pload = PojsonLoad.create();
        return pload.load(response, MemberData.class);
    }

    @Override
    public void deleteMember(String project, long member_id, PasswordAuthentication pa) throws KenaiException {
        RestConnection conn = new RestConnection(baseURL.toString() + "/api/projects/" + project + "/members/" + member_id + ".json", pa);
        RestResponse resp = null;

        long start = 0;
        if (TIMER.isLoggable(Level.FINE)) {
            start = System.currentTimeMillis();
            System.err.println("Removing member " + member_id + " from project " + project);
        }
        try {
            resp = conn.delete(null);
        } catch (IOException iOException) {
            throw new KenaiException(iOException);
        }
        if (TIMER.isLoggable(Level.FINE)) {
            System.err.println("User " + member_id + " removed in " + (System.currentTimeMillis()-start) + " ms");
        }

        if (resp.getResponseCode() != 200)
            throw new KenaiException(resp.getResponseMessage(),resp.getDataAsString());
    }

    @Override
    public void deleteProject(String project, PasswordAuthentication pa) throws KenaiException {
        RestConnection conn = new RestConnection(baseURL.toString() + "/api/projects/" + project + ".json", pa);
        RestResponse resp = null;

        long start = 0;
        if (TIMER.isLoggable(Level.FINE)) {
            start = System.currentTimeMillis();
            System.err.println("Removing project " + project);
        }
        try {
            resp = conn.delete(null);
        } catch (IOException iOException) {
            throw new KenaiException(iOException);
        }
        if (TIMER.isLoggable(Level.FINE)) {
            System.err.println("project " + project + " removed in " + (System.currentTimeMillis()-start) + " ms");
        }

        if (resp.getResponseCode() != 200)
            throw new KenaiException(resp.getResponseMessage(),resp.getDataAsString());
    }

    @Override
    public String getFeed(String relUrl, PasswordAuthentication pa) throws KenaiException {
        String url = relUrl.startsWith("/") ? baseURL.toString() + relUrl : baseURL.toString() + "/" + relUrl;
        RestConnection conn = new RestConnection(url, pa);
        
        RestResponse resp = null;

        long start = 0;
        if (TIMER.isLoggable(Level.FINE)) {
            start = System.currentTimeMillis();
            System.err.println("read feed " + url);
        }
        try {
            resp = conn.get(null);
        } catch (IOException iOException) {
            throw new KenaiException(iOException);
        }
        if (TIMER.isLoggable(Level.FINE)) {
            System.err.println("read feed " + url + " " + (System.currentTimeMillis()-start) + " ms");
        }

        if (resp.getResponseCode() != 200)
            throw new KenaiException(resp.getResponseMessage(),resp.getDataAsString());

        return resp.getDataAsString();
    }

    private class LazyList<COLLECTION extends ListData, ITEM> extends AbstractCollection<ITEM> {

        private COLLECTION col;
        private Class<COLLECTION> clazz;
        private PasswordAuthentication pa;


        public LazyList(COLLECTION col, Class<COLLECTION> clazz, PasswordAuthentication pa) {
            this.col=col;
            this.clazz=clazz;
            this.pa = pa;
        }

        @Override
        public Iterator<ITEM> iterator() {
            return new LazyIterator<COLLECTION, ITEM>(col, clazz, pa);
        }

        @Override
        public int size() {
            return col.total;
        }

    }
    private class LazyIterator<COLLECTION extends ListData, ITEM> implements Iterator<ITEM> {

        private COLLECTION col;
        private int currentIndex = 0;
        private Class<COLLECTION> clazz;
        private PasswordAuthentication pa;

        public LazyIterator(COLLECTION col, Class<COLLECTION> clazz, PasswordAuthentication pa) {
            this.col = col;
            this.clazz = clazz;
            this. pa = pa;
        }


        public boolean hasNext() {
            if (col.size()>currentIndex) {
                return true;
            }
            return col.next!=null;
        }

        public ITEM next() {
                if (currentIndex==col.size()) {
                    currentIndex-=col.size();
                    try {
                        col = loadPage(col.next, clazz, pa);
                    } catch (KenaiException ex) {
                        throw new RuntimeException("Error loading " + col.next, ex);
                    }
                }
                return colToItem(currentIndex++);
        }

        public void remove() {
            throw new UnsupportedOperationException("Cannot remove items");
        }

        private ITEM colToItem(int index) {
            if (col instanceof ProjectsListData) {
                return (ITEM) ((ProjectsListData) col).projects[index];
            } else if (col instanceof ServicesListData) {
                return (ITEM) ((ServicesListData) col).services[index];
            } else if (col instanceof LicensesListData) {
                return (ITEM) ((LicensesListData) col).licenses[index];
            } else if (col instanceof MembersListData) {
                return (ITEM) ((MembersListData) col).members[index];
            }
            throw new IllegalStateException();
        }

    }


    @Override
    public ProjectData createProject(
            String name,
            String displayName,
            String description,
            String[] licenses,
            String tags,
            PasswordAuthentication pa
            ) throws KenaiException {
        RestConnection conn = new RestConnection(baseURL.toString() + "/api/projects.json", pa);
        RestResponse resp = null;
        PojsonSave<ProjectCreateData> save = PojsonSave.create(ProjectCreateData.class);
        ProjectCreateData prdata = new ProjectCreateData();
        prdata.project.name = name;
        prdata.project.display_name = displayName;
        prdata.project.description = description;
        prdata.project.licenses = licenses;
        prdata.project.tags = tags;

        long start = 0;
        if (TIMER.isLoggable(Level.FINE)) {
            start = System.currentTimeMillis();
            System.err.println("Create project " + name);
        }
        try {
            resp = conn.post(null, save.asString(prdata));
        } catch (IOException iOException) {
            throw new KenaiException(iOException);
        }
        if (TIMER.isLoggable(Level.FINE)) {
            System.err.println("Project " + name + " created in " + (System.currentTimeMillis()-start) + " ms");
        }

        if (resp.getResponseCode() != 201)
            throw new KenaiException(resp.getResponseMessage(),resp.getDataAsString());

        String response = resp.getDataAsString();

        PojsonLoad pload = PojsonLoad.create();
        return pload.load(response, ProjectData.class);
    }

    @Override
    public FeatureData createProjectFeature(
            String projectName,
            String name,
            String display_name,
            String description,
            String url,
            String repository_url,
            String browse_url,
            String service,
            PasswordAuthentication pa
            ) throws KenaiException {
        
        RestConnection conn = new RestConnection(baseURL.toString() + "/api/projects/"+projectName+"/features.json", pa);
        RestResponse resp = null;
        PojsonSave<ProjectFeatureCreateData> save = PojsonSave.create(ProjectFeatureCreateData.class);
        ProjectFeatureCreateData fdata = new ProjectFeatureCreateData();
        fdata.feature.name = name;
        fdata.feature.display_name = display_name;
        fdata.feature.description = description;
        fdata.feature.service = service;
        fdata.feature.url = url;
        fdata.feature.repository_url = repository_url;
        fdata.feature.browse_url = browse_url;
        long start = 0;
        if (TIMER.isLoggable(Level.FINE)) {
            start = System.currentTimeMillis();
            System.err.println("Creating feature " + name);
        }
        try {
            resp = conn.post(null, save.asString(fdata));
        } catch (IOException iOException) {
            throw new KenaiException(iOException);
        }
        if (TIMER.isLoggable(Level.FINE)) {
            System.err.println("Feature " + name+ " created in " + (System.currentTimeMillis()-start) + " ms");
        }

        if (resp.getResponseCode() != 201)
            throw new KenaiException(resp.getResponseMessage(),resp.getDataAsString());

        String response = resp.getDataAsString();

        PojsonLoad pload = PojsonLoad.create();
        return pload.load(response, FeatureData.class);
    }

    @Override
    public void register(String username, char[] password) throws KenaiException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String verify(String username, char[] password) throws KenaiException {
        AuthenticationData auth = new AuthenticationData();
        auth.username = username;
        auth.password = new String(password);
        PojsonSave<AuthenticationData> save = PojsonSave.create(AuthenticationData.class);

        RestConnection conn = new RestConnection(baseURL.toString() + "/api/login/authenticate.json", null);
        RestResponse resp = null;
        
        long start = 0;
        if (TIMER.isLoggable(Level.FINE)) {
            start = System.currentTimeMillis();
            System.err.println("Loading page "+ baseURL.toString() + "/api/login/authenticate.json");
        }
        try {
            resp = conn.post(null, save.asString(auth));
        } catch (IOException iOException) {
            throw new KenaiException(iOException);
        }
        if (TIMER.isLoggable(Level.FINE)) {
            System.err.println("Page " + baseURL.toString() + "/api/login/authenticate.json loaded in" + (System.currentTimeMillis()-start) + " ms");
        }

        if (resp.getResponseCode() != 200) {
            throw new KenaiException(ResourceBundle.getBundle("org.netbeans.modules.kenai.Bundle").getString("LBL_AuthenticationFailed"), resp.getDataAsString());
        }
        try {
            String shortName = (String) ((HashMap) PojsonLoad.create().toCollections(resp.getDataAsString())).get("username");
            if (shortName==null || shortName.length()==0) {
                throw new KenaiException(ResourceBundle.getBundle("org.netbeans.modules.kenai.Bundle").getString("LBL_AuthenticationFailed"));
            }
            return shortName;
        } catch (IOException ex) {
            throw new KenaiException(ex);
        }
    }
}
