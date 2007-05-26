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

import javax.swing.JPanel;

import org.netbeans.modules.j2ee.sun.share.configbean.BaseEjb;
import org.netbeans.modules.j2ee.sun.share.configbean.StatelessEjb;

/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class StatelessEjbCustomizer extends SessionEjbCustomizer {

    private BeanPoolPanel beanPoolPanel;

    
    /** Creates a new instance of StatelessEjbCustomizer */
	public StatelessEjbCustomizer() {
	}

    public String getHelpId() {
        return "AS_CFG_StatelessEjb";                                   //NOI18N
    }

    // Get the bean specific panel
    protected JPanel getBeanPanel() {
        return null;
    }

    // Initialize all the elements in the bean specific panel
    protected void initializeBeanPanel(BaseEjb theBean) {
    }

    protected void addTabbedBeanPanels() {
        super.addTabbedBeanPanels();
        
        beanPoolPanel = new BeanPoolPanel(this);
        beanPoolPanel.getAccessibleContext().setAccessibleName(bundle.getString("BeanPool_Acsbl_Name"));             //NOI18N
        beanPoolPanel.getAccessibleContext().setAccessibleDescription(bundle.getString("BeanPool_Acsbl_Desc"));      //NOI18N  
        tabbedPanel.insertTab(bundle.getString("LBL_BeanPool"), null, beanPoolPanel, null, 0); // NOI18N
        
        // Select Bean Pool Panel
        tabbedPanel.setSelectedIndex(tabbedPanel.indexOfTab(bundle.getString("LBL_BeanPool")));  //NOI18N
    }


    protected void initializeTabbedBeanPanels(BaseEjb theBean) {
        super.initializeTabbedBeanPanels(theBean);
        
        if(theBean != null) {
            beanPoolPanel.initFields(theBean.getBeanPool());
        }
    }

    protected boolean setBean(Object bean) {
		boolean result = super.setBean(bean);
		
		if(bean instanceof StatelessEjb) {
			result = true;
		} else {
			// if bean is not a StatelessEjb, then it shouldn't have passed BaseEjb either.
			assert (result == false) : 
				"StatelessEjbCustomizer was passed wrong bean type in setBean(Object bean)";	// NOI18N
				
			result = false;
		}
		
		return result;
    }
}
