/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.bugtracking;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.team.ide.spi.IDEServices;
import org.netbeans.modules.team.ide.spi.ProjectServices;
import org.netbeans.modules.bugtracking.team.spi.TeamAccessor;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.team.spi.RecentIssue;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;

/**
 * Top level class that manages issues from all repositories.  
 * 
 * @author Maros Sandor
 */
public final class BugtrackingManager implements LookupListener {
    
    private static BugtrackingManager instance;

    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.bugtracking.BugtrackingManager"); // NOI18N

    private final RequestProcessor rp = new RequestProcessor("Bugtracking manager"); // NOI18N

    /**
     * Holds all registered connectors.
     */
    private final Collection<BugtrackingConnector> connectors = new ArrayList<BugtrackingConnector>(2);

    /**
     * Result of Lookup.getDefault().lookup(new Lookup.Template<RepositoryConnector>(RepositoryConnector.class));
     */
    private Lookup.Result<BugtrackingConnector> connectorsLookup;

    private Map<String, List<RecentIssue>> recentIssues;
    private TeamAccessor[] teamAccessors;

    private IDEServices ideServices;
    private ProjectServices projectServices;
    
    public synchronized static BugtrackingManager getInstance() {
        if(instance == null) {
            instance = new BugtrackingManager();
        }
        return instance;
    }

    private BugtrackingManager() { }

    public RequestProcessor getRequestProcessor() {
        return rp;
    }

    public DelegatingConnector[] getConnectors() {
        synchronized(connectors) {
            if(connectorsLookup == null) {
                refreshConnectors();
            }
            return connectors.toArray(new DelegatingConnector[connectors.size()]);
        }
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        refreshConnectors();
    }

    public List<IssueImpl> getRecentIssues(RepositoryImpl repo) {
        assert repo != null;
        List<RecentIssue> l = getRecentIssues().get(repo.getId());
        if(l == null) {
            return Collections.EMPTY_LIST;
        }
        List<IssueImpl> ret = new ArrayList<IssueImpl>(l.size());
        for (RecentIssue recentIssue : l) {
            ret.add(APIAccessor.IMPL.getImpl(recentIssue.getIssue()));
        }
        return ret;
    }

    public void addRecentIssue(RepositoryImpl repo, IssueImpl issue) {
        assert repo != null && issue != null;
        if (issue.getID() == null) {
            return;
        }
        List<RecentIssue> l = getRecentIssues().get(repo.getId());
        if(l == null) {
            l = new LinkedList<RecentIssue>();
            getRecentIssues().put(repo.getId(), l);
        }
        for (RecentIssue i : l) {
            if(i.getIssue().getID().equals(issue.getID())) {
                l.remove(i);
                break;
            }
        }
        l.add(0, new RecentIssue(issue.getIssue(), System.currentTimeMillis()));
        if(LOG.isLoggable(Level.FINE)) {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");   // NOI18N
            for (RecentIssue ri : l) {
                LOG.log(
                        Level.FINE,
                        "recent issue: [{0}, {1}, {2}]",                        // NOI18N
                        new Object[]{
                            APIAccessor.IMPL.getImpl(ri.getIssue()).getRepositoryImpl().getDisplayName(),
                            ri.getIssue().getID(),
                            f.format(new Date(ri.getTimestamp()))});                                                   // NOI18N
            }
        }
    }

    public Map<String, List<RecentIssue>> getAllRecentIssues() {
        return Collections.unmodifiableMap(getRecentIssues());
    }

    public TeamAccessor[] getTeamAccessors() {
        if (teamAccessors == null) {
            Collection<? extends TeamAccessor> coll = Lookup.getDefault().lookupAll(TeamAccessor.class);
            teamAccessors = coll.toArray(new TeamAccessor[coll.size()]);
        }
        return teamAccessors;
    }

    private Map<String, List<RecentIssue>> getRecentIssues() {
        if(recentIssues == null) {
            recentIssues = new HashMap<String, List<RecentIssue>>();
        }
        return recentIssues;
    }

    private void refreshConnectors() {
        synchronized (connectors) {
            if (connectorsLookup == null) {
                connectorsLookup = Lookup.getDefault().lookup(new Lookup.Template<BugtrackingConnector>(BugtrackingConnector.class));
                connectorsLookup.addLookupListener(this);
            }
            Collection<? extends BugtrackingConnector> conns = connectorsLookup.allInstances();
            if(LOG.isLoggable(Level.FINER)) {
                for (BugtrackingConnector c : conns) {
                    DelegatingConnector dc = 
                        c instanceof DelegatingConnector ? 
                            (DelegatingConnector) c :
                            new DelegatingConnector(c, "Unknown", "Unknown", "Unknown", null); // NOI18N
                    LOG.log(Level.FINER, "registered provider: {0}", dc.getDisplayName()); // NOI18N
                }
            }
            connectors.clear();
            connectors.addAll(conns);
        }
    }

    DelegatingConnector getConnector(String connectorId) {
        assert connectorId != null;
        for(DelegatingConnector c : getConnectors()) {
            if(connectorId.equals(c.getID())) {
                return c;
            }
        }
        return null;
    }

    public synchronized IDEServices getIDEServices() {
        if(ideServices == null) {
            ideServices = Lookup.getDefault().lookup(IDEServices.class);
        }
        return ideServices;
    }
    
    public synchronized ProjectServices getProjectServices() {
        if(projectServices == null) {
            projectServices = Lookup.getDefault().lookup(ProjectServices.class);
        }
        return projectServices;
    }
    
}
