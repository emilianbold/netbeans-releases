/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.tax.util;

import java.io.CharConversionException;

import org.openide.xml.XMLUtil;

import org.netbeans.tax.*;

/**
 *
 * @author Libor Kramolis
 */
public final class TAXUtil {
    
    /**
     * Try to set new value to the attribute. Method <code>XMLUtil.toAttributeValue</code> is used
     * to convert value to correct attribute value.
     *
     * @see org.openide.xml.XMLUtil#toAttributeValue
     */
    public static void setAttributeValue (TreeAttribute attribute, String value) throws TreeException {
        try {
            attribute.setValue (XMLUtil.toAttributeValue (value));
        } catch (CharConversionException exc) {
            throw new TreeException (exc);
        }
    }
    
    /**
     * Try to set new value to the text. Method <code>XMLUtil.toElementContent</code> is used
     * to convert value to correct element content.
     *
     * @see org.openide.xml.XMLUtil#toElementContent
     */
    public static void setTextData (TreeText text, String value) throws TreeException {
        try {
            text.setData (XMLUtil.toElementContent (value));
        } catch (CharConversionException exc) {
            throw new TreeException (exc);
        }
    }
    
}
