/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.schema2beans;

import java.beans.*;

public interface Bean {
    public void addPropertyChangeListener(PropertyChangeListener l);
    public void removePropertyChangeListener(PropertyChangeListener l);
    public BeanProp beanProp(String name);
    public String dtdName();
    public boolean isRoot();
    public Bean _getParent();
    public Bean _getRoot();
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
}
