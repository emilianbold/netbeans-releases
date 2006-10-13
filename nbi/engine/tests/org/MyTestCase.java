package org;

import java.io.File;
import java.io.FileInputStream;
import junit.framework.TestCase;
import java.util.logging.LogManager;
import org.netbeans.installer.downloader.DownloaderConsts;

/**
 *
 * @author Danila Dugurov
 */
public class MyTestCase extends TestCase {
   
   protected void setUp() throws Exception {
      super.setUp();
      DownloaderConsts.setWorkingDirectory(new File("testWd"));
      DownloaderConsts.setOutputDirectory(new File("testOutput"));
      final File logFile = new File(DownloaderConsts.getWorkingDirectory(), "logging.properties");
      LogManager.getLogManager().readConfiguration(new FileInputStream(logFile));
   }
}
