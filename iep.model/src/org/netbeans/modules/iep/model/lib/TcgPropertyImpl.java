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


package org.netbeans.modules.iep.model.lib;

/**
 * Concrete class implementing PdsPropery interface. TcgPropertyImpl represents
 * properties associated to TcgComponentType. This class is not to be
 * referenced by applications. All access is through the interface
 * TcgProperty.
 *
 * @author Bing Lu
 *
 * @see TcgPropertyImpl
 * @see TcgComponentType
 * @since April 30, 2002
 */
class TcgPropertyImpl implements TcgProperty {
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(TcgPropertyImpl.class.getName());
    
    private static final long serialVersionUID = -5569776882156252393L;
    
    private Object mValue = null;
    private TcgComponent mParentComponent = null;
    private TcgPropertyType mTcgPropertyType = null;
    
    /**
     * Constructor for the TcgProperty object
     *
     * @param type TcgPropertyType object that invokes this constructor
     * @param parentTcgComponent
     */
    TcgPropertyImpl(TcgPropertyType type, TcgComponent parentTcgComponent) {
        mParentComponent = parentTcgComponent;
        mTcgPropertyType = type;
        mValue = type.getDefaultValue();
    }
    
    /**
     * Gets the name attribute of the TcgProperty object
     *
     * @return The name value
     */
    public String getName() {
        return getType().getName();
    }
    
    /**
     * Gets the containing TcgComponent of this property
     *
     * @return The containing TcgComponent of this property
     */
    public TcgComponent getParentComponent() {
        return mParentComponent;
    }
    
    /**
     * Sets the value attribute of the TcgProperty object in string format
     *
     * @param val The new value in string format
     */
    public void setStringValue(String val) {
        setValueInternal(getType().getType().parse((String) val));
    }
    
    /**
     * Gets the value attribute of the TcgProperty object in string format
     *
     * @return The value in string format
     */
    public String getStringValue() {
        return getType().getType().format(this, mValue);
    }
    
    /**
     * Gets the type attribute of the TcgProperty object
     *
     * @return The type value
     */
    public TcgPropertyType getType() {
        return mInstanceTcgPropertyType; //mTcgPropertyType;
    }
    
    /**
     * Sets the value attribute of the TcgProperty object
     *
     * @param val The new value value
     */
    public void setValue(Object val) {
        setValueInternal(val);
    }
    
    /**
     * Gets the value attribute of the TcgProperty object
     *
     * @return The value value
     */
    public Object getValue() {
        return mValue;
    }
    
    private void setValueInternal(Object newValue) {
        if (newValue == null) {
            return;
        }
        
        Object oldValue = mValue;
        
        mValue = newValue;
        
        getParentComponent().getPropertyChangeSupport().firePropertyChange(getName(), oldValue, mValue);
        getParentComponent().getRoot().getPropertyChangeSupport().firePropertyChange(getName(), oldValue, mValue);
    }
    
    /**
     * Overrides Object's
     *
     * @return DOCUMENT ME!
     */
    public String toString() {
        
        StringBuffer sb = new StringBuffer();
        sb.append("name=" + getType().getName() + ", " + "value=" + getValue());
        
        return sb.toString();
    }
    
    //=========================================================================
    
    public boolean hasValue() {
        return mValue != null;
    }
    
    public java.util.List getListValue() {
        return (java.util.List)mValue;
    }
    
    public int getIntValue() {
        return ((Integer)mValue).intValue();
    }
    
    public double getDblValue() {
        return ((Double)mValue).doubleValue();
    }
    
    public boolean getBoolValue() {
        return ((Boolean)mValue).booleanValue();
    }
    //=========================================================================
    
    private InstanceTcgPropertyType mInstanceTcgPropertyType = new InstanceTcgPropertyType();
    
    // Override our type's attributes
    public class InstanceTcgPropertyType implements TcgPropertyType {
        public Object getDefaultValue() {
            return mTcgPropertyType.getDefaultValue();
        }
        
        public String getDefaultValueAsString() {
            return mTcgPropertyType.getDefaultValueAsString();
        }

        public String getDescription() {
            return mTcgPropertyType.getDescription();
        }
        
        public String getEditorName() {
            return mTcgPropertyType.getEditorName();
        }
        
        public boolean isMultiple() {
            return mTcgPropertyType.isMultiple();
        }
        
        public String getName() {
            return mTcgPropertyType.getName();
        }
        
        public boolean isReadable() {
            return getTypeAttribute("access").indexOf("read") >= 0; //mTcgPropertyType.isReadable();
        }
        
        public boolean isWritable() {
            return mTcgPropertyType.isWritable();
        }
        
        public boolean isMappableL() {
            return mTcgPropertyType.isMappableL();
        }
        
        public boolean isMappableR() {
            return mTcgPropertyType.isMappableR();
        }
        
        public boolean isExecutable() {
            return mTcgPropertyType.isExecutable();
        }
        
        public String getAccess() {
            return mTcgPropertyType.getAccess();
        }
        
        public boolean hasAccess(String access) {
            return mTcgPropertyType.hasAccess(access);
        }
        
        public String getRendererName() {
            return mTcgPropertyType.getRendererName();
        }
        
        public boolean isRequired() {
            return mTcgPropertyType.isRequired();
        }
        
        public String getTitle() {
            return mTcgPropertyType.getTitle();
        }
        
        public TcgType getType() {
            return mTcgPropertyType.getType();
        }
        
        public TcgProperty newTcgProperty(TcgComponent parentTcgComponent) {
            return mTcgPropertyType.newTcgProperty(parentTcgComponent);
        }
        
        public String getScript() {
            // Infinite loop possible! See genCode above
            return mTcgPropertyType.getScript();
        }
        
        public String getCategory() {
            return mTcgPropertyType.getCategory();
        }
        
        public boolean isTransient() {
            return mTcgPropertyType.isTransient();
        }
        
        private String getTypeAttribute(String attrName) {
            String ret = "";
            try {
                if (attrName.equals("access")) {
                    ret = mTcgPropertyType.getAccess();
                }
                if (attrName.equals("default")) {
                    ret = mTcgPropertyType.getDefaultValue()+"";
                }
                if (attrName.equals("description")) {
                    ret = mTcgPropertyType.getDescription();
                }
                if (attrName.equals("category")) {
                    ret = mTcgPropertyType.getCategory();
                }
                if (attrName.equals("editor")) {
                    ret = mTcgPropertyType.getEditorName();
                }
                if (attrName.equals("multiple")) {
                    ret = mTcgPropertyType.isMultiple()+"";
                }
                if (attrName.equals("name")) {
                    ret = mTcgPropertyType.getName();
                }
                if (attrName.equals("renderer")) {
                    ret = mTcgPropertyType.getRendererName();
                }
                if (attrName.equals("required")) {
                    ret = mTcgPropertyType.isRequired()+"";
                }
                if (attrName.equals("script")) {
                    ret = mTcgPropertyType.getScript();
                }
                if (attrName.equals("title")) {
                    ret = mTcgPropertyType.getTitle();
                }
                if (attrName.equals("transient")) {
                    ret = mTcgPropertyType.isTransient()+"";
                }
                if (attrName.equals("type")) {
                    ret = mTcgPropertyType.getType()+"";
                }
            //mLog.debug(key);
        } catch (Exception e) {
            System.err.println(this + " type: " + mTcgPropertyType + " error: " + e.getMessage() + " failed");
            e.printStackTrace();
            //throw e;
        }
        //mLog.debug("ret: " + ret);
        return ret;
    }
} // end InstanceTcgPropertyType
}
