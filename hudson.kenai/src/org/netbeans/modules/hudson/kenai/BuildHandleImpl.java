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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.modules.hudson.api.HudsonChangeListener;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.kenai.ui.spi.BuildHandle;
import org.openide.util.WeakListeners;

class BuildHandleImpl extends BuildHandle implements HudsonChangeListener {

    private final HudsonInstance instance;
    private final String jobName;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public BuildHandleImpl(HudsonInstance instance, String jobName) {
        this.instance = instance;
        this.jobName = jobName;
        instance.addHudsonChangeListener(WeakListeners.create(HudsonChangeListener.class, this, instance));
    }

    private HudsonJob job() {
        for (HudsonJob job : instance.getJobs()) {
            if (job.getName().equals(jobName)) {
                return job;
            }
        }
        return null;
    }

    public String getDisplayName() {
        HudsonJob job = job();
        if (job == null) {
            return "";
        }
        return job.getDisplayName();
    }

    public Status getStatus() {
        HudsonJob job = job();
        if (job == null) {
            return Status.UNKNOWN;
        }
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

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    public void stateChanged() {
        pcs.firePropertyChange(PROP_STATUS, null, null);
    }

    public void contentChanged() {}

}
