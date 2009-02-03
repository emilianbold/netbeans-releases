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

package org.netbeans.modules.hudson.impl;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;
import static org.netbeans.modules.hudson.constants.HudsonInstanceConstants.*;
import org.netbeans.modules.hudson.spi.ProjectHudsonProvider;

/**
 *
 * @author mkleint
 */
class ProjectHIP extends HudsonInstanceProperties {

    private final Set<Project> providers = new HashSet<Project>();
    public ProjectHIP() {
        super("", "");
    }

    public void addProvider(Project prov) {
        synchronized (providers) {
            providers.add(prov);
        }
        ProjectHudsonProvider p = prov.getLookup().lookup(ProjectHudsonProvider.class);
        if (p != null && p.isAssociated()) {
            String u = p.getServerUrl();
            put(INSTANCE_URL, u);
            put(INSTANCE_NAME, u.replaceFirst("https?://", "").replaceFirst("/$", "")); // NOI18N
            setPreferredJobs();
        }
        //TODO listen on changes from the provider and refire them as changed properties.
    }

    public void removeProvider(Project prov) {
        synchronized (providers) {
            providers.remove(prov);
        }
        setPreferredJobs();
    }

    public Set<Project> getProviders() {
        synchronized (providers) {
            return new HashSet<Project>(providers);
        }
    }

    public List<PropertyChangeListener> getCurrentListeners() {
        List<PropertyChangeListener> lst = new ArrayList<PropertyChangeListener>();
        synchronized (listeners) {
            lst.addAll(listeners);
        }
        return lst;
    }

    private void setPreferredJobs() {
        String list = "";
        for (Project prj : getProviders()) {
            ProjectHudsonProvider p = prj.getLookup().lookup(ProjectHudsonProvider.class);
            if (p != null && p.isAssociated()) {
                String name = p.getJobName();
                if (name != null && name.trim().length() > 0) {
                    list = list + "|" + name.trim(); //NOI18N
                }
            }
        }
        put(INSTANCE_PREF_JOBS, list);
    }

}
