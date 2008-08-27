// <editor-fold defaultstate="collapsed" desc=" License Header ">
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
//</editor-fold>

package org.netbeans.modules.glassfish.eecommon;

import java.util.Arrays;

/** Various utilities inspired by code in org.apache.jasper.JspUtil.
 */
public class JasperNameMappingHelper {

    static private final String javaKeywords[] = {
        "abstract", "assert", "boolean", "break", "byte", "case",
        "catch", "char", "class", "const", "continue",
        "default", "do", "double", "else", "enum", "extends",
        "final", "finally", "float", "for", "goto",
        "if", "implements", "import", "instanceof", "int",
        "interface", "long", "native", "new", "package",
        "private", "protected", "public", "return", "short",
        "static", "strictfp", "super", "switch", "synchronized",
        "this", "throws", "transient", "try", "void",
        "volatile", "while" };
        
    /**
     * The default package name for compiled jsp pages.
     */
    static public final String JSP_PACKAGE_NAME = "org.apache.jsp";
        
    
    /**
     * Converts the given path to a Java package or fully-qualified class name
     *
     * @param path Path to convert
     *
     * @return Java package corresponding to the given path
     */
    static public final String makeJavaPackage(String path) {
        String classNameComponents[] = path.split("/");
        StringBuilder legalClassNames = new StringBuilder(path.length());
        for (int i = 0; i < classNameComponents.length; i++) {
            if (classNameComponents[i].length() > 0) {
                legalClassNames.append(makeJavaIdentifier(classNameComponents[i]));
                if (i < classNameComponents.length - 1) {
                    legalClassNames.append('.');
                }
            }
        }
        return legalClassNames.toString();
    }
    
    /**
     * Converts the given identifier to a legal Java identifier
     *
     * @param identifier Identifier to convert
     *
     * @return Legal Java identifier corresponding to the given identifier
     */
    static public final String makeJavaIdentifier(String identifier) {
        StringBuilder modifiedIdentifier = new StringBuilder(identifier.length());
        if (!Character.isJavaIdentifierStart(identifier.charAt(0))) {
            modifiedIdentifier.append('_');
        }
        for (int i = 0; i < identifier.length(); i++) {
            char ch = identifier.charAt(i);
            if (Character.isJavaIdentifierPart(ch) && ch != '_') {
                modifiedIdentifier.append(ch);
            } else if (ch == '.') {
                modifiedIdentifier.append('_');
            } else {
                modifiedIdentifier.append(mangleChar(ch));
            }
        }
        if (isJavaKeyword(modifiedIdentifier.toString())) {
            modifiedIdentifier.append('_');
        }
        return modifiedIdentifier.toString();
    }
    
    /**
     * Mangle the specified character to create a legal Java class name.
     */
    static private final String mangleChar(char ch) {
        char[] result = new char[5];
        result[0] = '_';
        result[1] = Character.forDigit((ch >> 12) & 0xf, 16);
        result[2] = Character.forDigit((ch >> 8) & 0xf, 16);
        result[3] = Character.forDigit((ch >> 4) & 0xf, 16);
        result[4] = Character.forDigit(ch & 0xf, 16);
        return new String(result);
    }
    
    /**
     * Test whether the argument is a Java keyword
     */
    static private boolean isJavaKeyword(String key) {
        return Arrays.binarySearch(javaKeywords, key) >= 0;
    }
    
}
