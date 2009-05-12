/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.api;

import org.netbeans.modules.kenai.ServicesListData.ServicesListItem;

/**
 *
 * @author Jan Becicka
 */
public final class KenaiService {

    private ServicesListItem sli;

    KenaiService(ServicesListItem sli) {
        this.sli=sli;
    }

    /**
     * Getter for service description
     * @return service description
     */
    public String getDescription() {
        return sli.description;
    }

    /**
     * Getter for service name
     * @return service name
     */
    public String getName() {
        return sli.name;
    }

    /**
     * getter for service display name
     * @return service display name
     */
    public String getDisplayName() {
        return sli.display_name;
    }

    /**
     * Getter for service type
     * @return type of this service
     */
    public Type getType() {
        return Type.forId(sli.type);
    }

    @Override
    public String toString() {
        return "KenaiService " + getName();
    }

    /**
     * Well known name of services
     */
    public static final class Names {
        public static final String SUBVERSION = "subversion";
        public static final String MERCURIAL = "mercurial";
        public static final String GIT = "git";
        public static final String EXTERNAL_REPOSITORY = "external_repository";
        public static final String BUGZILLA = "bugzilla";
        public static final String JIRA = "jira";
        public static final String EXTERNAL_ISSUES = "external_issues";
        public static final String XMPP_CHAT = "instant_messenger";
    }

    /**
     * Service types
     */
    public static enum Type {

        FORUM("forum"),
        ISSUES("issues"),
        LISTS("lists"),
        SOURCE("scm"),
        WIKI("wiki"),
        CHAT("instant_messenger"),
        DOWNLOADS("downloads"),
        PROJECTS("projects"),
        UNKNOWN("unknown");
        private String id;

        Type(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public static Type forId(String id) {
            if (id.equals(SOURCE.id)) {
                return SOURCE;
            } else if (id.equals(CHAT.id)) {
                return CHAT;
            } else {
                try {
                    return Type.valueOf(id.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return UNKNOWN;
                }
            }
        }
    }
}
