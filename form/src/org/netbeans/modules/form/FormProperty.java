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

package org.netbeans.modules.form;

import java.util.*;
import java.beans.*;
import java.lang.reflect.*;
import org.openide.nodes.Node;

/** 
 * This class provides basic implementation of properties used in form editor.
 * FormProperty can use multiple property editors (by FormPropertyEditor) and
 * special "design values" (FormDesignValue implementations).
 *
 * FormProperty is an "interface" object that provides general access to one
 * property of some other object (called "target object"). To make it work,
 * only some connection to the target object must be implemented. There are
 * two (abstract) methods for this purpose in FormProperty class:
 *     public Object getTargetValue();
 *     public void setTargetValue(Object value);
 *
 * NOTE: Binding to target object can be switched off for reading or writing
 * by setting access type of property to DETACHED_READ or DETACHED_WRITE.
 *
 * There are some further methods (potentially suitable) for custom
 * implementation (overriding the default implementation):
 *     public boolean supportsDefaultValue();
 *     public Object getDefaultValue();
 *     public PropertyEditor getExpliciteEditor();
 *
 * NOTE: Properties are created for nodes and presented in property sheet.
 * Node object that owns properties should listen to the CURRENT_EDITOR
 * property change on each property and call firePropertySetsChange(null, null)
 * to notify the sheet about changing current property editor of a property.
 *
 * @author Tomas Pavek
 */
public abstract class FormProperty extends Node.Property {

    // --------------------
    // constants
    public static final String PROP_VALUE = "propertyValue";
    public static final String CURRENT_EDITOR = "currentEditor";
    public static final String PROP_PRE_CODE = "preCode";
    public static final String PROP_POST_CODE = "postCode";

    // Type of the property in relation to target object ("access type").
    // There are three levels of restriction here:
    //   NORMAL_RW - no restriction on both property and target object
    //   DETACHED_READ, DETACHED_WRITE - no reading or writing (or both) on
    //       target object (it is "detached"; value is cached by the property)
    //   NO_READ, NO_WRITE - it is not possible to perform read or write on
    //       property (so neither on target object)
    public static final int NORMAL_RW = 0;

    public static final int DETACHED_READ = 1; // no reading from target (bit 0)
    public static final int DETACHED_WRITE = 2; // no writing to target (bit 1)

    private static final int NO_READ_PROP = 4; // bit 2
    private static final int NO_WRITE_PROP = 8; // bit 3
    public static final int NO_READ = DETACHED_READ | NO_READ_PROP; // no reading from property (bits 0,2)
    public static final int NO_WRITE = DETACHED_WRITE | NO_WRITE_PROP; // no writing to property (bits 1,3)


    // ------------------------
    // variables
    protected int propType = NORMAL_RW;

    FormPropertyContext propertyContext = null;

    protected Object propertyValue = null; // cached value of the property
    protected boolean valueSet = false; // propertyValue validity
    boolean valueChanged = false; // i.e. non-default

    private boolean externalChangeMonitoring = true;
    private Object lastRealValue = null; // for detecting external change of the property value

    String preCode;
    String postCode;

    PropertyEditor currentEditor;

    private ArrayList listeners = new ArrayList();
    private boolean fireChanges = true;

    private DesignValueListener designValueListener = null;

    // ---------------------------
    // constructors

    protected FormProperty(FormPropertyContext propertyContext,
                           String name, Class type,
                           String displayName, String shortDescription) {
        super(type);
        setName(name);
        setDisplayName(displayName);
        setShortDescription(shortDescription);

        setPropertyContext(propertyContext);
    }

    protected FormProperty(FormPropertyContext propertyContext, Class type) {
        super(type);
        setPropertyContext(propertyContext);
    }

    // constructor of property without PropertyContext
    // setPropertyContext(...) should be called explicitly then
    protected FormProperty(String name, Class type,
                           String displayName, String shortDescription) {
        super(type);
        setName(name);
        setDisplayName(displayName);
        setShortDescription(shortDescription);

        this.propertyContext = FormPropertyContext.EmptyImpl.getInstance();
    }

