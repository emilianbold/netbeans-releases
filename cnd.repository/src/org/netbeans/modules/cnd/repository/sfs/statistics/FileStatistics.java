/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.repository.sfs.statistics;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.testbench.Stats;

/**
 * Responsible for collecting file-level statistics
 * @author Vladimir Kvashin
 */
public class FileStatistics {
    
    private static class ChunkStatistics {
	public int readCount;
	public int writeCount;
	public int resized;
    }
    
    private Map<Key, ChunkStatistics> map;
    
    public FileStatistics() {
	if( Stats.fileStatisticsLevel > 0 ) {
	    map = new HashMap<Key, ChunkStatistics>();
	}
    }
    
    private ChunkStatistics getStat(Key key) {
	ChunkStatistics stat = map.get(key);
	if( stat == null ) {
	    stat = new ChunkStatistics();
	    map.put(key, stat);
	}
	return stat;
    }
    
    public int getReadCount(Key key) {
	return (Stats.fileStatisticsLevel == 0) ? 0 : getStat(key).readCount;
    }
    
    public void incrementReadCount(Key key) {
	if( Stats.fileStatisticsLevel > 0 ) {
	    getStat(key).readCount++;
	}
    }
    
    public int getWriteCount(Key key) {
	return (Stats.fileStatisticsLevel == 0) ? 0 : getStat(key).writeCount;
    }
    
    public void incrementWriteCount(Key key, int oldSize, int newSize) {
	if( Stats.fileStatisticsLevel > 0 ) {
	    ChunkStatistics  stat = getStat(key);
	    stat.writeCount++;
	    if( oldSize > 0 && newSize != oldSize ) {
		stat.resized++;
	    }
	}
    }
    
    public void removeNotify(Key key) {
	if( Stats.fileStatisticsLevel > 0 ) {
	    map.remove(key);
	}
    }
}

