/*
 * ClassFile.java
 *
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2000-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * Contributor(s): Thomas Ball
 *
 * Version: $Revision$
 */

package org.netbeans.modules.classfile;

import java.io.*;
import java.util.*;

/**
 * Class representing a Java class file.
 *
 * @author Thomas Ball
 */
public class ClassFile {

    ConstantPool constantPool; 
    int classAccess;
    CPClassInfo classInfo;
    CPClassInfo superClassInfo;
    CPClassInfo[] interfaces;
    Variable[] variables;
    Method[] methods;
    String sourceFileName;
    boolean deprecated = false;
    boolean synthetic = false;
    InnerClass[] innerClasses;
    private HashMap attributes;
    private HashMap annotations;
    short majorVersion;
    short minorVersion;
    String typeSignature;
    EnclosingMethod enclosingMethod;
    
    /** size of buffer in buffered input streams */
    private static final int BUFFER_SIZE = 4096;
    
    /**
     * Create a new ClassFile object.
     * @param classData   an InputStream from which the defining bytes of this
     *                    class or interface are read.
     * @throws IOException if InputStream can't be read, or if the class data
     *         is malformed.
     */
    public ClassFile(InputStream classData) throws IOException {
	this(classData, true);
    }
    
    /**
     * Create a new ClassFile object.
     * @param classFileName the path of a class file.
     * @throws IOException if file cannot be opened or read.
     **/
    public ClassFile(String classFileName) throws IOException {
	this(classFileName, true);
    }
    
    /**
     * Create a new ClassFile object.
     * @param file a File instance of a class file.
     * @param includeCode true if this classfile should support operations
     *                    at the bytecode level.  Specify false to conserve
     *                    memory if code access isn't needed.
     * @throws IOException if file cannot be opened or read.
     **/
    public ClassFile(File file, boolean  includeCode) throws IOException {
	InputStream is = null;
        if( file == null || !file.exists() )
            throw new IOException("File name is invalid or file not exists");
        try {
            is = new BufferedInputStream( new FileInputStream( file ), BUFFER_SIZE);
            load(is, includeCode);
        } finally {
            if (is != null)
                is.close();
        }                
    }

    /**
     * Create a new ClassFile object.
     * @param classData  an InputStream from which the defining bytes of this
     * class or interface are read.
     * @param includeCode true if this classfile should support operations
     *                    at the bytecode level.  Specify false to conserve
     *                    memory if code access isn't needed.
     * @throws IOException if InputStream can't be read, or if the class data
     * is malformed.
     */
    public ClassFile(InputStream classData, boolean includeCode) throws IOException {
        if (classData == null)
            throw new IOException("input stream not specified");
        load(classData, includeCode);
    }
    
    /**
     * Create a new ClassFile object.
     * @param classFileName the path of a class file.
     * @param includeCode true if this classfile should support operations
     *                    at the bytecode level.  Specify false to conserve
     *                    memory if code access isn't needed.
     * @throws IOException if file cannot be opened or read.
     **/
    public ClassFile(String classFileName, boolean includeCode) throws IOException {
        InputStream in = null;
        try {
            if (classFileName == null)
                throw new IOException("input stream not specified");
            in = new BufferedInputStream(new FileInputStream(classFileName), BUFFER_SIZE);
            load(in, includeCode);
        } finally {
            if (in != null)
                in.close();
        }
    }
    
    
    /** Returns the ConstantPool object associated with this ClassFile.
     * @return the constant pool object
     */    
    public final ConstantPool getConstantPool() {
        return constantPool;
    }

    private void load(InputStream classData, boolean includeCode) throws IOException {
        try {
            DataInputStream in = new DataInputStream(classData);
            if (in == null)
                throw new IOException("invalid class format");
            constantPool = loadClassHeader(in);
            interfaces = getCPClassList(in, constantPool);
            variables = Variable.loadFields(in, constantPool, this);
            methods = Method.loadMethods(in, constantPool, this, includeCode);
            loadAttributes(in, constantPool);
        } catch (IOException ioe) {
            ioe.printStackTrace();
	    String msg = "invalid class format";
	    if (sourceFileName != null)
		msg += ": " + sourceFileName;
            throw new IOException(msg);
        }
    }

