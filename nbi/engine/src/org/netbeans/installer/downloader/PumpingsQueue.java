package org.netbeans.installer.downloader;

import java.io.File;
import java.net.URL;

/**
 * @author Danila_Dugurov
 */

public interface PumpingsQueue {

    void addListener(PumpingsQueueListener listener);
    
    void reset();

    Pumping getById(String id);

    Pumping[] toArray();

    Pumping add(URL url);//output in defualt folder
    
    Pumping add(URL url, File folder);

    Pumping delete(String id);
}