    // constructor of property without PropertyContext;
    // setPropertyContext(...) must be called explicitly before the property
    // is used first time
    protected FormProperty(Class type) {
        super(type);
        this.propertyContext = FormPropertyContext.EmptyImpl.getInstance();
    }

    // ----------------------------------------
    // getter, setter & related methods

    /** Gets the real value of this property directly from the target object.
     */
    public abstract Object getTargetValue() throws IllegalAccessException,
                                                   InvocationTargetException;

    /** Sets the real property value directly to the target object.
     */
    public abstract void setTargetValue(Object value) throws IllegalAccessException,
                                                      IllegalArgumentException,
                                                      InvocationTargetException;

    /** Gets the value of the property.
     */
    public Object getValue() throws IllegalAccessException,
                                    InvocationTargetException {
//        if (!canRead())
//            throw new IllegalAccessException("Not a readable property: "+getName());
        Object value = checkCurrentValue();

        if (valueSet || (propType & DETACHED_READ) == 0)
            return value;

        return getDefaultValue();
    }

    /** Sets the property value.
     */
    public void setValue(Object value) throws IllegalAccessException,
                                              IllegalArgumentException,
                                              InvocationTargetException
    {
//        if (!canWrite())
//            throw new IllegalAccessException("Not a writeable property: "+getName());
        Object oldValue = null;
        if (canRead()) {
            try { // get the old value (still the current)
                oldValue = getValue();
                if (!(value instanceof FormDesignValue)
                    && (value == oldValue
                        || (value != null && value.equals(oldValue))))
                    return; // no change
            }
            catch (Exception e) {  // no problem -> keep null
            }
        }

        if (value == BeanSupport.NO_VALUE) {
            // special - BeanSupport.NO_VALUE resets change flag
            setChanged(false);
            propertyValue = value;
            lastRealValue = null;
            propertyValueChanged(oldValue, value);
            return;
        }

        Object defValue = supportsDefaultValue() ?
                            getDefaultValue() : BeanSupport.NO_VALUE;

        if (canWriteToTarget()) {
            // derive real value
            Object realValue = getRealValue(value);

            // set the real value to the target object
            if (realValue != FormDesignValue.IGNORED_VALUE) {
                setTargetValue(realValue);
            }
            else if (valueSet && defValue != BeanSupport.NO_VALUE) {
                setTargetValue(defValue);
            }

            if (canReadFromTarget()) {
                lastRealValue = getTargetValue();
//                if (value == realValue)
//                    value = lastRealValue;

/*
  Some bad properties of bad beans return another value than the one just set.
  So which one should be then used as the valid property value (displayed,
  generated in code, etc) - the one just set, or that got in turn from getter?
  (1) When the value just set is taken, then e.g. NONE_OPTION (-1) set to
  debugGraphicsOption of JComponent will be used and code generated, altough it
  is converted to 0 which is the default value, so no code should be generated.
  (2) When oppositely the value from getter after performing setter is taken,
  then e.g. setting "text/xml" to contentType of JEditorPane may fail at design
  time (editor kit is not found), so the value reverts to "text/plain" (default)
  and no code is generated, however it could work at runtime, so the code
  should be generated.
  [See also bug 12413.]
*/
            }
        }

        propertyValue = value; // cache the value for later...
        valueSet = true;

        // "changed" == property is readable and writeable and the new value
        // is not equal to the default value (or default value doesn't exist).
        setChanged((propType & (NO_READ_PROP|NO_WRITE_PROP)) == 0
                   && (defValue == BeanSupport.NO_VALUE
                       || (value != defValue 
                           && (value == null || !value.equals(defValue)))));
        // or use Utilities.compareObjects(defValue,value) ?

        settleDesignValueListener(oldValue, value);
        propertyValueChanged(oldValue, value);
    }