    private ConstantPool loadClassHeader(DataInputStream in) throws IOException {
        int magic = in.readInt();
        if (magic != 0xCAFEBABE) {
            throw new IOException("invalid class format");
        }
            
        minorVersion = in.readShort();
        majorVersion = in.readShort();
        int count = in.readUnsignedShort();
        ConstantPool pool = new ConstantPool(count, in);
        classAccess = in.readUnsignedShort();
        classInfo = pool.getClass(in.readUnsignedShort());
        if (classInfo == null)
            throw new IOException("invalid class format");
        int index = in.readUnsignedShort();
        if (index != 0) // true for java.lang.Object
            superClassInfo = pool.getClass(index);
        return pool;
    }

    static CPClassInfo[] getCPClassList(DataInputStream in, ConstantPool pool)
      throws IOException {
        int count = in.readUnsignedShort();
        CPClassInfo[] classes = new CPClassInfo[count];
        for (int i = 0; i < count; i++) {
            classes[i] = pool.getClass(in.readUnsignedShort());
        }
        return classes;
    }
    
    //FIXME: rewrite to store all attributes as byte arrays, delay conversion
    private void loadAttributes(DataInputStream in, ConstantPool pool) 
      throws IOException {        
        int count = in.readUnsignedShort();
        attributes = new HashMap(count + 1, (float)1.0);
	annotations = new HashMap(2);
	final byte[] noBytes = new byte[0];
        for (int i = 0; i < count; i++) {
            try {
		CPUTF8Info entry = 
		    (CPUTF8Info)pool.get(in.readUnsignedShort());

		int len = in.readInt();
		String name = entry.getName();
		if (name.equals("Deprecated")){
		    attributes.put(name, noBytes);
		    deprecated = true;
		}
		else if (name.equals("Synthetic")){
		    attributes.put(name, noBytes);
		    synthetic = true;
		}
		else if (name.equals("SourceFile")) { //NOI18N
		    entry = (CPUTF8Info)pool.get(in.readUnsignedShort());
		    sourceFileName = entry.getName();
		    attributes.put(name, sourceFileName);
		} else if (name.equals("InnerClasses")){
		    innerClasses = InnerClass.loadInnerClasses(in, pool);
		    attributes.put(name, innerClasses);
		}
		else if (name.equals("Signature")) { //NOI18N
                    entry = (CPUTF8Info)pool.get(in.readUnsignedShort());
		    typeSignature = entry.getName();
		    attributes.put(name, typeSignature);
		}
		else if (name.equals("EnclosingMethod")) { //NOI18N
		    int classIndex = in.readUnsignedShort();
		    int natIndex = in.readUnsignedShort();
		    CPEntry classInfo = pool.get(classIndex);
		    if (classInfo.getTag() == ConstantPool.CONSTANT_Class) {
			enclosingMethod = 
			    new EnclosingMethod(pool, 
						(CPClassInfo)classInfo, 
						natIndex);
			attributes.put(name, enclosingMethod);
		    } else
			; // Dasho bug in 1.5 beta1's jce.jar
		}
		else if (name.equals("RuntimeVisibleAnnotations")) //NOI18N
		    Annotation.load(in, pool, true, annotations);
		else if (name.equals("RuntimeInvisibleAnnotations")) //NOI18N
		    Annotation.load(in, pool, false, annotations);
		else {
		    skip(in, len);
		    attributes.put(name, noBytes);
		}
            } catch (ClassCastException e) {
                throw new IOException("invalid constant pool entry");
            }
        }
        if (innerClasses == null)
            innerClasses = new InnerClass[0];
    }

    /*
     * version of InputStream.skip() which will skip the actual
     * number of requested bytes.
     */
    static void skip(InputStream in, int len) throws IOException {
	int n;
	while ((n = (int)in.skip(len)) > 0 && n < len)
	    len -= n;
    }

    /**
     * Returns the access permissions of this class or interface.
     * @return a mask of access flags.
     * @see org.netbeans.modules.classfile.Access
     */
    public final int getAccess() {
        return classAccess;
    }
    
