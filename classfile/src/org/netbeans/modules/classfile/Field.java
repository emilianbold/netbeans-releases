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
import java.util.Collection;
import java.util.HashMap;

/**
 * Base class for variables and methods.
 *
 * @author  Thomas Ball
 */
public abstract class Field {

    /* name and type are lazily initialized, so must be
     * kept private. 
     */
    private CPUTF8Info utfName;
    private CPUTF8Info utfType;
    private String typeSignature;
    private String _name;
    private String _type;

    int access;
    ClassFile classFile;
    private boolean deprecated = false;
    private boolean synthetic = false;
    private HashMap annotations;

    /** Creates new Field */
    Field(DataInputStream in, ConstantPool pool, ClassFile classFile) throws IOException {
        access = in.readUnsignedShort();
	this.classFile = classFile;
        CPEntry entry = null;
        try { // debug
	    utfName = (CPUTF8Info)pool.get(in.readUnsignedShort());
	    utfType = (CPUTF8Info)pool.get(in.readUnsignedShort());
        } catch (ClassCastException e) {
            // debug assertion
            System.out.println("error looking up constant pool entry: wanted type CPUTF8Info, got " + entry.getClass().getName() + "; e=" + e);
            e.printStackTrace();
            throw new IOException("internal error");
        }
    }

    Field(String name, String type) {
	_name = name;
	_type = type;
    }
    
    final void loadAttributes(DataInputStream in, ConstantPool pool) throws IOException {       
	annotations = new HashMap(2);
        int n = in.readUnsignedShort();
        for (int i = 0; i < n; i++) {
            CPUTF8Info entry = (CPUTF8Info)pool.get(in.readUnsignedShort());
            int len = in.readInt();
            String name = entry.getName();
            if (name.equals("Deprecated"))
                deprecated = true;
            else if (name.equals("Synthetic"))
                synthetic = true;
	    else if (name.equals("RuntimeVisibleAnnotations")) { //NOI18N
		ClassFile.skip(in, len); //FIXME
	    }
	    else if (name.equals("RuntimeInvisibleAnnotations")) { //NOI18N
		ClassFile.skip(in, len); //FIXME
	    }
            else if (!loadAttribute(name, len, in, pool))  {
                // ignore attribute...
		ClassFile.skip(in, len);
            }
        }
    }

    abstract boolean loadAttribute(String type, int len, 
        DataInputStream in, ConstantPool pool) throws IOException;
    
    public final String getName() {
	if (_name == null && utfName != null) {
            _name = utfName.getName();
	    utfName = null;              // release for gc
	}
        return _name;
    }

    public final String getDescriptor() {
	if (_type == null && utfType != null) {
            _type = utfType.getName();
	    utfType = null;              // release for gc
	}
        return _type;
    }

    public abstract String getDeclaration();
    
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

    /**
     * Returns the class file this field is defined in.
     * @return the class file of this field.
     */
    public final ClassFile getClassFile() {
        return classFile;
    }
    
    /**
     * Returns the generic type information associated with this field.  
     * If this field does not have generic type information, then null 
     * is returned.
     */
    public String getTypeSignature() {
	return typeSignature;
    }

    void setTypeSignature(String sig) {
	typeSignature = sig;
    }

    /**
     * Returns all runtime annotations defined for this field.  Inherited
     * annotations are not included in this collection.
     */
    public final Collection getAnnotations() {
	return annotations.values();
    }

    /**
     * Returns the annotation for a specified annotation type, or null if
     * no annotation of that type exists for this field.
     */
    public final Annotation getAnnotation(final ClassName annotationClass) {
	return (Annotation)annotations.get(annotationClass);
    }
    
    /**
     * Returns true if an annotation of the specified type is defined for
     * this field.
     */
    public final boolean isAnnotationPresent(final ClassName annotationClass) {
	return annotations.get(annotationClass) != null;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
	String name = getName();
	if (name != null) {
	    sb.append(getName());
	    sb.append(' ');
	}
        if (synthetic)
            sb.append("(synthetic)"); //NOI18N
        if (deprecated)
            sb.append("(deprecated)"); //NOI18N
        sb.append("type="); //NOI18N
        sb.append(getDescriptor());
	if (typeSignature != null) {
	    sb.append(", signature="); //NOI18N
	    sb.append(typeSignature);
	}
        sb.append(", access="); //NOI18N
        sb.append(Access.toString(access));
        return sb.toString();
    }
}
