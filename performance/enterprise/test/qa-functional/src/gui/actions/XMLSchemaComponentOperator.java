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

package gui.actions;

import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author mkhramov@netbeans.org
 */
public class XMLSchemaComponentOperator extends TopComponentOperator {
    
    /** Creates a new instance of XMLSchemaComponentOperator */
    public XMLSchemaComponentOperator(String topComponentName) {
        this(topComponentName,0);
    }
    /** Creates a new instance of XMLSchemaComponentOperator */
    public XMLSchemaComponentOperator(String topComponentName, int Index) {
        super(topComponentName,Index);
    }
    
    private JToggleButtonOperator getViewButton(String viewName) {
        return new JToggleButtonOperator(this,viewName);
    }
    public JToggleButtonOperator getSourceButton() {
        return getViewButton("Source"); // NOI18N
    }
    public JToggleButtonOperator getSchemaButton() {
        return getViewButton("Schema"); // NOI18N
    }
    public JToggleButtonOperator getDesignButton() {
        return getViewButton("Design"); // NOI18N
    }
    
}
