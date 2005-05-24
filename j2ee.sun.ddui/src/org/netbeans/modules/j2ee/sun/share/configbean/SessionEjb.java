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
import java.util.List;
import java.util.Map;

import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.model.exceptions.DDBeanCreateException;
import javax.enterprise.deploy.model.XpathEvent;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurity;
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint;

import org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule.DummyMethod;
import org.netbeans.modules.j2ee.sun.share.configbean.StorageBeanFactory;
import org.netbeans.modules.j2ee.sun.share.configbean.WebserviceOperationListInterface;


/**
 *
 * @author  rajeshwar patil
 */
abstract public class SessionEjb extends BaseEjb implements WebserviceOperationListInterface {

	/** Holds value of property webserviceEndpoint. */
	private WebserviceEndpoint[] webserviceEndpoint;

	/** Creates a new instance of SessionEjb */
	public SessionEjb() {
	}

	/* ------------------------------------------------------------------------
	 * XPath to Factory mapping support
	 */
/*
	private HashMap sessionEjbFactoryMap;

	protected Map getXPathToFactoryMap() {
		if(sessionEjbFactoryMap == null) {
			sessionEjbFactoryMap = (HashMap) super.getXPathToFactoryMap();

			// add child DCB's specific to Stateful Session Beans if any
		}

		return sessionEjbFactoryMap;
	}
 */

	/* ------------------------------------------------------------------------
	 * Persistence support.  Loads DConfigBeans from previously saved Deployment
	 * plan file.
	 */
	protected class SessionEjbSnippet extends BaseEjb.BaseEjbSnippet {
		public CommonDDBean getDDSnippet() {
			Ejb ejb = (Ejb) super.getDDSnippet();

			WebserviceEndpoint[] webSrvcEndPt = getWebserviceEndpoint();
			if (null != webSrvcEndPt) {
				for(int i=0; i<webSrvcEndPt.length; i++){
					ejb.addWebserviceEndpoint((WebserviceEndpoint) webSrvcEndPt[i].clone());
				}
			}
			
			return ejb;
		}

                public boolean hasDDSnippet() {
                    if(super.hasDDSnippet()){
                        return true;
                    }

                    if ((null != getWebserviceEndpoint()) && (getWebserviceEndpoint().length > 0)) {
                        return true;
                    }
                    return false;
                }
	}
	
	Collection getSnippets() {
		Collection snippets = new ArrayList();
		snippets.add(new SessionEjbSnippet());
		return snippets;
	}

	protected void loadEjbProperties(Ejb savedEjb) {
		super.loadEjbProperties(savedEjb);
		
		WebserviceEndpoint[] webSrvcEndPt = savedEjb.getWebserviceEndpoint();
		if(null != webSrvcEndPt){
			webserviceEndpoint = new WebserviceEndpoint[webSrvcEndPt.length];
			for(int i=0; i<webSrvcEndPt.length; i++){
				webserviceEndpoint[i]  =  webSrvcEndPt[i];
			}
		}
	}

	/** Indexed getter for property webserviceEndpoint.
	 * @param index Index of the property.
	 * @return Value of the property at <CODE>index</CODE>.
	 *
	 */
	public WebserviceEndpoint getWebserviceEndpoint(int index) {
		return this.webserviceEndpoint[index];
	}

	/** Getter for property webserviceEndpoint.
	 * @return Value of property webserviceEndpoint.
	 *
	 */
	public WebserviceEndpoint[] getWebserviceEndpoint() {
		return this.webserviceEndpoint;
	}

