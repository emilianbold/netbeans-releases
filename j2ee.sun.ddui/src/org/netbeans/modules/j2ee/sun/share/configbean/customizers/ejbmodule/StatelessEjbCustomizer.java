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
/*
 * StatelessEjbCustomizer.java        October 21, 2003, 12:15 PM
 *
 */
package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.event.TableModelListener;

//DEPLOYMENT API
import javax.enterprise.deploy.spi.DConfigBean;

import org.netbeans.modules.j2ee.sun.dd.api.ejb.BeanPool;
import org.netbeans.modules.j2ee.sun.share.configbean.BaseEjb;
import org.netbeans.modules.j2ee.sun.share.configbean.SessionEjb;
import org.netbeans.modules.j2ee.sun.share.configbean.StatelessEjb;

/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class StatelessEjbCustomizer extends SessionEjbCustomizer 
            implements TableModelListener {

    private StatelessEjb theBean;
    private BeanPoolPanel beanPoolPanel;

    /** Creates a new instance of StatelessEjbCustomizer */
	public StatelessEjbCustomizer() {
	}
	
    public StatelessEjbCustomizer(DConfigBean bean) {
		setObject(bean);
    }

    public void setObject(Object bean) {
        super.setObject(bean);
		
		// Only do this if the bean is actually changing.
		if(theBean != bean) {
			if(bean instanceof StatelessEjb) {
				theBean = (StatelessEjb) bean;
			}
		}
    }


    public String getHelpId() {
        return "AS_CFG_StatelessEjb";                                   //NOI18N
    }


    //get the bean specific panel
    protected javax.swing.JPanel getBeanPanel(){
        return null;
    }


    //initialize all the elements in the bean specific panel
    protected void initializeBeanPanel(BaseEjb theBean){};
    

    protected void addTabbedBeanPanels() {
        super.addTabbedBeanPanels();
        beanPoolPanel = new BeanPoolPanel(this);
        beanPoolPanel.getAccessibleContext().setAccessibleName(bundle.getString("BeanPool_Acsbl_Name"));             //NOI18N
        beanPoolPanel.getAccessibleContext().setAccessibleDescription(bundle.getString("BeanPool_Acsbl_Desc"));      //NOI18N  
        tabbedPanel.insertTab(bundle.getString("LBL_BeanPool"), null, beanPoolPanel, null, 0); // NOI18N
        //Select Bean Pool Panel
        tabbedPanel.setSelectedIndex(tabbedPanel.indexOfTab(bundle.getString("LBL_BeanPool")));  //NOI18N
    }


    protected void initializeTabbedBeanPanels(BaseEjb theBean) {
        super.initializeTabbedBeanPanels(theBean);
        if(!(theBean instanceof StatelessEjb)){
            assert(false);
        }
        StatelessEjb statelessEjb = (StatelessEjb)theBean;
        BeanPool beanPool = statelessEjb.getBeanPool();
        beanPoolPanel.setValues(beanPool);
    }


    public Collection getErrors(){
        ArrayList errors = null;
        if(validationSupport == null) assert(false);
        errors = (ArrayList)super.getErrors();

        //Stateless Session Ejb field Validations

        return errors;
    }


    public java.awt.GridBagConstraints getErrorPanelConstraints(){
        return super.getErrorPanelConstraints();
    }
}
