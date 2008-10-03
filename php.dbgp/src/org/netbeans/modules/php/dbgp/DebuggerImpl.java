/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.php.dbgp;

import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.project.api.PhpProjectUtils;
import org.netbeans.modules.php.project.spi.XDebugStarter;
import org.openide.filesystems.FileObject;
import org.openide.util.Cancellable;
import org.openide.util.RequestProcessor;

/**
 * @author Radek Matous
 *
 */
public class DebuggerImpl implements XDebugStarter {
    static String ID = "netbeans-PHP-DBGP-DebugInfo";// NOI18N
    static String SESSION_ID = "netbeans-PHP-DBGP-Session";// NOI18N
    static String ENGINE_ID = SESSION_ID + "/" + "PHP-Engine";// NOI18N

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.dbgp.api.Debugger#debug()
     */
    public void start(Project project, Callable<Cancellable> run, FileObject startFile, boolean closeSession) {
        assert startFile != null;
        SessionId sessionId = getSessionId(project);
        if (sessionId == null) {
            sessionId = new SessionId(startFile);
            DebuggerOptions options = new DebuggerOptions();
            options.debugForFirstPageOnly = closeSession;
            debug(sessionId, options, run);
            long started = System.currentTimeMillis();
            String serverFileUri = sessionId.waitServerFile(true);
            if (serverFileUri == null) {
                ConnectionErrMessage.showMe(((int) (System.currentTimeMillis() - started) / 1000));
                return;
            }
        }
    }

    public void stop() {
        Session phpSession = getPhpSession();
        if (phpSession != null) {
            SessionProgress forSession = SessionProgress.forSession(phpSession);
            if (forSession != null) {
                forSession.cancel();
            }
        }
    }

    public boolean isAlreadyRunning() {
        return getPhpSession() != null;
    }

    private Session getPhpSession() {
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
        for (Session session : sessions) {
            SessionId sessionId = session.lookupFirst(null, SessionId.class);
            if (sessionId != null) {
                Project sessionProject = sessionId.getProject();
                if (sessionProject != null && PhpProjectUtils.isPhpProject(sessionProject)) {
                    return session;
                }
            }
        }
        return null;
    }

    private SessionId getSessionId(Project project) {
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
        for (Session session : sessions) {
            SessionId sessionId = session.lookupFirst(null, SessionId.class);
            if (sessionId != null) {
                Project sessionProject = sessionId.getProject();
                if (project.equals(sessionProject)) {
                    return sessionId;
                }
            }
        }
        return null;
    }

    public Semaphore debug(SessionId id,DebuggerOptions options, Callable<Cancellable> run) {
        DebugSession session = new DebugSession(options);
        DebuggerInfo dInfo = DebuggerInfo.create(ID, new Object[]{id, session});
        DebuggerEngine[] engines = DebuggerManager.getDebuggerManager().startDebugging(dInfo);
        return StartActionProviderImpl.getInstance().start(session, run);
    }
}
