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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
