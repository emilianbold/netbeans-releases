/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tomcat5.nodes;

import java.io.IOException;
import java.util.Comparator;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.shared.CommandType;
import org.netbeans.modules.tomcat5.TomcatModule;
import org.netbeans.modules.tomcat5.nodes.actions.TomcatWebModuleCookie;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import java.io.*;
import org.netbeans.modules.tomcat5.config.*;
import org.netbeans.modules.tomcat5.*;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Date;
import org.openide.ErrorManager;
import org.netbeans.modules.tomcat5.util.LogViewer;
import org.netbeans.modules.tomcat5.util.UnsupportedLoggerException;

/**
 *
 * @author  Petr Pisl
 */
public class TomcatWebModule implements TomcatWebModuleCookie{
    
    /** path to context.xml */
    private static final String CONTEXT_XML_PATH = File.separator + "META-INF" + File.separator + "context.xml";  // NOI18N
 
    private final TomcatModule tomcatModule;
    private final DeploymentManager manager;    
    
    private boolean isRunning;
    
    private Node node;
    
    private final TargetModuleID[] target;
    
    private LogViewer logViewer;

    
    /** Creates a new instance of TomcatWebModule */
    public TomcatWebModule(DeploymentManager manager, TomcatModule tomcatModule, boolean isRunning) {
        this.tomcatModule = tomcatModule;
        this.manager = manager;
        this.isRunning = isRunning;
        target = new TargetModuleID[]{tomcatModule};
    }
    
    public TomcatModule getTomcatModule () {
        return tomcatModule;
    }
    
    public void setRepresentedNode(Node node){
        this.node = node;
    }
    
    public Node getRepresentedNode (){
        return node;
    }
    
    public DeploymentManager getDeploymentManager() {
        return manager;
    }
    
