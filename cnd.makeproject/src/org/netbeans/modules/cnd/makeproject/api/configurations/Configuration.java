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

import java.beans.PropertyChangeSupport;
import java.util.Enumeration;
import java.util.Vector;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;

public abstract class Configuration {
    private String baseDir;
    private String name;
    private boolean defaultConfiguration;
    
    private PropertyChangeSupport pcs = null;
    
    protected Vector auxObjects = new Vector();
    
    private Configuration cloneOf;
    
    public Configuration(String baseDir, String name) {
        this.baseDir = baseDir;
        this.name = name;
        defaultConfiguration = false;
        
        // For change support
        pcs = new PropertyChangeSupport(this);
        
        // Create and initialize auxiliary objects
        auxObjects = new Vector();
        synchronized (auxObjects) {
            ConfigurationAuxObjectProvider[] auxObjectProviders = ConfigurationDescriptorProvider.getAuxObjectProviders();
            for (int i = 0; i < auxObjectProviders.length; i++) {
                ConfigurationAuxObject pao = auxObjectProviders[i].factoryCreate(baseDir, pcs);
                pao.initialize();
                auxObjects.add(pao);
            }
        }
        System.err.println("------------------------------551-" + auxObjects.size());
            
    }
    
    public void setCloneOf(Configuration profile) {
        this.cloneOf = profile;
    }
    
    public Configuration getCloneOf() {
        return cloneOf;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getBaseDir() {
        // this dir is possibly local directory (in remote mode)
        return FilePathAdaptor.mapToRemote(baseDir);
    }
    
    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }
    
    public String getDisplayName() {
        if (isDefault())
            return getName() + " (active)";
        else
            return getName();
    }
    
    public boolean isDefault() {
        return defaultConfiguration;
    }
    
    public void setDefault(boolean b) {
        defaultConfiguration = b;
    }
    
    public String toString() {
        return getDisplayName();
    }
    
    public void addAuxObject(ConfigurationAuxObject pao) {
        synchronized (auxObjects) {
            auxObjects.add(pao);
        }
    }
    
    public void removeAuxObject(ConfigurationAuxObject pao) {
        synchronized (auxObjects) {
            auxObjects.removeElement(pao);
        }
    }
    
    public void removeAuxObject(String id) {
        ConfigurationAuxObject pao = getAuxObject(id);
        removeAuxObject(pao);
    }
    
    public ConfigurationAuxObject getAuxObject(String id) {
        ConfigurationAuxObject pao = null;
        synchronized (auxObjects) {
            for (Enumeration e = auxObjects.elements() ; e.hasMoreElements() ;) {
                ConfigurationAuxObject o = (ConfigurationAuxObject)e.nextElement();
                if (o.getId().equals(id)) {
                    pao = o;
                    break;
                }
            }
        }
        return pao;
    }
    
    public ConfigurationAuxObject[] getAuxObjects() {
        synchronized (auxObjects) {
            return (ConfigurationAuxObject[]) auxObjects.toArray(new ConfigurationAuxObject[auxObjects.size()]);
        }
    }
    
    public void setAuxObjects(Vector v) {
        synchronized (auxObjects) {
            auxObjects = v;
        }
    }
    
    public abstract Configuration cloneConf();
    
    public abstract void assign(Configuration conf);
    
    public abstract Configuration copy();
    
    public void cloneConf(Configuration clone) {
        // name is already cloned
        clone.setDefault(isDefault());
    }
    
    public RunProfile getProfile() {
        return (RunProfile)getAuxObject(RunProfile.PROFILE_ID);
    }
}
