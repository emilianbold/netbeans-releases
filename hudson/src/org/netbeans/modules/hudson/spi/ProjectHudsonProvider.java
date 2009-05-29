/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.hudson.spi;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.util.Utilities;
import org.openide.util.Lookup;

/**
 * Documents existence of a Hudson job associated with the project.
 * Should be registered to global lookup.
 */
public abstract class ProjectHudsonProvider {

    /**
     * Produces a singleton which delegates to all registered providers.
     * @return a proxy
     */
    public static ProjectHudsonProvider getDefault() {
        return new ProjectHudsonProvider() {
            public Association findAssociation(Project p) {
                for (ProjectHudsonProvider php : Lookup.getDefault().lookupAll(ProjectHudsonProvider.class)) {
                    Association a = php.findAssociation(p);
                    if (a != null) {
                        return a;
                    }
                }
                return null;
            }
            public boolean recordAssociation(Project p, Association a) {
                for (ProjectHudsonProvider php : Lookup.getDefault().lookupAll(ProjectHudsonProvider.class)) {
                    if (php.recordAssociation(p, a)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    /**
     * Determines whether a given project is associated with any Hudson instance.
     * @param project a source project
     * @return an association with Hudson, or null if none is known
     */
    public abstract Association findAssociation(Project project);

    /**
     * Tries to find a source project matching a given association.
     * The default implementation just checks open projects.
     * @param assoc a possible association
     * @return a matching project, or null
     */
    public Project findAssociatedProject(Association assoc) {
        Map<Association,Project> assocs = new HashMap<Association,Project>();
        for (Project p : OpenProjects.getDefault().getOpenProjects()) {
            Association found = findAssociation(p);
            if (assoc.equals(found)) {
                return p;
            }
            if (!assocs.containsKey(assoc)) {
                assocs.put(found, p);
            } else {
                // In case of ambiguity, do not return anything.
                // XXX better would be for this method to return Collection<Project>
                // so Hyperlinker could try all the alternatives in turn
                assocs.put(found, null);
            }
        }
        if (assoc.getJobName() != null) {
            // May happen that the PHP only knows the server URL, not the specific job.
            Project p = assocs.get(Association.fromString(assoc.getServerUrl()));
            if (p != null) {
                return p;
            }
        }
        return null;
    }

    /**
     * Attempts to record an association between a project and a job.
     * @param project a source project
     * @param assoc a Hudson job, perhaps newly created; or null to clear any association
     * @return true if the association was in fact recorded, false if not
     */
    public abstract boolean recordAssociation(Project project, Association assoc);

    /**
     * An association with a Hudson job.
     */
    public static final class Association {

        private final String serverURL;
        private final String jobName;

        /**
         * Creates an association.
         * @param serverURL as {@link #getServerUrl}
         * @param jobName as {@link #getJobName}
         * @throws IllegalArgumentException if parameters have invalid syntax
         */
        public Association(String serverURL, String jobName) throws IllegalArgumentException {
            URI.create(serverURL); // check syntax
            if (!serverURL.endsWith("/")) { // NOI18N
                throw new IllegalArgumentException(serverURL + " must end in a slash"); // NOI18N
            }
            if (jobName != null && (jobName.length() == 0 || !jobName.trim().equals(jobName))) {
                throw new IllegalArgumentException("Must provide a nonempty or null job name: " + jobName); // NOI18N
            }
            this.serverURL = serverURL;
            this.jobName = jobName;
        }

        /**
         * Creates an association based on a known job.
         * @param job a Hudson job
         * @return an association with the same server URL and job name
         */
        public static Association forJob(HudsonJob job) {
            return new Association(job.getInstance().getUrl(), job.getName());
        }

        /**
         * @return the root URL of the server ending in slash, e.g. {@code http://deadlock.netbeans.org/hudson/}
         */
        public String getServerUrl() {
            return serverURL;
        }

        /**
         * @return the code name of the job on that server; may be null
         */
        public String getJobName() {
            return jobName;
        }

        public @Override boolean equals(Object obj) {
            if (obj == null || !(obj instanceof Association)) {
                return false;
            }
            return toString().equals(obj.toString());
        }

        public @Override int hashCode() {
            return toString().hashCode();
        }

        /**
         * URL of either job or server root.
         */
        public @Override String toString() {
            return jobName != null ? serverURL + "job/" + Utilities.uriEncode(jobName) + "/" : serverURL; // NOI18N
        }

        /**
         * Inverse of {@link #toString}.
         * @return an association based on parsing a Hudson job or root URL, or null
         */
        public static Association fromString(String s) {
            Matcher m = Pattern.compile("(https?://.+?/)(?:job/([^/]+)/?)?").matcher(s); // NOI18N
            if (!m.matches()) {
                return null;
            }
            String jobNameRaw = m.group(2);
            return new Association(m.group(1), jobNameRaw != null ? Utilities.uriDecode(jobNameRaw) : null);
        }

    }

}
