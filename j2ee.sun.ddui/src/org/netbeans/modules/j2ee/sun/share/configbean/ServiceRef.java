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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.model.exceptions.DDBeanCreateException;
import javax.enterprise.deploy.model.XpathEvent;

import org.openide.ErrorManager;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;
import org.netbeans.modules.j2ee.sun.dd.api.common.CallProperty;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurityBinding;
import org.netbeans.modules.j2ee.sun.dd.api.common.PortInfo;
import org.netbeans.modules.j2ee.sun.dd.api.common.WsdlPort;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;

import org.netbeans.modules.j2ee.sun.share.configbean.Base.DefaultSnippet;


/** Property structure of ServiceRef from DTD:
 *
 *		service-ref : ServiceRef[0,n]
 *			service-ref-name : String
 *			port-info : PortInfo[0,n]
 *				service-endpoint-interface : String?
 *				wsdl-port : WsdlPort?
 *					namespaceURI : String
 *					localpart : String
 *				stub-property : StubProperty[0,n]
 *					name : String
 *					value : String
 *				call-property : CallProperty[0,n]
 *					name : String
 *					value : String
 *			call-property : CallProperty[0,n]
 *				name : String
 *				value : String
 *			wsdl-override : String?
 *			service-impl-class : String?
 *			service-qname : ServiceQname?
 *				namespaceURI : String
 *				localpart : String
 *
 *
 * @author  Peter Williams
 * @version %I%, %G%
 */
public class ServiceRef extends Base {

	/** property event names
	 */
	public static final String SERVICE_REF_NAME = "serviceRefName"; // NOI18N
	
	/** DDBean that refers to "service-ref-name" child of bound DDBean. */
	private DDBean serviceRefNameDD;
	
	/** Holds value of property ports */
	private List ports;

	/** Holds value of property ports */
	private List callProperties;
	
	/** Holds value of property wsdlOverride. */
	private String wsdlOverride;
	
	/** Holds value of property serviceImplClass. */
//	private String serviceImplClass;	// derived during deployment

	/** Holds value of property serviceQName. */
//	private ServiceQname serviceQName;	// derived during deployment
	
	/** Creates a new instance of ServiceRef */
	public ServiceRef() {
		setDescriptorElement(bundle.getString("BDN_ServiceRef"));	// NOI18N
	}

	/** Override init to enable grouping support for this bean and load name
	 *  field from related DDBean.
	 * @param dDBean DDBean matching this bean
	 * @param parent Parent DConfigBean in the tree
	 */
	protected void init(DDBean dDBean, Base parent) throws ConfigurationException {
		super.init(dDBean, parent);
		dDBean.addXpathListener(dDBean.getXpath(), this);
// !PW Disable grouping code for now, spec non-compliance.
//		initGroup(dDBean, parent);
		
		serviceRefNameDD = getNameDD("service-ref-name");       //NOI18N
		
        updateNamedBeanCache(SunWebApp.SERVICE_REF);
		
		loadFromPlanFile(getConfig());		
	}
	
	protected String getComponentName() {
		return getServiceRefName();
	}
	
	/** Getter for helpId property
	 * @return Help context ID for this DConfigBean
	 */
	public String getHelpId() {
		return "AS_CFG_ServiceRefGeneral";
	}	
	
	/** The DDBean (or one of it's children) that this DConfigBean is bound to
	 *  has changed.
	 *
	 * @param xpathEvent
	 */    
	public void notifyDDChange(XpathEvent xpathEvent) {
		super.notifyDDChange(xpathEvent);

		if(serviceRefNameDD == xpathEvent.getBean()) {
			// name changed...
			getPCS().firePropertyChange(SERVICE_REF_NAME, "", getServiceRefName());
			getPCS().firePropertyChange(DISPLAY_NAME, "", getDisplayName());

            updateNamedBeanCache(SunWebApp.SERVICE_REF);
		}
	}	

	/** Getter for property service-ref-name.
	 * @return Value of property service-ref-name.
	 *
	 */
	public String getServiceRefName() {
		return cleanDDBeanText(serviceRefNameDD);
	}
	
