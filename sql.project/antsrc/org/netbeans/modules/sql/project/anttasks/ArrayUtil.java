/* *************************************************************************
 *
 *          Copyright (c) 2002, SeeBeyond Technology Corporation,
 *          All Rights Reserved
 *
 *          This program, and all the routines referenced herein,
 *          are the proprietary properties and trade secrets of
 *          SEEBEYOND TECHNOLOGY CORPORATION.
 *
 *          Except as provided for by license agreement, this
 *          program shall not be duplicated, used, or disclosed
 *          without  written consent signed by an officer of
 *          SEEBEYOND TECHNOLOGY CORPORATION.
 *
 ***************************************************************************/

/*
 * ArrayUtil.java 1.0
 *
 * Copyright 2004-2006 Sun Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.sql.project.anttasks;


/**
 * ArrayUtil.java
 *
 * Created on July 25, 2006, 9:45 AM
 *
 * @author Bing Lu
 */
public class ArrayUtil {
    public static Object[] duplicate(Object[] a) {
        if (a == null) {
            return a;
        }
        Object[] c = new Object[a.length];
        System.arraycopy(a, 0, c, 0, a.length);
        return c;
    }
    
    public static String[] duplicate(String[] a) {
        if (a == null) {
            return a;
        }
        String[] c = new String[a.length];
        System.arraycopy(a, 0, c, 0, a.length);
        return c;
    }
}