    /** Returns the name of this class.
     * @return the name of this class.
     */
    public final ClassName getName() {
        return classInfo.getClassName();
    }

    /** Returns the name of this class's superclass.  A string is returned
     * instead of a ClassFile object to reduce object creation.
     * @return the name of the superclass of this class.
     */    
    public final ClassName getSuperClass() {
        if (superClassInfo == null)
            return null;
	return superClassInfo.getClassName();
    }
    
    /**
     * @return a collection of Strings describing this class's interfaces.
     */    
    public final Collection getInterfaces() {
        List l = new ArrayList();
        int n = interfaces.length;
        for (int i = 0; i < n; i++)
            l.add(interfaces[i].getClassName());
        return l;
    }
    
    /**
     * Looks up a variable by its name.
     *
     * NOTE: this method only looks up variables defined by this class,
     * and not inherited from its superclass.
     *
     * @param name the name of the variable
     * @return the variable,or null if no such variable in this class.
     */
    public final Variable getVariable(String name) {
        int n = variables.length;
        for (int i = 0; i < n; i++) {
            Variable v = variables[i];
            if (v.getName().equals(name))
                return v;
        }
        return null;
    }
    
    /**
     * @return a Collection of Variable objects representing the fields 
     *         defined by this class.
     */    
    public final Collection getVariables() {
        return Arrays.asList(variables);
    }

    /**
     * @return the number of variables defined by this class.
     */    
    public final int getVariableCount() {
        return variables.length;
    }
    
    /**
     * Looks up a method by its name and type signature, as defined
     * by the Java Virtual Machine Specification, section 4.3.3.
     *
     * NOTE: this method only looks up methods defined by this class,
     * and not methods inherited from its superclass.
     *
     * @param name the name of the method
     * @param signature the method's type signature
     * @return the method, or null if no such method in this class.
     */
    public final Method getMethod(String name, String signature) {
        int n = methods.length;
        for (int i = 0; i < n; i++) {
            Method m = methods[i];
            if (m.getName().equals(name) && m.getDescriptor().equals(signature))
                return m;
        }
        return null;
    }
    
    /**
     * @return a Collection of Method objects representing the methods 
     *         defined by this class.
     */    
    public final Collection getMethods() {
        return Arrays.asList(methods);
    }
    
    /**
     * @return the number of methods defined by this class.
     */    
    public final int getMethodCount() {
        return methods.length;
    }
    
    /**
     * @return the name of the source file the compiler used to create this class.
     */    
    public final String getSourceFileName() {
        return sourceFileName;
    }
    
    public final boolean isDeprecated() {
        return deprecated;
    }

    public final boolean isSynthetic() {
        return synthetic ||
	    (classAccess & Access.SYNTHETIC) == Access.SYNTHETIC;
    }


    /**
     * Returns true if this class is an annotation type.
     */
    public final boolean isAnnotation() {
	return (classAccess & Access.ANNOTATION) == Access.ANNOTATION;
    }
            
    /**
     * Returns true if this class defines an enum type.
     */
    public final boolean isEnum() {
	return (classAccess & Access.ENUM) == Access.ENUM;
    }

    /**
     * Returns a map of the raw attributes for this classfile.  The
     * keys for this map are the names of the attributes (as Strings,
     * not constant pool indexes).  The values are byte arrays that
     * hold the contents of the attribute.  Field attributes are
     * not returned in this map.
     *
     * @see org.netbeans.modules.classfile.Field#getAttributes
     */
    public final Map getAttributes(){
        return attributes;
    }
    
    public final Collection getInnerClasses(){
        return Arrays.asList(innerClasses);
    }

    /**
     * Returns the major version number of this classfile.
     */
    public int getMajorVersion() {
	return majorVersion;
    }

    /**
     * Returns the minor version number of this classfile.
     */
    public int getMinorVersion() {
	return minorVersion;
    }

    /**
     * Returns the generic type information associated with this class.  
     * If this class does not have generic type information, then null 
     * is returned.
     */
    public String getTypeSignature() {
	return typeSignature;
    }

