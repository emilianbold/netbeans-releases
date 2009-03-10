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

/**
 * Enumerates possible activities of a Kenai user.
 *
 * @author Maros Sandor
 */
public enum KenaiActivity {

    FORUM_READ      (KenaiFeature.FORUM,    "read"),
    FORUM_CREATE    (KenaiFeature.FORUM,    "create"),
    FORUM_UPDATE    (KenaiFeature.FORUM,    "update"),
    FORUM_DELETE    (KenaiFeature.FORUM,    "delete"),
    FORUM_ADMIN     (KenaiFeature.FORUM,    "admin"),

    ISSUES_CREATE   (KenaiFeature.ISSUES,   "create"),
    ISSUES_READ     (KenaiFeature.ISSUES,   "read"),
    ISSUES_WRITE    (KenaiFeature.ISSUES,   "write"),

    LISTS_ADMIN     (KenaiFeature.LISTS,    "admin"),
    LISTS_ARCHIVE   (KenaiFeature.LISTS,    "archive"),
    LISTS_CLOSE     (KenaiFeature.LISTS,    "close"),
    LISTS_CREATE    (KenaiFeature.LISTS,    "create"),
    LISTS_MODERATE  (KenaiFeature.LISTS,    "moderate"),
    LISTS_SEND      (KenaiFeature.LISTS,    "send"),
    LISTS_SUBSCRIBE (KenaiFeature.LISTS,    "subscribe"),

    WIKI_DELETE     (KenaiFeature.WIKI,     "delete"),
    WIKI_READ       (KenaiFeature.WIKI,     "read"),
    WIKI_WRITE      (KenaiFeature.WIKI,     "write"),

    PROJECTS_READ   (KenaiFeature.PROJECTS, "read"),
    PROJECTS_CREATE (KenaiFeature.PROJECTS, "create"),
    PROJECTS_UPDATE (KenaiFeature.PROJECTS, "update"),
    PROJECTS_DELETE (KenaiFeature.PROJECTS, "delete"),
    PROJECTS_ADMIN  (KenaiFeature.PROJECTS, "admin"),

    SOURCE_READ     (KenaiFeature.SOURCE,   "read"),
    SOURCE_WRITE    (KenaiFeature.SOURCE,   "write"),

    PUBLIC_READ     (KenaiFeature.PUBLIC,   "read"),

    API_READ        (KenaiFeature.API,      "read"),

    PROFILES_READ   (KenaiFeature.PROFILES, "read"),
    PROFILES_CREATE (KenaiFeature.PROFILES, "create"),
    PROFILES_UPDATE (KenaiFeature.PROFILES, "update"),
    PROFILES_DELETE (KenaiFeature.PROFILES, "delete")
            ;

    static KenaiActivity valueOf(String featureText, String activityText) {
        for (KenaiActivity kenaiActivity : KenaiActivity.values()) {
            if (kenaiActivity.feature.getId().equals(featureText) && kenaiActivity.name.equals(activityText)) return kenaiActivity;
        }

        return null;
    }

    private final KenaiFeature feature;
    private final String name;

    KenaiActivity(KenaiFeature feature, String name) {
        this.feature = feature;
        this.name = name;
    }

    public KenaiFeature getFeature() {
        return feature;
    }

    public String getName() {
        return name;
    }
}
