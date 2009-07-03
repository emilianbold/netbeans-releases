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

package org.netbeans.modules.jira.autoupdate;

import java.util.List;
import java.util.logging.Level;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.jira.core.model.JiraVersion;
import org.eclipse.mylyn.internal.jira.core.model.ServerInfo;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.repository.JiraConfiguration;
import org.netbeans.modules.jira.repository.JiraRepository;
import org.netbeans.modules.jira.util.JiraUtils;
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
        SUPPORTED_JIRA_VERSION = version != null ? new JiraVersion(version) : new JiraVersion("3.13.3"); // NOI18N
    }
    static final String JIRA_MODULE_CODE_NAME = "org.netbeans.libs.jira"; // NOI18N

    public void checkAndNotify(JiraRepository repository) {
        if(!checkSupportedJiraServerVersion(repository) && checkNewJiraPluginAvailable()) {
            AutoupdatePanel panel = new AutoupdatePanel();
            if(JiraUtils.show(
                    panel,
                    NbBundle.getMessage(JiraAutoupdate.class, "CTL_AutoupdateTitle"), // NOI18N
                    NbBundle.getMessage(JiraAutoupdate.class, "CTL_Yes"),             // NOI18N
                    new HelpCtx(JiraAutoupdate.class)))
            {
                BugtrackingUtil.openPluginManager();
            }
        }
    }

    boolean checkNewJiraPluginAvailable() {
        List<UpdateUnit> units = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE);
        for (UpdateUnit u : units) {
            if(u.getCodeName().equals(JIRA_MODULE_CODE_NAME)) {
                List<UpdateElement> elements = u.getAvailableUpdates();
                if(elements != null) {
                    return elements.size() > 0;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    boolean checkSupportedJiraServerVersion(JiraRepository repository) {
        JiraConfiguration conf = repository.getConfiguration();
        ServerInfo info = null;
        try {
            info = conf.getServerInfo(new NullProgressMonitor());
        } catch (JiraException ex) {
            Jira.LOG.log(Level.SEVERE, null, ex);
            return false;
        }
        String v = info.getVersion();
        JiraVersion version = new JiraVersion(v);
        boolean ret = isSupportedVersion(version);
        if(!ret) {
            Jira.LOG.log(Level.WARNING,
                         "Supported JIRA versions are <= {0}. JIRA repository [{1}] has version {2}. " +
                         "Please check the UC for a newer plugin version.", // NOI18N
                         new Object[] {SUPPORTED_JIRA_VERSION, repository.getUrl(), version});
        }
        return ret;
    }

    boolean isSupportedVersion(JiraVersion version) {
        return version.compareTo(SUPPORTED_JIRA_VERSION) <= 0;
    }
}
