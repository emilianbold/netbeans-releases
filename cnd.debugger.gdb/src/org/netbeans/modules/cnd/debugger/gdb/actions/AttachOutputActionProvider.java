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

package org.netbeans.modules.cnd.debugger.gdb.actions;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.api.execution.ExecutionListener;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.makeproject.api.BuildActionsProvider;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Egor Ushakov
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.makeproject.api.BuildActionsProvider.class)
public class AttachOutputActionProvider extends BuildActionsProvider {
    @Override
    public List<BuildAction> getActions(String ioTabName, ProjectActionEvent[] events) {
        if (events != null && events.length > 0 && events[events.length-1].getType() == ProjectActionEvent.Type.RUN) {
            return Collections.<BuildAction>singletonList(new AttachAction(events));
        }
        return Collections.emptyList();
    }

    private static final class AttachAction extends AbstractAction implements BuildAction {
        ProjectActionEvent[] events;
        private int step = -1;
        private long pid = ExecutionListener.UNKNOWN_PID;

        public AttachAction(ProjectActionEvent[] events) {
            this.events = events;
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/debugger/resources/actions/Attach.gif", false)); // NOI18N
            putValue(Action.SHORT_DESCRIPTION, NbBundle.getBundle(AttachOutputActionProvider.class).getString("OUTPUT_ATTACH_ACTION_TEXT")); // NOI18N
            setEnabled(false);
        }

        public void executionStarted(int pid) {
            if (step == events.length-1 && pid != ExecutionListener.UNKNOWN_PID) {
                this.pid = pid;
                setEnabled(true);
            }
        }

        public void executionFinished(int rc) {
            setEnabled(false);
        }

        public void setStep(int step) {
            this.step = step;
        }

        public void actionPerformed(ActionEvent e) {
            if (!isEnabled()) {
                return;
            }
            //do the real attach
            if (pid == ExecutionListener.UNKNOWN_PID) {
                return;
            }
            
            ProjectActionEvent event = events[step];
            ProjectInformation info = ProjectUtils.getInformation(event.getProject());
            if (info == null) {
                return;
            }

            try {
                GdbDebugger.attach(pid, info, (event.getConfiguration()).getDevelopmentHost().getExecutionEnvironment());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
