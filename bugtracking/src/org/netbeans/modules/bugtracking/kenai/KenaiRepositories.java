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

package org.netbeans.modules.bugtracking.kenai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiSupport;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.ui.spi.Dashboard;
import org.netbeans.modules.kenai.ui.spi.ProjectHandle;

/**
 *
 * @author Tomas Stupka
 */
public class KenaiRepositories {
    private static KenaiRepositories instance;

    private Map<String, Repository> map = new HashMap<String, Repository>();
    private KenaiRepositories() { }

    public static KenaiRepositories getInstance() {
        if(instance == null) {
            instance = new KenaiRepositories();
        }
        return instance;
    }

    public Repository[] getRepositories() {
        ProjectHandle[] phs = Dashboard.getDefault().getOpenProjects();
        if(phs == null || phs.length == 0) {
            return new Repository[0];
        }
        List<Repository> ret = new ArrayList<Repository>();
        for (ProjectHandle projectHandle : phs) {
            if(projectHandle == null) continue;
            Repository repo = getRepository(projectHandle);
            if(repo != null) {
                ret.add(repo);
            }
        }
        return ret.toArray(new Repository[ret.size()]);
    }

    public Repository getRepository(KenaiProject kp) {
        Repository repository = map.get(kp.getName());
        if(repository != null) {
            return repository;
        }
        BugtrackingConnector[] connectors = BugtrackingManager.getInstance().getConnectors();
        for (BugtrackingConnector c : connectors) {
            KenaiSupport support = c.getLookup().lookup(KenaiSupport.class);
            if(support != null) {
                repository = support.createRepository(kp);
                if(repository != null) {
                    // XXX what if more repos?!
                    map.put(kp.getName(), repository);
                    return repository;
                }
            }
        }
        return null;
    }

    Repository getRepository(ProjectHandle ph) {
        KenaiProject kp = getKenaiProject(ph);
        if(kp == null) {
            BugtrackingManager.LOG.warning("No issue tracking repository available for ProjectHandle [" + ph.getId() + "," + ph.getDisplayName() + "]"); // NOI18N
            return null;
        }

        Repository repository = map.get(kp.getName());
        if(repository != null) {
            return repository;
        }

        repository = getRepository(kp);
        if(repository == null) {
            BugtrackingManager.LOG.warning("No issue tracking repository available for project " + kp.getName()); // NOI18N
            return null;
        }        
        return repository;
    }

    static KenaiProject getKenaiProject(ProjectHandle ph) {
        // XXX cache ???
        try {
            return Kenai.getDefault().getProject(ph.getId());
        } catch (KenaiException ex) {
            BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
