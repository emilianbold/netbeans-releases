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
import java.util.List;

import org.netbeans.modules.schema2beans.BaseBean;

import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.XpathEvent;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.model.exceptions.DDBeanCreateException;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint;


/** This DConfigBean is a child of SunWebApp.
 *
 * Property structure of ServletRef from sun-web-app DTD:
 *
 *		servlet : Servlet[0,n]
 *			servlet-name : String
 *			principal-name : String?
 *			webservice-endpoint : WebserviceEndpoint[0,n]
 *				port-component-name : String
 *				endpoint-address-uri : String?
 *				login-config : LoginConfig?
 *					auth-method : String
 *				transport-guarantee : String?
 *				service-qname : ServiceQname?	[not used - set by server]
 *					namespaceURI : String		[not used - set by server]
 *					localpart : String			[not used - set by server]
 *				tie-class : String?				[not used - set by server]
 *				servlet-impl-class : String?	[not used - set by server]
 *
 *
 * @author  Peter Williams
 */
public class ServletRef extends Base {
    
	/** property event names
	 */
	public static final String SERVLET_NAME = "servletName"; // NOI18N
	public static final String RUN_AS_ROLE_NAME = "runAsRoleName"; // NOI18N
	
	
	/** DDBean that refers to "servlet-name" child of bound DDBean. */
	private DDBean servletNameDD;
	
	/** Holds value of property principalName. */
	private String principalName;
	
	/** Holds list of WebserviceEndpoints. */
	private List webServiceEndpoints;
	
    /** Creates a new instance of ServletRef */
	public ServletRef() {
		setDescriptorElement(bundle.getString("BDN_Servlet"));	// NOI18N	
	}

	/** Override init to enable grouping support for this bean and load name
	 *  field from related DDBean.
	 * @param dDBean DDBean matching this bean
	 * @param parent Parent DConfigBean in the tree
	 */
	protected void init(DDBean dDBean, Base parent) throws ConfigurationException {
		super.init(dDBean, parent);
		
// !PW Disable grouping code for now, spec non-compliance.
//		initGroup(dDBean, parent);
		
		servletNameDD = getNameDD("servlet-name");

		loadFromPlanFile(getConfig());		
	}

	protected String getComponentName() {
		return getServletName();
	}
	
	/** Getter for helpId property
	 * @return Help context ID for this DConfigBean
	 */
	public String getHelpId() {
		return "AS_CFG_Servlet";
	}
	
	/** Getter for property servlet-name.
	 * @return Value of property servlet-name.
	 *
	 */
	public String getServletName() {
		return cleanDDBeanText(servletNameDD);
	}
	
	public String getRunAsRoleName() {
		String roleName = null;
		
		DDBean[] beans = getDDBean().getChildBean("run-as/role-name");	// NOI18N
		if(beans.length > 0) {
			// beans[0] is the run-as element & it's role-name DD
			roleName = beans[0].getText();
		}
		
		return roleName;
	}
	
	/** The DDBean (or one of it's children) that this DConfigBean is bound to
	 *  has changed.
	 *
	 * @param xpathEvent
	 */    
	public void notifyDDChange(XpathEvent xpathEvent) {
		super.notifyDDChange(xpathEvent);

		DDBean eventBean = xpathEvent.getBean();
		
		if(eventBean == servletNameDD) {
			// name changed...
			getPCS().firePropertyChange(SERVLET_NAME, GenericOldValue, getServletName());
			getPCS().firePropertyChange(DISPLAY_NAME, GenericOldValue, getDisplayName());
		} else if(eventBean.getXpath().indexOf("run-as") != -1) {
			getPCS().firePropertyChange(RUN_AS_ROLE_NAME, GenericOldValue, getRunAsRoleName());
		}
	}
	
	/** Getter for property principalName.
	 * @return Value of property principalName.
	 *
	 */
	public String getPrincipalName() {
		return this.principalName;
	}
	
	/** Setter for property principalName.
	 * @param jndiName New value of property principalName.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setPrincipalName(String newPrincipalName) throws java.beans.PropertyVetoException {
		String oldPrincipalName = this.principalName;
		getVCS().fireVetoableChange("principalName", oldPrincipalName, principalName);
		this.principalName = newPrincipalName;
		getPCS().firePropertyChange("principalName", oldPrincipalName, principalName);
	}
	
	/** Getter for property webServiceEndpoint.
	 * @return Value of property webServiceEndpoint.
	 *
	 */
	public List getWebServiceEndpoints() {
		return webServiceEndpoints;
	}
	
