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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.XpathEvent;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.AsContext;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SasContext;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.TransportConfig;

import org.openide.ErrorManager;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.BeanCache;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.BeanPool;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.IorSecurityConfig;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Principal;


/** This is the base class for all Ejb related config beans.  It should have
 * properties to deal with all the "shared" deployment descriptor elements.
 * @author vkraemer
 */
public abstract class BaseEjb extends Base {
	
	/** property event names
	 */
	public static final String EJB_NAME = "ejbName"; // NOI18N	

    /** Holds value of property ejbNameDD */
	private DDBean ejbNameDD;

	/** Holds value of property jndiName. */
	private String jndiName;

	/** Holds value of property passByReference. */
	private String passByReference;

	/** Holds value of property principalName. */
	private String principalName;

	/** Holds value of property iorSecurityConfig. */
	private IorSecurityConfig iorSecurityConfig;

	/** Holds value of property beanPool. */
	private BeanPool beanPool;    

	/** Holds value of property beanCache. */
	private BeanCache beanCache;

	/** Creates a new instance of SunONEBaseEjbDConfigBean */
	public BaseEjb() {
		setDescriptorElement(bundle.getString("BDN_BaseEjb"));	// NOI18N
	}

	protected void init(DDBean dDBean, Base parent) throws ConfigurationException {
		super.init(dDBean,parent);

		ejbNameDD = getNameDD("ejb-name"); // NOI18N
        
        updateNamedBeanCache(EnterpriseBeans.EJB);
        
		loadFromPlanFile(getConfig());
	}
    
    protected String translateXpath(String ddXpath) {
        // Translate ejb-jar style xpath (.../enterprise-beans/[session|message-driven|entity]) to
        // sun-ejb-jar style (.../enterprise-beans/ejb).
        if(ddXpath.startsWith("/ejb-jar") && ddXpath.lastIndexOf('/') > 8) {
            return "/sun-ejb-jar" + ddXpath.substring(8, ddXpath.lastIndexOf('/') + 1) + "ejb";
        } else {
            // If we get here, it's a bug, but added for safety.  Will cause validation to fail
            // for whatever fields this bean handles.
            return ddXpath;
        }
    }
    
    /** -----------------------------------------------------------------------
     *  Validation implementation
     */

    // relative xpaths (double as field id's)
    public static final String FIELD_JNDI_NAME = "jndi-name"; // NOI18N
    public static final String FIELD_PASS_BY_REFERENCE = "pass-by-reference"; // NOI18N

    public static final String FIELD_IOR_TRANSPORT = "ior-security-config/transport-config"; // NOI18N
    public static final String FIELD_IOR_TRANSPORT_INTEGRITY = FIELD_IOR_TRANSPORT + "/integrity"; // NOI18N
    public static final String FIELD_IOR_TRANSPORT_CONFIDENTIALITY = FIELD_IOR_TRANSPORT + "/confidentiality"; // NOI18N
    public static final String FIELD_IOR_TRANSPORT_EST_TRUST_TARGET = FIELD_IOR_TRANSPORT + "/establish-trust-in-target"; // NOI18N
    public static final String FIELD_IOR_TRANSPORT_EST_TRUST_CLIENT = FIELD_IOR_TRANSPORT + "/establish-trust-in-client"; // NOI18N

    public static final String FIELD_IOR_ASCONTEXT = "ior-security-config/as-context"; // NOI18N
    public static final String FIELD_IOR_ASCONTEXT_REQUIRED = FIELD_IOR_ASCONTEXT + "/required"; // NOI18N
    public static final String FIELD_IOR_ASCONTEXT_AUTH_METHOD = FIELD_IOR_ASCONTEXT + "/auth-method"; // NOI18N
    public static final String FIELD_IOR_ASCONTEXT_REALM = FIELD_IOR_ASCONTEXT + "/realm"; // NOI18N

    public static final String FIELD_IOR_SAS = "ior-security-config/sas-context"; // NOI18N
    public static final String FIELD_IOR_SAS_CALLER_PROP = FIELD_IOR_SAS + "/caller-propagation"; // NOI18N
    
