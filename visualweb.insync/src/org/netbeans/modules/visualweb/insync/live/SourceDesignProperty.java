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
package org.netbeans.modules.visualweb.insync.live;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.Method;

import org.netbeans.modules.visualweb.extension.openide.util.Trace;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.PropertyEditor2;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.convert.Converter;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.validator.Validator;

/**
 * Abstract base partial DesignProperty implementation which manages a PropertyDescriptor and other
 * basic beans and designtime stuff and ties into the rest of the SourceLive* classes.
 *
 * @author Joe Nuxoll
 * @author Carl Quinn
 */
public abstract class SourceDesignProperty implements DesignProperty {

    public static final SourceDesignProperty[] EMPTY_ARRAY = {};
    public static final Object[] EMPTY_OBJECT_ARRAY = {};

    protected final PropertyDescriptor descriptor;
    protected final SourceDesignBean liveBean;

    protected PropertyEditor editor;
    protected boolean modified = false;
    protected Object initialValue;
    protected String category;

    //--------------------------------------------------------------------------------- Construction

    /**
     * @param descriptor
     * @param liveBean
     */
    protected SourceDesignProperty(PropertyDescriptor descriptor, SourceDesignBean liveBean) {
        this.descriptor = descriptor;
        this.liveBean = liveBean;
        if (descriptor.getWriteMethod() != null)
            this.initialValue = getValue();  // don't bother getting initial value for read-only props
    }

    /**
     * Called after construction to initialize the live state from the source
     */
    protected abstract void initLive();

    /**
     *
     */
    public static class ClipImage {
        String name;
        Object value;
        ClipImage(String name, Object value) { this.name = name; this.value = value; }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            toString(sb);
            return sb.toString();
        }

