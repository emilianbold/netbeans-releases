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
import java.beans.PropertyChangeListener;
import java.util.Collection;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiQueryProvider;
import org.netbeans.modules.bugtracking.kenai.spi.OwnerInfo;
import org.netbeans.modules.bugtracking.spi.QueryController;
import org.netbeans.modules.ods.tasks.issue.C2CIssue;
import org.netbeans.modules.ods.tasks.query.C2CQuery;
import org.netbeans.modules.ods.tasks.repository.C2CRepository;

/**
 *
 * @author Tomas Stupka
 */
public class C2CQueryProvider extends KenaiQueryProvider<C2CQuery, C2CIssue> {

    @Override
    public String getDisplayName(C2CQuery q) {
        return q.getDisplayName();
    }

    @Override
    public String getTooltip(C2CQuery q) {
        return q.getTooltip();
    }

    @Override
    public QueryController getController(C2CQuery q) {
        return q.getController();
    }

    @Override
    public boolean isSaved(C2CQuery q) {
        return q.isSaved();
    }

    @Override
    public void remove(C2CQuery q) {
        q.remove();
    }

    @Override
    public Collection<C2CIssue> getIssues(C2CQuery q) {
        return q.getIssues();
    }

    @Override
    public boolean contains(C2CQuery q, String id) {
        return q.contains(id);
    }

    @Override
    public void refresh(C2CQuery q) {
        q.getController().refresh(true);
    }

    @Override
    public void removePropertyChangeListener(C2CQuery q, PropertyChangeListener listener) {
        q.removePropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(C2CQuery q, PropertyChangeListener listener) {
        q.addPropertyChangeListener(listener);
    }

    /************************************************************************************
     * Team
     ************************************************************************************/
    
    @Override
    public boolean needsLogin (C2CQuery q) {
        C2CRepository repository = q.getRepository();
        return q != (repository).getPredefinedQuery(PredefinedTaskQuery.ALL)
            && q != (repository).getPredefinedQuery(PredefinedTaskQuery.RECENT);
    }

    @Override
    public void setOwnerInfo (C2CQuery q, OwnerInfo info) {
        q.setOwnerInfo(info);
    }
    
}
