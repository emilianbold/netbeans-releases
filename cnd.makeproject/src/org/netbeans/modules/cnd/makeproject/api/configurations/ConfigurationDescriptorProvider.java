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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.util.Vector;
import org.netbeans.modules.cnd.makeproject.configurations.ConfigurationXMLReader;
import org.openide.filesystems.FileObject;

public class ConfigurationDescriptorProvider {
    private static Vector auxObjectProviders = new Vector();
    
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
    
    public synchronized ConfigurationDescriptor getConfigurationDescriptor() {
        if (projectDescriptor == null && !hasTried) {
            hasTried = true;
            ConfigurationXMLReader reader;
            reader = new ConfigurationXMLReader(projectDirectory);
            try {
                projectDescriptor = reader.read(relativeOffset);
            } catch (java.io.IOException x) {
                ;	// most likely open failed
            }
            
            if (projectDescriptor == null) {
                // Big problems: cannot read descriptor. All information lost....
                /*
                projectDescriptor = new MakeProjectDescriptor(ProjectDescriptor.TYPE_APPLICATION);
                ((MakeProjectDescriptor)projectDescriptor).init();
                String folder = FileUtil.toFile(helper.getProjectDirectory()).getPath();
                 */
                /* OLD
                Moved into ConfigurationXMLReader or rather, XMLDocReader
                String errormsg = NbBundle.getMessage(ConfigurationDescriptorProvider.class, "CANTREADDESCRIPTOR", projectDirectory.getName());
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errormsg, NotifyDescriptor.ERROR_MESSAGE)); // NOI18N
                 */
            }
        }
        return projectDescriptor;
    }
    
    public static void addAuxObjectProvider(ConfigurationAuxObjectProvider paop) {
        synchronized(auxObjectProviders) {
            auxObjectProviders.add(paop);
        }
    }
    
    public static void removeAuxObjectProvider(ConfigurationAuxObjectProvider paop) {
        synchronized(auxObjectProviders) {
            auxObjectProviders.remove(paop);
        }
    }
    
    
    public static ConfigurationAuxObjectProvider[] getAuxObjectProviders() {
        waitUntilReady();
        synchronized(auxObjectProviders) {
            return (ConfigurationAuxObjectProvider[])auxObjectProviders.toArray(new ConfigurationAuxObjectProvider[auxObjectProviders.size()]);
        }
    }
    
    private static boolean ready = false;    
    private static void waitUntilReady() {
        if (ready)
            return;
        synchronized(Thread.currentThread()) {
            try {
                Thread.currentThread().wait(500);
            }
            catch (Exception e) {
            }
        }
        ready = true;
    }
}
