/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
