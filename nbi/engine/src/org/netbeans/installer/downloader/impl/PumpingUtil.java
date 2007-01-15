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
 * 
 * $Id$
 */
package org.netbeans.installer.downloader.impl;
import java.io.File;

/**
 *
 * @author Danila_Dugurov
 */

public class PumpingUtil {
  
  
  /////////////////////////////////////////////////////////////////////////////////
  // Static
  // however may be synchronization by dir more local but now I'm not sure that here
  //object that represent dir will be the same when anoth thread need the same dir
  
   public static synchronized File getFileNameFromURL(File dir, String urlPath) {
      String fileName;
      if (urlPath.endsWith("/")) fileName = "index.html";
      else if (urlPath.lastIndexOf('/') == -1) fileName = urlPath;
      else fileName = urlPath.substring(urlPath.lastIndexOf('/'));
     // fileName = fileName.split("[#?]")[0];
      File file = new File(dir, fileName);
      int index = 2;
      int dotPosition = fileName.lastIndexOf('.');
      while (file.exists()) {
         final String insert = "." + index;
         String newName;
         if (dotPosition == -1) newName = fileName + insert;
         else {
            final String preffix = fileName.substring(0, dotPosition);
            final String suffix = fileName.substring(dotPosition);
            newName = preffix + insert + suffix;
         }
         file = new File(dir, newName);
         index++;
      }
      return file;
   }
}
