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

import java.lang.reflect.InvocationTargetException;
import org.openide.nodes.PropertySupport;

/**
 *
 * @author jxu
 */
public class ReadOnlyDelimiterProperty
        extends PropertySupport.Reflection<String> {

    private EncodingOption mEncodingOption;

    /**
     * Creates a new instance of ReadOnlyDelimiterProperty
     * @param encodingOption (Bean) object to work on
     * @param clazz type of the property
     * @param getterName name of getter method
     * @throws NoSuchMethodException if the getter method cannot be found
     */
    public ReadOnlyDelimiterProperty(EncodingOption encodingOption,
            Class<String> clazz, String getterName)
            throws NoSuchMethodException {
        //  setterName = null so as to read only
        super(encodingOption, clazz, getterName, null);
        mEncodingOption= encodingOption;
    }

    @Override
    public String getValue()
            throws IllegalAccessException, IllegalArgumentException,
        InvocationTargetException {
        return mEncodingOption.getEndDelimitersAsString();
    }
}
