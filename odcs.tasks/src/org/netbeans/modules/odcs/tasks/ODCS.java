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

import com.tasktop.c2c.server.common.service.web.ApacheHttpRestClientDelegate;
import com.tasktop.c2c.server.tasks.domain.Priority;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.service.TaskServiceClient;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import oracle.eclipse.tools.cloud.dev.tasks.CloudDevClient;
import oracle.eclipse.tools.cloud.dev.tasks.CloudDevRepositoryConnector;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.netbeans.modules.bugtracking.commons.SimpleIssueFinder;
import org.netbeans.modules.bugtracking.issuetable.IssueNode;
import org.netbeans.modules.bugtracking.spi.BugtrackingSupport;
import org.netbeans.modules.bugtracking.spi.IssueFinder;
import org.netbeans.modules.bugtracking.spi.IssuePriorityInfo;
import org.netbeans.modules.bugtracking.spi.IssuePriorityProvider;
import org.netbeans.modules.bugtracking.spi.IssueScheduleInfo;
import org.netbeans.modules.bugtracking.spi.IssueScheduleProvider;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.netbeans.modules.mylyn.util.MylynSupport;
import org.netbeans.modules.odcs.tasks.issue.ODCSIssue;
import org.netbeans.modules.odcs.tasks.query.ODCSQuery;
import org.netbeans.modules.odcs.tasks.repository.ODCSRepository;
import org.openide.util.RequestProcessor;

/**
 *
 * 
 */
public class ODCS {
    
    private static ODCS instance;
    public final static Logger LOG = Logger.getLogger("org.netbeans.modules.odcs.tasks"); // NOI18N
    
    private CloudDevRepositoryConnector rc;
    
    private RequestProcessor rp;
    private RequestProcessor rpp;
    
    public static ODCS getInstance() {
        if(instance == null) {
            instance = new ODCS();
            instance.init();
        }
        return instance;
    }
    private ODCSIssueProvider odcsIssueProvider;
    private ODCSQueryProvider odcsQueryProvider;
    private ODCSRepositoryProvider odcsRepositoryProvider;
    private IssueStatusProvider<ODCSRepository, ODCSIssue> isp;    
    private IssuePriorityProvider<ODCSIssue> ipp;    
    private BugtrackingSupport<ODCSRepository, ODCSQuery, ODCSIssue> bf;
    private IssueNode.ChangesProvider<ODCSIssue> ocp;
    private IssueFinder issueFinder;
    private IssueScheduleProvider<ODCSIssue> ischp;

    private void init() {
        rc = MylynRepositoryConnectorProvider.getInstance().getConnector();
        MylynSupport.getInstance().addRepositoryListener(rc.getCloudDevClientManager());
    }
    
    public CloudDevRepositoryConnector getRepositoryConnector() {
        return rc;
    }
    
    public BugtrackingSupport<ODCSRepository, ODCSQuery, ODCSIssue> getBugtrackingFactory() {
        if(bf == null) {
            bf = new BugtrackingSupport<>(getRepositoryProvider(), getQueryProvider(), getIssueProvider());
        }    
        return bf;
    }
    
    public ODCSIssueProvider getIssueProvider() {
        if(odcsIssueProvider == null) {
            odcsIssueProvider = new ODCSIssueProvider();
        }
        return odcsIssueProvider; 
    }
    public ODCSQueryProvider getQueryProvider() {
        if(odcsQueryProvider == null) {
            odcsQueryProvider = new ODCSQueryProvider();
        }
        return odcsQueryProvider; 
    }
    public ODCSRepositoryProvider getRepositoryProvider() {
        if(odcsRepositoryProvider == null) {
            odcsRepositoryProvider = new ODCSRepositoryProvider();
        }
        return odcsRepositoryProvider; 
    }    

    public IssueStatusProvider<ODCSRepository, ODCSIssue> getStatusProvider() {
        if(isp == null) {
            isp = new IssueStatusProvider<ODCSRepository, ODCSIssue>() {
                @Override
                public IssueStatusProvider.Status getStatus(ODCSIssue issue) {
                    return issue.getStatus();
                }
                @Override
                public void setSeenIncoming(ODCSIssue issue, boolean seen) {
                    issue.setUpToDate(seen);
                }
                @Override
                public void removePropertyChangeListener(ODCSIssue issue, PropertyChangeListener listener) {
                    issue.removePropertyChangeListener(listener);
                }
                @Override
                public void addPropertyChangeListener(ODCSIssue issue, PropertyChangeListener listener) {
                    issue.addPropertyChangeListener(listener);
                }
                @Override
                public Collection<ODCSIssue> getUnsubmittedIssues(ODCSRepository r) {
                    return r.getUnsubmittedIssues();
                }
                @Override
                public void discardOutgoing(ODCSIssue i) {
                    i.discardLocalEdits();
                }
                @Override
                public boolean submit (ODCSIssue data) {
                    return data.submitAndRefresh();
                }                
            };
        }
        return isp;
    }
    
