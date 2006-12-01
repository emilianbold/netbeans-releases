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
* @deprecated No longer used.
*/
@Deprecated
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
            int len = (int)file.length();
            byte[] b = new byte[len];
            FileInputStream is = new FileInputStream (file);
            try {
                if (is.read(b) != len) throw new BuildException("Failed to read whole file", getLocation());
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
        
        try {
            
            // This encoding is unadorned 8-bit.
            String arr = new String (b, "ISO8859_1");
            int cnt = 0;
            int pos = -1;
            
            byte[] newbytes = newString.getBytes("ISO8859_1");
            if (newbytes.length != oldString.getBytes("ISO8859_1").length) {
                throw new BuildException("Strings to replace must be equal in length", getLocation());
            }
            while ((pos = arr.indexOf(oldString, pos + 1)) != -1) {
                System.arraycopy(newbytes, 0, b, pos, newbytes.length);
                cnt++;
            }
            
            return cnt;
            
        } catch (UnsupportedEncodingException e) {
            throw new BuildException("Error replacing text", e, getLocation());
        }
    }
        
}
