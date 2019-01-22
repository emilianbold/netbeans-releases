/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * <p/>
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 * <p/>
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 * <p/>
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * <p/>
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 * <p/>
 * Contributor(s):
 * <p/>
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jira;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import org.netbeans.modules.bugtracking.spi.RepositoryController;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.spi.RepositoryProvider;
import org.netbeans.modules.jira.issue.NbJiraIssue;
import org.netbeans.modules.jira.query.JiraQuery;
import org.netbeans.modules.jira.repository.JiraRepository;

/**
 *
 * 
 */
public class JiraRepositoryProvider implements RepositoryProvider<JiraRepository, JiraQuery, NbJiraIssue> {

    @Override
    public Image getIcon(JiraRepository r) {
        return r.getIcon();
    }

    @Override
    public RepositoryInfo getInfo(JiraRepository r) {
        return r.getInfo();
    }

    @Override
    public void removed(JiraRepository r) {
        r.remove();
    }

    @Override
    public RepositoryController getController(JiraRepository r) {
        return r.getController();
    }

    @Override
    public JiraQuery createQuery(JiraRepository r) {
        return r.createQuery();
    }

    @Override
    public NbJiraIssue createIssue(JiraRepository r) {
        return r.createIssue();
    }

    @Override
    public Collection<JiraQuery> getQueries(JiraRepository r) {
        return r.getQueries();
    }

    @Override
    public Collection<NbJiraIssue> simpleSearch(JiraRepository r, String criteria) {
        return r.simpleSearch(criteria);
    }

    @Override
    public Collection<NbJiraIssue> getIssues(JiraRepository r, String[] ids) {
        return r.getIssues(ids);
    }

    @Override
    public boolean canAttachFiles(JiraRepository r) {
        return true;
    }

    @Override
    public void removePropertyChangeListener(JiraRepository r, PropertyChangeListener listener) {
        r.removePropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(JiraRepository r, PropertyChangeListener listener) {
        r.addPropertyChangeListener(listener);
    }

    @Override
    public NbJiraIssue createIssue(JiraRepository r, String summary, String description) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
