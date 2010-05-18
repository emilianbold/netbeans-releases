/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.iep.model.util;

import java.net.URL;


/**
 * Class utilities
 *
 * @author       Bing Lu
 * @created      February 3, 2003
 */
public class ClassUtil {
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(ClassUtil.class.getName());

    /**
     * Returns the dimensions of the array
     *
     * @param c  the array
     * @return   the dimensions of the array
     */
    public static int deriveArrayDimensions(Class c) {
        int ret = 0;
        while (c.isArray()) {
            ret++;
            c = c.getComponentType();
        }
        return ret;
    }

    /**
     * Returns the class for the class name. Handles strings returned by java.lang.Class.getName()
     * i.e.,  B            byte
     * C            char
     * D            double
     * F            float
     * I            int
     * J            long
     * Lclassname;  class or interface
     * S            short
     * Z            boolean
     * V            void
     * etc
     *
     * @see                            java.lang.Class#getName
     * @param className                the class name
     * @return                         the class for the class name
     * @throws ClassNotFoundException  if the class is not found
     */
    public static Class forName(String className)
             throws ClassNotFoundException {
        Class c = null;
        if (className.equals("C") 
                 || className.equals("char")) {
            c = Character.TYPE;
        } else if (className.equals("Z")
                 || className.equals("boolean")) {
            c = Boolean.TYPE;
        } else if (className.equals("B")
                 || className.equals("byte")) {
            c = Byte.TYPE;
        } else if (className.equals("S") 
                 || className.equals("short")) {
            c = Short.TYPE;
        } else if (className.equals("I") 
                 || className.equals("int")) {
            c = Integer.TYPE;
        } else if (className.equals("J") 
                 || className.equals("long")) {
            c = Long.TYPE;
        } else if (className.equals("F") 
                 || className.equals("float")) {
            c = Float.TYPE;
        } else if (className.equals("D") 
                 || className.equals("double")) {
            c = Double.TYPE;
        } else if (className.equals("V") 
                 || className.equals("void")) {
            c = Void.TYPE;
        } else {
            c = Class.forName(className);
        }
        return c;
    }

    /**
     * Returns the object if it can be cast to the expected class
     *
     * @param o                       the object
     * @param expectedClass           the expected class
     * @return                        the object if it can be cast to the expected class
     * @exception ClassCastException  if object cannot be cast to expected class
     */
    public static Object narrow(Object o, Class expectedClass)
             throws ClassCastException {
        if (o != null) {
            if (!expectedClass.isAssignableFrom(o.getClass())) {
                Class actualClass = o.getClass();
                throw new ClassCastException(
                        "Expecting " + expectedClass + "; received " + actualClass + " " + o);
            }
        }
        return o;
    }    
    
    /**
     * Returns a description of the location of the resource
     *
     * @param s  the slash separated resource name
     * @return   a description of the location of the resource
     */
    public static String whichResource(String s) {
        return whichResource(Object.class, s);
    }

    /**
     * Returns a description of the location of the resource
     *
     * @param c  the class used to load the resource
     * @param s  the slash separated resource name
     * @return   a description of the location of the resource
     */
    public static String whichResource(Class c, String s) {
        URL url = c.getResource(s);
        if (url == null) {
            return "Resource " + s + " not found";
        } else {
            return "Resource " + s +
                    " found in " + url.getFile();
        }
    }    
    
    /**
     * Returns a description of the location of the object's class
     *
     * @param o  the object
     * @return   a description of the location of the object's class
     */
    public static String whichClass(Object o) {
        return whichClass(o.getClass());
    }

    /**
     * Returns a description of the location of the class
     *
     * @param s  the dot separated class name
     * @return   a description of the location of the class
     */
    public static String whichClass(String s) {
        try {
            return whichClass(Class.forName(s));
        } catch (Exception e) {
            //e.printStackTrace();

            return "Class " + s + " not found";
        }
    }

    /**
     * Returns a description of the location of the class
     *
     * @param c  the class
     * @return   a description of the location of the class
     */
    public static String whichClass(Class c) {
        URL url = c.getResource(deriveResourceName(c));
        if (url == null) {
            return "Class " + c + " not found";
        } else {
            return "Class " + c +
                    " found in " + url.getFile();
        }
    }

    /**
     * Returns the slash separated resource name from the class
     *
     * @see      java.lang.Class#getResource
     * @param s  the class
     * @return   the slash separated resource name from the class
     */
    private static String deriveResourceName(Class c) {
        return "/" + c.getName().replace('.', '/') + ".class";
    }

