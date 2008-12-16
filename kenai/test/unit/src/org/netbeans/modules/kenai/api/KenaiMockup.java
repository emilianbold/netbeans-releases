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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.kenai.spi.KenaiImpl;
import org.netbeans.modules.kenai.spi.KenaiProjectImpl;

/**
 * Mockup kenai server.
 *
 * @author Maros Sandor
 */
public class KenaiMockup extends KenaiImpl {

    private final List<KenaiProjectImpl> projects = new ArrayList<KenaiProjectImpl>();

    private final List<User> users = new ArrayList<User>();


    public KenaiMockup() {
        try {
            init();
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private User getUser(String username) {
        for (User user : users) {
            if (user.username.equals(username)) {
                return user;
            }
        }
        return null;
    }

    private void init() throws MalformedURLException {
        KenaiProjectImpl p = new KenaiProjectImpl("java-inline", new URL("http://localhost:7890/projects/java-inline"));
        p.put(KenaiProjectImpl.DISPLAY_NAME, "JavaInline for JRuby");
        projects.add(p);

        User u = new User("jerry", "mouse");
        users.add(u);
    }

    @Override
    public KenaiProjectImpl createProject(String name, String displayName, String username, char[] password) throws KenaiException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterator<KenaiProjectImpl> searchProjects(String pattern, String username, char[] password) throws KenaiException {
        List<KenaiProjectImpl> result = new ArrayList<KenaiProjectImpl>();
        for (KenaiProjectImpl p : projects) {
            if (p.getName().startsWith(pattern)) {
                result.add(p);
            }
        }
        return result.iterator();
    }
    
    @Override
    public KenaiProjectImpl getProject(String name, String username, char[] password) throws KenaiException {
        for (KenaiProjectImpl p : projects) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    @Override
    public boolean isAuthorized(String projectName, String feature, String activity, String username, char[] password) throws KenaiException {
        User user = getUser(username);
        if (user == null) {
            return false;
        }

        KenaiActivity ac = KenaiActivity.valueOf(feature, activity);
        
        switch (ac) {
            case FORUM_READ:
            case FORUM_CREATE:
            case FORUM_UPDATE:
            case ISSUES_CREATE:
            case ISSUES_READ:
            case ISSUES_WRITE:
            case LISTS_SEND:
            case LISTS_SUBSCRIBE:
            case WIKI_READ:
            case WIKI_WRITE:
            case PROJECTS_READ:
            case PROJECTS_CREATE:
            case PROJECTS_UPDATE:
            case SOURCE_READ:
            case SOURCE_WRITE:
            case PUBLIC_READ:
            case API_READ:
            case PROFILES_READ:
            case PROFILES_CREATE:
            case PROFILES_UPDATE:
                return true;

            case FORUM_DELETE:
            case FORUM_ADMIN:
            case LISTS_ADMIN:
            case LISTS_ARCHIVE:
            case LISTS_CLOSE:
            case LISTS_CREATE:
            case LISTS_MODERATE:
            case WIKI_DELETE:
            case PROJECTS_DELETE:
            case PROJECTS_ADMIN:
            case PROFILES_DELETE:
                return false;

            default:
                return false;
        }
    }

    @Override
    public void register(String username, char[] password) throws KenaiException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void verify(String username, char[] password) throws KenaiException {
        try {
            getUser(username).password.equals(new String(password));
        } catch (Exception e) {
            throw new KenaiException(e);
        }
    }

    static class User {
        String username;
        String password;

        private User(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}
