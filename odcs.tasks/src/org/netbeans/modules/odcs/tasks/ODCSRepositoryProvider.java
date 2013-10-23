/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.odcs.tasks;

import com.tasktop.c2c.server.tasks.domain.PredefinedTaskQuery;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import org.netbeans.modules.bugtracking.team.spi.TeamProject;
import org.netbeans.modules.bugtracking.team.spi.TeamRepositoryProvider;
import org.netbeans.modules.bugtracking.spi.RepositoryController;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.odcs.tasks.issue.ODCSIssue;
import org.netbeans.modules.odcs.tasks.query.ODCSQuery;
import org.netbeans.modules.odcs.tasks.repository.ODCSRepository;

/**
 *
 * @author Tomas Stupka
 */
public class ODCSRepositoryProvider extends TeamRepositoryProvider<ODCSRepository, ODCSQuery, ODCSIssue> {

    @Override
    public RepositoryInfo getInfo(ODCSRepository r) {
        return r.getInfo();
    }

    @Override
    public Image getIcon(ODCSRepository r) {
        return r.getIcon();
    }

    @Override
    public ODCSIssue[] getIssues(ODCSRepository r, String... ids) {
        return r.getIssues(ids);
    }

    @Override
    public void remove(ODCSRepository r) {
        r.remove();
    }

    @Override
    public RepositoryController getController(ODCSRepository r) {
        return r.getControler();
    }

    @Override
    public ODCSQuery createQuery(ODCSRepository r) {
        return r.createQuery();
    }

    @Override
    public ODCSIssue createIssue(ODCSRepository r) {
        return r.createIssue();
    }

    @Override
    public Collection<ODCSQuery> getQueries(ODCSRepository r) {
        return r.getQueries();
    }

    @Override
    public Collection<ODCSIssue> simpleSearch(ODCSRepository r, String criteria) {
        return r.simpleSearch(criteria);
    }

    @Override
    public boolean canAttachFiles(ODCSRepository r) {
        return false;
    }
    
    @Override
    public void removePropertyChangeListener(ODCSRepository r, PropertyChangeListener listener) {
        r.removePropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(ODCSRepository r, PropertyChangeListener listener) {
        r.addPropertyChangeListener(listener);
    }
    
    /************************************************************************************
     * Team Support
     ************************************************************************************/

    @Override
    public ODCSQuery getAllIssuesQuery (ODCSRepository repository) {
        return repository.getPredefinedQuery(PredefinedTaskQuery.ALL);
    }

    @Override
    public ODCSQuery getMyIssuesQuery (ODCSRepository repository) {
        return repository.getPredefinedQuery(PredefinedTaskQuery.MINE);
    }

    @Override
    public TeamProject getTeamProject(ODCSRepository repository) {
        return repository.getKenaiProject();
    }

    @Override
    public ODCSIssue createIssue(ODCSRepository r, String summary, String description) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
