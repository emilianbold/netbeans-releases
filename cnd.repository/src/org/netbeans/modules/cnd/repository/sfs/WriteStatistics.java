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

import org.netbeans.modules.cnd.repository.testbench.Stats;

/**
 * A class for gathering recent write statistics
 * @author Vladimir Kvashin
 */
public class WriteStatistics {
    
    private static final WriteStatistics instance = new WriteStatistics();
    
    /** Private constructor to prevent external creation */
    private WriteStatistics() {
    }
    
    public static final WriteStatistics instance() {
	return instance;
    }
    
    private long writeStatIntervalStart = 0;
    private int writeStatInterval = 1000;
    private int writeCount = 0;
    //private int[] writesPerInterval = new int[10];
    private long totalWriteCount = 0;

    public void update(int increment) {
	totalWriteCount += increment;
	long currTime = System.currentTimeMillis();
	if( this.writeStatIntervalStart == 0 ) {	// called first time
	    writeCount = increment;
	    writeStatIntervalStart = System.currentTimeMillis();
	} 
	else if( currTime - writeStatIntervalStart < writeStatInterval ) {
	    writeCount += increment;
	} 
	else {
	    int currentWPS = (int) (1000L * writeCount / (currTime - writeStatIntervalStart));
	    writeStatIntervalStart = currTime;
//	    for( int i = 1; i < writesPerInterval.length; i++ ) {
//		writesPerInterval[i-1] = writesPerInterval[i];
//	    }
//	    writesPerInterval[writesPerInterval.length-1] = currentWPS;
	    if( Stats.writeStatistics ) {
//		System.err.printf("Write statistics\n");
//		for (int i = 0; i < writesPerInterval.length; i++) {
//		    System.err.printf("\t%s %d WPS\n", baseFile.getName(), writesPerInterval[i]);
//		}
		System.err.printf("\tcurrent writes: %4d current WPS: %4d  total writes: %8d \n", 
			writeCount, currentWPS, totalWriteCount);
	    }
	    writeCount = increment;
	}
    }
}