    /**
     * Returns the enclosing method for this class.  A class will have an
     * enclosing class if and only if it is a local class or an anonymous
     * class, and has been compiled with a compiler target level of 1.5 
     * or above.  If no such attribute is present in the classfile, then
     * null is returned.
     */
    public EnclosingMethod getEnclosingMethod() {
	return enclosingMethod;
    }

    /**
     * Returns all runtime annotations defined for this class.  Inherited
     * annotations are not included in this collection.
     */
    public final Collection getAnnotations() {
	return annotations.values();
    }

    /**
     * Returns the annotation for a specified annotation type, or null if
     * no annotation of that type exists for this class.
     */
    public final Annotation getAnnotation(final ClassName annotationClass) {
	return (Annotation)annotations.get(annotationClass);
    }
    
    /**
     * Returns true if an annotation of the specified type is defined for
     * this class.
     */
    public final boolean isAnnotationPresent(final ClassName annotationClass) {
	return annotations.get(annotationClass) != null;
    }
    
    /* Return the collection of all unique class references in this class.
     *
     * @return a Set of ClassNames specifying the referenced classnames.
     */
    public final Set getAllClassNames() {
        Set set = new HashSet();

        // include all class name constants from constant pool
        Collection c = constantPool.getAllConstants(CPClassInfo.class);
        for (Iterator i = c.iterator(); i.hasNext();) {
            CPClassInfo ci = (CPClassInfo)i.next();
            set.add(ci.getClassName());
        }

	// scan variables and methods for other class references
	// (inner classes will caught above)
	for (int i = 0; i < variables.length; i++)
	    addClassNames(set, variables[i].getDescriptor());
	for (int i = 0; i < methods.length; i++)
	    addClassNames(set, methods[i].getDescriptor());

        return Collections.unmodifiableSet(set);
    }

    private void addClassNames(Set set, String type) {
        int i = 0;
        while ((i = type.indexOf('L', i)) != -1) {
            int j = type.indexOf(';', i);
            if (j > i) {
		// get name, minus leading 'L' and trailing ';'
                String classType = type.substring(i + 1, j);
		set.add(ClassName.getClassName(classType));
                i = j + 1;
            } else
		break;
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("ClassFile: "); //NOI18N
        sb.append(Access.toString(classAccess));
        sb.append(' ');
        sb.append(classInfo);
        if (synthetic)
            sb.append(" (synthetic)"); //NOI18N
        if (deprecated)
            sb.append(" (deprecated)"); //NOI18N
        sb.append("\n   source: "); //NOI18N
        sb.append(sourceFileName);
        sb.append("\n   super: "); //NOI18N
        sb.append(superClassInfo);
	if (typeSignature != null) {
	    sb.append("\n   signature: "); //NOI18N
	    sb.append(typeSignature);
	}
	if (enclosingMethod != null) {
	    sb.append("\n   enclosing method: "); //NOI18N
	    sb.append(enclosingMethod);
	}
        sb.append("\n   ");
	if (annotations.size() > 0) {
	    Iterator iter = annotations.values().iterator();
	    sb.append("annotations: ");
	    while (iter.hasNext()) {
                sb.append("\n      ");
		sb.append(iter.next().toString());
	    }
	    sb.append("\n   ");
	}
        if (interfaces.length > 0) {
            sb.append(arrayToString("interfaces", interfaces)); //NOI18N
            sb.append("\n   ");
        }
        if (innerClasses.length > 0) {
            sb.append(arrayToString("innerclasses", innerClasses)); //NOI18N
            sb.append("\n   ");
        }
        if (variables.length > 0) {
            sb.append(arrayToString("variables", variables)); //NOI18N
            sb.append("\n   ");
        }
        if (methods.length > 0)
            sb.append(arrayToString("methods", methods)); //NOI18N
        return sb.toString();
    }

    private String arrayToString(String name, Object[] array) {
        StringBuffer sb = new StringBuffer();
        sb.append(name);
        sb.append(": ");
        int n = array.length;
        if (n > 0) {
            int i = 0;
            do {
                sb.append("\n      ");
                sb.append(array[i++].toString());
            } while (i < n);
        } else
            sb.append("none"); //NOI18N
        return sb.toString();
    }
}