	public WebserviceEndpoint getWebServiceEndpoint(int index) {
		return (WebserviceEndpoint) webServiceEndpoints.get(index);
	}
	
	/** Setter for property webServiceEndpoint.
	 * @param webServiceEndpoint New value of property webServiceEndpoint.
	 *
	 * @throws PropertyVetoException
	 *
	 */
    public void setWebServiceEndpoints(List newWebServiceEndpoints) throws java.beans.PropertyVetoException {
        List oldWebServiceEndpoints = webServiceEndpoints;
        getVCS().fireVetoableChange("webServiceEndpoints", oldWebServiceEndpoints, newWebServiceEndpoints);	// NOI18N
        webServiceEndpoints = newWebServiceEndpoints;
        getPCS().firePropertyChange("webServiceEndpoints", oldWebServiceEndpoints, webServiceEndpoints);	// NOI18N
    }
    
	public void addWebServiceEndpoint(WebserviceEndpoint newWebServiceEndpoint) throws java.beans.PropertyVetoException {
		getVCS().fireVetoableChange("webServiceEndpoint", null, newWebServiceEndpoint);	// NOI18N
		if(webServiceEndpoints == null) {
			webServiceEndpoints = new ArrayList();
		}		
		webServiceEndpoints.add(newWebServiceEndpoint);
		getPCS().firePropertyChange("webServiceEndpoint", null, newWebServiceEndpoint );	// NOI18N
	}
	
	public void removeWebServiceEndpoint(WebserviceEndpoint oldWebServiceEndpoint) throws java.beans.PropertyVetoException {
		getVCS().fireVetoableChange("webServiceEndpoint", oldWebServiceEndpoint, null);	// NOI18N
		webServiceEndpoints.remove(oldWebServiceEndpoint);
		getPCS().firePropertyChange("webServiceEndpoint", oldWebServiceEndpoint, null );	// NOI18N
	}	
	
	/* ------------------------------------------------------------------------
	 * Persistence support.  Loads DConfigBeans from previously saved Deployment
	 * plan file.
	 */
	Collection getSnippets() {
		Collection snippets = new ArrayList();
		Snippet snipOne = new DefaultSnippet() {
			public CommonDDBean getDDSnippet() {
				Servlet sg = StorageBeanFactory.getDefault().createServlet();

				// write properties into Servlet bean
				String sn = getServletName();
				if(sn != null) {
					sg.setServletName(sn);
				}

				if(principalName != null && principalName.length() > 0) {
					sg.setPrincipalName(principalName);
				}
				
				WebserviceEndpoint [] wsEndpoints = (WebserviceEndpoint []) 
					Utils.listToArray(getWebServiceEndpoints(), WebserviceEndpoint.class);
				if(wsEndpoints != null) {
					sg.setWebserviceEndpoint(wsEndpoints);
				}				

				return sg;
			}
			
			public boolean hasDDSnippet() {
				if(principalName != null && principalName.length() > 0) {
					return true;
				}
				
				if(webServiceEndpoints != null && webServiceEndpoints.size() > 0) {
					return true;
				}
				
				return false;
			}	
			
/*
			public BaseBean mergeIntoRootDD(BaseBean ddRoot) {
				SunWebApp swa = SunWebApp.createGraph();
				BaseBean newBean = getDDSnippet();
				swa.addValue(SunWebApp.SERVLET, newBean);
				ddRoot.merge(swa, BaseBean.MERGE_UNION);
				return newBean;
			}
 */

/*
			public BaseBean mergeIntoRovingDD(BaseBean ddParent) {
				BaseBean newBean = getDDSnippet();
				ddParent.addValue(SunWebApp.SERVLET, newBean);
				return newBean;
			}
 */
			public String getPropertyName() {
				return SunWebApp.SERVLET;
			}
		};
		
		snippets.add(snipOne);
		return snippets;
	}
	
	private class ServletRefFinder extends NameBasedFinder {
		public ServletRefFinder(String beanName) {
			super(Servlet.SERVLET_NAME, beanName, Servlet.class);
		}
	}	
	
