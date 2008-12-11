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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.modules.kenai.KenaiREST;
import org.netbeans.modules.kenai.spi.KenaiImpl;
import org.netbeans.modules.kenai.spi.KenaiProjectImpl;

/**
 * Main entry point to Kenai integration.
 *
 * @author Maros Sandor
 */
public final class Kenai {

    private static final Map<Object, Kenai> instances = new HashMap<Object, Kenai>(1);

    public static synchronized Kenai getDefault() {
        try {
            return getInstance(new URL("http://kenai.com"));
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static synchronized Kenai getInstance(URL location) {
        Kenai kenai = instances.get(location);
        if (kenai == null) {
            KenaiImpl impl = new KenaiREST(location);
            kenai = new Kenai(impl);
        }
        return kenai;
    }

    private final KenaiImpl impl;

    /**
     * Currently user username.
     */
    private String username;

    /**
     * Currently used password.
     */
    private char[] password;

    private Kenai(KenaiImpl impl) {
        this.impl = impl;
    }

    private final Map<String, KenaiProject> projects = new HashMap<String, KenaiProject>();

    /**
     * Logs an existing user into Kenai. Login session persists until the login method
     * is called again. If the login fails then the current session resumes (if any).
     *
     * @param username
     * @param password
     * @return true if the login was successful, false otherwise
     */
    public void login(String username, char [] password) throws KenaiException {
        impl.verify(username, password);
        this.username = username;
        this.password = password;
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
     *
     * @return currently active credentials or null if the user is not logged in
     */
    public String getUsername() {
        return username;
    }

    /**
     * Search for Kenai projects on the Kenai server. The format of the search pattern is as follows:
     *
     * @param pattern search pattern. Only one method is recognized now: substring match
     * @return an interator over kenai domains that match given search pattern
     */
    public Iterator<KenaiProject> searchProjects(String pattern) throws KenaiException {
        Iterator<KenaiProjectImpl> prjs = impl.searchProjects(pattern, username, password);
        return new ProjectsIterator(prjs);
    }

    /**
     * Search for Kenai domains on the kenai server.
     *
     * @param pattern search pattern. Only one method is recognized now: substring match
     * @return an interator over kenai domains that match given search pattern
     */
    public KenaiProject getProject(String name) throws KenaiException {
        KenaiProject p = projects.get(name);
        if (p == null) {
            KenaiProjectImpl prj = impl.getProject(name, username, password);
            if (prj != null) {
                p = new KenaiProject(prj);
                projects.put(name, p);
            }
        }
        return p;
    }

    /**
     * Creates a new Kenai domain on the Kenai server
     *
     * @param name
     * @param displayName
     * @return
     * @throws org.netbeans.modules.kenai.api.KenaiException
     */
    public KenaiProject createProject(String name, String displayName) throws KenaiException {
        if (username == null) {
            throw new KenaiException("Guest user is not allowed to create new domains");
        }
        KenaiProjectImpl prj = impl.createProject(name, displayName, username, password);
        return new KenaiProject(prj);
    }

    public boolean isAuthorized(KenaiProject project, KenaiActivity activity) throws KenaiException {
        return impl.isAuthorized(project.getName(), activity.getFeature().getId(), activity.getName(), username, password);
    }

    private class ProjectsIterator implements Iterator<KenaiProject> {

        private final Iterator<KenaiProjectImpl> it;

        public ProjectsIterator(Iterator<KenaiProjectImpl> it) {
            this.it = it;
        }

        public boolean hasNext() {
            return it.hasNext();
        }

        public KenaiProject next() {
            KenaiProjectImpl prj = it.next();
            return toProject(prj);
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private KenaiProject toProject(KenaiProjectImpl prj) {
        KenaiProject p = projects.get(KenaiProjectImpl.NAME);
        if (p == null) {
            p = new KenaiProject(prj);
        }
        return p;
    }
}
