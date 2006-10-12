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
import java.util.List;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.XpathEvent;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;

import org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule.DummyMethod;
import org.openide.ErrorManager;


/**
 *
 * @author  rajeshwar patil
 */
abstract public class SessionEjb extends BaseEjb implements WebserviceOperationListInterface {

	/** Creates a new instance of SessionEjb */
	public SessionEjb() {
	}

    
    /* ------------------------------------------------------------------------
     * Persistence support.  Loads DConfigBeans from previously saved Deployment
     * plan file.
     */
    protected class SessionEjbSnippet extends BaseEjb.BaseEjbSnippet {
        public CommonDDBean getDDSnippet() {
            Ejb ejb = (Ejb) super.getDDSnippet();
            return ejb;
        }

        public boolean hasDDSnippet() {
            if(super.hasDDSnippet()) {
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
    }

    protected void clearProperties() {
        super.clearProperties();
    }
    
    protected void setDefaultProperties() {
        super.setDefaultProperties();
    }
    
    public void fireXpathEvent(XpathEvent xpathEvent) {
        super.fireXpathEvent(xpathEvent);
//		dumpNotification("SessionEjb.fireXpathEvent", xpathEvent);
        
        DDBean eventBean = xpathEvent.getBean();
        String xpath = eventBean.getXpath();

        if("/ejb-jar/enterprise-beans/session/remote".equals(xpath)) { // NOI18N
            try {
                if(xpathEvent.isAddEvent()) {
                    // Only set JNDI name automatically for pre-EJB 3.0 beans.  We don't use
                    // requiresJndiName() here because that we only want to confirm the version
                    // and that method has more logic.
                    if(EjbJarVersion.EJBJAR_2_1.compareTo(getJ2EEModuleVersion()) >= 0 ) {
                        setJndiName(getDefaultJndiName());
                    }
                } else if(xpathEvent.isRemoveEvent()) {
                    setJndiName(null);
                }
            } catch(PropertyVetoException ex) {
                // should never happen.
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
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
}
