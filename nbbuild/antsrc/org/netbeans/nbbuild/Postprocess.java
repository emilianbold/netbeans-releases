/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
