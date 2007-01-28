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

import org.netbeans.modules.visualweb.api.designer.cssengine.CssListValue;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValue;
import org.apache.batik.css.engine.value.ListValue;
import org.apache.batik.css.engine.value.Value;
import org.w3c.dom.DOMException;

/**
 * Bridge between the api interface and the batik value representation.
 *
 * @author Peter Zavadsky
 */
public class CssListValueImpl extends CssValueImpl implements CssListValue {

    /** Creates a new instance of CssListValueImpl */
    public CssListValueImpl(ListValue listValue) {
        super(listValue);
    }


    public ListValue getListValue() {
        return (ListValue)getValue();
    }


    public int getLength() throws DOMException {
        return getListValue().getLength();
    }

    public CssValue item(int index) throws DOMException {
        Value value = getListValue().item(index);
        return CssValueFactory.createCssValue(value);
    }
}
