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
package org.netbeans.modules.dlight.visualizers;

import org.netbeans.modules.dlight.management.api.DLightManager;
import org.netbeans.modules.dlight.management.api.DLightSession;
import org.netbeans.modules.dlight.management.api.DLightSession.SessionState;
import org.netbeans.modules.dlight.management.api.DLightSessionListener;
import org.netbeans.modules.dlight.management.api.SessionStateListener;

/**
 *
 * @author mt154047
 */
final class VisualizersSupport implements SessionStateListener, DLightSessionListener {

    private SessionState currentSessionState = null;
    private final SessionStateListener sessionStateListener;
    
    VisualizersSupport(SessionStateListener sessionStateListener) {
        this.sessionStateListener = sessionStateListener;
        final DLightManager mgr = DLightManager.getDefault();
        final DLightSession activeSession = mgr.getActiveSession();
        mgr.addDLightSessionListener(this);
        activeSession.addSessionStateListener(this);
        currentSessionState = activeSession.getState();
    }

    protected boolean isSessionRunning() {
        return (currentSessionState == SessionState.RUNNING);
    }

    protected boolean isSessionPaused() {
        return (currentSessionState == SessionState.PAUSED);
    }

    protected boolean isSessionAnalyzed() {
        return (currentSessionState == SessionState.ANALYZE);
    }

    public void sessionStateChanged(DLightSession session, SessionState oldState, SessionState newState) {
        currentSessionState = newState;

        if (newState == SessionState.PAUSED || newState == SessionState.ANALYZE) {
            sessionStateListener.sessionStateChanged(session, oldState, newState);
            return;
        }

        if (newState == SessionState.STARTING || newState == SessionState.RUNNING) {
            sessionStateListener.sessionStateChanged(session, oldState, newState);
            return;
        }
    }

    public void activeSessionChanged(DLightSession oldSession, DLightSession newSession) {
        if (oldSession != null) {
            oldSession.removeSessionStateListener(this);
            sessionStateChanged(oldSession, oldSession.getState(), oldSession.getState());
        }

        if (newSession != null) {
            newSession.addSessionStateListener(this);
            sessionStateChanged(newSession, SessionState.CONFIGURATION, newSession.getState());
        }
    }

    public void sessionAdded(DLightSession newSession) {
    }

    public void sessionRemoved(DLightSession removedSession) {
        if (removedSession != null) {
            removedSession.removeSessionStateListener(this);
            sessionStateListener.sessionStateChanged(removedSession, removedSession.getState(), SessionState.CLOSED);
            //stopTimer();
        }
    }
}
