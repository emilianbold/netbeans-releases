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

package org.netbeans.modules.iep.model;



import java.net.InetAddress;
import java.net.UnknownHostException;

import java.rmi.server.UID;



import java.util.HashSet;



/**
 * DOCUMENT ME!
 *
 * @author  Bing Lu
 */
public class NameUtil {
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(NameUtil.class.getName());

    /** Holds a list of reserved identifiers.  */
    private static final HashSet JAVA_KEYWORD = new HashSet();
    static {
        // Initialize "JAVA_KEYWORD".
        String[] key = {
        // Current and future keywords + primitive types + classes
        // automatically imported (java.lang.*).
                "abstract", "boolean", "break", "byte", "byvalue", "case", "cast",
                "catch", "char", "class", "const", "continue", "default", "do",
                "double", "else", "extends", "false", "final", "finalize",
                "finally", "float", "for", "future", "generic", "goto", "if",
                "implements", "import", "inner", "instanceof", "int", "interface",
                "long", "native", "new", "null", "operator", "outer", "package",
                "private", "protected", "public", "rest", "return", "short",
                "static", "strictfp", "super", "switch", "synchronized", "then",
                "this", "throw", "throws", "transient", "true", "try", "var",
                "void", "volatile", "while", "widefp",
        // classes in java.lang.* automatically imported
                "AbstractMethodError", "ArithmeticException",
                "ArrayIndexOutOfBoundsException", "ArrayStoreException", "Boolean",
                "Byte", "Character", "Class", "ClassCastException",
                "ClassCircularityError", "ClassFormatError", "ClassLoader",
                "ClassNotFoundException", "CloneNotSupportedException", "Clonable",
                "Compiler", "Double", "Error", "Exception",
                "ExceptionInInitializerError", "Float", "IllegalAccessError",
                "IllegalAccessException", "IllegalArgumentException",
                "IllegalMonitorStateException", "IllegalStateException",
                "IllegalThreadStateException", "IncompatibleClassChangeError",
                "IndexOutOfBoundsException", "InstantiationError",
                "InstantiationException", "Integer", "InternalError",
                "InterruptedException", "LinkageError", "Long", "Math",
                "NegativeArraySizeException", "NoClassDefFoundError",
                "NoSuchFieldError", "NoSuchFieldException", "NoSuchMethodError",
                "NoSuchMethodException", "NullPointerException", "Number",
                "NumberFormatException", "Object", "OutOfMemoryError", "Process",
                "Runnable", "Runtime", "RuntimeException", "SecurityException",
                "SecurityManager", "Short", "StackOverflowError", "String",
                "StringBuffer", "StringIndexOutOfBoundsException", "System",
                "Thread", "ThreadDeath", "ThreadGroup", "Throwable", "UnknownError",
                "UnsatisfiedLinkError", "VerifyError", "VirtualMachineError", "Void"
                };

        for (int i = 0; i < key.length; i++) {
            JAVA_KEYWORD.add(key[i]);
        }
    }

    /**
     * true if s in a legal java identifier that contains no $, s is not a Java keyword, 
     * and s is not a class name in java.lang.* package
     */
    public static boolean isLegalName(String s) {
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            boolean isIdentifierChar = (i == 0)
                ? Character.isJavaIdentifierStart(ch)
                : Character.isJavaIdentifierPart(ch);
            if (!isIdentifierChar || (ch == '$')) {
                return false;
            }
        }

        if ((s.length() == 0) || isKeyword(s)) {
            return false;
        }
        return true;
    }
    
    public static boolean isKeyword(String s) {
        return JAVA_KEYWORD.contains(s);
    }
