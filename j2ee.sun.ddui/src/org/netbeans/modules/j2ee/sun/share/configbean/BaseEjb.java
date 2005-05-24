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
package org.netbeans.modules.j2ee.sun.share.configbean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.XpathEvent;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar;
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
	public static final String RESOURCE_REF_LIST_CHANGED = "ResourceRefListChanged"; //NOI18N
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
            setDefaultProperties();
		loadFromPlanFile(getConfig());
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
		}
	}
	
	/* ------------------------------------------------------------------------
	 * Persistence support.  Loads DConfigBeans from previously saved Deployment
	 * plan file.
	 */
	protected class BaseEjbSnippet extends DefaultSnippet {
		public CommonDDBean getDDSnippet() {
			Ejb ejb = StorageBeanFactory.getDefault().createEjb();
                        ejb.setEjbName(getEjbName());

			if(null != jndiName){
				ejb.setJndiName(getJndiName());
			}

			if (null != passByReference) {
				ejb.setPassByReference(passByReference);
			}

			if (null != principalName) {
				Principal principal = ejb.newPrincipal();
				principal.setName(principalName);
				ejb.setPrincipal(principal);
			}

			IorSecurityConfig iorSecConf = getIorSecurityConfig();
			if(null != iorSecConf){
                                ejb.setIorSecurityConfig((IorSecurityConfig)iorSecConf.clone());
			}

			BeanPool beanPool = getBeanPool();
			if(null != beanPool){
                                ejb.setBeanPool((BeanPool)beanPool.clone());
			}

			BeanCache beanCache = getBeanCache();
			if(null != beanCache){
				ejb.setBeanCache((BeanCache)beanCache.clone());
			}
			return ejb;
		}

		public String getPropertyName() {
			return EnterpriseBeans.EJB;
		}

		public boolean hasDDSnippet() {
                    if(null != jndiName){
                        return true;
                    }

                    if (null != passByReference) {
                        return true;
                    }

                    if (null != principalName) {
                        return true;
                    }

                    if(null != getIorSecurityConfig()){
                        return true;
                    }

                    if(null != getBeanPool()){
                        return true;
                    }

                    if(null != getBeanCache()){
                        return true;
                    }

                    //return snippet in case of any child DConfigBeans.
                    Collection childList = getChildren();
                    if(childList.size() > 0){
                        return true;
                    }

                    return false;
		}
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
	private class EjbFinder extends NameBasedFinder {
		public EjbFinder(String beanName) {
			super(Ejb.EJB_NAME, beanName, Ejb.class);
		}
	}	
	
	boolean loadFromPlanFile(SunONEDeploymentConfiguration config) {
		String uriText = getUriText();

		Ejb ejb = (Ejb) config.getBeans(uriText, constructFileName(), null, 
			new EjbFinder(getEjbName()));

		if(null != ejb) {
			loadEjbProperties(ejb);
		}
		
		return (ejb != null);
	}
	
	protected void loadEjbProperties(Ejb savedEjb) {
		String  val = savedEjb.getJndiName();
		if(null != val) {
			this.jndiName = val.trim();
		}

		val = savedEjb.getPassByReference();
		if(null != val) {
			this.passByReference = val.trim();
		}

		Principal principal = savedEjb.getPrincipal();
		if(null != principal){
			String name = principal.getName();
			assert(name != null);
			this.principalName = name;
		}

		IorSecurityConfig iorSecurityConfig = savedEjb.getIorSecurityConfig();
		if(null != iorSecurityConfig){
			this.iorSecurityConfig = iorSecurityConfig;
		}

		BeanPool beanPool = savedEjb.getBeanPool();
		if(null != beanPool){
			this.beanPool = beanPool;
		}

		BeanCache beanCache = savedEjb.getBeanCache();
		if(null != beanCache){
			this.beanCache = beanCache;
		}
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
			
			if(getJ2EEModuleVersion().compareTo(EjbJarVersion.EJBJAR_2_1) >= 0) {
				baseEjbFactoryMap.put("service-ref", new DCBGenericFactory(ServiceRef.class));		// NOI18N
			}
		}
		return baseEjbFactoryMap;
	}		

	protected void setDefaultProperties() {
            if(writeJndiName()){
                jndiName = "ejb/" + getEjbName(); // NOI18N // J2EE recommended jndiName
            }
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
        
	protected void beanAdded(String xpath) {
		super.beanAdded(xpath);
		
		if(xpath != null && xpath.endsWith("resource-ref")) { // NOI18N
			getPCS().firePropertyChange(RESOURCE_REF_LIST_CHANGED, false, true);
		}
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

    
    private boolean writeJndiName(){
        boolean hasSnippet = false;
        DDBean ddBean = getDDBean();
        String xpath = ddBean.getXpath();
        //xpath - ejb-jar/enterprise-beans/<ejb-type>
        if("/ejb-jar/enterprise-beans/message-driven".equals(xpath)){           //NOI18N
            //In case of MDB always write the jndi-name to sun-ejb-jar.xml
            //jndi-name in case  of MDB represents message destination
            return true;
        }else{
            //In case of all other bean types write the jndi-name to sun-ejb-jar.xml
            //only if the bean has remote interface
            DDBean[] remoteBeans = ddBean.getChildBean("./remote");             //NOI18N
            if(remoteBeans.length > 0){
                DDBean remoteBean = remoteBeans[0];
                if(remoteBean != null){
                    return true;
                }
            }
        }
        return hasSnippet;
    }
}
