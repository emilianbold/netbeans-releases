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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.visualweb.insync.beans;

import java.beans.ParameterDescriptor;
import java.util.HashSet;
import java.util.Set;
import java.beans.Introspector;

/**
 * Public utility methods for JavaBean name pattern handling
 */
public class Naming {

    /**
     * Return an identifier string with the first letter made lowercase
     *
     * @param ident
     * @return The identifier string with the first letter made lowercase
     */
    public static final String firstLowered(String ident) {
        return Character.toLowerCase(ident.charAt(0)) + ident.substring(1, ident.length());
    }

    /**
     * Turn a fully qualified type name into a usable variable name
     *
     * @param type  Fully qualified type name
     * @return The last part of a dotted type string sequence made initial lowercase
     */
    public static final String varName(String type) {
        int dot = type.lastIndexOf('.');
        if (dot >= 0)
            type = type.substring(dot+1);
        return makeValidJavaBeanName(type);
    }

    /**
     * Convert an array of Classes, and possibly assisted by an array of ParameterDescriptors, into
     * a simple array of string parameter names.
     *
     * @param classes  Required list of parameter type classes
     * @param pds  Optional javabean parameter descriptors to use if available
     * @return A string array containing usable parameter names
     */
    public static final String[] paramNames(Class[] classes, ParameterDescriptor[] pds) {
        String[] names = new String[classes.length];
        if (pds != null) {
            for (int i = 0; i < classes.length; i++)
                names[i] = pds[i].getName();
        }
        else {
            String[] types = new String[classes.length];
            for (int i = 0; i < classes.length; i++) {
                String type = classes[i].getName();
                int dot = type.lastIndexOf('.');
                if (dot >= 0)
                    type = type.substring(dot+1);
                types[i] = type;
            }
            for (int i = 0; i < classes.length; i++) {
                boolean unique = true;
                for (int j = 0; j < types.length; j++) {
                    if (j != i && types[j].equals(types[i])) {
                        unique = false;
                        break;
                    }
                }
                StringBuffer nameb = new StringBuffer();
                for (int j = 0; j < types[i].length(); j++) {
                    char c = types[i].charAt(j);
                    if (Character.isUpperCase(c))
                        nameb.append(Character.toLowerCase(c));
                }
                if (!unique)
                    nameb.append(i);
                String name = nameb.toString();
                if(isJavaKeyWord(name))
                    names[i] = mangle(name, i);
                else
                    names[i] = name;
            }
        }
        return names;
    }

    /* JavaBeans introspector interprets getter "getFooBah" corresponding to
     * property "fooBah" but getter "getURL" as corresponding to property "URL".
     *
     * Examples where we can have problems
     * ( Type   --> property name --> Getter --> JavaBeans interpretation of property name)
     * 1) USWeather --> uSWeather --> getUSWeather --> USWeather
     * 2) eBay      --> eBay      --> getEBay      --> EBay
     *
     * Therefore the first two letters are made lower case unless both of them
     * are upper case
     *
     * Also, if the name is a single character, it should be lower case.
     * For example if the getter is getA(), property name is ambiguous('a' or 'A').
     *
     */
    public static String makeValidJavaBeanName(String name) {
        char chars[] = name.toCharArray();
        if (name.length() > 1 && 
            !(Character.isUpperCase(chars[0]) && Character.isUpperCase(chars[1])) ) {
            chars[0] = Character.toLowerCase(chars[0]);        
            chars[1] = Character.toLowerCase(chars[1]);
        }else if(name.length() == 1 && Character.isUpperCase(chars[0])){
            chars[0] = Character.toLowerCase(chars[0]);
        }
        return new String(chars);
    }
    
    /**
     * @param ident  Identifier to capitalize
     * @return The identifier string with the first character capitalized
     */
    public static final String firstUppered(String ident) {
        return Character.toUpperCase(ident.charAt(0)) + ident.substring(1, ident.length());
    }

    /**
     * @param name  Property name to create the getter method name for.
     * @return The getter method name.
     */
    public static final String getterName(String name) {
        return "get" + firstUppered(name);
    }

    /**
     * @param name  Property name to create the setter method name for.
     * @return The setter method name
     */
    public static final String setterName(String name) {
        return "set" + firstUppered(name);
    }

    /**
     * @param name  Event name to create the adder method name for.
     * @return The adder method name
     */
    public static final String adderName(String name) {
        return "add" + firstUppered(name) + "Listener";
    }

