/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */

package org.netbeans.modules.team.server.ui.spi;

import java.util.Collection;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.team.server.ui.spi.BuildHandle.Status;

/**
 * Main access point to Teams's Build API.
 * All methods except those returning an Action or ActionListener are allowed
 * to block indefinetely as they will be called outside AWT thread.
 * However the Dashboard UI may declare appropriate service(s) as unreachable
 * after some configurable time out interval.
 *
 * @author S. Aubrecht
 */
public abstract class BuilderAccessor<P> {

    public abstract Class<P> type();

    /**
     * Determines whether this project has a build service or not
     *
     * @param project
     * @return <code>true</code> in case the given project has a build service otherwise <code>false</code>
     */
    public abstract boolean hasBuilds(ProjectHandle<P> project);

    /**
     * Retrieve the list of jobs in given project. Only watched jobs are
     * included.
     *
     * @return a list of builds (never null)
     */
    public abstract List<JobHandle> getJobs( ProjectHandle<P> project );

    /**
     * Retrieve the handle to a job identified by its name
     * @return a job handle or <code>null</code> if no such job found
     */
    public abstract JobHandle getJob ( ProjectHandle<P> project, String jobName );

    /**
     * @return Action to invoke when user clicks 'New Build...' button, or null to disable
     */
    public abstract Action getNewBuildAction( ProjectHandle<P> project );

    /**
     * Determines the most interresting build handle from a collection of
     * handles. The default implementation returns build with the worst status.
     * If all builds are successful or unknown, null is returned.
     *
     * Note that default action of the returned build handle (see
     * {@link JobHandle#getDefaultAction()}) should have an icon.
     *
     * @return Build handle that deserves user's attention the most, or null if
     * there is no interresting build.
     */
    public JobHandle chooseMostInterrestingJob(
            Collection<? extends JobHandle> builds) {
        JobHandle worst = null;
        for (JobHandle bh : builds) {
            Status status = bh.getStatus();
            if ((status == Status.FAILED || status == Status.UNSTABLE)
                    && (worst == null
                    || (worst.getStatus() == Status.UNSTABLE
                    && status == Status.FAILED))) {
                worst = bh;
            }
        }
        return worst;
    }
}
