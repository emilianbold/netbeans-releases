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
 * EntityEjbCustomizer.java        October 23, 2003, 2:06 PM
 *
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;


import org.netbeans.modules.j2ee.sun.share.configbean.BaseEjb;
import org.netbeans.modules.j2ee.sun.share.configbean.EntityEjb;

/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class EntityEjbCustomizer extends EjbCustomizer {

    private EntityEjb theEntityBean;
    
    private EntityEjbPanel entityEjbPanel;
    private BeanPoolPanel beanPoolPanel;
    private BeanCachePanel beanCachePanel;


    /** Creates a new instance of EntityEjbCustomizer */
	public EntityEjbCustomizer() {
	}
    
    public EntityEjb getEntityBean() {
        return theEntityBean;
    }
	
    // Get the bean specific panel
    protected javax.swing.JPanel getBeanPanel() {
        entityEjbPanel = new EntityEjbPanel(this);
        return entityEjbPanel;
    }

    // Initialize all the elements in the bean specific panel
    protected void initializeBeanPanel(BaseEjb theBean) {
        entityEjbPanel.initFields(theEntityBean);
    };

    protected void addTabbedBeanPanels() {
        beanPoolPanel = new BeanPoolPanel(this);
        beanPoolPanel.getAccessibleContext().setAccessibleName(bundle.getString("BeanPool_Acsbl_Name"));             //NOI18N
        beanPoolPanel.getAccessibleContext().setAccessibleDescription(bundle.getString("BeanPool_Acsbl_Desc"));      //NOI18N  
        tabbedPanel.insertTab(bundle.getString("LBL_BeanPool"), null, beanPoolPanel, null, 0); // NOI18N

        beanCachePanel = new BeanCachePanel(this);
        beanCachePanel.getAccessibleContext().setAccessibleName(bundle.getString("BeanCache_Acsbl_Name"));             //NOI18N
        beanCachePanel.getAccessibleContext().setAccessibleDescription(bundle.getString("BeanCache_Acsbl_Desc"));      //NOI18N  
        tabbedPanel.addTab(bundle.getString("LBL_BeanCache"),          // NOI18N
            beanCachePanel);

        // Select Bean Pool Panel
        tabbedPanel.setSelectedIndex(tabbedPanel.indexOfTab(bundle.getString("LBL_BeanPool")));  //NOI18N
    }

    protected void initializeTabbedBeanPanels(BaseEjb theBean) {
        beanPoolPanel.initFields(theBean.getBeanPool());
        beanCachePanel.initFields(theBean.getBeanCache());
    }

    public String getHelpId() {
        return "AS_CFG_EntityEjb";                                      //NOI18N
    }

    protected boolean setBean(Object bean) {
		boolean result = super.setBean(bean);
		
		if(bean instanceof EntityEjb) {
            theEntityBean = (EntityEjb) bean;
			result = true;
		} else {
			// if bean is not a EntityEjb, then it shouldn't have passed BaseEjb either.
			assert (result == false) : 
				"EntityEjbCustomizer was passed wrong bean type in setBean(Object bean)";	// NOI18N
				
            theEntityBean = null;
			result = false;
		}
		
		return result;
    }       
}
