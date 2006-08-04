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
 * EMapFormat.java -- synopsis.
 *
 *
 *
 *
 */
package org.netbeans.mobility.antext;

import java.text.Format;
import java.util.Map;
import java.util.StringTokenizer;

/** A text format similar to <code>MessageFormat</code>
 * but using string rather than numeric keys.
 * You might use use this formatter like this:
 * <pre>MapFormat.format("Hello {name}", map);</pre>
 *
 * It also allows conditional formatting:
 * <pre>MapFormat.format("java {classpath|-classpath {classpath}}", map);</pre>
 *
 * It also allows more than one condition (in positive and negative form).
 * Following example shows "Show" if and only if map contains "Positive" key and does not contain "Negative" key:
 * <pre>MapFormat.format("{Positive,!Negative|Show}", map);</pre>
 *
 * produces either:
 * java -classpath <value_of_classpath>
 * or
 * java
 *
 * @author  Martin Ryzl
 */
public class EMapFormat extends Format
{
    
    final private Map<String,Object> map;
    
    /**
     * Creates a new instance of EMapFormat.
     * @param args Map with key-value pairs to replace.
     */
    EMapFormat(Map<String,Object> args)
    {
        super();
        this.map = args;
    }
    
    /** Process a pattern.
     * @param key pattern
     * @return formatted pattern
     */
    private Object processPattern(final String key)
    {
        final int condIdx = key.indexOf('|');
        if (condIdx != -1)
        {
            final String condition = key.substring(0, condIdx);
            final String value = key.substring(condIdx + 1);
            boolean ok = true;
            final StringTokenizer stt = new StringTokenizer(condition, ","); //NOI18N
            while (stt.hasMoreTokens())
            {
                String el = stt.nextToken();
                final boolean isNegative = el.startsWith("!"); //NOI18N
                if (isNegative)
                    el = el.substring(1);
                if ((map.get(el.trim()) == null) ^ isNegative)
                {
                    ok = false;
                    break;
                }
            }
            if (ok)
            {
                return new EMapFormat(map).format(value);
            }
        }
        final Object o = map.get(key);
        return o != null? o: ""; //NOI18N
    }
    
    /**
     * Designated method. It gets the string, initializes HashFormat object
     * and returns converted string. It scans  <code>pattern</code>
     * for {} brackets, then parses enclosed string and replaces it
     * with argument's  <code>get()</code> value.
     * @param pattern String to be parsed.
     * @param arguments Map with key-value pairs to replace.
     * @return Formatted string
     */
    static String format(final String pattern, final Map<String,Object> arguments)
    {
        final EMapFormat temp = new EMapFormat(arguments);
        return temp.format(pattern);
    }
    
    /**
     * Formats given string.
     * @param obj - The object to format
     * @param toAppendTo - where the text is to be appended
     * @param pos - A FieldPosition identifying a field in the formatted text
     * @return formated output
     */
    public StringBuffer format(final Object obj, final StringBuffer toAppendTo, @SuppressWarnings("unused")
	final java.text.FieldPosition pos)
    {
        final String s = (String) obj;
        final int l = s.length();
        int n = 0, lidx = -1, lastidx = 0;
        for (int i = 0; i < l; i++)
        {
            if (s.charAt(i) == '{')
            {
                n++;
                if (n == 1)
                {
                    lidx = i;
                    toAppendTo.append(s.substring(lastidx, i));
                    lastidx = i;
                }
            }
            if (s.charAt(i) == '}')
            {
                if (n == 1)
                {
                    toAppendTo.append(processPattern(s.substring(lidx + 1, i)));
                    lidx = -1;
                    lastidx = i + 1;
                }
                n--;
            }
        }
        if (n > 0)
        {
            toAppendTo.append(processPattern(s.substring(lidx + 1)));
        }
        else
        {
            toAppendTo.append(s.substring(lastidx));
        }
        return toAppendTo;
    }
    
    /**
     * This implementation does nothing.
     * @param source not used
     * @param pos not used
     * @return always null
     */
    public Object parseObject(@SuppressWarnings("unused")
	final String source, @SuppressWarnings("unused")
	final java.text.ParsePosition pos)
    {
        return null;
    }
}