//
    
    /**
     * Given a string, strip it to a legal Java identifier. Characters that are
     * not legal are removed; if the 1st char would be legal as 2nd but not as
     * 1st, prefix "_"; if identifier is in list of reserved words, prefix "_";
     * if no legal chars remain, return "_".
     *
     * @param str  the string to make into a legal java identifier
     * @return     the legal java/XML identifier
     */
    public static String makeJavaId(String str) {

        StringBuffer sb = new StringBuffer(str.length());

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            boolean isIdentifierChar =
                    //(i == 0)
                    //? Character.isJavaIdentifierStart(c) :
                    Character.isJavaIdentifierPart(c);

            if (!isIdentifierChar) { //|| (c == '$')) {
                sb.append('_');
            } else {
                sb.append(c);
            }
        }

        if ((sb.length() == 0) || isJavaKeyword(sb.toString()) ||
            !Character.isJavaIdentifierStart(sb.charAt(0))) {

            // Some kind of clash; prefix underscore.
            sb.insert(0, '_');
        }

        return sb.toString();
    }

    /**
     * Is given string a Java reserved keyword?
     *
     * @param s  the string to test to see if it is a reserved Java keyword
     * @return   <code>true</code> if the given string is a java keyword and
     *      <code>false</code> if it is not
     */
    public static boolean isJavaKeyword(String s) {
        return JAVA_KEYWORD.contains(s);
    }

    /**
     * Returns a legal java identifier that is globally unique.
     *
     * @return   the uid value
     */
    public static String getJUid() {

        StringBuffer buf = null;
        String uid = getUid();
        String prefix = "id_";

        buf = new StringBuffer(prefix.length() + uid.length());

        buf.append(prefix);

        char[] chars = uid.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            if (Character.isJavaIdentifierPart(chars[i])) {
                buf.append(chars[i]);
            } else {
                buf.append('_');
            }
        }

        return buf.toString();
    }
    
    /**
     * Creates a String identifier t
     * hat is globally unique. It is unique under
     * the following conditions: a) Any generating machine takes more than one
     * second to reboot. AND b) Any generating machine's clock is never set
     * backward.
     *
     * @return   The uid value
     */
    private static String mIP;
    public static String getUid() {
        String uid = (new UID()).toString();

        try {
            if (mIP == null) {
                mIP = InetAddress.getLocalHost().toString();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
            mIP = "localhost";
        }
        return mIP + "/" + uid;
    }

    private static boolean isAlphaNum(char c) {
        return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || ('0' <= c && c <= '9');
    }
    
    private static final char[] HEX_DIGIT = new char[] {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };
    
    /**
     * notation Converts a character c to _uXXXX_ notation where XXXX is the unicode of c
     * @param c character
     * @return _uXXXX_ notationed string
     */
    private static String unicode(char c) {
        StringBuffer sb = new StringBuffer();
        sb.append("_u");
        sb.append(HEX_DIGIT[(c >> 12) & 0xF]);
        sb.append(HEX_DIGIT[(c >> 8)  & 0xF]);
        sb.append(HEX_DIGIT[(c >> 4)  & 0xF]);
        sb.append(HEX_DIGIT[c & 0xF]);
        sb.append('_');
        return sb.toString();
    }

    public static String makeAlphaNumId(String s) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (isAlphaNum(c)) {
                sb.append(c);
            } else {
                sb.append(unicode(c));
            }
        }
        return sb.toString();
    }
    
    public static String makeAlphaNumId(String[] s) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length; i++) {
            sb.append(makeAlphaNumId(s[i]));
            if (i < s.length - 1) {
                sb.append("_");
            }
        }
        return sb.toString();
    }
    
    // FIX ME: see LinkImpl.getTargetNameSpaceButUrn() for reason
    public static String makeAlphaNumIdForLink(String[] s) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length; i++) {
            sb.append(makeAlphaNumId(s[i]));
            if (i < s.length - 2) {
                sb.append(".");
            } else if (i == s.length - 2) {
                sb.append("_");
            }
        }
        return sb.toString();
    }

    /**
     * The main program for the GenUtil class
     *
     * @param args  The command line arguments
     */
    public static void main(String[] args) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < args.length; i++) {
            sb.append(" " + args[i]);
        }
        System.out.println(makeAlphaNumId(sb.toString()));
    }

    
}
