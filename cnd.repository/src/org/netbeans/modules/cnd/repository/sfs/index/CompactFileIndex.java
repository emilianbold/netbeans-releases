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

package org.netbeans.modules.cnd.repository.sfs.index;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.support.KeyFactory;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.repository.util.LongHashMap;
import org.netbeans.modules.cnd.repository.util.SlicedLongHashMap;

/**
 * LongHashMap based implementation of FileIndex
 * @author Vladimir Kvashin
 */
public class CompactFileIndex implements FileIndex, SelfPersistent {
    private static final int shift = 37;
    private static final long mask = (1L<<shift)-1;
    private static final int DEFAULT_SLICE_CAPACITY;
    private static final int DEFAULT_SLICE_COUNT;
    static {
        int nrProc = Runtime.getRuntime().availableProcessors();
        if (nrProc <= 4) {
            DEFAULT_SLICE_COUNT = 32;
            DEFAULT_SLICE_CAPACITY = 512;
        } else {
            DEFAULT_SLICE_COUNT = 128;
            DEFAULT_SLICE_CAPACITY = 128;
        }
    }
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    private final SlicedLongHashMap<Key> map = new SlicedLongHashMap<Key>(DEFAULT_SLICE_COUNT, DEFAULT_SLICE_CAPACITY);
    
    public CompactFileIndex () {
    }
    
    public CompactFileIndex (final DataInput input ) throws IOException {
        
        assert input != null;
        
        final int size = input.readInt();
        
        for (int i = 0; i < size; i++) {
            map.put(KeyFactory.getDefaultFactory().readKey(input),
                    input.readLong());
        }
    }
    
    public int size() {
        try {
            lock.readLock().lock();
            return map.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public Collection<Key> keySet() {
        try {
            lock.readLock().lock();
            return map.keySet();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public Iterator<Key> getKeySetIterator() {
        return keySet().iterator();
    }
    
    public int remove(final Key key) {
        long data = LongHashMap.NO_VALUE;
        try {
            lock.writeLock().lock();
            data = map.remove(key);
        } finally {
            lock.writeLock().unlock();
        }
        return (data == LongHashMap.NO_VALUE) ? 0 : convertToSize(data);
    }

    public int put(final Key key, final long offset, final int size) {
        long data = LongHashMap.NO_VALUE;
        try {
            lock.writeLock().lock();
            data = map.put(key, convertToLongData(offset, size));
        } finally {
            lock.writeLock().unlock();
        }
	return (data == LongHashMap.NO_VALUE) ? 0 : convertToSize(data);
    }

    
    private static long convertToLongData(final long offset, final int size) {
	assert(offset <= mask) : "Offset " + offset + " is too large";
	assert(size < (1 << (64-shift))) : "Size " + size + " is too large";
	long data = size;
	data <<= shift;
	data |= (offset & mask);
	return data;
    }
    
    private static final int convertToSize(final long data) {
	final int size = (int) (data >>> shift);
	return size;
    }

    private static final long convertToOffset(final long data) {
	final long offset = data & mask;
	return offset;
    }
    

    public ChunkInfo get(final Key key) {
	final long entry = map.get(key);
	return (entry == LongHashMap.NO_VALUE) ? null : new LongChunkInfo(entry);
    }

    public void write(final DataOutput output) throws IOException {
        final Collection<LongHashMap.Entry<Key>> collection = map.entrySet();
        output.writeInt(collection.size());
        for(LongHashMap.Entry<Key> entry : collection) {
            KeyFactory.getDefaultFactory().writeKey(entry.getKey(), output);
            output.writeLong(entry.getValue());
        }
    }

    private static class LongChunkInfo implements ChunkInfo, Comparable, SelfPersistent {
	long entry;

	public LongChunkInfo(final long entry) {
	    this.entry = entry;
	}
	
	public int getSize() {
	    return convertToSize(entry);
	}
	
	public long getOffset() {
	    return convertToOffset(entry);
	}
	
	public int compareTo(final Object o) {
	    if (o instanceof ChunkInfo) {
		return (this.getOffset()  < ((ChunkInfo) o).getOffset()) ? -1 : 1;
	    }
	    return 1;
	}
	
	public void setOffset(final long offset) {
	    entry = convertToLongData(offset, getSize());
	}
	
        @Override
	public String toString() {
	    final Formatter f = new Formatter();
            long offset = getOffset();
	    f.format("ChunkInfo [offset=%d (%H) size=%d long=%d]", offset, offset, getSize(), entry); // NOI18N
	    return f.toString();
	}

        public void write(final DataOutput output) throws IOException {
            output.writeLong(entry);
        }
    }
}
