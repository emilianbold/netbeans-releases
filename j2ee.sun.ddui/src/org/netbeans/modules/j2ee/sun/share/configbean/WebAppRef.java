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

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.app.SunApplication;
import org.netbeans.modules.j2ee.sun.dd.api.app.Web;


/** This bean is contained by an AppRoot and represents a reference to a web
 *  module contained in that application
 *
 *  @auther Peter Williams
 */
public class WebAppRef extends BaseModuleRef {

	/** -----------------------------------------------------------------------
	 * Initialization
	 */
	
	/** Creates new WebAppRef 
	 */
	public WebAppRef() {
	}
	
	protected void init(DDBean dDBean, Base parent) throws ConfigurationException {
		super.init(dDBean, parent);

		contextRootDD = getNameDD("context-root");
		loadFromPlanFile(getConfig());
	}
	
	/** Called from BaseModuleRef.init() to get the correct module URI field
	 *  for the reference object.
	 */
	protected void initModuleUri(DDBean dDBean) {
		DDBean[] uriBeans = dDBean.getChildBean("web-uri");
		if(uriBeans.length > 0) {
			setModuleUri(uriBeans[0]);
		} else {
			setModuleUri(null);
		}
	}
	
	/** -----------------------------------------------------------------------
	 * JSR-88 Interface support
	 */
	
	// All JSR-88 methods inherited from base
	
	/** -----------------------------------------------------------------------
	 * Properties
	 */

	/** Holds value of property webUri. */
//	private DDBean webUri = null;
	
	/** Holds value of property contextRoot. */
	private DDBean contextRootDD = null;
	private String contextRoot;
	
	/** Getter for property webURI.
	 * @return Value of property webURI.
	 *
	 */
	public String getWebUri() {
		return getModuleUri();
	}
	
	/** Getter for property contextRoot.
	 * @return Value of property contextRoot.
	 *
	 */
	public String getContextRoot() {
		String result = null;
		
		/* if a contextRoot for this module has not been set, get the context
		 * root from the application.xml, otherwise, use the explicit setting
		 */
		if(contextRoot == null || contextRoot.length() == 0 && contextRootDD != null) {
			result = contextRootDD.getText();
		} else {
			result = contextRoot;
		}
		
		return result;
	}
	
	/** Setter for property contextRoot.
	 * @param newContextRoot New value of property contextRoot.
	 * @throws PropertyVetoException if the property change is vetoed
	 */
	 public void setContextRoot(String newContextRoot) throws java.beans.PropertyVetoException {
                if (newContextRoot!=null){
                    newContextRoot = newContextRoot.replace (' ', '_'); //NOI18N
                }
                if (newContextRoot!=null){ //see bug 56280
                    try{
                        String result="";
                        String s[] = newContextRoot.split("/");
                        for (int i=0;i<s.length;i++){
                            result=result+java.net.URLEncoder.encode(s[i], "UTF-8");
                            if (i!=s.length -1)
                                result=result+"/";
                        }
                        newContextRoot= result;
                    }
                    catch (Exception e){
                        
                    }
                }
                String oldContextRoot = contextRoot;
		getVCS().fireVetoableChange("contextRoot", oldContextRoot, newContextRoot);
		contextRoot = newContextRoot;
		getPCS().firePropertyChange("contextRoot", oldContextRoot, contextRoot);		 
	 }


        public String getHelpId() {
            return "AS_CFG_WebAppRef";                                  //NOI18N
        }


	/** -----------------------------------------------------------------------
	 * Persistance support
	 */
	 Collection getSnippets() {
		Collection snippets = new ArrayList();
		Snippet snipOne = new DefaultSnippet() {
			
			public CommonDDBean getDDSnippet() {
				Web web = StorageBeanFactory.getDefault().createWeb();
				
				web.setWebUri(getWebUri());
				web.setContextRoot(getContextRoot());
				
				return web;
			}
			
			public String getPropertyName() {
				return SunApplication.WEB;
			}			
		};
		
		snippets.add(snipOne);
		return snippets;
	}

	private class WebAppRefFinder implements ConfigFinder {
		public Object find(Object obj) {
			Web result = null;
			String webUri = getWebUri();
			
			if(obj instanceof SunApplication && webUri != null) {
				SunApplication sa = (SunApplication) obj;
				Web [] webModules = sa.getWeb();
				
				for(int i = 0; i < webModules.length; i++) {
					if(webUri.compareTo(webModules[i].getWebUri()) == 0) {
						result = webModules[i];
						break;
					}
				}
			}
			
			return result;
		}
	}
		
	boolean loadFromPlanFile(SunONEDeploymentConfiguration config) {
		String uriText = getUriText();
		
		Web beanGraph = (Web) config.getBeans(
			uriText, constructFileName(), null, new WebAppRefFinder());
		
		clearProperties();
		
		if(beanGraph != null) {
			String cr = beanGraph.getContextRoot();
			if(Utils.notEmpty(cr) && cr.compareTo(getContextRoot()) != 0) {
				contextRoot = cr;
			}
		} else {
			setDefaultProperties();
		}
		
		return (beanGraph != null);
	}	
	
	protected void clearProperties() {
		contextRoot = null;
	}
	
	protected void setDefaultProperties() {
		// no defaults
	}	
}
