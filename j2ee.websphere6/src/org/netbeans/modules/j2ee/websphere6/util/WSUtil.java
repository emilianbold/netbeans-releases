/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.j2ee.websphere6.util;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openide.*;

/**
 * Just a collection of static utility methods
 *
 * @author Kirill Sorokin
 */
public class WSUtil {

    private static Class clazz = new WSUtil().getClass();

    /**
     * Reads an entire text file into a string
     *
     * @param file the file's handle
     * @return a string with the file's contents
     */
    public static String readFile(File file) {
       // init the buffer for storing the file's contents
       StringBuffer buffer = new StringBuffer();
       
       try {
           // init the reader
           LineNumberReader reader = new LineNumberReader(new FileReader(file));
           
           // init the temp line
           String temp = "";
           
           // read the file
           while ((temp = reader.readLine()) != null) {
               buffer.append(temp).append("\n");
           }
           
           if (WSDebug.isEnabled()) 
               WSDebug.notify(clazz, "read string: \n" + buffer.toString());
           
           // return the string
           return buffer.toString();
       } catch (IOException e) {
           Logger.getLogger("global").log(Level.WARNING, null, e);
       }
       
       return null;
    }
    
    /**
     * Writes the supplied string to a file overwriting the previous contents
     * 
     * @param file the file to write to
     * @param the new file contents
     */
    public static void writeFile(File file, String contents) {
        try {
            if (WSDebug.isEnabled()) 
                WSDebug.notify(clazz, "write string: \n" + contents);
           
            // create a writer and write the contents
            new FileOutputStream(file).write(contents.getBytes());
        } catch (IOException e) {
            Logger.getLogger("global").log(Level.WARNING, null, e);
        }
    }
}