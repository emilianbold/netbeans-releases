/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo.editors;

import java.beans.*;

/**
 * Abstract class represents Editor for Wrappers of 8 known primitive types
 * (Byte, Short, Integer, Long, Boolean, Float, Double, Character)
 *
 * @author  Josef Kozak
 */
public abstract class WrappersEditor implements PropertyEditor {
    
    private PropertyEditor pe = null;
    
    public WrappersEditor(Class type) {
        super();
        pe = PropertyEditorManager.findEditor(type);
    }
    
    
    //----------------------------------------------------------------------
    
    /**
     * Accepts Character and String values. If the argument is
     * a String the first character is taken as the new value.
     * @param v new value
     */
    public void setValue(Object newValue) throws IllegalArgumentException {
        pe.setValue(newValue);
    }
    
    /**
     * Gets the value of the property.
     * @return The value of the property.
     */
    public Object getValue() {
	return pe.getValue();
    }        
    
    //----------------------------------------------------------------------
    
    /**
     * Determines whether this property editor is paintable.
     *
     * @return  True if the class will honor the paintValue method.
     */    
    public boolean isPaintable() {
	return pe.isPaintable();
    }

    /**
     * Paint a representation of the value into a given area of screen
     * real estate.  Note that the propertyEditor is responsible for doing
     * its own clipping so that it fits into the given rectangle.
     * <p>
     * If the PropertyEditor doesn't honor paint requests (see isPaintable)
     * this method should be a silent noop.
     * <p>
     * The given Graphics object will have the default font, color, etc of
     * the parent container.  The PropertyEditor may change graphics attributes
     * such as font and color and doesn't need to restore the old values.
     *
     * @param gfx  Graphics object to paint into.
     * @param box  Rectangle within graphics object into which we should paint.
     */
    public void paintValue(java.awt.Graphics gfx, java.awt.Rectangle box) {
        pe.paintValue(gfx, box);
    }        
    
    
    
    //----------------------------------------------------------------------    
    
    
    /** 
     * Converts the char to String by either leaving
     * the single char or by creating unicode escape.
     */
    public String getAsText () {
        return pe.getAsText();
    }
    /**
     * Set the property value by parsing given String.
     * @param text  The string to be parsed.
     */
    public void setAsText(String text) throws IllegalArgumentException {
        pe.setAsText(text);
    }
    
    
    //----------------------------------------------------------------------

    
    /**
     * If the property value must be one of a set of known tagged values, 
     * then this method should return an array of the tags.  This can
     * be used to represent (for example) enum values.  If a PropertyEditor
     * supports tags, then it should support the use of setAsText with
     * a tag value as a way of setting the value and the use of getAsText
     * to identify the current value.
     *
     * @return The tag values for this property.  May be null if this 
     *   property cannot be represented as a tagged value.
     *	
     */    
    public String[] getTags() {
	return pe.getTags();
    }
    
    //----------------------------------------------------------------------    

    /**
     * A PropertyEditor may choose to make available a full custom Component
     * that edits its property value.  It is the responsibility of the
     * PropertyEditor to hook itself up to its editor Component itself and
     * to report property value changes by firing a PropertyChange event.
     * <P>
     * The higher-level code that calls getCustomEditor may either embed
     * the Component in some larger property sheet, or it may put it in
     * its own individual dialog, or ...
     *
     * @return A java.awt.Component that will allow a human to directly
     *      edit the current property value.  May be null if this is
     *	    not supported.
     */    
    public java.awt.Component getCustomEditor() {
	return pe.getCustomEditor();
    }

    /**
     * Determines whether this property editor supports a custom editor.
     * @return  True if the propertyEditor can provide a custom editor.
     */
    public boolean supportsCustomEditor() {
	return pe.supportsCustomEditor();
    }
  
    //----------------------------------------------------------------------

    /**
     * Register a listener for the PropertyChange event.  When a
     * PropertyEditor changes its value it should fire a PropertyChange
     * event on all registered PropertyChangeListeners, specifying the
     * null value for the property name and itself as the source.
     *
     * @param listener  An object to be invoked when a PropertyChange
     *		event is fired.
     */    
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        pe.addPropertyChangeListener(listener);
    }

    /**
     * Remove a listener for the PropertyChange event.
     * @param listener  The PropertyChange listener to be removed.
     */
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        pe.removePropertyChangeListener(listener);
    }    
    
    //----------------------------------------------------------------------
    
    /**
     * This method is intended for use when generating Java code to set
     * the value of the property.  It should return a fragment of Java code
     * that can be used to initialize a variable with the current property
     * value.
     * <p>
     * Example results are "2", "new Color(127,127,34)", "Color.orange", etc.
     *
     * @return A fragment of Java code representing an initializer for the
     *   	current value.
     */    
    public abstract String getJavaInitializationString();
    
}
