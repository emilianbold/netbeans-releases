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
 * ActivationCfgPropertyPanel.java        October 27, 2003, 5:55 PM
 *
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import java.util.ResourceBundle;

import org.netbeans.modules.j2ee.sun.dd.api.ejb.ActivationConfigProperty;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.BeanInputDialog;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.BeanTablePanel;
import org.netbeans.modules.j2ee.sun.share.configbean.MDEjb;

/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class ActivationCfgPropertyPanel extends BeanTablePanel{
    /* A class implementation comment can go here. */
    
    static final ResourceBundle bundle = 
        ResourceBundle.getBundle(
            "org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule.Bundle"); //NOI18N

    /** Creates a new instance of ActivationCfgPropertyPanel */
    public ActivationCfgPropertyPanel(final ActivationCfgPropertyModel model) {
    	super(model);
    }


    void setModel(MDEjb mdEjb, 
        ActivationConfigProperty[] params) {
        super.setModel(mdEjb, params);
    }


    public BeanInputDialog  getInputDialog(Object[] values){
        //called in case of EDIT operation
        String title = bundle.getString("LBL_Property");                //NOI18N
        return new PropertyDialog(this, title, values);

    }


    public BeanInputDialog  getInputDialog(){
        //called in case of ADD operation
        String title = bundle.getString("LBL_Property");                //NOI18N
        return new PropertyDialog(this, title);
    }


    protected String getAccessibleName(){
        return bundle.getString("Activation_Config_Property_Acsbl_Name"); //NOI18N
    }


    protected String getAccessibleDesc(){
        return bundle.getString("Activation_Config_Property_Acsbl_Desc"); //NOI18N   
    }
}
