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

package com.netbeans.enterprise.modules.db.explorer.dlg;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.netbeans.ddl.*;
import org.openide.DialogDescriptor;
import org.openide.TopManager;
import org.openide.util.NbBundle;
import com.netbeans.enterprise.modules.db.explorer.*;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

/** 
* xxx
*
* @author Slavek Psenicka
*/

public class ColumnItem extends Hashtable
{
	public static final String NAME = "name";
	public static final String TYPE = "type";
	public static final String SIZE = "size";
	public static final String SCALE = "scale";
	public static final String PRIMARY_KEY = "pkey";
	public static final String INDEX = "idx";
	public static final String NULLABLE = "nullable";
	public static final String COMMENT = "comment";
	public static final String DEFVAL = "defval";
	public static final String UNIQUE = "unique";
	public static final String CHECK = "check";
	public static final String CHECK_CODE = "checkcode";

	private PropertyChangeSupport propertySupport;

	public static final Map getColumnProperty(int idx)
	{
		return (Map)getProperties().elementAt(idx);
	}

	public static final Vector getProperties()
	{
		return (Vector)CreateTableDialog.getProperties().get("columns");
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
		return getProperties("name");
	}

	public static final Vector getColumnTitles()
	{
		return getProperties("columntitle");
	}

	public static final Vector getColumnClasses()
	{
		return getProperties("columnclass");
	}

        static final long serialVersionUID =-6638535249384813829L;
	public ColumnItem()
	{
		Vector vec = getProperties();
		Enumeration evec = vec.elements();
		propertySupport = new PropertyChangeSupport(this);
		while (evec.hasMoreElements()) {
			Map pmap = (Map)evec.nextElement();
			Object pdv = pmap.get("default");
			if (pdv != null) {
				String pclass = (String)pmap.get("columnclass");
				if (pclass.equals("java.lang.Boolean")) pdv = new Boolean((String)pdv);
				put(pmap.get("name"), pdv);
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

/*
 * <<Log>>
 *  8    Gandalf   1.7         3/3/00   Radko Najman    added scale property
 *  7    Gandalf   1.6         11/27/99 Patrik Knakal   
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         9/8/99   Slavek Psenicka 
 *  4    Gandalf   1.3         6/15/99  Slavek Psenicka debug prints
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/21/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         5/14/99  Slavek Psenicka 
 * $
 */
