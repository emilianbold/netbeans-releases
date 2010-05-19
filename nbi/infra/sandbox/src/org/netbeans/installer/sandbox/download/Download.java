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
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.LogManager;

/**
 * This class represents an individual download.
 *
 * @author Kirill Sorokin
 */
public class Download {
    private URI uri;
    
    private File destination;
    
    private File tempFile;
    
    private DownloadOptions options;
    
    private Vector<DownloadThread> activeThreads = new Vector<DownloadThread>();
    private Vector<DownloadThread> finishedThreads = new Vector<DownloadThread>();
    
    private long contentLength = UNDEFINED_LENGTH;
    
    private long readLength = ZERO_LENGTH;
    
    private Date lastModified;
    
    private long currentSpeed = ZERO_SPEED;
    
    private int errorsNumber = INITIAL_ERRORS_NUMBER;
    
    private Vector<DownloadListener> listeners = new Vector<DownloadListener>();
    
    private Timer statusTimer = new Timer();
    
    Download(URI anURI, File aDestination, DownloadOptions someOptions, DownloadListener aListener) {
        // save the download source and destination parameters
        uri = anURI;
        destination = aDestination;
        
        // save the download options
        options = someOptions;
        
        if (aListener != null) {
            addDownloadListener(aListener);
        }
    }
    
    // download control methods ////////////////////////////////////////////////
    public void start() {
        // notify the download state listeners of what's happening
        notifyDownloadListeners(DownloadState.STARTED, "Started download from " + uri, null);
        
        // log
        LogManager.log("starting download from " + uri);
        LogManager.log("    ... source uri:       " + uri);
        LogManager.log("    ... destination file: " + destination);
        
        // check the destination
        if (!destination.exists()) {
            File destinationParent = destination.getParentFile();
            if (!destinationParent.exists() && !destinationParent.mkdirs()) {
                LogManager.log(ErrorLevel.ERROR, "... download failed -- cannot create the destination directory");
                notifyDownloadListeners(DownloadState.FAILED, "Download failed - cannot create destination directory", null);
                return;
            }
        } else if (destination.isDirectory()) {
            LogManager.log(ErrorLevel.ERROR, "... download failed -- the destination file is a directory");
            notifyDownloadListeners(DownloadState.FAILED, "Download failed - destination file is a directory", null);
            return;
        } else if (!destination.canRead()) {
            LogManager.log(ErrorLevel.ERROR, "... download failed -- cannot read the destination file");
            notifyDownloadListeners(DownloadState.FAILED, "Download failed - cannot read the destination file", null);
            return;
        } else if (!destination.canWrite()) {
            LogManager.log(ErrorLevel.ERROR, "... download failed -- cannot write to the destination file");
            notifyDownloadListeners(DownloadState.FAILED, "Download failed - cannot write to the destination file", null);
            return;
        }
        
        // create the temp file
        try {
            tempFile = FileUtils.createTempFile(destination.getParentFile());
        } catch (IOException e) {
            LogManager.log(ErrorLevel.ERROR, "... download failed -- cannot create temporary file");
            notifyDownloadListeners(DownloadState.FAILED, "Download failed - cannot create temp file", e);
            return;
        }
        LogManager.log(ErrorLevel.DEBUG, "    ... temporary file:   " + tempFile);
        
        // create the initial thread
        DownloadThread initialThread = new DownloadThread(this, uri, tempFile, options);
        
        if (options.getInt(DownloadOptions.MAX_SPEED) != -1) {
            initialThread.setMaximumSpeed(options.getInt(DownloadOptions.MAX_SPEED));
        }
        
        initialThread.start(false);
    }
    
    public void cancel() {
        LogManager.log("canceling download from " + uri);
        
        // cancel the threads
        cancelAllThreads();
        
        LogManager.log(ErrorLevel.DEBUG, "... canceled successfully");
        
        // notify the download state listeners of what's happening
        notifyDownloadListeners(DownloadState.CANCELED, "Canceled download from " + uri, null);
    }
    
    public void pause() {
        LogManager.log("pausing download from " + uri);
        
        // call pauseThread() on each active thread
        for (DownloadThread thread : activeThreads) {
            thread.pauseThread();
        }
        
        LogManager.log(ErrorLevel.DEBUG, "... paused successfully");
    }
    
