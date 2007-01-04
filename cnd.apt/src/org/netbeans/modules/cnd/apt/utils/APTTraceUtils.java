/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.apt.utils;

import java.io.IOException;
import java.io.Writer;
import org.netbeans.modules.cnd.apt.structure.APT;

/**
 *
 * @author Vladimir Voskresensky
 */
public class APTTraceUtils {
    
    /** Creates a new instance of APTTraceUtils */
    private APTTraceUtils() {
    }
 
    /** Print out a child-sibling tree in LISP notation */
    public static String toStringList(APT t) {
        StringBuffer ts = new StringBuffer();
        if (t.getFirstChild() != null) {
            ts.append(" (");
        }
        ts.append(" ").append(t.toString());
        if (t.getFirstChild() != null) {
            ts.append(toStringList(t.getFirstChild()));
        }
        if (t.getFirstChild() != null) {
            ts.append(" )");
        }
        if (t.getNextSibling() != null) {
            ts.append(toStringList(t.getNextSibling()));
        }
        return ts.toString();
    }

    public static String toStringTree(APT t) {
        StringBuffer ts = new StringBuffer();
        if (t.getFirstChild() != null) {
            ts.append(" (");
        }
        ts.append(" ").append(t.toString());
        if (t.getFirstChild() != null) {
            ts.append(toStringTree(t.getFirstChild()));
        }
        if (t.getFirstChild() != null) {
            ts.append(" )");
        }
        return ts.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // xml output support    
    
    public static void xmlSerialize(APT t, Writer out) throws IOException {
        // print out this node and all siblings
        for (APT node = t; node != null; node = node.getNextSibling()) {
            if (node.getFirstChild() == null) {
                // print closed element (class name, attributes)
                xmlSerializeNode(node, out);
            } else {
                // print open tag
                xmlSerializeRootOpen(node, out);

                // print children
                xmlSerialize(node.getFirstChild(), out);

                // print end tag
                xmlSerializeRootClose(node, out);
            }
        }
    }    
    
    ////////////////////////////////////////////////////////////////////////////
    // help implementations
    
    private static void xmlSerializeNode(APT t, Writer out) throws IOException {
        out.write("<" + xmlNodeText(t) + "\"/>\n");
    }

    private static void xmlSerializeRootOpen(APT t, Writer out) throws IOException {
        out.write("<" + xmlNodeText(t) + "\">\n");
    }
    
    private static void xmlSerializeRootClose(APT t, Writer out) throws IOException {
        out.write("</" + t.getClass().getSimpleName() + ">\n");
    }

    private static String xmlNodeText(APT t) {
        StringBuffer buf = new StringBuffer(100);
        buf.append(t.getClass().getSimpleName());
        buf.append(" type=\"").append(encode(getTypeName(t)));
        buf.append("\" text=\"").append(encode(t.toString()));
        return buf.toString();
    }
    
    private static String decode(String text) {
        char c, c1, c2, c3, c4, c5;
        StringBuffer n = new StringBuffer();
        for (int i = 0; i < text.length(); i++) {
            c = text.charAt(i);
            if (c == '&') {
                c1 = text.charAt(i + 1);
                c2 = text.charAt(i + 2);
                c3 = text.charAt(i + 3);
                c4 = text.charAt(i + 4);
                c5 = text.charAt(i + 5);

                if (c1 == 'a' && c2 == 'm' && c3 == 'p' && c4 == ';') {
                    n.append("&");
                    i += 5;
                }
                else if (c1 == 'l' && c2 == 't' && c3 == ';') {
                    n.append("<");
                    i += 4;
                }
                else if (c1 == 'g' && c2 == 't' && c3 == ';') {
                    n.append(">");
                    i += 4;
                }
                else if (c1 == 'q' && c2 == 'u' && c3 == 'o' &&
                    c4 == 't' && c5 == ';') {
                    n.append("\"");
                    i += 6;
                }
                else if (c1 == 'a' && c2 == 'p' && c3 == 'o' &&
                    c4 == 's' && c5 == ';') {
                    n.append("'");
                    i += 6;
                }
                else
                    n.append("&");
            }
            else
                n.append(c);
        }
        return new String(n);
    }

    private static String encode(String text) {
        char c;
        StringBuffer n = new StringBuffer();
        for (int i = 0; i < text.length(); i++) {
            c = text.charAt(i);
            switch (c) {
                case '&':
                    {
                        n.append("&amp;");
                        break;
                    }
                case '<':
                    {
                        n.append("&lt;");
                        break;
                    }
                case '>':
                    {
                        n.append("&gt;");
                        break;
                    }
                case '"':
                    {
                        n.append("&quot;");
                        break;
                    }
                case '\'':
                    {
                        n.append("&apos;");
                        break;
                    }
                default :
                    {
                        n.append(c);
                        break;
                    }
            }
        }
        return new String(n);
    }

    public static String getTypeName(APT t) {
        int/*APT.Type*/ type = t.getType();
        String str="<unknown>";
        switch (type) {
            case APT.Type.INVALID:
                str = "INVALID";
                break;
            case APT.Type.FILE:
                str = "FILE";
                break;
            case APT.Type.TOKEN_STREAM:
                str = "TOKEN_STREAM";
                break;
            case APT.Type.INCLUDE:
                str = "INCLUDE";
                break;
            case APT.Type.INCLUDE_NEXT:
                str = "INCLUDE_NEXT";
                break;
            case APT.Type.DEFINE:
                str = "DEFINE";
                break;
            case APT.Type.UNDEF:
                str = "UNDEF";
                break;
            case APT.Type.CONDITION_CONTAINER:
                str = "CONDITION_CONTAINER";
                break;
            case APT.Type.IFDEF:
                str = "IFDEF";
                break;
            case APT.Type.IFNDEF:
                str = "IFNDEF";
                break;
            case APT.Type.IF:
                str = "IF";
                break;
            case APT.Type.ELIF:
                str = "ELIF";
                break;
            case APT.Type.ELSE:
                str = "ELSE";
                break;
            case APT.Type.ENDIF:
                str = "ENDIF";
                break;
            case APT.Type.PRAGMA:
                str = "PRAGMA";
                break;
            case APT.Type.LINE:
                str = "LINE";
                break;
            case APT.Type.ERROR:
                str = "ERROR";
                break;
            case APT.Type.PREPROC_UNKNOWN:
                str = "PREPROC_UNKNOWN";
                break;
            default:
        }
        return str;
    }    
}
