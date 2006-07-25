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

package gui.window;

import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.editors.StringCustomEditorOperator;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of String Property Editor.
 *
 * @author  mmirilovic@netbeans.org
 */
public class PropertyEditorString extends PropertyEditors {
    
    /** Creates a new instance of PropertyEditorString */
    public PropertyEditorString(String testName) {
        super(testName);
    }
    
    /** Creates a new instance of PropertyEditorString */
    public PropertyEditorString(String testName, String performanceDataName) {
        super(testName,performanceDataName);
    }
    
    private Property property;
    
    public void prepare(){
        property = findProperty("String", propertiesWindow); //NOI18N impossible
    }
    
    public ComponentOperator open(){
        openPropertyEditor();
        return new StringCustomEditorOperator("String"); //NOI18N impossible
    }
    
    /** Test could be executed internaly in IDE without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new PropertyEditorString("measureTime"));
    }
    
    
}
