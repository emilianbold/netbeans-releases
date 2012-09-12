/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jira.autoupdate;

import com.atlassian.connector.eclipse.internal.jira.core.model.JiraVersion;
import com.atlassian.connector.eclipse.internal.jira.core.model.ServerInfo;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.core.runtime.CoreException;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.modules.autoupdate.ui.api.PluginManager;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.JiraConfig;
import org.netbeans.modules.jira.repository.JiraConfiguration;
import org.netbeans.modules.jira.repository.JiraRepository;
import org.netbeans.modules.jira.util.JiraUtils;
import org.netbeans.modules.mylyn.util.BugtrackingCommand;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class JiraAutoupdate {

    static final JiraVersion SUPPORTED_JIRA_VERSION;
    static {
        String version = System.getProperty("netbeans.t9y.jira.supported.version");
        SUPPORTED_JIRA_VERSION = version != null ? new JiraVersion(version) : new JiraVersion("5.0"); // NOI18N
    }
    static final String JIRA_MODULE_CODE_NAME = "org.netbeans.modules.jira"; // NOI18N
    private static final Pattern VERSION_PATTERN = Pattern.compile("^.*version ((\\d+?\\.\\d+?\\.\\d+?)|(\\d+?\\.\\d+?)).*$");
    
    private static Map<String, Long> lastChecks = null;

    /**
     * Checks if the remote JIRA has a version higher then actually supported and if
     * an update is available on the UC.
     *
     * @param repository the repository to check the version for
     * @return true if things are ok or if the user decided to continue even with a
     *         outdated version. False in case a new plugin version is abut to be
     *         downloaded
     */
    public boolean checkAndNotify(JiraRepository repository) {
        Jira.LOG.log(Level.FINE, "JiraAutoupdate.checkAndNotify start");

        try {
            if (wasCheckedToday(getLastCheck(repository))) {
                return true;
            }
            if (!JiraConfig.getInstance().getCheckUpdates()) {
                return true;
            }
            if (!checkSupportedJiraServerVersion(repository)) {
                UpdateElement ue = checkNewJiraPluginAvailable();
                if(ue != null) {
                    AutoupdatePanel panel = new AutoupdatePanel();
                    if (JiraUtils.show(
                            panel,
                            NbBundle.getMessage(JiraAutoupdate.class, "CTL_AutoupdateTitle"), // NOI18N
                            NbBundle.getMessage(JiraAutoupdate.class, "CTL_Yes"), // NOI18N
                            new HelpCtx(JiraAutoupdate.class))) {
                        OperationContainer<InstallSupport> oc = OperationContainer.createForUpdate();
                        oc.add(ue);
                        PluginManager.openInstallWizard(oc);
                        return false;
                    }
                }
            }
        } finally {
            Jira.LOG.log(Level.FINE, "JiraAutoupdate.checkAndNotify finish");                    
        }

        return true;
    }

    UpdateElement checkNewJiraPluginAvailable() {
        List<UpdateUnit> units = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE);
        for (UpdateUnit u : units) {
            if(u.getCodeName().equals(JIRA_MODULE_CODE_NAME)) {
                List<UpdateElement> elements = u.getAvailableUpdates();
                if(elements != null) {
                    for (UpdateElement updateElement : elements) {
                        String desc = updateElement.getDescription();
                        JiraVersion version = getVersion(desc);
                        if(version != null) {
                            if(SUPPORTED_JIRA_VERSION.compareTo(version) < 0){
                                return updateElement;
                            }
                        } else {
                            // looks like we weren't able to
                            // parse the version; on the other hand ->
                            // there is something so lets be optimistic
                            return elements.size() > 0 ? updateElement : null; 
                        }
                    }
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    public boolean checkSupportedJiraServerVersion(final JiraRepository repository) {
        final String[] v = new String[1];
        BugtrackingCommand cmd = new BugtrackingCommand() {
            @Override
            public void execute() throws CoreException, IOException, MalformedURLException {
                JiraConfiguration conf = repository.getConfiguration();
                ServerInfo info = conf.getServerInfo();
                v[0] = info.getVersion();
            }
        };
        repository.getExecutor().execute(cmd, false, false, false);
        if(cmd.hasFailed()) {
            return true; // be optimistic at this point
        }
        JiraVersion version = new JiraVersion(v[0]);
        boolean ret = isSupportedVersion(version);
        if(!ret) {
            Jira.LOG.log(Level.INFO,
                         "JIRA repository [{0}] has version {1}. ", // NOI18N
                         new Object[] {repository.getUrl(), version});
        }
        return ret;
    }

    boolean isSupportedVersion(JiraVersion version) {
        return version.compareTo(SUPPORTED_JIRA_VERSION) <= 0;
    }

    boolean wasCheckedToday(long lastCheck) {
        if (lastCheck < 0) {
            return false;
        }
        Calendar c = Calendar.getInstance();
        c.add(Calendar.SECOND, c.get(Calendar.SECOND) * -1);
        c.add(Calendar.MINUTE, c.get(Calendar.MINUTE) * -1);
        c.add(Calendar.HOUR, c.get(Calendar.HOUR) * -1);
        return lastCheck > c.getTime().getTime();
    }

    private long getLastCheck(JiraRepository repository) {
        if(lastChecks == null) {
            lastChecks = new HashMap<String, Long>(1);
        }
        Long l = lastChecks.get(repository.getUrl());
        if(l == null) {
            lastChecks.put(repository.getUrl(), System.currentTimeMillis());
            return -1;
        }
        return l;
    }

    JiraVersion getVersion(String desc) {
        Matcher m = VERSION_PATTERN.matcher(desc);
        if(m.matches()) {
            return new JiraVersion(m.group(1)) ;
        }
        return null;
    }
}
