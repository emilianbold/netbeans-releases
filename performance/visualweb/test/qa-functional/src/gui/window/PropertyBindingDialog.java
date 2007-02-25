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

package gui.window;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.DialogOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

/**
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class PropertyBindingDialog extends JSFComponentOptionsDialog {
    /** Creates a new instance of PropertyBindingDialog */
    public PropertyBindingDialog(String testName) {
        super(testName);
        expectedTime = 3000;
        WAIT_AFTER_OPEN=5000;
        categoryName = "Basic"; // NOI18N
        componentName = "Table"; // NOI18N
        addPoint = new java.awt.Point(50,50);
    }
    
    public PropertyBindingDialog(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 3000;
        WAIT_AFTER_OPEN=3000;
        categoryName = "Basic"; // NOI18N
        componentName = "Table"; // NOI18N
        addPoint = new java.awt.Point(50,50);
    }
    
    public ComponentOperator open(){
        log("::open");

        //Invoking popup menu on component
        JPopupMenuOperator popup = surface.clickPopup(70,70);
	if(popup == null) { log("popup operator is null");}

        popup.pushMenu("Property Bindings..."); //NOI18N
        
        return new DialogOperator("Property Bindings for table"); //NOI18N
    }
    
}
