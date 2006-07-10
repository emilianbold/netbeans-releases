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

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.Bundle;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.Action.Shortcut;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;

import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JTableOperator;

/**
 * Abstract class for Test of Property Editors.
 *
 * @author  mmirilovic@netbeans.org
 */
public abstract class PropertyEditors extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    protected static NbDialogOperator propertiesWindow = null;
    
    protected static JTableOperator tableOperator;
    
    /** Creates a new instance of ValidatePropertyEditors */
    public PropertyEditors(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of ValidatePropertyEditors */
    public PropertyEditors(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void shutdown() {
        if(propertiesWindow!=null && propertiesWindow.isShowing())
            propertiesWindow.close();
    }
    
    public void initialize(){
        propertiesWindow = openPropertySheet();
    }
    
    /** Open property sheet (bean customizer). */
    protected static NbDialogOperator openPropertySheet() {
        String waitDialogTimeout = "DialogWaiter.WaitDialogTimeout";
        long findTimeout = JemmyProperties.getCurrentTimeout(waitDialogTimeout);
        JemmyProperties.setCurrentTimeout(waitDialogTimeout, 3000);

        try{
            propertiesWindow = new NbDialogOperator(Bundle.getString("org.netbeans.core.Bundle", "CTL_FMT_LocalProperties", new Object[]{new Integer(1),"TestNode"}));
        }catch(org.netbeans.jemmy.TimeoutExpiredException exception){
            new PropertyEditorsTestSheet();
            propertiesWindow = new NbDialogOperator(Bundle.getString("org.netbeans.core.Bundle", "CTL_FMT_LocalProperties", new Object[]{new Integer(1),"TestNode"}));
        }
        
        JemmyProperties.setCurrentTimeout(waitDialogTimeout, findTimeout);
        
        return propertiesWindow;
    }
    
    protected static Property findProperty(String propertyName, NbDialogOperator propertiesWindow) {
        PropertySheetOperator propertySheet = new PropertySheetOperator(propertiesWindow);
        Property property = new Property(propertySheet, propertyName);
        
        // property.openEditor(); - doesn't work - custom editor is opened without Users Event
        // hack for invoking Custom Editor by pushing shortcut CTRL+SPACE
        tableOperator = propertySheet.tblSheet();
        // Need to request focus before selection because invokeCustomEditor action works
        // only when table is focused
        tableOperator.makeComponentVisible();
        tableOperator.requestFocus();
        tableOperator.waitHasFocus();
        // need to select property first
        ((javax.swing.JTable)tableOperator.getSource()).changeSelection(property.getRow(), 0, false, false);
//        return new Property(new PropertySheetOperator(propertiesWindow), propertyName);
        return property;
    }
    
    protected static void openPropertyEditor(){
        new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_SPACE, KeyEvent.CTRL_MASK)).perform(tableOperator);
    }
    
}
