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

package org.netbeans.modules.tomcat5;

import java.io.*;
import org.netbeans.modules.tomcat5.config.*;
import org.openide.ErrorManager;


/**
 * <code>TomcatModuleConfig</code> offers easy access to some context.xml and 
 * server.xml settings.
 *
 * @author Stepan Herold
 */
public class TomcatModuleConfig {
    private static final String CONTEXT_XML_PATH = "/META-INF/context.xml"; // NOI18N
    
    private File contextXml;
    private File serverXml;
    
    private long timestampContextXML;
    private long timestampServerXML;
    
    private String docBase;
    private String path;
    
    
    // context logger settings
    private boolean hasLogger;
    private String loggerClassName;
    private String loggerDir;
    private String loggerPrefix;
    private String loggerSuffix;
    private boolean loggerTimestamp;
    
    /** 
     * Creates a new instance of TomcatModuleConfig.
     *
     * @param docBase document base class.
     * @param path context path.
     * @param serverXmlPath path to server.xml file.
     */
    public TomcatModuleConfig(String docBase, String path, String serverXmlPath) {
        this.docBase = docBase;
        if (path.equals("/")) {
            this.path = ""; // NOI18N
        } else {
            this.path = path;
        }
        contextXml = new File(docBase + CONTEXT_XML_PATH);
        serverXml = new File(serverXmlPath);
        refresh();
    }
    
    /**
     * Returns context from META-INF/context.xml if exists, <code>null</code> otherwise
     * @return context from META-INF/context.xml if exists, <code>null</code> otherwise
     */
    private Context getContext() {
        try {
            timestampContextXML = contextXml.lastModified();
            Context ctx = Context.createGraph(contextXml);
            return ctx;
        } catch (IOException ioe) {
            return null;
        }
    }
    
    /**
     * Returns context element from server.xml if defined, <code>null</code> otherwise
     * @return context element from server.xml if defined, <code>null</code> otherwise
     */
    private SContext getSContext() {        
        try {
            timestampServerXML = serverXml.lastModified();
            Server server = Server.createGraph(serverXml);
            
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
            
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
        }
        return null;
    }
    
    /**
     * Returns <code>true</code> if there is a logger defined for this module, 
     * <code>false</code> otherwise.
     *
     * @return <code>true</code> if there is a logger defined for this module, 
     *         <code>false</code> otherwise.
     */
    public boolean hasLogger() {
        return hasLogger;
    }
    
    /**
     * Return logger class name.
     *
     * @return logger class name.
     */
    public String loggerClassName() {
        return loggerClassName;
    }
    
    /**
     * Return logger directory.
     *
     * @return logger directory.
     */
    public String loggerDir() {
        return loggerDir;
    }
    
    /**
     * Return logger prefix.
     *
     * @return logger prefix.
     */
    public String loggerPrefix() {
        return loggerPrefix;
    }
    
    /**
     * Return logger suffix.
     *
     * @return logger suffix.
     */
    public String loggerSuffix() {
        return loggerSuffix;
    }

    /**
     * Return <code>true</code> whether logger timestamps messages, <code>false</code>
     * otherwise.
     *
     * @return <code>true</code> whether logger timestamps messages, <code>false</code>
     *         otherwise.
     */
    public boolean loggerTimestamp() {
        return loggerTimestamp;
    }
    
    /**
     * Refresh cached values if the context.xml or server.xml file changed.
     */
    public void refresh() {
        if (contextXml.exists()) {
            long newTimestamp = contextXml.lastModified();
            if (newTimestamp > timestampContextXML) {
                timestampContextXML = newTimestamp;
                Context ctx = getContext();
                if (ctx != null) {
                    hasLogger = ctx.isLogger();
                    if (hasLogger) {
                        loggerClassName = ctx.getLoggerClassName();
                        loggerDir = ctx.getLoggerDirectory();
                        loggerPrefix = ctx.getLoggerPrefix();
                        loggerSuffix = ctx.getLoggerSuffix();
                        loggerTimestamp = Boolean.valueOf(ctx.getLoggerTimestamp()).booleanValue();
                        return;
                    }
                }
            }
        } else if (serverXml.exists()) {
            long newTimestamp = serverXml.lastModified();
            if (newTimestamp > timestampServerXML) {
                timestampServerXML = newTimestamp;
                SContext sCtx = getSContext();
                if (sCtx != null) {
                    hasLogger = sCtx.isLogger();
                    if (hasLogger) {
                        loggerClassName = sCtx.getAttributeValue(SContext.LOGGER, "className"); // NOI18N
                        loggerDir = sCtx.getAttributeValue(SContext.LOGGER, "directory"); // NOI18N
                        loggerPrefix = sCtx.getAttributeValue(SContext.LOGGER, "prefix"); // NOI18N
                        loggerSuffix = sCtx.getAttributeValue(SContext.LOGGER, "suffix"); // NOI18N
                        String timestamp = sCtx.getAttributeValue(SContext.LOGGER, "timestamp"); // NOI18N
                        loggerTimestamp = Boolean.valueOf(timestamp).booleanValue();
                    }
                }
            }
        } else {
            hasLogger = false; // this shouldn't happen
        }
    }
}
