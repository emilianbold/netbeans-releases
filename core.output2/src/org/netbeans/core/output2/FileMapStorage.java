/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.core.output2;

import java.util.logging.Logger;
import org.openide.util.NbBundle;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * An implementation of the Storage interface over a memory mapped file.
 *
 */
class FileMapStorage implements Storage {
    /** A file channel for reading/writing the mapped file */
    private FileChannel fileChannel;
    /** The base number of bytes to allocate when a getWriteBuffer for writing is
     * needed. */
    private static final int BASE_BUFFER_SIZE = 8196;
    /**
     * max possible range to map.. 1 MB
     */
    private static final long MAX_MAP_RANGE = 1024 * 1024;
    /**
     * The byte getWriteBuffer that write operations write into.  Actual buffers are
     * provided for writing by calling master.slice(); this getWriteBuffer simply
     * pre-allocates a fairly large chunk of memory to reduce repeated
     * allocations.
     */
    private ByteBuffer master;
    /** A byte getWriteBuffer mapped to the contents of the output file, from which
     * content is read. */
    private ByteBuffer contents;
    /** The number of bytes from the file that have been are currently mapped
     * into the contents ByteBuffer.  This will be checked on calls that read,
     * and if more than the currently mapped bytes are requested, the
     * contents bufffer will be replaced by a larger one */
    private long mappedRange;
    
    /**
     * start of the mapped range..
     */
    private long mappedStart;
    /**
     * The currently in use buffer.
     */
    private ByteBuffer buffer = null;
    /**
     * The number of bytes that have been written.
     */
    protected int bytesWritten = 0;
    /**
     * The file we are writing to.
     */
    private File outfile = null;
    
    private int outstandingBufferCount = 0;
    
    private boolean closed;

    FileMapStorage() {
        init();
    }

    private void init() {
        contents = null;
        mappedRange = -1;
        mappedStart = 0;
        master = ByteBuffer.allocateDirect (BASE_BUFFER_SIZE);
        fileChannel = null;
        buffer = null;
        bytesWritten = 0;
        closed = true;
    }

    /**
     * Ensure that the output file exists.
     */
    private void ensureFileExists() throws IOException {
        if (outfile == null) {
            String outdir = System.getProperty("java.io.tmpdir"); //NOI18N
            if (!outdir.endsWith(File.separator)) {
                outdir += File.separator;
            }
            File dir = new File (outdir);
            if (!dir.exists()) {
                //Handle the event that we cannot find the system temporary directory
                IllegalStateException ise = new IllegalStateException ("Cannot find temp directory " + outdir); //NOI18N
                Exceptions.attachLocalizedMessage(ise, NbBundle.getMessage(OutWriter.class, "FMT_CannotWrite", outdir));
                throw ise;
            }
            //#47196 - if user holds down F9, many threads can enter this method
            //simultaneously and all try to create the same file
            synchronized (FileMapStorage.class) {
                StringBuilder fname = new StringBuilder(outdir)
                        .append("output").append(Long.toString(System.currentTimeMillis())); //NOI18N
                outfile = new File (fname.toString());
                while (outfile.exists()) {
                    fname.append('x'); //NOI18N
                    outfile = new File(fname.toString());
                }
                outfile.createNewFile();
                if (!outfile.exists() || !outfile.canWrite()) {
                    //Handle the (unlikely) case we cannot write to the system temporary directory
                    IllegalStateException ise = new IllegalStateException ("Cannot write to " + fname); //NOI18N
                    Exceptions.attachLocalizedMessage(ise, NbBundle.getMessage(OutWriter.class, "FMT_CannotWrite", outdir));
                    throw ise;
                }
                outfile.deleteOnExit();
            }
        }
    }
    
    @Override
    public String toString() {
        return outfile == null ? "[unused or disposed FileMapStorage]" : outfile.getPath();
    }
    
    private FileChannel writeChannel() {
        FileChannel channel = fileChannel();
        closed = !channel.isOpen();
        return channel;
    }

    /**
     * Get a FileChannel opened for reading/writing against the output file.
     */
    private FileChannel fileChannel() {
        try {
            if (fileChannel == null || !fileChannel.isOpen()) {
                ensureFileExists();
                RandomAccessFile raf = new RandomAccessFile(outfile, "rw");
                fileChannel = raf.getChannel();
            }
            return fileChannel;
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace(); //XXX
        } catch (IOException ioe) {
            ioe.printStackTrace(); //XXX
        }
        return null;
    }

    /**
     * Fetch a getWriteBuffer of a specified size to use for appending new data to the
     * end of the file.
     */
    public synchronized ByteBuffer getWriteBuffer (int size) {
        if (master.capacity() - master.position() < size) {
            int newSize = Math.max (BASE_BUFFER_SIZE * 2, 
                size + BASE_BUFFER_SIZE);
            
            master = ByteBuffer.allocateDirect (newSize);
        }

        if (buffer == null) {
            buffer = master.slice();
        } else {
            int charsRemaining = AbstractLines.toCharIndex(buffer.capacity() - buffer.position());

            if (charsRemaining < size) {
                buffer.flip();
                buffer = master.slice();
            }
        }
        outstandingBufferCount++;
        return buffer;
    }

