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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.kenai;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.team.spi.TeamBugtrackingConnector.BugtrackingType;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiService;
import org.netbeans.modules.kenai.api.KenaiService.Type;
import org.netbeans.modules.team.server.ui.spi.QueryHandle;

/**
 *
 * @author Tomas Stupka
 */
class TeamProjectImpl extends org.netbeans.modules.bugtracking.team.spi.TeamProject {

    public static String getProjectKey(KenaiProject project) {
        return project.getKenai().getUrl().toString() + ":" + project.getName();
    }

    private KenaiProject project;
    private BugtrackingType type;
    private KenaiFeature feature;

    private TeamProjectImpl(KenaiProject project) {
        assert project != null;
        this.project = project;
    }

    public static TeamProjectImpl getInstance(KenaiProject project) {
        synchronized (Support.getInstance().projectsCache) {
            String projectKey = getProjectKey(project);
            WeakReference<TeamProjectImpl> wr = Support.getInstance().projectsCache.get(projectKey);
            TeamProjectImpl result = null;
            if (wr == null || (result = wr.get()) == null) {
                result = new TeamProjectImpl(project);
                Support.getInstance().projectsCache.put(getProjectKey(project), new WeakReference<TeamProjectImpl>(result));
            } else {
                result = wr.get();
                result.project = project;
            }
            return result;
        }
    }

    @Override
    public URL getWebLocation() {
        return project.getWebLocation();
    }

    @Override
    public String getFeatureLocation() {
        KenaiFeature f = getFeature();
        return f != null ? f.getLocation() : null;
    }

    public KenaiProject getProject() {
        return project;
    }

    @Override
    public BugtrackingType getType() {
        if (type == null) {
            setupFeature();
        }
        return type;
    }

    @Override
    public String getName() {
        return project.getName();
    }

    @Override
    public String getDisplayName() {
        return project.getDisplayName();
    }

    private KenaiFeature getFeature() {
        if(feature == null) {
            setupFeature();
        }
        return feature;
    }

    private void setupFeature() {
        try {
            KenaiFeature[] features = project.getFeatures(Type.ISSUES);

            // XXX check for available connectors and if only one available then
            // lookup only the relevant bugtracking type

            // look for bugzilla first ...
            for (KenaiFeature f : features) {
                if (KenaiService.Names.BUGZILLA.equals(f.getService())) {
                    type = BugtrackingType.BUGZILLA;
                    feature = f;
                    break;
                }
            }
            // ... then jira if no bugzilla found
            if(type == null) {
                for (KenaiFeature f : features) {
                    if (KenaiService.Names.JIRA.equals(f.getService())) {
                        type = BugtrackingType.JIRA;
                        feature = f;
                        break;
                    }
                }
            }
        } catch (KenaiException kenaiException) {
            Support.LOG.log(Level.SEVERE, kenaiException.getMessage(), kenaiException);
        }
    }

    @Override
    public void fireQueryActivated(Query query) {
        KenaiHandler handler = Support.getInstance().getKenaiHandler(project.getKenai());
        if(handler == null) {
            return;
        }
        List<QueryHandle> queries = handler.getQueryHandles(project.getName(), query);
        assert queries.size() == 1;
        QueryHandle qh = queries.get(0);
        assert qh instanceof QueryHandleImpl;
        if(qh instanceof QueryHandleImpl) {
            ((QueryHandleImpl) qh).fireQueryActivated();
        }
    }

}