    public void resume() {
        LogManager.log("resuming download from " + uri);
        
        // call resumeThread() on each paused thread
        for (DownloadThread thread : activeThreads) {
            if (thread.isPaused()) {
                thread.resumeThread();
            }
        }
        
        LogManager.log(ErrorLevel.DEBUG, "... resumed successfully");
    }
    
    // download <-> thread interaction /////////////////////////////////////////
    synchronized void threadStarted(DownloadThread thread) {
        LogManager.log(ErrorLevel.DEBUG, "    ... download thread started -- " + thread);
        
        notifyDownloadListeners(DownloadState.THREAD_STARTED, "Thread Started");
    }
    
    synchronized void threadRunning(DownloadThread thread) {
        LogManager.log(ErrorLevel.DEBUG, "    ... download thread running -- " + thread);
        
        // handle the first thread specially - we need to get some
        // information from it and perfrom some actions accordingly
        if (activeThreads.size() == 0) {
            // add the initial thread to the list
            activeThreads.add(thread);
            
            // save the total content length
            contentLength = thread.getContentLength();
            
            // save the last modified
            lastModified = thread.getModificationTime();
            
            // check the download conditions and decide whether we should
            // continue with downloading or the existing destination is OK
            if (!checkConditions()) {
                // check the available disk space
                long availableSpace = FileUtils.getFreeSpace(destination);
                if ((availableSpace != -1) && (availableSpace < contentLength)) {
                    cancelAllThreads();
                    LogManager.log(ErrorLevel.ERROR, "... download failed -- not enough disk space");
                    notifyDownloadListeners(DownloadState.FAILED, "Not enough space");
                    return;
                }
                
                // instruct the thread to start writing
                thread.startWriting();
                
                // if the download source support partial downloads - start
                // additional threads
                if (thread.supportsRanges()) {
                    startAdditionalThreads(thread);
                }
                
                TimerTask statusTask = new TimerTask() {
                    private long oldLength = 0;
                    
                    public void run() {
                        long length = readLength;
                        
                        currentSpeed = (long) (((double) (length - oldLength)) / 0.5);
                        
                        oldLength = length;
                    }
                };
                
                statusTimer.schedule(statusTask, 0, 500);
            } else {
                readLength = contentLength; // we need to make the percentage to be 100%
                
                cancelAllThreads();
                tempFile.delete();
                
                notifyDownloadListeners(DownloadState.COMPLETED, "Download from " + uri + " completed");
                return;
            }
        }
        
        // reset read length and get the read length for each thread and sum them
        readLength = ZERO_LENGTH;
        for (DownloadThread i : activeThreads) {
            readLength += i.getReadLength();
        }
        for (DownloadThread i : finishedThreads) {
            readLength += i.getReadLength();
        }
        
        // notify the download progress listeners of what's happening
        notifyDownloadListeners(DownloadState.RUNNING, "Thread Running");
    }
    
    synchronized void threadPaused(DownloadThread thread) {
        LogManager.log(ErrorLevel.DEBUG, "    ... download thread paused -- " + thread);
        
        notifyDownloadListeners(DownloadState.THREAD_PAUSED, "Thread Paused");
    }
    
    synchronized void threadResumed(DownloadThread thread) {
        LogManager.log(ErrorLevel.DEBUG, "    ... download thread resumed -- " + thread);
        
        notifyDownloadListeners(DownloadState.THREAD_RESUMED, "Thread Resumed");
    }
    
