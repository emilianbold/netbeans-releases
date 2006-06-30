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

import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.model.J2eeApplicationObject;

/**
 *
 * @author Peter Williams
 */
public class SessionEjbDCBFactory extends AbstractDCBFactory {
    static final String SESSION_TYPE_KEY = "session-type"; // NOI18N
    static final String STATELESS = "Stateless"; // NOI18N
    static final String STATEFUL = "Stateful"; // NOI18N
    
	protected Class getClass(DDBean ddBean, Base dcbParent) throws ConfigurationException {
		Class retVal = Object.class;
        String testRet[] = ddBean.getText(SESSION_TYPE_KEY);
        if(null != testRet && testRet.length == 1 && testRet[0].indexOf(STATELESS) > -1) {
            retVal = StatelessEjb.class;
        } else if (null != testRet && 1 == testRet.length && testRet[0].indexOf(STATEFUL) > -1) {
            retVal = StatefulEjb.class;
        } else {
            throw Utils.makeCE("ERR_UnknownSessionType", testRet, null);
        }

		return retVal;
	}
}
