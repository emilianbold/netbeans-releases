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

import java.text.MessageFormat;
import javax.swing.tree.DefaultMutableTreeNode;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.wizard.SelectConfigurationPanel;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
public class ProjectConfigurationNode extends DefaultMutableTreeNode {
    private ProjectConfigurationImpl project;
    private int count;
    
    public ProjectConfigurationNode(ProjectConfigurationImpl project) {
        super(project);
        this.project = project;
        count = project.getFiles().size();
        add(new FolderConfigurationNode((FolderConfigurationImpl) project.getRoot()));
    }
    
    @Override
    public String toString() {
        if (getProject().getLanguageKind() == ItemProperties.LanguageKind.C){
            return getString("ConfigurationLanguageC",""+count);  // NOI18N
        } else if (getProject().getLanguageKind() == ItemProperties.LanguageKind.CPP){
            return getString("ConfigurationLanguageCPP",""+count);  // NOI18N
        }
         return getString("ConfigurationLanguageUnknown",""+count);  // NOI18N
    }
    
    public ProjectConfigurationImpl getProject() {
        return project;
    }

    private String getString(String key, String files) {
        String message = NbBundle.getBundle(SelectConfigurationPanel.class).getString(key);
        return MessageFormat.format(message, new Object[]{files});
        
    }
}
