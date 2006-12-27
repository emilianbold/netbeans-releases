/*
 * Created on 27 Декабрь 2006 г., 11:39
 */

package org.netbeans.installer.downloader;

import java.io.File;
import org.netbeans.installer.Installer;

/**
 *
 * @author Danila_Dugurov
 */
public class DownloadConfig {
  
  public static final int DISPATCHER_QUANTUM;
  public static final int DISPATCHER_POOL;
  
  public static final File DEFAULT_OUTPUT_DIR;
  
  public static final int ATTEMPT_COUNT;
  public static final int REATTEMPT_DELAY;
  
  static {
    DISPATCHER_QUANTUM = 100;
    DISPATCHER_POOL = 10;
    
    DEFAULT_OUTPUT_DIR = new File(Installer.DEFAULT_LOCAL_DIRECTORY_PATH, "downloads");
    ATTEMPT_COUNT = 10;
    REATTEMPT_DELAY = 5 * 1000;
  }
}
