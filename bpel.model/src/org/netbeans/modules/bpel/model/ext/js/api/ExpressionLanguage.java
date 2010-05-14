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
package org.netbeans.modules.bpel.model.ext.js.api;

/**
 *
 * @author Vitaly Bychkov
 */
public enum ExpressionLanguage {
    JAVA_SCRIPT("urn:sun:bpel:JavaScript"),
    INVALID();

    ExpressionLanguage() {

    }

    ExpressionLanguage( String str ) {
        myValue = str;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.netbeans.modules.soa.model.bpel.api.support.EnumValue#isInvalid()
     */
    /** {@inheritDoc} */
    public boolean isInvalid() {
        return this.equals(INVALID);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return ""+myValue;
    }

    /**
     * @param str String representation of enum.
     * @return Enumeration class that have <code>str</code> representation.
     */
    public static ExpressionLanguage forString( String str ) {
        if ( str == null ){
            return null;
        }
        ExpressionLanguage[] values = ExpressionLanguage.values();
        for (ExpressionLanguage value : values) {
            if (str.equals(value.toString())) {
                return value;
            }
        }
        return INVALID;
    }

    private String myValue;
}
