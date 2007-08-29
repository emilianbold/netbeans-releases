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
 */
package org.netbeans.modules.j2ee.weblogic9.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.openide.windows.InputOutput;


/**
 * This class is capable of tailing the specified file or input stream. It
 * checks for changes at the specified intervals and outputs the changes to
 * the given I/O panel in NetBeans
 *
 * @author Kirill Sorokin
 */
public class WLTailer extends Thread {

    /**
     * Amount of time in milliseconds to wait between checks of the input
     * stream
     */
    private static final int DELAY = 500;

    private static final Logger LOGGER = Logger.getLogger(WLTailer.class.getName());

    /**
     * The input stream for which to track changes
     */
    private InputStream inputStream;

    /**
     * The I/O window where to output the changes
     */
    private InputOutput io;

    private volatile boolean exit;

    /**
     * Creates and starts a new instance of WLTailer.
     *
     * @param inputStream the input stream for which to track changes
     * @param uri uri of the server instance
     */
    public WLTailer(InputStream inputStream, String uri) {
        // save the parameters
        this.inputStream = inputStream;
        io = UISupport.getServerIO(uri);
    }

    /**
     * Implementation of the Runnable interface. Here all tailing is
     * performed
     */
    @Override
    public void run() {
        if (io == null) {
            return;
        }

        // clear the old output
        try {
            io.getOut().reset();
        } catch (IOException ioe) {
            LOGGER.log(Level.INFO, null, ioe);
        }
        io.select();

        try {

            // create a reader from the input stream
            InputStreamReader reader = new InputStreamReader(inputStream);

            // read from the input stream and put all the changes to the
            // I/O window
            char[] chars = new char[1024];
            while (!exit) {
                // while there is something in the stream to be read - read that
                while (reader.ready()) {
                    int count = reader.read(chars);
                    io.getOut().println(new String(chars, 0, count));
                }

                // when the stream is empty - sleep for a while
                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.WARNING, null, e);
            return;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, null, e);
        } finally {
            // close the opened stream
            try {
                inputStream.close();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, null, e);
            }
        }
    }

    /**
     * Exits this thread.
     */
    public void exit() {
        exit = true;
    }

}
