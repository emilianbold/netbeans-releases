/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.tha.ui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.cnd.gizmo.support.GizmoServiceInfo;
import org.netbeans.modules.cnd.tha.THAServiceInfo;
import org.netbeans.modules.dlight.management.api.DLightSession;
import org.netbeans.modules.dlight.management.api.DLightSession.SessionState;
import org.netbeans.modules.dlight.management.ui.spi.IndicatorComponentDelegator;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import org.netbeans.modules.dlight.util.UIThread;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mt154047
 */
@ServiceProvider(service = IndicatorComponentDelegator.class, position = 100)
public final class THAIndicatorDelegator implements IndicatorComponentDelegator, THAIndicatorsTopComponentActionsProvider {

    public void activeSessionChanged(DLightSession oldSession, final DLightSession newSession) {
        if (oldSession == newSession) {
            return;
        }
        if (oldSession != null) {
            oldSession.removeSessionStateListener(this);
        }
        if (newSession != null && newSession.getState() != SessionState.CLOSED) {
            newSession.addSessionStateListener(this);
        }
        if (newSession != null && newSession.getState() == SessionState.CLOSED) {
            return;
        }
////        if (newSession.getState() != SessionState.CONFIGURATION)
//        UIThread.invoke(new Runnable() {
//            public void run() {
//                getComponent(newSession).setSession(newSession);
//            }
//        });
    }

    private String getProjectFolder(DLightSession session) {
        if (session == null) {
            return null;
        }
        ServiceInfoDataStorage serviceInfoStorage = session.getServiceInfoDataStorage();
        return serviceInfoStorage.getValue(GizmoServiceInfo.GIZMO_PROJECT_FOLDER);
    }

    private String getPlatform(DLightSession session) {
        if (session == null) {
            return null;
        }
        ServiceInfoDataStorage serviceInfoStorage = session.getServiceInfoDataStorage();
        return serviceInfoStorage.getValue(GizmoServiceInfo.PLATFORM);
    }

    private THAIndicatorsTopComponent getComponent(DLightSession newSession) {
        String projectFolder = getProjectFolder(newSession);
        //get all opened
        THAIndicatorsTopComponent topComponent = THAIndicatorsTopComponent.findInstance();
        topComponent.setActionsProvider(this);
        if (THAIndicatorTopComponentRegsitry.getRegistry().getOpened().isEmpty()) {
            return topComponent;
        }
        //if default is opened for unsupported platform and current platform is also unsupported do not open it again
        boolean isCurrentPlatformSupported = GizmoServiceInfo.isPlatformSupported(getPlatform(newSession));
        if (!isCurrentPlatformSupported && !GizmoServiceInfo.isPlatformSupported(getPlatform(topComponent.getSession()))) {
            return topComponent;
        }
        for (THAIndicatorsTopComponent tc : THAIndicatorTopComponentRegsitry.getRegistry().getOpened()) {
            DLightSession tcSession = tc.getSession();
            if (tcSession == null && newSession != null){
                tc.setActionsProvider(this);
                return tc;
            }
            if (!isCurrentPlatformSupported && !GizmoServiceInfo.isPlatformSupported(getPlatform(tcSession))) {
                //can return even if it is different project as both platfortms are not supported and the same message will be displayed
                return tc;
            }
            String sessionPF = getProjectFolder(tcSession);
            if (sessionPF != null && sessionPF.equals(projectFolder)) {
                ///and old session is finished
                if (tcSession != null && (tcSession.getState() == SessionState.ANALYZE || tcSession.getState() == SessionState.CLOSED)) {
                    //reuse
                    return tc;
                }
                if (tcSession == null) {
                    return tc;
                }
            }
        }
        THAIndicatorsTopComponent result = THAIndicatorsTopComponent.newInstance();
        result.setActionsProvider(this);
        return result;
    }

    public void sessionStateChanged(final DLightSession session, SessionState oldState, SessionState newState) {
        if (newState == SessionState.STARTING) {
            if (!needToHandle(session)){
                session.removeSessionStateListener(this);
                return;
            }
            UIThread.invoke(new Runnable() {

                public void run() {
                    THAIndicatorsTopComponent indicators = getComponent(session);
                    indicators.setSession(session);
                    indicators.open();
                    //invoke requestActive only once per IDE session
                    if (GizmoServiceInfo.isPlatformSupported(getPlatform(session)) || !THAIndicatorTopComponentRegsitry.getRegistry().getOpened().contains(indicators)){
                        indicators.requestActive();
                    }
                }
            });
        }
    }

    public void sessionAdded(DLightSession newSession) {
        //System.out.println("Session added");
    }

    public void sessionRemoved(DLightSession removedSession) {
    }

    public Action[] getActions(THAIndicatorsTopComponent source) {
        THAIndicatorTopComponentRegsitry registry = THAIndicatorTopComponentRegsitry.getRegistry();
        if (registry.getOpened() == null || registry.getOpened().size() == 0){
            return null;
        }

        if (registry.getOpened().size() == 1 && registry.getOpened().contains(source)){
            return null;
        }
        Action[] result = new Action[registry.getOpened().size() -1 ];
        int i = 0;
        for (final THAIndicatorsTopComponent tc : registry.getOpened()){
            if (tc == source){
                i++;
                continue;
            }
            result[i] = new AbstractAction(tc.getDisplayName()) {
                public void actionPerformed(ActionEvent e) {
                    tc.requestActive();
                }
            };
             i++;
        }
        return result;
    }

    private boolean needToHandle(DLightSession session){
        ServiceInfoDataStorage serviceInfoStorage = session.getServiceInfoDataStorage();
        return serviceInfoStorage.getValue(THAServiceInfo.THA_RUN) != null;
    }
}
