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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.awt.*;
import java.beans.*;
import java.lang.ref.WeakReference;
import java.security.*;

import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.ExPropertyEditor;

/** A multiplexing PropertyEditor used in the form editor.
 * It allows multiple editors to be used with one currently selected.
 *
 * @author Ian Formanek
 */

public class FormPropertyEditor implements PropertyEditor,
                                           PropertyChangeListener,
                                           EnhancedPropertyEditor,
                                           ExPropertyEditor
{
    private static String NO_VALUE_TEXT;

    private Object value = BeanSupport.NO_VALUE;

    private FormProperty property;
    private WeakReference propertyEnv;

    private PropertyEditor[] allEditors;
    private PropertyEditor lastCurrentEditor;

    private PropertyChangeSupport changeSupport;
    
    /** Crates a new FormPropertyEditor */
    FormPropertyEditor(FormProperty property) {
        this.property = property;
        PropertyEditor prEd = property.getCurrentEditor();
        if (prEd != null) {
            prEd.addPropertyChangeListener(this);
            value = prEd.getValue();
        }
    }

    Class getPropertyType() {
        return property.getValueType();
    }

    FormProperty getProperty() {
        return property;
    }

    FormPropertyContext getPropertyContext() {
        return property.getPropertyContext();
    }

    PropertyEnv getPropertyEnv() {
        return propertyEnv != null ? (PropertyEnv) propertyEnv.get() : null;
    }

    PropertyEditor getCurrentEditor() {
        return property.getCurrentEditor();
    }

    // -----------------------------------------------------------------------------
    // PropertyChangeListener implementation

    public void propertyChange(PropertyChangeEvent evt) {
        PropertyEditor prEd = property.getCurrentEditor();
        if (prEd != null)
            value = prEd.getValue();

        // we run this as privileged to avoid security problems - because
        // the property change can be fired from untrusted property editor code
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                FormPropertyEditor.this.firePropertyChange();
                return null;
            }
        });
    }

    // -----------------------------------------------------------------------------
    // PropertyEditor implementation

    /**
     * Set(or change) the object that is to be edited.
     * @param value The new target object to be edited.  Note that this
     *     object should not be modified by the PropertyEditor, rather 
     *     the PropertyEditor should create a new object to hold any
     *     modified value.
     */
    public void setValue(Object newValue) {
        value = newValue;

        PropertyEditor prEd = property.getCurrentEditor();
        if (value != BeanSupport.NO_VALUE && prEd != null)
            prEd.setValue(value);
    }

    /**
     * Gets the value of the property.
     *
     * @return The value of the property.
     */
    public Object getValue() {
        PropertyEditor prEd = property.getCurrentEditor();
        return prEd != null ? prEd.getValue() : value;
    }

    // -----------------------------------------------------------------------------

    /**
     * Determines whether the class will honor the painValue method.
     *
     * @return  True if the class will honor the paintValue method.
     */
    public boolean isPaintable() {
        PropertyEditor prEd = property.getCurrentEditor();
        return prEd != null ? prEd.isPaintable() : false;
    }

    /**
     * Paint a representation of the value into a given area of screen
     * real estate.  Note that the propertyEditor is responsible for doing
     * its own clipping so that it fits into the given rectangle.
     * <p>
     * If the PropertyEditor doesn't honor paint requests(see isPaintable)
     * this method should be a silent noop.
     *
     * @param gfx  Graphics object to paint into.
     * @param box  Rectangle within graphics object into which we should paint.
     */
    public void paintValue(Graphics gfx, Rectangle box) {
        PropertyEditor prEd = property.getCurrentEditor();
        if (prEd != null)
            prEd.paintValue(gfx, box);
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
        PropertyEditor prEd = property.getCurrentEditor();
        return prEd != null ? prEd.getJavaInitializationString() : null;
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
        if (value == BeanSupport.NO_VALUE) {
            if (NO_VALUE_TEXT == null)
                NO_VALUE_TEXT = FormUtils.getBundleString("CTL_ValueNotSet"); // NOI18N
            return NO_VALUE_TEXT;
        }

        PropertyEditor prEd = property.getCurrentEditor();
        return prEd != null ? prEd.getAsText() : null;
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
        PropertyEditor prEd = property.getCurrentEditor();
        if (prEd != null)
            prEd.setAsText(text);
    }

    // -----------------------------------------------------------------------------

    /**
     * If the property value must be one of a set of known tagged values, 
     * then this method should return an array of the tag values.  This can
     * be used to represent(for example) enum values.  If a PropertyEditor
     * supports tags, then it should support the use of setAsText with
     * a tag value as a way of setting the value.
     *
     * @return The tag values for this property.  May be null if this 
     *   property cannot be represented as a tagged value.
     *	
     */
    public String[] getTags() {
        PropertyEditor prEd = property.getCurrentEditor();
        return prEd != null ? prEd.getTags() : null;
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

    public Component getCustomEditor() {
        // hack: PropertyPicker wants code regenerated - it might lead to
        // setting values to property editors
        FormModel formModel = property.getPropertyContext().getFormModel();
        if (formModel != null) {
            JavaCodeGenerator codeGen = (JavaCodeGenerator) FormEditor.getCodeGenerator(formModel);
            if (codeGen != null) { // may happen property sheet wants something from an already closed form (#111205)
                codeGen.regenerateCode();
            }
        }

        Component customEditor;

        PropertyEditor prEd = property.getCurrentEditor();
        if (prEd != null && prEd.supportsCustomEditor()) {
            customEditor = prEd.getCustomEditor();
            if (customEditor instanceof Window)
                return customEditor;
        }
        else customEditor = null;

        return new FormCustomEditor(this, customEditor);
    }

    /**
     * Determines whether the propertyEditor can provide a custom editor.
     *
     * @return  True if the propertyEditor can provide a custom editor.
     */
    public boolean supportsCustomEditor() {
        PropertyEditor[] editors = getAllEditors();

        if (!property.canWrite()) { // read only property
            for (int i=0; i < editors.length; i++)
                if (!editors[i].getClass().equals(RADConnectionPropertyEditor.class)
                        && editors[i].supportsCustomEditor())
                    return true;
            return false;
        }

        // writable property
        if (editors.length > 1)
            return true; // we must  at least allow to choose the editor
        if (editors.length == 1)
            return editors[0].supportsCustomEditor();

        return false;
    }

    synchronized PropertyEditor[] getAllEditors() {
        if (allEditors != null) {
            // the current property editor might have changed and so not
            // present among the cached editors
            PropertyEditor currentEditor = property.getCurrentEditor();
            if (currentEditor != lastCurrentEditor) {
                allEditors = null;
            }
        }

        if (allEditors == null) {
            PropertyEditor expliciteEditor = property.getExpliciteEditor();
            PropertyEditor currentEditor = property.getCurrentEditor();
            lastCurrentEditor = currentEditor;
            if (expliciteEditor != null && currentEditor != null
                    && expliciteEditor.getClass().equals(currentEditor.getClass()))
            {   // they are the same, take care about the current editor only
                expliciteEditor = null;
            }
            PropertyEditor[] typeEditors = FormPropertyEditorManager.getAllEditors(property);

            // Explicite editor should be added to editors (if not already present).
            // The current editor should replace the corresponding default editor.
            // Replace the delegate editor in ResourceWrapperEditor if needed.
            for (int i=0; i < typeEditors.length && (expliciteEditor != null || currentEditor != null); i++) {
                PropertyEditor prEd = typeEditors[i];
                ResourceWrapperEditor wrapper = null;
                if (prEd instanceof ResourceWrapperEditor && !(currentEditor instanceof ResourceWrapperEditor)) {
                    // the current editor might be just loaded and thus not wrapped...
                    wrapper = (ResourceWrapperEditor) prEd;
                    prEd = wrapper.getDelegatedPropertyEditor();
                }
                if (currentEditor != null && currentEditor.getClass().equals(prEd.getClass())) {
                    // current editor matches
                    if (wrapper != null) { // silently make it the current editor
                        wrapper.setDelegatedPropertyEditor(currentEditor);
                        boolean fire = property.isChangeFiring();
                        property.setChangeFiring(false);
                        property.setCurrentEditor(wrapper);
                        property.setChangeFiring(fire);
                        PropertyEnv env = getPropertyEnv();
                        if (env != null)
                            wrapper.attachEnv(env);
                    }
                    else {
                        if (prEd instanceof RADConnectionPropertyEditor
                            && ((RADConnectionPropertyEditor)prEd).getEditorType()
                                != ((RADConnectionPropertyEditor)currentEditor).getEditorType()) {
                            continue; // there are two types of RAD... editors
                        }
                        typeEditors[i] = currentEditor;
                    }
                    currentEditor = null;
                }
                else if (expliciteEditor != null && expliciteEditor.getClass().equals(prEd.getClass())) {
                    if (wrapper != null)
                        wrapper.setDelegatedPropertyEditor(expliciteEditor);
                    else
                        typeEditors[i] = expliciteEditor;
                    expliciteEditor = null;
                }
            }

            int count = typeEditors.length;
            if (expliciteEditor != null)
                count++;
            if (currentEditor != null)
                count++;
            if (count > typeEditors.length) {
                allEditors = new PropertyEditor[count];
                int index = 0;
                if (currentEditor != null)
                    allEditors[index++] = currentEditor;
                if (expliciteEditor != null)
                    allEditors[index++] = expliciteEditor;
                System.arraycopy(typeEditors, 0, allEditors, index, typeEditors.length);
            }
            else allEditors = typeEditors;
        }
        return allEditors;
    }

    // -----------------------------------------------------------------------------
    // EnhancedPropertyEditor implementation

    /** Get an in-place editor.
     * @return a custom property editor to be shown inside the property
     *         sheet
     */
    public Component getInPlaceCustomEditor() {
        PropertyEditor prEd = property.getCurrentEditor();
        return prEd instanceof EnhancedPropertyEditor ?
               ((EnhancedPropertyEditor)prEd).getInPlaceCustomEditor() : null;
    }

    /** Test for support of in-place custom editors.
     * @return <code>true</code> if supported
     */
    public boolean hasInPlaceCustomEditor() {
        PropertyEditor prEd = property.getCurrentEditor();
        return prEd instanceof EnhancedPropertyEditor ?
               ((EnhancedPropertyEditor)prEd).hasInPlaceCustomEditor() : false;
    }

    /** Test for support of editing of tagged values.
     * Must also accept custom strings, otherwise you may may specify a standard property editor accepting only tagged values.
     * @return <code>true</code> if supported
     */
    public boolean supportsEditingTaggedValues() {
        PropertyEditor prEd = property.getCurrentEditor();
        return prEd instanceof EnhancedPropertyEditor ?
               ((EnhancedPropertyEditor)prEd).supportsEditingTaggedValues() :
               Boolean.TRUE.equals(property.getValue("canEditAsText")); // NOI18N
    }

    // -------------------------------------------------------------
    // FormPropertyContainer implementation
    
//    public Node.Property[] getProperties() {
//        if (modifiedEditor instanceof FormPropertyContainer)
//            return ((FormPropertyContainer)modifiedEditor).getProperties();
//        else
//            return null;
//    }

    // -----------------------------------------------------------------------------

    /**
     * Register a listener for the PropertyChange event.  The class will
     * fire a PropertyChange value whenever the value is updated.
     *
     * @param listener  An object to be invoked when a PropertyChange
     *		event is fired.
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        synchronized (this) {
            if (changeSupport == null)
                changeSupport = new PropertyChangeSupport(this);
        }
        changeSupport.addPropertyChangeListener(l);
    }

    /**
     * Remove a listener for the PropertyChange event.
     *
     * @param listener  The PropertyChange listener to be removed.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (changeSupport != null)
            changeSupport.removePropertyChangeListener(l);
    }

    /**
     * Report that we have been modified to any interested listeners.
     */
    void firePropertyChange() {
        if (changeSupport != null)
            changeSupport.firePropertyChange(null, null, null);
    }

    // -------------
    // ExPropertyEditor implementation

    /** 
     * This method is called by the IDE to pass
     * the environment to the property editor.
     */
    public void attachEnv(PropertyEnv env) {
        propertyEnv = new WeakReference(env);
        PropertyEditor prEd = property.getCurrentEditor();
        if (prEd instanceof ExPropertyEditor)
            ((ExPropertyEditor)prEd).attachEnv(env);
    }

    // ---------
    // delegating hashCode() and equals(Object) methods to modifiedEditor - for
    // PropertyPanel mapping property editors to PropertyEnv

    public int hashCode() {
        PropertyEditor prEd = property.getCurrentEditor();
        return prEd != null ? prEd.hashCode() : super.hashCode();
    }

    public boolean equals(Object obj) {
        return obj != null ? hashCode() == obj.hashCode() : false;
    }
}
