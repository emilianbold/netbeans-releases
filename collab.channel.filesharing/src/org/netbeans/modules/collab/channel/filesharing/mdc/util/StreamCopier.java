/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.channel.filesharing.mdc.util;

import java.io.*;

import java.net.*;

import java.util.*;


/**
 * util for stream copy
 *
 * @author  Todd Fast <todd.fast@sun.com>
 * @version 1.0
 */
public class StreamCopier extends Object {
    ////////////////////////////////////////////////////////////////////////////////
    // Member variables
    ////////////////////////////////////////////////////////////////////////////////
    private static int BUFFER_SIZE = 8192;
    private InputStream inputStream;
    private OutputStream[] outputStreams;
    private boolean continueFlag = true;
    private int numReads = 0;
    private int bytesCopied = 0;
    private long elapsedTime = 0;

    /**
     *
     * @param inputStream
     * @param outputStreams
     */
    public StreamCopier(InputStream inputStream, OutputStream[] outputStreams) {
        super();
        this.inputStream = inputStream;
        this.outputStreams = outputStreams;
    }

    /**
     *
     * @param inputStream
     * @param outputStream
     */
    public StreamCopier(InputStream inputStream, OutputStream outputStream) {
        super();
        this.inputStream = inputStream;
        this.outputStreams = new OutputStream[] { outputStream };
    }

    /**
     *
     * @throws IOException
     * @return status
     */
    public int copy() throws IOException {
        return copy(-1);
    }

    /**
     *
     * @param length
     * @throws IOException
     * @return status
     */
    public int copy(int length) throws IOException {
        boolean done = false;
        long baseTime = new Date().getTime();
        byte[] buffer = new byte[BUFFER_SIZE];

        while (!done && continueFlag && Thread.currentThread().isAlive()) {
            try {
                int count = inputStream.read(buffer);

                if (count > 0) {
                    numReads++;

                    // Make sure we only copy as many bytes as we've been told
                    if ((length != -1) && ((bytesCopied + count) > length)) {
                        count = length - bytesCopied;
                    }

                    // Publish the stream contents to each of the output streams
                    for (int i = 0; i < outputStreams.length; i++) {
                        outputStreams[i].write(buffer, 0, count);
                        outputStreams[i].flush();
                    }

                    // Check if we've finished copying
                    bytesCopied += count;

                    if ((length != -1) && (bytesCopied >= length)) {
                        done = true;
                    }
                } else {
                    done = true;
                }
            } catch (InterruptedIOException e) {
                // Here, we want to ignore the interrupted exception
                // in case we're trying to do something like reset an 
                // underlying socket
            }
        }

        elapsedTime = new Date().getTime() - baseTime;

        return bytesCopied;
    }

    /**
     *
     *
     */
    public void stop() {
        continueFlag = false;
    }

    /**
     *
     * @return number of bytes copied
     */
    public int getNumberBytesCopied() {
        return bytesCopied;
    }

    /**
     *
     * @return elapsed time
     */
    public long getElapsedTime() {
        return elapsedTime;
    }

    /**
     *
     * @return average bytes per second
     */
    public float getAverageBytesPerSecond() {
        return bytesCopied / elapsedTime;
    }

    /**
     *
     * @return average Kilobytes per second
     */
    public float getAverageKilobytesPerSecond() {
        return getAverageBytesPerSecond() / 1024F;
    }

    /**
     *
     * @return average bytes per read
     */
    public float getAverageBytesPerRead() {
        return bytesCopied / numReads;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Staic methods
    ////////////////////////////////////////////////////////////////////////////////

    /**
     *
     * @param in
     * @param out
     * @throws IOException
     * @return status
     */
    public static int copyStream(InputStream in, OutputStream out)
    throws IOException {
        return copyStream(in, out, -1);
    }

    /**
     *
     * @param in
     * @param out
     * @param length
     * @throws IOException
     * @return status
     */
    public static int copyStream(InputStream in, OutputStream out, int length)
    throws IOException {
        OutputStream[] outputStreams = { out };

        return copyStream(in, outputStreams, length);
    }

    /**
     *
     * @param in
     * @param out
     * @throws IOException
     * @return status
     */
    public static int copyStream(InputStream in, OutputStream[] out)
    throws IOException {
        return copyStream(in, out, -1);
    }

    /**
     *
     * @param in
     * @param out
     * @param length
     * @throws IOException
     * @return status
     */
    public static int copyStream(InputStream in, OutputStream[] out, int length)
    throws IOException {
        return new StreamCopier(in, out).copy(length);
    }
}
