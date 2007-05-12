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
