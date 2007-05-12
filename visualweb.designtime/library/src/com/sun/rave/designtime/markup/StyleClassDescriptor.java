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

package com.sun.rave.designtime.markup;

import java.beans.FeatureDescriptor;
import org.w3c.dom.css.CSSStyleDeclaration;

/**
 * The StyleClassDescriptor describes a CSS style class declared in an associated CSS stylesheet.
 * These can be fetched as an array from a DesignContext by calling the 'getContextInfo(String key)'
 * method passing in the Constants.ContextData.CSS_STYLE_CLASS_DESCRIPTORS key.
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see com.sun.rave.designtime.DesignContext#getContextData(String)
 * @see com.sun.rave.designtime.Constants.ContextData#CSS_STYLE_CLASS_DESCRIPTORS
 */
public class StyleClassDescriptor extends FeatureDescriptor {

    protected CSSStyleDeclaration styleDeclaration;

    public StyleClassDescriptor() {}

    public StyleClassDescriptor(String styleClassName) {
        setName(styleClassName);
    }

    public StyleClassDescriptor(String styleClassName, CSSStyleDeclaration styleDeclaration) {
        setName(styleClassName);
        this.styleDeclaration = styleDeclaration;
    }

    public boolean equals(Object o) {
        if (o instanceof StyleClassDescriptor) {
            StyleClassDescriptor sd = (StyleClassDescriptor)o;
            return sd == this ||
                (getName() == sd.getName() ||
                 getName() != null && getName().equals(sd.getName())) &&
                (styleDeclaration == sd.styleDeclaration ||
                 styleDeclaration != null && styleDeclaration.equals(sd.styleDeclaration));
        }
        return false;
    }

    public void setStyleDeclaration(CSSStyleDeclaration styleDeclaration) {
        this.styleDeclaration = styleDeclaration;
    }

    public CSSStyleDeclaration getStyleDeclaration() {
        return styleDeclaration;
    }
}
