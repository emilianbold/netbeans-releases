/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.visualweb.webui.themes;

import org.netbeans.api.project.Project;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.visualweb.project.jsf.services.ThemeNodeService;
import org.openide.nodes.Node;

/**
 * Implementation of ThemeNodeService
 * @author winstonp
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.visualweb.project.jsf.services.ThemeNodeService.class)
public class ThemeNodeServiceImpl implements ThemeNodeService{

    public Node getThemeNode(Project project) {
        // find if project is of type j2ee 1.4 then return
        // Themes Folder Node else return null;
        if (JsfProjectUtils.isJavaEE5Project(project)){
          return null;   
        }else{
            return new ThemesFolderNode(project);
        }
    }

}
