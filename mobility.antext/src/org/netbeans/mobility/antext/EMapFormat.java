/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
