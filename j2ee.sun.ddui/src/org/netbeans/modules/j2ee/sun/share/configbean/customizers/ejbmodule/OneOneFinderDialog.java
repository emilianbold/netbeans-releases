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
