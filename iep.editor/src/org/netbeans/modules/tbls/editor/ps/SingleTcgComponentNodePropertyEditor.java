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

package org.netbeans.modules.tbls.editor.ps;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.tbls.model.TcgPropertyType;
import org.netbeans.modules.tbls.model.TcgType;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.HelpCtx;

/**
 *
 * @author Bing Lu
 */
public abstract class SingleTcgComponentNodePropertyEditor implements PropertyEditor, ComponentPropertyEditorConfig, ExPropertyEditor,HelpCtx.Provider {
    private static final Logger mLogger = Logger.getLogger(SingleTcgComponentNodePropertyEditor.class.getName());
   
    private Property mProperty;
    
    private OperatorComponent mComponent;
    
    private TcgPropertyType mPropertyType;
    
    private IEPModel mModel;
    
    // mValue is initialized to TcgComponentNodeProperty.getValue()
    // But we never use this initial value at all because we
    // have mProperty.  This mValue is used to store the modified value
    // and to pass it back to TcgComponentNodeProperty.setValue()
    protected Object mValue;  
    
    protected Vector mListeners;
    protected PropertyEnv mEnv;
    protected TcgComponentNodePropertyCustomizerState mCustomizerState;

    /**
     * Constructs a <code>PropertyEditorSupport</code> object.
     *
     * @param source the source used for event firing
     * @since 1.5
     */
    public SingleTcgComponentNodePropertyEditor() {
        
    }
    
    
    // Must be called right after instance creation
    public void setOperatorComponent(OperatorComponent component) {
        this.mComponent = component;
        if(component != null) {
            this.mModel = component.getModel();
        }
    }
    
    public OperatorComponent getOperatorComponent() {
        return this.mComponent;
    }
    
    public void setPropertyType(TcgPropertyType type) {
        this.mPropertyType = type;
    }
    
    public TcgPropertyType getPropertyType() {
        return this.mPropertyType;
    }
    
    public void setProperty(Property property) {
        this.mProperty = property;
    }
    
    public Property getProperty() {
        return this.mProperty;
    }
    
    
    public IEPModel getModel() {
        return this.mModel;
    }
    
  
    /**
     * Set (or change) the object that is to be edited.
     *
     * @param value The new target object to be edited.  Note that this
     *     object should not be modified by the PropertyEditor, rather 
     *     the PropertyEditor should create a new object to hold any
     *     modified value.
     */
    public void setValue(Object value) {
        mValue = value;
    firePropertyChange();
    }

    /**
     * Gets the value of the property.
     *
     * @return The value of the property.
     */
    public Object getValue() {
    return mValue;
    }

    //----------------------------------------------------------------------

    /**
     * Determines whether the class will honor the painValue method.
     *
     * @return  True if the class will honor the paintValue method.
     */

