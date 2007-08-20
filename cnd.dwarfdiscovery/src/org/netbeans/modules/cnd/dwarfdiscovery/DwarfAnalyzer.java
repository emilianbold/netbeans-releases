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

package org.netbeans.modules.cnd.dwarfdiscovery;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.discovery.api.Configuration;
import org.netbeans.modules.cnd.discovery.api.FolderProperties;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.dwarfdiscovery.provider.*;

/**
 *
 * @author Alexander Simon
 */
public class DwarfAnalyzer {
    
    public static void analyze(String[] files){
        DwarfProvider provider = new DwarfProvider();
        provider.getProperty(DwarfProvider.EXECUTABLES_KEY).setValue(files);
        dumpProject(provider, new ProjectProxy() {
            public boolean createSubProjects() {
                return false;
            }
            public Object getProject() {
                return null;
            }
        });
    }
    
    private static void dumpProject(DwarfProvider provider, ProjectProxy project){
        List<Configuration> confs = provider.analyze(project);
        for (Iterator<Configuration> it = confs.iterator(); it.hasNext();) {
            Configuration conf = it.next();
            List<ProjectProperties> langList = conf.getProjectConfiguration();
            for (Iterator<ProjectProperties> it2 = langList.iterator(); it2.hasNext();) {
                ProjectProperties proj = it2.next();
                String lang = ItemProperties.LanguageKind.CPP == proj.getLanguageKind() ? "c++":"c"; // NOI18N
                System.out.println("Project "+lang); // NOI18N
                System.out.println("User include paths:"); // NOI18N
                StringBuilder buf = new StringBuilder();
                for (String elem : proj.getUserInludePaths()) {
                    System.out.println(elem);
                    if (elem.startsWith(File.separator)){
                        if (buf.length()>0) {
                            buf.append(":"); // NOI18N
                        }
                        buf.append(elem);
                    }
                }
                System.out.println("Configuration string:"); // NOI18N
                System.out.println("    "+buf.toString()); // NOI18N
                System.out.println("User macros:"); // NOI18N
                for (Map.Entry<String,String> elem : proj.getUserMacros().entrySet()) {
                    System.out.println(elem.getKey()+"="+elem.getValue()); // NOI18N
                }
                List<FolderProperties> folders = proj.getConfiguredFolders();
                for (FolderProperties folder : folders) {
                    dumpFolder(folder);
                }
            }
        }
    }
    
    private static void dumpFolder(FolderProperties folder){
        System.out.println("Folder "+folder.getItemPath()); // NOI18N
        System.out.println("User include paths:"); // NOI18N
        StringBuilder buf = new StringBuilder();
        for (String elem : folder.getUserInludePaths()) {
            System.out.println(elem);
            if (elem.startsWith(File.separator)){
                if (buf.length()>0) {
                    buf.append(":"); // NOI18N
                }
                buf.append(elem);
            }
        }
        System.out.println("Configuration string:"); // NOI18N
        System.out.println("    "+buf.toString()); // NOI18N
        System.out.println("User macros:"); // NOI18N
        for (Map.Entry<String,String> elem : folder.getUserMacros().entrySet()) {
            System.out.println(elem.getKey()+"="+elem.getValue()); // NOI18N
        }
    }
}
