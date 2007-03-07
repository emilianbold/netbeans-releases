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


import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyException;

/**
 * Utilities for Performance tests, workarrounds, often used methods, ...
 *
 * @author  mmirilovic@netbeans.org
 */
public class VWPUtilities extends gui.Utilities{

    /** Creates a new instance of Utilities */
    public VWPUtilities() {
    }

    /*
     * open jsp file from project and return WebFormDesigner
     */
    public static gui.window.WebFormDesignerOperator openedWebDesignerForJspFile(String projectName, String jspFileName){
        Node openFile = new Node(new ProjectsTabOperator().getProjectRootNode(projectName),WEB_PAGES + "|" +jspFileName + ".jsp");
        new OpenAction().performAPI(openFile);
        try {
            return new gui.window.WebFormDesignerOperator(jspFileName);
        } catch(Exception ex) {
            throw new JemmyException("Exception in WebFormDesignerOperator creation ",ex);
        }
    }
    
}
