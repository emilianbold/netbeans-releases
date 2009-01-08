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

package org.netbeans.modules.kenai;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.modules.kenai.api.KenaiException;
import org.codeviation.pojson.*;

/**
 * Talks to remote Kenai server via Web services API.
 *
 * @author Maros Sandor
 */
public class KenaiREST extends KenaiImpl {

    private URL baseURL;

    public KenaiREST(URL baseURL) {
        this.baseURL = baseURL;
    }

    @Override
    public boolean isAuthorized(String projectName, String feature, String activity, String username, char [] password) throws KenaiException {
        String [][] params = {
            new String [] { "feature_id", feature },
            new String [] { "activity_id", activity },
            new String [] { "person_id", username },
            new String [] { "project_id", projectName }
        };
        RestConnection conn = new RestConnection(baseURL.toString() + "/api/login/authorize", params);
        RestResponse resp = null;
        try {
            resp = conn.get(null);
        } catch (IOException iOException) {
            throw new KenaiException(iOException);
        }
        return resp.getResponseCode() == 200;
    }

    @Override
    public ProjectData getProject(String name, String username, char[] password) throws KenaiException {
        RestConnection conn = new RestConnection(baseURL.toString() + "/api/projects/" + name + ".json");
        RestResponse resp = null;
        try {
            resp = conn.get(null);
        } catch (IOException iOException) {
            throw new KenaiException(iOException);
        }

        if (resp.getResponseCode() != 200) return null;

        String sss = resp.getDataAsString();

        PojsonLoad pload = PojsonLoad.create();
        return pload.load(sss, ProjectData.class);
    }

    @Override
    public Iterator<ProjectData> searchProjects(String pattern, String username, char[] password) throws KenaiException {
        RestConnection conn = new RestConnection(baseURL.toString() + "/home/live_lookup?val=" + pattern);
        RestResponse resp = null;
        try {
            resp = conn.get(null);
        } catch (IOException iOException) {
            throw new KenaiException(iOException);
        }
        String sss = resp.getDataAsString();

        PojsonLoad pload = PojsonLoad.create();
        JsonLiveLookup [] objs = pload.load(sss, JsonLiveLookup[].class);

        Set<ProjectData> projects = new HashSet<ProjectData>();
        for (JsonLiveLookup obj : objs) {
            if ("project".equals(obj.t)) {
                // TODO: FAKE name is the last part of the URL MAYBE!
                String name = obj.url;
                int idx = name.lastIndexOf('/');
                if (idx >= 0) {
                    name = name.substring(idx + 1);
                }
                ProjectData prj = new ProjectData();
                prj.name = name;
                prj.href = baseURL + obj.url;
                prj.display_name = obj.name;
                projects.add(prj);
            }
        }
        return projects.iterator();
    }

    @Override
    public ProjectData createProject(String name, String displayName, String username, char[] password) throws KenaiException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void register(String username, char[] password) throws KenaiException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void verify(String username, char[] password) throws KenaiException {
        String [][] params = {
            new String [] { "username", username },
            new String [] { "password", new String(password) }
        };
        RestConnection conn = new RestConnection(baseURL.toString() + "/api/login/authenticate", params);
        RestResponse resp = null;
        try {
            resp = conn.get(null);
        } catch (IOException iOException) {
            throw new KenaiException(iOException);
        }
        if (resp.getResponseCode() != 200) {
            throw new KenaiException("Authentication failed");
        }
    }

//        DateFormat df = new SimpleDateFormat("y-M-d'T'H:m:s'Z'");
}
