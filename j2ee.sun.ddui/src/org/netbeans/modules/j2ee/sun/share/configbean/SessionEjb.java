/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
            return super.hasDDSnippet();
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