    public static final String FIELD_BEANPOOL = "bean-pool";
    public static final String FIELD_BEANPOOL_STEADYPOOLSIZE = FIELD_BEANPOOL + "/steady-pool-size";
    public static final String FIELD_BEANPOOL_RESIZEQUANTITY = FIELD_BEANPOOL + "/resize-quantity";
    public static final String FIELD_BEANPOOL_IDLETIMEOUT = FIELD_BEANPOOL + "/pool-idle-timeout-in-seconds";
    public static final String FIELD_BEANPOOL_MAXPOOLSIZE = FIELD_BEANPOOL + "/max-pool-size";

    public static final String FIELD_BEANCACHE = "bean-cache";
    public static final String FIELD_BEANCACHE_MAXSIZE = FIELD_BEANCACHE + "/max-cache-size";
    public static final String FIELD_BEANCACHE_OVERFLOWALLOWED = FIELD_BEANCACHE + "/is-cache-overflow-allowed";
    public static final String FIELD_BEANCACHE_VICTIMPOLICY = FIELD_BEANCACHE + "/victim-selection-policy";
    public static final String FIELD_BEANCACHE_REMOVALTIMEOUT = FIELD_BEANCACHE + "/removal-timeout-in-seconds";
    public static final String FIELD_BEANCACHE_IDLETIMEOUT = FIELD_BEANCACHE + "/cache-idle-timeout-in-seconds";
    public static final String FIELD_BEANCACHE_RESIZEQUANTITY = FIELD_BEANCACHE + "/resize-quantity";
    
    protected void updateValidationFieldList() {
        super.updateValidationFieldList();

        validationFieldList.add(FIELD_JNDI_NAME);
        validationFieldList.add(FIELD_PASS_BY_REFERENCE);
        validationFieldList.add(FIELD_IOR_TRANSPORT);
        validationFieldList.add(FIELD_IOR_ASCONTEXT);
        validationFieldList.add(FIELD_IOR_SAS);
        validationFieldList.add(FIELD_BEANPOOL_STEADYPOOLSIZE);
        validationFieldList.add(FIELD_BEANPOOL_RESIZEQUANTITY);
        validationFieldList.add(FIELD_BEANPOOL_IDLETIMEOUT);
        validationFieldList.add(FIELD_BEANPOOL_MAXPOOLSIZE);
        validationFieldList.add(FIELD_BEANCACHE_MAXSIZE);
        validationFieldList.add(FIELD_BEANCACHE_OVERFLOWALLOWED);
        validationFieldList.add(FIELD_BEANCACHE_VICTIMPOLICY);
        validationFieldList.add(FIELD_BEANCACHE_REMOVALTIMEOUT);
        validationFieldList.add(FIELD_BEANCACHE_IDLETIMEOUT);
        validationFieldList.add(FIELD_BEANCACHE_RESIZEQUANTITY);
    }

