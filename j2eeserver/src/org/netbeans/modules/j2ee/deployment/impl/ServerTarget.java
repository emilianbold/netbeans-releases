/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.j2ee.deployment.impl;

import org.openide.nodes.Node;
import javax.enterprise.deploy.spi.Target;
import javax.management.j2ee.Management;
import javax.management.ObjectName;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.j2ee.ListenerRegistration;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.deployment.impl.ui.ManagedObject;
import org.netbeans.modules.j2ee.deployment.impl.ui.DeployProgressUI;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;

// PENDING use environment providers, not Cookies
// PENDING issue  --   Target <==> J2EEDomain relationship 1 to many, many to 1, 1 to 1, or many to many
public class ServerTarget implements Node.Cookie {
    
    public static final String EVT_STARTING = "j2ee.state.starting";
    public static final String EVT_RUNNING = "j2ee.state.running";
    public static final String EVT_FAILED = "j2ee.state.failed";
    public static final String EVT_STOPPED = "j2ee.state.stopped";
    public static final String EVT_STOPPING = "j2ee.state.stopping";
    private static final Integer STATE_RUNNING = new Integer(1);

    ServerInstance instance;
    Target target;
    EventLog eventLog;
    //PENDING: caching state, sync, display through icon and action list.
    
    public ServerTarget(ServerInstance instance, Target target) {
        this.instance = instance;
        this.target = target;
    }
    
    public ServerInstance getInstance() {
        return instance;
    }
    
    /**
     * Return JSR-77 Management EJB that cover this target server.
     * @return Management EJB or null if provider plugin does not support it.
     */
    public Management getManagement() {
        return getInstance().getManagement();
    }
    
    public ObjectName getJ2eeServer() {
        ManagementMapper mapper = instance.getManagementMapper();
        if (mapper != null)
            return mapper.getTargetJ2eeServer(target);
        else
            return null;
    }
    
    public String getName() {
        return target.getName();
    }
    
