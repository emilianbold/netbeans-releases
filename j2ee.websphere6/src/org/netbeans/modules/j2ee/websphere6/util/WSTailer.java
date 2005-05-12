/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.websphere6.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.openide.ErrorManager;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Kirill Sorokin
 */
public class WSTailer extends Thread {
    
    private static final int delay = 1000;
    
    private File file;
    private InputStream inputStream;
    private InputOutput io;
    
    public WSTailer(File file, String ioPanelName) {
        this.file = file;
        this.io = IOProvider.getDefault().getIO(ioPanelName, true);
    }
    
    public WSTailer(InputStream inputStream, String ioPanelName) {
        this.inputStream = inputStream;
        this.io = IOProvider.getDefault().getIO(ioPanelName, true);
    }
    
    public void run() {
        try {
            InputStreamReader reader;
            if (file != null) {
                inputStream = new FileInputStream(file);
            }
            
            reader = new InputStreamReader(inputStream);

            char[] chars = new char[1024];
            while (true) {
                while (reader.ready()) {
                    io.getOut().println(new String(chars, 0, reader.read(chars)));
                }
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
        } catch (FileNotFoundException e) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
            return;
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
            }
        }
    }
    
}
