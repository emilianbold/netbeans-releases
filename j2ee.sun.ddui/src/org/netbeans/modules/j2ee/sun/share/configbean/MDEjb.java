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

package org.netbeans.modules.j2ee.sun.share.configbean;

import java.beans.PropertyVetoException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.exceptions.DDBeanCreateException;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.common.DefaultResourcePrincipal;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.ActivationConfig;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.ActivationConfigProperty;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.MdbConnectionFactory;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.MdbResourceAdapter;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination;


/**
 *
 * @author  vkraemer
 */
public class MDEjb extends BaseEjb {
    
    public static final String __Enabled = "enabled"; //NOI18N
    public static final String __JMSResource = "jms"; //NOI18N
    public static final String __JMSConnectionFactory = "jms_CF"; //NOI18N
    public static final String __JndiName = "jndi-name"; //NOI18N
    public static final String __ResType = "res-type"; //NOI18N

    private static final String MESSAGE_DSTN_NAME = "msg_dstn_name"; //NOI18N
    private static final String MESSAGE_DSTN_TYPE = "msg_dstn_type"; //NOI18N

    private static final String QUEUE = "javax.jms.Queue"; //NOI18N
    private static final String QUEUE_CNTN_FACTORY = "javax.jms.QueueConnectionFactory"; //NOI18N
    private static final String TOPIC = "javax.jms.Topic"; //NOI18N
    private static final String TOPIC_CNTN_FACTORY = "javax.jms.TopicConnectionFactory"; //NOI18N

    
    /** Holds value of property subscriptionName. */
    private String subscriptionName;
    
    /** Holds value of property maxMessageLoad. */
    private String maxMessageLoad;
    
	/** Holds value of property mdbConnectionFactory. */
	private MdbConnectionFactory mdbConnectionFactory;

	/** Holds value of property mdbResourceAdapter. */
	private MdbResourceAdapter mdbResourceAdapter;

    
    /** Creates a new instance of SunONEStatelessEjbDConfigBean */
	public MDEjb() {
	}

    
    /** -----------------------------------------------------------------------
     *  Validation implementation
     */

    // relative xpaths (double as field id's)
    public static final String FIELD_MD_SUBSCRIPTION = "jms-durable-subscription-name";
    public static final String FIELD_MD_MAXMESSAGES = "jms-max-messages-load";
    public static final String FIELD_MD_ADAPTER = "mdb-resource-adapter";
    public static final String FIELD_MD_ADAPTERMID = FIELD_MD_ADAPTER + "/resource-adapter-mid";
    public static final String FIELD_MD_ACDESC = FIELD_MD_ADAPTER + "/activation-config/description";
    public static final String FIELD_MD_CONNFACTORY = "mdb-connection-factory";
    public static final String FIELD_MD_CONNFACTORY_JNDINAME = FIELD_MD_CONNFACTORY + "/jndi-name";
    public static final String FIELD_MD_CONNFACTORY_DRP_NAME = FIELD_MD_CONNFACTORY + "/default-resource-principal/name";
    public static final String FIELD_MD_CONNFACTORY_DRP_PASSWORD = FIELD_MD_CONNFACTORY + "/default-resource-principal/password";
    
    protected void updateValidationFieldList() {
        super.updateValidationFieldList();

        validationFieldList.add(FIELD_MD_SUBSCRIPTION);
        validationFieldList.add(FIELD_MD_MAXMESSAGES);
        validationFieldList.add(FIELD_MD_ADAPTER);
        validationFieldList.add(FIELD_MD_CONNFACTORY);
    }

