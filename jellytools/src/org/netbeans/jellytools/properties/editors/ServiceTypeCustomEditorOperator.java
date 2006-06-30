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

package org.netbeans.jellytools.properties.editors;

/*
 * ServiceTypeCustomEditorOperator.java
 *
 * Created on 6/12/02 4:39 PM
 */

import javax.swing.JDialog;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling Service Type Custom Editor
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0 */
public class ServiceTypeCustomEditorOperator extends NbDialogOperator {

    /** Creates new ServiceTypeCustomEditorOperator
     * @throws TimeoutExpiredException when NbDialog not found
     * @param title String title of custom editor */
    public ServiceTypeCustomEditorOperator(String title) {
        super(title);
    }

    /** creates new ServiceTypeCustomEditorOperator
     * @param wrapper JDialogOperator wrapper for custom editor */    
    public ServiceTypeCustomEditorOperator(JDialogOperator wrapper) {
        super((JDialog)wrapper.getSource());
    }

    private JButtonOperator _btDefault;
    private JSplitPaneOperator _splitPane;
    private JListOperator _lstServices;
    private PropertySheetOperator _propertySheet;

    /** Tries to find "Default" JButton in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btDefault() {
        if (_btDefault==null) {
            _btDefault = new JButtonOperator( this, Bundle.getString("org.netbeans.beaninfo.editors.Bundle", "LAB_DefaultServiceType"));
        }
        return _btDefault;
    }

    /** getter for JSplitPaneOperator
     * @return JSplitPaneOperator */    
    public JSplitPaneOperator splitPane() {
        if (_splitPane==null) {
            _splitPane=new JSplitPaneOperator(this);
        }
        return _splitPane;
    }
    
    /** Tries to find null ListView$NbList in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JListOperator
     */
    public JListOperator lstServices() {
        if (_lstServices==null) {
            _lstServices = new JListOperator(splitPane());
        }
        return _lstServices;
    }

    /** getter for PropertySheetOperator
     * @return PropertySheetOperator */    
    public PropertySheetOperator propertySheet() {
        if (_propertySheet==null) {
            _propertySheet=new PropertySheetOperator(splitPane());
        }
        return _propertySheet;
    }
    
    /** clicks on "Default" JButton
     * @throws TimeoutExpiredException when JButton not found
     */
    public void setDefault() {
        btDefault().push();
    }
    
    /** getter for selected service type name
     * @return String service type name */    
    public String getServiceTypeValue() {
        Object o=lstServices().getSelectedValue();
        if (o!=null) return o.toString();
        return null;
    }
    
    /** setter for service type name
     * @param serviceName String name of service type to be set */    
    public void setServiceTypeValue(String serviceName) {
        lstServices().selectItem(serviceName);
    }
    
    /** Performs verification by accessing all sub-components */    
    public void verify() {
        btDefault();
        lstServices();
        propertySheet();
    }
}