    /**
     * @param ident  The identifier to check
     * @return true iff the given string is a valid java identifier
     */
    public static final boolean isValidIdentifier(String ident) {
        if (ident != null && ident.length() > 0 && Character.isJavaIdentifierStart(ident.charAt(0))) {
            for (int i = 1; i < ident.length(); i++) {
                if (!Character.isJavaIdentifierPart(ident.charAt(i)))
                    return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Convert a string into a valid java identifier if possible
     * 
     * @return the fixed up string, or null if unfixable
     */
    public static final String makeValidIdentifier(String ident) {
        if (ident == null || ident.length() == 0)
            return null;

        StringBuffer sb = new StringBuffer(ident);
        // trim leading bad identifier chars (including whitespace)
        while (sb.length() > 0 && !Character.isJavaIdentifierStart(sb.charAt(0)))
            sb.deleteCharAt(0);

        // trim trailing bad identifier chars (including whitespace)
        for (int i = sb.length() - 1; i > 0; i--) {
            char ch = sb.charAt(i);
            if (!Character.isJavaIdentifierPart(ch))
                sb.deleteCharAt(i);
            else
                break;
        }
        sb = new StringBuffer(makeValidJavaBeanName(sb.toString()));

        // replace any remaining internal bad chars with underscores
        for (int i = 0; i < sb.length(); i++) {
            char ch = sb.charAt(i);
            if (!Character.isJavaIdentifierPart(ch))
                sb.setCharAt(i, '_');
        }

        // return the string, or null if there was nothing left 
        return (sb.length() > 0) ? sb.toString() : null;
    }

    /**
     * Extract the property name from a getter method name by removing the leading "get" & fixing
     * case.
     * 
     * @param mname  The getter method name
     * @param tryIs  If true, the 'is' prefix is tested first
     * @return The property name.
     */
    public static final String propertyName(String mname, boolean tryIs) {
        if (tryIs && mname.startsWith("is"))
            return Introspector.decapitalize(mname.substring(2, mname.length()));
        if (mname.startsWith("get"))
            return Introspector.decapitalize(mname.substring(3, mname.length()));
        return null;
    }

    /**
     * Extract the event name from an add method name by removing the leading "add" and trailing
     * "Listener", & fixing case.
     * 
     * @param mname  The adder method name
     * @return The event name.
     */
    public static final String eventName(String mname) {
        String add = "add"; //NOI18N
        String listener = "Listener"; //NOI18N
        if (mname.startsWith(add) && mname.endsWith(listener)) { 
            if (mname.length() > (add.length()+listener.length()))
                return Introspector.decapitalize(mname.substring(add.length(), mname.length()-listener.length()));
            else
                return "";  // Method name is just "addListener"
        }
        return null;
    }
    
    
    /**
     * <p>The set of reserved keywords in the Java language.</p>
     */
    protected static Set keywords = new HashSet();
    static {
        keywords.add("abstract");
        keywords.add("boolean");
        keywords.add("break");
        keywords.add("byte");
        keywords.add("case");
        keywords.add("cast");
        keywords.add("catch");
        keywords.add("char");
        keywords.add("class");
        keywords.add("const");
        keywords.add("continue");
        keywords.add("default");
        keywords.add("do");
        keywords.add("double");
        keywords.add("else");
        keywords.add("extends");
        keywords.add("final");
        keywords.add("finally");
        keywords.add("float");
        keywords.add("for");
        keywords.add("future");
        keywords.add("generic");
        keywords.add("goto");
        keywords.add("if");
        keywords.add("implements");
        keywords.add("import");
        keywords.add("inner");
        keywords.add("instanceof");
        keywords.add("int");
        keywords.add("interface");
        keywords.add("long");
        keywords.add("native");
        keywords.add("new");
        keywords.add("null");
        keywords.add("operator");
        keywords.add("outer");
        keywords.add("package");
        keywords.add("private");
        keywords.add("protected");
        keywords.add("public");
        keywords.add("rest");
        keywords.add("return");
        keywords.add("short");
        keywords.add("static");
        keywords.add("super");
        keywords.add("switch");
        keywords.add("synchronized");
        keywords.add("this");
        keywords.add("throw");
        keywords.add("throws");
        keywords.add("transient");
        keywords.add("try");
        keywords.add("var");
        keywords.add("void");
        keywords.add("volatile");
        keywords.add("while");
    }

    /**
     * <p>Return a mangled version of the specified name if it conflicts with
     * a Java keyword; </p>
     *
     * @param name Name to be potentially mangled
     */
    public static final String mangle(String name, int i) {
        if (keywords.contains(name)) {
            return (name + i);
        } else {
            return (name);
        }
    }    
    
    public static final boolean isJavaKeyWord(String name){
        if(keywords.contains(name)) {
            return true;
        } else {
            return false;
        }
    }
    
    public static String getBaseName(String name) {
        while (name.length() > 1 && Character.isDigit(name.charAt(name.length()-1)))
            name = name.substring(0, name.length()-1);  
        return makeValidJavaBeanName(name);
    }

    private Naming() {}  // this class is not instantiable
}
