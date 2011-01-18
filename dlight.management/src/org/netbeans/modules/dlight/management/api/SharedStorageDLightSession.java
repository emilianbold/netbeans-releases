/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.management.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.netbeans.modules.dlight.management.api.DLightSession.SessionState;
import org.netbeans.modules.dlight.management.api.impl.DataStorageManager;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;

/**
 *
 * @author mt154047
 */
public final class SharedStorageDLightSession implements SessionStateListener {

    private final String storageUniqueKey;
    private final List<DLightSession> sessions = new ArrayList<DLightSession>();
    private CountDownLatch latch;
    private List<SharedSessionStateListener> sessionStateListeners = null;
    private SessionState state;

    SharedStorageDLightSession(String storageUniqueKey) {
        this.storageUniqueKey = storageUniqueKey;
    }

    public ServiceInfoDataStorage getServiceInfoStorage() {
        return DataStorageManager.getInstance().getServiceInfoDataStorageFor(storageUniqueKey);
    }
    //it has state
    //list of DLigthSessions
    //storageUniqueID

    public void addSessionStateListener(SharedSessionStateListener listener) {
        if (sessionStateListeners == null) {
            sessionStateListeners = new ArrayList<SharedSessionStateListener>();
        }

        if (!sessionStateListeners.contains(listener)) {
            sessionStateListeners.add(listener);
        }
    }

    public void removeSessionStateListener(SharedSessionStateListener listener) {
        if (sessionStateListeners == null) {
            return;
        }

        sessionStateListeners.remove(listener);
    }

    void setState(SessionState state) {
        SessionState oldState = this.state;
        this.state = state;

        if (sessionStateListeners != null) {
            for (SharedSessionStateListener l : sessionStateListeners.toArray(new SharedSessionStateListener[0])) {
                l.sessionStateChanged(this, oldState, state);
            }
        }
    }

    public SessionState getState() {
        return state;
    }

    public void addDLightSession(DLightSession session) {
        session.addSessionStateListener(this);
        sessions.add(session);
    }

    public void run() {
        latch = new CountDownLatch(sessions.size());
        setState(SessionState.RUNNING);
        for (DLightSession session : sessions) {
            session.start();
        }
    }

    public String getSorageUniqueKey() {
        return storageUniqueKey;
    }

    public void closeSession() {
        setState(SessionState.CLOSED);
        for (DLightSession session : sessions) {
            session.close();
        }

    }

    @Override
    public void sessionStateChanged(DLightSession session, SessionState oldState, SessionState newState) {
        switch (newState) {
            case ANALYZE:
            case CLOSED:
                latch.countDown();
                if (latch.getCount() == 0) {
                    //notify that it is 
                    setState(SessionState.ANALYZE);
                }
        }
    }
}
