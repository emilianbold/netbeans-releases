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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.wsdlextensions.jms.validator;

import java.util.Properties;

/**
 * ConnectionUrl
 * A URL parser base class
 *
 */
public abstract class ConnectionUrl {
    public static final String PROTOCOL_JMS_PROVIDER_SUN_JAVA_SYSTEM_MQ    = "mq";
    public static final String PROTOCOL_JMS_PROVIDER_WEPSHERE_MQ           = "wmq";
    public static final String PROTOCOL_JMS_PROVIDER_STCMS                 = "stcms";
    public static final String PROTOCOL_JMS_PROVIDER_JBOSS                 = "jnp";
    public static final String PROTOCOL_JMS_PROVIDER_WAVE                  = "tcp";
    public static final String PROTOCOL_JMS_PROVIDER_WEBLOGIC              = "t3";
    public static final String PROTOCOL_GENERIC_JMS_JNDI                   = "jndi";
    
    /**
     * Extracts the key value pairs from the query string
     *
     * @param toAddTo Properties key-value pairs will be added to this properties set
     */
    public abstract void getQueryProperties(Properties toAddTo) throws ValidationException;

    /**
     * Returns the query string in the form of key-value pairs
     *
     * @return Properties
     */
    public Properties getQueryProperties()  throws ValidationException {
        Properties ret = new Properties();
        getQueryProperties(ret);
        return ret;
    }
}
