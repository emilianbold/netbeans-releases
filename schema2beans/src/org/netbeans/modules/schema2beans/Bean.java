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

package org.netbeans.modules.schema2beans;

import java.beans.*;

/**
 * @author cliffwd
 *
 * All generated beans that use the runtime will implement this interface.
 * It allows for some navigation and reflection.
 */
public interface Bean {
    public void addPropertyChangeListener(PropertyChangeListener l);
    public void removePropertyChangeListener(PropertyChangeListener l);

    /**
     * @return a representation of the property.  This method does not return
     * null.  If there is no object available for the specified
     * property name, an exception is thrown.
     */
    public BeanProp beanProp(String name);

    /**
     * @return the schema name of this bean as define by this bean's parent.
     */
    public String dtdName();
    public boolean isRoot();
    public Bean _getParent();
    public Bean _getRoot();

    /**
     *	Return the bean name of this graph node.
     */
    public String name();
    public boolean hasName(String name);
    public int indexToId(String name, int index);
    public int idToIndex(String name, int id);
    public Bean propertyById(String name, int id);
    public Object getValueById(String name, int id);
    public void setValue(String name, Object value);
    public void setValue(String name, int index, Object value);
    public void setValueById(String name, int id, Object value);
    public int removeValue(String name, Object value);
    public int addValue(String name, Object value);
    public Object getValue(String name);
    public Object getValue(String name, int index);
    public Object[] getValues(String name);
    public BaseProperty getProperty();
    public BaseProperty getProperty(String propName);
    public BaseProperty[] listProperties();

    /**
     * Find all child beans and put them into the give beans List.
     */
    public void childBeans(boolean recursive, java.util.List beans);
}