    public boolean validateField(String fieldId) {
        boolean result = super.validateField(fieldId);
        
        Collection/*ValidationError*/ errors = new ArrayList();

        // !PW use visitor pattern to get rid of switch/if statement for validation
        //     field -- data member mapping.
        //
        // ValidationSupport can return multiple errors for a single field.  We only want
        // to display one error per field, so we'll pick the first error rather than adding
        // them all.  As the user fixes each error, the remainder will display until all of
        // them are handled.  (Hopefully the errors are generated in a nice order, e.g. 
        // check blank first, then content, etc.  If not, we may have to reconsider this.)
        //
        String absoluteFieldXpath = getAbsoluteXpath(fieldId);
        if(fieldId.equals(FIELD_MD_SUBSCRIPTION)) {
            errors.add(executeValidator(ValidationError.PARTITION_EJB_GLOBAL, 
                    subscriptionName, absoluteFieldXpath, bundle.getString("LBL_Jms_Durable_Subscription_Name"))); // NOI18N
        } else if(fieldId.equals(FIELD_MD_MAXMESSAGES)) {
            errors.add(executeValidator(ValidationError.PARTITION_EJB_GLOBAL, 
                    maxMessageLoad, absoluteFieldXpath, bundle.getString("LBL_Jms_Max_Messages_Load"))); // NOI18N
        } else if(fieldId.equals(FIELD_MD_ADAPTER)) {
            if(hasContent(mdbResourceAdapter)) {
                errors.add(executeValidator(ValidationError.PARTITION_EJB_GLOBAL, 
                        mdbResourceAdapter.getResourceAdapterMid(), getAbsoluteXpath(FIELD_MD_ADAPTERMID), bundle.getString("LBL_Resource_Adapter_Mid"))); // NOI18N
                ActivationConfig ac = mdbResourceAdapter.getActivationConfig();
                String value = (ac != null) ? ac.getDescription() : null;
                errors.add(executeValidator(ValidationError.PARTITION_EJB_GLOBAL, 
                        value, getAbsoluteXpath(FIELD_MD_ACDESC), bundle.getString("LBL_Activation_Config_Description"))); // NOI18N
            } else {
                errors.add(ValidationError.getValidationErrorMask(
                        ValidationError.PARTITION_EJB_GLOBAL, getAbsoluteXpath(FIELD_MD_ADAPTERMID)));
                errors.add(ValidationError.getValidationErrorMask(
                        ValidationError.PARTITION_EJB_GLOBAL, getAbsoluteXpath(FIELD_MD_ACDESC)));
            }
        } else if(fieldId.equals(FIELD_MD_CONNFACTORY_JNDINAME)) {
            String value = (mdbConnectionFactory != null) ? mdbConnectionFactory.getJndiName() : null;
            if(hasDRP(mdbConnectionFactory) || Utils.notEmpty(value)) {
                errors.add(executeValidator(ValidationError.PARTITION_EJB_MDBCONNFACTORY, 
                        value, absoluteFieldXpath, bundle.getString("LBL_Jndi_Name"))); // NOI18N
            } else {
                errors.add(ValidationError.getValidationErrorMask(
                        ValidationError.PARTITION_EJB_MDBCONNFACTORY, getAbsoluteXpath(FIELD_MD_CONNFACTORY_JNDINAME)));
            }
        } else if(fieldId.equals(FIELD_MD_CONNFACTORY)) {
            // All the MDB Connection Factory fields have to be validated against each other.
            if(hasContent(mdbConnectionFactory)) {
                errors.add(executeValidator(ValidationError.PARTITION_EJB_MDBCONNFACTORY, 
                        mdbConnectionFactory.getJndiName(), getAbsoluteXpath(FIELD_MD_CONNFACTORY_JNDINAME), 
                        bundle.getString("LBL_Jndi_Name"))); // NOI18N
                DefaultResourcePrincipal drp = mdbConnectionFactory.getDefaultResourcePrincipal();
                if(hasDRP(mdbConnectionFactory)) {
                    errors.add(executeValidator(ValidationError.PARTITION_EJB_MDBCONNFACTORY, 
                            drp.getName(), getAbsoluteXpath(FIELD_MD_CONNFACTORY_DRP_NAME), 
                            bundle.getString("LBL_Name"))); // NOI18N
                    errors.add(executeValidator(ValidationError.PARTITION_EJB_MDBCONNFACTORY, 
                            drp.getPassword(), getAbsoluteXpath(FIELD_MD_CONNFACTORY_DRP_PASSWORD), 
                            bundle.getString("LBL_Password"))); // NOI18N
                } else {
                    errors.add(ValidationError.getValidationErrorMask(
                            ValidationError.PARTITION_EJB_MDBCONNFACTORY, getAbsoluteXpath(FIELD_MD_CONNFACTORY_DRP_NAME)));
                    errors.add(ValidationError.getValidationErrorMask(
                            ValidationError.PARTITION_EJB_MDBCONNFACTORY, getAbsoluteXpath(FIELD_MD_CONNFACTORY_DRP_PASSWORD)));
                }
            } else {
                errors.add(ValidationError.getValidationErrorMask(
                        ValidationError.PARTITION_EJB_MDBCONNFACTORY, getAbsoluteXpath(FIELD_MD_CONNFACTORY_JNDINAME)));
                errors.add(ValidationError.getValidationErrorMask(
                        ValidationError.PARTITION_EJB_MDBCONNFACTORY, getAbsoluteXpath(FIELD_MD_CONNFACTORY_DRP_NAME)));
                errors.add(ValidationError.getValidationErrorMask(
                        ValidationError.PARTITION_EJB_MDBCONNFACTORY, getAbsoluteXpath(FIELD_MD_CONNFACTORY_DRP_PASSWORD)));
            }
        }

        boolean noErrors = true;
        Iterator errorIter = errors.iterator();

        while(errorIter.hasNext()) {
            ValidationError error = (ValidationError) errorIter.next();
            getMessageDB().updateError(error);

            if(Utils.notEmpty(error.getMessage())) {
                noErrors = false;
            }
        }

        // return true if there was no error added
        return noErrors || result;
    }     
    
