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
package org.netbeans.modules.ods.tasks;

import com.tasktop.c2c.server.tasks.domain.PredefinedTaskQuery;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiRepositoryProvider;
import org.netbeans.modules.bugtracking.spi.RepositoryController;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.ods.tasks.issue.C2CIssue;
import org.netbeans.modules.ods.tasks.query.C2CQuery;
import org.netbeans.modules.ods.tasks.repository.C2CRepository;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Stupka
 */
public class C2CRepositoryProvider extends KenaiRepositoryProvider<C2CRepository, C2CQuery, C2CIssue> {

    @Override
    public RepositoryInfo getInfo(C2CRepository r) {
        return r.getInfo();
    }

    @Override
    public Image getIcon(C2CRepository r) {
        return r.getIcon();
    }

    @Override
    public C2CIssue[] getIssues(C2CRepository r, String... ids) {
        return r.getIssues(ids);
    }

    @Override
    public void remove(C2CRepository r) {
        r.remove();
    }

    @Override
    public RepositoryController getController(C2CRepository r) {
        return r.getControler();
    }

    @Override
    public C2CQuery createQuery(C2CRepository r) {
        return r.createQuery();
    }

    @Override
    public C2CIssue createIssue(C2CRepository r) {
        return r.createIssue();
    }

    @Override
    public Collection<C2CQuery> getQueries(C2CRepository r) {
        return r.getQueries();
    }

    @Override
    public Lookup getLookup(C2CRepository r) {
        return r.getLookup();
    }

    @Override
    public Collection<C2CIssue> simpleSearch(C2CRepository r, String criteria) {
        return r.simpleSearch(criteria);
    }

    @Override
    public void removePropertyChangeListener(C2CRepository r, PropertyChangeListener listener) {
        r.removePropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(C2CRepository r, PropertyChangeListener listener) {
        r.addPropertyChangeListener(listener);
    }

    /************************************************************************************
     * Team Support
     ************************************************************************************/

    @Override
    public C2CQuery getAllIssuesQuery (C2CRepository repository) {
        return (repository).getPredefinedQuery(PredefinedTaskQuery.ALL);
    }

    @Override
    public C2CQuery getMyIssuesQuery (C2CRepository repository) {
        return (repository).getPredefinedQuery(PredefinedTaskQuery.MINE);
    }
    
}
