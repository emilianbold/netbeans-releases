/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.core.ui.components;

import java.beans.PropertyChangeEvent;
import java.util.List;
import org.netbeans.modules.dlight.management.api.DLightSession;
import org.netbeans.modules.dlight.management.api.DLightSession.SessionState;
import org.netbeans.modules.dlight.management.ui.spi.IndicatorComponentDelegator;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.util.UIThread;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mt154047
 */
//@ServiceProvider(service = IndicatorComponentDelegator.class, position=10000)
public final class DLightIndicatorDelegator implements IndicatorComponentDelegator{

    public void activeSessionChanged(DLightSession oldSession, final DLightSession newSession) {
        if (oldSession == newSession) {
            return;
        }
        if (oldSession != null) {
            oldSession.removeSessionStateListener(this);
        }
        if (newSession != null) {
            newSession.addSessionStateListener(this);
        }
//        if (newSession.getState() != SessionState.CONFIGURATION)
        UIThread.invoke(new Runnable() {
            public void run() {
                DLightIndicatorsTopComponent.findInstance().setSession(newSession);
            }
        });
    }

    public void sessionStateChanged(final DLightSession session, SessionState oldState, SessionState newState) {
        if (newState == SessionState.STARTING) {
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
}
