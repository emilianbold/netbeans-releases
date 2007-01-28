/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
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
