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

import java.io.*;
import java.util.*;

/**
 * Collects a simple statistics
 * @author Vladimir Kvashin
 */
public class RangeStatistics extends BaseStatistics<Integer> {


    protected int rangeCount;
    
    public RangeStatistics(String text, int level) {
	this(text, level, 10);
    }
    
    public RangeStatistics(String text, int level, int rangeCount) {
	super(text, level);
	this.rangeCount = rangeCount;
    }

    public void consume(int value) {
	consume(value, value);
    }
    
    public void print(PrintStream ps) {
	int avg = (cnt == 0) ? 0 : sum / cnt;
	ps.printf("%s %8d min    %8d max    %8d avg\n", text, min, max, avg);	// NOI18N
	if( values != null ) {
	    printDistribution(ps);
	}
    }
    
    protected void printDistribution(PrintStream ps) {
	ps.printf("\tDistribution:\n");	// NOI18N
	if( level > LEVEL_MEDIUM || values.size() <= rangeCount ) {
	    printDistributionDetailed(ps);
	}
	else {
	    printDistributionGrouped(ps);
	}
    }
    

    static private class Range { 

	/** range start (inclusive) */
	public int from;

	/** range start (inclusive) */
	public int to;

	/** count of values that are in range */
	public int cnt;

	public Range(int from, int to) {
	    this.from = from;
	    this.to = to;
	}
	
	public String toString() {
	    StringBuilder sb = new StringBuilder();
	    sb.append(from);
	    sb.append('-');
	    sb.append(to);
	    sb.append(": "); // NOI18N
	    sb.append(cnt);
	    return sb.toString();
	}
    }
    
    private Range[] ranges;
    
    private void createRanges() {
	
	int[] valuesArray = new int[values.size()];
	int pos = 0;
	for( Map.Entry<Integer, Integer> entry : values.entrySet() ) {
	    valuesArray[pos++] = entry.getKey();
	}

	ranges = new Range[rangeCount];
	int rangeSize = values.size() / rangeCount + ( values.size() % rangeCount == 0 ? 0 : 1 );
	
	for( int i = 0; i < ranges.length; i++ ) {
	    int from = Math.min(rangeSize*i, valuesArray.length-1);;
	    int to = Math.min(from + rangeSize - 1, valuesArray.length-1);
	    try {
		ranges[i] = new Range(valuesArray[from], valuesArray[to]);
	    }
	    catch( ArrayIndexOutOfBoundsException e ) {
		System.err.printf("i=%d from=%d to=%d valuesArray.length=%d\n", i, from, to, valuesArray.length);
		e.printStackTrace(System.err);
	    }
	    catch( Exception e ) {
		e.printStackTrace(System.err);
	    }
	}
    }
    
    private Range getRange(int value) {
	if( ranges == null ) {
	    createRanges();
	}
	for (int i = 0; i < ranges.length; i++) {
	    if( ranges[i].from <= value && value <= ranges[i].to ) {
		return ranges[i];
	    }
	}
	throw new IllegalArgumentException("Value " + value + " are out of range " + min + '-' + max); // NOI18N
    }
    
    private void printDistributionGrouped(PrintStream ps) {
	
	for( Map.Entry<Integer, Integer> entry : values.entrySet() ) {
	    Range range = getRange(entry.getKey());
	    range.cnt += entry.getValue();
	}

	int maxFrom = 0, maxTo = 0, maxCnt = 0;
	for (int i = 0; i < ranges.length; i++) {
	    maxFrom = Math.max(maxCnt, ranges[i].from);
	    maxTo = Math.max(maxCnt, ranges[i].to);
	    maxCnt = Math.max(maxCnt, ranges[i].cnt);
	}
	maxFrom = (int) Math.log10(maxFrom) + 1;
	maxTo = (int) Math.log10(maxTo) + 1;
	maxCnt = (int) Math.log10(maxCnt) + 1;
	
	StringBuilder format = new StringBuilder("\t%"); // NOI18N
	format.append(maxFrom);
	format.append("d - %"); // NOI18N
	format.append(maxTo);
	format.append("d   %"); // NOI18N
	format.append(maxCnt);
	format.append("d   %2d%%\n"); // NOI18N

	
	for (int i = 0; i < ranges.length; i++) {
	    if( ranges[i].cnt > 0 ) {
		//ps.printf("\t%8d - %8d %8d\n", ranges[i].from, ranges[i].to, ranges[i].cnt);	// NOI18N
		int percent =  ranges[i].cnt*100/this.cnt;
		ps.printf(format.toString(), ranges[i].from, ranges[i].to, ranges[i].cnt, percent);	// NOI18N
	    }
	}
    }
}
