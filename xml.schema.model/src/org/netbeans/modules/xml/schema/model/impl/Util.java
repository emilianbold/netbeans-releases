/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/**
 *
 * @author nn136682
 */
package org.netbeans.modules.xml.schema.model.impl;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import org.netbeans.modules.xml.schema.model.Derivation;
import org.netbeans.modules.xml.schema.model.impl.DerivationsImpl.DerivationSet;

public class Util {

    public static <T extends Enum> T parse(Class<T> type, String s) {
        try {
            Method m = type.getMethod("values", new Class[] {});
            T[] values = (T[]) (m.invoke(null, new Object[0]));
            for (T value : values) {
                if (value.toString().equals(s)) {
                    return value;
                }
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        throw new IllegalArgumentException("Invalid String value " + s);
    }
    
    public static <F extends Enum, T extends Enum> Set<T> convertEnumSet(Class<T> toType, Set<F> values) {
        Set<T> result = new DerivationSet<T>();
        for (F v : values) {
            T t = toType.cast(Enum.valueOf(toType, v.name()));
            result.add(t);
        }
        return result;
    }
    
    public static <T extends Enum> Set<T> valuesOf(Class<T> type, String s) {
//        StringTokenizer tokenizer = new StringTokenizer(s, SEP);
        StringTokenizer tokenizer = new StringTokenizer(s); // to escape tabs and new lines as well
        Set<T> result = new DerivationSet<T>();
        if(tokenizer.countTokens()==0) { // to consider blank ("") string
            T value = parse(type, s);
            result.add(value);
        } else {
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                T value = parse(type, token);
                result.add(value);
            }
        }
        return result;
    }
    
    public static final String SEP = " ";
}
