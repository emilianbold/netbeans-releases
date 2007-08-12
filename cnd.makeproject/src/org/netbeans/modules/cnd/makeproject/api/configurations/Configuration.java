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

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.spi.project.ProjectConfiguration;
import org.openide.util.NbBundle;

public abstract class Configuration implements ProjectConfiguration {
    private String baseDir;
    private String name;
    private boolean defaultConfiguration;
    
    private PropertyChangeSupport pcs = null;
    
    private Map<String, ConfigurationAuxObject> auxObjectsMap = Collections.synchronizedSortedMap(new TreeMap<String, ConfigurationAuxObject>());
    
    private Configuration cloneOf;
    
    public Configuration(String baseDir, String name) {
        this.baseDir = baseDir;
        this.name = name;
        defaultConfiguration = false;
        
        // For change support
        pcs = new PropertyChangeSupport(this);
        
        // Create and initialize auxiliary objects
        ConfigurationAuxObjectProvider[] auxObjectProviders = ConfigurationDescriptorProvider.getAuxObjectProviders();
        for (int i = 0; i < auxObjectProviders.length; i++) {
            ConfigurationAuxObject pao = auxObjectProviders[i].factoryCreate(baseDir, pcs);
            pao.initialize();
            //auxObjects.add(pao);
            String id = pao.getId();
            if (auxObjectsMap.containsKey(id)) {
                System.err.println("Init duplicated ConfigurationAuxObject id="+id);
            }
            auxObjectsMap.put(id,pao);
        }
        
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
            return getName();
    }
    
    public boolean isDefault() {
        return defaultConfiguration;
    }
    
    public void setDefault(boolean b) {
        defaultConfiguration = b;
    }
    
    public String toString() {
        if (isDefault())
            return getDisplayName() + " " + getString("ActiveTxt"); // NOI18N
        else
        return getDisplayName();
    }
    
    public void addAuxObject(ConfigurationAuxObject pao) {
        String id = pao.getId();
        if (auxObjectsMap.containsKey(id)) {
            System.err.println("Add duplicated ConfigurationAuxObject id="+id);
        }
        auxObjectsMap.put(id,pao);
    }
    
    
    public void removeAuxObject(ConfigurationAuxObject pao) {
        auxObjectsMap.remove(pao.getId());
    }
    
    
    public void removeAuxObject(String id) {
        auxObjectsMap.remove(id);
    }
    
    public ConfigurationAuxObject getAuxObject(String id) {
        return auxObjectsMap.get(id);
    }
    
    public ConfigurationAuxObject[] getAuxObjects() {
        List<ConfigurationAuxObject> list;
        synchronized (auxObjectsMap){
            list = new ArrayList<ConfigurationAuxObject>(auxObjectsMap.values());
        }
        return (ConfigurationAuxObject[]) list.toArray(new ConfigurationAuxObject[list.size()]);
    }
    
    public void setAuxObjects(List<ConfigurationAuxObject> v) {
        synchronized (auxObjectsMap) {
            auxObjectsMap.clear();
            for(ConfigurationAuxObject object : v){
                auxObjectsMap.put(object.getId(),object);
            }
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
    
    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(Configuration.class, s);
    }
}
