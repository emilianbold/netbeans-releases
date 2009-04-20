/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.management.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.dlight.management.api.DLightManager;
import org.netbeans.modules.dlight.management.api.DLightSession;
import org.netbeans.modules.dlight.management.api.DLightSession.SessionState;
import org.netbeans.modules.dlight.management.api.DLightSessionListener;
import org.netbeans.modules.dlight.management.api.SessionStateListener;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mt154047
 */
public class StopDLightAction extends AbstractAction
    implements DLightSessionListener, SessionStateListener {

    public StopDLightAction() {
        super("Stop D-Light"); //NOI18N
        putValue("iconBase", "org/netbeans/modules/dlight/management/resources/stop24.png"); // NOI18N
        putValue(Action.SMALL_ICON, "org/netbeans/modules/dlight/management/resources/stop.png"); // NOI18N
        DLightManager.getDefault().addDLightSessionListener(this);
    }

    @Override
    public boolean isEnabled() {
        return (DLightManager.getDefault().getActiveSession() != null && ((DLightManager.getDefault().getActiveSession().getState() == DLightSession.SessionState.RUNNING) ||
            (DLightManager.getDefault().getActiveSession().getState() == DLightSession.SessionState.STARTING)));
    }

    public void actionPerformed(ActionEvent e) {
        RequestProcessor.getDefault().post(new Runnable() {

            public void run() {
                DLightLogger.instance.info("StopDLightAction performed @ " + System.currentTimeMillis());
                DLightManager.getDefault().stopActiveSession();
            }
        });

    }

    public void activeSessionChanged(DLightSession oldSession, DLightSession newSession) {
        if (oldSession != null) {
            oldSession.removeSessionStateListener(this);
        }

        if (newSession != null) {
            newSession.addSessionStateListener(this);
        }

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                setEnabled(isEnabled());
            }
        });
    }

    public void sessionStateChanged(DLightSession session, SessionState oldState, SessionState newState) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                setEnabled(isEnabled());
            }
        });
    }

    public void sessionAdded(DLightSession newSession) {
        if (newSession !=null){
            newSession.addSessionStateListener(this);
        }
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                setEnabled(isEnabled());
            }
        });
    }

    public void sessionRemoved(DLightSession removedSession) {
        if (removedSession != null) {
            removedSession.removeSessionStateListener(this);
        }
    //throw new UnsupportedOperationException("Not supported yet.");
    }
}