    /** Getter for property subscriptionName.
     * @return Value of property subscriptionName.
     *
     */
    public String getSubscriptionName() {
        return this.subscriptionName;
    }
   
    /** Setter for property subscriptionName.
     * @param subscriptionName New value of property subscriptionName.
     *
     * @throws PropertyVetoException
     *
     */
    public void setSubscriptionName(String subscriptionName) throws java.beans.PropertyVetoException {
        String oldSubscriptionName = this.subscriptionName;
        getVCS().fireVetoableChange("subscriptionName", oldSubscriptionName, subscriptionName);
        this.subscriptionName = subscriptionName;
        getPCS().firePropertyChange("subscriptionName", oldSubscriptionName, subscriptionName);
    }
    
    /** Getter for property maxMessageLoad.
     * @return Value of property maxMessageLoad.
     *
     */
    public String getMaxMessageLoad() {
        return this.maxMessageLoad;
    }
    
    /** Setter for property maxMessageLoad.
     * @param maxMessageLoad New value of property maxMessageLoad.
     *
     * @throws PropertyVetoException
     *
     */
    public void setMaxMessageLoad(String maxMessageLoad) throws java.beans.PropertyVetoException {
        String oldMaxMessageLoad = this.maxMessageLoad;
        getVCS().fireVetoableChange("maxMessageLoad", oldMaxMessageLoad, maxMessageLoad);
        this.maxMessageLoad = maxMessageLoad;
        getPCS().firePropertyChange("maxMessageLoad", oldMaxMessageLoad, maxMessageLoad);
    }
    
	/* ------------------------------------------------------------------------
	 * Persistence support.  Loads DConfigBeans from previously saved Deployment
	 * plan file.
	 */
	protected class MDEjbSnippet extends BaseEjb.BaseEjbSnippet {
        
        public CommonDDBean getDDSnippet() {
            Ejb ejb = (Ejb) super.getDDSnippet();
            String version = getAppServerVersion().getEjbJarVersionAsString();

            if(subscriptionName != null){
                ejb.setJmsDurableSubscriptionName(subscriptionName);
            }

            if(maxMessageLoad != null){
                ejb.setJmsMaxMessagesLoad(maxMessageLoad);
            }

            MdbConnectionFactory mcf = getMdbConnectionFactory();
            if(hasContent(mcf)) {
                ejb.setMdbConnectionFactory((MdbConnectionFactory) mcf.cloneVersion(version));
            }
            
            MdbResourceAdapter mra = getMdbResourceAdapter();
            if(hasContent(mra)) {
                ejb.setMdbResourceAdapter((MdbResourceAdapter) mra.cloneVersion(version));
            }

            return ejb;
        }

        public boolean hasDDSnippet() {
            if(super.hasDDSnippet()) {
                return true;
            }

            if(Utils.notEmpty(subscriptionName) ||
                    Utils.notEmpty(maxMessageLoad) ||
                    hasContent(getMdbConnectionFactory()) ||
                    hasContent(getMdbResourceAdapter())
                    ) {
                return true;
            }
            
            return false;
        }
        
    }

