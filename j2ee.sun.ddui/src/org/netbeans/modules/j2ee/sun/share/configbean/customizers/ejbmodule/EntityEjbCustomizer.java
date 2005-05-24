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
 * EntityEjbCustomizer.java        October 23, 2003, 2:06 PM
 *
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import java.util.ArrayList;
import java.util.Collection;

//DEPLOYMENT API
import javax.enterprise.deploy.spi.DConfigBean;

//Swing
import javax.swing.event.TableModelListener;

import org.netbeans.modules.j2ee.sun.dd.api.ejb.BeanCache;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.BeanPool;
import org.netbeans.modules.j2ee.sun.share.configbean.BaseEjb;
import org.netbeans.modules.j2ee.sun.share.configbean.EntityEjb;

/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class EntityEjbCustomizer extends EjbCustomizer
            implements TableModelListener {


    private EntityEjb theBean;
    private EntityEjbPanel enityEjbPanel;
    private BeanPoolPanel beanPoolPanel;
    private BeanCachePanel beanCachePanel;


    /** Creates a new instance of EntityEjbCustomizer */
	public EntityEjbCustomizer() {
	}
	
    public EntityEjbCustomizer(DConfigBean bean) {
        if(!(bean instanceof EntityEjb)){
            assert(false);
        }
		
		setObject(bean);
    }

    public void setObject(Object bean) {
        super.setObject(bean);
		
		// Only do this if the bean is actually changing.
		if(theBean != bean) {
			if(bean instanceof EntityEjb) {
				theBean = (EntityEjb) bean;
			}
		}
	}
    

    //get the bean specific panel
    protected javax.swing.JPanel getBeanPanel(){
        enityEjbPanel = new EntityEjbPanel(this);
        return enityEjbPanel;
    }


    //initialize all the elements in the bean specific panel
    protected void initializeBeanPanel(BaseEjb theBean){
        if(!(theBean instanceof EntityEjb)){
            assert(false);
        }

        EntityEjb entityEjb = (EntityEjb)theBean;
        String isReadOnlyBean = entityEjb.getIsReadOnlyBean();
        if(isReadOnlyBean != null){
            enityEjbPanel.setIsreadOnlyBean(isReadOnlyBean);
        }
        String refPeriodInSecs = entityEjb.getRefreshPeriodInSeconds();
        if(refPeriodInSecs != null){
            enityEjbPanel.setRefreshPeriodInSeconds(refPeriodInSecs);
        }
        String commitOption = entityEjb.getCommitOption();
        enityEjbPanel.setCommitOption(commitOption);
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

        //Select Bean Pool Panel
        tabbedPanel.setSelectedIndex(tabbedPanel.indexOfTab(bundle.getString("LBL_BeanPool")));  //NOI18N
    }


    protected void initializeTabbedBeanPanels(BaseEjb theBean) {
        if(!(theBean instanceof EntityEjb)){
            assert(false);
        }
        
        EntityEjb entityEjb = (EntityEjb)theBean;
        BeanPool beanPool = entityEjb.getBeanPool();
        beanPoolPanel.setValues(beanPool);
        
        BeanCache beanCache = entityEjb.getBeanCache();
        beanCachePanel.setValues(beanCache);
    }


    public Collection getErrors(){
        ArrayList errors = null;
        if(validationSupport == null) assert(false);
        errors = (ArrayList)super.getErrors();

        //Entity Ejb field Validations
        String property = enityEjbPanel.getIsreadOnlyBean();
        errors.addAll(validationSupport.validate(property,
            "/sun-ejb-jar/enterprise-beans/ejb/is-read-only-bean",      //NOI18N
                bundle.getString("LBL_Is_Read_Only_Bean")));            //NOI18N

        property = enityEjbPanel.getRefreshPeriodInSeconds();
        errors.addAll(validationSupport.validate(property,
            "/sun-ejb-jar/enterprise-beans/ejb/refresh-period-in-seconds", //NOI18N
                bundle.getString("LBL_Refresh_Period_In_Seconds")));    //NOI18N

        property = enityEjbPanel.getCommitOption();
        errors.addAll(validationSupport.validate(property,
            "/sun-ejb-jar/enterprise-beans/ejb/commit-option",          //NOI18N
                bundle.getString("LBL_Commit_Option")));                //NOI18N

        return errors;
    }


    public void validateEntries(){
        super.validateEntries();
    }


    public String getHelpId() {
        return "AS_CFG_EntityEjb";                                      //NOI18N
    }


    //Entity Ejb update methods
    void updateIsReadOnlyBean(String isReadOnlyBean){
        if(theBean != null){
            try{
                if(EMPTY_STRING.equals(isReadOnlyBean)){
                    theBean.setIsReadOnlyBean(null);
                }else{
                    theBean.setIsReadOnlyBean(isReadOnlyBean);
                }
            }catch(java.beans.PropertyVetoException exception){
            }
            notifyChange();
        }
    }


    void updateRefreshPeriodInSeconds(String refPeriodInSecs){
        if(theBean != null){
            try{
                if(EMPTY_STRING.equals(refPeriodInSecs)){
                    theBean.setRefreshPeriodInSeconds(null);
                }else{
                    theBean.setRefreshPeriodInSeconds(refPeriodInSecs);
                }
            }catch(java.beans.PropertyVetoException exception){
            }
            notifyChange();
        }
    }


    void updateSetCommitOption(String commitOption){
        if(theBean != null){
            try{
                if(EMPTY_STRING.equals(commitOption)){
                    theBean.setCommitOption(null);
                }else{
                    theBean.setCommitOption(commitOption);
                }
            }catch(java.beans.PropertyVetoException exception){
            }
            notifyChange();
        }
    }
}
