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

package org.netbeans.modules.cnd.repository.sfs;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.testbench.Stats;

/**
 *
 * @author Vladimir Kvashin
 */
public abstract class FileStorage {
    
    public static FileStorage create(String path) throws IOException {
	return  Stats.doubleFileStorage ? new DoubleFileStorage(path) : new SingleFileStorage(path);
    }
    
     public static FileStorage create(File file) throws IOException {
	return  Stats.doubleFileStorage ? new DoubleFileStorage(file) : new SingleFileStorage(file);
    }
    
    abstract public void close() throws IOException;
    
    abstract public void defragment() throws IOException;
    
    abstract public boolean defragment(long timeout) throws IOException;
    
    abstract public void dump(PrintStream ps) throws IOException;
    
    abstract public void dumpSummary(PrintStream ps) throws IOException;
    
    abstract public Persistent get(Key key) throws IOException;
    
    abstract public int getFragmentationPercentage() throws IOException;
    
    abstract public void put(Key key, Persistent object) throws IOException;
    
    abstract public void remove(Key key) throws IOException;
    
    abstract public long getSize() throws IOException;

    abstract public int getObjectsCount();
    
}
