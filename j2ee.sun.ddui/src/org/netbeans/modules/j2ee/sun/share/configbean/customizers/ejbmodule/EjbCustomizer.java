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
 * EjbJarRootCustomizer.java        October 1, 2003, 3:40 PM
 *
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;


//DEPLOYMENT API
import javax.enterprise.deploy.spi.DConfigBean;

import org.netbeans.modules.j2ee.sun.dd.api.ejb.AsContext;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.BeanCache;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.BeanPool;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.IorSecurityConfig;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SasContext;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.TransportConfig;
import org.netbeans.modules.j2ee.sun.share.configbean.BaseEjb;
import org.netbeans.modules.j2ee.sun.share.configbean.StorageBeanFactory;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.BeanCustomizer;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.CustomizerTitlePanel;


/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public abstract class EjbCustomizer extends BeanCustomizer 
        implements org.netbeans.modules.j2ee.sun.share.Constants {

    private BaseEjb theBean;
    private IorSecurityConfigPanel iorSecCfgPanel;

    static final ResourceBundle bundle = 
        ResourceBundle.getBundle(
            "org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule.Bundle"); // NOI18N


    /** Creates new customizer EjbCustomizer */
    public EjbCustomizer() {
        initComponents();
        createPanels();
    }
	
    //temporary constructor; will be removed later once the setObject() is
    //called  by the Studio, immediately after instantiation of this object.
    //Currently Studio calls this Constructor, instead.
    //As per  JSR88 tools should use the default constructor to instantiate
    // Customizer and use setObject() to pass the corresponding ConfigBean.
    public EjbCustomizer(DConfigBean bean){
        initComponents();
        createPanels();
        setObject(bean);
    }

    public void setObject(Object bean) {
                super.setObject(bean);
		// Only do this if the bean is actually changing.
		if(theBean != bean) {
			if(theBean != null) {
				// commit old object
			}
			if(bean instanceof BaseEjb) {
				theBean = (BaseEjb) bean;
				setComponentValues();
			}
		}
                initializing = false;
    }

    //get the bean specific panel
    protected abstract javax.swing.JPanel getBeanPanel();

    //initialize all the elements in the bean specific panel
    protected abstract void initializeBeanPanel(BaseEjb theBean);

    //add bean sepcific tabbed panels
    protected abstract void addTabbedBeanPanels();

    //initialize bean specific panels in tabbed pane
    protected abstract void initializeTabbedBeanPanels(BaseEjb theBean);



    //update methods called by the panel
    private void updateJndiName(String jndiName){
        //update theBean
        try{
            if(EMPTY_STRING.equals(jndiName)){
                theBean.setJndiName(null);
            }else{
                theBean.setJndiName(jndiName);
            }
        }catch(java.beans.PropertyVetoException exception){
        }
        notifyChange();
    }


    private void updatePassByRef(String passByRef){
        try{
            if(EMPTY_STRING.equals(passByRef)){
                theBean.setPassByReference(null);
            }else{
                theBean.setPassByReference(passByRef);
            }
        }catch(java.beans.PropertyVetoException exception){
        }
        notifyChange();
    }


    void updateIntegrity(String integrity){
        TransportConfig transportConfig = getTransportConfig();
        if((EMPTY_STRING.equals(integrity)) || (null == integrity)){
            transportConfig.setIntegrity(null);
            updateTransportConfig();
        }else{
            transportConfig.setIntegrity(integrity);
        }
        notifyChange();
    }


    void updateConfidentiality(String confidentiality){
        TransportConfig transportConfig = getTransportConfig();
        if((EMPTY_STRING.equals(confidentiality)) || (null == confidentiality)){
            transportConfig.setConfidentiality(null);
            updateTransportConfig();
        }else{
            transportConfig.setConfidentiality(confidentiality);
        }
        notifyChange();
    }


    void updateEstbTrstInTrgt(String estbTrstInTrgt){
        TransportConfig transportConfig = getTransportConfig();
        if((EMPTY_STRING.equals(estbTrstInTrgt)) || (null == estbTrstInTrgt)){
            transportConfig.setEstablishTrustInTarget(null);
            updateTransportConfig();
        }else{
            transportConfig.setEstablishTrustInTarget(estbTrstInTrgt);
        }
        notifyChange();
    }


    void updateEstbTrstInClnt(String estbTrstInClnt){
        TransportConfig transportConfig = getTransportConfig();
        if((EMPTY_STRING.equals(estbTrstInClnt)) || (null == estbTrstInClnt)){
            transportConfig.setEstablishTrustInClient(null);
            updateTransportConfig();
        }else{
            transportConfig.setEstablishTrustInClient(estbTrstInClnt);
        }
        notifyChange();
    }


    void updateAuthMethod(String authMethod){
        AsContext asContext = getAsContext();
        if((EMPTY_STRING.equals(authMethod)) || (null == authMethod)){
            asContext.setAuthMethod(null);
            updateAsContext();
        }else{
            asContext.setAuthMethod(authMethod);
        }
        notifyChange();
    }


    void updateRealm(String realm){
        AsContext asContext = getAsContext();
        if((EMPTY_STRING.equals(realm)) || (null == realm)){
            asContext.setRealm(null);
            updateAsContext();
        }else{
            asContext.setRealm(realm);
        }
        notifyChange();
    }


    void updateRequired(String reqd){
        AsContext asContext = getAsContext();
        if((EMPTY_STRING.equals(reqd)) || (null == reqd)){
            asContext.setRequired(null);
            updateAsContext();
        }else{
            asContext.setRequired(reqd);
        }
        notifyChange();
    }


    void updateCallerPropagation(String callerPrpgtn){
        SasContext sasContext = getSasContext();
        if((EMPTY_STRING.equals(callerPrpgtn)) || (null == callerPrpgtn)){
            sasContext.setCallerPropagation(null);
            updateSasContext();
        }else{
            sasContext.setCallerPropagation(callerPrpgtn);
        }
        notifyChange();
    }


    private void createPanels(){
        //add bean specific panel
        
        //title panel
        CustomizerTitlePanel titlePanel = new CustomizerTitlePanel();
        String title = getCustomizerTitle();
        titlePanel.setCustomizerTitle(title);

        java.awt.GridBagConstraints gridBagConstraints = 
            new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
        add(titlePanel, gridBagConstraints);

        javax.swing.JPanel beanSpecificPanel = getBeanPanel();
        if(beanSpecificPanel != null){
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
            add(beanSpecificPanel, gridBagConstraints);
        }

        iorSecCfgPanel = new IorSecurityConfigPanel(this);
        iorSecCfgPanel.getAccessibleContext().setAccessibleName(bundle.getString("IorSecurityConfig_Acsbl_Name"));             //NOI18N
        iorSecCfgPanel.getAccessibleContext().setAccessibleDescription(bundle.getString("IorSecurityConfig_Acsbl_Desc"));      //NOI18N  
        tabbedPanel.addTab(bundle.getString("LBL_IorSecurityConfig"),  // NOI18N
            iorSecCfgPanel);

        //add bean sepcific tabbed panels
        addTabbedBeanPanels();
    }


    public Collection getErrors(){
        ArrayList errors = null;
        if(validationSupport == null) assert(false);
        errors = new ArrayList();

        //Ejb field Validations
        String property = jndiNameTextField.getText();
        errors.addAll(validationSupport.validate(property,
            "/sun-ejb-jar/enterprise-beans/ejb/jndi-name",              //NOI18N
                bundle.getString("LBL_Jndi_Name")));                    //NOI18N
        
        property = (String)passByRefComboBox.getSelectedItem();
        errors.addAll(validationSupport.validate(property,
            "/sun-ejb-jar/enterprise-beans/ejb/pass-by-reference",  //NOI18N
                bundle.getString("LBL_Pass_By_Reference")));        //NOI18N

        return errors;
    }

    
    private void setComponentValues() {
        //initialize all the elements in the general panel
        nameTextField.setText(theBean.getEjbName());
        jndiNameTextField.setText(theBean.getJndiName());
        String passByRef = theBean.getPassByReference();
        if(passByRef != null){
            passByRefComboBox.setSelectedItem(passByRef);
        }

        //initialize all the elements in the bean specific panel
       initializeBeanPanel(theBean);  //abstract

       //initialize IorSecurityConfig
       initializeIorSecurityConfig();
       

       //initialize bean specific panels in tabbed pane
       initializeTabbedBeanPanels(theBean); //abstract
    }


    private void initializeIorSecurityConfig(){
        IorSecurityConfig iorSecurityConfig = theBean.getIorSecurityConfig();
        iorSecCfgPanel.setValues(iorSecurityConfig);
    }


    private TransportConfig getTransportConfig(){
        IorSecurityConfig iorSecCnfg = getIorSecurityConfig();
        TransportConfig transportCfg = iorSecCnfg.getTransportConfig();
        if(transportCfg == null){
            transportCfg = StorageBeanFactory.getDefault().createTransportConfig();
            iorSecCnfg.setTransportConfig(transportCfg);
        }
        return transportCfg;
    }


    private AsContext getAsContext(){
        IorSecurityConfig iorSecCnfg = getIorSecurityConfig();
        AsContext asContext = iorSecCnfg.getAsContext();
        if(asContext == null){
            asContext = StorageBeanFactory.getDefault().createAsContext();
            iorSecCnfg.setAsContext(asContext);
        }
        return asContext;
    }


    private SasContext getSasContext(){
        IorSecurityConfig iorSecCnfg = getIorSecurityConfig();
        SasContext sasContext = iorSecCnfg.getSasContext();
        if(sasContext == null){
            sasContext = StorageBeanFactory.getDefault().createSasContext();
            iorSecCnfg.setSasContext(sasContext);
        }
        return sasContext;
    }

    
    private IorSecurityConfig getIorSecurityConfig(){
        IorSecurityConfig iorSecCnfg = theBean.getIorSecurityConfig();
        if(iorSecCnfg == null){
            iorSecCnfg = StorageBeanFactory.getDefault().createIorSecurityConfig();
            try{
                theBean.setIorSecurityConfig(iorSecCnfg);
            }catch(java.beans.PropertyVetoException exception){
            }
        }
        return iorSecCnfg;
    }


    //BeanPool update methods
    void updateSteadyPoolSize(String steadyPoolSize){
        BeanPool beanPool = getBeanPool();
        if((EMPTY_STRING.equals(steadyPoolSize)) || (null == steadyPoolSize)){
            beanPool.setSteadyPoolSize(null);
            updateBeanPool();
        }else{
            beanPool.setSteadyPoolSize(steadyPoolSize);
        }
        notifyChange();
    }


    void updateResizeQuantity(String resizeQuantity){
        BeanPool beanPool = getBeanPool();
        if((EMPTY_STRING.equals(resizeQuantity)) || (null == resizeQuantity)){
            beanPool.setResizeQuantity(null);
            updateBeanPool();
        }else{
            beanPool.setResizeQuantity(resizeQuantity);
        }
        notifyChange();
    }


    void updateMaxPoolSize(String maxPoolSize){
        BeanPool beanPool = getBeanPool();
        if((EMPTY_STRING.equals(maxPoolSize)) || (null == maxPoolSize)){
            beanPool.setMaxPoolSize(null);
            updateBeanPool();
        }else{
            beanPool.setMaxPoolSize(maxPoolSize);
        }
        notifyChange();
    }


    void updatePoolIdleTimeoutInSeconds(String poolIdleTimeoutInSec){
        BeanPool beanPool = getBeanPool();
        if((EMPTY_STRING.equals(poolIdleTimeoutInSec)) || (null == poolIdleTimeoutInSec)){
            beanPool.setPoolIdleTimeoutInSeconds(null);
            updateBeanPool();
        }else{
            beanPool.setPoolIdleTimeoutInSeconds(poolIdleTimeoutInSec);
        }
        notifyChange();
    }


    //BeanCache update methods
    void updateMaxCacheSize(String maxCacheSize){
        BeanCache beanCache = getBeanCache();
        if((EMPTY_STRING.equals(maxCacheSize)) || (null == maxCacheSize)){
            beanCache.setMaxCacheSize(null);
            updateBeanCache();
        }else{
            beanCache.setMaxCacheSize(maxCacheSize);
        }
        notifyChange();
    }


    void updateCacheResizeQuantity(String resizeQuantity){
        BeanCache beanCache = getBeanCache();
        if((EMPTY_STRING.equals(resizeQuantity)) || (null == resizeQuantity)){
            beanCache.setResizeQuantity(null);
            updateBeanCache();
        }else{
            beanCache.setResizeQuantity(resizeQuantity);
        }
        notifyChange();
    }


    void updateIsCacheOverflowAllowed(String isOverflowAllowed){
        BeanCache beanCache = getBeanCache();
        if((EMPTY_STRING.equals(isOverflowAllowed)) || (null == isOverflowAllowed)){
            beanCache.setIsCacheOverflowAllowed(null);
            updateBeanCache();
        }else{
            beanCache.setIsCacheOverflowAllowed(isOverflowAllowed);
        }
        notifyChange();
    }


    void updateCacheIdleTimeoutInSeconds(String idleTimeoutInSec){
        BeanCache beanCache = getBeanCache();
        if((EMPTY_STRING.equals(idleTimeoutInSec)) || (null == idleTimeoutInSec)){
            beanCache.setCacheIdleTimeoutInSeconds(null);
            updateBeanCache();
        }else{
            beanCache.setCacheIdleTimeoutInSeconds(idleTimeoutInSec);
        }
        notifyChange();
    }


    void updateRemovalTimeoutInSeconds(String removalTimeoutInSeconds){
        BeanCache beanCache = getBeanCache();
        if((EMPTY_STRING.equals(removalTimeoutInSeconds)) || (null == removalTimeoutInSeconds)){
            beanCache.setRemovalTimeoutInSeconds(null);
            updateBeanCache();
        }else{
            beanCache.setRemovalTimeoutInSeconds(removalTimeoutInSeconds);
        }
        notifyChange();
    }


    void updateVictimSelectionPolicy(String victimSelectionPolicy){
        BeanCache beanCache = getBeanCache();
        if((EMPTY_STRING.equals(victimSelectionPolicy)) || (null == victimSelectionPolicy)){
            beanCache.setVictimSelectionPolicy(null);
            updateBeanCache();
        }else{
            beanCache.setVictimSelectionPolicy(victimSelectionPolicy);
        }
        notifyChange();
    }


    private BeanPool getBeanPool(){
        BeanPool beanPool = theBean.getBeanPool();
        if(beanPool == null){
            beanPool = StorageBeanFactory.getDefault().createBeanPool();
            try{
                theBean.setBeanPool(beanPool);
            }catch(java.beans.PropertyVetoException exception){
            }
        }
        return beanPool;
    }


    private BeanCache getBeanCache(){
        BeanCache beanCache = theBean.getBeanCache();
        if(beanCache == null){
            beanCache = StorageBeanFactory.getDefault().createBeanCache();
            try{
                theBean.setBeanCache(beanCache);
            }catch(java.beans.PropertyVetoException exception){
            }
        }
        return beanCache;
    }    


    public void tableChanged(javax.swing.event.TableModelEvent e) {
        notifyChange();
    }


    private String getCustomizerTitle(){
	  String title = bundle.getString("EJB_TITLE");
        return title;
    }        


    private void updateTransportConfig(){
        TransportConfig transportConfig = getTransportConfig();
        if(transportConfig.getIntegrity() != null) return;
        if(transportConfig.getConfidentiality() != null) return;
        if(transportConfig.getEstablishTrustInTarget() != null) return;
        if(transportConfig.getEstablishTrustInClient() != null) return;
        getIorSecurityConfig().setTransportConfig(null);
        updateIorSecurityConfig();
    }


    private void updateAsContext(){
        AsContext asContext = getAsContext();
        if(asContext.getAuthMethod() != null) return;
        if(asContext.getRealm() != null) return;
        if(asContext.getRequired() != null) return;
        getIorSecurityConfig().setAsContext(null);
        updateIorSecurityConfig();
    }


    private void updateSasContext(){
        SasContext sasContext = getSasContext();
        if(sasContext.getCallerPropagation() != null) return;
        getIorSecurityConfig().setSasContext(null);
        updateIorSecurityConfig();
    }


    private void updateIorSecurityConfig(){
        IorSecurityConfig iorSecCnfg = getIorSecurityConfig();
        if(iorSecCnfg.getTransportConfig() != null) return;
        if(iorSecCnfg.getAsContext() != null) return;
        if(iorSecCnfg.getSasContext() != null) return;
        try{
            theBean.setIorSecurityConfig(null);
        }catch(java.beans.PropertyVetoException exception){
        }
    }


    private void updateBeanPool(){
        BeanPool beanPool = getBeanPool();
        if(beanPool.getSteadyPoolSize() != null) return;
        if(beanPool.getResizeQuantity() != null) return;
        if(beanPool.getMaxPoolSize() != null) return;
        if(beanPool.getPoolIdleTimeoutInSeconds() != null) return;
        try{
            theBean.setBeanPool(null);
        }catch(java.beans.PropertyVetoException exception){
        }
    }


    private void updateBeanCache(){
        BeanCache beanCache = getBeanCache();
        if(beanCache.getMaxCacheSize() != null) return;
        if(beanCache.getResizeQuantity() != null) return;
        if(beanCache.getIsCacheOverflowAllowed() != null) return;
        if(beanCache.getCacheIdleTimeoutInSeconds() != null) return;
        if(beanCache.getRemovalTimeoutInSeconds() != null) return;
        if(beanCache.getVictimSelectionPolicy() != null) return;
        try{
            theBean.setBeanCache(null);
        }catch(java.beans.PropertyVetoException exception){
        }
    }


     /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        panel1 = new java.awt.Panel();
        generalPanel = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        jndiNameLabel = new javax.swing.JLabel();
        jndiNameTextField = new javax.swing.JTextField();
        passByRefLabel = new javax.swing.JLabel();
        passByRefComboBox = new javax.swing.JComboBox();
        tabbedPanel = new javax.swing.JTabbedPane();

        setLayout(new java.awt.GridBagLayout());

        setMinimumSize(new java.awt.Dimension(271, 169));
        setPreferredSize(new java.awt.Dimension(271, 169));
        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
        });

        generalPanel.setLayout(new java.awt.GridBagLayout());

        generalPanel.setBorder(new javax.swing.border.EtchedBorder());
        generalPanel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                generalPanelFocusGained(evt);
            }
        });

        nameLabel.setLabelFor(nameTextField);
        nameLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Name_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        generalPanel.add(nameLabel, gridBagConstraints);
        nameLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Name_Acsbl_Name"));
        nameLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Ejb_Name_Acsbl_Desc"));

        nameTextField.setEditable(false);
        nameTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Ejb_Name_Tool_Tip"));
        nameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                nameFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        generalPanel.add(nameTextField, gridBagConstraints);
        nameTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Name_Acsbl_Name"));
        nameTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Ejb_Name_Acsbl_Desc"));

        jndiNameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Jndi_Name").charAt(0));
        jndiNameLabel.setLabelFor(jndiNameTextField);
        jndiNameLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Jndi_Name_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        generalPanel.add(jndiNameLabel, gridBagConstraints);
        jndiNameLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Jndi_Name_Acsbl_Name"));
        jndiNameLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Ejb_Jndi_Name_Acsbl_Desc"));

        jndiNameTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Ejb_Jndi_Name_Tool_Tip"));
        jndiNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jndiNameActionPerformed(evt);
            }
        });
        jndiNameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jndiNameFocusGained(evt);
            }
        });
        jndiNameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jndiNameKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        generalPanel.add(jndiNameTextField, gridBagConstraints);
        jndiNameTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Jndi_Name_Acsbl_Name"));
        jndiNameTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Ejb_Jndi_Name_Acsbl_Desc"));

        passByRefLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Pass_By_Reference").charAt(0));
        passByRefLabel.setLabelFor(passByRefComboBox);
        passByRefLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Pass_By_Reference_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 5, 5);
        generalPanel.add(passByRefLabel, gridBagConstraints);
        passByRefLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Pass_By_Reference_Acsbl_Name"));
        passByRefLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Pass_By_Reference_Acsbl_Desc"));

        passByRefComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "true", "false" }));
        passByRefComboBox.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Pass_By_Reference_Tool_Tip"));
        passByRefComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                passByRefItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        generalPanel.add(passByRefComboBox, gridBagConstraints);
        passByRefComboBox.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Pass_By_Reference_Acsbl_Name"));
        passByRefComboBox.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Pass_By_Reference_Acsbl_Desc"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(generalPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(tabbedPanel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void jndiNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jndiNameActionPerformed
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_jndiNameActionPerformed

    private void jndiNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jndiNameFocusGained
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_jndiNameFocusGained

    private void generalPanelFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_generalPanelFocusGained
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_generalPanelFocusGained

    private void nameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameFocusGained
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_nameFocusGained

    private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_formFocusGained

    private void passByRefItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_passByRefItemStateChanged
        // Add your handling code here:
        updatePassByRef((String)passByRefComboBox.getSelectedItem());
        notifyChange();        
        validateEntries();
    }//GEN-LAST:event_passByRefItemStateChanged

    private void jndiNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jndiNameKeyReleased
		// Add your handling code here:
		updateJndiName(jndiNameTextField.getText());
                notifyChange();
                validateEntries();
    }//GEN-LAST:event_jndiNameKeyReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel generalPanel;
    private javax.swing.JLabel jndiNameLabel;
    private javax.swing.JTextField jndiNameTextField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private java.awt.Panel panel1;
    private javax.swing.JComboBox passByRefComboBox;
    private javax.swing.JLabel passByRefLabel;
    protected javax.swing.JTabbedPane tabbedPanel;
    // End of variables declaration//GEN-END:variables
}
