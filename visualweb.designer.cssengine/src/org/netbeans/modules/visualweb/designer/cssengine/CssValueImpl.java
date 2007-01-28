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


package org.netbeans.modules.visualweb.designer.cssengine;

import org.netbeans.modules.visualweb.api.designer.cssengine.CssValue;
import org.apache.batik.css.engine.value.Value;

/**
 * Bridge between the api interface and the batik value representation.
 *
 * @author Peter Zavadsky
 */
class CssValueImpl implements CssValue {

    private final Value value;

    /** Creates a new instance of CssValueImpl */
    public CssValueImpl(Value value) {
        if (value == null) {
            throw new NullPointerException("Null value is not accepted."); // NOI18N
        }
        this.value = value;
    }


    public Value getValue() {
        return value;
    }

    public int hashCode() {
        return value.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj instanceof CssValueImpl) {
            return value.equals(((CssValueImpl)obj).getValue());
        }

        return false;
    }


    public String getStringValue() {
        return value.getStringValue();
    }

    public float getFloatValue() {
        return value.getFloatValue();
    }

    public String toString() {
        return super.toString() + "[value=" + value + "]"; // NOI18N
    }

    public String getCssText() {
        return value.getCssText();
    }
}
