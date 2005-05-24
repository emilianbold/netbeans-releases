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
 * StatefulEjbCustomizer.java        October 22, 2003, 12:50 PM
 *
 */
package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.event.TableModelListener;

//DEPLOYMENT API
import javax.enterprise.deploy.spi.DConfigBean;

import org.netbeans.modules.j2ee.sun.dd.api.ejb.BeanCache;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.BeanPool;
import org.netbeans.modules.j2ee.sun.share.configbean.BaseEjb;
import org.netbeans.modules.j2ee.sun.share.configbean.SessionEjb;
import org.netbeans.modules.j2ee.sun.share.configbean.StatefulEjb;

/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class StatefulEjbCustomizer extends SessionEjbCustomizer 
            implements TableModelListener {

    private StatefulEjb theBean;
    private StatefulEjbPanel statefulEjbPanel;
    private BeanPoolPanel beanPoolPanel;
    private BeanCachePanel beanCachePanel;
    private CheckpointAtEndOfMethodPanel checkpointAtEndOfMethodPanel;



    /** Creates a new instance of StatefulEjbCustomizer */
	public StatefulEjbCustomizer() {
	}
	
    public StatefulEjbCustomizer(DConfigBean bean) {
        if(!(bean instanceof StatefulEjb)){
            assert(false);
        }
        
        setObject(bean);
    }


    public void setObject(Object bean) {
        super.setObject(bean);
		
		// Only do this if the bean is actually changing.
		if(theBean != bean) {
			if(bean instanceof StatefulEjb) {
				theBean = (StatefulEjb) bean;
			}
		}
    }


    //get the bean specific panel
    protected javax.swing.JPanel getBeanPanel(){
        statefulEjbPanel = new StatefulEjbPanel(this);
        return statefulEjbPanel;
    }


    //initialize all the elements in the bean specific panel
    protected void initializeBeanPanel(BaseEjb theBean){
        if(!(theBean instanceof StatefulEjb)){
            assert(false);
        }
        StatefulEjb statefulEjb = (StatefulEjb)theBean;
        String availabilityEnabled = statefulEjb.getAvailabilityEnabled();
        if(availabilityEnabled != null){
            statefulEjbPanel.setAvailabilityEnabled(availabilityEnabled);
        }
    };


    protected void addTabbedBeanPanels() {
        super.addTabbedBeanPanels();
        beanPoolPanel = new BeanPoolPanel(this);
        beanPoolPanel.getAccessibleContext().setAccessibleName(bundle.getString("BeanPool_Acsbl_Name"));             //NOI18N
        beanPoolPanel.getAccessibleContext().setAccessibleDescription(bundle.getString("BeanPool_Acsbl_Desc"));      //NOI18N  
        tabbedPanel.insertTab(bundle.getString("LBL_BeanPool"), null, beanPoolPanel, null, 0); // NOI18N

        beanCachePanel = new BeanCachePanel(this);
        beanCachePanel.getAccessibleContext().setAccessibleName(bundle.getString("BeanCache_Acsbl_Name"));             //NOI18N
        beanCachePanel.getAccessibleContext().setAccessibleDescription(bundle.getString("BeanCache_Acsbl_Desc"));      //NOI18N  
        tabbedPanel.addTab(bundle.getString("LBL_BeanCache"),          // NOI18N
            beanCachePanel);

         checkpointAtEndOfMethodPanel = new CheckpointAtEndOfMethodPanel(theBean, this);
        checkpointAtEndOfMethodPanel.addTableModelListener(this);
        tabbedPanel.addTab(bundle.getString("LBL_Checkpoint_At_End_Of_Method"),    // NOI18N
            checkpointAtEndOfMethodPanel);
        
        //Select Bean Pool Panel
        tabbedPanel.setSelectedIndex(tabbedPanel.indexOfTab(bundle.getString("LBL_BeanPool")));  //NOI18N

    }


    protected void initializeTabbedBeanPanels(BaseEjb theBean) {
        super.initializeTabbedBeanPanels(theBean);
        if(!(theBean instanceof StatefulEjb)){
            assert(false);
        }
        StatefulEjb statefulEjb = (StatefulEjb)theBean;
        BeanPool beanPool = statefulEjb.getBeanPool();
        beanPoolPanel.setValues(beanPool);

        BeanCache beanCache = statefulEjb.getBeanCache();
        beanCachePanel.setValues(beanCache);
        
        checkpointAtEndOfMethodPanel.setData(statefulEjb);
    }


    public Collection getErrors(){
        ArrayList errors = null;
        if(validationSupport == null) assert(false);
        errors = (ArrayList)super.getErrors();

        //Stateful Session Ejb field Validations
        //Stateful Ejb field Validations
        String property = statefulEjbPanel.getAvailabilityEnabled();
        errors.addAll(validationSupport.validate(property,
            "/sun-ejb-jar/enterprise-beans/ejb/availability-enabled",   //NOI18N
                bundle.getString("LBL_Availability_Enabled")));            //NOI18N
        return errors;
    }


    public void validateEntries(){
        super.validateEntries();
    }


    public String getHelpId() {
        return "AS_CFG_StatefulEjb";                                    //NOI18N
    }


    //Stateful Ejb update methods
    void updateAvailabilityEnabled(String availabilityEnabled){
        if(theBean != null){
            try{
                theBean.setAvailabilityEnabled(availabilityEnabled);
            }catch(java.beans.PropertyVetoException exception){
            }
            notifyChange();
        }
    }
}