    /** This method gets the real value of the property. This is a support
     * for special "design values" that hold additional information besides
     * the real value that can be set directly to target object.
     */
    public final Object getRealValue() throws IllegalAccessException,
                                              InvocationTargetException {
        return getRealValue(getValue());
    }

    /** This method "extracts" the real value from the given object.
     * FormDesignValue is recognized by default. Subclasses may override
     * this method to provide additional conversions.
     */
    protected Object getRealValue(Object value) {
        return value instanceof FormDesignValue ?
                 ((FormDesignValue)value).getDesignValue() : value;
    }

    /** Returns whether this property has a default value (false by default).
     * If any subclass provides default value, it should override this
     * and getDefaultValue() methods.
     * @return true if there is a default value, false otherwise
     */
    public boolean supportsDefaultValue () {
        return false;
    }

    /** Returns a default value of this property.
     * If any subclass provides default value, it should override this
     * and supportsDefaultValue() methods.
     * @returns the default value (null by default :)
     */
    public Object getDefaultValue() {
        return null;
    }

    /** Restores the property to its default value.
     */
    public void restoreDefaultValue() throws IllegalAccessException,
                                             InvocationTargetException {
//        if (!canWrite()) return;
        setChanged(false);

        Object oldValue = null;
        Object defValue = getDefaultValue();

        if (canRead()) {
            try {  // get the old value (still the current)
                oldValue = getValue();
                if (!(defValue instanceof FormDesignValue)
                    && (defValue == oldValue
                        || (defValue != null && defValue.equals(oldValue))))
                    return; // no change
            }
            catch (Exception e) {  // no problem -> keep null
            }
        }

        if (canWriteToTarget()) {
            // derive real value (from the default value)
            Object realValue = getRealValue(defValue);

            try {
                // set the default real value to the target
                if (realValue != FormDesignValue.IGNORED_VALUE) {
                    setTargetValue(realValue);
//                    lastRealValue = realValue;
                }
                else if (defValue != BeanSupport.NO_VALUE) {
                    setTargetValue(defValue);
//                    lastRealValue = defValue;
                }
//                else if (isExternalChangeMonitoring())
//                    lastRealValue = getTargetValue();

                lastRealValue = getTargetValue();
            }
            catch (IllegalArgumentException e) {} // should not happen
        }

        propertyValue = defValue;
        valueSet = true;

        // set default property editor as current
        PropertyEditor prEd = findDefaultEditor();
        if (prEd != null)
            setCurrentEditor(prEd);

        settleDesignValueListener(oldValue, defValue);
        propertyValueChanged(oldValue, defValue);
    }

    /** This method re-sets cached value of the property to the target object.
     * (If there is no cached value here, nothing is set to target object.)
     * This may be useful when target object was re-created and needs to be
     * initialized in accordance with current properties.
     */
    public void reinstateTarget() throws IllegalAccessException,
                                         InvocationTargetException {
        if (valueSet && canWriteToTarget()) 
            try {
                // re-set the real value of the property of the target object
                Object realValue = getRealValue(propertyValue);

                if (realValue != FormDesignValue.IGNORED_VALUE) {
                    setTargetValue(realValue);
                    lastRealValue = realValue;
                }
                else if (isExternalChangeMonitoring())
                    lastRealValue = getTargetValue();
            }
            catch (IllegalArgumentException e) { // should not happen
            }
    }

    /** This method updates state of the property according to the target
     * object. This may be useful when property needs to be initialized
     * with existing target object. But this approach doesn't work well with
     * bound and derived properties...
     */
    public void reinstateProperty() throws IllegalAccessException,
                                           InvocationTargetException {
        boolean mayChanged = canReadFromTarget()
                             && (propType & (NO_READ_PROP|NO_WRITE_PROP)) == 0;
            
        if (mayChanged) {
            Object value = getTargetValue();
            if (supportsDefaultValue()) {
                Object defValue = getDefaultValue();
                mayChanged = value != defValue
                             && (value == null || !value.equals(defValue));
            }
            if (mayChanged) {
                propertyValue = value;
                lastRealValue = value;
            }
        }
        
        valueSet = mayChanged;
        setChanged(mayChanged);
    }

