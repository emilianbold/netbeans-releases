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

package org.netbeans.lib.ddl.util;

import java.text.ParseException;
import java.util.Enumeration;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.openide.util.MapFormat;

/**
* The message formatter, It handles [] brackets as optional keys.
*
* @author   Slavek Psenicka
*/

public class CommandFormatter {
    /** Parsed items */
    Vector items;

    /** Formats pattern using arguments map
    * Returns formatted string.
    * @param pattern String to be formatted
    * @param arguments Argument map
    */
    public static String format(String pattern, Map arguments) throws ParseException, IllegalArgumentException {
        CommandFormatter temp = new CommandFormatter(pattern);
        return temp.format(arguments);
    }

    /** Constructor
    * @param pattern String to be formatted
    */
    public CommandFormatter(String pattern) throws ParseException {
        this(new StringTokenizer(pattern, "[]", true));
    }

    /** Constructor
    * @param tok Used tokenizer
    */
    private CommandFormatter(StringTokenizer tok) throws ParseException {
        items = scan(tok);
    }

    /** Scans data for [] brackets and prepares data for formatting
    * using MapFormat utility
    */
    private Vector scan(StringTokenizer tok) throws ParseException {
        Vector objvec = new Vector();
        while (tok.hasMoreTokens()) {
            String token = (String) tok.nextElement();
            Object obj = token;
            if (token.equals("["))
                obj = (Object)scan(tok);
            else if (token.equals("]"))
                break;
            objvec.add(obj);
        }

        return objvec;
    }

    /** Formats parsed string using given argument map
    * @param arguments Argument map
    */
    public String format(Map arguments) throws IllegalArgumentException {
        return format(items, arguments);
    }

    /** Formats given string vector using given argument map.
    * Used internally only.
    * @param arguments Argument map
    */
    private String format(Vector itemvec, Map arguments) throws IllegalArgumentException {
        String retstr = "";
        Enumeration items_e = itemvec.elements();
        while (items_e.hasMoreElements()) {
            Object e_item = items_e.nextElement();
            if (e_item instanceof Vector) {
                try {
                    e_item = format((Vector)e_item, arguments);
                } catch (Exception e) {
                    //PENDING
                }
            }

            if (e_item instanceof String) {
                MapFormat fmt = new MapFormat(arguments);
                fmt.setThrowExceptionIfKeyWasNotFound(true);
                String e_msg = fmt.format((String)e_item);
                if (e_msg != null)
                    retstr = retstr + e_msg;
                else
                    throw new IllegalArgumentException();
            }
        }

        return retstr;
    }
}
