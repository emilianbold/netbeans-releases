/*
 * Field.java
 *
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * Contributor(s): Thomas Ball
 *
 * Version: $Revision$
 */

package org.netbeans.modules.classfile;

import java.io.*;

/**
 * Base class for variables and methods.
 *
 * @author  Thomas Ball
 */
public abstract class Field {

    /* name and type are lazily initialized, so must be
     * kept private. 
     */
    CPUTF8Info utfName;
    CPUTF8Info utfType;
    private String _name;
    private String _type;

    int access;
    protected boolean includeCode;
    private boolean deprecated = false;
    private boolean synthetic = false;

    /** Creates new Field */
    Field(DataInputStream in, ConstantPool pool, boolean includeCode) throws IOException {
        access = in.readUnsignedShort();
	this.includeCode = includeCode;
        CPEntry entry = null;
        try { // debug
	    utfName = (CPUTF8Info)pool.get(in.readUnsignedShort());
	    utfType = (CPUTF8Info)pool.get(in.readUnsignedShort());
            loadAttributes(in, pool);
        } catch (ClassCastException e) {
            // debug assertion
            System.out.println("error looking up constant pool entry: wanted type CPUTF8Info, got " + entry.getClass().getName() + "; e=" + e);
            e.printStackTrace();
            throw new IOException("internal error");
        }
    }
    
    private void loadAttributes(DataInputStream in, ConstantPool pool) throws IOException {       
        int n = in.readUnsignedShort();
        for (int i = 0; i < n; i++) {
            CPUTF8Info entry = (CPUTF8Info)pool.get(in.readUnsignedShort());
            int len = in.readInt();
            String name = entry.getName();
            if (name.equals("Deprecated"))
                deprecated = true;
            else if (name.equals("Synthetic"))
                synthetic = true;
            else if (loadAttribute(name, len, in, pool) == false)  {
                // ignore attribute...
		ClassFile.skip(in, len);
            }
        }
    }

    abstract boolean loadAttribute(String type, int len, 
        DataInputStream in, ConstantPool pool) throws IOException;
    
    public final String getName() {
	if (_name == null) {
            _name = utfName.getName();
	    utfName = null;              // release for gc
	}
        return _name;
    }
    
    public final String getType() {
	if (_type == null) {
            _type = utfType.getName();
	    utfType = null;              // release for gc
	}
        return _type;
    }

    public abstract String getFullName();
    
    public final int getAccess() {
        return access;
    }
    
    public final boolean isStatic() {
        return Access.isStatic(access);
    }

    public final boolean isPublic() {
        return Access.isPublic(access);
    }

    public final boolean isProtected() {
        return Access.isProtected(access);
    }

    public final boolean isPackagePrivate() {
        return Access.isPackagePrivate(access);
    }

    public final boolean isPrivate() {
        return Access.isPrivate(access);
    }

    public final boolean isDeprecated() {
        return deprecated;
    }
    
    public final boolean isSynthetic() {
        return synthetic;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getName());
        if (synthetic)
            sb.append(" (synthetic)"); //NOI18N
        if (deprecated)
            sb.append(" (deprecated)"); //NOI18N
        sb.append(" type="); //NOI18N
        sb.append(getType());
        sb.append(", access="); //NOI18N
        sb.append(Access.toString(access));
        return sb.toString();
    }
}
