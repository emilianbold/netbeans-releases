/*
 * Access.java
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

/**
 * A utility class defining access flags and access utility methods.
 * Access flags are as defined by the Java Virtual Machine Specification
 * Second Edition, tables 4.1, 4.4, 4.5 and 4.7.
 *
 * @author Thomas Ball
 */
public class Access {

    public static final int PUBLIC       = 0x0001;
    public static final int PRIVATE      = 0x0002;
    public static final int PROTECTED    = 0x0004;
    public static final int STATIC       = 0x0008;
    public static final int FINAL        = 0x0010;
    public static final int SYNCHRONIZED = 0x0020;
    public static final int VOLATILE     = 0x0040;
    public static final int TRANSIENT    = 0x0080;
    public static final int NATIVE       = 0x0100;
    public static final int INTERFACE    = 0x0200;
    public static final int ABSTRACT     = 0x0400;
    public static final int STRICT       = 0x0800;

    /**
     * Return a text representation for a given set of access flags, such as:
     * <DL>
     *  <DD><CODE>"public static final"</CODE>,</DD>
     *  <DD><CODE>"package private"</CODE>, or</DD>
     *  <DD><CODE>"protected transient"</CODE>.</DD>
     * </DL>
     * @param access the mask of flags denoting access permission.
     * @return a text representation of the access flags.
 */
    public static String toString(int access) {
        StringBuffer sb = new StringBuffer();
        if ((access & PUBLIC) == PUBLIC)
            sb.append("public "); //NOI18N
        if ((access & PRIVATE) == PRIVATE)
            sb.append("private "); //NOI18N
        if ((access & PROTECTED) == PROTECTED)
            sb.append("protected "); //NOI18N
        if ((access & (PUBLIC | PRIVATE | PROTECTED)) == 0)
            sb.append("package private "); //NOI18N
        if ((access & STATIC) == STATIC)
            sb.append("static "); //NOI18N
        if ((access & FINAL) == FINAL)
            sb.append("final "); //NOI18N
        if ((access & SYNCHRONIZED) == SYNCHRONIZED)
            sb.append("synchronized "); //NOI18N
        if ((access & VOLATILE) == VOLATILE)
            sb.append("volatile "); //NOI18N
        if ((access & TRANSIENT) == TRANSIENT)
            sb.append("transient "); //NOI18N
        if ((access & NATIVE) == NATIVE)
            sb.append("native "); //NOI18N
        if ((access & INTERFACE) == INTERFACE)
            sb.append("interface "); //NOI18N
        if ((access & ABSTRACT) == ABSTRACT)
            sb.append("abstract "); //NOI18N
        if ((access & STRICT) == STRICT)
            sb.append("strict "); //NOI18N

        // trim trailing space
        return sb.substring(0, sb.length()-1);
    }

    public static boolean isStatic(int access) {
        return ((access & STATIC) == STATIC);
    }

    public static final boolean isPublic(int access) {
        return ((access & PUBLIC) == PUBLIC);
    }

    public static final boolean isProtected(int access) {
        return ((access & PROTECTED) == PROTECTED);
    }

    public static final boolean isPackagePrivate(int access) {
        return ((access & (PUBLIC | PRIVATE | PROTECTED)) == 0);
    }

    public static final boolean isPrivate(int access) {
        return ((access & PRIVATE) == PRIVATE);
    }

    private Access() {
        // don't allow instantiation
    }
}
