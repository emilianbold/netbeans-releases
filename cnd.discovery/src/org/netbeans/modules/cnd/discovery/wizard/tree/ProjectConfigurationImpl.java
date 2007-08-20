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
import java.util.List;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProperties;
import org.netbeans.modules.cnd.discovery.wizard.api.FileConfiguration;
import org.netbeans.modules.cnd.discovery.wizard.api.FolderConfiguration;
import org.netbeans.modules.cnd.discovery.wizard.api.ProjectConfiguration;

/**
 *
 * @author Alexander Simon
 */
public class ProjectConfigurationImpl extends NodeConfigurationImpl implements ProjectConfiguration {
    private ProjectProperties project;
    private FolderConfigurationImpl root;
    
    public ProjectConfigurationImpl(ProjectProperties project, FolderConfigurationImpl root) {
        this.project = project;
        this.root = root;
        linkChild(root);
        root.setParent(this);
    }

    private void linkChild(FolderConfigurationImpl folder){
        for(FolderConfiguration f : folder.getFolders()){
            ((FolderConfigurationImpl)f).setParent(folder);
            linkChild((FolderConfigurationImpl)f);
        }
        for(FileConfiguration f : folder.getFiles()){
            ((FileConfigurationImpl)f).setParent(folder);
        }
    }
    
    public ItemProperties.LanguageKind getLanguageKind() {
        return project.getLanguageKind();
    }

    public FolderConfiguration getRoot() {
        return root;
    }

    public List<FileConfiguration> getFiles() {
        ArrayList<FileConfiguration> list = new ArrayList<FileConfiguration>();
        gatherFiles(root,list);
        return list;
    }

    private void gatherFiles(FolderConfiguration folder, ArrayList<FileConfiguration> list){
        for(FolderConfiguration dir : folder.getFolders()){
            gatherFiles(dir, list);
        }
        list.addAll(folder.getFiles());
    }
}
