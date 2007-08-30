/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.makeproject.configurations.ConfigurationXMLReader;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

public class ConfigurationDescriptorProvider {
    private FileObject projectDirectory;
    private ConfigurationDescriptor projectDescriptor = null;
    boolean hasTried = false;
    private String relativeOffset = null;
    
    public ConfigurationDescriptorProvider(FileObject projectDirectory) {
        this.projectDirectory = projectDirectory;
    }
    
    public void setRelativeOffset(String relativeOffset) {
        this.relativeOffset = relativeOffset;
    }
    
    private Object readLock = new Object();
    public ConfigurationDescriptor getConfigurationDescriptor() {
        if (projectDescriptor == null) {
            // attempt to read configuration descriptor
            if (!hasTried) {
                // do this only once
                synchronized (readLock) {
                    // check again that someone already havn't read
                    if (!hasTried) {
                        ConfigurationXMLReader reader = new ConfigurationXMLReader(projectDirectory);
                        
                        if (SwingUtilities.isEventDispatchThread()) {
                            //System.err.println("ConfigurationDescriptorProvider Switching thead...");
                            ProjectReader projectReader = new ProjectReader(reader, relativeOffset);
                            RequestProcessor.Task task = RequestProcessor.getDefault().post(projectReader); 
                            task.waitFinished();
                            projectDescriptor = projectReader.projectDescriptor;
                        }
                        else {
                            try {
                                projectDescriptor = reader.read(relativeOffset);
                            } catch (java.io.IOException x) {
                                ;	// most likely open failed
                            }
                        }
                        
                        hasTried = true;
                    }
                }
            }
        }
        return projectDescriptor;
    }
    
    private class ProjectReader implements Runnable {
        public ConfigurationDescriptor projectDescriptor = null;
        private ConfigurationXMLReader reader;
        private String relativeOffset;
        
        public ProjectReader(ConfigurationXMLReader reader, String relativeOffset) {
            this.reader = reader;
            this.relativeOffset = relativeOffset;
        }
        
        public void run() {
            try {
                projectDescriptor = reader.read(relativeOffset);
            } catch (java.io.IOException x) {
                ;	// most likely open failed
            }
        }
    }
    
    public static ConfigurationAuxObjectProvider[] getAuxObjectProviders() {
        HashSet auxObjectProviders = new HashSet();
        Lookup.Template template = new Lookup.Template(ConfigurationAuxObjectProvider.class);
        Lookup.Result result = Lookup.getDefault().lookup(template);
        Collection collection = result.allInstances();
//      System.err.println("-------------------------------collection " + collection);
        Iterator iterator = collection.iterator();
        while (iterator.hasNext()) {
            Object caop = iterator.next();
            if (caop instanceof ConfigurationAuxObjectProvider) {
                auxObjectProviders.add(caop);
            }
        }
//      System.err.println("-------------------------------auxObjectProviders " + auxObjectProviders);
        return (ConfigurationAuxObjectProvider[])auxObjectProviders.toArray(new ConfigurationAuxObjectProvider[auxObjectProviders.size()]);
    }
}
