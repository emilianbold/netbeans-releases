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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.modules.hudson.api.HudsonChangeListener;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonManager;
import org.netbeans.modules.hudson.api.UI;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiService.Type;
import org.netbeans.modules.kenai.ui.spi.BuildAccessor;
import org.netbeans.modules.kenai.ui.spi.BuildHandle;
import org.netbeans.modules.kenai.ui.spi.ProjectHandle;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=BuildAccessor.class)
public class BuildAccessorImpl extends BuildAccessor {

    private static final Logger LOG = Logger.getLogger(BuildAccessorImpl.class.getName());

    private final Map<ProjectHandle,HudsonChangeListener> listeners = new WeakHashMap<ProjectHandle,HudsonChangeListener>();

    private static Collection<HudsonInstance> findServers(ProjectHandle handle) {
        List<HudsonInstance> servers = new ArrayList<HudsonInstance>();
        String id = handle.getId();
        try {
            KenaiProject prj = Kenai.getDefault().getProject(id);
            if (prj != null) {
                for (KenaiFeature feature : prj.getFeatures(Type.HUDSON)) {
                    String server = feature.getWebLocation().toString();
                    // XXX maybe remove these transient instances when the Kenai projects go away somehow?
                    servers.add(HudsonManager.addInstance(id, server, 5, false));
                }
            }
        } catch (KenaiException x) {
            LOG.log(Level.FINE, "Could not find project " + id, x);
        }
        return servers;
    }

    public boolean isEnabled(ProjectHandle handle) {
        String id = handle.getId();
        try {
            KenaiProject prj = Kenai.getDefault().getProject(id);
            if (prj != null) {
                // Avoid calling findServers as that would load hudson module code
                // just to show that there is a Builds node (even if never expanded).
                return prj.getFeatures(Type.HUDSON).length > 0;
            }
        } catch (KenaiException x) {
            LOG.log(Level.FINE, "Could not find project " + id, x);
        }
        return false;
    }

    public List<BuildHandle> getBuilds(final ProjectHandle handle) {
        HudsonChangeListener newListener;
        synchronized (listeners) {
            if (listeners.containsKey(handle)) {
                newListener = null;
            } else {
                listeners.put(handle, newListener = new HudsonChangeListener() {
                    private void change() {
                        handle.firePropertyChange(ProjectHandle.PROP_BUILD_LIST, null, null);
                    }
                    public void stateChanged() {
                        change();
                    }
                    public void contentChanged() {
                        change();
                    }
                });
            }
        }
        List<BuildHandle> builds = new ArrayList<BuildHandle>();
        for (HudsonInstance instance : findServers(handle)) {
            if (newListener != null) {
                instance.addHudsonChangeListener(WeakListeners.create(HudsonChangeListener.class, newListener, instance));
            }
            for (HudsonJob job : instance.getJobs()) {
                builds.add(new BuildHandleImpl(job));
            }
        }
        return builds;
    }

    public Action getNewBuildAction(ProjectHandle handle) {
        Collection<HudsonInstance> servers = findServers(handle);
        if (servers.isEmpty()) {
            return null;
        } else {
            return UI.createJobAction(servers.iterator().next());
        }
    }

}