    synchronized void threadCompleted(DownloadThread thread) {
        LogManager.log(ErrorLevel.DEBUG, "    ... download thread completed -- " + thread);
        
        notifyDownloadListeners(DownloadState.THREAD_COMPLETED, "Thread completed");
        
        activeThreads.remove(thread);
        finishedThreads.add(thread);
        
        // reset read length and get the read length for each thread and sum them
        readLength = ZERO_LENGTH;
        for (DownloadThread i : activeThreads) {
            readLength += i.getReadLength();
        }
        for (DownloadThread i : finishedThreads) {
            readLength += i.getReadLength();
        }
        
        // check whether there are any threads still downloading, if there
        // are none - the download is complete
        if (activeThreads.size() > 0) {
            return;
        }
        
        statusTimer.cancel();
        
        DownloadOptions verificationOptions = DownloadOptions.getDefaults();
        verificationOptions.put(DownloadOptions.MAX_THREADS, 1);
        verificationOptions.put(DownloadOptions.MAX_ERRORS, 1);
        
        if (options.getBoolean(DownloadOptions.VERIFY_CRC)) {
            LogManager.log(ErrorLevel.DEBUG, "    trying to verify the crc32 checksum");
            
            try {
                String crc32 = FileUtils.readFile(DownloadManager.getInstance().download(uri.toString() + ".crc32", verificationOptions)).trim();
                if (!crc32.equals(FileUtils.getCrc32String(tempFile))) {
                    LogManager.log(ErrorLevel.ERROR, "... download failed -- crc32 verification failed");
                    notifyDownloadListeners(DownloadState.FAILED, "CRC32 Checksum verification failed for the downloaded file", null);
                    return;
                }
            } catch (DownloadException e) {
                LogManager.log(ErrorLevel.DEBUG, "    ... failed to verify the crc32 checksum");
            } catch (IOException e) {
                LogManager.log(ErrorLevel.DEBUG, "    ... failed to verify the crc32 checksum");
            }
        }
        
        if (options.getBoolean(DownloadOptions.VERIFY_MD5)) {
            LogManager.log(ErrorLevel.DEBUG, "    trying to verify the md5 checksum");
            
            try {
                String md5 = FileUtils.readFile(DownloadManager.getInstance().download(uri.toString() + ".md5", verificationOptions)).trim();
                if (!md5.equals(FileUtils.getMd5String(tempFile))) {
                    LogManager.log(ErrorLevel.ERROR, "... download failed -- md5 verification failed");
                    notifyDownloadListeners(DownloadState.FAILED, "MD5 Checksum verification failed for the downloaded file", null);
                    return;
                }
            } catch (DownloadException e) {
                LogManager.log(ErrorLevel.DEBUG, "    ... failed to verify the md5 checksum");
            } catch (IOException e) {
                LogManager.log(ErrorLevel.DEBUG, "    ... failed to verify the md5 checksum");
            } catch (NoSuchAlgorithmException e) {
                LogManager.log(ErrorLevel.DEBUG, "    ... failed to verify the md5 checksum");
            }
        }
        
        if (destination.exists()) {
            if (!destination.delete()) {
                LogManager.log(ErrorLevel.ERROR, "... download failed -- cannot delete the existing destination file");
                notifyDownloadListeners(DownloadState.FAILED, "Failed to delete the existing file....", null);
                return;
            }
        }
        
        try {
            FileUtils.moveFile(tempFile, destination);
        } catch (IOException e) {
            LogManager.log(ErrorLevel.ERROR, "... download failed -- cannot rename the temporary file to the destination file");
            notifyDownloadListeners(DownloadState.FAILED, "Failed to rename the temporary file....", null);
            return;
        }
        
        if (lastModified != null) {
            destination.setLastModified(lastModified.getTime());
        }
        
        LogManager.log(ErrorLevel.DEBUG, "... download from " + uri + " completed");
        notifyDownloadListeners(DownloadState.COMPLETED, "Download from " + uri + " completed");
        return;
    }
    
    synchronized void threadFailed(DownloadThread thread, Throwable exception) {
        final int maxErrors = options.getInt(DownloadOptions.MAX_ERRORS);
        
        LogManager.log(ErrorLevel.DEBUG, "    ... download thread failed -- " + thread + "; errors/max -- " + (errorsNumber + 1) + "/" + maxErrors);
        
        // notify the download progress listeners of what's happening
        notifyDownloadListeners(DownloadState.THREAD_FAILED, "Thread Failed", exception);
        
        // increment the errors number counter
        errorsNumber++;
        
        // if we have reached the maximum number of possible errors per
        // download - cancelThread everything and fail the download, restart the
        // thread otherwise
        if (errorsNumber == maxErrors) {
            // cancel all threads
            cancelAllThreads();
            
            statusTimer.cancel();
            
            // delete the temp file
            tempFile.delete();
            
            LogManager.log(ErrorLevel.ERROR, "... download failed -- maximum numbers of errors (" + maxErrors + ") exceeded");
            
            // notify the download progress listeners of what's happening
            notifyDownloadListeners(DownloadState.FAILED, "Error occurred in downloading from " + uri);
        } else {
            // restart the thread
            restartThread(thread);
        }
    }
    
    synchronized void threadCanceled(DownloadThread thread) {
        LogManager.log(ErrorLevel.DEBUG, "    ... download thread canceled -- " + thread);
        
        notifyDownloadListeners(DownloadState.THREAD_CANCELED, "Thread Canceled");
    }
    
    // getters & setters ///////////////////////////////////////////////////////
    public URI getURI() {
        return uri;
    }
    
