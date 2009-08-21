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

package org.netbeans.modules.kenai.ui.project;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.ui.ProjectAccessorImpl;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author tester
 */
public class DetailsAction {

    static RequestProcessor.Task t = null;

    public static synchronized AbstractAction forProject(final String proj) {

        return new AbstractAction(NbBundle.getMessage(ProjectAccessorImpl.class, "CTL_EditProject")) { //NOI18N

            public void actionPerformed(ActionEvent e) {
                if (t != null && !t.isFinished()) {
                    t.cancel();
                }
                final ProgressHandle[] handle = new ProgressHandle[1];
                handle[0] = ProgressHandleFactory.createHandle(NbBundle.getMessage(ProjectAccessorImpl.class, "CTL_OpenKenaiProjectAction")); //NOI18N
                handle[0].start();
                t = RequestProcessor.getDefault().post(new Runnable() {

                    public void run() {

                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                try {
                                    KenaiProject kenaiProj = Kenai.getDefault().getProject(proj);
                                    kenaiProjectTopComponent tc = kenaiProjectTopComponent.getInstance(kenaiProj);
                                    tc.open();
                                    tc.requestActive();
                                } catch (KenaiException ex) {
                                    Exceptions.printStackTrace(ex);
                                } finally {
                                    handle[0].finish();
                                }
                            }
                        });
                    }
                });
            }
        };
    }

}
