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

package org.netbeans.modules.bugzilla.autoupdate;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaVersion;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.BugzillaConfig;
import org.netbeans.modules.bugzilla.repository.BugzillaConfiguration;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class BugzillaAutoupdate {

    static final BugzillaVersion SUPPORTED_BUGZILLA_VERSION;
    static {
        String version = System.getProperty("netbeans.t9y.bugzilla.supported.version"); // NOI18N
        SUPPORTED_BUGZILLA_VERSION = version != null ? new BugzillaVersion(version) : new BugzillaVersion("3.2.2"); // NOI18N
    }
    static final String BUGZILLA_MODULE_CODE_NAME = "org.netbeans.modules.bugzilla"; // NOI18N

    private static Map<String, Long> lastChecks = null;
    private static final Pattern VERSION_PATTERN = Pattern.compile("^.*version ((\\d+?\\.\\d+?\\.\\d+?)|(\\d+?\\.\\d+?)).*$");

    /**
     * Checks if the remote Bugzilla repository has a version higher then actually
     * supported and if an update is available on the UC.
     *
     * @param repository the repository to check the version for
     * @return true if things are ok or if the user desided to continue even with a
     *         outdated version. False in case a new plugin version is abut to be
     *         downloaded
     */
    public boolean checkAndNotify(BugzillaRepository repository) {
        Bugzilla.LOG.fine("BugzillaAutoupdate.checkAndNotify start");
        try {
            if(wasCheckedToday(getLastCheck(repository))) {
                return true;
            }
            if(!BugzillaConfig.getInstance().getCheckUpdates()) {
                return true;
            }
            if(!checkSupportedBugzillaServerVersion(repository) && checkNewBugzillaPluginAvailable()) {
                AutoupdatePanel panel = new AutoupdatePanel();
                if(BugzillaUtil.show(
                        panel,
                        NbBundle.getMessage(BugzillaAutoupdate.class, "CTL_AutoupdateTitle"), // NOI18N
                        NbBundle.getMessage(BugzillaAutoupdate.class, "CTL_Yes"),             // NOI18N
                        new HelpCtx(BugzillaAutoupdate.class)))
                {
                    BugtrackingUtil.openPluginManager();
                    return false;
                }
            }
        } finally {
            Bugzilla.LOG.fine("BugzillaAutoupdate.checkAndNotify finish");
        }
        return true;
    }

    boolean checkNewBugzillaPluginAvailable() {
        List<UpdateUnit> units = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE);
        for (UpdateUnit u : units) {
            if(u.getCodeName().equals(BUGZILLA_MODULE_CODE_NAME)) {
                List<UpdateElement> elements = u.getAvailableUpdates();
                if(elements != null) {
                    for (UpdateElement updateElement : elements) {
                        String desc = updateElement.getDescription();
                        BugzillaVersion version = getVersion(desc);
                        if(version != null && SUPPORTED_BUGZILLA_VERSION.compareTo(version) < 0) {
                            return true;
                        }
                    }
                    return elements.size() > 0; // looks like we weren't able to
                                                // parse the version; on the other hand ->
                                                // there is something so lets be optimistic
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the repository version is supported
     * @param repository
     * @return true if the repositories version is supported, othrwise false
     */
    boolean checkSupportedBugzillaServerVersion(BugzillaRepository repository) {
        BugzillaConfiguration conf = repository.getConfiguration();
        if(!conf.isValid()) {
            return true; // do not force the wrong version notification
        }
        BugzillaVersion version = conf.getInstalledVersion();
        if(version == null) {
            return true; // do not force the wrong version notification 
        }
        boolean ret = isSupportedVersion(version);
        if(!ret) {
            Bugzilla.LOG.log(Level.WARNING,
                         "Supported Bugzilla versions are <= {0}. The bugzilla repository [{1}] has version {2}. " + // NOI18N
                         "Please check the UC for a newer plugin.", // NOI18N
                         new Object[] {SUPPORTED_BUGZILLA_VERSION, repository.getUrl(), version});
        }
        return ret;
    }

    boolean isSupportedVersion(BugzillaVersion version) {
        return version.compareTo(SUPPORTED_BUGZILLA_VERSION) <= 0;
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

    private long getLastCheck(BugzillaRepository repository) {
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

    BugzillaVersion getVersion(String desc) {
        Matcher m = VERSION_PATTERN.matcher(desc);
        if(m.matches()) {
            return new BugzillaVersion(m.group(1)) ;
        }
        return null;
    }
}
