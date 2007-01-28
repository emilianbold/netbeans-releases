/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.designtime.faces;

import javax.faces.el.ValueBinding;
import com.sun.rave.designtime.DesignProperty;

/**
 * <p>A FacesDesignProperty represents a single property (setter/getter method pair) on a single
 * instance of a FacesDesignBean at design-time.  This interface adds methods for dealing with
 * value binding expressions on a DesignProperty.</p>
 *
 * <P><B>IMPLEMENTED BY CREATOR</B> - This interface is implemented by Creator for use by the
 * component (bean) author.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see DesignProperty
 * @see com.sun.rave.designtime.DesignBean#getProperties()
 * @see com.sun.rave.designtime.DesignBean#getProperty(String)
 */
public interface FacesDesignProperty extends DesignProperty {

    /**
     * Returns <code>true</code> if this property is currently bound with a value expression,
     * <code>false</code> if not.
     *
     * @return <code>true</code> if this property is currently bound with a value expression,
     * <code>false</code> if not
     */
    public boolean isBound();

    /**
     * Returns the ValueBinding object that this property is bound to, IF this property is currently
     * bound via a value expression, or null if it is not bound
     *
     * @return the ValueBinding object that this property is bound to, IF this property is currently
     * bound via a value expression, or null if it is not bound
     */
    public ValueBinding getValueBinding();

    /**
     * Sets the ValueBinding on this property, resulting in the property value being set to the
     * appropriate value binding expression
     *
     * @param binding The desired ValueBinding to set as the value on this property
     */
    public void setValueBinding(ValueBinding binding);
}
