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

import org.openide.ErrorManager;
import java.io.*;
import org.netbeans.modules.tomcat5.config.*;

/**
 * <code>TomcatManagerConfig</code> offers easy access to some server.xml settings.
 *
 * @author Stepan Herold
 */
public class TomcatManagerConfig {
    private File serverXml;
    private long timestamp;
    
    // shared context logger settings
    private boolean hasLogger;    
    private String loggerClassName;
    private String loggerDir;
    private String loggerPrefix;
    private String loggerSuffix;
    private boolean loggerTimestamp;
    
    /** 
     * Creates a new instance of TomcatManagerConfig.
     * 
     * @param serverXmlPath path to server.xml file.
     */
    public TomcatManagerConfig(String serverXmlPath) {
        serverXml = new File(serverXmlPath);
        refresh();
    }
    
    /**
     * Refresh cached values if the server.xml file changed.
     */
    public void refresh() {
        long newTimestamp = serverXml.lastModified();
        if (newTimestamp > timestamp) {
            timestamp = newTimestamp;            
            Host host = getHostElement();
            if (host != null && host.isLogger()) {
                hasLogger = true;
                loggerClassName = host.getAttributeValue(SContext.LOGGER, "className"); // NOI18N
                loggerDir = host.getAttributeValue(SContext.LOGGER, "directory"); // NOI18N
                loggerPrefix = host.getAttributeValue(SContext.LOGGER, "prefix"); // NOI18N
                loggerSuffix = host.getAttributeValue(SContext.LOGGER, "suffix"); // NOI18N
                String timestamp = host.getAttributeValue(SContext.LOGGER, "timestamp"); // NOI18N
                loggerTimestamp = Boolean.valueOf(timestamp).booleanValue();
            } else {
                Engine engine = getEngineElement();
                if  (engine != null && engine.isLogger()) {
                    hasLogger = true;
                    loggerClassName = engine.getAttributeValue(SContext.LOGGER, "className"); // NOI18N
                    loggerDir = engine.getAttributeValue(SContext.LOGGER, "directory"); // NOI18N
                    loggerPrefix = engine.getAttributeValue(SContext.LOGGER, "prefix"); // NOI18N
                    loggerSuffix = engine.getAttributeValue(SContext.LOGGER, "suffix"); // NOI18N
                    String timestamp = engine.getAttributeValue(SContext.LOGGER, "timestamp"); // NOI18N
                    loggerTimestamp = Boolean.valueOf(timestamp).booleanValue();
                } else {
                    hasLogger = false;
                }
            }
        }
    }
    
    /**
     * Return path to server.xml file.
     *
     * @return path to server.xml file.
     */
    public String serverXmlPath() {
        return serverXml.getAbsolutePath();
    }
    
    /**
     * Return bean representation of the Server element of the server.xml file.
     *
     * @return bean representation of the Server element of the server.xml file, 
     *         <code>null</code> if error occurs.
     */
    public Server getServerElement() {
        try {
            return Server.createGraph(serverXml);
        } catch (IOException ioe) {
            ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, ioe);
        }
        return null;
    }
    
    /**
     * Return engine element from server.xml if defined.
     * Looks only for the first appearance of the service element.
     * (ide currently does not support multiple service and host elements).
     */
    public Engine getEngineElement() {
        Server server = getServerElement();
        if (server == null) return null;
        Service[] service = server.getService();
        if (service.length > 0) {
            return service[0].getEngine();
        }
        return null;
    }
    
    /**
     * Return host element from server.xml if defined.
     * Looks only for the first appearance of the service and host element.
     * (ide currently does not support multiple service and host elements).
     */
    public Host getHostElement() {
        Engine engine = getEngineElement();
        if (engine != null) {
            Host[] host = engine.getHost();
            if (host.length > 0) {
                return host[0];
            }
        }
        return null;
    }
    
    /**
     * Return <code>true</code> if there is a logger defined in the first host 
     * or engine element in server.xml, <code>false</code> otherwise.
     *
     * @return <code>true</code> if there is a logger defined in the first host 
     *         or engine element in server.xml, <code>false</code> otherwise.
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
}
