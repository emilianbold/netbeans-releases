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

package org.netbeans.modules.cnd.discovery.wizard.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.discovery.wizard.api.FileConfiguration;
import org.netbeans.modules.cnd.discovery.wizard.api.FolderConfiguration;

/**
 *
 * @author Alexander Simon
 */
public class FolderConfigurationImpl extends NodeConfigurationImpl implements FolderConfiguration {
    private String path;
    private Map<String, FolderConfigurationImpl> folders = new HashMap<String,FolderConfigurationImpl>();
    private List<FileConfigurationImpl> files = new ArrayList<FileConfigurationImpl>();

    public FolderConfigurationImpl(String path) {
        this.path = path;
    }

    public FolderConfigurationImpl cut(){
        if (folders.size() == 1 && files.size() == 0){
            return folders.values().iterator().next();
        }
        return null;
    }
    
    public List<FolderConfiguration> getFolders() {
        return new ArrayList<FolderConfiguration>(folders.values());
    }

    public void addChild(FolderConfigurationImpl subfolder) {
        folders.put(subfolder.getFolderName(),subfolder);
    }

    public FolderConfigurationImpl getChild(String name) {
        return folders.get(name);
    }

    public List<FileConfiguration> getFiles() {
        return new ArrayList<FileConfiguration>(files);
    }

    public void addFile(FileConfigurationImpl file) {
        files.add(file);
    }

    public String getFolderPath() {
        return path;
    }

    public String getFolderName() {
        int i = path.lastIndexOf("/"); // NOI18N
        if(i>=0){
            return path.substring(i+1);
        }
        return path;
    }
}
