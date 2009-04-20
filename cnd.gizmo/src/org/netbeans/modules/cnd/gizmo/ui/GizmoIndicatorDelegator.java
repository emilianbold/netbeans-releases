/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.gizmo.ui;

import java.util.List;
import org.netbeans.modules.cnd.gizmo.GizmoServiceInfo;
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
@ServiceProvider(service = IndicatorComponentDelegator.class,  position = 10)
public final class GizmoIndicatorDelegator implements IndicatorComponentDelegator {

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
                getComponent(newSession).setSession(newSession);
            }
        });
    }

    private String getProjectFolder(DLightSession session) {
        if (session == null) {
            return null;
        }
        String projectFolder = null;
        List<DataStorage> storages = session.getStorages();
        if (storages != null) {
            for (DataStorage storage : storages) {
                if (storage.getValue(GizmoServiceInfo.GIZMO_PROJECT_FOLDER) != null) {
                    projectFolder = storage.getValue(GizmoServiceInfo.GIZMO_PROJECT_FOLDER);
                    break;
                }
            }
        }
        return projectFolder;
    }

    private GizmoIndicatorsTopComponent getComponent(DLightSession newSession) {
        String projectFolder = getProjectFolder(newSession);
        //get all opened
        if (GizmoIndicatorTopComponentRegsitry.getRegistry().getOpened().isEmpty()){
            return GizmoIndicatorsTopComponent.findInstance();
        }
        for (GizmoIndicatorsTopComponent tc : GizmoIndicatorTopComponentRegsitry.getRegistry().getOpened()) {
            //
            DLightSession tcSession = tc.getSession();
            String sessionPF = getProjectFolder(tcSession);
            if (sessionPF != null && sessionPF.equals(projectFolder)) {
                ///and old session is finished
                if (tcSession != null && (tcSession.getState() == SessionState.ANALYZE || tcSession.getState() == SessionState.CLOSED)) {
                    //reuse
                    return tc;
                }
                if (tcSession == null){
                    return tc;
                }
            }
        }
        return GizmoIndicatorsTopComponent.newInstance();
    }

    public void sessionStateChanged(final DLightSession session, SessionState oldState, SessionState newState) {
        if (newState == SessionState.STARTING) {
            UIThread.invoke(new Runnable() {

                public void run() {
                    GizmoIndicatorsTopComponent indicators = getComponent(session);
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
