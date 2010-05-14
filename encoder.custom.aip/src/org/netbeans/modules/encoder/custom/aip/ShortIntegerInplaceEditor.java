/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.encoder.custom.aip;

import java.math.BigInteger;
import org.openide.explorer.propertysheet.InplaceEditor;

/**
 * An InplaceEditor implementation for editing short integer values.
 * The reason for creating this implementation instead of using the default
 * is that the default one causes the container window to be closed everytime
 * the value is accepted.
 *
 * @author Jun Xu
 */
public class ShortIntegerInplaceEditor extends StringInplaceEditor implements InplaceEditor {
    
    private final BigInteger mMinInclusive;
    private final BigInteger mMaxInclusive;
    
    /** Creates a new instance of IntegerInplaceEditor */
    public ShortIntegerInplaceEditor() {
        super();
        mMinInclusive = new BigInteger(new Short(Short.MIN_VALUE).toString());
        mMaxInclusive = new BigInteger(new Short(Short.MAX_VALUE).toString());
    }

    /**
     * Creates a new instance of IntegerInplaceEditor with indicated
     * minInclusive value and maxInclusive value.
     * @param minInclusive the minimun (inclusive) value.
     * @param maxInclusive the maximum (inclusive) value.
     */
    public ShortIntegerInplaceEditor(short minInclusive, short maxInclusive) {
        super();
        mMinInclusive = new BigInteger(new Short(minInclusive).toString());
        mMaxInclusive = new BigInteger(new Short(maxInclusive).toString());
    }

    @Override
    public Object getValue() {
        short value;
        try {
            BigInteger rawValue = new BigInteger(mJTextField.getText());
            value = keepValueInRange(rawValue);
        } catch (NumberFormatException e) {
            if (mPropertyEditor != null) {
                setValue(mPropertyEditor.getValue());
                return mPropertyEditor.getValue();
            }
            value = keepValueInRange(BigInteger.ZERO);
            setValue(value);
        }
        return value;
    }
    
    /**
     * Keep the indicated value (as short) inside a range with max value of
     * upper bound and min value of lower bound.
     * 
     * @param value to be checked.
     * @return the original short value if the value is in the range, or
     * the upper (short maximum) or lower (short minimun) bound value.
     */
    private short keepValueInRange(BigInteger value) {
        if (value.compareTo(mMinInclusive) < 0) {
            return mMinInclusive.shortValue();
        }
        if (value.compareTo(mMaxInclusive) > 0) {
            return mMaxInclusive.shortValue();
        }
        return value.shortValue();
    }
}
