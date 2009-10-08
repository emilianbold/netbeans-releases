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
package org.netbeans.modules.bpel.project.anttasks.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class Util {

  private Util() {}

  public static void copyFile(File source, File destination) throws IOException {
      if (!source.exists() || !source.isFile()) {
          throw new IOException("Source is not valid for copying.");
      }
      
      if (!destination.exists()) {
          destination.getParentFile().mkdirs();
          destination.createNewFile();
      }
      
      final File realDest = destination.isDirectory() ? 
          new File(destination, source.getName()) : 
          destination;
      
      FileInputStream input = null;
      FileOutputStream output = null;
      
      try {
          input = new FileInputStream(source);
          output = new FileOutputStream(realDest);
          
          byte[] buffer = new byte[4096];
          while (input.available() > 0) {
              output.write(buffer, 0, input.read(buffer));
          }
      } finally {
          if (input != null) {
              input.close();
          }
          
          if (output != null) {
              output.close();
          }
      }
  }
  
  public static String getRelativePath(File home, File f){
      return matchPathLists(getPathList(home), getPathList(f));
  }

  private static List getPathList(File f) {
      List l = new ArrayList();
      File r;
      try {
          r = f.getCanonicalFile();
          while(r != null) {
              l.add(r.getName());
              r = r.getParentFile();
          }
      }
      catch (IOException e) {
          e.printStackTrace();
          l = null;
      }
      return l;
  }
  
  private static String matchPathLists(List r, List f) {
      int i;
      int j;
      String s;
      // start at the beginning of the lists
      // iterate while both lists are equal
      s = "";
      i = r.size()-1;
      j = f.size()-1;

      // first eliminate common root
      while((i >= 0)&&(j >= 0)&&(r.get(i).equals(f.get(j)))) {
          i--;
          j--;
      }

      // for each remaining level in the home path, add a ..
      for(;i>=0;i--) {
          s += ".." + File.separator;
      }

      // for each level in the file path, add the path
      for(;j>=1;j--) {
          s += f.get(j) + File.separator;
      }

      // file name
      s += f.get(j);
      return s;
  }
  
  public static class ProjectFileFilter implements FileFilter {

      public boolean accept(File pathname) {
          boolean result = false;

          String fileName = pathname.getName();
          String fileExtension = null;
          int dotIndex = fileName.lastIndexOf('.');

          if (dotIndex != -1) {
              fileExtension = fileName.substring(dotIndex + 1);
          }
          if (fileExtension != null && (fileExtension.equalsIgnoreCase(WSDL_FILE_EXTENSION) || fileExtension.equalsIgnoreCase(XSD_FILE_EXTENSION))) {
              result = true;
          }
          return result;
      }
  }
  
  public static class BpelFileFilter implements FileFilter {

      public boolean accept(File pathname) {
          if (pathname.isDirectory()) {
            return true;
          }
          String fileName = pathname.getName();
          int dotIndex = fileName.lastIndexOf('.');
          String fileExtension = null;

          if (dotIndex != -1) {
              fileExtension = fileName.substring(dotIndex + 1);
          }
          if (fileExtension == null) {
              return false;
          }
          if (fileExtension.equalsIgnoreCase(BPEL_FILE_EXTENSION)) {
            return true;
          }
          return false;
      }
  }
  
  public static class WsdlFileFilter implements FileFilter {

      public boolean accept(File pathname) {
          boolean result = false;
          String fileName = pathname.getName();
          String fileExtension = null;
          int dotIndex = fileName.lastIndexOf('.');

          if (dotIndex != -1) {
              fileExtension = fileName.substring(dotIndex + 1);
          }
          if (fileExtension != null && (fileExtension.equalsIgnoreCase(WSDL_FILE_EXTENSION))) {
              result = true;
          }
          return result;
      }
  }
  
  private static final String WSDL_FILE_EXTENSION = "wsdl"; // NOI18N
  private static final String XSD_FILE_EXTENSION = "xsd"; // NOI18N
  private static final String BPEL_FILE_EXTENSION = "bpel"; // NOI18N
}
