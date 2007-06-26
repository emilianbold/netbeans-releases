/*
 * FileNamePatternUtil.java
 * 
 * Created on May 17, 2007, 3:07:24 PM
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.wsdlextensions.file.validator;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 *
 * @author jfu
 */
public class Utils {
    private static final String ENV_VAR_REGEX = "\\$\\{([a-zA-Z0-9\\.\\-\\_^\\{\\}]+)\\}";
    public static final Pattern ENV_VAR_REF_REGEX_PATT = Pattern.compile(ENV_VAR_REGEX);

    public static boolean hasMigrationEnvVarRef(String attrVal) throws Exception {
        return ENV_VAR_REF_REGEX_PATT.matcher(attrVal).find();
    }

}
