/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
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
