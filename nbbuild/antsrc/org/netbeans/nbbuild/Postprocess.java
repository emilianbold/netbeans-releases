/*
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
 */

package org.netbeans.nbbuild;

import java.io.*;
import java.util.*;

import org.apache.tools.ant.*;

/** Changes content of a binary file. Usually used to change already compiled 
* bytecode to contain, for example, a different method name. This is one way
* to cause two methods with the same name and arguments
* to differ in return type.
* <p>Differs little from the standard <code>&lt;replace&gt;</code> task,
* though a little more customized for binary files.
*
* @author Jaroslav Tulach
*/
public class Postprocess extends Task {
    /** file to post process */
    private File file;
    /** string to replace */
    private String oldString;
    /** string to replace with */
    private String newString;
    /** minimum number of occurrences of the string */
    private int min = 0;
    /** maximum number of occurrences of the string */
    private int max = 1;
    
    /** Set the file to work on.
     * @param f the file
     */
    public void setFile (File f) {
        this.file = f;
    }
    
    /** Set the string to search for 
     */
    public void setOld (String s) {
        this.oldString = s;
    }
    
    /** Set new string.
     */
    public void setNew (String s) {
        this.newString = s;
    }
    
    /** Sets the minimum number of string occurrences 
     */
    public void setMin (int m) {
        this.min = m;
    }
    
    /** Sets the maximum number of string occurrences
     */
    public void setMax (int m) {
        this.max = m;
    }
    
    public void execute () throws BuildException {
        if (file == null) {
            throw new BuildException ("A file must be specified"); // NOI18N
        }
        
        if (
            oldString == null || newString == null || 
            oldString.length() != newString.length()
        ) {
            throw new BuildException ("New and old strings must be specified and they must have the same length"); // NOI18N
        }
        
        try {
            byte[] b = new byte[(int)file.length()];
            FileInputStream is = new FileInputStream (file);
            try {
                is.read (b);
            } finally {
                is.close ();
            }
            
            int cnt = replaceString (b);
            
            if (cnt < min || cnt > max) {
                throw new BuildException ("String " + oldString + " found " + cnt + " times, that is out of min/max range"); // NOI18N
            }
            
            if (cnt > 0) {
                log ("Replaced `" + oldString + "' by `" + newString + "' " + cnt + " times in " + file);
                FileOutputStream os = new FileOutputStream (file);
                try {
                    os.write (b);
                } finally {
                    os.close ();
                }
            }
            
        } catch (IOException ex) {
            throw new BuildException (ex);
        }
    }
    
    
    /** Scans the array and replaces the occurences of oldString by newString
     * @param b the array
     * @return the number of replaces
     */
    private int replaceString (byte[] b) {
        String arr = new String (b);

        for (int cnt = 0; /*notest*/; cnt++) {
            int i = arr.indexOf (oldString);
            
            if (i == -1) {
                return cnt;
            }
            
            System.arraycopy(newString.getBytes(), 0, b, i, oldString.length());
            
            // update also the array
            arr = new String (b);
        }
    }
        
}
