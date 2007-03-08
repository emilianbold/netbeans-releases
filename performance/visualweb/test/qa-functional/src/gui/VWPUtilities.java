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

package gui;

import gui.window.WebFormDesignerOperator;

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;

/**
 * Utilities for Performance tests, workarrounds, often used methods, ...
 *
 * @author  mmirilovic@netbeans.org
 */
public class VWPUtilities extends gui.Utilities{

    /**
     * open jsp file from project and return WebFormDesigner
     * @param projectName name of the project
     * @param jspFileName name of the file
     * @return Visual Web Designer
     */
    public static WebFormDesignerOperator openedWebDesignerForJspFile(String projectName, String jspFileName){
        Node openFile = new Node(new ProjectsTabOperator().getProjectRootNode(projectName),WEB_PAGES + "|" +jspFileName + ".jsp");
        new OpenAction().performAPI(openFile);
        
        WebFormDesignerOperator surface = new WebFormDesignerOperator(jspFileName);
        surface.switchToDesignView();

        return surface;
    }

    public static void waitForPendingBackgroundTasks() {
        // wait maximum 5 minutes
        for (int i=0; i<5*60; i++) {
            if (org.netbeans.progress.module.Controller.getDefault().getModel().getSize()==0)
                return;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
    
}