	boolean loadFromPlanFile(SunONEDeploymentConfiguration config) {
		String uriText = getUriText();

		Servlet beanGraph = (Servlet) config.getBeans(uriText, constructFileName(), 
			null, new ServletRefFinder(getServletName()));
		
		clearProperties();
		
		if(beanGraph != null) {
			principalName = beanGraph.getPrincipalName();
			webServiceEndpoints = Utils.arrayToList(beanGraph.getWebserviceEndpoint());			
		} else {
			setDefaultProperties();
		}
		
		return (beanGraph != null);
	}
	
	protected void clearProperties() {
		principalName = null;
		webServiceEndpoints = null;
	}
	
	protected void setDefaultProperties() {
		List epList = getDefaultEndpoints();
		try {
			setWebServiceEndpoints(epList);
		} catch(java.beans.PropertyVetoException ex) {
			System.out.println(ex.getMessage());
		}
	}
	
	private List getDefaultEndpoints() {
		List result = new ArrayList();
		try {
			// !PW FIXME this entire process is unstable on startup of the bean tree.
			// Specifically, if this bean is being created new, then everything should
			// be fine.  However, if this bean is being loaded (but is not found, so
			// it gets default values), the DDBean tree may not be fully constructed
			// at the time this method is executed, causing a NPE during the getDDBeanRoot()
			// call.  (This is why we did the whole 'module ref' thing for URI sharing
			// between Application and individual J2ee Modules.)
			DeployableObject dobj = getConfig().getDeployableObject();

			// !PW FIXME file bug in j2eeserver on path below -- /webservices is what is supposed to work
			DDBeanRoot webServicesRootDD = dobj.getDDBeanRoot("WEB-INF/webservices.xml"); // NOI18N
			if(webServicesRootDD != null) {
				jsr88Logger.warning("ServletRef.getDefaultEndpoints() failed to retrieve webservices DDRoot via xpath.  Using fallback method.");
			}
			
			if(webServicesRootDD != null) {
//				DDBean[] beans = webServicesRootDD.getChildBean("webservice-description/port-component/port-component-name"); // NOI18N
				DDBean[] servletLinkDDs = webServicesRootDD.getChildBean("webservice-description/port-component/service-impl-bean/servlet-link"); // NOI18N
				// First, find the service that corresponds to this servlet
				for(int i = 0; i < servletLinkDDs.length; i++) {
					if(servletNameDD.getText().equals(servletLinkDDs[i].getText())) {
						DDBean[] portNameDDs = servletLinkDDs[i].getChildBean("../../port-component-name"); // NOI18N
						for(int j = 0; j < portNameDDs.length; j++) {
							WebserviceEndpoint ep = StorageBeanFactory.getDefault().createWebserviceEndpoint();
							String pcn = portNameDDs[j].getText();
							ep.setPortComponentName(pcn);
							ep.setEndpointAddressUri(pcn);
							result.add(ep);
						}
					}
				}
			}
		} catch(DDBeanCreateException ex) {
			jsr88Logger.warning(ex.getMessage());
		} catch(java.io.FileNotFoundException ex) {
			jsr88Logger.warning(ex.getMessage());
		} catch(java.lang.NullPointerException ex) {
			// This can happen if the file is being loaded into a new (and thus partially
			// constructed tree.  Nothing to do but catch it and move on.
			jsr88Logger.warning(ex.getMessage());
		}
		
		return result;
	}
    
    /** Api to retrieve the interface definitions for this bean.  Aids usability
     *  during configuration, as the editors can display the existing methds
     *  rather than have the user enter them manually.
     */
    public java.util.List/*ConfigQuery.MethodData*/ getServiceOperations(String portComponentName) {
        /* !PW FIXME Temporary implementation values until plumbing in j2eeserver is worked out.
         */
        java.util.List operationList = new ArrayList();
        operationList.add(new ConfigQuery.MethodData("servlet_ws_operation1", java.util.Arrays.asList(new String [] { "arg1", "arg2" } )));
        operationList.add(new ConfigQuery.MethodData("servlet_ws_operation2", java.util.Arrays.asList(new String [] { "arg1" } )));
        operationList.add(new ConfigQuery.MethodData("servlet_ws_operation3", java.util.Arrays.asList(new String [] { "arg1", "arg2", "arg3" } )));
        return operationList;
    }
}
