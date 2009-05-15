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

package org.netbeans.modules.hudson.kenai;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonManager;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiService.Type;
import org.netbeans.modules.kenai.ui.spi.BuildAccessor;
import org.netbeans.modules.kenai.ui.spi.BuildHandle;
import org.netbeans.modules.kenai.ui.spi.ProjectHandle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=BuildAccessor.class)
public class BuildAccessorImpl extends BuildAccessor {

    private static final Logger LOG = Logger.getLogger(BuildAccessorImpl.class.getName());

    public List<BuildHandle> getBuilds(ProjectHandle handle) {
        String id = handle.getId();
        try {
            KenaiProject prj = Kenai.getDefault().getProject(id);
            if (prj != null) {
                for (KenaiFeature feature : prj.getFeatures(Type.HUDSON)) {
                    String server = feature.getWebLocation().toString();
                    { // XXX just for testing until real service works
                        LOG.warning("Ignoring reported server location " + server);
                        server = "http://localhost:8080/";
                    }
                    // XXX maybe remove these transient instances when the Kenai projects go away somehow?
                    HudsonInstance instance = HudsonManager.addInstance(id, server, 5, false);
                    List<BuildHandle> builds = new ArrayList<BuildHandle>();
                    for (HudsonJob job : instance.getJobs()) {
                        builds.add(new BuildHandleImpl(instance, job.getName()));
                    }
                    // XXX can call handle.firePropertyChange(ProjectHandle.PROP_BUILD_LIST) as needed
                    return builds;
                }
            }
        } catch (KenaiException x) {
            LOG.log(Level.INFO, "Could not find project " + id, x);
        }
        return Collections.emptyList();
    }

    public ActionListener getNewBuildAction(ProjectHandle project) {
        // XXX return CreateJob instance
        return null;
    }

    public ActionListener getDefaultAction(BuildHandle build) {
        // XXX similar to ProblemNotification if not stable, else just select build node?
        return null;
    }

}
