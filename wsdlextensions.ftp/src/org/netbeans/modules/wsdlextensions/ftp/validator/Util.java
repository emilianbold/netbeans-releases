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

/*
 * Util.java
 *
 * Created on January 18, 2007, 3:52 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.wsdlextensions.ftp.validator;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author jfu
 */
public class Util {
    private static final String APP_VAR_REGEX = "\\$\\{([a-zA-Z0-9\\.\\-\\_^\\{\\}]+)\\}";
    private static final Pattern APP_VAR_REF_PATT = Pattern.compile(APP_VAR_REGEX);
    private static final ResourceBundle mMessages =
            ResourceBundle.getBundle("org.netbeans.modules.wsdlextensions.ftp.validator.Bundle");
    
    public static String getMessage(String key) {
        return mMessages.getString(key);
    }

    public static String getMessage(String key, String param) {
        return getMessage(key, new Object[] {param});
    }
    
    public static String getMessage(String key, Object[] params) {
        String fmt = mMessages.getString(key);
        if ( params != null ) {
            return MessageFormat.format(fmt, params);
        } else {
            return fmt;
        }
    }
    
    public static boolean hasMigrationEnvVar(String attrVal) {
        return attrVal != null ? APP_VAR_REF_PATT.matcher(attrVal).find() : false;
    }
    
    public static boolean isEmpty(String s) {
        return (s == null || s.trim().length() == 0);
    }
}
