/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wsdlextensions.rest.validator;

import org.netbeans.modules.wsdlextensions.rest.RESTAddress;
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
public class RESTAddressURL implements AddressURL {

    private static final ResourceBundle mMessages = ResourceBundle.getBundle("org.netbeans.modules.wsdlextensions.rest.validator.Bundle");

    private String scheme;

    private String host;

    private String port;

    private String url;

    public RESTAddressURL(String url) {
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

    public boolean parse(Collection<Validator.ResultItem> results, Validator validator, RESTAddress target) {
//        // if missing
//        if (url == null || url.trim().length() == 0) {
//            results.add(new Validator.ResultItem(validator, Validator.ResultType.ERROR, target,
//                    mMessages.getString("RESTAddress.MISSING_REST_URL")));
//            return false;
//        }
//
//        // if still the place holder
//        if (url.startsWith(REST_URL_PLACEHOLDER)) {
//            results.add(new Validator.ResultItem(validator, Validator.ResultType.ERROR, target,
//                    mMessages.getString("RESTAddress.REPLACE_REST_URL_PLACEHOLDER_WITH_REAL_URL")));
//            return false;
//        }
//
//        if (!url.startsWith(REST_URL_PREFIX)) {
//            results.add(new Validator.ResultItem(validator, Validator.ResultType.ERROR, target,
//                    mMessages.getString("RESTAddress.INVALID_REST_URL_PREFIX") + url));
//            return false;
//        }
//        scheme = "rest";
//        if (url.length() > REST_URL_PREFIX.length()) {
//            String rest = url.substring(REST_URL_PREFIX.length());
//            if (rest.indexOf(URL_PATH_DELIM) >= 0) {
//                results.add(new Validator.ResultItem(validator, Validator.ResultType.ERROR, target,
//                        mMessages.getString("RESTAddress.INVALID_REST_URL_PATH_NOT_ALLOWED") + url));
//                return false;
//            }
//
//            int l = rest.trim().length();
//            int i = 0;
//            StringBuffer cur = new StringBuffer();
//            int at = 0;
//            int col = 0;
//            List comps = new Vector();
//            while (i < l) {
//                char c = rest.charAt(i);
//                switch (c) {
//                case '\\':
//                    if (i + 1 < l) {
//                        cur.append(url.charAt(i));
//                        i = i + 2;
//                    } else {
//                        cur.append(c);
//                        i++;
//                    }
//                    break;
//                case ':':
//                    col++;
//                    if (col > 1 || cur.length() == 0 /* :password and :port are invalid */) {
//                        // in each part: either user:password
//                        // or host:port, there can be at most 1
//                        // ':' delimiter;
//                        results.add(new Validator.ResultItem(validator, Validator.ResultType.ERROR, target,
//                                mMessages.getString("RESTAddress.MALFORMED_REST_URL") + url));
//                        return false;
//                    }
//                    comps.add(cur.toString());
//                    cur = new StringBuffer();
//                    i++;
//                    break;
//                default:
//                    cur.append(c);
//                    i++;
//                }
//
//            }
//            String tString = rest.trim();
//            String port = tString.substring(tString.indexOf(URL_COLON_DELIM) + 1);
//            comps.add(port);
//
//            switch (comps.size()) {
//            case 1:
//                this.host = (String) comps.get(0);
//                break;
//            case 2:
//                this.host = (String) comps.get(0);
//                this.port = (String) comps.get(1);
//                boolean goodPort = true;
//                if (port != null && port.trim().length() > 0) {
//                    // must be a positive int
//                    try {
//                        int pt = Integer.parseInt(port);
//                        if (pt <= 0)
//                            goodPort = false;
//                    } catch (Exception e) {
//                        goodPort = false;
//                    }
//                }
//
//                if (!goodPort) {
//                    results.add(new Validator.ResultItem(validator, Validator.ResultType.ERROR, target,
//                            mMessages.getString("RESTAddress.INVALID_PORT_IN_URL") + url));
//                    return false;
//                }
//
//                break;
//            default:
//                results.add(new Validator.ResultItem(validator, Validator.ResultType.ERROR, target,
//                        mMessages.getString("RESTAddress.MALFORMED_REST_URL") + url));
//                return false;
//            }
//
//            if (host == null || host.trim().length() == 0) {
//                results.add(new Validator.ResultItem(validator, Validator.ResultType.ERROR, target,
//                        mMessages.getString("RESTAddress.MALFORMED_REST_URL_HOST_REQUIRED") + url));
//                return false;
//            }
//        } else {
//            results.add(new Validator.ResultItem(validator, Validator.ResultType.ERROR, target,
//                    mMessages.getString("RESTAddress.MALFORMED_REST_URL") + url));
//            return false;
//        }
        return true;
    }

}
