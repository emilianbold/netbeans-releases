/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.loaders.form;

import java.beans.*;
import com.netbeans.ide.nodes.*;

/** 
 *
 * @author Ian Formanek
 */
public class FormPropertyEditor implements PropertyEditor, PropertyChangeListener {
  // -----------------------------------------------------------------------------
  // Private Variables

  private Object value;
  private Object source;
  private java.util.Vector listeners;
  private RADComponent radComponent;
  private PropertyEditor currentEditor;
  private Class propertyType;

  // -----------------------------------------------------------------------------
  // Constructor

  /** Crates a new FormPropertyEditor */
  FormPropertyEditor(RADComponent radComponent, Class propertyType, PropertyEditor defaultEditor) {
    source = this;
    this.radComponent = radComponent;
    this.propertyType = propertyType;
    currentEditor = defaultEditor;
    currentEditor.addPropertyChangeListener (this);
  }

  Class getPropertyType () {
    return propertyType;
  }
  
  PropertyEditor getCurrentEditor () {
    return currentEditor;
  }

  void setCurrentEditor (PropertyEditor editor) {
    if (currentEditor != null) {
      currentEditor.removePropertyChangeListener (this);
    }
    currentEditor = editor;
    currentEditor.setValue (value);
    if (currentEditor instanceof FormAwareEditor) {
      ((FormAwareEditor)currentEditor).setRADComponent (radComponent);
    }
    currentEditor.addPropertyChangeListener (this);
    firePropertyChange();
  }
  
  // -----------------------------------------------------------------------------
  // PropertyChangeListener implementation

  public void propertyChange (PropertyChangeEvent evt) {
    value = currentEditor.getValue ();
    firePropertyChange ();
  }
  
  // -----------------------------------------------------------------------------
  // PropertyEditor implementation

  /**
  * Set (or change) the object that is to be edited.
  * @param value The new target object to be edited.  Note that this
  *     object should not be modified by the PropertyEditor, rather 
  *     the PropertyEditor should create a new object to hold any
  *     modified value.
  */
  public void setValue(Object value) {
    this.value = value;
    currentEditor.setValue (value);
//    Thread.dumpStack();
    //firePropertyChange();
  }
  
  /**
  * Gets the value of the property.
  *
  * @return The value of the property.
  */
  public Object getValue() {
    return value;
  }

  // -----------------------------------------------------------------------------

  /**
  * Determines whether the class will honor the painValue method.
  *
  * @return  True if the class will honor the paintValue method.
  */
  public boolean isPaintable() {
    return currentEditor.isPaintable ();
  }

  /**
  * Paint a representation of the value into a given area of screen
  * real estate.  Note that the propertyEditor is responsible for doing
  * its own clipping so that it fits into the given rectangle.
  * <p>
  * If the PropertyEditor doesn't honor paint requests (see isPaintable)
  * this method should be a silent noop.
  *
  * @param gfx  Graphics object to paint into.
  * @param box  Rectangle within graphics object into which we should paint.
  */
  public void paintValue(java.awt.Graphics gfx, java.awt.Rectangle box) {
    currentEditor.paintValue (gfx, box);
  }

  // -----------------------------------------------------------------------------

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
  public String getJavaInitializationString() {
    return currentEditor.getJavaInitializationString ();
  }

  // -----------------------------------------------------------------------------

  /**
  * Gets the property value as a string suitable for presentation
  * to a human to edit.
  *
  * @return The property value as a string suitable for presentation
  *       to a human to edit.
  * <p>   Returns "null" is the value can't be expressed as a string.
  * <p>   If a non-null value is returned, then the PropertyEditor should
  *	     be prepared to parse that string back in setAsText().
  */
  public String getAsText() {
    return currentEditor.getAsText ();
  }

  /**
  * Sets the property value by parsing a given String.  May raise
  * java.lang.IllegalArgumentException if either the String is
  * badly formatted or if this kind of property can't be expressed
  * as text.
  *
  * @param text  The string to be parsed.
  */
  public void setAsText(String text) throws java.lang.IllegalArgumentException {
    currentEditor.setAsText (text);
  }

  // -----------------------------------------------------------------------------

  /**
  * If the property value must be one of a set of known tagged values, 
  * then this method should return an array of the tag values.  This can
  * be used to represent (for example) enum values.  If a PropertyEditor
  * supports tags, then it should support the use of setAsText with
  * a tag value as a way of setting the value.
  *
  * @return The tag values for this property.  May be null if this 
  *   property cannot be represented as a tagged value.
  *	
  */
  public String[] getTags() {
    return currentEditor.getTags ();
  }

  // -----------------------------------------------------------------------------

  /**
  * A PropertyEditor may chose to make available a full custom Component
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
    return new FormCustomEditor (this);
  }

  /**
  * Determines whether the propertyEditor can provide a custom editor.
  *
      if (allEditors[i].supportsCustomEditor ())
  * @return  True if the propertyEditor can provide a custom editor.
  */
  public boolean supportsCustomEditor() {
    PropertyEditor[] allEditors = FormPropertyEditorManager.getAllEditors (propertyType, false);
    if (allEditors.length > 1) return true; // we must allow to choose the editor even if none of them supports custom editing
    if (allEditors.length == 1) return allEditors[0].supportsCustomEditor ();
    return false;
  }

  // -----------------------------------------------------------------------------

  /**
  * Register a listener for the PropertyChange event.  The class will
  * fire a PropertyChange value whenever the value is updated.
  *
  * @param listener  An object to be invoked when a PropertyChange
  *		event is fired.
  */
  public synchronized void addPropertyChangeListener(
                            PropertyChangeListener listener) {
    if (listeners == null) {
      listeners = new java.util.Vector();
    }
    listeners.addElement(listener);
  }

  /**
  * Remove a listener for the PropertyChange event.
  *
  * @param listener  The PropertyChange listener to be removed.
  */
  public synchronized void removePropertyChangeListener(
                            PropertyChangeListener listener) {
    if (listeners == null) {
        return;
    }
    listeners.removeElement(listener);
  }

  /**
  * Report that we have been modified to any interested listeners.
  *
  * @param source  The PropertyEditor that caused the event.
  */
  public void firePropertyChange() {
    java.util.Vector targets;
    synchronized (this) {
      if (listeners == null) {
          return;
      }
      targets = (java.util.Vector) listeners.clone();
    }
    // Tell our listeners that "everything" has changed.
    PropertyChangeEvent evt = new PropertyChangeEvent(source, null, null, null);

    for (int i = 0; i < targets.size(); i++) {
      PropertyChangeListener target = (PropertyChangeListener)targets.elementAt(i);
      target.propertyChange(evt);
    }
  }

}

/*
 * Log
 *  3    Gandalf   1.2         5/31/99  Ian Formanek    Removed dumpStack
 *  2    Gandalf   1.1         5/30/99  Ian Formanek    
 *  1    Gandalf   1.0         5/24/99  Ian Formanek    
 * $
 */