    // ------------------------------
    // boolean flags

    /** Tests whether the property is readable.
     */
    public boolean canRead() {
        return (propType & NO_READ_PROP) == 0;
    }

    /** Tests whether the property is writable.
     */
    public boolean canWrite() {
        return (propType & NO_WRITE_PROP) == 0;
    }

    public final boolean canReadFromTarget() {
        return /*canRead() &&*/ (propType & DETACHED_READ) == 0;
    }

    public final boolean canWriteToTarget() {
        return /*canWrite() &&*/ (propType & DETACHED_WRITE) == 0;
    }

    /** Tests whether this property is marked as "changed". This method returns
     * true if the value of the property is different from the default value
     * and if it is accessible and replicable (readable and writeable property).
     */
    public boolean isChanged() {
        if (valueChanged && valueSet) { // update the changed flag
            try {
                checkCurrentValue();
            }
            catch (Exception ex) {
            }
        }
        return valueChanged;
    }

    /** Sets explicitly the flag indicating changed property.
     */
    public void setChanged(boolean changed) {
        valueChanged = changed;
    }

    // --------------------------------
    // property editors
    
    /** Gets a property editor for this property. This method implements
     * Node.Property.getPropertyEditor() and need not be further overriden.
     * It enables using of multiple individual editors by constructing
     * FormPropertyEditor class. There are other methods for controling the
     * FormPropertyEditor class here - see: getCurrentEditor(),
     * setCurrentEditor(...) and getExpliciteEditor().
     */
    public PropertyEditor getPropertyEditor() {
        PropertyEditor defaultEd = findDefaultEditor();

        if (!propertyContext.useMultipleEditors()) {
            propertyContext.initPropertyEditor(defaultEd);
            return defaultEd;
        }

        return defaultEd != null ?
                 new FormPropertyEditor(this) : null;
    }

    /** Gets the currently selected property editor (from multiple editors
     * managed by FormPropertyEditor).
     */
    public final PropertyEditor getCurrentEditor() {
        if (currentEditor == null) {
            currentEditor = findDefaultEditor();
            propertyContext.initPropertyEditor(currentEditor);
        }
        return currentEditor;
    }

    /** Sets the current property editor that will be used for this property
     * by FormPropertyEditor.
     */
    public final void setCurrentEditor(PropertyEditor editor) {
        PropertyEditor old = currentEditor;
        propertyContext.initPropertyEditor(editor);
        currentEditor = editor;

        currentEditorChanged(old, editor);
    }

    /** Gets the property editor explicitly designated for this property.
     * This editor is taken as default by FormPropertyEditor.
     * Subclasses should override this method if they provide a special
     * editor for this property.
     */
    public PropertyEditor getExpliciteEditor() {
        return null;
    }

    // ------------------------------
    // code generation

    /** Gets the java code initializing the property. It is obtained from
     * current property editor.
     */
    public String getJavaInitializationString() {
        try {
            Object value = getValue();
            if (value == null)
                return "null"; // NOI18N

            if (value == BeanSupport.NO_VALUE)
                return null;

            PropertyEditor ed = getCurrentEditor();
            if (ed == null)
                return null;

            // should we create a new instance of editor?
//            if (ed instanceof RADConnectionPropertyEditor)
//                ed = new RADConnectionPropertyEditor(getValueType());
//            else
//                ed = (PropertyEditor)ed.getClass().newInstance();
//            propertyContext.initPropertyEditor(ed);

            if (ed.getValue() != value)
                ed.setValue(value);
            return ed.getJavaInitializationString();
        }
        catch (Exception e) {
//            if (Boolean.getBoolean("netbeans.debug.exceptions"))
            e.printStackTrace();
        }
        return null;
    }

    // Property may optionally provide whole java setter code (not only
    // initialization String).
    public String getWholeSetterCode() {
        return null;
    }

