/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.dlg;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import org.netbeans.lib.ddl.*;
import org.openide.DialogDescriptor;
import org.openide.TopManager;
import org.openide.util.NbBundle;
import org.netbeans.modules.db.explorer.*;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

public class ColumnItem extends Hashtable {
    public static final String NAME = "name"; //NOI18N
    public static final String TYPE = "type"; //NOI18N
    public static final String SIZE = "size"; //NOI18N
    public static final String SCALE = "scale"; //NOI18N
    public static final String PRIMARY_KEY = "pkey"; //NOI18N
    public static final String INDEX = "idx"; //NOI18N
    public static final String NULLABLE = "nullable"; //NOI18N
    public static final String COMMENT = "comment"; //NOI18N
    public static final String DEFVAL = "defval"; //NOI18N
    public static final String UNIQUE = "unique"; //NOI18N
    public static final String CHECK = "check"; //NOI18N
    public static final String CHECK_CODE = "checkcode"; //NOI18N

    private PropertyChangeSupport propertySupport;

    public static final Map getColumnProperty(int idx)
    {
        return (Map)getProperties().elementAt(idx);
    }

    public static final Vector getProperties()
    {
        return (Vector)CreateTableDialog.getProperties().get("columns"); //NOI18N
    }

    public static final Vector getProperties(String pname)
    {
        Vector vec = getProperties(), cnames = new Vector(vec.size());
        Enumeration evec = vec.elements();
        while (evec.hasMoreElements()) {
            Map pmap = (Map)evec.nextElement();
            cnames.add(pmap.get(pname));
        }

        return cnames;
    }

    public static final Vector getColumnNames()
    {
        return getProperties("name"); //NOI18N
    }

    public static final Vector getColumnTitles()
    {
        return getProperties("columntitle"); //NOI18N
    }

    public static final Vector getColumnClasses()
    {
        return getProperties("columnclass"); //NOI18N
    }

    static final long serialVersionUID =-6638535249384813829L;
    public ColumnItem()
    {
        Vector vec = getProperties();
        Enumeration evec = vec.elements();
        propertySupport = new PropertyChangeSupport(this);
        while (evec.hasMoreElements()) {
            Map pmap = (Map)evec.nextElement();
            Object pdv = pmap.get("default"); //NOI18N
            if (pdv != null) {
                String pclass = (String)pmap.get("columnclass"); //NOI18N
                if (pclass.equals("java.lang.Boolean")) pdv = new Boolean((String)pdv); //NOI18N
                put(pmap.get("name"), pdv); //NOI18N
            }
        }
    }

    /** Add property change listener
    * Registers a listener for the PropertyChange event. The connection object
    * should fire a PropertyChange event whenever somebody changes driver, database,
    * login name or password.
    */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        propertySupport.addPropertyChangeListener (l);
    }

    /** Remove property change listener
    * Remove a listener for the PropertyChange event.
    */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        propertySupport.removePropertyChangeListener (l);
    }

    public Object getProperty(String pname)
    {
        return get(pname);
    }

    public void setProperty(String pname, Object value)
    {
        if (pname == null) return;
        Object old = get(pname);
        if (old != null) {
            Class oldc = old.getClass();
            if (old.equals(value)) return;

            try {
                if (!oldc.equals(value.getClass())) {
                    if (oldc.equals(Integer.class)) value = new Integer((String)value);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        put(pname, value);
        propertySupport.firePropertyChange(pname, old, value);
    }

    public String getName()
    {
        return (String)get(NAME);
    }

    public TypeElement getType()
    {
        return (TypeElement)get(TYPE);
    }

    public int getSize()
    {
        return ((Integer)get(SIZE)).intValue();
    }

    public boolean isPrimaryKey()
    {
        Boolean val = (Boolean)get(PRIMARY_KEY);
        if (val != null) return val.booleanValue();
        return false;
    }

    public boolean isUnique()
    {
        Boolean val = (Boolean)get(UNIQUE);
        if (val != null) return val.booleanValue();
        return false;
    }

    public boolean isIndexed()
    {
        Boolean val = (Boolean)get(INDEX);
        if (val != null) return val.booleanValue();
        return false;
    }

    public boolean allowsNull()
    {
        Boolean val = (Boolean)get(NULLABLE);
        if (val != null) return val.booleanValue();
        return false;
    }

    public boolean hasCheckConstraint()
    {
        Boolean val = (Boolean)get(CHECK);
        if (val != null) return val.booleanValue();
        return false;
    }

    public String getCheckConstraint()
    {
        return (String)get(CHECK_CODE);
    }

    public boolean hasDefaultValue()
    {
        String dv = getDefaultValue();
        if (dv != null && dv.length()>0) return true;
        return false;
    }

    public String getDefaultValue()
    {
        return (String)get(DEFVAL);
    }

    public boolean validate()
    {
        String name = getName();
        int size = getSize();
        int scale = getScale();

        if (size < scale)
            return false;
        if (name == null || name.length() == 0)
            return false;

        return true;
    }
    /** Getter for property scale.
     * @return Value of property scale.
     */
    public int getScale() {
        return ((Integer)get(SCALE)).intValue();
    }
}
