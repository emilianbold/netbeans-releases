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

package org.netbeans.modules.languages.php.lang;

/**
 * Magic constants.
 * 
 * @see 
 * <a href="http://www.php.net/manual/en/language.constants.predefined.php">
 * PHP Manual / Constants / Magic constants</a>
 * 
 * @author Victor G. Vasilyev
 */
public enum MagicConstants {
    
    /**
     * The current line number of the file.
     */
    LINE("__LINE__") {
        boolean equals(String value) {
            return value().equalsIgnoreCase(value);
        }
    },
    
    /**
     * The full path and filename of the file. 
     * If used inside an include, the name of the included file is returned. 
     * Since PHP 4.0.2, __FILE__ always contains an absolute path whereas in 
     * older versions it contained relative path under some circumstances.
     */
    FILE("__FILE__") {
        boolean equals(String value) {
            return value().equalsIgnoreCase(value);
        }
    },
    
    /**
     * The function name. (Added in PHP 4.3.0) 
     * As of PHP 5 this constant returns the function name as it was declared 
     * (case-sensitive). In PHP 4 its value is always lowercased.
     */
    FUNCTION("__FUNCTION__") {
        boolean equals(String value) {
            return value().equalsIgnoreCase(value);
        }
    },
    
    /**
     * The class name. (Added in PHP 4.3.0) 
     * As of PHP 5 this constant returns the class name as it was declared 
     * (case-sensitive). In PHP 4 its value is always lowercased.
     */
    CLASS("__CLASS__") {
        boolean equals(String value) {
            return value().equalsIgnoreCase(value);
        }
    },
    
    /**
     * The class method name. (Added in PHP 5.0.0) 
     * The method name is returned as it was declared (case-sensitive).
     */
    METHOD("__METHOD__") {
        boolean equals(String value) {
            return value().equalsIgnoreCase(value);
        }
    },
    ;
    

    MagicConstants(String value) { this.value = value; }
    private final String value;
    public String value() { return value; }
    
    public boolean isMatched(String prefix) {
        return prefix==null || 
                this.value.toLowerCase().startsWith(prefix.toLowerCase());
    }
    
    /**
     * Tests if the specified <code>value</code> is the underlying 
     * <code>MagicConstants</code>.
     * <p>Note: <code>MagicConstants</code> listed in the 
     * <i>Table 13.1. A few "magical" PHP constants</i> of the PHP Manual
     * are case-insensitive.</p>
     * 
     * @param value any string, including <code>null</code> that will be tested. 
     * @return <code>true</code> if the specified string is the underlying 
     * <code>MagicConstants</code>, otherwise - <code>false</code>.
     */
    abstract boolean equals(String value);

}