    public IssuePriorityProvider<ODCSIssue> getPriorityProvider(final ODCSRepository repository) {
        if(ipp == null) {
            ipp = new IssuePriorityProvider<ODCSIssue>() {
                private IssuePriorityInfo[] infos;

                @Override
                public String getPriorityID(ODCSIssue i) {
                    return i.getPriorityID();
                }

                @Override
                public IssuePriorityInfo[] getPriorityInfos() {
                    if(infos == null) {
                        RepositoryConfiguration rc = repository.getRepositoryConfiguration(false);
                        if(rc != null) {
                            List<Priority> priorities = rc.getPriorities();
                            Collections.sort(priorities, new Comparator<Priority>() {
                                @Override
                                public int compare(Priority p1, Priority p2) {
                                    if(p1 == null && p2 == null) return 0;
                                    if(p1 == null) return -1;
                                    if(p2 == null) return 1;
                                    return p1.getSortkey().compareTo(p2.getSortkey());
                                }
                            });
                            infos = new IssuePriorityInfo[priorities.size()];
                            for (int i = 0; i < priorities.size(); i++) {
                                Priority priority = priorities.get(i);
                                infos[i] = new IssuePriorityInfo(priority.getId().toString(), priority.getValue());
                            }
                        } else {
                            infos = new IssuePriorityInfo[0];
                        }
                    } 
                    return infos;
                }
            };
        }
        return ipp;
    }
    
    public IssueScheduleProvider<ODCSIssue> getSchedulingProvider () {
        if(ischp == null) {
            ischp = new IssueScheduleProvider<ODCSIssue>() {
                @Override
                public void setSchedule (ODCSIssue i, IssueScheduleInfo scheduleInfo) {
                    i.setTaskScheduleDate(scheduleInfo, true);
                }

                @Override
                public Date getDueDate (ODCSIssue i) {
                    return i.getPersistentDueDate();
                }

                @Override
                public IssueScheduleInfo getSchedule (ODCSIssue i) {
                    return i.getPersistentScheduleInfo();
                }
            };
        }
        return ischp;
    }
    
    public RequestProcessor getRequestProcessor() {
        if(rp == null) {
            rp = new RequestProcessor("ODCS Tasks", 1, true); // NOI18N
        }
        return rp;
    }

    public RequestProcessor getParallelRequestProcessor() {
        if(rpp == null) {
            rpp = new RequestProcessor("ODCS Parallel Tasks", 20, true); // NOI18N
        }
        return rpp;
    }

    public CloudDevClient getCloudDevClient(TaskRepository taskRepository) {
        CloudDevClient client = rc.getCloudDevClientManager().getCloudDevClient(taskRepository);

        // Workaround for missing proxy configuration in CloudDevClient (from oracle-eclipse-tools-cloud-dev-tasks.jar).
        Field f;
        try {
            f = CloudDevClient.class.getDeclaredField("taskServiceClient"); // NOI18N
            f.setAccessible(true);
            Object value = f.get(client);
            if (value != null) { // TaskServiceClient already set
                return client;
            }
        } catch (Exception ex) {
            ODCS.LOG.log(Level.WARNING, "", ex);
            return client;
        }

        Proxy proxy = null;
        String proxyHost = taskRepository.getProperty(TaskRepository.PROXY_HOSTNAME);
        if (proxyHost != null && proxyHost.length() > 0) {
            URI uri = null;
            try {
                uri = new URI(taskRepository.getRepositoryUrl());
            } catch (URISyntaxException ex) {
                ODCS.LOG.log(Level.INFO, "", ex);
            }
            List<Proxy> proxies = ProxySelector.getDefault().select(uri);
            for (Proxy p : proxies) {
                if (p.type() != Proxy.Type.DIRECT) {
                    SocketAddress addr = p.address();
                    if (addr instanceof InetSocketAddress) {                        
                        InetSocketAddress inet = (InetSocketAddress) addr;
                        if (proxyHost.equals(inet.getHostString())) {
                            proxy = p;
                            break;
                        }                        
                    }
                }
            }            
        }

        AuthenticationCredentials credentials = taskRepository.getCredentials(AuthenticationType.HTTP);
        ApacheHttpRestClientDelegate restDelegate;
        if (proxy != null) {
            AuthenticationCredentials proxyCredentials = taskRepository.getCredentials(AuthenticationType.PROXY);
            if (proxyCredentials != null) {
                restDelegate = new ApacheHttpRestClientDelegate(credentials.getUserName(), credentials.getPassword(),
                        proxy, proxyCredentials.getUserName(), proxyCredentials.getPassword());
            } else {
                restDelegate = new ApacheHttpRestClientDelegate(credentials.getUserName(), credentials.getPassword(), proxy);
            }
        } else {
            restDelegate = new ApacheHttpRestClientDelegate(credentials.getUserName(), credentials.getPassword());
        }
        TaskServiceClient taskServiceClient = new TaskServiceClient();
        taskServiceClient.setBaseUrl(taskRepository.getRepositoryUrl());
        taskServiceClient.setRestClientDelegate(restDelegate);

        try {
            f.set(client, taskServiceClient);
        } catch (Exception ex) {
            ODCS.LOG.log(Level.WARNING, "", ex);
        }
        return client;
    }

    public IssueNode.ChangesProvider<ODCSIssue> getChangesProvider() {
        if(ocp == null) {
            ocp = new IssueNode.ChangesProvider<ODCSIssue>() {
                @Override
                public String getRecentChanges(ODCSIssue i) {
                    return i.getRecentChanges();
                }
            };
        }
        return ocp;
    }    
    
    public IssueFinder getODCSIssueFinder() {
        if(issueFinder == null) {
            issueFinder = new IssueFinder() {
                @Override
                public int[] getIssueSpans(CharSequence text) {
                    return SimpleIssueFinder.getInstance().getIssueSpans(text);
                }
                @Override
                public String getIssueId(String issueHyperlinkText) {
                    return SimpleIssueFinder.getInstance().getIssueId(issueHyperlinkText);
                }
            };
        }
        return issueFinder;
    }        
}