    private boolean hasContent(MdbConnectionFactory mcf) {
        if(mcf == null) {
            return false;
        }
        
        if(Utils.notEmpty(mcf.getJndiName()) ||
                hasDRP(mcf)) {
            return true;
        }

        return false;
    }
    
    private boolean hasDRP(MdbConnectionFactory mcf) {
        DefaultResourcePrincipal drp = (mcf != null) ? mcf.getDefaultResourcePrincipal() : null;
        if(drp != null && (
                Utils.notEmpty(drp.getName()) ||
                Utils.notEmpty(drp.getPassword()))
                ) {
            return true;
        }
        
        return false;
    }
    
    private boolean hasContent(MdbResourceAdapter mra) {
        if(mra == null) {
            return false;
        }
        
        if(Utils.notEmpty(mra.getResourceAdapterMid())) {
            return true;
        }
        
        ActivationConfig ac = mra.getActivationConfig();
        if(ac != null && (
                Utils.notEmpty(ac.getDescription()) ||
                ac.sizeActivationConfigProperty() > 0)
                ) {
            return true;
        }
        
        return false;
    }
    
    java.util.Collection getSnippets() {
        Collection snippets = new ArrayList();
        snippets.add(new MDEjbSnippet());	
        return snippets;
    }


	protected void loadEjbProperties(Ejb savedEjb) {
		super.loadEjbProperties(savedEjb);
		
        subscriptionName = savedEjb.getJmsDurableSubscriptionName();
        maxMessageLoad = savedEjb.getJmsMaxMessagesLoad();
                
		MdbConnectionFactory mcf = savedEjb.getMdbConnectionFactory();
		if(mcf != null) {
			mdbConnectionFactory = mcf;
		}

		MdbResourceAdapter mra = savedEjb.getMdbResourceAdapter();
		if(mra != null) {
			mdbResourceAdapter = mra;
		}
	}
    
    protected void clearProperties() {
        super.clearProperties();
        StorageBeanFactory beanFactory = getConfig().getStorageFactory();        
        
        subscriptionName = null;
        maxMessageLoad = null;
        mdbConnectionFactory = beanFactory.createMdbConnectionFactory();
        mdbResourceAdapter = beanFactory.createMdbResourceAdapter();
    }
    
//    protected void setDefaultProperties() {
//        super.setDefaultProperties();
//
//        // Message driven beans always have a default JNDI name, which could be
//        // set here.  However, this is instead implemented in BaseEjb.setDefaultProperties()
//        // with help from getDefaultJndiName() and requiresJndiName() which are overridden
//        // in this class to provide the correct defaults (jms/[ejbname] and true).
//    }
    
    // Not really necessary to override this, but do it anyway so the proper name
    // is always available.
    protected String getDefaultJndiName() {
        return "jms/" + getEjbName(); // NOI18N // J2EE recommended jndiName for jms resources.
    }

    protected boolean requiresJndiName() {
        // For JavaEE5 and later spec bean, jndi name is optional, otherwise required.
        return J2EEVersion.J2EE_1_4.compareSpecification(getJ2EEModuleVersion()) >= 0;
    }

    
	/* ------------------------------------------------------------------------
	 * XPath to Factory mapping support
	 */
/*
	private HashMap mdEjbFactoryMap;

	protected Map getXPathToFactoryMap() {
		if(mdEjbFactoryMap == null) {
			mdEjbFactoryMap = (HashMap) super.getXPathToFactoryMap();
			
			// add child DCB's specific to Entity Beans
		}
		
		return mdEjbFactoryMap;
	}
*/

    //Methods called by Customizer
    public void addActivationConfigProperty(ActivationConfigProperty property){
        ActivationConfig activationCfg = getActivationConfig();
        activationCfg.addActivationConfigProperty(property);
    }


    public void removeActivationConfigProperty(ActivationConfigProperty property){
        if(null != mdbResourceAdapter){
            ActivationConfig activationCfg = mdbResourceAdapter.getActivationConfig();
            if(null != activationCfg){
                activationCfg.removeActivationConfigProperty(property);
            }
            if(activationCfg.sizeActivationConfigProperty() < 1){
                activationCfg.setActivationConfigProperty(null);
            }
            if(null == activationCfg.getDescription()){
                mdbResourceAdapter.setActivationConfig(null);
            }
        }
    }


