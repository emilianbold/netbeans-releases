/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.mixeddev;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.util.Pair;

/**
 *
 * @author Petr Kudryavtsev <petrk@netbeans.org>
 */
public final class MixedDevUtils {
    
    public static final String DOT = ".";
    
    public static final String COMMA = ",";
    
    public static final String LPAREN = "(";
    
    public static final String RPAREN = ")";
    
    public static final String SCOPE = "::";
    
    public static final String POINTER = "*";    
    
    public static String stringize(Collection<? extends CharSequence> collection, CharSequence separator) {
        boolean first = true;
        StringBuilder result = new StringBuilder();
        for (CharSequence seq : collection) {
            if (!first) {
                result.append(separator);
            } else {
                first = false;
            }
            result.append(seq);
        }
        return result.toString();
    }
    
    public static String repeat(String pattern, int times) {
        StringBuilder sb = new StringBuilder();
        while (times-- > 0) {
            sb.append(pattern);
        }
        return sb.toString();
    }
    
    public static <K, V> Map<K, V> createMapping(Pair<K, V> ... pairs) {
        Map<K, V> mapping = new HashMap<K, V>();
        for (Pair<K, V> pair : pairs) {
            mapping.put(pair.first(), pair.second());
        }
        return mapping;
    }    
    
    public static interface Converter<F, T> {

        T convert(F from);

    }

    public static <F, T> T[] transform(F[] from, Converter<F, T> converter, Class<T> toClass) {
        T[] to = (T[]) Array.newInstance(toClass, from.length);
        for (int i = 0; i < from.length; i++) {
            to[i] = converter.convert(from[i]);
        }
        return to;
    }

    public static <F, T> List<T> transform(List<F> from, Converter<F, T> converter) {
        List<T> to = new ArrayList<T>(from.size());
        for (F f : from) {
            to.add(converter.convert(f));
        }
        return to;
    }    

    private MixedDevUtils() {
        throw new AssertionError("Not instantiable");
    }
}
