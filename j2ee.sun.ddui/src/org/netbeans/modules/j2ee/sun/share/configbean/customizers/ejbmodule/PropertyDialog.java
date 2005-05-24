/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * PropertyDialog.java
 *
 * Created on October 13, 2003, 11:36 AM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

import javax.swing.JPanel;

import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.BeanInputDialog;

/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */

public class PropertyDialog extends BeanInputDialog{
    /* A class implementation comment can go here. */
    String name;
    String value;
    PropertyDialogPanel propertyDlgPanel;
    String helpId;

    static final ResourceBundle bundle = 
        ResourceBundle.getBundle(
            "org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule.Bundle"); // NOI18N
    
    private static final String ACTIVATION_CONFIG_PROPERTY = "AS_CFG_Activation_Config_Property";   //NOI18N


    /** Creates a new instance of PropertyDialog */
    public PropertyDialog(PropertyPanel parent,
            String title, Object[] values, String helpID){
        super(parent, title, true, values);
        name = (String)values[0];
        value = (String)values[1];
        helpId = helpID;
    }


    public PropertyDialog(PropertyPanel parent,
            String title, String helpID){
        super(parent, title, true);
        helpId = helpID;
    }


    /** Creates a new instance of PropertyDialog */
    public PropertyDialog(ActivationCfgPropertyPanel parent,
            String title, Object[] values){
        super(parent, title, true, values);
        name = (String)values[0];
        value = (String)values[1];
        helpId = ACTIVATION_CONFIG_PROPERTY;
    }


    public PropertyDialog(ActivationCfgPropertyPanel parent,
            String title){
        super(parent, title, true);
        helpId = ACTIVATION_CONFIG_PROPERTY;
    }


    public String getHelpId() {
        return helpId;
    }


    protected JPanel getDialogPanel(Object[] values){
        //called in case of EDIT operation
        //create panel
        //initialize all the components in the panel
        //provide handlers for all the components; these handlers will update
        // name and value.
        propertyDlgPanel = new PropertyDialogPanel(values);
        return propertyDlgPanel;
    }


    protected JPanel getDialogPanel(){
        //called in case of ADD operation
        //create panel
        //initialize all the components in the panel
        //provide handlers for all the components; these handlers will update
        // name and value.
        propertyDlgPanel = new PropertyDialogPanel();
        return propertyDlgPanel;
    }


    protected Object[] getValues(){
        Object[] values = new Object[2];
        values[0] = (Object)propertyDlgPanel.getName();
        values[1] = (Object)propertyDlgPanel.getValue();
        return values; 
    }


    protected Collection getErrors(){
        ArrayList errors = new ArrayList();

        //perform validation for name and value.
        if(validationSupport == null) assert(false);

        String property = propertyDlgPanel.getName();
        errors.addAll(validationSupport.validate(property,
            "/sun-ejb-jar/enterprise-beans/cmp-resource/property/name", //NOI18N
                bundle.getString("LBL_Name")));                         //NOI18N     

        property = propertyDlgPanel.getValue();
        errors.addAll(validationSupport.validate(property,
            "/sun-ejb-jar/enterprise-beans/cmp-resource/property/value",//NOI18N
                bundle.getString("LBL_Value")));                        //NOI18N

        return errors;
    }


    // returns number of elements in this dialog
    protected int getNOofFields() {
        return 2;
    }
}
