/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.jboss4.config.mdb;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.jboss4.config.JBDeploymentConfiguration;
import org.netbeans.modules.j2ee.jboss4.config.gen.Mbean;
import org.netbeans.modules.j2ee.jboss4.config.gen.Server;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Libor Kotouc
 */
public class MessageDestinationSupport {
    
    public static String MSG_DEST_RESOURCE_NAME_JB4 = "netbeans-destinations-service.xml"; // NOI18N
    
    //model of the destination service file
    private Server destinationServiceModel;
    
    //destination service file (placed in the resourceDir)
    private File destinationsFile;
    
    //destination service file object
    private FileObject destinationsFO;
    
    public MessageDestinationSupport(File resourceDir) {
        this.destinationsFile = destinationsFile;

        destinationsFile = new File(resourceDir, MSG_DEST_RESOURCE_NAME_JB4);
        ensureDestinationsFOExists();
    }
    
    /**
     * Listener of netbeans-destinations-service.xml document changes.
     */
    private class MessageDestinationFileListener extends FileChangeAdapter {
        
        public void fileChanged(FileEvent fe) {
            assert(fe.getSource() == destinationsFO);
            destinationServiceModel = null;
        }

        public void fileDeleted(FileEvent fe) {
            assert(fe.getSource() == destinationsFO);
            destinationServiceModel = null;
        }
    } 
    
    private void ensureDestinationsFOExists() {
        if (!destinationsFile.exists()) {
            return;
        }
        if (destinationsFO == null || !destinationsFO.isValid()) {
            destinationsFO = FileUtil.toFileObject(destinationsFile);
            assert(destinationsFO != null);
            destinationsFO.addFileChangeListener(new MessageDestinationFileListener());
        }
    }
    
    public Set<MessageDestination> getMessageDestinations() throws ConfigurationException {
        
        Server server = getMessageDestinationGraph();
        if (server == null) {
            return Collections.<MessageDestination>emptySet();
        }
        
        HashSet<MessageDestination> destinations = new HashSet<MessageDestination>();
        
        for (Mbean mbean : destinationServiceModel.getMbean()) {
            String mbeanNameAttribute = mbean.getName();
            if (mbeanNameAttribute == null) {
                continue;
            }
            
            MessageDestination.Type type = null;
            if (mbeanNameAttribute.indexOf("service=Queue") > -1) { // NOI18N
                type = MessageDestination.Type.QUEUE;
            }
            else
            if (mbeanNameAttribute.indexOf("service=Topic") > -1) { // NOI18N
                type = MessageDestination.Type.TOPIC;
            }
            if (type == null) {
                continue;
            }
            
            int nameIndex = mbeanNameAttribute.indexOf("name="); // NOI18N
            if (nameIndex == -1) {
                continue;
            }
            
            String name = mbeanNameAttribute.substring(nameIndex + 5); // "name=".length() == 5
            if (name.indexOf(",") > -1) {
                name = name.substring(0, name.indexOf(",")); // NOI18N
            }
                
            destinations.add(new JBossMessageDestination(name, type));
        }
        
        return destinations;
    }

    public MessageDestination createMessageDestination(String name, MessageDestination.Type type) 
    throws UnsupportedOperationException, ConfigurationException {
        // TODO write into file
        return new JBossMessageDestination(name, type);
    }
    
    /**
     * Return destination service graph. If it was not created yet, load it from the file
     * and cache it. If the file does not exist, generate it.
     *
     * @return Destination service graph or null if the netbeans-destinations-service.xml file is not parseable.
     */
    private synchronized Server getMessageDestinationGraph() {
        
        try {
            if (destinationsFile.exists()) {
                // load configuration if already exists
                try {
                    if (destinationServiceModel == null)
                        destinationServiceModel = Server.createGraph(destinationsFile);
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ioe);
                } catch (RuntimeException re) {
                    // netbeans-destinations-service.xml is not parseable, do nothing
                }
            } else {
                // create netbeans-destinations-service.xml if it does not exist yet
                destinationServiceModel = new Server();
                JBDeploymentConfiguration.writeFile(destinationsFile, destinationServiceModel);
                ensureDestinationsFOExists();
            }
        } catch (ConfigurationException ce) {
            ErrorManager.getDefault().notify(ce);
        }

        return destinationServiceModel;
    }
    
}
