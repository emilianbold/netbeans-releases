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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.XpathEvent;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.exceptions.BeanNotFoundException;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.openide.LifecycleManager;
 
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.CmpResource;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.PmDescriptor;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.PmDescriptors;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.PropertyElement;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SchemaGeneratorProperties;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination;
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription;

import org.netbeans.modules.j2ee.deployment.common.api.SourceFileMap;
import org.netbeans.modules.j2ee.deployment.common.api.OriginalCMPMapping;
import com.sun.jdo.api.persistence.mapping.ejb.beans.SunCmpMappings;
import com.sun.jdo.modules.persistence.mapping.core.util.MappingContext;
import com.sun.jdo.api.persistence.mapping.ejb.EJBInfoHelper;
import com.sun.jdo.api.persistence.mapping.ejb.ConversionHelper;
import com.sun.jdo.modules.persistence.mapping.ejb.EJBDevelopmentInfoHelper;
import com.sun.jdo.modules.persistence.mapping.ejb.util.MappingConverter;
import com.sun.jdo.modules.persistence.mapping.ejb.util.SunOneUtilsCMP;
// TODO - consider moving all the model imports and handling to another class
import com.sun.jdo.api.persistence.model.Model;
import com.sun.jdo.api.persistence.model.ModelException;
import com.sun.jdo.api.persistence.model.jdo.PersistenceClassElement;
import com.sun.jdo.api.persistence.model.jdo.PersistenceFieldElement;
import com.sun.jdo.api.persistence.model.jdo.PersistenceElementProperties;
import com.sun.jdo.api.persistence.model.jdo.RelationshipElement;
import com.sun.jdo.api.persistence.model.mapping.MappingClassElement;
import com.sun.jdo.api.persistence.model.mapping.MappingElementProperties;
import com.sun.jdo.api.persistence.model.jdo.impl.PersistenceFieldElementImpl;
import com.sun.jdo.api.persistence.model.jdo.impl.RelationshipElementImpl;
import com.sun.jdo.api.persistence.model.mapping.MappingFieldElement;
import com.sun.jdo.api.persistence.model.mapping.impl.MappingFieldElementImpl;
import com.sun.jdo.api.persistence.model.mapping.impl.MappingRelationshipElementImpl;
// end TODO
/**
 *
 * @author  vkraemer
 */
public class EjbJarRoot extends BaseRoot implements javax.enterprise.deploy.spi.DConfigBean {

    private static final String ABSOLUTE_XPATH_ROOT = "/ejb-jar/"; // NOI18N
    private static final String SECURITY_ROLE_R_XPATH =
        "assembly-descriptor/security-role"; // NOI18N
    static final String SECURITY_ROLE_XPATH =
        ABSOLUTE_XPATH_ROOT + SECURITY_ROLE_R_XPATH;
    private static final String ENTITY_R_XPATH = "enterprise-beans/entity"; // NOI18N
    static final String ENTITY_XPATH = ABSOLUTE_XPATH_ROOT + ENTITY_R_XPATH;
    private static final String SESSION_R_XPATH = "enterprise-beans/session"; // NOI18N
    static final String SESSION_XPATH = ABSOLUTE_XPATH_ROOT + SESSION_R_XPATH;
    private static final String MD_R_XPATH = "enterprise-beans/message-driven"; // NOI18N
    static final String MD_XPATH = ABSOLUTE_XPATH_ROOT + MD_R_XPATH;
    private static final String CMP_FIELD_XPATH = ENTITY_XPATH + "/cmp-field"; // NOI18N
    private static final String CMP_FIELD_NAME_XPATH = CMP_FIELD_XPATH + "/field-name"; // NOI18N
    private static final String EJB_RELATION_XPATH = "/ejb-jar/relationships/ejb-relation"; // NOI18N
    private static final String CMR_FIELD_XPATH = EJB_RELATION_XPATH + "/ejb-relationship-role/cmr-field"; // NOI18N
    private static final String CMR_FIELD_NAME_XPATH = CMR_FIELD_XPATH + "/cmr-field-name"; // NOI18N
    private static final String CMP_MAPPING_FILE = "sun-cmp-mappings.xml"; // NOI18N

    public static final String EJB_LIST_CHANGED = "EjbListChanged"; //NOI18N

    // TODO - these are copied from the appserver's DTDRegistry for now
    public static final String SUN_CMP_MAPPING_810_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Application Server 8.1 OR Mapping//EN"; // NOI18N
    public static final String SUN_CMP_MAPPING_810_DTD_SYSTEM_ID =
        "http://www.sun.com/software/appserver/dtds/sun-cmp-mapping_1_2.dtd"; // NOI18N
    // end copied

	/** Holds value of property name. */
	private String name;

	/** Holds value of property uniqueId. */
	private String uniqueId;        

	/** Holds value of property pmDescriptors. */
	private PmDescriptors pmDescriptors;

	/** Holds value of property cmpResource. */
	private CmpResource cmpResource;

	/** Holds value of property messageDestination. */
	private MessageDestination[] messageDestination;

	/** Holds value of property webserviceDescription. */
	private WebserviceDescription[] webserviceDescription;    

	/** Holds the value of the cmp mapping info */
	private MappingContext mappingContext;    

	/** Holds the value of the cmp mapping info helper */
	private EJBInfoHelper ejbInfoHelper;    

	/** Holds the value of the cmp mapping conversion helper */
	private ConversionHelper conversionHelper;    

	private PropertyChangeListener cmpMappingListener = 
		new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				Object source = evt.getSource();
				String propName = evt.getPropertyName();

