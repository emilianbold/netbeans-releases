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

import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.model.J2eeApplicationObject;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;

/** This factory creates instances of DCB's that depend on the value of the
 * persistence-type element.
 *
 * Includes attempted default value manipulation for the EjbJarRoot, if the jar 
 * will contain a cmp bean. 
 *
 * This introduces a "stateful"-ness to this factory
 *
 * @author Peter Williams
 * @author vkraemer
 */
public class EntityEjbDCBFactory extends AbstractDCBFactory { // implements DCBFactory {
    
    static final String PERSISTENCE_TYPE_KEY = "persistence-type"; // NOI18N
    static final String CONTAINER = "Container"; // NOI18N
    static final String  BEAN = "Bean"; // NOI18N

	private EjbJarRoot parent;

	/** Create the factory related to a specific EjbJarRoot DCB
	 *
	 * @param parent The EjbJarRoot bean
	 */
	public EntityEjbDCBFactory(EjbJarRoot parent) {
		this.parent = parent;
	}

	protected Class getClass(DDBean ddBean, Base dcbParent) throws ConfigurationException {
		Class dcbClass;
		String testRet[] = ddBean.getText(PERSISTENCE_TYPE_KEY);

		if(null != testRet && 1 == testRet.length && testRet[0].indexOf(CONTAINER) > -1) {
			dcbClass = CmpEntityEjb.class;

			// FIXME !PW is there a better place to put this???
			//
			// if the Jar hasn't had the CmpResourceJndiName property set
			// get a default value and set it.
			//
			// Picking this well should make the server easier to build 
			// cmp beans for.
                        parent.addCmpResourceIfNotPresent();
			/*if(null == parent.getCmpResourceJndiName()) {
				try {
					parent.setCmpResourceJndiName(Utils.getDefaultCmpResourceJndiName(parent));
				}
				catch(java.beans.PropertyVetoException pve) {
					jsr88Logger.severe("bug in Utils.getDefaultCmpResourceJndiName");
				}
			}*/
		} else if(null != testRet && 1 == testRet.length && testRet[0].indexOf(BEAN) > -1) {
			dcbClass = EntityEjb.class;
		} else {
			throw Utils.makeCE("ERR_UnknownPersistenceType", testRet, null);
		}
        
		return dcbClass;
	}
	
	/* 
	public Base createDCB(DDBean ddBean, Base dcbParent) throws ConfigurationException {
		System.out.println("EntityEjbDCBFactory: createDCB");
		System.out.println("dDBean.getXpath()=="+ddBean.getXpath());
		//System.out.println("      .getText()=="+((ddBean.getText() != null) ? ddBean.getText() : "(null)"));

		Base newDCB = null;

		Class dcbClass = Object.class;
		String testRet[] = ddBean.getText("persistence-type");
		if (null != testRet && 1 == testRet.length && testRet[0].indexOf("Container") > -1) {
			newDCB = new CmpEntityEjb();
			dcbClass = CmpEntityEjb.class;
		} else if (null != testRet && 1 == testRet.length && testRet[0].indexOf("Bean") > -1) {
			newDCB = new EntityEjb();
			dcbClass = EntityEjb.class;
		} else {
			System.out.println("Error: Unknown persistence-type element value in deployment descriptor");
			// throw exception?
		}

		if(newDCB != null) {
			newDCB.init(ddBean, dcbParent);
		}
		else {
			newDCB = new Error(this, dcbClass, null);
		}			
		return newDCB;
	}
	**/

    public Base createDCB(J2eeModule module, Base object) throws ConfigurationException {
        throw new UnsupportedOperationException();
    }
}
