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
package org.netbeans.modules.hudson.ui.notification;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJob.Color;

public class ProblemNotificationController {

    private static final Logger LOG = Logger.getLogger(ProblemNotificationController.class.getName());

    private final HudsonInstance instance;
    private final Set<ProblemNotification> notifications = new HashSet<ProblemNotification>();

    public ProblemNotificationController(HudsonInstance instance) {
        this.instance = instance;
    }

    public synchronized void updateNotifications() {
        LOG.log(Level.FINE, "Updating notifications for {0}", instance);
        for (HudsonJob job : instance.getJobs()) {
            if (!job.isSalient()) {
                continue;
            }
            ProblemNotification n;
            int build = job.getLastCompletedBuild();
            Color color = job.getColor();
            LOG.log(Level.FINER, "{0} has status {1}", new Object[] {job, color});
            switch (color) {
            case red:
                // XXX should check if the user is among the culprits (need API)
                n = new ProblemNotification(job, build, true, false);
                break;
            case red_anime:
                n = new ProblemNotification(job, build, true, true);
                break;
            case yellow:
                n = new ProblemNotification(job, build, false, false);
                break;
            case yellow_anime:
                n = new ProblemNotification(job, build, false, true);
                break;
            default:
                n = null;
            }
            if (n != null && notifications.add(n)) {
                LOG.log(Level.FINE, "Adding {0}", n);
                n.add();
            }
        }
    }

}