				if (((source instanceof MappingClassElement) &&
					MappingElementProperties.PROP_MODIFIED.equals(propName)) ||
					((source instanceof PersistenceClassElement) &&
					PersistenceElementProperties.PROP_MODIFIED.equals(propName)))
				{
					boolean modified =
						((Boolean)evt.getNewValue()).booleanValue();

					// only set if true -- otherwise could be endless loop
					if (modified)
						getPCS().firePropertyChange("mapping", null, null);
				}
			}
		};

	/** Creates a new instance of SunONEAppDConfigBean */
	public EjbJarRoot() {
		setDescriptorElement(bundle.getString("BDN_EjbJarRoot"));	// NOI18N
	}

	protected void init(DDBeanRoot dDBean, SunONEDeploymentConfiguration dc, DDBean ddbExtra) throws ConfigurationException  {
		super.init(dDBean, dc, ddbExtra);
		
		loadFromPlanFile(dc);
	}
	
	/** Get the ejbjar root version of this module.
	 *
	 * @return EjbJarVersion enum for the version of this module.
	 */
	 public J2EEBaseVersion getJ2EEModuleVersion() {
		DDBeanRoot ddbRoot = (DDBeanRoot) getDDBean();
		
		// From JSR-88 1.1
		String versionString = ddbRoot.getDDBeanRootVersion();
		if(versionString == null) {
			// If the above doesn't get us what we want.
			versionString = ddbRoot.getModuleDTDVersion();
		}
		
		J2EEBaseVersion ejbJarVersion = EjbJarVersion.getEjbJarVersion(versionString);
		if(ejbJarVersion == null) {
			// Default to EjbJar 2.1 if we can't find out what version this is.
			ejbJarVersion = EjbJarVersion.EJBJAR_2_1;
		}
		
		return ejbJarVersion;
	}	


        public String getHelpId() {
            return "AS_CFG_EjbJarRoot";                                 //NOI18N
        }


	protected void beanAdded(String xpath) {
		super.beanAdded(xpath);
		
		if("/ejb-jar/enterprise-beans/session".equals(xpath) ||         // NOI18N
                        "/ejb-jar/enterprise-beans/entity".equals(xpath) ||          // NOI18N
                                "/ejb-jar/enterprise-beans/message-driven".equals(xpath)) {// NOI18N
			getPCS().firePropertyChange(EJB_LIST_CHANGED, false, true);
		}

                //save();
	} 


	/* ------------------------------------------------------------------------
	 * Persistence support.  Loads DConfigBeans from previously saved Deployment
	 * plan file.
	 */
	Collection getSnippets() {
		Collection snippets = new ArrayList();
//		Snippet snipCmpMapping = getCmpMappingSnippet();
		Snippet snipOne = new DefaultSnippet() {
			public CommonDDBean getDDSnippet() {
                SunEjbJar sej = (SunEjbJar) DDProvider.getDefault().newGraph(SunEjbJar.class);
				
				EnterpriseBeans eb = sej.newEnterpriseBeans();

				boolean setCmpResourceElement = false;
				if (null != name) {
					eb.setName(name);
				}

				//assert(null != uniqueId);
				//eb.setUniqueId(uniqueId);

				PmDescriptors pmDescriptors = getPmDescriptors();
				if(null != pmDescriptors){
                                        PmDescriptors clone = (PmDescriptors) pmDescriptors.clone();
                                        eb.setPmDescriptors(clone);
				}

				CmpResource cmpResource = getCmpResource();
				if(null != cmpResource){
                                        CmpResource clone = (CmpResource) cmpResource.clone();
                                        eb.setCmpResource(clone);
				}

				MessageDestination[] mesgDestn = getMessageDestination();
				if(null != mesgDestn){
                                        MessageDestination clone = null;
					for(int i=0; i<mesgDestn.length; i++){
                                                clone = (MessageDestination) mesgDestn[i].clone();
                                                eb.addMessageDestination(clone);
					}
				}

				WebserviceDescription[] websvcDesc = getWebserviceDescription();
				if(null != websvcDesc){
                                        WebserviceDescription clone = null;
					for(int i=0; i<websvcDesc.length; i++){
                                                clone = (WebserviceDescription) websvcDesc[i].clone();
                                                eb.addWebserviceDescription(clone);
					}
				}
				sej.setEnterpriseBeans(eb);
				return sej;
			}
		};

		snippets.add(snipOne);
//		if (snipCmpMapping != null)
//			snippets.add(snipCmpMapping);

		return snippets;
	}

	Snippet getCmpMappingSnippet() {
		return new Snippet() {
			public CommonDDBean getDDSnippet() {
                return null;
			}
            public org.netbeans.modules.schema2beans.BaseBean getCmpDDSnippet() {
				SunCmpMappings sunCmpMappings = 
					SunOneUtilsCMP.getSunCmpMappings(mappingContext, ejbInfoHelper);
				
				// iterate created MCEs mark them as unmodified after save
				Iterator iterator = 
					mappingContext.getModel().getMappingCache().values().iterator();
				while (iterator.hasNext()) {
					((MappingClassElement)iterator.next()).setModified(false);
				// TODO - need to do PCEs too?
				}

// TODO: decide if it's empty or skeleton and possibly return null in that case?
				return sunCmpMappings;
            }
			public boolean hasDDSnippet() {
// TODO: optimize here - no cmps, empty, or skeleton: return false
				return mappingContext != null && ejbInfoHelper != null;
			}
			public String getFileName() {
				return CMP_MAPPING_FILE; 
			}
			// Peter says these shouldn't be called
			public String getPropertyName() {
				//control should never reach here.
				assert(false);
				return null;
			}
			public CommonDDBean mergeIntoRootDD(CommonDDBean ddRoot) {
				//control should never reach here.
				assert(false);
				return null;
			}
			public CommonDDBean mergeIntoRovingDD(CommonDDBean ddParent) {
				//control should never reach here.
				assert(false);
				return null;
			}	
			// end Peter says these shouldn't be called
		};
	}

	/** Calculate what the parent S2B bean should be for this child and return
	 *  that bean.
	 */
	protected CommonDDBean processParentBean(CommonDDBean bean, DConfigBean child) {
		// If the child is an Ejb, we need to return the EnterpriseBeans bean
		if(child instanceof BaseEjb) {
			return ((SunEjbJar) bean).getEnterpriseBeans();
		}

		// All other children require no translation.
		return bean;
	}

	/**
	 * @param dConfigBean
	 * @throws BeanNotFoundException
	 */
	public void removeDConfigBean(DConfigBean dConfigBean) throws BeanNotFoundException {
		// remove cmp before super so save works on both files
		if ((dConfigBean != null) && (dConfigBean instanceof CmpEntityEjb))
			removeMappingForCmp(((CmpEntityEjb)dConfigBean).getEjbName());
		super.removeDConfigBean(dConfigBean);
	}

	// methods used to read a DConfigBean from a deployment plan
	public class SunEjbJarParser implements ConfigParser {
		public Object parse(java.io.InputStream stream) {
            DDProvider provider = DDProvider.getDefault();
			SunEjbJar result = null;
            
            if(null != stream) {
                try {
                    result = provider.getEjbDDRoot(new org.xml.sax.InputSource(stream));
                } catch (Exception ex) {
                    jsr88Logger.severe("invalid stream for SunWebApp"); // FIXME
                }
            }
            
            // If we have a null stream or there is a problem reading the graph,
            // return a blank graph.
            if(result == null) {
                result = (SunEjbJar) provider.newGraph(SunEjbJar.class);
            }
            
            return result;
		}
	}


	public class EjbJarRootFinder implements ConfigFinder {
		public Object find(Object obj) {
			SunEjbJar retVal = (SunEjbJar) obj;
			return retVal;
		}
	}

	// methods used to read a DConfigBean from a deployment plan
	public class SunCmpMappingsParser implements ConfigParser {
		public Object parse(java.io.InputStream stream) {
			SunCmpMappings retVal;
			// TODO - this seems like creation of an empty one - 
			// might not need this, maybe instead just surround the try
			// below with null!=stream.. or is this where we create a skeleton
			// if necessary?  maybe can be done in utils
			if (null == stream)
				return SunCmpMappings.createGraph();
			try {
				retVal = SunCmpMappings.createGraph(stream);
			}
			catch (Throwable t) {
				jsr88Logger.severe("invalid stream for SunCmpMappings");
				// TODO - this seems like creation of an empty one - 
				// might not need this
				retVal = SunCmpMappings.createGraph();
			}
			return retVal;
		}
	}

	public class SunCmpMappingsRootFinder implements ConfigFinder {
		public Object find(Object obj) {
			SunCmpMappings retVal = (SunCmpMappings) obj;
			return retVal;
		}
	}

	boolean loadFromPlanFile(SunONEDeploymentConfiguration config) {
		String uriText = getUriText();

		SunEjbJar beanGraph = (SunEjbJar) config.getBeans(uriText, 
			constructFileName(), new SunEjbJarParser(), new EjbJarRootFinder());
		boolean cmpMappingLoaded = false;
		
		clearProperties();
		
		if(null != beanGraph) {
			EnterpriseBeans eb = beanGraph.getEnterpriseBeans();
			if (null != eb) {
				try {
					this.name = eb.getName();
					this.uniqueId = eb.getUniqueId();
					PmDescriptors pmDesc = eb.getPmDescriptors();
					if(null != pmDesc){
						setPmDescriptors(pmDesc);
					}
					CmpResource cr = eb.getCmpResource();
					if (null != cr) {
						setCmpResource(cr);
					}
					MessageDestination[] msgDestn = eb.getMessageDestination();
					if(null != msgDestn){
						messageDestination = new MessageDestination[msgDestn.length];
						for(int i=0; i<msgDestn.length; i++){
							setMessageDestination(i, msgDestn[i]);
						}
					}
					WebserviceDescription[] websvcDesc = eb.getWebserviceDescription();
					if(null != websvcDesc){
						webserviceDescription = new WebserviceDescription[websvcDesc.length];
						for(int i=0; i<websvcDesc.length; i++){
							setWebserviceDescription(i,websvcDesc[i]);
						}
					}
				} catch(java.beans.PropertyVetoException vetoException){
					jsr88Logger.throwing("EjbJarRoot", "loadFromPlanFile",//NOI18N
						vetoException);         
				}
			}
		} else {
			setDefaultProperties();
		}

		cmpMappingLoaded = loadCmpMappingsFromPlanFile(config);

		return ((beanGraph != null) || cmpMappingLoaded);
	}

	public MappingContext getMappingContext () {
		return getMappingContext(null, getEJBInfoHelper());
	}

	private MappingContext getMappingContext (SunCmpMappings beanGraph, 
			EJBInfoHelper infoHelper) {
		if (mappingContext == null) {
			try {
				mappingContext = SunOneUtilsCMP.getMappingContext(beanGraph, 
					ejbInfoHelper);
				SunOneUtilsCMP.setExistingMappingContext(ejbInfoHelper, 
					mappingContext);
				// iterate created MCEs and add cmpMappingListener as a
				// PropertyChangeListener
				Iterator iterator = 
					mappingContext.getModel().getMappingCache().values().iterator();
				while (iterator.hasNext()) {
					addMappingListener((MappingClassElement)iterator.next());
				}
			} catch (Exception e) {
				// TODO - what is proper handling of this exception? logging?
				// TODO - narrower exceptions? (could be Model or DBException)
				// for now, returns null
				e.printStackTrace();
			}
		}

		return mappingContext;
	}

	private void addMappingListener (MappingClassElement mce) {
		mce.addPropertyChangeListener(cmpMappingListener);
		// TODO - need to add PCE listener too?  if not, maybe can
		// simplify imports and PCL implementation above
	}

	public EJBInfoHelper getEJBInfoHelper () {
		if (ejbInfoHelper == null) {
			ejbInfoHelper = new EJBDevelopmentInfoHelper(null, 
				SourceFileMap.findSourceMap(getConfig().getDeployableObject()));
                        
		}

		return ejbInfoHelper;
	}

	private ConversionHelper getConversionHelper () {
		if (conversionHelper == null) {
			conversionHelper = getEJBInfoHelper().createConversionHelper();
		}

		return conversionHelper;
	}

	public void mapCmpBeans(DeploymentConfiguration config, 
			OriginalCMPMapping[] mapping) {
		SunCmpMappings beanGraph = (SunCmpMappings)
			((SunONEDeploymentConfiguration)config).getBeans(
			getUriText(), CMP_MAPPING_FILE, new SunCmpMappingsParser(), 
			new SunCmpMappingsRootFinder());
		EJBInfoHelper infoHelper = getEJBInfoHelper();
	
	// TODO: this intializes the mappingContext, but check whether 
	// it is still necessary, and if so, if it must be before creation
	// of the mappingConverter. If it is no longer necessary, check whether
	// beanGraph above is needed and whether infoHelper in MappingConverter's
	// constructor can be used inline w/o the var above
	getMappingContext(beanGraph, infoHelper);

		MappingConverter mappingConverter = new MappingConverter(
			infoHelper, SourceFileMap.findSourceMap(getConfig().getDeployableObject()));
		Collection newMCEs = null;
		
		try {
			newMCEs = mappingConverter.toMappingClasses(mapping);
		} catch (Exception e) {
			// TODO - what is proper handling of this exception? logging?
			// TODO - narrower exceptions? (could be Model or DBException)
			// for now, newMCEs will be null and no registration of them
			e.printStackTrace();
		}

		if (newMCEs != null) {
			Iterator iterator = newMCEs.iterator();
			while (iterator.hasNext())
				addMappingListener((MappingClassElement)iterator.next());
		}
	}

	public void ensureCmpMappingExists (String beanName) {
		MappingContext mappingContext = getMappingContext();
		EJBInfoHelper infoHelper = getEJBInfoHelper();
		ConversionHelper myConversionHelper = getConversionHelper();

		// if no corresponding MCE object, this must be a new
		// bean, create the skeleton
		if (mappingContext.getModel().getMappingClass(
				myConversionHelper.getMappedClassName(beanName)) == null) {
			MappingConverter mappingConverter = new MappingConverter(
				infoHelper, SourceFileMap.findSourceMap(getConfig().getDeployableObject()));
			MappingClassElement newMCE = null;
			try {
				newMCE = mappingConverter.toMappingClass(beanName);
			} catch (Exception e) {
				// TODO - what is proper handling of this exception? logging?
				// TODO - narrower exceptions? (could be Model or DBException)
				// for now, newMCE will be null and no registration of it
				e.printStackTrace();
			}

			if (newMCE != null) {
				addMappingListener(newMCE);
			}
		}
	}

	private void removeMappingForCmp(String beanName) {
		if ((beanName != null) && (mappingContext != null)) {
			EJBInfoHelper infoHelper = getEJBInfoHelper();
			ConversionHelper myConversionHelper = getConversionHelper();
			Model model = mappingContext.getModel();
			String pcClassName = conversionHelper.getMappedClassName(beanName);
			MappingClassElement mce = model.getMappingClass(pcClassName);

			if (mce != null) {
				// remove the listener then the mce from model's cache
				mce.removePropertyChangeListener(cmpMappingListener);
				model.updateKeyForClass(null, pcClassName);
			}
		}
	}

	private void addMappingForCmrField(String beanName, String fieldName) {
		addMappingForCmpField(beanName, fieldName, true);
	}

	private void addMappingForCmpField(String beanName, String fieldName) {
		addMappingForCmpField(beanName, fieldName, false);
	}

	private void addMappingForCmpField(String beanName, String fieldName, 
			boolean isRelationship) {
		if ((beanName != null) && (mappingContext != null)) {
			EJBInfoHelper infoHelper = getEJBInfoHelper();
			ConversionHelper myConversionHelper = getConversionHelper();
			Model model = mappingContext.getModel();
			String pcClassName = conversionHelper.getMappedClassName(beanName);
			MappingClassElement mce = model.getMappingClass(pcClassName);

			if ((mce != null) && (mce.getField(fieldName) == null)) {
				PersistenceClassElement pce = 
					model.getPersistenceClass(pcClassName);
			// workaround - problem with timing - bean impl update doesn't 
			// seem to be done yet, so model's automatic field vs. 
			// rel check based on type doesn't work
			// we can determine field vs. rel here, but coll vs. upper bound
			// is not correct & inverse is not set
				//model.addFieldElement(pce, fieldName);
				// PersistenceFieldElement newPFE = pce.getField(fieldName);
				PersistenceFieldElement newPFE = ((isRelationship) ?
					new RelationshipElement(
						new RelationshipElementImpl(fieldName), pce) :
					new PersistenceFieldElement(new
						PersistenceFieldElementImpl(fieldName), pce));

				try {
					pce.addField(newPFE);
				// end above timing issue
					MappingFieldElement mfe = ((isRelationship) ?
						new MappingRelationshipElementImpl(fieldName, mce) : 
						new MappingFieldElementImpl(fieldName, mce));
					mce.addField(mfe);
				} catch (ModelException e) {
					// TODO - what is proper handling of this exception?logging?
					// for now, no mapping will be added
					e.printStackTrace();
				}
			}
		}
	}

	private void removeMappingForCmpField(String beanName, String fieldName,  
			boolean removeInverse) {
		if (mappingContext != null) {
			EJBInfoHelper infoHelper = getEJBInfoHelper();
			ConversionHelper myConversionHelper = getConversionHelper();
			Model model = mappingContext.getModel();
			String pcClassName = conversionHelper.getMappedClassName(beanName);
			MappingClassElement mce = model.getMappingClass(pcClassName);

			if (mce != null) {
				MappingFieldElement mfe = mce.getField(fieldName);
				PersistenceFieldElement pfe = 
					model.getPersistenceClass(pcClassName).getField(fieldName);
				RelationshipElement inverse = (
					(removeInverse && (pfe instanceof RelationshipElement)) ? 
					((RelationshipElement)pfe).getInverseRelationship(model) : 
					null);

				try {
					model.removeFieldElement(pfe);
					if (mfe != null)
						mce.removeField(mfe);

					if (inverse != null) {
						String inverseName = inverse.getName();
						MappingClassElement inverseMCE = model.getMappingClass(
							inverse.getDeclaringClass().getName());
						MappingFieldElement inverseMFE =
							inverseMCE.getField(inverseName);

						model.removeFieldElement(inverse);
						if (inverseMFE != null)
							inverseMCE.removeField(inverseMFE);
					}
				} catch (ModelException e) {
					// TODO - what is proper handling of this exception?logging?
					// for now, no mapping will be removed
					e.printStackTrace();
				}
			}
		}
	}

	private void removeMappingForCmpField(String beanName, String fieldName) {
		removeMappingForCmpField(beanName, fieldName, false);
	}

	private void renameMappingForCmpField(String beanName, String oldFieldName,
			String newFieldName) {
		if ((beanName != null) && (mappingContext != null)) {
			EJBInfoHelper infoHelper = getEJBInfoHelper();
			ConversionHelper myConversionHelper = getConversionHelper();
			Model model = mappingContext.getModel();
			String pcClassName = conversionHelper.getMappedClassName(beanName);
			MappingClassElement mce = model.getMappingClass(pcClassName);

			if (mce != null) {
				MappingFieldElement mfe = mce.getField(oldFieldName);
				PersistenceFieldElement pfe = model.getPersistenceClass(
					pcClassName).getField(oldFieldName);

				try {
					if (mfe != null)
						mfe.setName(newFieldName);
					if (pfe != null){
						pfe.setName(newFieldName);
                                                if (pfe instanceof RelationshipElement)
                                                {
                                                    RelationshipElement relationship = (RelationshipElement)pfe;
                                                    RelationshipElement inverse =
                                                                       relationship.getInverseRelationship(model);

                                                    if (inverse != null)
                                                        inverse.setInverseRelationship(relationship, model);
                                                }
                                        }
				} catch (ModelException e) {
					// TODO - what is proper handling of this exception?logging?
					// for now, no mapping will be renamed
					e.printStackTrace();
				}
			}
		}
	}

	boolean loadCmpMappingsFromPlanFile(SunONEDeploymentConfiguration config) {
		SunCmpMappings beanGraph = (SunCmpMappings) config.getBeans(
			getUriText(), CMP_MAPPING_FILE, new SunCmpMappingsParser(), 
			new SunCmpMappingsRootFinder());
	// TODO - is this sufficient or do we need to add 
	// mappingContext and ejbInfoHelper to clearProperties?  they will be
	// assigned to new instances here
		if (beanGraph != null) {
			if (ejbInfoHelper == null)
				ejbInfoHelper = new EJBDevelopmentInfoHelper(beanGraph, 
					SourceFileMap.findSourceMap(getConfig().getDeployableObject()));
			// TODO: else load schemas if they are different?
			// also probably do if mappingContext is already non-null
			// call intoMappingClasses in utils

			// TODO put this in a background thread (at least the
			// intoMappingClasses part), because it can take a while
			getMappingContext(beanGraph, ejbInfoHelper);
		}

		return (beanGraph != null);
	}

	protected void clearProperties() {
		name = null;
		uniqueId = null;
		pmDescriptors = null;
		cmpResource = null;
		messageDestination = null;
		webserviceDescription = null;
	}

	protected void setDefaultProperties() {
	}


	/* ------------------------------------------------------------------------
	 * XPath to Factory mapping support
	 */
	private HashMap ejbRootFactoryMap;

	/** Retrieve the XPathToFactory map for this DConfigBean.  For AppRoot,
	 *  this maps application xpaths to factories for other contained root
	 *  objects plus a SecurityRoleModel factory
	 * @return
	 */  
	protected java.util.Map getXPathToFactoryMap() {
		if(ejbRootFactoryMap == null) {
			ejbRootFactoryMap = new HashMap(17);

			ejbRootFactoryMap.put(SECURITY_ROLE_R_XPATH, new DCBGenericFactory(SecurityRoleMapping.class));
			ejbRootFactoryMap.put(ENTITY_R_XPATH, new EntityEjbDCBFactory(this));
			ejbRootFactoryMap.put(SESSION_R_XPATH, new SessionEjbDCBFactory());
			ejbRootFactoryMap.put(MD_R_XPATH, new DCBGenericFactory(MDEjb.class));
                        
			// an ejb-jar can have one CmpJar DConfigBean
			//DCBSingletonFactory cmpJarFactory = new DCBSingletonFactory(CmpJar.class);
			//ejbRootFactoryMap.put("/ejb-jar/enterprise-beans/entity/cmp-field", cmpJarFactory);
			//ejbRootFactoryMap.put("/ejb-jar/enterprise-beans/entity/abstract-schema-name", cmpJarFactory);
			// pending
			//ejbRootFactoryMap.put("/ejb-jar/relationships/ejb-relation/ejb-relationship-role/cmr-field/cmr-field-name",
			//	new DCBGenericFactory(CmrField.class));
		}

		return ejbRootFactoryMap;
	}	


	public void addCmpResourceIfNotPresent(){
		if(null == cmpResource){
			cmpResource = StorageBeanFactory.getDefault().createCmpResource();
			cmpResource.setJndiName(Utils.getDefaultCmpResourceJndiName(this));
		}
	}


	//Methods called by Customizer
	public void addMessageDestination(MessageDestination msgDstn) throws java.beans.PropertyVetoException {
		if(null == messageDestination){
			messageDestination = new MessageDestination[1];
			setMessageDestination(0, msgDstn);
		} else {
			MessageDestination[] tempMsgDstn = new
				MessageDestination[messageDestination.length + 1];
			for(int i=0; i<messageDestination.length; i++){
				tempMsgDstn[i] = messageDestination[i];
			}
                        messageDestination = tempMsgDstn;
                        setMessageDestination(messageDestination.length-1, msgDstn);
			}
	   }


	public void removeMessageDestination(MessageDestination msgDstn) throws java.beans.PropertyVetoException {
		if(null != messageDestination){
			MessageDestination[] tempMsgDstn = new
				MessageDestination[messageDestination.length - 1];
			int tempIndex = 0;
			for(int i=0; i<messageDestination.length; i++){
				if(!messageDestination[i].equals(msgDstn)){
					tempMsgDstn[tempIndex] = messageDestination[i];
					tempIndex++;
				}
			}
			setMessageDestination(tempMsgDstn);
			}
		}


	public void removeMessageDestination(int index) throws java.beans.PropertyVetoException {
		if(null != messageDestination){
			if(1 == messageDestination.length){
				messageDestination = null;
			}else{
				MessageDestination[] tempMsgDstn = new
					MessageDestination[messageDestination.length - 1];
				int tempIndex = 0;
				for(int i=0; i<messageDestination.length; i++){
					if(i != index){
						tempMsgDstn[tempIndex] = messageDestination[i];
						tempIndex++;
					}
				}
        			setMessageDestination(tempMsgDstn);
				}
			}
	   }


	public void addWebserviceDescription(WebserviceDescription websrvcDesc){
		if(null == webserviceDescription){
			webserviceDescription = new WebserviceDescription[1];
			webserviceDescription[0] = websrvcDesc;
		} else {
			WebserviceDescription[] tempWebsrvcDesc = new
				WebserviceDescription[webserviceDescription.length + 1];
			for(int i=0; i<webserviceDescription.length; i++){
				tempWebsrvcDesc[i] = webserviceDescription[i];
			}
			tempWebsrvcDesc[webserviceDescription.length] = websrvcDesc;
			webserviceDescription = new 
				WebserviceDescription[tempWebsrvcDesc.length];
			for(int i=0; i<tempWebsrvcDesc.length; i++){
				webserviceDescription[i] = tempWebsrvcDesc[i];
			}
	   }
	}


	public void removeWebserviceDescription(WebserviceDescription websrvcDesc){
		if(null != webserviceDescription){
			WebserviceDescription[] tempWebsrvcDesc = new
				WebserviceDescription[webserviceDescription.length - 1];
			int tempIndex = 0;
			for(int i=0; i<webserviceDescription.length; i++){
				if(!webserviceDescription[i].equals(websrvcDesc)){
					tempWebsrvcDesc[tempIndex] = webserviceDescription[i];
					tempIndex++;
				}
			}
			webserviceDescription = new 
				WebserviceDescription[tempWebsrvcDesc.length];
			for(int i=0; i<tempWebsrvcDesc.length; i++){
				webserviceDescription[i] = tempWebsrvcDesc[i];
			}
		}
	}


	public void addPmDescriptor(PmDescriptor pmDescriptor){
		if(null == pmDescriptors){
			pmDescriptors = StorageBeanFactory.getDefault().createPmDescriptors();
		}
		pmDescriptors.addPmDescriptor(pmDescriptor);
	}


	public void removePmDescriptor(PmDescriptor pmDescriptor){
		if(null != pmDescriptors){
			pmDescriptors.removePmDescriptor(pmDescriptor);
		}
	}


	public void addProperty(String parent, PropertyElement property){
		if(parent.equals("CmpResource")){                               //NOI18N
			if(null == cmpResource){
				cmpResource = StorageBeanFactory.getDefault().createCmpResource();
			}
			cmpResource.addPropertyElement(property);
		}else{
			if(parent.equals("SchemaGenerator")){                       //NOI18N
				if(null == cmpResource){
					cmpResource = StorageBeanFactory.getDefault().createCmpResource();
				}
				SchemaGeneratorProperties schmaGnrtrProps =
					cmpResource.getSchemaGeneratorProperties();
				if(null == schmaGnrtrProps){
					schmaGnrtrProps = cmpResource.newSchemaGeneratorProperties();
					cmpResource.setSchemaGeneratorProperties(schmaGnrtrProps);
				}
				schmaGnrtrProps.addPropertyElement(property);
			}else{
				//control should never reach here.
				assert(false);
			}
		}
	}


	public void removeProperty(String parent, PropertyElement property){
		if(parent.equals("CmpResource")){                               //NOI18N
			if(null != cmpResource){
				cmpResource.removePropertyElement(property);
			}
		}else{
			if(parent.equals("SchemaGenerator")){                       //NOI18N
				if(null != cmpResource){
					SchemaGeneratorProperties schmaGnrtrProps =
						cmpResource.getSchemaGeneratorProperties();
					if(null != schmaGnrtrProps){
						schmaGnrtrProps.removePropertyElement(property);
					}
				}
			}else{
				//control should never reach here.
				assert(false);
			}
		}
	}


	/** Getter for property name.
	 * @return Value of property name.
	 *
	 */
	public String getName() {
		return this.name;
	}


	/** Setter for property name.
	 * @param name New value of property name.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setName(String name) throws java.beans.PropertyVetoException {
		String oldName = this.name;
		getVCS().fireVetoableChange("name", oldName, name);
		this.name = name;
		getPCS().firePropertyChange("name", oldName, name);
	}


	/** Getter for property uniqueId.
	 * @return Value of property uniqueId.
	 *
	 */
	public String getUniqueId() {
		return this.uniqueId;
	}        


	/** Getter for property pmDescriptors.
	 * @return Value of property pmDescriptors.
	 *
	 */
	public PmDescriptors getPmDescriptors() {
		return this.pmDescriptors;
	}


	/** Setter for property pmDescriptors.
	 * @param pmDescriptors New value of property pmDescriptors.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setPmDescriptors(PmDescriptors pmDescriptors) throws java.beans.PropertyVetoException {
		PmDescriptors oldPmDescriptors = this.pmDescriptors;
		getVCS().fireVetoableChange("pmDescriptors", oldPmDescriptors, pmDescriptors);
		this.pmDescriptors = pmDescriptors;
		getPCS().firePropertyChange("pmDescriptors", oldPmDescriptors, pmDescriptors);
	}


	/** Getter for property cmpResource.
	 * @return Value of property cmpResource.
	 *
	 */
	public CmpResource getCmpResource() {
		return this.cmpResource;
	}


	/** Setter for property cmpResource.
	 * @param cmpResource New value of property cmpResource.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setCmpResource(CmpResource cmpResource) throws java.beans.PropertyVetoException {
		CmpResource oldCmpResource = this.cmpResource;
		getVCS().fireVetoableChange("cmpResource", oldCmpResource, cmpResource);
		this.cmpResource = cmpResource;
		getPCS().firePropertyChange("cmpResource", oldCmpResource, cmpResource);
	}


	/** Indexed getter for property messageDestination.
	 * @param index Index of the property.
	 * @return Value of the property at <CODE>index</CODE>.
	 *
	 */
	public MessageDestination getMessageDestination(int index) {
		return this.messageDestination[index];
	}


	/** Getter for property messageDestination.
	 * @return Value of property messageDestination.
	 *
	 */
	public MessageDestination[] getMessageDestination() {
		return this.messageDestination;
	}


	/** Indexed setter for property messageDestination.
	 * @param index Index of the property.
	 * @param messageDestination New value of the property at <CODE>index</CODE>.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setMessageDestination(int index, MessageDestination messageDestination) throws java.beans.PropertyVetoException {
		MessageDestination oldMessageDestination = this.messageDestination[index];
		this.messageDestination[index] = messageDestination;
		try {
			getVCS().fireVetoableChange("messageDestination", null, null );
		}
		catch(java.beans.PropertyVetoException vetoException ) {
			this.messageDestination[index] = oldMessageDestination;
			throw vetoException;
		}
		getPCS().firePropertyChange("messageDestination", null, null );
	}


	/** Setter for property messageDestination.
	 * @param messageDestination New value of property messageDestination.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setMessageDestination(MessageDestination[] messageDestination) throws java.beans.PropertyVetoException {
		MessageDestination[] oldMessageDestination = this.messageDestination;
		getVCS().fireVetoableChange("messageDestination", oldMessageDestination, messageDestination);
		this.messageDestination = messageDestination;
		getPCS().firePropertyChange("messageDestination", oldMessageDestination, messageDestination);
	}


	/** Indexed getter for property webserviceDescription.
	 * @param index Index of the property.
	 * @return Value of the property at <CODE>index</CODE>.
	 *
	 */
	public WebserviceDescription getWebserviceDescription(int index) {
		return this.webserviceDescription[index];
	}


	/** Getter for property webserviceDescription.
	 * @return Value of property webserviceDescription.
	 *
	 */
	public WebserviceDescription[] getWebserviceDescription() {
		return this.webserviceDescription;
	}


	/** Indexed setter for property webserviceDescription.
	 * @param index Index of the property.
	 * @param webserviceDescription New value of the property at <CODE>index</CODE>.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setWebserviceDescription(int index, WebserviceDescription webserviceDescription) throws java.beans.PropertyVetoException {
		WebserviceDescription oldWebserviceDescription = this.webserviceDescription[index];
		this.webserviceDescription[index] = webserviceDescription;
		try {
			getVCS().fireVetoableChange("webserviceDescription", null, null );
		}
		catch(java.beans.PropertyVetoException vetoException ) {
			this.webserviceDescription[index] = oldWebserviceDescription;
			throw vetoException;
		}
		getPCS().firePropertyChange("webserviceDescription", null, null );
	}


	/** Setter for property webserviceDescription.
	 * @param webserviceDescription New value of property webserviceDescription.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setWebserviceDescription(WebserviceDescription[] webserviceDescription) throws java.beans.PropertyVetoException {
		WebserviceDescription[] oldWebserviceDescription = this.webserviceDescription;
		getVCS().fireVetoableChange("webserviceDescription", oldWebserviceDescription, webserviceDescription);
		this.webserviceDescription = webserviceDescription;
		getPCS().firePropertyChange("webserviceDescription", oldWebserviceDescription, webserviceDescription);
	}    


    // !PW FIXME The code below needs review.  At best, it should not be necessary
    // and at worst, I don't think it belongs here if it needs to exist at all.
    
        //We need to handle this event as we are not getting the call 
        //removeDConfigBean() for removed beans. This is to fix the issue
        //* /49655
	public void fireXpathEvent(XpathEvent xpathEvent) {
            //ADD , REMOVE or CHANGE events
            DDBean bean = xpathEvent.getBean();
            String xpath = bean.getXpath();

            if(xpathEvent.isRemoveEvent()){
                if("/ejb-jar/enterprise-beans/session".equals(xpath) ||         // NOI18N
                        "/ejb-jar/enterprise-beans/entity".equals(xpath) ||          // NOI18N
                        "/ejb-jar/enterprise-beans/message-driven".equals(xpath)) {// NOI18N
                        try {
                            DConfigBean childBean =  getDConfigBean(bean);
                            if(childBean != null){
                                    //removeDConfigBean(childBean);
                                    getPCS().firePropertyChange(EJB_LIST_CHANGED, false, true);
                                    //save();
                            }
                        }catch(Exception exception){
                        }
                }
            }

            // add or remove cmp field gets this event
            if (CMP_FIELD_XPATH.equals(xpath)) {
                String beanName = extractBeanName(xpath, bean);
                String fieldName = extractFieldName(xpath, bean);

                if ((beanName != null) && (fieldName != null)) {
                    if (xpathEvent.isAddEvent()) {
                        addMappingForCmpField(beanName, fieldName);
                    } else if (xpathEvent.isRemoveEvent()) {
                        removeMappingForCmpField(beanName, fieldName);
                    }
                }
            } else if (CMP_FIELD_NAME_XPATH.equals(xpath)) {
                // rename cmp field (on the method) gets this event
                if (xpathEvent.isChangeEvent()) {
                    String beanName = extractBeanName(xpath, bean);

                    if (beanName != null) {
                        PropertyChangeEvent changeEvent = 
                            xpathEvent.getChangeEvent();
                        renameMappingForCmpField(beanName, 
                            changeEvent.getOldValue().toString(), 
                            changeEvent.getNewValue().toString());
                    }
                }
            } else if (EJB_RELATION_XPATH.equals(xpath)) {
                // remove cmr fields by removing entire relationship 
                // gets this event
                if (xpathEvent.isRemoveEvent()) {
                    String beanName = extractBeanName(xpath, bean);
                    String fieldName = extractFieldName(xpath, bean);

                    // when delete from multiview editor, only get 
                    // this event (as opposed to some others below), but
                    // only one field name is coming through, 
                    // so handle it by a flag to removeMapping
                    // method which deletes the inverse too
                    if ((beanName != null) && (fieldName != null)) {
                        removeMappingForCmpField(beanName, fieldName, true);
                    }
                }
            } else if (CMR_FIELD_XPATH.equals(xpath)) {
                // remove cmr fields by delete from the logical node 
                // CM fields gets this event
                if (xpathEvent.isRemoveEvent()) {
                    String beanName = extractBeanName(xpath, bean);
                    String fieldName = extractFieldName(xpath, bean);

                    if ((beanName != null) && (fieldName != null)) {
                        removeMappingForCmpField(beanName, fieldName);
                    }
                }
            } else if (CMR_FIELD_NAME_XPATH.equals(xpath)) {
                String beanName = extractBeanName(xpath, bean);
                String fieldName = extractFieldName(xpath, bean);

                // add cmr fields by creating a new relationship in 
                // the multiview editor gets this event, but can't 
                // figure out the inverse info and cardinality, 
                // remove events by?
                if ((beanName != null) && (fieldName != null)) {
                    if (xpathEvent.isAddEvent()) {
                        addMappingForCmrField(beanName, fieldName);
                    } else if (xpathEvent.isRemoveEvent()) {
                        removeMappingForCmpField(beanName, fieldName);
                    } else if(xpathEvent.isChangeEvent()) {
                        if (beanName != null) {
                            PropertyChangeEvent changeEvent = 
                                xpathEvent.getChangeEvent();
                            renameMappingForCmpField(beanName, 
                                changeEvent.getOldValue().toString(), 
                                changeEvent.getNewValue().toString());
                        }
                    }
                 }
            }
	}

	private String extractFieldName(String xpath, DDBean bean) {
		DDBean field = null;

		if (CMP_FIELD_XPATH.equals(xpath)) {
			field = bean.getChildBean("field-name")[0]; // NOI18N
		} else if (EJB_RELATION_XPATH.equals(xpath)) {
			DDBean[] fields = bean.getChildBean(
				"ejb-relationship-role/cmr-field/cmr-field-name"); // NOI18N
			field = ((fields.length > 0) ? fields[0] : null);
		} else if (CMR_FIELD_XPATH.equals(xpath)) {
			DDBean[] fields = bean.getChildBean("cmr-field-name"); // NOI18N
			field = (((fields != null) && (fields.length > 0)) ? 
				fields[0] : null);
		} else if (CMR_FIELD_NAME_XPATH.equals(xpath)) {
			field = bean;
		}

		return ((field != null) ? field.getText() : null);
	}

	private String extractBeanName(String xpath, DDBean bean) {
		DDBean entity = null;

		if (CMP_FIELD_XPATH.equals(xpath)) {
			entity =  bean.getChildBean("../ejb-name")[0];
		} else if (CMP_FIELD_NAME_XPATH.equals(xpath)) {
			entity = bean.getChildBean("../../ejb-name")[0]; // NOI18N
		} else if (EJB_RELATION_XPATH.equals(xpath)) {
			DDBean[] entities = bean.getChildBean(
				"ejb-relationship-role/relationship-role-source/ejb-name"); // NOI18N
			entity = ((entities.length > 0) ? entities[0] : null);
		} else if (CMR_FIELD_XPATH.equals(xpath)) {
			entity = bean.getChildBean("../relationship-role-source/ejb-name")[0]; // NOI18N
		} else if (CMR_FIELD_NAME_XPATH.equals(xpath)) {
			entity = bean.getChildBean(
				"../../relationship-role-source/ejb-name")[0];// NOI18N
		}

		return ((entity != null) ? entity.getText() : null);
	}

        private void save(){
            //Need to find a way to save selectively eg: jndi-name; not everything
            //Needs to be done after EA1
            ///
            LifecycleManager lifecycleManager = LifecycleManager.getDefault();
            if(lifecycleManager != null){
                //lifecycleManager.saveAll();
            }
            ///
        }//*/
}
