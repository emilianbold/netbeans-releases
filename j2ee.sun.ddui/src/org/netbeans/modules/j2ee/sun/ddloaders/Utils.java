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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.sun.ddloaders;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.schema2beans.Schema2BeansRuntimeException;
import org.openide.ErrorManager;

/**
 *
 * @author Peter Williams
 */
public class Utils {

    public static final String ICON_BASE_DD_VALID =
            "org/netbeans/modules/j2ee/ddloaders/resources/DDValidIcon"; // NOI18N
    public static final String ICON_BASE_DD_INVALID =
            "org/netbeans/modules/j2ee/ddloaders/resources/DDInvalidIcon"; // NOI18N
    public static final String ICON_BASE_ERROR_BADGE = 
            "org/netbeans/modules/j2ee/sun/ddloaders/resources/ErrorBadge"; // NOI18N
    
    
    /** No instances of this class should be created.
     */
    private Utils() {
    }

    
	private static final String [] booleanStrings = {
		"0", "1",			// NOI18N
		"false", "true",	// NOI18N
		"no", "yes",		// NOI18N
		"off", "on"			// NOI18N
	};

	public static boolean booleanValueOf(String val) {
		boolean result = false;
		int valueIndex = -1;

		if(val != null && val.length() > 0) {
            val = val.trim();
			for(int i = 0; i < booleanStrings.length; i++) {
				if(val.compareToIgnoreCase(booleanStrings[i]) == 0) {
					valueIndex = i;
					break;
				}
			}
		}

		if(valueIndex >= 0) {
			if(valueIndex%2 == 1) {
				result = true;
			}
		}

		return result;
	}
    
    public static boolean notEmpty(String testedString) {
        return (testedString != null) && (testedString.length() > 0);
    }
    
    public static boolean strEmpty(String testedString) {
        return testedString == null || testedString.length() == 0;
    }
    
    public static boolean strEquals(String one, String two) {
        boolean result = false;
        
        if(one == null) {
            result = (two == null);
        } else {
            if(two == null) {
                result = false;
            } else {
                result = one.equals(two);
            }
        }
        return result;
    }
    
    public static boolean strEquivalent(String one, String two) {
        boolean result = false;
        
        if(strEmpty(one) && strEmpty(two)) {
            result = true;
        } else if(one != null && two != null) {
            result = one.equals(two);
        }
        
        return result;
    }
    
    public static int strCompareTo(String one, String two) {
        int result;
        
        if(one == null) {
            if(two == null) {
                result = 0;
            } else {
                result = -1;
            }
        } else {
            if(two == null) {
                result = 1;
            } else {
                result = one.compareTo(two);
            }
        }
        
        return result;
    }
    
    public static String getBeanDisplayName(CommonDDBean bean, String nameProperty) {
        String name = null;
        try {
            name = (String) bean.getValue(nameProperty);
        } catch(Schema2BeansRuntimeException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return name != null ? name : " "; // NOI18N
    }
    
}