	/* ------------------------------------------------------------------------
	 * Persistence support.  Loads DConfigBeans from previously saved Deployment
	 * plan file.
	 */
	Collection getSnippets() {
		Collection snippets = new ArrayList();
		Snippet snipOne = new DefaultSnippet() {
			public CommonDDBean getDDSnippet() {
				org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef serviceRef = 
                    getConfig().getStorageFactory().createServiceRef();
                String version = getAppServerVersion().getEjbJarVersionAsString();

				// write properties into Servlet bean
				String sn = getServiceRefName();
				if(sn != null) {
					serviceRef.setServiceRefName(sn);
				}
				
				if(wsdlOverride != null && wsdlOverride.length() > 0) {
					serviceRef.setWsdlOverride(wsdlOverride);
				}

				PortInfo [] portInfos = (PortInfo []) 
					Utils.listToArray(getPortInfos(), PortInfo.class, version);
				if(portInfos != null) {
					serviceRef.setPortInfo(portInfos);
				}
				
				CallProperty [] callProps = (CallProperty []) 
					Utils.listToArray(getCallProperties(), CallProperty.class, version);
				if(callProps != null) {
					serviceRef.setCallProperty(callProps);
				}				
				
				return serviceRef;
			}
			
			public boolean hasDDSnippet() {
				if(wsdlOverride != null && wsdlOverride.length() > 0) {
					return true;
				}
				
				if(ports != null && ports.size() > 0) {
					return true;
				}
				
				if(callProperties != null && callProperties.size() > 0) {
					return true;
				}
				
				return false;
			}	
			
			public String getPropertyName() {
				return SunWebApp.SERVICE_REF;
			}
		};
		
		snippets.add(snipOne);
		return snippets;
	}
	
	private class ServiceRefFinder extends NameBasedFinder {
		public ServiceRefFinder(String beanName) {
			super(org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef.SERVICE_REF_NAME,
				beanName, org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef.class);
		}
	}	
	
	boolean loadFromPlanFile(SunONEDeploymentConfiguration config) {
		String uriText = getUriText();

		org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef beanGraph = 
			(org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef) config.getBeans(uriText, 
			constructFileName(), getParser(), new ServiceRefFinder(getServiceRefName()));
		
		clearProperties();
		
		if(beanGraph != null) {
			wsdlOverride = beanGraph.getWsdlOverride();
			ports = Utils.arrayToList(beanGraph.getPortInfo());
			callProperties = Utils.arrayToList(beanGraph.getCallProperty());			
		} else {
			setDefaultProperties();
		}
		
		return (beanGraph != null);
	}
	
	protected void clearProperties() {
		wsdlOverride = null;
		ports = null;
		callProperties = null;			
	}
	
	protected void setDefaultProperties() {
        setDefaultPorts();
        getConfig().getMasterDCBRoot().setDirty();
	}
    
    private void setDefaultPorts() {
		List portInfoList = getDefaultPortInfos();
		try {
			setPortInfos(portInfoList);
		} catch(java.beans.PropertyVetoException ex) {
			// Should not happen.
			ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
		}
    }

