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
 * OneOneFinderDialog.java        November 3, 2003, 1:45 PM
 *
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
public class OneOneFinderDialog extends BeanInputDialog{
    /* A class implementation comment can go here. */

    String methodName;
    String queryParams;
    String queryFilter;
    String queryVariables;
    String queryOrdering;
    OneOneFinderDialogPanel finderDlgPanel;

    static final ResourceBundle bundle = 
        ResourceBundle.getBundle(
            "org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule.Bundle"); // NOI18N


    /** Creates a new instance of OneOneFinderDialog */
    public OneOneFinderDialog(OneOneFinderPanel parent, String title,
            Object[] values){
        super(parent, title, true, values);
        methodName = (String)values[0];
        queryParams = (String)values[1];
        queryFilter = (String)values[2];
        queryVariables = (String)values[3];
        queryOrdering = (String)values[4];
    }


    public OneOneFinderDialog(OneOneFinderPanel parent, String title){
        super(parent, title, true);
    }


    public String getHelpId() {
        return "AS_CFG_OneOneFinder";                                   //NOI18N
    }


    protected JPanel getDialogPanel(Object[] values){
        //called in case of EDIT operation
        //create panel
        //initialize all the components in the panel
        //porvide handlers for all the components; these handlers will
        //update methodName, queryParams, queryFilter, queryVariables and
        //queryOrdering.
        finderDlgPanel = new OneOneFinderDialogPanel(values);
        return finderDlgPanel;
    }


    protected JPanel getDialogPanel(){
        //called in case of ADD operation
        //create panel
        //initialize all the components in the panel
        //porvide handlers for all the components; these handlers will
        //update methodName, queryParams, queryFilter, queryVariables and
        //queryOrdering.
        finderDlgPanel = new OneOneFinderDialogPanel();
        return finderDlgPanel;
    }


    protected Object[] getValues(){
        Object[] values = new Object[5];
        values[0] = (Object)finderDlgPanel.getMethodName();
        values[1] = (Object)finderDlgPanel.getQueryParams();
        values[2] = (Object)finderDlgPanel.getQueryFilter();
        values[3] = (Object)finderDlgPanel.getQueryVariables();
        values[4] = (Object)finderDlgPanel.getQueryOrdering();
        return values; 
    }


    protected Collection getErrors(){
        ArrayList errors = new ArrayList();
        //perform validation for methodName, queryParams, queryFilter,
        //queryVariables and queryOrdering.
        if(validationSupport == null) assert(false);

        String property = finderDlgPanel.getMethodName();
        errors.addAll(validationSupport.validate(property,
            "/sun-ejb-jar/enterprise-beans/ejb/cmp/one-one-finders/finder/method-name", //NOI18N
                bundle.getString("LBL_Method_Name")));                  //NOI18N

        property = finderDlgPanel.getQueryParams();
        errors.addAll(validationSupport.validate(property,
            "/sun-ejb-jar/enterprise-beans/ejb/cmp/one-one-finders/finder/query-params", //NOI18N
                bundle.getString("LBL_Query_Params")));                 //NOI18N
        
        property = finderDlgPanel.getQueryFilter();
        errors.addAll(validationSupport.validate(property,
            "/sun-ejb-jar/enterprise-beans/ejb/cmp/one-one-finders/finder/query-filter", //NOI18N
                bundle.getString("LBL_Query_Filter")));                 //NOI18N

        property = finderDlgPanel.getQueryVariables();
        errors.addAll(validationSupport.validate(property,
            "/sun-ejb-jar/enterprise-beans/ejb/cmp/one-one-finders/finder/query-variables", //NOI18N
                bundle.getString("LBL_Query_Variables")));              //NOI18N

        property = finderDlgPanel.getQueryOrdering();
        errors.addAll(validationSupport.validate(property,
            "/sun-ejb-jar/enterprise-beans/ejb/cmp/one-one-finders/finder/query-ordering", //NOI18N
                bundle.getString("LBL_Query_Ordering")));               //NOI18N
        
        return errors;
    }


    // returns number of elements in this dialog
    protected int getNOofFields() {
        return 5;
    }    
}
