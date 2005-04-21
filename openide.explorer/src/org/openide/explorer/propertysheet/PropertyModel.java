/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.explorer.propertysheet;

import java.beans.PropertyChangeListener;

import java.lang.reflect.InvocationTargetException;


/**
 * A model defining the behavior of a property.  This model is used to allow
 * components such as PropertyPanel to be used with arbitrary JavaBeans, without
 * requiring them to be instances of Node.Property.
 * <p>
 * <b>Note:</b>While not yet deprecated, this class will soon be deprecated.  The
 * only functionality it offers that is distinct from Node.Property and/or
 * PropertySupport.Reflection is the ability to listen to the model for
 * changes, rather than listening to the bean it is a property of.
 * <p>
 * Users of PropertyPanel are encouraged instead to use
 * its Node.Property constructor unless you are absolutely sure that
 * the property you want to display can be changed by circumstances
 * beyond your control <strong>while it is on screen</strong> (this is
 * usually a bug not a feature).
 *
 * @see DefaultPropertyModel
 * @author Jaroslav Tulach, Petr Hamernik
 */
public interface PropertyModel {
    /** Name of the 'value' property. */
    public static final String PROP_VALUE = "value"; // NOI18N

    /**
     * Getter for current value of a property.
     * @return the value
     * @throws InvocationTargetException if, for example, the getter method
     * cannot be accessed
     */
    public Object getValue() throws InvocationTargetException;

    /** Setter for a value of a property.
    * @param v the value
    * @exception InvocationTargetException if, for example, the setter cannot
    * be accessed
    */
    public void setValue(Object v) throws InvocationTargetException;

    /**
     * The class of the property.
     * @return A class object
     */
    public Class getPropertyType();

    /**
     * The class of the property editor or <CODE>null</CODE>
     * if default property editor should be used.
     * @return the class of PropertyEditor that should be used to edit this
     * PropertyModel
     */
    public Class getPropertyEditorClass();

    /** Add listener to change of the value.
    */
    public void addPropertyChangeListener(PropertyChangeListener l);

    /** Remove listener to change of the value.
    */
    public void removePropertyChangeListener(PropertyChangeListener l);
}
