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
 * An InplaceEditor implementation for editing int values.  The reason for
 * creating this implementation instead of using the default is that the
 * default one causes the container window to be closed everytime the value
 * is accepted.
 *
 * @author Jun Xu
 */
public class IntegerInplaceEditor extends StringInplaceEditor implements InplaceEditor {
    
    private final BigInteger mMinInclusive;
    private final BigInteger mMaxInclusive;
    
    /** Creates a new instance of IntegerInplaceEditor */
    public IntegerInplaceEditor() {
        super();
        mMinInclusive = new BigInteger(new Integer(Integer.MIN_VALUE).toString());
        mMaxInclusive = new BigInteger(new Integer(Integer.MAX_VALUE).toString());
    }

    public IntegerInplaceEditor(int minInclusive, int maxInclusive) {
        super();
        mMinInclusive = new BigInteger(new Integer(minInclusive).toString());
        mMaxInclusive = new BigInteger(new Integer(maxInclusive).toString());
    }

    @Override
    public Object getValue() {
        int value;
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

    private int keepValueInRange(BigInteger value) {
        if (value.compareTo(mMinInclusive) < 0) {
            return mMinInclusive.intValue();
        }
        if (value.compareTo(mMaxInclusive) > 0) {
            return mMaxInclusive.intValue();
        }
        return value.intValue();
    }
}
