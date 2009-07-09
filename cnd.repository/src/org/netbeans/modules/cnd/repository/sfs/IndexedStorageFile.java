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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.repository.sfs;

import org.netbeans.modules.cnd.repository.sfs.index.FileIndexFactory;
import org.netbeans.modules.cnd.repository.sfs.index.SimpleFileIndex;
import org.netbeans.modules.cnd.repository.sfs.index.FileIndex;
import org.netbeans.modules.cnd.repository.sfs.index.ChunkInfo;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Iterator;
import org.netbeans.modules.cnd.repository.sfs.index.CompactFileIndex;
import org.netbeans.modules.cnd.repository.sfs.statistics.FileStatistics;
import org.netbeans.modules.cnd.repository.sfs.statistics.RangeStatistics;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.testbench.Stats;

/**
 * Represents the data file with the indexed access
 *
 * @author Nickolay Dalmatov
 */
class IndexedStorageFile extends FileStorage {

    private static final boolean TRACE = false;
    final private File dataFile;
    final private File indexFile;
    final private FileStatistics fileStatistics;
    private FileIndex index;
    final private FileRWAccess fileRWAccess;
    // used to accumulate the total currently used chunk suze;
    // is necessary for tracking fragmentation
    private long usedSize;
    private static final class Lock {}
    private final Object writeLock = new Lock();

    public IndexedStorageFile(final File basePath, final String name, final boolean create) throws IOException {
        dataFile = new File(basePath, name + "-data"); // NOI18N
        indexFile = new File(basePath, name + "-index"); // NOI18N
        fileStatistics = new FileStatistics();
        boolean filesExists = (dataFile.exists() && indexFile.exists());
        fileRWAccess = createFileRWAccess(dataFile);
        boolean recreate = create || !filesExists;

        if (!recreate) {
            try {
                loadIndex();
                recalcUsedSize();
            } catch (IOException e) {
                recreate = true;
            }

            indexFile.delete();

            if (usedSize == 0) {
                fileRWAccess.truncate(0);
            }
        }

        if (recreate) {
            index = Stats.useCompactIndex ? new CompactFileIndex() : new SimpleFileIndex();
            //index = new SimpleFileIndex();            
            fileRWAccess.truncate(0);

            if (indexFile.exists()) {
                indexFile.delete();
            }

            usedSize = 0;
        }
    }

    public Persistent read(final Key key) throws IOException {
        Persistent object = null;

        final ChunkInfo chunkInfo = index.get(key);
        if (chunkInfo != null) {
            object = fileRWAccess.read(key.getPersistentFactory(), chunkInfo.getOffset(), chunkInfo.getSize());
            fileStatistics.incrementReadCount(key);
        }

        return object;
    }

    public void write(final Key key, final Persistent object) throws IOException {

        final long offset;
        final int size;
        final int oldSize;

        synchronized (writeLock) {
            offset = fileRWAccess.size();
            size = fileRWAccess.write(key.getPersistentFactory(), object, offset);
            oldSize = index.put(key, offset, size);
            usedSize += (size - oldSize);
            fileStatistics.incrementWriteCount(key, oldSize, size);
        }
    }

    public void remove(final Key key) throws IOException {
        fileStatistics.removeNotify(key);

        final int oldSize = index.remove(key);

        if (oldSize != 0) {
            if (index.size() == 0) {
                fileRWAccess.truncate(0);
                usedSize = 0;
            } else {
                usedSize -= -oldSize;
            }
        }
    }

    public int getObjectsCount() {
        return index.size();
    }

    public long getSize() throws IOException {
        return fileRWAccess.size();

    }

    public void close() throws IOException {
        if (Stats.dumoFileOnExit) {
            dump(System.out);
        } else {
            if (Stats.fileStatisticsLevel > 0) {
                dumpSummary(System.out);
            }
        }

        fileRWAccess.close();
        storeIndex();
    }

    public int getFragmentationPercentage() throws IOException {
        final long fileSize;
        final float delta;

        fileSize = fileRWAccess.size();
        delta = fileSize - usedSize;

        final float percentage = delta * 100 / fileSize;
        return Math.round(percentage);
    }

    public void dump(final PrintStream ps) throws IOException {
        if (TRACE) {
            ps.printf("\nDumping %s\n", dataFile.getAbsolutePath()); // NOI18N
            ps.printf("\nKeys:\n"); // NOI18N
        }
        for (Key key : index.keySet()) {
            ChunkInfo chunk = index.get(key);
            if (TRACE) {
                ps.printf("\t%s: ", key); // NOI18N
            }
            print(ps, null, chunk, true);
        }

        if (TRACE) {
            ps.printf("\nChunks:\n"); // NOI18N
        }
        final ChunkInfo[] infos = sortedChunkInfos();
        for (int i = 0; i < infos.length; i++) {
            print(ps, null, infos[i], true);
        }

        dumpSummary(ps, infos);
    }

    private long recalcUsedSize() {
        long calcUsedSize = 0;
        for (Key key : index.keySet()) {
            ChunkInfo info = index.get(key);
            calcUsedSize += info.getSize();
        }
        usedSize = calcUsedSize;
        return calcUsedSize;
    }

    public void dumpSummary(final PrintStream ps) throws IOException {
        dumpSummary(ps, null);
    }

