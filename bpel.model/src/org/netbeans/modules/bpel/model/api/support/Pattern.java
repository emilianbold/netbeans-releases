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

/**
 *
 */
package org.netbeans.modules.bpel.model.api.support;

/**
 * @author ads
 */
public enum Pattern implements EnumValue {
    REQUEST("request"), RESPONSE("response"),                       // NOI18N
    REQUEST_RESPONSE("request-response"), NOT_SPECIFIED("---"),     // NOI18N
    INVALID();

    Pattern( String value ) {
        myValue = value;
    }

    Pattern() {
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    /** {@inheritDoc} */
    public String toString() {
        return ""+myValue;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.EnumValue#isInvalid()
     */
    /** {@inheritDoc} */
    public boolean isInvalid() {
        return equals(INVALID);
    }

    /**
     * Returns enumeration via its string representation.
     * 
     * @param str
     *            string that represent enumeration.
     * @return enumeration.
     */
    public static Pattern forString( String str ) {
        if ( str == null ){
            return null;
        }
        Pattern[] values = Pattern.values();
        for (Pattern pattern : values) {
            if (pattern.toString().equals(str)) {
                return pattern;
            }
        }
        return INVALID;
    }
    
    public static boolean isRequestApplicable( Pattern pattern ){
        return REQUEST.equals( pattern ) || REQUEST_RESPONSE.equals( pattern ); 
    }
    
    public static boolean isResponseApplicable( Pattern pattern ){
        return RESPONSE.equals( pattern ) || REQUEST_RESPONSE.equals(pattern );
    }

    private String myValue;
}
