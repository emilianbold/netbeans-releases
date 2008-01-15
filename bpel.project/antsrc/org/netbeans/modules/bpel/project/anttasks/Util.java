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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.bpel.project.anttasks;

import java.io.File;
import java.io.FileFilter;

public class Util {

    public static final String WSDL_FILE_EXTENSION = "wsdl";
    public static final String XSD_FILE_EXTENSION = "xsd";
    public static final String BPEL_FILE_EXTENSION = "bpel";
    public static final String FOUND_VALIDATION_ERRORS = "Found validation error(s).";
    
    public static String getError(File file, int column, int line, String description, String type) {
      StringBuffer buffer = new StringBuffer();

      if (file != null) {
        buffer.append(file.getPath());

        if (line != -1) {
          buffer.append(": ");
          buffer.append(line);
        }
        if (column != -1) {
          buffer.append(", ");
          buffer.append(column);
        }
      }
      buffer.append("\n" + type + ": " + description);

      return buffer.toString();
    }

    static class ProjectFileFilter implements FileFilter {
        
        public boolean accept(File pathname) {
            boolean result = false;
            
            String fileName = pathname.getName();
            String fileExtension = null;
            int dotIndex = fileName.lastIndexOf('.');

            if(dotIndex != -1) {
                fileExtension = fileName.substring(dotIndex +1);
            }
            if (fileExtension != null && (fileExtension.equalsIgnoreCase(WSDL_FILE_EXTENSION) || fileExtension.equalsIgnoreCase(XSD_FILE_EXTENSION))) {
                result = true;
            }
            return result;
        }
     }
     
     static class BpelFileFilter implements FileFilter {
        
        public boolean accept(File pathname) {
            boolean result = false;
            if(pathname.isDirectory()) {
                return true;
            }
            
            String fileName = pathname.getName();
            String fileExtension = null;
            int dotIndex = fileName.lastIndexOf('.');
            if(dotIndex != -1) {
                fileExtension = fileName.substring(dotIndex +1);
            }
            
            if(fileExtension != null 
               && (fileExtension.equalsIgnoreCase(BPEL_FILE_EXTENSION))) {
                result = true;
            }
            
            return result;
        }
     }
     
     static class WsdlFileFilter implements FileFilter {
        
        public boolean accept(File pathname) {
            boolean result = false;
            
            String fileName = pathname.getName();
            String fileExtension = null;
            int dotIndex = fileName.lastIndexOf('.');
            if(dotIndex != -1) {
                fileExtension = fileName.substring(dotIndex +1);
            }
            
            if(fileExtension != null 
               && (fileExtension.equalsIgnoreCase(WSDL_FILE_EXTENSION))) {
                result = true;
            }
            
            return result;
        }
     }
}
