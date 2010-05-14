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
/*
 * HL7AddressURL.java
 *
 * Created on October 10, 2006, 1:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.wsdlextensions.hl7.validator;

import org.netbeans.modules.wsdlextensions.hl7.HL7Address;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;

/**
 *
 * @author raghunadh.teegavarapu@sun.com
 */
public class HL7AddressURL implements AddressURL {

    private static final ResourceBundle mMessages = ResourceBundle.getBundle("org.netbeans.modules.wsdlextensions.hl7.validator.Bundle");

    private String scheme;

    private String host;

    private String port;

    private String url;

    public HL7AddressURL(String url) {
        this.url = url;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public boolean parse(Collection<Validator.ResultItem> results, Validator validator, HL7Address target) {
        // if missing
        if (url == null || url.trim().length() == 0) {
            results.add(new Validator.ResultItem(validator, Validator.ResultType.ERROR, target,
                    mMessages.getString("HL7Address.MISSING_HL7_URL")));
            return false;
        }

        // if still the place holder
        if (url.startsWith(HL7_URL_PLACEHOLDER)) {
            results.add(new Validator.ResultItem(validator, Validator.ResultType.ERROR, target,
                    mMessages.getString("HL7Address.REPLACE_HL7_URL_PLACEHOLDER_WITH_REAL_URL")));
            return false;
        }

        if (!url.startsWith(HL7_URL_PREFIX)) {
            results.add(new Validator.ResultItem(validator, Validator.ResultType.ERROR, target,
                    mMessages.getString("HL7Address.INVALID_HL7_URL_PREFIX") + url));
            return false;
        }
        scheme = "hl7";
        if (url.length() > HL7_URL_PREFIX.length()) {
            String rest = url.substring(HL7_URL_PREFIX.length());
            if (rest.indexOf(URL_PATH_DELIM) >= 0) {
                results.add(new Validator.ResultItem(validator, Validator.ResultType.ERROR, target,
                        mMessages.getString("HL7Address.INVALID_HL7_URL_PATH_NOT_ALLOWED") + url));
                return false;
            }

            int l = rest.trim().length();
            int i = 0;
            StringBuffer cur = new StringBuffer();
            int at = 0;
            int col = 0;
            List comps = new Vector();
            while (i < l) {
                char c = rest.charAt(i);
                switch (c) {
                case '\\':
                    if (i + 1 < l) {
                        cur.append(url.charAt(i));
                        i = i + 2;
                    } else {
                        cur.append(c);
                        i++;
                    }
                    break;
                case ':':
                    col++;
                    if (col > 1 || cur.length() == 0 /* :password and :port are invalid */) {
                        // in each part: either user:password
                        // or host:port, there can be at most 1
                        // ':' delimiter;
                        results.add(new Validator.ResultItem(validator, Validator.ResultType.ERROR, target,
                                mMessages.getString("HL7Address.MALFORMED_HL7_URL") + url));
                        return false;
                    }
                    comps.add(cur.toString());
                    cur = new StringBuffer();
                    i++;
                    break;
                default:
                    cur.append(c);
                    i++;
                }

            }
            String tString = rest.trim();
            String port = tString.substring(tString.indexOf(URL_COLON_DELIM) + 1);
            comps.add(port);

            switch (comps.size()) {
            case 1:
                this.host = (String) comps.get(0);
                break;
            case 2:
                this.host = (String) comps.get(0);
                this.port = (String) comps.get(1);
                boolean goodPort = true;
                if (port != null && port.trim().length() > 0) {
                    // must be a positive int
                    try {
                        int pt = Integer.parseInt(port);
                        if (pt <= 0)
                            goodPort = false;
                    } catch (Exception e) {
                        goodPort = false;
                    }
                }

                if (!goodPort) {
                    results.add(new Validator.ResultItem(validator, Validator.ResultType.ERROR, target,
                            mMessages.getString("HL7Address.INVALID_PORT_IN_URL") + url));
                    return false;
                }

                break;
            default:
                results.add(new Validator.ResultItem(validator, Validator.ResultType.ERROR, target,
                        mMessages.getString("HL7Address.MALFORMED_HL7_URL") + url));
                return false;
            }

            if (host == null || host.trim().length() == 0) {
                results.add(new Validator.ResultItem(validator, Validator.ResultType.ERROR, target,
                        mMessages.getString("HL7Address.MALFORMED_HL7_URL_HOST_REQUIRED") + url));
                return false;
            }
        } else {
            results.add(new Validator.ResultItem(validator, Validator.ResultType.ERROR, target,
                    mMessages.getString("HL7Address.MALFORMED_HL7_URL") + url));
            return false;
        }
        return true;
    }

}