    private List getDefaultPortInfos() {
        List result = new ArrayList();
        try {
            DeployableObject dobj = getConfig().getDeployableObject();

            // !PW FIXME this does not work for ejb-jar.xml!!!!
            DDBeanRoot webRootDD = dobj.getDDBeanRoot("WEB-INF/web.xml"); // NOI18N
            if(webRootDD == null) {
                jsr88Logger.warning("ServiceRef.getDefaultServiceRefs() failed to retrieve web DDRoot via xpath.  Using fallback method."); 
            }

            if(webRootDD != null) {
                DDBean[] serviceRefNameDDBean = 
                    webRootDD.getChildBean("web-app/service-ref/service-ref-name"); //NOI18N
                // First, find the service-ref that corresponds to this service
                for(int i = 0; i < serviceRefNameDDBean.length; i++) {
                    if(serviceRefNameDD.getText().equals(serviceRefNameDDBean[i].getText())) {
                        DDBean[] portComponentRefDDs = 
                            serviceRefNameDDBean[i].getChildBean("../port-component-ref"); //NOI18N
                        String serviceEndpointInterface = null;
                        for(int j = 0; j < portComponentRefDDs.length; j++) {
                            DDBean[] serviceEndpointInterfaceDD = 
                                portComponentRefDDs[j].getChildBean("service-endpoint-interface");  //NOI18N
                            if(serviceEndpointInterfaceDD != null && serviceEndpointInterfaceDD.length > 0) {
                                serviceEndpointInterface = serviceEndpointInterfaceDD[0].getText();
                                if(serviceEndpointInterface != null){
                                    PortInfo portInfo = getConfig().getStorageFactory().createPortInfo();
                                    portInfo.setServiceEndpointInterface(serviceEndpointInterface);
                                    result.add(portInfo);
                                }
                            }
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

    /* ------------------------------------------------------------------------
     * Property getter/setter support
     */

    /** Getter for property wsdlOverride.
     * @return Value of property wsdlOverride.
     */
    public String getWsdlOverride() {
        return wsdlOverride;
    }

    /** Setter for property wsdlOverride.
     * @param wsdlOverride New value of property wsdlOverride.
     *
     * @throws PropertyVetoException
     */
    public void setWsdlOverride(String newWsdlOverride) throws java.beans.PropertyVetoException {
        String oldWsdlOverride = wsdlOverride;
        getVCS().fireVetoableChange("wsdlOverride", oldWsdlOverride, newWsdlOverride);
        wsdlOverride = newWsdlOverride;
        getPCS().firePropertyChange("wsdlOverride", oldWsdlOverride, wsdlOverride);
    }

    /** Getter for property portInfos.
     * @return Value of property portInfos.
     *
     */
    public List getPortInfos() {
        return ports;
    }

    public PortInfo getPortInfo(int index) {
        return (PortInfo) ports.get(index);
    }

    /** Setter for property portInfos.
     * @param newPorts New value of property portInfos.
     *
     * @throws PropertyVetoException
     *
     */
    public void setPortInfos(List newPorts) throws java.beans.PropertyVetoException {
        List oldPorts = ports;
        getVCS().fireVetoableChange("portInfos", oldPorts, newPorts);	// NOI18N
        ports = newPorts;
        getPCS().firePropertyChange("portInfos", oldPorts, ports);	// NOI18N
    }

    public void addPortInfo(PortInfo newPortInfo) throws java.beans.PropertyVetoException {
        getVCS().fireVetoableChange("portInfo", null, newPortInfo);	// NOI18N
        if(ports == null) {
            ports = new ArrayList();
        }
        ports.add(newPortInfo);
        getPCS().firePropertyChange("portInfo", null, newPortInfo );	// NOI18N
    }

    public void removePortInfo(PortInfo oldPortInfo) throws java.beans.PropertyVetoException {
        getVCS().fireVetoableChange("portInfo", oldPortInfo, null);	// NOI18N
        ports.remove(oldPortInfo);
        getPCS().firePropertyChange("portInfo", oldPortInfo, null );	// NOI18N
    }

    /** Note: For now, this method applies a copy of the specified binding to each port
     *  currently configured on this service reference, if any.
     * 
     * @deprecated
     */
    public void setMessageSecurityBinding(MessageSecurityBinding newBinding) throws java.beans.PropertyVetoException, VersionNotSupportedException {
        if(ports != null && ports.size() > 0) {
            MessageSecurityBinding oldBinding;
            oldBinding = ((PortInfo) ports.get(0)).getMessageSecurityBinding();
            getVCS().fireVetoableChange("messageSecurityBinding", oldBinding, newBinding);   // NOI18N
            Iterator portIterator = ports.iterator();
            while(portIterator.hasNext()) {
                PortInfo portInfo = (PortInfo) portIterator.next();
                portInfo.setMessageSecurityBinding((newBinding != null) ? (MessageSecurityBinding) newBinding.clone() : null);
            }
            getPCS().firePropertyChange("messageSecurityBinding", oldBinding, newBinding );  // NOI18N
        }
    }

    /** Sets the MessageSecurityBinding for the specified port of this ServiceRef.  If the port
     *  is not found, a new entry is created for it.
     */
    public void setMessageSecurityBinding(String namespaceURI, String localpart, MessageSecurityBinding newBinding) 
    throws java.beans.PropertyVetoException, VersionNotSupportedException {
        PortInfo thePortInfo = null;
        
        /// find the right port...
        if(ports != null) {
            Iterator iter = ports.iterator();
            while(iter.hasNext()) {
                PortInfo portInfo = (PortInfo) iter.next();
                WsdlPort port = portInfo.getWsdlPort();
                if(port != null && namespaceURI.equals(port.getNamespaceURI()) && localpart.equals(port.getLocalpart())) {
                    thePortInfo = portInfo;
                    break;
                }
            }
        } else {
            // No list of port-info entries yet, make one.
            ports = new ArrayList();
        }
        
        if(thePortInfo == null) {
            // if we didn't find the port, make one.
            thePortInfo = getConfig().getStorageFactory().createPortInfo();
            WsdlPort wsdlPort = thePortInfo.newWsdlPort();
            wsdlPort.setNamespaceURI(namespaceURI);
            wsdlPort.setLocalpart(localpart);
            thePortInfo.setWsdlPort(wsdlPort);
            ports.add(thePortInfo);
        }
        
        // finally set the binding.
        MessageSecurityBinding oldBinding = thePortInfo.getMessageSecurityBinding();
        getVCS().fireVetoableChange("messageSecurityBinding", oldBinding, newBinding);   // NOI18N
        thePortInfo.setMessageSecurityBinding((newBinding != null) ? (MessageSecurityBinding) newBinding.clone() : null);
        getPCS().firePropertyChange("messageSecurityBinding", oldBinding, newBinding );  // NOI18N
    }
    
    /** Getter for property callProperties.
     * @return Value of property callProperties.
     *
     */
    public List getCallProperties() {
        return callProperties;
    }

    public CallProperty getCallProperty(int index) {
        return (CallProperty) ports.get(index);
    }

    /** Setter for property callProperties.
     * @param newPorts New value of property callProperties.
     *
     * @throws PropertyVetoException
     *
     */
    public void setCallProperties(List newCallProperties) throws java.beans.PropertyVetoException {
        List oldCallProperties = callProperties;
        getVCS().fireVetoableChange("callProperties", oldCallProperties, newCallProperties);	// NOI18N
        callProperties = newCallProperties;
        getPCS().firePropertyChange("callProperties", oldCallProperties, callProperties);	// NOI18N
    }

    public void addCallProperty(CallProperty newCallProperty) throws java.beans.PropertyVetoException {
        getVCS().fireVetoableChange("callProperty", null, newCallProperty);	// NOI18N
        if(callProperties == null) {
            callProperties = new ArrayList();
        }		
        callProperties.add(newCallProperty);
        getPCS().firePropertyChange("callProperty", null, newCallProperty );	// NOI18N
    }

    public void removeCallProperty(CallProperty oldCallProperty) throws java.beans.PropertyVetoException {
        getVCS().fireVetoableChange("callProperty", oldCallProperty, null);	// NOI18N
        callProperties.remove(oldCallProperty);
        getPCS().firePropertyChange("callProperty", oldCallProperty, null );	// NOI18N
    }

    public void fireXpathEvent(XpathEvent xpathEvent) {
        String xpath = xpathEvent.getBean().getXpath();
        if(xpath.equals("/web-app/service-ref/port-component-ref")){ //NOI18N
            setDefaultPorts();
        }
    }

    /** Api to retrieve the interface definitions for this bean.  Aids usability
     *  during configuration, as the editors can display the existing methds
     *  rather than have the user enter them manually.
     */
    public java.util.List/*ConfigQuery.MethodData*/ getServiceOperations(DDBean portInfoDD) {
        /* !PW FIXME Temporary implementation values until plumbing in j2eeserver is worked out.
         */
        java.util.List operationList = new ArrayList();
        operationList.add(new ConfigQuery.MethodData("pi_operation1", java.util.Arrays.asList(new String [] { "arg1", "arg2" } )));
        operationList.add(new ConfigQuery.MethodData("pi_operation2", java.util.Arrays.asList(new String [] { "arg1" } )));
        operationList.add(new ConfigQuery.MethodData("pi_operation3", java.util.Arrays.asList(new String [] { "arg1", "arg2", "arg3" } )));
        return operationList;
    }
}
