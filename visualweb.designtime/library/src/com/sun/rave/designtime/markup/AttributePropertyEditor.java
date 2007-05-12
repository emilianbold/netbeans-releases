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

import java.beans.PropertyEditor;

/**
 * <p>The AttributePropertyEditor interface extends the PropertyEditor interface (a special type of
 * property editor for markup attributes) to add the ability to persist and resolve complex types
 * in markup.  Normally, JavaBeans properties are persisted using the PropertyEditor method:
 * "getJavaInitializationString()", which must return a valid Java expression as a String.  When
 * an IDE rebuilds the state of a saved file, a Java compiler is used to interpret the property
 * settings.</p>
 *
 * <p>In markup attributes, a Java expression is not typically what is used to store a setting -
 * and thus a Java compiler cannot be used to interpret the expressions stored in markup.  This
 * interface is designed to replace the "getJavaInitializationString()" and compiler interpretation
 * steps in property persistence.</p>
 *
 * <P><B>IMPLEMENTED BY THE COMPONENT AUTHOR</B> - This interface is designed to be implemented by
 * the component (bean) author.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 */
public interface AttributePropertyEditor extends PropertyEditor {

    /**
     * <p>This method is intended for use when generating markup to set the value of a property.  It
     * should return a fragment of markup that can be used to initialize a variable with the current
     * property value.  It is not necessary to include the quote marks, as any attribute string will
     * be automatically enclosed in quotes.  If quotes are included, they will not double-up.</p>
     *
     * <p>Example results are "2", "#FF00FF", "#{WebPage1.button1}", etc.</b>
     *
     * @return A fragment of markup representing an initializer for the current value of this
     *         PropertyEditor.  This will be enclosed in quote marks in the markup.
     */
    public String getMarkupInitializationString();

    /**
     * This method is called while "resurrecting" the state of a DesignBean from markup.  The string
     * stored as the attribute value will be passed in (initString), and an instance of the
     * appropriate type for the property setting is expected to be returned.
     *
     * @param initString The String stored as the attribute value
     * @return The appropriately typed instance representing the property setting.
     */
    public Object resolveMarkupInitializationString(String initString);
}
