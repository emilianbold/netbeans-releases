/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package com.sun.rave.designtime;

import java.beans.PropertyDescriptor;

/**
 * <p>A DesignProperty represents a single property (setter/getter method pair) on a single instance
 * of a DesignBean at design-time.  All manipulation of properties at design-time should be done via
 * this interface.  This allows the IDE to both persist the changes as well as reflect them in the
 * design-time session.</p>
 *
 * <P><B>IMPLEMENTED BY CREATOR</B> - This interface is implemented by Creator for use by the
 * component (bean) author.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see DesignBean#getProperties()
 * @see DesignBean#getProperty(String)
 */
public interface DesignProperty {

    /**
     * Returns the PropertyDescriptor associated with this DesignProperty
     *
     * @return the PropertyDescriptor associated with this DesignProperty
     */
    public PropertyDescriptor getPropertyDescriptor();

    /**
     * Returns the DesignBean that this DesignProperty is associated with
     *
     * @return the DesignBean that this DesignProperty is associated with
     */
    public DesignBean getDesignBean();

    /**
     * Returns the current value of this DesignProperty.  The returned value is the *actual* value
     * that the design-time instance of the DesignBean has set for this property.
     *
     * @return the current value of this DesignProperty
     */
    public Object getValue();

    /**
     * Sets the current value of this DesignProperty.  This will set the *actual* value of this
     * property on the design-time instance of this DesignBean.  The associated PropertyEditor will
     * be used to produce the appropriate Java or markup code to set the property.  Calling this
     * method results in the persistence being written, and will cause the backing file buffer to
     * become "dirty".
     *
     * @param value The Object value to set as the currrent value of this property
     * @return <code>true</code> if the property setting was successful, <code>false</code> if it
     *         was not
     * @see java.beans.PropertyEditor
     */
    public boolean setValue(Object value);

    /**
     * Returns the source-persistence String value of this property.  This is the value that
     * the associated PropertyEditor would use to persist the property's current value in source
     * code.
     *
     * @return the source-persistence String value of this property
     * @see java.beans.PropertyEditor#getJavaInitializationString()
     */
    public String getValueSource();

    /**
     * Sets the source-persistence String value for this property.  This is the value that will
     * acutally appear in Java source code (or markup), depending on how the property setting is
     * persisted.
     *
     * @param source the source-persistence String value for this property
     * @return <code>true</code> if the property source setting was successful, <code>false</code>
     *         if not
     */
    public boolean setValueSource(String source);

    /**
     * Returns the value that this property would have if it were unset.  This is the property's
     * original (default) state, which is determined by reading the property value on a fresh
     * instance of the associated class (that owns this property).
     *
     * @return The unset (default) value for this property
     */
    public Object getUnsetValue();

    /**
     * Removes the property setting (if it exists) from the source code, and reverts the property
     * setting back to its original (default) state.  The original state is determined by reading
     * the property value on a fresh instance of the associated class (that owns this property),
     * and reading the default value of the property.
     *
     * @return <code>true</code> if the unset operation was successful, <code>false</code> if not
     */
    public boolean unset();

    /**
     * Returns <code>true</code> if this DesignProperty has been modified from the 'default' value.
     * A 'modified' property is one that differs in value (== and .equals()) from a newly
     * constructed instance of the DesignBean.
     *
     * @return <code>true</code> if this DesignProperty has been modified from the 'default' value,
     *         <code>false</code> if not
     */
    public boolean isModified();

//    /**
//     * Returns an array of DesignProperty objects representing the sub-properties of this property
//     * based on the static type of this property.
//     *
//     * @return
//     */
//    public DesignProperty[] getProperties();
//
//    /**
//     *
//     * @param propertyName
//     * @return
//     */
//    public DesignProperty getProperty(String propertyName);
//
//    /**
//     *
//     * @param property
//     * @return
//     */
//    public DesignProperty getProperty(PropertyDescriptor property);
}
