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

package org.netbeans.modules.cnd.repository.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.apt.utils.FilePathCache;
import org.netbeans.modules.cnd.repository.translator.RepositoryTranslatorImpl;


/**
 * Maps strings to integers and vice versa.
 * Used to make persustence storage more compact
 */

public class IntToStringCache {
    
    protected final List<String> cache;
    protected final int version;
    protected final long timestamp;
    
    public IntToStringCache() {
	cache = new ArrayList<String>();
        version = RepositoryTranslatorImpl.getVersion();
        timestamp = System.currentTimeMillis();
    }
    
    public IntToStringCache(DataInput stream) throws IOException {
	assert stream != null;
	
	cache = new ArrayList<String>();
        version = stream.readInt();

        timestamp = stream.readLong();
	
	int size = stream.readInt();
	
	for (int i = 0; i < size; i++) {
	    String value = FilePathCache.getString(stream.readUTF());
	    if (value.equals("")) {
		value = null;
	    }
	    cache.add(value);
	}
    }
    
	/*
	 * Persists the master index: unit name <-> integer index
	 *
	 */
    public void write(DataOutput stream) throws IOException {
	assert cache != null;
	assert stream != null;
        
        stream.writeInt(version);
        stream.writeLong(timestamp);
	
	int size = cache.size();
	stream.writeInt(size);
	
	for (int i = 0; i < size; i++) {
	    String value = cache.get(i);
	    if (value == null) {
		stream.writeUTF("");
	    } else {
		stream.writeUTF(value);
	    }
	}
    }
    
    public int getId(String value) {
        int id = cache.indexOf(value);
        if (id == -1) {
            synchronized (cache) {
                id = cache.indexOf(value);
                if (id == -1) {
                    id = makeId(value);
                }
            }
        }
        return id;
    }
    
    /**
     * synchronization is controlled by calling getId() method
     */
    protected int makeId(String value) {
	cache.add(value);
	return cache.indexOf(value);
    }
    
    public String getValueById(int id) {
	return cache.get(id);
    }
    
    public boolean containsId(int id) {
	return 0 <= id && id < cache.size();
    }
    
    public boolean containsValue (String value) {
        return cache.contains(value);
    }
    
    public int size () {
        return cache.size();
    }

    public int getVersion() {
        return version;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
}
