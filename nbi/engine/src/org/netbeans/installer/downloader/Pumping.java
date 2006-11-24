package org.netbeans.installer.downloader;

import java.io.File;
import java.net.URL;
import org.netbeans.installer.utils.helper.Pair;

/**
 *
 * @author Danila_Dugurov
 */

public interface Pumping {
   
   String getId();
   
   URL declaredURL();
   URL realURL();
   File outputFile();
   File folder();
   
   long length();
   
   PumpingMode mode();
   
   State state();
   
   Section[] getSections();
   
   interface Section {
      Pair<Long, Long> getRange();
      long offset();
   }
   
   enum State {
      NOT_PROCESSED, CONNECTING, PUMPING, WAITING, INTERRUPTED, FAILED, FINISHED, DELETED
   }
}