    public File getDestination() {
        return destination;
    }
    
    public File getTempFile() {
        return tempFile;
    }
    
    public DownloadOptions getOptions() {
        return options;
    }
    
    public long getContentLength() {
        return contentLength;
    }
    
    public long getReadLength() {
        return readLength;
    }
    
    public Date getLastModified() {
        return lastModified;
    }
    
    public long getCurrentSpeed() {
        return currentSpeed;
    }
    
    public int getErrorsNumber() {
        return errorsNumber;
    }
    
    public int getPercentage() {
        if (contentLength != UNDEFINED_LENGTH) {
            return (int) (readLength * 100 / contentLength);
        } else {
            return ZERO_LENGTH;
        }
    }
    
    // private stuff ///////////////////////////////////////////////////////////
    private void cancelAllThreads() {
        // call cancelThread() on each active thread
        for (DownloadThread thread : activeThreads) {
            thread.cancelThread();
        }
    }
    
    private void startAdditionalThreads(DownloadThread initialThread) {
        // assertion
        assert initialThread.supportsRanges();
        
        // get the maximum threads number
        int threadsNumber = options.getInt(DownloadOptions.MAX_THREADS);
        
        // get the length to read per thread
        long chunk = contentLength / threadsNumber;
        
        // if the chunk is not zero length
        if (chunk != ZERO_LENGTH) {
            // correct the langth of the first thread
            initialThread.setLength(chunk);
            
            int speedPerThread;
            if (options.getInt(DownloadOptions.MAX_SPEED) != -1) {
                speedPerThread = options.getInt(DownloadOptions.MAX_SPEED) / options.getInt(DownloadOptions.MAX_THREADS);
            } else {
                speedPerThread = -1;
            }
            
            initialThread.setMaximumSpeed(speedPerThread);
            
            // start additional threads
            for (int i = 1; i < threadsNumber; i++) {
                // set the offset and length for each started thread; the length for
                // the last thread will be undefined as it will read as much as
                // possible
                long offset = i * chunk;
                long length = (i != threadsNumber - 1) ? chunk : UNDEFINED_LENGTH;
                
                // create the thread
                DownloadThread thread =
                        new DownloadThread(this, uri, tempFile, offset, length, options);
                
                thread.setMaximumSpeed(speedPerThread);
                
                // start the thread, note that since it's not the first thread 
                // it can start writing from the very beginning
                thread.start(true);
                
                // add it to the active threads list
                activeThreads.add(thread);
            }
        }
    }
    
    private void restartThread(DownloadThread failedThread) {
        // get the thread's properties
        long offset = failedThread.getOffset() + failedThread.getReadLength();
        long length = failedThread.getLength();
        
        // if length is finite - truncate it by the read length amount
        if (length != UNDEFINED_LENGTH) {
            length = length - failedThread.getReadLength();
        }
        
        activeThreads.remove(failedThread);
        finishedThreads.add(failedThread);
        
        // create a new thread with the settings of the failed one
        DownloadThread thread = new DownloadThread(this, uri, tempFile, offset, length, options);
        
        // add it to the active threads list
        activeThreads.add(thread);
        
        int speedPerThread;
        if (options.getInt(DownloadOptions.MAX_SPEED) != -1) {
            thread.setMaximumSpeed(options.getInt(DownloadOptions.MAX_SPEED) / options.getInt(DownloadOptions.MAX_THREADS));
        }
        
        // register this download as the listener for thread's events and start the
        // thread
        thread.start(true);
    }
    
    private boolean checkConditions() {
        boolean checkExistance = options.getBoolean(DownloadOptions.CHECK_EXISTANCE);
        boolean checkSize = options.getBoolean(DownloadOptions.CHECK_SIZE);
        boolean checkLastModified = options.getBoolean(DownloadOptions.CHECK_LAST_MODIFIED);
        
        if (checkExistance || checkSize || checkLastModified) {
            if (checkExistance && !destination.exists()) {
                return false;
            }
            if (checkSize && (destination.length() != contentLength)) {
                return false;
            }
            if (checkLastModified && (destination.lastModified() < lastModified.getTime())) {
                return false;
            }
            
            return true;
        } else {
            return false;
        }
    }
    
