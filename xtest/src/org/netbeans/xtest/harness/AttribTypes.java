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

/*
 * AttribTypes.java
 *
 * Created on October 23, 2001, 11:35 PM
 */

package org.netbeans.xtest.harness;

/**
 *
 * @author  mk97936
 * @version 
 */
public interface AttribTypes {

    public static final int LOG_AND = 1;
    public static final int LOG_OR  = 2;
    public static final int LOG_NOT = 3;
    public static final int OP_PAR  = 4;
    public static final int CL_PAR  = 5;
    public static final int ATTR    = 6;
    public static final int ERR     = 0;
    public static final int EOF     = 10;
    
}

