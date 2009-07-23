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

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.HudsonMavenModuleBuild;
import org.netbeans.modules.hudson.api.UI;
import org.netbeans.modules.kenai.ui.spi.BuildHandle;
import org.openide.util.RequestProcessor;

class BuildHandleImpl extends BuildHandle {

    private final HudsonJob job;

    public BuildHandleImpl(HudsonJob job) {
        this.job = job;
    }

    public String getDisplayName() {
        return job.getDisplayName();
    }

    public Status getStatus() {
        switch (job.getColor()) {
        case blue_anime:
        case yellow_anime:
        case red_anime:
        case aborted_anime:
        case grey_anime:
            return Status.RUNNING;
        case blue:
            return Status.STABLE;
        case yellow:
            return Status.UNSTABLE;
        case red:
            return Status.FAILED;
        default:
            return Status.UNKNOWN;
        }
    }

    // Unused; create a new HJ and hence new BHI after every server refresh w/ changes
    public void addPropertyChangeListener(PropertyChangeListener l) {}
    public void removePropertyChangeListener(PropertyChangeListener l) {}

    public Action getDefaultAction() {
        return new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                // Mostly copied from ProblemNotification.actionPerformed.
                final int build = job.getLastBuild();
                if (build == -1) {
                    UI.selectNode(job.getInstance().getUrl(), job.getName());
                } else {
                    UI.selectNode(job.getInstance().getUrl(), job.getName(), Integer.toString(build));
                }
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        if (build == -1) {
                            job.start();
                            return;
                        }
                        for (HudsonJobBuild b : job.getBuilds()) {
                            if (b.getNumber() == build) {
                                switch (b.getResult()) {
                                case FAILURE:
                                case ABORTED:
                                    UI.showConsoleAction(b).actionPerformed(e);
                                    break;
                                case UNSTABLE:
                                    if (b.getMavenModules().isEmpty()) {
                                        UI.showFailuresAction(b).actionPerformed(e);
                                    } else {
                                        for (HudsonMavenModuleBuild module : b.getMavenModules()) {
                                            switch (module.getColor()) {
                                            case yellow:
                                            case yellow_anime:
                                                UI.showFailuresAction(module).actionPerformed(e);
                                            }
                                        }
                                    }
                                    break;
                                case SUCCESS:
                                case NOT_BUILT:
                                    UI.showChangesAction(b).actionPerformed(e);
                                    break;
                                }
                                break;
                            }
                        }
                    }
                });
            }
        };
    }

}
