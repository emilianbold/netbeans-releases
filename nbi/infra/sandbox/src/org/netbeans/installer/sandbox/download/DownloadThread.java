/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.sandbox.download;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;
import org.netbeans.installer.sandbox.download.connection.Connection;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.ErrorManager;

/**
 *
 * @author Kirill Sorokin
 */
class DownloadThread extends Thread {
    private URI uri;
    private File file;
    private long offset;
    private long length;
    
    private Download download;
    private DownloadOptions options;
    
    private int maximumSpeed = -1;
    
    private byte[] buffer;
    
    private long contentLength;
    private long readLength;
    private boolean supportsRanges;
    
    private Date modificationTime;
    
    private boolean paused;
    private boolean canceled;
    
    private boolean resumeFailed;
    
    private boolean canWrite;
    
    private Connection connection;
    private RandomAccessFile output;
    
    private ReentrantLock lock = new ReentrantLock();
    
    DownloadThread(Download aDownload, URI anURI, File aFile, DownloadOptions someOptions) {
        this(aDownload, anURI, aFile, Download.ZERO_OFFSET, Download.UNDEFINED_LENGTH, someOptions);
    }
    
    DownloadThread(Download aDownload, URI anURI, File aFile, long anOffset, long aLength, DownloadOptions someOptions) {
        // save the incoming parameters
        download = aDownload;
        
        uri = anURI;
        file = aFile;
        offset = anOffset;
        length = aLength;
        
        // save the download options
        options = someOptions;
        
        // initialize the buffer
        buffer = new byte[BUFFER_LENGTH];
    }
    
    public void start(boolean isCanWrite) {
        canWrite = isCanWrite;
        start();
    }
    
    public void run() {
        try {
            // notify the download that we have started this thread
            download.threadStarted(this);
            
            // open the streams (connection and file output)
            openStreams();
            
            // get the connection properties
            contentLength = connection.getContentLength();
            supportsRanges = connection.supportsRanges();
            modificationTime = connection.getModificationDate();
            
            // loop while we haven't read all that is available or all that we
            // need to read
            while (!downloadComplete()) {
                if (canceled || resumeFailed) {
                    return;
                }
                
                if (!paused) {
                    // notify the download that we are still running
                    download.threadRunning(this);
                    
                    // if the thread is not yet allowed to write, skip the i/o
                    // operations
                    if (canWrite) {
                        lock.lock();
                        try {
                            // read what's available from the connection and if we
                            // have read more than we needed to - truncate
                            int read = correctReadAmount(connection.read(buffer));
                            
                            // write the read bytes to the output
                            output.write(buffer, 0, read);
                            
                            // add the read bytes to the total amount of read data
                            readLength += read;
                            
                            // if the maximum speed is specified - wait
                            if (maximumSpeed != -1) {
                                try {
                                    Thread.sleep(250);
                                } catch (InterruptedException e) {
                                    ErrorManager.notify(ErrorLevel.DEBUG, "Interrupted while sleeping", e);
                                }
                            }
                        } finally {
                            lock.unlock();
                        }
                    }
                } else {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        ErrorManager.notify(ErrorLevel.DEBUG, "Interrupted while sleeping", e);
                    }
                }
            }
        } catch (InitializationException e) {
            download.threadFailed(this, e);
            return;
        } catch (IOException e) {
            download.threadFailed(this, e);
            return;
        } finally {
            // close the streams
            closeStreams();
        }
        
        // we have successfully completed - notify the download
        download.threadCompleted(this);
    }
    
    private int correctReadAmount(int read) {
        if ((length != Download.UNDEFINED_LENGTH) && (read > (length - readLength))) {
            return (int) (length - readLength);
        } else if ((contentLength != Download.UNDEFINED_LENGTH) && (read > (contentLength - readLength))) {
            return (int) (contentLength - readLength);
        } else {
            return read;
        }
    }
    
    private void openStreams() throws InitializationException, IOException {
        lock.lock();
        try {
            long correctLength =
                    length != Download.UNDEFINED_LENGTH ? length - readLength : length;
            
            // initialize the connection and open it
            connection = Connection.getConnection(uri, offset + readLength, correctLength, options);
            connection.open();
            
            // initialize the output file and move to the specified offset
            output = new RandomAccessFile(file, "rw");
            output.seek(offset + readLength);
        } finally {
            lock.unlock();
        }
    }
    
    private void closeStreams() {
        lock.lock();
        try {
            // if the output is not null - try to close it
            try {
                if (output != null) {
                    output.close();
                }
            } catch (IOException e) {
                ErrorManager.notify(ErrorLevel.WARNING, e);
            }
            
            // if the connection is not null - try to close it
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (IOException e) {
                ErrorManager.notify(ErrorLevel.WARNING, e);
            }
        } finally {
            lock.unlock();
        }
    }
    
    public void cancelThread() {
        download.threadCanceled(this);
        
        canceled = true;
    }
    
    public void startWriting() {
        canWrite = true;
    }
    
    public void pauseThread() {
        // notify the download
        download.threadPaused(this);
        
        // set the marker
        paused = true;
        
        closeStreams();
    }
    
    public void resumeThread() {
        try {
            // notify the download
            download.threadResumed(this);
            
            openStreams();
            
            // set the marker
            paused = false;
        } catch (InitializationException e) {
            resumeFailed = true;
            download.threadFailed(this, e);
        } catch (IOException e) {
            resumeFailed = true;
            download.threadFailed(this, e);
        }
    }
    
    // if the length is finite, we should read until we read as much as we
    // are told to. if the length in infinite (undefined) we should read as
    // much as it is available. if we don't know how much is available (the
    // content length is infinite - we should read while there is something to
    // read.
    private boolean downloadComplete() throws IOException {
        lock.lock();
        try {
            if (length != Download.UNDEFINED_LENGTH) {
                if (readLength == length) {
                    return true;
                } else {
                    return false;
                }
            } else {
                if (contentLength != Download.UNDEFINED_LENGTH) {
                    if (readLength == contentLength) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    if (paused || connection.available() > 0) {
                        return false;
                    } else {
                        return true;
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }
    
    public long getOffset() {
        return offset;
    }
    
    public long getLength() {
        return length;
    }
    
    public void setLength(long aLength) {
        length = aLength;
    }
    
    public boolean supportsRanges() {
        return supportsRanges;
    }
    
    public long getContentLength() {
        return contentLength;
    }
    
    public Date getModificationTime() {
        return modificationTime;
    }
    
    public long getReadLength() {
        return readLength;
    }
    
    public boolean isCanceled() {
        return canceled;
    }
    
    public boolean isPaused() {
        return paused;
    }
    
    public void setMaximumSpeed(int aMaximumSpeed) {
        maximumSpeed = aMaximumSpeed;
        
        if (maximumSpeed == -1) {
            buffer = new byte[BUFFER_LENGTH];
        } else {
            int bufferSize = maximumSpeed / 4;
            buffer = new byte[bufferSize];
        }
    }
    
    public int getMaximumSpeed() {
        return maximumSpeed;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final int BUFFER_LENGTH = 1024 * 100;
}
