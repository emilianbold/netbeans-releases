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

package org.netbeans.modules.xslt.model.enums;



/**
 * @author ads
 *
 */
public enum Validation implements EnumValue {

    STRICT,
    LAX,
    PRESERVE,
    STRIP,
    INVALID;

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.enums.EnumValue#isInvalid()
     */
    /** {@inheritDoc} */
    public boolean isInvalid() {
        return this == INVALID ;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    /** {@inheritDoc} */
    public String toString() {
        if ( isInvalid() ) {
            return "";
        }
        else {
            return super.toString().toLowerCase();
        }
    }
    
    /**
     * Returns enum by its string value.
     * 
     * @param str
     *            string representation.
     * @return enum
     */
    public static Validation forString( String str ) {
        if ( str == null ) {
            return null;
        }
        Validation[] validations = values();
        for (Validation validation : validations) {
            if ( str.equals( validation.toString())) {
                return validation;
            }
        }
        return INVALID;
    }
    
}