    // listeners ///////////////////////////////////////////////////////////////
    public void addDownloadListener(DownloadListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    public void removeDownloadListener(DownloadListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    private void notifyDownloadListeners(DownloadState state, String message) {
        notifyDownloadListeners(state, message, null);
    }
    
    private void notifyDownloadListeners(DownloadState state, String message, Throwable exception) {
        DownloadEvent event = new DownloadEvent(this, state, message, exception);
        
        for (DownloadListener listener : listeners.toArray(new DownloadListener[0])) {
            switch (state) {
                case STARTED:
                    listener.downloadStarted(event);
                    break;
                case RUNNING:
                    listener.downloadRunning(event);
                    break;
                case PAUSED:
                    listener.downloadPaused(event);
                    break;
                case RESUMED:
                    listener.downloadResumed(event);
                    break;
                case FAILED:
                    listener.downloadFailed(event);
                    break;
                case CANCELED:
                    listener.downloadCanceled(event);
                    break;
                case COMPLETED:
                    listener.downloadCompleted(event);
                    break;
                case THREAD_STARTED:
                    listener.downloadThreadStarted(event);
                    break;
                case THREAD_PAUSED:
                    listener.downloadThreadPaused(event);
                    break;
                case THREAD_RESUMED:
                    listener.downloadThreadResumed(event);
                    break;
                case THREAD_COMPLETED:
                    listener.downloadThreadCompleted(event);
                    break;
                case THREAD_FAILED:
                    listener.downloadThreadFailed(event);
                    break;
                case THREAD_CANCELED:
                    listener.downloadThreadCanceled(event);
                    break;
                default:
                    ErrorManager.notify(ErrorLevel.CRITICAL, "WTF WTF The download state is not recognized..");
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner classes
    public static interface DownloadListener {
        public void downloadStarted(DownloadEvent event);
        
        public void downloadRunning(DownloadEvent event);
        
        public void downloadPaused(DownloadEvent event);
        
        public void downloadResumed(DownloadEvent event);
        
        public void downloadFailed(DownloadEvent event);
        
        public void downloadCanceled(DownloadEvent event);
        
        public void downloadCompleted(DownloadEvent event);
        
        public void downloadThreadStarted(DownloadEvent event);
        
        public void downloadThreadPaused(DownloadEvent event);
        
        public void downloadThreadResumed(DownloadEvent event);
        
        public void downloadThreadCompleted(DownloadEvent event);
        
        public void downloadThreadFailed(DownloadEvent event);
        
        public void downloadThreadCanceled(DownloadEvent event);
    }
    
    public static class DownloadAdapter implements DownloadListener {
        public void downloadStarted(DownloadEvent event) {
        }
        
        public void downloadRunning(DownloadEvent event) {
        }
        
        public void downloadPaused(DownloadEvent event) {
        }
        
        public void downloadResumed(DownloadEvent event) {
        }
        
        public void downloadFailed(DownloadEvent event) {
        }
        
        public void downloadCanceled(DownloadEvent event) {
        }
        
        public void downloadCompleted(DownloadEvent event) {
        }
        
        public void downloadThreadStarted(DownloadEvent event) {
        }
        
        public void downloadThreadPaused(DownloadEvent event) {
        }
        
        public void downloadThreadResumed(DownloadEvent event) {
        }
        
        public void downloadThreadCompleted(DownloadEvent event) {
        }
        
        public void downloadThreadFailed(DownloadEvent event) {
        }
        
        public void downloadThreadCanceled(DownloadEvent event) {
        }
    }
    
    public static class DownloadEvent {
        private Download source;
        
        private DownloadState state;
        private String message;
        private Throwable exception;
        
        public DownloadEvent(Download aSource, DownloadState aState, String aMessage, Throwable anException) {
            source = aSource;
            
            state = aState;
            message = aMessage;
            exception = anException;
        }
        
        public Download getSource() {
            return source;
        }
        
        public DownloadState getState() {
            return state;
        }
        
        public String getMessage() {
            return message;
        }
        
        public Throwable getException() {
            return exception;
        }
    }
    
    public static enum DownloadState {
        STARTED,
        RUNNING,
        PAUSED,
        RESUMED,
        FAILED,
        CANCELED,
        COMPLETED,
        THREAD_STARTED,
        THREAD_PAUSED,
        THREAD_RESUMED,
        THREAD_COMPLETED,
        THREAD_FAILED,
        THREAD_CANCELED;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final int INITIAL_ERRORS_NUMBER = 0;
    public static final int ZERO_OFFSET = 0;
    public static final int ZERO_LENGTH = 0;
    public static final int ZERO_SPEED = 0;
    public static final int UNDEFINED_LENGTH = -1;
}
