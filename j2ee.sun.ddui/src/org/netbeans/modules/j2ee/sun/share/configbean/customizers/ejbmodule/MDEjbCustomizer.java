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
 * MDEjbCustomizer.java        October 27, 2003, 1:05 PM
 *
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.event.TableModelListener;

//DEPLOYMENT API
import javax.enterprise.deploy.spi.DConfigBean;

import org.netbeans.modules.j2ee.sun.dd.api.common.DefaultResourcePrincipal;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.ActivationConfig;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.ActivationConfigProperty;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.BeanPool;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.MdbConnectionFactory;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.MdbResourceAdapter;
import org.netbeans.modules.j2ee.sun.share.configbean.BaseEjb;
import org.netbeans.modules.j2ee.sun.share.configbean.MDEjb;
import org.netbeans.modules.j2ee.sun.share.configbean.StorageBeanFactory;


/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class MDEjbCustomizer extends EjbCustomizer 
            implements TableModelListener {

    private MDEjb theBean;
    private MDEjbPanel mdEjbPanel;
    private BeanPoolPanel beanPoolPanel;
    private ActivationCfgPropertyPanel actvnCfgPrptyPanel;
    private MdbConnectionFactoryPanel mdbConnectionFactoryPanel;
    

    /** Creates a new instance of MDEjbCustomizer */
	public MDEjbCustomizer() {
	}
	
    public MDEjbCustomizer(DConfigBean bean) {
        if(!(theBean instanceof MDEjb)){
            assert(false);
        }
	setObject(bean);
    }


    public void setObject(Object bean) {
        super.setObject(bean);
		
		// Only do this if the bean is actually changing.
		if(theBean != bean) {
			if(bean instanceof MDEjb) {
				theBean = (MDEjb) bean;
			}
		}
    }


    //get the bean specific panel
    protected javax.swing.JPanel getBeanPanel(){
        mdEjbPanel = new MDEjbPanel(this);
        return mdEjbPanel;
    }


    //initialize all the elements in the bean specific panel
    protected void initializeBeanPanel(BaseEjb theBean){
        if(!(theBean instanceof MDEjb)){
            assert(false);
        }

        MDEjb mdEjb = (MDEjb)theBean;
        String jmsDurableSubscriptionName = mdEjb.getSubscriptionName();
        mdEjbPanel.setJmsDurableSubscriptionName(jmsDurableSubscriptionName);

        String maxMessagesLoad = mdEjb.getMaxMessageLoad();
        mdEjbPanel.setMaxMessagesLoad(maxMessagesLoad);

        MdbResourceAdapter mdbResourceAdapter = mdEjb.getMdbResourceAdapter();
        if(mdbResourceAdapter != null){
            String resourceAdapterMid =
                mdbResourceAdapter.getResourceAdapterMid();
            mdEjbPanel.setResourceAdapterMid(resourceAdapterMid);
            
            ActivationConfig activationCfg = 
                mdbResourceAdapter.getActivationConfig();
            if(activationCfg != null){
                String description = activationCfg.getDescription();
                mdEjbPanel.setActivationConfigDescription(description);
            }
        }
    }


    protected void addTabbedBeanPanels() {
        beanPoolPanel = new BeanPoolPanel(this);
        tabbedPanel.insertTab(bundle.getString("LBL_BeanPool"), null, beanPoolPanel, null, 0); // NOI18N

        mdbConnectionFactoryPanel = new MdbConnectionFactoryPanel(this);
        mdbConnectionFactoryPanel.getAccessibleContext().setAccessibleName(bundle.getString("Mdb_Connection_Factory_Acsbl_Name"));       //NOI18N
        mdbConnectionFactoryPanel.getAccessibleContext().setAccessibleDescription(bundle.getString("Mdb_Connection_Factory_Acsbl_Desc"));      //NOI18N
        tabbedPanel.addTab(bundle.getString("LBL_Mdb_Connection_Factory"), // NOI18N
            mdbConnectionFactoryPanel);

        ActivationCfgPropertyModel activnCfgPrptyModel = 
            new ActivationCfgPropertyModel();
        activnCfgPrptyModel.addTableModelListener(this);
        actvnCfgPrptyPanel = 
            new ActivationCfgPropertyPanel(activnCfgPrptyModel);
        actvnCfgPrptyPanel.getAccessibleContext().setAccessibleName(bundle.getString("Activation_Config_Property_Acsbl_Name"));             //NOI18N
        actvnCfgPrptyPanel.getAccessibleContext().setAccessibleDescription(bundle.getString("Activation_Config_Property_Acsbl_Desc"));      //NOI18N
        tabbedPanel.addTab(bundle.getString("LBL_Activation_Config_Property"), // NOI18N
            actvnCfgPrptyPanel);

        //Select Bean Pool Panel
        tabbedPanel.setSelectedIndex(tabbedPanel.indexOfTab(bundle.getString("LBL_BeanPool")));  //NOI18N        
    }


    protected void initializeTabbedBeanPanels(BaseEjb theBean) {
        if(!(theBean instanceof MDEjb)){
            assert(false);
        }

        MDEjb mdEjb = (MDEjb)theBean;
        BeanPool beanPool = mdEjb.getBeanPool();
        beanPoolPanel.setValues(beanPool);

        MdbConnectionFactory mdbConnectionFactory =
            mdEjb.getMdbConnectionFactory();
        mdbConnectionFactoryPanel.setValues(mdbConnectionFactory);

        MdbResourceAdapter mdbResourceAdapter = mdEjb.getMdbResourceAdapter();
        if(mdbResourceAdapter == null){
            actvnCfgPrptyPanel.setModel(mdEjb,null);
        }else{
            ActivationConfig activationCfg =
                mdbResourceAdapter.getActivationConfig();
            if(activationCfg == null){
                actvnCfgPrptyPanel.setModel(mdEjb,null);
            }else{
                ActivationConfigProperty[] activationCfgProperty =
                    activationCfg.getActivationConfigProperty();
                actvnCfgPrptyPanel.setModel(mdEjb,activationCfgProperty);
            }
        }
    }


    public Collection getErrors(){
        ArrayList errors = null;
        if(validationSupport == null) assert(false);
        errors = (ArrayList)super.getErrors();

        //Message Driven Ejb field Validations
        String property = mdEjbPanel.getJmsDurableSubscriptionName();
        errors.addAll(validationSupport.validate(property,
            "/sun-ejb-jar/enterprise-beans/ejb/jms-durable-subscription-name", //NOI18N
                bundle.getString("LBL_Jms_Durable_Subscription_Name")));//NOI18N

        property = mdEjbPanel.getMaxMessagesLoad();
        errors.addAll(validationSupport.validate(property,
            "/sun-ejb-jar/enterprise-beans/ejb/jms-max-messages-load",  //NOI18N
                bundle.getString("LBL_Jms_Max_Messages_Load")));        //NOI18N

        boolean isResourceAdapterPresent = isResourceAdapterPresent();
        if(isResourceAdapterPresent){
            property = mdEjbPanel.getResourceAdapterMid();
            errors.addAll(validationSupport.validate(property,
                "/sun-ejb-jar/enterprise-beans/ejb/mdb-resource-adapter/resource-adapter-mid", //NOI18N
                    bundle.getString("LBL_Resource_Adapter_Mid")));     //NOI18N

            property = mdEjbPanel.getActivationConfigDescription();
            errors.addAll(validationSupport.validate(property,
                "/sun-ejb-jar/enterprise-beans/ejb/mdb-resource-adapter/activation-config/description", //NOI18N
                    bundle.getString("LBL_Activation_Config_Description")));//NOI18N
        }
            
        return errors;
    }


    public void validateEntries(){
        super.validateEntries();
    }


    public String getHelpId() {
        return "AS_CFG_MDEjb";                                          //NOI18N
    }

    //MD Ejb update methods
    void updateJmsDurableSubscriptionName(String jmsDurblSubsName){
        try{
            if(EMPTY_STRING.equals(jmsDurblSubsName)){
                theBean.setSubscriptionName(null);
            }else{
                theBean.setSubscriptionName(jmsDurblSubsName);
            }
        }catch(java.beans.PropertyVetoException exception){
        }
        notifyChange();
    }


    void updateJmsMaxMessagesLoad(String maxMessagesLoad){
        try{
            if(EMPTY_STRING.equals(maxMessagesLoad)){
                theBean.setMaxMessageLoad(null);
            }else{
                theBean.setMaxMessageLoad(maxMessagesLoad);
            }
        }catch(java.beans.PropertyVetoException exception){
        }
        notifyChange();
    }


    void updateMdbConnectionFactoryJndiName(String jndiName){
        MdbConnectionFactory mddbConnectionFactory
            = getMdbConnectionFactory();

        if((EMPTY_STRING.equals(jndiName)) || (null == jndiName)){
            mddbConnectionFactory.setJndiName(null);
            updateMdbConnectionFactory();
        }else{
            mddbConnectionFactory.setJndiName(jndiName);
        }
        notifyChange();
    }


    void updateDefaultResourcePrincipalName(String principalName){
        DefaultResourcePrincipal defaultResourcePrincipal =
            getDefaultResourcePrincipal();

        if((EMPTY_STRING.equals(principalName)) || (null == principalName)){
            defaultResourcePrincipal.setName(null);
            updateDefaultResourcePrincipal();
        }else{
            defaultResourcePrincipal.setName(principalName);
        }

        notifyChange();
    }


    void updateDefaultResourcePrincipalPassword(String principalPassword){
        DefaultResourcePrincipal defaultResourcePrincipal =
            getDefaultResourcePrincipal();

        if((EMPTY_STRING.equals(principalPassword)) || (null == principalPassword)){
            defaultResourcePrincipal.setPassword(null);
            updateDefaultResourcePrincipal();
        }else{
            defaultResourcePrincipal.setPassword(principalPassword);
        }

        notifyChange();
    }


    void updateResourceAdapterMid(String mid){
        MdbResourceAdapter mdbResourceAdapter =
            getMdbResourceAdapter();

        if((EMPTY_STRING.equals(mid)) || (null == mid)){
            mdbResourceAdapter.setResourceAdapterMid(null);
            updateResourceAdapter();
        }else{
            mdbResourceAdapter.setResourceAdapterMid(mid);
        }

        notifyChange();
    }


    void updateActivationConfigDescription(String description){
        ActivationConfig activationConfig = getActivationConfig();

        if((EMPTY_STRING.equals(description)) || (null == description)){
            activationConfig.setDescription(null);
            updateActivationConfig();
        }else{
            activationConfig.setDescription(description);
        }

        notifyChange();
    }


    private MdbConnectionFactory getMdbConnectionFactory(){
        MdbConnectionFactory mdbConnectionFactory = 
            theBean.getMdbConnectionFactory();
        if(mdbConnectionFactory == null){
            mdbConnectionFactory = StorageBeanFactory.getDefault().createMdbConnectionFactory();
            try{
                theBean.setMdbConnectionFactory(mdbConnectionFactory);
            }catch(java.beans.PropertyVetoException exception){
            }
        }
        return mdbConnectionFactory;
    }


    private DefaultResourcePrincipal getDefaultResourcePrincipal(){
        MdbConnectionFactory mdbConnectionFactory = 
            getMdbConnectionFactory();
        DefaultResourcePrincipal defaultResourcePrincipal =
            mdbConnectionFactory.getDefaultResourcePrincipal();
        if(defaultResourcePrincipal == null){
            defaultResourcePrincipal = StorageBeanFactory.getDefault().createDefaultResourcePrincipal();
            mdbConnectionFactory.setDefaultResourcePrincipal(
               defaultResourcePrincipal);
        }
        return defaultResourcePrincipal;
    }


    private MdbResourceAdapter getMdbResourceAdapter(){
        MdbResourceAdapter mdbResourceAdapter = 
            theBean.getMdbResourceAdapter();
        if(mdbResourceAdapter == null){
            mdbResourceAdapter = StorageBeanFactory.getDefault().createMdbResourceAdapter();
            try{
                theBean.setMdbResourceAdapter(mdbResourceAdapter);
            }catch(java.beans.PropertyVetoException exception){
            }
        }
        return mdbResourceAdapter;
    }


    private ActivationConfig getActivationConfig(){
        MdbResourceAdapter mdbResourceAdapter =
            getMdbResourceAdapter();
        ActivationConfig activationConfig =
            mdbResourceAdapter.getActivationConfig();
        if(activationConfig == null){
            activationConfig = StorageBeanFactory.getDefault().createActivationConfig();
            mdbResourceAdapter.setActivationConfig(
               activationConfig);
        }
        return activationConfig;
    }


    private boolean isResourceAdapterPresent(){
        boolean resourceAdapterPresent = false;
        String property = mdEjbPanel.getResourceAdapterMid();
        while(true){
            if((property != null) && (property.length() != 0)){
                resourceAdapterPresent = true;
                break;
            }

            property = mdEjbPanel.getActivationConfigDescription();
            if((property != null) && (property.length() != 0)){
                resourceAdapterPresent = true;
                break;
            }
            break;
        }
        return resourceAdapterPresent;
    }


    private void updateDefaultResourcePrincipal(){
        DefaultResourcePrincipal defaultResourcePrincipal =
            getDefaultResourcePrincipal();
        
        if(defaultResourcePrincipal.getName() != null) return;
        if(defaultResourcePrincipal.getPassword() != null) return;
        getMdbConnectionFactory().setDefaultResourcePrincipal(null);
        updateMdbConnectionFactory();
    }


    private void updateMdbConnectionFactory(){
        MdbConnectionFactory mddbConnectionFactory
            = getMdbConnectionFactory();

        if(mddbConnectionFactory.getJndiName() != null) return;
        if(mddbConnectionFactory.getDefaultResourcePrincipal() != null) return;
        try{
            theBean.setMdbConnectionFactory(null);
        }catch(java.beans.PropertyVetoException exception){
        }
    }


    private void updateActivationConfig(){
        ActivationConfig activationConfig = getActivationConfig();

        if(activationConfig.getDescription() != null) return;
        if((activationConfig.getActivationConfigProperty() != null) &&
                (activationConfig.getActivationConfigProperty().length > 0)) return;

        getMdbResourceAdapter().setActivationConfig(null);
        updateResourceAdapter();
    }


    private void updateResourceAdapter(){
        MdbResourceAdapter mdbResourceAdapter =
            getMdbResourceAdapter();

        if(mdbResourceAdapter.getResourceAdapterMid() != null) return;
        if(mdbResourceAdapter.getActivationConfig() != null) return;
        try{
            theBean.setMdbResourceAdapter(null);
        }catch(java.beans.PropertyVetoException exception){
        }
    }
}