    public String getPreCode() {
        return preCode;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPreCode(String value) {
        preCode = value;
    }

    public void setPostCode(String value) {
        postCode = value;
    }

    // ------------------------

    public FormPropertyContext getPropertyContext() {
        return propertyContext;
    }

    public void setPropertyContext(FormPropertyContext context) {
        propertyContext = context != null ? context :
                          FormPropertyContext.EmptyImpl.getInstance();
        if (currentEditor != null)
            propertyContext.initPropertyEditor(currentEditor);
    }

    public int getAccessType() {
        return propType;
    }

    public void setAccessType(int type) {
        if (type >= 0)
            propType = type;
    }

    public boolean isExternalChangeMonitoring() {
        return externalChangeMonitoring && propType == NORMAL_RW;
    }

    public void setExternalChangeMonitoring(boolean val) {
        externalChangeMonitoring = val;
    }

    // ----------------------------

    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        if (listeners == null)
            listeners = new ArrayList();
        else
            listeners.remove(l); // do not allow duplicates
        listeners.add(l);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        if (listeners != null)
            listeners.remove(l);
    }

    public boolean isChangeFiring() {
        return fireChanges;
    }

    public void setChangeFiring(boolean fire) {
        fireChanges = fire;
    }

    protected void propertyValueChanged(Object old, Object current) {
        if (fireChanges)
            firePropertyChange(PROP_VALUE, old, current);
    }

    protected void currentEditorChanged(PropertyEditor old,
                                           PropertyEditor current)
    {
        if (fireChanges)
            firePropertyChange(CURRENT_EDITOR, old, current);
    }

    private void firePropertyChange(String propName, Object old, Object current) {
//        if (!fireChanges) return;
        ArrayList targets;
        synchronized (this) {
            if (listeners == null) return;
            targets = (ArrayList)listeners.clone();
        }
        PropertyChangeEvent evt = new PropertyChangeEvent(this,
                                          propName, old, current);
        for (int i=0, n=targets.size(); i < n; i++)
            ((PropertyChangeListener)targets.get(i)).propertyChange(evt);
    }

    // ----------------------------
    // private methods

    private Object checkCurrentValue() throws IllegalAccessException,
                                              InvocationTargetException {
        if (valueSet) {
            Object value = null;

            if (isExternalChangeMonitoring()) {
                value = getTargetValue();
                if (value != lastRealValue
                      && (value == null || !value.equals(lastRealValue))) {
                    // the value is different from the one last set
                    valueSet = false;
                    if (value == null
                        || ((value.getClass().isPrimitive() || value instanceof String)
                            && !value.equals(lastRealValue)))
                        setChanged(false);
                        // the real value of the property was changed "externally"
                        // e.g. like label of JButton is changed when text is set
                    return value;
                }
            }
            return propertyValue;
        }
        return (propType & DETACHED_READ) == 0 ? getTargetValue() : null;
    }

    private PropertyEditor findDefaultEditor() {
        PropertyEditor defaultEditor = getExpliciteEditor();
        if (defaultEditor == null) {
            return FormPropertyEditorManager.findEditor(getValueType());
        } 
        return defaultEditor;
    }

    private void settleDesignValueListener(Object oldVal, Object newVal) {
        if (oldVal == newVal) return;

        if (oldVal instanceof FormDesignValue.Listener && designValueListener != null)
            ((FormDesignValue.Listener)oldVal).removeChangeListener(designValueListener);

        if (newVal instanceof FormDesignValue.Listener) {
            if (designValueListener == null)
                designValueListener = new DesignValueListener();
            ((FormDesignValue.Listener)newVal).addChangeListener(designValueListener);
        }
    }

    class DesignValueListener implements javax.swing.event.ChangeListener {
        public void stateChanged(javax.swing.event.ChangeEvent e) {
            if (valueSet && propertyValue == e.getSource())
                try {
                    setValue(propertyValue);
                }
                catch (Exception ex) { // can't do nothing here
                }
        }
    }
}
