/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.text.Document;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;

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
    
    public static void checkType(Class type1, Class type2) {
        if (! type1.isAssignableFrom(type2)) {
            throw new IllegalArgumentException("Invalid requested component type");
        }
    }

    public static final String SEP = " "; //NOI18N
}