    private ActivationConfig getActivationConfig(){
        MdbResourceAdapter mdbResourceAdapter = getMdbResourceAdapter();
        if(null == mdbResourceAdapter){
            mdbResourceAdapter = getConfig().getStorageFactory().createMdbResourceAdapter();
            try{
                setMdbResourceAdapter(mdbResourceAdapter);
            }catch(java.beans.PropertyVetoException exception){
            }
        }

        ActivationConfig activationCfg = 
            mdbResourceAdapter.getActivationConfig();
        if(null == activationCfg){
            activationCfg = getConfig().getStorageFactory().createActivationConfig();
            mdbResourceAdapter.setActivationConfig(activationCfg);
        }
        return activationCfg;
    }


    /** Getter for property mdbConnectionFactory.
     * @return Value of property mdbConnectionFactory.
     *
     */
    public MdbConnectionFactory getMdbConnectionFactory() {
        return this.mdbConnectionFactory;
    }    

    /** Setter for property mdbConnectionFactory.
     * @param mdbConnectionFactory New value of property mdbConnectionFactory.
     *
     * @throws PropertyVetoException
     *
     */
    public void setMdbConnectionFactory(MdbConnectionFactory mdbConnectionFactory) throws java.beans.PropertyVetoException {
        MdbConnectionFactory oldMdbConnectionFactory = this.mdbConnectionFactory;
        getVCS().fireVetoableChange("mdbConnectionFactory", oldMdbConnectionFactory, mdbConnectionFactory);
        this.mdbConnectionFactory = mdbConnectionFactory;
        getPCS().firePropertyChange("mdbConnectionFactory", oldMdbConnectionFactory, mdbConnectionFactory);
    }
    
    /** Getter for property mdbResourceAdapter.
     * @return Value of property mdbResourceAdapter.
     *
     */
    public MdbResourceAdapter getMdbResourceAdapter() {
        return this.mdbResourceAdapter;
    }
    
    /** Setter for property mdbResourceAdapter.
     * @param mdbResourceAdapter New value of property mdbResourceAdapter.
     *
     * @throws PropertyVetoException
     *
     */
    public void setMdbResourceAdapter(MdbResourceAdapter mdbResourceAdapter) throws java.beans.PropertyVetoException {
        MdbResourceAdapter oldMdbResourceAdapter = this.mdbResourceAdapter;
        getVCS().fireVetoableChange("mdbResourceAdapter", oldMdbResourceAdapter, mdbResourceAdapter);
        this.mdbResourceAdapter = mdbResourceAdapter;
        getPCS().firePropertyChange("mdbResourceAdapter", oldMdbResourceAdapter, mdbResourceAdapter);
    }


    public String getHelpId() {
        return "AS_CFG_MDEjb";                                          //NOI18N
    }

    private String getMessageDestinationInfo(String parameter){
        String msgDstnInfo = null; 
        DDBeanRoot ejbJarRootDD = getDDBean().getRoot();

        if(ejbJarRootDD != null) {
            DDBean[] ejbNameDD = 
                ejbJarRootDD.getChildBean("ejb-jar/enterprise-beans/message-driven/ejb-name"); //NOI18N
            // First, find the ejb-name that corresponds to this bean
            for(int i = 0; i < ejbNameDD.length; i++) {
                //System.out.println("getEjbName(): " + getEjbName());  //NOI18N
                //System.out.println("ejbNameDD[i].getText(): " + ejbNameDD[0].getText());  //NOI18N
                if(getEjbName().equals(ejbNameDD[i].getText())) {
                    if(parameter.equals(MESSAGE_DSTN_NAME)){
                        DDBean[] msgDstnLink = 
                            ejbNameDD[i].getChildBean("../message-destination-link"); //NOI18N
                       if(msgDstnLink.length > 0){
                           msgDstnInfo = msgDstnLink[0].getText();  
                        }
                    } else {
                        if(parameter.equals(MESSAGE_DSTN_TYPE)){
                            DDBean[] msgDstnType = 
                                ejbNameDD[i].getChildBean("../message-destination-type"); //NOI18N
                           if(msgDstnType.length > 0){
                               msgDstnInfo = msgDstnType[0].getText();  
                            }
                        }
                    }
                }
            }
        }
        return msgDstnInfo;
    }
}
