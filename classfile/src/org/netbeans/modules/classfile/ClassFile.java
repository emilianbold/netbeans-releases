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
    
    /** size of buffer in buffered input streams */
    private static final int BUFFER_SIZE = 4096;
    
    /**
     * Create a new ClassFile object.
     * @param classData   an InputStream from which the defining bytes of this
     *                    class or interface are read.
     * @param includeCode true if this classfile should support operations
     *                    at the bytecode level.  Specify false to conserve
     *                    memory if code access isn't needed.
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
            throw new IllegalArgumentException("File name is invalid or file not exists");
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
            throw new IllegalArgumentException("input stream not specified");
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
                throw new IllegalArgumentException("input stream not specified");
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
            constantPool = loadClassHeader(in);
            interfaces = getCPClassList(in, constantPool);
            variables = Variable.loadFields(in, constantPool);
            methods = Method.loadMethods(in, constantPool, includeCode);
            loadAttributes(in, constantPool);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new IOException("invalid class format");
        }
    }
    
    private ConstantPool loadClassHeader(DataInputStream in) throws IOException {
        int magic = in.readInt();
        if (magic != 0xCAFEBABE) {
            throw new IllegalArgumentException("invalid class format");
        }
            
        short minor = in.readShort();
        short major = in.readShort();
        int count = in.readShort();
        ConstantPool pool = new ConstantPool(count, in);
        classAccess = in.readUnsignedShort();
        classInfo = pool.getClass(in.readUnsignedShort());
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
    
    private void loadAttributes(DataInputStream in, ConstantPool pool) 
      throws IOException {        
        int count = in.readUnsignedShort();
        attributes = new HashMap(count + 1, (float)1.0);
        for (int i = 0; i < count; i++) {
            CPUTF8Info entry = (CPUTF8Info)pool.get(in.readUnsignedShort());
            int len = in.readInt();
            String name = entry.getName();
            if (name.equals("Deprecated")){
                attributes.put(name, null);
                deprecated = true;
            }
            else if (name.equals("Synthetic")){
                attributes.put(name, null);
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
            else {
                System.out.println("skipped unknown class attribute: " + name);
		skip(in, len);
                attributes.put(name, null);
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
    
    /** Returns the name of this class, including its package (if any).
     * @return the full name of this class.
     */    
    public final String getFullName() {
        return externalizeClassName(classInfo.getName());
    }

    /** Returns the name of this class, without any package prefix.
     * @return the name of this class.
     */
    public final String getName() {
        String clsName = classInfo.getInternalName();
        int idx = clsName.lastIndexOf('/');
        if (idx == -1)
            return clsName;
        return externalizeClassName(clsName.substring(idx + 1));
    }

    /** Returns the name of this class in "internal" format, separating
     * package names with '/' and inner classes with '$'.
     */
    public final String getInternalName() {
        return classInfo.getInternalName();
    }

    /** Returns the package this class is part of, or an empty string
     * if it doesn't have one.
     * @return the package of this class.
     */
    public final String getPackage() {
        String clsName = classInfo.getInternalName();
        int idx = clsName.lastIndexOf('/');
        if (idx == -1)
            return "";
        String pkg = clsName.substring(0, idx);
        return externalizeClassName(pkg);
    }
    
    /** Returns the name of this class's superclass.  A string is returned
     * instead of a ClassFile object to reduce object creation.
     * @return the name of the superclass of this class.
     */    
    public final String getSuperClass() {
        if (superClassInfo == null)
            return null;
        String superClassName = 
            externalizeClassName(superClassInfo.getName());
        // Superclass name may be "" for java.lang.Object, although it should be null.
        return (superClassName.length() > 0) ? superClassName : null;
    }
    
    /**
     * @return a collection of Strings describing this class's interfaces.
     * @throws IOException if the classfile can't be read.
     */    
    public final Collection getInterfaces() throws IOException {
        List l = new ArrayList();
        int n = interfaces.length;
        for (int i = 0; i < n; i++)
            l.add(externalizeClassName(interfaces[i].getName()));
        return l;
    }
    
    /**
     * @return a Collection of Variable objects representing the fields 
     *         defined by this class.
     */    
    public final Collection getVariables() {
        return Arrays.asList(variables);
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
        return synthetic;
    }
            
    public final Map getAttributes(){
        return attributes;
    }
    
    public final Collection getInnerClasses(){
        return Arrays.asList(innerClasses);
    }
    
    static String externalizeClassName(String s) {
        return s.replace('/', '.').replace('$', '.');
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("ClassFile: "); //NOI18N
        sb.append(Access.toString(classAccess));
        sb.append(' ');
        sb.append(classInfo.getName());
        if (synthetic)
            sb.append(" (synthetic)"); //NOI18N
        if (deprecated)
            sb.append(" (deprecated)"); //NOI18N
        sb.append("\n   source: "); //NOI18N
        sb.append(sourceFileName);
        sb.append("\n   super: "); //NOI18N
        sb.append(superClassInfo.getName());
        sb.append("\n   ");
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