    private void dumpSummary(final PrintStream ps, ChunkInfo[] sortedInfos) throws IOException {
        RangeStatistics write = new RangeStatistics("Writes:", Stats.fileStatisticsLevel, Stats.fileStatisticsRanges);   // NOI18N
        RangeStatistics read = new RangeStatistics("Reads: ", Stats.fileStatisticsLevel, Stats.fileStatisticsRanges);    // NOI18N
        RangeStatistics size = new RangeStatistics("Sizes: ", Stats.fileStatisticsLevel, Stats.fileStatisticsRanges);    // NOI18N
        for (Key key : index.keySet()) {
            ChunkInfo info = index.get(key);
            usedSize += info.getSize();
            read.consume(fileStatistics.getReadCount(key));
            write.consume(fileStatistics.getWriteCount(key));
            size.consume(info.getSize());
        }
        long channelSize = fileRWAccess.size();

        if (TRACE) {
            ps.printf("\n"); // NOI18N
            ps.printf("Dumping %s\n", dataFile.getAbsolutePath()); // NOI18N
            ps.printf("Entries count: %d\n", index.size()); // NOI18N
            ps.printf("\n"); // NOI18N
            write.print(ps);
            read.print(ps);
            size.print(ps);
            ps.printf("\n"); // NOI18N
            ps.printf("File size:  %16d\n", channelSize); // NOI18N
            ps.printf("Used size:  %16d\n", usedSize); // NOI18N
            ps.printf("Percentage used: %11d%%\n", channelSize == 0 ? 0 : ((100 * usedSize) / channelSize)); // NOI18N
            ps.printf("Fragmentation:   %11d%%\n", getFragmentationPercentage()); // NOI18N
        }
        if (sortedInfos == null) {
            sortedInfos = sortedChunkInfos();
        }
        long firstExtent = (sortedInfos.length > 0) ? sortedInfos[0].getOffset() : 0;
        if (TRACE) {
            ps.printf("First busy extent: %9d (0x%H)\n\n", firstExtent, firstExtent); // NOI18N
        }
    }

    private void print(final PrintStream ps, final Key key, final ChunkInfo chunk, final boolean lf) {
        final long endOffset = chunk.getOffset() + chunk.getSize() - 1;
        if (TRACE) {
            ps.printf("%d-%d %d [0x%H-0x%H] read: %d written: %d (%s) %c", // NOI18N
                    chunk.getOffset(), endOffset, chunk.getSize(), chunk.getOffset(), endOffset,
                    fileStatistics.getReadCount(key), fileStatistics.getWriteCount(key), chunk.toString(),
                    lf ? '\n' : ' '); // NOI18N
        }
    }

    private ChunkInfo[] sortedChunkInfos() {
        ChunkInfo[] infos = new ChunkInfo[index.size()];
        int pos = 0;

        for (Key key : index.keySet()) {
            infos[pos++] = index.get(key);
        }

        Arrays.sort(infos);
        return infos;
    }

    /*packet */ String getTraceString() throws IOException {
        final Formatter formatter = new Formatter();
        formatter.format("%s index size %d  file size %d  fragmentation %d%%", // NOI18N
                dataFile.getName(), index.size(), getSize(), getFragmentationPercentage());
        return formatter.toString();
    }

    /*packet*/ Iterator<Key> getKeySetIterator() {
        return new IndexIterator();
    }

    /* packet */ ChunkInfo getChunkInfo(Key key) {
        return index.get(key);

    }

    /* packet */ String getDataFileName() {
        return dataFile.getName();
    }

    /*packet */ long getDataFileUsedSize() {
        return usedSize;

    }

    /*packet */ void moveDataFromOtherFile(FileRWAccess fileRW, long l, int size, long newOffset, Key key) throws IOException {
        fileRWAccess.move(fileRW, l, size, newOffset);
        index.put(key, newOffset, size);
        usedSize += size;
    }

    /*packet */ FileRWAccess getDataFile() {
        return fileRWAccess;
    }

    /*packet */ FileRWAccess createFileRWAccess(File file) throws IOException {
        FileRWAccess result;
        switch (Stats.fileRWAccess) {
            case 0:
                result = new BufferedRWAccess(file);
                break;
            case 1:
                result = new SimpleRWAccess(file);
                break;
            default:
                result = new BufferedRWAccess(file);
        }
        //result.truncate(0);
        return result;
    }

    private void loadIndex() throws IOException {
        InputStream in, bin;
        DataInputStream din = null;

        try {
            in = new FileInputStream(indexFile);
            bin = new BufferedInputStream(in);
            din = new DataInputStream(bin);

            index = FileIndexFactory.getDefaultFactory().readIndex(din);

        } finally {
            if (din != null) {
                din.close();
            }
        }
    }

    private void storeIndex() throws IOException {
        OutputStream out, bos;
        DataOutputStream dos = null;

        try {
            out = new FileOutputStream(indexFile);
            bos = new BufferedOutputStream(out, 1024);
            dos = new DataOutputStream(bos);

            FileIndexFactory.getDefaultFactory().writeIndex(index, dos);
        } finally {
            if (dos != null) {
                dos.close();
            }
        }
    }

    public boolean defragment(long timeout) throws IOException {
        return false;
    }

    /*
     *  Iterator<Key> implementation for the index
     *
     */
    private class IndexIterator implements Iterator<Key> {

        private Iterator<Key> indexIterator;
        private Key currentKey;

        IndexIterator() {
            indexIterator = index.getKeySetIterator();
        }

        public boolean hasNext() {
            return indexIterator.hasNext();
        }

        public Key next() {
            currentKey = indexIterator.next();
            return currentKey;
        }

        public void remove() {
            assert currentKey != null;
            final ChunkInfo chi = getChunkInfo(currentKey);
            indexIterator.remove();

            if (index.size() == 0) {
                try {
                    fileRWAccess.truncate(0);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                usedSize = 0;
            } else {
                final int size = chi.getSize();
                usedSize -= size;
            }
        }
    }
}
