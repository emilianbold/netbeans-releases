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

import org.netbeans.modules.j2ee.sun.share.Constants;


/**
 *
 * @author Peter Williams
 * @author Vince Kraemer
 */
public abstract class AbstractDCBFactory implements DCBFactory, Constants {
    
	abstract protected Class getClass(DDBean ddBean, Base dcbParent) throws ConfigurationException;

	public Base createDCB(DDBean ddBean, Base dcbParent) throws ConfigurationException {
		Class dcbClass = getClass(ddBean, dcbParent);
//		System.out.println(this.getClass().getName()+"('" + dcbClass.getName() + "': createDCB");
//		System.out.println("  dDBean.getXpath()=="+ddBean.getXpath());
//		System.out.println("        .getText()=="+((ddBean.getText() != null) ? ddBean.getText() : "(null)"));
//		System.out.println("  Parent DCB: "+ dcbParent.getClass().getName());

		Base newDCB = null;
//		Throwable cause = null;

		try {
			newDCB = (Base) dcbClass.newInstance();
			newDCB.init(ddBean, dcbParent);
		} catch(InstantiationException ex) {
			Object [] args = new Object [1];
			args[0] = dcbClass.getName();
			throw Utils.makeCE("ERR_UnexpectedInstantiateException", args, ex);	// NOI18N
		} catch(IllegalAccessException ex) {
			Object [] args = new Object [1];
			args[0] = dcbClass.getName();
			throw Utils.makeCE("ERR_UnexpectedIllegalAccessException", args, ex);	// NOI18N
		} catch (RuntimeException ex) {
            throw Utils.makeCE("ERR_UnexpectedRuntimeException", null, ex);	// NOI18N
		}
		
//		if (null == newDCB) {
//			try {
//				//getErrorClass().
//				newDCB = new Error(); 
//				newDCB.init(ddBean, dcbParent); 
//			} catch (Throwable t) {
//				String message = "Could not instantiate the error class bean: Error";
//				jsr88Logger.severe(message);
//				throw new ConfigurationException(message);
//			}
//		} else {
//			if (null != cause)
//				jsr88Logger.throwing("a","b",cause);
//		}

		return newDCB;
	}
}
