/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tomcat5.util;

import java.util.*;

import org.netbeans.modules.tomcat5.*;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;


/**
 * <code>LogManager</code> manages all context and shared context logs for one
 * Tomcat server instace (one <code>TomcatManager</code>).
 *
 * @author  Stepan Herold
 */
public class LogManager {
    private LogViewer sharedContextLogViewer;
    private Map/*<TomcatModule, TomcatModuleConfig>*/ tomcatModuleConfigs = Collections.synchronizedMap(new WeakHashMap());
    private Map/*<String, LogViewer>*/ contextLogViewers = Collections.synchronizedMap(new HashMap());
    private TomcatManager manager;
    
    /** Creates a new instance of LogManager */
    public LogManager(TomcatManager tm) {
        manager = tm;
    }
    
    private Object sharedContextLogLock = new Object();
    
    /**
     * Opens shared context log. Shared context log can be defined in the host or
     * engine element. Definition in the host element overrides definition in the 
     * engine element.
     */
    public void openSharedContextLog() {
        TomcatManagerConfig tomcatManagerConfig = manager.tomcatManagerConfig();
        tomcatManagerConfig.refresh();
        if (!tomcatManagerConfig.hasLogger()) return;
        LogViewer newSharedContextLog = null;
        try {
            newSharedContextLog = new LogViewer(
                manager.getCatalinaDir(),
                manager.getCatalinaWork(),
                null,
                tomcatManagerConfig.loggerClassName(),
                tomcatManagerConfig.loggerDir(),
                tomcatManagerConfig.loggerPrefix(),
                tomcatManagerConfig.loggerSuffix(),
                tomcatManagerConfig.loggerTimestamp(),
                false);
        } catch (UnsupportedLoggerException e) {
            NotifyDescriptor notDesc = new NotifyDescriptor.Message(
                NbBundle.getMessage(LogManager.class, "MSG_UnsupportedLogger", 
                        e.getLoggerClassName()));
            DialogDisplayer.getDefault().notify(notDesc);
            return;
        } catch (NullPointerException npe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, npe);
        }
        
        // ensure only one thread will be opened
        synchronized(sharedContextLogLock) {
            if (sharedContextLogViewer != null && sharedContextLogViewer.isOpen() 
                && !sharedContextLogViewer.equals(newSharedContextLog)) {
                sharedContextLogViewer.removeAllLogViewerStopListener();
                sharedContextLogViewer.close();
                sharedContextLogViewer = newSharedContextLog;
		sharedContextLogViewer.addLogViewerStopListener(new LogViewer.LogViewerStopListener() {
                   public void callOnStop() {
                       synchronized(sharedContextLogLock) {
                           sharedContextLogViewer = null;
                       }
                   }
                });
                sharedContextLogViewer.start();
            } else if (sharedContextLogViewer == null || !sharedContextLogViewer.isOpen()) {
                if (sharedContextLogViewer != null) {
                    sharedContextLogViewer.removeAllLogViewerStopListener();
                }
                sharedContextLogViewer = newSharedContextLog;
		sharedContextLogViewer.addLogViewerStopListener(new LogViewer.LogViewerStopListener() {
                   public void callOnStop() {
                       synchronized(sharedContextLogLock) {
                           sharedContextLogViewer = null;
                       }
                   }
                });
                sharedContextLogViewer.start();
            }
            sharedContextLogViewer.takeFocus();
        }
    }


    /**
     * Is shared context log defined for this server?
     *
     * @return <code>true</code> shared context log is defined, <code>false</code>
     *         otherwise.
     */
    public boolean hasSharedLogger() {
        TomcatManagerConfig tomcatManagerConfig = manager.tomcatManagerConfig();
        tomcatManagerConfig.refresh();
        return tomcatManagerConfig.hasLogger();
    }
    
    private Object contextLogLock = new Object();
    
    /**
     * Open a context log for the specified module.
     *
     * @param module its context log should be opened.
     */
    public void openContextLog(TomcatModule module) {
        final String moduleID = module.getModuleID();
        Object o = tomcatModuleConfigs.get(module);
        TomcatModuleConfig moduleConfig = null;
        LogViewer contextLog = null;
        if (o == null) {
            moduleConfig = new TomcatModuleConfig(
                    module.getDocRoot(),
                    module.getPath(),
                    manager.tomcatManagerConfig().serverXmlPath());
            tomcatModuleConfigs.put(module, moduleConfig);
        } else {
            moduleConfig = (TomcatModuleConfig)o;
            moduleConfig.refresh();
        }
        if (!moduleConfig.hasLogger()) return;
        contextLog = (LogViewer)contextLogViewers.get(moduleID);
        LogViewer newContextLog = null;
        try {
            newContextLog = new LogViewer(
                manager.getCatalinaDir(),
                manager.getCatalinaWork(),
                module.getPath(),
                moduleConfig.loggerClassName(),
                moduleConfig.loggerDir(),
                moduleConfig.loggerPrefix(),
                moduleConfig.loggerSuffix(),
                moduleConfig.loggerTimestamp(),
                false);
        } catch (UnsupportedLoggerException e) {
            NotifyDescriptor notDesc = new NotifyDescriptor.Message(
                NbBundle.getMessage(LogManager.class, "MSG_UnsupportedLogger", 
                        e.getLoggerClassName()));
            DialogDisplayer.getDefault().notify(notDesc);
            return;
        } catch (NullPointerException npe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, npe);
        }
        
        // ensure only one thread will be opened
        synchronized(contextLogLock) {
            if (contextLog != null && contextLog.isOpen() 
                && !contextLog.equals(newContextLog)) {
                contextLog.removeAllLogViewerStopListener();
                contextLog.close();
                contextLog = newContextLog;
                contextLog.addLogViewerStopListener(new LogViewer.LogViewerStopListener() {
                   public void callOnStop() {
                       contextLogViewers.remove(moduleID);
                   }
                });
                contextLogViewers.put(moduleID, contextLog);
                contextLog.start();
            } else if (contextLog == null || !contextLog.isOpen()) {
                if (contextLog != null) {
                    contextLog.removeAllLogViewerStopListener();
                }
                contextLog = newContextLog;
                contextLog.addLogViewerStopListener(new LogViewer.LogViewerStopListener() {
                   public void callOnStop() {
                       contextLogViewers.remove(moduleID);
                   }
                });                
                contextLogViewers.put(moduleID, contextLog);
                contextLog.start();
            }
        }
        contextLog.takeFocus();
    }

    /**
     * Is context log defined for the specified module.
     *
     * @param module which should be examined.
     * @return <code>true</code> if specified module has a context log defined, 
     *         <code>false</code> otherwise.
     */
    public boolean hasContextLogger(TomcatModule module) {
        Object o = tomcatModuleConfigs.get(module);
        TomcatModuleConfig moduleConfig = null;
        if (o == null) {
            moduleConfig = new TomcatModuleConfig(
                    module.getDocRoot(),
                    module.getPath(),
                    manager.tomcatManagerConfig().serverXmlPath());
            tomcatModuleConfigs.put(module, moduleConfig);
        } else {
            moduleConfig = (TomcatModuleConfig)o;
            moduleConfig.refresh();
        }
        return moduleConfig.hasLogger();
    }
}