    public boolean validateField(String fieldId) {
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
        if(fieldId.equals(FIELD_JNDI_NAME)) {
            errors.add(executeValidator(ValidationError.PARTITION_EJB_GLOBAL, 
                    jndiName, absoluteFieldXpath, bundle.getString("LBL_Jndi_Name"))); // NOI18N
        } else if(fieldId.equals(FIELD_PASS_BY_REFERENCE)) {
            errors.add(executeValidator(ValidationError.PARTITION_EJB_GLOBAL, 
                    passByReference, absoluteFieldXpath, bundle.getString("LBL_Pass_By_Reference"))); // NOI18N
        } else if(fieldId.equals(FIELD_IOR_TRANSPORT)) {
            // All transport-config fields have to be validated against each other.
            if(hasTransportConfig(iorSecurityConfig)) {
                TransportConfig tc = iorSecurityConfig.getTransportConfig();
                errors.add(executeValidator(ValidationError.PARTITION_EJB_IORSECURITY, 
                        tc.getIntegrity(), getAbsoluteXpath(FIELD_IOR_TRANSPORT_INTEGRITY), 
                        bundle.getString("LBL_Integrity"))); // NOI18N
                errors.add(executeValidator(ValidationError.PARTITION_EJB_IORSECURITY, 
                        tc.getConfidentiality(), getAbsoluteXpath(FIELD_IOR_TRANSPORT_CONFIDENTIALITY), 
                        bundle.getString("LBL_Confidentiality"))); // NOI18N
                errors.add(executeValidator(ValidationError.PARTITION_EJB_IORSECURITY, 
                        tc.getEstablishTrustInTarget(), getAbsoluteXpath(FIELD_IOR_TRANSPORT_EST_TRUST_TARGET), 
                        bundle.getString("LBL_Establish_Trust_In_Target"))); // NOI18N
                errors.add(executeValidator(ValidationError.PARTITION_EJB_IORSECURITY, 
                        tc.getEstablishTrustInClient(), getAbsoluteXpath(FIELD_IOR_TRANSPORT_EST_TRUST_CLIENT), 
                        bundle.getString("LBL_Establish_Trust_In_Client"))); // NOI18N
            } else {
                errors.add(ValidationError.getValidationErrorMask(
                        ValidationError.PARTITION_EJB_IORSECURITY, getAbsoluteXpath(FIELD_IOR_TRANSPORT_INTEGRITY)));
                errors.add(ValidationError.getValidationErrorMask(
                        ValidationError.PARTITION_EJB_IORSECURITY, getAbsoluteXpath(FIELD_IOR_TRANSPORT_CONFIDENTIALITY)));
                errors.add(ValidationError.getValidationErrorMask(
                        ValidationError.PARTITION_EJB_IORSECURITY, getAbsoluteXpath(FIELD_IOR_TRANSPORT_EST_TRUST_TARGET)));
                errors.add(ValidationError.getValidationErrorMask(
                        ValidationError.PARTITION_EJB_IORSECURITY, getAbsoluteXpath(FIELD_IOR_TRANSPORT_EST_TRUST_CLIENT)));
            }
        } else if(fieldId.equals(FIELD_IOR_ASCONTEXT)) {
            // All as-context fields have to be validated against each other.
            if(hasAsContext(iorSecurityConfig)) {
                AsContext ac = iorSecurityConfig.getAsContext();
                errors.add(executeValidator(ValidationError.PARTITION_EJB_IORSECURITY, 
                        ac.getRequired(), getAbsoluteXpath(FIELD_IOR_ASCONTEXT_REQUIRED), 
                        bundle.getString("LBL_Required"))); // NOI18N
                errors.add(executeValidator(ValidationError.PARTITION_EJB_IORSECURITY, 
                        ac.getAuthMethod(), getAbsoluteXpath(FIELD_IOR_ASCONTEXT_AUTH_METHOD), 
                        bundle.getString("LBL_Auth_Method"))); // NOI18N
                errors.add(executeValidator(ValidationError.PARTITION_EJB_IORSECURITY, 
                        ac.getRealm(), getAbsoluteXpath(FIELD_IOR_ASCONTEXT_REALM), 
                        bundle.getString("LBL_Realm"))); // NOI18N
            } else {
                errors.add(ValidationError.getValidationErrorMask(
                        ValidationError.PARTITION_EJB_IORSECURITY, getAbsoluteXpath(FIELD_IOR_ASCONTEXT_REQUIRED)));
                errors.add(ValidationError.getValidationErrorMask(
                        ValidationError.PARTITION_EJB_IORSECURITY, getAbsoluteXpath(FIELD_IOR_ASCONTEXT_AUTH_METHOD)));
                errors.add(ValidationError.getValidationErrorMask(
                        ValidationError.PARTITION_EJB_IORSECURITY, getAbsoluteXpath(FIELD_IOR_ASCONTEXT_REALM)));
            }
        } else if(fieldId.equals(FIELD_IOR_SAS)) {
            // All sas-context fields have to be validated against each other.  (Only one for now).
            if(hasSasContext(iorSecurityConfig)) {
                SasContext sac = iorSecurityConfig.getSasContext();
                errors.add(executeValidator(ValidationError.PARTITION_EJB_IORSECURITY, 
                        sac.getCallerPropagation(), getAbsoluteXpath(FIELD_IOR_SAS_CALLER_PROP), 
                        bundle.getString("LBL_Caller_Propagation"))); // NOI18N
            } else {
                errors.add(ValidationError.getValidationErrorMask(
                        ValidationError.PARTITION_EJB_IORSECURITY, getAbsoluteXpath(FIELD_IOR_SAS_CALLER_PROP)));
            }
        } else if(fieldId.startsWith(FIELD_BEANPOOL)) {
            // Bean pool fields validate separately
            if(fieldId.equals(FIELD_BEANPOOL_STEADYPOOLSIZE)) {
                String value = (beanPool != null) ? beanPool.getSteadyPoolSize() : null;
                errors.add(executeValidator(ValidationError.PARTITION_EJB_BEANPOOL, value, 
                        absoluteFieldXpath, bundle.getString("LBL_Steady_Pool_Size"))); // NOI18N
            } else if(fieldId.equals(FIELD_BEANPOOL_RESIZEQUANTITY)) {
                String value = (beanPool != null) ? beanPool.getResizeQuantity() : null;
                errors.add(executeValidator(ValidationError.PARTITION_EJB_BEANPOOL, value, 
                        absoluteFieldXpath, bundle.getString("LBL_Resize_Quantity"))); // NOI18N
            } else if(fieldId.equals(FIELD_BEANPOOL_IDLETIMEOUT)) {
                String value = (beanPool != null) ? beanPool.getPoolIdleTimeoutInSeconds() : null;
                errors.add(executeValidator(ValidationError.PARTITION_EJB_BEANPOOL, value, 
                        absoluteFieldXpath, bundle.getString("LBL_Pool_Idle_Timeout_In_Seconds"))); // NOI18N
            } else if(fieldId.equals(FIELD_BEANPOOL_MAXPOOLSIZE)) {
                String value = (beanPool != null) ? beanPool.getMaxPoolSize() : null;
                errors.add(executeValidator(ValidationError.PARTITION_EJB_BEANPOOL, value, 
                        absoluteFieldXpath, bundle.getString("LBL_Max_Pool_Size"))); // NOI18N
            }
        } else if(fieldId.startsWith(FIELD_BEANCACHE)) {
            if(fieldId.equals(FIELD_BEANCACHE_MAXSIZE)) {
                String value = (beanCache != null) ? beanCache.getMaxCacheSize() : null;
                errors.add(executeValidator(ValidationError.PARTITION_EJB_BEANCACHE, value, 
                        absoluteFieldXpath, bundle.getString("LBL_Max_Cache_Size"))); // NOI18N
            } else if(fieldId.equals(FIELD_BEANCACHE_OVERFLOWALLOWED)) {
                String value = (beanCache != null) ? beanCache.getIsCacheOverflowAllowed() : null;
                errors.add(executeValidator(ValidationError.PARTITION_EJB_BEANCACHE, value, 
                        absoluteFieldXpath, bundle.getString("LBL_Is_Cache_Overflow_Allowed"))); // NOI18N
             } else if(fieldId.equals(FIELD_BEANCACHE_VICTIMPOLICY)) {
                String value = (beanCache != null) ? beanCache.getVictimSelectionPolicy() : null;
                errors.add(executeValidator(ValidationError.PARTITION_EJB_BEANCACHE, value, 
                        absoluteFieldXpath, bundle.getString("LBL_Victim_Selection_Policy"))); // NOI18N
            } else if(fieldId.equals(FIELD_BEANCACHE_REMOVALTIMEOUT)) {
                String value = (beanCache != null) ? beanCache.getRemovalTimeoutInSeconds() : null;
                errors.add(executeValidator(ValidationError.PARTITION_EJB_BEANCACHE, value, 
                        absoluteFieldXpath, bundle.getString("LBL_Removal_Timeout_In_Seconds"))); // NOI18N
            } else if(fieldId.equals(FIELD_BEANCACHE_IDLETIMEOUT)) {
                String value = (beanCache != null) ? beanCache.getCacheIdleTimeoutInSeconds() : null;
                errors.add(executeValidator(ValidationError.PARTITION_EJB_BEANCACHE, value, 
                        absoluteFieldXpath, bundle.getString("LBL_Cache_Idle_Timeout_In_Seconds"))); // NOI18N
            } else if(fieldId.equals(FIELD_BEANCACHE_RESIZEQUANTITY)) {
                String value = (beanCache != null) ? beanCache.getResizeQuantity() : null;
                errors.add(executeValidator(ValidationError.PARTITION_EJB_BEANCACHE, value, 
                        absoluteFieldXpath, bundle.getString("LBL_Resize_Quantity"))); // NOI18N
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
        return noErrors;
    }

    protected ValidationError executeValidator(ValidationError.Partition partition, String propertyValue, 
            String fieldXpath, String fieldLabel) {
        ValidationError error = null;
        Collection messages = validationSupport.validate(propertyValue, fieldXpath, fieldLabel);

        if(messages != null && messages.size() > 0) {
            error = ValidationError.getValidationError(partition, fieldXpath, (String) (messages.iterator().next()));
        } else {
            error = ValidationError.getValidationErrorMask(partition, fieldXpath);
        }

        return error;
    }    
	
	protected String getComponentName() {
		return getEjbName();
	}
	
	/** The DDBean (or one of it's children) that this DConfigBean is bound to
	 *  has changed.
	 *
	 * @param xpathEvent
	 */    
	public void notifyDDChange(XpathEvent xpathEvent) {
		super.notifyDDChange(xpathEvent);

		if(ejbNameDD == xpathEvent.getBean()) {
			// name changed...
			getPCS().firePropertyChange(EJB_NAME, "", getEjbName());
			getPCS().firePropertyChange(DISPLAY_NAME, "", getDisplayName());

            updateNamedBeanCache(EnterpriseBeans.EJB);
		}
	}
	
	/* ------------------------------------------------------------------------
	 * Persistence support.  Loads DConfigBeans from previously saved Deployment
	 * plan file.
	 */
	protected class BaseEjbSnippet extends DefaultSnippet {
        
		public CommonDDBean getDDSnippet() {
			Ejb ejb = getConfig().getStorageFactory().createEjb();
            String version = getAppServerVersion().getEjbJarVersionAsString();
            
            ejb.setEjbName(getEjbName());

			if(Utils.notEmpty(jndiName)) {
				ejb.setJndiName(getJndiName());
			}

			if(Utils.notEmpty(passByReference)) {
				ejb.setPassByReference(passByReference);
			}

			if(Utils.notEmpty(principalName)) {
				Principal principal = ejb.newPrincipal();
				principal.setName(principalName);
				ejb.setPrincipal(principal);
			}

			IorSecurityConfig iorSecConf = getIorSecurityConfig();
            if(hasContent(iorSecConf)) {
                ejb.setIorSecurityConfig((IorSecurityConfig) iorSecConf.cloneVersion(version));
			}

			BeanPool beanPool = getBeanPool();
			if(hasContent(beanPool)) {
                ejb.setBeanPool((BeanPool) beanPool.cloneVersion(version));
			}

			BeanCache beanCache = getBeanCache();
			if(hasContent(beanCache)) {
				ejb.setBeanCache((BeanCache) beanCache.cloneVersion(version));
			}
            
            /* IZ 84549, etc - add remaining saved named beans here.  All entries that are represented
             * by real DConfigBeans should have been removed by now. */
            restoreAllNamedBeans(ejb, version);

			return ejb;
		}

		public String getPropertyName() {
			return EnterpriseBeans.EJB;
		}

		public boolean hasDDSnippet() {
            if(Utils.notEmpty(jndiName)) {
                return true;
            }

            if(Utils.notEmpty(passByReference)) {
                return true;
            }

            if(Utils.notEmpty(principalName)) {
                return true;
            }

            if(hasContent(getIorSecurityConfig())) {
                return true;
            }

            if(hasContent(getBeanPool())) {
                return true;
            }

            if(hasContent(getBeanCache())) {
                return true;
            }

            //return snippet in case of any child DConfigBeans.
            Collection childList = getChildren();
            if(childList.size() > 0){
                return true;
            }

            return false;
		}
        
        private boolean hasContent(IorSecurityConfig isc) {
            return hasTransportConfig(isc) ||
                    hasAsContext(isc) ||
                    hasSasContext(isc);
        }
        
        private boolean hasContent(BeanPool bp) {
            return hasBeanPool(bp);
        }
        
        private boolean hasContent(BeanCache bc) {
            return hasBeanCache(bc);
        }
	}

    private boolean hasTransportConfig(IorSecurityConfig isc) {
        TransportConfig tc = (isc != null) ? isc.getTransportConfig() : null;
        if(tc != null && (
                Utils.notEmpty(tc.getConfidentiality()) ||
                Utils.notEmpty(tc.getIntegrity()) ||
                Utils.notEmpty(tc.getEstablishTrustInTarget()) ||
                Utils.notEmpty(tc.getEstablishTrustInClient()))
                ) {
            return true;
        }
        
        return false;
    }
    
    private boolean hasAsContext(IorSecurityConfig isc) {
        AsContext asc = (isc != null) ? isc.getAsContext() : null;
        if(asc != null && (
                Utils.notEmpty(asc.getRequired()) ||
                Utils.notEmpty(asc.getAuthMethod()) ||
                Utils.notEmpty(asc.getRealm()))
                ) {
            return true;
        }
        
        return false;
    }

    private boolean hasSasContext(IorSecurityConfig isc) {
        SasContext sasc = (isc != null) ? isc.getSasContext() : null;
        if(sasc != null && Utils.notEmpty(sasc.getCallerPropagation())) {
            return true;
        }

        return false;
    }

    private boolean hasBeanPool(BeanPool bp) {
        if(bp != null && (
                Utils.notEmpty(bp.getMaxPoolSize()) ||
                Utils.notEmpty(bp.getMaxWaitTimeInMillis()) ||
                Utils.notEmpty(bp.getPoolIdleTimeoutInSeconds()) ||
                Utils.notEmpty(bp.getResizeQuantity()) ||
                Utils.notEmpty(bp.getSteadyPoolSize()))
                ) {
            return true;
        }

        return false;
    }

    private boolean hasBeanCache(BeanCache bc) {
        if(bc != null && (
                Utils.notEmpty(bc.getCacheIdleTimeoutInSeconds()) ||
                Utils.notEmpty(bc.getIsCacheOverflowAllowed()) ||
                Utils.notEmpty(bc.getMaxCacheSize()) ||
                Utils.notEmpty(bc.getRemovalTimeoutInSeconds()) ||
                Utils.notEmpty(bc.getResizeQuantity()) ||
                Utils.notEmpty(bc.getVictimSelectionPolicy()))
                ) {
            return true;
        }

        return false;
    }
    
/*
	public class EjbFinder implements ConfigFinder {
		private String beanName;

		public EjbFinder(String beanName) {
			this.beanName = beanName;
		}

		public Object find(Object obj) {
			Ejb retVal = null;			
			SunEjbJar root = (SunEjbJar) obj;
//			String[] attrs = root.findAttributeValue("ejb-name", beanName);
			String[] props = root.findPropertyValue("ejb-name", beanName);
			for (int i = 0; i < props.length; i++) {
				CommonDDBean candidate = root.graphManager().getPropertyParent(props[i]);
				if (candidate instanceof Ejb) {
					retVal = (Ejb) candidate;
				}
			}
//			String[] values = root.findValue(beanName);
			return retVal;
		}
	}
 */
	private static class EjbFinder extends NameBasedFinder {
		public EjbFinder(String beanName) {
			super(Ejb.EJB_NAME, beanName, Ejb.class);
		}
	}	
	
	boolean loadFromPlanFile(SunONEDeploymentConfiguration config) {
		String uriText = getUriText();

		Ejb ejb = (Ejb) config.getBeans(uriText, constructFileName(), getParser(), 
			new EjbFinder(getEjbName()));
            
        clearProperties();
        
		if(null != ejb) {
			loadEjbProperties(ejb);
            
            // IZ 84549, etc - cache the data for all named beans.
            saveAllNamedBeans(ejb);
		} else {
            setDefaultProperties();
        }
		
		return (ejb != null);
	}
	
	protected void loadEjbProperties(Ejb savedEjb) {
		String jn = savedEjb.getJndiName();
        if(jn != null) {
            jndiName = jn.trim();
        }
        
        String pbr = savedEjb.getPassByReference();
        if(pbr != null) {
            passByReference = pbr.trim();
        }

		Principal principal = savedEjb.getPrincipal();
		if(principal != null) {
            String pn = principal.getName();
            if(pn != null) {
                principalName = pn;
            }
		}

		IorSecurityConfig isc = savedEjb.getIorSecurityConfig();
		if(isc != null){
			iorSecurityConfig = (IorSecurityConfig) isc.clone();
		}

		BeanPool bp = savedEjb.getBeanPool();
		if(bp != null) {
			beanPool = (BeanPool) bp.clone();
		}

		BeanCache bc = savedEjb.getBeanCache();
		if(bc != null){
			beanCache = (BeanCache) bc.clone();
		}
	}
    
    protected void clearProperties() {
        StorageBeanFactory beanFactory = getConfig().getStorageFactory();        
        
        jndiName = null;
        passByReference = null;
        principalName = null;
        iorSecurityConfig = beanFactory.createIorSecurityConfig();
        beanPool = beanFactory.createBeanPool();
        beanCache = beanFactory.createBeanCache();
    }

	protected void setDefaultProperties() {
        // Default behavior - remote interface = has jndi name.
        // MDB overrides this to always set the JNDI name.
        if(requiresJndiName()) {
            jndiName = getDefaultJndiName();
            getConfig().getMasterDCBRoot().setDirty();
        }
	}
    
    protected String getDefaultJndiName() {
        return "ejb/" + getEjbName(); // NOI18N // J2EE recommended jndiName
    }
    
    protected boolean requiresJndiName() {
        // For JavaEE5 and later spec bean, jndi name is optional.
        boolean needsJndi = super.requiresJndiName();

        if(needsJndi) {
            // For J2EE 1.4 and previous beans, jndi name is only required for beans with
            // remote interfaces.  Note this does not apply message driven beans and
            // MDEjb.java overrides this method with logic correct to that bean type.
            DDBean [] remoteDDBeans = getDDBean().getChildBean("remote"); // NOI18N
            if(!(remoteDDBeans.length > 0 && remoteDDBeans[0] != null)) {
                // remote interface is not present, return false.
                needsJndi = false;
            }
        }
        
        return needsJndi;
    }
    
    private static Collection ejbBeanSpecs = new ArrayList();
    
    static {
        ejbBeanSpecs.addAll(getCommonNamedBeanSpecs());
    }
    
    protected Collection getNamedBeanSpecs() {
        return ejbBeanSpecs;
    }    

	/* ------------------------------------------------------------------------
	 * XPath to Factory mapping support
	 */
	private HashMap baseEjbFactoryMap;

	/** Retrieve the XPathToFactory map common to all EJB baseed DConfigBean.
	 *  So far, this is:
	 *
	 *     EjbRef
	 *     ResourceRef
	 *     ResourceEnvRef
	 *     ServiceRef
	 *
	 * @return
	 */
	protected java.util.Map getXPathToFactoryMap() {
		if(baseEjbFactoryMap == null) {
			baseEjbFactoryMap = new HashMap(17);

			baseEjbFactoryMap.put("ejb-ref", new DCBGenericFactory(EjbRef.class));					// NOI18N
			baseEjbFactoryMap.put("resource-ref", new DCBGenericFactory(ResourceRef.class));		// NOI18N
			baseEjbFactoryMap.put("resource-env-ref", new DCBGenericFactory(ResourceEnvRef.class));	// NOI18N
			
            J2EEBaseVersion moduleVersion = getJ2EEModuleVersion();
			if(moduleVersion.compareTo(EjbJarVersion.EJBJAR_2_1) >= 0) {
				baseEjbFactoryMap.put("service-ref", new DCBGenericFactory(ServiceRef.class));		// NOI18N
                
                if(moduleVersion.compareTo(EjbJarVersion.EJBJAR_3_0) >= 0) {
                    baseEjbFactoryMap.put("message-destination-ref", new DCBGenericFactory(MessageDestinationRef.class));// NOI18N
                }
			}
		}
		return baseEjbFactoryMap;
	}		

	/* ------------------------------------------------------------------------
	 * Property support -- methods to manipulate the properties maintained by
	 * this bean.
	 */

	/** Get /sun-ejb-jar/enterprise-beans/ejb/ejb-name element value.
	 * @return Value of element /sun-ejb-jar/enterprise-beans/ejb/ejb-name
	 */
	public String getEjbName() {
		return cleanDDBeanText(ejbNameDD);
	}

	/** Get /sun-ejb-jar/enterprise-beans/ejb/jndi-name element value.
	 * @return Value of element /sun-ejb-jar/enterprise-beans/ejb/jndi-name
	 */
	public String getJndiName() {
			return this.jndiName;
	}

	/** Set /sun-ejb-jar/enterprise-beans/ejb/jndi-name element value.
	 * @param jndiName New value of property jndiName.
	 * @throws PropertyVetoException In cases where the jndi name is illegal
	 */
	public void setJndiName(String jndiName) throws java.beans.PropertyVetoException {
			String oldJndiName = this.jndiName;
			getVCS().fireVetoableChange("jndiName", oldJndiName, jndiName);
			this.jndiName = jndiName;
			getPCS().firePropertyChange("jndiName", oldJndiName, jndiName);
	}

	/** Get /sun-ejb-jar/enterprise-beans/ejb/pass-by-reference element value
	 * @return Value /sun-ejb-jar/enterprise-beans/ejb/pass-by-reference.
	 */
	public String getPassByReference() {
			return this.passByReference;
	}

	/** Setter for property passByReference.
	 * @param passByReference New value of property passByReference.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setPassByReference(String passByReference) throws java.beans.PropertyVetoException {
			String oldPassByReference = this.passByReference;
			getVCS().fireVetoableChange("passByReference", oldPassByReference, passByReference);
			this.passByReference = passByReference;
			getPCS().firePropertyChange("passByReference", oldPassByReference, passByReference);
	}

	/** Getter for property principalName.
	 * @return Value of property principalName.
	 *
	 */
	public String getPrincipalName() {
			return this.principalName;
	}

	/** Setter for property principalName.
	 * @param principalName New value of property principalName.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setPrincipalName(String principalName) throws java.beans.PropertyVetoException {
			String oldPrincipalName = this.principalName;
			getVCS().fireVetoableChange("principalName", oldPrincipalName, principalName);
			this.principalName = principalName;
			getPCS().firePropertyChange("principalName", oldPrincipalName, principalName);
	}

	/** Getter for property iorSecurityConfig.
	 * @return Value of property iorSecurityConfig.
	 *
	 */
	public IorSecurityConfig getIorSecurityConfig() {
		return this.iorSecurityConfig;
	}

	/** Setter for property iorSecurityConfig.
	 * @param iorSecurityConfig New value of property iorSecurityConfig.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setIorSecurityConfig(IorSecurityConfig iorSecurityConfig) throws java.beans.PropertyVetoException {
		IorSecurityConfig oldIorSecurityConfig = this.iorSecurityConfig;
		getVCS().fireVetoableChange("iorSecurityConfig", oldIorSecurityConfig, iorSecurityConfig);
		this.iorSecurityConfig = iorSecurityConfig;
		getPCS().firePropertyChange("iorSecurityConfig", oldIorSecurityConfig, iorSecurityConfig);
	}

	/** Getter for property beanPool.
	 * @return Value of property beanPool.
	 *
	 */
	public BeanPool getBeanPool() {
		return this.beanPool;
	}

	/** Setter for property beanPool.
	 * @param beanPool New value of property beanPool.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setBeanPool(BeanPool beanPool) throws java.beans.PropertyVetoException {
		BeanPool oldBeanPool = this.beanPool;
		getVCS().fireVetoableChange("beanPool", oldBeanPool, beanPool);
		this.beanPool = beanPool;
		getPCS().firePropertyChange("beanPool", oldBeanPool, beanPool);
	}

	/** Getter for property beanCache.
	 * @return Value of property beanCache.
	 *
	 */
	public BeanCache getBeanCache() {
		return this.beanCache;
	}

	/** Setter for property beanCache.
	 * @param beanCache New value of property beanCache.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setBeanCache(BeanCache beanCache) throws java.beans.PropertyVetoException {
		BeanCache oldBeanCache = this.beanCache;
		getVCS().fireVetoableChange("beanCache", oldBeanCache, beanCache);
		this.beanCache = beanCache;
		getPCS().firePropertyChange("beanCache", oldBeanCache, beanCache);
	}
        
    /** Api to retrieve the interface definitions for this bean.  Aids usability
     *  during configuration, as the editors can display the existing methds
     *  rather than have the user enter them manually.
     */
    public ConfigQuery.InterfaceData getEJBMethods() {
        /* !PW FIXME Temporary implementation values until plumbing in j2eeserver is worked out.
         */
        java.util.List hi = new ArrayList();
        hi.add(new ConfigQuery.MethodData("home_method1", java.util.Arrays.asList(new String [] { "arg1", "arg2" } )));
        
        java.util.List ri = new ArrayList();
        ri.add(new ConfigQuery.MethodData("remote_method1", java.util.Arrays.asList(new String [] { "arg1", "arg2", "arg3" } )));
        ri.add(new ConfigQuery.MethodData("remote_method2", java.util.Arrays.asList(new String [] { "arg1" } )));
        
        java.util.List lhi = new ArrayList();
        lhi.add(new ConfigQuery.MethodData("local_home_method1", java.util.Arrays.asList(new String [] { "arg1", "arg2" } )));
        
        java.util.List li = new ArrayList();
        li.add(new ConfigQuery.MethodData("local_method1", java.util.Arrays.asList(new String [] { "arg1", "arg2" } )));
        li.add(new ConfigQuery.MethodData("local_method2", java.util.Arrays.asList(new String [] { "arg1" } )));
        li.add(new ConfigQuery.MethodData("local_method3", java.util.Arrays.asList(new String [] { "arg1", "arg2", "arg3" } )));
        
        return new ConfigQuery.InterfaceData(hi, ri, lhi, li);
    }
}
