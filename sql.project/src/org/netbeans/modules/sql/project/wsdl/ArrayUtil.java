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
package org.netbeans.modules.sql.project.wsdl;


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