    public void undeploy() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run () {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(TomcatWebModule.class, "MSG_START_UNDEPLOY",  // NOI18N
                    new Object []{getTomcatModule ().getPath()})); 
                ProgressObject po = manager.undeploy(target);
                po.addProgressListener(new TomcatProgressListener());
            }
        }, 0);                
    }

    public void start() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run () {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(TomcatWebModule.class, "MSG_START_STARTING",  // NOI18N
                    new Object []{getTomcatModule ().getPath()}));
                ProgressObject po = manager.start(target);
                po.addProgressListener(new TomcatProgressListener());
            }
        }, 0);
    }

    public void stop() {        
        RequestProcessor.getDefault().post(new Runnable() {
            public void run () {                
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(TomcatWebModule.class, "MSG_START_STOPPING",  // NOI18N
                    new Object []{getTomcatModule ().getPath()}));
                ProgressObject po = manager.stop(target);
                po.addProgressListener(new TomcatProgressListener());
            }
        }, 0);
    }

    public boolean isRunning() {
        return isRunning;
    }   
    
    
    private String constructDisplayName(){
        if (isRunning())
            return getTomcatModule ().getPath();
        else
            return getTomcatModule ().getPath() + " [" + NbBundle.getMessage(TomcatWebModuleNode.class, "LBL_Stopped")  // NOI18N
               +  "]";
    }            
    
    /**
     * Returns context from META-INF/context.xml if exists, <code>null</code> otherwise
     * @return context from META-INF/context.xml if exists, <code>null</code> otherwise
     */
    private Context getContext() {
        Context ctx = null;
        try {                
            String docBase = tomcatModule.getDocRoot();
            File contextXml = new File(docBase + CONTEXT_XML_PATH);
            FileInputStream in = new FileInputStream(contextXml);
            ctx = Context.createGraph(in);
            in.close();                
            return ctx;                        
        } catch (FileNotFoundException fnfe) {
            return null;
        } catch (IOException ioe) {
            return null;
        }
    }
    
    /**
     * Returns context element from server.xml if defined, <code>null</code> otherwise
     * @return context element from server.xml if defined, <code>null</code> otherwise
     */
    private SContext getSContext() {
        Server server = ((TomcatManager)manager).getRoot();
        if (server == null) return null;
        String path = tomcatModule.getPath();
        if (path.equals("/")) path = ""; // NOI18N

        // Looks for the first appearance of the service and host element.
        // (ide currently does not support multiple service and host elements).
        Service[] service = server.getService();
        if (service.length > 0) {
            Engine engine = service[0].getEngine();
            if (engine != null) {
                Host[] host = engine.getHost();
                if (host.length > 0) {                    
                    SContext[] sContext = host[0].getSContext();
                    for (int i = 0; i < sContext.length; i++) {
                        if (sContext[i].getAttributeValue("path").equals(path)) { // NOI18N
                            return sContext[i];
                        }                        
                    }
                }
            }
        }
        return null;
    }
    
    private Object lock = new Object();
    
    /**
     * Opens the log file defined for this web moudel in the ouput window.
     */
    public void openLog() {        
        File catalinaDir = ((TomcatManager)manager).getCatalinaDir();
        String className = null;
        String dir = null;
        String prefix = null;
        String suffix = null;
        String timestamp = null;
        
        Context ctx = getContext();
        if (ctx != null && ctx.isLogger()) {
            className = ctx.getLoggerClassName();
            dir = ctx.getLoggerDirectory();
            prefix = ctx.getLoggerPrefix();
            suffix = ctx.getLoggerSuffix();            
            timestamp = ctx.getLoggerTimestamp();
        } else {
            SContext sCtx = getSContext();
            if (sCtx == null || !sCtx.isLogger()) return;
            className = sCtx.getAttributeValue(SContext.LOGGER, "className"); // NOI18N
            dir = sCtx.getAttributeValue(SContext.LOGGER, "directory"); // NOI18N
            prefix = sCtx.getAttributeValue(SContext.LOGGER, "prefix"); // NOI18N
            suffix = sCtx.getAttributeValue(SContext.LOGGER, "suffix"); // NOI18N
            timestamp = sCtx.getAttributeValue(SContext.LOGGER, "timestamp"); // NOI18N         
        }        
        boolean isTimestamped = Boolean.valueOf(timestamp).booleanValue();
        
        String msg = null; // error message
        // ensure only one thread will be opened
        synchronized(lock) {
            if (logViewer != null && logViewer.isOpen()) {
                logViewer.takeFocus();
                return;
            }
            try {
                logViewer = new LogViewer(catalinaDir, className, dir, prefix, 
                        suffix, isTimestamped, true);
                logViewer.start();
                return;
            } catch (UnsupportedLoggerException e) {
                msg = NbBundle.getMessage(TomcatWebModule.class, 
                        "MSG_UnsupportedLogger", e.getLoggerClassName());
            } catch (NullPointerException npe) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, npe);
            }
        }        
        if (msg != null) DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));        
    }
    
    /**
     * Stops the thread which listens to changes made in the log file and which
     * takes care for displying them. This method does not close the tab in
     * the ouput window.
     */
    private void closeLog() {
        synchronized(lock) {
            if (logViewer != null) {
                logViewer.close();
                logViewer = null;
            }
        }
    }
    
    /**
     * Returns <code>true</code> if there is a logger defined for this module, 
     * <code>false</code> otherwise.
     * @return <code>true</code> if there is a logger defined for this module, 
     * <code>false</code> otherwise.
     */
    public boolean hasLogger() {
        Context ctx = getContext();
        if (ctx != null && ctx.isLogger()) {
            return true;
        } else {
            SContext sCtx = getSContext();
            if  (sCtx != null && sCtx.isLogger()) {
                return true;
            }            
        }
        return false;
    }
    
    private class TomcatProgressListener implements ProgressListener {
        public void handleProgressEvent(ProgressEvent progressEvent) {
            DeploymentStatus deployStatus = progressEvent.getDeploymentStatus();
            if (deployStatus.getState() == StateType.COMPLETED) {
                CommandType command = deployStatus.getCommand();
                if (command == CommandType.START || command == CommandType.STOP) {
                        StatusDisplayer.getDefault().setStatusText(deployStatus.getMessage());
                        if (command == CommandType.START) isRunning = true; else isRunning = false;
                        node.setDisplayName(constructDisplayName());                        
                } else if (command == CommandType.UNDEPLOY) {
                        Children children = node.getParentNode().getChildren();
                        if (children instanceof TomcatWebModuleChildren){
                            ((TomcatWebModuleChildren)children).updateKeys();
                            StatusDisplayer.getDefault().setStatusText(deployStatus.getMessage());
                        }                    
                }
            } else if (deployStatus.getState() == StateType.FAILED) {
                NotifyDescriptor notDesc = new NotifyDescriptor.Message(
                        deployStatus.getMessage(), 
                        NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(notDesc);
                StatusDisplayer.getDefault().setStatusText(deployStatus.getMessage());                
            }
        }
    }
    
    public static class TomcatWebModuleComparator implements Comparator {
        
        public int compare(Object o1, Object o2) {
            TomcatWebModule wm1 = (TomcatWebModule) o1;
            TomcatWebModule wm2 = (TomcatWebModule) o2;
            
            return wm1.getTomcatModule ().getModuleID().compareTo(wm2.getTomcatModule ().getModuleID());
        }
        
    }
}
