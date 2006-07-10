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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package gui.menu;

import gui.Utilities;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.nodes.Node;

/**
 * Test of popup menu on node in Component Inspector.
 * @author  juhrik@netbeans.org, mmirilovic@netbeans.org
 */
public class FormInspectorNodePopupMenu extends ValidatePopupMenuOnNodes {


    /** Creates a new instance of FormInspectorNodePopupMenu */
    public FormInspectorNodePopupMenu(String testName) {
        super(testName);
        WAIT_AFTER_PREPARE = 1000;
    }
    
    /** Creates a new instance of FormInspectorNodePopupMenu */
    public FormInspectorNodePopupMenu(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        WAIT_AFTER_PREPARE = 1000;
    }
    
    public void testFormNodePopupMenuInspector(){
        doMeasurement();
    }
   
     public void initialize(){
        Utilities.openSmallFormFile();
     }
    
    public void shutdown(){
        EditorOperator.closeDiscardAll();
    }    
    public void prepare(){
        String path = "[JFrame]";
        dataObjectNode = new Node(new ComponentInspectorOperator().treeComponents(), path);
        super.prepare();
    }
}
