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

package com.netbeans.enterprise.modules.db.explorer;

import java.beans.*;
import java.awt.*;
import java.sql.*;

/** A property editor for Color class.
* @author   Jan Jancura, Ian Formanek
* @version  0.10, 09 Mar 1998
*/
public class DatabaseTypePropertyEditor implements PropertyEditor
{
	private int[] constants;
	private String[] names;
	private int index;
	private String name;
	private PropertyChangeSupport support;

	public DatabaseTypePropertyEditor()
	{
		support = new PropertyChangeSupport(this);
		constants = new int[] {java.sql.Types.ARRAY, java.sql.Types.BIGINT, java.sql.Types.BINARY, java.sql.Types.BIT, java.sql.Types.BLOB, java.sql.Types.CHAR, java.sql.Types.CLOB, java.sql.Types.DATE, java.sql.Types.DECIMAL, java.sql.Types.DISTINCT, java.sql.Types.DOUBLE, java.sql.Types.FLOAT, java.sql.Types.INTEGER, java.sql.Types.JAVA_OBJECT, java.sql.Types.LONGVARBINARY, java.sql.Types.LONGVARCHAR, java.sql.Types.NUMERIC, java.sql.Types.REAL, java.sql.Types.REF, java.sql.Types.SMALLINT, java.sql.Types.TIME, java.sql.Types.TIMESTAMP, java.sql.Types.TINYINT, java.sql.Types.VARBINARY, java.sql.Types.VARCHAR};
		names = new String[] {"ARRAY", "BIGINT", "BINARY", "BIT", "BLOB", "CHAR", "CLOB", "DATE", "DECIMAL", "DISTINCT", "DOUBLE", "FLOAT", "INTEGER", "JAVA_OBJECT", "LONGVARBINARY", "LONGVARCHAR", "NUMERIC", "REAL", "REF", "SMALLINT", "TIME", "TIMESTAMP", "TINYINT", "VARBINARY", "VARCHAR"};
	}

	public DatabaseTypePropertyEditor(int[] types, String[] titles)
	{
		support = new PropertyChangeSupport(this);
		constants = types;
		names = titles;
	}

	public Object getValue () 
	{
		return new Integer(constants[index]);
	}

	public void setValue (Object object) 
	{
		System.out.println("Type for key "+object);
		if (!(object instanceof Integer)) throw new IllegalArgumentException ();
		int ii = ((Integer)object).intValue ();
		int i, k = constants.length;
		for (i = 0; i < k; i++) if (constants [i] == ii) break;
		if (i == k)  {
			throw new IllegalArgumentException ();
		}
		index = i;
		name = names [i];
		support.firePropertyChange (null, null, null);
	}

  public String getAsText () {
    return name;
  }

  public void setAsText (String string)
  throws IllegalArgumentException {
    int i, k = names.length;
    for (i = 0; i < k; i++) if (names [i].equals (string)) break;
    if (i == k)  throw new IllegalArgumentException ();
    index = i;
    name = names [i];
		System.out.println("Type for name "+string+": "+name);
    return;
  }

  public String getJavaInitializationString () {
    return "" + index;
  }

  public String[] getTags () {
    return names;
  }

  public boolean isPaintable () {
    return false;
  }

  public void paintValue (Graphics g, Rectangle rectangle) {
  }

  public boolean supportsCustomEditor () {
    return false;
  }

  public Component getCustomEditor () {
    return null;
  }

  public void addPropertyChangeListener (PropertyChangeListener propertyChangeListener) {
    support.addPropertyChangeListener (propertyChangeListener);
  }

  public void removePropertyChangeListener (PropertyChangeListener propertyChangeListener) {
    support.removePropertyChangeListener (propertyChangeListener);
  }
}

/*
 * <<Log>>
 *  2    Gandalf   1.1         5/21/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         5/14/99  Slavek Psenicka 
 * $
 */




