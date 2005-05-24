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

import java.text.MessageFormat;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.DConfigBeanRoot;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;


/**
 *
 * @author Vince Kraemer
 * @author Peter Williams
 */
public abstract class BaseRoot extends Base implements DConfigBeanRoot {

	private SunONEDeploymentConfiguration dc;
	private DDBean uri;
	private DDBean displayNameDD;

	protected SunONEDeploymentConfiguration getConfig() {
		if(null == getParent()) {
			return this.dc;
		} else {
			return getParent().getConfig();
		}
	}

	protected void setConfig(SunONEDeploymentConfiguration dc) {
		this.dc = dc;
	}

	public BaseRoot() {
	}

	protected void init(DDBeanRoot dDBeanRoot, SunONEDeploymentConfiguration dc, DDBean ddbExtra) throws ConfigurationException  {
		super.init(dDBeanRoot, null);
		this.dc = dc;
		this.uri = ddbExtra;
		
		// initialize reference if we can
		findRefDCB(dDBeanRoot);
	}
	
	public DConfigBean getDConfigBean(DDBeanRoot dDBeanRoot) {
		// webservices.xml gets requested here.
		return null;
	}

	public String getComponentName() {
		String name = getUriText();
		
		// This method should return null if there is no name, while getUriText
		// must always return non-null.
		if(!Utils.notEmpty(name)) {
			name = null;
		}

		return name;
	}

	public String getUriText() {
		BaseModuleRef ref = getReference();
		
		if(ref != null) {
			return ref.getModuleUri();
		}

		// No reference, no URI.  This should only happen with solo modules
		// such as webapp modules and ejbjar modules.
		return ""; // NOI18N
	}
	
	/** Get the J2EE version of this module.  The derived class must implement
	 *  this method and return the correct J2EEBaseVersion derivative representing
	 *  the version of the module, e.g. ServletVersion.SERVLET_X_Y for web-apps,
	 *
	 *  This method MUST be overridden in derived root classes (e.g. WebAppRoot, etc.)
	 *  It is not declared abstract because there is a real implementation in
	 *  Base.java that is used by the child beans and I'm not sure if java will
	 *  allow turning a concrete function to abstract for a portion of the hierarchy.
	 *  It's a sketchy concept at best, even if allowed.
	 *
	 * @return J2EEVersion enum for the version of this module.
	 */
	public J2EEBaseVersion getJ2EEModuleVersion() {
		// This implementation should never be called.
		assert false : this.getClass().getName() + " does not override getJ2EEModuleVersion!!!"; // NOI18N
		
		// Prevent other runtime issues, if something slips past by returning
		// something reasonable, but likely benign.
		return J2EEVersion.J2EE_1_4;
	}

	/** -----------------------------------------------------------------------
	 * Properties
	 */
	
	/** Getter for property refIdentity.
	 * @return Value of property refIdentity.
	 *
	 */
	public String getRefIdentity() {
		String result = "(null)";	// NOI18N
		if(getReference() != null) {
			result = getReference().getIdentity();
		}
		
		return result;
	}
	
	/** -----------------------------------------------------------------------
	 * Reference support
	 */
	private BaseModuleRef rootReference = null;
	
	protected BaseModuleRef getReference() {
		return rootReference;
	}
	
	protected void setReference(BaseModuleRef ref) {
		rootReference = ref;
		this.uri = ref.getDDBean();
	}
	
	protected void findRefDCB(DDBeanRoot ddbRoot) {
		// For this direction, check patch cache to see if there is an unpatched
		// reference bean under this key.
		//
		BaseModuleRef ref = (BaseModuleRef) dc.getPatchList().get(ddbRoot);
		
		if(ref != null) {
			setReference(ref);
			ref.setReference(this);
			
			// Since we found and patched the reference, remove it from the
			// patch list.
			dc.getPatchList().remove(ddbRoot);
		}
	}
	
	/** -----------------------------------------------------------------------
	 *  Persistence support helper objects
	 */
/* !PW not currently used.  I didn't like the additional runtime issues that
 *     using introspection added to the code.
 *
	public static class SimpleRootParser implements ConfigParser {
		private Class rootBaseBeanClass;
		private Method createGraphMethod;
		private Method createGraphFromStreamMethod;
		
		public SimpleRootParser(Class rootTargetClass) {
			rootBaseBeanClass = rootTargetClass;
			
			try {
				createGraphMethod = rootBaseBeanClass.getMethod("createGraph", null); // NOI18N

				Class [] paramTypes = new Class[1];
				paramTypes[0] = java.io.InputStream.class;
				createGraphFromStreamMethod = rootBaseBeanClass.getMethod("createGraph", paramTypes); // NOI18N
			} catch(NoSuchMethodException ex) {
//				System.out.println("Exception: " + ex.getClass().getName());
//				ex.printStackTrace(System.out);
			} catch(SecurityException ex) {
//				System.out.println("Exception: " + ex.getClass().getName());
//				ex.printStackTrace(System.out);
			}
		}		
		
		public Object parse(java.io.InputStream stream) {
			Object result = null;
			
			try {
				if(null == stream) {
					// call <createGraph>();
					result = createGraphMethod.invoke(null, null);
				} else {
					try {
						// call <createGraph>(stream);
						Object [] params = new Object[1];
						params[0] = stream;
						createGraphFromStreamMethod.invoke(null, params);
					}
					catch(Throwable t) {
						jsr88Logger.severe("invalid stream for " + rootBaseBeanClass.getName());
						// call <createGraph>();
						result = createGraphMethod.invoke(null, null);
					}
				}
			} catch(Exception ex) {
				// must invocation exception
//				System.out.println("Exception: " + ex.getClass().getName());
//				ex.printStackTrace(System.out);
			}
			
			return result;
		}
	}
 */
	
	public static class SimpleRootFinder implements ConfigFinder {
		private Class rootBaseBeanClass;
		
		public SimpleRootFinder(Class rootTargetClass) {
			rootBaseBeanClass = rootTargetClass;
		}
		
		public Object find(Object obj) {
			Object result = null;
			if(rootBaseBeanClass.equals(obj.getClass())) {
				result = obj;
			}
			return result;
		}
	}	
}
