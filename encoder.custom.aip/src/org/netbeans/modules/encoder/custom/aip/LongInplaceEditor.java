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

import org.openide.explorer.propertysheet.InplaceEditor;

/**
 * An InplaceEditor implementation for editing int values.  The reason for
 * creating this implementation instead of using the default is that the
 * default one causes the container window to be closed everytime the value
 * is accepted.
 *
 * @author Sun
 */
public class LongInplaceEditor extends StringInplaceEditor implements InplaceEditor {
    
    private final Long mMinInclusive;
    private final Long mMaxInclusive;
    
    /**
     * Creates a new instance of LongInplaceEditor using default bounds of 
     * Long.MIN_VALUE and Long.MAX_VALUE.
     */
    public LongInplaceEditor() {
        super();
        mMinInclusive = new Long(Long.MIN_VALUE);
        mMaxInclusive = new Long(Long.MAX_VALUE);
    }

    /**
     * Creates a new instance of LongInplaceEditor using bounds specified.
     * @param minInclusive - the lower bound.
     * @param maxInclusive - the upper bound.
     */
    public LongInplaceEditor(long minInclusive, long maxInclusive) {
        super();
        mMinInclusive = new Long(minInclusive);
        mMaxInclusive = new Long(maxInclusive);
    }

    @Override
    public Object getValue() {
        long value;
        try {
            Long rawValue = new Long(mJTextField.getText());
            value = keepValueInRange(rawValue);
        } catch (NumberFormatException e) {
            if (mPropertyEditor != null) {
                setValue(mPropertyEditor.getValue());
                return mPropertyEditor.getValue();
            }
            value = keepValueInRange(0L);
            setValue(value);
        }
        return value;
    }

    private long keepValueInRange(Long value) {
        if (value.compareTo(mMinInclusive) < 0) {
            return mMinInclusive.longValue();
        }
        if (value.compareTo(mMaxInclusive) > 0) {
            return mMaxInclusive.longValue();
        }
        return value.longValue();
    }
}
