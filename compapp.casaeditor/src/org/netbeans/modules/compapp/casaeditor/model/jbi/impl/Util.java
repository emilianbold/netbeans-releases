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

package org.netbeans.modules.compapp.casaeditor.model.jbi.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import javax.xml.namespace.QName;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class Util {
    
    public static List<String> parse(String s) {
        if (s == null) return null;
        StringTokenizer st = new StringTokenizer(s, SEP);
        List<String> result = new ArrayList<String>();
        while(st.hasMoreTokens()) {
            result.add(st.nextToken());
        }
        return result;
    }
    
    public static String toString(Collection<String> tokens) {
        // this is used only in setX/addX/removeX methods so, null when empty
        if (tokens == null || tokens.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String token : tokens) {
            if (first) {
                first = false;
            } else {
                sb.append(SEP);
            }
            sb.append(token);
        }
        return sb.toString();
    }
    
    public static QName getQName(Element el, JBIComponentImpl context) {
        String namespace = el.getNamespaceURI();
        String prefix = el.getPrefix();
        if (namespace == null && context != null) {
            namespace = context.lookupNamespaceURI(prefix);
        }
        String localName = el.getLocalName();
        assert(localName != null);
        if (namespace == null && prefix == null) {
            return new QName(localName);
        } else if (namespace != null && prefix == null) {
            return new QName(namespace, localName);
        } else {
            return new QName(namespace, localName, prefix);
        }
    }
    
    public static final String SEP = Constants.SPACE; //NOI18N
}
