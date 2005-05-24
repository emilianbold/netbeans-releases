/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * EditorUtils.java
 *
 * Created on September 16, 2003, 4:43 PM
 */

package org.netbeans.modules.j2ee.sun.ide.editors;

/**
 *
 * @author  nityad
 */
public class EditorUtils {
    
    /** Creates a new instance of EditorUtils */
    public EditorUtils() {
    }
    
    static public boolean isValidInt0(String str){
        int val;
        //represents 0 - MAX_INT range
        try
        {
            val = Integer.parseInt(str);
        }
        catch(NumberFormatException e)
        {
            return false;
        }
        if(val < 0)
        {
            return false;
        }
        return true;
    }     
    
    static public boolean isValidLong(String str){
        //represents -ve , 0  +ve range represented by Long
        try
        {
            Long.parseLong(str);
        }
        catch(NumberFormatException e)
        {
            return false;
        }
        return true;
    }
}