    /**
     * The command line
     *
     * @param args  command line arguments
     */
    public static void main(String args[]) {
        if (args[0].equals("-r")) {
            mLog.info(ClassUtil.whichResource(args[1]));
        }
        if (args[0].equals("-c")) {
            mLog.info(ClassUtil.whichClass(args[1]));
        }
        try {
            Object o = new Long(0);
            String s = (String) ClassUtil.narrow(o, String.class);
            mLog.info(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            ClassUtil.forName("C"); 
            ClassUtil.forName("char"); 
            ClassUtil.forName(Object.class.getName()); 

            ClassUtil.forName("[Ljava.lang.Object;"); 
            Class.forName("C"); 

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
/*
    public static List getClassNames(List classes) {
        List list = new ArrayList();
        for (int i = 0; i < classes.size(); i++) {
            list.add(ClassUtil.getClassName((Class) classes.get(i)));
        }
        return list;
    }        
    
    public static String getJavaCodeClass(Class c) {
        String code = null;
        if (c.isArray()) {
            Class type = c.getComponentType();
            StringBuffer buf = new StringBuffer();

            for (int i = 0; i < deriveArrayDimensions(c); i++) {
                buf.append("[]");
            }

            if (type == Character.TYPE) {
                code = "char";
            } else if (type == Boolean.TYPE) {
                code = "boolean";
            } else if (type == Byte.TYPE) {
                code = "byte";
            } else if (type == Short.TYPE) {
                code = "short";
            } else if (type == Integer.TYPE) {
                code = "int";
            } else if (type == Long.TYPE) {
                code = "long";
            } else if (type == Float.TYPE) {
                code = "float";
            } else if (type == Double.TYPE) {
                code = "double";
            } else if (type == Void.TYPE) {
                code = "void";
            } else {
                code = c.getComponentType().getName();
            }
            code += (buf + ".class");
        } else if (c.isPrimitive()) {
            if (c == Character.TYPE) {
                code = "Character.TYPE";
            } else if (c == Boolean.TYPE) {
                code = "Boolean.TYPE";
            } else if (c == Byte.TYPE) {
                code = "Byte.TYPE";
            } else if (c == Short.TYPE) {
                code = "Short.TYPE";
            } else if (c == Integer.TYPE) {
                code = "Integer.TYPE";
            } else if (c == Long.TYPE) {
                code = "Long.TYPE";
            } else if (c == Float.TYPE) {
                code = "Float.TYPE";
            } else if (c == Double.TYPE) {
                code = "Double.TYPE";
            } else if (c == Void.TYPE) {
                code = "Void.TYPE";
            } else {
                throw new RuntimeException("unsupported primitive " + c);
            }
        } else {
            code = c.getName() + ".class";
        }
        return code;
    }

    public static String getJavaCodeRef(Class c) {
        String code = null;
        if (c.isArray()) {
            Class type = c.getComponentType();
            StringBuffer buf = new StringBuffer();

            for (int i = 0; i < deriveArrayDimensions(c); i++) {
                buf.append("[]");
            }

            if (type == Character.TYPE) {
                code = "char";
            } else if (type == Boolean.TYPE) {
                code = "boolean";
            } else if (type == Byte.TYPE) {
                code = "byte";
            } else if (type == Short.TYPE) {
                code = "short";
            } else if (type == Integer.TYPE) {
                code = "int";
            } else if (type == Long.TYPE) {
                code = "long";
            } else if (type == Float.TYPE) {
                code = "float";
            } else if (type == Double.TYPE) {
                code = "double";
            } else if (type == Void.TYPE) {
                code = "void";
            } else {
                code = c.getComponentType().getName();
            }
            code += (buf);
        } else if (c.isPrimitive()) {
            if (c == Character.TYPE) {
                code = "char";
            } else if (c == Boolean.TYPE) {
                code = "boolean";
            } else if (c == Byte.TYPE) {
                code = "byte";
            } else if (c == Short.TYPE) {
                code = "short";
            } else if (c == Integer.TYPE) {
                code = "int";
            } else if (c == Long.TYPE) {
                code = "long";
            } else if (c == Float.TYPE) {
                code = "float";
            } else if (c == Double.TYPE) {
                code = "double";
            } else if (c == Void.TYPE) {
                code = "void";
            } else {
                throw new RuntimeException("unsupported primitive " + c);
            }
        } else {
            code = c.getName();
        }
        return code;
    }
*/
