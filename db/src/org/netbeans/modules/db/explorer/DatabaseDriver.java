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

package org.netbeans.modules.db.explorer;

import java.util.*;
import java.io.*;

/** 
* xxx
*
* @author Slavek Psenicka
*/
public class DatabaseDriver extends Object implements Externalizable
{
	private String name;
	private String url;
	private String prefix;
	private String adaptor;

        static final long serialVersionUID =7937512184160164098L;
	public DatabaseDriver()
	{
	}
	
	public DatabaseDriver(String dname, String durl)
	{
		name = dname;
		url = durl;
	}

	public DatabaseDriver(String dname, String durl, String dprefix)
	{
		name = dname;
		url = durl;
		prefix = dprefix;
	}
		
	public DatabaseDriver(String dname, String durl, String dprefix, String dbadap)
	{
		name = dname;
		url = durl;
		prefix = dprefix;
		adaptor = dbadap;
	}
	
	public String getName()
	{
		if (name != null) return name;
		return url;
	}
	
	public void setName(String nname)
	{
		name = nname;
	}
	
	public String getURL()
	{
		return url;
	}
	
	public void setURL(String nurl)
	{
		url = nurl;
	}

	public String getDatabasePrefix()
	{
		return prefix;
	}
	
	public void setDatabasePrefix(String pref)
	{
		prefix = pref;
	}
	
	public String getDatabaseAdaptor()
	{
		return adaptor;
	}
	
	public void setDatabaseAdaptor(String name)
	{
		if (name == null || name.length() == 0) adaptor = null;
		else if (name.startsWith("Database.Adaptors.")) adaptor = name;
		else adaptor = "Database.Adaptors."+name;
//		System.out.println("Metadata adaptor class set = "+adaptor);
	}
	
	public boolean equals(Object obj)
	{
		if (obj instanceof String) return obj.equals(url); 
		boolean c1 = ((DatabaseDriver)obj).getURL().equals(url); 
		boolean c2 = ((DatabaseDriver)obj).getName().equals(name); 
		return c1 && c2;
	}
	
	public String toString()
	{
		return getName();
	}
	
	/** Writes data
	* @param out ObjectOutputStream
	*/
  	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeObject(name);
		out.writeObject(url);	
		out.writeObject(prefix);
		out.writeObject(adaptor);
	}
	
	/** Reads data
	* @param in ObjectInputStream
	*/
 	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		name = (String)in.readObject();
		url = (String)in.readObject();
		prefix = (String)in.readObject();
		adaptor = (String)in.readObject();
	}	
}
/*
 * <<Log>>
 *  8    Gandalf   1.7         11/27/99 Patrik Knakal   
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         10/12/99 Radko Najman    debug messages removed
 *  5    Gandalf   1.4         9/27/99  Slavek Psenicka setAdaptor changed
 *  4    Gandalf   1.3         9/8/99   Slavek Psenicka adaptor changes
 *  3    Gandalf   1.2         7/21/99  Slavek Psenicka database prefix
 *  2    Gandalf   1.1         5/21/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         4/23/99  Slavek Psenicka 
 * $
 */
