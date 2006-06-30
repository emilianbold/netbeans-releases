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

/*
 * XTestEntityResolver.java
 *
 * Created on November 6, 2001, 1:18 PM
 */

package org.netbeans.xtest;

import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

/**
 *
 * @author  mk97936
 */
public class XTestEntityResolver implements EntityResolver {

    /** Creates new XTestEntityResolver */
    public XTestEntityResolver() {
    }

    // This is only dummy solution is should look for given DTD in
    // filesystem somewhere a provide InputSource for it
    public org.xml.sax.InputSource resolveEntity(String pubID, String sysID) throws SAXException, java.io.IOException {
        // System.out.println("PublicID = " + pubID + "\nSystemID = " + sysID);
        if ( sysID.equals("http://www.netbeans.org/dtds/xtest-cfg-1_0.dtd") ) {
            return new org.xml.sax.InputSource( new java.io.StringReader( "" ) );
        } else if ( sysID.equals("http://www.netbeans.org/dtds/xtest-master-config-1_0.dtd") ) {
            return new org.xml.sax.InputSource( new java.io.StringReader( "" ) );
        } else {
            return null;
        }
    }
    
}
