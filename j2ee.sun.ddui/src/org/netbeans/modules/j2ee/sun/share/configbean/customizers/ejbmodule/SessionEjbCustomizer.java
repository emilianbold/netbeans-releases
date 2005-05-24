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
 * SessionEjbCustomizer        October 20, 2003, 11:49 PM
 *
 */
package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.event.TableModelListener;

import javax.enterprise.deploy.spi.DConfigBean;

import org.netbeans.modules.j2ee.sun.share.configbean.BaseEjb;
import org.netbeans.modules.j2ee.sun.share.configbean.SessionEjb;

/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public abstract class SessionEjbCustomizer extends EjbCustomizer 
            implements TableModelListener {

    private WebserviceEndpointPanel websrvcEndpointPanel;

    /** Creates a new instance of SessionEjbCustomizer */
	public SessionEjbCustomizer() {
	}
	
    public SessionEjbCustomizer(DConfigBean bean) {
    }


    protected void addTabbedBeanPanels() {
        WebserviceEndpointModel websrvcEndpointModel = 
            new WebserviceEndpointModel();
        websrvcEndpointModel.addTableModelListener(this);
        websrvcEndpointPanel = new WebserviceEndpointPanel(websrvcEndpointModel);
        websrvcEndpointPanel.getAccessibleContext().setAccessibleName(bundle.getString("WebserviceEndpoint_Acsbl_Name"));       //NOI18N
        websrvcEndpointPanel.getAccessibleContext().setAccessibleDescription(bundle.getString("WebserviceEndpoint_Acsbl_Desc"));      //NOI18N
        tabbedPanel.addTab(bundle.getString("LBL_WebserviceEndpoint"), // NOI18N
            websrvcEndpointPanel);
    }


    protected void initializeTabbedBeanPanels(BaseEjb theBean) {
        if(!(theBean instanceof SessionEjb)){
            assert(false);
        }
        SessionEjb sessionEjb = (SessionEjb)theBean;
        websrvcEndpointPanel.setModel(sessionEjb,
            sessionEjb.getWebserviceEndpoint());
    }


    public Collection getErrors(){
        ArrayList errors = null;
        if(validationSupport == null) assert(false);
        errors = (ArrayList)super.getErrors();

        //Session Ejb field Validations

        return errors;
    }
}