	/** Indexed setter for property webserviceEndpoint.
	 * @param index Index of the property.
	 * @param webserviceEndpoint New value of the property at <CODE>index</CODE>.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setWebserviceEndpoint(int index, WebserviceEndpoint webserviceEndpoint) throws java.beans.PropertyVetoException {
		WebserviceEndpoint oldWebserviceEndpoint = this.webserviceEndpoint[index];
		this.webserviceEndpoint[index] = webserviceEndpoint;
		try {
			getVCS().fireVetoableChange("webserviceEndpoint", null, null );
		}
		catch(java.beans.PropertyVetoException vetoException ) {
			this.webserviceEndpoint[index] = oldWebserviceEndpoint;
			throw vetoException;
		}
		getPCS().firePropertyChange("webserviceEndpoint", null, null );
	}

	/** Setter for property webserviceEndpoint.
	 * @param webserviceEndpoint New value of property webserviceEndpoint.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setWebserviceEndpoint(WebserviceEndpoint[] webserviceEndpoint) throws java.beans.PropertyVetoException {
		WebserviceEndpoint[] oldWebserviceEndpoint = this.webserviceEndpoint;
		getVCS().fireVetoableChange("webserviceEndpoint", oldWebserviceEndpoint, webserviceEndpoint);
		this.webserviceEndpoint = webserviceEndpoint;
		getPCS().firePropertyChange("webserviceEndpoint", oldWebserviceEndpoint, webserviceEndpoint);
	}


	//Methods called by Customizer
	public void addWebserviceEndpoint(WebserviceEndpoint websrvcEndpoint){
		if(null == webserviceEndpoint){
			webserviceEndpoint = new WebserviceEndpoint[1];
			webserviceEndpoint[0] = websrvcEndpoint;
		} else {
			WebserviceEndpoint[] tempWebsrvcEndpoint = new
				WebserviceEndpoint[webserviceEndpoint.length + 1];
			for(int i=0; i<webserviceEndpoint.length; i++){
				tempWebsrvcEndpoint[i] = webserviceEndpoint[i];
			}
			tempWebsrvcEndpoint[webserviceEndpoint.length] = websrvcEndpoint;
			webserviceEndpoint = 
				new WebserviceEndpoint[tempWebsrvcEndpoint.length];
			for(int i=0; i<tempWebsrvcEndpoint.length; i++){
				webserviceEndpoint[i] = tempWebsrvcEndpoint[i];
			}
	   }
	}


	public void removeWebserviceEndpoint(WebserviceEndpoint websrvcEndpoint){
		if(null != webserviceEndpoint){
			WebserviceEndpoint[] tempWebsrvcEndpoint = new
				WebserviceEndpoint[webserviceEndpoint.length - 1];
			int tempIndex = 0;
			for(int i=0; i<webserviceEndpoint.length; i++){
				if(!webserviceEndpoint[i].equals(websrvcEndpoint)){
					tempWebsrvcEndpoint[tempIndex] = webserviceEndpoint[i];
					tempIndex++;
				}
			}
			webserviceEndpoint = 
				new WebserviceEndpoint[tempWebsrvcEndpoint.length];
			for(int i=0; i<tempWebsrvcEndpoint.length; i++){
				webserviceEndpoint[i] = tempWebsrvcEndpoint[i];
			}
		}
	}


        //Methods of WebserviceOperationListInterface
        //get all the mehods in the given webservice endpoint
        public List getOperations(String portInfoName){
            if(portInfoName.equals("first")){
                //XXX dummy impementation; replace by real one
                //returning a list of java.lang.reflect.Method objects
                //change later to return a list of real *Method* objects.
                //*Method* objects could be netbeans Method object
                ArrayList methods = new ArrayList();
                DummyMethod method = new DummyMethod();
                method.setName("FirstMethod");
                String[] params = {"int"};
                method.setParameterTypes(params);
                method.setIsMethod(true);
                methods.add(method);

                DummyMethod method2 = new DummyMethod();
                method2.setName("SecondMethod");
                String[] params2 = {"int", "String"};
                method2.setParameterTypes(params2);
                method2.setIsMethod(true);
                methods.add(method2);
                
                DummyMethod method3 = new DummyMethod();
                method3.setName("ThirdMethod");
                method3.setIsMethod(false);
                methods.add(method3);
                return methods;
            }else{
                ArrayList methods = new ArrayList();
                DummyMethod method = new DummyMethod();
                method.setName("FirstMethod");
                String[] params = {"String"};
                method.setParameterTypes(params);
                methods.add(method);
                return methods;
            }
        }


	protected void setDefaultProperties() {
                super.setDefaultProperties();
		List epList = getDefaultEndpoints();
                int size = epList.size();
                if(size > 0){
                    try {
                        WebserviceEndpoint[] webserviceEndpoint = new WebserviceEndpoint[size];
                        for(int i=0; i<size; i++){
                            webserviceEndpoint[i] = (WebserviceEndpoint)epList.get(i);
                        }                                
                        setWebserviceEndpoint(webserviceEndpoint);
                    } catch(java.beans.PropertyVetoException ex) {
                            System.out.println(ex.getMessage());
                    }
                }
	}


	private List getDefaultEndpoints() {
		List result = new ArrayList();
		try {
			DeployableObject dobj = getConfig().getDeployableObject();
                        DDBeanRoot webServicesRootDD = dobj.getDDBeanRoot("/webservices.xml"); // NOI18N
			
			if(webServicesRootDD != null) {
				DDBean[] ejbLinkDDs = webServicesRootDD.getChildBean("webservice-description/port-component/service-impl-bean/ejb-link"); // NOI18N
				// First, find the service that corresponds to this ejb
				for(int i = 0; i < ejbLinkDDs.length; i++) {
					if(getEjbName().equals(ejbLinkDDs[i].getText())) {
						DDBean[] portNameDDs = ejbLinkDDs[i].getChildBean("../../port-component-name"); // NOI18N
						for(int j = 0; j < portNameDDs.length; j++) {
							WebserviceEndpoint ep = StorageBeanFactory.getDefault().createWebserviceEndpoint();
							String pcn = portNameDDs[j].getText();
							ep.setPortComponentName(pcn);
							ep.setEndpointAddressUri("webservice/"+pcn); // NOI18N see bug...57034 and 52265
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
        operationList.add(new ConfigQuery.MethodData("ejb_ws_operation1", java.util.Arrays.asList(new String [] { "arg1", "arg2" } )));
        operationList.add(new ConfigQuery.MethodData("ejb_ws_operation2", java.util.Arrays.asList(new String [] { "arg1" } )));
        operationList.add(new ConfigQuery.MethodData("ejb_ws_operation3", java.util.Arrays.asList(new String [] { "arg1", "arg2", "arg3" } )));
        return operationList;
    }


    //this is to Add/Remove jndi-name to/from sun-ejb-jar on
    //addition/deletion of <remote> interface
    public void fireXpathEvent(XpathEvent xpathEvent) {
        //ADD , REMOVE or CHANGE events
        DDBean bean = xpathEvent.getBean();
        String xpath = bean.getXpath();

        if( (xpathEvent.isAddEvent()) || (xpathEvent.isRemoveEvent()) ){
            if("/ejb-jar/enterprise-beans/session/remote".equals(xpath)) {      // NOI18N
                setDirty();
            }
        }
    }
}
