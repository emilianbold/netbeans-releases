package org.netbeans.installer.downloader;

import java.io.File;
import java.io.IOException;
import org.netbeans.installer.Installer;

import org.netbeans.installer.downloader.queue.DispatchedQueue;
import org.netbeans.installer.utils.FileUtils;

/**
 * @author Danila_Dugurov
 */

public class DownloadManager {
    
    public static final DownloadManager DM = new DownloadManager();
    
    private final DispatchedQueue queue;
    private final File wd;
    
    private DownloadManager() {
        wd = new File(Installer.DEFAULT_LOCAL_DIRECTORY_PATH, "wd");
        wd.mkdirs();
        queue = new DispatchedQueue(new File(wd, "state.xml"));
        queue.reset();
        try {
            //now reset becouse we can't work withstateful queue
            //clear downloads it's temp. will FileManager not impl.
            FileUtils.deleteFile(new File(Installer.DEFAULT_LOCAL_DIRECTORY_PATH, "downloads"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public PumpingsQueue getQueue() {
        return queue;
    }
    
    public void invoke() {
        queue.invoke();
    }
    
    public void terminate() {
        System.out.println("terminating dispatcher");
        queue.terminate();
    }
    
    public boolean isActive() {
        return queue.isActive();
    }
    
    public File getWd() {
        return  wd;
    }
}
