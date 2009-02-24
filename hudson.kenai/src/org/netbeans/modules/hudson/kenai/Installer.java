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

package org.netbeans.modules.hudson.kenai;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import org.netbeans.modules.hudson.api.HudsonChangeAdapter;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonManager;
import org.openide.modules.ModuleInstall;

public class Installer extends ModuleInstall {

    public @Override void restored() {
        updateInstances();
        HudsonManager.getDefault().addHudsonChangeListener(new HudsonChangeAdapter() {
            public @Override void contentChanged() {
                updateInstances();
            }
        });
        // XXX may want to also suppress org.netbeans.modules.hudson.ui.notification.HudsonStatusLineElementProvider
    }

    private final Map<HudsonInstance,InstanceHandler> handlers = new HashMap<HudsonInstance,InstanceHandler>();
    private synchronized void updateInstances() {
        Collection<HudsonInstance> instances = HudsonManager.getDefault().getInstances();
        Iterator<Map.Entry<HudsonInstance,InstanceHandler>> handlersI = handlers.entrySet().iterator();
        while (handlersI.hasNext()) {
            Map.Entry<HudsonInstance,InstanceHandler> entry = handlersI.next();
            if (!instances.contains(entry.getKey())) {
                entry.getValue().clear();
                handlersI.remove();
            }
        }
        for (HudsonInstance instance : instances) {
            if (!handlers.containsKey(instance)) {
                handlers.put(instance, new InstanceHandler(instance));
            }
        }
    }

    private static class InstanceHandler {

        private final HudsonInstance instance;
        private final Collection<ProblemNotification> notifications = new LinkedList<ProblemNotification>();

        InstanceHandler(HudsonInstance instance) {
            this.instance = instance;
            updateNotifications();
            instance.addHudsonChangeListener(new HudsonChangeAdapter() {
                public @Override void contentChanged() {
                    updateNotifications();
                }
            });
        }

        private synchronized void updateNotifications() {
            // XXX should keep track of which build a notification is for.
            // Otherwise when the user clears a notification we might readd it.
            clear();
            Collection<HudsonJob> jobs = instance.getPreferredJobs();
            if (jobs.isEmpty()) {
                jobs = instance.getJobs();
            }
            for (HudsonJob job : jobs) {
                ProblemNotification n;
                switch (job.getColor()) {
                case red:
                    // XXX should check if the user is among the culprits (need API)
                    n = new ProblemNotification(job, true, false);
                    break;
                case red_anime:
                    n = new ProblemNotification(job, true, true);
                    break;
                case yellow:
                    n = new ProblemNotification(job, false, false);
                    break;
                case yellow_anime:
                    n = new ProblemNotification(job, false, true);
                    break;
                default:
                    n = null;
                }
                if (n != null) {
                    notifications.add(n);
                    n.add();
                }
            }
        }

        void clear() {
            for (ProblemNotification n : notifications) {
                n.remove();
            }
        }

    }

}
