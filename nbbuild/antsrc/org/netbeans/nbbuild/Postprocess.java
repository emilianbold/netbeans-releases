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

/** Changes content of a file. Usually used to change already compiled 
* bytecode to contains for example different method name. This is a way
* how one can achieve that two methods with same name and arguments
* differ in return type.
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
    /** minimum number of occurences of the string */
    private int min = 0;
    /** maximum number of occurences of the string */
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
    
    /** Sets the minimum number of string occurences 
     */
    public void setMin (int m) {
        this.min = m;
    }
    
    /** Sets the maximum number of string occurences
     */
    public void setMax (int m) {
        this.max = m;
    }
    
    /** Executes the task.
     */
    public void execute () throws BuildException {
        if (file == null) {
            throw new BuildException ("A file has to be specified"); // NOI18N
        }
        
        if (
            oldString == null || newString == null || 
            oldString.length() != newString.length()
        ) {
            throw new BuildException ("new and old strings have to be specified and they has to have the same length"); // NOI18N
        }
        
        try {
            byte[] b = new byte[(int)file.length()];
            FileInputStream is = new FileInputStream (file);
            is.read (b);
            is.close ();
            
            int cnt = replaceString (b);
            
            if (cnt < min || cnt > max) {
                throw new BuildException ("String " + oldString + " found " + cnt + " times, that is out of min/max range"); // NOI18N
            }
            
            FileOutputStream os = new FileOutputStream (file);
            os.write (b);
            os.close ();
            
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

/*
* Log
* $
*/
