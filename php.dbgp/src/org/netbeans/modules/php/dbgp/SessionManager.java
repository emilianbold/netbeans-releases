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
package org.netbeans.modules.php.dbgp;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.openide.util.Cancellable;

/**
 * @author Radek Matous
 */
public class SessionManager {
    static String ID = "netbeans-PHP-DBGP-DebugInfo";// NOI18N
    private Set<DebugSession> debugSessions;
    private ServerThread serverThread;
    private static final SessionManager INSTANCE = new SessionManager();

    public static SessionManager getInstance(){
        return INSTANCE;
    }
    
    SessionManager() {
        debugSessions = new CopyOnWriteArraySet<DebugSession>();
        serverThread = new ServerThread(this);
    }

    public synchronized  void startSession(SessionId id,DebuggerOptions options, Callable<Cancellable> backendLauncher) {
        DebugSession dbgSession = new DebugSession(options, new BackendLauncher(backendLauncher));
        DebuggerInfo dInfo = DebuggerInfo.create(ID, new Object[]{id, dbgSession});
        DebuggerInfo.create(ID, new Object[]{id, dbgSession});
        DebuggerEngine[] engines = DebuggerManager.getDebuggerManager().startDebugging(dInfo);
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
        for (Session session : sessions) {
            DebugSession debugSession = session.lookupFirst(null, DebugSession.class);
            if (debugSession != null && debugSession == dbgSession) {
                dbgSession.setSession(session);
            }
        }
        serverThread.invokeLater();
    }


    synchronized DebugSession add(DebugSession session) {
        debugSessions.add(session);
        return session;
    }

    synchronized DebugSession remove(DebugSession session) {
        debugSessions.remove(session);
        return session;
    }

    public synchronized List<DebugSession> findSessionsById(SessionId id){
        List<DebugSession> result = new LinkedList<DebugSession>();
        for( DebugSession session : debugSessions) {
            SessionId sessId = session.getSessionId();
            if (id.equals(sessId)){
                result.add( session );
            }
        }
        return result;
    }

    public synchronized DebugSession getCurrentSession( SessionId id ){
        if ( id == null ) {
            return null;
        }
        return ConversionUtils.toDebugSession(id);
    }

    public synchronized void stop(Session session) {
        SessionId id = session.lookupFirst(null, SessionId.class);
        List<DebugSession> list = findSessionsById(id);
        for (DebugSession debSess : list) {
            debSess.cancel();
            remove(debSess);
        }
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
        boolean last = true;
        for (Session sess : sessions) {
            if ( sess.equals(session )) {
                continue;
            }
            if ( sess.lookupFirst( null , SessionId.class )!= null ) {
                last = false;
            }
        }
        if ( last ) {
            serverThread.cancel();
        }

        stopEngines( session );
    }

    private void stopEngines( Session session ) {
        String[] languages = session.getSupportedLanguages();
        for (String language : languages) {
            DebuggerEngine engine = session.getEngineForLanguage(language);
            ((DbgpEngineProvider)engine.lookupFirst(null,
                    DebuggerEngineProvider.class)).getDestructor().killEngine();
        }
    }
}
