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
 * The format of the ConnectionURL is as follows:
 *   ConnectionURL := protocol://server:host[?options]
 *   protocol := wmq, wmqs
 *   options := key=value[&key=value]*
 *              QueueManager=QM_localhost&
 *              TransportType=JMSC_MQJMS_TP_CLIENT_MQ_TCPIP(1) or 
 *              JMSC.MQJMS_TP_BINDINGS_MQ(0)           
 */
public class WMQConnectionUrl extends ConnectionUrl {
    private UrlParser mUrlParser;
    
    /**
     * Constructor
     * 
     * @param s connection url string
     */
    public WMQConnectionUrl(String s) {
        mUrlParser = new UrlParser(s);
    }

    /**
     * @see com.stc.jmsjca.util.ConnectionUrl#getQueryProperties(java.util.Properties)
     */
    public void getQueryProperties(Properties props) throws ValidationException {
        mUrlParser.getQueryProperties(props);
    }

    /**
     * Returns the parsers that constitute the URL
     * 
     * @return parsers
     */
    public UrlParser getUrlParser() {
        return mUrlParser;
    }
}
