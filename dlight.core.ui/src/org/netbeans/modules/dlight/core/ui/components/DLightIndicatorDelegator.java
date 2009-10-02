/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.core.ui.components;

import org.netbeans.modules.dlight.management.api.DLightSession;
import org.netbeans.modules.dlight.management.api.DLightSession.SessionState;
import org.netbeans.modules.dlight.management.ui.spi.IndicatorComponentDelegator;
import org.netbeans.modules.dlight.spi.impl.DLightServiceInfo;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import org.netbeans.modules.dlight.util.UIThread;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mt154047
 */
@ServiceProvider(service = IndicatorComponentDelegator.class, position = 10000)
public final class DLightIndicatorDelegator implements IndicatorComponentDelegator {

    public void activeSessionChanged(DLightSession oldSession, final DLightSession newSession) {
        if (oldSession == newSession) {
            return;
        }
        if (oldSession != null) {
            oldSession.removeSessionStateListener(this);
        }
         if (newSession == null) {
             return;
         }
        if (newSession != null) {
            newSession.addSessionStateListener(this);
        }     
    }

    public void sessionStateChanged(final DLightSession session, SessionState oldState, SessionState newState) {
        if (newState == SessionState.STARTING) {
            if (!needToHandle(session)) {
                session.removeSessionStateListener(this);
                return;
            }
            UIThread.invoke(new Runnable() {

                public void run() {
                    DLightIndicatorsTopComponent indicators = DLightIndicatorsTopComponent.findInstance();
                    indicators.setSession(session);
                    indicators.open();
                    indicators.requestActive();
                }
            });
        }
    }

    public void sessionAdded(DLightSession newSession) {
        //System.out.println("Session added");
    }

    public void sessionRemoved(DLightSession removedSession) {
    }

    private boolean needToHandle(DLightSession session) {
        if (session == null){
            return false;
        }
        ServiceInfoDataStorage serviceInfoStorage = session.getServiceInfoDataStorage();
        return serviceInfoStorage != null && serviceInfoStorage.getValue(DLightServiceInfo.DLIGHT_RUN) != null;
    }
}
