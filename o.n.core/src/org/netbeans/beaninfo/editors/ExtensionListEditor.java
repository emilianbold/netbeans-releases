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
import java.util.*;

import org.openide.loaders.ExtensionList;
import org.openide.util.enum.ArrayEnumeration;
import org.openide.explorer.propertysheet.editors.*;

/** Property editor for {@link ExtensionList}s.
*
* @see ExtensionList
*
* @author Jaroslav Tulach
* @version 0.11 November 11, 1997
*/
public class ExtensionListEditor extends Object implements PropertyEditor, StringArrayCustomizable {
    /** value to edit */
    private ExtensionList value;
    private PropertyChangeSupport support = new PropertyChangeSupport (this);

    /*
    * Set (or change) the object that is to be edited.  Builtin types such
    * as "int" must be wrapped as the corresponding object type such as
    * "java.lang.Integer".
    *
    * @param value The new target object to be edited.  Note that this
    *     object should not be modified by the PropertyEditor, rather
    *     the PropertyEditor should create a new object to hold any
    *     modified value.
    */
    public void setValue(Object value) {
        this.value = (ExtensionList)((ExtensionList)value).clone ();
        support.firePropertyChange (null, null, null);
    }

    /*
    * @return The value of the property.  Builtin types such as "int" will
    * be wrapped as the corresponding object type such as "java.lang.Integer".
    */
    public Object getValue() {
        return value;
    }

    /*
    * @return  True if the class will honor the paintValue method.
    */
    public boolean isPaintable() {
        return false;
    }

    /*
    * Paint a representation of the value into a given area of screen
    * real estate.  Note that the propertyEditor is responsible for doing
    * its own clipping so that it fits into the given rectangle.
    *

    * If the PropertyEditor doesn't honor paint requests (see isPaintable)
    * this method should be a silent noop.
    *

    * The given Graphics object will have the default font, color, etc of
    * the parent container.  The PropertyEditor may change graphics attributes
    * such as font and color and doesn't need to restore the old values.
    *
    * @param gfx  Graphics object to paint into.
    * @param box  Rectangle within graphics object into which we should paint.
    */
    public void paintValue (java.awt.Graphics gfx, java.awt.Rectangle box) {
    }

    /*
    * This method is intended for use when generating Java code to set
    * the value of the property.  It should return a fragment of Java code
    * that can be used to initialize a variable with the current property
    * value.
    *

    * Example results are "2", "new Color(127,127,34)", "Color.orange", etc.
    *
    * @return A fragment of Java code representing an initializer for the
    *          current value.
    */
    public String getJavaInitializationString() {
        return null;
    }

    /*
    * @return The property value as a human editable string.
    * Returns null if the value can't be expressed as an editable string.
    * If a non-null value is returned, then the PropertyEditor should
    *       be prepared to parse that string back in setAsText().
    */
    public String getAsText () {
        StringBuffer sb = new StringBuffer ();
        String[] exts = getStringArray ();
        for (int i = 0; i < exts.length; i++) {
            if (i > 0) sb.append (", "); // NOI18N
            sb.append (exts[i]);
        }
        return sb.toString ();
    }

    /*
    * Set the property value by parsing a given String.  May raise
    * java.lang.IllegalArgumentException if either the String is
    * badly formatted or if this kind of property can't be expressed
    * as text.
    * @param text  The string to be parsed.
    */
    public void setAsText (String text) throws java.lang.IllegalArgumentException {
        StringTokenizer st = new StringTokenizer (text, ",. \n\t"); // NOI18N
        setAs (st);
    }

    /*
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
        return null;
    }

    /*
    * A PropertyEditor may choose to make available a full custom Component
    * that edits its property value.  It is the responsibility of the
    * PropertyEditor to hook itself up to its editor Component itself and
    * to report property value changes by firing a PropertyChange event.
    *

    * The higher-level code that calls getCustomEditor may either embed
    * the Component in some larger property sheet, or it may put it in
    * its own individual dialog, or ...
    *
    * @return A java.awt.Component that will allow a human to directly
    *      edit the current property value.  May be null if this is
    *      not supported.
    */

    public java.awt.Component getCustomEditor() {
        return new StringArrayCustomEditor (this);
    }

    /*
    * @return  True if the propertyEditor can provide a custom editor.
    */
    public boolean supportsCustomEditor() {
        return true;
    }

    /*
    * Register a listener for the PropertyChange event.  When a
    * PropertyEditor changes its value it should fire a PropertyChange
    * event on all registered PropertyChangeListeners, specifying the
    * null value for the property name and itself as the source.
    *
    * @param listener  An object to be invoked when a PropertyChange
    *          event is fired.
    */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener (listener);
    }

    /*
    * Remove a listener for the PropertyChange event.
    *
    * @param listener  The PropertyChange listener to be removed.
    */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener (listener);
    }

    /** Used to acquire the current value from the PropertyEditor
     * @return the current value of the property
     */
    public String[] getStringArray() {
        List l = new ArrayList ();
        Enumeration e = value.extensions ();
        while (e.hasMoreElements ())
            l.add (e.nextElement ());
        
        
        e = value.mimeTypes ();
        while (e.hasMoreElements ())
            l.add (e.nextElement ());
        
        Collections.sort (l);
        return (String[]) l.toArray (new String[l.size ()]);
    }

    /** Used to modify the current value in the PropertyEditor
     * @param value the new value of the property
     */
    public void setStringArray(String[] nue) {
        setAs (new ArrayEnumeration (nue));
    }

    
    /** Sets the value as enumeration.
     * @param en enumeration of strings
     */
    private void setAs (Enumeration en) {
        ExtensionList list = new ExtensionList ();

        while (en.hasMoreElements()) {
            String tok = (String)en.nextElement ();
            if (tok.indexOf('/') >= 0) {
                // mime type!?
                list.addMimeType(tok);
            } else {
                list.addExtension (tok);
            }
        }
        value = list;
        
        support.firePropertyChange (null, null, null);
    }
        
}

/*
 * Log
 *  1    Jaga      1.0         4/4/00   Jaroslav Tulach 
 * $
 */
