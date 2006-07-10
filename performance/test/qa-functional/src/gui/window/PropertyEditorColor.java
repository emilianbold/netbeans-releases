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
import org.netbeans.jellytools.properties.editors.ColorCustomEditorOperator;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of Color Property Editor.
 *
 * @author  mmirilovic@netbeans.org
 */
public class PropertyEditorColor extends PropertyEditors {

    /** Creates a new instance of PropertyEditorColor */
    public PropertyEditorColor(String testName) {
        super(testName);
    }

    /** Creates a new instance of PropertyEditorColor */
    public PropertyEditorColor(String testName, String performanceDataName) {
        super(testName,performanceDataName);
    }
    
    private Property property;
    
    public void prepare(){
        property = findProperty("Color", propertiesWindow); //NOI18N impossible
    }
    
    public ComponentOperator open(){
        openPropertyEditor();
        return new ColorCustomEditorOperator("Color"); //NOI18N impossible
    }

}
