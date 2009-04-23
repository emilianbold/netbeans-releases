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

import org.netbeans.modules.kenai.api.KenaiService.Type;

/**
 * Enumerates possible activities of a Kenai user.
 * TODO: do we need this class at all?
 * @author Maros Sandor
 */
public enum KenaiActivity {

    FORUM_READ      (Type.FORUM,    "read"),
    FORUM_CREATE    (Type.FORUM,    "create"),
    FORUM_UPDATE    (Type.FORUM,    "update"),
    FORUM_DELETE    (Type.FORUM,    "delete"),
    FORUM_ADMIN     (Type.FORUM,    "admin"),

    ISSUES_CREATE   (Type.ISSUES,   "create"),
    ISSUES_READ     (Type.ISSUES,   "read"),
    ISSUES_WRITE    (Type.ISSUES,   "write"),

    LISTS_ADMIN     (Type.LISTS,    "admin"),
    LISTS_ARCHIVE   (Type.LISTS,    "archive"),
    LISTS_CLOSE     (Type.LISTS,    "close"),
    LISTS_CREATE    (Type.LISTS,    "create"),
    LISTS_MODERATE  (Type.LISTS,    "moderate"),
    LISTS_SEND      (Type.LISTS,    "send"),
    LISTS_SUBSCRIBE (Type.LISTS,    "subscribe"),

    WIKI_DELETE     (Type.WIKI,     "delete"),
    WIKI_READ       (Type.WIKI,     "read"),
    WIKI_WRITE      (Type.WIKI,     "write"),

    PROJECTS_READ   (Type.PROJECTS, "read"),
    PROJECTS_CREATE (Type.PROJECTS, "create"),
    PROJECTS_UPDATE (Type.PROJECTS, "update"),
    PROJECTS_DELETE (Type.PROJECTS, "delete"),
    PROJECTS_ADMIN  (Type.PROJECTS, "admin"),

    SOURCE_READ     (Type.SOURCE,   "read"),
    SOURCE_WRITE    (Type.SOURCE,   "write"),

    //PUBLIC_READ     (Type.PUBLIC,   "read"),

//    API_READ        (Type.API,      "read"),

//    PROFILES_READ   (Type.PROFILES, "read"),
//    PROFILES_CREATE (Type.PROFILES, "create"),
//    PROFILES_UPDATE (Type.PROFILES, "update"),
//    PROFILES_DELETE (Type.PROFILES, "delete")
            ;

    static KenaiActivity valueOf(String featureText, String activityText) {
        for (KenaiActivity kenaiActivity : KenaiActivity.values()) {
            if (kenaiActivity.feature.getId().equals(featureText) && kenaiActivity.name.equals(activityText)) return kenaiActivity;
        }

        return null;
    }

    private final Type feature;
    private final String name;

    KenaiActivity(Type feature, String name) {
        this.feature = feature;
        this.name = name;
    }

    public Type getFeature() {
        return feature;
    }

    public String getName() {
        return name;
    }
}
