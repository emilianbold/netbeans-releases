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

import java.util.TreeMap;
import javax.swing.tree.DefaultMutableTreeNode;
import org.netbeans.modules.cnd.discovery.wizard.api.FileConfiguration;
import org.netbeans.modules.cnd.discovery.wizard.api.FolderConfiguration;

/**
 *
 * @author Alexander Simon
 */
public class FolderConfigurationNode extends DefaultMutableTreeNode {
    private FolderConfigurationImpl folder;
    
    public FolderConfigurationNode(FolderConfigurationImpl folder) {
        super(folder);
        this.folder = folder;
        addChild(folder);
    }

    private void addChild(FolderConfiguration root){
       TreeMap<String, FolderConfiguration> sorted = new TreeMap<String, FolderConfiguration>();
       for(FolderConfiguration child : root.getFolders()){
           sorted.put(child.getFolderName(),child);
        }
       for(FolderConfiguration child :sorted.values()){
           add(new FolderConfigurationNode((FolderConfigurationImpl) child));
       }
       TreeMap<String, FileConfiguration> sorted2 = new TreeMap<String, FileConfiguration>();
       for(FileConfiguration file : root.getFiles()){
           sorted2.put(file.getFileName(),file);
        }
       for(FileConfiguration file : sorted2.values()){
           add(new FileConfigurationNode((FileConfigurationImpl) file));
        }
    }
    
    
    @Override
    public String toString() {
        return folder.getFolderName();
    }
    
    public FolderConfigurationImpl getFolder() {
        return folder;
    }
    
    public boolean isCheckedInclude() {
        return !folder.overrideIncludes();
    }
    
    public void setCheckedInclude(boolean checkedInclude) {
        folder.setOverrideIncludes(!checkedInclude);
    }
    
    public boolean isCheckedMacro() {
        return !folder.overrideMacros();
    }
    
    public void setCheckedMacro(boolean checkedMacro) {
        folder.setOverrideMacros(!checkedMacro);
    }
}
