package org.netbeans.installer.downloader.impl;
import java.io.File;

/**
 *
 * @author Danila_Dugurov
 */

public class PumpingUtil {
  
  // however may be synchronization by dir more local but now I'm not sure that here
  //object that represent dir will be the same when anoth thread need the same dir
  
   public static synchronized File getFileNameFromURL(File dir, String urlPath) {
      String fileName;
      if (urlPath.endsWith("/")) fileName = "index.html";
      else if (urlPath.lastIndexOf('/') == -1) fileName = urlPath;
      else fileName = urlPath.substring(urlPath.lastIndexOf('/'));
      File file = new File(dir, fileName);
      int index = 2;
      int dotPosition = fileName.lastIndexOf('.');
      while (file.exists()) {
         final String insert = "(" + index + ")";
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
