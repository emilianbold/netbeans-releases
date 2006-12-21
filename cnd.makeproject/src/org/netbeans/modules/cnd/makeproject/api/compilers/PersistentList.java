
/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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
package org.netbeans.modules.cnd.makeproject.api.compilers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Vector;

public class PersistentList extends Vector implements Serializable{
    private static final long serialVersionUID = -8893123456464434693L;
    
    /** Creates a new instance of PersistentList */
    public PersistentList() {
    }
    
    public PersistentList(List values) {
        super(values);
    }
    
    private static String getRoot() {
        String dir = System.getProperty("netbeans.user") + "/config/cndcodemodel/"; // NOI18N
        return dir;
    }
    
    /**
     * For serialization
     */
    public void saveList(ObjectOutputStream out) {
	try {
	    out.writeObject(this);
	}
	catch (IOException ioe) {
	    System.out.println("PersistentList - saveList - ioe " + ioe);
	}
    }

    public void saveList(String name) {
	File dirfile = new File(getRoot());
	if (!dirfile.exists()) {
	    dirfile.mkdirs();
	}

	try {
	    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getRoot() + name));
	    saveList(oos);
	    oos.flush();
	    oos.close();
	}
	catch (Exception e) {
	    System.out.println("e " + e);
	}
    }

    /**
     * For serialization
     */
    public static PersistentList restoreList(ObjectInputStream in) throws Exception {
        PersistentList list = null;
	try {
	    list = (PersistentList)in.readObject();
	}
	catch (Exception e) {
	    System.err.println("PersistentList - restorePicklist - e " + e); // NOI18N
	    throw e;
	}
        return list;
    }

    public static PersistentList restoreList(String name) {
        PersistentList ret = null;
	File file = new File(getRoot() + File.separator + name);
	if (!file.exists()) {
	    ; // nothing
	}
	else {
	    try {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getRoot() + name));
		ret = restoreList(ois);
		ois.close();
	    }
	    catch (Exception e) {
		System.err.println("PersistentList - restoreList - e" + e); // NOI18N
		System.err.println(getRoot() + name + " deleted"); // NOI18N
		file.delete();
	    }
	}
        return ret;
    }
    
//    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
//	try {
//	    out.writeObject(new Integer(size()));
//            for (int i = 0; i < size(); i++)
//                out.writeObject(elementAt(i));
//	}
//	catch (IOException ioe) {
//	    System.err.println("DefaultPicklistModel - writeObject - ioe " + ioe); // NOI18N
//	    throw ioe;
//	}
//    }

//    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
//	try {
//            int size = ((Integer)in.readObject()).intValue();
//            for (int i = 0; i < size; i++)
//                add(((String)in.readObject()));
//	}
//	catch (IOException e) {
//	    System.err.println("DefaultPicklistModel - readObject - e " + e); // NOI18N
//	    throw e;
//	}
//	catch (ClassNotFoundException e) {
//	    System.err.println("DefaultPicklistModel - readObject - e " + e); // NOI18N
//	    throw e;
//	}
//    }
}