    /**
     * Start event log listening on all notifications on each of managed object within the J2EEServer
     * represented by this target.
     */
    public void startEventLog() {
        Management mgmt = getManagement();
        ObjectName j2eeServer = getJ2eeServer();
        if (mgmt == null || j2eeServer == null)
            return;
        if (eventLog != null) {
            //eventLog.open();
            return;
        }
        try {
            String domain = j2eeServer.getDomain();
            String name = j2eeServer.getKeyProperty("name"); //NOI18N
            eventLog = new EventLog(domain + ":" + name); //NOI18N
            ObjectName query = new ObjectName(domain+":*,J2EEServer="+name); //NOI18N
            java.util.Set result = mgmt.queryNames(query, null);
            ObjectName[] objects = (ObjectName[]) result.toArray(new ObjectName[result.size()]);
            ListenerRegistration registry = getInstance().getListenerRegistry();
            if (registry == null) return;
            for (int i=0; i<objects.length; i++) {
                registry.addNotificationListener(objects[i], eventLog, null, null);
            }
        } catch (Exception e) {
            org.openide.ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
    }
    
    Boolean isEventProvider;
    public boolean isEventProvider() {
        if (eventLog != null)
            return true;
        
        if (isEventProvider  != null)
            return isEventProvider.booleanValue();
        
        isEventProvider = Boolean.FALSE;
        
        Management mgmt = getManagement();
        if (mgmt == null) {
            isEventProvider = Boolean.FALSE;
            return false;
        }
        
        ObjectName j2eeServer = getJ2eeServer();
        if (j2eeServer == null) {
            isEventProvider = Boolean.FALSE;
            return false;
        }
        
        try {
            Object value = mgmt.getAttribute(j2eeServer, "eventProvider"); //NOI18N
            if (value instanceof Boolean)
                isEventProvider = (Boolean) value;
        } catch (Exception e) {
            ErrorManager.getDefault().log(ErrorManager.EXCEPTION, e.toString());
        }
        return isEventProvider.booleanValue();
    }
    
    Boolean isStateManageable;
    public boolean isStateManageable() {
        if (eventLog != null)
            return true;
        
        if (isStateManageable  != null)
            return isStateManageable.booleanValue();
        
        //PENDING: default to FALSE after testing
        isStateManageable = Boolean.TRUE;
        
        Management mgmt = getManagement();
        if (mgmt == null) {
            isStateManageable = Boolean.FALSE;
            return false;
        }
        
        ObjectName j2eeServer = getJ2eeServer();
        if (j2eeServer == null) {
            isStateManageable = Boolean.FALSE;
            return false;
        }
        
        try {
            Object value = mgmt.getAttribute(j2eeServer, "stateManageable"); //NOI18N
            if (value instanceof Boolean)
                isStateManageable = (Boolean) value;
        } catch (Exception e) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, e.toString());
        }
        return isStateManageable.booleanValue();
    }
    
    public boolean hasWebContainerOnly() {
        Server server = instance.getServer();
        return (server.canDeployWars() && ! server.canDeployEars() && ! server.canDeployEjbJars());
    }
    
    public Target getTarget() {
        return target;
    }
    
    public boolean isAlsoServerInstance() {
        return instance.getStartServer().isAlsoTargetServer(target);
    }
    
    public boolean isRunning() {
        if (isAlsoServerInstance())
            return instance.isRunning();
        
        if (! isStateManageable()) 
            return false;
        
        Management mgmt = getManagement();
        if (mgmt == null) {
            isStateManageable = Boolean.FALSE;
            return false;
        }
        
        ObjectName j2eeServer = getJ2eeServer();
        if (j2eeServer == null) {
            isStateManageable = Boolean.FALSE;
            return false;
        }
        
        try {
            Object value = mgmt.getAttribute(j2eeServer, "state"); //NOI18N
            //System.out.println("J2EEServer state="+value);
            if (STATE_RUNNING.equals(value))
                return true;
        } catch (Exception e) {
            ErrorManager.getDefault().log(ErrorManager.EXCEPTION, e.toString());
        }
        return false;
    }
    
    public synchronized boolean start(DeployProgressUI ui) {
        final Management mgmt = getManagement();
        ObjectName j2eeServer = getJ2eeServer();
        final ServerProgress sp = new ServerProgress(j2eeServer);
        ui.setProgressObject(sp);

        if (mgmt == null) {
            sp.setStatusStartFailed(NbBundle.getMessage(ServerTarget.class, "MSG_NoManagementMapper", getName()));
            return false;
        }
        if (j2eeServer == null) {
            sp.setStatusStartFailed(NbBundle.getMessage(ServerTarget.class, "MSG_CouldNotMapTarget", getName()));
            return false;
        }
        if (! isStateManageable()) {
            sp.setStatusStartFailed(NbBundle.getMessage(ServerTarget.class, "MSG_NotStateManageable", getName()));
            return false;
        }

        NotificationListener nl = null;
        DeployProgressUI.CancelHandler ch = null;
        try {
            nl = registerStartJ2eeServerListener(sp);
            ch = getCancelHandler();
            ui.addCancelHandler(ch);
        
            Runnable r = new Runnable() {
                public void run() {
                    try {
                        mgmt.invoke(getJ2eeServer(), ManagedObject.OP_START, new Object[0], new String[0]);
                    } catch (Exception ex) {
                        sp.setStatusStartFailed(ex.toString());
                    }
                }
            };
            (new Thread(r)).start();


            // wait until done or cancelled
            sleep();
            
            if (ui.checkCancelled())
                return false;
            
            return true;
            
        } catch(Exception e) {
            sp.setStatusStartFailed(NbBundle.getMessage(ServerTarget.class, "MSG_ErrorInvokingStart", getName(), e));
            return false;
        } finally {
            if (ch != null) ui.removeCancelHandler(ch);
            if (nl != null) try { removeJ2eeServerListener(nl); } catch(Exception e) {}
        }
    }

    private DeployProgressUI.CancelHandler getCancelHandler() {
        DeployProgressUI.CancelHandler ch = new DeployProgressUI.CancelHandler() {
            public void handle() {
                ServerTarget.this.wakeUp();
            }
        };
        return ch;
    }
    
    private NotificationListener registerStartJ2eeServerListener(final ServerProgress sp) 
    throws java.rmi.RemoteException, javax.management.InstanceNotFoundException
    {
        Management mgnt = getManagement();
        if (mgnt == null)
            return null;
        ListenerRegistration registry = mgnt.getListenerRegistry();
        NotificationListener listener = new NotificationListener() {
            public void handleNotification(Notification notification, Object obj) {
                //Translating jsr77 event to jsr88 event
                String type = notification.getType();
                String msg = notification.getMessage();
                if (type.equals(EVT_STARTING))
                    sp.setStatusStartRunning(msg);
                else if (type.equals(EVT_RUNNING)) {
                    sp.setStatusStartCompleted(msg);
                    ServerTarget.this.wakeUp(); //also notify waiting completion
                } else if (type.equals(EVT_FAILED))
                    sp.setStatusStartFailed(msg);
            }
        };
        registry.addNotificationListener(getJ2eeServer(), listener, null, null);
        return listener;
    }

    private NotificationListener registerStopJ2eeServerListener(final ServerProgress sp) 
    throws java.rmi.RemoteException, javax.management.InstanceNotFoundException
    {
        Management mgnt = getManagement();
        ObjectName j2eeServer = getJ2eeServer();
        if (mgnt == null || j2eeServer == null)
            return null;
        
        ListenerRegistration registry = mgnt.getListenerRegistry();
        NotificationListener listener = new NotificationListener() {
            public void handleNotification(Notification notification, Object obj) {
                //Translating jsr77 event to jsr88 event
                String type = notification.getType();
                String msg = notification.getMessage();
                if (type.equals(EVT_STOPPING))
                    sp.setStatusStopRunning(msg);
                else if (type.equals(EVT_STOPPED)) {
                    sp.setStatusStopCompleted(msg);
                    ServerTarget.this.wakeUp(); //also notify waiting completion
                } else if (type.equals(EVT_FAILED))
                    sp.setStatusStopFailed(msg);
            }
        };
        registry.addNotificationListener(getJ2eeServer(), listener, null, null);
        return listener;
    }

    private void removeJ2eeServerListener(NotificationListener nl) 
    throws java.rmi.RemoteException, javax.management.InstanceNotFoundException, javax.management.ListenerNotFoundException
    {
        Management mgnt = getManagement();
        ObjectName j2eeServer = getJ2eeServer();
        if (mgnt == null || j2eeServer == null)
            return;
        ListenerRegistration registry = mgnt.getListenerRegistry();
        registry.removeNotificationListener(j2eeServer, nl);
    }        

    public synchronized boolean stop(DeployProgressUI ui) {
        final Management mgmt = getManagement();
        ObjectName j2eeServer = getJ2eeServer();
        if (mgmt == null || j2eeServer == null)
            return false;

        final ServerProgress sp = new ServerProgress(j2eeServer);
        ui.setProgressObject(sp);

        if (mgmt == null) {
            sp.setStatusStopFailed(NbBundle.getMessage(ServerTarget.class, "MSG_NoManagementMapper", getName()));
            return false;
        }
        if (j2eeServer == null) {
            sp.setStatusStopFailed(NbBundle.getMessage(ServerTarget.class, "MSG_CouldNotMapTarget", getName()));
            return false;
        }
        if (! isStateManageable()) {
            sp.setStatusStopFailed(NbBundle.getMessage(ServerTarget.class, "MSG_NotStateManageable", getName()));
            return false;
        }

        NotificationListener nl = null;
        DeployProgressUI.CancelHandler ch = null;
        try {
            nl = registerStopJ2eeServerListener(sp);
            ch = getCancelHandler();
            ui.addCancelHandler(ch);
        
            Runnable r = new Runnable() {
                public void run() {
                    try {
                        mgmt.invoke(getJ2eeServer(), ManagedObject.OP_STOP, new Object[0], new String[0]);
                    } catch (Exception ex) {
                        sp.setStatusStopFailed(ex.toString());
                    }
                }
            };
            (new Thread(r)).start();


            // wait until done or cancelled
            sleep();
            
            if (ui.checkCancelled())
                return false;
            
            return true;
            
        } catch(Exception e) {
            sp.setStatusStopFailed(NbBundle.getMessage(ServerTarget.class, "MSG_ErrorInvokingStop", getName(), e));
            return false;
        } finally {
            if (ch != null) ui.removeCancelHandler(ch);
            if (nl != null) try { removeJ2eeServerListener(nl); } catch(Exception e) {}
        }
    }
    private synchronized void wakeUp() {
        notify();
    }
    
    private synchronized void sleep() {
        try {        
            wait();
        } catch (Exception e) {}
    }

    public void showEventWindows() {
        StartServer ss = getInstance().getStartServer();
        if (ss == null) return;
        org.openide.windows.InputOutput[] ios = null;/*ss.getServerOutput(target);
        
        if (ios != null && ios.length > 0) {
            for (int i=0; i<ios.length; i++)
                ios[i].select();
        } else if (isEventProvider()) */{
            this.startEventLog();
        }
    }
}
