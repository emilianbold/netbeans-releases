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

package org.netbeans.lib.ddl;

/**
* Interface of argument type. It should be used for all in(out) values in
* procedures/functions.
*
* @author Slavek Psenicka
*/
public interface Argument {

    /** Returns name */
    public String getName();

    /** Sets name
    * @param aname New name.
    */
    public void setName(String aname);

    /** Describes type of argument: in, out, in/out or return value
    * of procedure. Particular values you can find in DatabaseMetadata;
    */
    public int getType();

    /** Sets type of argument
    * @param aatypename New type.
    */
    public void setType(int atype);

    /** Returns datatype of argument */
    public int getDataType();

    /** Sets datatype of argument
    * @param aatypename New type.
    */
    public void setDataType(int dtype);
}

/*
* <<Log>>
*/
