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

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import org.netbeans.modules.tomcat5.*;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;


/**
 * <code>LogManager</code> manages all context and shared context logs for one
 * Tomcat server instace (one <code>TomcatManager</code>).
 *
 * @author  Stepan Herold
 */
public class LogManager {
    private ServerLog serverLog;    
    private LogViewer sharedContextLogViewer;
    private Map/*<TomcatModule, TomcatModuleConfig>*/ tomcatModuleConfigs = Collections.synchronizedMap(new WeakHashMap());
    private Map/*<String, LogViewer>*/ contextLogViewers = Collections.synchronizedMap(new HashMap());
    private TomcatManager manager;
    
    private Object serverLogLock = new Object();
    private Object sharedContextLogLock = new Object();
    private Object contextLogLock = new Object();
    
    /** Creates a new instance of LogManager */
    public LogManager(TomcatManager tm) {
        manager = tm;
    }
    
    // ------- server log (output) ---------------------------------------------
    
    /**
     * Open the server log (output).
     */
    public void openServerLog() {
        final Process process = manager.getTomcatProcess();
        assert process != null;
        synchronized(serverLogLock) {
            if (serverLog != null) {
                serverLog.takeFocus();
                return;
            }
            serverLog = new ServerLog(
                manager.getUri(),
                manager.getTomcatProperties().getDisplayName(),
                new InputStreamReader(process.getInputStream()),
                new InputStreamReader(process.getErrorStream()),
                true,
                false);
            serverLog.start();
        }
        //PENDING: currently we copy only Tomcat std & err output. We should
        //         also support copying to Tomcat std input.
        new Thread() {
            public void run() {
                try {
                    int ret = process.waitFor();
                    Thread.sleep(2000);  // time for server log
                } catch (InterruptedException e) {
                } finally {
                    serverLog.interrupt();
                }
            }
        }.start();
    }
    
    /**
     * Stop the server log thread, if started.
     */
    public void closeServerLog() {
        synchronized(serverLogLock) {
            if (serverLog != null) {
                serverLog.interrupt();
                serverLog = null;
            }
        }
    }

    /**
     * Can be the server log (output) displayed?
     *
     * @return <code>true</code> if the server log can be displayed, <code>false</code>
     *         otherwise.
     */
    public boolean hasServerLog() {
        return manager.getTomcatProcess() != null;
    }
    
    // ------- end of server log (output) --------------------------------------
    
    // ------- shared context log ----------------------------------------------
    
    /**
     * Opens shared context log. Shared context log can be defined in the host or
     * engine element. Definition in the host element overrides definition in the 
     * engine element.
     */
    public void openSharedContextLog() {
        TomcatManagerConfig tomcatManagerConfig = manager.getTomcatManagerConfig();
        tomcatManagerConfig.refresh();
        if (!tomcatManagerConfig.hasLogger()) return;
        LogViewer newSharedContextLog = null;
        try {
            TomcatProperties tp = manager.getTomcatProperties();
            newSharedContextLog = new LogViewer(
                tp.getCatalinaDir(),
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
        TomcatManagerConfig tomcatManagerConfig = manager.getTomcatManagerConfig();
        tomcatManagerConfig.refresh();
        return tomcatManagerConfig.hasLogger();
    }
    
    // ------- end of shared context log ---------------------------------------
    
    // ------- context log -----------------------------------------------------
    
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
                    manager.getTomcatManagerConfig().serverXmlPath());
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
                manager.getTomcatProperties().getCatalinaDir(),
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
                    manager.getTomcatManagerConfig().serverXmlPath());
            tomcatModuleConfigs.put(module, moduleConfig);
        } else {
            moduleConfig = (TomcatModuleConfig)o;
            moduleConfig.refresh();
        }
        return moduleConfig.hasLogger();
    }
    
    // ------- end of context log ----------------------------------------------
}
