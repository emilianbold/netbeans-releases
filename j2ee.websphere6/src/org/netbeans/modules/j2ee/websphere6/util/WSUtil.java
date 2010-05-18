/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
public final class WSUtil {

    private static final Logger LOGGER = Logger.getLogger(WSUtil.class.getName());

    private WSUtil() {
        super();
    }

    /**
     * Reads an entire text file into a string
     *
     * @param file the file's handle
     * @return a string with the file's contents
     */
    public static String readFile(File file) {
       try {
           // init the reader
           LineNumberReader reader = new LineNumberReader(new FileReader(file));

           try {
               StringBuffer buffer = new StringBuffer();
               // init the temp line
               String temp = ""; // NOI18N

               // read the file
               while ((temp = reader.readLine()) != null) {
                   buffer.append(temp).append("\n"); // NOI18N
               }

               if (LOGGER.isLoggable(Level.FINEST)) {
                   LOGGER.log(Level.FINEST, "read string: \n" + buffer.toString()); // NOI18N
               }

               // return the string
               return buffer.toString();
           } finally {
               reader.close();
           }
       } catch (IOException e) {
           LOGGER.log(Level.WARNING, null, e);
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
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "write string: \n" + contents); // NOI18N
        }

        try {
            FileOutputStream fos = new FileOutputStream(file);
            try {
                fos.write(contents.getBytes());
            } finally {
                fos.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, null, e);
        }
    }
}