    public boolean isPaintable() {
    return false;
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
     *       current value.
     */
    public String getJavaInitializationString() {
    return "???";
    }

    //----------------------------------------------------------------------

    /**
     * Gets the property value as a string suitable for presentation
     * to a human to edit.
     * 
     * Used by property cell renderer
     *
     * @return The property value as a string suitable for presentation
     *       to a human to edit.
     * <p>   Returns "null" is the value can't be expressed as a string.
     * <p>   If a non-null value is returned, then the PropertyEditor should
     *         be prepared to parse that string back in setAsText().
     */
    public String getAsText() {
        // See TcgComponentNodeProperty.getValue() for all possible return-types
        Object value = getValue();
        if (value == null) {
            return "";
        }
        TcgType type = getPropertyType().getType();
        Property prop = getProperty();
        if(prop == null) {
            return "";
        }
        
        if (type == TcgType.STRING_LIST) {
            
            String val = prop.getValue();
            List list =  ( List) prop.getPropertyType().getType().parse(val);
            StringBuffer sb = new StringBuffer();
            sb.append("[");
            for (int i = 0, I = list.size(); i < I; i++) {
                String s = (String)list.get(i);
                if (0 < i) {
                    sb.append(",");
                }
                if (s.startsWith("i18n.")) {
                    sb.append(TcgPsI18n.getI18nString(s.substring(5)));
                } else {
                    sb.append(s);
                }
            }
            sb.append("]");
            return sb.toString();
        }
        if (type == TcgType.STRING) {
            String s = prop.getValue();
            if (s.startsWith("i18n.")) {
                return TcgPsI18n.getI18nString(s.substring(5));
            } 
            return s;
        }
        if (mProperty.getPropertyType().isMultiple()) {
            String s = prop.getValue();
            s = "[" + s.replace("\\", ",") + "]";
            return s;
        }
        return prop.getValue();
    }

    /**
     * Sets the property value by parsing a given String.  May raise
     * java.lang.IllegalArgumentException if either the String is
     * badly formatted or if this kind of property can't be expressed
     * as text.
     * Used by inplace editing
     *
     * @param text  The string to be parsed.
     */
    public void setAsText(String t) {
        // Note that TcgComponentNodeProperty.setValue() do take String as input
        // and call TcgProperty.setStringValue(t)
        setValue(t);
    }

    //----------------------------------------------------------------------

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
    return null;
    }

    //----------------------------------------------------------------------

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
     *        not supported.
     */

    public java.awt.Component getCustomEditor() {
    return null;
    }

    /**
     * Determines whether the propertyEditor can provide a custom editor.
     *
     * @return  True if the propertyEditor can provide a custom editor.
     */
    public boolean supportsCustomEditor() {
    return false;
    }
  
    //----------------------------------------------------------------------

    /**
     * Register a listener for the PropertyChange event.  The class will
     * fire a PropertyChange value whenever the value is updated.
     *
     * @param listener  An object to be invoked when a PropertyChange
     *        event is fired.
     */
    public synchronized void addPropertyChangeListener(
                PropertyChangeListener listener) {
    if (mListeners == null) {
        mListeners = new java.util.Vector();
    }
    mListeners.addElement(listener);
    }

    /**
     * Remove a listener for the PropertyChange event.
     *
     * @param listener  The PropertyChange listener to be removed.
     */
    public synchronized void removePropertyChangeListener(
                PropertyChangeListener listener) {
    if (mListeners == null) {
        return;
    }
    mListeners.removeElement(listener);
    }

    /**
     * Report that we have been modified to any interested listeners.
     */
    public void firePropertyChange() {
    java.util.Vector targets;
    synchronized (this) {
        if (mListeners == null) {
            return;
        }
        targets = (java.util.Vector) mListeners.clone();
    }
    // Tell our listeners that "everything" has changed.
        PropertyChangeEvent evt = new PropertyChangeEvent(mProperty, null, null, null);

    for (int i = 0; i < targets.size(); i++) {
        PropertyChangeListener target = (PropertyChangeListener)targets.elementAt(i);
        target.propertyChange(evt);
    }
    }
    
    //============ ExPropertyEditor interface ==============================//
    /**
     * This method is called by the IDE to pass
     * the environment to the property editor.
     * @param env Environment passed by the ide.
     */
    public void attachEnv(PropertyEnv env) {
        mEnv = env;
    }

    /**
     * Describe <code>getEnvState</code> method here.
     *
     * @return an <code>int</code> value
     */
    public Object getEnvState() {
        return mEnv.getState();
    }

    public PropertyEnv getEnv() {
        return mEnv;
    }
    //============ ExPropertyEditor interface ==============================//
    
    public void attachCustomizerState(TcgComponentNodePropertyCustomizerState customizerState) {
        mCustomizerState = customizerState;
    }
    
    public HelpCtx getHelpCtx() {
        //return new HelpCtx("org.netbeans.modules.iep.editor.tcg.ps.TcgComponentNodePropertyEditor");   
        return new HelpCtx("iep_work_iepops");   
    }

 }
