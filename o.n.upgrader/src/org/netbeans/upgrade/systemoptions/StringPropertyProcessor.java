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

package org.netbeans.upgrade.systemoptions;

import java.util.Iterator;
import java.util.List;

/**
 * @author Radek Matous
 */
class StringPropertyProcessor extends PropertyProcessor {
    StringPropertyProcessor() {
        super("java.lang.String");//NOI18N
    }
    
    void processPropertyImpl(String propertyName, Object value) {
        if ("userProxyHost".equals(propertyName)) {//NOI18N
            addProperty("proxyHttpHost", value.toString());
        } else if ("userProxyPort".equals(propertyName)) {//NOI18N 
            addProperty("proxyHttpPort", value.toString());
        } else if ("userNonProxy".equals(propertyName)) {//NOI18N 
            addProperty("proxyNonProxyHosts", value.toString());
        } else {
            addProperty(propertyName, value == SerParser.NULL ? null : value.toString());
        }
    }
}
