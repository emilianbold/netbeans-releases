/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.schema2beans;

import java.io.*;

public class XMLUtil {
    private XMLUtil() {}

    /**
     * Takes some text to be printed into an XML stream and escapes any
     * characters that might make it invalid XML (like '<').
     */
    public static void printXML(StringBuffer out, String msg) {
        printXML(out, msg, true);
    }

    public static void printXML(StringBuffer out, String msg, boolean attribute) {
        if (msg == null)
            return;
        int msgLength = msg.length();
        for (int i = 0; i < msgLength; ++i) {
            char c = msg.charAt(i);
            printXML(out, c, attribute);
        }
    }

    public static void printXML(StringBuffer out, char msg, boolean attribute) {
        if (msg == '&')
            out.append("&amp;");
        else if (msg == '<')
            out.append("&lt;");
        else if (msg == '>')
            out.append("&gt;");
        else if (attribute && msg == '"')
            out.append("&quot;");
        else if (attribute && msg == '\'')
            out.append("&apos;");
        else if (attribute && msg == '\n')
            out.append("&#xA");
        else if (attribute && msg == '\t')
            out.append("&#x9");
        else
            out.append(msg);
    }

    public static boolean shouldEscape(char c) {
        if (c == '&')
            return true;
        else if (c == '<')
            return true;
        else if (c == '>')
            return true;
        return false;
    }

    public static boolean shouldEscape(String s) {
        if (s == null)
            return false;
        int msgLength = s.length();
        for (int i = 0; i < msgLength; ++i) {
            char c = s.charAt(i);
            if (shouldEscape(c))
                return true;
        }
        return false;
    }

    /**
     * Takes some text to be printed into an XML stream and escapes any
     * characters that might make it invalid XML (like '<').
     */
    public static void printXML(java.io.Writer out, String msg) throws java.io.IOException {
        printXML(out, msg, true);
    }

    public static void printXML(java.io.Writer out, String msg, boolean attribute) throws java.io.IOException {
        if (msg == null)
            return;
        int msgLength = msg.length();
        for (int i = 0; i < msgLength; ++i) {
            char c = msg.charAt(i);
            printXML(out, c, attribute);
        }
    }

    public static void printXML(java.io.Writer out, char msg, boolean attribute) throws java.io.IOException {
        if (msg == '&')
            out.write("&amp;");
        else if (msg == '<')
            out.write("&lt;");
        else if (msg == '>')
            out.write("&gt;");
        else if (attribute && msg == '"')
            out.write("&quot;");
        else if (attribute && msg == '\'')
            out.write("&apos;");
        else if (attribute && msg == '\n')
            out.write("&#xA;");
        else if (attribute && msg == '\t')
            out.write("&#x9;");
        else
            out.write(msg);
    }
}
