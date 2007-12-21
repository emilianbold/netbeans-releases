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
package org.netbeans.modules.xslt.model.impl;

import javax.xml.namespace.QName;


/**
 * @author ads
 *
 */
class QNameBuilder {
    
    public static QName createQName( XslComponentImpl component ,
            XslAttributes attribute) {
        String str = component.getAttribute(attribute);
        return createQName(component, str);
    }
    
    public static QName createQName( XslComponentImpl component, String value ){
        if (value == null) {
            return null;
        }
        String[] splited = new String[2];
        splitQName( value , splited );
        
        String uri = component.lookupNamespaceURI( splited[0] , true );
        if (uri == null && splited[0] == null) {
            // prefix isn't defined; default namespace isn't found
            return new QName(splited[1]);
        }
        if ( uri != null && splited[0] == null) {
            // prefix isn't defined but the default namspace is found.
            // default namespace is used
            return new QName(uri, splited[1]);
        }
        return new QName(uri, splited[1], splited[0]);
    }
    
    public static void splitQName( String qName , String[] result ){
        assert qName!=null;
        assert result != null;
        String[] parts = qName.split(":"); //NOI18N
        String prefix;
        String localName;
        if (parts.length == 2) {
            prefix = parts[0];
            localName = parts[1];
        } else {
            prefix = null;
            localName = parts[0];
        }
        if ( result.length >0 ){
            result[0] = prefix;
        }
        if ( result.length >1 ){
            result[1]=localName;
        }
    }
}