    /**
     * Dispose of a ByteBuffer which has been acquired for writing by one of
     * the write methods, writing its contents to the file.
     */
    public int write (ByteBuffer bb) throws IOException {
        synchronized (this) {
            if (bb == buffer) {
                buffer = null;
            }
        }
        int position = size();
        int byteCount = bb.position();
        bb.flip();
        FileChannel channel = writeChannel();
        if (channel.isOpen()) { //If a thread was terminated while writing, it will be closed
            channel.write (bb);
            synchronized (this) {
                bytesWritten += byteCount;
                outstandingBufferCount--;
            }
        }
        return position;
    }

    public synchronized void dispose() {
        if (Controller.LOG) {
            Controller.log ("Disposing file map storage");
            Controller.logStack();
        }
        final FileChannel oldChannel = fileChannel;
        final File oldFile = outfile;
        final MappedByteBuffer mbb = contents instanceof MappedByteBuffer ? (MappedByteBuffer) contents : null;
        fileChannel = null;
        closed = true;
        outfile = null;
        buffer = null;
        contents = null;
        if (oldChannel != null || oldFile != null) {
            RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    try {
                        if (mbb != null) {
                            unmap(mbb);
                        }
                        if (oldChannel != null && oldChannel.isOpen()) {
                            oldChannel.close();
                        }
                        if (oldFile != null && oldFile.exists()) {
                            oldFile.delete();
                        }
                    } catch (Exception e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            });
        }
    }

    File getOutputFile() {
        return outfile;
    }

    /**
     * Workaround for JDK issue #4715154 (http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4715154)
     */
    private void unmap(Object buffer) {
        try {
            Method getCleanerMethod = buffer.getClass().getMethod("cleaner", new Class[0]);
            getCleanerMethod.setAccessible(true);
            sun.misc.Cleaner cleaner = (sun.misc.Cleaner) getCleanerMethod.invoke(buffer, new Object[0]);
            cleaner.clean();
        } catch (Exception e) {
        }
    }

    /**
     * Get a byte buffer representing the a getText of the contents of the
     * output file.  This is optimized to possibly map more of the output file
     * into memory if it is not already mapped.
     */
    public ByteBuffer getReadBuffer(int start, int byteCount) throws IOException {
        ByteBuffer cont;
        synchronized (this) {
            cont = this.contents;
            if (cont == null || start + byteCount > mappedRange || start < mappedStart) {
                FileChannel ch = fileChannel();
                mappedStart = Math.max((long)0, start - (MAX_MAP_RANGE /2));
                long prevMappedRange = mappedRange;
                long map = byteCount > (MAX_MAP_RANGE / 2) ? (byteCount + byteCount / 10) : (MAX_MAP_RANGE / 2);
                mappedRange = Math.min(ch.size(), start + map);
                try {
                    try {
                        cont = ch.map(FileChannel.MapMode.READ_ONLY, mappedStart, mappedRange - mappedStart);
                        this.contents = cont;
                    } catch (IOException ioe) {
                        Logger.getAnonymousLogger().info("Failed to memory map output file for reading. Trying to read it normally."); //NOI18N

                        // Memory mapping failed, fallback to non-mapped
                        cont = ByteBuffer.allocate((int) (mappedRange - mappedStart));
                        ch.read(cont, mappedStart);
                        this.contents = cont;
                    }
                } catch (IOException ioe) {
                    Logger.getAnonymousLogger().info("Failed to read output file. Start:" + start + " bytes reqd=" + //NOI18N
                        byteCount + " mapped range=" + mappedRange + //NOI18N
                        " previous mapped range=" + prevMappedRange + //NOI18N
                        " channel size: " + ch.size()); //NOI18N
                    throw ioe;
                }
            }
            if (start - mappedStart > cont.limit() - byteCount) {
                cont.position(Math.max(0, cont.limit() - byteCount));
            } else {
                cont.position((int) (start - mappedStart));
            }
        }
        int limit = Math.min(cont.limit(), byteCount);
        try {
            return (ByteBuffer) cont.slice().limit(limit);
        } catch (Exception e) {
            throw new IllegalStateException ("Error setting limit to " + limit //NOI18N
            + " contents size = " + cont.limit() + " requested: read " + //NOI18N
            "buffer from " + start + " to be " + byteCount + " bytes"); //NOI18N
        }
    }

    public synchronized int size() {
        return bytesWritten;
    }

    public void flush() throws IOException {
        if (buffer != null) {
            if (Controller.LOG) Controller.log("FILEMAP STORAGE flush(): " + outstandingBufferCount);
            write (buffer);
            fileChannel.force(false);
            buffer = null;
        }
    }

    public void close() throws IOException {
        if (fileChannel != null) {
            flush();
        }
        closed = true;
    }

    public boolean isClosed() {
        return fileChannel == null || closed;
    }
}