        public void toString(StringBuffer sb) {
            sb.append("[DesignProperty.ClipImage");
            sb.append(" name=" + name);
            sb.append(" value=" + value);
            sb.append("]");
        }
    }

    /**
     * @return
     */
    public ClipImage getClipImage() {
        if (!isModified())
            return null;
        return new ClipImage(descriptor.getName(), getValue());
    }

    //------------------------------------------------------------------------------------ Accessors

    /**
     * @param category
     */
    public void setPropertyCategory(String category) {
        this.category = category;
    }

    /**
     * @return
     */
    public String getPropertyCategory() {
        return category;
    }

    /*
     * @see com.sun.rave.designtime.DesignProperty#getPropertyDescriptor()
     */
    public PropertyDescriptor getPropertyDescriptor() {
        return descriptor;
    }

    /*
     * @see com.sun.rave.designtime.DesignProperty#getDesignBean()
     */
    public DesignBean getDesignBean() {
        return liveBean;
    }

    /**
     * Load the property editor for this property and cache it for internal use.
     */
    protected void loadEditor() {
        loadEditor(descriptor.getPropertyEditorClass());
    }

    /**
     * Load the property editor with an instance of editorClass for this property
     * and cache it for internal use.
     */
    protected void loadEditor(Class editorClass) {
        // Attempt to load specified property editor, if specified
        if (editorClass != null) {
            try {
                editor = (PropertyEditor)editorClass.newInstance();
            } catch (Exception e) {
                System.err.println("Caught " + e + " in SLP.loadEditor instantiating editor class: " +
                        editorClass.getName());
                //e.printStackTrace();
            }
        }
        // If no editor loaded, attempt to load a Creator-specific, default editor appropriate
        // for the property type
        if (editor == null) {
            Class propertyType = this.getPropertyDescriptor().getPropertyType();
            if (String.class.isAssignableFrom(propertyType))
                editor = new com.sun.rave.propertyeditors.StringPropertyEditor();
            else if (Integer.class.isAssignableFrom(propertyType))
                editor = new com.sun.rave.propertyeditors.IntegerPropertyEditor();
            else if (Long.class.isAssignableFrom(propertyType))
                editor = new com.sun.rave.propertyeditors.LongPropertyEditor();
            else if (Double.class.isAssignableFrom(propertyType))
                editor = new com.sun.rave.propertyeditors.DoublePropertyEditor();
            else if (Converter.class.isAssignableFrom(propertyType))
                editor = new com.sun.rave.propertyeditors.ConverterPropertyEditor();
            else if (Validator.class.isAssignableFrom(propertyType))
                editor = new com.sun.rave.propertyeditors.ValidatorPropertyEditor();
            else if (ValueBinding.class.isAssignableFrom(propertyType) || ValueExpression.class.isAssignableFrom(propertyType))
                editor = new com.sun.rave.propertyeditors.binding.ValueBindingPropertyEditor();
            else if (MethodBinding.class.isAssignableFrom(propertyType) || MethodExpression.class.isAssignableFrom(propertyType))
                editor = new com.sun.rave.propertyeditors.MethodBindingPropertyEditor();
            // If no editor loaded still, attempt to load the editor registered statically
            // with the Java Beans editor manager
            if (editor == null) {
                editor = PropertyEditorManager.findEditor(descriptor.getPropertyType());
                // An ultimate fallback editor is one that allows sibling bean selection
                if (editor == null)
                    editor = new BeanSelectionEditor(this);
            }
        }
        if (editor instanceof PropertyEditor2) {
            try {
                ((PropertyEditor2)editor).setDesignProperty(this);
            } catch (Exception e) {
                System.err.println("Caught " + e + " in SLP.loadEditor initing live editor: " + editor);
                //e.printStackTrace();
            }
        }
    }
    
    /**
     * Remove any property editor that may have been loaded prior.
     */
    protected void unloadPropertyEditor() {
        editor = null;
    }
    
    /**
     * @return The property editor for this property, for internal use.
     */
    public PropertyEditor getPropertyEditor() {
        if (editor == null)
            loadEditor();
        return editor;
    }
    
    //-------------------------------------------------------------------------------------- Getters
    
    /**
     * @return
     */
    protected Object invokeGetter() {
        Method getter = descriptor.getReadMethod();
        if (getter == null)
            return null;
        
        try {
            Object instance = liveBean.getInstance();
            if (instance != null) {
                Object value = getter.invoke(instance, EMPTY_OBJECT_ARRAY);
                assert Trace.trace("insync.live", "SLP.getValue " + descriptor.getName() + " is:" + value);
                return value;
            }
        } catch (Exception e) {
            System.err.println("Caught " + e + " in SLP.invokeGetter " +
                    liveBean.beanInfo.getBeanDescriptor().getBeanClass().getName() + "." +
                    descriptor.getName());
            //e.printStackTrace();
        }
        return null;
    }
    
    /*
     * @see com.sun.rave.designtime.DesignProperty#getValue()
     */
    public Object getValue() {
    	ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
    	try {
    		Thread.currentThread().setContextClassLoader(((LiveUnit)getDesignBean().getDesignContext()).getBeansUnit().getClassLoader());    	
            return invokeGetter();
    	} finally {
    		Thread.currentThread().setContextClassLoader(oldContextClassLoader);
    	}
    }
    
    /**
     * Expected to be overriden by subclasses
     */
    public abstract String getValueSource();
    
    /**
     * Calculate the Java source representation for a given object value using the property editor.
     *
     * @param value The value object.
     * @return the Java initialization string for the value.
     */
    protected final String toJavaInitializationString(Object value) {
        if (editor == null)
            loadEditor();
        if (editor != null) {
            try {
                editor.setValue(value);
                String text = editor.getJavaInitializationString();
                return text != null ? text : "null";
            } catch (Exception e) {
                System.err.println("Caught " + e + " in SLP.toJavaInitializationString editor: " + editor);
                //e.printStackTrace();
            }
        }
        return value != null ? String.valueOf(value) : "null";
    }
    
    //-------------------------------------------------------------------------------------- Setters
    
    /**
     * @param value
     * @return
     */
    protected boolean invokeSetter(Object value) {
        Method setter = descriptor.getWriteMethod();
        if (setter == null)
            return false;
        
        try {
            setter.invoke(liveBean.getInstance(), new Object[] {value});
            setModified(true);
            return true;
        } catch (Exception e) {
            System.err.print("Caught " + e + " in SLP.invokeSetter " +
                    liveBean.beanInfo.getBeanDescriptor().getBeanClass().getName() + "." +
                    descriptor.getName() + " to:" + value);
            if (value != null)
                System.err.println(" [" + value.getClass().getName() + "]");
            else
                System.err.println();
            //e.printStackTrace();
            return false;
        }
    }
    
    /*
     * @see com.sun.rave.designtime.DesignProperty#setValue(java.lang.Object)
     */
    public boolean setValue(Object value) {
    	ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
    	try {
    		Thread.currentThread().setContextClassLoader(((LiveUnit)getDesignBean().getDesignContext()).getBeansUnit().getClassLoader());
	        Object oldValue = invokeGetter();
	        boolean ok = invokeSetter(value);
	        if (ok)
	            liveBean.fireDesignPropertyChanged(this, oldValue);
	        return ok;
	    } finally {
			Thread.currentThread().setContextClassLoader(oldContextClassLoader);
		}
    }
    
    /*
     * @see com.sun.rave.designtime.DesignProperty#setValueSource(java.lang.String)
     */
    public abstract boolean setValueSource(String value);
    
    //--------------------------------------------------------------------------------- DesignProperty
    
    /**
     * Set this property as modified or unmodified. If the latter, then also regrab the initial
     * value.
     *
     * @param modified The modified state that this property should be set to.
     */
    public void setModified(boolean modified) {
        this.modified = modified;
        if (!modified)
            initialValue = getValue();
    }
    
    /**
     * Returns whether or not two given values are equal, including null-ness
     *
     * @param v1 The first value.
     * @param v2 The second value.
     * @return True iff the two values are equal.
     */
    public static boolean objectsEqual(Object v1, Object v2) {
        return (v1 == null) == (v2 == null) && (v1 == null || v1.equals(v2));
    }
    
    /**
     * Returns whether or not a given value is differnt than the initial value
     *
     * @param value The value to compare against this properties initial value.
     * @return True iff the value is equal to this properties initial value.
     */
    public boolean equalsInitial(Object value) {
        return objectsEqual(initialValue, value);
    }
    
    /*
     * @see com.sun.rave.designtime.DesignProperty#isModified()
     */
    public boolean isModified() {
        return modified;
    }
    
    
    /*
     * @see com.sun.rave.designtime.DesignProperty#getUnsetValue()
     */
    public Object getUnsetValue() {
        return initialValue;
    }
    
    /*
     * @see com.sun.rave.designtime.DesignProperty#unset()
     */
    public boolean unset() {
        boolean ok = setValue(initialValue);
        if (ok)
            modified = false;  // no longer modified, but no need to reacquire
        return ok;
    }
    
    //--------------------------------------------------------------------------------------- Object
    
    /**
     *
     */
    public String toString() {
        return "[SLP name:" + descriptor.getName() + " value:" + getValue() + "]";
    }
}
