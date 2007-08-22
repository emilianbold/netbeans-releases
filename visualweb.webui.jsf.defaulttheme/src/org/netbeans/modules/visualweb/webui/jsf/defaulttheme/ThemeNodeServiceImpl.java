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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.visualweb.webui.jsf.defaulttheme;

import org.netbeans.api.project.Project;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.visualweb.project.jsf.services.ThemeNodeService;
import org.openide.nodes.Node;

/**
 * Implementation of ThemeNodeService
 * @author winstonp
 */
public class ThemeNodeServiceImpl implements ThemeNodeService{

    public Node getThemeNode(Project project) {
        // find if project is of type j2ee 1.4 then return
        // Themes Folder Node else return null;
        if (JsfProjectUtils.isJavaEE5Project(project)){
            return new ThemesFolderNode(project);
        }else{
            return null;
        }
    }

}
